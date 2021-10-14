package co.tecno.sersoluciones.analityco.ui.createPersonal

import android.content.Context
import android.database.Cursor
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import co.com.sersoluciones.facedetectorser.utilities.DebugLog.log
import co.com.sersoluciones.facedetectorser.utilities.DebugLog.logW
import co.tecno.sersoluciones.analityco.ApplicationContext
import co.tecno.sersoluciones.analityco.databases.DBHelper
import co.tecno.sersoluciones.analityco.databinding.FragmentNavSelectContractBinding
import co.tecno.sersoluciones.analityco.models.ContractEnrollment
import co.tecno.sersoluciones.analityco.models.ObjectList
import co.tecno.sersoluciones.analityco.nav.CreatePersonalViewModel
import co.tecno.sersoluciones.analityco.ui.createPersonal.adapters.DataItemListAdapter
import co.tecno.sersoluciones.analityco.utilities.Constantes
import co.tecno.sersoluciones.analityco.utilities.Utils
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.json.JSONArray
import javax.inject.Inject


class SelectNavContractFragment : Fragment() {

    private lateinit var binding: FragmentNavSelectContractBinding
    private var ListFilter: ArrayList<ContractEnrollment>? = null

    @Inject
    lateinit var viewModel: CreatePersonalViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentNavSelectContractBinding.inflate(inflater)
        binding.lifecycleOwner = this

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel.fetchContractsByPersonal()
        ListFilter = ArrayList()
        (requireActivity() as AppCompatActivity).supportActionBar?.title = "CC ${viewModel.personal.value!!.DocumentNumber}"
        (requireActivity() as AppCompatActivity).supportActionBar?.subtitle =
                "${viewModel.personal.value!!.Name} ${viewModel.personal.value!!.LastName}"

        viewModel.contracts.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            it?.let {
                updateLists(it.toMutableList(), viewModel.selectedProject.value!!.Id)
            }
        })

        viewModel.navigateToJoinContractFragment.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            it?.let {
                findNavController()
                        .navigate(
                                SelectNavContractFragmentDirections.actionSelectNavContractFragmentToJoinPersonalNavContractFragment(
                                        viewModel.contract.value!!,
                                        viewModel.personalContract.value
                                )
                        )
                viewModel.doneNavigatingSelectContract()
            }
        })

        viewModel.selectedProject.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            it?.let {
                if (viewModel.contracts.value != null)
                    updateLists(viewModel.contracts.value!!.toMutableList(), it.Id)
            }
        })

        binding.fabBack.setOnClickListener {
            findNavController()
                    .navigate(
                            SelectNavContractFragmentDirections.actionSelectNavContractFragmentToDetailsPersonalFragment(viewModel.personal.value!!)
                    )
        }

    }
    private fun updateLists(contractList: MutableList<ContractEnrollment>,  projectId: String) {

        var jsonArrayContracts = JSONArray()
        val contractNumbersExcept = ArrayList<String>()
        if (contractList.size > 0) {
            for (contractEnrollment in contractList) {
                contractNumbersExcept.add(contractEnrollment.ContractNumber!!)
            }
            getCursorContracts(contractNumbersExcept, projectId = projectId)?.use {
                jsonArrayContracts = Utils.cursorToJArray(it)
            }

        } else {
            getCursorContracts(projectId = projectId)?.use {
                jsonArrayContracts = Utils.cursorToJArray(it)
            }
        }
        val contractPrincipalList = Gson().fromJson<ArrayList<ContractEnrollment>>(
            jsonArrayContracts.toString(),
            object : TypeToken<ArrayList<ContractEnrollment>>() {
            }.type
        )
        if (viewModel.typeContract!!.Value == "AD" || viewModel.typeContract!!.Value == "AS") {
            val itr = contractPrincipalList.iterator()
            while (itr.hasNext()) {
                val contractEnrollment = itr.next()
                if (!contractEnrollment.IsRegister && contractEnrollment.CountPersonal > 0) {
                    itr.remove()
                }
            }
        }
        ListFilter = Gson().fromJson<ArrayList<ContractEnrollment>>(
                jsonArrayContracts.toString(),
                object : TypeToken<ArrayList<ContractEnrollment>>() {
                }.type
        )
        if (viewModel.typeContract!!.Value == "AD" || viewModel.typeContract!!.Value == "AS") {
            val itr = ListFilter!!.iterator()
            while (itr.hasNext()) {
                val contractEnrollment = itr.next()
                if (!contractEnrollment.IsRegister && contractEnrollment.CountPersonal > 0) {
                    itr.remove()
                }
            }
        }

        val adapter = DataItemListAdapter(DataItemListAdapter.OnClickListener {
                val contract = (it as ContractEnrollment)
                viewModel.navigateToJoinPersonal(contract,false)
            })
            for (contract in contractPrincipalList) {
                contract.isJoin = false
                contract.ContractType = viewModel.typeContract?.Value
            }
            adapter.addSubmitList(contractPrincipalList)

            binding.contracts.adapter = adapter

            updateListJoinContract(contractList, projectId)

            if (contractPrincipalList.size == 1 && contractPrincipalList[0].IsActive) {
                val contract = contractPrincipalList[0]
                contract.isJoin = false
                contract.ContractType = viewModel.typeContract?.Value
//            if (viewModel.externalData)
//                viewModel.navigateToJoinPersonal(contract)
            }
        if(contractPrincipalList.size==1 && contractList.size==0) {
            val contract = contractPrincipalList[0]
            viewModel.navigateToJoinPersonal(contract,true)
        }
    }

    private fun getCursorContracts(projectId: String = "", contractId: String = ""): Cursor? {

        val item = String.format("%s", viewModel.typeContract!!.Id)

        val arrayList: ArrayList<String> = ArrayList()
        arrayList.add(item)
        var selection = "(" + DBHelper.CONTRACT_TABLE_COLUMN_ACTIVE + " = 1 AND " +
                DBHelper.CONTRACT_TABLE_COLUMN_CONTRACT_TYPE_ID + " = ? "

        if (contractId.isNotEmpty()) {
            selection += " AND " + DBHelper.CONTRACT_TABLE_COLUMN_SERVER_ID + " = ? "
            arrayList.add(contractId)
        }

        if (projectId.isNotEmpty()) {
            selection += " AND " + DBHelper.CONTRACT_TABLE_COLUMN_PROJECT_IDS + " like ? "
            arrayList.add("%$projectId%")
        }

        selection += " AND " + DBHelper.CONTRACT_TABLE_COLUMN_PROJECT_IDS + " != \"\" )"
        //        if (contractType.Value.equals("AD") || contractType.Value.equals("AS"))
        //            selection += " AND " + DBHelper.CONTRACT_TABLE_COLUMN_COUNT_PERSONAL + " = 0 ";

        val selectionArgs = arrayList.toTypedArray()

        return requireActivity().contentResolver.query(
            Constantes.CONTENT_CONTRACT_URI, null,
            selection, selectionArgs, null
        )
    }


    private fun getCursorContracts(contractsExceptArray: ArrayList<String>, projectId: String = "", contractId: String = ""): Cursor? {

        var selection = "(" + DBHelper.CONTRACT_TABLE_COLUMN_ACTIVE + " = 1 AND " +
                DBHelper.CONTRACT_TABLE_COLUMN_CONTRACT_TYPE_ID + " = ? "

        //        if (contractType.Value.equals("AD") || contractType.Value.equals("AS"))
        //            selection += " AND " + DBHelper.CONTRACT_TABLE_COLUMN_COUNT_PERSONAL + " = 0 ";

        val item = String.format("%s", viewModel.typeContract!!.Id)
        //arrayList.add(0, "1");
        val arrayList = ArrayList<String>()
        arrayList.add(0, item)

        if (contractId.isNotEmpty()) {
            selection += " AND " + DBHelper.CONTRACT_TABLE_COLUMN_SERVER_ID + " = ? "
            arrayList.add(1, contractId)
        }

        if (projectId.isNotEmpty()) {
            selection += (" AND " + DBHelper.CONTRACT_TABLE_COLUMN_PROJECT_IDS + " like ? ")
            arrayList.add(1, "%$projectId%")
        }

        selection += (" AND " + DBHelper.CONTRACT_TABLE_COLUMN_PROJECT_IDS + " != \"\" AND "
                + DBHelper.CONTRACT_TABLE_COLUMN_CONTRACT_NUMBER + " not in(" + Utils.makePlaceholders(contractsExceptArray.size) + ")"
                + ")")
        arrayList.addAll(contractsExceptArray)
        logW("Query $selection")
        val selectionArgs = arrayList.toTypedArray()
        for (select in selectionArgs) {
            log("select: $select")
        }
        return requireActivity().contentResolver.query(
            Constantes.CONTENT_CONTRACT_URI, null,
            selection, selectionArgs, null
        )
    }

    private fun updateListJoinContract(contractList: MutableList<ContractEnrollment>, projectId: String, contractId: String = "") {

        binding.layoutContractJoin.visibility = View.GONE
        if (contractList.size > 0) {
            binding.layoutContractJoin.visibility = View.VISIBLE
        } else return

        if (projectId.isNotEmpty()) {
            val it = contractList.iterator()
            while (it.hasNext()) {
                if (!it.next().ProjectIds!!.contains(projectId)) {
                    it.remove()
                }
            }
        }

        if (contractList.size == 1 && contractId.isNotEmpty() && contractList[0].IsActive) {
        }

        for (contract in contractList) {
            contract.isJoin = true
            contract.ContractType = viewModel.typeContract?.Value
            contract.ContractorName = contract.EmployerName
        }
        val adapter = DataItemListAdapter(DataItemListAdapter.OnClickListener {
            val contract = (it as ContractEnrollment)
            if (!contract.IsActive) {
                Toast.makeText(activity, "Este empleado se encuentra inactivo en ese contrato.", Toast.LENGTH_SHORT).show()
            }
            viewModel.fetchPersonalContract(contract)
        })
        adapter.addSubmitList(contractList)
        binding.contractsJoin.adapter = adapter
    }

    private fun getCursorContractsJoin(arrayList: ArrayList<String>, projectId: String): Cursor? {

//            contractType = (activity as PersonalWizardActivity).contractType
        val selection = ("("
                + DBHelper.CONTRACT_TABLE_COLUMN_PROJECT_IDS + " like ? AND "
                //+ DBHelper.CONTRACT_TABLE_COLUMN_ACTIVE + " = ? AND "
                + DBHelper.CONTRACT_TABLE_COLUMN_CONTRACT_NUMBER + " in (" + Utils.makePlaceholders(arrayList.size) + ")"
                + ")")

        arrayList.add(0, "%$projectId%")
        //arrayList.add(1, "1");

        //String item = String.format("%s", contractType.Id);
        val selectionArgs = arrayList.toTypedArray()
        return requireActivity().contentResolver.query(
            Constantes.CONTENT_CONTRACT_URI, null,
            selection, selectionArgs, null
        )
    }
    override fun onAttach(context: Context) {
        super.onAttach(context)
        ApplicationContext.analitycoComponent.inject(this)
    }
    fun Filtro(item : String){
        val filtrarList: ArrayList<ContractEnrollment> = ArrayList<ContractEnrollment>()
        for (contract in ListFilter!!){
            if(contract.ContractReview!!.toLowerCase().contains(item) || contract.ContractorName!!.toLowerCase().contains(item)){
                filtrarList.add(contract)
            }
        }
        val adapter = DataItemListAdapter(DataItemListAdapter.OnClickListener {
            val contract = (it as ContractEnrollment)
            viewModel.navigateToJoinPersonal(contract,false)
        })
        for (contract in filtrarList) {
            contract.isJoin = false
            contract.ContractType = viewModel.typeContract?.Value
        }
        adapter.addSubmitList(filtrarList)
        binding.contracts.adapter = adapter
    }
}

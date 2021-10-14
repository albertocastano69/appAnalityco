package co.tecno.sersoluciones.analityco

import android.content.Context
import android.database.Cursor
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import co.com.sersoluciones.barcodedetectorser.utilities.DebugLog
import co.tecno.sersoluciones.analityco.adapters.ContractButtons
import co.tecno.sersoluciones.analityco.adapters.ContractButtonsAdapter
import co.tecno.sersoluciones.analityco.databases.DBHelper
import co.tecno.sersoluciones.analityco.databinding.ContractTypeBinding
import co.tecno.sersoluciones.analityco.models.ContractType
import co.tecno.sersoluciones.analityco.models.ProjectList
import co.tecno.sersoluciones.analityco.models.User
import co.tecno.sersoluciones.analityco.nav.CreateCarViewModel
import co.tecno.sersoluciones.analityco.ui.createPersonal.ContractTypeFragmentArgs
import co.tecno.sersoluciones.analityco.ui.createPersonal.ContractTypeFragmentDirections
import co.tecno.sersoluciones.analityco.utilities.Constantes
import co.tecno.sersoluciones.analityco.utilities.MyPreferences
import co.tecno.sersoluciones.analityco.utilities.Utils
import com.google.gson.Gson
import javax.inject.Inject

class ContractTypeFragmentCar : Fragment() {

    private lateinit var binding: ContractTypeBinding
    private var adapter: ContractButtonsAdapter? = null
    var claims = mutableListOf<String>()
    var data = mutableListOf<ContractButtons>()

    @Inject
    lateinit var viewModel: CreateCarViewModel

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        binding = ContractTypeBinding.inflate(inflater)
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (requireActivity() as AppCompatActivity).supportActionBar?.title = "Enrolar Personal"
        (requireActivity() as AppCompatActivity).supportActionBar?.subtitle = ""

        adapter = ContractButtonsAdapter(ContractButtonsAdapter.OnClickListener {
            selectContractTyp(it.type)
        })

        val arguments = ContractTypeFragmentArgs.fromBundle(requireArguments())
        val projectList = arguments.project
        DebugLog.logW(Gson().toJson(projectList))
        permissions(projectList)

        viewModel.selectedProject.observe(viewLifecycleOwner, Observer {
            it?.let {
                permissions(it)
            }
        })

        binding.fabBack.setOnClickListener { findNavController().popBackStack() }

        if (data.size == 1) {
            Handler().postDelayed({
                selectContractTyp(data.first().type)
            }, 200)
        }
    }

    private fun selectContractTyp(type: String) {
        val cursor = getCursor(type)
        if (cursor != null && cursor.count > 0) {
            cursor.moveToFirst()
            val typeStr = Utils.cursorToJObject(cursor).toString()
            val contractType = Gson().fromJson(typeStr, ContractType::class.java)
            viewModel.typeContract = contractType
            findNavController()
                    .navigate(
                            ContractTypeFragmentDirections.actionContractTypeFragmentToScanFragmnet()
                    )
        }
    }
    private fun getCursor(str: String): Cursor? {
        val selection = ("(" + DBHelper.COPTIONS_COLUMN_TYPE + " LIKE ? "
                + " AND " + DBHelper.COPTIONS_COLUMN_VALUE + " LIKE ? "
                + ")")
        val item = String.format("%s", str)
        val selectionArgs = arrayOf("ContractType", item)
        return requireActivity().contentResolver.query(
                Constantes.CONTENT_COMMON_OPTIONS_URI, null,
                selection, selectionArgs, null
        )
    }

    private fun permissions(projectList: ProjectList? = null) {

        val preferences = MyPreferences(requireActivity())
        val profile = preferences.profile
        val user = Gson().fromJson(profile, User::class.java)

        if (!user.IsSuperUser) {
            if (user.Companies.size == 1 || user.IsAdmin) {
                claims = user.claims
            } else {
                for (comp in user.Companies) {
                    if (projectList != null && comp.Id == projectList.CompanyInfoId) {
                        claims = comp.Permissions
                        break
                    }
                }

            }
        }
        validate(claims, projectList = projectList)
    }

    private fun validate(permissions: MutableList<String>, projectList: ProjectList? = null) {
        if (permissions.isEmpty()) return
        data.clear()
        if (permissions.contains("personalsad.add"))
            data.add(ContractButtons(getString(R.string.vehiculo_administrativo), "AD", R.drawable.ic_02_administrativotrasparente2))
        if (permissions.contains("personalsfu.add"))
            data.add(ContractButtons(getString(R.string.vehiculo_funcionario), "FU", R.drawable.ic_14_funcionariotrasparente))
        if (permissions.contains("personalspr.add"))
            data.add(ContractButtons(getString(R.string.vehiculo_proveedor), "PR", R.drawable.ic_10_proveedor_trasparente))
        if (permissions.contains("personalsas.add")) {
            val buttonAs = ContractButtons(getString(R.string.vehiculo_asociado), "AS", R.drawable.ic_04_asociado_trasparente2)
            if (projectList?.JoinCompaniesCount == 0) buttonAs.enable = false
            data.add(buttonAs)
        }
        if (permissions.contains("personalsco.add"))
            data.add(ContractButtons(getString(R.string.vehiculo_contratista), "CO", R.drawable.ic_06_contratista_trasparente))
        if (permissions.contains("personalsvi.add"))
            data.add(ContractButtons(getString(R.string.vehiculo_visitante), "VI", R.drawable.ic_16_visitante_trasparente))
        if (permissions.contains("personalsot.add"))
            data.add(ContractButtons(getString(R.string.vehiculo_otro), "OT", R.drawable.ic_18_otros_trasparente))
        adapter?.data = data
        binding.buttonsGrid.adapter = adapter
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        ApplicationContext.analitycoComponent.inject(this)
    }
}
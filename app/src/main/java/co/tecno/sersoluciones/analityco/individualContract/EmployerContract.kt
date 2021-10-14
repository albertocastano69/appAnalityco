package co.tecno.sersoluciones.analityco.individualContract

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.content.IntentFilter
import android.graphics.drawable.PictureDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.fragment.findNavController
import co.tecno.sersoluciones.analityco.ApplicationContext
import co.tecno.sersoluciones.analityco.GlideApp
import co.tecno.sersoluciones.analityco.R
import co.tecno.sersoluciones.analityco.adapters.EmployerIndividualContractRecyclerViewAdapter
import co.tecno.sersoluciones.analityco.callback.OnListEmployerInteractionListener
import co.tecno.sersoluciones.analityco.databinding.FragmentEmployerContractBinding
import co.tecno.sersoluciones.analityco.models.CompanyList
import co.tecno.sersoluciones.analityco.models.ObjectList
import co.tecno.sersoluciones.analityco.nav.CreatePersonalViewModel
import co.tecno.sersoluciones.analityco.receivers.RequestBroadcastReceiver
import co.tecno.sersoluciones.analityco.services.CRUDService
import co.tecno.sersoluciones.analityco.services.CrudIntentService
import co.tecno.sersoluciones.analityco.ui.createPersonal.adapters.DataItemListAdapter
import co.tecno.sersoluciones.analityco.ui.createPersonal.viewmodels.PersonalViewModel
import co.tecno.sersoluciones.analityco.utilities.Constantes
import com.android.volley.Request
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import org.json.JSONArray
import java.util.*
import java.util.regex.Pattern
import javax.inject.Inject

class EmployerContract: Fragment(), RequestBroadcastReceiver.BroadcastListener {

    private var mValues: ArrayList<ObjectList>? = null
    private var adapter: EmployerIndividualContractRecyclerViewAdapter? = null
    private var employerId: String? = null
    private var requestBroadcastReceiver: RequestBroadcastReceiver? = null

    @Inject
    lateinit var viewModel: CreatePersonalViewModel

    @Inject
    lateinit var personalViewModel: PersonalViewModel

    private lateinit var binding: FragmentEmployerContractBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Handle the back button event
                alertClose()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
        mValues = ArrayList()
    }
    private fun alertClose() {
        val builder = MaterialAlertDialogBuilder(requireActivity())
            .setTitle("Atencion")
            .setMessage("¿Desea cancelar este proceso?")
            .setPositiveButton("Aceptar") { _, _ ->
                viewModel.clearForm()
                personalViewModel.clearData()
                activity?.finish()
            }
            .setNegativeButton("Cancelar", null)
        builder.create().show()
    }
    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        requestBroadcastReceiver = RequestBroadcastReceiver(this)
        binding = FragmentEmployerContractBinding.inflate(inflater)
        binding.lifecycleOwner = this
        return binding.root
    }
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (requireActivity() as AppCompatActivity).supportActionBar?.title = "Seleccionar empleador"
        (requireActivity() as AppCompatActivity).supportActionBar?.subtitle = ""

        //viewModel.verifyInitialDataInDB()
        updateListEmployers()
        binding.controlButtons.nextButton.setOnClickListener {
            findNavController()
                .navigate(
                        EmployerContractDirections.actionEmployerContractToScanFragmnet2()
                )
        }

        viewModel.initialData.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            it?.let {
                if (it) viewModel.getCompanies()
            }
        })
        val adapter = DataItemListAdapter(DataItemListAdapter.OnClickListener {
            val company = it as CompanyList
            viewModel.navigateToJoinProject(company)

        })
        viewModel.companies.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            it?.let {
                adapter.addSubmitList<CompanyList>(it)
            }
        })
        binding.searchEmployer.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            if (hasFocus){
                binding.employersList.visibility = View.VISIBLE
            }
        }
        binding.searchEmployer.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filter(s.toString())
            }

        })


        showProgress(true)
    }
    override fun onAttach(context: Context) {
        super.onAttach(context)
        ApplicationContext.analitycoComponent.inject(this)
    }
    override fun onResume() {
        super.onResume()
        val intentFilter = IntentFilter()
        intentFilter.addAction(CRUDService.ACTION_REQUEST_SAVE)
        intentFilter.addAction(CrudIntentService.ACTION_REQUEST)
        intentFilter.addAction(CrudIntentService.ACTION_REQUEST_POST)
        intentFilter.addAction(Constantes.BROADCAST_GET_JSON)
        LocalBroadcastManager.getInstance(requireActivity()).registerReceiver(
                requestBroadcastReceiver!!,
                intentFilter
        )
    }
    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(requireActivity()).unregisterReceiver(
                requestBroadcastReceiver!!
        )
    }

    private fun updateListEmployers() {
        CrudIntentService.startRequestCRUD(
                context, Constantes.EMPLOYER_FOR_INDIVIDUAL_CONTRACT_URL,
                Request.Method.GET, "", "", true, false
        )
    }

    private fun updateList(response: String?) {
        var jsonArray = JSONArray(response)
        mValues!!.clear()
        for (i in 0 until jsonArray.length()){
            val mItem =
                Gson().fromJson(
                        jsonArray.getJSONObject(i).toString(),
                        ObjectList::class.java
                )
            mValues!!.add(mItem)
        }
        if(mValues!!.size == 1){
            for(employer in mValues!!){
                viewModel.EmployerId = employer.CompanyId
                viewModel.ItemEmployer = employer
            }
            findNavController()
                    .navigate(
                            EmployerContractDirections.actionEmployerContractToScanFragmnet2()
                    )
        }

        adapter = EmployerIndividualContractRecyclerViewAdapter(
                requireActivity(),
                OnListEmployerInteractionListener { item, _ ->
                    item?.let {
                        fillData(item)
                    }
                },
                mValues!!
        )
        showProgress(false)
        binding.employersList.adapter = adapter


    }
    private fun fillData(item: ObjectList) {
        binding.employersList.visibility= View.GONE
        binding.LabelSearch.visibility = View.VISIBLE
        binding.searchEmployer.setText("")
        viewModel.ItemEmployer = item
        viewModel.EmployerId = item.CompanyId
        this.employerId = employerId
        binding.textName.text = item.Name
        binding.textAddress.text = String.format(
                "%s %s",
                item.DocumentType,
                item.DocumentNumber
        )
        binding.textValidity.text = item.Address
        binding.labelValidity.visibility = View.GONE
        binding.labelActive.visibility = View.GONE

        val url = Constantes.URL_IMAGES + item.Logo
        val format = url.split(Pattern.quote(".").toRegex()).toTypedArray()
        if (format[format.size - 1] == "svg") {
            GlideApp.with(requireActivity())
                    .`as`(PictureDrawable::class.java)
                    .apply(
                            RequestOptions()
                                    .placeholder(R.drawable.loading_animation)
                                    .error(R.drawable.image_not_available)
                    )
                    .fitCenter()
                    .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.RESOURCE))
                    .load(url)
                    .into(binding.logo)
            } else {
                Picasso.get().load(url)
                    .resize(0, 250)
                    .placeholder(R.drawable.image_not_available)
                    .error(R.drawable.image_not_available)
                    .into(binding.logo)
            }
        binding.cardViewDetail.visibility = View.VISIBLE
    }
    override fun onStringResult(action: String?, option: Int, Jsonresponse: String?, url: String?) {
        when (option) {
            Constantes.SEND_REQUEST -> {
                updateList(Jsonresponse)
            }
        }
    }
    private fun showProgress(show: Boolean) {
        val shortAnimTime = resources.getInteger(android.R.integer.config_shortAnimTime)
        binding.mProgressView.visibility = if (show) View.VISIBLE else View.GONE
        binding.mProgressView.animate().setDuration(shortAnimTime.toLong()).alpha(
                if (show) 1.toFloat() else 0.toFloat()
        ).setListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                binding.mProgressView.visibility = if (show) View.VISIBLE else View.GONE
            }
        })
    }
    private fun filter(TextSearch: String) {
        val filtrarlista: ArrayList<ObjectList> = ArrayList<ObjectList>()
        for (model : ObjectList in mValues!!) {

              if(model.Name != null){
                  if(model.Name.toLowerCase().contains(TextSearch.toLowerCase())) filtrarlista.add(model)
                  println("Lisrt${model.Name}")
              }

        }
        adapter!!.filtar(filtrarlista)
    }
}
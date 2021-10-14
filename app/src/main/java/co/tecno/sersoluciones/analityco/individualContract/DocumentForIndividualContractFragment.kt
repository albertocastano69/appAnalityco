package co.tecno.sersoluciones.analityco.individualContract

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.DatePicker
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import co.com.sersoluciones.barcodedetectorser.utilities.DebugLog
import co.com.sersoluciones.facedetectorser.FaceTrackerActivity
import co.com.sersoluciones.facedetectorser.serlibrary.PhotoSer
import co.tecno.sersoluciones.analityco.ApplicationContext
import co.tecno.sersoluciones.analityco.R
import co.tecno.sersoluciones.analityco.adapters.AdapterDocumentsRecyclerView
import co.tecno.sersoluciones.analityco.adapters.RequerimentIndividualContractAdapter
import co.tecno.sersoluciones.analityco.databinding.DocumentIndividualContractFragmentBinding
import co.tecno.sersoluciones.analityco.models.RequerimentIndividualContract
import co.tecno.sersoluciones.analityco.nav.CreatePersonalViewModel
import co.tecno.sersoluciones.analityco.nav.SecurityReference
import co.tecno.sersoluciones.analityco.receivers.RequestBroadcastReceiver
import co.tecno.sersoluciones.analityco.services.CRUDService
import co.tecno.sersoluciones.analityco.services.CrudIntentService
import co.tecno.sersoluciones.analityco.ui.createPersonal.viewmodels.PersonalViewModel
import co.tecno.sersoluciones.analityco.utilities.Constantes
import com.android.volley.Request
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.Gson
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import java.util.function.Predicate
import javax.inject.Inject
import kotlin.collections.ArrayList

class DocumentForIndividualContractFragment: Fragment(), RequestBroadcastReceiver.BroadcastListener, DatePickerDialog.OnDateSetListener, AdapterDocumentsRecyclerView.OnDocumentIndivudualContractListener, RequerimentIndividualContractAdapter.OnDocumentIndivudualContractListenerEdit {

    private lateinit var binding: DocumentIndividualContractFragmentBinding
    private var requestBroadcastReceiver: RequestBroadcastReceiver? = null
    private var mValues:ArrayList<RequerimentIndividualContract>? = null
    private var requiered:ArrayList<RequerimentIndividualContract>? = null
    private var Adapter : AdapterDocumentsRecyclerView? = null
    private var AdapterRequeried : RequerimentIndividualContractAdapter? = null
    private var IdContract: String? = null
    private var IdRequeriment: String? = null
    private var positionArray = 0
    private var mImageUri: Uri? = null
    private var fromDateStr: String? = null
    private var fromDate: Date? = null
    private var fromDatePickerDialog: DatePickerDialog? = null
    private var epsId = 0
    private var afpId = 0
    private var DocumentIsEps = false
    private var DocumentIsAfps = false
    private var DocumentRequeriedDate  = false
    private var DocumentNoRequeried  = false
    private var AmountDocuments = 0
    private var DocumentsUpload = 0
    private var CompletedDocumets = false
    private var Nationality : String = ""
    var IsUpload = false

    @Inject
    lateinit var createContractViewModel: CreatePersonalViewModel
    @Inject
    lateinit var viewModel: PersonalViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                alertClose()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }
    private fun alertClose() {
        val builder = MaterialAlertDialogBuilder(requireActivity())
                .setTitle("Atencion")
                .setMessage("¿Desea cancelar este proceso?")
                .setPositiveButton("Aceptar") { _, _ ->
                    viewModel.clearData()
                    createContractViewModel.clearForm()
                    activity?.finish()
                }
                .setNegativeButton("Cancelar", null)
        builder.create().show()
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DocumentIndividualContractFragmentBinding.inflate(inflater)
        binding.lifecycleOwner = this
        mValues = ArrayList()
        requiered = ArrayList()
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (requireActivity() as AppCompatActivity).supportActionBar?.title = "Documentos y requisitos"
        requestBroadcastReceiver = RequestBroadcastReceiver(this)
        val arguments = DocumentForIndividualContractFragmentArgs.fromBundle(requireArguments())
        IdContract = arguments.idContract
        Nationality = arguments.nationality

        showProgress(true)
        viewModel.secRefereces.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            val epsList = mutableListOf<SecurityReference>()
            val afpList = mutableListOf<SecurityReference>()
            it?.forEach { secReference ->
                when (secReference) {
                    is SecurityReference.EPS -> {
                        epsList.add(secReference)
                    }
                    is SecurityReference.AFP -> {
                        afpList.add(secReference)
                    }
                }
            }
            val epsAdapter = ArrayAdapter(requireContext(), R.layout.list_item, epsList)
            val afpAdapter = ArrayAdapter(requireContext(), R.layout.list_item, afpList)
            DebugLog.logW("eps count ${epsList.count()} afp count ${afpList.count()} total ${it.count()}")
            binding.epsAutoCompleteTextView.setAdapter(epsAdapter)
            binding.afpAutoCompleteTextView.setAdapter(afpAdapter)
        })
        binding.epsAutoCompleteTextView.apply {
            setOnItemClickListener { parent, view, position, id ->
                val item = parent.adapter.getItem(position) as SecurityReference.EPS
                epsId = item.item.Id
                DebugLog.logW("eps: ${item.item.Name}}")
            }

            addTextChangedListener {
                it?.let {
                    if (it.isEmpty()) {
                        binding.epsInputLayout.endIconMode = TextInputLayout.END_ICON_DROPDOWN_MENU
                    } else binding.epsInputLayout.endIconMode = TextInputLayout.END_ICON_CLEAR_TEXT
                }

            }
        }

        binding.afpAutoCompleteTextView.apply {
            setOnItemClickListener { parent, view, position, id ->
                val item = parent.adapter.getItem(position) as SecurityReference.AFP
                afpId = item.item.Id
                DebugLog.logW("afp: ${item.item.Name}}")
            }
            addTextChangedListener {
                it?.let {
                    if (it.isEmpty()) {
                        binding.afpInputLayout.endIconMode = TextInputLayout.END_ICON_DROPDOWN_MENU
                    } else binding.afpInputLayout.endIconMode = TextInputLayout.END_ICON_CLEAR_TEXT
                }

            }
        }
        CRUDService.startRequest(
                activity, Constantes.INDIVIDUAL_DOCUMENT_CONTRACT_URL,
                Request.Method.GET, "", false
        )


        binding.iconFile.setOnClickListener {
            startFaceDectectorActivity()
        }
        binding.positiveButton.setOnClickListener {
            updateImage()
        }
        binding.negativeButton.setOnClickListener {
            binding.recyclerRequirements.visibility = View.VISIBLE
            binding.recyclerRequirementscompliments.visibility = View.VISIBLE
            binding.controlButtons.nextButton.visibility = View.VISIBLE
            binding.controlButtons.backButton.visibility = View.VISIBLE
            binding.editRequirement.visibility = View.GONE
        }
        binding.fromDateBtn.setOnClickListener {
            fromDatePickerDialog!!.datePicker.tag = R.id.fromDateBtn

            fromDatePickerDialog!!.show()
        }
        binding.fabRemove.setOnClickListener{
            mImageUri = null
            binding.iconFile.setImageResource(R.drawable.ic_note_text)
        }
        binding.controlButtons.nextButton.isEnabled = false

        binding.controlButtons.nextButton.setOnClickListener {
            findNavController().navigate(
                    DocumentForIndividualContractFragmentDirections.actionDocumentForIndividualContractFragmentToDataForEmployeeFragment2(IdContract!!, Nationality)
            )
        }
    }
    private fun startFaceDectectorActivity() {
        PhotoSer.ActivityBuilder()
                .setDetectFace(false)
                .setQuality(50)
                .setSaveGalery(false)
                .setCrop(false)
                .start(this, requireActivity())
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun LoadDocuments(response: String?) {
        val jsonArrayRequeriments: JSONArray
        var DocumentContents: String
        var IsUpload : Boolean
        var requerimentIndividualContract: RequerimentIndividualContract
        try {
            jsonArrayRequeriments = JSONArray(response)
            for (i in 0 until jsonArrayRequeriments.length()) {
                DocumentContents = jsonArrayRequeriments.getJSONObject(i).getString("DocumentContents")
                IsUpload = jsonArrayRequeriments.getJSONObject(i).getBoolean("IsUpload")
                if (DocumentContents == "DocumentsForm" && IsUpload) {
                    requerimentIndividualContract = Gson().fromJson(jsonArrayRequeriments.getJSONObject(i).toString(), RequerimentIndividualContract::class.java)
                    mValues!!.add(requerimentIndividualContract)
                    if(Nationality == "0"){
                        val condition: Predicate<RequerimentIndividualContract> = Predicate<RequerimentIndividualContract> { mValues: RequerimentIndividualContract -> mValues.Name.equals("Permios Especial de Permanecia") }
                        mValues!!.removeIf(condition)
                    }
                }
                AmountDocuments = mValues!!.size
                binding.recyclerRequirements.layoutManager = LinearLayoutManager(activity)
                Adapter = AdapterDocumentsRecyclerView(requireActivity(), mValues!!, this,true)
                binding.recyclerRequirements.adapter = Adapter
                Adapter!!.notifyDataSetChanged()
                showProgress(false)
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }

    }
    fun ShowDocuments(response: String?){
        if (response == null || response == "[]") return
        val jsonArrayRequeriments: JSONArray
        var jsonObjectDocument: JSONObject
        var jsonFile: String?
        var DocumentContents: String
        var requerimentIndividualContract: RequerimentIndividualContract
        requiered!!.clear()
        try {
            jsonArrayRequeriments = JSONArray(response)
            for (i in 0 until jsonArrayRequeriments.length()) {
                jsonObjectDocument = jsonArrayRequeriments.getJSONObject(i).getJSONObject("Document")
                jsonFile = jsonArrayRequeriments.getJSONObject(i).getString("File")
                DocumentContents = jsonObjectDocument.getString("DocumentContents")
                if (DocumentContents == "DocumentsForm") {
                    requerimentIndividualContract = Gson().fromJson(jsonObjectDocument.toString(), RequerimentIndividualContract::class.java)
                    requerimentIndividualContract.File = jsonFile
                    requerimentIndividualContract.Name = jsonObjectDocument.getString("Name")
                    requerimentIndividualContract.Abrv = jsonObjectDocument.getString("Abrv")
                    requerimentIndividualContract.Description = jsonObjectDocument.getString("Description")
                    requiered!!.add(requerimentIndividualContract)
                }
            }
            binding.recyclerRequirementscompliments.visibility = View.VISIBLE
            DocumentsUpload = requiered!!.size
            AdapterRequeried = RequerimentIndividualContractAdapter(requireActivity(), requiered!!, this,true)
            binding.recyclerRequirementscompliments.layoutManager = LinearLayoutManager(activity)
            binding.recyclerRequirementscompliments.adapter = AdapterRequeried
            AdapterRequeried!!.notifyDataSetChanged()
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }
    fun ValidarDocuments(){
        if(DocumentsUpload >= AmountDocuments-1) {
            CompletedDocumets = true
            binding.controlButtons.nextButton.isEnabled = true
            binding.controlButtons.nextButton.setOnClickListener {
                findNavController().navigate(
                        DocumentForIndividualContractFragmentDirections.actionDocumentForIndividualContractFragmentToRequestOrderFragment(IdContract!!)
                )
            }
        }else {
            binding.controlButtons.nextButton.isEnabled = false
        }
    }
    @SuppressLint("SimpleDateFormat")
    private fun updateImage() {
        if (mImageUri != null && !mImageUri.toString().isEmpty()) {
            if(DocumentIsEps){
                if(!binding.epsAutoCompleteTextView.text.isNullOrEmpty()){
                    showProgress(true)
                val jsonObject = JSONObject()
                val jsonObjectDocument = JSONObject()
                try {
                    jsonObject.put("EpsId", epsId)
                    jsonObjectDocument.put("IndividualContractId", IdContract)
                    jsonObjectDocument.put("IdDocument", IdRequeriment)
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
                    UploadInfoDocument(jsonObject.toString(),jsonObjectDocument.toString())
                    UploadDocumentImage(IdRequeriment!!)
                }else{
                    binding.epsAutoCompleteTextView.error = getString(R.string.error_field_required)
                }
            }
            else if(DocumentIsAfps){
                if(!binding.afpAutoCompleteTextView.text.isNullOrEmpty()){
                    showProgress(true)
                val jsonObject = JSONObject()
                val jsonObjectDocument = JSONObject()
                try {
                    jsonObject.put("AfpId", afpId)
                    jsonObjectDocument.put("IndividualContractId", IdContract)
                    jsonObjectDocument.put("IdDocument", IdRequeriment)
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
                    UploadInfoDocument(jsonObject.toString(),jsonObjectDocument.toString())
                    UploadDocumentImage(IdRequeriment!!)
                }else{
                    binding.afpAutoCompleteTextView.error = getString(R.string.error_field_required)
                }

            }
            else if(DocumentRequeriedDate){
                if (fromDateStr != null) {
                    binding.tvFromDateError.error = "fecha obligatoria"
                    val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                    fromDateStr = format.format(fromDate!!)
                }
                showProgress(true)
                val jsonObjectDocument = JSONObject()
                try {
                    jsonObjectDocument.put("IndividualContractId", IdContract)
                    jsonObjectDocument.put("RequiredDate", fromDateStr)
                    jsonObjectDocument.put("IdDocument", IdRequeriment)
                    jsonObjectDocument.put("IsActive", true)
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
                CrudIntentService.startRequestCRUD(
                        requireActivity(),
                        "api/Document/IndividualContract", Request.Method.POST, jsonObjectDocument.toString(), "", false
                )
                UploadDocumentImage(IdRequeriment!!)
            }else{
                showProgress(true)
                val jsonObjectDocument = JSONObject()
                try {
                    jsonObjectDocument.put("IndividualContractId", IdContract)
                    jsonObjectDocument.put("IdDocument", IdRequeriment)
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
                CrudIntentService.startRequestCRUD(
                        requireActivity(),
                        "api/Document/IndividualContract", Request.Method.POST, jsonObjectDocument.toString(), "", false
                )
                UploadDocumentImage(IdRequeriment!!)
            }
            //mImageUri = null
            DocumentIsEps = false
            DocumentIsAfps = false
            DocumentRequeriedDate = false
        } else {
            val builder = AlertDialog.Builder(requireActivity())
                    .setMessage("Es necesario seleccionar una imagen")
            builder.create().show()
        }
    }

    override fun onResume() {
        super.onResume()
        var intentFilter = IntentFilter()
        intentFilter.addAction(CRUDService.ACTION_REQUEST_SAVE)
        intentFilter.addAction(CRUDService.ACTION_REQUEST_GET)
        intentFilter.addAction(CrudIntentService.ACTION_REQUEST_POST)
        LocalBroadcastManager.getInstance(requireActivity())
                .registerReceiver(requestBroadcastReceiver!!, intentFilter)
    }

    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(requireActivity()).unregisterReceiver(requestBroadcastReceiver!!)
    }
    override fun onAttach(context: Context) {
        super.onAttach(context)
        ApplicationContext.analitycoComponent.inject(this)
    }
    fun UploadDocumentImage(idDocument: String){
        val params = HashMap<String, String>()
        params["file"] = mImageUri.toString()
        val url = "api/Document/IndividualContract/Upload/$idDocument"
        CrudIntentService.startActionFormData(
                requireActivity(), url,
                Request.Method.PUT, params
        )
    }
    private fun UploadInfoDocument(jsonObject: String, jsonObjectDocument: String) {
        CrudIntentService.startRequestCRUD(
                requireActivity(),
                "api/PersonalEmployerInfo/" + createContractViewModel.personalDataToPhoto.value!!.Id, Request.Method.PUT, jsonObject, "", false
        )
        CrudIntentService.startRequestCRUD(
                requireActivity(),
                "api/Document/IndividualContract", Request.Method.POST, jsonObjectDocument, "", false
        )
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        val code = view?.tag as Int
        val calendar = Calendar.getInstance()
        calendar[year, month] = dayOfMonth
        val myFormat = "dd/MMM/yyyy"
        val sdf = SimpleDateFormat(myFormat, Locale("es", "ES"))
        when (code) {
            R.id.fromDateBtn -> {
                fromDateStr = sdf.format(calendar.time)
                binding.fromDateBtn.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        0,
                        0
                )
                binding.fromDateBtn.text = fromDateStr
                fromDate = calendar.time
                binding.fromDateBtn.error = null
                binding.tvFromDateError.visibility = View.GONE
                binding.tvFromDateError.error = null
            }
        }
    }

    private fun updateDatePicker(fromDate: Date) {
        val cF = Calendar.getInstance()
        cF.time = fromDate
        fromDatePickerDialog = DatePickerDialog(
                requireActivity(), this, cF[Calendar.YEAR],
                cF[Calendar.MONTH], cF[Calendar.DAY_OF_MONTH]
        )
        fromDatePickerDialog!!.datePicker.minDate = System.currentTimeMillis()
    }
    override fun editRequerimentItemUpload(mItem: RequerimentIndividualContract?, pos: Int) {
        val builder = AlertDialog.Builder(requireActivity())
                .setPositiveButton("SI") { _: DialogInterface?, _: Int ->
                    positionArray = pos
                    IsUpload = true
                    binding.recyclerRequirementscompliments.visibility = View.GONE
                    binding.recyclerRequirements.visibility = View.GONE
                    binding.controlButtons.nextButton.visibility = View.GONE
                    binding.controlButtons.backButton.visibility = View.GONE
                    binding.editRequirement.visibility = View.VISIBLE
                    binding.requirementTitle.text = mItem!!.Name
                    IdRequeriment = mItem.Id

                    if(mItem.IsByDate){
                        binding.DateToRequeriment.visibility = View.VISIBLE
                        binding.SearchToRequeriment.visibility = View.GONE
                        val calendar = Calendar.getInstance()
                        fromDate = calendar.time
                        calendar.add(Calendar.DAY_OF_MONTH, 1)
                        val myFormat = "dd/MMM/yyyy"
                        val sdf = SimpleDateFormat(myFormat, Locale("es", "ES"))
                        fromDateStr = sdf.format(fromDate!!)
                        binding.fromDateBtn.setCompoundDrawablesWithIntrinsicBounds(
                                0,
                                0,
                                0,
                                0
                        )
                        binding.fromDateBtn.text = fromDateStr
                        DocumentRequeriedDate = true
                        updateDatePicker(fromDate!!)
                    }else{
                        binding.SearchToRequeriment.visibility = View.VISIBLE
                        binding.DateToRequeriment.visibility = View.GONE
                        when {
                            mItem.Abrv.equals("EPS") -> {
                                binding.epsInputLayout.visibility = View.VISIBLE
                                binding.afpInputLayout.visibility = View.GONE
                                DocumentIsEps = true
                            }
                            mItem.Abrv.equals("AFP") -> {
                                binding.afpInputLayout.visibility = View.VISIBLE
                                binding.epsInputLayout.visibility = View.GONE
                                DocumentIsAfps = true
                            }
                            else -> {
                                binding.afpInputLayout.visibility = View.GONE
                                binding.epsInputLayout.visibility = View.GONE
                                DocumentNoRequeried = true
                            }
                        }
                    }
                }.setNegativeButton("NO", null)
                .setMessage("Es necesario tener acceso a internet para subir archivos, desea continuar?")
        builder.create().show()
        requiered!![pos] = mItem!!
        Adapter!!.notifyDataSetChanged()
    }

    override fun editRequerimentItem(mItem: RequerimentIndividualContract?, pos: Int) {
        val builder = AlertDialog.Builder(requireActivity())
                .setPositiveButton("SI") { _: DialogInterface?, _: Int ->
                    positionArray = pos
                    IsUpload = false
                    binding.recyclerRequirementscompliments.visibility = View.GONE
                    binding.recyclerRequirements.visibility = View.GONE
                    binding.controlButtons.nextButton.visibility = View.GONE
                    binding.controlButtons.backButton.visibility = View.GONE
                    binding.editRequirement.visibility = View.VISIBLE
                    binding.requirementTitle.text = mItem!!.Name
                    IdRequeriment = mItem.Id
                    if(mItem.IsByDate){
                        binding.DateToRequeriment.visibility = View.VISIBLE
                        binding.SearchToRequeriment.visibility = View.GONE
                        val calendar = Calendar.getInstance()
                        fromDate = calendar.time
                        calendar.add(Calendar.DAY_OF_MONTH, 1)
                        val myFormat = "dd/MMM/yyyy"
                        val sdf = SimpleDateFormat(myFormat, Locale("es", "ES"))
                        fromDateStr = sdf.format(fromDate!!)
                        binding.fromDateBtn.setCompoundDrawablesWithIntrinsicBounds(
                                0,
                                0,
                                0,
                                0
                        )
                        binding.fromDateBtn.text = fromDateStr
                        DocumentRequeriedDate = true
                        updateDatePicker(fromDate!!)
                    }else{
                        binding.SearchToRequeriment.visibility = View.VISIBLE
                        binding.DateToRequeriment.visibility = View.GONE
                        if(mItem.Abrv.equals("EPS")){
                            binding.epsInputLayout.visibility = View.VISIBLE
                            binding.afpInputLayout.visibility = View.GONE
                            DocumentIsEps = true
                        }
                        else if(mItem.Abrv.equals("AFP")){
                            binding.afpInputLayout.visibility = View.VISIBLE
                            binding.epsInputLayout.visibility = View.GONE
                            DocumentIsAfps = true
                        }else{
                            binding.afpInputLayout.visibility = View.GONE
                            binding.epsInputLayout.visibility = View.GONE
                            DocumentNoRequeried = true
                        }
                    }

                }.setNegativeButton("NO", null)
                .setMessage("Es necesario tener acceso a internet para subir archivos, desea continuar?")
        builder.create().show()
        mValues!![pos] = mItem!!
        Adapter!!.notifyDataSetChanged()
    }
    @RequiresApi(Build.VERSION_CODES.N)
    override fun onStringResult(action: String?, option: Int, response: String?, url: String?) {
        when (option) {
            Constantes.SUCCESS_REQUEST -> {
                if (url == Constantes.INDIVIDUAL_DOCUMENT_CONTRACT_URL) {
                    LoadDocuments(response)
                }
                if (url == Constantes.INDIVIDUAL_REQUERIMENT_CONTRACT_URL + IdContract) {
                    ShowDocuments(response)
                }
                if (url == "api/Document/IndividualContract") {
                    if (!IsUpload) {
                        DeleteElement(positionArray)
                    } else {
                        IsUpload = false
                    }
                    CRUDService.startRequest(
                            activity, Constantes.INDIVIDUAL_REQUERIMENT_CONTRACT_URL + IdContract,
                            Request.Method.GET, "", false
                    )
                    ValidarDocuments()
                    mImageUri = null
                    binding.iconFile.setImageResource(R.drawable.ic_note_text)
                    binding.recyclerRequirements.visibility = View.VISIBLE
                    binding.recyclerRequirementscompliments.visibility = View.VISIBLE
                    binding.controlButtons.nextButton.visibility = View.VISIBLE
                    binding.controlButtons.backButton.visibility = View.VISIBLE
                    binding.editRequirement.visibility = View.GONE
                    showProgress(false)
                }
            }
            Constantes.NOT_INTERNET -> {
                showProgress(false)
                requireActivity().finish()
            }
            Constantes.UNAUTHORIZED -> {
                showProgress(false)
                requireActivity().finish()
            }
            Constantes.FORBIDDEN -> {
                showProgress(false)
                requireActivity().finish()
            }
        }
    }

    private fun DeleteElement(positionArray: Int) {
        mValues!!.removeAt(positionArray)
        Adapter!!.notifyItemRemoved(positionArray)
        Adapter!!.notifyDataSetChanged()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PhotoSer.SER_IMAGE_ACTIVITY_REQUEST_CODE) {
            when (resultCode) {
                Activity.RESULT_OK -> {
                    val imageUri =
                            Uri.parse(data!!.getStringExtra(FaceTrackerActivity.URI_IMAGE_KEY))
                    binding.iconFile.setImageURI(imageUri)
                    mImageUri = imageUri
                    binding.fabRemove.visibility = View.VISIBLE
                }
                else -> {
                }
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
}
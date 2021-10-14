package co.tecno.sersoluciones.analityco.individualContract

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Context
import android.content.IntentFilter
import android.graphics.drawable.PictureDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.fragment.findNavController
import co.tecno.sersoluciones.analityco.ApplicationContext
import co.tecno.sersoluciones.analityco.GlideApp
import co.tecno.sersoluciones.analityco.R
import co.tecno.sersoluciones.analityco.databases.DBHelper
import co.tecno.sersoluciones.analityco.databinding.FragmentInitiateOrderBinding
import co.tecno.sersoluciones.analityco.nav.CreatePersonalViewModel
import co.tecno.sersoluciones.analityco.receivers.RequestBroadcastReceiver
import co.tecno.sersoluciones.analityco.services.CRUDService
import co.tecno.sersoluciones.analityco.services.CrudIntentService
import co.tecno.sersoluciones.analityco.utilities.Constantes
import co.tecno.sersoluciones.analityco.utilities.HttpRequest
import com.android.volley.Request
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.squareup.picasso.Picasso
import org.json.JSONException
import org.json.JSONObject
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern
import javax.inject.Inject

class IinitiateOrderFragment:Fragment(), RequestBroadcastReceiver.BroadcastListener {
    private lateinit var binding: FragmentInitiateOrderBinding
    private var requestBroadcastReceiver: RequestBroadcastReceiver? = null
    private  var ProjectID : String? = null
    private  var ContractID : String? = null
    var PersoinBlackList = ""
    var PersonalEmployerInfoId : String? = null
    var docType : String? = null
    var docNumber : String? = null
    var personInContract : Boolean? = false

    @Inject
    lateinit var viewModel: CreatePersonalViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentInitiateOrderBinding.inflate(inflater)
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        requestBroadcastReceiver = RequestBroadcastReceiver(this)
        val arguments = IinitiateOrderFragmentArgs.fromBundle(requireArguments())
        binding.placeToJob =  arguments.placetoJob
        ProjectID = arguments.placetoJob.projectid
        ContractID = arguments.placetoJob.ContractId
        if(viewModel.personalData.value != null){
            binding.personal = viewModel.personalData.value!!
            binding.textNameEmpleado.text = String.format(
                    "%s %s",
                    viewModel.personalData.value!!.Name,
                    viewModel.personalData.value!!.LastName
            )
        }
        if(viewModel.PersonalAssociateWithEmployer){
            val values = ContentValues()
            values.put("EmployerId", viewModel.EmployerId)
            values.put("PersonalId", viewModel.PersonalId)
            val paramsQuery = HttpRequest.makeParamsInUrl(values)
            CRUDService.startRequest(activity, Constantes.PERSONAL_EMPLOYER_INFO_PARAMS_URL,
                    Request.Method.GET, paramsQuery, false)
        }else{
            CRUDService.startRequest(activity, Constantes.PERSONAL_EMPLOYER_INFO_URL + viewModel.personalDataToPhoto.value!!.Id,
                    Request.Method.GET, "", false)
        }

        LoadEmployerSelected()
        LoadImagePlacetojob()
        binding.controlButtons.nextButton.text = "INICIAR"
        detectPersonInContract()
        binding.controlButtons.nextButton.setOnClickListener {
            if(personInContract!!){
                val alertDialogBuilder = AlertDialog.Builder(requireActivity())
                alertDialogBuilder.setTitle("Lo sentimos,")
                alertDialogBuilder.setMessage("No podemos tramitar su solicitud")
                val alertDialog = alertDialogBuilder.create()
                alertDialogBuilder.setPositiveButton("NO"){_,_ ->
                    alertDialog.dismiss()
                }
                alertDialog.show()
            }else{
                if(viewModel.PersonalAssociateWithEmployer){
                    val values = ContentValues()
                    values.put("documentType", docType)
                    values.put("documentNumber", docNumber)
                    val paramsQuery = HttpRequest.makeParamsInUrl(values)
                    CRUDService.startRequest(activity, Constantes.PERSONAL_BLACK_LIST_INFO_URL,
                            Request.Method.GET, paramsQuery, false)
                }else {
                    val values = ContentValues()
                    values.put("documentType", viewModel.personalData.value!!.DocumentType)
                    values.put("documentNumber", viewModel.personalData.value!!.DocumentNumber)
                    val paramsQuery = HttpRequest.makeParamsInUrl(values)
                    CRUDService.startRequest(activity, Constantes.PERSONAL_BLACK_LIST_INFO_URL,
                            Request.Method.GET, paramsQuery, false)
                }
                if(PersoinBlackList == "true"){
                    val alertDialogBuilder = AlertDialog.Builder(requireActivity())
                    alertDialogBuilder.setTitle("Lo sentimos,")
                    alertDialogBuilder.setMessage("No podemos tramitar su solicitud")
                    val alertDialog = alertDialogBuilder.create()
                    alertDialogBuilder.setNegativeButton("NO"){_,_ ->
                        alertDialog.dismiss()
                    }
                    alertDialog.show()
                }else {

                    CrudIntentService.startRequestCRUD(activity, Constantes.LIST_INDIVIDUALCONTRACTS_URL,
                           Request.Method.POST, jsonInsert(), "", false)
                }
            }
        }
    }

    private fun detectPersonInContract() {
        CRUDService.startRequest(activity, Constantes.PERSONAL_IN_CONTRACT_INFO_URL+viewModel.PersonalEmployerInfoId,
                Request.Method.GET, "", false)
    }

    private fun jsonInsert(): String{
        val jsonReturn : String
        val selection = ("(" + DBHelper.COPTIONS_COLUMN_DESC + " = ?)")
        val selectionArgs = arrayOf("INICIADO")
        val cursor = requireActivity().applicationContext.contentResolver.query(Constantes.CONTENT_COMMON_OPTIONS_URI,null,selection,selectionArgs,null)
        cursor?.moveToFirst()
        val StageTypeId = cursor?.getInt(cursor?.getColumnIndex(DBHelper.COPTIONS_COLUMN_SERVER_ID))
        if(viewModel.PersonalAssociateWithEmployer){
            val jsonObject = JSONObject()
            val json1 = JSONObject()
            try {
                jsonObject.put("ContractNumber",ContractNumber())
                jsonObject.put("PersonalEmployerInfoId", PersonalEmployerInfoId)
                jsonObject.put("ProjectId", ProjectID)
                jsonObject.put("ContractId", ContractID)
                jsonObject.put("IndividualContractStage", json1.put("StageTypeId", StageTypeId))
            } catch (e: JSONException) {
                e.printStackTrace()
            }
            jsonReturn = jsonObject.toString()
        }else {
            val jsonObject = JSONObject()
            val json1 = JSONObject()
            try {
                jsonObject.put("ContractNumber",ContractNumber())
                jsonObject.put("PersonalEmployerInfoId", viewModel.personalDataToPhoto.value!!.Id)
                jsonObject.put("ProjectId", ProjectID)
                jsonObject.put("ContractId", ContractID)
                jsonObject.put("IndividualContractStage", json1.put("StageTypeId", StageTypeId))
            } catch (e: JSONException) {
                e.printStackTrace()
            }
            jsonReturn = jsonObject.toString()
        }
        return  jsonReturn
    }
    @SuppressLint("SimpleDateFormat")
    fun ContractNumber():String{
        val formate = SimpleDateFormat("yyyyMMdd")
        val contractNumberDate = formate.format(Date())
        val numRandom = (100..999).random()
        return "CO-$contractNumberDate-$numRandom"
    }
    fun LoadImageEmpleado(photo: String){
        val url = Constantes.URL_IMAGES + photo
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
                    .into(binding.logoempleado)
        } else {
            Picasso.get().load(url)
                    .resize(0, 250)
                    .placeholder(R.drawable.image_not_available)
                    .error(R.drawable.image_not_available)
                    .into(binding.logoempleado)
        }
    }
    @SuppressLint("SimpleDateFormat")
    fun LoadImagePlacetojob(){
        val url = Constantes.URL_IMAGES + binding.placeToJob!!.Logo
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
                    .into(binding.logoPlaceJob)
        } else {
            Picasso.get().load(url)
                    .resize(0, 250)
                    .placeholder(R.drawable.image_not_available)
                    .error(R.drawable.image_not_available)
                    .into(binding.logoPlaceJob)
        }
        if(binding.placeToJob!!.FinishDate !=null){
            var FinishDate = ""
            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
            val format = SimpleDateFormat("dd/MM/yyyy")
            try {
                val date = simpleDateFormat.parse(binding.placeToJob!!.FinishDate)
                FinishDate = format.format(date)
                binding.textDate.text = FinishDate
            }catch (e: ParseException) {
                e.printStackTrace()
            }
        }
    }
    fun LoadEmployerSelected(){
        binding.textName.text = viewModel.ItemEmployer!!.Name
        binding.textAddress.text = String.format(
                "%s %s",
                viewModel.ItemEmployer!!.DocumentType,
                viewModel.ItemEmployer!!.DocumentNumber
            )
            binding.textValidity.text = viewModel.ItemEmployer!!.Address
            binding.labelValidity.visibility = View.GONE
            binding.labelActive.visibility = View.GONE
            val url = Constantes.URL_IMAGES + viewModel.ItemEmployer!!.Logo
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
    }
    override fun onResume() {
        super.onResume()
        val intentFilter = IntentFilter()
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

    override fun onStringResult(action: String?, option: Int, response: String?, url: String?) {
        when (option) {
            Constantes.SUCCESS_REQUEST -> {
                if(url == Constantes.PERSONAL_IN_CONTRACT_INFO_URL+viewModel.PersonalEmployerInfoId){
                    personInContract = true
                }
                if(viewModel.personalData.value != null){
                    if (url == Constantes.PERSONAL_EMPLOYER_INFO_URL + viewModel.personalDataToPhoto.value!!.Id) {
                        val PersonPhoto = JSONObject(response)
                        if(binding.textName.text.isNullOrEmpty()){

                        }
                        LoadImageEmpleado(PersonPhoto.getString("Photo"))
                    }
                }else{
                    if(url == Constantes.PERSONAL_EMPLOYER_INFO_PARAMS_URL){
                        System.out.println("Respeonse server${response}")
                        val Response = JSONObject(response)
                        val personal = Response.getJSONObject("personal")
                        PersonalEmployerInfoId = Response.getString("Id")
                        LoadImageEmpleado(Response.getString("Photo"))
                        docType = personal.getString("DocumentType")
                        docNumber = personal.getString("DocumentNumber")
                        binding.textNameEmpleado.text = String.format(
                                "%s %s",
                                personal.getString("Name"),
                                personal.getString("LastName")
                        )
                        binding.textDocument.text = String.format(
                                "%s %s",
                                docType,
                                docNumber
                        )
                    }
                }
                if (url == Constantes.PERSONAL_BLACK_LIST_INFO_URL) {
                    PersoinBlackList = response!!
                }
                if (action == CrudIntentService.ACTION_REQUEST_POST){
                    val jsonObject = JSONObject(response)
                    val idContract = jsonObject.getString("Id")
                    if(viewModel.personaCreateFirtTime){
                        if(viewModel.personalData.value != null){
                            findNavController().navigate(
                                    IinitiateOrderFragmentDirections.actionIinitiateOrderFragmentToDocumentForIndividualContractFragment(idContract,viewModel.personalDataToPhoto.value!!.Nationality.toString())
                            )
                        }else {
                            findNavController().navigate(
                                    IinitiateOrderFragmentDirections.actionIinitiateOrderFragmentToDocumentForIndividualContractFragment(idContract,viewModel.Nationality.toString())
                            )
                        }
                    }else {
                        if(viewModel.personalData.value != null){
                            findNavController().navigate(
                                    IinitiateOrderFragmentDirections.actionIinitiateOrderFragmentToDataForEmployeeFragment(idContract,viewModel.personalDataToPhoto.value!!.Nationality.toString())
                            )
                        }else {
                            findNavController().navigate(
                                    IinitiateOrderFragmentDirections.actionIinitiateOrderFragmentToDataForEmployeeFragment(idContract,viewModel.Nationality.toString())
                            )
                        }
                    }
                }

            }
            Constantes.PRECONDITION_FAILED-> {
                if(url == Constantes.PERSONAL_IN_CONTRACT_INFO_URL+viewModel.PersonalEmployerInfoId){
                    personInContract = false
                }
                if(url == Constantes.PERSONAL_EMPLOYER_INFO_PARAMS_URL){
                    println("Respeonse server${response}")
                }
            }
        }

    }
}



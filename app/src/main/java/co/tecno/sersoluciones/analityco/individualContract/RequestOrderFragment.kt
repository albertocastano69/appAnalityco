package co.tecno.sersoluciones.analityco.individualContract

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.ColorStateList
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import co.com.sersoluciones.barcodedetectorser.utilities.DebugLog
import co.tecno.sersoluciones.analityco.ApplicationContext
import co.tecno.sersoluciones.analityco.R
import co.tecno.sersoluciones.analityco.adapters.repositories.PositionIndividualContract
import co.tecno.sersoluciones.analityco.databases.DBHelper
import co.tecno.sersoluciones.analityco.databinding.FragmentRequestOrderBinding
import co.tecno.sersoluciones.analityco.models.*
import co.tecno.sersoluciones.analityco.nav.CreatePersonalViewModel
import co.tecno.sersoluciones.analityco.receivers.RequestBroadcastReceiver
import co.tecno.sersoluciones.analityco.services.CRUDService
import co.tecno.sersoluciones.analityco.services.CrudIntentService
import co.tecno.sersoluciones.analityco.ui.createPersonal.viewmodels.PersonalViewModel
import co.tecno.sersoluciones.analityco.utilities.Constantes
import co.tecno.sersoluciones.analityco.utilities.MetodosPublicos
import co.tecno.sersoluciones.analityco.utilities.MyPreferences
import com.android.volley.Request
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.Gson
import org.json.JSONException
import org.json.JSONObject
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class RequestOrderFragment:Fragment(), DatePickerDialog.OnDateSetListener,RequestBroadcastReceiver.BroadcastListener {
    private lateinit var binding: FragmentRequestOrderBinding
    private var dateInitPickerDialog: DatePickerDialog? = null
    private var requestBroadcastReceiver: RequestBroadcastReceiver? = null
    private var dateInit: Date? = null
    private var nationality = 0
    private var adapter: ArrayAdapter<PlaceToJob>? = null
    private var IdContract : String = ""
    private var user: User? = null


    @Inject
    lateinit var viewModel: PersonalViewModel
    @Inject
    lateinit var CreateviewModel: CreatePersonalViewModel

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
                    CreateviewModel.clearForm()
                    activity?.finish()
                }
                .setNegativeButton("Cancelar", null)
        builder.create().show()
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentRequestOrderBinding.inflate(inflater)
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (requireActivity() as AppCompatActivity).supportActionBar?.title = "DATOS DE INGRESO"
        val argument = RequestOrderFragmentArgs.fromBundle(requireArguments())
        IdContract = argument.idContract
        val preferences = MyPreferences(requireActivity())
        user = Gson().fromJson(preferences.profile, User::class.java)
        binding.viewModel = viewModel
        requestBroadcastReceiver = RequestBroadcastReceiver(this)
        binding.controlButtons.nextButton.text = "SOLICITAR ORDEN"
        binding.controlButtons.backButton.text = "ANTERIOR"
        (binding.CountryContract as? TextView)?.text = "COLOMBIA"

        viewModel.getDaneCity()
        viewModel.getPosition(CreateviewModel.EmployerId)
        CreateviewModel.getPlaceToJob()
        if(CreateviewModel.ItemPlaceJob !== null){
            (binding.placeJobCompleteTextView as TextView).text = CreateviewModel.ItemPlaceJob!!.Name
            viewModel.PersonlIndividualContract.value!!.ProjectId = CreateviewModel.ItemPlaceJob!!.projectid
            viewModel.PersonlIndividualContract.value!!.ContractId = CreateviewModel.ItemPlaceJob!!.ContractId
        }
        CreateviewModel.placeJob.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, it)
            binding.placeJobCompleteTextView.setAdapter(adapter)
        })
        binding.placeJobCompleteTextView.apply {
            setOnItemClickListener { parent, _, position, _ ->
                val item = parent.adapter.getItem(position) as PlaceToJob
                DebugLog.logW("placeJob: ${item.Name}}")
                viewModel.PersonlIndividualContract.value!!.ProjectId = item.projectid
                viewModel.PersonlIndividualContract.value!!.ContractId = item.ContractId
            }
        }
        viewModel.daneCity.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            val daneCityAdapter = ArrayAdapter(requireContext(), R.layout.list_item, it)
            binding.cityContractCompleteTextView.setAdapter(daneCityAdapter)
        })
        binding.cityContractCompleteTextView.apply {
            setOnItemClickListener { parent, _, position, _ ->
                val item = parent.adapter.getItem(position) as DaneCity
                viewModel.PersonlIndividualContract.value!!.ContractCityId = item.Id?.toInt()
                DebugLog.logW("city: ${item.CityName}}")
            }
            addTextChangedListener {
                it?.let {
                    if (it.isEmpty()) {
                        binding.CityContract.endIconMode = TextInputLayout.END_ICON_DROPDOWN_MENU
                    } else binding.CityContract.endIconMode = TextInputLayout.END_ICON_CLEAR_TEXT
                }
            }
        }
        viewModel.payPeriod.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            DebugLog.logW("payPeriod count ${it.count()}")
            val payperiodAdapter = ArrayAdapter(requireContext(), R.layout.list_item, it)
            binding.payPeriodCompleteTextView.setAdapter(payperiodAdapter)
            if (it.size == 1) {
                for (item in it) {
                    (binding.payPeriodCompleteTextView as? TextView)?.text = item.Description
                    viewModel.PersonlIndividualContract.value!!.PositionId = item.Id?.toInt()
                }
            }
        })
        binding.payPeriodCompleteTextView.apply {
            setOnItemClickListener { parent, _, position, _ ->
             val item = parent.adapter.getItem(position) as PayPeriod
                viewModel.PersonlIndividualContract.value!!.PayPeriodId = item.Id
                DebugLog.logW("payPeriod: ${item.Id}}")
            }
        }
        viewModel.contractType.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            val contractTypeAdapter = ArrayAdapter(requireContext(), R.layout.list_item, it)
            binding.typeContractCompleteTextView.setAdapter(contractTypeAdapter)
            if (it.size == 1) {
                for (item in it) {
                    (binding.typeContractCompleteTextView as? TextView)?.text = item.Description
                    viewModel.PersonlIndividualContract.value!!.IndividualContractTypeId = item.Id?.toInt()

                }
            }
        })
        binding.typeContractCompleteTextView.apply {
            setOnItemClickListener{ parent, _, position, _ ->
            val item = parent.adapter.getItem(position) as IndividualContractsTypeModel
                DebugLog.logW("contractId: ${item.Id}")
                val id = item.Id
                viewModel.PersonlIndividualContract.value!!.IndividualContractTypeId = id?.toInt()
            }
            addTextChangedListener {
                it?.let {
                    if(it.isEmpty()){
                        binding.typeContract.endIconMode = TextInputLayout.END_ICON_DROPDOWN_MENU
                    }else binding.typeContract.endIconMode = TextInputLayout.END_ICON_CLEAR_TEXT
                }
            }
        }
        viewModel.position.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            DebugLog.logW("jobs count ${it.count()}")
            val jobAdapter = ArrayAdapter(requireContext(), R.layout.list_item, it)
            binding.typePositionCompleteTextView.setAdapter(jobAdapter)
        })
        binding.typePositionCompleteTextView.apply {
            setOnItemClickListener { parent, _, position, _ ->
                val item = parent.adapter.getItem(position) as PositionIndividualContract
                viewModel.PersonlIndividualContract.value!!.PositionId = item.Id
                DebugLog.logW("job: ${item.Name}}")
            }
            addTextChangedListener {
                it?.let {
                    if (it.isEmpty()) {
                        binding.typePosition.endIconMode = TextInputLayout.END_ICON_DROPDOWN_MENU
                    }else binding.typePosition.endIconMode = TextInputLayout.END_ICON_CLEAR_TEXT
                }
            }
        }

        (binding.countryContractcCompleteTextView as? TextView)?.text = "COLOMBIA"
        val listNationality = arrayOf("COLOMBIA", "VENEZUELA")
        val adapterNationality = ArrayAdapter(requireContext(), R.layout.list_item, listNationality)

        (binding.countryContractcCompleteTextView as? AutoCompleteTextView)?.setAdapter(adapterNationality)
        binding.countryContractcCompleteTextView.apply {
            setOnItemClickListener { parent, _, position, _ ->
                val item = parent.adapter.getItem(position) as String
                nationality = position
                DebugLog.logW("nationality: $item position: $position")
            }
        }

        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        dateInit = calendar.time

        configDatePicker(dateInit)
        binding.edtInitDate.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if(hasFocus){
                if (binding.edtInitDate.text.isEmpty()) {
                    showDatePicker()
                } else {
                    val myFormat = "dd/MMM/yyyy"
                    val sdf = SimpleDateFormat(myFormat, Locale("es", "ES"))
                    try {
                        dateInit = sdf.parse(binding.edtInitDate.text.toString())
                    }catch (e: ParseException) {
                        e.printStackTrace()
                    }
                }
            }
        }
        binding.controlButtons.nextButton.setOnClickListener {
            if(submit()){
                val inflater = layoutInflater
                val view = inflater.inflate(R.layout.custom_alert_dialog, null)
                val builder = AlertDialog.Builder(requireActivity())
                val butonAceptar = view.findViewById<Button>(R.id.Aceptar)
                val butonCancelar = view.findViewById<Button>(R.id.Cancel)
                builder.setView(view)
                val alertDialog = builder.create()
                alertDialog.show()
                butonCancelar.setOnClickListener {
                    alertDialog.dismiss()
                }
                butonAceptar.setOnClickListener {
                    DisableComponents(true)
                    RequestOrder()
                    alertDialog.dismiss()
                }
            }
        }
        binding.controlButtons.nextButton.isEnabled = ValidatePermission()
    }

    @SuppressLint("SimpleDateFormat")
    private fun RequestOrder() {
        val newContract = viewModel.PersonlIndividualContract.value
        val salary = binding.salaryEdText.text.toString()
        newContract!!.Salary = salary.toInt()

        val start = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
        val StartDate = start.format(dateInit)


        val jsonObject = JSONObject()
        try {
            jsonObject.put("IndividualContractTypeId", newContract.IndividualContractTypeId)
            jsonObject.put("PersonalEmployerInfoId", CreateviewModel.personalDataToPhoto.value!!.Id)
            jsonObject.put("PositionId", newContract.PositionId)
            jsonObject.put("Salary", newContract.Salary)
            jsonObject.put("PositionId", newContract.PositionId)
            jsonObject.put("PayPeriodId", newContract.PayPeriodId)
            jsonObject.put("StartDate", StartDate)
            jsonObject.put("ProjectId", newContract.ProjectId)
            jsonObject.put("ContractId", newContract.ContractId)
            jsonObject.put("ContractCityId", newContract.ContractCityId)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        val jsonObjectAddStage = JSONObject()
        try{
            jsonObjectAddStage.put("IndividualContractId", IdContract)
            jsonObjectAddStage.put("StageTypeId", requestStageTypeId("SOLICITADO"))
        }catch (e: JSONException){
            e.printStackTrace()
        }
        CrudIntentService.startRequestCRUD(activity, Constantes.LIST_INDIVIDUALCONTRACTS_URL + IdContract,
                Request.Method.PUT, jsonObject.toString(), "", false)

        CrudIntentService.startRequestCRUD(
                requireActivity(),
                Constantes.REQUEST_INDIVIDUAL_CONTRACT_ADDSTAGE_URL, Request.Method.POST, jsonObjectAddStage.toString(), "", false
        )
        showProgress(true)
    }

    private fun submit(): Boolean {
        var send = true
        var focusView: View? = null
        when {
            TextUtils.isEmpty(binding.typeContractCompleteTextView.text.toString()) -> {
                binding.typePositionCompleteTextView.error = getString(R.string.error_field_required)
                send = false
                focusView = binding.typeContractCompleteTextView
            }
            TextUtils.isEmpty(binding.typePositionCompleteTextView.text.toString()) -> {
                binding.typePositionCompleteTextView.error = getString(R.string.error_field_required)
                send = false
                focusView = binding.typePositionCompleteTextView
            }
            TextUtils.isEmpty(binding.salaryEdText.text.toString()) -> {
                //binding.salaryEdText.error = getString(R.string.error_field_required)
                send = false
                focusView = binding.salaryEdText
            }
            TextUtils.isEmpty(binding.payPeriodCompleteTextView.text.toString()) -> {
                binding.payPeriodCompleteTextView.error = getString(R.string.error_field_required)
                send = false
                focusView = binding.payPeriodCompleteTextView
            }
            TextUtils.isEmpty(binding.countryContractcCompleteTextView.text.toString()) -> {
                binding.countryContractcCompleteTextView.error = getString(R.string.error_field_required)
                send = false
                focusView = binding.countryContractcCompleteTextView
            }
            TextUtils.isEmpty(binding.cityContractCompleteTextView.text.toString()) -> {
                binding.cityContractCompleteTextView.error = getString(R.string.error_field_required)
                send = false
                focusView = binding.cityContractCompleteTextView
            }
            TextUtils.isEmpty(binding.placeJobCompleteTextView.text.toString()) -> {
                binding.placeJobCompleteTextView.error = getString(R.string.error_field_required)
                send = false
                focusView = binding.placeJobCompleteTextView
            }
        }
        if (!send) {
            focusView!!.requestFocus()
        }
        return send
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        val calendar = Calendar.getInstance()
        calendar[year, month] = dayOfMonth
        val myFormat = "dd/MMM/yyyy"
        val sdf = SimpleDateFormat(myFormat, Locale("es", "ES"))
        val dateInitStr = sdf.format(calendar.time)
        binding.edtInitDate.setText(dateInitStr)
        dateInit = calendar.time

    }
    private fun configDatePicker(dateinit: Date?) {
        val currentDate = MetodosPublicos.getCurrentDate()
        val cF = Calendar.getInstance()
        cF.time = dateinit!!
        dateInitPickerDialog = DatePickerDialog(
                requireContext(), this, cF[Calendar.YEAR],
                cF[Calendar.MONTH], cF[Calendar.DAY_OF_MONTH]
        )
        dateInitPickerDialog!!.datePicker.minDate = currentDate.time
    }
    private fun showDatePicker() {
        dateInitPickerDialog!!.datePicker.tag = R.id.btn_from_date
        dateInitPickerDialog!!.show()
    }
    override fun onAttach(context: Context) {
        super.onAttach(context)
        ApplicationContext.analitycoComponent.inject(this)
    }

    override fun onStringResult(action: String?, option: Int, response: String?, url: String?) {
        when(option){
            Constantes.SUCCESS_REQUEST -> {
                if (url == Constantes.LIST_INDIVIDUALCONTRACTS_URL + IdContract) {
                    CrudIntentService.startRequestCRUD(
                            requireActivity(),
                            Constantes.REQUEST_HIRING_DOCUMENT_URL + IdContract, Request.Method.POST, "", "", false
                    )
                    binding.ordenReject.visibility = View.GONE
                }
                if (url == Constantes.REQUEST_HIRING_DOCUMENT_URL + IdContract) {
                    val jsonObject = JSONObject(response)
                    val urlOrdenRequest = jsonObject.getString("url")
                    ExecuteOrderRequest(urlOrdenRequest)
                }
                if (url == Constantes.REQUEST_APROVED_HIRING_DOCUMENT_URL + IdContract) {
                    val jsonObject = JSONObject(response)
                    val urlOrdenAproved = jsonObject.getString("url")
                    ExecuteOrderAproved(urlOrdenAproved)
                }
                if (url == Constantes.REQUEST_REJECT_HIRING_DOCUMENT_URL + IdContract) {
                    val jsonObject = JSONObject(response)
                    val urlOrdenReject = jsonObject.getString("url")
                    ExecuteOrderReject(urlOrdenReject)
                }
            }
        }
    }

    private fun ExecuteOrderReject(urlOrdenReject: String?) {
        if(urlOrdenReject != null){
            showProgress(false)
            binding.controlButtons.nextButton.text = "SOLICITAR ORDEN"
            binding.controlButtons.backButton.text = "ANTERIOR"
            binding.controlButtons.nextButton.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.bar_decoded))
            (requireActivity() as AppCompatActivity).supportActionBar?.setBackgroundDrawable(ColorDrawable(resources.getColor(R.color.red)))
            DisableComponents(false)
            binding.controlButtons.nextButton.setOnClickListener {
                if(submit()){
                    val inflater = layoutInflater
                    val view = inflater.inflate(R.layout.custom_alert_dialog, null)
                    val builder = AlertDialog.Builder(requireActivity())
                    val butonAceptar = view.findViewById<Button>(R.id.Aceptar)
                    val butonCancelar = view.findViewById<Button>(R.id.Cancel)
                    builder.setView(view)
                    val alertDialog =builder.create()
                    alertDialog.show()
                    butonCancelar.setOnClickListener {
                        alertDialog.dismiss()
                    }
                    butonAceptar.setOnClickListener {
                        DisableComponents(true)
                        RequestOrder()
                        alertDialog.dismiss()
                    }
                }
            }
            binding.ordenReject.visibility = View.VISIBLE
            binding.ordenRequest.visibility = View.GONE
            binding.ordenAproved.visibility = View.GONE
            val focusView: View? = binding.ordenReject
            focusView!!.requestFocus()
            binding.ordenReject.setOnClickListener {
                val intent1 = Intent(Intent.ACTION_VIEW).setData(Uri.parse(urlOrdenReject))
                startActivity(intent1)
            }
        }
    }
    
    private fun ExecuteOrderAproved(urlOrdenAproved: String?) {
        showProgress(false)
        binding.controlButtons.backButton.text = "RECHAZAR ORDEN"
        binding.controlButtons.backButton.isEnabled = false
        binding.controlButtons.backButton.setTextColor(ContextCompat.getColor(requireActivity(), R.color.white))
        binding.controlButtons.nextButton.isEnabled = false
        binding.controlButtons.nextButton.setTextColor(ContextCompat.getColor(requireActivity(), R.color.white))
        if(urlOrdenAproved != null){
            binding.ordenAproved.visibility = View.VISIBLE
            var focusView: View? = binding.ordenAproved
            focusView!!.requestFocus()
            binding.ordenAproved.setOnClickListener {
                val intent1 = Intent(Intent.ACTION_VIEW).setData(Uri.parse(urlOrdenAproved))
                startActivity(intent1)
            }
        }
    }

    private fun ExecuteOrderRequest(url: String?) {
        showProgress(false)
        if (url != null ) {
            binding.ordenRequest.visibility = View.VISIBLE
            var focusView: View? = binding.ordenRequest
            focusView!!.requestFocus()
            binding.ordenRequest.setOnClickListener {
                val intent1 = Intent(Intent.ACTION_VIEW).setData(Uri.parse(url))
                startActivity(intent1)
            }
            DisableComponents(true)
        }
        if(!ValidatePermissionAprroved()){
            binding.controlButtons.nextButton.isEnabled = false
            return
        }
        binding.controlButtons.backButton.text = "RECHAZAR ORDEN"
        binding.controlButtons.backButton.setTextColor(ContextCompat.getColor(requireActivity(), R.color.white))
        binding.controlButtons.backButton.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.red))
        binding.controlButtons.nextButton.text = "APROBAR ORDEN"
        binding.controlButtons.nextButton.setTextColor(ContextCompat.getColor(requireActivity(), R.color.white))
        binding.controlButtons.nextButton.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.primary_dark))
        (requireActivity() as AppCompatActivity).supportActionBar?.setBackgroundDrawable(ColorDrawable(resources.getColor(R.color.primary_dark)))

            binding.controlButtons.nextButton.setOnClickListener {
                val inflater = layoutInflater
                val view = inflater.inflate(R.layout.custom_alert_dialog, null)
                val builder = AlertDialog.Builder(requireActivity())
                val title = view.findViewById<TextView>(R.id.labelConfirmar)
                val Subtitle = view.findViewById<TextView>(R.id.labelsubtitle)
                val email = view.findViewById<TextView>(R.id.labelEmail)
                val butonAceptar = view.findViewById<Button>(R.id.Aceptar)
                val butonCancelar = view.findViewById<Button>(R.id.Cancel)
                title.text = "CONFIRMAR APROBACIÓN"
                Subtitle.visibility = View.GONE
                email.visibility = View.GONE
                builder.setView(view)
                val alertDialog =builder.create()
                alertDialog.show()
                butonCancelar.setOnClickListener {
                    alertDialog.dismiss()
                }
                butonAceptar.setOnClickListener {
                    val jsonObject = JSONObject()
                    try {
                        jsonObject.put("IndividualContractId", IdContract)
                        jsonObject.put("StageTypeId", requestStageTypeId("APROBADO"))
                    } catch (e: JSONException) {
                        DisableComponents(true)
                        e.printStackTrace()
                    }
                    CrudIntentService.startRequestCRUD(
                            requireActivity(),
                            Constantes.REQUEST_INDIVIDUAL_CONTRACT_ADDSTAGE_URL, Request.Method.POST, jsonObject.toString(), "", false
                    )
                    CrudIntentService.startRequestCRUD(
                            requireActivity(),
                            Constantes.REQUEST_APROVED_HIRING_DOCUMENT_URL + IdContract, Request.Method.POST, "", "", false
                    )
                    showProgress(true)
                    alertDialog.dismiss()
                }
            }
            binding.controlButtons.backButton.setOnClickListener {
                val inflater = layoutInflater
                val view = inflater.inflate(R.layout.alert_dialog_rejected, null)
                val builder = AlertDialog.Builder(requireActivity())
                val comment = view.findViewById<EditText>(R.id.commentRejected)
                val butonAceptar = view.findViewById<Button>(R.id.Aceptar)
                val butonCancelar = view.findViewById<Button>(R.id.Cancel)
                builder.setView(view)
                val alertDialog = builder.create()
                alertDialog.show()
                butonCancelar.setOnClickListener {
                    alertDialog.dismiss()
                }
                butonAceptar.setOnClickListener {
                    DisableComponents(true)
                    val jsonObject = JSONObject()
                    try {
                        jsonObject.put("IndividualContractId", IdContract)
                        jsonObject.put("StageTypeId", requestStageTypeId("RECHAZADO"))
                        if(comment.text.toString().isNotEmpty()){
                            jsonObject.put("Comment", comment.text.toString())
                        }else{
                            jsonObject.put("Comment", "")
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                    CrudIntentService.startRequestCRUD(
                            requireActivity(),
                            Constantes.REQUEST_INDIVIDUAL_CONTRACT_ADDSTAGE_URL, Request.Method.POST, jsonObject.toString(), "", false
                    )
                    CrudIntentService.startRequestCRUD(
                            requireActivity(),
                            Constantes.REQUEST_REJECT_HIRING_DOCUMENT_URL + IdContract, Request.Method.POST, "", "", false
                    )
                    showProgress(true)
                    alertDialog.dismiss()
                }
            }

    }

    override fun onResume() {
        super.onResume()
        var intentFilter = IntentFilter()
        intentFilter.addAction(CRUDService.ACTION_REQUEST_SAVE)
        intentFilter.addAction(CRUDService.ACTION_REQUEST_GET)
        intentFilter.addAction(CrudIntentService.ACTION_REQUEST_POST)
        intentFilter.addAction(CrudIntentService.ACTION_REQUEST_PUT)
        LocalBroadcastManager.getInstance(requireActivity())
                .registerReceiver(requestBroadcastReceiver!!, intentFilter)
    }

    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(requireActivity()).unregisterReceiver(requestBroadcastReceiver!!)
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
    private fun ValidatePermission(): Boolean{
        var permission = false
        if(!user!!.claims.contains("contractingdata.requestdelete") || !user!!.IsSuperUser){
         permission  = true
        }
        return permission
    }
    private fun ValidatePermissionAprroved(): Boolean{
        var permission = false
        if (user!!.claims.contains("contractingdata.approvereject") || user!!.IsSuperUser) {
            permission = true
        }
        return permission
    }
    private fun DisableComponents(Disable: Boolean){
        if(Disable){
            binding.typeContractCompleteTextView.isEnabled = false
            binding.typeContract.endIconMode = TextInputLayout.END_ICON_NONE

            binding.typePositionCompleteTextView.isEnabled = false
            binding.typePosition.endIconMode = TextInputLayout.END_ICON_NONE
            binding.salaryEdText.isEnabled = false
            binding.payPeriodCompleteTextView.isEnabled = false
            binding.payPeriod.endIconMode = TextInputLayout.END_ICON_NONE
            binding.edtInitDate.isEnabled = false
            binding.cityContractCompleteTextView.isEnabled = false
            binding.CityContract.endIconMode = TextInputLayout.END_ICON_NONE
            binding.countryContractcCompleteTextView.isEnabled = false
            binding.CountryContract.endIconMode = TextInputLayout.END_ICON_NONE
            binding.placeJobCompleteTextView.isEnabled = false
            binding.PlaceJob.endIconMode = TextInputLayout.END_ICON_NONE
        }else {
            binding.typeContractCompleteTextView.isEnabled = true
            binding.typeContract.endIconMode = TextInputLayout.END_ICON_CLEAR_TEXT
            binding.typePositionCompleteTextView.isEnabled = true
            binding.typePosition.endIconMode = TextInputLayout.END_ICON_CLEAR_TEXT
            binding.salaryEdText.isEnabled = true
            binding.payPeriodCompleteTextView.isEnabled = true
            binding.payPeriod.endIconMode = TextInputLayout.END_ICON_CLEAR_TEXT
            binding.edtInitDate.isEnabled = true
            binding.countryContractcCompleteTextView.isEnabled = true
            binding.CountryContract.endIconMode = TextInputLayout.END_ICON_CLEAR_TEXT
            binding.cityContractCompleteTextView.isEnabled = true
            binding.CityContract.endIconMode = TextInputLayout.END_ICON_CLEAR_TEXT
            binding.placeJobCompleteTextView.isEnabled = true
            binding.PlaceJob.endIconMode = TextInputLayout.END_ICON_CLEAR_TEXT
        }
    }
    fun requestStageTypeId(args : String): Int{
        val selection = ("(" + DBHelper.COPTIONS_COLUMN_DESC + " = ?)")
        val selectionArgs = arrayOf(args)
        val cursor = requireActivity().applicationContext.contentResolver.query(Constantes.CONTENT_COMMON_OPTIONS_URI,null,selection,selectionArgs,null)
        cursor?.moveToFirst()
        val stageId = cursor?.getInt(cursor?.getColumnIndex(DBHelper.COPTIONS_COLUMN_SERVER_ID))
        return stageId!!
    }
}
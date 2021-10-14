package co.tecno.sersoluciones.analityco.fragments.personal.wizard

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.content.Context
import android.content.IntentFilter
import android.graphics.drawable.PictureDrawable
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import co.com.sersoluciones.barcodedetectorser.utilities.DebugLog
import co.com.sersoluciones.barcodedetectorser.utilities.DebugLog.logW
import co.tecno.sersoluciones.analityco.*
import co.tecno.sersoluciones.analityco.adapters.CustomArrayAdapter
import co.tecno.sersoluciones.analityco.databinding.FragmentJoinPersonalContractBinding
import co.tecno.sersoluciones.analityco.models.*
import co.tecno.sersoluciones.analityco.receivers.RequestBroadcastReceiver
import co.tecno.sersoluciones.analityco.receivers.RequestBroadcastReceiver.BroadcastListener
import co.tecno.sersoluciones.analityco.services.CRUDService
import co.tecno.sersoluciones.analityco.utilities.*
import com.android.volley.Request
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import java.text.Normalizer
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

/**
 * A simple [Fragment] subclass.
 */
class JoinPersonalContractFragment : Fragment(), OnDateSetListener, BroadcastListener {

    private var toDate: Date? = null
    private var fromDate: Date? = null
    private var fromDateStr: String? = null
    private var toDateStr: String? = null
    private var fromDatePickerDialog: DatePickerDialog? = null
    private var toDatePickerDialog: DatePickerDialog? = null
    private var positionList: ArrayList<Position> = ArrayList<Position>()
    private var contract: ContractEnrollment? = null
    private var contractType: ContractType? = null
    private var startDate: String? = null
    private var finishDate: String? = null
    private var requestBroadcastReceiver: RequestBroadcastReceiver? = null
    private var personal: Personal? = null
    private var startDateContract: Date? = null
    private var finishDateContract: Date? = null
    private var mListener: OnJoinContractListener? = null
    private var personalContract: PersonalContract? = null
    private var moveToPersonal = false
    private var urlResponse: String? = null
    private var typePermission: String? = null
    private var setDefaultDate = false
    private var adapterPosition: ArrayAdapter<Position>? = null

    private lateinit var binding: FragmentJoinPersonalContractBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        moveToPersonal = false
        toDate = Date()
        fromDate = Date()
        requestBroadcastReceiver = RequestBroadcastReceiver(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentJoinPersonalContractBinding.inflate(inflater)
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        adapterPosition = ArrayAdapter<Position>(requireContext(), android.R.layout.simple_list_item_1, positionList)
        binding.editPosition.setAdapter(adapterPosition)
        binding.editPosition.onItemClickListener = OnItemClickListener { adapterView, view, position, id ->
            val positionJob = adapterView.getItemAtPosition(position) as Position
            binding.editPosition.setText(positionJob.Name)
        }

        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        fromDate = calendar.time
        calendar.add(Calendar.DAY_OF_MONTH, 1)
        toDate = calendar.time
        updateDatePicker(fromDate, toDate)
        val tipoRiesgos = arrayOf("Seleccione", "V", "IV", "III", "II", "I")
        binding.spinnerTypeRisk.adapter = CustomArrayAdapter(requireActivity(), tipoRiesgos)
        binding.spinnerTypeRisk.setSelection(1)

        binding.btnFromDate.setOnClickListener { fromDateDialog() }
        binding.btnToDate.setOnClickListener { toDateDialog() }
    }


    fun configFrament(
        contractType: ContractType, contract: ContractEnrollment, personal: Personal?,
        personalContract: PersonalContract?, companyId: String?, positionList: ArrayList<Position>?,
        moveToPersonal: Boolean, typePermission: String?
    ) {
        this.contract = contract
        startDate = contract.StartDate
        finishDate = contract.FinishDate
        this.contractType = contractType

        positionList?.let {
            this.positionList = positionList
            adapterPosition = ArrayAdapter<Position>(requireContext(), android.R.layout.simple_list_item_1, this.positionList)
            binding.editPosition.setAdapter(adapterPosition)
        }

        this.personal = personal
        this.moveToPersonal = moveToPersonal
        this.personalContract = personalContract
        this.typePermission = typePermission
        @SuppressLint("SimpleDateFormat") val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
        try {
            startDateContract = format.parse(startDate!!)
            finishDateContract = format.parse(finishDate!!)
            logW("startDateContract $startDateContract finishDateContract $finishDateContract")
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        binding.textName.text = contract.ContractReview
        binding.textSubName.text = contract.CompanyName
        val myFormat = "dd-MMM-yyyy"
        val sdf = SimpleDateFormat(myFormat, Locale("es", "ES"))
        binding.textIcon1.text = sdf.format(finishDateContract!!)
        binding.textIcon2.text = contract.ContractNumber
        when (contractType.Value) {
            "AD" -> binding.icon1.setImageResource(R.drawable.ic_02_administrativotrasparente2)
            "FU" -> binding.icon1.setImageResource(R.drawable.ic_14_funcionariotrasparente)
            "PR" -> binding.icon1.setImageResource(R.drawable.ic_10_proveedor_trasparente)
            "AS" -> binding.icon1.setImageResource(R.drawable.ic_04_asociado_trasparente2)
            "VI" -> binding.icon1.setImageResource(R.drawable.ic_16_visitante_trasparente)
            "OT" -> binding.icon1.setImageResource(R.drawable.ic_18_otros_trasparente)
            "CO" -> binding.icon1.setImageResource(R.drawable.ic_06_contratista_trasparente)
        }
        if (contract.FormImageLogo != null) {
            val url = Constantes.URL_IMAGES + contract.FormImageLogo
            val cFormat = url.split(Pattern.quote(".").toRegex()).toTypedArray()
            if (cFormat[cFormat.size - 1] == "svg") {
//                Uri uri = Uri.parse(url);
                GlideApp.with(requireContext())
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
        permission()
        configPerson()
    }

    @SuppressLint("SetTextI18n")
    private fun configPerson() {
        cleanForm()
        startDate = ""
        finishDate = ""
        fromDateStr = ""
        toDateStr = ""
        val myFormat = "dd/MMM/yyyy"
        val sdf = SimpleDateFormat(myFormat, Locale("es", "ES"))
        if (contractType!!.Value == "AD" || contractType!!.Value == "CO" || contractType!!.Value == "PR") {
            if (startDateContract != null && finishDateContract != null) {
                if (startDateContract!!.time > fromDate!!.time) fromDate = startDateContract
                fromDateStr = sdf.format(fromDate!!.time)
                binding.btnFromDate.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
                binding.btnFromDate.text = fromDateStr
                toDate = finishDateContract
                toDateStr = sdf.format(toDate!!.time)
                binding.btnToDate.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
                binding.btnToDate.text = toDateStr
            }
        } else {
            fromDateStr = sdf.format(fromDate!!.time)
            binding.btnFromDate.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
            binding.btnFromDate.text = fromDateStr


            toDate = Utils.getLastTimeDay()
            toDateStr = sdf.format(toDate!!.time)
            binding.btnToDate.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
            binding.btnToDate.text = toDateStr
        }
        binding.editPosition.setText("Cargo Desconocido")
        if (personalContract != null) {
            if (personalContract!!.Position != null) binding.editPosition.setText(personalContract!!.Position)
            if (personalContract!!.StartDate != null && personalContract!!.FinishDate != null) {
                @SuppressLint("SimpleDateFormat") val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                try {
                    if (setDefaultDate) {
                        fromDate = format.parse(personalContract!!.StartDate)
                        toDate = format.parse(personalContract!!.FinishDate)
                    }
                    fromDateStr = sdf.format(fromDate!!.time)
                    binding.btnFromDate.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
                    binding.btnFromDate.text = fromDateStr
                    binding.btnFromDate.error = null
                    binding.tvFromDateError.visibility = View.GONE
                    binding.tvFromDateError.error = null
                    toDateStr = sdf.format(toDate!!.time)
                    binding.btnToDate.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
                    binding.btnToDate.text = toDateStr
                    binding.btnToDate.error = null
                    binding.tvToDateError.visibility = View.GONE
                    binding.tvToDateError.error = null
                } catch (e: ParseException) {
                    e.printStackTrace()
                }
            }
        }
        logW("fromDate $fromDate toDate $toDate")
        updateDatePicker(fromDate, toDate)
        if (personal!!.Photo != null && personal!!.Photo!!.isNotEmpty()) {
            val url = Constantes.URL_IMAGES + personal!!.Photo
            Picasso.get().load(url)
                .resize(0, 250)
                .transform(CircleTransform())
                .placeholder(R.drawable.profile_dummy)
                .error(R.drawable.profile_dummy)
                .into(binding.iconLogo)
        }
    }

    fun fromDateDialog() {
        fromDatePickerDialog!!.datePicker.tag = R.id.btn_from_date
        fromDatePickerDialog!!.show()
    }

    fun toDateDialog() {
        toDatePickerDialog!!.datePicker.tag = R.id.btn_to_date
        toDatePickerDialog!!.show()
    }

    private fun updateDatePicker(fromDate: Date?, toDate: Date?) {
        val currentDate = MetodosPublicos.getCurrentDate()
        val cF = Calendar.getInstance()
        cF.time = fromDate!!
        val cT = Calendar.getInstance()
        cT.time = toDate!!
        fromDatePickerDialog = DatePickerDialog(
            requireContext(), this, cF[Calendar.YEAR],
            cF[Calendar.MONTH], cF[Calendar.DAY_OF_MONTH]
        )
        fromDatePickerDialog!!.datePicker.minDate = currentDate.time
        toDatePickerDialog = DatePickerDialog(
            requireContext(), this, cT[Calendar.YEAR],
            cT[Calendar.MONTH], cT[Calendar.DAY_OF_MONTH]
        )
        if (startDateContract != null && startDateContract!!.time > fromDate.time) toDatePickerDialog!!.datePicker.minDate =
            startDateContract!!.time else toDatePickerDialog!!.datePicker.minDate = currentDate.time
        if (finishDateContract != null) toDatePickerDialog!!.datePicker.maxDate = finishDateContract!!.time
    }

    fun getPersonalContract(): PersonalContract? {
        if (validateForm()) {
            @SuppressLint("SimpleDateFormat") val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
            val calendar = Calendar.getInstance()
            if (fromDateStr!!.isNotEmpty()) {
                calendar.time = fromDate!!
                calendar[Calendar.HOUR_OF_DAY] = 0
                calendar[Calendar.MINUTE] = 0
                calendar[Calendar.SECOND] = 0
                fromDateStr = format.format(calendar.time)
            } else fromDateStr = null
            if (toDateStr!!.isNotEmpty()) {
                calendar.time = toDate!!
                calendar[Calendar.HOUR_OF_DAY] = 23
                calendar[Calendar.MINUTE] = 59
                calendar[Calendar.SECOND] = 59
                toDateStr = format.format(calendar.time)
            } else toDateStr = null
            val personalContract = PersonalContract(
                fromDateStr, toDateStr, personal!!.PersonalCompanyInfoId,
                binding.editPosition.text.toString(), binding.spinnerTypeRisk.selectedItem.toString()
            )
            val json = Gson().toJson(personalContract)
            DebugLog.logW(json)
            return personalContract
        }
        return null
    }

    @SuppressLint("SetTextI18n")
    private fun validateForm(): Boolean {
        var focusView: View? = null
        var cancel = false
        binding.textError.text = ""
        if (TextUtils.isEmpty(binding.editPosition.text.toString())) {
            cancel = true
            binding.editPosition.error = getString(R.string.error_field_required)
            focusView = binding.editPosition
        } /*else if (fromDateStr.isEmpty()) {
            fromDateBtn.setError(getString(R.string.error_field_required));
            fromDateBtn.requestFocus();

            tvFromDateError.setVisibility(View.VISIBLE);
            tvFromDateError.setError(getString(R.string.error_field_required));
            focusView = tvFromDateError;
            cancel = true;
        } */
        if (fromDateStr!!.isNotEmpty() && toDateStr!!.isEmpty()) {
            binding.btnToDate.error = getString(R.string.error_field_required)
            binding.btnToDate.requestFocus()
            binding.tvToDateError.visibility = View.VISIBLE
            binding.tvToDateError.error = getString(R.string.error_field_required)
            focusView = binding.tvToDateError
            cancel = true
        }
        if (fromDateStr!!.isNotEmpty() && fromDate!!.time > toDate!!.time) {
            binding.btnToDate.error = "La fecha final debe ser mayor a la de inicio"
            binding.btnToDate.requestFocus()
            binding.tvToDateError.visibility = View.VISIBLE
            binding.tvToDateError.error = "La fecha final debe ser mayor a la de inicio"
            focusView = binding.tvToDateError
            cancel = true
        }
        if (fromDateStr!!.isNotEmpty() && fromDate!!.time < startDateContract!!.time) {
            binding.btnFromDate.error = "La fecha no puede ser antes del inicio del contrato"
            binding.btnFromDate.requestFocus()
            binding.tvFromDateError.visibility = View.VISIBLE
            binding.tvFromDateError.error = "La fecha no puede ser antes del inicio del contrato"
            focusView = binding.tvFromDateError
            cancel = true
        }
        if (toDateStr!!.isNotEmpty() && toDate!!.time > finishDateContract!!.time) {
            binding.btnToDate.error = "La fecha no puede ser mayor al fin del contrato"
            binding.btnToDate.requestFocus()
            val myFormat = "dd/MMM/yyyy HH:mm:ss"
            val sdf = SimpleDateFormat(myFormat, Locale("es", "ES"))
            binding.textError.text = "La fecha no puede ser mayor a " + sdf.format(finishDateContract!!.time)
            DebugLog.logW("le fecha final es: " + sdf.format(toDate!!.time))
            binding.tvToDateError.visibility = View.VISIBLE
            binding.tvToDateError.error = "La fecha no puede ser mayor a " + sdf.format(finishDateContract!!.time)
            focusView = binding.tvToDateError
            cancel = true
        }
        //        else if ((contractType.Value.equals("AD") || contractType.Value.equals("AS") || contractType.Value.equals("CO")) && TextUtils.isEmpty(positionString.getText().toString())) {
//            cancel = true;
//            positionString.setError("Cargo para este tipo de contrato es obligatorio");
//            focusView = positionString;
//        }
        if (cancel) {
            focusView!!.requestFocus()
        }
        return !cancel
    }

    private fun cleanForm() {
        binding.editPosition.setText("")
        binding.spinnerTypeRisk.setSelection(1)
    }

    override fun onDateSet(view: DatePicker, year: Int, month: Int, dayOfMonth: Int) {
        val code = view.tag as Int
        val calendar = Calendar.getInstance()
        calendar[year, month] = dayOfMonth
        val myFormat = "dd/MMM/yyyy"
        val sdf = SimpleDateFormat(myFormat, Locale("es", "ES"))
        when (code) {
            R.id.btn_from_date -> {
                fromDateStr = sdf.format(calendar.time)
                binding.btnFromDate.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
                binding.btnFromDate.text = fromDateStr
                fromDate = calendar.time
                binding.btnFromDate.error = null
                binding.tvFromDateError.visibility = View.GONE
                binding.tvFromDateError.error = null
            }
            R.id.btn_to_date -> {
                toDateStr = sdf.format(calendar.time)
                binding.btnToDate.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
                binding.btnToDate.text = toDateStr
                toDate = calendar.time
                binding.btnToDate.error = null
                binding.tvToDateError.visibility = View.GONE
                binding.tvToDateError.error = null
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val intentFilter = IntentFilter()
        intentFilter.addAction(CRUDService.ACTION_REQUEST_POST)
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
            requestBroadcastReceiver!!,
            intentFilter
        )
    }

    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(requestBroadcastReceiver!!)
    }

    @SuppressLint("SetTextI18n")
    fun submit() {
        val calendar = Calendar.getInstance()
        if (validateForm()) {
            @SuppressLint("SimpleDateFormat") val format = SimpleDateFormat("yyyy-MM-dd")
            if (fromDateStr!!.isNotEmpty()) {
                calendar.time = fromDate!!
                calendar[Calendar.HOUR_OF_DAY] = 0
                calendar[Calendar.MINUTE] = 0
                calendar[Calendar.SECOND] = 0
                fromDateStr = format.format(calendar.time)
            } else fromDateStr = null
            if (toDateStr!!.isNotEmpty()) {
                calendar.time = toDate!!
                calendar[Calendar.HOUR_OF_DAY] = 23
                calendar[Calendar.MINUTE] = 59
                calendar[Calendar.SECOND] = 59
                toDateStr = format.format(calendar.time)
            } else toDateStr = null
            val personalContract = PersonalContract(
                fromDateStr, toDateStr, personal!!.PersonalCompanyInfoId,
                binding.editPosition.text.toString(), binding.spinnerTypeRisk.selectedItem.toString()
            )
            if (activity is JoinPersonalWizardActivity) (activity as JoinPersonalWizardActivity?)!!.showProgress(
                true
            ) else if (activity is JoinPersonalContractActivity) (activity as JoinPersonalContractActivity?)!!.showProgress(true) else if (activity is JoinPersonalProjectWizardActivity) {
                personalContract.EmployerId = (activity as JoinPersonalProjectWizardActivity?)!!.employerId
                (activity as JoinPersonalProjectWizardActivity?)!!.showProgress(true)
            }
            val gson = Gson()
            val json = gson.toJson(personalContract)
            DebugLog.logW(json)
            var url = Constantes.LIST_CONTRACTS_URL + contract!!.Id + "/PersonalInfo/"
            if (moveToPersonal) {
                url = Constantes.LIST_CONTRACTS_URL + contract!!.Id + "/MovePersonalInfo/"
            }
            CRUDService.startRequest(
                activity, url,
                Request.Method.POST, json
            )
        }
    }

    override fun onStringResult(action: String?, option: Int, response: String?, url: String?) {
        urlResponse = url
        when (action) {
            CRUDService.ACTION_REQUEST_GET -> {
            }
            CRUDService.ACTION_REQUEST_POST, CRUDService.ACTION_REQUEST_PUT, CRUDService.ACTION_REQUEST_DELETE -> if (contract != null && (url == Constantes.LIST_CONTRACTS_URL + contract!!.Id + "/PersonalInfo/" || url == Constantes.LIST_CONTRACTS_URL + contract!!.Id + "/MovePersonalInfo/")) processRequestAll(
                option
            )
            CRUDService.ACTION_REQUEST_FORM_DATA -> {
            }
            CRUDService.ACTION_REQUEST_SAVE -> {
            }
        }
    }

    private fun processRequestAll(option: Int) {
        when (option) {
            Constantes.SUCCESS_REQUEST -> {
                //mListener.onSuccessJoinContract();
                Toast.makeText(activity, "Empleado asociado exitosamente", Toast.LENGTH_SHORT).show()
                if (activity is JoinPersonalWizardActivity) (activity as JoinPersonalWizardActivity?)!!.submitCreate()
                else if (activity is JoinPersonalContractActivity) (activity as JoinPersonalContractActivity?)!!.submitCreate()
                else if (activity is JoinPersonalProjectWizardActivity) (activity as JoinPersonalProjectWizardActivity?)!!.submitCreate(personal!!.PersonalCompanyInfoId)
            }
            Constantes.BAD_REQUEST -> {
                DebugLog.logW("CASE 400")
                if (urlResponse == Constantes.LIST_CONTRACTS_URL + contract!!.Id + "/PersonalInfo/") {
                    val alertDialog = AlertDialog.Builder(requireContext())
                    alertDialog.setCancelable(false)
                    alertDialog.setTitle("Mensaje")
                    alertDialog.setMessage("La persona esta enrolada y activa en otro contrato de este mismo proyecto \n¿Desea trasladarlo?")
                    alertDialog.setPositiveButton("SI") { _, _ ->
                        if (!contractType!!.Value.isEmpty() && (contractType!!.Value == "AD" || contractType!!.Value == "AS" || contractType!!.Value == "VI" || contractType!!.Value == "FU" || contractType!!.Value == "PR" || contractType!!.Value == "CO" || contractType!!.Value == "VI" || contractType!!.Value == "OT")) moveToPersonal = true
                        submit()
                    }
                    alertDialog.setNegativeButton("NO") { dialog, which ->
                        dialog.dismiss()
                        requireActivity().finish()
                    }
                    alertDialog.create().show()
                }
            }
            Constantes.NOT_INTERNET, Constantes.REQUEST_NOT_FOUND -> {
            }
            Constantes.FORBIDDEN -> {
                Toast.makeText(requireContext(), "Este usuario no tiene permisos para esta accion", Toast.LENGTH_SHORT).show()
                activity?.finish()
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mListener = if (context is OnJoinContractListener) {
            context
        } else {
            throw RuntimeException(
                context.toString()
                        + " must implement OnJoinContractListener"
            )
        }
    }

    interface OnJoinContractListener {
        fun onSuccessJoinContract()
    }

    private fun permission() {
        if (typePermission == null || typePermission!!.isEmpty()) return

        val claims = AppPermissions.getPermissionsOfUser(requireContext(), companyAdminId = personal!!.CompanyInfoId)
        validateClaims(claims)

        if (!setDefaultDate) {
            finishDateContract = Utils.getLastTimeDay()
            startDateContract = Utils.getFirstTimeDay()
        }
    }

    private fun configDateButtons(enabled: Boolean) {
        if (enabled) {
            binding.btnFromDate.isEnabled = true
            binding.btnFromDate.isClickable = true
            binding.btnToDate.isEnabled = true
            binding.btnToDate.isClickable = true
        } else {
            binding.btnFromDate.isEnabled = false
            binding.btnFromDate.isClickable = false
            binding.btnToDate.isEnabled = false
            binding.btnToDate.isClickable = false
        }
    }

    private fun validateClaims(claims: ArrayList<String>) {
        if (typePermission == "contract") {
            // permiso editar vigencia contrato
            setDefaultDate = if (claims.contains("contractspersonalsvalidity.config")) {
                configDateButtons(true)
                true
            } else {
                configDateButtons(false)
                false
            }
            // permiso editar tipo riesgo
            binding.spinnerTypeRisk.isEnabled = claims.contains("contractspersonalsrisk.config")
        } else if (typePermission == "personal") {
            // permiso editar vigencia contrato
            setDefaultDate = if (claims.contains("personalscontractsvalidity.config")) {
                configDateButtons(true)
                true
            } else {
                configDateButtons(false)
                false
            }
            // permiso editar cargo
            binding.editPosition.isEnabled = claims.contains("personalscontractsposition.config")
        }
    }
}
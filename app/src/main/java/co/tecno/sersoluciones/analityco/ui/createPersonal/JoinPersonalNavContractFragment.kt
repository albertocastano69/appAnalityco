    package co.tecno.sersoluciones.analityco.ui.createPersonal

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.content.Context
import android.content.Intent
import android.graphics.drawable.PictureDrawable
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import android.widget.DatePicker
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import co.com.sersoluciones.barcodedetectorser.utilities.DebugLog
import co.com.sersoluciones.barcodedetectorser.utilities.DebugLog.logW
import co.tecno.sersoluciones.analityco.ApplicationContext
import co.tecno.sersoluciones.analityco.GlideApp
import co.tecno.sersoluciones.analityco.R
import co.tecno.sersoluciones.analityco.adapters.CustomArrayAdapter
import co.tecno.sersoluciones.analityco.databinding.FragmentJoinNavPersonalContractBinding
import co.tecno.sersoluciones.analityco.models.*
import co.tecno.sersoluciones.analityco.nav.CreatePersonalViewModel
import co.tecno.sersoluciones.analityco.nav.LoadingApiStatus
import co.tecno.sersoluciones.analityco.nav.ResponseResult
import co.tecno.sersoluciones.analityco.utilities.AppPermissions
import co.tecno.sersoluciones.analityco.utilities.Constantes
import co.tecno.sersoluciones.analityco.utilities.MetodosPublicos
import co.tecno.sersoluciones.analityco.utilities.Utils
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern
import javax.inject.Inject

@Suppress("DEPRECATION")
class JoinPersonalNavContractFragment : Fragment(), OnDateSetListener {

    private var toDate: Date? = null
    private var fromDate: Date? = null
    private var fromDateStr: String? = null
    private var toDateStr: String? = null
    private var fromDatePickerDialog: DatePickerDialog? = null
    private var toDatePickerDialog: DatePickerDialog? = null
    private var contract: ContractEnrollment? = null
    private var startDate: String? = null
    private var finishDate: String? = null
    private var startDateContract: Date? = null
    private var finishDateContract: Date? = null
    private var personalContract: PersonalContract? = null
    private var typePermission: String? = null
    private var contractType: String? = null
    private var setDefaultDate = false
    private var path: String = ""
    private var personal: PersonalNetwork? = null
    private var dialog: AlertDialog? = null
    private var adapterPosition: ArrayAdapter<Position>? = null
    private lateinit var binding: FragmentJoinNavPersonalContractBinding

    @Inject
    lateinit var viewModel: CreatePersonalViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        toDate = Date()
        fromDate = Date()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentJoinNavPersonalContractBinding.inflate(inflater)
        binding.lifecycleOwner = this
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.personal = viewModel.personal.value
        val arguments = JoinPersonalNavContractFragmentArgs.fromBundle(requireArguments())

        val contract = arguments.contract
        val personalContract = arguments.personalContract

        viewModel.positions.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            adapterPosition = ArrayAdapter<Position>(requireContext(), android.R.layout.simple_list_item_1, it)
            binding.editPosition.setAdapter(adapterPosition)
        })

        binding.editPosition.onItemClickListener = OnItemClickListener { adapterView, _, position, _ ->
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
        contractType = contract.ContractType
        val idContract = contract.Id
        logW("Id del contract $idContract")
        binding.btnFromDate.setOnClickListener { fromDateDialog() }
        binding.btnToDate.setOnClickListener { toDateDialog() }
        if(viewModel.Onecontract){
            binding.controlButtons.backButton.isEnabled=false
        }else{
            binding.controlButtons.backButton.setOnClickListener { findNavController().popBackStack() }
        }
        binding.controlButtons.nextButton.setOnClickListener {
            if (contractType == "AD" || contractType == "AS")
                submit()
            else {
                if (validateForm()) {
                    this.personalContract = getPersonalContractToSend()
                    this.personalContract?.let {
                        viewModel.navigateToSelectEmployer()
                    }
                }
            }
        }
        if (contractType == "AD" || contractType == "AS")
            binding.controlButtons.nextButton.text = "VINICULAR"

        viewModel.navigateToRequirementFragment.observe(viewLifecycleOwner,androidx.lifecycle.Observer {
            it?.let {
                if (it) {
                    if (idContract != null) {
                        findNavController()
                                .navigate(
                                        JoinPersonalNavContractFragmentDirections.actionJoinPersonalNavContractFragmentToRequerimentsFragment(idContract)

                                )
                        viewModel.doneNavigatoRequeriments()
                    }else{
                        logW("Id del contract $idContract")
                    }
                }
            }
        })

        viewModel.navigateToSelectEmployerFragment.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            it?.let {
                if (it) {
                    findNavController()
                        .navigate(
                            JoinPersonalNavContractFragmentDirections.actionJoinPersonalNavContractFragmentToSelectNavEmployerFragment(
                                contract, viewModel.movePersonal, this@JoinPersonalNavContractFragment.personalContract!!
                            )
                        )
                    viewModel.doneNavigatingEmployer()
                }
            }
        })
        viewModel.msgPersonToMove.observe(::getLifecycle, ::evaluateResponseIsInContract)
        viewModel.responseJoinContract.observe(::getLifecycle, ::processRequestAll)
        configFragment(contract, personalContract = personalContract)
        if (contractType == "VI" && viewModel.externalData) {
//            this.personalContract = getPersonalContract()
            submit()
        }

    }



    private fun configFragment(
        contract: ContractEnrollment, typePermission: String = "personal", personalContract: PersonalContract? = null
    ) {
        this.contract = contract
        this.personalContract = personalContract
        startDate = contract.StartDate
        finishDate = contract.FinishDate
        this.personal = viewModel.personal.value

        (requireActivity() as AppCompatActivity).supportActionBar?.title = "CC ${personal!!.DocumentNumber}"
        (requireActivity() as AppCompatActivity).supportActionBar?.subtitle = "${personal!!.Name} ${personal!!.LastName}"
        this.typePermission = typePermission
        @SuppressLint("SimpleDateFormat")
        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
        try {
            startDateContract = format.parse(startDate!!)
            val calendar = Calendar.getInstance()
            calendar.time = startDateContract!!
            calendar[Calendar.HOUR_OF_DAY] = 0
            calendar[Calendar.MINUTE] = 0
            calendar[Calendar.SECOND] = 0
            startDateContract = calendar.time
            finishDateContract = format.parse(finishDate!!)
            logW("startDateContract $startDateContract finishDateContract $finishDateContract")
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        binding.textName.text = contract.ContractReview
        //binding.textSubName.text = contract.CompanyName
        binding.textSubName.text = contract.ContractorName
        val myFormat = "dd-MMM-yyyy"
        val sdf = SimpleDateFormat(myFormat, Locale("es", "ES"))
        binding.textIcon1.text = sdf.format(finishDateContract!!)
        binding.textIcon2.text = contract.ContractNumber
        when (contract.ContractType) {
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
            val arrayUrl = url.split(Pattern.quote(".").toRegex()).toTypedArray()
            if (arrayUrl[arrayUrl.size - 1] == "svg") {
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
                    .placeholder(R.drawable.loading_animation)
                    .error(R.drawable.image_not_available)
                    .into(binding.logo)
            }
        }

        permission()
        configPerson(contract.ContractType!!)
    }

    private fun evaluateResponseIsInContract(result: String?) {
        result?.let {
            var errorResponse = result
            if (errorResponse.isNullOrEmpty()) return
            if (errorResponse == "null") {
                Toast.makeText(
                    requireActivity(), "Este proyecto carece de proyectos asociados", Toast.LENGTH_LONG
                ).show()
                viewModel.clearForm()
                activity?.finish()
            } else {
                errorResponse = errorResponse.replace("\\", "")
                logW("errorResponse $errorResponse")
                val obj = Gson().fromJson(errorResponse, IsContract::class.java)
                if (dialog != null) dialog!!.dismiss()
                val alertDialogBuilder = MaterialAlertDialogBuilder(requireActivity())
                alertDialogBuilder.setCancelable(false)
                alertDialogBuilder.setTitle("Mensaje")
                alertDialogBuilder.setMessage(
                    """
                            La persona esta enrolada y activa en el proyecto con el contrato ${obj.ContractNumber}
                            ¿Desea trasladarlo?
                            """.trimIndent()
                )
                alertDialogBuilder.setPositiveButton("SI") { dialog, _ ->
                    dialog.dismiss()
                }
                alertDialogBuilder.setNegativeButton("NO") { dialog, _ ->
                    Toast.makeText(
                        requireActivity(), "Proceso terminado sin asociar el empleado al contrato", Toast.LENGTH_LONG
                    ).show()
                    viewModel.clearForm()
                    activity?.finish()
                    dialog.dismiss()
                }
                dialog = alertDialogBuilder.create()
                dialog!!.show()
            }
            viewModel.clearMsg()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun configPerson(contractType: String) {
        cleanForm()
        startDate = ""
        finishDate = ""
        fromDateStr = ""
        toDateStr = ""
        val myFormat = "dd/MMM/yyyy"
        val sdf = SimpleDateFormat(myFormat, Locale("es", "ES"))
        if (contractType == "AD" || contractType == "CO" || contractType == "PR") {
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
            binding.editPosition.setText(personalContract!!.Position)
            if (personalContract!!.StartDate != null && personalContract!!.FinishDate != null) {
                @SuppressLint("SimpleDateFormat") val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                try {
                    if (setDefaultDate) {
                        fromDate = format.parse(personalContract!!.StartDate!!)
                        toDate = format.parse(personalContract!!.FinishDate!!)
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
    }

    fun fromDateDialog() {
        fromDatePickerDialog?.datePicker?.tag = R.id.btn_from_date
        if (!requireActivity().isFinishing)
            fromDatePickerDialog?.show()
    }

    fun toDateDialog() {
        toDatePickerDialog?.datePicker?.tag = R.id.btn_to_date
        if (!requireActivity().isFinishing)
            toDatePickerDialog?.show()
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

    private fun getPersonalContractToSend(): PersonalContract {
        val calendar = Calendar.getInstance()

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
//            if (activity is JoinPersonalProjectWizardActivity)
//                personalContract.EmployerId = (activity as JoinPersonalProjectWizardActivity?)!!.employerId
        val json = Gson().toJson(personalContract)
        logW(json)
        path = "PersonalInfo"
        if (viewModel.movePersonal) {
            path = "MovePersonalInfo"
        }
        return personalContract
    }


    private fun getPersonalContract(): PersonalContract? {
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
            logW(json)
            return personalContract
        }
        return null
    }

    @SuppressLint("SetTextI18n")
    private fun validateForm(): Boolean {
        var focusView: View? = null
        var cancel = false
        binding.textError.text = ""
        logW(finishDate)
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

            val myFormat = "dd/MMM/yyyy HH:mm:ss"
            val sdf = SimpleDateFormat(myFormat, Locale("es", "ES"))
            binding.textError.text = "La fecha no puede ser menor a " + sdf.format(startDateContract!!.time) + " " + sdf.format(fromDate!!.time)
        }
        if (toDateStr!!.isNotEmpty() && toDate!!.day > finishDateContract!!.day && viewModel.typeContract?.Value != "VI") {
            binding.btnToDate.error = "La fecha no puede ser mayor al fin del contrato"
            binding.btnToDate.requestFocus()
            val myFormat = "dd/MMM/yyyy HH:mm:ss"
            val sdf = SimpleDateFormat(myFormat, Locale("es", "ES"))
            binding.textError.text = "La fecha no puede ser mayor a " + sdf.format(finishDateContract!!.time)
            logW("le fecha final es: " + sdf.format(toDate!!.time))
            binding.tvToDateError.visibility = View.VISIBLE
            binding.tvToDateError.error = "La fecha no puede ser mayor a " + sdf.format(finishDateContract!!.time)
            focusView = binding.tvToDateError
            cancel = true
        }
        if (toDateStr!!.isNotEmpty() && toDate!!.time > finishDateContract!!.time && viewModel.typeContract?.Value == "VI") {
            binding.btnToDate.error = "La fecha no puede ser mayor al fin del contrato"
            binding.btnToDate.requestFocus()
            val myFormat = "dd/MMM/yyyy HH:mm:ss"
            val sdf = SimpleDateFormat(myFormat, Locale("es", "ES"))
            binding.textError.text = "La fecha no puede ser mayor a " + sdf.format(finishDateContract!!.time)
            logW("le fecha final es: " + sdf.format(toDate!!.time))
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

    @SuppressLint("SetTextI18n")
    fun submit() {
        if (validateForm()) {
            showProgress(true)
            this.personalContract = getPersonalContractToSend()
            viewModel.createPersonalContract(this.personalContract!!, contract!!.Id!!, path)

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

    private fun processRequestAll(response: ResponseResult<Any>?) {
        response?.let {
            when (response) {
                is ResponseResult.Success -> {
                    Toast.makeText(activity, "Empleado asociado exitosamente", Toast.LENGTH_SHORT).show()
                    val intent = Intent()
                    intent.putExtra("personalInfoId", personal!!.PersonalCompanyInfoId)
                    activity?.setResult(Activity.RESULT_OK, intent)
                    showProgress(false)
                    viewModel.navigateToRequirements()
               /*     viewModel.clearForm()
                    activity?.finish()*/
                }
                is ResponseResult.Error -> {
                    if (response.exception.code() == 400) {
                        if (path == "PersonalInfo") {
                            val alertDialog = MaterialAlertDialogBuilder(requireContext())
                            alertDialog.setCancelable(false)
                            alertDialog.setTitle("Mensaje")
                            alertDialog.setMessage("La persona esta enrolada y activa en otro contrato de este mismo proyecto \n¿Desea trasladarlo?")
                            alertDialog.setPositiveButton("SI") { _, _ ->
                                if (!contractType.isNullOrEmpty() && (contractType!! == "AD" || contractType!! == "AS"))
                                    viewModel.movePersonal = true
                                submit()
                            }
                            alertDialog.setNegativeButton("NO") { dialog, _ ->
                                dialog.dismiss()
                                Toast.makeText(activity, "Empleado sin asociar a este contrato", Toast.LENGTH_SHORT).show()
                                viewModel.clearForm()
                                requireActivity().finish()
                            }
                            alertDialog.create().show()
                        } else {

                        }
                    } else if (response.exception.code() == 403) {
                        Toast.makeText(requireContext(), "Este usuario no tiene permisos para esta accion", Toast.LENGTH_SHORT).show()
                        viewModel.clearForm()
                        activity?.finish()
                    } else {

                    }
                }
            }
        }
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

    override fun onAttach(context: Context) {
        super.onAttach(context)
        ApplicationContext.analitycoComponent.inject(this)
    }
}
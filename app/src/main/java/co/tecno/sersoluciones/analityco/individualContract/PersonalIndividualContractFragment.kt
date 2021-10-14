package co.tecno.sersoluciones.analityco.individualContract

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import co.com.sersoluciones.barcodedetectorser.utilities.DebugLog
import co.com.sersoluciones.facedetectorser.FaceTrackerActivity
import co.com.sersoluciones.facedetectorser.serlibrary.PhotoSer
import co.tecno.sersoluciones.analityco.ApplicationContext
import co.tecno.sersoluciones.analityco.R
import co.tecno.sersoluciones.analityco.databinding.FragmentPersonalIndividualcontractBinding
import co.tecno.sersoluciones.analityco.models.*
import co.tecno.sersoluciones.analityco.nav.CreatePersonalViewModel
import co.tecno.sersoluciones.analityco.ui.createPersonal.viewmodels.PersonalViewModel
import co.tecno.sersoluciones.analityco.utilities.MetodosPublicos
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_survey_details.*
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class PersonalIndividualContractFragment: Fragment(), DatePickerDialog.OnDateSetListener{
    private lateinit var binding: FragmentPersonalIndividualcontractBinding
    private var dateBirthPickerDialog: DatePickerDialog? = null
    private var infoUSER = ""

    @Inject
    lateinit var createPersonalViewModel: CreatePersonalViewModel

    @Inject
    lateinit var viewModel: PersonalViewModel

    //1 creacion 2 edicion
    private var process = 1

    //    private var mImageUri: Uri? = null
    private var dateBirth: Date? = null
    private var dateBirthedt: Date? = null

    private var jobCode: String? = null
    private var epsId = 0
    private var afpId = 0
    private var nationality = 0
    private var expecion:Boolean? = false
    private var PersonalId :String? = null
    private var PersonalEmployerInfoId : String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Handle the back button event
                cancel()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }


    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPersonalIndividualcontractBinding.inflate(inflater)
        binding.lifecycleOwner = this
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.viewModel = viewModel
        val arguments = PersonalIndividualContractFragmentArgs.fromBundle(requireArguments())

        (binding.nationalityAutoCompleteTextView as? TextView)?.text = "COLOMBIANO"
        (activity as AppCompatActivity).supportActionBar?.title = ""
        (activity as AppCompatActivity).supportActionBar?.subtitle = ""

        val personal = arguments.personal
        val infoUser = arguments.infoUser
        val docNumber = arguments.documentNumber.toString()
        infoUSER = infoUser.toString()
        PersonalEmployerInfoId = arguments.personalEmployerInfoId
        DebugLog.logW("docNumber ========== $docNumber")
        if (personal != null) {
            viewModel.setPersonal(personal)
            process = 2
            epsId = personal.EpsId!!
            afpId = personal.AfpId!!
            jobCode = personal.JobCode
            PersonalId = personal.PersonalId
            basicForm(personal)
            DebugLog.logW("docNumber ========== $docNumber")

        } else {
            process = 1
            binding.infoRhSex.visibility = View.GONE
            viewModel.personal.value!!.DocumentNumber = docNumber
            (activity as AppCompatActivity).supportActionBar?.title = docNumber
            DebugLog.logW("personal network ${Gson().toJson(viewModel.personal.value)}")
            infoUser?.let {
                DebugLog.logW("personal infoUser ${Gson().toJson(it)}")
                binding.infoRhSex.visibility = View.VISIBLE
                viewModel.personal.value!!.DocumentNumber = it.dni.toString()
                viewModel.personal.value!!.DocumentType = it.documentType
                viewModel.personal.value!!.Name = it.name
                viewModel.personal.value!!.LastName = it.lastname
                viewModel.personal.value!!.RH = it.rh
                viewModel.personal.value!!.Sex = it.sex
                viewModel.personal.value!!.BirthDate = it.birthDate.toString()
                if (!it.cityOfBirthCode.isNullOrEmpty())
                    viewModel.getCity(it.cityOfBirthCode)
                else
                    viewModel.getCity(it.stateCode, it.cityCode)
                basicForm(viewModel.personal.value!!)
            }
        }
        binding.iconLogo.setOnClickListener { startFaceDectectorActivity() }
        viewModel.isReady.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            if (it && process == 1) {
                jobCode = viewModel.genericJob?.Code
                epsId = viewModel.genericEps!!.Id
                afpId = viewModel.genericAfp!!.Id
                (binding.jobAutoCompleteTextView as TextView).text = viewModel.genericJob?.Name
            }
        })
        viewModel.jobs.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            DebugLog.logW("jobs count ${it.count()}")
            val jobAdapter = ArrayAdapter(requireContext(), R.layout.list_item, it)
            binding.jobAutoCompleteTextView.setAdapter(jobAdapter)
        })
        viewModel.daneCities.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            DebugLog.logW("dane cities count ${it.count()}")
            val daneCityAdapter = ArrayAdapter(requireContext(), R.layout.list_item, it)
            binding.daneCityAutoCompleteTextView.setAdapter(daneCityAdapter)
        })

        viewModel.cities.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            DebugLog.logW("cities count ${it.count()}")
            val citiesAdapter = ArrayAdapter(requireContext(), R.layout.list_item, it)
            binding.cityAutoCompleteTextView.setAdapter(citiesAdapter)
        })
        viewModel.cityPerson.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            val cityAdapter = ArrayAdapter(requireContext(), R.layout.list_item, it)
            binding.cityAutoCompleteTextView.setAdapter(cityAdapter)
        })
        viewModel.city.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            it?.let {
                val code = String.format("%s%s", nationality, it.Code)
                System.out.println("Codigo${code}")
                viewModel.personal.value!!.CityOfBirthCode = code
                viewModel.personal.value!!.CityOfBirthName = "${it.Name} (${it.StateName})"
                (binding.cityAutoCompleteTextView as TextView).text = "${it.Name} (${it.StateName})"
                DebugLog.logW("cities cbirth ${viewModel.personal.value!!.CityOfBirthCode}")
            }
        })
        binding.cityAutoCompleteTextView.apply {
            setOnItemClickListener { parent, view, position, id ->
                val item = parent.adapter.getItem(position) as City
                viewModel.personal.value!!.CityOfBirthCode = item.Code
                DebugLog.logW("city: ${item.Name}}")
                DebugLog.logW("cityCode: ${item.Code}}")
            }
            addTextChangedListener {
                it?.let {
                    if (it.isEmpty()) {
                        binding.cityInputLayout.endIconMode = TextInputLayout.END_ICON_DROPDOWN_MENU
                    } else binding.cityInputLayout.endIconMode = TextInputLayout.END_ICON_CLEAR_TEXT
                }
            }
        }

        binding.daneCityAutoCompleteTextView.apply {
            setOnItemClickListener { parent, view, position, id ->
                val item = parent.adapter.getItem(position) as DaneCity
                viewModel.personal.value!!.CityCode = item.CityCode
                DebugLog.logW("city: ${item.CityName}}")
            }
            addTextChangedListener {
                it?.let {
                    if (it.isEmpty()) {
                        binding.daneCityInputLayout.endIconMode = TextInputLayout.END_ICON_DROPDOWN_MENU
                    } else binding.daneCityInputLayout.endIconMode = TextInputLayout.END_ICON_CLEAR_TEXT
                }

            }
        }

        binding.jobAutoCompleteTextView.apply {
            setOnItemClickListener { parent, view, position, id ->
                val item = parent.adapter.getItem(position) as Job
                jobCode = item.Code
                DebugLog.logW("job: ${item.Name}}")
            }

            addTextChangedListener {
                it?.let {
                    if (it.isEmpty()) {
                        binding.jobInputLayout.endIconMode = TextInputLayout.END_ICON_DROPDOWN_MENU
                    } else binding.jobInputLayout.endIconMode = TextInputLayout.END_ICON_CLEAR_TEXT
                }

            }
        }

        binding.nationalityAutoCompleteTextView.apply {setOnItemClickListener { parent, view, position, id ->
            var PositionNationality = 0
            val item = parent.adapter.getItem(position) as String
            nationality = position
            DebugLog.logW("nationality: $item position: $position")
            if(item == "VENEZOLANO"){
                PositionNationality = 1
            }else {
                PositionNationality = 0
            }
            viewModel.getCities(PositionNationality)
            viewModel.personal.value!!.CityOfBirthCode = null
            (binding.cityAutoCompleteTextView as TextView).text = ""
        }
            addTextChangedListener {
                it?.let {
                    if(it.isEmpty()){
                        binding.nationalityInputLayout.endIconMode = TextInputLayout.END_ICON_DROPDOWN_MENU
                    }else{
                        binding.nationalityInputLayout.endIconMode = TextInputLayout.END_ICON_CLEAR_TEXT
                    }
                }
            }
        }

        (binding.nationalityAutoCompleteTextView as? TextView)?.text = "COLOMBIANO"
        val listNationality = arrayOf("COLOMBIANO", "VENEZOLANO")
        val adapterNationality = ArrayAdapter(requireContext(), R.layout.list_item, listNationality)
        (binding.nationalityAutoCompleteTextView as? AutoCompleteTextView)?.setAdapter(adapterNationality)
        binding.submitButton.setOnClickListener {
            val personal = arguments.personal
            if(binding.edtBirthDate.text.isNotEmpty()){
                try{
                    val myFormatedt = "dd/MMM/yyyy"
                    val sdfedt = SimpleDateFormat(myFormatedt, Locale("es", "ES"))
                    dateBirthedt=sdfedt.parse(binding.edtBirthDate.text.toString())
                    expecion = false
                }catch (e: ParseException){
                    expecion=true
                }
                if(expecion == true){
                    Toast.makeText(activity, "Formato incorrecto de su fecha de nacimiento", Toast.LENGTH_SHORT).show()
                }else if (expecion == false && binding.nationalityAutoCompleteTextView.text.isNotEmpty() && personal == null){
                    if (submit()) {
                        DebugLog.logW("jobCode $jobCode epsId $epsId afpId $afpId")
                        if (jobCode == null || binding.jobAutoCompleteTextView.text.isEmpty()) {
                            jobCode = viewModel.genericJob?.Code
                        }
                        if (process == 1) createPersonal()
                    }
                }
            }
            if(binding.edtBirthDate.text.isEmpty() && process == 1 && !createPersonalViewModel.scan){
                Toast.makeText(activity, "Debe llenar su fecha de nacimiento", Toast.LENGTH_SHORT).show()
            }
            if(binding.nationalityAutoCompleteTextView.text.isEmpty()){
                Toast.makeText(activity, "Debe seleccionar su nacionalidad", Toast.LENGTH_SHORT).show()
            }
            if(createPersonalViewModel.scan){
                if (submit()) {
                    DebugLog.logW("jobCode $jobCode epsId $epsId afpId $afpId")
                    if (jobCode == null || binding.jobAutoCompleteTextView.text.isEmpty()) {
                        jobCode = viewModel.genericJob?.Code
                    }
                    if (process == 1) createPersonal()
                }
            }
        }
        binding.cancelButton.setOnClickListener {
            cancel()
        }
        val itemsSex = listOf("M", "F")
        val sexAdapter = ArrayAdapter(requireContext(), R.layout.list_item, itemsSex)
        (binding.sexAutoCompleteTextView as? AutoCompleteTextView)?.setAdapter(sexAdapter)

        val itemsRH = listOf("O+", "O-", "A+", "A-", "B+", "B-", "AB+", "AB-")
        val adapterRH = ArrayAdapter(requireContext(), R.layout.list_item, itemsRH)
        (binding.rhAutoCompleteTextView as? AutoCompleteTextView)?.setAdapter(adapterRH)



        val calendar = Calendar.getInstance()
        configDatePicker(calendar.time)
        binding.edtBirthDate.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                if (binding.edtBirthDate.text.isEmpty()) {
                    showDatePicker()
                } else {
                    val myFormat = "dd/MMM/yyyy"
                    val sdf = SimpleDateFormat(myFormat, Locale("es", "ES"))
                    try {
                        dateBirthedt = sdf.parse(binding.edtBirthDate.text.toString())
                    } catch (e: ParseException) {
                        expecion = true
                    }
                }
            }
        }
        createPersonalViewModel.navigateToSelectPlaceJobFragment.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            it?.let {
                if (it) {
                    findNavController()
                            .navigate(
                                    PersonalIndividualContractFragmentDirections.actionPersonalIndividualContractFragmentToPlaceToJobFragment()
                            )
                    viewModel.clearData()
                    createPersonalViewModel.infoUser = null
                    createPersonalViewModel.doneNavigatingSelectPlaceToJob()
                }
            }
        })
    }
    @SuppressLint("SimpleDateFormat")
    private fun createPersonal() {

        val create = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
        val CreateDate = create.format(Date())

        val personal = viewModel.personal.value!!
        val myFormat = "yyyyMMdd"
        val sdf = SimpleDateFormat(myFormat, Locale("es", "ES"))
        if (dateBirth != null){
            personal.BirthDate = sdf.format(dateBirth!!)
        }else if(dateBirth==null){
            try{
                val myFormatedt = "dd/MMM/yyyy"
                val sdfedt = SimpleDateFormat(myFormatedt, Locale("es", "ES"))
                dateBirthedt=sdfedt.parse(binding.edtBirthDate.text.toString())
                personal.BirthDate=sdf.format(dateBirthedt)
            }catch (e: ParseException){
                expecion=true
            }
        }
        val newPersonal = PersonalNew(
                nationality,
                createPersonalViewModel.TypeDocument!!,
                personal.DocumentNumber,
                personal.Name,
                personal.LastName,
                personal.CityOfBirthCode,
                personal.RH,
                personal.Sex,
                personal.BirthDate,
                CreateDate,
                personal.Email,
                viewModel.genericEps!!.Id,
                viewModel.genericAfp!!.Id,
                infoUSER
        )
        newPersonal.Photo = personal.Photo
        val json = Gson().toJson(newPersonal)
        DebugLog.logW("jsonInsert: ${Gson().toJson(json)}")
        createPersonalViewModel.PersonalAssociateWithEmployer = false
        createPersonalViewModel.createPersonalToIndividualContract(newPersonal)

    }
    private fun basicForm(personal: PersonalNetwork) {
        (requireActivity() as AppCompatActivity).supportActionBar?.title = "CC ${personal.DocumentNumber}"
        (requireActivity() as AppCompatActivity).supportActionBar?.subtitle = "${personal.Name} ${personal.LastName}"
        binding.basicInfo.visibility = View.GONE
    }

    private fun configDatePicker(date: Date) {
        val cF = Calendar.getInstance()
        cF.time = date
        dateBirthPickerDialog = DatePickerDialog(
                requireContext(), this, cF[Calendar.YEAR],
                cF[Calendar.MONTH], cF[Calendar.DAY_OF_MONTH]
        )
        dateBirthPickerDialog!!.datePicker.maxDate = System.currentTimeMillis()
    }

    private fun showDatePicker() {
        dateBirthPickerDialog!!.datePicker.tag = R.id.btn_from_date
        dateBirthPickerDialog!!.show()
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        val calendar = Calendar.getInstance()
        calendar[year, month] = dayOfMonth
        val myFormat = "dd/MMM/yyyy"
        val sdf = SimpleDateFormat(myFormat, Locale("es", "ES"))
        val dateBirthStr = sdf.format(calendar.time)
        binding.edtBirthDate.setText(dateBirthStr)
        dateBirth = calendar.time
    }
    fun cancel() {
        if (process == 1) {
            val builder = AlertDialog.Builder(requireActivity())
                .setTitle("Atencion")
                .setMessage("¿Desea salir sin crear este empleado?")
                .setPositiveButton("Aceptar") { _, _ ->
                    findNavController().popBackStack()
                }
                .setNegativeButton("Cancelar", null)
            builder.create().show()
        } else
            findNavController().popBackStack()
        viewModel.clearData()
        createPersonalViewModel.infoUser = null
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PhotoSer.SER_IMAGE_ACTIVITY_REQUEST_CODE) {
            when (resultCode) {
                Activity.RESULT_OK -> {
                    val uriImage = Uri.parse(data!!.getStringExtra(FaceTrackerActivity.URI_IMAGE_KEY))
                    DebugLog.logW("uriImage $uriImage")
                    Picasso.get().load(uriImage)
                            .placeholder(R.drawable.profile_dummy)
                            .error(R.drawable.profile_dummy)
                            .into(binding.iconLogo)
                    viewModel.personal.value!!.Photo = uriImage.toString()
                }
                else -> {
                }
            }
        }
    }

    private fun startFaceDectectorActivity() {
        PhotoSer.ActivityBuilder()
            .setDetectFace(true)
            .setSaveGalery(true)
            .start(this, requireActivity())
    }

    @SuppressLint("SetTextI18n")
    fun submit(): Boolean {
        var send = true
        var focusView: View? = null
        if (process == 1) {
            DebugLog.logW("createPersonalViewModel.typeContract ${createPersonalViewModel.typeContract?.Value}")
            DebugLog.logW("personal network ${viewModel.personal.value}")
            if ((viewModel.personal.value!!.Photo == null || viewModel.personal.value!!.Photo.isNullOrEmpty())) {
                MetodosPublicos.alertDialog(activity, "La foto es obligatoria")
                send = false
                focusView = binding.iconLogo
            } else if (TextUtils.isEmpty(binding.edttName.text.toString())) {
                binding.edttName.error = getString(R.string.error_field_required)
                send = false
                focusView = binding.edttName
            } else if (TextUtils.isEmpty(binding.edttLastName.text.toString())) {
                binding.edttLastName.error = getString(R.string.error_field_required)
                send = false
                focusView = binding.edttLastName
            } else if (TextUtils.isEmpty(binding.rhAutoCompleteTextView.text.toString())) {
                binding.rhAutoCompleteTextView.error = getString(R.string.error_field_required)
                send = false
                focusView = binding.rhAutoCompleteTextView
            } else if (TextUtils.isEmpty(binding.sexAutoCompleteTextView.text.toString())) {
                binding.sexAutoCompleteTextView.error = getString(R.string.error_field_required)
                send = false
                focusView = binding.sexAutoCompleteTextView
            } else if (TextUtils.isEmpty(binding.cityAutoCompleteTextView.text.toString())) {
                binding.cityAutoCompleteTextView.error = getString(R.string.error_field_required)
                send = false
                focusView = binding.cityAutoCompleteTextView
            } else if (dateBirth == null && viewModel.personal.value!!.BirthDate == null && binding.edtBirthDate.toString().isEmpty()) {
                Snackbar.make(binding.edtBirthDate, "Escoja su fecha de nacimiento", Snackbar.LENGTH_LONG)
                    .show()
                send = false
                focusView = binding.edtBirthDate
            }else if(viewModel.personal.value!!.CityOfBirthCode == null){
                Snackbar.make(binding.cityAutoCompleteTextView, "Escoja su ciudad de nacimiento", Snackbar.LENGTH_LONG)
                    .show()
                (binding.cityAutoCompleteTextView as TextView).text = ""
                binding.cityAutoCompleteTextView.hint = "Ciudad de Nacimiento"
                send = false
                focusView = binding.cityAutoCompleteTextView
            }else if(TextUtils.isEmpty(binding.daneCityAutoCompleteTextView.text.toString())){
                binding.daneCityAutoCompleteTextView.error = getString(R.string.error_field_required)
                send = false
                focusView = binding.daneCityAutoCompleteTextView
            }else if(TextUtils.isEmpty(binding.edttAddress.text.toString())){
                binding.edttAddress.error = getString(R.string.error_field_required)
                send = false
                focusView = binding.edttAddress
            }else if (TextUtils.isEmpty(binding.edttPhone.text.toString())){
                binding.edttPhone.error = getString(R.string.error_field_required)
                send = false
                focusView = binding.edttPhone
            }else if (TextUtils.isEmpty(binding.edttEmail.text.toString())){
                binding.edttEmail.error = getString(R.string.error_field_required)
                send = false
                focusView = binding.edttEmail
            }
        }
        if (!send) {
            focusView!!.requestFocus()
        }
        return send
    }
    override fun onAttach(context: Context) {
        super.onAttach(context)
        ApplicationContext.analitycoComponent.inject(this)
    }

}

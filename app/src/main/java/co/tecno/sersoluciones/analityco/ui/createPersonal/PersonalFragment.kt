package co.tecno.sersoluciones.analityco.ui.createPersonal

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
import android.view.View.OnFocusChangeListener
import android.view.ViewGroup
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import co.com.sersoluciones.barcodedetectorser.utilities.DebugLog.logW
import co.com.sersoluciones.facedetectorser.FaceTrackerActivity
import co.com.sersoluciones.facedetectorser.serlibrary.PhotoSer
import co.tecno.sersoluciones.analityco.ApplicationContext
import co.tecno.sersoluciones.analityco.R
import co.tecno.sersoluciones.analityco.databinding.FragmentPersonalBinding
import co.tecno.sersoluciones.analityco.models.*
import co.tecno.sersoluciones.analityco.nav.CreatePersonalViewModel
import co.tecno.sersoluciones.analityco.nav.SecurityReference
import co.tecno.sersoluciones.analityco.services.CRUDService
import co.tecno.sersoluciones.analityco.ui.createPersonal.viewmodels.PersonalViewModel
import co.tecno.sersoluciones.analityco.utilities.Constantes
import co.tecno.sersoluciones.analityco.utilities.MetodosPublicos
import com.android.volley.Request
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_personal.*
import kotlinx.android.synthetic.main.requerimets_list_adapter.*
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject


class PersonalFragment : Fragment(), DatePickerDialog.OnDateSetListener {

    private lateinit var binding: FragmentPersonalBinding
    private var dateBirthPickerDialog: DatePickerDialog? = null

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
    private var expecion: Boolean? = false
    private var docType: String? = null
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
        binding = FragmentPersonalBinding.inflate(inflater)
        binding.lifecycleOwner = this
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.viewModel = viewModel
        val arguments = PersonalFragmentArgs.fromBundle(requireArguments())

        
        (binding.nationalityAutoCompleteTextView as? TextView)?.text = "COLOMBIANO"
        (activity as AppCompatActivity).supportActionBar?.title = ""
        (activity as AppCompatActivity).supportActionBar?.subtitle = ""

        val personal = arguments.personal
        val infoUser = arguments.infoUser
        val docNumber = arguments.documentNumber.toString()
        docType = arguments.documentType
        logW("docNumber ========== $docNumber")
        if (personal != null) {
            viewModel.setPersonal(personal)
            process = 2
            epsId = personal.EpsId!!
            afpId = personal.AfpId!!
            jobCode = personal.JobCode
            basicForm(personal)
            logW("docNumber ========== $docNumber")

        } else {
            process = 1
            viewModel.personal.value!!.DocumentNumber = docNumber
            (activity as AppCompatActivity).supportActionBar?.title = docNumber
            logW("personal network ${Gson().toJson(viewModel.personal.value)}")
            infoUser?.let {
                logW("personal infoUser ${Gson().toJson(it)}")
                viewModel.personal.value!!.DocumentNumber = it.dni.toString()
                viewModel.personal.value!!.Name = it.name
                viewModel.personal.value!!.LastName = it.lastname
                viewModel.personal.value!!.RH = it.rh
                viewModel.personal.value!!.Sex = it.sex
                viewModel.personal.value!!.BirthDate = it.birthDate.toString()
                var Create = ""
                val simpleDateFormat = SimpleDateFormat("yyyymmdd")
                val format = SimpleDateFormat("dd/MMM/yyyy")
                try {
                    val date = simpleDateFormat.parse(it.birthDate.toString())
                    Create = format.format(date)
                    edt_birth_date.setText(Create)


                } catch (e: ParseException) {
                    e.printStackTrace()//32404173
                }

                logW("fecha:"+ it.birthDate.toString())
                logW("fechanueva"+ Create)
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
                (binding.epsAutoCompleteTextView as TextView).text = viewModel.genericEps?.Name
                (binding.afpAutoCompleteTextView as TextView).text = viewModel.genericAfp?.Name
//                viewModel.cleanIsReady()

            }
        })
        viewModel.jobs.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            logW("jobs count ${it.count()}")
            val jobAdapter = ArrayAdapter(requireContext(), R.layout.list_item, it)
            binding.jobAutoCompleteTextView.setAdapter(jobAdapter)
        })
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
            logW("eps count ${epsList.count()} afp count ${afpList.count()} total ${it.count()}")
            binding.epsAutoCompleteTextView.setAdapter(epsAdapter)
            binding.afpAutoCompleteTextView.setAdapter(afpAdapter)
        })
        viewModel.daneCities.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            logW("dane cities count ${it.count()}")
            val daneCityAdapter = ArrayAdapter(requireContext(), R.layout.list_item, it)
            binding.daneCityAutoCompleteTextView.setAdapter(daneCityAdapter)
        })

        viewModel.cities.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            logW("cities count ${it.count()}")
            val citiesAdapter = ArrayAdapter(requireContext(), R.layout.list_item, it)
            binding.cityAutoCompleteTextView.setAdapter(citiesAdapter)
        })
        viewModel.city.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            it?.let {
                viewModel.personal.value!!.CityOfBirthCode = it.Code
                viewModel.personal.value!!.CityOfBirthName = "${it.Name} (${it.StateName})"
                (binding.cityAutoCompleteTextView as TextView).text = "${it.Name} (${it.StateName})"

                logW("cities cbirth ${viewModel.personal.value!!.CityOfBirthCode}")
            }
        })
        createPersonalViewModel.navigateToSelectContractFragment.observe(
            viewLifecycleOwner,
            androidx.lifecycle.Observer {
                it?.let {
                    if (it) {
                        findNavController()
                            .navigate(
                                PersonalFragmentDirections.actionPersonalFragmentToSelectNavContractFragment()
                            )
                        viewModel.clearData()
                        createPersonalViewModel.infoUser = null
                        createPersonalViewModel.doneNavigatingPersonal()
                    }
                }
            })
        binding.cityAutoCompleteTextView.apply {
            setOnItemClickListener { parent, view, position, id ->
                val item = parent.adapter.getItem(position) as City
                viewModel.personal.value!!.CityOfBirthCode = item.Code
                logW("city: ${item.Name}}")
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
                logW("city: ${item.CityName}}")
            }
            addTextChangedListener {
                it?.let {
                    if (it.isEmpty()) {
                        binding.daneCityInputLayout.endIconMode =
                            TextInputLayout.END_ICON_DROPDOWN_MENU
                    } else binding.daneCityInputLayout.endIconMode =
                        TextInputLayout.END_ICON_CLEAR_TEXT
                }

            }
        }

        binding.jobAutoCompleteTextView.apply {
            setOnItemClickListener { parent, view, position, id ->
                val item = parent.adapter.getItem(position) as Job
                jobCode = item.Code
                logW("job: ${item.Name}}")
            }

            addTextChangedListener {
                it?.let {
                    if (it.isEmpty()) {
                        binding.jobInputLayout.endIconMode = TextInputLayout.END_ICON_DROPDOWN_MENU
                    } else binding.jobInputLayout.endIconMode = TextInputLayout.END_ICON_CLEAR_TEXT
                }

            }
        }
        binding.epsAutoCompleteTextView.apply {
            setOnItemClickListener { parent, view, position, id ->
                val item = parent.adapter.getItem(position) as SecurityReference.EPS
                epsId = item.item.Id
                logW("eps: ${item.item.Name}}")
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
                logW("afp: ${item.item.Name}}")
            }

            addTextChangedListener {
                it?.let {
                    if (it.isEmpty()) {
                        binding.afpInputLayout.endIconMode = TextInputLayout.END_ICON_DROPDOWN_MENU
                    } else binding.afpInputLayout.endIconMode = TextInputLayout.END_ICON_CLEAR_TEXT
                }

            }
        }

        (binding.nationalityAutoCompleteTextView as? TextView)?.text = "COLOMBIANO"
        val listNationality = arrayOf("COLOMBIANO", "VENEZOLANO")
        val adapterNationality = ArrayAdapter(requireContext(), R.layout.list_item, listNationality)
        (binding.nationalityAutoCompleteTextView as? AutoCompleteTextView)?.setAdapter(
            adapterNationality
        )

        binding.nationalityAutoCompleteTextView.apply {
            setOnItemClickListener { parent, view, position, id ->
                var PositionNationality = 0
                val item = parent.adapter.getItem(position) as String
                nationality = position
                logW("nationality: $item position: $position")
                if (item == "VENEZOLANO") {
                    PositionNationality = 1
                } else {
                    PositionNationality = 0
                }
                viewModel.getCities(PositionNationality)
                viewModel.personal.value!!.CityOfBirthCode = null
                (binding.cityAutoCompleteTextView as TextView).text = ""
            }
            addTextChangedListener {
                it?.let {
                    if (it.isEmpty()) {
                        binding.nationalityInputLayout.endIconMode =
                            TextInputLayout.END_ICON_DROPDOWN_MENU
                    } else {
                        binding.nationalityInputLayout.endIconMode =
                            TextInputLayout.END_ICON_CLEAR_TEXT
                    }
                }
            }
        }
        binding.submitButton.setOnClickListener {

            val personal = arguments.personal
            if (binding.edtBirthDate.text.isNotEmpty() || !viewModel.personal.value!!.BirthDate.isNullOrEmpty() || createPersonalViewModel.typeContract?.Value == "FU" || createPersonalViewModel.typeContract?.Value == "VI"  || createPersonalViewModel.typeContract?.Value != "OT" || createPersonalViewModel.typeContract?.Value != "PR") {
                try {
                    val myFormatedt = "dd/MMM/yyyy"
                    val sdfedt = SimpleDateFormat(myFormatedt, Locale("es", "ES"))
                    dateBirthedt = sdfedt.parse(binding.edtBirthDate.text.toString())
                    expecion = false
                } catch (e: ParseException) {
                    expecion = true
                }
                if (expecion == true && viewModel.personal.value!!.BirthDate.isNullOrEmpty() && (createPersonalViewModel.typeContract?.Value != "FU" && createPersonalViewModel.typeContract?.Value != "VI" && createPersonalViewModel.typeContract?.Value != "OT" && createPersonalViewModel.typeContract?.Value != "PR" )) {
                    Toast.makeText(
                        activity,
                        "Formato incorrecto de su fecha de nacimiento",
                        Toast.LENGTH_SHORT
                    ).show()
                } else if ((expecion == false && binding.nationalityAutoCompleteTextView.text.isNotEmpty() && personal == null) || !viewModel.personal.value!!.BirthDate.isNullOrEmpty() || (createPersonalViewModel.typeContract?.Value == "FU" || createPersonalViewModel.typeContract?.Value == "VI" || createPersonalViewModel.typeContract?.Value != "OT" || createPersonalViewModel.typeContract?.Value != "PR"  )) {
                    if (submit()) {
                        logW("jobCode $jobCode epsId $epsId afpId $afpId")
                        if (jobCode == null || binding.jobAutoCompleteTextView.text.isEmpty()) {
                            jobCode = viewModel.genericJob?.Code
                        }
                        if (epsId == 0 || binding.epsAutoCompleteTextView.text.isEmpty()) {
                            epsId = viewModel.genericEps!!.Id
                        }
                        if (afpId == 0 || binding.afpAutoCompleteTextView.text.isEmpty()) {
                            afpId = viewModel.genericAfp!!.Id
                        }
                        if (process == 1) createPersonal()
                        else updatePersonal()
                    }
                }
            }
            if ((binding.edtBirthDate.text.isEmpty() && process == 1 && !createPersonalViewModel.scan) && viewModel.personal.value!!.BirthDate.isNullOrEmpty() && (createPersonalViewModel.typeContract?.Value != "FU" && createPersonalViewModel.typeContract?.Value != "VI" && createPersonalViewModel.typeContract?.Value != "OT" && createPersonalViewModel.typeContract?.Value != "PR")) {
                Toast.makeText(activity, "Debe llenar su fecha de nacimiento", Toast.LENGTH_SHORT)
                    .show()
            }
            if (binding.nationalityAutoCompleteTextView.text.isEmpty()) {
                Toast.makeText(activity, "Debe seleccionar su nacionalidad", Toast.LENGTH_SHORT)
                    .show()
            }
            if (createPersonalViewModel.scan) {
                if (submit()) {
                    logW("jobCode $jobCode epsId $epsId afpId $afpId")
                    if (jobCode == null || binding.jobAutoCompleteTextView.text.isEmpty()) {
                        jobCode = viewModel.genericJob?.Code
                    }
                    if (epsId == 0 || binding.epsAutoCompleteTextView.text.isEmpty()) {
                        epsId = viewModel.genericEps!!.Id
                    }
                    if (afpId == 0 || binding.afpAutoCompleteTextView.text.isEmpty()) {
                        afpId = viewModel.genericAfp!!.Id
                    }
                    if (process == 1) createPersonal()
                    else updatePersonal()
                }
            }
            if (process == 2) {
                if (jobCode == null || binding.jobAutoCompleteTextView.text.isEmpty()) {
                    jobCode = viewModel.genericJob?.Code
                }
                if (epsId == 0 || binding.epsAutoCompleteTextView.text.isEmpty()) {
                    epsId = viewModel.genericEps!!.Id
                }
                if (afpId == 0 || binding.afpAutoCompleteTextView.text.isEmpty()) {
                    afpId = viewModel.genericAfp!!.Id
                }
                updatePersonal()

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
        binding.edtBirthDate.onFocusChangeListener = OnFocusChangeListener { v, hasFocus ->
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
    }


    private fun createPersonal() {
        val personal = viewModel.personal.value!!
        val myFormat = "yyyyMMdd"
        val sdf = SimpleDateFormat(myFormat, Locale("es", "ES"))
        if (dateBirth != null) {
            personal.BirthDate = sdf.format(dateBirth!!)
        } else if (dateBirth == null) {
            try {
                val myFormatedt = "dd/MMM/yyyy"
                val sdfedt = SimpleDateFormat(myFormatedt, Locale("es", "ES"))
                dateBirthedt = sdfedt.parse(binding.edtBirthDate.text.toString())
                personal.BirthDate = sdf.format(dateBirthedt)
            } catch (e: ParseException) {
                expecion = true
            }
        }

        val newPersonal = PersonalNew(
            createPersonalViewModel.companyInfoId.value!!,
            personal.DocumentNumber!!,
            docType?:"CC",
            personal.Name!!,
            personal.LastName!!,
            personal.PhoneNumber,
            personal.CityOfBirthCode ?: "",
            nationality,
            personal.BirthDate ?: "",
            personal.RH ?: "",
            personal.Sex ?: "",
            jobCode!!,
            personal.CityCode,
            personal.Address,
            personal.EmergencyContact,
            personal.EmergencyContactPhone,
            epsId,
            afpId
           // binding.edttParentesContact.text.toString()






        )
        newPersonal.Photo = personal.Photo
        val json = Gson().toJson(newPersonal)
        logW("json: ${Gson().toJson(json)}")
        createPersonalViewModel.createPersonal(newPersonal)
    }

    private fun basicForm(personal: PersonalNetwork) {
        (requireActivity() as AppCompatActivity).supportActionBar?.title =
            "CC ${personal.DocumentNumber}"
        (requireActivity() as AppCompatActivity).supportActionBar?.subtitle =
            "${personal.Name} ${personal.LastName}"
        if (!personal.DocumentRaw.isNullOrEmpty()) {
            binding.basicInfo.visibility = View.GONE
        } else {
            binding.basicInfo.visibility = View.VISIBLE
        }
    }

    private fun updatePersonal() {
        val personal = viewModel.personal.value!!
        logW("jobCode2 $jobCode epsId2 $epsId afpId2 $afpId")
        val updatePersonalInfo = UpdatePersonalInfo(
            personal.PhoneNumber, personal.CityCode, personal.Address,
            personal.EmergencyContact,
            personal.EmergencyContactPhone,
            jobCode!!, epsId, afpId, Photo = personal.Photo
        )
        val jsonPersonal = Gson().toJson(updatePersonalInfo)
        logW(jsonPersonal)
        createPersonalViewModel.updatePersonal(updatePersonalInfo, personal.PersonalCompanyInfoId)
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
        //binding.btnBirthDate.setText(dateBirthStr)
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
                    val uriImage =
                        Uri.parse(data!!.getStringExtra(FaceTrackerActivity.URI_IMAGE_KEY))
                    logW("uriImage $uriImage")
                    Picasso.get().load(uriImage)
//                        .transform(CircleTransform())
                        .placeholder(R.drawable.profile_dummy)
                        .error(R.drawable.profile_dummy)
                        .into(binding.iconLogo)
                    viewModel.personal.value!!.Photo = uriImage.toString()
//                    mImageUri = uriImage
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
            logW("createPersonalViewModel.typeContract ${createPersonalViewModel.typeContract?.Value}")
            logW("personal network ${viewModel.personal.value}")
            if (createPersonalViewModel.typeContract?.Value != "VI" && createPersonalViewModel.typeContract?.Value != "OT" && createPersonalViewModel.typeContract?.Value != "PR" && createPersonalViewModel.typeContract?.Value != "FU" && (viewModel.personal.value!!.Photo == null || viewModel.personal.value!!.Photo.isNullOrEmpty())) {
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
            } else if (TextUtils.isEmpty(binding.rhAutoCompleteTextView.text.toString()) && (createPersonalViewModel.typeContract?.Value != "VI" && createPersonalViewModel.typeContract?.Value != "OT" && createPersonalViewModel.typeContract?.Value != "PR" && createPersonalViewModel.typeContract?.Value != "FU")) {
                binding.rhAutoCompleteTextView.error = getString(R.string.error_field_required)
                send = false
                focusView = binding.rhAutoCompleteTextView
            } else if (TextUtils.isEmpty(binding.sexAutoCompleteTextView.text.toString()) && (createPersonalViewModel.typeContract?.Value != "VI" && createPersonalViewModel.typeContract?.Value != "OT" && createPersonalViewModel.typeContract?.Value != "PR" && createPersonalViewModel.typeContract?.Value != "FU")) {
                binding.sexAutoCompleteTextView.error = getString(R.string.error_field_required)
                send = false
                focusView = binding.sexAutoCompleteTextView
            } else if (TextUtils.isEmpty(binding.cityAutoCompleteTextView.text.toString()) && (createPersonalViewModel.typeContract?.Value != "VI" && createPersonalViewModel.typeContract?.Value != "OT" && createPersonalViewModel.typeContract?.Value != "PR" && createPersonalViewModel.typeContract?.Value != "FU")) {
                binding.cityAutoCompleteTextView.error = getString(R.string.error_field_required)
                send = false
                focusView = binding.cityAutoCompleteTextView
            } else if (dateBirth == null && viewModel.personal.value!!.BirthDate == null && binding.edtBirthDate.toString()
                    .isEmpty() && (createPersonalViewModel.typeContract?.Value) != "VI" && (createPersonalViewModel.typeContract?.Value != "OT") && (createPersonalViewModel.typeContract?.Value != "PR") && (createPersonalViewModel.typeContract?.Value != "FU")
            ) {
                Snackbar.make(
                    binding.edtBirthDate,
                    "Escoja su fecha de nacimiento",
                    Snackbar.LENGTH_LONG
                )
                    .show()
                send = false
                //focusView = binding.btnBirthDate
                focusView = binding.edtBirthDate
            } else if (viewModel.personal.value!!.CityOfBirthCode == null && (createPersonalViewModel.typeContract?.Value != "VI" && createPersonalViewModel.typeContract?.Value != "OT" && createPersonalViewModel.typeContract?.Value != "PR" && createPersonalViewModel.typeContract?.Value != "FU")) {
                Snackbar.make(
                    binding.cityAutoCompleteTextView,
                    "Escoja su ciudad de nacimiento",
                    Snackbar.LENGTH_LONG
                )
                    .show()
                (binding.cityAutoCompleteTextView as TextView).text = ""
                binding.cityAutoCompleteTextView.hint = "Ciudad de Nacimiento"
                send = false
                focusView = binding.cityAutoCompleteTextView
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
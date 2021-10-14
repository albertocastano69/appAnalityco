package co.tecno.sersoluciones.analityco.individualContract

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.fragment.findNavController
import co.com.sersoluciones.barcodedetectorser.utilities.DebugLog

import co.com.sersoluciones.facedetectorser.FaceTrackerActivity
import co.com.sersoluciones.facedetectorser.serlibrary.PhotoSer
import co.tecno.sersoluciones.analityco.ApplicationContext
import co.tecno.sersoluciones.analityco.R
import co.tecno.sersoluciones.analityco.databases.DBHelper
import co.tecno.sersoluciones.analityco.databinding.FragmentDataforemployeeBinding
import co.tecno.sersoluciones.analityco.models.DaneCity
import co.tecno.sersoluciones.analityco.models.PersonalDataContact
import co.tecno.sersoluciones.analityco.models.PersonalNew
import co.tecno.sersoluciones.analityco.nav.CreatePersonalViewModel
import co.tecno.sersoluciones.analityco.receivers.RequestBroadcastReceiver
import co.tecno.sersoluciones.analityco.services.CRUDService
import co.tecno.sersoluciones.analityco.services.CrudIntentService
import co.tecno.sersoluciones.analityco.ui.createPersonal.viewmodels.PersonalViewModel
import co.tecno.sersoluciones.analityco.utilities.Constantes
import co.tecno.sersoluciones.analityco.utilities.HttpRequest
import co.tecno.sersoluciones.analityco.utilities.MetodosPublicos
import com.android.volley.Request
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout
import com.squareup.picasso.Picasso
import org.json.JSONObject
import javax.inject.Inject

class DataForEmployeeFragment: Fragment(), RequestBroadcastReceiver.BroadcastListener {
    private  lateinit var binding: FragmentDataforemployeeBinding
    private var requestBroadcastReceiver: RequestBroadcastReceiver? = null
    var name: String? = null
    var lastName: String? = null
    var docType: String? = null
    var docNumber: String? = null
    var dateBirth: String? = null
    var cityBirth: String? = null
    var nationality: Int? = 0
    var IdContract : String? = null
    var nationalityForSubmit : String? = null
    var idPersonEmployerInfo : String? = null

    @Inject
    lateinit var viewModel: CreatePersonalViewModel

    @Inject
    lateinit var viewModelList: PersonalViewModel

    private var isScaneed = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentDataforemployeeBinding.inflate(inflater)
        binding.lifecycleOwner = this
        val onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                alertClose()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, onBackPressedCallback)
        return binding.root
    }
    private fun alertClose() {
        val builder = MaterialAlertDialogBuilder(requireActivity())
                .setTitle("Atencion")
                .setMessage("¿Desea cancelar este proceso?")
                .setPositiveButton("Aceptar") { _, _ ->
                    viewModel.clearForm()
                    viewModelList.clearData()
                    activity?.finish()
                }
                .setNegativeButton("Cancelar", null)
        builder.create().show()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        requestBroadcastReceiver = RequestBroadcastReceiver(this)
        (requireActivity() as AppCompatActivity).supportActionBar?.title = "Datos del empleado"
        binding.controlButtons.backButton.text = "Cancelar"

        val arguments = DataForEmployeeFragmentArgs.fromBundle(requireArguments())
        IdContract = arguments.idContract
        nationalityForSubmit = arguments.nationality

        viewModelList.daneCities.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            DebugLog.logW("dane cities count ${it.count()}")
            val daneCityAdapter = ArrayAdapter(requireContext(), R.layout.list_item, it)
            binding.daneCityAutoCompleteTextView.setAdapter(daneCityAdapter)
        })

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

        val values = ContentValues()
        values.put("EmployerId", viewModel.EmployerId)
        values.put("PersonalId", viewModel.PersonalId)
        val paramsQuery = HttpRequest.makeParamsInUrl(values)
        CRUDService.startRequest(activity, Constantes.PERSONAL_EMPLOYER_INFO_PARAMS_URL,
                Request.Method.GET, paramsQuery, false)

        if(isScaneed){
            binding.infoUserLayout.visibility = View.VISIBLE
            binding.namePersonLayout.visibility = View.VISIBLE
        }else {
            binding.infoUserLayoutEdit.visibility = View.VISIBLE
            binding.namePersonLayoutEdit.visibility = View.VISIBLE
        }

        binding.headerImg.setOnClickListener {startFaceDectectorActivity()}

        binding.controlButtons.nextButton.setOnClickListener {
            if (!isScaneed && submitEdit()) {
                UpdateInfoPerson()
            } else if (isScaneed && submit()) {
                UpdatePersonalEmployerInfo()
            }
        }
        binding.controlButtons.backButton.setOnClickListener {
            alertClose()
        }
    }

    private fun UpdatePersonalEmployerInfo() {
        val personal = viewModel.personal.value!!
        val dataContact = PersonalDataContact(
                binding.edttPhone.text.toString(),
                personal.CityCode,
                binding.edttAddress.text.toString(),
                binding.edttNameContact.text.toString(),
                binding.edttPhoneContact.text.toString(),
                binding.edttEmail.text.toString(),
                viewModel.EmployerId,
                viewModel.PersonalId
        )
        viewModel.UpdatePersonalEmloyerInfo(dataContact,idPersonEmployerInfo!!)
        navigateToDocuments()
    }

    private fun UpdateInfoPerson() {
        val personal = viewModel.personal.value!!
        val dataContact = PersonalDataContact(
                binding.edttPhone.text.toString(),
                personal.CityCode,
                binding.edttAddress.text.toString(),
                binding.edttNameContact.text.toString(),
                binding.edttPhoneContact.text.toString(),
                binding.edttEmail.text.toString(),
                viewModel.EmployerId,
                viewModel.PersonalId
        )
        viewModel.UpdatePersonalEmloyerInfo(dataContact, idPersonEmployerInfo!!)

        val UpdatePersonal = PersonalNew(
                "",
                nationality!!,
                docType!!,
                docNumber,
                binding.namePersonEdit.text.toString(),
                binding.lastnameEdit.text.toString(),
                cityBirth,
                binding.textRhEdit.text.toString(),
                binding.textGenderEdit.text.toString(),
                dateBirth
        )
        viewModel.updatePersonalToIndividualContract(UpdatePersonal)
        navigateToDocuments()
    }

    private fun navigateToDocuments() {
        findNavController().navigate(
                DataForEmployeeFragmentDirections.actionDataForEmployeeFragmentToDocumentForIndividualContractFragment(IdContract!!, nationalityForSubmit!!)
        )
    }
    private fun startFaceDectectorActivity() {
        PhotoSer.ActivityBuilder()
                .setDetectFace(true)
                .setSaveGalery(true)
                .start(this, requireActivity())
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PhotoSer.SER_IMAGE_ACTIVITY_REQUEST_CODE) {
            when (resultCode) {
                Activity.RESULT_OK -> {
                    val uriImage = Uri.parse(data!!.getStringExtra(FaceTrackerActivity.URI_IMAGE_KEY))
                    Picasso.get().load(uriImage)
                            .placeholder(R.drawable.profile_dummy)
                            .error(R.drawable.profile_dummy)
                            .into(binding.headerImg)
                    viewModel.personal.value!!.Photo = uriImage.toString()
                }
                else -> {
                }
            }
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
        if(url == Constantes.PERSONAL_EMPLOYER_INFO_PARAMS_URL){
            val Response = JSONObject(response)
            val personal = Response.getJSONObject("personal")
            idPersonEmployerInfo = Response.getString("Id")
            isScaneed = Response.getBoolean("IsScanned")
            docType = personal.getString("DocumentType")
            docNumber = personal.getString("DocumentNumber")
            if(!docType.isNullOrEmpty() && !docNumber.isNullOrEmpty()){
                viewModel.findPersonalToIndividualContract(docType!!, docNumber!!)
                binding.personal = viewModel.personal.value
                nationality = viewModel.personal.value!!.Nationality
                dateBirth = viewModel.personal.value!!.BirthDate
                cityBirth = viewModel.personal.value!!.CityOfBirthCode
                LoadInformation()
            }
        }
    }

    private fun LoadInformation() {
        if (viewModel.personal.value!!.Nationality == 0) binding.nationalityFlag.setImageResource(R.drawable.ic_flag_of_colombia) else binding.nationalityFlag.setImageResource(R.drawable.ic_flag_of_venezuela)
        val code = StringBuilder(viewModel.personal.value!!.CityOfBirthCode!!)
        code.deleteCharAt(0)
        SelectCityBirth(code, viewModel.personal.value!!.Nationality)
    }

    private fun SelectCityBirth(code: StringBuilder, nationality: Int) {
        val selection = ("(" + DBHelper.CITY_TABLE_COLUMN_CODE + " = ?) and ("
                + DBHelper.CITY_TABLE_COLUMN_COUNTRY_CODE + " lIKE ?)")
        val CityBirth = requireActivity().contentResolver.query(Constantes.CONTENT_CITY_URI, null, selection, arrayOf(code.toString(), nationality.toString()), null)
        if (CityBirth!!.moveToFirst()) {
            val City = CityBirth.getString(CityBirth.getColumnIndex(DBHelper.CITY_TABLE_COLUMN_NAME))
            val Region = CityBirth.getString(CityBirth.getColumnIndex(DBHelper.CITY_TABLE_COLUMN_STATE))
            binding.textPlaceBirth.text = (String.format("%s %s", City, Region))
            (binding.textPlaceBirthEdit as TextView).text = (String.format("%s %s", City, Region))
        }
    }
    @SuppressLint("SetTextI18n")
    fun submitEdit(): Boolean {
        var send = true
        var focusView: View? = null
            if ((viewModel.personal.value!!.Photo == null || viewModel.personal.value!!.Photo.isNullOrEmpty())) {
                MetodosPublicos.alertDialog(activity, "La foto es obligatoria")
                send = false
                focusView = binding.headerImg
            } else if (TextUtils.isEmpty(binding.namePersonEdit.text.toString())) {
                binding.namePersonEdit.error = getString(R.string.error_field_required)
                send = false
                focusView = binding.namePersonEdit
            } else if (TextUtils.isEmpty(binding.lastnameEdit.text.toString())) {
                binding.lastnameEdit.error = getString(R.string.error_field_required)
                send = false
                focusView = binding.lastnameEdit
            } else if (TextUtils.isEmpty(binding.textRhEdit.text.toString())) {
                binding.textRhEdit.error = getString(R.string.error_field_required)
                send = false
                focusView = binding.textRhEdit
            } else if (TextUtils.isEmpty(binding.textGenderEdit.text.toString())) {
                binding.textGenderEdit.error = getString(R.string.error_field_required)
                send = false
                focusView = binding.textGenderEdit
            } else if(TextUtils.isEmpty(binding.edttAddress.text.toString())){
                binding.edttAddress.error = getString(R.string.error_field_required)
                send = false
                focusView = binding.edttAddress
            }else if (TextUtils.isEmpty(binding.edttEmail.text.toString())){
                binding.edttEmail.error = getString(R.string.error_field_required)
                send = false
                focusView = binding.edttEmail
            }else if (TextUtils.isEmpty(binding.edttPhone.text.toString())){
                binding.edttPhone.error = getString(R.string.error_field_required)
                send = false
                focusView = binding.edttPhone
            }
        if (!send) {
            focusView!!.requestFocus()
        }
        return send
    }
    @SuppressLint("SetTextI18n")
    fun submit(): Boolean {
        var send = true
        var focusView: View? = null
        if ((viewModel.personal.value!!.Photo == null || viewModel.personal.value!!.Photo.isNullOrEmpty())) {
            MetodosPublicos.alertDialog(activity, "La foto es obligatoria")
            send = false
            focusView = binding.headerImg
        } else if (TextUtils.isEmpty(binding.namePerson.text.toString())) {
            binding.namePerson.error = getString(R.string.error_field_required)
            send = false
            focusView = binding.namePerson
        } else if (TextUtils.isEmpty(binding.lastname.text.toString())) {
            binding.lastname.error = getString(R.string.error_field_required)
            send = false
            focusView = binding.lastname
        }else if(TextUtils.isEmpty(binding.edttAddress.text.toString())){
            binding.edttAddress.error = getString(R.string.error_field_required)
            send = false
            focusView = binding.edttAddress
        }else if (TextUtils.isEmpty(binding.edttEmail.text.toString())){
            binding.edttEmail.error = getString(R.string.error_field_required)
            send = false
            focusView = binding.edttEmail
        }else if (TextUtils.isEmpty(binding.edttPhone.text.toString())){
            binding.edttPhone.error = getString(R.string.error_field_required)
            send = false
            focusView = binding.edttPhone
        }
        if (!send) {
            focusView!!.requestFocus()
        }
        return send
    }
}
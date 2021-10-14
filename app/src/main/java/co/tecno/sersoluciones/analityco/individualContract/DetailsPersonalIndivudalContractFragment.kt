package co.tecno.sersoluciones.analityco.individualContract

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import co.com.sersoluciones.barcodedetectorser.utilities.DebugLog
import co.com.sersoluciones.facedetectorser.FaceTrackerActivity
import co.com.sersoluciones.facedetectorser.serlibrary.PhotoSer
import co.tecno.sersoluciones.analityco.ApplicationContext
import co.tecno.sersoluciones.analityco.R
import co.tecno.sersoluciones.analityco.databases.DBHelper
import co.tecno.sersoluciones.analityco.databinding.FragmentPersonDetailsIndividualContractBinding
import co.tecno.sersoluciones.analityco.models.DaneCity
import co.tecno.sersoluciones.analityco.models.PersonalDataContact
import co.tecno.sersoluciones.analityco.models.PersonalIndividualContract
import co.tecno.sersoluciones.analityco.models.PersonalNew
import co.tecno.sersoluciones.analityco.nav.CreatePersonalViewModel
import co.tecno.sersoluciones.analityco.ui.createPersonal.viewmodels.PersonalViewModel
import co.tecno.sersoluciones.analityco.utilities.Constantes
import co.tecno.sersoluciones.analityco.utilities.MetodosPublicos
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class DetailsPersonalIndivudalContractFragment: Fragment() {

    private lateinit var binding: FragmentPersonDetailsIndividualContractBinding
    var PersonalId : String? = null
    var Nationality : Int? = 0
    var CityCode : String? = null
    var DateBirthDate : String? = null
    var CreateDate : String? = null
    var CreatePersonalEmployerInfo = false
    var UpdatePersonalEmployerInfo = false
    var UpdatePerson = false
    var PersonalEmployerInfoId:String? = null
    var PhotoPerson : String? = null

    @Inject
    lateinit var viewModel: CreatePersonalViewModel

    @Inject
    lateinit var viewModelList: PersonalViewModel

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPersonDetailsIndividualContractBinding.inflate(inflater)
        binding.lifecycleOwner = this
        return binding.root
    }

    @SuppressLint("SimpleDateFormat")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val arguments = DetailsPersonalIndivudalContractFragmentArgs.fromBundle(requireArguments())
        val personal = arguments.personal
        binding.personal = personal

        val json = Gson().toJson(personal)
        DebugLog.logW("PersonalCreate: ${Gson().toJson(json)}")
        (requireActivity() as AppCompatActivity).supportActionBar?.title = "Seleccionar empleador"

        PersonalId = personal.PersonalId
        CityCode = personal.CityOfBirthCode
        DateBirthDate = personal.BirthDate

        CreateDate = if (personal.CreateDate != null){
            personal.CreateDate
        }else{
            val create = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
            create.format(Date())
        }

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

        Nationality = personal.Nationality

        viewModel.DataContactNotFound.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            it?.let {
                println("Existe la persona ${it}")
                if(it){
                    CreatePersonalEmployerInfo = true
                }else{
                    binding.personalContact = viewModel.personalContractData.value
                    PersonalEmployerInfoId = viewModel.personalContractData.value!!.Id
                    viewModel.PersonalAssociateWithEmployer = true
                    viewModel.PersonalId = PersonalId
                    viewModel.Nationality = Nationality
                    findNavController()
                            .navigate(
                                    DetailsPersonalIndivudalContractFragmentDirections.actionDetailsPersonalFragment2ToPlaceToJobFragment()
                            )
                }
            }
        })
        viewModel.navigateToSelectPlaceJobFragment.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            it?.let {
                if (it) {
                    findNavController()
                            .navigate(
                                    DetailsPersonalIndivudalContractFragmentDirections.actionDetailsPersonalFragment2ToPlaceToJobFragment()
                            )
                    viewModel.doneNavigatingSelectPlaceToJob()
                }
            }
        })
        binding.controlButtons.nextButton.setOnClickListener {
            if(submit()){
                if(UpdatePerson){
                    UpdatePersonal()
                }
                if(CreatePersonalEmployerInfo){
                    viewModel.PersonalAssociateWithEmployer = false
                    createPersonEmployerInfo()
                }

            }
        }
        binding.controlButtons.backButton.setOnClickListener {
            findNavController().popBackStack()
        }

        if(personal.CityOfBirthCode !=null){
        val Code = StringBuilder(personal.CityOfBirthCode!!)
        Code.deleteCharAt(0)
        SelectCityBirth(Code, personal.Nationality)
        }
        if (personal.Nationality == 0) binding.nationalityFlag.setImageResource(R.drawable.ic_flag_of_colombia) else binding.nationalityFlag.setImageResource(R.drawable.ic_flag_of_venezuela)

        binding.headerImg.setOnClickListener {startFaceDectectorActivity()}
        println("Escaneado?${personal.IsScanned!!}")
        if(personal.IsScanned!!){
            binding.infoUserLayout.visibility = View.VISIBLE
            binding.namePersonLayout.visibility = View.VISIBLE
            UpdatePerson = false
        }else {
            binding.infoUserLayoutEdit.visibility = View.VISIBLE
            binding.namePersonLayoutEdit.visibility = View.VISIBLE
            UpdatePerson =  true
        }
    }

    private fun UpdatePersonal() {
        val UpdatePersonal = PersonalNew(
                "",
                binding.personal?.Nationality!!,
                binding.personal?.DocumentType!!,
                binding.personal?.DocumentNumber!!,
                binding.namePersonEdit.text.toString(),
                binding.lastnameEdit.text.toString(),
                CityCode,
                binding.textRhEdit.text.toString(),
                binding.textGenderEdit.text.toString(),
                DateBirthDate
        )
        val json = Gson().toJson(UpdatePersonal)
        DebugLog.logW("jsonInsertPerson: ${Gson().toJson(json)}")
        viewModel.updatePersonalToIndividualContract(UpdatePersonal)
    }
    private fun createPersonEmployerInfo() {
        val personal = viewModel.personal.value!!
        val dataContact = PersonalDataContact(
                binding.edttPhone.text.toString(),
                personal.CityCode,
                binding.edttAddress.text.toString(),
                binding.edttNameContact.text.toString(),
                binding.edttPhoneContact.text.toString(),
                binding.edttEmail.text.toString(),
                viewModel.EmployerId,
                PersonalId
        )
        val newPersonal = PersonalIndividualContract(
                binding.personal?.Id,
                binding.personal?.PersonalId,
                binding.personal?.CompanyInfoId,
                binding.namePerson.text.toString(),
                binding.lastname.text.toString(),
                binding.personal?.DocumentNumber!!,
                binding.textGender.text.toString(),
                binding.textRh.text.toString(),
                binding.personal?.CityOfBirthCode,
                DateBirthDate,
                binding.personal?.Nationality!!,
                "",
                CreateDate,
                binding.personal?.DocumentType
        )
        val json = Gson().toJson(dataContact)
        DebugLog.logW("jsonInsert: ${Gson().toJson(json)}")
        viewModel.CreatePersonalEmployerInfo(dataContact,PhotoPerson,newPersonal)
    }
    @SuppressLint("Recycle")
    private fun SelectCityBirth(code: java.lang.StringBuilder, nationality: Int) {
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
                    PhotoPerson = uriImage.toString()
                    println("Foto tomada${PhotoPerson}")
                }
                else -> {
                }
            }
        }
    }
    override fun onAttach(context: Context) {
        super.onAttach(context)
        ApplicationContext.analitycoComponent.inject(this)
    }
    fun submit(): Boolean {
        var send = true
        var focusView: View? = null
        if (CreatePersonalEmployerInfo || UpdatePersonalEmployerInfo) {
            if ((viewModel.personal.value!!.Photo == null || viewModel.personal.value!!.Photo.isNullOrEmpty())) {
                MetodosPublicos.alertDialog(activity, "La foto es obligatoria")
                send = false
                focusView = binding.headerImg
            } else if (TextUtils.isEmpty(binding.edttAddress.text.toString())) {
                binding.edttAddress.error = getString(R.string.error_field_required)
                send = false
                focusView = binding.edttAddress
            } else if (TextUtils.isEmpty(binding.edttEmail.text.toString())) {
                binding.edttEmail.error = getString(R.string.error_field_required)
                send = false
                focusView = binding.edttEmail
            } else if (TextUtils.isEmpty(binding.edttPhone.text.toString())) {
                binding.edttPhone.error = getString(R.string.error_field_required)
                send = false
                focusView = binding.edttPhone
            }
        }
        if (!send) {
            focusView!!.requestFocus()
        }
        return send
    }
}
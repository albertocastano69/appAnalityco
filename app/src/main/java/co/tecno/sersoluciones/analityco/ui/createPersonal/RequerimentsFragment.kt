package co.tecno.sersoluciones.analityco.ui.createPersonal

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.content.*
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.DatePicker
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import co.com.sersoluciones.barcodedetectorser.utilities.DebugLog
import co.com.sersoluciones.barcodedetectorser.utilities.DebugLog.logW
import co.com.sersoluciones.facedetectorser.FaceTrackerActivity
import co.com.sersoluciones.facedetectorser.serlibrary.PhotoSer
import co.tecno.sersoluciones.analityco.ApplicationContext
import co.tecno.sersoluciones.analityco.ApplicationContext.Companion.applicationContext
import co.tecno.sersoluciones.analityco.R
import co.tecno.sersoluciones.analityco.adapters.CustomArrayAdapter
import co.tecno.sersoluciones.analityco.adapters.registerAndVerify.VerifyRequirimentsAdapter
import co.tecno.sersoluciones.analityco.databases.DBHelper
import co.tecno.sersoluciones.analityco.databinding.FragmentRequerimentsBinding

import co.tecno.sersoluciones.analityco.models.PersonalContractOfflineNetwork
import co.tecno.sersoluciones.analityco.models.ProjectPersonalOffine
import co.tecno.sersoluciones.analityco.models.RequirementsList
import co.tecno.sersoluciones.analityco.models.Scheduler
import co.tecno.sersoluciones.analityco.nav.CreatePersonalViewModel
import co.tecno.sersoluciones.analityco.nav.ResponseResult
import co.tecno.sersoluciones.analityco.nav.SecurityReference
import co.tecno.sersoluciones.analityco.receivers.RequestBroadcastReceiver
import co.tecno.sersoluciones.analityco.services.CRUDService
import co.tecno.sersoluciones.analityco.services.CrudIntentService
import co.tecno.sersoluciones.analityco.services.LocationUpdateService
import co.tecno.sersoluciones.analityco.utilities.*
import co.tecno.sersoluciones.analityco.viewmodels.PersonalDailyViewModel
import com.android.volley.Request
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.squareup.picasso.Picasso
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

class RequerimentsFragment : Fragment(), RequestBroadcastReceiver.BroadcastListener,
    DatePickerDialog.OnDateSetListener,
    VerifyRequirimentsAdapter.OnVerifyReqListener {
    @Inject
    lateinit var viewModel: CreatePersonalViewModel
    @Inject
    lateinit var viewModelPerson: PersonalDailyViewModel
    private lateinit var binding: FragmentRequerimentsBinding
    private var requirements: ArrayList<RequirementsList> = arrayListOf()
    private var verifyRequirimentsAdapter: VerifyRequirimentsAdapter? = null
    private var typeARLModel: RequirementsList.TypeARL? = null
    private var documentNumberSurvey: String? = null
    private var birthdaySurvey: String? = null
    private var idProject: String? = null
    private var urlModel: String? = null
    private var reqsToSend: Array<String?>? = null
    private var positionArray = 0
    private var idReqOk = -1
    private var idReq = 0
    private var fromDateStr: String? = null
    private var fromDate: Date? = null
    private var fromDatePickerDialog: DatePickerDialog? = null
    private var updatePersonal: Boolean = false
    private var mImageUri: Uri? = null
    private var contractId: String? = null
    private var personalInfoId = 0
    private var nameOfPersonal : String? =  null
    private var idSteps = 0
    private var requestBroadcastReceiver: RequestBroadcastReceiver? = null
    private var isValidedCheck = false
    var TypeARL : String? = null
    var isArl = false
    var isAfp = false
    var isEps = false
    private var epsId = 0
    private var afpId = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requirements = ArrayList()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentRequerimentsBinding.inflate(inflater)
        binding.lifecycleOwner = this
        return binding.root
    }

    @SuppressLint("SetTextI18n", "UseRequireInsteadOfGet")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        logW("Se ejecuta onActivityCreated")
        showProgress(true)
        requestBroadcastReceiver = RequestBroadcastReceiver(this)
        (requireActivity() as AppCompatActivity).supportActionBar?.title =
            "CC ${viewModel.personal.value!!.DocumentNumber}"
        (requireActivity() as AppCompatActivity).supportActionBar?.subtitle =
            "${viewModel.personal.value!!.Name} ${viewModel.personal.value!!.LastName}"
        val arguments = RequerimentsFragmentArgs.fromBundle(requireArguments())
        val name = viewModel.personal.value!!.Name
        val lastname = viewModel.personal.value!!.LastName
        personalInfoId = viewModel.personal.value!!.Id!!.toInt()
        nameOfPersonal="$name $lastname"
        contractId = arguments.contractId
        idProject = viewModel.selectedProject.value!!.Id

        viewModelPerson.secRefereces.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
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

        LoadContractPerOffline(idProject)
        binding.submitButton.setOnClickListener {
            LoadContractPerOffline(idProject)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        ApplicationContext.analitycoComponent.inject(this)
    }

    @SuppressLint("UseRequireInsteadOfGet")
    private fun init() {
        showProgress(false)
        viewModelPerson.personalContractOffline.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            it?.let {
                if (updatePersonal) replacePersonalInfo(it)
            }
        })
        var mLayoutManager = LinearLayoutManager(activity)
        binding.recyclerRequirements.layoutManager = mLayoutManager
        verifyRequirimentsAdapter = VerifyRequirimentsAdapter(activity !!, requirements, this,personalInfoId,nameOfPersonal)
        binding.recyclerRequirements.itemAnimator = DefaultItemAnimator()
        val mDividerItemDecoration = DividerItemDecoration(
            binding.recyclerRequirements.context,
            mLayoutManager.orientation
        )
        //recyclerRequirements.addItemDecoration(mDividerItemDecoration);//recyclerRequirements.addItemDecoration(mDividerItemDecoration);

        if (nameOfPersonal != null && viewModel.personal.value!!.DocumentNumber != null) {
            logW("Se ejecuta ContentResolver ContractPerOffline")
            val selection =
                ("(" + DBHelper.CONTRACT_PERSON_TABLE_COLUMN_PROJECT_ID + " = ?) and (" + DBHelper.CONTRACT_PERSON_TABLE_COLUMN_DOC_NUM + " = ?) and ( "
                        + DBHelper.CONTRACT_PERSON_TABLE_COLUMN_CONTRACT_ID + " = ? )")
            val selectionArgs = arrayOf(idProject, viewModel.personal.value!!.DocumentNumber, contractId)
            requireActivity().contentResolver.query(
                Constantes.CONTENT_CONTRACT_PER_OFFLINE_URI,
                null,
                selection,
                selectionArgs,
                null
            )?.use { cursorPersonal ->
                logW("Entra el cursor personal para validar vigencia")
                validateVigency(cursorPersonal)
                cursorPersonal.close()
            }
        }

        binding.recyclerRequirements.adapter = verifyRequirimentsAdapter
        binding.fabRemove.visibility = View.GONE
        requirementOffline()
        binding.iconFile.setOnClickListener {
            startFaceDectectorActivity()
        }
        binding.fromDateBtn.setOnClickListener{
            fromDatePickerDialog!!.datePicker.tag = R.id.fromDateBtn
            fromDatePickerDialog!!.show()
        }
        binding.negativeButton.setOnClickListener {
            binding.editRequirement.visibility = View.GONE
            binding.recyclerRequirements.visibility = View.VISIBLE
        }
        binding.positiveButton.setOnClickListener {
            updateImage()
        }
        binding.fabRemove.setOnClickListener{
            mImageUri = null
            binding.iconFile.setImageResource(R.drawable.ic_note_text)
            binding.fabRemove.visibility = View.GONE
        }
        binding.submitButton.setOnClickListener {
            val i = Intent()
            i.putExtra("personalInfoId", personalInfoId)
            activity?.setResult(Activity.RESULT_OK, i)
            viewModel.clearForm()
            activity?.finish()
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
    private  fun LoadContractPerOffline(idProject: String?){
        val values = ContentValues()
        values.put(Constantes.KEY_SELECT, true)
        values.put("project", true)
        values.put("projectId", idProject)
        val paramsQuery = HttpRequest.makeParamsInUrl(values)
        co.com.sersoluciones.facedetectorser.utilities.DebugLog.logW("Entrada$paramsQuery")
        CRUDService.startRequest(
            activity, Constantes.LIST_CONTRACT_PER_OFFLINE_URL,
            Request.Method.GET, paramsQuery, true
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == 207) {
            viewModelPerson.loadPersonalContractOffline(personalInfoId.toLong())
        }
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
    override fun onDateSet(datePicker: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        val code = datePicker?.tag as Int
        //logE("CODE: " + code);
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
    @SuppressLint("SimpleDateFormat", "UseRequireInsteadOfGet")
    private fun updateImage() {
        // Log.e("path", "logo:" + mPhotoPath);
        var cancel = true
        if (fromDateStr != null) {
            cancel = false
            binding.tvFromDateError.error = "fecha obligatoria"
            val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
            fromDateStr = format.format(fromDate!!)
        }
        if (mImageUri != null && !mImageUri.toString().isEmpty() && !cancel) {
            val params = HashMap<String, String>()
            params["file"] = mImageUri.toString()
            val url = "api/$urlModel/UpdateFile/$idReq"
            CrudIntentService.startActionFormData(
                activity!!, url,
                Request.Method.PUT, params
            )
            val requeriment = RequerimentValid(fromDateStr!!)
            val gson = Gson()
            val json = gson.toJson(requeriment)
            CrudIntentService.startRequestCRUD(
                activity,
                "api/$urlModel/$idReq", Request.Method.PUT, json, "", false
            )
            mImageUri = null
            //showProgress(true)
        } else {
            val builder = AlertDialog.Builder(activity!!)
                .setMessage("Es necesario seleccionar una imagen")
            builder.create().show()
        }
    }

    override fun editRequerimentItem(mItem: RequirementsList?, pos: Int) {
        val builder = AlertDialog.Builder(requireActivity())
            .setPositiveButton("SI") { dialog: DialogInterface?, which: Int ->
                urlModel = mItem!!.Model
                positionArray = pos
                idReq =
                    if (mItem.EPSName != null || mItem.AFPName != null || mItem.ARLName != null) mItem.Id else mItem.Id
                idReqOk = pos
                binding.fromDateBtn.visibility = View.VISIBLE
                binding.editRequirement.visibility = View.VISIBLE
                binding.recyclerRequirements.visibility = View.GONE
                binding.requirementTitle.text = mItem.Type
                binding.requiremnetDes.text = mItem.Desc
                when{
                    mItem.Type.equals("ARL")->{
                        binding.typeRisk.visibility = View.VISIBLE
                        val tipoRiesgos = arrayOf("Seleccione", "V", "IV", "III", "II", "I")
                        binding.spinnerTypeRisk.adapter = CustomArrayAdapter(requireContext(), tipoRiesgos)
                        binding.spinnerTypeRisk.setSelection(1)
                        TypeARL = binding.spinnerTypeRisk.selectedItem.toString()
                        binding.afpInputLayout.visibility = View.GONE
                        binding.epsInputLayout.visibility = View.GONE
                        isArl = true
                        isAfp = false
                        isEps = false
                    }
                    mItem.Type.equals("EPS") ->{
                        isEps = true
                        binding.epsInputLayout.visibility = View.VISIBLE
                        binding.typeRisk.visibility = View.GONE
                        binding.afpInputLayout.visibility = View.GONE
                        isArl = false
                        isAfp = false
                    }
                    mItem.Type.equals("AFP") ->{
                        isAfp = true
                        binding.afpInputLayout.visibility = View.VISIBLE
                        binding.epsInputLayout.visibility = View.GONE
                        binding.typeRisk.visibility = View.GONE
                        isArl = false
                        isEps = true
                    }
                }
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
                binding.fromDateBtn!!.text = fromDateStr
                if (!mItem.RequiredDate) binding.fromDateBtn!!.visibility = View.GONE
                updateDatePicker(fromDate!!)
            }
            .setNegativeButton("NO", null)
            .setMessage("Es necesario tener acceso a internet para subir archivos, desea continuar?")
        builder.create().show()
        requirements!![pos] = mItem!!
        verifyRequirimentsAdapter!!.notifyDataSetChanged()
    }

    override fun arlRiskAlert() {
    }

    override fun openSurveyActivity(intent: Intent, pos: Int) {
        startActivityForResult(intent, 707)
        updatePersonal = true
    }

    @SuppressLint("SetTextI18n")
    @Throws(JSONException::class)
    private fun requirementsObjectOffline(ReqJson: String?) {
        logW("se ejecuta requerimentsObjectOffline")
        if (ReqJson == null || ReqJson == "{}") return
        requirements!!.clear()
        isValidedCheck = false
        val jsonObject: JSONObject
        val jsonObjectSs: JSONArray
        val jsonObjectTypeARL: JSONObject
        val jsonObjectSurvey: JSONArray
        var requirement: RequirementsList
        typeARLModel = null
        jsonObject = JSONObject(ReqJson)

        jsonObjectTypeARL = jsonObject.getJSONObject("TypeARL")
        typeARLModel = Gson().fromJson(
                jsonObjectTypeARL.toString(),
                object : TypeToken<RequirementsList.TypeARL?>() {}.type
            )
            jsonObjectSs = jsonObject.getJSONArray("SocialSecurity")
            for (i in 0 until jsonObjectSs.length()) {
                requirement = Gson().fromJson(
                    jsonObjectSs.getJSONObject(i).toString(),
                    object : TypeToken<RequirementsList?>() {}.type
                )
                requirement.withFile = true
                requirement.RequiredDate = true
                requirement.IsSocialSec = true
                if (requirement.ARLName != null) {
                    requirement.typeARL = typeARLModel
                }
                logW("DocsRequerimentJSONSocialSecurity: " + requirement)
                requirements!!.add(requirement)
                if (!requirement.IsValided && !isValidedCheck) isValidedCheck = true

            }
        var jsonObjectReq: JSONArray = jsonObject.getJSONArray("Certification")
            for (j in 0 until jsonObjectReq.length()) {
                requirement = Gson().fromJson(
                    jsonObjectReq.getJSONObject(j).toString(),
                    object : TypeToken<RequirementsList?>() {}.type
                )
                requirement.withFile = true
                if (requirement.IsDate) requirement.RequiredDate = true
                requirement.IsCertification = true
                logW("DocsJSON: " + requirement.DocsJSON)
                if (requirement.DocsJSON == null || requirement.DocsJSON.isEmpty() || JSONObject(
                        requirement.DocsJSON
                    ).length() == 0
                ) {
                    requirement.IsValided = false
                }
                requirements!!.add(requirement)
                logW("DocsRequerimentJSONCertification: " + requirement)
               // if (!requirement.IsValided && !isValidedCheck) isValidedCheck = true

            }
            jsonObjectSurvey = jsonObject.getJSONArray("Surveys")
            for (j in 0 until jsonObjectSurvey.length()) {
                requirement = Gson().fromJson(
                        jsonObjectSurvey.getJSONObject(j).toString(),
                        object : TypeToken<RequirementsList?>() {}.type
                )
                requirement.withFile = false
                requirement.IsSurvey = true
                requirement.RequiredDate = true
                requirement.documentNumber = documentNumberSurvey
                requirement.birthday = birthdaySurvey
                requirement.projectId = idProject


                requirements!!.add(requirement)
                logW("DocsRequerimentJSONSurvers: " + requirement)
                //if (!requirement.IsValided && !isValidedCheck) isValidedCheck = true
                //isValidedSurvey = false
            }
        if(requirements.size == 0) {
            binding.recyclerRequirements.visibility = View.GONE
            binding.labelRequisitos.visibility = View.VISIBLE
            Toast.makeText(activity, "No tiene Requisitos asociados", Toast.LENGTH_SHORT).show()
            val i = Intent()
            i.putExtra("personalInfoId", personalInfoId)
            activity?.setResult(Activity.RESULT_OK, i)
            viewModel.clearForm()
            activity?.finish()
        }
        verifyRequirimentsAdapter!!.update()
    }
    @SuppressLint("UseRequireInsteadOfGet")
    private fun updateDatePicker(fromDate: Date) {
        val cF = Calendar.getInstance()
        cF.time = fromDate
        fromDatePickerDialog = DatePickerDialog(
            activity!!, this, cF[Calendar.YEAR],
            cF[Calendar.MONTH], cF[Calendar.DAY_OF_MONTH]
        )
        fromDatePickerDialog!!.datePicker.minDate = System.currentTimeMillis()
    }
    @SuppressLint("UseRequireInsteadOfGet")
    private fun startFaceDectectorActivity() {
        PhotoSer.ActivityBuilder()
            .setDetectFace(false)
            .setQuality(50)
            .setSaveGalery(false)
            .setCrop(false)
            .start(this, requireActivity())
    }


    internal inner class RequerimentValid(var Date: String) {
        var IsValided = true

    }
    private fun replacePersonalInfo(listInfo: List<PersonalContractOfflineNetwork>) {

        val personalInfo = listInfo.firstOrNull { x ->
            x.ProjectId == idProject
                    && x.ContractId == contractId && x.PersonalCompanyInfoId == personalInfoId
        }
        personalInfo.let {
            val contentValues = Utils.reflectToContentValue(JSONObject(Gson().toJson(personalInfo)))
            val selection =
                ("(" + DBHelper.CONTRACT_PERSON_TABLE_COLUMN_PERSONALCOMPANY_ID + " = ?) and ( "
                        + DBHelper.CONTRACT_PERSON_TABLE_COLUMN_CONTRACT_ID + " = ? )")
            val selectionArgs =
                arrayOf(personalInfo?.PersonalCompanyInfoId.toString(), personalInfo?.ContractId)
            requireActivity().contentResolver.delete(
                Constantes.CONTENT_CONTRACT_PER_OFFLINE_URI,
                selection,
                selectionArgs
            )
            requireActivity().contentResolver.insert(Constantes.CONTENT_CONTRACT_PER_OFFLINE_URI, contentValues)
            updatePersonal = false
            viewModelPerson.clearPersonalContractOffline()
            requireActivity().recreate()
        }
    }
    private fun requirementOffline() {
        logW("se ejecuta requerimentOffline")
        if (reqsToSend == null || reqsToSend!!.isEmpty()) return
        logW("jsonreq: ${reqsToSend!![0]}")
        val reqJson = reqsToSend!![0]
        try {
            requirementsObjectOffline(reqJson)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        //schedulerOffline(maxJson, minJson, dayJson, startDateJson, finishDateJson)
    }
    @SuppressLint("UseRequireInsteadOfGet")
    private fun validateVigency(cursorPersonal: Cursor) {
        logW("Se ejecuta validateVigency")
//        var enterProject: Int
        var flagEnter: Boolean
        if (cursorPersonal.moveToFirst()) {
//            val EnterProjectCursor = cursorPersonal.getColumnIndex(DBHelper.CONTRACT_PERSON_TABLE_COLUMN_ENTERPROJECT)
            val startDatePersonal =
                cursorPersonal.getColumnIndex(DBHelper.CONTRACT_PERSON_TABLE_COLUMN_START_DATE)
            logW("DatePersonal: ${startDatePersonal}")
            val finishDatePersonal =
                cursorPersonal.getColumnIndex(DBHelper.CONTRACT_PERSON_TABLE_COLUMN_FINISH_DATE)
            logW("DatePersonal: ${startDatePersonal}")
            val startDateContrcat =
                cursorPersonal.getColumnIndex(DBHelper.CONTRACT_PERSON_TABLE_COLUMN_START_DATE_CONTRACT)
            logW("DatePersonal: ${startDatePersonal}")
            val finishDateContract =
                cursorPersonal.getColumnIndex(DBHelper.CONTRACT_PERSON_TABLE_COLUMN_FINISH_DATE_CONTRACT)
            logW("DatePersonal: ${startDatePersonal}")
            val contractValidity = Scheduler()
            contractValidity.Allow = true
            do {
//                enterProject = cursorPersonal.getInt(EnterProjectCursor)
//                if (enterProject == 1) {
                flagEnter = true
                var startDateJson = cursorPersonal.getString(startDatePersonal)
                if (startDateJson == null) startDateJson =
                    cursorPersonal.getString(startDateContrcat)
                var finishDateJson = cursorPersonal.getString(finishDatePersonal)
                if (finishDateJson == null) finishDateJson =
                    cursorPersonal.getString(finishDateContract)
                if (startDateJson != null && finishDateJson != null) {
                    @SuppressLint("SimpleDateFormat") val format =
                        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                    try {
                        logW("Entro al try")
                        val startDate = format.parse(startDateJson)
                        val finishDate = format.parse(finishDateJson)
                        val rightNow = Calendar.getInstance()
                        if (rightNow.time.after(startDate)) {
                            if (rightNow.time.before(finishDate)) {
                                contractValidity.Allow = true
                                contractValidity.Desc = "OK"
                                logW("contractValidy " + contractValidity.Desc)
                            } else {
                                contractValidity.Allow = false
                                contractValidity.Desc = "Contrato vencido"
                                logW("contractValidy " + contractValidity.Desc)
                            }
                        } else {
                            contractValidity.Allow = false
                            contractValidity.Desc = "Contrato sin iniciar"
                            logW("contractValidy " + contractValidity.Desc)
                        }
                    } catch (e: ParseException) {
                        e.printStackTrace()
                    }
                    if (contractValidity.Allow){
                        logW("Contract validity allow")
                        break
                    }
                }
//
            }
            while (cursorPersonal.moveToNext())
            logW("flag validateVigency" + flagEnter)
            if (!flagEnter) {
                val builder = AlertDialog.Builder(activity!!)
                    .setTitle("Permitir paso")
                    .setPositiveButton("Aceptar", null)
                    .setMessage("No hay acceso a este proyecto")
                builder.create().show()
            } else if (!contractValidity.Allow) {
                val builder = AlertDialog.Builder(activity!!)
                    .setTitle("Vigencia del contrato")
                    .setMessage(contractValidity.Desc)
                builder.create().show()
            } else {
                reqsToSend = processPersonalReqs(cursorPersonal)
                launchCheckReqPersonal()
            }
        }
    }
    private fun launchCheckReqPersonal() {
        logW("se ejecuta launchCheckReqPersonal")
        requirementOffline()
    }
    private fun processPersonalReqs(cursorPersonal: Cursor): Array<String?> {
        logW("se ejecuta processPersonalReqs")

        var reqsToSend = arrayOfNulls<String>(0)
        val projectContracts =
            cursorPersonal.getColumnIndex(DBHelper.CONTRACT_PERSON_TABLE_COLUMN_PROJECT_CONTRACTS)

        val req = cursorPersonal.getColumnIndex(DBHelper.CONTRACT_PERSON_TABLE_COLUMN_REQS)
        val max = cursorPersonal.getColumnIndex(DBHelper.CONTRACT_PERSON_TABLE_COLUMN_MAXHOUR)
        val min = cursorPersonal.getColumnIndex(DBHelper.CONTRACT_PERSON_TABLE_COLUMN_MINHOUR)
        val maxa = cursorPersonal.getColumnIndex(DBHelper.CONTRACT_PERSON_TABLE_COLUMN_AGEMAX)
        val mina = cursorPersonal.getColumnIndex(DBHelper.CONTRACT_PERSON_TABLE_COLUMN_AGEMIN)
        val day = cursorPersonal.getColumnIndex(DBHelper.CONTRACT_PERSON_TABLE_COLUMN_WEEKDAYS)
        val contract =
            cursorPersonal.getColumnIndex(DBHelper.CONTRACT_PERSON_TABLE_COLUMN_CONTRACT_ID)
        val person =
            cursorPersonal.getColumnIndex(DBHelper.CONTRACT_PERSON_TABLE_COLUMN_PERSONAL_ID)
        val startDate =
            cursorPersonal.getColumnIndex(DBHelper.CONTRACT_PERSON_TABLE_COLUMN_START_DATE)
        val finishDate =
            cursorPersonal.getColumnIndex(DBHelper.CONTRACT_PERSON_TABLE_COLUMN_FINISH_DATE)
        val startDateContrcat =
            cursorPersonal.getColumnIndex(DBHelper.CONTRACT_PERSON_TABLE_COLUMN_START_DATE_CONTRACT)
        val finishDateContract =
            cursorPersonal.getColumnIndex(DBHelper.CONTRACT_PERSON_TABLE_COLUMN_FINISH_DATE_CONTRACT)

        val projectJson = cursorPersonal.getString(projectContracts)
        val idContract = cursorPersonal.getString(contract)
        logW("projectId: $idProject ProjectContracts: $projectJson $maxa $mina")
        val projectObj = Gson().fromJson<ArrayList<ProjectPersonalOffine>>(
            projectJson,
            object : TypeToken<ArrayList<ProjectPersonalOffine?>?>() {}.type
        )
        documentNumberSurvey =
            cursorPersonal.getString(cursorPersonal.getColumnIndex(DBHelper.CONTRACT_PERSON_TABLE_COLUMN_DOC_NUM))
        birthdaySurvey =
            cursorPersonal.getString(cursorPersonal.getColumnIndex(DBHelper.CONTRACT_PERSON_TABLE_COLUMN_BIRTHDAY))
        for (projectPersonalOffine in projectObj) {
            for (stage in projectPersonalOffine.ProjectStages) {
                if (projectPersonalOffine.ProjectId == idProject && contractId == idContract) {
                    logW(Gson().toJson(projectPersonalOffine))
                    idSteps = Integer.valueOf(stage)
                    val ReqJson = cursorPersonal.getString(req)
                    val maxJson = cursorPersonal.getString(max)
                    val minJson = cursorPersonal.getString(min)
                    val dayJson = cursorPersonal.getString(day)
                    var startDateJson = cursorPersonal.getString(startDate)
                    if (startDateJson == null) startDateJson =
                        cursorPersonal.getString(startDateContrcat)
                    var finishDateJson = cursorPersonal.getString(finishDate)
                    if (finishDateJson == null) finishDateJson =
                        cursorPersonal.getString(finishDateContract)
                    logW("Requeriments"+ReqJson)
                    reqsToSend =
                        arrayOf(ReqJson, maxJson, minJson, dayJson, startDateJson, finishDateJson)
                    break
                }
            }
        }
        return reqsToSend
    }
    @SuppressLint("UseRequireInsteadOfGet")
    override fun onResume() {
        super.onResume()
        val intentFilter = IntentFilter()
        intentFilter.addAction(Constantes.BROADCAST_GET_JSON)
        intentFilter.addAction(CrudIntentService.ACTION_REQUEST_FORM_DATA)
        intentFilter.addAction(CRUDService.ACTION_REQUEST_SAVE)
        intentFilter.addAction(CRUDService.ACTION_REQUEST_GET)
        LocalBroadcastManager.getInstance(activity!!).registerReceiver(
            requestBroadcastReceiver!!,
            intentFilter
        )
        bindMyService()
    }

    @SuppressLint("UseRequireInsteadOfGet")
    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(activity!!).unregisterReceiver(requestBroadcastReceiver!!)
        //if (LocationUpdateService.isRunning())+}
        requireActivity().unbindService(myConnection)

    }

    private fun bindMyService() {
        val intent = Intent(applicationContext, LocationUpdateService::class.java)
        requireActivity().bindService(intent, myConnection, Context.BIND_AUTO_CREATE)
    }
    private var myService: LocationUpdateService? = null
    private val myConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(
            componentName: ComponentName,
            iBinder: IBinder
        ) {
            DebugLog.log("Servicio conectado")
            val b = iBinder as LocationUpdateService.MyBinder
            myService = b.service
        }

        override fun onServiceDisconnected(componentName: ComponentName) {
            DebugLog.log("Servicio desconectado")
            myService = null
        }
    }
    override fun onStringResult(action: String?, option: Int, response: String?, url: String?) {
        logW(url + option)
        when (option) {
            Constantes.SUCCESS_REQUEST -> {
                when (url) {
                    Constantes.LIST_CONTRACT_PER_OFFLINE_URL -> {
                        co.com.sersoluciones.facedetectorser.utilities.DebugLog.log("tabla llena LIST_CONTRACT_PER_OFFLINE_URL")
                        init()

                    }
                }
            }
            Constantes.SUCCESS_FILE_UPLOAD -> if (url == "api/$urlModel/UpdateFile/$idReq") {
                logW(response)
                val reqRequest = Gson().fromJson<RequirementsList>(
                        response,
                        object : TypeToken<RequirementsList?>() {}.type
                )
                updatePersonal = true
                viewModelPerson.loadPersonalContractOffline(personalInfoId.toLong())
            }
            Constantes.SEND_REQUEST -> if (url == Constantes.LIST_CONTRACT_PER_OFFLINE_URL) {
                //showProgress(false)
                requireActivity().recreate()
            }
            Constantes.NOT_INTERNET, Constantes.BAD_REQUEST, Constantes.TIME_OUT_REQUEST -> {
                //showProgress(false)
                Toast.makeText(activity, "Sin conexion a Internet", Toast.LENGTH_SHORT).show()
                binding.editRequirement.visibility = View.GONE
            }
        }
    }
}
package co.tecno.sersoluciones.analityco

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.content.*
import android.content.pm.ActivityInfo
import android.content.res.AssetFileDescriptor
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.graphics.drawable.ColorDrawable
import android.media.MediaPlayer
import android.net.Uri
import android.os.*
import android.util.TypedValue
import android.view.*
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ArrayAdapter
import android.widget.DatePicker
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.*
import co.com.sersoluciones.barcodedetectorser.utilities.DebugLog
import co.com.sersoluciones.barcodedetectorser.utilities.DebugLog.log
import co.com.sersoluciones.barcodedetectorser.utilities.DebugLog.logW
import co.com.sersoluciones.facedetectorser.FaceTrackerActivity
import co.com.sersoluciones.facedetectorser.serlibrary.PhotoSer
import co.tecno.sersoluciones.analityco.adapters.CustomArrayAdapter
import co.tecno.sersoluciones.analityco.adapters.PersonalDailyAdapter
import co.tecno.sersoluciones.analityco.adapters.registerAndVerify.InspectRequirimentsAdapter
import co.tecno.sersoluciones.analityco.adapters.registerAndVerify.InspectRequirimentsAdapter.OnInspectReqListener
import co.tecno.sersoluciones.analityco.adapters.registerAndVerify.VerifyRequirimentsAdapter
import co.tecno.sersoluciones.analityco.adapters.registerAndVerify.VerifyRequirimentsAdapter.OnVerifyReqListener
import co.tecno.sersoluciones.analityco.databases.DBHelper
import co.tecno.sersoluciones.analityco.databinding.ActivityCheckReqsPersonalBinding
import co.tecno.sersoluciones.analityco.databinding.ContentCheckReqsPersonalBinding
import co.tecno.sersoluciones.analityco.models.*
import co.tecno.sersoluciones.analityco.models.ReportPersonal.OptionsJson
import co.tecno.sersoluciones.analityco.models.RequirementsList.TypeARL
import co.tecno.sersoluciones.analityco.nav.SecurityReference
import co.tecno.sersoluciones.analityco.receivers.RequestBroadcastReceiver
import co.tecno.sersoluciones.analityco.receivers.RequestBroadcastReceiver.BroadcastListener
import co.tecno.sersoluciones.analityco.services.CRUDService
import co.tecno.sersoluciones.analityco.services.CrudIntentService
import co.tecno.sersoluciones.analityco.services.LocationUpdateService
import co.tecno.sersoluciones.analityco.services.UpdateDBService
import co.tecno.sersoluciones.analityco.utilities.*
import co.tecno.sersoluciones.analityco.viewmodels.PersonalDailyViewModel
import com.android.volley.Request
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.squareup.picasso.Picasso
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern
import javax.inject.Inject
import kotlin.collections.ArrayList

/**
 * Created by Ser Soluciones SAS on 09/11/2018.
 * www.sersoluciones.com - contacto@sersoluciones.com
 */
class CheckReqsPersonalActivity : BaseActivity(), BroadcastListener, OnDateSetListener,
        OnVerifyReqListener, OnInspectReqListener {

    private var dialogLoad: AlertDialog? = null
    private var dialog: AlertDialog? = null
    private var personalCompanyInfoId = 0
    private var fillArray: BooleanArray = BooleanArray(1)
    private val mHandler = Handler()
    private var data: LinkedHashMap<Uri, String>? = null
    private var positionArray = 0
    private var idProject: String? = null
    private var idReqOk = -1
    private var requestBroadcastReceiver: RequestBroadcastReceiver? = null
    private var requirements: ArrayList<RequirementsList> = arrayListOf()
    private var requirementsInspect: ArrayList<RequirementsList> = arrayListOf()
    private var userLogin: User? = null
    private var scheduler: Scheduler? = null
    private var expiresUserTime = false
    private var handlerInteraction: Handler? = null
    private var isEnabledButtons = false
    private var fromDateStr: String? = null
    private var fromDate: Date? = null
    private var fromDatePickerDialog: DatePickerDialog? = null
    private var checkReq = false
    private var inspectReq = false
    private var registerIn = false
    private var registerOut = false
    private var typeARLModel: TypeARL? = null
    private var reqsToSend: Array<String?>? = null
    private var requirementReport: RequirementReport? = null
    private var mImageUri: Uri? = null
    private var urlModel: String? = null
    private var idReq = 0
    private var contractId: String? = null
    private var personalInfoId = 0
    private var personalJob: String? = null
    private var contractFinisDate: String? = null
    private var verifyRequirimentsAdapter: VerifyRequirimentsAdapter? = null
    private var inspectRequirimentsAdapter: InspectRequirimentsAdapter? = null
    private var document: String? = null
    private var nameOfPersonal: String? = null
    private var namePersonal: String? = null
    private var lastNamePersonal: String? = null
    private var isValidedCheck = false
    private var isValidedSurvey = false
    private var isValidedInspect = false
    private var scan = false
    private var showScheduleMessage = false
    private var showRequerimentsMessage = false
    private var showInspectMessage = true
    private var idSteps = 0
    private var posSurvey = 0
    private var user: User? = null
    private var claims: ArrayList<String> = ArrayList()
    private var companyInfoId: String? = null
    private var afd: AssetFileDescriptor? = null
    private var reportEvent = 0
    private var valideAge = true
    var mSqLiteDatabase: SQLiteDatabase? = null
    var mDbHelper: DBHelper? = null
    private var employerName: String? = null
    private var contractType: String? = null
    var reqJson : String? = null
    var registerEntry = 0L
    var registerOutLong = 0L
    var inputStatus = 0L
    var surveyDetails : Survey? = null
    var TypeARL : String? = null
    var isArl = false
    var isAfp = false
    var isEps = false
    var surveyOffline = false
    var surveyState = 0
    private var epsId = 0
    private var afpId = 0
    private var PersonGuidId : String? = null
    private var mValues: ArrayList<PersonalList>? = null
    var replace: String? = null
    var replacepersonalIds:String? = null
    private var companyAdminId: String = ""
    private var projectId: String? = null
    private var personalId: String? = null

    private var documentNumberSurvey: String? = null
    private var birthdaySurvey: String? = null

    private var mBottomSheetBehavior: BottomSheetBehavior<View>? = null
    private var updatePersonal: Boolean = false

    @Inject
    lateinit var viewModel: PersonalDailyViewModel

    lateinit var binding: ActivityCheckReqsPersonalBinding
    private lateinit var includeInternalLayout: ContentCheckReqsPersonalBinding

    override fun attachLayoutResource() {
//        super.setChildLayout(R.layout.activity_check_reqs_personal)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("SimpleDateFormat", "SetTextI18n", "SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        ApplicationContext.analitycoComponent.inject(this)
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(
                this, R.layout.activity_check_reqs_personal
        )
//        binding = ActivityCheckReqsPersonalBinding.inflate(layoutInflater, container, false)
        includeInternalLayout = binding.contentCkecksPersonal
//        binding.viewModel = viewModel
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        setSupportActionBar(binding.toolbar)
        requestBroadcastReceiver = RequestBroadcastReceiver(this)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        expiresUserTime = false
        handlerInteraction = Handler()
       /* val profile = preferences.profile*/
        val preferences = MyPreferences(this)
        val profile = preferences.profile
        user = Gson().fromJson(profile, User::class.java)
        isEnabledButtons = preferences.enabledErollmentButtons

        timeInteractionUser()
        viewModel.surveyFormInfo.observe(this, androidx.lifecycle.Observer {
            surveyDetails = it
            init()
        })
        enabledButtons()
        configBottomSheet()

        viewModel.personalRealTime.observe(this, androidx.lifecycle.Observer {
            it?.let {
                viewModel.sendMsgSignalR(it)
                viewModel.clearPersonal()
            }
        })

        viewModel.personalContractOffline.observe(this, androidx.lifecycle.Observer {
            it?.let {
                if (updatePersonal) replacePersonalInfo(it)
            }
        })
        viewModel.personEntry.observe(this, androidx.lifecycle.Observer {
            registerEntry = it
        })
        viewModel.personExit.observe(this, androidx.lifecycle.Observer {
            registerOutLong = it
        })
        viewModel.inputStatus.observe(this, androidx.lifecycle.Observer {
            inputStatus = it
            if (it == 1L && preferences.getVerifyAdnRegisterSettings(5)) {
                val date = SimpleDateFormat("HH:mm")
                        .format(registerEntry).toString()
                binding.register.text = "HOY SE REGISTRO INGRESO $date"
                binding.inButton.visibility = View.GONE
            } else if ((it == 0L && !preferences.getVerifyAdnRegisterSettings(5)) && !preferences.getVerifyAdnRegisterSettings(4)) {
                binding.inButton.visibility = View.VISIBLE
            }
            if (it == 2L && preferences.getVerifyAdnRegisterSettings(5)) binding.inButton.visibility = View.VISIBLE
        })




              // viewModel.updatePersonalByProject(projectId!!)








    }
    private fun enableComponents(enable: Boolean) {
        enabledButtons()
    }

    private val enabledSyncButton = Runnable {
        binding.syncButton.isEnabled = true
    }
    private fun LoadPersonalByid(personalCompanyInfoId: Int) {
        CRUDService.startRequest(
            this, Constantes.LIST_CONTRACT_PER_OFFLINE_URL_PCII+personalCompanyInfoId,
            Request.Method.GET, "", false
        )
        //viewModel.changeStatus(LoadingApiStatus.LOADING)
    }

    private fun init() {

        mDbHelper = DBHelper(this)
        mSqLiteDatabase = mDbHelper!!.readableDatabase

        val preferences = MyPreferences(this)
        val profile = preferences.profile
        scheduler = Scheduler()
        userLogin = Gson().fromJson(profile, User::class.java)
        scan = false
        requirements = ArrayList()
        requirementsInspect = ArrayList()
        var mLayoutManager = LinearLayoutManager(this)
        includeInternalLayout.recyclerRequirements.layoutManager = mLayoutManager

        val extras = intent.extras
        if (extras != null) {
            claims = extras.getStringArrayList(ARG_CLAIMS) as ArrayList<String>
            idProject = extras.getString(ARG_PROJECT_ID)
            //idSteps = extras.getInt(ARG_STEP_ID);
            // String service = extras.getString(ARG_SERVICE);
            personalCompanyInfoId = extras.getInt(ARG_PER_COMP_IF_ID)
            companyInfoId = extras.getString(ARG_COMPANY_INFO_ID)
            namePersonal = extras.getString(ARG_NAME)
            lastNamePersonal = extras.getString(ARG_LAST_NAME)
            nameOfPersonal = "$namePersonal $lastNamePersonal"
            document = extras.getString(ARG_DOC_NUMBER)
            personalInfoId = extras.getInt(ARG_PERSONAL_ID)
            contractId = extras.getString(ARG_CONTRACT_ID)
            personalJob = extras.getString(ARG_PERSONAL_JOB)
            contractFinisDate = extras.getString(ARG_CONTRACT_FINISH_DATE)
            employerName = extras.getString(ARG_EMPLOYER_CONTRACT)
            contractType = extras.getString(ARG_TYPE_CONTRACT)
            PersonGuidId = extras.getString(ARG_PERSON_GUID)
            //viewModel.configSurveyForm(personalInfoId.toLong(),"")

            if(contractType.equals("Contratista")){
                ValidateTypeContratista(contractId, employerName)
            }else{
                includeInternalLayout.descriptionType.text = contractType
                ValideteColorContract(contractType)
                if(contractType.equals("Otro")){
                    includeInternalLayout.showContractReview.visibility = View.GONE
                }
            }
            includeInternalLayout.EmployerContract.text = employerName
            if (user!!.claims.contains("enrollments.validaterequirements") || user!!.IsSuperUser) checkReq =
                    extras.getBoolean(ARG_CHECK_REQ)
            if (user!!.claims.contains("enrollments.inspectrequirements") || user!!.IsSuperUser) inspectReq =
                    extras.getBoolean(ARG_INSPECT_REQ)
            if (user!!.claims.contains("enrollments.verifyenterexit") || user!!.IsSuperUser) {
                registerIn = extras.getBoolean(ARG_REG_IN)
                registerOut = extras.getBoolean(ARG_REG_OUT)
            }
            scan = extras.getBoolean(ARG_OPT_SCAN)

            verifyRequirimentsAdapter = VerifyRequirimentsAdapter(this, requirements, this, personalInfoId, nameOfPersonal)
            includeInternalLayout.recyclerRequirements.itemAnimator = DefaultItemAnimator()
            val mDividerItemDecoration = DividerItemDecoration(
                    includeInternalLayout.recyclerRequirements.context,
                    mLayoutManager.orientation
            )
            //recyclerRequirements.addItemDecoration(mDividerItemDecoration);
            includeInternalLayout.recyclerRequirements.adapter = verifyRequirimentsAdapter
            mLayoutManager = LinearLayoutManager(this)
            includeInternalLayout.recyclerRequirementInspect.layoutManager = mLayoutManager
            inspectRequirimentsAdapter = InspectRequirimentsAdapter(this, requirementsInspect, this)
            includeInternalLayout.recyclerRequirementInspect.adapter = inspectRequirimentsAdapter
            binding.option1Button.backgroundTintList =
                    ContextCompat.getColorStateList(this, R.color.primary_dark)
            binding.option2Button.backgroundTintList =
                    ContextCompat.getColorStateList(this, R.color.primary_dark)
            binding.option3Button.backgroundTintList =
                    ContextCompat.getColorStateList(this, R.color.primary_dark)
            binding.option4Button.backgroundTintList =
                    ContextCompat.getColorStateList(this, R.color.primary_dark)

            binding.positiveButton2.setOnClickListener {
                positiveButton2()
            }

            if (checkReq) binding.option1Button.backgroundTintList =
                    ContextCompat.getColorStateList(this, R.color.colorAccent)
            if (inspectReq) binding.option2Button.backgroundTintList =
                    ContextCompat.getColorStateList(this, R.color.colorAccent)
            if (registerIn) binding.option3Button.backgroundTintList =
                    ContextCompat.getColorStateList(this, R.color.colorAccent)
            if (registerOut) binding.option4Button.backgroundTintList =
                    ContextCompat.getColorStateList(this, R.color.colorAccent)
            includeInternalLayout.textPersonalJob.text = personalJob
            requirementReport = RequirementReport()

            if (extras.getString(ARG_PHOTO) != null) {
                val url = Constantes.URL_IMAGES + extras.getString(ARG_PHOTO)
                Picasso.get().load(url)
                        .resize(0, 150)
                        .transform(CircleTransform())
                        .placeholder(R.drawable.profile_dummy)
                        .error(R.drawable.profile_dummy)
                        .into(includeInternalLayout.iconLogo)
            }
            if (nameOfPersonal != null && document != null) {
                val selection =
                        ("(" + DBHelper.CONTRACT_PERSON_TABLE_COLUMN_PROJECT_ID + " = ?) and (" + DBHelper.CONTRACT_PERSON_TABLE_COLUMN_DOC_NUM + " = ?) and ( "
                                + DBHelper.CONTRACT_PERSON_TABLE_COLUMN_CONTRACT_ID + " = ? )")
                val selectionArgs = arrayOf(idProject, document, contractId)
                contentResolver.query(
                        Constantes.CONTENT_CONTRACT_PER_OFFLINE_URI,
                        null,
                        selection,
                        selectionArgs,
                        null
                )?.use { cursorPersonal ->
                    validateVigency(cursorPersonal)
                    cursorPersonal.close()
                }
            }
        }
        viewModel.secRefereces.observe(this, androidx.lifecycle.Observer {
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
            val epsAdapter = ArrayAdapter(this, R.layout.list_item, epsList)
            val afpAdapter = ArrayAdapter(this, R.layout.list_item, afpList)
            logW("eps count ${epsList.count()} afp count ${afpList.count()} total ${it.count()}")
            binding.contentCkecksPersonal.epsAutoCompleteTextView.setAdapter(epsAdapter)
            binding.contentCkecksPersonal.afpAutoCompleteTextView.setAdapter(afpAdapter)
        })

        binding.contentCkecksPersonal.epsAutoCompleteTextView.apply {
            setOnItemClickListener { parent, view, position, id ->
                val item = parent.adapter.getItem(position) as SecurityReference.EPS
                epsId = item.item.Id
            }

        }

        binding.contentCkecksPersonal.afpAutoCompleteTextView.apply {
            setOnItemClickListener { parent, view, position, id ->
                val item = parent.adapter.getItem(position) as SecurityReference.AFP
                afpId = item.item.Id
            }
        }

        val tipoRiesgos = arrayOf("Seleccione", "V", "IV", "III", "II", "I")
        binding.contentCkecksPersonal.spinnerTypeRisk.adapter = CustomArrayAdapter(this, tipoRiesgos)
        binding.contentCkecksPersonal.spinnerTypeRisk.setSelection(1)
        TypeARL = binding.contentCkecksPersonal.spinnerTypeRisk.selectedItem.toString()

        includeInternalLayout.fabRemove.visibility = View.GONE
        includeInternalLayout.iconFile.setOnClickListener { v: View? -> startFaceDectectorActivity() }
        showProgress(false)
        requirementOffline()

        binding.syncButton.setOnClickListener {
            var animation: Animation =
                AnimationUtils.loadAnimation(this, R.anim.rotatesync)
            binding.syncButton.startAnimation(animation)
            enableComponents(false)
            UpdateDBService.startRequest(this, false)
            LoadPersonalByid(personalCompanyInfoId)


        }
        enableComponents(true)
        if (!verifyInDb()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                showAlertLoad()
            }
        }


    }

    private fun ValideteColorContract(contractType: String?) {
        when {
            contractType.equals("Funcionario") -> {
                includeInternalLayout.descriptionType.setTextColor(ContextCompat.getColor(applicationContext, R.color.funcionario))
                includeInternalLayout.bussinesIcon.visibility = View.GONE
                includeInternalLayout.editRequirement.visibility = View.GONE
            }
            contractType!!.equals("Administrativo") -> includeInternalLayout.descriptionType.setTextColor(ContextCompat.getColor(applicationContext, R.color.administrativo))
            contractType.equals("Asociado") -> includeInternalLayout.descriptionType.setTextColor(ContextCompat.getColor(applicationContext, R.color.asociado))
            contractType.equals("Proveedor") -> {
                includeInternalLayout.descriptionType.setTextColor(ContextCompat.getColor(applicationContext, R.color.proveedor))
                includeInternalLayout.bussinesIcon.visibility = View.GONE
                includeInternalLayout.editRequirement.visibility = View.GONE
            }
            contractType.equals("Visitante") -> {
                includeInternalLayout.descriptionType.setTextColor(ContextCompat.getColor(applicationContext, R.color.visitante))
                includeInternalLayout.bussinesIcon.visibility = View.GONE

                includeInternalLayout.editRequirement.visibility = View.GONE
            }
        }
    }
    private fun ValidateTypeContratista(contractId: String?, employerName: String?) {
        val Nombre = arrayOf(DBHelper.CONTRACT_TABLE_COLUMN_CONTRACT_NAME)
        val args = arrayOf(contractId)
        val cursor : Cursor = mSqLiteDatabase?.query(DBHelper.TABLE_NAME_CONTRACT, Nombre, "Id=?", args, null, null, null)!!
        if(cursor.moveToFirst()){
            val ContractorName = cursor.getString(0)
            if(employerName.equals(ContractorName)){
                includeInternalLayout.descriptionType.text = "Contratista"
                includeInternalLayout.descriptionType.setTextColor(ContextCompat.getColor(applicationContext, R.color.contratista))
            }else{
                includeInternalLayout.descriptionType.text = "Subcontratista"
                includeInternalLayout.descriptionType.setTextColor(ContextCompat.getColor(applicationContext, R.color.asociado))
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility", "SetTextI18n")
    private fun configBottomSheet() {

        mBottomSheetBehavior = BottomSheetBehavior.from<View>(binding.bottomSheet)
        mBottomSheetBehavior!!.state = BottomSheetBehavior.STATE_COLLAPSED

        mBottomSheetBehavior!!.addBottomSheetCallback(object :
                BottomSheetBehavior.BottomSheetCallback() {
            @SuppressLint("RestrictedApi")
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_COLLAPSED -> {
                        //mBottomSheetBehavior.setPeekHeight(0);
                        binding.shadow.visibility = View.GONE
                        binding.shadow2.visibility = View.GONE
                        binding.labelSliding.text = "Registros"
//                        binding.toolbar.setCollapsible(false)
                        binding.iconTabD.visibility = View.GONE
                        binding.iconTab.visibility = View.VISIBLE
                        binding.listPersonal.isNestedScrollingEnabled = true
                    }
                    BottomSheetBehavior.STATE_HIDDEN -> {
                    }
                    BottomSheetBehavior.STATE_EXPANDED -> {
                        binding.shadow.visibility = View.GONE
                        binding.shadow2.visibility = View.GONE
                        binding.iconTabD.visibility = View.VISIBLE
                        binding.iconTab.visibility = View.GONE
                        binding.labelSliding.text = ""
                        binding.listPersonal.isNestedScrollingEnabled = false
//                        binding.toolbar.setCollapsible(true)
                    }
                    BottomSheetBehavior.STATE_DRAGGING -> {
                        binding.shadow.visibility = View.VISIBLE
                        binding.shadow2.visibility = View.VISIBLE
                    }
                    BottomSheetBehavior.STATE_SETTLING -> {
                    }
                    BottomSheetBehavior.STATE_HALF_EXPANDED -> {
                    }
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {}
        })

        val adapter = PersonalDailyAdapter()
        binding.listPersonal.adapter = adapter
        binding.listPersonal.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN ->                         // Disallow NestedScrollView to intercept touch events.
                    v.parent.parent.requestDisallowInterceptTouchEvent(true)
                MotionEvent.ACTION_UP ->                         // Allow NestedScrollView to intercept touch events.
                    v.parent.parent.requestDisallowInterceptTouchEvent(false)
            }
            v.onTouchEvent(event)
            true
        }
        val smoothScroller: RecyclerView.SmoothScroller = object : LinearSmoothScroller(this) {
            override fun getVerticalSnapPreference(): Int {
                return SNAP_TO_START
            }
        }
        val mLayoutManager = LinearLayoutManager(this)
        val mDividerItemDecoration = DividerItemDecoration(
                binding.listPersonal.context,
                mLayoutManager.orientation
        )
        binding.listPersonal.addItemDecoration(mDividerItemDecoration)
        binding.listPersonal.layoutManager = mLayoutManager
        viewModel.personals.observe(this, androidx.lifecycle.Observer {
            it?.let {
                adapter.data = it
                if (it.isNotEmpty()) {
                    smoothScroller.targetPosition = 0
                    mLayoutManager.startSmoothScroll(smoothScroller)
                }
            }
        })

        viewModel.afIndicator.observe(this, androidx.lifecycle.Observer {
            binding.afIndicator.text = "AF = $it"
        })


        viewModel.updateList.observe(this, androidx.lifecycle.Observer {
            it?.let {
                if (it)
                    viewModel.updatePersonalList()
            }
        })
    }

    public override fun onResume() {
        super.onResume()
        val intentFilter = IntentFilter()
        intentFilter.addAction(Constantes.BROADCAST_GET_JSON)
        intentFilter.addAction(CrudIntentService.ACTION_REQUEST_FORM_DATA)
        intentFilter.addAction(CRUDService.ACTION_REQUEST_SAVE)
        intentFilter.addAction(CRUDService.ACTION_REQUEST_GET)
        LocalBroadcastManager.getInstance(this).registerReceiver(
                requestBroadcastReceiver!!,
                intentFilter
        )
        bindMyService()
    }

    public override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(requestBroadcastReceiver!!)
        //if (LocationUpdateService.isRunning())
        unbindService(myConnection)
    }

    override fun onStop() {
        super.onStop()
        handlerInteraction!!.removeCallbacks(closeActivity)
    }

    override fun onStart() {
        super.onStart()
        if (!expiresUserTime) timeInteractionUser() else finish()
    }

    private fun bindMyService() {
        val intent = Intent(applicationContext, LocationUpdateService::class.java)
        bindService(intent, myConnection, Context.BIND_AUTO_CREATE)
    }

    private var myService: LocationUpdateService? = null
    private val myConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(
                componentName: ComponentName,
                iBinder: IBinder
        ) {
            log("Servicio conectado")
            val b = iBinder as LocationUpdateService.MyBinder
            myService = b.service
        }

        override fun onServiceDisconnected(componentName: ComponentName) {
            log("Servicio desconectado")
            myService = null
        }
    }

    private val currentLocation: JSONObject?
        get() = myService?.getLastLocationJson()

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.enrollment_menu, menu)
        val menuItemSettings = menu.findItem(R.id.action_settings)

        if (claims.isNotEmpty() && claims.contains("enrollments.blockbuttons")) {
            menuItemSettings.isEnabled = false
            menuItemSettings.title = getString(R.string.botones_fijos)
            preferences.enabledErollmentButtons = false
            isEnabledButtons = false
        }

        if (isEnabledButtons) menuItemSettings.title =
                getString(R.string.fijar_botones) else menuItemSettings.title =
                getString(R.string.desfijar_botones)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.action_settings) {
            preferences.enabledErollmentButtons = !isEnabledButtons
            isEnabledButtons = !isEnabledButtons
            enabledButtons()
            if (isEnabledButtons) item.title = getString(R.string.fijar_botones) else item.title =
                    getString(R.string.desfijar_botones)
            return true
        } else if (id == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun startFaceDectectorActivity() {
        PhotoSer.ActivityBuilder()
                .setDetectFace(false)
                .setQuality(50)
                .setSaveGalery(false)
                .setCrop(false)
                .start(this)
    }

    @SuppressLint("SimpleDateFormat")
    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == 207) {
            surveyOffline = true
            surveyState = 0
            isValidedCheck = false
            isValidedSurvey = true
            //viewModel.loadPersonalContractOffline(personalInfoId.toLong())
            val create = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
            val CreateDate = create.format(Date())
            var requirement: RequirementsList
            val jsonObject = JSONObject(reqJson)
            val jsonObjectSurvey = jsonObject.getJSONArray("Surveys")
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
                requirement.State = 0
                requirement.Date = CreateDate
                requirement.IsValided = true
                requirements!!.add(requirement)
                logW("DocsRequerimentJSONSurvers: $requirement")
                requirements.removeAt(posSurvey)
                verifyRequirimentsAdapter!!.notifyItemRemoved(posSurvey)
                verifyRequirimentsAdapter!!.notifyDataSetChanged()
            }
            if(registerIn && registerOut){
                if(preferences.getVerifyAdnRegisterSettings(4)){
                    binding.requerimentIncomplete.visibility = View.GONE
                    binding.inButton.background = getDrawable(R.drawable.bt_register_in_blue)
                    binding.inButton.visibility = View.VISIBLE
                }
                if(preferences.getVerifyAdnRegisterSettings(5) && inputStatus == 1L){
                    binding.inButton.visibility = View.GONE
                }
                supportActionBar!!.setBackgroundDrawable(ColorDrawable(resources.getColor(R.color.primary)))
                binding.inOutMessage.visibility = View.VISIBLE
                binding.inOutMessage.setBackgroundColor(resources.getColor(R.color.primary))
                binding.inButton.background = getDrawable(R.drawable.bt_register_in_blue)
                binding.outButton.background = getDrawable(R.drawable.bt_register_out_blue)
                binding.inOutNegativeMessage.visibility = View.GONE
            }
        }
        if(resultCode == 208) {
            surveyOffline = true
            isValidedSurvey = false
            surveyState = 1
            val create = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
            val CreateDate = create.format(Date())
            var requirement: RequirementsList
            val jsonObject = JSONObject(reqJson)
            val jsonObjectSurvey = jsonObject.getJSONArray("Surveys")
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
                requirement.State = 1
                requirement.Date = CreateDate
                requirement.IsValided = true
                requirements!!.add(requirement)
                logW("DocsRequerimentJSONSurvers: $requirement")
                requirements.removeAt(posSurvey)
                verifyRequirimentsAdapter!!.notifyItemRemoved(posSurvey)
                verifyRequirimentsAdapter!!.notifyDataSetChanged()
            }
            if(registerIn && registerOut) {
                supportActionBar!!.setBackgroundDrawable(ColorDrawable(resources.getColor(R.color.orange)))
                if (preferences.getVerifyAdnRegisterSettings(4)) {
                    binding.requerimentIncomplete.visibility = View.GONE
                    binding.inButton.background = getDrawable(R.drawable.bt_register_in_blue)
                    binding.inButton.visibility = View.VISIBLE
                }
                if (preferences.getVerifyAdnRegisterSettings(5) && inputStatus == 1L) {
                    binding.inButton.visibility = View.GONE
                }
                binding.inOutMessage.visibility = View.VISIBLE
                binding.inOutMessage.setBackgroundColor(resources.getColor(R.color.primary))
                binding.inButton.background = getDrawable(R.drawable.bt_register_in_blue)
                binding.outButton.background = getDrawable(R.drawable.bt_register_out_blue)
                binding.inOutNegativeMessage.visibility = View.GONE
            }
            isValidedCheck = true
        }
        if(resultCode == 209) {
            surveyOffline = true
            surveyState = 2
            val create = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
            val CreateDate = create.format(Date())
            var requirement: RequirementsList
            val jsonObject = JSONObject(reqJson)
            val jsonObjectSurvey = jsonObject.getJSONArray("Surveys")
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
                requirement.State = 2
                requirement.Date = CreateDate
                requirement.IsValided = true
                requirements!!.add(requirement)
                logW("DocsRequerimentJSONSurvers: $requirement")
                requirements.removeAt(posSurvey)
                verifyRequirimentsAdapter!!.notifyItemRemoved(posSurvey)
                verifyRequirimentsAdapter!!.notifyDataSetChanged()
                supportActionBar!!.setBackgroundDrawable(
                        ColorDrawable(
                                resources.getColor(
                                        R.color.bar_undecoded
                                )
                        )
                )
                if (registerIn && registerOut) {
                    if (preferences.getVerifyAdnRegisterSettings(4)) {
                        binding.requerimentIncomplete.visibility = View.VISIBLE
                        binding.inOutNegativeMessage.visibility = View.VISIBLE
                        binding.inOutNegativeMessage.text =
                                getString(R.string.negative_title_1)
                        binding.inButton.visibility = View.GONE
                        binding.inOutMessage.visibility = View.VISIBLE
                        binding.inOutMessage.setBackgroundColor(resources.getColor(R.color.bar_undecoded))

                    } else {
                        binding.requerimentIncomplete.visibility = View.GONE
                        binding.inOutMessage.visibility = View.VISIBLE
                        binding.inOutMessage.setBackgroundColor(resources.getColor(R.color.bar_undecoded))
                        binding.inOutNegativeMessage.visibility = View.VISIBLE
                        binding.inOutNegativeMessage.text =
                                getString(R.string.negative_title_1)
                        binding.inButton.background = getDrawable(R.drawable.bt_register_in_red)
                    }
                    if (preferences.getVerifyAdnRegisterSettings(5) && inputStatus == 1L) {
                        binding.inButton.visibility = View.GONE
                    }
                    binding.outButton.background = getDrawable(R.drawable.bt_register_out_red)
                }
            }
        }
        if (requestCode == PhotoSer.SER_IMAGE_ACTIVITY_REQUEST_CODE) {
            when (resultCode) {
                Activity.RESULT_OK -> {
                    val imageUri =
                            Uri.parse(data!!.getStringExtra(FaceTrackerActivity.URI_IMAGE_KEY))
                    includeInternalLayout.iconFile.setImageURI(imageUri)
                    mImageUri = imageUri
                    includeInternalLayout.fabRemove.visibility = View.VISIBLE
                }
                else -> {
                }
            }
        }
    }

    private fun requirementOffline() {

//        if (mListener != null) {
//            mListener.onUsrNotInProject(document);
//        }
        if (reqsToSend == null || reqsToSend!!.isEmpty()) return
        reqJson = reqsToSend!![0]
        val maxJson = reqsToSend!![1]
        val minJson = reqsToSend!![2]
        val dayJson = reqsToSend!![3]
        val startDateJson = reqsToSend!![4]
        val finishDateJson = reqsToSend!![5]
        try {
            requirementsObjectOffline(reqJson)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        schedulerOffline(maxJson, minJson, dayJson, startDateJson, finishDateJson)
    }

    fun fromDateDialog(view: View) {
        fromDatePickerDialog!!.datePicker.tag = R.id.fromDateBtn
        fromDatePickerDialog!!.show()
    }

    fun removeImage(view: View) {
        mImageUri = null
        includeInternalLayout.iconFile.setImageResource(R.drawable.ic_note_text)
        includeInternalLayout.fabRemove.visibility = View.GONE
    }

    fun cancelButton(view: View) {
        includeInternalLayout.editRequirement.visibility = View.GONE
        binding.contentCkecksPersonal.cardValidity.visibility = View.VISIBLE
        binding.contentCkecksPersonal.employerDescription.visibility = View.VISIBLE
        binding.inOutMessage.visibility = View.VISIBLE
        binding.contentCkecksPersonal.recyclerRequirements.visibility = View.VISIBLE
        binding.contentCkecksPersonal.recyclerRequirementInspect.visibility = View.VISIBLE
        binding.buttonsSelected.visibility = View.VISIBLE
        binding.contentCkecksPersonal.documentAge.visibility = View.VISIBLE
        binding.contentCkecksPersonal.generalInfo.visibility = View.VISIBLE
    }

    fun positiveButton(view: View) {
        updateImage()
    }

    @SuppressLint("SimpleDateFormat")
    private fun updateImage() {
        // Log.e("path", "logo:" + mPhotoPath);
        var cancel = true
        if (fromDateStr != null) {
            cancel = false
            includeInternalLayout.tvFromDateError.error = "fecha obligatoria"
            val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
            fromDateStr = format.format(fromDate!!)
        }
        if (mImageUri != null && mImageUri.toString().isNotEmpty() && !cancel &&(!binding.contentCkecksPersonal.epsAutoCompleteTextView.text.isNullOrEmpty() ||  !binding.contentCkecksPersonal.afpAutoCompleteTextView.text.isNullOrEmpty() || !TypeARL.isNullOrEmpty())) {
            if(isEps){
                try {
                    val json = JSONObject()
                    json.put("op", "replace")
                    json.put("path", "/EpsId")
                    json.put("value", epsId)
                    val jsonArray = JSONArray()
                    jsonArray.put(json)
                    CrudIntentService.startRequestPatch(this, Constantes.PERSON_URL + PersonGuidId, jsonArray.toString())
                }catch (e: JSONException){
                    e.printStackTrace()
                }
            }
            if(isAfp) {
                try {
                    val json = JSONObject()
                    json.put("op", "replace")
                    json.put("path", "/AfpId")
                    json.put("value", afpId)
                    val jsonArray = JSONArray()
                    jsonArray.put(json)
                    CrudIntentService.startRequestPatch(this, Constantes.PERSON_URL + PersonGuidId, jsonArray.toString())
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
            if(isArl){
                try{
                    val json = JSONObject()
                    json.put("TypeARL",TypeARL)
                    json.put("IsValided",false)
                    json.put("RequiredDate",false)
                    CrudIntentService.startRequestCRUD(this,
                        "api/EmployerPersonalRequirement/$idReq",Request.Method.PUT,json.toString(),"",false)
                }catch (e: JSONException){
                    e.printStackTrace()
                }
            }
            val params = HashMap<String, String>()
            params["file"] = mImageUri.toString()
            val url = "api/$urlModel/UpdateFile/$idReq"
            CrudIntentService.startActionFormData(
                    this, url,
                    Request.Method.PUT, params
            )
            val requeriment = RequerimentValid(fromDateStr!!)
            val gson = Gson()
            val json = gson.toJson(requeriment)
            CrudIntentService.startRequestCRUD(
                    this,
                    "api/$urlModel/$idReq", Request.Method.PUT, json, "", false
            )
            mImageUri = null
            showProgress(true)
        } else {
            var message : String? = null
            when {
                mImageUri == null->{
                    message = "Es necesario seleccionar una imagen"
                }
                binding.contentCkecksPersonal.afpAutoCompleteTextView.text.isNullOrEmpty()->{
                    message = "Es necesario seleccionar una AFP"
                }
                binding.contentCkecksPersonal.epsAutoCompleteTextView.text.isNullOrEmpty()->{
                    message = "Es necesario seleccionar una AFP"
                }
            }
            val builder = AlertDialog.Builder(this)
                    .setMessage(message)
            builder.create().show()
        }
    }

    private fun requirementsObjectOK() {
        requirements!!.removeAll(requirementsInspect!!)
        verifyRequirimentsAdapter!!.update()
        inspectRequirimentsAdapter!!.update()
    }

    @SuppressLint("SetTextI18n", "SimpleDateFormat")
    @Throws(JSONException::class)
    private fun requirementsObjectOffline(ReqJson: String?) {
        if (ReqJson == null || ReqJson == "{}") return

        supportActionBar!!.setBackgroundDrawable(ColorDrawable(resources.getColor(R.color.primary)))
        requirements!!.clear()
        requirementsInspect!!.clear()
        isValidedCheck = false
        isValidedInspect = false
        val jsonObject: JSONObject
        val jsonObjectSs: JSONArray
        var jsonObjectReq: JSONArray
        val jsonObjectTypeARL: JSONObject
        val jsonObjectSurvey: JSONArray
        var requirement: RequirementsList
        typeARLModel = null
        jsonObject = JSONObject(ReqJson)
        if (checkReq) {
            jsonObjectTypeARL = jsonObject.getJSONObject("TypeARL")
            typeARLModel = Gson().fromJson(
                    jsonObjectTypeARL.toString(),
                    object : TypeToken<TypeARL?>() {}.type
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
                requirements!!.add(requirement)
                if (!requirement.IsValided && !isValidedCheck) isValidedCheck = true
                if (!requirement.IsValided) {
                    showRequerimentsMessage = true
                }
            }
            jsonObjectReq = jsonObject.getJSONArray("Certification")
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
                logW("DocsRequerimentJSONCertification: $requirement")
                if (!requirement.IsValided && !isValidedCheck) isValidedCheck = true
                if (!requirement.IsValided) {
                    showRequerimentsMessage = true
                }
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
                if(!surveyOffline){
                    requirement.State = surveyDetails?.responseState?.toLong() ?:0
                    requirement.IsActive = surveyDetails?.isActive ?: false
                    requirement.Date = surveyDetails?.createDate ?: ""

                    if(surveyDetails?.createDate != null && surveyDetails?.responseState != null ){
                        var mDate = surveyDetails?.createDate
                        @SuppressLint("SimpleDateFormat") val format =
                                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                        val date = format.parse(mDate)
                        @SuppressLint("SimpleDateFormat") val dateFormat = SimpleDateFormat("yyyy MMM dd")
                        val dateSurvey = dateFormat.format(date)
                        val create = SimpleDateFormat("yyyy MMM dd")
                        val CreateDate = create.format(Date())
                        requirement.IsValided = dateSurvey.equals(CreateDate) && surveyDetails?.responseState != 2
                    }
                }else {
                    requirement.State = surveyState.toLong()
                    requirement.Date = surveyDetails?.createDate ?: ""
                    requirement.IsValided = surveyState != 2
                    showRequerimentsMessage = false
                }
                requirements!!.add(requirement)
                logW("DocsRequerimentJSONSurvers: ${requirement.IsValided}")

                if (!requirement.IsValided && !isValidedCheck) isValidedCheck = true
                if (!requirement.IsValided) {
                    showRequerimentsMessage = true
                }
                isValidedSurvey = false
                if (requirement.IsValided && requirement?.State != null && requirement?.State!! == 1L) {
                    supportActionBar!!.setBackgroundDrawable(ColorDrawable(resources.getColor(R.color.orange)))
                    isValidedSurvey = true
                    isValidedCheck = true
                }
            }
            if (isValidedCheck && !isValidedSurvey) supportActionBar!!.setBackgroundDrawable(
                    ColorDrawable(
                            resources.getColor(
                                    R.color.bar_undecoded
                            )
                    )
            )
            //verifyRequirimentsAdapter!!.update()
        }
        if (inspectReq) {
            jsonObjectReq = jsonObject.getJSONArray("Entry")
            for (j in 0 until jsonObjectReq.length()) {
                requirement = Gson().fromJson(
                        jsonObjectReq.getJSONObject(j).toString(),
                        object : TypeToken<RequirementsList?>() {}.type
                )
                requirement.IsEntry = true
                if (requirement.Attr == "Temperatura") changeMainButtonsState(false)
                requirementsInspect.add(requirement)
                if (!requirement.IsValided && !isValidedInspect) isValidedInspect = true
                if (!requirement.IsValided) {
                    showInspectMessage = true
                }
            }
            if (isValidedInspect) supportActionBar!!.setBackgroundDrawable(
                    ColorDrawable(
                            resources.getColor(
                                    R.color.bar_undecoded
                            )
                    )
            )
            if (requirementsInspect.isEmpty()) showInspectMessage = false
        }
        val jsonArrayBio = jsonObject.getJSONArray("Biometric")
        val jsonObjectAge = jsonArrayBio.getJSONObject(0)
        includeInternalLayout.textAgeRange.text = jsonObjectAge.getString("MinValue") + " ≤ " +
                "Edad" + " < " + jsonObjectAge.getString("MaxValue")
        if (jsonObjectAge.getInt("MinValue") <= jsonObjectAge.getInt("Value") &&
                jsonObjectAge.getInt("MaxValue") > jsonObjectAge.getInt("Value")
        ) {
            includeInternalLayout.checkAgeRange.setImageResource(R.drawable.ic_checkmark)
            valideAge = true
        } else {
            includeInternalLayout.checkAgeRange.setImageResource(R.drawable.ic_close_arl)
            showRequerimentsMessage = true
            valideAge = false
        }

        inspectRequirimentsAdapter!!.update()
        requirements.sortByDescending { requirementsList -> requirementsList.IsValided == false || requirementsList.IsValided == null }

    }

    @SuppressLint("SetTextI18n", "SimpleDateFormat")
    private fun schedulerOffline(
            maxJson: String?, minJson: String?, dayJson: String?, startDateJson: String?,
            finishDateJson: String?
    ) {
        includeInternalLayout.cardViewEntradaSalida.visibility = View.VISIBLE
        includeInternalLayout.cardViewEntradaSalida.visibility = View.VISIBLE
        includeInternalLayout.textAgeRange.visibility = View.VISIBLE
        includeInternalLayout.checkAgeRange.visibility = View.VISIBLE
        includeInternalLayout.bottomBar.visibility = View.VISIBLE
        showMenssageAllow()
        // getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.primary)));
        includeInternalLayout.textWorkDays.setTextColor(resources.getColor(R.color.dark_gray))
        includeInternalLayout.icWorkDays.backgroundTintList =
                resources.getColorStateList(R.color.dark_gray)
        includeInternalLayout.textHorario.setTextColor(resources.getColor(R.color.dark_gray))
        includeInternalLayout.icClock.backgroundTintList =
                resources.getColorStateList(R.color.dark_gray)
        val rightNow = Calendar.getInstance()
        var dayWek = rightNow[Calendar.DAY_OF_WEEK]
        val localDateFormat = SimpleDateFormat("HH:mm")
        val time = localDateFormat.format(Date())

        var dayOk = false
        if (dayJson != null) {
            val daysWeek = dayJson.split(Pattern.quote(",").toRegex()).toTypedArray()
            dayWek = if (dayWek == 1) 0 else dayWek - 1

            for (i in daysWeek.indices) {
                if (i == 0 && daysWeek[i] == "{$dayWek" || i == daysWeek.size - 1 && daysWeek[i] == "$dayWek}" || daysWeek[i] == "" + dayWek) {
                    dayOk = true
                    break
                }
            }
        }
        if ((registerIn || registerOut || inspectReq) && !checkReq) {
            includeInternalLayout.textAgeRange.visibility = View.INVISIBLE
            includeInternalLayout.checkAgeRange.visibility = View.INVISIBLE
        }
        if (!checkReq) {
            includeInternalLayout.recyclerRequirements.visibility = View.GONE
        } else includeInternalLayout.recyclerRequirements.visibility = View.VISIBLE
        if (registerIn || registerOut) {
            if (maxJson != null && minJson != null) {
                try {
                    val maxHour = localDateFormat.parse(maxJson)
                    val minHour = localDateFormat.parse(minJson)
                    includeInternalLayout.textWorkDays.text = changeDaysWeek(dayJson ?: "")
                    val mnHour = Calendar.getInstance()
                    val mxHour = Calendar.getInstance()
                    mnHour.time = minHour
                    mxHour.time = maxHour
                    val mnHourStr =
                            mnHour[Calendar.HOUR_OF_DAY].toString() + ":" + mnHour[Calendar.MINUTE]
                    val mxHourStr =
                            mxHour[Calendar.HOUR_OF_DAY].toString() + ":" + mxHour[Calendar.MINUTE]
                    includeInternalLayout.textHorario.text =
                            "Horario: " + localDateFormat.format(minHour) + " - " + localDateFormat.format(
                                    maxHour
                            )
                    val now = localDateFormat.parse(time)
                    logW("current time $time")
                    if (dayOk) {
                        if (mnHourStr == "0:0" && mxHourStr == "23:59"
                                || now.after(minHour) && now.before(maxHour) && registerIn
                                || registerOut && mxHourStr == "23:59" && !registerIn
                        ) {
                            scheduler!!.Allow = true
                            scheduler!!.Late = false
                            scheduler!!.Desc = "OK"
                            showScheduleMessage = false
                            logW("caso " + scheduler!!.Desc)
                            if (!isValidedCheck && !isValidedInspect) supportActionBar!!.setBackgroundDrawable(
                                    ColorDrawable(resources.getColor(R.color.primary))
                            )
                            validateContractDate()
                        } else if (now.after(minHour) && now.before(maxHour) && registerOut) {
                            scheduler!!.Allow = true
                            scheduler!!.Exit = true
                            scheduler!!.Desc = "OK"
                            showScheduleMessage = false
                            if (!isValidedCheck && !isValidedInspect) supportActionBar!!.setBackgroundDrawable(
                                    ColorDrawable(resources.getColor(R.color.primary))
                            )
                            validateContractDate()
                            logW("caso " + "OK2")
                        } else if (now.before(minHour) || now.after(maxHour) && registerIn) {
                            scheduler!!.Allow = false
                            scheduler!!.Late = true
                            showScheduleMessage = true
                            scheduler!!.Desc = "Supero horario de Entrada"
                            logW("caso" + scheduler!!.Desc)
                            validateContractDate()
                            redItemsHorario()
                        } else if (now.after(minHour) && now.after(maxHour) && registerOut) {
                            scheduler!!.Allow = false
                            scheduler!!.Exit = true
                            showScheduleMessage = true
                            scheduler!!.Desc = "Supero horario de salida"
                            logW("caso" + scheduler!!.Desc)
                            validateContractDate()
                            redItemsHorario()
                        }
                    } else if ((now.before(minHour) || now.after(maxHour)) && registerIn) {
                        scheduler!!.Allow = false
                        scheduler!!.Late = true
                        showScheduleMessage = true
                        scheduler!!.Desc =
                                "Dia de ingreso a la obra no permitido y Supero horario de Entrada"
                        logW("caso " + scheduler!!.Desc)
                        validateContractDate()
                        redItemsHorario()
                        redItemsDaysWeek()
                    } else if (now.after(maxHour) && registerOut) {
                        scheduler!!.Allow = false
                        scheduler!!.Exit = true
                        showScheduleMessage = true
                        scheduler!!.Desc =
                                "Dia de ingreso a la obra no permitido y Supero horario de salida"
                        logW("caso " + scheduler!!.Desc)
                        validateContractDate()
                        redItemsHorario()
                        redItemsDaysWeek()
                    } else {
                        scheduler!!.Desc = "Dia de ingreso a la obra no permitido"
                        logW("caso " + scheduler!!.Desc)
                        showScheduleMessage = true
                        scheduler!!.Allow = false
                        validateContractDate()
                        redItemsDaysWeek()
                    }
                    showMenssageAllow()
                } catch (e: ParseException) {
                    e.printStackTrace()
                }
                requirementReport!!.Schedule = scheduler

            } else {

                includeInternalLayout.cardViewEntradaSalida.visibility = View.GONE
            }
        } else {

            includeInternalLayout.cardViewEntradaSalida.visibility = View.GONE
        }
        if (startDateJson != null && finishDateJson != null) {
            val contractValidity = Scheduler()
            @SuppressLint("SimpleDateFormat") val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
            try {
                val startDate = format.parse(startDateJson)
                val finishDate = format.parse(finishDateJson)
                if (rightNow.time.after(startDate)) {
                    if (rightNow.time.before(finishDate)) {
                        contractValidity.Allow = true
                        contractValidity.Desc = "OK"
                    } else {
                        contractValidity.Allow = false
                        contractValidity.Desc = "Contrato vencido"
                    }
                } else {
                    contractValidity.Allow = false
                    contractValidity.Desc = "Contrato sin iniciar"
                }
            } catch (e: ParseException) {
                e.printStackTrace()
            }
            if (!contractValidity.Allow) {
                val builder = AlertDialog.Builder(this)
                        .setTitle("Vigencia del contrato")
                        .setMessage(contractValidity.Desc)
                builder.create().show()
            }
        }
    }

    override fun onDateSet(datePicker: DatePicker, year: Int, month: Int, dayOfMonth: Int) {
        val code = datePicker.tag as Int
        //logE("CODE: " + code);
        val calendar = Calendar.getInstance()
        calendar[year, month] = dayOfMonth
        val myFormat = "dd/MMM/yyyy"
        val sdf = SimpleDateFormat(myFormat, Locale("es", "ES"))
        when (code) {
            R.id.fromDateBtn -> {
                fromDateStr = sdf.format(calendar.time)
                includeInternalLayout.fromDateBtn.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        0,
                        0
                )
                includeInternalLayout.fromDateBtn.text = fromDateStr
                fromDate = calendar.time
                includeInternalLayout.fromDateBtn.error = null
                includeInternalLayout.tvFromDateError.visibility = View.GONE
                includeInternalLayout.tvFromDateError.error = null
            }
        }
    }

    internal inner class RequerimentValid(var Date: String) {
        var IsValided = true
    }

    private fun changeDaysWeek(daysWeek: String?): String {
        var daysWeek = daysWeek
        daysWeek = daysWeek!!.replace("{", "")
        daysWeek = daysWeek.replace("}", "")
        daysWeek = dltRepWords(daysWeek)
        daysWeek = daysWeek.replace("1", "Lun")
        daysWeek = daysWeek.replace("2", "Mar")
        daysWeek = daysWeek.replace("3", "Mie")
        daysWeek = daysWeek.replace("4", "jue")
        daysWeek = daysWeek.replace("5", "Vie")
        daysWeek = daysWeek.replace("6", "Sab")
        daysWeek = daysWeek.replace("0", "Dom")
        return daysWeek
    }

    private fun dltRepWords(s: String): String {
        val newString =
                LinkedHashSet(Arrays.asList(*s.split(",".toRegex()).toTypedArray())).toString()
                        .replace("(^\\[|\\]$)".toRegex(), "").replace(", ", ",")
        return sortString(newString)
    }

    private fun sortString(s: String): String {
        val stringArr = s.split(",".toRegex()).toTypedArray()
        Arrays.sort(stringArr)
        val buffer = StringBuilder()
        for (each in stringArr) buffer.append(",").append(each)
        return buffer.deleteCharAt(0).toString()
    }

    private fun redItemsHorario() {
//        bottomBar.setVisibility(View.GONE);
        includeInternalLayout.textHorario.setTextColor(resources.getColor(R.color.bar_undecoded))
        includeInternalLayout.icClock.backgroundTintList =
                resources.getColorStateList(R.color.bar_undecoded)
        supportActionBar!!.setBackgroundDrawable(ColorDrawable(resources.getColor(R.color.bar_undecoded)))
    }

    private fun redItemsDaysWeek() {
//        bottomBar.setVisibility(View.GONE);
        includeInternalLayout.textWorkDays.setTextColor(resources.getColor(R.color.bar_undecoded))
        includeInternalLayout.icWorkDays.backgroundTintList =
                resources.getColorStateList(R.color.bar_undecoded)
        supportActionBar!!.setBackgroundDrawable(ColorDrawable(resources.getColor(R.color.bar_undecoded)))
    }

    @SuppressLint("SimpleDateFormat")
    private fun allowMessageButtons() {
        val format = SimpleDateFormat("h:mm a")
        val hour = format.format(Date())
        binding.inButton.setOnClickListener { v: View? ->
            scheduler!!.Allow = !showScheduleMessage
            reportEvent = 1
            viewModel.speak("Entrada $hour")
            positiveButton2()
        }
        binding.outButton.setOnClickListener { v: View? ->
            reportEvent = 2
            scheduler!!.Allow = !showScheduleMessage
            viewModel.speak("Salida $hour")
            positiveButton2()
        }
        binding.registerButton.setOnClickListener { v: View? ->
            reportEvent = 0
            scheduler!!.Allow = true
            if (checkReq && showRequerimentsMessage) scheduler!!.Allow =
                    false else if (inspectReq && showInspectMessage) scheduler!!.Allow = false
            viewModel.speak(getString(R.string.speak_registrer))
            positiveButton2()
        }
        binding.denyIncome.setOnClickListener { v: View? ->
            scheduler!!.Allow = false
            supportActionBar!!.title = "ENROLAR"
            binding.denyIncome.isEnabled = false
            if (registerOut) positiveButton2() else onCancelUserInfo()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun showMenssageAllow() {
        var isOk = true
        binding.inOutMessage.visibility = View.GONE
        binding.registerMessage.visibility = View.GONE
        binding.registerNegativeTitle.visibility = View.GONE
        binding.inOutNegativeMessage.visibility = View.GONE
        binding.outButton.visibility = View.VISIBLE
        if ((registerIn || registerOut) && (showScheduleMessage || showRequerimentsMessage && checkReq || showInspectMessage && inspectReq)) {
            binding.inOutMessage.visibility = View.VISIBLE
            if (!registerIn) binding.inButton.visibility = View.GONE
            if (!registerOut) binding.outButton.visibility = View.GONE
            binding.inOutMessage.setBackgroundColor(resources.getColor(R.color.bar_undecoded))
            binding.inOutNegativeMessage.visibility = View.VISIBLE
            if (!showScheduleMessage) binding.inOutNegativeMessage.text =
                    getString(R.string.negative_title_1) else binding.inOutNegativeMessage.text =
                    getString(R.string.horario_no_permitido)
            if(registerIn){
                if(registerEntry == 0L || !preferences.getVerifyAdnRegisterSettings(5) || inputStatus == 2L || inputStatus == 0L){
                    binding.inButton.visibility = View.VISIBLE
                    binding.inButton.background = getDrawable(R.drawable.bt_register_in_red)
                }else if((registerEntry != 0L && preferences.getVerifyAdnRegisterSettings(5)) ) {
                    binding.inButton.visibility = View.GONE
                }
                if(preferences.getVerifyAdnRegisterSettings(4) && (showRequerimentsMessage && checkReq || showInspectMessage && inspectReq)){
                    binding.requerimentIncomplete.visibility = View.VISIBLE
                    binding.inButton.visibility = View.GONE
                }else {
                    binding.requerimentIncomplete.visibility = View.GONE
                }
            }
            binding.outButton.background = getDrawable(R.drawable.bt_register_out_red)
            isOk = false
        } else if (showRequerimentsMessage && checkReq || showInspectMessage && inspectReq) {
            binding.registerMessage.visibility = View.VISIBLE
            binding.registerMessage.setBackgroundColor(resources.getColor(R.color.bar_undecoded))
            binding.registerNegativeTitle.visibility = View.VISIBLE
            if(preferences.getVerifyAdnRegisterSettings(4)){
                binding.requerimentIncomplete.visibility = View.VISIBLE
            }
            isOk = false
        } else if ((!showRequerimentsMessage && checkReq || !showInspectMessage && inspectReq) && !(registerIn || registerOut)) {
            binding.registerMessage.visibility = View.VISIBLE
            binding.registerMessage.setBackgroundColor(resources.getColor(R.color.primary))
        } else if (registerIn || registerOut) {
            binding.inOutMessage.visibility = View.VISIBLE
            if (!registerIn){
                binding.inButton.visibility = View.GONE
                binding.register.visibility = View.GONE
            }
            if (!registerOut) binding.outButton.visibility = View.GONE
            binding.inOutMessage.setBackgroundColor(resources.getColor(R.color.primary))
            if(registerIn){
                if(inputStatus == 0L || !preferences.getVerifyAdnRegisterSettings(5) || inputStatus == 2L ) {
                    binding.inButton.visibility = View.VISIBLE
                    binding.inButton.background = getDrawable(R.drawable.bt_register_in_blue)
                }else {
                    binding.inButton.visibility = View.GONE
                }
                binding.requerimentIncomplete.visibility = View.GONE
                binding.outButton.background = getDrawable(R.drawable.bt_register_out_blue)
            }

        }
        configurations(isOk)
        allowMessageButtons()
    }

    private fun validateContractDate() {
        try {
            @SuppressLint("SimpleDateFormat") val dateFormat =
                    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
            @SuppressLint("SimpleDateFormat") val dateFor = SimpleDateFormat("dd MMM yy")
            val date = dateFormat.parse(contractFinisDate!!)
            includeInternalLayout.contractDate.text = dateFor.format(date!!)
            val msDiff = Calendar.getInstance().timeInMillis - date.time
            val daysDiff = TimeUnit.MILLISECONDS.toDays(msDiff) * -1
            if (daysDiff <= 10) {
                supportActionBar!!.setBackgroundDrawable(ColorDrawable(resources.getColor(R.color.orange)))
                includeInternalLayout.contractDate.setTextColor(resources.getColor(R.color.orange))
                includeInternalLayout.icDate.backgroundTintList =
                        resources.getColorStateList(R.color.orange)
            }
        } catch (e: ParseException) {
            e.printStackTrace()
        }
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    private fun showProgress(show: Boolean) {
        val shortAnimTime = resources.getInteger(android.R.integer.config_shortAnimTime)
        binding.mMainFormView.visibility = if (show) View.GONE else View.VISIBLE
        binding.mMainFormView.animate().setDuration(shortAnimTime.toLong()).alpha(
                if (show) 0f else 1f
        ).setListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                binding.mMainFormView.visibility = if (show) View.GONE else View.VISIBLE
            }
        })
        binding.mProgressView.visibility = if (show) View.VISIBLE else View.GONE
        binding.mProgressView.animate().setDuration(shortAnimTime.toLong()).alpha(
                if (show) 1f else 0f
        ).setListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                binding.mProgressView.visibility = if (show) View.VISIBLE else View.GONE
            }
        })
    }

    override fun onBackPressed() {
        val state: Int = mBottomSheetBehavior!!.state
        if (state == BottomSheetBehavior.STATE_COLLAPSED || state == BottomSheetBehavior.STATE_HIDDEN) {
            val intent = Intent()
            intent.putExtra(ARG_CHECK_REQ, checkReq)
            intent.putExtra(ARG_INSPECT_REQ, inspectReq)
            intent.putExtra(ARG_REG_IN, registerIn)
            intent.putExtra(ARG_REG_OUT, registerOut)
            setResult(EnrollmentActivity.REQUEST_WRITE, intent)
            finish()
        } else {
            mBottomSheetBehavior!!.state = BottomSheetBehavior.STATE_COLLAPSED
        }
    }

    private fun onCancelUserInfo() {
        val intent = Intent()
        intent.putExtra(ARG_CHECK_REQ, checkReq)
        intent.putExtra(ARG_INSPECT_REQ, inspectReq)
        intent.putExtra(ARG_REG_IN, registerIn)
        intent.putExtra(ARG_REG_OUT, registerOut)
        if (scan) setResult(EnrollmentActivity.REQUEST_SCAN, intent) else setResult(
                EnrollmentActivity.REQUEST_WRITE,
                intent
        )
        finish()
        //        nameReq = null;
//        documentReq = null;
//        photoReq = null;
//        getSupportActionBar().setTitle("Enrolar");
//        getSupportActionBar().setSubtitle(" ");
//        replaceFragment(ScanPersonalFragmnet.newInstance());
    }

    fun onClickOption(v: View) {
        when (v.id) {
            R.id.option1_button -> if (checkReq) {
                checkReq = false
                v.backgroundTintList = ContextCompat.getColorStateList(this, R.color.primary_dark)
            } else {
                viewModel.speak(getString(R.string.speak_verificar))
                checkReq = true
                v.backgroundTintList = ContextCompat.getColorStateList(this, R.color.colorAccent)
            }
            R.id.option2_button -> if (inspectReq) {
                inspectReq = false
                v.backgroundTintList = ContextCompat.getColorStateList(this, R.color.primary_dark)
            } else {
                viewModel.speak(getString(R.string.speak_inspeccionar))
                inspectReq = true
                v.backgroundTintList = ContextCompat.getColorStateList(this, R.color.colorAccent)
            }
            R.id.option3_button -> if (registerIn) {
                registerIn = false
                v.backgroundTintList = ContextCompat.getColorStateList(this, R.color.primary_dark)
            } else {
                viewModel.speak(getString(R.string.speak_in))
                registerIn = true
                v.backgroundTintList = ContextCompat.getColorStateList(this, R.color.colorAccent)
            }
            R.id.option4_button -> if (registerOut) {
                registerOut = false
                v.backgroundTintList = ContextCompat.getColorStateList(this, R.color.primary_dark)
            } else {
                viewModel.speak(getString(R.string.speak_out))
                registerOut = true
                v.backgroundTintList = ContextCompat.getColorStateList(this, R.color.colorAccent)
            }
        }
        viewModel.surveyFormInfo.observe(this, androidx.lifecycle.Observer {
            surveyDetails = it

        })
        if (!checkReq && !inspectReq && !registerIn && !registerOut) {
            val builder = AlertDialog.Builder(this)
                    .setTitle("Sin Selección")
                    .setCancelable(false)
                    .setPositiveButton("OK") { dialog, which ->
                        viewModel.speak(getString(R.string.speak_verificar))
                        checkReq = true
                        permissions()
                        val selection =
                                ("(" + DBHelper.CONTRACT_PERSON_TABLE_COLUMN_PROJECT_ID + " = ?) and (" + DBHelper.CONTRACT_PERSON_TABLE_COLUMN_DOC_NUM + " = ?) and ( "
                                        + DBHelper.CONTRACT_PERSON_TABLE_COLUMN_CONTRACT_ID + " = ? )")
                        val selectionArgs = arrayOf(idProject, document, contractId)
                        contentResolver.query(
                                Constantes.CONTENT_CONTRACT_PER_OFFLINE_URI,
                                null,
                                selection,
                                selectionArgs,
                                null
                        )?.use { cursorPersonal ->
                            validateVigency(cursorPersonal)
                            cursorPersonal.close()
                        }

                    }
                    .setMessage("No ha seleccionado ninguna opción de procedimiento")
            builder.create().show()
        }
        if (nameOfPersonal != null && document != null) {
            val selection =
                    ("(" + DBHelper.CONTRACT_PERSON_TABLE_COLUMN_PROJECT_ID + " = ?) and (" + DBHelper.CONTRACT_PERSON_TABLE_COLUMN_DOC_NUM + " = ?) and ( "
                            + DBHelper.CONTRACT_PERSON_TABLE_COLUMN_CONTRACT_ID + " = ? )")
            val selectionArgs = arrayOf(idProject, document, contractId)
            contentResolver.query(
                    Constantes.CONTENT_CONTRACT_PER_OFFLINE_URI,
                    null,
                    selection,
                    selectionArgs,
                    null
            )?.use { cursorPersonal ->
                validateVigency(cursorPersonal)
                cursorPersonal.close()
            }

        }
    }

    private fun validateVigency(cursorPersonal: Cursor) {
//        var enterProject: Int
        var flagEnter: Boolean
        if (cursorPersonal.moveToFirst()) {
//            val EnterProjectCursor = cursorPersonal.getColumnIndex(DBHelper.CONTRACT_PERSON_TABLE_COLUMN_ENTERPROJECT)
            val startDatePersonal =
                    cursorPersonal.getColumnIndex(DBHelper.CONTRACT_PERSON_TABLE_COLUMN_START_DATE)
            val finishDatePersonal =
                    cursorPersonal.getColumnIndex(DBHelper.CONTRACT_PERSON_TABLE_COLUMN_FINISH_DATE)
            val startDateContrcat =
                    cursorPersonal.getColumnIndex(DBHelper.CONTRACT_PERSON_TABLE_COLUMN_START_DATE_CONTRACT)
            val finishDateContract =
                    cursorPersonal.getColumnIndex(DBHelper.CONTRACT_PERSON_TABLE_COLUMN_FINISH_DATE_CONTRACT)
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
                        val startDate = format.parse(startDateJson)
                        val finishDate = format.parse(finishDateJson)
                        val rightNow = Calendar.getInstance()
                        if (rightNow.time.after(startDate)) {
                            if (rightNow.time.before(finishDate)) {
                                contractValidity.Allow = true
                                contractValidity.Desc = "OK"
                            } else {
                                contractValidity.Allow = false
                                contractValidity.Desc = "Contrato vencido"
                            }
                        } else {
                            contractValidity.Allow = false
                            contractValidity.Desc = "Contrato sin iniciar"
                        }
                    } catch (e: ParseException) {
                        e.printStackTrace()
                    }
                    if (contractValidity.Allow) break
                }
//                } else {
//                    flagEnter = false
//                }
            } while (cursorPersonal.moveToNext())
            if (!flagEnter) {
                val builder = AlertDialog.Builder(this)
                        .setTitle("Permitir paso")
                        .setPositiveButton("Aceptar", null)
                        .setMessage("No hay acceso a este proyecto")
                builder.create().show()
            } else if (!contractValidity.Allow) {
                val builder = AlertDialog.Builder(this)
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
        supportActionBar!!.title = nameOfPersonal
        supportActionBar!!.subtitle = "CC $document"

        requirementOffline()
    }

    private fun processPersonalReqs(cursorPersonal: Cursor): Array<String?> {
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
                    // logW(ReqJson);
                    reqsToSend =
                            arrayOf(ReqJson, maxJson, minJson, dayJson, startDateJson, finishDateJson)
                    break
                }
            }
        }
        return reqsToSend
    }

    override fun updateValidityItem(mItem: RequirementsList, pos: Int) {
        requirementsInspect!![pos] = mItem
        //inspectRequirimentsAdapter!!.notifyDataSetChanged()
        supportActionBar!!.setBackgroundDrawable(ColorDrawable(resources.getColor(R.color.primary)))
        isValidedInspect = false
        showInspectMessage = false
        for (requirement in requirementsInspect!!) {
            if (!requirement.IsValided) {
                isValidedInspect = true
                break
            }
        }
        if (isValidedInspect || (isValidedCheck && !isValidedSurvey)) {
            showInspectMessage = true
            supportActionBar!!.setBackgroundDrawable(ColorDrawable(resources.getColor(R.color.bar_undecoded)))
            showMenssageAllow()
        }
        showMenssageAllow()
    }

    // Metodo para calcular temperatura segun requisito
    override fun updateValidityInputItem(mItem: RequirementsList, pos: Int, input: String) {
        // Validacion para cuando el campo no se ha llenado
        val pattern = "[1-9]\\d*(\\.\\d+)?".toRegex()
        if (input.isBlank() || !input.matches(pattern)) {
            changeMainButtonsState(false)
            return
        } else {
            changeMainButtonsState(true)
        }
        // calculo para cambiar UI

        val isValidedOld = mItem.IsValided
        val value = input.toFloat()
        when (mItem.ArithmeticOperator) {
            0L -> mItem.IsValided = value < mItem.EntryValue
            1L -> mItem.IsValided = value <= mItem.EntryValue
            2L -> mItem.IsValided = value > mItem.EntryValue
            3L -> mItem.IsValided = value >= mItem.EntryValue
            4L -> mItem.IsValided = value == mItem.EntryValue
        }
        if (isValidedOld != mItem.IsValided) updateValidityItem(mItem, pos)
    }

    private fun changeMainButtonsState(state: Boolean) {
        binding.outButton.isClickable = state
        binding.inButton.isClickable = state
    }

    override fun editRequerimentItem(mItem: RequirementsList?, pos: Int) {
        val builder = AlertDialog.Builder(this)
                .setPositiveButton("SI") { dialog: DialogInterface?, which: Int ->
                    urlModel = mItem!!.Model
                    logW("urlModel${urlModel}")
                    positionArray = pos
                    idReq =
                            if (mItem.EPSName != null || mItem.AFPName != null || mItem.ARLName != null) mItem.Id else mItem.Id
                    idReqOk = pos
                    includeInternalLayout.fromDateBtn.visibility = View.VISIBLE
                    includeInternalLayout.editRequirement.visibility = View.VISIBLE
                    includeInternalLayout.requirementTitle.text = mItem.Type
                    includeInternalLayout.requiremnetDes.text = mItem.Desc
                    when{
                        mItem.Type.equals("ARL")->{
                            binding.contentCkecksPersonal.typeRisk.visibility = View.VISIBLE

                            binding.contentCkecksPersonal.afpInputLayout.visibility = View.GONE
                            binding.contentCkecksPersonal.epsInputLayout.visibility = View.GONE
                            isArl = true
                            isAfp = false
                            isEps = false
                        }
                        mItem.Type.equals("EPS") ->{
                            isEps = true
                            binding.contentCkecksPersonal.epsInputLayout.visibility = View.VISIBLE
                            binding.contentCkecksPersonal.typeRisk.visibility = View.GONE
                            binding.contentCkecksPersonal.afpInputLayout.visibility = View.GONE
                            isArl = false
                            isAfp = false
                        }
                        mItem.Type.equals("AFP") ->{
                            isAfp = true
                            binding.contentCkecksPersonal.afpInputLayout.visibility = View.VISIBLE
                            binding.contentCkecksPersonal.epsInputLayout.visibility = View.GONE
                            binding.contentCkecksPersonal.typeRisk.visibility = View.GONE
                            isArl = false
                            isEps = false
                        }
                    }
                    binding.contentCkecksPersonal.cardValidity.visibility = View.GONE
                    binding.contentCkecksPersonal.employerDescription.visibility = View.GONE
                    binding.inOutMessage.visibility = View.GONE
                    binding.contentCkecksPersonal.recyclerRequirements.visibility = View.GONE
                    binding.contentCkecksPersonal.recyclerRequirementInspect.visibility = View.GONE
                    binding.buttonsSelected.visibility = View.GONE
                    binding.contentCkecksPersonal.documentAge.visibility = View.GONE
                    binding.contentCkecksPersonal.generalInfo.visibility = View.GONE

                    val calendar = Calendar.getInstance()
                    fromDate = calendar.time
                    calendar.add(Calendar.DAY_OF_MONTH, 1)
                    val myFormat = "dd/MMM/yyyy"
                    val sdf = SimpleDateFormat(myFormat, Locale("es", "ES"))
                    fromDateStr = sdf.format(fromDate!!)
                    includeInternalLayout.fromDateBtn.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            0,
                            0
                    )
                    includeInternalLayout.fromDateBtn!!.text = fromDateStr
                    if (!mItem.RequiredDate) includeInternalLayout.fromDateBtn!!.visibility = View.GONE
                    updateDatePicker(fromDate!!)
                }
                .setNegativeButton("NO", null)
                .setMessage("Es necesario tener acceso a internet para subir archivos, desea continuar?")
        builder.create().show()
        requirements!![pos] = mItem!!
        verifyRequirimentsAdapter!!.notifyDataSetChanged()
    }

    override fun arlRiskAlert() {
        showRequerimentsMessage = true
        showMenssageAllow()
    }

    override fun openSurveyActivity(intent: Intent, pos: Int) {
        startActivityForResult(intent, 707)
        posSurvey = pos
    }


    private fun updateDatePicker(fromDate: Date) {
        val cF = Calendar.getInstance()
        cF.time = fromDate
        fromDatePickerDialog = DatePickerDialog(
                this, this, cF[Calendar.YEAR],
                cF[Calendar.MONTH], cF[Calendar.DAY_OF_MONTH]
        )
        fromDatePickerDialog!!.datePicker.minDate = System.currentTimeMillis()
    }

    @SuppressLint("SetTextI18n")
    private fun configurations(isOk: Boolean) {
        val pf = MyPreferences(this)
        try {
            if (isOk) {
                afd = assets.openFd("correct-ding.mp3")
                handlerInteraction!!.removeCallbacks(playSound)
                handlerInteraction!!.postDelayed(playSound, DELAY_PLAY_SOUND)
            } else {
                supportActionBar!!.setBackgroundDrawable(ColorDrawable(resources.getColor(R.color.bar_undecoded)))
                afd = assets.openFd("incorrect-ding.mp3")
                handlerInteraction!!.removeCallbacks(playSound)
                handlerInteraction!!.postDelayed(playSound, DELAY_PLAY_SOUND)
                logW("preference " + preferences.getVerifyAdnRegisterSettings(1))
                if (pf.getVerifyAdnRegisterSettings(1)) vibrateAlert()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private val playSound = Runnable {
        val player = MediaPlayer()
        try {
            player.setDataSource(afd!!.fileDescriptor, afd!!.startOffset, afd!!.length)
            player.prepareAsync()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        player.setOnPreparedListener { player.start() }
        player.setOnCompletionListener { player.release() }
    }
    private val closeActivity = Runnable {
        expiresUserTime = true
        finish()
    }

    override fun onUserInteraction() {
        super.onUserInteraction()
        if (!expiresUserTime) timeInteractionUser() else finish()
    }

    private fun timeInteractionUser() {
        handlerInteraction!!.removeCallbacks(closeActivity)
        handlerInteraction!!.postDelayed(closeActivity, DELAY_USER_TIME)
    }

    private fun permissions() {
        setSelectetButtons()
        claims = AppPermissions.getPermissionsOfUser(this, companyAdminId = companyInfoId)
        validate(claims)
    }

    private fun validate(permissions: ArrayList<String>) {
        if (!permissions.contains("enrollments.validaterequirements")) {
            binding.option1Button.backgroundTintList =
                    ContextCompat.getColorStateList(this, R.color.gray)
            binding.option1Button.isClickable = false
        }
        if (!permissions.contains("enrollments.inspectrequirements")) {
            binding.option2Button.backgroundTintList =
                    ContextCompat.getColorStateList(this, R.color.gray)
            binding.option2Button.isClickable = false
        }
        if (!permissions.contains("enrollments.verifyenterexit")) {
            binding.option3Button.backgroundTintList =
                    ContextCompat.getColorStateList(this, R.color.gray)
            binding.option3Button.isClickable = false
            binding.option4Button.backgroundTintList =
                    ContextCompat.getColorStateList(this, R.color.gray)
            binding.option4Button.isClickable = false
        }

        if (!permissions.contains("enrollments.registers")) {
            binding.bottomSheet.visibility = View.GONE
        } else if (permissions.contains("enrollments.registers")) {
            binding.bottomSheet.visibility = View.GONE
            val layoutParams =
                    (binding.constraintLayoutContent.layoutParams as? ViewGroup.MarginLayoutParams)
            val sizeInDP = 0f
            val marginInDp = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, sizeInDP, resources
                    .displayMetrics
            )
            layoutParams?.setMargins(0, 0, 0, marginInDp.toInt())
            binding.constraintLayoutContent.layoutParams = layoutParams
        }

        preferences.editReqEnabled = permissions.contains("enrollments.editrequirement")
    }

    private fun vibrateAlert() {
        val v = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        // Vibrate for 500 milliseconds
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Objects.requireNonNull(v)
                    .vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            //deprecated in API 26
            Objects.requireNonNull(v).vibrate(500)
        }
    }

    private fun enabledButtons() {
        binding.option1Button.isEnabled = isEnabledButtons
        binding.option2Button.isEnabled = isEnabledButtons
        binding.option3Button.isEnabled = isEnabledButtons
        binding.option4Button.isEnabled = isEnabledButtons
        if (isEnabledButtons) {
            binding.option1Button.backgroundTintList =
                    ContextCompat.getColorStateList(this, R.color.primary_dark)
            binding.option2Button.backgroundTintList =
                    ContextCompat.getColorStateList(this, R.color.primary_dark)
            binding.option3Button.backgroundTintList =
                    ContextCompat.getColorStateList(this, R.color.primary_dark)
            binding.option4Button.backgroundTintList =
                    ContextCompat.getColorStateList(this, R.color.primary_dark)
            permissions()
        } else {
            binding.option1Button.backgroundTintList =
                    ContextCompat.getColorStateList(this, R.color.colorBlueDisabled)
            binding.option2Button.backgroundTintList =
                    ContextCompat.getColorStateList(this, R.color.colorBlueDisabled)
            binding.option3Button.backgroundTintList =
                    ContextCompat.getColorStateList(this, R.color.colorBlueDisabled)
            binding.option4Button.backgroundTintList =
                    ContextCompat.getColorStateList(this, R.color.colorBlueDisabled)
            permissions()
        }
    }

    private fun setSelectetButtons() {
        if (checkReq) {
            if (isEnabledButtons) binding.option1Button.backgroundTintList =
                    ContextCompat.getColorStateList(
                            this,
                            R.color.colorAccent
                    ) else binding.option1Button.backgroundTintList =
                    ContextCompat.getColorStateList(this, R.color.colorGreenDisabled)
        }
        if (inspectReq) {
            if (isEnabledButtons) binding.option2Button.backgroundTintList =
                    ContextCompat.getColorStateList(
                            this,
                            R.color.colorAccent
                    ) else binding.option2Button.backgroundTintList =
                    ContextCompat.getColorStateList(this, R.color.colorGreenDisabled)
        }
        if (registerIn) {
            if (isEnabledButtons) binding.option3Button.backgroundTintList =
                    ContextCompat.getColorStateList(
                            this,
                            R.color.colorAccent
                    ) else binding.option3Button.backgroundTintList =
                    ContextCompat.getColorStateList(this, R.color.colorGreenDisabled)
        }
        if (registerOut) {
            if (isEnabledButtons) binding.option4Button.backgroundTintList =
                    ContextCompat.getColorStateList(
                            this,
                            R.color.colorAccent
                    ) else binding.option4Button.backgroundTintList =
                    ContextCompat.getColorStateList(this, R.color.colorGreenDisabled)
        }
    }

    /**
     * metodo para enviar la informacion del registro al servidor
     */
    private fun positiveButton2() {
        if (checkReq || inspectReq || registerIn || registerOut) {
            supportActionBar!!.title = "ENROLAR"
            binding.positiveButton2.isEnabled = false
            var isValided = true
            requirements.addAll(requirementsInspect)
            for (item in requirements){
                println("item${item.DocsJSON}")
            }
            requirementReport!!.RemainRequirement = requirements
            supportActionBar!!.setBackgroundDrawable(ColorDrawable(resources.getColor(R.color.primary)))
            for (requirementsList in requirementReport!!.RemainRequirement) {
                if (!requirementsList.IsValided) {
                    isValided = false
                    break
                }
            }
            val valideTime = scheduler!!.Allow
            val gsonReq = Gson()

            requirementReport?.RemainRequirement = searchDuplicatedItems(requirementReport?.RemainRequirement)
            val jsonReq = gsonReq.toJson(requirementReport)
            val newReport: ReportPersonal
            val jsonLocation = currentLocation
            newReport = Gson().fromJson(jsonLocation.toString(), ReportPersonal::class.java)
            newReport.TimeMillis = System.currentTimeMillis()
            newReport.PersonalCompanyInfoId = personalInfoId
            if (userLogin!!.IsAdmin || userLogin!!.IsSuperUser) newReport.CompanyInfoId =
                    userLogin!!.CompanyId else if (companyInfoId != null) newReport.CompanyInfoId =
                    companyInfoId
            newReport.ProjectId = idProject
            newReport.ProjectStageId = idSteps
            newReport.ContractId = contractId
            newReport.RequirementJson = jsonReq
            newReport.Event = reportEvent
            try {
                var options = JSONObject(Gson().toJson(scheduler))
                options = Utils.merge(
                        options, JSONObject(
                        Gson().toJson(
                                OptionsJson(
                                        checkReq,
                                        requirementsInspect.isNotEmpty() && inspectReq,
                                        registerIn || registerOut,
                                        isValided && valideTime && valideAge
                                )
                        )
                )
                )
                newReport.OptionsJson = options.toString()
            } catch (e: JSONException) {
                e.printStackTrace()
            }
            newReport.VersionApp = "${BuildConfig.VERSION_NAME} - ${BuildConfig.VERSION_CODE}"
            DebugLog.logE("Requerimento: $jsonReq")
            logW("Event: " + newReport.Event + ", registerIn: " + registerIn)
            logW(newReport.OptionsJson)
            log("Json report: ${Gson().toJson(newReport)}")
            val json = Gson().toJson(newReport)

            viewModel.sendReport(json)

//            val cv = ContentValues()
//            cv.put(DBHelper.PERSONAL_REPORT_TABLE_COLUMN_IMAGES, "")
//            cv.put(DBHelper.PERSONAL_REPORT_TABLE_COLUMN_SERVER_ID, 0)
//            cv.put(DBHelper.PERSONAL_REPORT_TABLE_COLUMN_URL, Constantes.PERSONAL_REPORT_URL)
//            cv.put(DBHelper.PERSONAL_REPORT_TABLE_COLUMN_METHOD, Request.Method.POST)
//            cv.put(DBHelper.PERSONAL_REPORT_TABLE_COLUMN_DATA, json)
//            logW(json)
//            this.contentResolver.insert(Constantes.CONTENT_PERSON_REPORT_URI, cv)
//            UpdateDBService.startRequest(this, false)
            onCancelUserInfo()

            if (registerIn || registerOut) {
                sendEventSignalR()
            }
        }
    }

    private fun sendEventSignalR() {
        if (reportEvent == 0) return

        val personalRealTime = PersonalRealTime(
                personalId = personalInfoId.toLong(),
                projectId = idProject!!
        )
        if (reportEvent == 1)
            personalRealTime.getTime = System.currentTimeMillis()

        if (reportEvent == 2)
            personalRealTime.lastTime = System.currentTimeMillis()


        viewModel.insertPersonalRealTimeDB(
                personalRealTime,
                reportEvent == 1,
                document,
                companyInfoId,
                namePersonal,
                lastNamePersonal
        )

    }

    private fun verifyInDb(): Boolean {
        val data = LinkedHashMap<Uri, String>()
        data[Constantes.CONTENT_CONTRACT_PER_OFFLINE_URI] = Constantes.LIST_CONTRACT_PER_OFFLINE_URL_PCII
        val fillArray = BooleanArray(data.size)
        var pos = 0
        for (o in data.entries) {
            val pair = o as Map.Entry<*, *>
            var selection: String? = null
            var selectionArgs: Array<String>? = null
            if (pair.value == Constantes.LIST_CONTRACT_PER_OFFLINE_URL_PCII && personalCompanyInfoId != null) {
                selection = ("( " + DBHelper.CONTRACT_PERSON_TABLE_COLUMN_PERSONALCOMPANY_ID + " = ?)")
                selectionArgs = arrayOf(personalInfoId.toString())
            }
            contentResolver.query(
                (pair.key as Uri?)!!, null, selection, selectionArgs,
                null
            ).use { cursor ->
                cursor?.let {
                    fillArray[pos] = cursor.count != 0
                    logW(pos.toString() + ". URL to Request: " + pair.value + ": " + fillArray[pos] + " cursor.count ${cursor.count}")
                    pos++
                }
            }
        }

        var flag = true
        for (item in fillArray) {
            if (!item) {
                flag = false
                break
            }
        }
        return flag
    }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStringResult(action: String?, option: Int, jsonObjStr: String?, url: String?) {
        //progressDialog.dismiss();
        logW(url + option)
        when (option) {

            Constantes.SUCCESS_REQUEST -> {
               when (url) {
                    Constantes.LIST_CONTRACT_PER_OFFLINE_URL_PCII + personalCompanyInfoId -> {//"api/Personal/ContractsOffline?full=true&personalCompanyInfoId="
                        val project = JSONObject(jsonObjStr)
                        val personalids = project.getString("PersonalIds")
                        replacepersonalIds = personalids.replace("\\[|]|\\s".toRegex(), "")
                        loadDataFromServer(replacepersonalIds!!)

                    }

                }
                if(verifyFillData()){
                    if( preferences.synContractOffline){
                        preferences.syncDate = Calendar.getInstance().time.time
                        val sdf = SimpleDateFormat("yyyy-MM-dd hh:mm:ss")
                        val currentDate = sdf.format(Date())
                        Toast.makeText(
                            this,
                            "Sincronización de datos exitosa",
                            Toast.LENGTH_SHORT
                        ).show()
                        viewModel.insertPersonalSyn(personalCompanyInfoId)
                    }else{
                        Toast.makeText(
                            this,
                            "La sincronización de datos fallo, por favor vuelva a sincronizar",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    binding.syncButton.clearAnimation()
                }
            }


            Constantes.SUCCESS_FILE_UPLOAD -> if (url == "api/$urlModel/UpdateFile/$idReq") {
                logW(jsonObjStr)
                val reqRequest = Gson().fromJson<RequirementsList>(
                        jsonObjStr,
                        object : TypeToken<RequirementsList?>() {}.type
                )
                updatePersonal = true
                viewModel.loadPersonalContractOffline(personalInfoId.toLong())
                /*
                requirements!![idReqOk].IsValided = true
                requirements!![idReqOk].Date = reqRequest.Date
                requirements!![idReqOk].ValidityDate = reqRequest.ValidityDate
                requirements!![idReqOk].File = reqRequest.File

//                    requirementReport.RemainRequirement = requirements;
                includeInternalLayout.editRequirement.visibility = View.GONE
                requirementsObjectOK()
                val values = ContentValues()
                values.put("project", true)
                val paramsQuery = HttpRequest.makeParamsInUrl(values)
                CrudIntentService.startRequestCRUD(
                    this,
                    Constantes.LIST_CONTRACT_PER_OFFLINE_URL,
                    Request.Method.GET,
                    "",
                    paramsQuery,
                    true,
                    true
                )*/
            }
            Constantes.SEND_REQUEST -> if (url == Constantes.LIST_CONTRACT_PER_OFFLINE_URL_PCII) {
                showProgress(false)
                recreate()
            }
            Constantes.NOT_INTERNET, Constantes.BAD_REQUEST, Constantes.TIME_OUT_REQUEST -> {
                showProgress(false)
                Toast.makeText(this, "Sin conexion a Internet", Toast.LENGTH_SHORT).show()
                includeInternalLayout.editRequirement.visibility = View.GONE
                binding.contentCkecksPersonal.cardValidity.visibility = View.VISIBLE
                binding.contentCkecksPersonal.employerDescription.visibility = View.VISIBLE
                binding.inOutMessage.visibility = View.VISIBLE
                binding.contentCkecksPersonal.recyclerRequirements.visibility = View.VISIBLE
                binding.contentCkecksPersonal.recyclerRequirementInspect.visibility = View.VISIBLE
                binding.buttonsSelected.visibility = View.VISIBLE
                binding.contentCkecksPersonal.documentAge.visibility = View.VISIBLE
                binding.contentCkecksPersonal.generalInfo.visibility = View.VISIBLE
            }
        }
    }

    private fun verifyFillData(): Boolean {
        var flag = true
        for (item in fillArray) {
            if (!item) {
                flag = false
                break
            }

        }
        if (flag) {
            binding.syncButton.isEnabled = true
            mHandler.removeCallbacks(enabledSyncButton)
            preferences.syncDate = Calendar.getInstance().time.time
//            showProgress(false)
            enableComponents(true)
            verifyPersonalRegistros()
//            if (viewModel.projects.value.isNullOrEmpty()) withoutProjects()
        }
        return flag
    }

    private fun verifyPersonalRegistros() {
        projectId?.let {

            viewModel.syncSignalR(personalCompanyInfoId.toString())
            viewModel.updatePersonalByProject(projectId!!)
        }
    }


    private fun loadDataFromServer(PersonalIds:String) {
        if (projectId.isNullOrEmpty()) {
            Toast.makeText(
                this,
                "Por favor espere mientras que se carga el proyecto segun la ubicacion",
                Toast.LENGTH_SHORT
            ).show()
            showProgress(false)

            return
        }
        binding.syncButton.isEnabled = false
        mHandler.postDelayed(enabledSyncButton, 60000)
        data = LinkedHashMap()
        data!![Constantes.CONTENT_CONTRACT_PER_OFFLINE_URI] = Constantes.LIST_CONTRACT_PER_OFFLINE_URL_PCII
        fillArray = BooleanArray(data!!.size)
        launchRequests(PersonalIds)
    }

    private fun launchRequests(PersonalIds:String) {
        for ((pos, o) in data!!.entries.withIndex()) {
            val pair = o as Map.Entry<*, *>
            val values = ContentValues()
            when (pair.value) {
                Constantes.LIST_CONTRACT_PER_OFFLINE_URL_PCII -> {
                    values.put(Constantes.KEY_SELECT, true)
                    values.put("personal", true)
                    values.put("personalId", personalId)
                    val paramsQuery = HttpRequest.makeParamsInUrl(values)
                    co.com.sersoluciones.facedetectorser.utilities.DebugLog.logW("Entrada$paramsQuery")
                    CRUDService.startRequest(
                        this, pair.value as String?,
                        Request.Method.GET, paramsQuery, true
                    )
                }

                Constantes.PERSONAL_BY_ARRAYIDS -> {
                    CRUDService.startRequest(
                        this, pair.value as String?,
                        Request.Method.POST, true,PersonalIds,true
                    )
                }
            }
        }
    }

    private fun replacePersonalInfo(listInfo: List<PersonalContractOfflineNetwork>) {
        showProgress(false)
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
            contentResolver.delete(
                    Constantes.CONTENT_CONTRACT_PER_OFFLINE_URI,
                    selection,
                    selectionArgs
            )
            contentResolver.insert(Constantes.CONTENT_CONTRACT_PER_OFFLINE_URI, contentValues)
            updatePersonal = false
            viewModel.clearPersonalContractOffline()
            recreate()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun showAlertLoad() {
        if (!isFinishing) {
            if (dialog != null && dialog!!.isShowing) return
            dialogLoad?.dismiss()
            val builder = AlertDialog.Builder(this)
                .setTitle("Sin Datos")
                .setCancelable(false)
                .setMessage("Por favor sincronice los datos con el servidor para el correcto funcionamiento del modulo")
                .setPositiveButton("Sincronizar") { dialog, _ ->
                    var animation : Animation = AnimationUtils.loadAnimation(this,R.anim.rotatesync)
                    binding.syncButton.startAnimation(animation)
                    enableComponents(false)
                    UpdateDBService.startRequest(this, false)
                    personalCompanyInfoId?.let {
                        LoadPersonalByid(personalCompanyInfoId)
                        preferences.selectedPersonContractOffline = personalCompanyInfoId.toString()

                    }
                    //loadDataFromServer()
                    dialog.dismiss()
                }
            dialogLoad = builder.create()
            dialogLoad?.show()
        }
    }

    private fun searchDuplicatedItems(remainRequirements: ArrayList<RequirementsList>?): ArrayList<RequirementsList>? {
        if (remainRequirements == null) return remainRequirements
        val it = remainRequirements.iterator()
        while (it.hasNext()) {
            val req = it.next()
            val list = remainRequirements.filter { x -> x.Id == req.Id && x.Type == req.Type }
            if (list.size > 1) {
                it.remove()
            }
        }
        return remainRequirements
    }

    companion object {
        const val ARG_DOC_NUMBER = "document"
        const val ARG_PROJECT_ID = "projectId"
        const val ARG_NAME = "name"
        const val ARG_LAST_NAME = "lastName"
        const val ARG_PHOTO = "photo"
        const val ARG_PERSONAL_ID = "personalId"
        const val ARG_CONTRACT_ID = "contractId"
        const val ARG_PER_COMP_IF_ID = "personalCompayInfoId"
        const val ARG_PERSONAL_JOB = "personalJob"
        const val ARG_CONTRACT_FINISH_DATE = "contractFinisDate"
        const val ARG_CHECK_REQ = "checkReq"
        const val ARG_INSPECT_REQ = "inspectReq"
        const val ARG_REG_OUT = "registerOut"
        const val ARG_REG_IN = "registerIn"
        const val ARG_OPT_SCAN = "option_scan"
        const val ARG_CLAIMS = "claims"
        const val ARG_COMPANY_INFO_ID = "CompanyInfoId"
        const val ARG_TYPE_CONTRACT = "TypeContract"
        const val ARG_EMPLOYER_CONTRACT = "EmployerContract"
        const val ARG_PERSON_GUID = "PersonGuidId"
        private const val DELAY_USER_TIME = 5 * 60 * 1000.toLong()
        private const val DELAY_PLAY_SOUND: Long = 1000
    }
    fun hideBottomSheet(view: View) {
        mBottomSheetBehavior!!.state = BottomSheetBehavior.STATE_COLLAPSED
    }
}
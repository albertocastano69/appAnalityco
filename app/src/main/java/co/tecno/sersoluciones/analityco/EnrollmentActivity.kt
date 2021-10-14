package co.tecno.sersoluciones.analityco

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.app.Activity
import android.content.*
import android.content.pm.ActivityInfo
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import co.com.sersoluciones.barcodedetectorser.BarcodeDecodeSerActivity
import co.com.sersoluciones.barcodedetectorser.utilities.DebugLog.log
import co.com.sersoluciones.barcodedetectorser.utilities.DebugLog.logW
import co.tecno.sersoluciones.analityco.adapters.PersonalDailyAdapter
import co.tecno.sersoluciones.analityco.adapters.project.ProjectRecyclerViewAdapter
import co.tecno.sersoluciones.analityco.adapters.repositories.EnrollmentRepository
import co.tecno.sersoluciones.analityco.callback.OnListProjectInteractionListener
import co.tecno.sersoluciones.analityco.databases.DBHelper
import co.tecno.sersoluciones.analityco.databinding.ActivityEnrollmentBinding
import co.tecno.sersoluciones.analityco.databinding.ContentEnrollmentBinding
import co.tecno.sersoluciones.analityco.models.*
import co.tecno.sersoluciones.analityco.nav.CreatePersonalActivity
import co.tecno.sersoluciones.analityco.nav.CreatePersonalViewModel
import co.tecno.sersoluciones.analityco.nav.LoadingApiStatus
import co.tecno.sersoluciones.analityco.receivers.RequestBroadcastReceiver
import co.tecno.sersoluciones.analityco.receivers.RequestBroadcastReceiver.BroadcastListener
import co.tecno.sersoluciones.analityco.repository.AnalitycoRepository
import co.tecno.sersoluciones.analityco.repository.ApiStatus
import co.tecno.sersoluciones.analityco.services.*
import co.tecno.sersoluciones.analityco.utilities.*
import co.tecno.sersoluciones.analityco.utilities.Constantes.URL_VALIDATED_PERSONAL_CODE
import co.tecno.sersoluciones.analityco.utilities.DecodeBarcode.InfoUser
import co.tecno.sersoluciones.analityco.viewmodels.PersonalDailyViewModel
import com.android.volley.Request
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_edit_form_company.*
import kotlinx.android.synthetic.main.fragment_info.view.*
import kotlinx.coroutines.*
import kotlinx.coroutines.Job
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.lang.Runnable
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList
import kotlin.collections.LinkedHashMap
import kotlin.coroutines.CoroutineContext

var CREATE_PERSONAL_CODE = 11
var REQUEST_JOIN_PERSONAL = 7

class EnrollmentActivity : BaseActivity(), OnItemSelectedListener, BroadcastListener,
        CoroutineScope {


    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job
    private lateinit var job: Job

    @Inject
    lateinit var viewModel: PersonalDailyViewModel

    @Inject
    lateinit var repository: EnrollmentRepository

    @Inject
    lateinit var createPersonalViewModel: CreatePersonalViewModel

    @Inject
    lateinit var repositoryAnalityco: AnalitycoRepository
    private var requestBroadcastReceiver: RequestBroadcastReceiver? = null
    private var projectId: String? = null

    private val mListener: OnListProjectInteractionListener? = null
    private var adapter: ProjectRecyclerViewAdapter? = null
    private var mValues: ArrayList<ProjectList>? = null
    var replace: String? = null
    var replacepersonalIds:String? = null
    var replaceEmployerIds:String? = null
    //private int stepId;
    private var loadProject = false
    private  var projectselect:String?=null
    private var namePersonal: String? = null
    private var lastNamePersonal: String? = null
    private var photoReq: String? = null
    private var documentReq: String? = null
    private var EmployerId: String? = null
    private var personalId = 0
    private var personalcompanyinfoId = 0
    private var personalGuiId : String? = null
    private var personalJob: String? = null
    private var descrpciontype: String? = null
    private var contractId: String? = null
    private var contractFinishDate: String? = null
    private var exit = false
    private var checkReq = false
    private var inspectReq = false
    private var registerOut = false
    private var registerIn = false
    private var optionScan = false
    private var dialog: AlertDialog? = null
    private var dialogLoad: AlertDialog? = null
    private var data: LinkedHashMap<Uri, String>? = null
    private var fillArray: BooleanArray = BooleanArray(5)
    private var isFlash = false
    private var addPersonalFlag = false
    private var user: User? = null
    var flagStart = true
    private var claims: ArrayList<String> = ArrayList()
    private var companyInfoId: String? = null
    private var isEnabledButtons = false
    private var code: String = ""
    private var documentNumber: String = ""
    private var tempEmployerId: String? = null
    private var companyAdminId: String = ""
    private val mHandler = Handler()
    private var codeUrl = ""
    private var mBottomSheetBehavior: BottomSheetBehavior<View>? = null
    var startDateStr: String? = null
    var startDateJson: String? = null
    var documentNewDate: String? =null
    var EmployerName : String? = null
    var mSqLiteDatabase: SQLiteDatabase? = null
    var mDbHelper: DBHelper? = null
    lateinit var binding: ActivityEnrollmentBinding
    private lateinit var includeInternalLayout: ContentEnrollmentBinding
    @SuppressLint("SetTextI18n", "SourceLockedOrientationActivity")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        ApplicationContext.analitycoComponent.inject(this)
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(
                this, R.layout.activity_enrollment
        )
        showProgress(true)
        job = Job()
        includeInternalLayout = binding.contentEnrollment
        mDbHelper = DBHelper(this)
        mSqLiteDatabase = mDbHelper!!.readableDatabase
//        binding.viewModel = viewModel
        viewModel.getCountRegistersDB()
        viewModel.countDB.observe(this, androidx.lifecycle.Observer {
            it?.let {
                includeInternalLayout.registers.text = "Registros sin subir a la nube =  $it"
                if (it >= 500) {
                    includeInternalLayout.btnUpdateRegisters.visibility = View.VISIBLE
                    includeInternalLayout.showregisters.visibility=View.VISIBLE
                    includeInternalLayout.sync.setOnClickListener {
                        sendreport()
                    }
                    includeInternalLayout.btnUpdateRegisters.setOnClickListener {
                        sendreport()
                    }
                } else if (it == 0) {
                    includeInternalLayout.btnUpdateRegisters.visibility = View.GONE
                    includeInternalLayout.showregisters.visibility=View.GONE
                }
            }
        })
        includeInternalLayout.showBottonSheet.setOnClickListener {

            binding.bottomSheet.visibility= View.VISIBLE
            val mBottomSheet = findViewById<View>(R.id.bottom_sheet)
            mBottomSheetBehavior = BottomSheetBehavior.from<View>(mBottomSheet)
            mBottomSheetBehavior!!.state = BottomSheetBehavior.STATE_EXPANDED
            findViewById<View>(R.id.shadow).visibility = View.GONE
            findViewById<View>(R.id.shadow2).visibility = View.GONE
            (findViewById<View>(R.id.label_sliding1) as TextView).text = "Registros"
            binding.labelSliding1.visibility = View.VISIBLE
            binding.iconTabD.visibility = View.VISIBLE
            binding.iconTab.visibility = View.GONE
            binding.listPersonal.isNestedScrollingEnabled = false
        }

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        setSupportActionBar(binding.toolbar)
        if (supportActionBar != null) {
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.title = "ENROLAR"
        }
        val preferences = MyPreferences(this@EnrollmentActivity)
        val profile = preferences.profile
        user = Gson().fromJson(profile, User::class.java)
        fillArray = BooleanArray(5)
        requestBroadcastReceiver = RequestBroadcastReceiver(this)
        exit = false
        optionScan = false
        checkReq = false
        registerOut = false
        registerIn = false
        inspectReq = false
        isEnabledButtons = preferences.enabledErollmentButtons
        //        findViewById(R.id.option1).setBackgroundTintList(EnrollmentActivity.this.getResources().getColorStateList(R.color.colorAccent));
        isFlash = false
        includeInternalLayout.switchLight.setOnCheckedChangeListener { _, isChecked ->
            isFlash = isChecked
            if (isChecked) includeInternalLayout.switchLight.text =
                    "LUZ/ON" else includeInternalLayout.switchLight.text = "LUZ/OFF"
        }
        enableComponents(false)

        permissions()
        viewModel.loadSurveyFromServer()
        viewModel.createSignalR()

        loadProjects()

        binding.contentEnrollment.spinnerProjects.onItemSelectedListener = this
        viewModel.projects.observe(this, androidx.lifecycle.Observer {
            it?.let {
                val adapter = ArrayAdapter(this, R.layout.simple_spinner_item_project, it)
                binding.contentEnrollment.spinnerProjects.adapter = adapter
            }
        })

        viewModel.notFoundProjects.observe(this, androidx.lifecycle.Observer {
            it?.let {
                if (it) withoutProjects()
            }
        })
        viewModel.selectedProject.observe(this, androidx.lifecycle.Observer { projectList ->
            projectList?.let {

                companyAdminId = it.CompanyInfoId
                projectId = it.Id
                companyInfoId = it.CompanyInfoId
                preferences.selectedProjectId = projectId
                viewModel.startSignalR(projectId!!)
                viewModel.updatePersonalByProject(projectId!!)
                permissions()
                var i = 0
                for (project in viewModel.projects.value!!) {
                    if (projectList.Name == project.Name) {
                        logW("project found ${projectList.Name} position $i")
                        binding.contentEnrollment.spinnerProjects.setSelection(i)
                        projectselect=projectList.Name
                        logW("project found ${projectList.Name} position $i")
                        binding.syncButton.setOnClickListener{
                            var animation : Animation = AnimationUtils.loadAnimation(this,R.anim.rotatesync)
                            binding.syncButton.startAnimation(animation)
                            enableComponents(false)
                            UpdateDBService.startRequest(this, false)
                            LoadProjectByids(projectId!!)
                        }
                        //logW("urlproject:${return1}")
                        break
                    }
                    i++
                }
                enableComponents(true)
                if (!verifyInDb()) {
                    showAlertLoad()
                }
            }
        })
        configBottomSheet()

        mValues = ArrayList()
        adapter = ProjectRecyclerViewAdapter(this, mListener, mValues)
    }

    private fun loadProjects() {
        val values = ContentValues()
        values.put(Constantes.KEY_SELECT, true)
        val paramsQuery = HttpRequest.makeParamsInUrl(values)
        CRUDService.startRequest(
                this, Constantes.LIST_PROJECTS_URL,
                Request.Method.GET, paramsQuery, true
        )
        showProgress(false)
    }
    override fun attachLayoutResource() {
//        super.setChildLayout(R.layout.activity_enrollment)
    }

    fun hideBottomSheet(view: View) {
        mBottomSheetBehavior!!.state = BottomSheetBehavior.STATE_COLLAPSED
    }

    @SuppressLint("ClickableViewAccessibility", "SetTextI18n")
    private fun configBottomSheet() {

        val mBottomSheet = findViewById<View>(R.id.bottom_sheet)
        mBottomSheetBehavior = BottomSheetBehavior.from<View>(mBottomSheet)
        mBottomSheetBehavior!!.state = BottomSheetBehavior.STATE_COLLAPSED

        mBottomSheetBehavior!!.addBottomSheetCallback(object : BottomSheetCallback() {
            @SuppressLint("RestrictedApi")
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_COLLAPSED -> {
                        /*//mBottomSheetBehavior.setPeekHeight(0);
                        findViewById<View>(R.id.shadow).visibility = View.GONE
                        findViewById<View>(R.id.shadow2).visibility = View.GONE
                        (findViewById<View>(R.id.label_sliding) as TextView).text = "Registros"
                        binding.iconTabD.visibility = View.GONE
                        binding.iconTab.visibility = View.VISIBLE
//                        binding.toolbar.setCollapsible(false)
                        binding.listPersonal.isNestedScrollingEnabled = true*/
                        mBottomSheet.visibility = View.GONE
                        binding.listPersonal.isNestedScrollingEnabled = true
                        findViewById<View>(R.id.shadow).visibility = View.GONE
                        findViewById<View>(R.id.shadow2).visibility = View.GONE
                    }
                    BottomSheetBehavior.STATE_HIDDEN -> {
                    }
                    BottomSheetBehavior.STATE_EXPANDED -> {
                        findViewById<View>(R.id.shadow).visibility = View.GONE
                        findViewById<View>(R.id.shadow2).visibility = View.GONE
                        //(findViewById<View>(R.id.label_sliding) as TextView).text = ""
                        (findViewById<View>(R.id.label_sliding1) as TextView).text = "Registros"
                        binding.labelSliding1.visibility = View.VISIBLE
                        binding.iconTabD.visibility = View.VISIBLE
                        binding.iconTab.visibility = View.GONE
//                        binding.toolbar.setCollapsible(true)
                        binding.listPersonal.isNestedScrollingEnabled = false
                    }
                    BottomSheetBehavior.STATE_DRAGGING -> {
                        findViewById<View>(R.id.shadow).visibility = View.VISIBLE
                        findViewById<View>(R.id.shadow2).visibility = View.VISIBLE
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
        viewModel.personals.observe(this, Observer {
            it?.let {
                adapter.data = it
                if (it.isNotEmpty()) {
                    smoothScroller.targetPosition = 0
                    mLayoutManager.startSmoothScroll(smoothScroller)
                }
                showProgress(false)

//                mLayoutManager.scrollToPosition(0)
            }
        })
        viewModel.personalContractOffline.observe(this, androidx.lifecycle.Observer {
            it?.let {
                replacePersonalInfo(it)
            }
        })

      viewModel.updateList.observe(this, Observer {
            it?.let {
                if (it && projectId != null)
                    viewModel.updatePersonalByProject(projectId!!)
            }
        })

        viewModel.afIndicator.observe(this, Observer {
            binding.afIndicator.text = "AFORO = $it"
        })

    }
    override fun onStart() {
        super.onStart()
        checkLocationSettings { value ->
            if (!value) {
                Toast.makeText(
                        this,
                        "Por favor prenda el GPS y vuelva a ingresar a este modulo",
                        Toast.LENGTH_LONG
                ).show()
                finish()
            }
        }
    }
    override fun onStop() {
        super.onStop()
        if (exit) stopService(Intent(this, LocationUpdateService::class.java))
    }

    override fun onResume() {
        super.onResume()
        var intentFilter = IntentFilter()
        intentFilter.addAction(CRUDService.ACTION_REQUEST_SAVE)
        intentFilter.addAction(CRUDService.ACTION_REQUEST_GET)
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(requestBroadcastReceiver!!, intentFilter)

        intentFilter = IntentFilter()
        intentFilter.addAction(SER_LOCATION_ACTION)
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(locationReceiver, intentFilter)
    }

    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(requestBroadcastReceiver!!)
        LocalBroadcastManager.getInstance(this).unregisterReceiver(locationReceiver)
    }

    private fun replacePersonalInfo(listInfo: List<PersonalContractOfflineNetwork>) {

        val personalContractOfflineList = listInfo.filter { x ->
            x.ProjectId == projectId
        }
        if (personalContractOfflineList.isNotEmpty()) {

            val personalCompanyInfo = personalContractOfflineList.first()
            val selection = (DBHelper.CONTRACT_PERSON_TABLE_COLUMN_PERSONALCOMPANY_ID + " = ? ")

            val selectionArgs = arrayOf(personalCompanyInfo.PersonalCompanyInfoId.toString())
            var count = contentResolver.delete(
                    Constantes.CONTENT_CONTRACT_PER_OFFLINE_URI, selection, selectionArgs
            )
            logW("Count delete $count")
            val jArray = JSONArray(Gson().toJson(personalContractOfflineList))
            logW("personalCompanyInfo.DocumentNumber ${personalCompanyInfo.DocumentNumber}")
            val contentValues = Utils.reflectToContentValues(jArray)
            count = contentResolver.bulkInsert(
                    Constantes.CONTENT_CONTRACT_PER_OFFLINE_URI_BULK_INSERT,
                    contentValues
            )
            logW(String.format("CONTENT_CONTRACT_PER_OFFLINE_URI_BULK_INSERT count: %s", count))
            showProgress(true)
            Handler().postDelayed({
               showProgress(false)

                launch {
                    verifyPersonalRegistros()
                    //viewModel.updatePersonalList()
                    validateCursorPersonal(
                            findPerson(personalCompanyInfo.DocumentNumber),
                            personalCompanyInfo.DocumentNumber!!
                    )
                    viewModel.clearPersonalContractOffline()
                }

            }, 1000)


        }
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val item = parent!!.adapter.getItem(position) as ProjectList
        viewModel.setSelectedProject(item)
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
    }

    private fun withoutProjects() {
        if (!isFinishing) {
            dialog?.dismiss()
            showProgress(false)

            val builder = AlertDialog.Builder(this@EnrollmentActivity)
                    .setTitle("Sin Proyectos")
                    .setCancelable(false)
                    .setMessage("No se encontraron proyectos en esta ubicación")
                    .setPositiveButton("Aceptar") { _, _ ->
                        exit = true
                        viewModel.clearFormData()
                        finish()
                    }
            dialog = builder.create()
            dialog!!.show()
        }
    }

    public override fun onDestroy() {
        if (dialog != null) {
            dialog!!.dismiss()
            dialog = null
        }
        viewModel.stopSignalR()
        job.cancel()
        super.onDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean { // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.enrollment_menu, menu)
        val menuItem = menu.findItem(R.id.action_settings)

        if (claims.isNotEmpty() && claims.contains("enrollments.blockbuttons")) {
            menuItem.isEnabled = false
            menuItem.title = getString(R.string.botones_fijos)
            preferences.enabledErollmentButtons = false
            isEnabledButtons = false
        }

        if (isEnabledButtons) menuItem.title =
                getString(R.string.fijar_botones) else menuItem.title =
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

    override fun onBackPressed() {
        if (mBottomSheetBehavior!!.state == BottomSheetBehavior.STATE_COLLAPSED) {
            val builder = android.app.AlertDialog.Builder(this@EnrollmentActivity)
                    .setTitle("Alerta")
                    .setMessage("Esta seguro de volver atrás?")
                    .setPositiveButton("Aceptar") { _, _ ->
                        exit = true
                        viewModel.clearFormData()
                        finish()
                    }
                    .setNegativeButton("Cancelar") { dialogInterface, i -> dialogInterface.dismiss() }
            if (!isFinishing)
                builder.create().show()

        } else {
            mBottomSheetBehavior!!.state = BottomSheetBehavior.STATE_COLLAPSED
        }

    }

    fun scanDNI(view: View) {
        val intent = Intent(this, BarcodeDecodeSerActivity::class.java)
        intent.putExtra("isFlash", isFlash)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP and Intent.FLAG_ACTIVITY_SINGLE_TOP)
        startActivityForResult(intent, 0)
    }

    fun sendDocument(view: View) {
        if (includeInternalLayout.document.text.toString().isEmpty()) {
            includeInternalLayout.document.error = "Ingrese documento"
        } else {
            val imm = (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
            imm.hideSoftInputFromWindow(includeInternalLayout.document.windowToken, 0)
            onApplyScanDoc(includeInternalLayout.document.text.toString())
        }
    }

    private fun onApplyScan(infoUser: InfoUser?) {
        if (infoUser == null) return
        optionScan = true
        logW("cc: " + infoUser.dni)
        launch {
            if (infoUser.dni.equals(0)) {
                MetodosPublicos.alertDialog(
                        this@EnrollmentActivity,
                        "No se pudo procesar el codigo de barras, intente de nuevo."
                )
                return@launch
            }
            validateCursorPersonal(
                    findPerson(infoUser.dni.toString()),
                    infoUser.dni.toString(),
                    infoUser = infoUser
            )
            documentNewDate=infoUser.dni.toString()
        }
    }

    private fun onApplyScanDoc(document: String) {
        logW("doc: $document")
        optionScan = false
        launch {
            val newUser = InfoUserScan()
            newUser.dni = document.toLong()
            if (document.isEmpty()) {
                MetodosPublicos.alertDialog(
                        this@EnrollmentActivity,
                        "No se pudo procesar el codigo de barras, intente de nuevo."
                )
                return@launch
            }
            validateCursorPersonal(findPerson(document), document)
            documentNewDate=document
            clearText()
        }
    }

    private suspend fun validateCursorPersonal(
            cursorPersonal: Cursor?,
            document: String,
            infoUser: InfoUser? = null
    ) {
        cursorPersonal.use { cursor ->
            if (cursor == null) {
                val builder = AlertDialog.Builder(this@EnrollmentActivity)
                        .setTitle("Personal no encontrado en este proyecto")
                        .setMessage("El empleado con el documento: $document no ha sido creado o asociado, desea crearlo?.")
                if (addPersonalFlag) {
                    builder.setPositiveButton("SI") { dialog, _ ->
                        createPersonalViewModel.scan = optionScan
                        val intent =
                                Intent(this@EnrollmentActivity, CreatePersonalActivity::class.java)
                        intent.putExtra("documentNumber", document)
                        if (infoUser != null)
                            intent.putExtra("infoUser", infoUser)
                        intent.putExtra("projectId", projectId)
                        //                        // Closing all the Activities
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        //                        // Add new Flag to start new Activity
                        //                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivityForResult(intent, CREATE_PERSONAL_CODE)
                        dialog.dismiss()
                    }
                    builder.setNegativeButton("NO") { dialogInterface, _ ->
                        dialogInterface.dismiss()
                        if (optionScan) {
                            val intent = Intent(this, BarcodeDecodeSerActivity::class.java)
                            intent.putExtra("isFlash", isFlash)
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP and Intent.FLAG_ACTIVITY_SINGLE_TOP)
                            startActivityForResult(intent, 0)
                        }

                    }
                } else {
                    builder.setNegativeButton("OK", null)
                    builder.setMessage("El empleado con el documento: $document no ha sido creado o asociado, desea crearlo?. Solicite los permisos requeridos.")
                }
                builder.create().show()

            } else {
                processValidity(cursor)
            }
        }
    }

    private fun onApplyScanDocQR(code: String?) {
        if (code == null) return
        launch {
            optionScan = true
            if (code.isEmpty()) {
                MetodosPublicos.alertDialog(
                        this@EnrollmentActivity,
                        "No se pudo procesar el codigo de barras, intente de nuevo."
                )
                return@launch
            }
            when (findPersonQR(code)) {
                0 -> {
                    val selection =
                            ("(" + DBHelper.CONTRACT_PERSON_TABLE_COLUMN_PROJECT_ID + " = ?) and ("
                                    + DBHelper.CONTRACT_PERSON_TABLE_COLUMN_CARNET_JSON + " lIKE ?)")
                    val selectionArgs = arrayOf(projectId!!, "%$code%")
                    contentResolver.query(
                            Constantes.CONTENT_CONTRACT_PER_OFFLINE_URI,
                            null,
                            selection,
                            selectionArgs,
                            null
                    )?.use { cursor ->
                        processValidity(cursor)
                    }
                }
                1 -> {
                }
                2 -> {
                    MetodosPublicos.alertDialog(
                            this@EnrollmentActivity,
                            "EL TRABAJADOR NO ESTA ACTIVO EN EL PROYECTO"
                    )
                }
                4 -> {
                    MetodosPublicos.alertDialog(
                            this@EnrollmentActivity,
                            "EL CARNET DEL TRABAJADOR NO SE ENCUENTRA ACTIVO"
                    )
                }
                5 -> {
                    MetodosPublicos.alertDialog(
                            this@EnrollmentActivity,
                            "EL TRABAJADOR NO ESTA ACTIVO EN EL PROYECTO"
                    )
                }
                6 -> {
                    val builder = AlertDialog.Builder(this@EnrollmentActivity)
                            .setTitle("EL TRABAJADOR NO ESTA ACTIVO EN EL PROYECTO")
                            .setPositiveButton("SI") { dialog, _ ->
                                this@EnrollmentActivity.code = code
                                dialog.dismiss()
                                val intent = Intent(
                                        this@EnrollmentActivity,
                                        JoinPersonalProjectWizardActivity::class.java
                                )
                                intent.putExtra("companyId", companyInfoId)
                                intent.putExtra("projectId", projectId)
                                intent.putExtra("docNumber", documentNumber)
                                intent.putExtra("employerId", tempEmployerId)
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

                                startActivityForResult(intent, REQUEST_JOIN_PERSONAL)

                            }
                            .setNegativeButton("NO", null)
                            .setMessage("Desea vincularlo?")
                    builder.create().show()
                }
                7 -> {
                    MetodosPublicos.alertDialog(
                            this@EnrollmentActivity,
                            "EL TRABAJADOR NO ESTA ACTIVO EN EL PROYECTO"
                    )
                }
                8 -> {
                    MetodosPublicos.alertDialog(
                            this@EnrollmentActivity, "- Este carnet no se encuentra activo\n" +
                            "El trabajador esta activo en este proyecto con otro empleador"
                    )
                }
            }
        }
    }
    private suspend fun processValidity(cursor: Cursor) {

        when (validateVigency(cursor)) {
            0 ->{
                viewModel.verifyInorOut(documentReq!!,personalId.toLong())
                launchCheckReqPersonalFragment()
            }
            1 -> {
                val builder = AlertDialog.Builder(this@EnrollmentActivity)
                        .setTitle("Permitir paso")
                        .setPositiveButton("Aceptar", null)
                        .setMessage("No hay acceso a este proyecto")
                builder.create().show()
            }
            2 -> {
                if (descrpciontype!! == "Visitante") {
                    val inflater = layoutInflater
                    val view = inflater.inflate(R.layout.alert_dialog_visitor, null)
                    val builder = android.app.AlertDialog.Builder(this)
                    val butonAceptar = view.findViewById<TextView>(R.id.btn_ok)
                    val butonCancelar = view.findViewById<TextView>(R.id.btn_cancel)
                    builder.setView(view)
                    val alertDialog = builder.create()
                    alertDialog.show()
                    alertDialog.setCancelable(false)
                    butonCancelar.setOnClickListener {
                        alertDialog.dismiss()
                    }
                    butonAceptar.setOnClickListener {
                        @SuppressLint("SimpleDateFormat")
                        val format = SimpleDateFormat("yyyy-MM-dd'T'23:59:00")
                        val newdatefinish = format.format(Date())
                        val url = Constantes.LIST_CONTRACTS_URL + contractId + "/PersonalInfo"
                        val personalContract = PersonalContract(startDateJson, newdatefinish, personalId, null)
                        val gson = Gson()
                        val json = gson.toJson(personalContract)
                        CRUDService.startRequest(this, url, Request.Method.POST, json)
                        val cv = ContentValues()
                        cv.put("FinishDate", newdatefinish)
                        val where = arrayOf(projectId, personalId.toString())
                        repository.updatedatecontract(cv, where)
                        runBlocking {
                            validarcontratonuevo()
                        }
                        alertDialog.dismiss()
                    }
                } else {
                    val builder =
                            AlertDialog.Builder(this@EnrollmentActivity) //                    .setTitle("Vigencia del contrato")
                                    //                    .setMessage(contractValidity.Desc);
                                    .setTitle("Persona no activa en este proyecto")
                                    .setMessage("No se registra el ingreso")
                                    .setCancelable(false)
                                    .setPositiveButton("Aceptar") { dialogInterface, _ ->
                                        dialogInterface.dismiss()
                                    }
                    builder.create().show()
                }
            }
        }
    }

    suspend fun validarcontratonuevo() {
        val selection =
                ("(" + DBHelper.CONTRACT_PERSON_TABLE_COLUMN_PROJECT_ID + " = ?) and ("
                        + DBHelper.CONTRACT_PERSON_TABLE_COLUMN_DOC_NUM + " lIKE ?)")
        val selectionArgs = arrayOf(projectId!!, "%$documentNewDate%")
        contentResolver.query(
                Constantes.CONTENT_CONTRACT_PER_OFFLINE_URI,
                null,
                selection,
                selectionArgs,
                null
        )?.use { cursor ->
            processValidity(cursor)
        }
    }


    private suspend fun findPerson(document: String?): Cursor? {
        return withContext(Dispatchers.IO) {
            if (projectId == null || projectId!!.isEmpty()) { // processGeoJsonOffline();
                val intent = intent
                finish()
                startActivity(intent)
                return@withContext null
            }

            val selection =
                    "(" + DBHelper.CONTRACT_PERSON_TABLE_COLUMN_PROJECT_ID + " = ?) and (" + DBHelper.CONTRACT_PERSON_TABLE_COLUMN_DOC_NUM + " = ?)"
            val selectionArgs = arrayOf(projectId!!, document)

            contentResolver.query(
                    Constantes.CONTENT_CONTRACT_PER_OFFLINE_URI,
                    null,
                    selection,
                    selectionArgs,
                    null
            )?.let { cursorPersonal ->
                if (cursorPersonal.count == 0) {
                    return@withContext null
                } else return@withContext cursorPersonal
            }

            return@withContext null
        }
    }

    private suspend fun findPersonQR(code: String): Int {
        return withContext(Dispatchers.IO) {
            if (projectId == null || projectId!!.isEmpty()) { // processGeoJsonOffline();
                val intent = intent
                finish()
                startActivity(intent)
                return@withContext 1
            }

            var selection = ("( " + DBHelper.CONTRACT_PERSON_TABLE_COLUMN_CARNET_JSON + " lIKE ?)")
            var selectionArgs = arrayOf("%$code%")

            contentResolver.query(
                    Constantes.CONTENT_CONTRACT_PER_OFFLINE_URI,
                    null,
                    selection,
                    selectionArgs,
                    null
            )?.use { cursorPersonal ->
                if (!addPersonalFlag && (cursorPersonal.count <= 0 || !cursorPersonal.moveToFirst())) {
                    return@withContext 2
                }

                if (cursorPersonal.count <= 0 || !cursorPersonal.moveToFirst()) {
                    codeUrl = "$URL_VALIDATED_PERSONAL_CODE$code/$projectId"
                    CRUDService.startRequest(
                            this@EnrollmentActivity, codeUrl,
                            Request.Method.GET, "", false
                    )
                    return@withContext 3
                }

                var isActive = true
                cursorPersonal.moveToFirst()
                var carnetJsonStr =
                        cursorPersonal.getString(cursorPersonal.getColumnIndex(DBHelper.CONTRACT_PERSON_TABLE_COLUMN_CARNET_JSON))

                if (carnetJsonStr.isNotEmpty() && isJSONValid(carnetJsonStr)) {
                    val carnetJson = JSONArray(carnetJsonStr)
                    for (i in 0 until carnetJson.length()) {
                        if (carnetJson.getJSONObject(i).getString("f1") == code) {
                            isActive = carnetJson.getJSONObject(i).getBoolean("f3")
                            break
                        }
                    }
                }
                if (!isActive) {
                    return@withContext 4
                }

                cursorPersonal.moveToFirst()

                var personalInProject = false
                var employerId: String? = null
                var employerDbId: String? = null

                var docNumber: String
                do {

                    val projectIdDb =
                            cursorPersonal.getString(cursorPersonal.getColumnIndex(DBHelper.CONTRACT_PERSON_TABLE_COLUMN_PROJECT_ID))
                    docNumber =
                            cursorPersonal.getString(cursorPersonal.getColumnIndex(DBHelper.CONTRACT_PERSON_TABLE_COLUMN_DOC_NUM))
//                    val enterProject = cursorPersonal.getInt(cursorPersonal.getColumnIndex(DBHelper.CONTRACT_PERSON_TABLE_COLUMN_ENTERPROJECT))

                    logW("Code:$code, projectId: $projectId projectIdDb $projectIdDb")
                    if (projectId == projectIdDb) {

                        startDateStr =
                                cursorPersonal.getString(cursorPersonal.getColumnIndex(DBHelper.CONTRACT_PERSON_TABLE_COLUMN_START_DATE))
                        var finishDateStr =
                                cursorPersonal.getString(cursorPersonal.getColumnIndex(DBHelper.CONTRACT_PERSON_TABLE_COLUMN_FINISH_DATE))
                        val startDateContract =
                                cursorPersonal.getString(cursorPersonal.getColumnIndex(DBHelper.CONTRACT_PERSON_TABLE_COLUMN_START_DATE_CONTRACT))
                        val finishDateContract =
                                cursorPersonal.getString(cursorPersonal.getColumnIndex(DBHelper.CONTRACT_PERSON_TABLE_COLUMN_FINISH_DATE_CONTRACT))
                        carnetJsonStr =
                                cursorPersonal.getString(cursorPersonal.getColumnIndex(DBHelper.CONTRACT_PERSON_TABLE_COLUMN_CARNET_JSON))
                        employerDbId =
                                cursorPersonal.getString(cursorPersonal.getColumnIndex(DBHelper.CONTRACT_PERSON_TABLE_COLUMN_CONTRACTOR_ID))

                        if (startDateStr.isNullOrEmpty() || startDateStr == "null")
                            startDateStr = startDateContract

                        if (finishDateStr.isNullOrEmpty() || finishDateStr == "null")
                            finishDateStr = finishDateContract

                        @SuppressLint("SimpleDateFormat")
                        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                        val startDate = format.parse(startDateStr)
                        val finishDate = format.parse(finishDateStr)
                        val rightNow = Calendar.getInstance()

                        if (rightNow.time.after(startDate)) {
                            if (rightNow.time.before(finishDate)) {
                                personalInProject = true
                                break
                            }
                        }
                    }
                } while (cursorPersonal.moveToNext())

                if (!carnetJsonStr.isNullOrEmpty() && isJSONValid(carnetJsonStr)) {
                    val carnetJson = JSONArray(carnetJsonStr)
                    for (i in 0 until carnetJson.length()) {
                        if (carnetJson.getJSONObject(i).getString("f1") == code) {
                            employerId = carnetJson.getJSONObject(i).getString("f2")
                            logW("employerDbId $employerDbId employerId $employerId is active $isActive")
                            break
                        }
                    }
                }

                if (!addPersonalFlag && !personalInProject) {
                    return@withContext 5
                }

                if (!personalInProject && !employerId.isNullOrEmpty()) {

                    var tempEmployerId: String? = null
                    if (employerDbId.isNullOrBlank()) {
                        selection =
                                ("(" + DBHelper.EMPLOYER_TABLE_COLUMN_COMPANY + " = ?) and ( " + DBHelper.EMPLOYER_TABLE_COLUMN_COMPANY_ID + " = ?)")
                        selectionArgs = arrayOf(employerId, companyInfoId!!)
                        contentResolver.query(
                                Constantes.CONTENT_EMPLOYER_URI,
                                null,
                                selection,
                                selectionArgs,
                                null
                        )?.use { cursorEmployer ->
                            cursorEmployer.moveToFirst()
                            val employer = Gson().fromJson(
                                    Utils.cursorToJObject(cursorEmployer).toString(),
                                    Employer::class.java
                            )
                            tempEmployerId = employer.Id
                        }
                    }
                    documentNumber = docNumber
                    this@EnrollmentActivity.tempEmployerId = tempEmployerId

                    return@withContext 6
                } else if (!personalInProject) {
                    return@withContext 7
                }

                if (!employerId.isNullOrEmpty()) {

                    selection = ("(" + DBHelper.EMPLOYER_TABLE_COLUMN_COMPANY + " = ?)")
                    selectionArgs = arrayOf(employerId)
                    contentResolver.query(
                            Constantes.CONTENT_EMPLOYER_URI,
                            null,
                            selection,
                            selectionArgs,
                            null
                    )?.use { cursorEmployer ->
                        var isSameEmployer = false
                        while (cursorEmployer.moveToNext()) {
                            val employer = Gson().fromJson(
                                    Utils.cursorToJObject(cursorEmployer).toString(),
                                    Employer::class.java
                            )
                            logW("employerDbId $employerDbId employer: ${Gson().toJson(employer)}")
                            if (employerDbId == employer.Id) {
                                isSameEmployer = true
                                break
                            }
                        }
                        if (cursorEmployer.count == 0 || !isSameEmployer) {
                            return@withContext 8
                        }
                    }
                }
            }

            return@withContext 0
        }
    }

    private fun launchCheckReqPersonalFragment() {
        val intent = Intent(this, CheckReqsPersonalActivity::class.java)
        intent.putExtra(CheckReqsPersonalActivity.ARG_DOC_NUMBER, documentReq)
        intent.putExtra(CheckReqsPersonalActivity.ARG_PROJECT_ID, projectId)
        //intent.putExtra(CheckReqsPersonalActivity.ARG_STEP_ID, stepId);
//intent.putExtra(CheckReqsPersonalActivity.ARG_SERVICE, jsonObject.toString());
        intent.putExtra(CheckReqsPersonalActivity.ARG_NAME, namePersonal)
        intent.putExtra(CheckReqsPersonalActivity.ARG_PER_COMP_IF_ID, personalcompanyinfoId)
        intent.putExtra(CheckReqsPersonalActivity.ARG_LAST_NAME, lastNamePersonal)
        intent.putExtra(CheckReqsPersonalActivity.ARG_PHOTO, photoReq)
        intent.putExtra(CheckReqsPersonalActivity.ARG_PERSONAL_ID, personalId)
        intent.putExtra(CheckReqsPersonalActivity.ARG_CONTRACT_ID, contractId)
        intent.putExtra(CheckReqsPersonalActivity.ARG_PERSONAL_JOB, personalJob)
        intent.putExtra(CheckReqsPersonalActivity.ARG_CONTRACT_FINISH_DATE, contractFinishDate)
        intent.putExtra(CheckReqsPersonalActivity.ARG_CHECK_REQ, checkReq)
        intent.putExtra(CheckReqsPersonalActivity.ARG_INSPECT_REQ, inspectReq)
        intent.putExtra(CheckReqsPersonalActivity.ARG_REG_IN, registerIn)
        intent.putExtra(CheckReqsPersonalActivity.ARG_REG_OUT, registerOut)
        intent.putExtra(CheckReqsPersonalActivity.ARG_OPT_SCAN, optionScan)
        intent.putExtra(CheckReqsPersonalActivity.ARG_CLAIMS, claims)
        intent.putExtra(CheckReqsPersonalActivity.ARG_COMPANY_INFO_ID, companyInfoId)
        intent.putExtra(CheckReqsPersonalActivity.ARG_TYPE_CONTRACT, descrpciontype)
        intent.putExtra(CheckReqsPersonalActivity.ARG_EMPLOYER_CONTRACT, EmployerName)
        intent.putExtra(CheckReqsPersonalActivity.ARG_PERSON_GUID, personalGuiId)
        startActivityForResult(intent, 777)
    }

    private fun isJSONValid(param: String): Boolean {
        try {
            JSONObject(param)
        } catch (ex: JSONException) {

            try {
                JSONArray(param)
            } catch (ex1: JSONException) {
                return false
            }
        }
        return true
    }

    private suspend fun validateVigency(cursorPersonal: Cursor): Int {
        return withContext(Dispatchers.IO) {
//            var enterProject: Int
            var flagEnter: Boolean
            val document: String
            val contractValidity = Scheduler()
            contractValidity.Allow = true
            if (cursorPersonal.moveToFirst()) {
//                val enterProjectCursor = cursorPersonal.getColumnIndex(DBHelper.CONTRACT_PERSON_TABLE_COLUMN_ENTERPROJECT)
                val documentCursor =
                        cursorPersonal.getColumnIndex(DBHelper.CONTRACT_PERSON_TABLE_COLUMN_DOC_NUM)
                val startDatePersonal =
                        cursorPersonal.getColumnIndex(DBHelper.CONTRACT_PERSON_TABLE_COLUMN_START_DATE)
                val finishDatePersonal =
                        cursorPersonal.getColumnIndex(DBHelper.CONTRACT_PERSON_TABLE_COLUMN_FINISH_DATE)
                val startDateContrcat =
                        cursorPersonal.getColumnIndex(DBHelper.CONTRACT_PERSON_TABLE_COLUMN_START_DATE_CONTRACT)
                val finishDateContract =
                        cursorPersonal.getColumnIndex(DBHelper.CONTRACT_PERSON_TABLE_COLUMN_FINISH_DATE_CONTRACT)
                descrpciontype=
                        cursorPersonal.getString(cursorPersonal.getColumnIndex(DBHelper.CONTRACT_PERSON_TABLE_COLUMN_PERSONAL_TYPE))
                contractId =
                        cursorPersonal.getString(cursorPersonal.getColumnIndex(DBHelper.CONTRACT_PERSON_TABLE_COLUMN_CONTRACT_ID))
                personalId =
                        cursorPersonal.getInt(cursorPersonal.getColumnIndex(DBHelper.CONTRACT_PERSON_TABLE_COLUMN_PERSONALCOMPANY_ID))
                personalGuiId=
                        cursorPersonal.getString(cursorPersonal.getColumnIndex(DBHelper.CONTRACT_PERSON_TABLE_COLUMN_PERSONAL_ID))
                do {
//                    enterProject = cursorPersonal.getInt(enterProjectCursor)
//                    if (enterProject == 1) {
                    flagEnter = true
                    startDateJson = cursorPersonal.getString(startDatePersonal)
                    var finishDateJson = cursorPersonal.getString(finishDatePersonal)

                    if (startDateJson.isNullOrEmpty() || startDateJson == "null")
                        startDateJson = cursorPersonal.getString(startDateContrcat)

                    if (finishDateJson.isNullOrEmpty() || finishDateJson == "null")
                        finishDateJson = cursorPersonal.getString(finishDateContract)

                    if (startDateJson != null && finishDateJson != null) {
                        @SuppressLint("SimpleDateFormat") val format =
                                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                        try {
                            val startDate = format.parse(startDateJson)
                            val finishDate = format.parse(finishDateJson)
                            val rightNow = Calendar.getInstance()
                            if (rightNow.time.after(startDate)) {
                                if (rightNow.time.before(finishDate) ) {
                                    contractValidity.Allow = true
                                    contractValidity.Desc = "OK"
                                    EmployerId =
                                            cursorPersonal.getString(cursorPersonal.getColumnIndex(DBHelper.CONTRACT_PERSON_TABLE_COLUMN_CONTRACTOR_ID))
                                    getEmployer(EmployerId)
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
//                    } else {
//                        flagEnter = false
//                    }
                } while (cursorPersonal.moveToNext())
                if (!flagEnter) {

                    return@withContext 1
                } else if (!contractValidity.Allow) {

                    return@withContext 2
                } else {

                    document = cursorPersonal.getString(documentCursor)
                    personalId =
                            cursorPersonal.getInt(cursorPersonal.getColumnIndex(DBHelper.CONTRACT_PERSON_TABLE_COLUMN_PERSONALCOMPANY_ID))
                    contractId =
                            cursorPersonal.getString(cursorPersonal.getColumnIndex(DBHelper.CONTRACT_PERSON_TABLE_COLUMN_CONTRACT_ID))
                    personalJob =
                            cursorPersonal.getString(cursorPersonal.getColumnIndex(DBHelper.CONTRACT_PERSON_TABLE_COLUMN_POSITION))
                    contractFinishDate =
                            cursorPersonal.getString(cursorPersonal.getColumnIndex(DBHelper.CONTRACT_PERSON_TABLE_COLUMN_FINISH_DATE))

                    personalcompanyinfoId =
                        cursorPersonal.getInt(cursorPersonal.getColumnIndex(DBHelper.PERSONAL_TABLE_COLUMN_PERSONAL_COMPANY_ID))
                    if (contractFinishDate == null || contractFinishDate!!.isEmpty()) contractFinishDate =
                            cursorPersonal.getString(cursorPersonal.getColumnIndex(DBHelper.CONTRACT_PERSON_TABLE_COLUMN_FINISH_DATE_CONTRACT))

                    log("personalId $personalId")

                    contentResolver.query(
                        Uri.parse("content://" + Constantes.AUTHORITY + "/" + DBHelper.TABLE_NAME_PERSONAL + "/" + personalId),
                        null, null, null, null
                    )?.use { cursor ->
                        if (cursor.count == 0) {
                            LoadProjectByids(projectId!!)
                            Thread.sleep(10000)
                        }
                    }

                    contentResolver.query(
                            Uri.parse("content://" + Constantes.AUTHORITY + "/" + DBHelper.TABLE_NAME_PERSONAL + "/" + personalId),
                            null, null, null, null
                    )?.use { cursor ->
                        if (cursor.count > 0 && cursor.moveToFirst()) { // cursor.moveToFirst();
                            val name =
                                    cursor.getString(cursor.getColumnIndex(DBHelper.PERSONAL_TABLE_COLUMN_NAME))
                            val lastName =
                                    cursor.getString(cursor.getColumnIndex(DBHelper.PERSONAL_TABLE_COLUMN_LASTNAME))
                            val photo =
                                    cursor.getString(cursor.getColumnIndex(DBHelper.PERSONAL_TABLE_COLUMN_PHOTO))
                            namePersonal = name
                            lastNamePersonal = lastName
                            documentReq = document
                            photoReq = photo

                            return@withContext 0

                        } else return@withContext 1
                    }

                }

            }
            0
        }
    }

    private fun getEmployer(employerId: String?) {
        val Nombre = arrayOf(DBHelper.EMPLOYER_TABLE_COLUMN_NAME)
        val args = arrayOf(employerId)
        val cursor : Cursor = mSqLiteDatabase?.query(DBHelper.TABLE_NAME_EMPLOYER,Nombre,"Id=?",args,null,null,null)!!
        if(cursor.moveToFirst()){
            EmployerName = cursor.getString(0)

        }
    }

    /**
     * Shows the progress UI and hides the login form.
     */
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


    fun onClickOption(v: View) {

        when (v.id) {
            R.id.option1Button -> {
                if (checkReq) {
                    checkReq = false
                    v.backgroundTintList =
                            ContextCompat.getColorStateList(this, R.color.primary_dark)
                } else {
                    viewModel.speak(getString(R.string.speak_verificar))
                    checkReq = true
                    v.backgroundTintList =
                            ContextCompat.getColorStateList(this, R.color.colorAccent)
                }
                preferences.setVerifyAdnRegisterOptions(checkReq, 1)
            }
            R.id.option2Button -> {
                if (inspectReq) {
                    inspectReq = false
                    v.backgroundTintList =
                            ContextCompat.getColorStateList(this, R.color.primary_dark)
                } else {
                    viewModel.speak(getString(R.string.speak_inspeccionar))
                    inspectReq = true
                    v.backgroundTintList =
                            ContextCompat.getColorStateList(this, R.color.colorAccent)
                }
                preferences.setVerifyAdnRegisterOptions(inspectReq, 2)
            }
            R.id.option3Button -> {
                if (registerIn) {
                    registerIn = false
                    v.backgroundTintList =
                            ContextCompat.getColorStateList(this, R.color.primary_dark)
                } else {
                    viewModel.speak(getString(R.string.speak_in))
                    registerIn = true
                    v.backgroundTintList =
                            ContextCompat.getColorStateList(this, R.color.colorAccent)
                }
                preferences.setVerifyAdnRegisterOptions(registerIn, 3)
            }
            R.id.option4Button -> {
                if (registerOut) {
                    registerOut = false
                    v.backgroundTintList =
                            ContextCompat.getColorStateList(this, R.color.primary_dark)
                } else {
                    viewModel.speak(getString(R.string.speak_out))
                    registerOut = true
                    v.backgroundTintList =
                            ContextCompat.getColorStateList(this, R.color.colorAccent)
                }
                preferences.setVerifyAdnRegisterOptions(registerOut, 4)
            }
        }
        if (!flagStart) {
            if (!checkReq && !inspectReq && !registerIn && !registerOut) {
                val builder = AlertDialog.Builder(this@EnrollmentActivity)
                        .setTitle("Sin Selección")
                        .setCancelable(false)
                        .setPositiveButton("OK") { dialog, which ->

                            checkReq = true
                            if (isEnabledButtons) binding.option1Button.backgroundTintList =
                                    ContextCompat.getColorStateList(
                                            this,
                                            R.color.colorAccent
                                    ) else binding.option1Button.backgroundTintList =
                                    ContextCompat.getColorStateList(this, R.color.colorGreenDisabled)
                            permissions()
                        }
                        .setMessage("No ha seleccionado ninguna opción de procedimiento")
                builder.create().show()
            }
        }
        flagStart = false

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1) {
            val intent = intent
            finish()
            startActivity(intent)

        } else if (requestCode == REQUEST_JOIN_PERSONAL || requestCode == CREATE_PERSONAL_CODE) {

            if (resultCode == Activity.RESULT_OK) {
                data?.apply {
                    val personalInfoId = getIntExtra("personalInfoId", 0)
                    viewModel.loadPersonalContractOffline(personalInfoId.toLong())

                }
            }
        } else if (requestCode == 777) {
            isEnabledButtons = preferences.enabledErollmentButtons
            checkReq = false
            inspectReq = false
            registerIn = false
            registerOut = false
            if (data != null && data.extras != null) {
                checkReq = data.extras!!.getBoolean(CheckReqsPersonalActivity.ARG_CHECK_REQ)
                inspectReq = data.extras!!.getBoolean(CheckReqsPersonalActivity.ARG_INSPECT_REQ)
                registerIn = data.extras!!.getBoolean(CheckReqsPersonalActivity.ARG_REG_IN)
                registerOut = data.extras!!.getBoolean(CheckReqsPersonalActivity.ARG_REG_OUT)
                preferences.setVerifyAdnRegisterOptions(checkReq, 1)
                preferences.setVerifyAdnRegisterOptions(inspectReq, 2)
                preferences.setVerifyAdnRegisterOptions(registerIn, 3)
                preferences.setVerifyAdnRegisterOptions(registerOut, 4)
                enabledButtons()
            }
            when (resultCode) {
                REQUEST_SCAN -> {
                    val intent = Intent(this, BarcodeDecodeSerActivity::class.java)
                    intent.putExtra("isFlash", isFlash)
                    startActivityForResult(intent, 0)
                }
                REQUEST_WRITE -> {
                }
                else -> {
                }
            }
        } else {
            when (resultCode) {
                BarcodeDecodeSerActivity.SUCCESS -> if (data != null && data.hasExtra("barcode")) {
                    val barcodeRes = data.getStringExtra("barcode")
                    includeInternalLayout.readBarcode.isEnabled = true
                    if (barcodeRes == null || barcodeRes.isEmpty()) {
                        MetodosPublicos.alertDialog(
                                this@EnrollmentActivity,
                                "No se pudo procesar el codigo de barras, intente de nuevo."
                        )
                        return
                    }
                    when (data.getIntExtra("barcode-type", 0)) {
                        Barcode.QR_CODE -> onApplyScanDocQR(barcodeRes)
                        Barcode.PDF417 -> processBarcodePDF417(barcodeRes)
                    }
                }
            }
        }
    }

    private fun processBarcodePDF417(barcodeRes: String) {
        val processCode = DecodeBarcode.processBarcode(barcodeRes)
        if (processCode.isNotEmpty()) {
            includeInternalLayout.tvScanButtonError.error = null
            //readBarcode.setError(null);
            val infoUser = Gson().fromJson(processCode, InfoUser::class.java)
            onApplyScan(infoUser)
        } else {
            MetodosPublicos.alertDialog(
                    this@EnrollmentActivity,
                    "No se pudo procesar el codigo de barras, intente de nuevo."
            )
        }
    }

    private fun clearText() {
        includeInternalLayout.document.setText("")
    }
    private fun LoadProjectByids(projecids:String) {
        CRUDService.startRequest(
                this, Constantes.LIST_PROJECTS_URL+projecids,
                Request.Method.GET, "", false
        )
        //viewModel.changeStatus(LoadingApiStatus.LOADING)
    }
    private fun loadDataFromServer(PersonalIds:String,EmployerIds:String,ContractIds:String) {
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
        data!![Constantes.CONTENT_CONTRACT_URI] = Constantes.CONTRACT_BY_ARRAYIDS
        data!![Constantes.CONTENT_PERSONAL_URI] = Constantes.PERSONAL_BY_ARRAYIDS
        data!![Constantes.CONTENT_PROJECT_URI] = Constantes.LIST_PROJECTS_URL
        data!![Constantes.CONTENT_CONTRACT_PER_OFFLINE_URI] =
            Constantes.LIST_CONTRACT_PER_OFFLINE_URL
        data!![Constantes.CONTENT_EMPLOYER_URI] = Constantes.EMPLOYER_BY_ARRAYIDS
        fillArray = BooleanArray(data!!.size)
        launchRequests(PersonalIds,EmployerIds,ContractIds)
    }

    private fun launchRequests(PersonalIds:String,EmployerIds:String,ContractIds:String) {
        for ((pos, o) in data!!.entries.withIndex()) {
            val pair = o as Map.Entry<*, *>
            val values = ContentValues()
            when (pair.value) {
                Constantes.LIST_CONTRACT_PER_OFFLINE_URL -> {
                    //                viewModel.deleteAllContractPerson(projectId)
                    values.put(Constantes.KEY_SELECT, true)
                    values.put("project", true)
                    values.put("projectId", projectId)
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
                Constantes.EMPLOYER_BY_ARRAYIDS -> {
                    values.put("&projectId",projectId)
                    values.put("?Ids", EmployerIds)
                    CRUDService.startRequest(
                            this, pair.value as String?,
                            Request.Method.GET, values.toString(), true,EmployerIds
                    )
                }
                Constantes.CONTRACT_BY_ARRAYIDS -> {
                    values.put("&projectId",projectId)
                    values.put("?Ids", ContractIds)
                    CRUDService.startRequest(
                            this, pair.value as String?,
                            Request.Method.GET, values.toString(), true,ContractIds
                    )
                }
                Constantes.LIST_PROJECTS_URL -> {
                    values.put(Constantes.KEY_SELECT, true)
                    val paramsQuery = HttpRequest.makeParamsInUrl(values)
                    CRUDService.startRequest(
                            this, pair.value as String?,
                            Request.Method.GET, paramsQuery, true
                    )
                }
            }
            co.com.sersoluciones.facedetectorser.utilities.DebugLog.logW(pos.toString() + ". URL to Request: " + pair.value + ": " + fillArray[pos])
            //it.remove();
        }
    }
    private fun verifyInDb(): Boolean {
        val data = LinkedHashMap<Uri, String>()
        data[Constantes.CONTENT_CONTRACT_URI] = Constantes.CONTRACT_BY_ARRAYIDS
        data[Constantes.CONTENT_PERSONAL_URI] = Constantes.PERSONAL_BY_ARRAYIDS
        data[Constantes.CONTENT_PROJECT_URI] = Constantes.LIST_PROJECTS_URL
        data[Constantes.CONTENT_CONTRACT_PER_OFFLINE_URI] = Constantes.LIST_CONTRACT_PER_OFFLINE_URL
        data[Constantes.CONTENT_EMPLOYER_URI] = Constantes.EMPLOYER_BY_ARRAYIDS
        val fillArray = BooleanArray(data.size)
        var pos = 0
        for (o in data.entries) {
            val pair = o as Map.Entry<*, *>
            var selection: String? = null
            var selectionArgs: Array<String>? = null
            if (pair.value == Constantes.LIST_CONTRACT_PER_OFFLINE_URL && projectId != null) {
                selection = ("( " + DBHelper.CONTRACT_PERSON_TABLE_COLUMN_PROJECT_ID + " = ?)")
                selectionArgs = arrayOf(projectId!!)
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
    @SuppressLint("SimpleDateFormat")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStringResult(action: String?, option: Int, jsonObjStr: String?, url: String?) {
        when (option) {
            Constantes.SUCCESS_REQUEST -> {
                when (url) {
                    Constantes.LIST_PROJECTS_URL+projectId ->{
                        val project = JSONObject(jsonObjStr)
                        val contractids = project.getString("ContractIds")
                        val personalids=project.getString("PersonalIds")
                        val employerids=project.getString("EmployerIds")
                        replace = contractids.replace("\\[|]|\\s".toRegex(), "").replace("""[""]""".toRegex(), "")
                        replacepersonalIds = personalids.replace("\\[|]|\\s".toRegex(), "")
                        replaceEmployerIds = employerids.replace("\\[|]|\\s".toRegex(), "").replace("""[""]""".toRegex(), "")
                        loadDataFromServer(replacepersonalIds!!, replaceEmployerIds!!, replace!!)
                    }
                    Constantes.PERSONAL_BY_ARRAYIDS -> {
                        co.com.sersoluciones.facedetectorser.utilities.DebugLog.log("tabla llena PERSONAL")
                        fillArray[2] = true
                    }
                    Constantes.LIST_PROJECTS_URL -> {
                        co.com.sersoluciones.facedetectorser.utilities.DebugLog.log("tabla llena LIST_PROJECTS_URL")
                        fillArray[0] = true
                        loadProject = true
                    }
                    Constantes.LIST_CONTRACT_PER_OFFLINE_URL -> {
                        co.com.sersoluciones.facedetectorser.utilities.DebugLog.log("tabla llena LIST_CONTRACT_PER_OFFLINE_URL")
                        fillArray[1] = true
                    }
                    Constantes.CONTRACT_BY_ARRAYIDS -> {
                        co.com.sersoluciones.facedetectorser.utilities.DebugLog.log("tabla llena CONTRACT")
                        fillArray[3] = true

                    }
                    Constantes.EMPLOYER_BY_ARRAYIDS -> {
                        co.com.sersoluciones.facedetectorser.utilities.DebugLog.log("tabla llena EMPLOYER")
                        fillArray[4] = true
                    }
                    codeUrl -> {
                        dialogJoinPersonal(jsonObjStr ?: "")
                    }

                }
                if(verifyFillData()){
                    if(preferences.synContract && preferences.synPersonal && preferences.synEmployer && preferences.synContractOffline){
                        preferences.syncDate = Calendar.getInstance().time.time
                        val sdf = SimpleDateFormat("yyyy-MM-dd hh:mm:ss")
                        val currentDate = sdf.format(Date())
                        Toast.makeText(
                                this@EnrollmentActivity,
                                "Sincronización de datos exitosa",
                                Toast.LENGTH_SHORT
                        ).show()
                        viewModel.insertProjectsSyn(projectId,projectselect,currentDate.toString())
                    }else{
                        Toast.makeText(
                                this@EnrollmentActivity,
                                "La sincronización de datos fallo, por favor vuelva a sincronizar",
                                Toast.LENGTH_SHORT
                        ).show()
                    }
                    binding.syncButton.clearAnimation()
                }
//                if (code.isNotEmpty()) {
//                    showProgress(false)
//                    findPersonQR(code)
//                    code = ""
//                }
            }
            Constantes.NOT_INTERNET, Constantes.BAD_REQUEST, Constantes.TIME_OUT_REQUEST, Constantes.NOT_CONNECTION -> {
                if (url == codeUrl) {
                    jsonObjStr?.let {
                        val infoJobj = JSONObject(jsonObjStr)
                        val infoUser = InfoUser()
                        infoUser.dni = infoJobj["DocumentNumber"].toString().toLong()
                        infoUser.name = infoJobj["Name"].toString()
                        infoUser.lastname = infoJobj["LastName"].toString()
                        infoUser.birthDate = infoJobj["BirthDate"].toString().toInt()
                        infoUser.documentType = infoJobj["DocumentType"].toString()
                        infoUser.rh = infoJobj["RH"].toString()
                        infoUser.sex = infoJobj["Sex"].toString()
                        infoUser.cityOfBirthCode = infoJobj["CityOfBirthCode"].toString()
                        logW("infoUser ${Gson().toJson(infoUser)}")
                        dialogPersonalNotFound(infoUser)
                    }

                } else {
                    log("Sin internet")
                    Snackbar.make(
                            findViewById(android.R.id.content),
                            "Sin conexion con el servidor. No se pudo sincronizar los datos",
                            Snackbar.LENGTH_LONG
                    )
                            .show()
                    Toast.makeText(this, "", Toast.LENGTH_SHORT).show()
                    fillArray[0] = true
                    fillArray[1] = true
                    fillArray[2] = true
                    fillArray[3] = true
                    fillArray[4] = true
                    loadProject = true
                    verifyFillData()
                }
            }
            Constantes.UNAUTHORIZED -> {
//                finish()
            }
            Constantes.FORBIDDEN -> {
                Toast.makeText(
                        this,
                        "Este usuario no tiene permisos para esta accion",
                        Toast.LENGTH_SHORT
                ).show()
                viewModel.clearFormData()
                finish()
            }
            Constantes.REQUEST_NOT_FOUND -> {
                if (url == codeUrl) {
                    dialogPersonalNotFound()
                }
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
    private fun permissions() {
        claims = AppPermissions.getPermissionsOfUser(this, companyAdminId = companyAdminId)
        setSelectetButtons()
        validate(claims)
    }
    private fun validate(permissions: ArrayList<String>) {
        if (permissions.contains("enrollments.validaterequirements")
                || permissions.contains("enrollments.inspectrequirements")
                || permissions.contains("enrollments.verifyenterexit")
        ) {
            findViewById<View>(R.id.scan_view).visibility = View.VISIBLE
            findViewById<View>(R.id.alertPermissions2).visibility = View.GONE
        }
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
        if (permissions.contains("enrollments.addpersonal")) addPersonalFlag = true

        if (!permissions.contains("enrollments.enableeditdoc")) includeInternalLayout.digitacionGroup.visibility =
                View.GONE

        if (!permissions.contains("enrollments.registers")) {
            binding.bottomSheet.visibility = View.GONE
            viewModel.stopSignalR()
        }
    }

    private fun setSelectetButtons() {
        if (preferences.getVerifyAdnRegisterOption(1)) {
            checkReq = true
            if (isEnabledButtons) binding.option1Button.backgroundTintList =
                    ContextCompat.getColorStateList(
                            this,
                            R.color.colorAccent
                    ) else binding.option1Button.backgroundTintList =
                    ContextCompat.getColorStateList(this, R.color.colorGreenDisabled)
        }
        if (preferences.getVerifyAdnRegisterOption(2)) {
            inspectReq = true
            if (isEnabledButtons) binding.option2Button.backgroundTintList =
                    ContextCompat.getColorStateList(
                            this,
                            R.color.colorAccent
                    ) else binding.option2Button.backgroundTintList =
                    ContextCompat.getColorStateList(this, R.color.colorGreenDisabled)
        }
        if (preferences.getVerifyAdnRegisterOption(3)) {
            registerIn = true
            if (isEnabledButtons) binding.option3Button.backgroundTintList =
                    ContextCompat.getColorStateList(
                            this,
                            R.color.colorAccent
                    ) else binding.option3Button.backgroundTintList =
                    ContextCompat.getColorStateList(this, R.color.colorGreenDisabled)
        }
        if (preferences.getVerifyAdnRegisterOption(4)) {
            registerOut = true
            if (isEnabledButtons) binding.option4Button.backgroundTintList =
                    ContextCompat.getColorStateList(
                            this,
                            R.color.colorAccent
                    ) else binding.option4Button.backgroundTintList =
                    ContextCompat.getColorStateList(this, R.color.colorGreenDisabled)
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

    private fun enableComponents(enable: Boolean) {
        binding.option1Button.isEnabled = enable
        binding.option2Button.isEnabled = enable
        binding.option3Button.isEnabled = enable
        binding.option4Button.isEnabled = enable

        includeInternalLayout.switchLight.isEnabled = enable
        includeInternalLayout.document.isEnabled = enable
        includeInternalLayout.readBarcode.isEnabled = enable

        enabledButtons()
    }

    private val enabledSyncButton = Runnable {
        binding.syncButton.isEnabled = true
    }

    private fun dialogPersonalNotFound(infoUser: InfoUser? = null) {
        val builder = AlertDialog.Builder(this@EnrollmentActivity)
                .setTitle("EL TRABAJADOR NO ESTA ACTIVO EN EL PROYECTO")
                .setPositiveButton("SI") { dialog, _ ->
                    val intent = Intent(this@EnrollmentActivity, CreatePersonalActivity::class.java)
                    infoUser?.let {
                        intent.putExtra("documentNumber", infoUser.dni.toString())
                        intent.putExtra("infoUser", infoUser)
                    }
                    intent.putExtra("projectId", projectId)
                    startActivityForResult(intent, CREATE_PERSONAL_CODE)
                    dialog.dismiss()
                }
                .setNegativeButton("NO", null)
                .setMessage("Desea crearlo?") //O este carnet fue reemplazado.
        builder.create().show()
    }

    private fun dialogJoinPersonal(data: String) {
        if (data == "") return
        val jsonPersonal = JSONObject(data)
        if (!jsonPersonal.getBoolean("IsActiveCarnet")) {
            MetodosPublicos.alertDialog(
                    this@EnrollmentActivity,
                    getString(R.string.dialog_error_scan)
            )
            return
        }

        val builder = AlertDialog.Builder(this@EnrollmentActivity)
                .setTitle("EL TRABAJADOR NO ESTA ACTIVO EN EL PROYECTO")
                .setPositiveButton("SI") { dialog, _ ->
//                this.code = code
                    this.documentNumber = jsonPersonal.getString("DocumentNumber")
                    dialog.dismiss()
                    val intent =
                            Intent(this@EnrollmentActivity, JoinPersonalProjectWizardActivity::class.java)
                    intent.putExtra("companyId", companyInfoId)
                    intent.putExtra("projectId", projectId)
                    intent.putExtra("docNumber", jsonPersonal.getString("DocumentNumber"))
                    intent.putExtra("employerId", jsonPersonal.getString("EmployerId"))
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

                    startActivityForResult(intent, REQUEST_JOIN_PERSONAL)
                }
                .setNegativeButton("NO", null)
                .setMessage("Desea vincularlo?")
        builder.create().show()
    }

    private val locationReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent!!.hasExtra("location")) {
                val location = intent.getParcelableExtra<Location>("location")
                val currentLocation = LatLng(location!!.latitude, location.longitude)

                val selection = "(" + DBHelper.PROJECT_TABLE_COLUMN_ACTIVE + " = ? )"
                val selectionArgs = arrayOf("1")
                if (loadProject) {
//                    showProgress(false)
//                    logW("currentLocation ${currentLocation.latitude}")
                    viewModel.getProjects(
                            currentLocation,
                            selection = selection,
                            selectionArgs = selectionArgs
                    )

                }
            }

        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun showAlertLoad() {
        if (!isFinishing) {
            if (dialog != null && dialog!!.isShowing) return
            dialogLoad?.dismiss()
            val builder = AlertDialog.Builder(this@EnrollmentActivity)
                    .setTitle("Sin Datos")
                    .setCancelable(false)
                    .setMessage("Por favor sincronice los datos con el servidor para el correcto funcionamiento del modulo")
                    .setPositiveButton("Sincronizar") { dialog, _ ->
                        var animation : Animation = AnimationUtils.loadAnimation(this,R.anim.rotatesync)
                        binding.syncButton.startAnimation(animation)
                        enableComponents(false)
                        UpdateDBService.startRequest(this, false)
                        projectId?.let {
                            LoadProjectByids(projectId!!)
                            preferences.selectedProjectId = projectId

                        }
                        //loadDataFromServer()
                        dialog.dismiss()
                    }
            dialogLoad = builder.create()
            dialogLoad?.show()
        }
    }

    private fun verifyPersonalRegistros() {
        projectId?.let {

            viewModel.syncSignalR(projectId!!)
            viewModel.updatePersonalByProject(projectId!!)
        }
    }
    private fun sendreport(){
        GlobalScope.launch(Dispatchers.IO) {
            val reports = repositoryAnalityco.getAllReportes()
            loop@ for (report in reports) {

                when (repositoryAnalityco.sendReporte(report)) {
                    ApiStatus.ERROR -> {
                        co.com.sersoluciones.barcodedetectorser.utilities.DebugLog.logE("---------- reporte no enviado y puesto en un worker para futuro envio -> $report.id")
                    }
                    ApiStatus.DONE -> {
                        log("reporte borrado exitosamente ${report.id}")
                        repositoryAnalityco.deleteReporte(report.id)
                    }
                }
            }
        }
    }
    companion object {
        const val REQUEST_SCAN = 1
        const val REQUEST_WRITE = 2
    }
}
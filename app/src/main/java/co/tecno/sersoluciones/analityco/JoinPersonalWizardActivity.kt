package co.tecno.sersoluciones.analityco

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.TargetApi
import android.app.Activity
import android.content.ContentValues
import android.content.IntentFilter
import android.content.pm.ActivityInfo
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.MenuItem
import android.view.View
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import androidx.appcompat.app.AlertDialog
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import butterknife.BindView
import butterknife.OnClick
import co.com.sersoluciones.barcodedetectorser.utilities.DebugLog
import co.tecno.sersoluciones.analityco.adapters.personal.WizardPersonalPagerAdapter
import co.tecno.sersoluciones.analityco.callback.OnListEmployerInteractionListener
import co.tecno.sersoluciones.analityco.databases.DBHelper
import co.tecno.sersoluciones.analityco.fragments.personal.wizard.*
import co.tecno.sersoluciones.analityco.fragments.personal.wizard.CreatePersonalFragment.OnNewUserListener
import co.tecno.sersoluciones.analityco.fragments.personal.wizard.NewOtherPersonFragment.OnCreateOtherUserListener
import co.tecno.sersoluciones.analityco.fragments.personal.wizard.ScanPersonalFragmnet.OnScanListener
import co.tecno.sersoluciones.analityco.fragments.personal.wizard.SelectContractFragment.OnSelectContractListener
import co.tecno.sersoluciones.analityco.models.*
import co.tecno.sersoluciones.analityco.receivers.RequestBroadcastReceiver
import co.tecno.sersoluciones.analityco.receivers.RequestBroadcastReceiver.BroadcastListener
import co.tecno.sersoluciones.analityco.services.CRUDService
import co.tecno.sersoluciones.analityco.utilities.Constantes
import co.tecno.sersoluciones.analityco.utilities.DecodeBarcode.InfoUser
import co.tecno.sersoluciones.analityco.utilities.HttpRequest
import co.tecno.sersoluciones.analityco.utilities.MetodosPublicos
import co.tecno.sersoluciones.analityco.utilities.Utils.cursorToJObject
import co.tecno.sersoluciones.analityco.views.CustomViewPager
import com.android.volley.Request
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.json.JSONException
import org.json.JSONObject
import java.util.*

class JoinPersonalWizardActivity : BaseActivity(), BroadcastListener, OnNewUserListener, OnSelectContractListener,
    JoinPersonalContractFragment.OnJoinContractListener, OnItemSelectedListener,
    OnCreateOtherUserListener, OnListEmployerInteractionListener, OnPageChangeListener, OnScanListener {
    @JvmField
    @BindView(R.id.progress)
    var mProgress: ProgressBar? = null

    @JvmField
    @BindView(R.id.progressView)
    var mProgressView: LinearLayout? = null

    @JvmField
    @BindView(R.id.progressLayout)
    var mProgressLayout: LinearLayout? = null

    @JvmField
    @BindView(R.id.previous_button)
    var previousButton: Button? = null

    @JvmField
    @BindView(R.id.next_button)
    var nextButton: Button? = null

    @JvmField
    @BindView(R.id.submit_button)
    var submitButton: Button? = null

    @JvmField
    @BindView(R.id.register_view_pager)
    var mViewPager: CustomViewPager? = null

    @JvmField
    @BindView(R.id.form)
    var mFormView: View? = null

    @JvmField
    @BindView(R.id.image_bg)
    var imageViewBg: ImageView? = null

    @JvmField
    @BindView(R.id.spinnerBase)
    var spinnerProjects: Spinner? = null

    @JvmField
    @BindView(R.id.layoutSpinner)
    var linearLayoutSpinner: LinearLayout? = null

    @JvmField
    @BindView(R.id.controls)
    var linearLayoutControls: LinearLayout? = null
    private var posPage = 0
    private var mSectionsPagerAdapter: WizardPersonalPagerAdapter? = null
    private var companyId: String? = null
    var contractType: ContractType? = null
        private set
    private var requestBroadcastReceiver: RequestBroadcastReceiver? = null
    private var infoUser: InfoUser? = null
    private var personal: Personal? = null
    private var joinPersonalContract: String? = null
    private var selectedContract: ContractEnrollment? = null
    private var positionList: ArrayList<Position>? = null
    private var personalContract: PersonalContract? = null
    private var moveToPersonal = false
    private var endWizard = false
    private var selectedContractTypeValue: String? = null
    private var fillArray: BooleanArray = booleanArrayOf()
    private var data: LinkedHashMap<Uri, String>? = null
    private var contractId: String? = ""
    private var doc: String? = ""
    private var typePermission: String? = "personal"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        DebugLog.log("ON CREATE")
        savedInstanceState?.let { onRestoreInstanceState(it) }
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        requestBroadcastReceiver = RequestBroadcastReceiver(this)
        joinPersonalContract = ""
        selectedContractTypeValue = ""
        personalContract = null
        endWizard = false
        positionList = ArrayList()
        mViewPager = findViewById(R.id.register_view_pager)
        mViewPager!!.setPagingEnabled(false)
        moveToPersonal = false
        companyId = ""
        imageViewBg!!.setImageResource(R.drawable.personal)
        linearLayoutSpinner!!.visibility = View.GONE
        submitButton!!.visibility = View.GONE
        previousButton!!.isEnabled = false
        nextButton!!.visibility = View.VISIBLE
        loadModules()
        val extras = intent.extras
        if (extras != null) {
            contractId = extras.getString("contractId")
            co.com.sersoluciones.facedetectorser.utilities.DebugLog.logW("id contrato $contractId")
            selectedContractTypeValue = extras.getString("contractType")
            companyId = extras.getString("companyInfoId")
            infoUser = InfoUser()
            if (extras.containsKey("docNumber")) {
                doc = extras.getString("docNumber")
                infoUser!!.dni = java.lang.Long.valueOf(doc!!)
            }
            if (extras.containsKey("typePermission")) typePermission = extras.getString("typePermission")
            val selection = ("(" + DBHelper.COPTIONS_COLUMN_TYPE + " LIKE ? "
                    + " AND " + DBHelper.COPTIONS_COLUMN_VALUE + " LIKE ? "
                    + ")")
            val item = String.format("%s", selectedContractTypeValue)
            val selectionArgs = arrayOf("ContractType", item)
            val cursor = contentResolver.query(
                Constantes.CONTENT_COMMON_OPTIONS_URI, null,
                selection, selectionArgs, null
            )
            if (cursor != null && cursor.count > 0) {
                cursor.moveToFirst()
                val typeStr = cursorToJObject(cursor).toString()
                contractType = Gson().fromJson(typeStr, ContractType::class.java)
                cursor.close()
            }
        }
        if (loadDataFromServer()) {
            if (infoUser!!.dni > 0) {
                CRUDService.startRequest(
                    this@JoinPersonalWizardActivity,
                    Constantes.PERSONALCOMPANY_URL + infoUser!!.dni + "/" + companyId, Request.Method.GET
                )
                showProgress(true)
            }
        } else showProgress(true)
    }

    private fun loadDataFromServer(): Boolean {
        val values = ContentValues()
        values.put(Constantes.KEY_SELECT, true)
        val paramsQuery = HttpRequest.makeParamsInUrl(values)
        CRUDService.startRequest(
            this, Constantes.LIST_CONTRACTS_URL,
            Request.Method.GET, paramsQuery, true
        )
        data = LinkedHashMap()
        data!![Constantes.CONTENT_EMPLOYER_URI] = Constantes.LIST_EMPLOYERS_URL
        fillArray = BooleanArray(data!!.size)
        return verifyInDb()
    }

    private fun verifyInDb(): Boolean {
        val values = ContentValues()
        values.put(Constantes.KEY_SELECT, true)
        val paramsQuery = HttpRequest.makeParamsInUrl(values)
        var pos = 0
        for (o in data!!.entries) {
            val pair = o as Map.Entry<*, *>
            val cursor = contentResolver.query(
                (pair.key as Uri?)!!, null, null, null,
                null
            )
            if (cursor != null && cursor.count == 0) {
                CRUDService.startRequest(
                    this, pair.value as String?,
                    Request.Method.GET, paramsQuery, true
                )
            } else {
                fillArray[pos] = true
            }
            co.com.sersoluciones.facedetectorser.utilities.DebugLog.logW(pos.toString() + ". URL to Request: " + pair.value + ": " + fillArray[pos])
            cursor!!.close()
            pos++
            //it.remove();
        }
        var flag = true
        for (item in fillArray) {
            if (!item) {
                flag = true
                break
            }
        }
        return flag
    }

    override fun attachLayoutResource() {
        super.setChildLayout(R.layout.activity_personal_wizard)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                //NavUtils.navigateUpFromSameTask(this);
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onResume() {
        super.onResume()
        val intentFilter = IntentFilter()
        intentFilter.addAction(CRUDService.ACTION_REQUEST_GET)
        intentFilter.addAction(CRUDService.ACTION_REQUEST_SAVE)
        LocalBroadcastManager.getInstance(this).registerReceiver(
            requestBroadcastReceiver!!,
            intentFilter
        )
    }

    override fun onPause() {
        super.onPause()
        DebugLog.log("ON PAUSE")
        LocalBroadcastManager.getInstance(this).unregisterReceiver(requestBroadcastReceiver!!)
        //if (LocationUpdateService.isRunning())
    }

    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        savedInstanceState.putSerializable("infoUser", infoUser)
        savedInstanceState.putString("companyId", companyId)
        savedInstanceState.putSerializable("contractType", contractType)
        //        savedInstanceState.putInt("pos", posPage);
        super.onSaveInstanceState(savedInstanceState)
    }

    public override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        if (savedInstanceState == null) return
        infoUser = savedInstanceState.getSerializable("infoUser") as InfoUser?
        companyId = savedInstanceState.getString("companyId")
        contractType = savedInstanceState.getSerializable("contractType") as ContractType?
        //        posPage = savedInstanceState.getInt("pos");
    }

    @OnClick(R.id.next_button)
    fun nextPage() {
        val fragment = mSectionsPagerAdapter!!.primaryFragment
        if ((personal == null || infoUser == null) && fragment is ScanPersonalFragmnet) {
            val builder = AlertDialog.Builder(this)
                .setMessage("Escanee la cedula o digite el numero de cedula")
                .setPositiveButton("Aceptar", null)
            builder.create().show()
        } else if (fragment is CreatePersonalFragment) {
            fragment.submit()
        } else if (fragment is SelectContractFragment && selectedContract == null) {
            val builder = AlertDialog.Builder(this)
                .setMessage("Seleccione un contrato")
                .setPositiveButton("Aceptar", null)
            builder.create().show()
        } else if (fragment is JoinPersonalContractFragment) {
            if (!selectedContractTypeValue!!.isEmpty() && (selectedContractTypeValue == "AD" || selectedContractTypeValue == "AS")) {
                fragment.submit()
            } else {
                personalContract =
                    fragment.getPersonalContract()
                if (personalContract != null) {
                    posPage++
                    mViewPager!!.currentItem = posPage
                }
            }
        } else {
            posPage++
            mViewPager!!.currentItem = posPage
        }
    }

    @OnClick(R.id.previous_button)
    fun previousPage() {
        posPage--
        val fragment = mSectionsPagerAdapter!!.primaryFragment
        if (fragment is SelectContractFragment) {
            posPage--
        } else if (fragment is JoinPersonalContractFragment && !contractId!!.isEmpty()) {
            posPage--
            posPage--
        }
        mViewPager!!.currentItem = posPage
    }

    @OnClick(R.id.submit_button)
    fun submitRequest() {
        val fragment = mSectionsPagerAdapter!!.primaryFragment
        if (fragment is JoinPersonalContractFragment) {
            fragment.submit()
        } else if (fragment is SelectEmployerFragment) {
            fragment.submit()
        }
    }

    fun submitCreate() {
        setResult(Activity.RESULT_OK)
        finish()
    }

    private fun nextPage(pos: Int) {
        mViewPager!!.currentItem = pos
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    fun showProgress(show: Boolean) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        val shortAnimTime = resources.getInteger(android.R.integer.config_shortAnimTime)
        mFormView!!.visibility = if (show) View.INVISIBLE else View.VISIBLE
        mFormView!!.animate().setDuration(shortAnimTime.toLong()).alpha(
            if (show) 0.toFloat() else 1.toFloat()
        ).setListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                mFormView!!.visibility = if (show) View.INVISIBLE else View.VISIBLE
            }
        })
        mProgressView!!.visibility = if (show) View.VISIBLE else View.GONE
        mProgressView!!.animate().setDuration(shortAnimTime.toLong()).alpha(
            if (show) 1.toFloat() else 0.toFloat()
        ).setListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                mProgressView!!.visibility = if (show) View.VISIBLE else View.GONE
            }
        })
        if (posPage != 0) previousButton!!.isEnabled = !show
        nextButton!!.isEnabled = !show
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    fun showProgressLayout(show: Boolean) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        val shortAnimTime = resources.getInteger(android.R.integer.config_shortAnimTime)
        mFormView!!.visibility = if (show) View.INVISIBLE else View.VISIBLE
        mFormView!!.animate().setDuration(shortAnimTime.toLong()).alpha(
            if (show) 0.toFloat() else 1.toFloat()
        ).setListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                mFormView!!.visibility = if (show) View.INVISIBLE else View.VISIBLE
            }
        })
        mProgressLayout!!.visibility = if (show) View.VISIBLE else View.GONE
        mProgressLayout!!.animate().setDuration(shortAnimTime.toLong()).alpha(
            if (show) 1.toFloat() else 0.toFloat()
        ).setListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                mProgressLayout!!.visibility = if (show) View.VISIBLE else View.GONE
            }
        })
        if (posPage != 0) previousButton!!.isEnabled = !show
        nextButton!!.isEnabled = !show
    }

    override fun onApplyNewUser(personal: Personal, mImageUri: Uri?, create: Boolean) {
        this.personal = personal
        if (joinPersonalContract != null && !joinPersonalContract!!.isEmpty() && this.personal != null && this.personal!!.PersonalCompanyInfoId == personal.PersonalCompanyInfoId) {
            nextPage(3)
        } else {
            choiceEditUser(true)
            showProgress(true)
            CRUDService.startRequest(
                this@JoinPersonalWizardActivity, String.format("api/Personal/%s/Contracts/", personal.PersonalCompanyInfoId),
                Request.Method.GET
            )
        }
    }

    override fun choiceEditUser(option: Boolean) {
        linearLayoutControls!!.visibility = if (option) View.VISIBLE else View.GONE
        findViewById<View>(R.id.view2).visibility = if (option) View.VISIBLE else View.GONE
    }

    override fun onApplyNewUserOther(personal: Personal, mImageUri: Uri) {
        this.personal = personal
        if (joinPersonalContract != null && !joinPersonalContract!!.isEmpty() && this.personal != null && this.personal!!.PersonalCompanyInfoId == personal.PersonalCompanyInfoId) {
            nextPage(3)
        } else {
            choiceEditUser(true)
            showProgress(true)
            CRUDService.startRequest(
                this@JoinPersonalWizardActivity, String.format("api/Personal/%s/Contracts/", personal.PersonalCompanyInfoId),
                Request.Method.GET
            )
        }
    }

    override fun onJoinContract(contract: ContractEnrollment, contractTypeValue: String) {
        selectedContract = contract
        selectedContractTypeValue = contractTypeValue
        showProgress(true)
        CRUDService.startRequest(
            this, Constantes.LIST_CONTRACTS_URL + selectedContract!!.Id + "/IsContract/" + personal!!.PersonalCompanyInfoId,
            Request.Method.GET
        )
    }

    override fun onApplyContract(contract: ContractEnrollment) {
        selectedContract = contract
        showProgress(true)
        val values = ContentValues()
        values.put("personalInfoId", personal!!.PersonalCompanyInfoId)
        val paramsQuery = HttpRequest.makeParamsInUrl(values)
        CRUDService.startRequest(
            this, Constantes.LIST_CONTRACTS_URL + contract.Id + "/PersonalInfo/",
            Request.Method.GET, paramsQuery, false
        )
    }

    override fun onSuccessJoinContract() {
        nextPage(5)
    }

    override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
        val cursor = parent.adapter.getItem(position) as Cursor
        val jsonObject = cursorToJObject(cursor)
        val projectList = Gson().fromJson(jsonObject.toString(), ProjectList::class.java)
        val fragment = mSectionsPagerAdapter!!.primaryFragment
        if (fragment is SelectContractFragment) fragment.updateListContracts(
            joinPersonalContract!!,
            projectList.Id,
            contractId!!
        )
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {}
    override fun onListFragmentInteraction(
        item: ObjectList,
        imageView: ImageView
    ) {
        val fragment = mSectionsPagerAdapter!!.primaryFragment
        if (fragment is SelectEmployerFragment) fragment.fillData(item.Id)
    }

    override fun onStringResult(action: String?, option: Int, response: String?, url: String?) {
        when (action) {
            CRUDService.ACTION_REQUEST_GET -> {
                showProgress(false)
                try {
                    processRequestGET(option, response, url)
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
            CRUDService.ACTION_REQUEST_POST, CRUDService.ACTION_REQUEST_PUT, CRUDService.ACTION_REQUEST_DELETE -> {
            }
            CRUDService.ACTION_REQUEST_FORM_DATA -> {
            }
            CRUDService.ACTION_REQUEST_SAVE -> {
                showProgress(false)
                when (url) {
                    Constantes.LIST_CONTRACTS_URL -> co.com.sersoluciones.facedetectorser.utilities.DebugLog.log("tabla actualizada LIST_CONTRACTS_URL")
                    Constantes.LIST_EMPLOYERS_URL -> {
                        co.com.sersoluciones.facedetectorser.utilities.DebugLog.log("tabla llena LIST_EMPLOYERS_URL")
                        fillArray[0] = true
                    }
                    Constantes.LIST_PROJECTS_URL -> {
                        co.com.sersoluciones.facedetectorser.utilities.DebugLog.log("tabla llena LIST_PROJECTS_URL")
                        fillArray[1] = true
                    }
                }
            }
        }
    }

    @Throws(JSONException::class)
    private fun processRequestGET(option: Int, response: String?, url: String?) {
        when (option) {
            Constantes.SUCCESS_REQUEST -> if (url == Constantes.LIST_AUTOPOSITION_URL + companyId) {
                positionList = Gson().fromJson(
                    response,
                    object : TypeToken<ArrayList<Position?>?>() {}.type
                )
                nextPage(1)
            } else if (infoUser != null && url == Constantes.PERSONALCOMPANY_URL + infoUser!!.dni + "/" + companyId) {
                val jsonObject = JSONObject(response)
                jsonObject.remove("Id")
                co.com.sersoluciones.facedetectorser.utilities.DebugLog.logW(jsonObject.toString())
                personal = Gson().fromJson(jsonObject.toString(), Personal::class.java)
                Handler().postDelayed({
                    submitButton!!.visibility = View.GONE
                    previousButton!!.isEnabled = false
                    nextButton!!.visibility = View.VISIBLE
                    nextPage(1)
                }, 500)
                //onApplyNewUser(personal);
            } else if (personal != null && url == String.format("api/Personal/%s/Contracts/", personal!!.PersonalCompanyInfoId)) {
                joinPersonalContract = response
                nextPage(3)
            } else if (personal != null && url == Constantes.LIST_CONTRACTS_URL + selectedContract!!.Id + "/IsContract/" + personal!!.PersonalCompanyInfoId) {
                if (!selectedContractTypeValue!!.isEmpty() && (selectedContractTypeValue == "AD" || selectedContractTypeValue == "AS")) endWizard =
                    true
                personalContract = null
                nextPage(4)
            } else if (url == Constantes.LIST_CONTRACTS_URL + selectedContract!!.Id + "/PersonalInfo/") {
                personalContract = Gson().fromJson(response, PersonalContract::class.java)
                val json = Gson().toJson(personalContract)
                endWizard = true
                co.com.sersoluciones.facedetectorser.utilities.DebugLog.logW(json)
                nextPage(4)
            }
            Constantes.BAD_REQUEST -> {
                if (personal != null && url == Constantes.LIST_CONTRACTS_URL + selectedContract!!.Id + "/IsContract/" + personal!!.PersonalCompanyInfoId) {
                    if (response == "null") MetodosPublicos.alertDialog(this, "Este proyecto carece de proyectos asociados") else {
                        val jsonObject = JSONObject(response)
                        val alertDialog = AlertDialog.Builder(this)
                        alertDialog.setCancelable(false)
                        alertDialog.setTitle("Mensaje")
                        alertDialog.setMessage(
                            """
                            La persona esta enrolada y activa en el proyecto con el contrato ${jsonObject.getString("ContractNumber")}
                            ¿Desea trasladarlo?
                            """.trimIndent()
                        )
                        alertDialog.setPositiveButton("SI") { dialog, which ->
                            if (!selectedContractTypeValue!!.isEmpty() && (selectedContractTypeValue == "AD" || selectedContractTypeValue == "AS")) endWizard =
                                true
                            moveToPersonal = true
                            nextPage(4)
                        }
                        alertDialog.setNegativeButton("NO") { dialog, which ->
                            selectedContract = null
                            dialog.dismiss()
                            onBackPressed()
                        }
                        alertDialog.create().show()
                    }
                }
                if (url == Constantes.PERSONALCOMPANY_URL + infoUser!!.dni + "/" + companyId) {
                    personal = null
                    if (infoUser!!.name != null && !infoUser!!.name.isEmpty()) {
                        showProgress(true)
                        Handler().postDelayed({
                            showProgress(false)
                            nextPage(1)
                        }, 500)
                    } else {
                        Handler().postDelayed({ nextPage(2) }, 200)
                    }
                }
            }
            Constantes.NOT_INTERNET -> {
                val alert = AlertDialog.Builder(this@JoinPersonalWizardActivity)
                    .setTitle("Sin conexion a Internet")
                    .setMessage("Conectese a una red con Internet para continuar.")
                    .setCancelable(false)
                    .setPositiveButton("Aceptar") { _, _ -> finish() }
                alert.create().show()
            }
            Constantes.FORBIDDEN -> {
                Toast.makeText(this, "Este usuario no tiene permisos para esta accion", Toast.LENGTH_SHORT).show()
                finish()
            }
            Constantes.REQUEST_NOT_FOUND -> if (url == Constantes.PERSONALCOMPANY_URL + infoUser!!.dni + "/" + companyId) {
                personal = null
                if (infoUser!!.name != null && !infoUser!!.name.isEmpty()) {
                    showProgress(true)
                    Handler().postDelayed({
                        showProgress(false)
                        nextPage(1)
                    }, 500)
                } else {
                    Handler().postDelayed({ nextPage(2) }, 200)
                }
            }
        }
    }

    private fun loadModules() {
        mSectionsPagerAdapter = WizardPersonalPagerAdapter(supportFragmentManager)
        mSectionsPagerAdapter!!.addFragment(ScanPersonalFragmnet(), "")
        mSectionsPagerAdapter!!.addFragment(CreatePersonalFragment(), "")
        mSectionsPagerAdapter!!.addFragment(NewOtherPersonFragment(), "")
        mSectionsPagerAdapter!!.addFragment(SelectContractFragment(), "")
        mSectionsPagerAdapter!!.addFragment(JoinPersonalContractFragment(), "")
        mSectionsPagerAdapter!!.addFragment(SelectEmployerFragment(), "Empleador")
        mViewPager!!.adapter = mSectionsPagerAdapter
        mViewPager!!.pageMargin = 0
        mViewPager!!.offscreenPageLimit = 6
        mViewPager!!.addOnPageChangeListener(this)
    }

    //    private void verifyFillData() {
    //        boolean flag = true;
    //        for (boolean item : fillArray) {
    //            if (!item) {
    //                flag = false;
    //                break;
    //            }
    //        }
    //        if (flag) {
    //            DebugLog.log("Reinicio de activity");
    //            Intent intent = getIntent();
    //            finish();
    //            startActivity(intent);
    //        } else {
    //            showProgress(true);
    //        }
    //    }
    override fun onPageScrolled(i: Int, v: Float, i1: Int) {}
    override fun onPageSelected(position: Int) {
        DebugLog.log("pos: $position")
        val fragment = mSectionsPagerAdapter!!.primaryFragment
        posPage = position
        val title = mSectionsPagerAdapter!!.getPageTitle(position).toString()
        if (this@JoinPersonalWizardActivity.supportActionBar == null) return
        this@JoinPersonalWizardActivity.supportActionBar!!.title = "Vincular Empleado"
        this@JoinPersonalWizardActivity.supportActionBar!!.subtitle = title
        when (position) {
            0 -> {
                submitButton!!.visibility = View.GONE
                previousButton!!.isEnabled = false
                nextButton!!.visibility = View.VISIBLE
            }
            1, 2, 3, 4 -> {
                submitButton!!.visibility = View.GONE
                previousButton!!.isEnabled = true
                nextButton!!.visibility = View.VISIBLE
                if (!doc!!.isEmpty()) {
                    submitButton!!.visibility = View.GONE
                    previousButton!!.isEnabled = false
                    nextButton!!.visibility = View.VISIBLE
                }

//                linearLayoutSpinner.setVisibility(View.VISIBLE);
                if (fragment is CreatePersonalFragment) {
                    supportActionBar!!.title = String.format("CC %s ", infoUser!!.dni)
                    if (personal != null) {
                        supportActionBar!!.subtitle = String.format("%s %s", personal!!.Name, personal!!.LastName)
                        fragment.fillData(infoUser, personal, companyId)
                    } else {
                        supportActionBar!!.subtitle = String.format("%s %s", infoUser!!.name, infoUser!!.lastname)
                        fragment.setData(infoUser, companyId)
                    }
                } else if (fragment is NewOtherPersonFragment) {
                    fragment.setData(infoUser, companyId)
                } else if (fragment is SelectContractFragment) {
                    selectedContract = null
                    moveToPersonal = false
                    endWizard = false
                    supportActionBar!!.title = String.format("CC %s ", infoUser!!.dni)
                    supportActionBar!!.subtitle = String.format("%s %s", personal!!.Name, personal!!.LastName)
                    fragment.updateListContracts(joinPersonalContract!!, "", contractId!!)
                } else if (fragment is JoinPersonalContractFragment) {
                    if (endWizard) {
                        submitButton!!.visibility = View.VISIBLE
                        previousButton!!.isEnabled = true
                        nextButton!!.visibility = View.GONE
                    }
                    linearLayoutSpinner!!.visibility = View.GONE
                    supportActionBar!!.title = String.format("CC %s ", infoUser!!.dni)
                    supportActionBar!!.subtitle = String.format("%s %s", personal!!.Name, personal!!.LastName)
                    fragment.configFrament(
                        contractType!!,
                        selectedContract!!,
                        personal,
                        personalContract,
                        companyId,
                        positionList,
                        moveToPersonal,
                        typePermission
                    )
                }
            }
            5 -> {
                if (fragment is SelectEmployerFragment) {
                    fragment.fillData(selectedContract!!, moveToPersonal, personalContract)
                }
                submitButton!!.visibility = View.VISIBLE
                previousButton!!.isEnabled = true
                nextButton!!.visibility = View.GONE
            }
        }
    }

    override fun onPageScrollStateChanged(i: Int) {}
    override fun onApplyScan(infoUser: InfoUser) {
        this.infoUser = infoUser
        CRUDService.startRequest(
            this,
            Constantes.PERSONALCOMPANY_URL + infoUser.dni + "/" + companyId, Request.Method.GET
        )
        showProgress(true)
    }

    override fun onApplyScanDoc(doc: String) {
        infoUser = InfoUser()
        infoUser!!.dni = java.lang.Long.valueOf(doc)
        CRUDService.startRequest(
            this,
            Constantes.PERSONALCOMPANY_URL + doc + "/" + companyId, Request.Method.GET
        )
        showProgress(true)
    }

    override fun onApplyScanDocQR() {}
}
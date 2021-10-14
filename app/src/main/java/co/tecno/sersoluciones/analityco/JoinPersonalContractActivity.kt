package co.tecno.sersoluciones.analityco

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Activity
import android.content.ContentValues
import android.content.IntentFilter
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import butterknife.BindView
import butterknife.OnClick
import co.com.sersoluciones.barcodedetectorser.utilities.DebugLog
import co.tecno.sersoluciones.analityco.adapters.personal.WizardPersonalPagerAdapter
import co.tecno.sersoluciones.analityco.callback.OnListEmployerInteractionListener
import co.tecno.sersoluciones.analityco.databases.DBHelper
import co.tecno.sersoluciones.analityco.fragments.personal.wizard.JoinPersonalContractFragment
import co.tecno.sersoluciones.analityco.fragments.personal.wizard.SelectEmployerFragment
import co.tecno.sersoluciones.analityco.models.*
import co.tecno.sersoluciones.analityco.receivers.RequestBroadcastReceiver
import co.tecno.sersoluciones.analityco.receivers.RequestBroadcastReceiver.BroadcastListener
import co.tecno.sersoluciones.analityco.services.CRUDService
import co.tecno.sersoluciones.analityco.utilities.Constantes
import co.tecno.sersoluciones.analityco.utilities.DecodeBarcode.InfoUser
import co.tecno.sersoluciones.analityco.utilities.HttpRequest
import co.tecno.sersoluciones.analityco.utilities.MetodosPublicos
import co.tecno.sersoluciones.analityco.utilities.Utils.cursorToJArray
import co.tecno.sersoluciones.analityco.utilities.Utils.cursorToJObject
import co.tecno.sersoluciones.analityco.views.CustomViewPager
import com.android.volley.Request
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.json.JSONException
import org.json.JSONObject
import java.util.*

class JoinPersonalContractActivity : BaseActivity(), BroadcastListener,
    JoinPersonalContractFragment.OnJoinContractListener, OnListEmployerInteractionListener,
    OnPageChangeListener {
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
    private var contractType: ContractType? = null
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
    private var typePermission: String? = ""
    @SuppressLint("SourceLockedOrientationActivity")
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
            selectedContractTypeValue = extras.getString("contractType")
            companyId = extras.getString("companyInfoId")
            infoUser = InfoUser()
            if (extras.containsKey("docNumber")) {
//                choiceEditUser(false);
                //infoUser!!.dni = java.lang.Long.valueOf(extras.getString("docNumber", ""))
                infoUser!!.dni=java.lang.Long.valueOf(extras.getString("docNumber",""))

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
                    this@JoinPersonalContractActivity,
                    Constantes.PERSONALCOMPANY_URL + infoUser!!.dni + "/" + companyId, Request.Method.GET
                )
                showProgress(true)
            }
        } else showProgress(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        //NavUtils.navigateUpFromSameTask(this)
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun loadDataFromServer(): Boolean {
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
        co.com.sersoluciones.facedetectorser.utilities.DebugLog.logW("hay datos $flag")
        return flag
    }

    override fun attachLayoutResource() {
        super.setChildLayout(R.layout.activity_personal_wizard)
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
        if (fragment is JoinPersonalContractFragment) {
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
        mViewPager!!.visibility = if (show) View.INVISIBLE else View.VISIBLE
        mViewPager!!.animate().setDuration(shortAnimTime.toLong()).alpha(
            if (show) 0.toFloat() else 1.toFloat()
        ).setListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                mViewPager!!.visibility = if (show) View.INVISIBLE else View.VISIBLE
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

    private fun getCursorContracts(contractId: String?) {
        val item = String.format("%s", contractType!!.Id)
        val arrayList = ArrayList<String?>()
        arrayList.add(item)
        var selection = "(" + DBHelper.CONTRACT_TABLE_COLUMN_CONTRACT_TYPE_ID + " = ? "
        if (contractId!!.isNotEmpty()) {
            selection += " AND " + DBHelper.CONTRACT_TABLE_COLUMN_SERVER_ID + " = ? "
            arrayList.add(contractId)
        }
        selection += " AND " + DBHelper.CONTRACT_TABLE_COLUMN_PROJECT_IDS + " != \"\" )"
        var selectionArgs: Array<String?>? = arrayOfNulls(arrayList.size)
        selectionArgs = arrayList.toArray(selectionArgs)
        val cursor = contentResolver.query(
            Constantes.CONTENT_CONTRACT_URI, null,
            selection, selectionArgs, null
        )
        if (cursor != null) {
            val jsonArrayContracts = cursorToJArray(cursor)
            val contractList =
                Gson().fromJson<ArrayList<ContractEnrollment>>(
                    jsonArrayContracts.toString(),
                    object : TypeToken<ArrayList<ContractEnrollment?>?>() {}.type
                )
            if (contractList.size == 1 && !contractId.isEmpty() && contractList[0].IsActive) {
                onApplyContract(contractList[0])
            }
            cursor.close()
        }
    }

    fun onApplyContract(contract: ContractEnrollment) {
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

    override fun onListFragmentInteraction(
        item: ObjectList,
        imageView: ImageView
    ) {
        val fragment = mSectionsPagerAdapter!!.primaryFragment
        if (fragment is SelectEmployerFragment) fragment.fillData(
            item.Id
        )
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
            } else if (infoUser != null && url == Constantes.PERSONALCOMPANY_URL + infoUser!!.dni + "/" + companyId) {
                val jsonObject = JSONObject(response)
                jsonObject.remove("Id")
                co.com.sersoluciones.facedetectorser.utilities.DebugLog.logW(jsonObject.toString())
                personal = Gson().fromJson(jsonObject.toString(), Personal::class.java)
                supportActionBar!!.title = String.format("CC %s ", infoUser!!.dni)
                supportActionBar!!.subtitle = String.format("%s %s", personal!!.Name, personal!!.LastName)
                getCursorContracts(contractId)
            } else if (personal != null && url == String.format("api/Personal/%s/Contracts/", personal!!.PersonalCompanyInfoId)) {
                joinPersonalContract = response
            } else if (personal != null && url == Constantes.LIST_CONTRACTS_URL + selectedContract!!.Id + "/IsContract/" + personal!!.PersonalCompanyInfoId) {
                if (!selectedContractTypeValue!!.isEmpty() && (selectedContractTypeValue == "AD" || selectedContractTypeValue == "AS")) endWizard =
                    true
                personalContract = null
            } else if (url == Constantes.LIST_CONTRACTS_URL + selectedContract!!.Id + "/PersonalInfo/") {
                personalContract = Gson().fromJson(response, PersonalContract::class.java)
                endWizard = true
                submitButton!!.visibility = View.VISIBLE
                previousButton!!.isEnabled = false
                nextButton!!.visibility = View.GONE
                val fragment = mSectionsPagerAdapter!!.primaryFragment
                if (fragment is JoinPersonalContractFragment) fragment
                    .configFrament(
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
            Constantes.BAD_REQUEST -> {
                if (url == Constantes.LIST_CONTRACTS_URL + selectedContract!!.Id + "/IsContract/" + personal!!.PersonalCompanyInfoId) {
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
                        }
                        alertDialog.setNegativeButton("NO") { dialog, which ->
                            selectedContract = null
                            dialog.dismiss()
                        }
                        alertDialog.create().show()
                    }
                }
                if (url == Constantes.PERSONALCOMPANY_URL + infoUser!!.dni + "/" + companyId) {
                    personal = null
                    finish()
                }
            }
            Constantes.NOT_INTERNET -> {
                val alert = AlertDialog.Builder(this@JoinPersonalContractActivity)
                    .setTitle("Sin conexion a Internet")
                    .setMessage("Conectese a una red con Internet para continuar.")
                    .setCancelable(false)
                    .setPositiveButton("Aceptar") { dialog, which -> finish() }
                alert.create().show()
            }
            Constantes.FORBIDDEN -> {
                Toast.makeText(this, "Este usuario no tiene permisos para esta accion", Toast.LENGTH_SHORT).show()
                finish()
            }
            Constantes.REQUEST_NOT_FOUND -> if (url == Constantes.PERSONALCOMPANY_URL + infoUser!!.dni + "/" + companyId) {
                personal = null
                finish()
            }
        }
    }

    private fun loadModules() {
        mSectionsPagerAdapter = WizardPersonalPagerAdapter(supportFragmentManager)
        mSectionsPagerAdapter!!.addFragment(JoinPersonalContractFragment(), "")
        mSectionsPagerAdapter!!.addFragment(SelectEmployerFragment(), "Empleador")
        mViewPager!!.adapter = mSectionsPagerAdapter
        mViewPager!!.pageMargin = 0
        mViewPager!!.offscreenPageLimit = 2
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
        if (this@JoinPersonalContractActivity.supportActionBar == null) return
        this@JoinPersonalContractActivity.supportActionBar!!.title = "Vincular Empleado"
        this@JoinPersonalContractActivity.supportActionBar!!.subtitle = title
        when (position) {
            0 -> {
                submitButton!!.visibility = View.GONE
                previousButton!!.isEnabled = false
                nextButton!!.visibility = View.VISIBLE
            }
            1 -> if (fragment is JoinPersonalContractFragment) {
                if (endWizard) {
                    previousButton!!.isEnabled = true
                    linearLayoutControls!!.visibility = View.VISIBLE
                    findViewById<View>(R.id.view2).visibility = View.VISIBLE
                    previousButton!!.isEnabled = false
                    submitButton!!.visibility = View.VISIBLE
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
            2 -> {
                if (fragment is SelectEmployerFragment) {
                    fragment.fillData(
                        selectedContract!!,
                        moveToPersonal,
                        personalContract
                    )
                }
                submitButton!!.visibility = View.VISIBLE
                previousButton!!.isEnabled = true
                nextButton!!.visibility = View.GONE
            }
        }
    }

    override fun onPageScrollStateChanged(i: Int) {}
}
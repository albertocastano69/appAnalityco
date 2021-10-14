package co.tecno.sersoluciones.analityco

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Activity
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ActivityInfo
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MenuItem
import android.view.View
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import androidx.cursoradapter.widget.SimpleCursorAdapter
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import butterknife.BindView
import butterknife.OnClick
import co.com.sersoluciones.barcodedetectorser.utilities.DebugLog
import co.tecno.sersoluciones.analityco.adapters.personal.WizardPersonalPagerAdapter
import co.tecno.sersoluciones.analityco.databases.DBHelper
import co.tecno.sersoluciones.analityco.fragments.personal.wizard.JoinPersonalContractFragment
import co.tecno.sersoluciones.analityco.fragments.personal.wizard.SelectContractFragment
import co.tecno.sersoluciones.analityco.fragments.personal.wizard.SelectContractFragment.OnSelectContractListener
import co.tecno.sersoluciones.analityco.fragments.personal.wizard.SelectContractTypeFragment
import co.tecno.sersoluciones.analityco.fragments.personal.wizard.SelectContractTypeFragment.ContractTypeListener
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

class JoinPersonalProjectWizardActivity : BaseActivity(), ContractTypeListener, BroadcastListener, OnSelectContractListener,
    JoinPersonalContractFragment.OnJoinContractListener, OnItemSelectedListener {
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
    var mForm: View? = null

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
    private var requestBroadcastReceiver: RequestBroadcastReceiver? = null
    private var infoUser: InfoUser? = null
    private var personal: Personal? = null
    private var joinPersonalContract: String? = null
    private var selectedContract: ContractEnrollment? = null
    private var positionList: ArrayList<Position>? = null
    private var personalContract: PersonalContract? = null
    private var projectList: ProjectList? = null
    private var moveToPersonal = false
    private var endWizard = false
    private var selectedContractTypeValue: String? = null
    private var fillArray: BooleanArray = booleanArrayOf()
    private var data: LinkedHashMap<Uri, String>? = null
    var employerId: String? = ""
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        DebugLog.log("ON CREATE")
        savedInstanceState?.let { onRestoreInstanceState(it) }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
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
        mSectionsPagerAdapter = WizardPersonalPagerAdapter(supportFragmentManager)
        mSectionsPagerAdapter!!.addFragment(SelectContractTypeFragment(), "Tipo de Contrato")
        mSectionsPagerAdapter!!.addFragment(SelectContractFragment(), "")
        mSectionsPagerAdapter!!.addFragment(JoinPersonalContractFragment(), "")
        mViewPager!!.setOffscreenPageLimit(3)
        mViewPager!!.setAdapter(mSectionsPagerAdapter)
        val extras = intent.extras
        if (extras != null) {
            companyId = extras.getString("companyId")
            val projectId = extras.getString("projectId")
            val doc = extras.getString("docNumber")
            employerId = extras.getString("employerId")
            infoUser = InfoUser()
            infoUser!!.dni = java.lang.Long .valueOf(doc!!)
            CRUDService.startRequest(
                this, Constantes.LIST_AUTOPOSITION_URL + companyId,
                Request.Method.GET
            )
            showProgress(true)
            updateProjectSpinner()
            selectValue(spinnerProjects, projectId)
            linearLayoutSpinner!!.visibility = View.GONE
        }

//        mViewPager.setPageMargin(0);
        mViewPager!!.addOnPageChangeListener(object : OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
            override fun onPageSelected(position: Int) {
                DebugLog.log("pos: $position")
                val fragment = mSectionsPagerAdapter!!.primaryFragment
                posPage = position
                val title = mSectionsPagerAdapter!!.getPageTitle(position).toString()
                if (this@JoinPersonalProjectWizardActivity.supportActionBar == null) return
                this@JoinPersonalProjectWizardActivity.supportActionBar!!.setTitle("Enrolar Personal")
                this@JoinPersonalProjectWizardActivity.supportActionBar!!.setSubtitle(title)
                when (position) {
                    0 -> {
                        submitButton!!.visibility = View.GONE
                        previousButton!!.isEnabled = false
                        nextButton!!.visibility = View.VISIBLE
                        linearLayoutSpinner!!.visibility = View.GONE
                        if (fragment is SelectContractTypeFragment) {
                            fragment.updateBtns(projectList!!)
                            if (contractType != null) this@JoinPersonalProjectWizardActivity.supportActionBar!!
                                .setSubtitle(String.format("Tipo de Contrato: %s", contractType!!.Description))
                        }
                    }
                    1 -> {
                        submitButton!!.visibility = View.GONE
                        previousButton!!.isEnabled = true
                        nextButton!!.visibility = View.VISIBLE
                        if (fragment is SelectContractFragment) {
                            selectedContract = null
                            moveToPersonal = false
                            endWizard = false
                            supportActionBar!!.setTitle(String.format("CC %s ", infoUser!!.dni))
                            supportActionBar!!.setSubtitle(String.format("%s %s", personal!!.Name, personal!!.LastName))
                            fragment.updateListContracts(joinPersonalContract!!, projectList!!.Id, "")
                        }
                    }
                    2 -> {
                        if (fragment is JoinPersonalContractFragment) {
                            if (endWizard) {
                                submitButton!!.visibility = View.VISIBLE
                                previousButton!!.isEnabled = true
                                nextButton!!.visibility = View.GONE
                            }
                            supportActionBar!!.setTitle(String.format("CC %s ", infoUser!!.dni))
                            supportActionBar!!.setSubtitle(String.format("%s %s", personal!!.Name, personal!!.LastName))
                            fragment.configFrament(
                                contractType!!,
                                selectedContract!!,
                                personal,
                                personalContract,
                                companyId,
                                positionList,
                                moveToPersonal,
                                "personal"
                            )
                        }
                        submitButton!!.visibility = View.VISIBLE
                        previousButton!!.isEnabled = true
                        nextButton!!.visibility = View.GONE
                    }
                }
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })
        loadDataFromServer()
    }

    private fun loadDataFromServer() {
        val values = ContentValues()
        values.put(Constantes.KEY_SELECT, true)
        val paramsQuery = HttpRequest.makeParamsInUrl(values)
        CRUDService.startRequest(
            this, Constantes.LIST_CONTRACTS_URL,
            Request.Method.GET, paramsQuery, true
        )
        data = LinkedHashMap()
        data!![Constantes.CONTENT_EMPLOYER_URI] = Constantes.LIST_EMPLOYERS_URL
        data!![Constantes.CONTENT_PROJECT_URI] = Constantes.LIST_PROJECTS_URL
        fillArray = BooleanArray(data!!.size)
        showProgress(true)
        verifyInDb()
    }

    private fun verifyInDb() {
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
    }

    override fun attachLayoutResource() {
        super.setChildLayout(R.layout.activity_personal_wizard)
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
        LocalBroadcastManager.getInstance(this).unregisterReceiver(requestBroadcastReceiver!!)
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

    override fun onBackPressed() {
        val builder = AlertDialog.Builder(this)
            .setTitle("Alerta")
            .setMessage("Esta seguro de volver atrás y no completar el registro?")
            .setPositiveButton("Aceptar") { dialog, which -> super@JoinPersonalProjectWizardActivity.finish() }
            .setNegativeButton("Cancelar") { dialogInterface, i -> dialogInterface.dismiss() }
        val alertDialog = builder.create()
        alertDialog.show()
    }

    @OnClick(R.id.next_button)
    fun nextPage() {
        val fragment = mSectionsPagerAdapter!!.primaryFragment
        if (contractType == null && fragment is SelectContractTypeFragment) {
            val builder = androidx.appcompat.app.AlertDialog.Builder(this)
                .setMessage("Seleccione un tipo de contrato")
                .setPositiveButton("Aceptar", null)
            builder.create().show()
        } else if (fragment is SelectContractFragment && selectedContract == null) {
            val builder = androidx.appcompat.app.AlertDialog.Builder(this)
                .setMessage("Seleccione un contrato")
                .setPositiveButton("Aceptar", null)
            builder.create().show()
        } else if (fragment is JoinPersonalContractFragment) {
            personalContract =
                fragment.getPersonalContract()
            fragment.submit()
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
        }
        mViewPager!!.currentItem = posPage
    }

    @OnClick(R.id.submit_button)
    fun submitRequest() {
        val fragment = mSectionsPagerAdapter!!.primaryFragment
        if (fragment is JoinPersonalContractFragment) {
            fragment.submit()
        }
    }

    fun submitCreate(personalInfoId: Int) {
        val i = Intent()
        i.putExtra("personalInfoId", personalInfoId)
        setResult(Activity.RESULT_OK, i)
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
        mForm!!.visibility = if (show) View.INVISIBLE else View.VISIBLE
        mForm!!.animate().setDuration(shortAnimTime.toLong()).alpha(
            if (show) 0.toFloat() else 1.toFloat()
        ).setListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                mForm!!.visibility = if (show) View.INVISIBLE else View.VISIBLE
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

    override fun onApplyContractType(contractType: ContractType?) {
        DebugLog.logW("contractType: " + contractType!!.Value)
        this.contractType = contractType
        val fragment = mSectionsPagerAdapter!!.primaryFragment
        if (fragment is SelectContractTypeFragment) {
            fragment.updateBtns(projectList!!)
            this@JoinPersonalProjectWizardActivity.supportActionBar?.subtitle = String.format("Tipo de Contrato: %s", contractType.Description)
        }
        showProgress(true)
        CRUDService.startRequest(
            this@JoinPersonalProjectWizardActivity, String.format("api/Personal/%s/Contracts/", personal!!.PersonalCompanyInfoId),
            Request.Method.GET
        )
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
        nextPage(3)
    }

    private fun selectValue(spinner: Spinner?, value: String?) {
        for (i in 0 until spinner!!.count) {
            val cursor = spinner.getItemAtPosition(i) as Cursor
            val jsonObject = cursorToJObject(cursor)
            val projectList = Gson().fromJson(jsonObject.toString(), ProjectList::class.java)
            if (projectList.Id == value) {
                this.projectList = projectList
                spinner.setSelection(i)
                break
            }
        }
    }

    private fun updateProjectSpinner() {
        var selection = "(" + DBHelper.PROJECT_TABLE_COLUMN_COMPANY_INFO_ID + " = ? )"
        selection += " AND (" + DBHelper.PROJECT_TABLE_COLUMN_ACTIVE + " = ? )"
        val selectionArgs = arrayOf(companyId, "1")
        @SuppressLint("Recycle") val mCursor = contentResolver.query(
            Constantes.CONTENT_PROJECT_URI, null, selection, selectionArgs,
            DBHelper.PROJECT_TABLE_COLUMN_NAME
        )
        val simpleAdapter = SimpleCursorAdapter(
            this,
            R.layout.simple_spinner_item_project,
            mCursor,
            arrayOf(DBHelper.PROJECT_TABLE_COLUMN_NAME),
            intArrayOf(android.R.id.text1),
            0
        )
        spinnerProjects!!.onItemSelectedListener = this
        spinnerProjects!!.adapter = simpleAdapter
    }

    override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
        val cursor = parent.adapter.getItem(position) as Cursor
        val jsonObject = cursorToJObject(cursor)
        projectList = Gson().fromJson(jsonObject.toString(), ProjectList::class.java)
        updateItem(projectList!!.Id)
        val fragment = mSectionsPagerAdapter!!.primaryFragment
        if (fragment is SelectContractFragment) fragment.updateListContracts(joinPersonalContract!!, projectList!!.Id, "")
        if (fragment is SelectContractTypeFragment) fragment.updateBtns(projectList!!)
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {}
    private fun updateItem(projectId: String) {
        var contentValues = ContentValues()
        contentValues.put(DBHelper.PROJECT_TABLE_COLUMN_IS_SELECTED, false)
        contentResolver.update(Constantes.CONTENT_PROJECT_URI, contentValues, null, null)
        contentValues = ContentValues()
        contentValues.put(DBHelper.PROJECT_TABLE_COLUMN_IS_SELECTED, true)
        val selection = "(" + DBHelper.PROJECT_TABLE_COLUMN_SERVER_ID + " = ? )"
        val selectionArgs = arrayOf(projectId)
        contentResolver.update(
            Constantes.CONTENT_PROJECT_URI,
            contentValues, selection, selectionArgs
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
            CRUDService.ACTION_REQUEST_SAVE -> when (url) {
                Constantes.LIST_CONTRACTS_URL -> co.com.sersoluciones.facedetectorser.utilities.DebugLog.log("tabla actualizada LIST_CONTRACTS_URL")
                Constantes.LIST_EMPLOYERS_URL -> {
                    co.com.sersoluciones.facedetectorser.utilities.DebugLog.log("tabla llena LIST_EMPLOYERS_URL")
                    fillArray[0] = true
                    verifyFillData()
                }
                Constantes.LIST_PROJECTS_URL -> {
                    co.com.sersoluciones.facedetectorser.utilities.DebugLog.log("tabla llena LIST_PROJECTS_URL")
                    fillArray[1] = true
                    verifyFillData()
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
                showProgress(true)
                CRUDService.startRequest(
                    this,
                    Constantes.PERSONALCOMPANY_URL + infoUser!!.dni + "/" + companyId, Request.Method.GET
                )
            } else if (infoUser != null && url == Constantes.PERSONALCOMPANY_URL + infoUser!!.dni + "/" + companyId) {
                val jsonObject = JSONObject(response)
                jsonObject.remove("Id")
                DebugLog.logW(jsonObject.toString())
                personal = Gson().fromJson(jsonObject.toString(), Personal::class.java)
                val fragment = mSectionsPagerAdapter!!.primaryFragment
                (fragment as SelectContractTypeFragment).updateBtns(projectList!!)
            } else if (personal != null && url == String.format("api/Personal/%s/Contracts/", personal!!.PersonalCompanyInfoId)) {
                joinPersonalContract = response
                nextPage(1)
            } else if (personal != null && url == Constantes.LIST_CONTRACTS_URL + selectedContract!!.Id + "/IsContract/" + personal!!.PersonalCompanyInfoId) {
                if (!selectedContractTypeValue!!.isEmpty() && (selectedContractTypeValue == "AD" || selectedContractTypeValue == "AS")) endWizard =
                    true
                personalContract = null
                nextPage(2)
            } else if (personal != null && url == Constantes.LIST_CONTRACTS_URL + selectedContract!!.Id + "/PersonalInfo/") {
                personalContract = Gson().fromJson(response, PersonalContract::class.java)
                val json = Gson().toJson(personalContract)
                endWizard = true
                DebugLog.logW(json)
                nextPage(2)
            }
            Constantes.BAD_REQUEST -> {
                if (personal != null && url == Constantes.LIST_CONTRACTS_URL + selectedContract!!.Id + "/IsContract/" + personal!!.PersonalCompanyInfoId) {
                    if (response == "null") MetodosPublicos.alertDialog(this, "Este proyecto carece de proyectos asociados") else {
                        val jsonObject = JSONObject(response)
                        val alertDialog = androidx.appcompat.app.AlertDialog.Builder(this)
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
                            nextPage(2)
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
                    DebugLog.logE("Personal no encontrado. No nos fue posible encontrar el personal, por favor utilice el modo Scan.")
                }
            }
            Constantes.NOT_INTERNET -> {
                val alert =
                    androidx.appcompat.app.AlertDialog.Builder(this@JoinPersonalProjectWizardActivity)
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
                DebugLog.logE("Personal no encontrado. No nos fue posible encontrar el personal, por favor utilice el modo Scan.")
            }
        }
    }

    private fun verifyFillData() {
        var flag = true
        for (item in fillArray) {
            if (!item) {
                flag = false
                break
            }
        }
        if (flag) {
            preferences.syncDate = Calendar.getInstance().time.time
            co.com.sersoluciones.facedetectorser.utilities.DebugLog.log("Reinicio de activity")
            val intent = intent
            startActivity(intent)
            finish()
        } else {
            showProgress(true)
        }
    }

    override fun onDetectInternet(online: Boolean) {
        Handler(Looper.getMainLooper())
            .post { findViewById<View>(R.id.textViewOnline).visibility = if (online) View.GONE else View.VISIBLE }
    }

}
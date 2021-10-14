package co.tecno.sersoluciones.analityco

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.*
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.util.Log
import android.util.Patterns
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView.OnEditorActionListener
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import co.com.sersoluciones.facedetectorser.utilities.DebugLog
import co.tecno.sersoluciones.analityco.databases.DBHelper
import co.tecno.sersoluciones.analityco.models.Profile
import co.tecno.sersoluciones.analityco.receivers.LoginResultReceiver
import co.tecno.sersoluciones.analityco.receivers.RequestBroadcastReceiver
import co.tecno.sersoluciones.analityco.receivers.RequestBroadcastReceiver.BroadcastListener
import co.tecno.sersoluciones.analityco.services.CRUDService
import co.tecno.sersoluciones.analityco.utilities.*
import co.tecno.sersoluciones.analityco.viewmodels.PersonalDailyViewModel
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.ImageRequest
import com.google.gson.Gson
import net.steamcrafted.materialiconlib.MaterialDrawableBuilder
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.util.*
import javax.inject.Inject

/**
 * A login screen that offers login via email/password.
 * Created by Ser Soluciones SAS on 14/06/2017.
 * www.sersoluciones.com - contacto@sersoluciones.com
 */
class LoginActivity : AppCompatActivity(), LoginResultReceiver.Receiver, BroadcastListener {
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private var mAuthTask: UserLoginTask? = null

    // UI references.
    private var mPasswordView: EditText? = null
    private var mProgressView: View? = null
    private var mLoginFormView: View? = null
    private var mReceiver: LoginResultReceiver? = null
    private var preferences: MyPreferences? = null
    private var mUsenameView: EditText? = null
    private var username: String? = null
    private var password: String? = null
    private var requestBroadcastReceiver: RequestBroadcastReceiver? = null
    private var data: LinkedHashMap<Uri, String>? = null
    private var fillArray = BooleanArray(2)
    private var createProfile = false

    @Inject
    lateinit var viewModel: PersonalDailyViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        ApplicationContext.analitycoComponent.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowTitleEnabled(false)
        val upArrow = MaterialDrawableBuilder.with(this) // provide a context
            .setIcon(MaterialDrawableBuilder.IconValue.ARROW_LEFT) // provide an icon
            .setColor(Color.WHITE) // set the icon color
            .setToActionbarSize() // set the icon size
            .build()
        supportActionBar!!.setHomeAsUpIndicator(upArrow)

        // Set up the login form.
        mUsenameView = findViewById(R.id.user_id)
        mPasswordView = findViewById(R.id.password)
        mPasswordView?.setOnEditorActionListener(OnEditorActionListener { _, id, _ ->
            if (id == R.id.login || id == EditorInfo.IME_NULL) {
                attemptLogin()
                return@OnEditorActionListener true
            }
            false
        })
        preferences = MyPreferences(this)
        val mEmailSignInButton = findViewById<Button>(R.id.email_sign_in_button)
        mEmailSignInButton.setOnClickListener { attemptLogin() }
        requestBroadcastReceiver = RequestBroadcastReceiver(this)
        mLoginFormView = findViewById(R.id.login_form)
        mProgressView = findViewById(R.id.login_progress)
        mReceiver = LoginResultReceiver(Handler())
        mReceiver!!.setReceiver(this)
        data = LinkedHashMap()
        fillArray = BooleanArray(2)
        createProfile = false
        val extras = intent.extras
        if (extras != null) {
            username = extras.getString(Constantes.KEY_USERNAME)
            password = extras.getString(Constantes.KEY_PASS)
            showProgress(true)
            mAuthTask = UserLoginTask()
            mAuthTask!!.execute(username, password)
        }

//        if (preferences.isUserLogin()) {
//            goMainActivity();
//        } else {
//            preferences.cleanJWT();
//        }
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
        val intent = Intent(this, SplashActivity::class.java)
        intent.putExtra("viewAnimation", false)
        startActivity(intent)
        overridePendingTransition(R.anim.left_to_right, R.anim.right_to_left)
        finish()
        //        super.onBackPressed();
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

    /**
     * Metodo para ir a la actividad principal
     */
    private fun goMainActivity() {
        DebugLog.logW("goMainActivity LOGIN ACTIVITY")
        preferences!!.isUserLogin = true
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        intent.putExtra("Login",true)
        //intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(intent)
        finish()
    }

    override fun onDestroy() {
        DebugLog.logE("ON DESTROY LOGIN ACTIVITY")
        super.onDestroy()
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private fun attemptLogin() {
        if (mAuthTask != null) {
            return
        }

        // Reset errors.
        mUsenameView!!.error = null
        mPasswordView!!.error = null

        // Store values at the time of the login attempt.
        username = mUsenameView!!.text.toString()
        password = mPasswordView!!.text.toString()
        var cancel = false
        var focusView: View? = null

        // Check for a valid email address.
        if (TextUtils.isEmpty(username)) {
            mUsenameView!!.error = getString(R.string.error_field_required)
            focusView = mUsenameView
            cancel = true
        } else if (!isEmailValid(username!!)) {
            mUsenameView!!.error = getString(R.string.error_invalid_email)
            focusView = mUsenameView
            cancel = true
        } else if (TextUtils.isEmpty(password)) {
            mPasswordView!!.error = getString(R.string.error_field_required)
            focusView = mPasswordView
            cancel = true
        }
        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password!!)) {
            mPasswordView!!.error = getString(R.string.error_invalid_password)
            focusView = mPasswordView
            cancel = true
        }
        if (cancel) {
            focusView!!.requestFocus()
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true)
            mAuthTask = UserLoginTask()
            mAuthTask!!.execute(username, password)
        }
    }

    private fun isEmailValid(email: String): Boolean {
        val pattern = Patterns.EMAIL_ADDRESS
        return pattern.matcher(email).matches()
    }

    private fun isPasswordValid(password: String): Boolean {
        //TODO: Replace this with your own logic
        return password.length > 5
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private fun showProgress(show: Boolean) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        val shortAnimTime = resources.getInteger(android.R.integer.config_shortAnimTime)
        mLoginFormView!!.visibility = if (show) View.GONE else View.VISIBLE
        mLoginFormView!!.animate().setDuration(shortAnimTime.toLong()).alpha(
            if (show) 0.toFloat() else 1.toFloat()
        ).setListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                mLoginFormView!!.visibility = if (show) View.GONE else View.VISIBLE
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
    }

    override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {
        mAuthTask = null
        showProgress(false)
        when (resultCode) {
            Constantes.LOGIN_SUCCESS -> {
                if (username != preferences!!.username) {
                    val dbHelper = DBHelper(this@LoginActivity)
                    dbHelper.deleteAllContractPerson()
                    dbHelper.deleteAllContract()
                    dbHelper.deleteAllCompany()
                    dbHelper.deleteAllProject()
                    dbHelper.deleteAllEmployer()
                    dbHelper.deleteAllPersonal()
                    preferences?.selectedProjectId = ""
                    viewModel.deletePersonalRealTime()
                }
                preferences!!.username = username!!
                updateDB()
            }
            Constantes.LOGIN_ERROR -> {
                val error = resultData?.getString(Intent.EXTRA_TEXT)
                if (error != null) {
                    if (!error.isEmpty()) {
                        DebugLog.logE(error)
                        mPasswordView!!.error = getString(R.string.error_incorrect_password)
                        mPasswordView!!.requestFocus()
                        MetodosPublicos.alertDialog(this@LoginActivity, error)
                    } else {
                        MetodosPublicos.alertDialog(this@LoginActivity, "Sin conexion con el servidor")
                    }
                } else MetodosPublicos.alertDialog(this@LoginActivity, "Sin conexion con el servidor")
            }
        }
    }

    private fun requestImage(profile: String?) {
        showProgress(true)
        try {
            val jsonObjectProfile = JSONObject(profile)
            val avatarUrl = jsonObjectProfile.getString("avatar")
            val url = Constantes.URL_IMAGES + avatarUrl
            DebugLog.log("avatarUrl: $avatarUrl")
            if (!avatarUrl.isEmpty() && avatarUrl != "null") {
                // Retrieves an image specified by the URL, displays it in the UI.
                val request = ImageRequest(url,
                    Response.Listener { bitmap ->
                        DebugLog.log("Success bitmap profile")
                        saveAvatarImage(bitmap)
                    }, 80, 80, ImageView.ScaleType.FIT_CENTER, null,
                    Response.ErrorListener {
                        showProgress(false)
                        preferences!!.avatarImgPath = ""
                        try {
                            createProfile("")
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                        createProfile = true
                        verifyFillData()
                    })
                MySingleton.getInstance(this).addToRequestQueue(request)
            } else {
                showProgress(false)
                preferences!!.avatarImgPath = ""
                createProfile("")
                createProfile = true
                verifyFillData()
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    @Throws(JSONException::class)
    private fun createProfile(pathImage: String) {
        val profileStr = preferences!!.profile
        val profile =
            Gson().fromJson(profileStr, Profile::class.java)
        val profileJson = Gson().toJson(profile)
        val jsonArray = JSONArray()
        val jsonObject = JSONObject(profileJson)
        jsonObject.put(Constantes.KEY_IMAGE_AVATAR_PATH, pathImage)
        jsonArray.put(jsonObject)
        preferences!!.profiles = jsonArray.toString()
    }

    private fun saveAvatarImage(bitmap: Bitmap) {
        val cw = ContextWrapper(applicationContext)
        val directory = cw.getDir("profile", Context.MODE_PRIVATE)
        if (!directory.exists()) {
            directory.mkdir()
        }
        val mypath = File(directory, "thumbnail.png")
        val fos: FileOutputStream
        try {
            fos = FileOutputStream(mypath)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
            fos.close()
            showProgress(false)
            preferences!!.avatarImgPath = mypath.path
            createProfile(mypath.path)
            createProfile = true
            verifyFillData()
        } catch (e: Exception) {
            Log.e("SAVE_IMAGE", e.message, e)
        }
    }

    private fun updateDB() {
        showProgress(true)
        loadDataFromServer()
    }

    private fun loadDataFromServer() {
        CRUDService.startRequest(this, Constantes.USER_INFO_URL, Request.Method.GET)
        data = LinkedHashMap()
        //        data.put(Constantes.CONTENT_CONTRACT_URI, Constantes.LIST_CONTRACTS_URL);
//        data.put(Constantes.CONTENT_PERSONAL_URI, Constantes.LIST_PERSONAL_URL);
        data!![Constantes.CONTENT_COMMON_OPTIONS_URI] = Constantes.LIST_COMMON_OPTIONS_URL
        data!![Constantes.CONTENT_PROJECT_URI] = Constantes.LIST_PROJECTS_URL
        //        data.put(Constantes.CONTENT_CONTRACT_PER_OFFLINE_URI, Constantes.LIST_CONTRACT_PER_OFFLINE_URL);
        fillArray = BooleanArray(data!!.size)
        launchRequests()
    }

    private fun launchRequests() {
        var pos = 0
        for (o in data!!.entries) {
            val pair = o as Map.Entry<*, *>
            val values = ContentValues()
            if (pair.value == Constantes.LIST_CONTRACT_PER_OFFLINE_URL) values.put("project", true) else values.put(Constantes.KEY_SELECT, true)
            val paramsQuery = HttpRequest.makeParamsInUrl(values)
            CRUDService.startRequest(
                this, pair.value as String?,
                Request.Method.GET, paramsQuery, true
            )
            DebugLog.logW(pos.toString() + ". URL to Request: " + pair.value + ": " + fillArray[pos])
            pos++
            //it.remove();
        }
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    @SuppressLint("StaticFieldLeak")
    private inner class UserLoginTask : AsyncTask<String, Void, Boolean>() {

        override fun doInBackground(vararg params: String): Boolean {
            val url = preferences!!.urlServer + Constantes.API_TOKEN_AUTH_SERVER
            val imei = preferences!!.deviceId
            DebugLog.log("imei: $imei")
            HttpRequest.refreshToken(
                url,
                HttpRequest.makeStringParamsLogin(params[0], params[1]),
                mReceiver, params[0]
            )
            return false
        }

        override fun onCancelled() {
            mAuthTask = null
            showProgress(false)
        }
    }

    override fun onStringResult(action: String?, option: Int, response: String?, url: String?) {
        when (action) {
            CRUDService.ACTION_REQUEST_SAVE, CRUDService.ACTION_REQUEST_GET -> processRequestGET(option, response, url)
            CRUDService.ACTION_REQUEST_POST, CRUDService.ACTION_REQUEST_PUT, CRUDService.ACTION_REQUEST_DELETE -> {
            }
            CRUDService.ACTION_REQUEST_FORM_DATA -> {
            }
        }
    }

    private fun processRequestGET(option: Int, response: String?, url: String?) {
        when (option) {
            Constantes.SUCCESS_REQUEST -> {
                when (url) {
                    Constantes.USER_INFO_URL -> {
                        DebugLog.log("URL USER_INFO_URL")
                        preferences!!.profile = response
                        requestImage(response)
                    }
                    Constantes.LIST_CONTRACTS_URL -> {
                        DebugLog.log("tabla llena LIST_CONTRACTS_URL")
                        fillArray[0] = true
                    }
                    Constantes.LIST_PERSONAL_URL -> {
                        DebugLog.log("tabla llena LIST_PERSONAL_URL")
                        fillArray[1] = true
                    }
                    Constantes.LIST_PROJECTS_URL -> {
                        DebugLog.log("tabla llena LIST_PROJECTS_URL")
                        fillArray[1] = true
                    }
                    Constantes.LIST_COMMON_OPTIONS_URL -> {
                        DebugLog.log("tabla llena LIST_COMMON_OPTIONS_URL")
                        fillArray[0] = true
                    }
                    Constantes.LIST_CONTRACT_PER_OFFLINE_URL -> {
                        DebugLog.log("tabla llena LIST_CONTRACT_PER_OFFLINE_URL")
//                        fillArray[4] = true
                    }
                }
                verifyFillData()
            }
            Constantes.BAD_REQUEST -> {
            }
            Constantes.UNAUTHORIZED, Constantes.FORBIDDEN -> {
                when (url) {
                    Constantes.LIST_CONTRACTS_URL -> {
                        DebugLog.logW("FORBIDDEN LIST_CONTRACTS_URL")
                        fillArray[0] = true
                    }
                    Constantes.LIST_PERSONAL_URL -> {
                        DebugLog.logW("FORBIDDEN LIST_PERSONAL_URL")
                        fillArray[1] = true
                    }
                    Constantes.LIST_PROJECTS_URL -> {
                        DebugLog.logW("FORBIDDEN LIST_PROJECTS_URL")
                        fillArray[1] = true
                    }
                    Constantes.LIST_COMMON_OPTIONS_URL -> {
                        DebugLog.logW("FORBIDDEN LIST_COMMON_OPTIONS_URL")
                        fillArray[0] = true
                    }
                    Constantes.LIST_CONTRACT_PER_OFFLINE_URL -> {
                        DebugLog.logW("FORBIDDEN LIST_CONTRACT_PER_OFFLINE_URL")
//                        fillArray[4] = true
                    }
                }
                verifyFillData()
            }
            Constantes.REQUEST_NOT_FOUND, Constantes.NOT_INTERNET -> {
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
        DebugLog.logW("verifyFillData flag: $flag")
        if (flag && createProfile) {
            preferences!!.syncDate = Calendar.getInstance().time.time
            showProgress(false)
            goMainActivity()
        } else if (flag) {
            preferences!!.syncDate = Calendar.getInstance().time.time
            //Toast.makeText(this, "Sincronización de datos exitosa", Toast.LENGTH_SHORT).show()
        }
        return flag
    }
}
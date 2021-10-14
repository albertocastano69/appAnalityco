package co.tecno.sersoluciones.analityco.fragments

import android.app.Application
import android.content.Context
import android.content.IntentFilter
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import android.text.InputType
import android.widget.EditText
import androidx.lifecycle.ViewModel
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.preference.EditTextPreference
import androidx.preference.EditTextPreference.OnBindEditTextListener
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import co.com.sersoluciones.facedetectorser.utilities.DebugLog
import co.tecno.sersoluciones.analityco.ApplicationContext
import co.tecno.sersoluciones.analityco.R
import co.tecno.sersoluciones.analityco.models.Expiry
import co.tecno.sersoluciones.analityco.models.RequestPatch
import co.tecno.sersoluciones.analityco.receivers.RequestBroadcastReceiver
import co.tecno.sersoluciones.analityco.receivers.RequestBroadcastReceiver.BroadcastListener
import co.tecno.sersoluciones.analityco.services.CrudIntentService
import co.tecno.sersoluciones.analityco.utilities.Constantes
import co.tecno.sersoluciones.analityco.utilities.MyPreferences
import com.google.gson.Gson
import org.json.JSONException
import org.json.JSONObject
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsViewModel @Inject constructor(
    private val application: Application
) : ViewModel() {

    fun restartDagger() {
        ApplicationContext.restartDagger(application)
    }
}

class SettingFragment : PreferenceFragmentCompat(), OnSharedPreferenceChangeListener,
    BroadcastListener {

    override fun onAttach(context: Context) {
        super.onAttach(context)
        ApplicationContext.analitycoComponent.inject(this)
    }

    @Inject
    lateinit var viewModel: SettingsViewModel
    private var preferences: MyPreferences? = null
    private var expiry: Expiry? = null
    private var isChange = false
    private var requestBroadcastReceiver: RequestBroadcastReceiver? = null
    private var jsonObjectProfile: JSONObject? = null
    private var changeServer = false

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        isChange = false
        preferences = MyPreferences(requireActivity())
        requestBroadcastReceiver = RequestBroadcastReceiver(this)
        try {
            val profile = preferences!!.profile
            jsonObjectProfile = JSONObject(profile)
            jsonObjectProfile!!.getString("Expiry")
            expiry = Gson().fromJson(jsonObjectProfile!!.getString("Expiry"), Expiry::class.java)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        setPreferencesFromResource(R.xml.preferences, rootKey)


        preferenceManager.findPreference<EditTextPreference>("pref_key_company_days")!!.setOnBindEditTextListener { editText
            ->
            editText.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_SIGNED
        }
        preferenceManager.findPreference<EditTextPreference>("pref_key_employer_days")!!.setOnBindEditTextListener { editText
            ->
            editText.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_SIGNED
        }
        preferenceManager.findPreference<EditTextPreference>("pref_key_projects_days")!!.setOnBindEditTextListener { editText
            ->
            editText.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_SIGNED
        }
        preferenceManager.findPreference<EditTextPreference>("pref_key_contracts_days")!!.setOnBindEditTextListener { editText
            ->
            editText.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_SIGNED
        }
        preferenceManager.findPreference<EditTextPreference>("pref_key_personal_days")!!.setOnBindEditTextListener { editText
            ->
            editText.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_SIGNED
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        sharedPreferences?.let {
            isChange = false
            changeServer = false
            var enabled = false
            var dayscom = 21
            var dayspro = 28
            var daysemp = 21
            var dayscon = 14
            var daysper = 7
            var regex = "-?\\d+"
            when (key) {
                "pref_key_company_days" -> {
                    if (sharedPreferences.getString(key, "")!!.matches(regex.toRegex())) {
                        dayscom = Integer.valueOf(sharedPreferences.getString(key, "")!!)
                        expiry!!.Company = dayscom
                        preferences!!.setCompanyDays(expiry!!.Company)
                        isChange = true
                    }
                }
                "pref_key_employer_days" -> {
                    if (sharedPreferences.getString(key, "")!!.matches(regex.toRegex())) {
                        daysemp = Integer.valueOf(sharedPreferences.getString(key, "")!!)
                        expiry!!.Employer = daysemp
                        preferences!!.setEmployerDays(expiry!!.Employer)
                        isChange = true
                    }
                }
                "pref_key_projects_days" -> {
                    if (sharedPreferences.getString(key, "")!!.matches(regex.toRegex())) {
                        dayspro = Integer.valueOf(sharedPreferences.getString(key, "")!!)
                        expiry!!.Project = dayspro
                        preferences!!.setProjectDays(expiry!!.Project)
                        isChange = true
                    }
                }
                "pref_key_contracts_days" -> {
                    if (sharedPreferences.getString(key, "")!!.matches(regex.toRegex())) {
                        dayscon = Integer.valueOf(sharedPreferences.getString(key, "")!!)
                        expiry!!.Contract = dayscon
                        preferences!!.setContractDays(expiry!!.Contract)
                        isChange = true
                    }
                }
                "pref_key_personal_days" -> {
                    if (sharedPreferences.getString(key, "")!!.matches(regex.toRegex())) {
                        daysper = Integer.valueOf(sharedPreferences.getString(key, "")!!)
                        expiry!!.Personal = daysper
                        preferences!!.setPersonalDays(expiry!!.Personal)
                        isChange = true
                    }
                }
                "pref_key_pass_server" -> {
                    val pass = sharedPreferences.getString(key, "")
                    if (pass == requireActivity().getString(R.string.pref_value_password)) {
                        (findPreference("pref_key_ip_server") as EditTextPreference?)!!.isEnabled =
                            true
                        (findPreference("pref_key_port_server") as EditTextPreference?)!!.isEnabled =
                            true
                        (findPreference("pref_key_bool_server") as SwitchPreference?)!!.isEnabled =
                            true
                        (findPreference("pref_key_enable_registers") as SwitchPreference?)!!.isEnabled =
                            true
                    }
                }
                "pref_key_bool_server" -> changeServer = true
                "pref_key_vibration" -> {
                    enabled = sharedPreferences.getBoolean(key, false)
                    preferences!!.setVerifyAdnRegisterSettings(enabled, 1)
                }
                "pref_key_speak" -> {
                    enabled = sharedPreferences.getBoolean(key, false)
                    preferences!!.setVerifyAdnRegisterSettings(enabled, 2)
                }
                "pref_key_default_start" -> {
                    enabled = sharedPreferences.getBoolean(key, false)
                    preferences!!.setVerifyAdnRegisterSettings(enabled, 3)
                }
                "pref_key_default_requeriment_incomplete"->{
                    enabled = sharedPreferences.getBoolean(key, false)
                    preferences!!.setVerifyAdnRegisterSettings(enabled, 4)
                }
                "pref_key_default_entry"->{
                    enabled = sharedPreferences.getBoolean(key, false)
                    preferences!!.setVerifyAdnRegisterSettings(enabled, 5)
                }
                else -> {
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        preferenceScreen.sharedPreferences
            .registerOnSharedPreferenceChangeListener(this)
        val intentFilter = IntentFilter()
        intentFilter.addAction(CrudIntentService.ACTION_REQUEST_PATCH)
        LocalBroadcastManager.getInstance(requireActivity()).registerReceiver(
            requestBroadcastReceiver!!,
            intentFilter
        )
    }

    override fun onPause() {
        super.onPause()
        preferenceScreen.sharedPreferences
            .unregisterOnSharedPreferenceChangeListener(this)
        LocalBroadcastManager.getInstance(requireActivity())
            .unregisterReceiver(requestBroadcastReceiver!!)
        (findPreference("pref_key_pass_server") as EditTextPreference?)!!.text = ""
    }


    override fun onStop() {
        super.onStop()
        if (isChange) {
            val requestPatches = ArrayList<RequestPatch>()
            requestPatches.add(RequestPatch("Expiry", Gson().toJson(expiry)))
            val json = Gson().toJson(requestPatches)
            DebugLog.logW(json)
            sendFormToServer(json)
        }
        if (changeServer) {
            preferences!!.cleanJWT()
            preferences!!.notification = false
            preferences!!.isUserLogin = false
            viewModel.restartDagger()
            requireActivity().finish()
        }
    }

    private fun sendFormToServer(json: String) {
        try {
            val url = "api/User/" + jsonObjectProfile!!.getString("sub")
            CrudIntentService.startRequestPatch(activity, url, json)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    override fun onStringResult(action: String?, option: Int, res: String?, url: String?) {
        when (option) {
            Constantes.SUCCESS_REQUEST -> {
            }
            Constantes.REQUEST_NOT_FOUND -> {
            }
            Constantes.SUCCESS_FILE_UPLOAD -> {
            }
            Constantes.BAD_REQUEST, Constantes.TIME_OUT_REQUEST -> {
            }
        }
    }
}
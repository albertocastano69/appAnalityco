package co.tecno.sersoluciones.analityco

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.preference.PreferenceManager
import co.com.sersoluciones.barcodedetectorser.utilities.DebugLog.logW
import co.tecno.sersoluciones.analityco.adapters.TransportListAdapter
import co.tecno.sersoluciones.analityco.databinding.ActivitySurveyDetailsBinding
import co.tecno.sersoluciones.analityco.models.ModelResponseSurvey
import co.tecno.sersoluciones.analityco.models.ReportSurvey
import co.tecno.sersoluciones.analityco.utilities.Constantes
import co.tecno.sersoluciones.analityco.utilities.MyPreferences
import co.tecno.sersoluciones.analityco.utilities.WebAppInterface
import co.tecno.sersoluciones.analityco.viewmodels.PersonalDailyViewModel
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.android.synthetic.main.toast_survey.*
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList


class SurveyDetailsActivity : AppCompatActivity(), WebAppInterface.WebViewListener {

    @Inject
    lateinit var viewModel: PersonalDailyViewModel

    lateinit var binding: ActivitySurveyDetailsBinding
    private var preferences: MyPreferences? = null
    private var surveyToken: String? = null
    private var projectId: String? = null
    private var optionFeber = false
    private var optionTos = false
    private var optionCongestionNasal = false
    private var optionDolorGaranta = false
    private var optionDificultadRespirar = false
    private var optionFatiga = false
    private var optionEscalofrios = false
    private var optionDolorMusculos = false
    private var optionDigestivos = false
    private var optionPersonWithSymptom_Ok = false
    private var optionPersonWithSymptomNegative = false
    private var optionPersonwithCovidOk = false
    private var optionPersonwithCovidNegative = false
    private var optionCaminando = false
    private var optionBicicleta = false
    private var optionMoto = false
    private var optionAuto = false
    private var optionMasivo = false
    private var optionSitp = false
    private var optionTransporteEmpresa = false
    private var optionBusInter = false
    private var optionTaxi = false
    private var optionHomeOffice = false
    private var optionNoSelected = false
    private var responseState = 0
    private var temperature = 0
    var list: ArrayList<String>? = null
    var listTransport: ArrayList<String>? = null
    var listResponse: ArrayList<ModelResponseSurvey>? = null
    val jsonArray = JSONArray()
    private var personInfoId: String? = null
    var StateRed = false
    var StateOrange = false

    @SuppressLint("SetTextI18n", "SimpleDateFormat")
    override fun onCreate(savedInstanceState: Bundle?) {

        ApplicationContext.analitycoComponent.inject(this)
        super.onCreate(savedInstanceState)
        preferences = MyPreferences(this)
        //viewModel.clearFormData()
        list = ArrayList()
        listTransport = ArrayList()
        listResponse = ArrayList()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_survey_details)
        binding.lifecycleOwner = this
        if (intent.extras != null) {
            if (intent.getStringExtra("mode") != null)
                when (intent.getStringExtra("mode")) {
                    "Details" -> {
                        binding.details.visibility = View.VISIBLE
                        binding.editsurvey.visibility = View.GONE
                        binding.scrollSurvey.visibility = View.GONE
                        binding.loader.visibility = View.GONE
                        val surveyId = intent.getStringExtra("surveyId") ?: "0"
                        personInfoId = intent.getStringExtra("PersonalInfoId") ?: "0"
                        val createDate = intent.getStringExtra("createDate") ?: ""
                        viewModel.configSurveyForm(personInfoId!!.toLong(), createDate)
                    }
                    "Edit" -> {
                        //binding.surveyWebView.visibility = View.VISIBLE
                        binding.loader.visibility = View.GONE
                        val document = intent.getStringExtra("DocumentNumber") ?: "0"
                        val birthday = intent.getStringExtra("BirthDate") ?: "0000"
                        personInfoId = intent.getStringExtra("PersonalInfoId") ?: "0"
                        projectId = intent.getStringExtra("ProjectId") ?: "0"
                        val name = intent.getStringExtra("namePerson")
                        binding.editsurvey.visibility = View.VISIBLE
                        binding.details.visibility = View.GONE
                        binding.scrollSurvey.visibility = View.VISIBLE
                        binding.infoPerson.text = name
                        //viewModel.loadSurveyToken(document, birthday.substring(0, 4))
                    }
                }
            binding.optionNinguna.backgroundTintList =
                ContextCompat.getColorStateList(this, R.color.orange)
            binding.optionNinguna.setTextColor(ContextCompat.getColor(this, R.color.white))
            optionNoSelected = true
            if (optionNoSelected) {
                optionDigestivos = false
                optionDolorMusculos = false
                optionEscalofrios = false
                optionFatiga = false
                optionDificultadRespirar = false
                optionDolorGaranta = false
                optionCongestionNasal = false
                optionTos = false
                optionFeber = false
            }
            binding.safeAnswer.setOnClickListener {
                if (submit()) {
                    val create = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                    val CreateDate = create.format(Date())
                    var sendSymptm = ""
                    if (optionFeber) {
                        list!!.add(getString(R.string.fiebre_de_mas_de_38_grados))
                    }
                    if (optionTos) {
                        list!!.add(getString(R.string.tos))
                    }
                    if (optionCongestionNasal) {
                        list!!.add(getString(R.string.congestion_nasal))
                    }
                    if (optionDolorGaranta) {
                        list!!.add(getString(R.string.dolor_de_garganta))
                    }
                    if (optionDificultadRespirar) {
                        list!!.add(getString(R.string.dificultad_para_respirar))
                    }
                    if (optionFatiga) {
                        list!!.add(getString(R.string.fatiga))
                    }
                    if (optionEscalofrios) {
                        list!!.add(getString(R.string.escalofrios))
                    }
                    if (optionDolorMusculos) {
                        list!!.add(getString(R.string.dolor_de_musculos))
                    }
                    if (optionDigestivos) {
                        list!!.add(getString(R.string.problemas_digestivos))
                    }
                    if (optionPersonWithSymptom_Ok) {
                        list!!.add(getString(R.string.contact_with_symptoms))
                    }
                    if (optionPersonwithCovidOk) {
                        list!!.add(getString(R.string.contagious_contact))
                    }
                    if (temperature != null) {
                        //val temp =  (binding.edtTemperature.text).toString())

                        list!!.add("Temperatura=" + (binding.edtTemperature.text).toString())
                    }
                    logW("temperatura: " + temperature)

                    if (optionNoSelected && (!optionPersonWithSymptom_Ok && !optionPersonwithCovidOk)) {
                        list!!.clear()
                        responseState = 0
                        sendSymptm = ""
                    } else {
                        responseColor()
                    }
                    if (optionHomeOffice) {
                        listTransport!!.clear()
                    } else {
                        listTransport()
                    }
                    if (list!!.size >= 1) {
                        for (item in list!!) {
                            sendSymptm += item
                        }
                        if (StateOrange) {
                            val report = ReportSurvey(
                                true,
                                list,
                                optionPersonWithSymptom_Ok,
                                optionPersonwithCovidOk,
                                false,
                                listTransport,
                                "[]",
                                Gson().toJson(listResponse).toString(),
                                personInfoId!!.toLong(),
                                responseState,
                                CreateDate,
                                ""
                            )
                            logW("Json report: ${Gson().toJson(report)}")

                            val builder = GsonBuilder()
                            builder.disableHtmlEscaping()
                            val gson = builder.create()
                            val insertData =  gson.toJson(report)
                            viewModel.insertSurveyOffline(insertData, report)
                            showAlertDialog()
                        }
                        if (StateRed) {
                            val report = ReportSurvey(
                                true,
                                list,
                                optionPersonWithSymptom_Ok,
                                optionPersonwithCovidOk,
                                false,
                                listTransport,
                                Gson().toJson(listResponse).toString(),
                                "[]",
                                personInfoId!!.toLong(),
                                responseState,
                                CreateDate,
                                ""
                            )
                            logW("Json report: ${Gson().toJson(report)}")

                            val builder = GsonBuilder()
                            builder.disableHtmlEscaping()
                            val gson = builder.create()
                            val insertData =  gson.toJson(report)
                            viewModel.insertSurveyOffline(insertData, report)
                            showAlertDialog()

                        }
                    } else {
                        if (responseState == 0) {
                            val report = ReportSurvey(
                                true,
                                list,
                                optionPersonWithSymptom_Ok,
                                optionPersonwithCovidOk,
                                false,
                                listTransport,
                                "[]",
                                "[]",
                                personInfoId!!.toLong(),
                                responseState,
                                CreateDate,
                                ""
                            )

                            val builder = GsonBuilder()
                            builder.disableHtmlEscaping()
                            val gson = builder.create()
                            val insertData =  gson.toJson(report)

                            viewModel.insertSurveyOffline(insertData, report)

                            showAlertDialog()

                        }
                    }
                }
            }
        } else finish()

        viewModel.surveyForm.observe(this, Observer {
            it.let {
                binding.surveyForm = it
            }
        })

        viewModel.surveyToken.observe(this, Observer {
            logW("actualiza token... $it")
            it.let {
                surveyToken = it
                initWebView()
            }
        })

        viewModel.transportList.observe(this, Observer {
            logW("test ${it.size}")
            val adapter = TransportListAdapter()
            binding.transportList.adapter = adapter
            it.let {
                adapter.data = it
            }
        })

    }

    private fun showAlertDialog() {
        val inflater = layoutInflater
        val view = inflater.inflate(R.layout.alertdialog_survey, null)
        val builder = AlertDialog.Builder(this)
        val title = view.findViewById<TextView>(R.id.title_survey)
        val btn_cancel = view.findViewById<Button>(R.id.closeSurvey)
        builder.setView(view)
        val alertDialog = builder.create()
        alertDialog.show()
        alertDialog.setCancelable(false)
        when {
            StateRed -> {
                title.text = getString(R.string.survey_rejected)
                title.background = ContextCompat.getDrawable(this, R.color.red)
            }
            StateOrange -> {
                title.text = getString(R.string.survey_orange)
                title.background = ContextCompat.getDrawable(this, R.color.orange)
            }
            else -> {
                title.text = getString(R.string.survey_ok)
                title.background = ContextCompat.getDrawable(this, R.color.bar_decoded)
            }
        }
        btn_cancel.setOnClickListener {
            when {
                StateRed -> {
                    setResult(State_2)
                }
                StateOrange -> {
                    setResult(State_1)
                }
                else -> {
                    setResult(State_0)
                }
            }
            alertDialog.dismiss()
            finish()

        }
    }

    private fun responseColor() {
        if (optionFeber) {
            listResponse!!.add(
                ModelResponseSurvey(
                    1,
                    true,
                    getString(R.string.fiebre_de_mas_de_38_grados),
                    ""
                )
            )
            StateRed = true
            StateOrange = false
            responseState = 2
        }
        if (optionTos) {
            listResponse!!.add(ModelResponseSurvey(2, true, getString(R.string.tos), ""))
            StateOrange = true
            StateRed = false
            responseState = 1
        }
        if (optionCongestionNasal) {
            listResponse!!.add(
                ModelResponseSurvey(
                    3,
                    true,
                    getString(R.string.congestion_nasal),
                    ""
                )
            )
            StateOrange = true
            StateRed = false
            responseState = 1
        }
        if (optionDolorGaranta) {
            listResponse!!.add(
                ModelResponseSurvey(
                    4,
                    true,
                    getString(R.string.dolor_de_garganta),
                    ""
                )
            )
            StateOrange = true
            StateRed = false
            responseState = 1
        }
        if (optionDificultadRespirar) {
            listResponse!!.add(
                ModelResponseSurvey(
                    5,
                    true,
                    getString(R.string.dificultad_para_respirar),
                    ""
                )
            )
            StateRed = true
            StateOrange = false
            responseState = 2
        }
        if (optionFatiga) {
            listResponse!!.add(ModelResponseSurvey(6, true, getString(R.string.fatiga), ""))
            StateRed = true
            StateOrange = false
            responseState = 2
        }
        if (optionEscalofrios) {
            listResponse!!.add(ModelResponseSurvey(7, true, getString(R.string.escalofrios), ""))
            StateRed = true
            StateOrange = false
            responseState = 2
        }
        if (optionDolorMusculos) {
            listResponse!!.add(
                ModelResponseSurvey(
                    8,
                    true,
                    getString(R.string.dolor_de_musculos),
                    ""
                )
            )
            StateOrange = true
            StateRed = false
            responseState = 1
        }
        if (optionDigestivos) {
            listResponse!!.add(
                ModelResponseSurvey(
                    8,
                    true,
                    getString(R.string.problemas_digestivos),
                    ""
                )
            )
            StateOrange = true
            StateRed = false
            responseState = 1
        }
        if (optionPersonWithSymptom_Ok) {
            listResponse!!.add(
                ModelResponseSurvey(
                    10,
                    true,
                    getString(R.string.contact_with_symptoms),
                    ""
                )
            )
            StateOrange = true
            StateRed = false
            responseState = 1
        }
        if (optionPersonwithCovidOk) {
            listResponse!!.add(
                ModelResponseSurvey(
                    11,
                    true,
                    getString(R.string.contagious_contact),
                    ""
                )
            )
            StateRed = true
            StateOrange = false
            responseState = 2
        }
        if ((optionTos && optionFeber) || (optionTos && optionCongestionNasal) || (optionTos && optionDolorGaranta) || (optionTos && optionDificultadRespirar) || (optionTos && optionFatiga) || (optionTos && optionEscalofrios) || (optionTos && optionDolorMusculos) || (optionTos && optionPersonWithSymptom_Ok) || (optionTos && optionPersonwithCovidOk)) {
            StateRed = true
            StateOrange = false
            responseState = 2
        }
        if ((optionCongestionNasal && optionFeber) || (optionTos && optionCongestionNasal) || (optionCongestionNasal && optionDolorGaranta) || (optionCongestionNasal && optionDificultadRespirar) || (optionCongestionNasal && optionFatiga) || (optionCongestionNasal && optionEscalofrios) || (optionCongestionNasal && optionDolorMusculos) || (optionCongestionNasal && optionPersonWithSymptom_Ok) || (optionCongestionNasal && optionPersonwithCovidOk)) {
            StateRed = true
            StateOrange = false
            responseState = 2
        }
        if ((optionDolorGaranta && optionFeber) || (optionTos && optionDolorGaranta) || (optionCongestionNasal && optionDolorGaranta) || (optionDolorGaranta && optionDificultadRespirar) || (optionDolorGaranta && optionFatiga) || (optionDolorGaranta && optionEscalofrios) || (optionDolorGaranta && optionDolorMusculos) || (optionDolorGaranta && optionPersonWithSymptom_Ok) || (optionDolorGaranta && optionPersonwithCovidOk)) {
            StateRed = true
            StateOrange = false
            responseState = 2
        }
        if ((optionDolorMusculos && optionFeber) || (optionTos && optionDolorMusculos) || (optionDolorMusculos && optionDolorGaranta) || (optionDolorMusculos && optionDificultadRespirar) || (optionDolorMusculos && optionFatiga) || (optionDolorMusculos && optionEscalofrios) || (optionDolorGaranta && optionDolorMusculos) || (optionDolorMusculos && optionPersonWithSymptom_Ok) || (optionDolorMusculos && optionPersonwithCovidOk)) {
            StateRed = true
            StateOrange = false
            responseState = 2
        }
        if ((optionPersonWithSymptom_Ok && optionFeber) || (optionTos && optionPersonWithSymptom_Ok) || (optionPersonWithSymptom_Ok && optionDolorGaranta) || (optionPersonWithSymptom_Ok && optionDificultadRespirar) || (optionPersonWithSymptom_Ok && optionFatiga) || (optionPersonWithSymptom_Ok && optionEscalofrios) || (optionPersonWithSymptom_Ok && optionDolorMusculos) || (optionDolorMusculos && optionPersonWithSymptom_Ok) || (optionPersonWithSymptom_Ok && optionPersonwithCovidOk)) {
            StateRed = true
            StateOrange = false
            responseState = 2
        }
    }

    private fun listTransport() {
        if (optionCaminando) {
            listTransport!!.add(getString(R.string.caminando))
        }
        if (optionBicicleta) {
            listTransport!!.add(getString(R.string.biclicleta))
        }
        if (optionMoto) {
            listTransport!!.add(getString(R.string.moto))
        }
        if (optionAuto) {
            listTransport!!.add(getString(R.string.auto))
        }
        if (optionMasivo) {
            listTransport!!.add(getString(R.string.publico))
        }
        if (optionSitp) {
            listTransport!!.add(getString(R.string.sitp))
        }
        if (optionMasivo) {
            listTransport!!.add(getString(R.string.publico))
        }
        if (optionBusInter) {
            listTransport!!.add(getString(R.string.bus))
        }
        if (optionTransporteEmpresa) {
            listTransport!!.add(getString(R.string.empresa))
        }
        if (optionTaxi) {
            listTransport!!.add(getString(R.string.taxi))
        }
    }

    @SuppressLint("SetTextI18n")
    private fun submit(): Boolean {
        var send = true
        var focusView: View? = null
        if (!optionPersonWithSymptom_Ok && !optionPersonWithSymptomNegative) {
            send = false
            focusView = binding.questionThree
        } else if (!optionPersonwithCovidOk && !optionPersonwithCovidNegative) {
            send = false
            focusView = binding.questionFour
        } else if (!optionCaminando && !optionBicicleta && !optionAuto && !optionMoto && !optionMasivo && !optionSitp && !optionBusInter && !optionTransporteEmpresa && !optionTaxi && !optionHomeOffice) {
            send = false
            focusView = binding.questionTransport
        }
        if (!send) {
            focusView!!.requestFocus()
            val layout = layoutInflater.inflate(R.layout.toast_survey, toast_layout_root)
            val myToast = Toast(applicationContext)
            myToast.duration = Toast.LENGTH_LONG
            myToast.setGravity(Gravity.BOTTOM, 0, 0)
            myToast.view = layout//setting the view of custom toast layout
            myToast.show()
        }
        return send
    }

    private fun initWebView() {
        if (surveyToken == null) return
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val domain = if (!sharedPreferences.getBoolean("pref_key_bool_server", false))
            "https://acceso.analityco.com/SymptomReport"
        else "http://acceso-stg.analityco.com/${Constantes.URL_EDIT_SURVEY}"
        val cookieManager = CookieManager.getInstance()
        cookieManager.setAcceptCookie(true)
        cookieManager.setAcceptThirdPartyCookies(binding.surveyWebView, true)
        cookieManager.setCookie(domain, "SymptomReportToken=$surveyToken")
        cookieManager.setCookie(domain, "ProjectId=$projectId")
        binding.surveyWebView.webViewClient = WebViewClient()
        binding.surveyWebView.settings.javaScriptEnabled = true
        binding.surveyWebView.webChromeClient = WebChromeClient()
        binding.surveyWebView.settings.loadsImagesAutomatically = true
        binding.surveyWebView.addJavascriptInterface(WebAppInterface(this), "Android")
        binding.surveyWebView.loadUrl(domain)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        viewModel.clearFormData()
    }

    override fun onCloseWebView() {
        viewModel.loadSurveyFromServer()
        //setResult(RELOAD_SURVEY)
        finish()
    }

    fun onClickOption(v: View) {
        when (v.id) {
            R.id.option_feber -> {
                if (optionFeber) {
                    optionFeber = false
                    v.backgroundTintList =
                        ContextCompat.getColorStateList(this, R.color.colorUnderline)
                    binding.optionFeber.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.black_alpha
                        )
                    )
                    list!!.remove(getString(R.string.fiebre_de_mas_de_38_grados))
                } else {
                    optionFeber = true
                    v.backgroundTintList =
                        ContextCompat.getColorStateList(this, R.color.orange)
                    optionNoSelected = false
                    binding.optionNinguna.backgroundTintList =
                        ContextCompat.getColorStateList(this, R.color.colorUnderline)
                    binding.optionFeber.setTextColor(ContextCompat.getColor(this, R.color.white))
                    binding.optionNinguna.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.black_alpha
                        )
                    )
                }

            }
            R.id.option_tos -> {
                if (optionTos) {
                    optionTos = false
                    v.backgroundTintList =
                        ContextCompat.getColorStateList(this, R.color.colorUnderline)
                    binding.optionTos.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.black_alpha
                        )
                    )
                    list!!.remove(getString(R.string.tos))
                } else {
                    optionTos = true
                    v.backgroundTintList =
                        ContextCompat.getColorStateList(this, R.color.orange)
                    optionNoSelected = false
                    binding.optionNinguna.backgroundTintList =
                        ContextCompat.getColorStateList(this, R.color.colorUnderline)
                    binding.optionTos.setTextColor(ContextCompat.getColor(this, R.color.white))
                    binding.optionNinguna.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.black_alpha
                        )
                    )
                }

            }
            R.id.option_congestion_nasal -> {
                if (optionCongestionNasal) {
                    optionCongestionNasal = false
                    v.backgroundTintList =
                        ContextCompat.getColorStateList(this, R.color.colorUnderline)
                    binding.optionCongestionNasal.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.black_alpha
                        )
                    )
                    list!!.remove(getString(R.string.congestion_nasal))
                } else {
                    optionCongestionNasal = true
                    v.backgroundTintList =
                        ContextCompat.getColorStateList(this, R.color.orange)
                    optionNoSelected = false
                    binding.optionNinguna.backgroundTintList =
                        ContextCompat.getColorStateList(this, R.color.colorUnderline)
                    binding.optionCongestionNasal.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.white
                        )
                    )
                    binding.optionNinguna.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.black_alpha
                        )
                    )
                }
            }
            R.id.option_dolor_garganta -> {
                if (optionDolorGaranta) {
                    optionDolorGaranta = false
                    v.backgroundTintList =
                        ContextCompat.getColorStateList(this, R.color.colorUnderline)
                    binding.optionDolorGarganta.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.black_alpha
                        )
                    )
                    list!!.remove(getString(R.string.dolor_de_garganta))
                } else {
                    optionDolorGaranta = true
                    v.backgroundTintList =
                        ContextCompat.getColorStateList(this, R.color.orange)
                    optionNoSelected = false
                    binding.optionNinguna.backgroundTintList =
                        ContextCompat.getColorStateList(this, R.color.colorUnderline)
                    binding.optionDolorGarganta.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.white
                        )
                    )
                    binding.optionNinguna.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.black_alpha
                        )
                    )
                }
            }
            R.id.option_dificultad_respirar -> {
                if (optionDificultadRespirar) {
                    optionDificultadRespirar = false
                    v.backgroundTintList =
                        ContextCompat.getColorStateList(this, R.color.colorUnderline)
                    binding.optionDificultadRespirar.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.black_alpha
                        )
                    )
                    list!!.remove(getString(R.string.dificultad_para_respirar))
                } else {
                    optionDificultadRespirar = true
                    v.backgroundTintList =
                        ContextCompat.getColorStateList(this, R.color.orange)
                    optionNoSelected = false
                    binding.optionNinguna.backgroundTintList =
                        ContextCompat.getColorStateList(this, R.color.colorUnderline)
                    binding.optionDificultadRespirar.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.white
                        )
                    )
                    binding.optionNinguna.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.black_alpha
                        )
                    )
                }
            }
            R.id.option_fatiga -> {
                if (optionFatiga) {
                    optionFatiga = false
                    v.backgroundTintList =
                        ContextCompat.getColorStateList(this, R.color.colorUnderline)
                    binding.optionFatiga.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.black_alpha
                        )
                    )
                    list!!.remove(getString(R.string.fatiga))
                } else {
                    optionFatiga = true
                    v.backgroundTintList =
                        ContextCompat.getColorStateList(this, R.color.orange)
                    optionNoSelected = false
                    binding.optionNinguna.backgroundTintList =
                        ContextCompat.getColorStateList(this, R.color.colorUnderline)
                    binding.optionFatiga.setTextColor(ContextCompat.getColor(this, R.color.white))
                    binding.optionNinguna.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.black_alpha
                        )
                    )
                }
            }
            R.id.option_escalofrios -> {
                if (optionEscalofrios) {
                    optionEscalofrios = false
                    v.backgroundTintList =
                        ContextCompat.getColorStateList(this, R.color.colorUnderline)
                    optionNoSelected = false
                    binding.optionNinguna.backgroundTintList =
                        ContextCompat.getColorStateList(this, R.color.colorUnderline)
                    binding.optionEscalofrios.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.black_alpha
                        )
                    )
                    list!!.remove(getString(R.string.escalofrios))
                } else {
                    optionEscalofrios = true
                    v.backgroundTintList =
                        ContextCompat.getColorStateList(this, R.color.orange)
                    optionNoSelected = false
                    binding.optionNinguna.backgroundTintList =
                        ContextCompat.getColorStateList(this, R.color.colorUnderline)
                    binding.optionEscalofrios.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.white
                        )
                    )
                    binding.optionEscalofrios.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.white
                        )
                    )
                    binding.optionNinguna.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.black_alpha
                        )
                    )
                }
            }
            R.id.option_dolor_musculos -> {
                if (optionDolorMusculos) {
                    optionDolorMusculos = false
                    v.backgroundTintList =
                        ContextCompat.getColorStateList(this, R.color.colorUnderline)
                    binding.optionDolorMusculos.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.black_alpha
                        )
                    )
                    list!!.remove(getString(R.string.dolor_de_musculos))
                } else {
                    optionDolorMusculos = true
                    v.backgroundTintList =
                        ContextCompat.getColorStateList(this, R.color.orange)
                    optionNoSelected = false
                    binding.optionNinguna.backgroundTintList =
                        ContextCompat.getColorStateList(this, R.color.colorUnderline)
                    binding.optionNinguna.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.black_alpha
                        )
                    )
                    binding.optionDolorMusculos.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.white
                        )
                    )
                }
            }
            R.id.option_digestivo -> {
                if (optionDigestivos) {
                    optionDigestivos = false
                    v.backgroundTintList =
                        ContextCompat.getColorStateList(this, R.color.colorUnderline)
                    binding.optionDigestivo.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.black_alpha
                        )
                    )
                    list!!.remove(getString(R.string.problemas_digestivos))
                } else {
                    optionDigestivos = true
                    v.backgroundTintList =
                        ContextCompat.getColorStateList(this, R.color.orange)
                    optionNoSelected = false
                    binding.optionNinguna.backgroundTintList =
                        ContextCompat.getColorStateList(this, R.color.colorUnderline)
                    binding.optionNinguna.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.black_alpha
                        )
                    )
                    binding.optionDigestivo.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.white
                        )
                    )
                }
            }
            R.id.option_ninguna -> {
                if (optionNoSelected) {
                    optionNoSelected = false
                    v.backgroundTintList =
                        ContextCompat.getColorStateList(this, R.color.colorUnderline)
                    binding.optionNinguna.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.black_alpha
                        )
                    )
                } else {
                    optionNoSelected = true
                    v.backgroundTintList =
                        ContextCompat.getColorStateList(this, R.color.orange)
                    binding.optionNinguna.setTextColor(ContextCompat.getColor(this, R.color.white))
                    optionDigestivos = false
                    optionDolorMusculos = false
                    optionEscalofrios = false
                    optionFatiga = false
                    optionDificultadRespirar = false
                    optionDolorGaranta = false
                    optionCongestionNasal = false
                    optionTos = false
                    optionFeber = false
                    list!!.clear()
                    binding.optionFeber.backgroundTintList =
                        ContextCompat.getColorStateList(this, R.color.colorUnderline)
                    binding.optionFeber.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.black_alpha
                        )
                    )
                    binding.optionTos.backgroundTintList =
                        ContextCompat.getColorStateList(this, R.color.colorUnderline)
                    binding.optionTos.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.black_alpha
                        )
                    )
                    binding.optionCongestionNasal.backgroundTintList =
                        ContextCompat.getColorStateList(this, R.color.colorUnderline)
                    binding.optionCongestionNasal.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.black_alpha
                        )
                    )
                    binding.optionDolorGarganta.backgroundTintList =
                        ContextCompat.getColorStateList(this, R.color.colorUnderline)
                    binding.optionDolorGarganta.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.black_alpha
                        )
                    )
                    binding.optionDificultadRespirar.backgroundTintList =
                        ContextCompat.getColorStateList(this, R.color.colorUnderline)
                    binding.optionDificultadRespirar.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.black_alpha
                        )
                    )
                    binding.optionFatiga.backgroundTintList =
                        ContextCompat.getColorStateList(this, R.color.colorUnderline)
                    binding.optionFatiga.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.black_alpha
                        )
                    )
                    binding.optionEscalofrios.backgroundTintList =
                        ContextCompat.getColorStateList(this, R.color.colorUnderline)
                    binding.optionEscalofrios.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.black_alpha
                        )
                    )
                    binding.optionDolorMusculos.backgroundTintList =
                        ContextCompat.getColorStateList(this, R.color.colorUnderline)
                    binding.optionDolorMusculos.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.black_alpha
                        )
                    )
                    binding.optionDigestivo.backgroundTintList =
                        ContextCompat.getColorStateList(this, R.color.colorUnderline)
                    binding.optionDigestivo.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.black_alpha
                        )
                    )
                }
            }
            R.id.option_person_with_symptom_ok -> {
                if (optionPersonWithSymptom_Ok) {
                    optionPersonWithSymptom_Ok = false
                    v.backgroundTintList =
                        ContextCompat.getColorStateList(this, R.color.colorUnderline)
                    binding.optionPersonWithSymptomOk.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.black_alpha
                        )
                    )
                } else {
                    optionPersonWithSymptom_Ok = true
                    v.backgroundTintList =
                        ContextCompat.getColorStateList(this, R.color.orange)
                    binding.optionPersonWithSymptomOk.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.white
                        )
                    )
                    optionPersonWithSymptomNegative = false
                    binding.optionPersonWithSymptomNegativa.backgroundTintList =
                        ContextCompat.getColorStateList(this, R.color.colorUnderline)
                    binding.optionPersonWithSymptomNegativa.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.black_alpha
                        )
                    )
                }
            }
            R.id.option_person_with_symptom_negativa -> {
                if (optionPersonWithSymptomNegative) {
                    optionPersonWithSymptomNegative = false
                    v.backgroundTintList =
                        ContextCompat.getColorStateList(this, R.color.colorUnderline)
                    binding.optionPersonWithSymptomNegativa.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.black_alpha
                        )
                    )
                } else {
                    optionPersonWithSymptomNegative = true
                    v.backgroundTintList =
                        ContextCompat.getColorStateList(this, R.color.orange)
                    binding.optionPersonWithSymptomNegativa.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.white
                        )
                    )
                    optionPersonWithSymptom_Ok = false
                    binding.optionPersonWithSymptomOk.backgroundTintList =
                        ContextCompat.getColorStateList(this, R.color.colorUnderline)
                    binding.optionPersonWithSymptomOk.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.black_alpha
                        )
                    )
                }
            }
            R.id.option_person_with_covid_ok -> {
                if (optionPersonwithCovidOk) {
                    optionPersonwithCovidOk = false
                    v.backgroundTintList =
                        ContextCompat.getColorStateList(this, R.color.colorUnderline)
                    binding.optionPersonWithCovidOk.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.black_alpha
                        )
                    )
                } else {
                    optionPersonwithCovidOk = true
                    v.backgroundTintList =
                        ContextCompat.getColorStateList(this, R.color.orange)
                    binding.optionPersonWithCovidOk.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.white
                        )
                    )
                    optionPersonwithCovidNegative = false
                    binding.optionPersonWithCovidNegativa.backgroundTintList =
                        ContextCompat.getColorStateList(this, R.color.colorUnderline)
                    binding.optionPersonWithCovidNegativa.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.black_alpha
                        )
                    )
                }
            }
            R.id.option_person_with_covid_negativa -> {
                if (optionPersonwithCovidNegative) {
                    optionPersonwithCovidNegative = false
                    v.backgroundTintList =
                        ContextCompat.getColorStateList(this, R.color.colorUnderline)
                    binding.optionPersonWithSymptomNegativa.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.black_alpha
                        )
                    )
                } else {
                    optionPersonwithCovidNegative = true
                    v.backgroundTintList =
                        ContextCompat.getColorStateList(this, R.color.orange)
                    binding.optionPersonWithCovidNegativa.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.white
                        )
                    )
                    optionPersonwithCovidOk = false
                    binding.optionPersonWithCovidOk.backgroundTintList =
                        ContextCompat.getColorStateList(this, R.color.colorUnderline)
                    binding.optionPersonWithCovidOk.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.black_alpha
                        )
                    )
                }
            }
            R.id.option_caminando -> {
                if (optionCaminando) {
                    optionCaminando = false
                    v.backgroundTintList =
                        ContextCompat.getColorStateList(this, R.color.colorUnderline)
                    binding.optionCaminando.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.black_alpha
                        )
                    )
                } else {
                    optionCaminando = true
                    v.backgroundTintList =
                        ContextCompat.getColorStateList(this, R.color.orange)
                    binding.optionCaminando.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.white
                        )
                    )
                    optionHomeOffice = false
                    binding.optionHomeOffice.backgroundTintList =
                        ContextCompat.getColorStateList(this, R.color.colorUnderline)
                    binding.optionHomeOffice.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.black_alpha
                        )
                    )
                }

            }
            R.id.option_bicicleta -> {
                if (optionBicicleta) {
                    optionBicicleta = false
                    v.backgroundTintList =
                        ContextCompat.getColorStateList(this, R.color.colorUnderline)
                    binding.optionBicicleta.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.black_alpha
                        )
                    )
                } else {
                    optionBicicleta = true
                    v.backgroundTintList =
                        ContextCompat.getColorStateList(this, R.color.orange)
                    binding.optionBicicleta.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.white
                        )
                    )
                    optionHomeOffice = false
                    binding.optionHomeOffice.backgroundTintList =
                        ContextCompat.getColorStateList(this, R.color.colorUnderline)
                    binding.optionHomeOffice.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.black_alpha
                        )
                    )
                }
            }
            R.id.option_moto -> {
                if (optionMoto) {
                    optionMoto = false
                    v.backgroundTintList =
                        ContextCompat.getColorStateList(this, R.color.colorUnderline)
                    binding.optionMoto.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.black_alpha
                        )
                    )
                } else {
                    optionMoto = true
                    v.backgroundTintList =
                        ContextCompat.getColorStateList(this, R.color.orange)
                    binding.optionMoto.setTextColor(ContextCompat.getColor(this, R.color.white))
                    optionHomeOffice = false
                    binding.optionHomeOffice.backgroundTintList =
                        ContextCompat.getColorStateList(this, R.color.colorUnderline)
                    binding.optionHomeOffice.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.black_alpha
                        )
                    )
                }
            }
            R.id.option_auto -> {
                if (optionAuto) {
                    optionAuto = false
                    v.backgroundTintList =
                        ContextCompat.getColorStateList(this, R.color.colorUnderline)
                    binding.optionAuto.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.black_alpha
                        )
                    )
                } else {
                    optionAuto = true
                    v.backgroundTintList =
                        ContextCompat.getColorStateList(this, R.color.orange)
                    binding.optionAuto.setTextColor(ContextCompat.getColor(this, R.color.white))
                    optionHomeOffice = false
                    binding.optionHomeOffice.backgroundTintList =
                        ContextCompat.getColorStateList(this, R.color.colorUnderline)
                    binding.optionHomeOffice.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.black_alpha
                        )
                    )
                }
            }
            R.id.option_transmilenio -> {
                if (optionMasivo) {
                    optionMasivo = false
                    v.backgroundTintList =
                        ContextCompat.getColorStateList(this, R.color.colorUnderline)
                    binding.optionTransmilenio.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.black_alpha
                        )
                    )
                } else {
                    optionMasivo = true
                    v.backgroundTintList =
                        ContextCompat.getColorStateList(this, R.color.orange)
                    binding.optionTransmilenio.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.white
                        )
                    )
                    optionHomeOffice = false
                    binding.optionHomeOffice.backgroundTintList =
                        ContextCompat.getColorStateList(this, R.color.colorUnderline)
                    binding.optionHomeOffice.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.black_alpha
                        )
                    )
                }
            }
            R.id.option_sitp -> {
                if (optionSitp) {
                    optionSitp = false
                    v.backgroundTintList =
                        ContextCompat.getColorStateList(this, R.color.colorUnderline)
                    binding.optionSitp.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.black_alpha
                        )
                    )
                } else {
                    optionSitp = true
                    v.backgroundTintList =
                        ContextCompat.getColorStateList(this, R.color.orange)
                    binding.optionSitp.setTextColor(ContextCompat.getColor(this, R.color.white))
                    optionHomeOffice = false
                    binding.optionHomeOffice.backgroundTintList =
                        ContextCompat.getColorStateList(this, R.color.colorUnderline)
                    binding.optionHomeOffice.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.black_alpha
                        )
                    )
                }
            }
            R.id.option_bus -> {
                if (optionBusInter) {
                    optionBusInter = false
                    v.backgroundTintList =
                        ContextCompat.getColorStateList(this, R.color.colorUnderline)
                    binding.optionBus.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.black_alpha
                        )
                    )
                } else {
                    optionBusInter = true
                    v.backgroundTintList =
                        ContextCompat.getColorStateList(this, R.color.orange)
                    binding.optionBus.setTextColor(ContextCompat.getColor(this, R.color.white))
                    optionHomeOffice = false
                    binding.optionHomeOffice.backgroundTintList =
                        ContextCompat.getColorStateList(this, R.color.colorUnderline)
                    binding.optionHomeOffice.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.black_alpha
                        )
                    )
                }
            }
            R.id.option_transport_empresa -> {
                if (optionTransporteEmpresa) {
                    optionTransporteEmpresa = false
                    v.backgroundTintList =
                        ContextCompat.getColorStateList(this, R.color.colorUnderline)
                    binding.optionTransportEmpresa.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.black_alpha
                        )
                    )
                } else {
                    optionTransporteEmpresa = true
                    v.backgroundTintList =
                        ContextCompat.getColorStateList(this, R.color.orange)
                    binding.optionTransportEmpresa.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.white
                        )
                    )
                    optionHomeOffice = false
                    binding.optionHomeOffice.backgroundTintList =
                        ContextCompat.getColorStateList(this, R.color.colorUnderline)
                    binding.optionHomeOffice.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.black_alpha
                        )
                    )
                }
            }
            R.id.option_taxi -> {
                if (optionTaxi) {
                    optionTaxi = false
                    v.backgroundTintList =
                        ContextCompat.getColorStateList(this, R.color.colorUnderline)
                    binding.optionTaxi.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.black_alpha
                        )
                    )
                } else {
                    optionTaxi = true
                    v.backgroundTintList =
                        ContextCompat.getColorStateList(this, R.color.orange)
                    binding.optionTaxi.setTextColor(ContextCompat.getColor(this, R.color.white))
                    optionHomeOffice = false
                    binding.optionHomeOffice.backgroundTintList =
                        ContextCompat.getColorStateList(this, R.color.colorUnderline)
                    binding.optionHomeOffice.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.black_alpha
                        )
                    )
                }
            }
            R.id.option_home_office -> {
                if (optionHomeOffice) {
                    optionHomeOffice = false
                    v.backgroundTintList =
                        ContextCompat.getColorStateList(this, R.color.colorUnderline)
                    binding.optionHomeOffice.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.black_alpha
                        )
                    )
                } else {
                    optionHomeOffice = true
                    v.backgroundTintList =
                        ContextCompat.getColorStateList(this, R.color.orange)
                    binding.optionHomeOffice.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.white
                        )
                    )
                    optionTaxi = false
                    optionTransporteEmpresa = false
                    optionBusInter = false
                    optionSitp = false
                    optionMasivo = false
                    optionAuto = false
                    optionMoto = false
                    optionBicicleta = false
                    optionCaminando = false
                    binding.optionCaminando.backgroundTintList =
                        ContextCompat.getColorStateList(this, R.color.colorUnderline)
                    binding.optionCaminando.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.black_alpha
                        )
                    )
                    binding.optionBicicleta.backgroundTintList =
                        ContextCompat.getColorStateList(this, R.color.colorUnderline)
                    binding.optionBicicleta.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.black_alpha
                        )
                    )
                    binding.optionMoto.backgroundTintList =
                        ContextCompat.getColorStateList(this, R.color.colorUnderline)
                    binding.optionMoto.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.black_alpha
                        )
                    )
                    binding.optionAuto.backgroundTintList =
                        ContextCompat.getColorStateList(this, R.color.colorUnderline)
                    binding.optionAuto.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.black_alpha
                        )
                    )
                    binding.optionTransmilenio.backgroundTintList =
                        ContextCompat.getColorStateList(this, R.color.colorUnderline)
                    binding.optionTransmilenio.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.black_alpha
                        )
                    )
                    binding.optionSitp.backgroundTintList =
                        ContextCompat.getColorStateList(this, R.color.colorUnderline)
                    binding.optionSitp.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.black_alpha
                        )
                    )
                    binding.optionBus.backgroundTintList =
                        ContextCompat.getColorStateList(this, R.color.colorUnderline)
                    binding.optionBus.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.black_alpha
                        )
                    )
                    binding.optionTransportEmpresa.backgroundTintList =
                        ContextCompat.getColorStateList(this, R.color.colorUnderline)
                    binding.optionTransportEmpresa.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.black_alpha
                        )
                    )
                    binding.optionTaxi.backgroundTintList =
                        ContextCompat.getColorStateList(this, R.color.colorUnderline)
                    binding.optionTaxi.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.black_alpha
                        )
                    )
                }
            }
        }
    }

    companion object {
        const val State_0 = 207
        const val State_1 = 208
        const val State_2 = 209
    }
}
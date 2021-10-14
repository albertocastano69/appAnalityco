package co.tecno.sersoluciones.analityco.viewmodels

import android.content.Context
import android.os.Build
import android.speech.tts.TextToSpeech
import android.text.format.DateFormat
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.work.WorkManager
import co.com.sersoluciones.barcodedetectorser.utilities.DebugLog.log
import co.com.sersoluciones.barcodedetectorser.utilities.DebugLog.logW
import co.tecno.sersoluciones.analityco.R
import co.tecno.sersoluciones.analityco.adapters.repositories.EnrollmentRepository
import co.tecno.sersoluciones.analityco.models.*
import co.tecno.sersoluciones.analityco.nav.CreatePersonalRepository
import co.tecno.sersoluciones.analityco.nav.LoadingApiStatus
import co.tecno.sersoluciones.analityco.nav.SecurityReference
import co.tecno.sersoluciones.analityco.repository.AnalitycoRepository
import co.tecno.sersoluciones.analityco.services.SignalRService
import co.tecno.sersoluciones.analityco.services.UpdateDBService
import co.tecno.sersoluciones.analityco.utilities.MyPreferences
import co.tecno.sersoluciones.analityco.utilities.isIngeo
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PersonalDailyViewModel @Inject constructor(
    private val repository: EnrollmentRepository,
    private val analitycoRepository: AnalitycoRepository,
    private val createPersonalRepository: CreatePersonalRepository,
    private val preferences: MyPreferences,
    private val workManager: WorkManager,
    private val mContext: Context
) : ViewModel() {

    private val viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    val reports = repository.reports

    private val _personals = MutableLiveData<List<PersonalRealTime>?>()
    val personals: LiveData<List<PersonalRealTime>?>
        get() = _personals

    private val _status = MutableLiveData<LoadingApiStatus>()
    val status: LiveData<LoadingApiStatus>
        get() = _status

    private val _projectId = MutableLiveData<String>()

    private val _personalRealTime = MutableLiveData<PersonalRealTime?>()
    val personalRealTime: LiveData<PersonalRealTime?>
        get() = _personalRealTime

    private val _updateList = MutableLiveData<Boolean>()
    val updateList: LiveData<Boolean>
        get() = _updateList

    private val _surveyForm = MutableLiveData<SurveyDetails>()
    val surveyForm: LiveData<SurveyDetails>
        get() = _surveyForm


    private val _surveyFormInfo = MutableLiveData<Survey>()
    val surveyFormInfo: LiveData<Survey>
        get() = _surveyFormInfo

    private val _surveyToken = MutableLiveData<String>()
    val surveyToken: LiveData<String>
        get() = _surveyToken

    private var _transportList = MutableLiveData<List<String>>()
    val transportList: LiveData<List<String>>
        get() = _transportList

    private var _notFoundProjects = MutableLiveData<Boolean?>()
    val notFoundProjects: LiveData<Boolean?>
        get() = _notFoundProjects

    private var _personalContractOffline = MutableLiveData<List<PersonalContractOfflineNetwork>?>()
    val personalContractOffline: LiveData<List<PersonalContractOfflineNetwork>?>
        get() = _personalContractOffline

    private val _selectedPersonContractOffline = MutableLiveData<PersonalContractOfflineNetwork>()
    val selectedPersonContractOffline: LiveData<PersonalContractOfflineNetwork>
        get()= _selectedPersonContractOffline

    private val _afIndicator = MutableLiveData<Float>()
    val afIndicator: LiveData<Float>
        get() = _afIndicator

    private val _personEntry = MutableLiveData<Long>()
    val personEntry: LiveData<Long>
        get() = _personEntry

    private val _personExit = MutableLiveData<Long>()
    val personExit : LiveData<Long>
        get() = _personExit

    private val _inputStatus = MutableLiveData<Long>()
    val inputStatus : LiveData<Long>
        get() = _inputStatus

    private val _secRefereces = MutableLiveData<List<SecurityReference>>()
    val secRefereces: LiveData<List<SecurityReference>>
        get() = _secRefereces

    private val _projects = MutableLiveData<List<ProjectList>?>()
    val projects: LiveData<List<ProjectList>?>
        get() = _projects

    private val _selectedProject = MutableLiveData<ProjectList?>()
    val selectedProject: LiveData<ProjectList?>
        get() = _selectedProject

   /* private val _personal = MutableLiveData<List<PersonalList>?>()
    val personal: LiveData<List<PersonalList>?>
        get() = _personal


      private val _selectedPerson = MutableLiveData<PersonalList>()
      val selectedPerson: LiveData<PersonalList?>
          get()= _selectedPerson*/

    private val _currentLocation = MutableLiveData<LatLng?>()
    val currentLocation: LiveData<LatLng?>
        get() = _currentLocation

    private val _countDB = MutableLiveData<Int?>()
    val countDB: LiveData<Int?>
        get() = _countDB

    private var signalRService: SignalRService? = null

    var genericEps: SocialSecurity? = null
    var genericAfp: SocialSecurity? = null

    private var textToSpeech: TextToSpeech? = null
    private val idSpeak = "co.tecno.sersoluciones.analityco.speak.HABLAR"

    init {
        _updateList.value = false
        _afIndicator.value = 0.0f
        getData()
    }

    fun getData(){
        uiScope.launch {
        _secRefereces.value = createPersonalRepository.getSecurityReferences()
            genericEps =
                    (_secRefereces.value?.singleOrNull { x -> x is SecurityReference.EPS && x.item.Code == "EPS00000" } as? SecurityReference.EPS)?.item
            genericAfp =
                    (_secRefereces.value?.singleOrNull { x -> x is SecurityReference.AFP && x.item.Code == "AFP00000" } as? SecurityReference.AFP)?.item
        }
    }

    fun createSignalR() {
        if (signalRService == null)
            signalRService = SignalRService(mContext, preferences, repository, this)
        signalRService!!.onCreate()
        createSpeakService()
    }

    private fun createSpeakService() {
        if (textToSpeech == null)
            textToSpeech = TextToSpeech(mContext, TextToSpeech.OnInitListener { status ->
                if (status != TextToSpeech.ERROR) {
                    textToSpeech!!.language = Locale.getDefault()
                }
            })
    }

    fun startSignalR(projectId: String) {
        if (signalRService?.getProjectId() == null || projectId != signalRService?.getProjectId()) {
            signalRService?.setProject(projectId)
            signalRService?.startConnection()
        }
    }

    fun stopSignalR() {
        signalRService?.onDestroy()
        signalRService = null


        if (textToSpeech != null) {
            textToSpeech?.stop()
            textToSpeech?.shutdown()
            textToSpeech = null
            logW("textToSpeech Destroyed")
        }
    }

    fun syncSignalR(projectId: String) {
        signalRService?.setProject(projectId)
        signalRService?.updatePersonal()
    }

    fun sendMsgSignalR(personalRealTime: PersonalRealTime) {
        signalRService?.sendMsg(personalRealTime)
    }

    fun speak(toSpeak: String?) {
        if (preferences.getVerifyAdnRegisterSettings(2)) {
            createSpeakService()
            textToSpeech?.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null, idSpeak)
        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    fun insertProjectsSyn(id: String?, Name: String?, Date: String?){
        repository.LoadProjectSync(id,Name,Date)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun insertPersonalSyn(personalCompanyInfoId: Int?) {
        repository.LoadPersonalSync(personalCompanyInfoId)
    }

    fun getProjects(
        currentLocation: LatLng,
        columns: Array<String>? = null, selection: String? = null, selectionArgs: Array<String>? = null, orderBy: String? = null
    ) {
        if (_currentLocation.value == null) {
            _status.value = LoadingApiStatus.LOADING
            _currentLocation.value = currentLocation
            uiScope.launch {
                val projects =
                    createPersonalRepository.getProjects(columns = columns, selection = selection, selectionArgs = selectionArgs, orderBy = orderBy)
                val projectListInGeo = mutableListOf<ProjectList>()
                var projectList: ProjectList? = null
                var isInGeo: Boolean
                projects.forEach { project ->
                    val geofenceJson = project.GeoFenceJson
                    isInGeo = if(!geofenceJson.equals("null")){
                        isIngeo(geofenceJson, _currentLocation.value)
                    }else {
                        true
                    }
                    val name = project.Name
                    if (isInGeo) {
                        logW("Project name: $name isInGeo: $isInGeo")
                        projectListInGeo.add(project)
                        if (_selectedProject.value == null)
                            projectList = project
                    }
                }
                _projects.value = projectListInGeo
                _selectedProject.value = projectList
                _status.value = LoadingApiStatus.DONE
                if (projectListInGeo.isEmpty())_notFoundProjects.value = true
            }
        }
    }

    fun setSelectedProject(project: ProjectList) {
        _selectedProject.value = project
    }

    fun updatePersonalByProject(projectId: String) {
        uiScope.launch {
            _projectId.value = projectId
            _personals.value = repository.getPersonalByProject(projectId)
            _afIndicator.value = repository.getAfIndicator(projectId).toFloat()
            _updateList.value = false
        }
    }

    fun updatePersonalList() {
        uiScope.launch {
           _personals.value = repository.getPersonalByProject(_projectId.value!!)
            _afIndicator.value = repository.getAfIndicator(_projectId.value!!).toFloat()
            _updateList.value = false
        }
    }

    fun verifyInorOut(document: String, PersonalInfoId: Long){
        uiScope.launch {
            _personEntry.value = repository.personInorOut(document)
            _personExit.value = repository.personOut(document)
            _inputStatus.value = repository.inputStatus(document)
            _surveyFormInfo.value = repository.getSurveyInfoDB(PersonalInfoId,"")

        }
    }

    fun insertPersonalRealTimeDB(
        personalRealTime: PersonalRealTime, isEnter: Boolean, documentNumber: String?,
        companyInfoId: String?, name: String?, lastName: String?
    ) {

        uiScope.launch {

            val personalList: PersonalList? = repository.getPersonalById(personalRealTime.personalId)
            if (personalList != null) {
                personalRealTime.documentNumber = personalList.DocumentNumber
                personalRealTime.companyInfoId = personalList.CompanyInfoId
                personalRealTime.name = personalList.Name
                personalRealTime.lastName = personalList.LastName
            } else {
                personalRealTime.documentNumber = documentNumber
                personalRealTime.companyInfoId = companyInfoId
                personalRealTime.name = name
                personalRealTime.lastName = lastName
            }

            var obj = repository.getPersonalRealTimeDB(personalRealTime.personalId)
            if (obj != null) {
                obj.projectId = personalRealTime.projectId
                log("obj.getTime ${obj.getTime}")
                if (isEnter) {
//                    if (obj.getTime != 0L) return@launch
                    obj.getTime = personalRealTime.getTime
                    obj.inputStatus = 1
                } else if (!isEnter) {
                    if (obj.getTime == 0L) return@launch
                    obj.lastTime = personalRealTime.lastTime
                    obj.inputStatus = 2
                }
            } else obj = personalRealTime

            if (isEnter && obj.lastTime != 0L) {
                val calendar = Calendar.getInstance()
                calendar.timeInMillis = System.currentTimeMillis()
                calendar.set(Calendar.MILLISECOND, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.HOUR_OF_DAY, 0)

                val startDate = calendar.timeInMillis
//                log("tiempo de salida : ${convertLongToSimpleDateString(obj.lastTime)}")
//                log("tiempo inicio dia : ${convertLongToSimpleDateString(startDate)}")
                if (obj.lastTime < startDate) {
                    obj.lastTime = 0L
                }
            }

            if (_projectId.value != null && obj.projectId == null)
                obj.projectId = _projectId.value!!

            repository.insertPersonalRealTime(obj)
            _personalRealTime.value = obj

            if (_projectId.value != null) {
                _personals.value = repository.getPersonalRealTimeByProject(_projectId.value!!)
                _afIndicator.value = repository.getAfIndicator(_projectId.value!!).toFloat()
            }
            _updateList.value = false
        }
    }

    private fun convertLongToSimpleDateString(systemTime: Long): String {
        return DateFormat.format("EEEE dd-MMM-yyyy HH:mm", systemTime).toString()
    }

    fun clearPersonal() {
        _personalRealTime.value = null
    }

    fun updateListPersonal() {
        _updateList.value = true
    }

    fun sendReport(data: String) {
        uiScope.launch {
            analitycoRepository.insertReporte(Reporte(data = data))
            UpdateDBService.startRequest(mContext, false)
        }
    }

    override fun onCleared() {
        super.onCleared()

        viewModelJob.cancel()
    }

    fun loadSurveyFromServer() {
        uiScope.launch {
            try {
                _status.value = LoadingApiStatus.LOADING
                repository.fetchSurvey()
                _status.value = LoadingApiStatus.DONE
            } catch (e: Exception) {
                e.printStackTrace()
                _status.value = LoadingApiStatus.ERROR
            }
        }

    }

    fun configSurveyForm(PersonalInfoId: Long,CreateDate : String) {
        if (PersonalInfoId == 0L) return
        uiScope.launch {
            try {
                val survey = repository.getSurveyDB(PersonalInfoId,CreateDate)
                if (survey != null) {
                    println("Encuestsa$survey")
                    _transportList.value = survey.transport.split(",")
                    loadSurveyForm(survey)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    private fun loadSurveyForm(survey: Survey) {
        val symptoms: List<String> = survey.symptoms.split(",")


        _surveyForm.value = SurveyDetails(
            fever = symptoms.contains("Fiebre de más de 38 grados"),
            cough = symptoms.contains("Tos"),
            nasalCongestion = symptoms.contains("Congestión nasal"),
            soreThroat = symptoms.contains("Dolor de garganta"),
            difficultyBreathing = symptoms.contains("Dificultad para respirar"),
            fatigue = symptoms.contains("Fatiga"),
            shivers = symptoms.contains("Escalofrios"),
            musclePain = symptoms.contains("Dolor de músculos"),
            otherPersonHasSymptoms = if (survey.otherPersonHasSymptoms) "SI" else "NO",
            otherPersonHasCOVID19 = if (survey.otherPersonHasCOVID19) "SI" else "NO",
            temperatura = if (symptoms.contains("Temperature: "))"0" else symptoms[symptoms.size -1]+"°",
            isActive = survey.isActive,
            requestState = survey.responseState,
            createDate = survey.createDate!!//



        )
    }

    fun loadSurveyToken(document: String, pin: String) {
        uiScope.launch {
            try {
                _status.value = LoadingApiStatus.LOADING
                _surveyToken.value = repository.getSurveyToken(document, pin)
                _status.value = LoadingApiStatus.DONE
            } catch (e: Exception) {
                e.printStackTrace()
                _status.value = LoadingApiStatus.ERROR
            }
        }
    }

    fun loadPersonalContractOffline(personalCompanyInfoId: Long) {
        uiScope.launch {
            try {
                _status.value = LoadingApiStatus.LOADING
                _personalContractOffline.value = repository.fetchPersonalContractOffline(personalCompanyInfoId)
                _status.value = LoadingApiStatus.DONE
                logW("test loadPersonalContractOffline OK")

            } catch (e: Exception) {
                e.printStackTrace()
                _status.value = LoadingApiStatus.ERROR
            }
        }
    }

    fun clearPersonalContractOffline() {
        _personalContractOffline.value = null
    }

    fun clearFormData() {
        _surveyForm.value = null
        _surveyToken.value = ""
        _projects.value = null
        _currentLocation.value = null
        _selectedProject.value = null
        _notFoundProjects.value = null
    }

    fun deletePersonalRealTime() {
        uiScope.launch {
            repository.removeAllPersonalRealTime()
        }
    }

    fun deleteAllContractPerson(projectId: String?) {
        uiScope.launch {
            repository.removePersonalRealTime(projectId)
        }
    }

    fun getCountRegistersDB() {
        uiScope.launch {
            _countDB.value = repository.getCountRegistersDB()
        }
    }

    fun insertSurveyOffline(data: String, report: ReportSurvey) {
        uiScope.launch {
            analitycoRepository.insertSurveySimptom(SurveySymptom(data = data))
            repository.fetchSurveyOffline(report)
            UpdateDBService.startRequest(mContext, false)
        }
    }
}
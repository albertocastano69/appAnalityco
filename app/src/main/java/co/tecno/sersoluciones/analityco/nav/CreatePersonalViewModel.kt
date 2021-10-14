package co.tecno.sersoluciones.analityco.nav

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.com.sersoluciones.barcodedetectorser.utilities.DebugLog
import co.com.sersoluciones.barcodedetectorser.utilities.DebugLog.logE
import co.com.sersoluciones.barcodedetectorser.utilities.DebugLog.logW
import co.com.sersoluciones.facedetectorser.utilities.DebugLog.log
import co.tecno.sersoluciones.analityco.models.*
import co.tecno.sersoluciones.analityco.repository.ApiStatus
import co.tecno.sersoluciones.analityco.ui.createPersonal.viewmodels.PersonalViewModel
import co.tecno.sersoluciones.analityco.utilities.DecodeBarcode
import co.tecno.sersoluciones.analityco.utilities.MyPreferences
import co.tecno.sersoluciones.analityco.utilities.isIngeo
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

enum class LoadingApiStatus {
    LOADING, ERROR, DONE
}

@Singleton
class CreatePersonalViewModel @Inject constructor(
        private val repository: CreatePersonalRepository,
        private val mContext: Context,
        private val preferences: MyPreferences
) : ViewModel() {

    @Inject
    lateinit var viewModel: PersonalViewModel
    private var viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    var typeContract: ContractType? = null
    var movePersonal: Boolean = false
    var Onecontract : Boolean = false
    var documentNumber: String? = null
    var infoUser: DecodeBarcode.InfoUser? = null
    var projectId: String? = null
    var EmployerId: String? = null
    var ItemEmployer: ObjectList? = null
    var externalData: Boolean = false
    var scan: Boolean = false
    var PersonalId: String? = null
    var PersonalEmployerInfoId: String? = null
    var Nationality: Int? = 0
    var ItemPlaceJob : PlaceToJob? = null
    var TypeDocument : String? = null
    var PersonalAssociateWithEmployer : Boolean = false
    var personaCreateFirtTime : Boolean = false
    var personinContract : Boolean = false


    private val _selectedProject = MutableLiveData<ProjectList?>()
    val selectedProject: LiveData<ProjectList?>
        get() = _selectedProject

    private val _selectedCompany = MutableLiveData<CompanyList?>()
    val selectedCompany: LiveData<CompanyList?>
        get() = _selectedCompany

    private val _companies = MutableLiveData<List<CompanyList>?>()
    val companies: LiveData<List<CompanyList>?>
        get() = _companies

    private val _contract = MutableLiveData<ContractEnrollment?>()
    val contract: LiveData<ContractEnrollment?>
        get() = _contract

    private val _currentLocation = MutableLiveData<LatLng?>()
    val currentLocation: LiveData<LatLng?>
        get() = _currentLocation

    private val _personal = MutableLiveData<PersonalNetwork?>()
    val personal: LiveData<PersonalNetwork?>
        get() = _personal

    private val _personalContactData = MutableLiveData<PersonalNetworkData?>()
    val personalContractData: LiveData<PersonalNetworkData?>
        get() = _personalContactData

    private val _personalData = MutableLiveData<PersonalIndividualContract?>()
    val personalData: LiveData<PersonalIndividualContract?>
        get() = _personalData

    private val _personalDataToPhoto = MutableLiveData<PersonalIndividualContract?>()
    val personalDataToPhoto: LiveData<PersonalIndividualContract?>
        get() = _personalDataToPhoto

    private val _personalContract = MutableLiveData<PersonalContract?>()
    val personalContract: LiveData<PersonalContract?>
        get() = _personalContract

    private val _contracts = MutableLiveData<List<ContractEnrollment>?>()
    val contracts: LiveData<List<ContractEnrollment>?>
        get() = _contracts

    private val _companyInfoId = MutableLiveData<String>()
    val companyInfoId: LiveData<String>
        get() = _companyInfoId

    private val _projects = MutableLiveData<List<ProjectList>?>()
    val projects: LiveData<List<ProjectList>?>
        get() = _projects


    private val _placeJob = MutableLiveData<List<PlaceToJob>>()
    val placeJob: LiveData<List<PlaceToJob>>
        get() = _placeJob

    private val _positions = MutableLiveData<List<Position>>()
    val positions: LiveData<List<Position>>
        get() = _positions

    private val _personalNotFound = MutableLiveData<Boolean?>()
    val personalNotFound: LiveData<Boolean?>
        get() = _personalNotFound

    private val _DataContactNotFound = MutableLiveData<Boolean?>()
    val DataContactNotFound: LiveData<Boolean?>
        get() = _DataContactNotFound

    private val _navigateToSelectProjectFragment = MutableLiveData<Boolean>()
    val navigateToSelectProjectFragment: LiveData<Boolean>
        get() = _navigateToSelectProjectFragment

    private val _navigateToSelectTypeContractFragment = MutableLiveData<Boolean>()
    val navigateToSelectTypeContractFragment: LiveData<Boolean>
        get() = _navigateToSelectTypeContractFragment

    private val _navigateToSelectContractFragment = MutableLiveData<Boolean>()
    val navigateToSelectContractFragment: LiveData<Boolean>
        get() = _navigateToSelectContractFragment

    private val _navigateToSelectPlaceJobFragment = MutableLiveData<Boolean>()
    val navigateToSelectPlaceJobFragment: LiveData<Boolean>
        get() = _navigateToSelectPlaceJobFragment

    private val _navigateToJoinContractFragment = MutableLiveData<Boolean>()
    val navigateToJoinContractFragment: LiveData<Boolean>
        get() = _navigateToJoinContractFragment

    private val _navigateToSelectEmployerFragment = MutableLiveData<Boolean>()
    val navigateToSelectEmployerFragment: LiveData<Boolean>
        get() = _navigateToSelectEmployerFragment

    private val _navigateToRequirementFragment = MutableLiveData<Boolean>()
    val navigateToRequirementFragment: LiveData<Boolean>
        get() = _navigateToRequirementFragment


    private val _initialData = MutableLiveData<Boolean>()
    val initialData: LiveData<Boolean>
        get() = _initialData

    private val _status = MutableLiveData<LoadingApiStatus>()
    val status: LiveData<LoadingApiStatus>
        get() = _status

    private val _msgPersonToMove: MutableLiveData<String?> = MutableLiveData()
    val msgPersonToMove: LiveData<String?>
        get() = _msgPersonToMove

    private val _responseJoinContract: MutableLiveData<ResponseResult<Any>?> = MutableLiveData()
    val responseJoinContract: LiveData<ResponseResult<Any>?>
        get() = _responseJoinContract

    init {
        movePersonal = false
    }

    fun verifyInitialDataInDB() {
        viewModelScope.launch {
            _initialData.value = repository.verifyInitialDataInDB()
            if (!_initialData.value!!)
                _status.value = LoadingApiStatus.LOADING
            else setInitialData()
        }
    }

    private fun setInitialData() {
        _status.value = LoadingApiStatus.DONE
        _initialData.value = true
    }

    fun getCompanies() {
        if (_companies.value != null) return
        val profile = preferences.profile
        val user = Gson().fromJson(
                profile,
                User::class.java
        )
        _companies.value = user.Companies
       if (user.Companies.size == 1)
            navigateToJoinProject(user.Companies[0])
    }

    fun getPlaceToJob(){
        viewModelScope.launch {
            try {
                _placeJob.value = repository.PlaceJob(EmployerId)
            }   catch (e:Exception){

            }
        }
    }
    fun getPlaceToJobWizard(employerId: String){
        viewModelScope.launch {
            try {
                _placeJob.value = repository.PlaceJob(employerId)
            }   catch (e:Exception){

            }
        }
    }

    fun getProjects(
            columns: Array<String>? = null, selection: String? = null, selectionArgs: Array<String>? = null, orderBy: String? = null
    ) {
        uiScope.launch {
            val projects = repository.getProjects(columns = columns, selection = selection, selectionArgs = selectionArgs, orderBy = orderBy)
            selectedProject.value?.let {
                projects.singleOrNull { x -> x.Id == it.Id }?.IsSelected = true
            }
            _projects.value = projects
        }
    }

    private fun setCompanyInfoId(companyInfoId: String) {
        _companyInfoId.value = companyInfoId
    }

    fun setLocation(currentLocation: LatLng) {
        if (_currentLocation.value == null) {
            _currentLocation.value = currentLocation
            detectProjectInGeofence()
        }
    }



    fun updateListProject(project: ProjectList, location: LatLng? = null) {
        if (_currentLocation.value == null && location != null)
            _currentLocation.value = location
        _selectedProject.value = project
        _projects.value!!.forEach { it.IsSelected = false }
        _projects.value!!.singleOrNull { x -> x.Id == project.Id }?.IsSelected = true
        _projects.value = _projects.value
        _navigateToSelectTypeContractFragment.value = true
    }
    private fun detectProjectInGeofence() {
        _projects.value?.let {
            for (project in it) {
                val geofenceJson = project.GeoFenceJson
                val isInGeo = isIngeo(geofenceJson, currentLocation.value)
                val name = project.Name

                if (isInGeo) {
                    logW("Project name: $name isInGeo: $isInGeo")
                    updateListProject(project)
                    break
                }
            }
        }
    }
    fun detectedPersonInContract(){
        viewModelScope.launch {
            try {
                repository.foundContactPerson(PersonalEmployerInfoId!!)
                personinContract = true
            }catch (e : Exception){
                if (e is retrofit2.HttpException)
                    when (e.code()) {
                        412 -> {
                            personinContract = true
                        }
                    }
            }
        }
    }
    fun fetchPersonal(doc: String) {
        if (_personal.value != null && doc == _personal.value!!.DocumentNumber) {
            _navigateToSelectContractFragment.value = true
            return
        }
        viewModelScope.launch {
            try {
                _status.value = LoadingApiStatus.LOADING
                _personal.value = repository.fetchPersonal(doc, _companyInfoId.value!!)
                _contracts.value = null
                _navigateToSelectContractFragment.value = true
                _status.value = LoadingApiStatus.DONE

            } catch (e: Exception) {
//                this@CreatePersonalViewModel.infoUser = null
                if (e is retrofit2.HttpException)
                    when (e.code()) {
                        404 -> {
                            logE(e.message)
                            _personalNotFound.value = true
                            _contracts.value = null
                        }
                        400 -> {
                            var error = e.response()?.errorBody()?.string()
                            error = trimMessage(error)?.trimStart('[', '"')?.trimEnd('"', ']')
                            error = error?.replace("\\", "")
                            logE("msg ${e.message()} response ${e.response()} error $error")
                            error?.let {
                                val infoJobj = JSONObject(error)
                                val infouser = DecodeBarcode.InfoUser()
                                infouser.dni = infoJobj["DocumentNumber"].toString().toLong()
                                infouser.name = infoJobj["Name"].toString()
                                infouser.lastname = infoJobj["LastName"].toString()
                                infouser.birthDate = infoJobj["BirthDate"].toString().toInt()
                                infouser.documentType = infoJobj["DocumentType"].toString()
                                infouser.rh = infoJobj["RH"].toString()
                                infouser.sex = infoJobj["Sex"].toString()
                                infouser.cityOfBirthCode = infoJobj["CityOfBirthCode"].toString()
                                infouser.DocumentRaw = infoJobj["DocumentRaw"].toString()
                                this@CreatePersonalViewModel.infoUser = infouser
                                logW("infoUser ${Gson().toJson(infoUser)}")
                            }
                            _personalNotFound.value = true
                            _contracts.value = null
                        }
                    }
                _status.value = LoadingApiStatus.ERROR
            }
        }
    }
    fun findPersonalToIndividualContract(docType:String,doc: String) {
        if (_personal.value != null && doc == _personal.value!!.DocumentNumber) {
            _navigateToSelectContractFragment.value = true
            return
        }
        viewModelScope.launch {
            try {
                _status.value = LoadingApiStatus.LOADING
                _personal.value = repository.findPersonalToIndividualContract(docType,doc)
                DebugLog.log("personal to insert ${Gson().toJson(_personal.value)}")
                findDataContact(_personal.value!!.PersonalId)
            } catch (e: Exception) {
                if (e is retrofit2.HttpException)
                    when (e.code()) {
                        404 -> {
                            logE(e.message)
                            _personalNotFound.value = true
                            _contracts.value = null
                        }
                        400 -> {
                            var error = e.response()?.errorBody()?.string()
                            error = trimMessage(error)?.trimStart('[', '"')?.trimEnd('"', ']')
                            error = error?.replace("\\", "")
                            logE("msg ${e.message()} response ${e.response()} error $error")
                            error?.let {
                                val infoJobj = JSONObject(error)
                                val infouser = DecodeBarcode.InfoUser()
                                infouser.dni = infoJobj["DocumentNumber"].toString().toLong()
                                infouser.name = infoJobj["Name"].toString()
                                infouser.lastname = infoJobj["LastName"].toString()
                                infouser.birthDate = infoJobj["BirthDate"].toString().toInt()
                                infouser.documentType = infoJobj["DocumentType"].toString()
                                infouser.rh = infoJobj["RH"].toString()
                                infouser.sex = infoJobj["Sex"].toString()
                                infouser.cityOfBirthCode = infoJobj["CityOfBirthCode"].toString()
                                this@CreatePersonalViewModel.infoUser = infouser
                                logW("infoUser ${Gson().toJson(infoUser)}")
                            }
                            _personalNotFound.value = true
                            _contracts.value = null
                        }
                        412 ->{
                            logE(e.message)
                            _personalNotFound.value = true
                            _contracts.value = null
                        }
                    }
                _status.value = LoadingApiStatus.ERROR
            }
        }
    }
    fun findDataContact(PersonalId: String?){
        viewModelScope.launch {
            try{
                _personalContactData.value = repository.findDataToContact(PersonalId,EmployerId)
                _navigateToSelectContractFragment.value = true
                _DataContactNotFound.value = false
                PersonalEmployerInfoId = _personalContactData.value!!.Id
                _status.value = LoadingApiStatus.DONE
            }catch (e:Exception){
                if (e is retrofit2.HttpException)
                    when (e.code()) {
                        412 ->{
                            logE(e.message)
                            _DataContactNotFound.value = true
                            _navigateToSelectContractFragment.value = true
                            _personalContactData.value = null
                        }
                    }
                _status.value = LoadingApiStatus.ERROR
            }
        }
    }
    private fun getPositions() {
        if (_positions.value != null && _positions.value!!.isNotEmpty()) return
        viewModelScope.launch {
            try {
                _status.value = LoadingApiStatus.LOADING
                _positions.value = repository.fetchPersonalPositions(_companyInfoId.value!!)
                logW("size positions ${_positions.value!!.size}")
                _status.value = LoadingApiStatus.DONE
            } catch (e: Exception) {
                _status.value = LoadingApiStatus.ERROR
            }
        }
    }

    suspend fun updatePositions(companyInfoId: String): ApiStatus {
        return try {
            _positions.value = repository.fetchPersonalPositions(companyInfoId)
            logW("size positions ${_positions.value!!.size}")
            ApiStatus.DONE
        } catch (e: Exception) {
            ApiStatus.ERROR
        }
    }

    fun setStatus(status: LoadingApiStatus) {
        _status.value = status
    }

    fun fetchPersonalContract(contract: ContractEnrollment) {
        viewModelScope.launch {
            try {
                _status.value = LoadingApiStatus.LOADING
                _personalContract.value = repository.fetchPersonalContract(_personal.value!!.PersonalCompanyInfoId, contract.Id!!)
                _contract.value = contract
                _navigateToJoinContractFragment.value = true
                _status.value = LoadingApiStatus.DONE
            } catch (e: Exception) {
                _status.value = LoadingApiStatus.ERROR
            }
        }
    }

    fun fetchContractsByPersonal() {
        if (_contracts.value != null) return
        viewModelScope.launch {
            try {
                _status.value = LoadingApiStatus.LOADING
                _contracts.value = repository.fetchContractsByPersonal(_personal.value!!.PersonalCompanyInfoId.toString())
                _status.value = LoadingApiStatus.DONE
            } catch (e: Exception) {
                _status.value = LoadingApiStatus.ERROR
            }
        }
    }

    fun createPersonal(personal: PersonalNew) {
        viewModelScope.launch {
            try {
                _status.value = LoadingApiStatus.LOADING
                _personal.value = repository.createPersonal(personal)
                if (personal.Photo != null) {
                    uploadPhotoPersonalAsync(personal.Photo!!, _personal.value!!.PersonalCompanyInfoId)
                    _personal.value!!.Photo = personal.Photo
                }
                _navigateToSelectContractFragment.value = true
                _status.value = LoadingApiStatus.DONE
            } catch (e: Exception) {
                e.printStackTrace()
                _status.value = LoadingApiStatus.ERROR
            }
        }
    }
    fun createPersonalToIndividualContract(personal1:PersonalNew) {
        viewModelScope.launch {
            try {
                val personal = viewModel.personal.value!!
                _status.value = LoadingApiStatus.LOADING
                _personalData.value = repository.createPersonalToIndividualContract(personal1)
                personaCreateFirtTime = true
                if(personal.Address != null) {
                    val dataContact = PersonalDataContact(
                        personal.PhoneNumber,
                        personal.CityCode,
                        personal.Address,
                        personal.EmergencyContact,
                        personal.EmergencyContactPhone,
                        personal.Email,
                        EmployerId,
                        _personalData.value!!.Id
                    )
                    try{
                        DebugLog.logW("jsonInsertPersonalEmployer: ${Gson().toJson(dataContact)}")
                        _personalDataToPhoto.value = repository.createContactPersonalToIndividualContract(dataContact)
                        _navigateToSelectPlaceJobFragment.value = true
                        PersonalEmployerInfoId = _personalDataToPhoto.value!!.Id
                        uploadPhotoPersonalAsyncForIndivudualContract(personal.Photo!!, _personalDataToPhoto.value!!.Id)
                    }catch (e : Exception){
                        if (e is retrofit2.HttpException)
                            when (e.code()) {
                                409 ->{
                                    logE(e.message)
                                }
                            }
                        _status.value = LoadingApiStatus.ERROR
                    }

                }
                _status.value = LoadingApiStatus.DONE
            } catch (e: Exception) {
                e.printStackTrace()
                _status.value = LoadingApiStatus.ERROR
            }
        }
    }
    fun updatePersonalToIndividualContract(personal1:PersonalNew) {
        viewModelScope.launch {
            try {
                _status.value = LoadingApiStatus.LOADING
                _personalData.value = repository.updatePersonalToIndividualContract(personal1)
                _status.value = LoadingApiStatus.DONE
            } catch (e: Exception) {
                e.printStackTrace()
                _status.value = LoadingApiStatus.ERROR
            }
        }
    }

    fun CreatePersonalEmployerInfo(personalContact: PersonalDataContact, Photo: String?, newPersonal: PersonalIndividualContract){
        viewModelScope.launch {
            try{
                _status.value = LoadingApiStatus.LOADING
                _personalDataToPhoto.value = repository.createContactPersonalToIndividualContract(personalContact)
                _navigateToSelectPlaceJobFragment.value = true
                _personalData.value = newPersonal
                personaCreateFirtTime = true
                PersonalEmployerInfoId = _personalDataToPhoto.value!!.Id
                uploadPhotoPersonalAsyncForIndivudualContract(Photo!!, _personalDataToPhoto.value!!.Id)
                _status.value = LoadingApiStatus.DONE
            }catch (e : Exception){
                e.printStackTrace()
                _status.value = LoadingApiStatus.ERROR
            }
        }
    }
    fun UpdatePersonalEmloyerInfo(personalContact: PersonalDataContact,IdPersonalEmployerInfo : String){
        viewModelScope.launch {
            try {
                val personal = viewModel.personal.value!!
                _status.value = LoadingApiStatus.LOADING
                _personalDataToPhoto.value = repository.UpdateContactPersonalToIndividualContract(personalContact,IdPersonalEmployerInfo)
                _navigateToSelectPlaceJobFragment.value = true
                PersonalEmployerInfoId = IdPersonalEmployerInfo
                uploadPhotoPersonalAsyncForIndivudualContract(personal.Photo!!, IdPersonalEmployerInfo)
                _status.value = LoadingApiStatus.DONE
            }catch (e:Exception){
                e.printStackTrace()
                _status.value = LoadingApiStatus.ERROR
            }
        }
    }
    private fun uploadPhotoPersonalAsyncForIndivudualContract(photo: String, id: String?) {
        try {
            log("imageUri $photo")
            val uri = Uri.parse(photo)
            mContext.contentResolver.openFileDescriptor(uri, "r").use {

            }
            viewModelScope.launch {
                repository.uploadPhotoPersonalToIndividualContractAsync(photo, id)
            }
        } catch (e: Exception) {
            }
    }
    fun updatePersonal(personal: UpdatePersonalInfo, personalInfoId: Int) {
        viewModelScope.launch {
            try {
                _status.value = LoadingApiStatus.LOADING
                _personal.value = repository.updatePersonal(personal, personalInfoId)
                if (personal.Photo != null) {
                    uploadPhotoPersonalAsync(personal.Photo!!, _personal.value!!.PersonalCompanyInfoId)
                    _personal.value!!.Photo = personal.Photo
                }
                _navigateToSelectContractFragment.value = true
                _status.value = LoadingApiStatus.DONE
            } catch (e: Exception) {
                e.printStackTrace()
                _status.value = LoadingApiStatus.ERROR
            }
        }
    }
    fun UpdateApiPerson(personal1:PersonalNew,PersonalEmployerInfoId: String) {
        viewModelScope.launch {
            try {
                val personal = viewModel.personal.value!!
                _status.value = LoadingApiStatus.LOADING
                _personalData.value = repository.UpdateApiPerson(personal1)
                if (personal.Address != null) {
                    val dataContact = PersonalDataContact(
                            personal.PhoneNumber,
                            personal.CityCode,
                            personal.Address,
                            personal.EmergencyContact,
                            personal.EmergencyContactPhone,
                            personal.Email,
                            EmployerId,
                            _personalData.value!!.Id
                    )
                    _personalDataToPhoto.value = repository.UpdateContactPersonalToIndividualContract(dataContact,PersonalEmployerInfoId)
                    _navigateToSelectPlaceJobFragment.value = true

                    uploadPhotoPersonalAsyncForIndivudualContract(personal.Photo!!, _personalDataToPhoto.value!!.Id)
                }
            }catch (e:Exception){

            }
        }
    }

    private fun uploadPhotoPersonalAsync(imageUri: String, personalCompanyInfoId: Int) {
        try {
            log("imageUri $imageUri")
            val uri = Uri.parse(imageUri)
            mContext.contentResolver.openFileDescriptor(uri, "r").use {

            }
            viewModelScope.launch {
                repository.uploadPhotoPersonalAsync(imageUri, personalCompanyInfoId)
            }
        } catch (e: Exception) {
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun isPersonalInContractAsync(contractId: String, personalCompanyInfoId: Int) {
        viewModelScope.launch {
            try {
                _status.value = LoadingApiStatus.LOADING
                val response: Map<String, String> = repository.isPersonalInContractAsync(contractId, personalCompanyInfoId) as Map<String, String>
                val jObj = JSONObject(response)
                logW(jObj.toString())
                movePersonal = false
                _msgPersonToMove.value = ""
                _status.value = LoadingApiStatus.DONE
            } catch (e: Exception) {
                var error: String? = ""
                if (e is retrofit2.HttpException) {
                    if (e.code() == 400) {
                        error = e.response()?.errorBody()?.string()
                        error = trimMessage(error)?.trimStart('[', '"')?.trimEnd('"', ']')
                        logE("msg ${e.message()} response ${e.response()} error $error")
                        movePersonal = true
                    }
                    if (e.code() == 403)
                        logE("Este usuario no tiene permisos para esta accion")

                    _msgPersonToMove.value = error
                }
                e.printStackTrace()
                _status.value = LoadingApiStatus.ERROR
            }
        }
    }

    fun createPersonalContract(personalContract: PersonalContract, contractId: String, path: String) {
        viewModelScope.launch {
            try {
                _status.value = LoadingApiStatus.LOADING
                val response = repository.createPersonalContract(personalContract, contractId, path)
                _responseJoinContract.value = ResponseResult.Success(response!!)
                _status.value = LoadingApiStatus.DONE
            } catch (e: Exception) {
                if (e is retrofit2.HttpException) {
                    if (e.code() == 400) {
                        var error = e.response()?.errorBody()?.string()
                        error = trimMessage(error)?.trimStart('[', '"')?.trimEnd('"', ']')
                        error = error?.replace("\\", "")
                        logE("msg ${e.message()} response ${e.response()} error $error")
                    }
                    _responseJoinContract.value = ResponseResult.Error(e, null)
                }
                e.printStackTrace()
                _status.value = LoadingApiStatus.ERROR
            }
        }
    }

    fun navigateToJoinPersonal(contract: ContractEnrollment, OneContract : Boolean) {
        _contract.value = contract
        _personalContract.value = null
        _navigateToJoinContractFragment.value = true
        Onecontract = OneContract
    }
    fun navigateToJoinProject(company: CompanyList) {
        _selectedCompany.value = company
        setCompanyInfoId(company.Id)
        getPositions()
        _navigateToSelectProjectFragment.value = true
    }

    fun navigateToSelectEmployer() {
        _navigateToSelectEmployerFragment.value = true
    }
    fun navigateToRequirements(){
        _navigateToRequirementFragment.value = true
    }
    fun doneNavigatoRequeriments() {
        _navigateToRequirementFragment.value = null
    }

    fun doneNavigatingSelectContract() {
        isPersonalInContractAsync(contract.value!!.Id!!, personal.value!!.PersonalCompanyInfoId)
        _navigateToJoinContractFragment.value = null
    }
    fun doneNavigatingSelectPlaceToJob() {
        _navigateToSelectPlaceJobFragment.value = null
    }

    fun doneNavigating() {
        _navigateToSelectTypeContractFragment.value = null
    }
    fun doneNavigatingPersonal() {
        _navigateToSelectContractFragment.value = null
    }

    fun doneNavigatingEmployer() {
        _navigateToSelectEmployerFragment.value = null
    }

    fun doneNavigatingCompany() {
        _navigateToSelectProjectFragment.value = null
    }

    fun clearFlagPersonal() {
        _personalNotFound.value = false
        documentNumber = null
//        infoUser = null
    }

    fun clearMsg() {
        _msgPersonToMove.value = null
    }

    fun setSelectedProject(project: ProjectList) {
        _selectedProject.value = project
    }

    fun clearForm() {
        _personalNotFound.value = null
        _navigateToSelectTypeContractFragment.value = null
        _navigateToSelectContractFragment.value = null
        _navigateToJoinContractFragment.value = null
        _navigateToSelectEmployerFragment.value = null
        _navigateToRequirementFragment.value = null
        _navigateToSelectProjectFragment.value = null
        _navigateToSelectPlaceJobFragment.value = null
        _personalData.value = null
        _personalDataToPhoto.value = null
        EmployerId = null
        PersonalEmployerInfoId = null
        PersonalId = null
        PersonalAssociateWithEmployer = false
        _contract.value = null
        _selectedProject.value = null
        _personal.value = null
        _companyInfoId.value = null
        _currentLocation.value = null
        _contracts.value = null
        _projects.value = null
        _companies.value = null
        _selectedCompany.value = null
        _msgPersonToMove.value = null
        _personalContract.value = null
        _responseJoinContract.value = null

        documentNumber = null
        infoUser = null
        projectId = null
        typeContract = null
        movePersonal = false
        externalData = false
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
}

sealed class ResponseResult<out T : Any> {

    data class Success<out T : Any>(val data: T) : ResponseResult<T>()
    data class Error(val exception: retrofit2.HttpException, val msg: String? = null) : ResponseResult<Nothing>()

    override fun toString(): String {
        return when (this) {
            is Success<*> -> "Success[data=$data]"
            is Error -> "Error[exc=$exception]"
        }
    }
}

fun trimMessage(json: String?): String? {
    var trimmedString: String? = ""
    try {
        val jsonObj = JSONObject(json!!)

        val keysIterator = jsonObj.keys()

        if (jsonObj.has("error_description"))
            trimmedString = jsonObj.getString("error_description")
        else
            while (keysIterator.hasNext()) {
                val key = keysIterator.next()
                trimmedString = jsonObj.getString(key)
                break
            }

    } catch (e: JSONException) {
        e.printStackTrace()
        return null
    }

    return trimmedString
}
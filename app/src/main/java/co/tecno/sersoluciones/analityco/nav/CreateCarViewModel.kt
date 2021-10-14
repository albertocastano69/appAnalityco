package co.tecno.sersoluciones.analityco.nav

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.com.sersoluciones.barcodedetectorser.utilities.DebugLog
import co.tecno.sersoluciones.analityco.CarViewModel
import co.tecno.sersoluciones.analityco.CreateCarRepository
import co.tecno.sersoluciones.analityco.models.*
import co.tecno.sersoluciones.analityco.ui.createPersonal.viewmodels.PersonalViewModel
import co.tecno.sersoluciones.analityco.utilities.MyPreferences
import co.tecno.sersoluciones.analityco.utilities.isIngeo
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

enum class LoadingApiStatusCar {
    LOADING, ERROR, DONE
}

@Singleton
class CreateCarViewModel @Inject constructor(
        private val repository: CreateCarRepository,
        private val mContext: Context,
        private val preferences: MyPreferences
) : ViewModel() {

    @Inject
    lateinit var viewModel: CarViewModel
    private var viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    var projectId: String? = null
    var typeContract: ContractType? = null

    private val _selectedProject = MutableLiveData<ProjectList?>()
    val selectedProject: LiveData<ProjectList?>
        get() = _selectedProject

    private val _selectedCompany = MutableLiveData<CompanyList?>()
    val selectedCompany: LiveData<CompanyList?>
        get() = _selectedCompany

    private val _companies = MutableLiveData<List<CompanyList>?>()
    val companies: LiveData<List<CompanyList>?>
        get() = _companies

    private val _initialData = MutableLiveData<Boolean>()
    val initialData: LiveData<Boolean>
        get() = _initialData

    private val _currentLocation = MutableLiveData<LatLng?>()
    val currentLocation: LiveData<LatLng?>
        get() = _currentLocation

    private val _navigateToSelectTypeContractFragment = MutableLiveData<Boolean>()
    val navigateToSelectTypeContractFragment: LiveData<Boolean>
        get() = _navigateToSelectTypeContractFragment

    private val _status = MutableLiveData<LoadingApiStatusCar>()
    val status: LiveData<LoadingApiStatusCar>
        get() = _status

    private val _navigateToSelectProjectFragment = MutableLiveData<Boolean>()
    val navigateToSelectProjectFragment: LiveData<Boolean>
        get() = _navigateToSelectProjectFragment

    private val _companyInfoId = MutableLiveData<String>()
    val companyInfoId: LiveData<String>
        get() = _companyInfoId

    private val _positions = MutableLiveData<List<Position>>()
    val positions: LiveData<List<Position>>
        get() = _positions

    private val _projects = MutableLiveData<List<ProjectList>?>()
    val projects: LiveData<List<ProjectList>?>
        get() = _projects

    fun verifyInitialDataInDB() {
        viewModelScope.launch {
            _initialData.value = repository.verifyInitialDataInDB()
            if (!_initialData.value!!)
                _status.value = LoadingApiStatusCar.LOADING
            else setInitialData()
        }
    }

    private fun setInitialData() {
        _status.value = LoadingApiStatusCar.DONE
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

    fun navigateToJoinProject(company: CompanyList) {
        _selectedCompany.value = company
        setCompanyInfoId(company.Id)
        getPositions()
        _navigateToSelectProjectFragment.value = true
    }

    private fun setCompanyInfoId(companyInfoId: String) {
        _companyInfoId.value = companyInfoId
    }

    private fun getPositions() {
        if (_positions.value != null && _positions.value!!.isNotEmpty()) return
        viewModelScope.launch {
            try {
                _status.value = LoadingApiStatusCar.LOADING
                _positions.value = repository.fetchPersonalPositions(_companyInfoId.value!!)
                DebugLog.logW("size positions ${_positions.value!!.size}")
                _status.value = LoadingApiStatusCar.DONE
            } catch (e: Exception) {
                _status.value = LoadingApiStatusCar.ERROR
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

    fun setSelectedProject(project: ProjectList) {
        _selectedProject.value = project
    }

    fun setLocation(currentLocation: LatLng) {
        if (_currentLocation.value == null) {
            _currentLocation.value = currentLocation
            detectProjectInGeofence()
        }
    }
    private fun detectProjectInGeofence() {
        _projects.value?.let {
            for (project in it) {
                val geofenceJson = project.GeoFenceJson
                val isInGeo = isIngeo(geofenceJson, currentLocation.value)
                val name = project.Name

                if (isInGeo) {
                    DebugLog.logW("Project name: $name isInGeo: $isInGeo")
                    updateListProject(project)
                    break
                }
            }
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

    fun doneNavigatingCompany() {
        _navigateToSelectProjectFragment.value = null
    }
    fun doneNavigating() {
        _navigateToSelectTypeContractFragment.value = null
    }

}
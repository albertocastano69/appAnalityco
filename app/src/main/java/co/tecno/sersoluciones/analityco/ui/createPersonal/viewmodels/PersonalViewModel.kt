package co.tecno.sersoluciones.analityco.ui.createPersonal.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import co.com.sersoluciones.barcodedetectorser.utilities.DebugLog.logW
import co.tecno.sersoluciones.analityco.adapters.repositories.PositionIndividualContract
import co.tecno.sersoluciones.analityco.models.*
import co.tecno.sersoluciones.analityco.nav.CreatePersonalRepository
import co.tecno.sersoluciones.analityco.nav.SecurityReference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PersonalViewModel @Inject constructor(
    private val repository: CreatePersonalRepository
) : ViewModel() {

    private var viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private val _daneCities = MutableLiveData<List<DaneCity>>()
    val daneCities: LiveData<List<DaneCity>>
        get() = _daneCities

    private val _daneCity = MutableLiveData<List<DaneCity>>()
    val daneCity: LiveData<List<DaneCity>>
        get() = _daneCity

    private val _cities = MutableLiveData<List<City>>()
    val cities: LiveData<List<City>>
        get() = _cities

    private val _contractType = MutableLiveData<List<IndividualContractsTypeModel>>()
    val contractType: LiveData<List<IndividualContractsTypeModel>>
        get() = _contractType

    private val _payPeriod = MutableLiveData<List<PayPeriod>>()
    val payPeriod: LiveData<List<PayPeriod>>
        get() = _payPeriod

    private val _city = MutableLiveData<City?>()
    val city: LiveData<City?>
        get() = _city

    private val _cityPerson = MutableLiveData<List<City>>()
    val cityPerson: LiveData<List<City>>
        get() = _cityPerson

    private val _jobs = MutableLiveData<List<co.tecno.sersoluciones.analityco.models.Job>>()
    val jobs: LiveData<List<co.tecno.sersoluciones.analityco.models.Job>>
        get() = _jobs

    private val _positions = MutableLiveData<List<PositionIndividualContract>>()
    val position: LiveData<List<PositionIndividualContract>>
        get() = _positions

    private val _secRefereces = MutableLiveData<List<SecurityReference>>()
    val secRefereces: LiveData<List<SecurityReference>>
        get() = _secRefereces

    private val _listEps = MutableLiveData<List<EpsList>>()
    val listEps: LiveData<List<EpsList>>
        get() = _listEps

    private val _listAfp = MutableLiveData<List<AfpList>>()
    val listAfp: LiveData<List<AfpList>>
        get() = _listAfp

    var personal: LiveData<PersonalNetwork> = MutableLiveData<PersonalNetwork>().also {
        it.postValue(PersonalNetwork(DocumentNumber = ""))
    }

    var PersonlIndividualContract : LiveData<PersonalInividualContractRequest> = MutableLiveData<PersonalInividualContractRequest>().also {
        it.postValue(PersonalInividualContractRequest(IndividualContractTypeId =  0))
    }

    private val _isReady = MutableLiveData<Boolean>()
    val isReady: LiveData<Boolean>
        get() = _isReady

    var genericEps: SocialSecurity? = null
    var genericAfp: SocialSecurity? = null
    var genericJob: co.tecno.sersoluciones.analityco.models.Job? = null

    init {
        getData()
        _isReady.postValue(false)
    }


    fun getData() {
        uiScope.launch {
            _jobs.value = repository.getJobs()
            _daneCities.value = repository.getDaneCities()
            _secRefereces.value = repository.getSecurityReferences()
            _contractType.value = repository.getContractType()
            _payPeriod.value = repository.getPayPeriod()
            _cities.value = repository.getCities(0)
            _cityPerson.value = repository.getCityPerson()
            _listEps.value = repository.getEpsList()
            _listAfp.value = repository.getAfpList()

            genericJob = _jobs.value?.singleOrNull { x -> x.Code == "D-0000.000" }
            genericEps =
                    (_secRefereces.value?.singleOrNull { x -> x is SecurityReference.EPS && x.item.Code == "EPS00000" } as? SecurityReference.EPS)?.item
            genericAfp =
                (_secRefereces.value?.singleOrNull { x -> x is SecurityReference.AFP && x.item.Code == "AFP00000" } as? SecurityReference.AFP)?.item

            _isReady.value = true

            logW("genericJobCode ${genericJob?.Code} genericEpsId ${genericEps?.Id} genericAfpId ${genericAfp?.Id}")
        }
    }
    fun getPosition(EmployerId: String?){
        uiScope.launch {
            _positions.value = repository.getPositions(EmployerId)
        }
    }
    fun getDaneCity(){
        uiScope.launch {
            try {
                _daneCity.value = repository.getDaneCity()
            }catch (e:Exception){

            }
        }

    }

    fun getCities(countryCode: Int) {
        uiScope.launch {
            _cities.value = repository.getCities(countryCode)
        }
    }

    fun getCitiesForNationality(countryCode: Int) {
        uiScope.launch {
            _cityPerson.value = repository.getCityForNationality(countryCode)
        }
    }

    fun setPersonal(personalNetwork: PersonalNetwork) {
        personal = MutableLiveData<PersonalNetwork>().also {
            it.value = personalNetwork
        }
    }

    fun cleanIsReady() {
        _isReady.value = false
    }

    fun getCity(stateCode: String, cityCode: String) {
        uiScope.launch {
            _city.value = repository.getCity(stateCode, cityCode, 0)
            logW("CityBirthDate2 ${_city.value}")
            if(_city.value == null){
                getCityunknow("00","000")
            }
        }
    }
     fun getCityunknow(stateCode: String, cityCode: String) {
         uiScope.launch {
             _city.value = repository.getCityunknown(stateCode, cityCode, 0)
             logW("CityBirthDateUnknow ${_city.value}")
         }
    }
    fun getCity(code: String) {
        uiScope.launch {
            _city.value = repository.getCity(code, 0)
            if(_city.value == null){
                getCityunknow("00","000")
            }
            logW("CityBirthDate ${_city.value}")
        }
    }
    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    fun clearData() {
        personal = MutableLiveData<PersonalNetwork>().also {
            it.value = PersonalNetwork(DocumentNumber = "")
        }
        PersonlIndividualContract = MutableLiveData<PersonalInividualContractRequest>().also {
            it.value = PersonalInividualContractRequest(IndividualContractTypeId =  0)
        }
        _city.value = null
    }

}
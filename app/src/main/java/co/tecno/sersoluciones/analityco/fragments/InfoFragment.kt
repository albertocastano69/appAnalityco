package co.tecno.sersoluciones.analityco.fragments


import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import co.com.sersoluciones.facedetectorser.utilities.DebugLog
import co.tecno.sersoluciones.analityco.*
import co.tecno.sersoluciones.analityco.adapters.AdapterProjectDetails
import co.tecno.sersoluciones.analityco.callback.LoadProject
import co.tecno.sersoluciones.analityco.databases.DBHelper
import co.tecno.sersoluciones.analityco.models.ProjectDetails
import co.tecno.sersoluciones.analityco.receivers.RequestBroadcastReceiver
import co.tecno.sersoluciones.analityco.repository.AnalitycoRepository
import co.tecno.sersoluciones.analityco.repository.ApiStatus
import co.tecno.sersoluciones.analityco.services.CRUDService
import co.tecno.sersoluciones.analityco.utilities.Constantes
import co.tecno.sersoluciones.analityco.utilities.HttpRequest
import co.tecno.sersoluciones.analityco.utilities.MyPreferences
import co.tecno.sersoluciones.analityco.viewmodels.PersonalDailyViewModel
import com.android.volley.Request
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_info.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList


/**
 * A simple [Fragment] subclass.
 *
 */
open class InfoFragment : Fragment(), LoadProject, RequestBroadcastReceiver.BroadcastListener {
    private var adapter: AdapterProjectDetails? = null
    private var data: LinkedHashMap<Uri, String>? = null
    private var fillArray: BooleanArray = BooleanArray(5)
    private var mValues: ArrayList<ProjectDetails>? = null
    private var mSqLiteDatabase: SQLiteDatabase? = null
    private var mDbHelper: DBHelper? = null
    private var connectivityManager: ConnectivityManager? = null
    private var InternetConexion:Boolean=true
    private var requestBroadcastReceiver: RequestBroadcastReceiver? = null
    private var replace: String? = null
    private var replacepersonalIds:String? = null
    private var replaceEmployerIds:String? = null
    private var Projectids:String? = null
    private var NameProject:String? = null
    var recyclerView : RecyclerView? = null
    @Inject
    lateinit var viewModel: PersonalDailyViewModel

    @Inject
    lateinit var repository: AnalitycoRepository

    @SuppressLint("SimpleDateFormat", "SetTextI18n", "UseRequireInsteadOfGet", "ResourceAsColor")
    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_info, container, false)
        mDbHelper = DBHelper(activity)

        requestBroadcastReceiver = RequestBroadcastReceiver(this)
        mSqLiteDatabase = mDbHelper!!.readableDatabase
        fillArray = BooleanArray(5)
        view.verName.text = "Versión (${BuildConfig.VERSION_NAME})"
        view.verCode.text = "${BuildConfig.VERSION_CODE}"
        view.registers.text = "Registros sin subir a la nube = 0"
        recyclerView =view.findViewById(R.id.list)
        recyclerView!!.layoutManager = LinearLayoutManager(activity)
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val enableBtn = sharedPreferences.getBoolean("pref_key_enable_registers", false)
        if (!enableBtn) view.btnRegisters.visibility = View.GONE
       /* view.btnRegisters.setOnClickListener {
            (activity as DetailActivity).replaceFragment(RegisterListFragment())
        }*/
        viewModel.getCountRegistersDB()

        viewModel.countDB.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            it?.let {
                view.registers.text = "Registros sin subir a la nube =  $it"
                if (it > 0) {
                    view.btnUpdateRegisters.visibility = View.VISIBLE
                    view.sync.setBackgroundColor(ContextCompat.getColor(
                            activity!!,
                            R.color.bar_decoded
                    ))
                    view.sync.setOnClickListener {
                        sendreport()
                    }

                    view.btnUpdateRegisters.setOnClickListener {
                        sendreport()
                    }
                } else if (it == 0) {
                    view.btnUpdateRegisters.visibility = View.GONE
                    view.sync.isEnabled = false
                    view.sync.setBackgroundColor(ContextCompat.getColor(
                            activity!!,
                            R.color.gray
                    ))
                }
            }
        })
        connectivityManager = activity?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        onDetectInternet(isOnline(activity!!))
        mValues = ArrayList()
        adapter= AdapterProjectDetails(mValues, activity, InternetConexion, this)
        recyclerView!!.adapter=adapter
        loadprojectsSqlite()
        return view
    }
    override fun onAttach(context: Context) {
        super.onAttach(context)
        ApplicationContext.analitycoComponent.inject(this)
    }
    override fun LoadProjectByids(projecids: String, nameProject: String){
        NameProject = nameProject
        Projectids=projecids
        CRUDService.startRequest(
                activity, Constantes.LIST_PROJECTS_URL + Projectids,
                Request.Method.GET, "", false
        )
    }
    @SuppressLint("Recycle", "SimpleDateFormat")
    fun loadprojectsSqlite(){
        val campos = arrayOf(DBHelper.PROJECT_TABLE_COLUMN_ID_Sync, DBHelper.PROJECT_TABLE_COLUMN_NAME_SYNC, DBHelper.PROJECT_TABLE_COLUMN_DATE_SYNC)
        val cursor: Cursor =  mSqLiteDatabase?.query(DBHelper.TABLE_NAME_PROJECT_SYNC, campos, null, null, null, null, null)!!
        var projectDetails: ProjectDetails?
        mValues!!.removeAll(mValues!!)
        while (cursor.moveToNext()){
            projectDetails = ProjectDetails()
            projectDetails.IdProject=cursor.getString(0)
            projectDetails.Name=cursor.getString(1)
            projectDetails.Date=cursor.getString(2)
            mValues?.add(projectDetails)
        }
    }
    @SuppressLint("UseRequireInsteadOfGet")
    override fun onResume() {
        super.onResume()
//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P)
        onDetectInternet(isOnline(activity!!))
        var intentFilter = IntentFilter()
        intentFilter.addAction(CRUDService.ACTION_REQUEST_SAVE)
        intentFilter.addAction(CRUDService.ACTION_REQUEST_GET)
        LocalBroadcastManager.getInstance(activity!!)
                .registerReceiver(requestBroadcastReceiver!!, intentFilter)
    }

    @SuppressLint("UseRequireInsteadOfGet")
    override fun onPause() {
        super.onPause()
        onDetectInternet(isOnline(activity!!))
        LocalBroadcastManager.getInstance(activity!!).unregisterReceiver(requestBroadcastReceiver!!)

    }

    override fun onStart() {
        super.onStart()
        registerConnectivityNetworkMonitor()
    }

    override fun onStop() {
        super.onStop()
        connectivityManager?.unregisterNetworkCallback(networkCallback)
    }

    private fun registerConnectivityNetworkMonitor() {

        val builder = NetworkRequest.Builder()
        connectivityManager?.registerNetworkCallback(
                builder.build(), networkCallback
        )
    }
    @Suppress("DEPRECATION")
    private fun isOnline(context: Context): Boolean {
        var isOnline = false
        try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val netInfo = cm.activeNetworkInfo
            //should check null because in airplane mode it will be null
            isOnline = netInfo != null && netInfo.isConnected
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        return isOnline
    }

    /**
     * @param noConnection boolean
     * @return intent
     */
    @Suppress("unused")
    private fun getConnectivityIntent(noConnection: Boolean): Intent {
        val intent = Intent()
        intent.action = "co.com.sersoluciones.CONNECTIVITY_CHANGE"
        intent.putExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, noConnection)
        return intent
    }

    private var isNetwork = false

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        /**
         * @param network
         */
        override fun onAvailable(network: Network) {
            if (!isNetwork) {
                DebugLog.log("Connection network available fragment")
                isNetwork = true
                onDetectInternet(true)
            }
        }
        /**
         * @param network
         */
        override fun onLost(network: Network) {
            if (isNetwork) {
                DebugLog.log("Connection network lost fragment")
                isNetwork = false
                onDetectInternet(false)
            }
        }
    }

    open fun onDetectInternet(online: Boolean) {
        Handler(Looper.getMainLooper()).post {
            if (online){
                DebugLog.log("hay internet")
                InternetConexion=true
                //adapter= AdapterProjectDetails(mValues, activity, InternetConexion)
                adapter?.online=true
            }
            else{
                DebugLog.log("no hay internet")
                InternetConexion=false
                //adapter= AdapterProjectDetails(mValues, activity, InternetConexion)
                adapter?.online=false
            }
        }
    }
    private fun loadDataFromServer(PersonalIds: String, EmployerIds: String, ContractIds: String) {
        data = LinkedHashMap()
        data!![Constantes.CONTENT_CONTRACT_URI] = Constantes.CONTRACT_BY_ARRAYIDS
        data!![Constantes.CONTENT_PERSONAL_URI] = Constantes.PERSONAL_BY_ARRAYIDS
        data!![Constantes.CONTENT_PROJECT_URI] = Constantes.LIST_PROJECTS_URL
        data!![Constantes.CONTENT_CONTRACT_PER_OFFLINE_URI] =
                Constantes.LIST_CONTRACT_PER_OFFLINE_URL
        data!![Constantes.CONTENT_EMPLOYER_URI] = Constantes.EMPLOYER_BY_ARRAYIDS
        fillArray = BooleanArray(data!!.size)
        launchRequests(PersonalIds, EmployerIds, ContractIds)
    }

    private fun launchRequests(PersonalIds: String, EmployerIds: String, ContractIds: String) {
        for ((pos, o) in data!!.entries.withIndex()) {
            val pair = o as Map.Entry<*, *>
            val values = ContentValues()
            when (pair.value) {
                Constantes.LIST_CONTRACT_PER_OFFLINE_URL -> {
                    //                viewModel.deleteAllContractPerson(projectId)
                    values.put(Constantes.KEY_SELECT, true)
                    values.put("project", true)
                    values.put("projectId", Projectids)
                    val paramsQuery = HttpRequest.makeParamsInUrl(values)
                    CRUDService.startRequest(
                            activity, pair.value as String?,
                            Request.Method.GET, paramsQuery, true
                    )
                }
                Constantes.PERSONAL_BY_ARRAYIDS -> {
                    CRUDService.startRequest(
                            activity, pair.value as String?,
                            Request.Method.POST, true, PersonalIds,true
                    )
                }
                Constantes.EMPLOYER_BY_ARRAYIDS -> {
                    values.put("&projectId", Projectids)
                    values.put("?Ids", EmployerIds)
                    CRUDService.startRequest(
                            activity, pair.value as String?,
                            Request.Method.GET, values.toString(), true, EmployerIds
                    )
                }
                Constantes.CONTRACT_BY_ARRAYIDS -> {
                    values.put("&projectId", Projectids)
                    values.put("?Ids", ContractIds)
                    CRUDService.startRequest(
                            activity, pair.value as String?,
                            Request.Method.GET, values.toString(), true, ContractIds
                    )
                }
                Constantes.LIST_PROJECTS_URL -> {
                    values.put(Constantes.KEY_SELECT, true)
                    val paramsQuery = HttpRequest.makeParamsInUrl(values)
                    CRUDService.startRequest(
                            activity, pair.value as String?,
                            Request.Method.GET, paramsQuery, true
                    )
                }
            }
        }
    }
    @SuppressLint("NewApi")
    override fun onStringResult(action: String?, option: Int, response: String?, url: String?) {
        when (option) {
            Constantes.SUCCESS_REQUEST -> {
                when (url) {
                    Constantes.LIST_PROJECTS_URL + Projectids -> {
                        val project = JSONObject(response)
                        val contractids = project.getString("ContractIds")
                        val personalids = project.getString("PersonalIds")
                        val employerids = project.getString("EmployerIds")
                        replace = contractids.replace("\\[|]|\\s".toRegex(), "").replace("""[""]""".toRegex(), "")
                        replacepersonalIds = personalids.replace("\\[|]|\\s".toRegex(), "")
                        replaceEmployerIds = employerids.replace("\\[|]|\\s".toRegex(), "").replace("""[""]""".toRegex(), "")
                        loadDataFromServer(replacepersonalIds!!, replaceEmployerIds!!, replace!!)
                    }
                    Constantes.PERSONAL_BY_ARRAYIDS -> {
                        DebugLog.log("tabla llena PERSONAL")
                        fillArray[0] = true

                    }
                    Constantes.LIST_PROJECTS_URL -> {
                        DebugLog.log("tabla llena LIST_PROJECTS_URL")
                        fillArray[1] = true
                        //loadProject = true
                    }
                    Constantes.LIST_CONTRACT_PER_OFFLINE_URL -> {
                        DebugLog.log("tabla llena LIST_CONTRACT_PER_OFFLINE_URL")
                        fillArray[2] = true
                    }
                    Constantes.CONTRACT_BY_ARRAYIDS -> {
                        DebugLog.log("tabla llena CONTRACT")
                        fillArray[3] = true

                    }
                    Constantes.EMPLOYER_BY_ARRAYIDS -> {
                        DebugLog.log("tabla llena EMPLOYER")
                        fillArray[4] = true
                    }
                }
                if(verifyFillData()){
                    val preferences = MyPreferences(requireActivity())
                    if(preferences.synContract && preferences.synPersonal && preferences.synEmployer && preferences.synContractOffline){
                        Toast.makeText(
                                activity,
                                "Sincronización de datos exitosa",
                                Toast.LENGTH_SHORT
                        ).show()
                        LoadInfo()
                    }else{
                        Toast.makeText(
                                activity,
                                "La sincronización de datos fallo, por favor vuelva a sincronizar",
                                Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
            Constantes.NOT_INTERNET, Constantes.BAD_REQUEST, Constantes.TIME_OUT_REQUEST, Constantes.NOT_CONNECTION -> {
                co.com.sersoluciones.barcodedetectorser.utilities.DebugLog.log("Sin internet")
                Toast.makeText(activity, "Sin conexion con el servidor. No se pudo sincronizar los datos", Toast.LENGTH_SHORT).show()
                fillArray[0] = true
                fillArray[1] = true
                fillArray[2] = true
                fillArray[3] = true
                fillArray[4] = true
                //loadProject = true
                verifyFillData()
            }
            Constantes.FORBIDDEN -> {
                Toast.makeText(
                        activity,
                        "Este usuario no tiene permisos para esta accion",
                        Toast.LENGTH_SHORT
                ).show()

            }
            Constantes.REQUEST_NOT_FOUND -> {
                Toast.makeText(
                        activity,
                        "NO SE PUDO ENCONTRAR",
                        Toast.LENGTH_SHORT
                ).show()
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
        return flag
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private  fun LoadInfo(){
        val sdf = SimpleDateFormat("yyyy-MM-dd hh:mm:ss")
        val currentDate = sdf.format(Date())
        viewModel.insertProjectsSyn(Projectids, NameProject, currentDate.toString())
        adapter = AdapterProjectDetails(mValues, activity, InternetConexion, this)
        recyclerView!!.adapter = adapter
        loadprojectsSqlite()
    }
    private fun sendreport(){
        GlobalScope.launch(Dispatchers.IO) {
            val reports = repository.getAllReportes()
            loop@ for (report in reports) {

                when (repository.sendReporte(report)) {
                    ApiStatus.ERROR -> {
                        co.com.sersoluciones.barcodedetectorser.utilities.DebugLog.logE("---------- reporte no enviado y puesto en un worker para futuro envio -> $report.id")
                    }
                    ApiStatus.DONE -> {
                        co.com.sersoluciones.barcodedetectorser.utilities.DebugLog.log("reporte borrado exitosamente ${report.id}")
                        repository.deleteReporte(report.id)
                    }
                }
            }
        }
    }
}

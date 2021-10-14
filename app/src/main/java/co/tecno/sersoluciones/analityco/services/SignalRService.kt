package co.tecno.sersoluciones.analityco.services

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.os.AsyncTask
import android.os.Handler
import android.os.Message
import co.com.sersoluciones.barcodedetectorser.utilities.DebugLog.*
import co.tecno.sersoluciones.analityco.adapters.repositories.EnrollmentRepository
import co.tecno.sersoluciones.analityco.models.PersonalRealTime
import co.tecno.sersoluciones.analityco.notifications.NotificationStatus
import co.tecno.sersoluciones.analityco.utilities.MyPreferences
import co.tecno.sersoluciones.analityco.viewmodels.PersonalDailyViewModel
import com.google.gson.Gson
import com.microsoft.signalr.HubConnection
import com.microsoft.signalr.HubConnectionBuilder
import com.microsoft.signalr.HubConnectionState
import com.microsoft.signalr.TransportEnum
import io.reactivex.Single
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class SignalRService(
    private val mContext: Context,
    private val preferences: MyPreferences,
    private val repository: EnrollmentRepository,
    private val viewModel: PersonalDailyViewModel
) : CoroutineScope {

    private var killedService = false
    private var notificationStatus: NotificationStatus? = null
    private var projectId: String? = null
    private var hubConnection: HubConnection? = null

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job
    private lateinit var job: Job

    fun onCreate() {
        job = Job()
        val id = System.identityHashCode(this)
        logW("ON CREATE SIGNALR $id")
        notificationStatus = NotificationStatus(mContext, mStateHandler)
//        if (preferences.isUserLogin)
        init()
    }

    private fun init() {

        if (hubConnection == null)
            hubConnection = HubConnectionBuilder
                .create("${preferences.urlServer}analitycoHub")
                .withHandshakeResponseTimeout(30 * 1000)
//                .withTransport(TransportEnum.WEBSOCKETS)
//                .shouldSkipNegotiate(true)
                .withAccessTokenProvider(Single.defer { Single.just(preferences.token) }).build()

        hubConnection?.on("ReceiveMessage", { message ->
            receiveMsg(message)

        }, String::class.java)

        hubConnection?.on("Send", { message ->
            logW("msg $message")
        }, String::class.java)

        hubConnection?.onClosed {
            logE("Conexion cerrada SignalR de lado de servidor")

            isOnline = false
            if (!killedService)
                startConnection()
        }

    }

    fun startConnection() {
        logW("Trying to start signalR service... ${preferences.urlServer}analitycoHub")
        if (hubConnection == null)
            hubConnection = HubConnectionBuilder
                .create("${preferences.urlServer}analitycoHub")
                .withHandshakeResponseTimeout(30 * 1000)
//                .withTransport(TransportEnum.LONG_POLLING)
                .withAccessTokenProvider(Single.defer { Single.just(preferences.token) }).build()

        notificationStatus?.setState(STATE_CONNECTING)
        HubConnectionTask { state ->
            log("state $state")
            handleConnectionHub(state)
        }.execute(hubConnection)
    }

    fun getProjectId(): String? = projectId

    fun setProject(projectId: String) {
        if (this.projectId != null && hubConnection?.connectionState == HubConnectionState.CONNECTED) {
            hubConnection?.send("RemoveFromProject", projectId)
        }
        this.projectId = projectId
        if (hubConnection?.connectionState == HubConnectionState.CONNECTED)
            hubConnection?.send("AddToProject", projectId)
    }

    internal class HubConnectionTask constructor(val listener: (Boolean) -> Unit) : AsyncTask<HubConnection, Void, Boolean>() {

        override fun doInBackground(vararg params: HubConnection?): Boolean {
            try {
                val hub = params[0]
                hub!!.start().blockingAwait()
                logW("Hub Connection CONNECTED ${hub.connectionState} connectionId ${hub.connectionId}")
                if (hub.connectionState == HubConnectionState.CONNECTED)
                    return true
            } catch (e: Exception) {
                e.printStackTrace()
                logE("ERROR: ${e.message}")
                if (e.message != null && e.message!!.contains("401 Unauthorized", ignoreCase = true))
                    return false
            }
            return false
        }

        override fun onPostExecute(result: Boolean?) {
            super.onPostExecute(result)
            listener.invoke(result!!)
        }
    }

    private fun handleConnectionHub(state: Boolean) {
        if (state) {
            isOnline = true
            if (!projectId.isNullOrEmpty())
                if (hubConnection?.connectionState == HubConnectionState.CONNECTED)
                    hubConnection?.send("AddToProject", projectId)

            updatePersonal()
            notificationStatus!!.setState(STATE_CONNECTED)

            if (killedService) {
                try {
                    hubConnection?.stop()
                } catch (e: Exception) {

                }
                return
            }
            killedService = false
        } else {
            isOnline = false
            if (!killedService)
                Handler().postDelayed({ startConnection() }, RESTART_DELAY)

        }
    }

    fun updatePersonal() {
        launch {
            try {
                repository.fetchDailyPersonal()
                viewModel.updateListPersonal()
            } catch (e: Exception) {
            }
        }
    }

    fun onDestroy() {
        val id = System.identityHashCode(this)
        logE("ON DESTROY SIGNAL R $id")
        killedService = true
        job.cancel()

        try {
            hubConnection?.stop()
        } catch (e: Exception) {
        }
        notificationStatus!!.cancelStatus()
        isOnline = false
        hubConnection = null
//        stopForeground(true)
    }


    private fun receiveMsg(message: String) {
        logW("msg: $message")

        val personalRealTime = Gson().fromJson(message, PersonalRealTime::class.java)
        launch {
            repository.updateRegister(personalRealTime)
            viewModel.updateListPersonal()
        }
    }

    fun sendMsg(personalRealTime: PersonalRealTime) {

        val message = Gson().toJson(personalRealTime)
        if (isOnline) {
            try {
                logW("SendMessageToProject ${personalRealTime.projectId} message $message")
                projectId = personalRealTime.projectId
                if (hubConnection?.connectionState == HubConnectionState.CONNECTED)
                    hubConnection?.send("SendMessageToProject", projectId, message)
            } catch (e: Exception) {
                e.printStackTrace()
                saveMsg(message)
            }
        } else
            saveMsg(message)
    }

    private fun saveMsg(message: String) {
        repository.createWorkRequest(message)
        repository.setReportWork()
    }

    /**
     * Check if network available or not
     *
     * @param context
     */
    @Suppress("unused", "DEPRECATION")
    fun isOnline(context: Context): Boolean {
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

    companion object {
        // Constants that indicate the current connection state
        const val STATE_NONE = 0       // we're doing nothing
        const val STATE_LISTEN = 1     // now listening for incoming connections
        const val STATE_CONNECTING = 2 // now initiating an outgoing connection
        const val STATE_CONNECTED = 3  // now connected to a remote device
        const val MESSAGE_STATE_CHANGE = 1
        var isOnline: Boolean = false
        const val RESTART_DELAY: Long = 10 * 1000
    }


    // The Handler that gets information back from the BluetoothChatService
    private val mStateHandler: Handler = @SuppressLint("HandlerLeak")
    object : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                MESSAGE_STATE_CHANGE ->

                    when (msg.arg1) {
                        STATE_CONNECTED -> {
                            logW("Conectado!")
                            notificationStatus!!.buildNotification(android.R.drawable.stat_notify_chat)
                        }
                        STATE_CONNECTING -> {
                            logW("Conectando...")
                            notificationStatus!!.buildNotification(android.R.drawable.stat_notify_sync)
                        }
                        STATE_LISTEN, STATE_NONE -> {
                            logE("Desconectado!")
                            notificationStatus!!.buildNotification(android.R.drawable.stat_notify_error)
                        }
                        else -> {
                            logW("unimplemented")
                            notificationStatus!!.buildNotification(android.R.drawable.stat_notify_error)
                        }
                    }
                else -> {
                    logW("unimplemented")
                }
            }
        }
    }

}
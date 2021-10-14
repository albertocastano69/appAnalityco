package co.tecno.sersoluciones.analityco.notifications

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Handler
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import co.com.sersoluciones.barcodedetectorser.utilities.DebugLog.logW
import co.tecno.sersoluciones.analityco.services.SignalRService


class NotificationStatus(private val mContext: Context, private val mHandler: Handler) {

    private var mState = SignalRService.STATE_NONE
    private var notificationManager: NotificationManagerCompat? = null
    private val ID_NOTI_STATUS = 7

    init {
        notificationManager = NotificationManagerCompat.from(mContext)
        setState(SignalRService.STATE_NONE)
    }

    fun buildNotification(icon: Int) {

        notificationManager!!.cancel(ID_NOTI_STATUS)
        val channelId = "com.sersoluciones.analityco.services.signalr_service"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(channelId)
        }

        val builder = NotificationCompat.Builder(mContext, channelId)
            .setContentTitle("Analityco")
            .setContentText("SignalR Service")
            .setAutoCancel(false)
            .setCategory(Notification.CATEGORY_SERVICE)
            .setSmallIcon(icon)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setWhen(System.currentTimeMillis())

        notificationManager!!.notify(ID_NOTI_STATUS, builder.build())
//        mService.startForeground(ID_NOTI_STATUS, builder.build())
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private fun createNotificationChannel(id: String) {
        val chan = NotificationChannel(
            id,
            "SignalR Service", NotificationManager.IMPORTANCE_LOW
        )
        chan.lightColor = Color.BLUE
        chan.lockscreenVisibility = Notification.VISIBILITY_SECRET

        val service = mContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(chan)
    }

    /**
     * Set the current state of the chat connection
     * @param state  An integer defining the current connection state
     */
    @Synchronized
    fun setState(state: Int) {
        logW("setState() $mState -> $state")
        mState = state

        // Give the new state to the Handler so the UI Activity can update
        mHandler.obtainMessage(SignalRService.MESSAGE_STATE_CHANGE, state, -1).sendToTarget()
    }

    fun cancelStatus() {
        notificationManager!!.cancel(ID_NOTI_STATUS)
        notificationManager!!.cancelAll()
    }
}
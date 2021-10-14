package co.tecno.sersoluciones.analityco.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import co.com.sersoluciones.barcodedetectorser.utilities.DebugLog
import co.tecno.sersoluciones.analityco.ApplicationContext
import co.tecno.sersoluciones.analityco.adapters.repositories.EnrollmentRepository
import co.tecno.sersoluciones.analityco.adapters.repositories.EnrollmentRepository.Companion.MSG_KEY
import co.tecno.sersoluciones.analityco.models.PersonalRealTime
import co.tecno.sersoluciones.analityco.utilities.MyPreferences
import com.google.gson.Gson
import javax.inject.Inject

class SendMsgWorker @Inject constructor(appContext: Context, workerParameters: WorkerParameters) :
    CoroutineWorker(appContext, workerParameters) {

    companion object {
        const val TAG = "SendMsgWorker"
    }

    init {
        ApplicationContext.analitycoComponent.injectIntoWorker(this)
    }

    @Inject
    lateinit var repository: EnrollmentRepository

    @Inject
    lateinit var preferences: MyPreferences

    override suspend fun doWork(): Result {
        val msg = inputData.getStringArray(MSG_KEY)

        val stringObj = msg?.get(0) ?: ""

        if (!preferences.isUserLogin) return Result.retry()
        val personalRealTime = Gson().fromJson(stringObj, PersonalRealTime::class.java) ?: return Result.failure()
        //        DebugLog.log("stringObj $stringObj")
        return try {
            repository.sendPersonalRealTime(
                personalRealTime
            )
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }

}
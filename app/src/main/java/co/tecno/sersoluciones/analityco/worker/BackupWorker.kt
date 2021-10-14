package co.tecno.sersoluciones.analityco.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import co.com.sersoluciones.barcodedetectorser.utilities.DebugLog.log
import co.tecno.sersoluciones.analityco.ApplicationContext
import co.tecno.sersoluciones.analityco.repository.AnalitycoRepository
import co.tecno.sersoluciones.analityco.repository.ApiStatus
import co.tecno.sersoluciones.analityco.utilities.MyPreferences
import javax.inject.Inject

class BackupWorker @Inject constructor(appContext: Context, params: WorkerParameters) :
    CoroutineWorker(appContext, params) {

    companion object {
        const val WORK_NAME = "BackupWorker"
    }

    init {
        ApplicationContext.analitycoComponent.injectIntoWorker(this)
    }

    @Inject
    lateinit var repository: AnalitycoRepository

    @Inject
    lateinit var preferences: MyPreferences

    override suspend fun doWork(): Result {

        var result: Result = Result.success()

        if (!preferences.isUserLogin) return Result.failure()

        val reports = repository.getAllReportes()
        loop@ for (report in reports) {

            result = when (repository.sendReporte(report)) {
                ApiStatus.ERROR -> {
                    Result.failure()
                    //                    break@loop
                }
                ApiStatus.DONE -> {
                    log("reporte borrado exitosamente ${report.id}")
                    repository.deleteReporte(report.id)
                    Result.success()
                }
            }

        }

        return result
    }

}
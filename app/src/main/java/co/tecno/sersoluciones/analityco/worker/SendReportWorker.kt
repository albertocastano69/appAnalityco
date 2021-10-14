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

class SendReportWorker @Inject constructor(appContext: Context, params: WorkerParameters) :
    CoroutineWorker(appContext, params) {

    companion object {
        const val WORK_NAME = "SendReportWorker"
    }

    init {
        ApplicationContext.analitycoComponent.injectIntoWorker(this)
    }

    @Inject
    lateinit var repository: AnalitycoRepository

    override suspend fun doWork(): Result {

        var result: Result = Result.success()

        val reports = repository.getAllReportes()
        loop@ for (report in reports) {

            when (repository.sendReporte(report)) {
                ApiStatus.ERROR -> {
                    log("reporte no enviado ${report.id}")
                    result = Result.retry()
                    break@loop
                }
                ApiStatus.DONE -> {
                    log("reporte borrado exitosamente ${report.id}")
                    repository.deleteReporte(report.id)
                    result = Result.success()
                }
            }

        }

        return result
    }

}
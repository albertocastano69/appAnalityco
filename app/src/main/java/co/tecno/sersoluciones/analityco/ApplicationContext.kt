package co.tecno.sersoluciones.analityco

import android.app.Application
import android.content.Context
import androidx.work.*
import co.tecno.sersoluciones.analityco.dependencyInjection.AppModule
import co.tecno.sersoluciones.analityco.dependencyInjection.ApplicationComponent
import co.tecno.sersoluciones.analityco.dependencyInjection.DaggerApplicationComponent
import co.tecno.sersoluciones.analityco.dependencyInjection.NetworkModule
import co.tecno.sersoluciones.analityco.utilities.MyPreferences
import co.tecno.sersoluciones.analityco.worker.BackupWorker
import co.tecno.sersoluciones.analityco.worker.FetchPositionWorker
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

/**
 * Created by Ser Soluciones SAS on 09/05/2017.
 * www.sersoluciones.com - contacto@sersoluciones.com
 */
class ApplicationContext : Application() {
    private var mRequestQueue: RequestQueue? = null
    private val applicationScope = CoroutineScope(Dispatchers.Default)

    /**
     * Metodo que se lanza en cuanto la aplicacion se crea
     */
    override fun onCreate() {
        super.onCreate()
        Companion.applicationContext = applicationContext
        instance = this
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            MetodosPublicos.scheduleJob(applicationContext)
//        }
        analitycoComponent = DaggerApplicationComponent
            .builder()
            .appModule(AppModule(this))
            .networkModule(NetworkModule(MyPreferences(this), this))
            .build()
        delayedInit()
    }

    private fun delayedInit() {
        applicationScope.launch {
            setupRecurringWork()
            setupRecurringWorkPosition()
        }
    }

    private fun setupRecurringWork() {

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val repeatingRequest = PeriodicWorkRequestBuilder<BackupWorker>(
            15,
            TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            BackupWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.REPLACE,
            repeatingRequest
        )
    }

    private fun setupRecurringWorkPosition() {

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val repeatingRequest = PeriodicWorkRequestBuilder<FetchPositionWorker>(
            5,
            TimeUnit.DAYS
        )
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            FetchPositionWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            repeatingRequest
        )
    }

    private val requestQueue: RequestQueue?
        get() {
            if (mRequestQueue == null) {
                mRequestQueue = Volley.newRequestQueue(applicationContext)
            }
            return mRequestQueue
        }

    fun <T> addToRequestQueue(req: Request<T>) {
        req.tag = TAG
        requestQueue!!.add(req)
    }

    companion object {
        @JvmField
        var applicationContext: Context? = null
        private const val TAG = "ApplicationContext"

        @JvmStatic
        @get:Synchronized
        var instance: ApplicationContext? = null
            private set
        lateinit var analitycoComponent: ApplicationComponent

        fun restartDagger(application: Application) {
            analitycoComponent = DaggerApplicationComponent
                .builder()
                .appModule(AppModule(application))
                .networkModule(NetworkModule(MyPreferences(application), application))
                .build()
        }
    }
}
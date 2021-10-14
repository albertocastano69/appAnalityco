package co.tecno.sersoluciones.analityco.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import co.tecno.sersoluciones.analityco.ApplicationContext
import co.tecno.sersoluciones.analityco.models.User
import co.tecno.sersoluciones.analityco.nav.CreatePersonalViewModel
import co.tecno.sersoluciones.analityco.repository.ApiStatus
import co.tecno.sersoluciones.analityco.utilities.MyPreferences
import com.google.gson.Gson
import javax.inject.Inject

class FetchPositionWorker @Inject constructor(appContext: Context, params: WorkerParameters) :
    CoroutineWorker(appContext, params) {

    companion object {
        const val WORK_NAME = "FetchPositionWorker"
    }

    init {
        ApplicationContext.analitycoComponent.injectIntoWorker(this)
    }

    @Inject
    lateinit var viewModel: CreatePersonalViewModel

    @Inject
    lateinit var preferences: MyPreferences

    override suspend fun doWork(): Result {

        var result: Result = Result.success()

        if (!preferences.isUserLogin) return Result.failure()

        val companyInfoId = getCompanyInfoByUser() ?: return Result.failure()
        val status = viewModel.updatePositions(companyInfoId)
        if (status == ApiStatus.ERROR)
            result = Result.failure()

        return result
    }

    private fun getCompanyInfoByUser(): String? {
        val profile = preferences.profile
        val user = Gson().fromJson(profile, User::class.java)
        if (!user.IsSuperUser) {
            if (user.Companies.size > 0 || user.IsAdmin) {
                return user.Companies.first().Id
            }
        }
        return null
    }

}
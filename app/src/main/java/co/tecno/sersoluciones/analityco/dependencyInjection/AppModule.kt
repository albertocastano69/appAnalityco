package co.tecno.sersoluciones.analityco.dependencyInjection

import android.app.Application
import android.location.Geocoder
import androidx.work.WorkManager
import co.tecno.sersoluciones.analityco.room.AnalitycoDatabase
import dagger.Module
import dagger.Provides
import javax.inject.Singleton


@Module
class AppModule(val app: Application) {

    @Singleton
    @Provides
    fun provideDb(app: Application) = AnalitycoDatabase.getInstance(app)

    @Singleton
    @Provides
    fun provideAnalitycoDao(db: AnalitycoDatabase) = db.analitycoDao

    @Singleton
    @Provides
    fun provideContext(application: Application) = application.applicationContext

    @Provides
    @Singleton
    fun provideApp() = app

    @Provides
    fun provideWorkManager(application: Application) = WorkManager.getInstance(application)
}
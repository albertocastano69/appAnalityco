package co.tecno.sersoluciones.analityco.dependencyInjection

import android.app.Application
import co.tecno.sersoluciones.analityco.retrofit.AnalitycoApiService
import co.tecno.sersoluciones.analityco.retrofit.CustomInterceptor
import co.tecno.sersoluciones.analityco.retrofit.TokenAuthenticator
import co.tecno.sersoluciones.analityco.utilities.MyPreferences
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton


const val REQUEST_TIMEOUT = 30L

@Module
class NetworkModule(val preferences: MyPreferences, val application: Application) {

    @Singleton
    @Provides
    fun provideOkHttpClient(): OkHttpClient {
        val httpLoggingInterceptor = HttpLoggingInterceptor()
        return OkHttpClient
            .Builder()
            .connectTimeout(REQUEST_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(REQUEST_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(REQUEST_TIMEOUT, TimeUnit.SECONDS)
            .addInterceptor(CustomInterceptor(preferences, application.applicationContext))
            .addInterceptor(httpLoggingInterceptor.apply {
                httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BASIC
            })
            .retryOnConnectionFailure(true)
            .authenticator(TokenAuthenticator(preferences))
            .build()
    }

    @Singleton
    @Provides
    fun provideMoshi(): Moshi {
        return Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }

    @Singleton
    @Provides
    fun provideRetrofit(moshi: Moshi, client: OkHttpClient): Retrofit {
        val baseUrl = preferences.urlServer
        return Retrofit.Builder()
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .addCallAdapterFactory(CoroutineCallAdapterFactory())
            .baseUrl(baseUrl)
            .build()
    }

    @Singleton
    @Provides
    fun provideAnalitycoApiService(retrofit: Retrofit) =
        retrofit.create(AnalitycoApiService::class.java)
}
package com.carlosjimz87.copyfiles.di

import android.app.Application
import android.app.DownloadManager
import android.content.Context
import com.carlosjimz87.copyfiles.core.Constants.BASE_URL
import com.carlosjimz87.copyfiles.core.Constants.TIMEOUT_SEC
import com.carlosjimz87.copyfiles.core.Constants.WRITE_TIMEOUT_SEC
import com.carlosjimz87.copyfiles.data.api.DownloaderApi
import com.carlosjimz87.copyfiles.generators.ContentWorkerGenerator
import com.carlosjimz87.copyfiles.managers.DownloadsManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideContext(
        application: Application
    ): Context = application.applicationContext

    @Provides
    @Singleton
    fun provideDownloadManager(
        context: Context
    ): DownloadManager {
        return context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    }

    @Provides
    @Singleton
    fun provideHttpInterceptor(): HttpLoggingInterceptor {
        val logging = HttpLoggingInterceptor()
        logging.level = HttpLoggingInterceptor.Level.BODY
        return logging
    }

    @Provides
    @Singleton
    fun provideHttpClient(httpLoggingInterceptor: HttpLoggingInterceptor): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(httpLoggingInterceptor)
            .connectTimeout(TIMEOUT_SEC, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT_SEC, TimeUnit.SECONDS)
            .writeTimeout(WRITE_TIMEOUT_SEC, TimeUnit.SECONDS)
//            .protocols(Collections.singletonList(Protocol.HTTP_1_1))
            .build()
    }

    @Provides
    @Singleton
    fun provideDownloaderApi(httpClient: OkHttpClient): DownloaderApi {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(httpClient)
            .build()
            .create(DownloaderApi::class.java)
    }


    @Provides
    @Singleton
    fun provideDownloadsManager(
        context: Context,
        downloadManager: DownloadManager,
        downloaderApi: DownloaderApi
    ): DownloadsManager {
        return DownloadsManager(
            context,
            downloadManager,
            downloaderApi,
        )
    }

    @Provides
    @Singleton
    fun provideContentWorkerGenerator(
        context: Context
    ): ContentWorkerGenerator {
        return ContentWorkerGenerator(
            context
        )
    }
}
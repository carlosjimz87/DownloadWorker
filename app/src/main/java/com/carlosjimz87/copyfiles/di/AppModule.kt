package com.carlosjimz87.copyfiles.di

import android.app.Application
import android.app.DownloadManager
import android.content.Context
import com.carlosjimz87.copyfiles.managers.DownloadsManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
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
    fun provideHTTPDownloader(
        context: Context,
        downloadManager: DownloadManager,
    ): DownloadsManager {
        return DownloadsManager(
            context,
            downloadManager,
        )
    }
}
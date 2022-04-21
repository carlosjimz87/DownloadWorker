package com.carlosjimz87.copyfiles

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.carlosjimz87.copyfiles.core.SampleData.photosDownload
import com.carlosjimz87.copyfiles.core.SampleData.videosDownload
import com.carlosjimz87.copyfiles.data.api.DownloaderApi
import com.carlosjimz87.copyfiles.managers.DownloadsManager
import com.carlosjimz87.copyfiles.managers.FileManager
import com.carlosjimz87.copyfiles.models.DownloadRemote
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var downloadCopyManager: DownloadsManager

    @Inject
    lateinit var downloaderApi: DownloaderApi

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val manager = FileManager.Builder.init(baseContext)
        val dataDestination = manager.getDataLocation()
        val extDestination = manager.getExtLocation()

        Timber.w("DATA_DESTINATION: $dataDestination")
        Timber.w("EXTERNAL_DESTINATION: $extDestination")

        val finalDownloads = photosDownload.plus(videosDownload)
        executeDownload(extDestination, finalDownloads)
    }

    private fun executeDownload(dataDestination: String?, downloads: List<DownloadRemote>) {
        lifecycleScope.launchWhenStarted {
            dataDestination?.let { destination ->
                downloads.forEach { download ->

                    val finalDownload = download.copy(
                        destination = destination,
                        startTime = System.currentTimeMillis()
                    )

                    Timber.d("Proceed to download $download in $destination")
                    // execute download via RETROFIT

                    withContext(Dispatchers.IO) {
                        try {
                            downloadCopyManager.download(
                                finalDownload,
                                DownloadsManager.METHOD.RETROFIT
                            )
                            Timber.d("DOWNLOAD SUCCEEDED ${download.name}")
                        } catch (e: Exception) {
                            Timber.e("DOWNLOAD FAILED: ${download.name} [${e.message}]")
                        }
                    }

                }
            }

        }
    }
}
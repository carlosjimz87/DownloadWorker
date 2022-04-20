package com.carlosjimz87.copyfiles

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.carlosjimz87.copyfiles.core.SampleData
import com.carlosjimz87.copyfiles.data.api.DownloaderApi
import com.carlosjimz87.copyfiles.managers.DownloadsManager
import com.carlosjimz87.copyfiles.managers.FileManager
import dagger.hilt.android.AndroidEntryPoint
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

        executeDownload(extDestination)
    }

    private fun executeDownload(dataDestination: String?) {
        lifecycleScope.launchWhenStarted {
            dataDestination?.let { destination ->
                SampleData.photosDownload.forEach { download ->

                    val finalDownload = download.copy(
                        destination = destination,
                    )

                    Timber.d("Proceed to download $download in $destination")
                    // execute download via RETROFIT

                    downloadCopyManager.download(
                        finalDownload,
                        DownloadsManager.METHOD.RETROFIT
                    ).fold(
                        {
                            Timber.e("DOWNLOAD FAILED: ${it.message}")
                        },
                        {
                            Timber.d("DOWNLOAD SUCCEEDED")
                        }
                    )
                }
            }

        }
    }
}
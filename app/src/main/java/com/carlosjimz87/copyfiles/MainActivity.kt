package com.carlosjimz87.copyfiles

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import com.carlosjimz87.copyfiles.core.SampleData.photosDownload
import com.carlosjimz87.copyfiles.core.SampleData.videosDownload
import com.carlosjimz87.copyfiles.data.api.DownloaderApi
import com.carlosjimz87.copyfiles.generators.ContentWorkerGenerator
import com.carlosjimz87.copyfiles.managers.DownloadsManager
import com.carlosjimz87.copyfiles.managers.FileManager
import com.carlosjimz87.copyfiles.models.DownloadRemote
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
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

    @Inject
    lateinit var contentWorkerGenerator: ContentWorkerGenerator

    private var downloading: MutableLiveData<Boolean> = MutableLiveData(false)
    private var message: MutableLiveData<String> = MutableLiveData("Init")
    private var downloadCounter: Int = 0
    private var totalToDownload: Int = photosDownload.size + videosDownload.size

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val manager = FileManager.Builder.init(baseContext)
        val dataDestination = manager.getDataLocation()
        val extDestination = manager.getExtLocation()

        Timber.w("DATA_DESTINATION: $dataDestination")
        Timber.w("EXTERNAL_DESTINATION: $extDestination")

        subscribeObservers()

        downloading.value = true
        executeDownload(dataDestination, photosDownload)
        executeDownload(extDestination, videosDownload)
        downloading.value = false
    }

    private fun subscribeObservers() {

        message.observe(this) { m ->
            Timber.d("MESSAGE: $m")
            textView.text = m
        }

        downloading.observe(this) { d ->
            Timber.d("DOWNLOADING: $d")
            when (d) {
                true -> {
                    progressCircular.visibility = View.VISIBLE
                }
                false -> {
                    progressCircular.visibility = View.GONE
                }
            }
        }

    }

    private fun executeDownload(dataDestination: String?, downloads: List<DownloadRemote>) {
        Timber.d("Downloads: ${downloads.size}")
        lifecycleScope.launchWhenStarted {

            dataDestination?.let { destination ->
                downloads.forEach { download ->

                    downloadCounter++
                    val finalDownload = download.copy(
                        destination = destination,
                        startTime = System.currentTimeMillis()
                    )

                    Timber.d("Proceed to download $download in $destination")
                    message.value =
                        "Downloading: ${download.name} ($downloadCounter)/$totalToDownload"
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

                message.value = "Downloads completed: $downloadCounter"
                Toast.makeText(baseContext, "Downloads completed", Toast.LENGTH_SHORT).show()
            }

        }
    }
}
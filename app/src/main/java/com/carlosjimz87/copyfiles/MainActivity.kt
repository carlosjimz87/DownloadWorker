package com.carlosjimz87.copyfiles

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
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
    private var downloadCounter: Int = 1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val manager = FileManager.Builder.init(baseContext)
        val dataDestination = manager.getDataLocation()
        val extDestination = manager.getExtLocation()

        Timber.w("DATA_DESTINATION: $dataDestination")
        Timber.w("EXTERNAL_DESTINATION: $extDestination")

        val finalDownloads = photosDownload.plus(videosDownload)
        subscribeObservers()
        executeDownload(extDestination, finalDownloads)
    }

    private fun subscribeObservers() {

        message.observe(this) { m ->
            Timber.d("MESSAGE: $m")
            textView.text = m
        }

        downloading.observe(this) { d ->
            Timber.d("DOWNLOADING: $d")
            when(d){
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
                downloading.value =true
                downloads.forEach { download ->

                    val finalDownload = download.copy(
                        destination = destination,
                        startTime = System.currentTimeMillis()
                    )

                    Timber.d("Proceed to download $download in $destination")
                    message.value = "Downloading: ${download.name} ($downloadCounter)"
                    // execute download via RETROFIT

                    withContext(Dispatchers.IO) {
                        try {
                            downloadCopyManager.download(
                                finalDownload,
                                DownloadsManager.METHOD.RETROFIT
                            )

                            downloadCounter++
                            Timber.d("DOWNLOAD SUCCEEDED ${download.name}")
                        } catch (e: Exception) {
                            Timber.e("DOWNLOAD FAILED: ${download.name} [${e.message}]")
                        }
                    }

                }

                downloading.value =false
                message.value = "Downloads completed: $downloadCounter"
            }

        }
    }
}
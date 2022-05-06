package com.carlosjimz87.copyfiles

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import arrow.fx.IO
import com.carlosjimz87.copyfiles.core.SampleData.photosDownload
import com.carlosjimz87.copyfiles.core.SampleData.videosDownload
import com.carlosjimz87.copyfiles.core.SampleData.zipDownload
import com.carlosjimz87.copyfiles.data.api.DownloaderApi
import com.carlosjimz87.copyfiles.generators.ContentWorkerGenerator
import com.carlosjimz87.copyfiles.managers.DownloadsManager
import com.carlosjimz87.copyfiles.managers.FileManager
import com.carlosjimz87.copyfiles.managers.FileManager.Companion.unzipFile
import com.carlosjimz87.copyfiles.models.DownloadRemote
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val PLANTILLAS_FOLDER = "OTS${File.separator}MM${File.separator}PLANTILLAS"

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
    private lateinit var manager: FileManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        manager = FileManager.Builder.init(baseContext)

        val downloadsFolder = manager.getDownloadsLocation()
        val dataFolder = manager.getDataLocation()
        val externalFolder = manager.getExtLocation()

        Timber.w("DOWNLOADS: $downloadsFolder")
        Timber.w("DATA: $dataFolder")
        Timber.w("EXTERNAL: $externalFolder")

        subscribeObservers()


        lifecycleScope.launchWhenStarted {
            downloading.value = true

            testDownloadCopy(dataFolder, externalFolder)

            testZip(dataFolder, downloadsFolder)

            downloading.value = false
        }

    }

    private suspend fun testDownloadCopy(dataFolder: String?, externalFolder: String?) {
//        executeDownload(dataFolder, zipDownload)
//        executeDownload(dataFolder, photosDownload)
        executeDownload(externalFolder, videosDownload)
    }

    private suspend fun testZip(dataFolderPath: String?, downloadsFolderPath: String? = null) {
        withContext(Dispatchers.IO){
            if (dataFolderPath?.isNotEmpty() == true) {
                val files = FileManager.filesInFolder(File(dataFolderPath))

                val pairs = hashMapOf<String, String?>()
                files?.forEach { file ->
                    pairs[file.name] = downloadsFolderPath
                }
                Timber.d("To Unzip: ${files?.size}")

                pairs.entries.forEach { (filename, destination) ->
                    unpackZip(
//                    sourcePath = extDestination + File.separator + PLANTILLAS_FOLDER,
                        sourcePath = dataFolderPath,
                        zipname = filename,
                        destination = destination
                    ).attempt()
                        .unsafeRunSync().fold(
                            {
                                Timber.e("Error while Unzip $filename ${it.message}")
                            },
                            {
                                if (it) {
                                    Timber.d("Unzip $filename was successful")
                                } else {
                                    Timber.e("Error while Unzip $filename")
                                }
                            }
                        )
                }
            }
        }
    }

    private fun unpackZip(
        sourcePath: String,
        zipname: String,
        destination: String?
    ): IO<Boolean> {
        val sourceFile = File(sourcePath, zipname)
        Timber.d("UnpackingZip $zipname from $sourceFile to $destination")
        if (sourceFile.exists() && sourceFile.isFile && sourceFile.extension == "zip") {

            val destinationFile: File? = getDestinationFile(destination, sourcePath, zipname)

            return IO { sourceFile.unzipFile(destinationFile) }
        }

        return IO { false }
    }

    private fun getDestinationFile(
        destination: String? = null,
        path: String,
        zipname: String
    ): File? {
        val destinationFile: File? = when (destination) {
            null -> {
                null
            }
            "" -> {
                File(path, zipname.replace(".zip", ""))
            }
            else -> {
                File(destination, zipname.replace(".zip", ""))
            }
        }
        return destinationFile
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

    private suspend fun executeDownload(dataDestination: String?, downloads: List<DownloadRemote>) {
        Timber.d("To Download: ${downloads.size}")

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
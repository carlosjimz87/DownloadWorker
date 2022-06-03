package com.carlosjimz87.copyfiles

import android.Manifest
import android.os.Build
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
import com.microsoft.appcenter.AppCenter
import com.microsoft.appcenter.analytics.Analytics
import com.microsoft.appcenter.crashes.Crashes
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import timber.log.Timber
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), EasyPermissions.PermissionCallbacks {

    private var permissions = arrayOf(
        // TO add a new runtime permission, add it here and ...
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    companion object {
        // add the permission state flag here
        const val PERMISSION_WRITE_EXTERNAL_STORAGE = 1
    }

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

        setupAppCenter()

        manager = FileManager.Builder.init(baseContext)

        subscribeObservers()

        init()

    }

    private fun setupAppCenter() {
        AppCenter.start(
            application, "5879625b-35a0-4120-bd90-6f68e04c184d",
            Analytics::class.java, Crashes::class.java
        )
    }

    private fun getLocations(): Triple<String?, String?, String?> {
        val downloadsFolder = manager.getDownloadsLocation()
        val dataFolder = manager.getDataLocation()
        val externalFolder = manager.getExtLocation()

        Timber.w("DOWNLOADS: $downloadsFolder")
        Timber.w("DATA: $dataFolder")
        Timber.w("EXTERNAL: $externalFolder")

        return Triple(downloadsFolder, dataFolder, externalFolder)
    }

    private fun init() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (hasWriteExternalStoragePermission()) {
                start()
            } else {
                requestWriteExternalStoragePermission()
            }
        } else {
            start()
        }
    }

    private fun start() {
//        startDownloads()
        Toast.makeText(this, "Initialization Done", Toast.LENGTH_LONG).show()
    }

    private fun startDownloads() {
        getLocations().let { (downloadsFolder, dataFolder, externalFolder) ->
            lifecycleScope.launchWhenStarted {
                downloading.value = true

                testDownloadCopy(dataFolder, externalFolder)

                testZip(dataFolder, downloadsFolder)

                downloading.value = false
            }
        }
    }

    private fun hasWriteExternalStoragePermission() =
        EasyPermissions.hasPermissions(
            baseContext,
            *permissions
        )

    private fun requestWriteExternalStoragePermission() {
        if (EasyPermissions.hasPermissions(this, *permissions)) {
            start()
        } else {
            EasyPermissions.requestPermissions(
                this,
                resources.getString(R.string.external_permission_text),
                PERMISSION_WRITE_EXTERNAL_STORAGE,
                *permissions
            )
        }
    }

    private suspend fun testDownloadCopy(dataFolder: String?, externalFolder: String?) {
        executeDownload(dataFolder, zipDownload)
        executeDownload(dataFolder, photosDownload)
        executeDownload(externalFolder, videosDownload)
    }

    private suspend fun testZip(dataFolderPath: String?, downloadsFolderPath: String? = null) {
        withContext(Dispatchers.IO) {
            if (dataFolderPath?.isNotEmpty() == true) {
                val files = FileManager.filesInFolder(File(dataFolderPath))

                val pairs = hashMapOf<String, String?>()
                files?.filter { it.extension == "zip" }?.forEach { file ->
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

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show()
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            AppSettingsDialog.Builder(this).build().show()
        } else {
            requestWriteExternalStoragePermission()
        }
        Toast.makeText(baseContext, "Write External Permission Was Denied", Toast.LENGTH_SHORT)
            .show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

}
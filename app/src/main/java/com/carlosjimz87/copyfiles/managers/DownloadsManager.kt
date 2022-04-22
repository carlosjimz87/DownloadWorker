package com.carlosjimz87.copyfiles.managers

import android.accounts.NetworkErrorException
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import com.carlosjimz87.copyfiles.core.Constants.BASE_URL
import com.carlosjimz87.copyfiles.data.api.DownloaderApi
import com.carlosjimz87.copyfiles.models.DownloadRemote
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.io.IOException
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


@Suppress("BlockingMethodInNonBlockingContext")
class DownloadsManager @Inject constructor(
    private val context: Context,
    private val downloadManager: DownloadManager,
    private val downloaderApi: DownloaderApi,
) {
    enum class METHOD {
        RETROFIT,
        MANAGER
    }

    private val downloadsFolder = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)

    suspend fun download(
        download: DownloadRemote,
        method: METHOD = METHOD.RETROFIT
    ): DownloadRemote {

        return when (method) {
            METHOD.RETROFIT -> downloadWithRetrofit(download)
            METHOD.MANAGER -> downloadWithManager(download)
        }
    }


    private suspend fun downloadWithManager(
        download: DownloadRemote,
    ): DownloadRemote {

        Timber.d("Downloading ${download.id} with Manager")

        executeDownload(download)

        waitForDownloadToComplete()

        copyFileAndDelete(download)
        return download
    }

    @Throws(Exception::class)
    suspend fun downloadWithRetrofit(download: DownloadRemote): DownloadRemote {
        Timber.d("Downloading ${download.name} with Retrofit")
//        val response = downloaderApi.getFile(download.type, download.id)
        val response = downloaderApi.getFile(download.url)

        response.body()?.let { body ->

            Timber.d("Reading $body")

            return withContext(Dispatchers.IO) {
                val file = File(download.destination, download.name)
                Timber.d("Creating stream File at ${file.path}")

                FileManagerKt.copyBytes(body.byteStream(), file)
                download
            }
        }
        if (!response.isSuccessful) {
            Timber.e("Network error ${response.code()}: ${response.message()}")
            throw NetworkErrorException("Network error ${response.code()}: ${response.message()}")
        }
        Timber.e("Unknown error $response")
        throw UnknownError("Unknown error")
    }

    private fun executeDownload(download: DownloadRemote) {

        val sourceUri: Uri = Uri.parse("$BASE_URL/${download.type}/${download.id}/download/")
        val request = DownloadManager.Request(sourceUri)
        request.setAllowedOverRoaming(true)
        request.setAllowedOverMetered(true)
        request.setTitle("Content")
        request.setDescription("Content_download")

        Timber.d("Execute download for ${download.name}")

        request.setDestinationInExternalFilesDir(
            context.applicationContext,
            Environment.DIRECTORY_DOWNLOADS,
            File.separator + download.name
        )

        request.setNotificationVisibility(
            DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
        )

        downloadManager.enqueue(request)
    }

    private fun checkIfAlreadyDownloaded(downloadedFile: File): Boolean {
        return downloadedFile.exists()
    }

    private fun checkIfPrevDownloading(file: File): Boolean {
        var isDownloading = false
        val query = DownloadManager.Query()
        query.setFilterByStatus(
            DownloadManager.STATUS_PAUSED or
                    DownloadManager.STATUS_PENDING or
                    DownloadManager.STATUS_RUNNING or
                    DownloadManager.STATUS_SUCCESSFUL
        )
        val cur: Cursor = downloadManager.query(query)
        cur.use {
            val col = it.getColumnIndex(
                DownloadManager.COLUMN_LOCAL_FILENAME
            )
            cur.moveToFirst()
            while (!cur.isAfterLast) {
                isDownloading = isDownloading || (file.path.trim() == cur.getString(col).trim())
                Timber.d("Checking ${file.path.trim()} == ${cur.getString(col).trim()}")
                if (isDownloading) break
                cur.moveToNext()
            }
        }
        return isDownloading
    }


    private fun copyFileAndDelete(
        download: DownloadRemote
    ): DownloadRemote {
        val downloadedFile = File(downloadsFolder, download.name)
        val destinationFile = File(download.destination, download.name)

        Timber.d("Copying file ${download.name} from $downloadsFolder to ${download.destination}")

        try {
            downloadedFile.copyTo(destinationFile, true)
            downloadedFile.delete()
        } catch (ex: Exception) {
            when (ex) {
                is NoSuchFileException, is IOException, is FileSystemException -> {
                    Timber.e("Error copying/deleting ${downloadedFile.path} (${ex.message}")
                    throw ex
                }
                else -> {/*ignored*/
                }
            }
        }

        return download
    }


    private suspend fun waitForDownloadToComplete(): Long {
        return suspendCoroutine { continuation ->
            Timber.d("Waiting for download to complete")
            val downloadReceiver = object : BroadcastReceiver() {

                override fun onReceive(c: Context, intent: Intent) {

                    val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L)
                    context.unregisterReceiver(this)
                    continuation.resume(id)
                }
            }

            context.registerReceiver(
                downloadReceiver,
                IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
            )
        }
    }
}
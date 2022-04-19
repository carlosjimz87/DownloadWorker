package com.carlosjimz87.copyfiles.managers

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import arrow.core.Either
import arrow.fx.IO
import com.carlosjimz87.copyfiles.models.DownloadRemote
import timber.log.Timber
import java.io.File
import java.io.IOException
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


class DownloadsManager @Inject constructor(
    private val context: Context,
    private val downloadManager: DownloadManager,
) {

    private val downloadsFolder = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)

    fun downloadFileFold(
        remotePath: String,
        destinationPath: String,
        fileName: String
    ): Either<Throwable, DownloadRemote> {
        return downloadFile(
            remotePath,
            destinationPath,
            fileName
        )
            .attempt()
            .unsafeRunSync()
    }

    private fun downloadFile(
        remotePath: String,
        destinationPath: String,
        fileName: String
    ): IO<DownloadRemote> {

        return IO {
            Timber.d("Downloading $fileName of $remotePath to Downloads")

            val download = DownloadRemote(
                remotePath,
                fileName,
                destinationPath
            )

            val downloadedFile = File(downloadsFolder, fileName)
//            if (checkIfPrevDownloading(downloadedFile)) {
//                Timber.d("File $fileName already downloading.")
//                return@IO download.copy(shouldCopy = false)
//            }

//            if (checkIfAlreadyDownloaded(downloadedFile)) {
//                Timber.d("File $fileName already downloaded.")
//                return@IO download
//            }

            executeDownload(download)

            waitForDownloadToComplete()

            download
        }.flatMap {
            copyFileAndDelete(it)
        }
    }

    private fun executeDownload(download: DownloadRemote) {
        val sourceUri: Uri = Uri.parse(download.remotePath)
        val request = DownloadManager.Request(sourceUri)
        request.setAllowedOverRoaming(true)
        request.setAllowedOverMetered(true)
        request.setTitle("Content")
        request.setDescription("Content_download")

        Timber.d("Execute download for ${download.fileName} from ${download.remotePath}]")

        request.setDestinationInExternalFilesDir(
            context.applicationContext,
            Environment.DIRECTORY_DOWNLOADS,
            File.separator + download.fileName
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
    ): IO<DownloadRemote> {
        return IO {
            val downloadedFile = File(downloadsFolder, download.fileName)
            val destinationFile = File(download.destination, download.fileName)

            Timber.d("Copying file ${download.fileName} from $downloadsFolder to ${download.destination}")

            try {
                downloadedFile.copyTo(destinationFile, true)
                downloadedFile.delete()
            } catch (ex: Exception) {
                when (ex) {
                    is NoSuchFileException, is IOException, is FileSystemException -> {
                        Timber.e("Error copying/deleting ${download.fileName} (${ex.message}")
                        throw ex
                    }
                    else -> {/*ignored*/
                    }
                }
            }

            download
        }
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
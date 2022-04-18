package com.carlosjimz87.copyfiles.managers

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Environment
import arrow.core.Either
import arrow.fx.IO
import com.carlosjimz87.copyfiles.models.DownloadRemote
import timber.log.Timber
import java.io.File
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class DownloadManager(private val context: Context) {
    private val downloadManager: DownloadManager =
        context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
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
            )

            executeDownload(download)

            waitForDownloadToComplete()

            download
        }.flatMap {
            copyFileAndDelete(destinationPath, fileName, it)
        }
    }

    private fun executeDownload(download: DownloadRemote) {
        val sourceUri: Uri = Uri.parse(download.remotePath)
        val request = DownloadManager.Request(sourceUri)
        request.setAllowedOverRoaming(true)
        request.setAllowedOverMetered(true)
        request.setTitle("Content")
        request.setDescription("Content_download")
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


    private fun copyFileAndDelete(
        destinationPath: String,
        filename: String,
        download: DownloadRemote
    ): IO<DownloadRemote> {
        return IO {
            val originFile = File(downloadsFolder, filename)
            val destinationFile = File(destinationPath, filename)
            val thread = Thread.currentThread().name
            Timber.d("Copying file $filename from $downloadsFolder to $destinationPath on [$thread]")

            try {
                originFile.copyTo(destinationFile, true)
                originFile.deleteOnExit()
            } catch (ex: Exception) {
                when (ex) {
                    is NoSuchFileException, is IOException, is FileSystemException -> {
                        Timber.e("Error copying/deleting $filename (${ex.message}")
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
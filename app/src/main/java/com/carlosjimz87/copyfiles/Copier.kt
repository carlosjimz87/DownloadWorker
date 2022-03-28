package com.carlosjimz87.copyfiles

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Environment
import arrow.core.Either
import arrow.fx.IO
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class Copier(private val context: Context) {
    private val downloadManager: DownloadManager =
        context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    private val downloadsFolder = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)


    fun downloadFileFold(
        remotePath: String,
        destinationPath: String,
        fileName: String
    ): Either<Throwable, Download> {
        return downloadFile(
            remotePath,
            destinationPath,
            fileName
        )
            .attempt()
            .unsafeRunSync()
    }

    fun downloadFile(
        remotePath: String,
        destinationPath: String,
        fileName: String
    ): IO<Download> {

        val download = Download(remotePath)
        return IO {
            CoroutineScope(Dispatchers.IO).launch {
                Timber.d("Thread Copier: ${Thread.currentThread().name}")
                Timber.d("Downloading $fileName of $remotePath to Downloads")
                val sourceUri: Uri = Uri.parse(remotePath)
                val request = DownloadManager.Request(sourceUri)
                request.setAllowedOverRoaming(true)
                request.setAllowedOverMetered(true)
                request.setTitle("Content")
                request.setDescription("Content_download")
                request.setDestinationInExternalFilesDir(
                    context.applicationContext,
                    Environment.DIRECTORY_DOWNLOADS,
                    File.separator + fileName
                )

                request.setNotificationVisibility(
                    DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
                )
                try {
                    downloadManager.enqueue(request)
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                waitForDownloadToComplete()
                copyFileAndDelete(destinationPath, fileName)
            }
            download
        }
    }


    private fun copyFileAndDelete(
        destinationPath: String,
        filename: String
    ) {
        val originFile = File(downloadsFolder, filename)
        val destinationFile = File(destinationPath, filename)
        try {

            Timber.d("Copying file $filename to ${destinationFile.absolutePath}")
            originFile.copyTo(destinationFile, true)
            originFile.delete()
        } catch (e: Exception) {
            Timber.e("Error copying file $filename (${e.message})")
        }
    }


    private suspend fun waitForDownloadToComplete(): Long {
        return suspendCoroutine { continuation ->
            val downloadReceiver = object : BroadcastReceiver() {
                override fun onReceive(c: Context, intent: Intent) {
                    val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L)
                    Timber.d("Download of ID:·$id received")
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
package com.carlosjimz87.copyfiles.workers

import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.carlosjimz87.copyfiles.core.Constants.DOWNLOAD_CHANNEL_ID
import com.carlosjimz87.copyfiles.core.Constants.DOWNLOAD_CHANNEL_NAME
import com.carlosjimz87.copyfiles.core.Constants.RETRIES
import com.carlosjimz87.copyfiles.managers.DownloadsManager
import com.carlosjimz87.copyfiles.models.DownloadRemote
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.lang.Exception
import javax.inject.Inject
import kotlin.random.Random


class DownloadContentWorker(
    val context: Context,
    params: WorkerParameters
) :
    CoroutineWorker(context, params) {

    @Inject
    lateinit var downloadsManager: DownloadsManager

    override suspend fun doWork(): Result {
        startForegroundService()
        val contentID = inputData.getInt("content_id", 0)
        val folder = inputData.getString("folder")
        val filename = inputData.getString("filename")
        val md5 = inputData.getString("md5")
        Timber.d("Executing Content DownloadWorker for $contentID file: $filename to folder: $folder md5 $md5")

        return withContext(Dispatchers.IO) {

            val download = DownloadRemote(
                contentID.toLong(),
                "photo",
                File(folder, filename).path
            )

            when {
                runAttemptCount < RETRIES -> {
                    try {
                        downloadsManager.download(download, DownloadsManager.METHOD.RETROFIT)
                        Result.success()
                    }
                    catch (e:Exception){
                        Result.failure()
                    }
//                        .fold(
//                        {
//                            Timber.e("Error ${it.cause}")
//                            Result.retry()
//                        },
//                        {
//                            Timber.d("Content downloaded $contentID")
//                            Result.success()
//                        }
//                    )
                }
                else -> {
                    Result.failure()
                }
            }
        }
    }


    private suspend fun startForegroundService() {
        setForeground(
            ForegroundInfo(
                Random.nextInt(),
                NotificationCompat.Builder(context, DOWNLOAD_CHANNEL_ID)
                    .setContentText("Downloading...")
                    .setContentTitle(DOWNLOAD_CHANNEL_NAME)
                    .build()
            )
        )
    }
}
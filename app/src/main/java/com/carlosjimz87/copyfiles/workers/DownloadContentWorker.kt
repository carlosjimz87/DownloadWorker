package com.carlosjimz87.copyfiles.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import arrow.core.Either
import com.carlosjimz87.copyfiles.core.Constants.RETRIES
import com.carlosjimz87.copyfiles.models.Download
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File


class DownloadContentFileWorker(
    context: Context,
    params: WorkerParameters
) :
    CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {

        val contentID = inputData.getInt("content_id", 0)
        val folder = inputData.getString("folder")
        val filename = inputData.getString("filename")
        val md5 = inputData.getString("md5")
        Timber.d("Executing Content DownloadWorker for $contentID file: $filename to folder: $folder md5 $md5")

        return withContext(Dispatchers.IO) {
            when {
                runAttemptCount < RETRIES -> {
                    downloadContent(contentID, folder!!, filename!!, md5!!).fold(
                        {
                            Timber.e("Error ${it.cause}")
                            Result.retry()
                        },
                        {
                            Timber.d("Content downloaded $contentID")
                            Result.success()
                        }
                    )
                }
                else -> {
                    Result.success()
                }
            }
        }
    }


    private suspend fun downloadContent(
        contentID: Int,
        folder: String,
        filename: String,
        md5: String
    ): Either<Throwable, Download> {
//        return downloadContentFile(
//            DownloadContentFile.Params(
//                contentID,
//                folder,
//                filename,
//                md5
//            )
//        )
//            .attempt()
//            .unsafeRunSync()
        return Either.right(Download("contentID", File(filename), md5))
    }

}
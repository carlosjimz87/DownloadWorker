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

class DownloadPlaylistWorker(
    context: Context,
    params: WorkerParameters
) :
    CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {

        val contentID = inputData.getInt("action_id", 0)
        val folder = inputData.getString("folder")

        Timber.d("Executing Playlist DownloadWorker for $contentID to $folder")
        return withContext(Dispatchers.IO) {
            when {
                runAttemptCount < RETRIES -> {
                    downloadPlaylist(contentID, folder!!).fold(
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
                    Result.failure()
                }
            }
        }
    }

    private suspend fun downloadPlaylist(
        contentID: Int,
        folder: String
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
        return Either.right(Download("contentID", File(folder)))
    }
}
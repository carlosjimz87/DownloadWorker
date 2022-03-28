package com.carlosjimz87.copyfiles

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.carlosjimz87.copyfiles.Constants.RETRIES
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

class DownloadWorker(
    context: Context,
    params: WorkerParameters,
) :
    CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        Timber.d("Thread Worker ${Thread.currentThread().name}")
        val remotePath = inputData.getString("remotePath")
        val destinationPath = inputData.getString("destinationPath")
        val filename = inputData.getString("fileName")
        return withContext(Dispatchers.IO) {
            Timber.d("Thread WithContext: ${Thread.currentThread().name}")
            when (runAttemptCount < RETRIES) {
                true -> {
                    if (remotePath != null && destinationPath != null && filename != null) {
                        val copier = Copier(applicationContext)
                        Timber.d("Executing Content DownloadWorker file: $filename destination: $destinationPath remote: $remotePath")
                        copier.downloadFile(
                            remotePath = remotePath,
                            destinationPath = destinationPath,
                            fileName = filename
                        )
                    } else {
                        Result.failure()
                    }
                }

                else -> {
                    Result.success()
                }
            }
            Result.success()
        }
    }
}
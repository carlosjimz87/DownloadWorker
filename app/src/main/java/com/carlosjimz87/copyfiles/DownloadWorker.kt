package com.carlosjimz87.copyfiles

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.carlosjimz87.copyfiles.Constants.RETRIES
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File

class DownloadWorker(
    context: Context,
    params: WorkerParameters,
) :
    CoroutineWorker(context, params) {

    private val copier = Copier(applicationContext)

    override suspend fun doWork(): Result {
        Timber.d("Thread Worker ${Thread.currentThread().name}")
        val remotePath = inputData.getString("remotePath")
        val destinationPath = inputData.getString("destinationPath")
        val filename = inputData.getString("fileName")

        return withContext(Dispatchers.IO) {
            when {
                runAttemptCount < RETRIES -> {
                    if (remotePath != null && destinationPath != null && filename != null) {
                        copier.downloadFileFold(
                            remotePath = remotePath,
                            destinationPath = destinationPath,
                            fileName = filename
                        ).fold(
                            {
                                Timber.e("Error ${it.cause}. Retrying")
                                Result.retry()
                            }, {
                                if(File(destinationPath+File.separator + filename).exists()){
                                    Timber.d("File $filename was downloaded successfully")
                                    Result.success()
                                }
                                else{
                                    Result.retry()
                                }
                            }
                        )

                    } else {
                        Result.failure()
                    }
                }
                else -> {
                    Result.success()
                }
            }
        }
    }
}
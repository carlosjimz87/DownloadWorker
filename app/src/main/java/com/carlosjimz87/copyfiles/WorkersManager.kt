package com.carlosjimz87.copyfiles

import androidx.work.BackoffPolicy
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import timber.log.Timber
import java.util.concurrent.TimeUnit

object WorkersManager {
    fun downloadContentWorker(
        remotePath: String,
        destinationPath: String,
        fileName: String
    ): OneTimeWorkRequest {

        val worker = OneTimeWorkRequest.Builder(DownloadWorker::class.java)
        val data = Data.Builder()

        data.putString("remotePath", remotePath)
        data.putString("destinationPath", destinationPath)
        data.putString("fileName", fileName)
        worker.setInputData(data.build())

        worker.setBackoffCriteria(
            BackoffPolicy.LINEAR,
            10 * 1000,
            TimeUnit.MILLISECONDS
        )

        Timber.d("Downloading fileName:$fileName destination: $destinationPath remote:$remotePath")
        return worker.addTag("download_content").build()
    }
}
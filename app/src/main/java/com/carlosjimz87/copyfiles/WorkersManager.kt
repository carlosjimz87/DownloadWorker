package com.carlosjimz87.copyfiles

import androidx.work.*
import timber.log.Timber
import java.util.*
import java.util.concurrent.TimeUnit

object WorkersManager {
    private lateinit var manager: WorkManager
    private lateinit var activity: MainActivity

    fun init(activity: MainActivity) {
        this.activity = activity
        manager = WorkManager.getInstance(activity)
    }

    fun downloadContentWorker(
        remotePath: String,
        destinationPath: String,
        fileName: String
    ): OneTimeWorkRequest {


        val data = Data.Builder().apply {
            putString("remotePath", remotePath)
            putString("destinationPath", destinationPath)
            putString("fileName", fileName)
        }.build()

        val oneTimeWorkerRequest =
            buildOneTimeWorkRemoteWorkRequest(DownloadWorker::class.java, data)

        Timber.d("Downloading fileName:$fileName destination: $destinationPath remote:$remotePath")
        manager.enqueue(oneTimeWorkerRequest)
        return oneTimeWorkerRequest
    }

    fun observeWorkerBy(workerId: UUID) {
        manager.getWorkInfoByIdLiveData(workerId).observe(activity) { workInfo ->
            if (workInfo?.state == WorkInfo.State.FAILED) {
                Timber.e("Download worker: ${workInfo.id} was failed")
            } else if (workInfo?.state == WorkInfo.State.SUCCEEDED) {
                Timber.w("Download worker: ${workInfo.id} was successful")
            }
        }
    }

    private fun buildOneTimeWorkRemoteWorkRequest(
        listenableWorkerClass: Class<out ListenableWorker>,
        inputData: Data,
    ): OneTimeWorkRequest {

        return OneTimeWorkRequest.Builder(listenableWorkerClass)
            .setInputData(inputData)
            .setBackoffCriteria(
                BackoffPolicy.LINEAR,
                10 * 1000,
                TimeUnit.MILLISECONDS
            )
            .build()
    }
}
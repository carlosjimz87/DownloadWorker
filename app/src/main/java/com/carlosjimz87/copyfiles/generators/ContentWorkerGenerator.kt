package com.carlosjimz87.copyfiles.generators


import android.content.Context
import androidx.lifecycle.LifecycleOwner
import androidx.work.*
import com.carlosjimz87.copyfiles.models.Download
import com.carlosjimz87.copyfiles.workers.DownloadContentWorker
import com.carlosjimz87.copyfiles.workers.DownloadPlaylistWorker
import com.carlosjimz87.copyfiles.workers.DummyInitWorker
import timber.log.Timber
import java.util.*
import java.util.concurrent.TimeUnit


private const val TIMEOUT = 10 * 1000L

class ContentWorkerGenerator(context: Context) {

    private var workManager: WorkManager = WorkManager.getInstance(context)

    private fun generateWorker(
        workerClass: Class<out ListenableWorker>,
        inputData: Data? = null,
        backoffPolicy: BackoffPolicy = BackoffPolicy.LINEAR,
        backoffDelay: Long = 10 * 1000,
        timeUnit: TimeUnit = TimeUnit.MILLISECONDS
    ): OneTimeWorkRequest {

        val worker = OneTimeWorkRequest.Builder(workerClass)

        inputData?.let {
            worker.setInputData(it)
        }

        worker.setBackoffCriteria(
            backoffPolicy,
            backoffDelay,
            timeUnit
        )

        return worker.build()
    }

    fun beginDownloadWorkers(downloads: ArrayList<Download>): Pair<UUID, List<UUID>> {
        Timber.d("Gen contentsWorkers for ${downloads.size} downloads")

        val checkTokenWorker = generateWorker(DummyInitWorker::class.java)
        val downloadsWorkList = generateDownloadWorkerList(downloads)
        val idsSum = downloads.map { it.identifier.toInt() }.reduce { acc, id -> acc + id }

        var continuation = workManager!!.beginUniqueWork(
            idsSum.toString(),
            ExistingWorkPolicy.KEEP,
            checkTokenWorker
        )

        for (workSet in downloadsWorkList) {
            continuation = continuation
                .then(downloadsWorkList)
        }
        continuation.enqueue()

        return Pair(checkTokenWorker.id, downloadsWorkList.map { it.id })
    }

    fun beginPlaylistWorker(actionId: String, path: String): Pair<UUID, UUID> {
        Timber.d("Gen playlistWorker for $actionId")

        val checkTokenWorker = generateWorker(DummyInitWorker::class.java)
        val downloadBackgroundWorker = downloadPlaylistWorker(
            Integer.parseInt(actionId),
            path
        )

        workManager!!.beginUniqueWork(actionId, ExistingWorkPolicy.KEEP, checkTokenWorker)
            .then(downloadBackgroundWorker)
            .enqueue()
        Timber.d("Return workers ${checkTokenWorker.id} and ${downloadBackgroundWorker.id}")
        return Pair(checkTokenWorker.id, downloadBackgroundWorker.id)
    }

    private fun generateDownloadWorkerList(downloads: List<Download>): List<OneTimeWorkRequest> {

        return downloads.filter { it.isValid() }.map {
            Timber.d("Content to download: ${it.identifier} ${it.localFile}")
            downloadContentWorker(
                it.identifier.toInt(),
                it.localPath.path,
                it.localFile,
                it.md5
            )

        }
    }

    private fun downloadContentWorker(
        contentID: Int,
        folder: String,
        filename: String,
        md5: String
    ): OneTimeWorkRequest {

        val worker = OneTimeWorkRequest.Builder(DownloadContentWorker::class.java)
        val data = Data.Builder()

        data.putInt("content_id", contentID)
        data.putString("folder", folder)
        data.putString("filename", filename)
        data.putString("md5", md5)
        worker.setInputData(data.build())
        //worker.setConstraints(constraints)
        worker.setBackoffCriteria(
            BackoffPolicy.LINEAR,
            TIMEOUT,
            TimeUnit.MILLISECONDS
        )

        return worker.addTag("download_content_$contentID").build()
    }

    private fun downloadPlaylistWorker(
        actionID: Int,
        folder: String
    ): OneTimeWorkRequest {

        val constraints = Constraints.Builder()
            .setRequiresStorageNotLow(true)
            .build()


        val worker = OneTimeWorkRequest.Builder(DownloadPlaylistWorker::class.java)
        val data = Data.Builder()

        data.putInt("action_id", actionID)
        data.putString("folder", folder)

        worker.setInputData(data.build())
        //worker.setConstraints(constraints)
        worker.setBackoffCriteria(
            BackoffPolicy.LINEAR,
            TIMEOUT,
            TimeUnit.MILLISECONDS
        )


        Timber.d("Downloading playlist content id:$actionID f:$folder")
        return worker.addTag("download_playlist").build()
    }


    fun handleWorkerResult(
        owner: LifecycleOwner,
        workerId: UUID,
        succeed: (() -> Unit)? = null,
        failed: (() -> Unit)? = null
    ) {

        workManager.getWorkInfoByIdLiveData(workerId).observe(
            owner
        ) { workInfo ->
            Timber.d("Handling Worker $workerId status ${workInfo?.state}")
            when (workInfo?.state) {
                WorkInfo.State.SUCCEEDED -> {
                    if (succeed != null) {
                        succeed()
                    }
                }
                WorkInfo.State.FAILED -> {
                    if (failed != null) {
                        failed()
                    }
                }
                else -> {}
            }
        }
    }
}

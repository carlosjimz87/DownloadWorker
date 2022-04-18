package com.carlosjimz87.copyfiles.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext


class CheckTokenWorker(
    context: Context,
    params: WorkerParameters
) :

    CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {

        return withContext(Dispatchers.IO) {
            delay(200L)
            Result.success()
        }
    }
}
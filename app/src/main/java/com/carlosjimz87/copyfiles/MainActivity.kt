package com.carlosjimz87.copyfiles

import android.os.Bundle
import android.os.Environment
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import java.lang.Thread.sleep


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val photos = listOf(
//            "https://www.pexels.com/photo/10852344/download/?search_query=&tracking_id=9715odxld8v",
            "https://www.pexels.com/wrongurl", // wrong url
            "https://www.pexels.com/photo/11341064/download/?search_query=&tracking_id=9715odxld8v",
            "https://www.pexels.com/photo/11567527/download/?search_query=&tracking_id=9715odxld8v",
        )
        val names = listOf(
            "castle.jpg",
            "plane.jpg",
            "building.jpg",
        )

//        val destination = this.filesDir.path
//
//        // initialize WorkManager
//        WorkersManager.init(this@MainActivity)
//
//
//        photos.forEachIndexed { index, photoUri ->
//            // execute downloadContentWorker
//            val work = WorkersManager.downloadContentWorker(
//                remotePath = photoUri,
//                destinationPath = destination,
//                fileName = names[index]
//            )
//
//            // observe worker
//            WorkersManager.observeWorkerBy(work.id)
//        }

//        val file = ContextCompat.getExternalFilesDirs(this,"Download")

//        runAsync()

        waitFx()
    }

    private fun waitFx(){
        Timber.d("Init")
        Thread(){
            sleep(2000)
            Timber.d("Continue")
        }.run()
        Timber.d("End")

    }
    private fun runAsync() {

        CoroutineScope(Dispatchers.IO).launch {
            val path =
                "/mnt/mmcblk1/mmcblk1p1/Android/data/com.onthespot.androidplayer/files/OTS/SW/update/"
            val filename = "copyfiles.apk"

            val originFile = File(path, filename)

            val file = asyncCopyToDownload(originFile)
            val result = (file?.exists() ?: false)
            Timber.d("Result: $result of ${file?.path}")
        }
    }


    private suspend fun asyncCopyToDownload(origin: File): File? {
        val destination = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                .toString(), origin.name
        )

        Timber.d("Copying file ${origin.path} to ${destination.path}")

        return CoroutineScope(Dispatchers.IO).async {
            return@async if (origin.exists() && origin.isFile && origin.canRead()) {
                return@async origin.copyTo(destination, overwrite = true)
            } else null
        }.await()
    }

}
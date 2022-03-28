package com.carlosjimz87.copyfiles

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity


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

        val destination = this.filesDir.path

        // initialize WorkManager
        WorkersManager.init(this@MainActivity)


        photos.forEachIndexed { index, photoUri ->
            // execute downloadContentWorker
            val work = WorkersManager.downloadContentWorker(
                remotePath = photoUri,
                destinationPath = destination,
                fileName = names[index]
            )

            // observe worker
            WorkersManager.observeWorkerBy(work.id)
        }
    }
}
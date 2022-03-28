package com.carlosjimz87.copyfiles

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.work.OneTimeWorkRequest
import timber.log.Timber

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val photos = listOf(
            "https://www.pexels.com/photo/10852344/download/?search_query=&tracking_id=9715odxld8v",
            "https://www.pexels.com/photo/11341064/download/?search_query=&tracking_id=9715odxld8v",
            "https://www.pexels.com/photo/11567527/download/?search_query=&tracking_id=9715odxld8v"
        )
        val names = listOf(
            "castle.jpg",
            "plane.jpg",
            "building.jpg",
        )

        val destination = this.filesDir.path

        val workList = ArrayList<OneTimeWorkRequest>()
        photos.forEachIndexed { index, photoUri ->
            val work = WorkersManager.downloadContentWorker(
                remotePath = photoUri,
                destinationPath = destination,
                fileName = names[index]
            )
            workList.add(work)
        }

//        val copier = Copier(this)
//        photos.forEachIndexed { index, photoUri ->
//            copier.downloadFile(
//                remotePath = photoUri,
//                destinationPath = destination,
//                fileName = names[index]
//            )
//        }
    }
}
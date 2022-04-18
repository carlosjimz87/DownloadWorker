package com.carlosjimz87.copyfiles

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.carlosjimz87.copyfiles.managers.FileManager
import com.carlosjimz87.copyfiles.core.SampleData
import timber.log.Timber


class MainActivity : AppCompatActivity() {

//    private val downloadCopyManager = DownloadCopyManager(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val uri = SampleData.photos.first()
        val name = SampleData.names.first()
        val manager = FileManager.Builder.init(baseContext)
        val dataDestination = manager.getDataLocation()
        val extDestination = manager.getExtLocation()

        Timber.w("DATA_DESTINATION: $dataDestination")
        Timber.w("EXTERNAL_DESTINATION: $extDestination")

//        downloadCopyManager.downloadFileFold(uri,destination, name).fold(
//            {
//                Timber.e("Error")
//            },
//            {
//                Timber.d("Downloaded")
//            }
//        )
    }
}
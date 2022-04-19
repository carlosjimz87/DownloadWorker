package com.carlosjimz87.copyfiles

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.carlosjimz87.copyfiles.managers.FileManager
import com.carlosjimz87.copyfiles.core.SampleData
import com.carlosjimz87.copyfiles.managers.DownloadsManager
import timber.log.Timber


class MainActivity : AppCompatActivity() {
    lateinit var downloadCopyManager: DownloadsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        downloadCopyManager = DownloadsManager(baseContext)


        val manager = FileManager.Builder.init(baseContext)
        val dataDestination = manager.getDataLocation()
        val extDestination = manager.getExtLocation()

        Timber.w("DATA_DESTINATION: $dataDestination")
        Timber.w("EXTERNAL_DESTINATION: $extDestination")

        dataDestination?.let {
            val uri = SampleData.photos[0]
            val name = SampleData.names[0]

            for(i in 0..3){

                downloadCopyManager.downloadFileFold(uri, it, name).fold(
                    {
                        Timber.e("Error")
                    },
                    {
                        Timber.d("Downloaded")
                    }
                )
            }
        }

        extDestination?.let { destination ->

            SampleData.photos.forEachIndexed { index, uri ->
                val name = SampleData.names[index]
                Timber.d("Proceed to download $name from $uri in $destination")
                downloadCopyManager.downloadFileFold(uri, destination, name).fold(
                    {
                        Timber.e("Error")
                    },
                    {
                        Timber.d("Downloaded")
                    }
                )
            }

        }

    }
}
package com.carlosjimz87.copyfiles

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.carlosjimz87.copyfiles.core.SampleData
import com.carlosjimz87.copyfiles.managers.DownloadsManager
import com.carlosjimz87.copyfiles.managers.FileManager
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var downloadCopyManager: DownloadsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val manager = FileManager.Builder.init(baseContext)
        val dataDestination = manager.getDataLocation()
        val extDestination = manager.getExtLocation()

        Timber.w("DATA_DESTINATION: $dataDestination")
        Timber.w("EXTERNAL_DESTINATION: $extDestination")

        dataDestination?.let { destination ->

            val uri = SampleData.photos.forEachIndexed { i, photo ->

                val name = SampleData.names[i]

                for (i in 0..2) {   // repeat same download

                    downloadCopyManager.downloadFileFold(photo, destination, name).fold(
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
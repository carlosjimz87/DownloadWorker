package com.carlosjimz87.copyfiles.managers

import android.content.Context
import com.carlosjimz87.copyfiles.core.Constants
import timber.log.Timber
import java.io.File

class FileManager private constructor(
    private val context: Context,
    private val dataAppPath: String = context.filesDir.path,
    private val externalAppPath: String = context.getExternalFilesDir(null)?.path ?: "",
    private val sourceAppPath: String =
        "/mnt/mmcblk1/mmcblk1p1/Android/data/${Constants.SPOTDYNA_PACK_NAME}/files"
) {

    private var TEST_FILE_NAME: String = "TestFile.txt"

    object Builder {
        fun init(baseContext: Context): FileManager {
            return FileManager(baseContext)
        }
    }

    fun getDataLocation(): String? {

        return when (createAndDeleteFile(dataAppPath, TEST_FILE_NAME)) {
            true -> dataAppPath
            false -> null
        }
    }


    fun getExtLocation(): String? {

        return when {

            // external sdcard (root?)
            createAndDeleteFile(sourceAppPath, TEST_FILE_NAME) -> sourceAppPath

            // internal sdcard
            createAndDeleteFile(externalAppPath, TEST_FILE_NAME) -> externalAppPath

            else -> null
        }
    }

    @Throws(Exception::class)
    fun createAndDeleteFile(path: String, filename: String): Boolean {
        Timber.d("Checking $path")
        return try {
            val file = File(path, filename)

            if (file.exists()) {
                file.delete()
            }

            file.createNewFile().also {
                if (it) {
                    file.delete()
                }
            }
        } catch (e: Exception) {
            Timber.e("Error getting location $path (${e.message})")
            false
        }
    }


}
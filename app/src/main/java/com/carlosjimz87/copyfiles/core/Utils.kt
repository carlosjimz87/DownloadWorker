package com.carlosjimz87.copyfiles.core

import timber.log.Timber
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.RandomAccessFile

object Utils {

    fun copyBytes(source: InputStream, destination: File, md5: String?): Boolean {

        val randomAccessFile = RandomAccessFile(destination, "rw")
        randomAccessFile.use { raf ->
            source.use { inputStream ->
                return try {
                    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                    var len: Int
                    while (inputStream.read(buffer).also { len = it } != -1) {
                        raf.write(buffer, 0, len)
                    }
                    destination.reallyExists(md5)
                } catch (e: IOException) {
                    Timber.e("Error copying bytes: ${e.message}")
                    false
                }
            }
        }
    }

}
package com.carlosjimz87.copyfiles.managers

import com.carlosjimz87.copyfiles.core.reallyExists
import timber.log.Timber
import java.io.*

object FileManagerKt {

    fun copyBytesInChunks(source: InputStream, destination: File) {
        source.use { inputStream ->
            try {
                val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                var bytes = inputStream.read(buffer)
                while (bytes >= 0) {
                    destination.outputStream().use { outputStream ->
                        outputStream.write(buffer, 0, bytes)
                        bytes = inputStream.read(buffer)
                        outputStream.flush()
                    }
                }
            } catch (e: Exception) {
                Timber.e("Error copying bytes: ${e.message}")
            }
        }
    }

    fun copyBytesInChunks(source: InputStream, destination: FileOutputStream) {
        source.use { inputStream ->
            try {
                val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                var bytes = inputStream.read(buffer)
                while (bytes >= 0) {
                    destination.use { outputStream ->
                        outputStream.write(buffer, 0, bytes)
                        bytes = inputStream.read(buffer)
                        outputStream.flush()
                    }
                }
            } catch (e: Exception) {
                Timber.e("Error copying bytes: ${e.message}")
            }
        }

    }

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
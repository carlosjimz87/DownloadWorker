package com.carlosjimz87.copyfiles.managers

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

//        try {
//            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
//            var bytes = fileInputStream.read(buffer)
//            while (bytes >= 0) {
//                destination.write(buffer, 0, bytes)
//                bytes = fileInputStream.read(buffer)
//            }
//        } catch (exception: Exception) {
//            exception.printStackTrace()
//        } finally {
//            destination.flush()
//            destination.close()
//            fileInputStream.close()
//        }
    }

    fun copyBytes(source: InputStream, destination: File): Boolean {
        val randomAccessFile = RandomAccessFile(destination, "rw")

        source.use { inputStream ->
            try {

                val buffer = ByteArray(1024)
                var len: Int
                while (inputStream.read(buffer).also { len = it } != -1) {
                    randomAccessFile.write(buffer, 0, len)
                }
                randomAccessFile.close()
            } catch (e: IOException) {
                Timber.e("Error copying bytes: ${e.message}")
                return false
            }
            return true
        }
    }

}
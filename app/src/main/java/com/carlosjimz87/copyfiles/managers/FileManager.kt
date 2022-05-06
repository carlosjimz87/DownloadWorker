package com.carlosjimz87.copyfiles.managers

import android.content.Context
import android.os.Environment
import com.carlosjimz87.copyfiles.core.Constants
import okhttp3.ResponseBody
import timber.log.Timber
import java.io.File
import java.io.InputStream
import java.io.RandomAccessFile
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

class FileManager private constructor(
    private val context: Context,
    private val dataAppPath: String = context.filesDir.path,
    private val externalAppPath: String = context.getExternalFilesDir(null)?.path ?: "",

    private val internalPath: String =
        "/mnt/mmcblk1/mmcblk1p1/Android/data/${Constants.SPOTDYNA_PACK_NAME}/files"
) {
    private val downloadsPath: String =
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).path
    private var TEST_FILE_NAME: String = "TestFile.txt"

    object Builder {
        fun init(baseContext: Context): FileManager {
            return FileManager(baseContext)
        }
    }

    fun getDownloadsLocation(): String? {

        return when (createAndDeleteFile(downloadsPath, TEST_FILE_NAME)) {
            true -> downloadsPath
            false -> null
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
            createAndDeleteFile(internalPath, TEST_FILE_NAME) -> internalPath

            // internal sdcard
            createAndDeleteFile(externalAppPath, TEST_FILE_NAME) -> externalAppPath

            else -> null
        }
    }

    companion object {

        fun filesInFolder(folder: File): Array<out File>? {
            return folder.listFiles()
        }

        fun copyBytes(body: ResponseBody?, destination: String, filename: String): Boolean {
            if (body == null) return false

            val size = body.contentLength()
            Timber.d("Copying length -> $size")
            return if (size < 200000000) {
                copySmallBytes(body.byteStream(), destination, filename)
            } else {
                copyBigBytes(body.byteStream(), destination, filename)
            }
        }

        fun File.unzipFile(unzipLocationRoot: File? = null): Boolean {
            Timber.d("Unzipping file ${this.absolutePath} to ${unzipLocationRoot?.absolutePath}")
            if (this.exists() && this.isFile) {
                this.unzip(unzipLocationRoot)
                return true
            }
            return false
        }

        data class ZipIO(val entry: ZipEntry, val output: File)

        private fun File.unzip(unzipLocationRoot: File? = null) {
            val rootFolder = unzipLocationRoot
                ?: File(parentFile?.absolutePath + File.separator + "update")
            if (!rootFolder.exists()) {
                rootFolder.mkdirs()
            }

            ZipFile(this).use { zip ->
                zip.entries().asSequence().map {
                    val outputFile = File(rootFolder.absolutePath + File.separator + it.name)
                    ZipIO(it, outputFile)
                }.map {
                    it.output.parentFile?.run {
                        if (!exists()) mkdirs()
                    }
                    it
                }.filter {
                    !it.entry.isDirectory
                }.forEach { (entry, output) ->
                    zip.getInputStream(entry).use { input ->
                        output.outputStream().use { output ->
                            input.copyTo(output)
                            output.close()
                        }
                    }
                }
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

        private fun copySmallBytes(source: InputStream, path: String, filename: String): Boolean {
            Timber.d("Copy small bytes")
            val destinationFileStream = File(path, filename).outputStream()

            return try {
                destinationFileStream.use { outputStream ->
                    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)

                    source.use { inputStream ->
                        var bytes = inputStream.read(buffer)
                        while (bytes >= 0) {
                            outputStream.write(buffer, 0, bytes)
                            bytes = inputStream.read(buffer)
                        }
                        Timber.d("Bytes copied in $path")
                        true
                    }

                }

            } catch (e: Exception) {
                Timber.e("Error copying bytes: ${e.message}")
                false
            }
        }

        private fun copyBigBytes(source: InputStream, path: String, filename: String): Boolean {
            Timber.d("Copy big bytes")
            val destinationFile = File(path, filename)
            val randomAccessFile = RandomAccessFile(destinationFile, "rwd")
            randomAccessFile.use { raf ->
                source.use { inputStream ->
                    return try {
                        val buffer = ByteArray(DEFAULT_BUFFER_SIZE * 2)
                        var len: Int
                        while (inputStream.read(buffer).also { len = it } != -1) {
                            raf.write(buffer, 0, len)
                        }
                        true
                    } catch (e: java.lang.Exception) {
                        Timber.e("Error copying bytes: ${e.message}")
                        false
                    }
                }
            }
        }
    }

}
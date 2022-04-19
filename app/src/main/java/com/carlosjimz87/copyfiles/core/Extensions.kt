package com.carlosjimz87.copyfiles.core

import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.security.MessageDigest
import java.util.*

fun File.getMD5Hash(): String {
    val md = MessageDigest.getInstance("MD5")
    val stream: InputStream
    stream = FileInputStream(this)

    val buffer = ByteArray(8192)
    var read: Int
    while (stream.read(buffer).also { read = it } > 0) {
        md.update(buffer, 0, read)
    }
    stream.close()

    return md.digest().toHex().lowercase(Locale.ROOT)
}

private fun ByteArray.toHex(): String {
    return joinToString("") { "%02x".format(it) }
}

fun File.copyToF(
    target: File,
    overwrite: Boolean = false,
    bufferSize: Int = DEFAULT_BUFFER_SIZE
): File {
    if (!this.exists()) {
        throw NoSuchFileException(file = this, reason = "The source file doesn't exist.")
    }

    if (target.exists()) {
        if (!overwrite)
            throw FileAlreadyExistsException(
                file = this,
                other = target,
                reason = "The destination file already exists."
            )
        else if (!target.delete())
            throw FileAlreadyExistsException(
                file = this,
                other = target,
                reason = "Tried to overwrite the destination, but failed to delete it."
            )
    }

    if (this.isDirectory) {
        if (!target.mkdirs())
            throw FileSystemException(
                file = this,
                other = target,
                reason = "Failed to create target directory."
            )
    } else {
        target.parentFile?.mkdirs()

        this.inputStream().use { input ->
            target.outputStream().use { output ->
                // if bufferSize > 0 means something was copied, then we try to flush the buffer (and close it?)
                if (input.copyTo(output, bufferSize) > 0) {
                    output.flush()
                    output.close()
                    input.close()
                }
            }
        }

    }

    return target
}

package com.carlosjimz87.copyfiles.core

import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
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

fun File.generateChecksum(): String {
    val inputStream = inputStream()
    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
    var bytes = inputStream.read(buffer)
    val digest: MessageDigest = MessageDigest.getInstance("SHA-512")

    while (bytes >= 0) {
        digest.digest(buffer, 0, bytes)
        bytes = inputStream.read(buffer)
    }

    return buffer.printableHexString()
}

fun ByteArray.printableHexString(): String {
    val hexString: StringBuilder = StringBuilder()
    for (messageDigest: Byte in this) {
        var h: String = Integer.toHexString(0xFF and messageDigest.toInt())
        while (h.length < 2)
            h = "0$h"
        hexString.append(h)
    }
    return hexString.toString()
}

fun File.reallyExists(md5: String?): Boolean {
    return this.exists() && this.isFile && this.canRead() && md5?.let { this.verifyMd5(it) } ?: true
}

fun File.verifyMd5(md5: String): Boolean {
    if (this.generateChecksum() == md5) {
        return true
    }
    throw FileNotFoundException("MD5s do not match")
}

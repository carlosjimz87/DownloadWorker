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

    return md.digest().toHex().toLowerCase(Locale.ROOT)
}

private fun ByteArray.toHex(): String {
    return joinToString("") { "%02x".format(it) }
}

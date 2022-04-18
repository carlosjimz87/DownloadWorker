package com.carlosjimz87.copyfiles.models

import java.io.File

data class Download(
    val filename: String,
    val localPath: File,
    val identifier: String = "0",
    val localFile: String = "",
    val priority: Int = 0,
    val remoteUrl: String = "",
    val md5: String = ""
) {
    fun isValid(): Boolean {
        return identifier != "0"
    }
}
package com.carlosjimz87.copyfiles.models

data class DownloadRemote(
    val remotePath: String,
    val fileName: String,
    val destination: String = "",
    val shouldCopy: Boolean = true
)
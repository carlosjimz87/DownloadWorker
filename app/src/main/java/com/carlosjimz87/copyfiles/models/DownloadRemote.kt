package com.carlosjimz87.copyfiles.models

import com.carlosjimz87.copyfiles.core.Constants.IMAGE_EXT

data class DownloadRemote(
    val id: Long,
    val type: String,
    val destination: String = "",
    val shouldCopy: Boolean = true,
    val name: String = "$id$IMAGE_EXT"
)
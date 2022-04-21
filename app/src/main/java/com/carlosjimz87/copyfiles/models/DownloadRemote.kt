package com.carlosjimz87.copyfiles.models

import com.carlosjimz87.copyfiles.core.Constants.IMAGE_EXT
import com.carlosjimz87.copyfiles.core.Constants.VIDEO_EXT

data class DownloadRemote(
    val id: Long,
    val type: String,
    val destination: String = "",
    val url: String = "",
    val shouldCopy: Boolean = true,
    val name: String = if (type=="video") "$id$VIDEO_EXT" else "$id$IMAGE_EXT",
    var startTime: Long = 0L
)
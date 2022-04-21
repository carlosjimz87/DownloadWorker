package com.carlosjimz87.copyfiles.models

import com.carlosjimz87.copyfiles.core.Constants.IMAGE_EXT
import com.carlosjimz87.copyfiles.core.Constants.VIDEO_EXT

data class DownloadRemote(
    val id: Long,
    val type: String,
    val destination: String = "",
    val url: String = "",
    val shouldCopy: Boolean = true,
    val ext : String = url.ext(type),
    val name: String = "$id$ext",
    var startTime: Long = 0L
)

fun String.ext(type: String = "video"): String {
    val default = if (type == "video") VIDEO_EXT else IMAGE_EXT
    val tempExt = substringAfterLast(delimiter = '.', missingDelimiterValue = default)
    return if (tempExt.isNotEmpty() && !tempExt.contains('/')) ".$tempExt" else default
}
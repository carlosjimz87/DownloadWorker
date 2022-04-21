package com.carlosjimz87.copyfiles.core

import com.carlosjimz87.copyfiles.core.Constants.PHOTO_TYPE
import com.carlosjimz87.copyfiles.core.Constants.VIDEO_TYPE
import com.carlosjimz87.copyfiles.models.DownloadRemote

object SampleData {

    val photosDownload = listOf(
        DownloadRemote(
            id = 10852344L,
            type = PHOTO_TYPE,
            url = "/photo/10852344/download/",
        ),
        DownloadRemote(
            id = 11341064L,
            type = PHOTO_TYPE,
            url = "/photo/11341064/download/",
        ),
        DownloadRemote(
            id = 11567527L,
            type = PHOTO_TYPE,
            url = "/photo/11567527/download/",
        )
    )

    val videosDownload = listOf(
        DownloadRemote(
            id = 3818213L,
            type = VIDEO_TYPE,
            url = "/video/3818213/download/",
        ),
        DownloadRemote(
            id = 8116496L,
            type = VIDEO_TYPE,
            url = "/video/8116496/download/",
        ),
        DownloadRemote(
            id = 11257476L,
            type = VIDEO_TYPE,
            url = "/video/1321208/download/", // big
        )
    )
}
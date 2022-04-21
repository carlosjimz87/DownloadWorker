package com.carlosjimz87.copyfiles.core

import com.carlosjimz87.copyfiles.core.Constants.PHOTO_TYPE
import com.carlosjimz87.copyfiles.core.Constants.VIDEO_TYPE
import com.carlosjimz87.copyfiles.models.DownloadRemote

object SampleData {

    val photosDownload = listOf(
        DownloadRemote(
            id = 1L,
            type = PHOTO_TYPE,
            url = "https://spotdyna-app.s3.eu-west-1.amazonaws.com/BIGcontent/snowskate-winter-sports1920x1080.png",
        ),
        DownloadRemote(
            id = 2L,
            type = PHOTO_TYPE,
            url = "https://spotdyna-app.s3.eu-west-1.amazonaws.com/BIGcontent/4kparacaidistas-skydive.jpg",
        ),
        DownloadRemote(
            id = 3L,
            type = PHOTO_TYPE,
            url = "https://www.pexels.com/photo/11780920/download/?search_query=&tracking_id=ah6g7at4jei",
        )

    )

    val videosDownload = listOf(
        DownloadRemote(
            id = 4L,
            type = VIDEO_TYPE,
            url = "https://www.pexels.com/video/3818213/download/",
        ),
        DownloadRemote(
            id = 5L,
            type = VIDEO_TYPE,
            url = "https://www.pexels.com/video/8116496/download/",
        ),
        DownloadRemote(
            id = 6L,
            type = VIDEO_TYPE,
            url = "https://spotdyna-app.s3.eu-west-1.amazonaws.com/BIGcontent/365_0910_BlueBayou_HD.mp4", // big
        )
    )
}
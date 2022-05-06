package com.carlosjimz87.copyfiles.core

import com.carlosjimz87.copyfiles.core.Constants.PHOTO_TYPE
import com.carlosjimz87.copyfiles.core.Constants.VIDEO_TYPE
import com.carlosjimz87.copyfiles.core.Constants.ZIP_TYPE
import com.carlosjimz87.copyfiles.models.DownloadRemote

object SampleData {

    val zipDownload = listOf(
        DownloadRemote(
            id = 1L,
            type = ZIP_TYPE,
            url = "https://spotdyna-app.s3.eu-west-1.amazonaws.com/apk/10925_14283.zip",
        ),
    )

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
        )
    )

    val videosDownload = listOf(
        DownloadRemote(
            id = 4L,
            type = VIDEO_TYPE,
            url = "https://spotdyna-app.s3.eu-west-1.amazonaws.com/BIGcontent/365_0910_BlueBayou_HD.mp4",
        ),
        DownloadRemote(
            id = 5L,
            type = VIDEO_TYPE,
            url = "https://spotdyna-app.s3.eu-west-1.amazonaws.com/BIGcontent/1_samsung_cines3_1696x1664_03092021.mp4", // big
        ),
        DownloadRemote(
            id = 6L,
            type = VIDEO_TYPE,
            url = "https://spotdyna-app.s3.eu-west-1.amazonaws.com/BIGcontent/fundacion telefonica.mp4", // big
        )
    )
}
package com.carlosjimz87.copyfiles.core

import com.carlosjimz87.copyfiles.core.Constants.PHOTO_TYPE
import com.carlosjimz87.copyfiles.core.Constants.VIDEO_TYPE
import com.carlosjimz87.copyfiles.models.DownloadRemote

object SampleData {
    val photos = listOf(
        "/photo/10852344/download/?search_query=&tracking_id=9715odxld8v",
//            "/wrongurl", // wrong url
        "/photo/11341064/download/?search_query=&tracking_id=9715odxld8v",
        "/photo/11567527/download/?search_query=&tracking_id=9715odxld8v",
    )

    val photosDownload = listOf(
        DownloadRemote(
            id = 10852344L,
            type = PHOTO_TYPE,
        ),
        DownloadRemote(
            id = 11341064L,
            type = PHOTO_TYPE,
        ),
        DownloadRemote(
            id = 11567527L,
            type = PHOTO_TYPE,
        )
    )

    val names = listOf(
        "castle.jpg",
        "plane.jpg",
        "building.jpg",
    )

    val videos = listOf(
        "/es-es/video/3818213/download/?search_query=&tracking_id=ah6g7at4jei",
        "/es-es/video/8116496/download/?search_query=&tracking_id=ah6g7at4jei",
        "/es-es/video/11257476/download/?search_query=&tracking_id=ah6g7at4jei",
    )

    val videosDownload = listOf(
        DownloadRemote(
            id = 3818213L,
            type = VIDEO_TYPE,
        ),
        DownloadRemote(
            id = 8116496L,
            type = VIDEO_TYPE,
        ),
        DownloadRemote(
            id = 11257476L,
            type = VIDEO_TYPE,
        )
    )
}
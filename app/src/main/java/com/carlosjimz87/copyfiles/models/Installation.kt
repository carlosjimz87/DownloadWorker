package com.carlosjimz87.copyfiles.models

import kotlinx.serialization.Serializable


@Serializable
data class Installation(
    val filename: String,
    val md5: String,
    val order: String
)

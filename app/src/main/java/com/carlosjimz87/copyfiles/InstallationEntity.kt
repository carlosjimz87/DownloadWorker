package com.carlosjimz87.copyfiles

import kotlinx.serialization.Serializable


@Serializable
data class InstallationEntity(
    val filename: String,
    val md5: String,
    val order: String
)

package com.carlosjimz87.models

data class TestFile(
    val name: String = "TestFile",
    val extension: String = "txt",
    val fullName: String = "${name}.${extension}",
)
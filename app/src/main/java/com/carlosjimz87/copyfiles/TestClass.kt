package com.carlosjimz87.copyfiles

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.File
import java.io.FileReader


private val json = Json { ignoreUnknownKeys = true }

class TestClass {

    fun readManifest(path: String, filename: String): List<InstallationEntity> {

        return BufferedReader(FileReader(File(path, filename))).use { br ->
            parseManifest(br.readText())
        }
    }

    fun parseManifest(text: String): List<InstallationEntity> {
        return Json.decodeFromString(text)
    }
}
package com.carlosjimz87.copyfiles.parsers

import com.carlosjimz87.copyfiles.models.Installation
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.File
import java.io.FileReader


private val json = Json { ignoreUnknownKeys = true }

class ManifestParser {

    fun readManifest(path: String, filename: String): List<Installation> {

        return BufferedReader(FileReader(File(path, filename))).use { br ->
            parseManifest(br.readText())
        }
    }

    fun parseManifest(text: String): List<Installation> {
        return Json.decodeFromString(text)
    }
}
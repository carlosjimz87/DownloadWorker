package com.carlosjimz87.copyfiles.parsers

import com.carlosjimz87.copyfiles.SampleData
import com.carlosjimz87.copyfiles.models.Installation
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ManifestParserTest {

    private val sampleResponse = listOf(
        Installation(
            filename = "copyfiles.apk",
            md5 = "8d0596706a2b704ada3c8bea5523bf38",
            order = "1"
        )
    )

    @Test
    fun sample_manifest_parse_successfully() {
        val parser = ManifestParser()
        val resp = parser.parseManifest(SampleData.manifestJson)

        assertThat(resp).isEqualTo(
            sampleResponse
        )
    }
}
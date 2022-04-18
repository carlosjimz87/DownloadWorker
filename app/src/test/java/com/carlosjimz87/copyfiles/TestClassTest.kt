package com.carlosjimz87.copyfiles

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class TestClassTest {

    private val sampleResponse = listOf(
        InstallationEntity(
            filename = "copyfiles.apk",
            md5 = "8d0596706a2b704ada3c8bea5523bf38",
            order = "1"
        )
    )

    @Test
    fun sample_manifest_parse_successfully() {
        val parser = TestClass()
        val resp = parser.parseManifest(SampleManifest.json)

        assertThat(resp).isEqualTo(
            sampleResponse
        )
    }
}
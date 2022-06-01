package com.carlosjimz87.copyfiles

import com.carlosjimz87.models.TestFile
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.io.File
import java.net.URL


class FileManagerTest {
    private val ZIP_TEST_FILE: TestFile = TestFile(
        "update",
        "zip"
    )
    private val PHOTO_TEST_FILE: TestFile = TestFile(
        "image",
        "jpg"
    )

    private fun getFileFromPath(fileName: String): File? {
        val classLoader = ClassLoader.getSystemClassLoader()
        val resource: URL? = classLoader.getResource(fileName)
        return resource?.path?.let { File(it) }
    }

    @Test
    fun test_zip_file() {
        val file: File? = getFileFromPath(ZIP_TEST_FILE.fullName)
        assertThat(file).isNotNull()
        assertThat(file!!.exists()).isTrue()
        assertThat(file.name).isEqualTo(ZIP_TEST_FILE.fullName)
        assertThat(file.extension).isEqualTo(ZIP_TEST_FILE.extension)
    }

    @Test
    fun test_image_file() {
        val file: File? = getFileFromPath(PHOTO_TEST_FILE.fullName)
        assertThat(file).isNotNull()
        assertThat(file!!.exists()).isTrue()
        assertThat(file.name).isEqualTo(PHOTO_TEST_FILE.fullName)
        assertThat(file.extension).isEqualTo(PHOTO_TEST_FILE.extension)
    }

}
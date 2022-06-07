package com.carlosjimz87.copyfiles.managers

import org.apache.commons.io.IOUtils
import timber.log.Timber
import java.io.IOException


object RuntimeManager {

    sealed class RuntimeResult {
        data class Success(val value: Int, val output: String) : RuntimeResult()
        data class Failure(val error: String?) : RuntimeResult()
    }

    @JvmStatic
    fun root(): Boolean {

        return when (val result = this.execute("su")) {
            is RuntimeResult.Success -> {
                if (result.value == 0) {
                    true
                }
                return true
            }
            is RuntimeResult.Failure -> {
                Timber.e(result.error)
                false
            }
        }
    }

    @JvmStatic
    fun execute(commands: String): RuntimeResult {
        return this.execute(commands.split(" ").toTypedArray())
    }

    @JvmStatic
    fun execute(commands: Array<String>): RuntimeResult {

        Timber.d("EXEC [[ ${commands.joinToString(" ")} ]]")
        try {

            val prefixAdb = "sh -c"
            val p = Runtime.getRuntime().exec(commands)

            val (stdOut, stdErr) = p.extractStreams()

            if (stdErr.isNotEmpty()) {
                Timber.e("EXEC ERR: $stdErr")
                return RuntimeResult.Failure(stdErr)
            }
            Timber.w("EXEC OUT: $stdOut")
            return RuntimeResult.Success(p.waitFor(), stdOut)
        } catch (e: IOException) {
            Timber.e("EXEC ERR: ${e.message}")
            return RuntimeResult.Failure(e.message)
        }
    }

    @JvmStatic
    fun screencap(imagePath: String): Boolean {
//        val sh = Runtime.getRuntime().exec("su")
//        sh.outputStream.use {
//            it.write("/system/bin/screencap -p $imagePath".toByteArray(charset("ASCII")))
//        }
////        os.write(("/system/bin/screencap -p $imagePath").toByteArray(charset("ASCII")))
////        os.flush()
////        os.close()
//        sh.waitFor()

        return when (val result = execute("/system/bin/screencap -p $imagePath")) {
            is RuntimeResult.Success -> {
                if (result.value == 0) {
                    true
                }
                return true
            }
            is RuntimeResult.Failure -> {
                Timber.e(result.error)
                false
            }
        }
    }

    private fun Process.extractStreams(): Pair<String, String> {
        val stdOut = IOUtils.toString(this.inputStream, Charsets.UTF_8)
        val stdErr = IOUtils.toString(this.errorStream, Charsets.UTF_8)
        return Pair(stdOut, stdErr)
    }

}

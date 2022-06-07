package com.carlosjimz87.copyfiles.managers

import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageInstaller
import android.net.Uri
import android.os.Build
import android.os.Environment.getExternalStorageDirectory
import androidx.core.content.FileProvider
import com.carlosjimz87.copyfiles.BuildConfig
import com.carlosjimz87.copyfiles.models.InstallerType
import timber.log.Timber
import java.io.File
import java.io.FileInputStream


class InstallManager private constructor(
    private val activity: Activity,
) {

    private val destinationFolder: String = getExternalStorageDirectory().path + "/Download"
    private val copyFirst: Boolean = true
    private val root: Boolean = false
    private var update: Boolean = false

    companion object {
        const val APK_INSTALL_PATH = "application/vnd.android.package-archive"
        const val REQUEST_INSTALL = 1

    }

    object Builder {
        fun init(activity: Activity): InstallManager {
            return InstallManager(activity)
        }
    }

    fun handleActivityResult(
        requestCode: Int,
        resultCode: Int,
        result: (String) -> Unit
    ) {
        if (requestCode == REQUEST_INSTALL) {
            when (resultCode) {
                Activity.RESULT_OK -> result("Install succeeded!")
                Activity.RESULT_CANCELED -> result("Install canceled!")
                else -> result("Install failed!")
            }
        }
    }

    suspend fun install(
        apk: File,
        type: InstallerType = InstallerType.UNKNOWN_INSTALL,
        update: Boolean = false
    ) {
        this.update = update
        when (type) {
            InstallerType.NORMAL_INSTALL -> installNormalApk(apk)
            InstallerType.UNKNOWN_INSTALL -> installUnknownApk(apk)
            InstallerType.SILENT_INSTALL -> installSilentApk(apk)
            InstallerType.CMD_INSTALL -> installCmdApk(apk)
            else -> throw IllegalArgumentException("Unknown installer type")
        }
    }

    private suspend fun installCmdApk(apk: File) {
        if (apk.exists()) {
            try {
                RuntimeManager.screencap("$destinationFolder/screencap.png")
//                if (copyFirst) {
//                    RuntimeManager.execute(copyToSdCard(apk))
//                    val finalApk = getCopiedApk(apk)
//                    finalApk?.let {
//                        RuntimeManager.execute(installCmd(it))
//                    }
//                }
//                else{
//                    RuntimeManager.execute(installCmd(apk))
//                }
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun getCopiedApk(apk: File): File? {
        val destinationApk = File(destinationFolder, apk.name)
        return if (destinationApk.exists() && destinationApk.isFile && destinationApk.extension == "apk") {
            File(destinationFolder, apk.name)
        } else null
    }

    private fun copyToSdCard(apk: File): Array<String> {
        val command = listOf("cp", "-r", apk.absolutePath, destinationFolder)
        val prefix = mutableListOf("su", "-c")
        val finalCMD = if (root) {
            prefix.addAll(command)
            prefix
        } else {
            command
        }.toTypedArray()
        Timber.d("CMD: ${finalCMD.joinToString(" ")}")
        return finalCMD
    }

    private fun installCmd(apk: File): Array<String> {
        val command = listOf("pm", "install", "-r", apk.absolutePath)
        val prefix = mutableListOf("su", "-c")
        val finalCMD = if (root) {
            prefix.addAll(command)
            prefix
        } else {
            command
        }.toTypedArray()
        Timber.d("CMD: ${finalCMD.joinToString(" ")}")
        return finalCMD
    }

    private fun installUnknownApk(apk: File) {
        Timber.d("About to unknown install apk: ${apk.absolutePath}")
        val apkUri = getApkUri(apk)
        Timber.d("Apk uri: ${apkUri.toString()}")

        createIntent(apkUri, type = InstallerType.UNKNOWN_INSTALL).apply {
            activity.startActivity(this)
        }
    }

    private fun installNormalApk(apk: File) {
        Timber.d("About to install apk: ${apk.absolutePath}")
        val apkUri = getApkUri(apk)
        Timber.d("Apk uri: ${apkUri.toString()}")

        createIntent(apkUri, type = InstallerType.NORMAL_INSTALL).apply {
            activity.startActivity(this)
        }
    }

    private fun installSilentApk(apk: File) {
        val packageInstaller = activity.packageManager.packageInstaller
        val params =
            PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL)

        try {
            val sessionID = packageInstaller.createSession(params)
            val session = packageInstaller.openSession(sessionID)

            FileInputStream(apk).use { input ->
                session.openWrite(
                    apk.name,
                    0,
                    apk.length()
                ).use { out ->

                    val buffer = ByteArray(65536)
                    var c: Int
                    while (input.read(buffer).also { c = it } != -1) {
                        out.write(buffer, 0, c)
                    }
                    session.fsync(out)
                }

            }

            session.commit(createIntentSender(activity.baseContext, sessionID));
        } catch (e: Exception) {
            Timber.d("Error installing apk error: ${e.message}")
        }
    }


    private fun createIntent(
        apkUri: Uri?,
        type: InstallerType
    ): Intent {
        return when (type) {
            InstallerType.UNKNOWN_INSTALL -> {  // API >= N

                IntentsManager.createAndSendIntent(
                    activity.baseContext,
                    Intent.ACTION_INSTALL_PACKAGE,
                    Intent.FLAG_ACTIVITY_CLEAR_TOP,
                    send = false
                ).apply {
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true)
//                    putExtra(Intent.EXTRA_RETURN_RESULT, true)

                    if (update) {
                        putExtra(
                            Intent.EXTRA_INSTALLER_PACKAGE_NAME,
                            activity.applicationInfo.packageName
                        )
                    }
                    data = apkUri
                }
            }
            InstallerType.NORMAL_INSTALL -> { // API < N
                IntentsManager.createAndSendIntent(
                    activity.baseContext,
                    Intent.ACTION_INSTALL_PACKAGE,
                    Intent.FLAG_ACTIVITY_CLEAR_TOP,
                    send = false
                ).apply {
                    setDataAndType(apkUri, APK_INSTALL_PATH)
                }
            }
            InstallerType.VND_INSTALL -> {

                val multiFlags = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N

                IntentsManager.createAndSendIntent(
                    activity.baseContext,
                    Intent.ACTION_INSTALL_PACKAGE,
                    if (multiFlags)
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION
                    else
                        Intent.FLAG_GRANT_READ_URI_PERMISSION,
                    send = false
                ).apply {
                    setDataAndType(apkUri, APK_INSTALL_PATH)
                }
            }
            else -> {
                throw IllegalArgumentException("Unknown installer type")
            }
        }
    }


    private fun createIntentSender(context: Context, sessionId: Int): IntentSender {
        val intent = Intent(Intent.ACTION_INSTALL_PACKAGE).apply {
            data = Uri.parse("package:$sessionId")
            putExtra(Intent.EXTRA_INSTALLER_PACKAGE_NAME, context.packageName)
        }
        return PendingIntent.getActivity(context, 0, intent, 0).intentSender
    }

    private fun getApkUri(fileToInstall: File): Uri? {
        // Before N, a MODE_WORLD_READABLE file could be passed via the ACTION_INSTALL_PACKAGE
        // Intent. Since N, MODE_WORLD_READABLE files are forbidden, and a FileProvider is
        // recommended.
        val useFileProvider = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N

        // Copy the given asset out into a file so that it can be installed.
        // Returns the path to the file.
        val fileMode = if (useFileProvider) Context.MODE_PRIVATE else Context.MODE_WORLD_READABLE

        return if (useFileProvider) {
            FileProvider.getUriForFile(
//                activity.baseContext, "com.example.android.apis.installapkprovider", fileToInstall
//                activity.baseContext, activity.applicationContext.packageName, fileToInstall
                activity.baseContext, BuildConfig.APPLICATION_ID + ".provider", fileToInstall
            )
        } else {
            Uri.fromFile(fileToInstall)
        }
    }

    fun checkApkIsInstalled(packageName: String): Boolean {
        return activity.packageManager.getLaunchIntentForPackage(packageName) != null
    }
}
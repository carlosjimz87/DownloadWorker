package com.carlosjimz87.copyfiles.managers

import android.content.Context
import android.content.Intent

class InstallManager private constructor(
    val context: Context
) {

    object Builder {
        fun init(baseContext: Context): InstallManager {
            return InstallManager(baseContext)
        }
    }

    fun installUnkownSource(source: String) {
        IntentsManager.createAndSendIntent(
            context,
            Intent.ACTION_INSTALL_PACKAGE,
            Intent.FLAG_GRANT_READ_URI_PERMISSION,
            send = true
        )
    }
}
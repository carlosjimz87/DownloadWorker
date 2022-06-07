package com.carlosjimz87.copyfiles.core

object Constants {
    const val DOWNLOAD_CHANNEL_ID = "download_channel"
    const val DOWNLOAD_CHANNEL_NAME = "File download"
    const val BASE_URL = "https://www.pexels.com/"
    const val IMAGES_BASE_URL = "https://images.pexels.com/"
    const val IMAGE_EXT = ".jpeg"
    const val VIDEO_EXT = ".mp4"
    const val RETRIES = 3
    const val TIMEOUT_SEC = 10L
    const val WRITE_TIMEOUT_SEC = 30L
    const val VIDEO_TYPE = "video"
    const val PHOTO_TYPE = "photos"
    const val ZIP_TYPE = "zip"
    const val APK_TYPE = "apk"
    const val PACK_NAME = "com.carlosjimz87.copyfiles"
    const val SPOTDYNA_PACK_NAME = "com.onthespot.androidplayer"
}


object Actions {
    const val ACTION = "com.onthespot.player.ACTION"
    const val SYSTEM_TIME = "com.onthespot.system.action.time"
    const val SYSTEM_CONFIG = "com.onthespot.system.action.config"
    const val SYSTEM_SHUTDOWN = "com.onthespot.system.action.shutdown"
    const val SYSTEM_UPDATE = "com.onthespot.system.action.update"
    const val SYSTEM_INSTALL = "com.onthespot.system.INSTALL"
    const val SYSTEM_UNINSTALL = "com.onthespot.system.action.UNINSTALL"
    const val SYSTEM_KILL_PROCESS = "com.onthespot.system.action.killProcess"
    const val SYSTEM_FORCE_STOP = "com.onthespot.system.action.forcestop"
    const val SYSTEM_SERIAL_PORT_WRITER = "com.onthespot.system.action.serialPortWriter"
    const val SYSTEM_SSHOT = "com.onthespot.system.action.SSHOT"
    const val SYSTEM_UPL= "com.onthespot.system.action.UPL"
    const val SYSTEM_BOOT_COMPLETED = "android.intent.action.BOOT_COMPLETED"
    const val SYSTEM_SCREEN_ON = "android.intent.action.SCREEN_ON"
    const val TIME = "com.onthespot.action.time"
    const val RESTORE = "com.onthespot.androidplayer.action.RESTORE"
    const val INSTALL = "com.onthespot.system.action.install"
    const val CLOSE_PLAYER = "com.onthespot.androidplayer.STOP"
    const val FORCE_STOP = "com.onthespot.action.forcestop"
    const val SYSTEM_REBOOT = "com.onthespot.system.action.reboot"
    const val REBOOTED = "com.onthespot.action.rebooted"
    const val UPDATE = "com.onthespot.action.update"
    const val INSTALLED = "com.onthespot.action.INSTALLED"
    const val PACKAGE_REPLACED =  "android.intent.action.PACKAGE_REPLACED"
    const val IP =  "com.onthespot.action.ip"
    const val UPL =  "com.onthespot.action.UPL"
    const val SCREENSHOT_TAKEN = "com.onthespot.player.SSHOT"
    const val SERIAL_PORT_WRITER = "com.onthespot.action.serialPortWriter"
    const val SHUTDOWN = "com.onthespot.action.shutdown"
    const val REQUEST_SHUTDOWN = "android.intent.action.ACTION_REQUEST_SHUTDOWN"
}

object Phillips{
    const val FLAG_ACTIVITY_PREVIOUS_IS_TOP = 0x01000000
    const val TPV_REBOOT_DEVICE = "php.intent.action.REBOOT"
    const val TPV_TAKE_SCREENSHOT = "php.intent.action.TAKE_SCREENSHOT"
    const val TPV_UPDATE = "php.intent.action.UPDATE_APK"
}
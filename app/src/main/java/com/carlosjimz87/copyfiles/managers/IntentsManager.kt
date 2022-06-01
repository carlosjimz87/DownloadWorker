package com.carlosjimz87.copyfiles.managers

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import com.carlosjimz87.copyfiles.core.Phillips
import org.jetbrains.annotations.NotNull
import timber.log.Timber
import java.lang.ref.WeakReference

object IntentsManager {
    private lateinit var context: WeakReference<Context>

    fun getStringExtras(intent: Intent, vararg keys: String): Map<String, String> {
        return keys.map { key ->
            if (intent.hasExtra(key)) {
                val value = intent.getStringExtra(key)
                if (value != null) {
                    return mapOf(key to value)
                }
            }
            return mapOf()
        }.toMap()
    }

    @JvmStatic
    fun <T : BroadcastReceiver> createPendingIntent(
        @NotNull context: Context,
        @NotNull receiver: Class<T>,
        @NotNull code: Int,
        @NotNull flag: Int
    ): PendingIntent? {
        val intent = Intent(context, receiver)
        return PendingIntent.getBroadcast(
            context, code, intent, flag
        )
    }

    @SuppressLint("WrongConstant")
    @JvmStatic
    fun sendPhillipsIntent(
        @NotNull context: Context,
        @NotNull action: String,
        extras: Array<out Pair<String, Any>>? = emptyArray(),
        send: Boolean = false
    ): Intent {
        this.context = WeakReference(context)

        val intent = createIntent(
            action,
            null,
            null,
            extras
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            intent.addFlags(Phillips.FLAG_ACTIVITY_PREVIOUS_IS_TOP)
        }

        if (send) this.context.get()?.sendBroadcast(intent)
        return intent
    }

    @JvmStatic
    fun createAndSendIntent(
        @NotNull context: Context,
        @NotNull action: String,
        flag: Int? = Intent.FLAG_ACTIVITY_NEW_TASK
    ): Intent {
        this.context = WeakReference(context)
        return this.createAndSendIntent(
            context,
            action,
            flag,
            null,
            null,
            null
        )
    }

    @JvmStatic
    fun createAndSendIntent(
        @NotNull context: Context,
        @NotNull action: String,
        flag: Int? = Intent.FLAG_ACTIVITY_NEW_TASK,
        category: String? = Intent.CATEGORY_DEFAULT,
        extras: Array<out Pair<String, Any?>>? = emptyArray(),
        send: Boolean? = true
    ): Intent {
        this.context = WeakReference(context)

        val intent = createIntent(action, flag, category, extras)

        if (send == true) this.context.get()?.sendBroadcast(intent)
        return intent
    }

    @JvmStatic
    private fun createIntent(
        action: String,
        flag: Int?,
        category: String?,
        extras: Array<out Pair<String, Any?>>? = emptyArray()
    ): Intent {
        val intent = Intent(action)

        if (flag != null) intent.flags = flag
        if (category != null) intent.addCategory(category)

        extras?.forEach { (key, value) ->

            when (value) {
                is String -> intent.putExtra(key, value)
                is Boolean -> intent.putExtra(key, value)
                is Int -> intent.putExtra(key, value)
                else -> throw IllegalArgumentException("Extras must be String, Boolean or Int")
            }
        }
        return intent
    }

    @JvmStatic
    fun registerReceiver(
        context: Context,
        receiver: BroadcastReceiver
    ) {
        setContext(context)
        this.register(
            receiver = receiver
        )
    }

    @JvmStatic
    fun registerReceiver(
        context: Context,
        receiver: BroadcastReceiver,
        intentFilter: IntentFilter? = null
    ) {
        setContext(context)
        this.register(
            receiver = receiver,
            intentFilter = intentFilter
        )
    }

    @JvmStatic
    fun registerReceiver(
        context: Context,
        receiver: BroadcastReceiver,
        intentFilter: IntentFilter? = null,
        permission: String? = null
    ) {
        setContext(context)
        this.register(
            receiver = receiver,
            intentFilter = intentFilter,
            permission = permission
        )
    }

    private fun setContext(context: Context) {
        try {
            this.context = WeakReference(context)
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    private fun register(
        receiver: BroadcastReceiver,
        intentFilter: IntentFilter? = null,
        permission: String? = null
    ) {
        try {
            this.context.get()?.registerReceiver(
                receiver,
                intentFilter,
                permission,
                null
            )
        } catch (e: IllegalArgumentException) {
            Timber.e("Error registering receiver: ${e.message}")
        }
    }

}
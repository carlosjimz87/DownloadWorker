package com.carlosjimz87.copyfiles

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class FakeReceiver : BroadcastReceiver() {

    companion object {
        val REQUEST_CODE = 12345
    }

    override fun onReceive(p0: Context?, p1: Intent?) {
        TODO("Not yet implemented")
    }
}
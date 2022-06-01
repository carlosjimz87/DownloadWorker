package com.carlosjimz87.copyfiles

import android.content.Intent
import androidx.test.espresso.intent.Intents
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.ext.truth.content.IntentSubject.assertThat
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import com.carlosjimz87.copyfiles.core.Actions
import com.carlosjimz87.copyfiles.core.Phillips
import com.carlosjimz87.copyfiles.managers.IntentsManager
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class IntentsManagerShould {

    @get:Rule
    val activityTestRule = ActivityTestRule(MainActivity::class.java)


    @Before
    fun setUp() {
        Intents.init()
    }

    @After
    fun tearDown() {
        Intents.release()
    }

    @Test
    fun create_flag_extras_intent() {
        val actionInstalled = Actions.INSTALLED
        val flag = Intent.FLAG_INCLUDE_STOPPED_PACKAGES
        val extras = arrayOf(
            "path" to "/path/to/apk.apk",
            "installed" to true,
            "counter" to 10
        )

        val intent = IntentsManager.createAndSendIntent(
            context = activityTestRule.activity,
            action = actionInstalled,
            flag = flag,
            extras = extras
        )

        assertThat(intent).hasAction(actionInstalled)
        assertThat(intent).hasFlags(flag)

        extras.forEach { (key, value) ->
            assertThat(intent).extras().containsKey(key)
            when (value) {
                is String -> assertThat(intent).extras().string(key).isEqualTo(value)
                is Boolean -> assertThat(intent).extras().bool(key).isEqualTo(value)
                is Int -> assertThat(intent).extras().integer(key).isEqualTo(value)
            }
        }
    }

    @Test
    fun create_and_send_phillips_intent() {
        val actionInstalled = Phillips.TPV_UPDATE
        val extras = arrayOf(
            "path" to "/path/to/apk.apk",
            "keep" to true,
            "isAllowDowngrade" to 1
        )

        val intent = IntentsManager.sendPhillipsIntent(
            activityTestRule.activity,
            actionInstalled,
            extras = extras,
            send = true
        )

        assertThat(intent).hasAction(actionInstalled)

        extras.forEach { (key, value) ->
            assertThat(intent).extras().containsKey(key)
            when (value) {
                is String -> assertThat(intent).extras().string(key).isEqualTo(value)
                is Boolean -> assertThat(intent).extras().bool(key).isEqualTo(value)
                is Int -> assertThat(intent).extras().integer(key).isEqualTo(value)
            }

        }
    }

    @Test
    fun create_no_extras_intent() {
        val actionInstalled = Actions.UPL
        val flag = Intent.FLAG_INCLUDE_STOPPED_PACKAGES

        val intent = IntentsManager.createAndSendIntent(
            activityTestRule.activity,
            actionInstalled,
            flag = flag,
            send = true
        )

        assertThat(intent).hasAction(actionInstalled)
        assertThat(intent).hasFlags(flag)
    }

    @Test
    fun create_no_flag_no_extras_intent() {
        val actionInstalled = Actions.UPL

        val intent = IntentsManager.createAndSendIntent(
            activityTestRule.activity,
            actionInstalled,
            send = true
        )

        assertThat(intent).hasAction(actionInstalled)
    }

    @Test
    fun return_string_extras_from_intent() {
        val actionInstalled = Actions.INSTALLED
        val flag = Intent.FLAG_INCLUDE_STOPPED_PACKAGES
        val extras = arrayOf(
            "path" to "/path/to/apk.apk",
            "installed" to "1",
            "counter" to "0"
        )

        val intent = IntentsManager.createAndSendIntent(
            context = activityTestRule.activity,
            action = actionInstalled,
            flag = flag,
            extras = extras
        )

        val resultedExtras = IntentsManager.getStringExtras(intent, "path", "installed", "counter")

        resultedExtras.forEach { (key, value) ->
            assertThat(intent).extras().containsKey(key)
            assertThat(intent).extras().string(key).isEqualTo(value)
        }
    }

//    @Test
//    fun test_pending_intent_creation() {
//        val actionInstalled = Actions.UPL
//        val flag = Intent.FLAG_INCLUDE_STOPPED_PACKAGES
//        val pendingIntent = IntentsManager.createPendingIntent(
//            activityTestRule.activity,
//            FakeReceiver::class.java,
//            flag = flag,
//            code = FakeReceiver.REQUEST_CODE
//        )
//
//        assertThat(pendingIntent).hasAction(actionInstalled)
//        assertThat(intent).hasFlags(flag)
//    }

}
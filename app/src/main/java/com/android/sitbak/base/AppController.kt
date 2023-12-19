package com.android.sitbak.base

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.provider.Settings
import com.android.sitbak.auth.login.LoginData
import com.android.sitbak.network.SocketIO
import com.android.sitbak.onesignal.OneSignalNotificationHandler
import com.onesignal.OneSignal

class AppController : Application() {

    companion object {
        var resetOTP = 0
        var isGuest = false
        var deviceAppUID = ""
        var pageCount = 10
        var isHomeActive = false

        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
        var inviteReferralCode: String? = ""
        var userDetails: LoginData? = null
        lateinit var socket: SocketIO

        fun isSocketInitialized(): Boolean = ::socket.isInitialized
    }

    @SuppressLint("HardwareIds")
    override fun onCreate() {
        super.onCreate()
        deviceAppUID = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
        context = this


        OneSignal.startInit(this)
            .filterOtherGCMReceivers(true)
            .disableGmsMissingPrompt(false)
            .autoPromptLocation(false) // default call promptLocation later
            .setNotificationReceivedHandler(OneSignalNotificationHandler(this@AppController))
            .setNotificationOpenedHandler(OneSignalNotificationHandler(this@AppController))
            .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
            .init()

    }


}
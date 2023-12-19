package com.android.sitbak.onesignal

import android.widget.RemoteViews
import com.onesignal.NotificationExtenderService
import com.onesignal.OSNotificationReceivedResult

class OnSignalNotificationExtenderService : NotificationExtenderService() {

    override fun onNotificationProcessing(receivedResult: OSNotificationReceivedResult): Boolean {
        return false
    }
}

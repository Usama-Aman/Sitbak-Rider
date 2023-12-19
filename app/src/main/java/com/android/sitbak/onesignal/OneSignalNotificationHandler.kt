package com.android.sitbak.onesignal

import android.content.Intent
import android.util.Log
import com.android.sitbak.auth.BoardingActivity
import com.android.sitbak.auth.login.LoginActivity
import com.android.sitbak.base.AppController
import com.android.sitbak.home.HomeActivity
import com.android.sitbak.home.chat.ChatActivity
import com.android.sitbak.utils.Constants
import com.android.sitbak.utils.NotificationTypes
import com.android.sitbak.utils.SharedPreference
import com.onesignal.OSNotification
import com.onesignal.OSNotificationOpenResult
import com.onesignal.OneSignal
import org.json.JSONObject

class OneSignalNotificationHandler(private var baseApplication: AppController) :
    OneSignal.NotificationOpenedHandler,
    OneSignal.NotificationReceivedHandler {

    override fun notificationOpened(result: OSNotificationOpenResult) {

        val data = result.notification.payload.additionalData

        Log.e("NotificationOpened", data.toString())

        try {
            val json = JSONObject(data.toString())
            val notificationType = json.getString("type")
            val orderId = json.getInt("order_id")
            val body = json.getJSONObject("body")

            var sender = JSONObject()
            if (json.has("sender"))
                sender = json.getJSONObject("sender")

            if (SharedPreference.getBoolean(baseApplication, Constants.isUserLoggedIn)) {
                when (notificationType) {
                    NotificationTypes.MessageReceivedFromBuyer -> {
                        val intent = Intent(baseApplication, ChatActivity::class.java)
                        intent.putExtra(Constants.fromNotification, true)
                        intent.putExtra(Constants.orderId, orderId)
                        intent.putExtra(Constants.chatSenderName, sender.getString("name"))
                        intent.putExtra(Constants.chatSenderPhoto, sender.getString("photo_path"))
                        intent.putExtra(Constants.notificationBody, body.toString())
                        intent.putExtra(Constants.notificationType, notificationType.toString())
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                        baseApplication.startActivity(intent)
                    }
                    NotificationTypes.DriverAcceptDelivery -> {
                        val intent = Intent(baseApplication, HomeActivity::class.java)
                        intent.putExtra(Constants.fromNotification, true)
                        intent.putExtra(Constants.notificationType, notificationType)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                        baseApplication.startActivity(intent)
                    }
                }
            } else {
                val intent = Intent(baseApplication, LoginActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                baseApplication.startActivity(intent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    override fun notificationReceived(notification: OSNotification) {
        val data = notification.payload.additionalData
        Log.e("NotificationData", data.toString())

        try {

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}
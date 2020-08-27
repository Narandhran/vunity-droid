package com.vunity.fcm

import android.annotation.SuppressLint
import android.content.Intent
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.vunity.general.Constants.PUSH_NOTIFICATION

@SuppressLint("MissingFirebaseInstanceTokenRefresh")
class FCMMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.e(TAG, "Notification From: " + remoteMessage.from)
//        Check if message contains a notification payload.
        if (remoteMessage.notification != null) {
            val title = remoteMessage.notification?.title.toString()
            val body = remoteMessage.notification?.body.toString()
            val data = remoteMessage.data.toString()
            Log.e(TAG, "Notification Title: $title")
            Log.e(TAG, "Notification Body: $body")
            Log.e(TAG, "Notification payload: $data")
            val pushNotification = Intent(PUSH_NOTIFICATION)
            LocalBroadcastManager.getInstance(this).sendBroadcast(pushNotification)
            NotificationUtils().sendNotification(this, title, body, data)
//            NotificationUtils().playNotificationSound(this)

        }
    }

    companion object {
        val TAG = FCMMessagingService::class.java.simpleName
    }
}

package com.vunity.fcm

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import com.vunity.R
import com.vunity.user.Splash
import java.util.*


class NotificationUtils {
    fun sendNotification(context: Context, title: String?, body: String?, bookId: String?) {
        val intent = Intent(context, Splash::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.putExtra("title", title)
        intent.putExtra("body", body)
        intent.putExtra("bookId", bookId)
        val pendingIntent = PendingIntent.getActivity(
            context, 0 /* Request code */, intent,
            PendingIntent.FLAG_ONE_SHOT
        )

        val sound =
            Uri.parse("android.resource://" + context.packageName.toString() + "/" + R.raw.custom_sound)
        val largeIcon = BitmapFactory.decodeResource(context.resources, R.drawable.im_company_logo)
        try {
            val notificationBuilder =
                NotificationCompat.Builder(context, context.getString(R.string.app_name))
            notificationBuilder.setSmallIcon(R.drawable.ic_stat_notification)
                .setLargeIcon(largeIcon)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setChannelId(context.getString(R.string.default_notification_channel_id))
                .setSound(sound)
                .setContentIntent(pendingIntent)
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText(body)
                )
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val time = Date().time
            val tmpStr = time.toString()
            val last4Str = tmpStr.substring(tmpStr.length - 5)
            val notificationId = Integer.valueOf(last4Str)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    context.getString(R.string.default_notification_channel_id),
                    context.getString(R.string.app_name),
                    NotificationManager.IMPORTANCE_HIGH
                )
                channel.setSound(sound, Notification.AUDIO_ATTRIBUTES_DEFAULT)
                notificationManager.createNotificationChannel(channel)
            }
            notificationManager.notify(
                notificationId/* ID of notification */,
                notificationBuilder.build()
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun playNotificationSound(context: Context) {
        try {
            val sound =
                Uri.parse("android.resource://" + context.packageName.toString() + "/" + R.raw.custom_sound)
            val r = RingtoneManager.getRingtone(context, sound)
            r.play()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
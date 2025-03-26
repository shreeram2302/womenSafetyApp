package com.example.womensafetyapp.services

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.*
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.IconCompat
import com.example.womensafetyapp.Activities.ChatRoomsActivity
import com.example.womensafetyapp.Activities.GChatActivity
import com.example.womensafetyapp.MainActivity
import com.example.womensafetyapp.R
import com.example.womensafetyapp.models.ChatRoom
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.util.*

class MyFirebaseMessagingService : FirebaseMessagingService() {
    private val channelID = "class-update"
    private val channelName = "class-updates"

    private val notificationManager: NotificationManager by lazy {
        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        }

        // 🔹 Create an Intent to open an Activity when notification is clicked
        val intent = Intent(this, ChatRoomsActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }



        // 🔹 Create a PendingIntent
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE // Ensure it works on Android 12+
        )

        val builder = NotificationCompat.Builder(applicationContext, channelID)
            .setSmallIcon(IconCompat.createWithResource(applicationContext, R.drawable.applogo))
            .setColor(applicationContext.getColor(R.color.black))
            .setContentTitle(message.data["title"])
            .setContentText(message.data["body"])
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setBadgeIconType(R.drawable.applogo)
            .setAutoCancel(true)
            .setAutoCancel(true) // Notification disappears when clicked
            .setContentIntent(pendingIntent)
            .setOngoing(false)
            .setLights(
                ContextCompat.getColor(applicationContext, R.color.black),
                5000,
                5000
            )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            with(NotificationManagerCompat.from(applicationContext)) {
                if (ActivityCompat.checkSelfPermission(
                        applicationContext,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return
                }
                notify(Random().nextInt(3000), builder.build())
            }
        } else {
            NotificationManagerCompat.from(applicationContext).notify(Random().nextInt(3000), builder.build())
        }

        // Play ringtone and vibrate when notification is received
        playRingtoneAndVibrate()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            channelID,
            channelName,
            NotificationManager.IMPORTANCE_HIGH
        )
        notificationManager.createNotificationChannel(channel)
    }

    private fun playRingtoneAndVibrate() {
        // Play a ringtone
        val notificationUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
        val ringtone: Ringtone = RingtoneManager.getRingtone(applicationContext, notificationUri)
        ringtone.play()

        // Stop ringtone after 10 seconds
        Handler(Looper.getMainLooper()).postDelayed({
            if (ringtone.isPlaying) {
                ringtone.stop()
            }
        }, 10_000)

        // Vibrate the phone
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(1000) // Vibrate for 1 second
        }
    }
}

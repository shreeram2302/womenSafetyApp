package com.example.womensafetyapp.services

import android.Manifest
import android.app.*
import android.content.*
import android.content.pm.PackageManager
import android.location.*
import android.media.AudioManager
import android.os.*
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.example.womensafetyapp.utils.SharedPreferencesHelper.getFromSharedPrefs
import com.google.android.gms.location.*
import com.google.android.gms.location.LocationRequest
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class VolumeButtonService : Service() {

    private var lastPressTime: Long = 0
    private var pressCount = 0
    private lateinit var audioManager: AudioManager
    private lateinit var receiver: BroadcastReceiver
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val firestore = FirebaseFirestore.getInstance()
    private lateinit var chatId :String // Change this dynamically if needed
    private var isTracking = false

    override fun onCreate() {
        super.onCreate()
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
//        chatId = getFromSharedPrefs(this, "ChatIdKey").toString()
        chatId ="9921908795"
//        chatId=getFromSharedPrefs(this,"ChatIdKey")

        receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == "android.media.VOLUME_CHANGED_ACTION") {
                    detectTriplePress()
                }
            }
        }
        val filter = IntentFilter("android.media.VOLUME_CHANGED_ACTION")
        registerReceiver(receiver, filter)

        startForegroundService()
    }

    private fun detectTriplePress() {
        val currentTime = SystemClock.elapsedRealtime()

        if (currentTime - lastPressTime < 1000) {
            pressCount++
        } else {
            pressCount = 1
        }
        lastPressTime = currentTime

        if (pressCount >= 4) {
            Log.d("VolumeService", "Triple press detected! Starting emergency service.")
            Toast.makeText(this, "Emergency Triggered!", Toast.LENGTH_SHORT).show()
            startEmergencyService()
            vibrateDevice()
            pressCount = 0
        }
    }

    private fun startEmergencyService() {
        val serviceIntent = Intent(this, VoiceRecordingService::class.java)
        startService(serviceIntent)

        val serviceIntent2 = Intent(this, CameraCaptureService::class.java)
        startService(serviceIntent2)

        // Start tracking and updating location every 10 seconds
        if (!isTracking) {
            isTracking = true
            startLocationUpdates()
        }
    }

    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000)
            .setMinUpdateIntervalMillis(10000)  // Update every 10 sec
            .build()

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    uploadLocationToFirebase(location.latitude, location.longitude)
                }
            }
        }

        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        }
    }

    private fun uploadLocationToFirebase(latitude: Double, longitude: Double) {
        val messageId = "locationmsg"  // Generate unique message ID
        val locationData = mapOf(
            "latitude" to latitude,
            "longitude" to longitude,
            "timestamp" to System.currentTimeMillis()
        )

        firestore.collection("chats").document(chatId)
            .collection("messages").document(messageId)
            .set(locationData)
            .addOnSuccessListener {
                Log.d("Firebase", "Location added successfully")
            }
            .addOnFailureListener { e ->
                Log.e("Firebase", "Error adding location", e)
            }
    }

    private fun vibrateDevice() {
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(500)
        }
    }

    private fun startForegroundService() {
        val notificationChannelId = "VOLUME_SERVICE_CHANNEL"
        val notificationManager = getSystemService(NotificationManager::class.java)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                notificationChannelId,
                "Volume Button Service",
                NotificationManager.IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notification: Notification = NotificationCompat.Builder(this, notificationChannelId)
            .setContentTitle("Women Safety Active")
            .setContentText("Listening for volume key presses...")
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .build()

        startForeground(1, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
    }

    override fun onBind(intent: Intent?): IBinder? = null
}

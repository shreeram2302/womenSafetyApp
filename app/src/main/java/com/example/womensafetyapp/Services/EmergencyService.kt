package com.example.womensafetyapp.services

import android.app.*
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.MediaRecorder
import android.os.*
import android.util.Log
import android.view.Surface
import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.example.womensafetyapp.R
import com.example.womensafetyapp.utils.SharedPreferencesHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.sqrt

class EmergencyService : LifecycleService(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var lastShakeTime: Long = 0
    private var shakeCount = 0

    private lateinit var cameraExecutor: ExecutorService
    private lateinit var imageCapture: ImageCapture
    private lateinit var cameraProvider: ProcessCameraProvider
    private var mediaRecorder: MediaRecorder? = null
    private lateinit var audioFile: File
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate() {
        super.onCreate()
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI)

        cameraExecutor = Executors.newSingleThreadExecutor()
        startForegroundService()
//        startForeground(1, createNotification())
    }

    private fun startForegroundService() {
        val channelId = "emergency_service_channel"
        val channelName = "Emergency Service"

        val notificationManager = getSystemService(NotificationManager::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, channelName, NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Emergency Service Active")
            .setContentText("Listening for shake detection and capturing emergency data")
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setOngoing(true) // This keeps the notification persistent
            .build()

        startForeground(1, notification)
    }

    private fun createNotification(): Notification {
        val channelId = "emergency_service_channel"
        val channelName = "Emergency Service"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, channelName, NotificationManager.IMPORTANCE_HIGH
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Emergency Service Active")
            .setContentText("Monitoring for shakes and ready for action!")
            .setSmallIcon(R.drawable.baseline_services_24)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            val acceleration = sqrt(
                event.values[0] * event.values[0] +
                        event.values[1] * event.values[1] +
                        event.values[2] * event.values[2]
            ) - SensorManager.GRAVITY_EARTH

            if (acceleration > 12) {
                val currentTime = System.currentTimeMillis()

                if (currentTime - lastShakeTime < 1000) {
                    shakeCount++
                    if (shakeCount >= 6) {
                        triggerEmergencyActions()
                        shakeCount = 0
                    }
                } else {
                    shakeCount = 1
                }

                lastShakeTime = currentTime
            }
        }
    }

    private fun triggerEmergencyActions() {
//        sendEmergencyMessage()
        captureFrontImage()
        startVoiceRecording()
    }

    private fun sendEmergencyMessage() {
        val message = "Emergency! Need help. My location: https://maps.google.com/?q=latitude,longitude"
        val contacts = SharedPreferencesHelper.loadEmergencyContacts(this)

        if (contacts.isNotEmpty()) {
            val smsManager = android.telephony.SmsManager.getDefault()

            contacts.forEach { contact ->
                try {
                    smsManager.sendTextMessage(contact.second, null, message, null, null)
                    Toast.makeText(this, "Emergency Message Sent!", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(this, "Failed to send message!", Toast.LENGTH_SHORT).show()
                    e.printStackTrace()
                }
            }
        }
    }

    private fun captureFrontImage() {
        lifecycleScope.launch(Dispatchers.Main) {
            startCamera(CameraSelector.LENS_FACING_FRONT)

            handler.postDelayed({
                val frontPhotoFile = createFile("front")
                val outputOptions = ImageCapture.OutputFileOptions.Builder(frontPhotoFile).build()

                imageCapture.takePicture(
                    outputOptions,
                    ContextCompat.getMainExecutor(this@EmergencyService),
                    object : ImageCapture.OnImageSavedCallback {
                        override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                            Log.d("EmergencyService", "Front image saved: ${frontPhotoFile.absolutePath}")
                            Toast.makeText(this@EmergencyService,"image captured",Toast.LENGTH_LONG)

                            captureBackImage()
                        }

                        override fun onError(exception: ImageCaptureException) {
                            Log.e("EmergencyService", "Error capturing front image: ${exception.message}")
                        }
                    }
                )
            }, 500)
        }
    }

    private fun captureBackImage() {
        lifecycleScope.launch(Dispatchers.Main) {
            startCamera(CameraSelector.LENS_FACING_BACK)

            handler.postDelayed({
                val backPhotoFile = createFile("back")
                val outputOptions = ImageCapture.OutputFileOptions.Builder(backPhotoFile).build()

                imageCapture.takePicture(
                    outputOptions,
                    ContextCompat.getMainExecutor(this@EmergencyService),
                    object : ImageCapture.OnImageSavedCallback {
                        override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                            Log.d("EmergencyService", "Back image saved: ${backPhotoFile.absolutePath}")
                        }

                        override fun onError(exception: ImageCaptureException) {
                            Log.e("EmergencyService", "Error capturing back image: ${exception.message}")
                        }
                    }
                )
            }, 500)
        }
    }

    private fun startCamera(lensFacing: Int) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()
            cameraProvider.unbindAll()

            val cameraSelector = CameraSelector.Builder()
                .requireLensFacing(lensFacing)
                .build()

            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .setTargetRotation(Surface.ROTATION_0)
                .build()

            try {
                cameraProvider.bindToLifecycle(this, cameraSelector, imageCapture)
            } catch (e: Exception) {
                Log.e("EmergencyService", "Camera binding failed: ${e.message}")
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun startVoiceRecording() {
        try {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val storageDir = getExternalFilesDir(Environment.DIRECTORY_MUSIC)
            audioFile = File(storageDir, "recording_$timeStamp.3gp")

            mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setOutputFile(audioFile.absolutePath)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                prepare()
                start()
            }

            Log.d("EmergencyService", "Recording Started")
            handler.postDelayed({ stopVoiceRecording() }, 10000)

        } catch (e: Exception) {
            Log.e("EmergencyService", "Error starting recording: ${e.message}")
        }
    }

    private fun stopVoiceRecording() {
        mediaRecorder?.apply {
            stop()
            release()
        }
        mediaRecorder = null
        Log.d("EmergencyService", "Recording Saved: ${audioFile.absolutePath}")
        stopSelf()
    }

    private fun createFile(prefix: String): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val storageDir = getExternalFilesDir(null)
        return File(storageDir, "IMG_${prefix}_$timeStamp.jpg")
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onDestroy() {
        super.onDestroy()
//        cameraExecutor.shutdown()
//        sensorManager.unregisterListener(this)
    }
    override fun onTaskRemoved(rootIntent: Intent?) {
        val restartServiceIntent = Intent(applicationContext, EmergencyService::class.java).apply {
            setPackage(packageName)
        }
        val pendingIntent = PendingIntent.getService(
            this, 1, restartServiceIntent, PendingIntent.FLAG_IMMUTABLE
        )
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + 1000, pendingIntent)

        super.onTaskRemoved(rootIntent)
    }

//    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
//        super.onStartCommand(intent, flags, startId)
//        Log.d("EmergencyService", "Service Started")
//        startForegroundService()
//        return START_STICKY // Ensures service restarts if it gets killed
//    }

}

package com.example.womensafetyapp.services




import android.app.*
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.MediaRecorder
import android.os.*
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class VoiceRecordingService : Service() {
    private var mediaRecorder: MediaRecorder? = null
    private lateinit var audioFile: File
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate() {
        super.onCreate()
        startForeground(1, createNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("EmergencyService", "Service Started")
        capturePhoto()
        startVoiceRecording()
        return START_STICKY
    }

    private fun createNotification(): Notification {
        val channelId = "EMERGENCY_CHANNEL"
        val channelName = "Emergency Service"
        val notificationManager = getSystemService(NotificationManager::class.java)
        val channel = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH)
        } else {
            TODO("VERSION.SDK_INT < O")
        }
        notificationManager.createNotificationChannel(channel)

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Emergency Mode Active")
            .setContentText("Recording and capturing photos...")
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
    }

    private fun capturePhoto() {
        Log.d("EmergencyService", "Capturing Photos from both cameras")
        // Implement camera snapshot logic here
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
            Toast.makeText(this, "Recording Started", Toast.LENGTH_LONG).show()
            Handler(Looper.getMainLooper()).postDelayed({
                stopRecording()
            }, 10000)
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
    }

    private fun abandonAudioFocus() {
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.abandonAudioFocus(null)
    }

    private fun stopRecording() {
        try {
            mediaRecorder?.apply {
                stop() // Stop recording
                reset() // Reset the recorder
                release() // Release the microphone
            }
            mediaRecorder = null

            // Release Audio Focus
            abandonAudioFocus()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    /// for cam


}






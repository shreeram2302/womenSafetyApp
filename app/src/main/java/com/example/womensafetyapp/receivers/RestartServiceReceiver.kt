package com.example.womensafetyapp.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.womensafetyapp.services.CameraCaptureService
import com.example.womensafetyapp.services.EmergencyService
import com.example.womensafetyapp.services.ShakeDetectionService
import com.example.womensafetyapp.services.VoiceRecordingService
import com.example.womensafetyapp.services.VolumeButtonService

class RestartServiceReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        Log.d("RestartServiceReceiver", "Restarting VolumeButtonService")
//        val serviceIntent = Intent(context, VolumeButtonService::class.java)
        val serviceIntent1 = Intent(context, ShakeDetectionService::class.java)
//        val serviceIntent2 = Intent(context, EmergencyService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            context.startForegroundService(serviceIntent)
            context.startForegroundService(serviceIntent1)
//            context.startForegroundService(serviceIntent2)
        }
    }
}

package com.example.womensafetyapp.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.womensafetyapp.services.CameraCaptureService
import com.example.womensafetyapp.services.EmergencyService

import com.example.womensafetyapp.services.ShakeDetectionService
import com.example.womensafetyapp.services.VoiceRecordingService
import com.example.womensafetyapp.services.VolumeButtonService

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context?.startForegroundService(Intent(context, ShakeDetectionService::class.java))
//                context?.startForegroundService(Intent(context, VoiceRecordingService::class.java))
//                context?.startForegroundService(Intent(context, EmergencyService::class.java))
//                context?.startForegroundService(Intent(context, CameraCaptureService::class.java))
            }
        }
    }
}

package com.example.womensafetyapp.services

import android.app.*
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraCaptureService : LifecycleService() {

    private lateinit var cameraExecutor: ExecutorService
    private lateinit var imageCapture: ImageCapture
    private lateinit var cameraProvider: ProcessCameraProvider

    override fun onCreate() {
        super.onCreate()
        cameraExecutor = Executors.newSingleThreadExecutor()
        startForegroundService()

        // Capture images in the background
        lifecycleScope.launch(Dispatchers.IO) {
            captureFrontImage()
        }
    }

    /** Start Foreground Notification */
    private fun startForegroundService() {
        val channelId = "camera_service_channel"
        val channelName = "Camera Capture Service"

        val notificationManager = getSystemService(NotificationManager::class.java)
        val channel = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel(
                channelId, channelName, NotificationManager.IMPORTANCE_LOW
            )
        } else {
            TODO("VERSION.SDK_INT < O")
        }
        notificationManager.createNotificationChannel(channel)

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Capturing Images...")
            .setContentText("Women's Safety App is capturing images in background")
            .setSmallIcon(android.R.drawable.ic_menu_camera)
            .build()

        startForeground(1, notification)
    }

    /** Capture image from the Front Camera */
    private fun captureFrontImage() {
        lifecycleScope.launch(Dispatchers.Main) {
            startCamera(CameraSelector.LENS_FACING_FRONT)

            Handler(Looper.getMainLooper()).postDelayed({
                val frontPhotoFile = createFile("front")
                val outputOptions = ImageCapture.OutputFileOptions.Builder(frontPhotoFile).build()

                imageCapture.takePicture(
                    outputOptions,
                    ContextCompat.getMainExecutor(this@CameraCaptureService),
                    object : ImageCapture.OnImageSavedCallback {
                        override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                            Log.d("CameraService", "Front image saved: ${frontPhotoFile.absolutePath}")
                            Toast.makeText(this@CameraCaptureService, "Front image captured", Toast.LENGTH_SHORT).show()

                            Handler(Looper.getMainLooper()).postDelayed({
                                captureBackImage()
                            }, 5000) // 5-second delay
                        }

                        override fun onError(exception: ImageCaptureException) {
                            Log.e("CameraService", "Error capturing front image: ${exception.message}")
                        }
                    }
                )
            }, 500)
        }
    }

    /** Capture image from the Back Camera */
    private fun captureBackImage() {
        lifecycleScope.launch(Dispatchers.Main) {
            startCamera(CameraSelector.LENS_FACING_BACK)

            Handler(Looper.getMainLooper()).postDelayed({
                val backPhotoFile = createFile("back")
                val outputOptions = ImageCapture.OutputFileOptions.Builder(backPhotoFile).build()

                imageCapture.takePicture(
                    outputOptions,
                    ContextCompat.getMainExecutor(this@CameraCaptureService),
                    object : ImageCapture.OnImageSavedCallback {
                        override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                            Log.d("CameraService", "Back image saved: ${backPhotoFile.absolutePath}")
                            Toast.makeText(this@CameraCaptureService, "Back image captured", Toast.LENGTH_SHORT).show()
                            stopSelf() // Stop the service after capturing both images
                        }

                        override fun onError(exception: ImageCaptureException) {
                            Log.e("CameraService", "Error capturing back image: ${exception.message}")
                        }
                    }
                )
            }, 500)
        }
    }

    /** Initialize CameraX with specified camera */
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

            // Use lifecycleScope for binding
            lifecycleScope.launch(Dispatchers.Main) {
                try {
                    cameraProvider.bindToLifecycle(
                        this@CameraCaptureService, cameraSelector, imageCapture
                    )
                } catch (e: Exception) {
                    Log.e("CameraService", "Camera binding failed: ${e.message}")
                }
            }
        }, ContextCompat.getMainExecutor(this))
    }

    /** Generate Unique File Name */
    private fun createFile(cameraPosition: String): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val storageDir = getExternalFilesDir(null)
        return File(storageDir, "IMG_${cameraPosition}_$timeStamp.jpg")
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}

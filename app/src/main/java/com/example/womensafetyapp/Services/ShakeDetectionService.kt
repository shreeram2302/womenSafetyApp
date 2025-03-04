package com.example.womensafetyapp.services

import android.Manifest
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.MediaRecorder
import android.os.IBinder
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.core.app.NotificationCompat
import com.example.womensafetyapp.R
import com.example.womensafetyapp.utils.SharedPreferencesHelper
import android.telephony.SmsManager
import android.util.Log
import android.view.Surface
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.cloudinary.android.MediaManager
import com.example.womensafetyapp.MyApplication
import com.example.womensafetyapp.utils.CloudinaryHelper.uploadAudioToCloudinary
import com.example.womensafetyapp.utils.CloudinaryHelper.uploadImageToCloudinary
import com.example.womensafetyapp.utils.SharedPreferencesHelper.getFromSharedPrefs
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.sqrt

class ShakeDetectionService : LifecycleService(),SensorEventListener {
    private lateinit var sensorManager: SensorManager
    private lateinit var sensor: Sensor
    private var isRecording = false

    private var lastShakeTime: Long = 0
    private var shakeCount = 0




    // Firebase
    private val db = FirebaseFirestore.getInstance()
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    val chatRef = db.collection("chats").document(userId.toString())





    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            locationResult.locations.forEach { location ->
                sendLocationToChatroom(location.latitude, location.longitude)
            }
        }
    }

private lateinit var fusedLocationClient: FusedLocationProviderClient

    private lateinit var cameraExecutor: ExecutorService
    private lateinit var imageCapture: ImageCapture
    private lateinit var cameraProvider: ProcessCameraProvider
    private var mediaRecorder: MediaRecorder? = null
    private lateinit var audioFile: File
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate() {
        super.onCreate()
        sensorManager = applicationContext.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val smsManager = SmsManager.getDefault()

        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI)
        cameraExecutor = Executors.newSingleThreadExecutor()
        startForeground(1, createNotification()) // Start Foreground Service

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        startLocationUpdates()

    }

    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.create().apply {
            interval = 10000  // Update every 5 seconds
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    }








    private fun createNotification(): Notification {
        val notificationChannelId = "shake_service_channel"
        val channelName = "Shake Detection Service"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                notificationChannelId,
                channelName,
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        return NotificationCompat.Builder(this, notificationChannelId)
            .setContentTitle("Shake Detection Active")
            .setContentText("Your device is being monitored for emergency shakes.")
            .setSmallIcon(R.drawable.baseline_services_24)
            .build()
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            val acceleration = sqrt(event.values[0] * event.values[0] +
                    event.values[1] * event.values[1] +
                    event.values[2] * event.values[2]) - SensorManager.GRAVITY_EARTH

            if (acceleration > 12) {  // Threshold for detecting shake
                val currentTime = System.currentTimeMillis()

                if (currentTime - lastShakeTime < 1000) {
                    shakeCount++
                    if (shakeCount >= 6) {
                        sendEmergencyMessage()
                        triggerEmergencyActions()
//                        Toast.makeText(this, "Message Sent Successfully!", Toast.LENGTH_SHORT).show()
                        vibrateDevice(this)
                        shakeCount = 0
                    }
                } else {
                    shakeCount = 1
                }

                lastShakeTime = currentTime
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    fun vibrateDevice(context: Context) {
        val vibrator = context.getSystemService(Service.VIBRATOR_SERVICE) as Vibrator

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(500)
        }
    }
    private fun sendEmergencyMessage() {
        val message = "Emergency! Need help. My location: https://maps.google.com/?q=latitude,longitude"

        val contacts = SharedPreferencesHelper.loadEmergencyContacts(this)

        if (contacts.isNotEmpty()) {
            val smsManager = SmsManager.getDefault()

            contacts.forEach { contact ->
                val phoneNumber = contact.second
                try {
                    smsManager.sendTextMessage(phoneNumber, null, message, null, null)
//                    Toast.makeText(this, "Message Sent Successfully!", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(this, "Failed to send!", Toast.LENGTH_SHORT).show()

                    e.printStackTrace()
                }
            }
            val emergencyServiceIntent = Intent(this, EmergencyService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                startForegroundService(emergencyServiceIntent)
            } else {
//                startService(emergencyServiceIntent)
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        startForeground(1, createNotification())
        return START_STICKY // Keep the service running
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        val restartServiceIntent = Intent(applicationContext, ShakeDetectionService::class.java).also {
            it.setPackage(packageName)
        }
        val restartPendingIntent = PendingIntent.getService(
            this, 1, restartServiceIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, System.currentTimeMillis() + 1000, restartPendingIntent)
    }

    private fun stopVoiceRecording() {
        mediaRecorder?.apply {
            stop()
            release()
        }
        mediaRecorder = null
        Log.d("EmergencyService", "Recording Saved: ${audioFile.absolutePath}")

        // Don’t call stopSelf() here — we want the service to keep running
    }


    private fun triggerEmergencyActions() {
//        sendEmergencyMessage()
        captureFrontImage()
        startVoiceRecording()
    }


    private fun captureFrontImage() {
        lifecycleScope.launch(Dispatchers.Main) {
            startCamera(CameraSelector.LENS_FACING_FRONT)

            handler.postDelayed({
                val frontPhotoFile = createFile("front")
                val outputOptions = ImageCapture.OutputFileOptions.Builder(frontPhotoFile).build()

                imageCapture.takePicture(
                    outputOptions,
                    ContextCompat.getMainExecutor(this@ShakeDetectionService),
                    object : ImageCapture.OnImageSavedCallback {
                        override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                            Log.d("EmergencyService", "Front image saved: ${frontPhotoFile.absolutePath}")
                            Toast.makeText(this@ShakeDetectionService,"image captured",Toast.LENGTH_LONG)
                            compressImage(frontPhotoFile)

                            uploadImageToCloudinary(frontPhotoFile,
                                onSuccess = { url -> Log.d("Cloudinary", "Image uploaded: $url")


                                    sendToFirebase(chatRef,"Please save me ",url,"image")
                                },
                                onError = { error -> Log.e("Cloudinary", "Upload failed: $error") }
                            )


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
                    ContextCompat.getMainExecutor(this@ShakeDetectionService),
                    object : ImageCapture.OnImageSavedCallback {
                        override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                            Log.d("EmergencyService", "Back image saved: ${backPhotoFile.absolutePath}")
                            Toast.makeText(this@ShakeDetectionService,"image captured",Toast.LENGTH_SHORT)
                            compressImage(backPhotoFile)

                            uploadImageToCloudinary(backPhotoFile,
                                onSuccess = { url -> Log.d("Cloudinary", "Image uploaded: $url")

                                    sendToFirebase(chatRef,"Please save me ",url,"image")
                                            },
                                onError = { error -> Log.e("Cloudinary", "Upload failed: $error") }
                            )


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
            try {
                cameraProvider = cameraProviderFuture.get()
                cameraProvider.unbindAll() // Unbind before rebinding to avoid conflicts

                val cameraSelector = CameraSelector.Builder()
                    .requireLensFacing(lensFacing)
                    .build()

                imageCapture = ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    .setTargetRotation(Surface.ROTATION_0)
                    .build()

                cameraProvider.bindToLifecycle(this, cameraSelector, imageCapture)
                Log.d("EmergencyService", "Camera started successfully with lens facing $lensFacing")

            } catch (e: Exception) {
                Log.e("EmergencyService", "Failed to bind camera lifecycle: ${e.message}")
            }
        }, ContextCompat.getMainExecutor(this))
    }


    private fun createFile(prefix: String): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val storageDir = getExternalFilesDir(null)
        return File(storageDir, "IMG_${prefix}_$timeStamp.jpg")
    }

    private fun compressImage(imageFile: File) {
        try {
            // Decode the original image into a Bitmap
            val bitmap = BitmapFactory.decodeFile(imageFile.absolutePath)

            // Create a compressed file
            val compressedFile = File(imageFile.parent, "COMPRESSED_${imageFile.name}")

            // Compress the image (JPEG format, 50% quality)
            val outputStream = FileOutputStream(compressedFile)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, outputStream)
            outputStream.flush()
            outputStream.close()

            // Delete the original file and rename the compressed file to original name
            imageFile.delete()
            compressedFile.renameTo(imageFile)

            Log.d("EmergencyService", "Image compressed successfully: ${imageFile.absolutePath}")

        } catch (e: Exception) {
            Log.e("EmergencyService", "Error compressing image: ${e.message}")
        }
    }



    private fun startVoiceRecording() {
    if (isRecording) {
        Log.d("EmergencyService", "Recording already in progress, skipping duplicate call")
        return
    }

    isRecording = true

    val audioFile = File(getExternalFilesDir(Environment.DIRECTORY_MUSIC), "recording_${getCurrentTimestamp()}.3gp")

    mediaRecorder = MediaRecorder().apply {
        setAudioSource(MediaRecorder.AudioSource.MIC)
        setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
        setOutputFile(audioFile.absolutePath)
        setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)

        try {
            prepare()
            start()
            Log.d("EmergencyService", "Recording started")
        } catch (e: IOException) {
            Log.e("EmergencyService", "Error starting recording: ${e.message}")
            isRecording = false // Reset flag on failure
        }
    }

    // Stop recording after 10 seconds
    Handler(Looper.getMainLooper()).postDelayed({
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            Log.d("EmergencyService", "Recording Saved: ${audioFile.absolutePath}")
//            Toast.makeText(this@ShakeDetectionService,"Recoding saved",Toast.LENGTH_SHORT)
            uploadAudioToCloudinary(audioFile,
                onSuccess = {url -> Log.d("Cloudinary","Upload successful $url")

                    sendToFirebase(chatRef,"Please save me ",url,"audio")
                    sendEmergencyMsg(chatRef,"Please save me i'm in Danger ")
                    Toast.makeText(this,"uploaded to firebase",Toast.LENGTH_SHORT)
                            },
                onError = { err -> Log.e("Cloudinary","Upload Failed  :$err")})

        } catch (e: Exception) {
            Log.e("EmergencyService", "Error stopping recording: ${e.message}")
        } finally {
            mediaRecorder = null
            isRecording = false // Reset flag after recording stops
        }
    }, 10000)
}

    private fun getCurrentTimestamp(): String {
        val formatter = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        return formatter.format(Date())
    }



    override fun onDestroy() {
        super.onDestroy()
        if (::sensorManager.isInitialized) {
            sensorManager.unregisterListener(this)
        }
    }

    private fun sendLocationToChatroom(latitude: Double, longitude: Double) {
        if (userId != null) {
            FirebaseFirestore.getInstance().collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    val senderName = document.getString("username") ?: "Unknown"

                    val locationData = mapOf(
                        "chatId" to chatRef.id,
                        "senderId" to userId,
                        "senderName" to senderName,
                        "latitude" to latitude,
                        "longitude" to longitude,
                        "type" to "location",
                        "timestamp" to System.currentTimeMillis()
                    )
                    chatRef.collection("messages").document(userId).set(locationData)
                        .addOnSuccessListener {
                            Log.d("Firestore", "Message sent successfully with type: Location")

                        }
                        .addOnFailureListener { e ->
                            Log.e("FirestoreError", "Message sending failed: ${e.message}")
                        }
                }
                .addOnFailureListener { e ->
                    Log.e("FirestoreError", "Failed to fetch sender name: ${e.message}")
                }
        }

    }
    fun sendToFirebase(chatRef: DocumentReference, message: String, fileUrl: String? = null, messageType: String = "text") {

        if (userId != null) {
            FirebaseFirestore.getInstance().collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    val senderName = document.getString("username") ?: "Unknown"

                    val messageData = hashMapOf(
                        "chatId" to chatRef.id,
                        "senderId" to userId,
                        "senderName" to senderName,
                        "text" to "", // Store text only for text messages
                        "fileUrl" to fileUrl, // Store file URL if available
                        "type" to messageType, // Message type (text, image, audio)
                        "timestamp" to System.currentTimeMillis()
                    )

                    chatRef.collection("messages").add(messageData)
                        .addOnSuccessListener {
                            Log.d("Firestore", "Message sent successfully with type: $messageType")
                        }
                        .addOnFailureListener { e ->
                            Log.e("FirestoreError", "Message sending failed: ${e.message}")
                        }
                }
                .addOnFailureListener { e ->
                    Log.e("FirestoreError", "Failed to fetch sender name: ${e.message}")
                }
        }
    }

    fun sendEmergencyMsg(chatRef: DocumentReference, message: String) {

        if (userId != null) {
            FirebaseFirestore.getInstance().collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    val senderName = document.getString("username") ?: "Unknown"

                    val messageData = hashMapOf(
                        "chatId" to chatRef.id,
                        "senderId" to userId,
                        "senderName" to senderName,
                        "text" to message, // Store text only for text messages
                        "timestamp" to System.currentTimeMillis()
                    )

                    chatRef.collection("messages").add(messageData)
                        .addOnSuccessListener {
                            Log.d("Firestore", "Message sent successfully with type: $message")
                        }
                        .addOnFailureListener { e ->
                            Log.e("FirestoreError", "Message sending failed: ${e.message}")
                        }
                }
                .addOnFailureListener { e ->
                    Log.e("FirestoreError", "Failed to fetch sender name: ${e.message}")
                }
        }
    }


//    fun uploadImageToCloudinary(imageFile: File, onSuccess: (String) -> Unit, onError: (String) -> Unit) {
//        MediaManager.get().upload(imageFile.absolutePath)
//            .option("resource_type", "image") // Specify it's an image
//            .callback(object : com.cloudinary.android.callback.UploadCallback {
//                override fun onStart(requestId: String?) {
//                    Log.d("Cloudinary", "Upload started")
//                }
//
//                override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {}
//
//                override fun onSuccess(requestId: String?, resultData: MutableMap<Any?, Any?>?) {
//                    val imageUrl = resultData?.get("url").toString()
//                    Toast.makeText(this@ShakeDetectionService,"uploaded",Toast.LENGTH_SHORT)
//                    onSuccess(imageUrl) // Return the uploaded image URL
//
//                }
//
//                override fun onError(requestId: String?, error: com.cloudinary.android.callback.ErrorInfo?) {
//                    onError(error?.description ?: "Unknown error")
//                }
//
//                override fun onReschedule(requestId: String?, error: com.cloudinary.android.callback.ErrorInfo?) {}
//            })
//            .dispatch()
//    }


}

//    private fun stopVoiceRecording() {
//        mediaRecorder?.apply {
//            stop()
//            release()
//        }
//        mediaRecorder = null
//        Log.d("EmergencyService", "Recording Saved: ${audioFile.absolutePath}")
////        stopSelf()
//    }

//    private fun startVoiceRecording() {
//        try {
//            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
//            val storageDir = getExternalFilesDir(Environment.DIRECTORY_MUSIC)
//            audioFile = File(storageDir, "recording_$timeStamp.3gp")
//
//            mediaRecorder = MediaRecorder().apply {
//                setAudioSource(MediaRecorder.AudioSource.MIC)
//                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
//                setOutputFile(audioFile.absolutePath)
//                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
//                prepare()
//                start()
//            }
//
//            Log.d("EmergencyService", "Recording Started")
//            handler.postDelayed({ stopVoiceRecording() }, 10000)
//
//        } catch (e: Exception) {
//            Log.e("EmergencyService", "Error starting recording: ${e.message}")
//        }
//    }

//    override fun onTaskRemoved(rootIntent: Intent?) {
//        val restartServiceIntent = Intent(applicationContext, ShakeDetectionService::class.java)
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            startForegroundService(restartServiceIntent)
//        }
//        super.onTaskRemoved(rootIntent)
//    }
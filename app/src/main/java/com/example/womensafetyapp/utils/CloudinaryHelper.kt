package com.example.womensafetyapp.utils

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.cloudinary.android.MediaManager
import com.example.womensafetyapp.MyApplication
import java.io.File
import com.example.womensafetyapp.R

object CloudinaryHelper {
        fun initializeCloudinary(context: Context) {
            val api_secret = context.resources.getString(R.string.cloudinary_api_secret)
            val cloud_name = context.resources.getString(R.string.cloud_name)
            val api_key = context.resources.getString(R.string.cloudinary_api_key)

//            val appContext = context.applicationContext // Use app context instead of MyApplication.getInstance()
            // Initialize Cloudinary with appContext
            val config = mapOf(
                "cloud_name" to "${cloud_name}",
                "api_key" to "${api_key}",
                "api_secret" to "${api_secret}"
            )

            MediaManager.init(context.applicationContext, config)
        }
        fun uploadImageToCloudinary(
            imageFile: File,
            onSuccess: (String) -> Unit,
            onError: (String) -> Unit
        ) {
            val options = hashMapOf(
                "folder" to "womensafety", // Upload inside 'womensafety' folder
                "resource_type" to "image"
            )

            MediaManager.get().upload(imageFile.absolutePath)
                .options(options)
                .callback(object : com.cloudinary.android.callback.UploadCallback {
                    override fun onStart(requestId: String?) {
                        Log.d("Cloudinary", "Upload started")
                    }

                    override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {}

                    override fun onSuccess(requestId: String?, resultData: MutableMap<Any?, Any?>?) {
                        val imageUrl = resultData?.get("secure_url").toString()
                        Log.d("Cloudinary", "Upload successful: $imageUrl")

                        // Fix Toast (previously missing .show())
                        Toast.makeText(MyApplication.getInstance(), "Uploaded Successfully", Toast.LENGTH_SHORT).show()

                        onSuccess(imageUrl) // Return uploaded image URL
                    }

                    override fun onError(requestId: String?, error: com.cloudinary.android.callback.ErrorInfo?) {
                        Log.e("Cloudinary", "Upload failed: ${error?.description}")
                        onError(error?.description ?: "Unknown error")
                    }

                    override fun onReschedule(requestId: String?, error: com.cloudinary.android.callback.ErrorInfo?) {}
                })
                .dispatch()
        }



    fun uploadAudioToCloudinary(audioFile: File, onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        MediaManager.get().upload(audioFile.absolutePath)
            .option("resource_type", "video")  // Cloudinary treats audio as 'video'
            .option("folder", "audios")        // Upload to "audios" folder
            .option("format", "mp3")           // Convert to 'mp3' for compatibility
            .callback(object : com.cloudinary.android.callback.UploadCallback {
                override fun onStart(requestId: String?) {
                    Log.d("Cloudinary", "Audio upload started")
                }

                override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {}

                override fun onSuccess(requestId: String?, resultData: MutableMap<Any?, Any?>?) {
                    val audioUrl = resultData?.get("secure_url").toString()
                    Log.d("Cloudinary", "Audio uploaded: $audioUrl")
                    Toast.makeText(MyApplication.getInstance(), "Audio Uploaded Successfully", Toast.LENGTH_SHORT).show()
                    onSuccess(audioUrl) // Return uploaded audio URL
                }

                override fun onError(requestId: String?, error: com.cloudinary.android.callback.ErrorInfo?) {
                    Log.e("Cloudinary", "Audio upload failed: ${error?.description}")
                    onError(error?.description ?: "Unknown error")
                }

                override fun onReschedule(requestId: String?, error: com.cloudinary.android.callback.ErrorInfo?) {}
            })
            .dispatch()
    }



}

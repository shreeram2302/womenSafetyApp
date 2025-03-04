package com.example.womensafetyapp

import android.app.Application
import com.example.womensafetyapp.utils.CloudinaryHelper

class MyApplication : Application() {
    companion object {
        private var instance: MyApplication? = null

        fun getInstance(): MyApplication {
            return instance ?: throw IllegalStateException("Application not created yet!")
        }
    }

    override fun onCreate() {
        super.onCreate()
        instance = this // Ensure this is initialized first
        CloudinaryHelper.initializeCloudinary(this) // Move this after initialization
    }
}



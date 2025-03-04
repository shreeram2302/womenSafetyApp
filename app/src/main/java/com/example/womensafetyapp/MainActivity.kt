package com.example.womensafetyapp

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.provider.Settings

import android.os.Bundle
import android.os.PowerManager
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.womensafetyapp.Activities.ChatRoomsActivity

import com.example.womensafetyapp.services.ShakeDetectionService
import com.example.womensafetyapp.utils.PermissionsHelper
import com.example.womensafetyapp.utils.SharedPreferencesHelper.getFromSharedPrefs
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {
    lateinit var contactsBtn : Button
    lateinit var stopbtn : Button
    lateinit var chatbtn : Button
    lateinit var gmapbtn : Button
    private var is_group : String? = null
    val db = FirebaseFirestore.getInstance()
    val cuserId = FirebaseAuth.getInstance().currentUser?.uid




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)



        PermissionsHelper.requestPermissions(this)

        contactsBtn=findViewById(R.id.btnStartRecording)
        stopbtn=findViewById(R.id.stopButton)
        chatbtn=findViewById(R.id.page_Chatrooms)
        gmapbtn=findViewById(R.id.page_Gmap)

        contactsBtn.setOnClickListener(){
            val intent = Intent(this, EmergencyContactActivity::class.java)
            startActivity(intent)
        }

        chatbtn.setOnClickListener(){
            val intent =Intent(this,ChatRoomsActivity::class.java)
            startActivity(intent)
        }

        stopbtn.setOnClickListener {
            // Stop the EmergencyService
            disable_Serices()
        }

        requestBatteryOptimizationExemption()
        disableBatteryOptimization()

        val intent = Intent(this, ShakeDetectionService::class.java)
        ContextCompat.startForegroundService(this, intent)



        if (PermissionsHelper.hasAllPermissions(this)) {
            val intent = Intent(this, ShakeDetectionService::class.java)
            ContextCompat.startForegroundService(this, intent)
        }else {
            PermissionsHelper.requestPermissions(this)
            Toast.makeText(this, "Permissions required!", Toast.LENGTH_SHORT).show()
        }




    }
 private fun hasEmeregencyGroup() :Boolean {
    val docRef = db.collection("chats").document(cuserId.toString()) // Replace with your collection and document ID
     var hasgroup = false
     docRef.get()

    docRef.get().addOnSuccessListener { document ->
        if (document.exists()) {
            println("Document exists: ${document.data}")
            hasgroup=true

        } else {
            println("Document does not exist")
        }

    }.addOnFailureListener { exception ->
        println("Error fetching document: ${exception.message}")
    }
     return hasgroup
}

//    private fun startShakeDetectionService() {
//        val serviceIntent = Intent(this, ShakeDetectionService::class.java)
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            startForegroundService(serviceIntent)
//        }  // Ensures the service is not stopped
//    }

    // SharedPreferences Helper function to store and retrieve the flag
    object SharedPreferencesHelper {
        private const val PREF_NAME = "com.example.womensafetyapp.preferences"
        private const val KEY_BATTERY_OPTIMIZATION_DISABLED = "battery_optimization_disabled"

        fun setBatteryOptimizationDisabled(context: Context, isDisabled: Boolean) {
            val sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            sharedPreferences.edit().putBoolean(KEY_BATTERY_OPTIMIZATION_DISABLED, isDisabled).apply()
        }

        fun isBatteryOptimizationDisabled(context: Context): Boolean {
            val sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            return sharedPreferences.getBoolean(KEY_BATTERY_OPTIMIZATION_DISABLED, false)
        }
    }

    fun disableBatteryOptimization() {
        // Check if battery optimization has already been disabled
        if (SharedPreferencesHelper.isBatteryOptimizationDisabled(this)) {
            return // Skip if it's already been done
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
            try {
                startActivity(intent)
                // Once the activity is opened, set the flag to true
                SharedPreferencesHelper.setBatteryOptimizationDisabled(this, true)
            } catch (e: ActivityNotFoundException) {
                Log.e("BatteryOptimization", "Battery optimization settings activity not found", e)
                Toast.makeText(this, "Battery optimization settings not available on your device.", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Battery optimization is not needed on this Android version.", Toast.LENGTH_SHORT).show()
        }
    }



    override fun onDestroy() {
        super.onDestroy()
        stopShakeDetectionService()
    }

    private fun stopShakeDetectionService() {
        val serviceIntent = Intent(this, ShakeDetectionService::class.java)
//        stopService(serviceIntent)
    }
    private fun requestBatteryOptimizationExemption() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
            val packageName = packageName

            // Check if the app is already whitelisted
            if (!powerManager.isIgnoringBatteryOptimizations(packageName)) {
                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                    data = Uri.parse("package:$packageName")
                }
                try {
                    startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    Log.e("BatteryOptimization", "Request ignore battery optimization activity not found", e)
                    Toast.makeText(this, "Feature not supported on your device.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Log.d("BatteryOptimization", "Battery optimization already disabled.")
            }
        }
    }

    private fun disable_Serices (){

        stopService(Intent(this, ShakeDetectionService::class.java))
    }
}

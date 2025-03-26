package com.example.womensafetyapp

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.os.StrictMode
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.example.womensafetyapp.Activities.ChatRoomsActivity
import com.example.womensafetyapp.MainActivity.SharedPreferencesHelper.KEY_BATTERY_OPTIMIZATION_DISABLED
import com.example.womensafetyapp.MainActivity.SharedPreferencesHelper.PREF_NAME
import com.example.womensafetyapp.services.ShakeDetectionService
import com.example.womensafetyapp.utils.PermissionsHelper
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore


class MainActivity : AppCompatActivity() {
    lateinit var contactsBtn : CardView
    lateinit var stopbtn : CardView
    lateinit var chatbtn : CardView
    lateinit var profilebtn : CardView
    lateinit var tv_safetytips : CardView

    lateinit var chat_opn : ImageView
    lateinit var txt_primary : TextView

    private var is_group : String? = null
    val db = FirebaseFirestore.getInstance()
    val cuserId = FirebaseAuth.getInstance().currentUser?.uid

//    private val db = FirebaseFirestore.getInstance()  // Firestore instance
    private val auth = FirebaseAuth.getInstance()
//    private var currentUserId: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        window.statusBarColor = Color.parseColor("#002F6C")


        PermissionsHelper.requestPermissions(this)

        val policy =StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        contactsBtn=findViewById(R.id.btnStartRecording)
        fetchUserData()

//        saveFCMTokenToFirebase()

        stopbtn=findViewById(R.id.stopButton)
        chatbtn=findViewById(R.id.page_Chatrooms)
//        gmapbtn=findViewById(R.id.tv_guide)
        profilebtn=findViewById(R.id.profilepage)
        chat_opn=findViewById(R.id.chat_opn)
        tv_safetytips=findViewById(R.id.tv_safetytips)
//        tv_guide=findViewById(R.id.tv_guide)
        txt_primary=findViewById(R.id.txt_primary)


//        tv_guide.setOnClickListener {
//            startActivity(Intent(this,GuideActivity::class.java))
//        }

        tv_safetytips.setOnClickListener {
            val intent = Intent(this, SafetyTipsActivity::class.java)
            startActivity(intent)
        }


        chat_opn.setOnClickListener(){
            val intent = Intent(this, ChatRoomsActivity::class.java)
            startActivity(intent)
        }
        contactsBtn.setOnClickListener(){
            val intent = Intent(this, EmergencyContactActivity::class.java)
            startActivity(intent)
        }

        chatbtn.setOnClickListener(){
            val intent =Intent(this,ChatRoomsActivity::class.java)
            startActivity(intent)
        }
        profilebtn.setOnClickListener{
            val intent =Intent(this,ProfileActivity::class.java)
            startActivity(intent)
        }

        stopbtn.setOnClickListener {
            // Stop the EmergencyService
            disable_Services()
            val rootView = findViewById<View>(android.R.id.content)

            val snackbar = Snackbar.make(rootView, "Service is Stopped", Snackbar.LENGTH_LONG)
            snackbar.setBackgroundTint(resources.getColor(R.color.primaryColor))
            snackbar.setTextColor(resources.getColor(R.color.white))
            snackbar.setActionTextColor(resources.getColor(R.color.red))
            snackbar.setAction("OK") { /* Action here */ }
            snackbar.show()

//            Snackbar.make(rootView, "Service Is Stopped", Snackbar.LENGTH_SHORT).show()
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
//            Toast.makeText(this, "Permissions required!", Toast.LENGTH_SHORT).show()
        }



    }

    private fun fetchUserData() {
        db.collection("users").document(cuserId!!).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                     val username = document.getString("username") ?: "1"
                    txt_primary.text= "Hiii ${username}"

                    if (username!="1") {
                        val sharedPreferences =
                            getSharedPreferences("Username", Context.MODE_PRIVATE)
                        sharedPreferences.edit()
                            .putString("UsernameKey", username).apply()
//                        Toast.makeText(this, "User name ${username}", Toast.LENGTH_SHORT).show()
                        // Set data to views
                    }
                } else {
//                    Toast.makeText(this, "User data not found!", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
//                Toast.makeText(this, "Error fetching data: ${e.message}", Toast.LENGTH_SHORT).show()
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

    private fun disable_Services (){

        stopService(Intent(this, ShakeDetectionService::class.java))
    }

//    fun saveFCMTokenToFirebase() {
//        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
//            if (task.isSuccessful) {
//                val token = task.result
//                val userId = FirebaseAuth.getInstance().currentUser?.uid
//
//                if (userId != null) {
//                    db.collection("users").document(userId).update("fcmToken",token).addOnCompleteListener(){
//                        Log.d("TOKEN", "Token saved successfully: $token")
//                        Toast.makeText(applicationContext, "Token saved", Toast.LENGTH_SHORT).show()
//                    }
//                } else {
//                    Log.e("TOKEN", "User is not authenticated")
//                }
//            } else {
//                Log.e("TOKEN", "Fetching FCM token failed", task.exception)
//            }
//        }
//    }


}

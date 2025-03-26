package com.example.womensafetyapp

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.womensafetyapp.Activities.ChatRoomsActivity
import com.example.womensafetyapp.Activities.GoogleMapActivity
import com.example.womensafetyapp.Activities.UsersActivity

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging

class SignInActivity : AppCompatActivity() {

    private lateinit var et_pass: EditText
    private lateinit var et_email: EditText
    private lateinit var tv_signup: TextView
    private lateinit var btn_signin: Button
    private lateinit var firebaseAuth: FirebaseAuth
    private var db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)
        firebaseAuth = FirebaseAuth.getInstance()

        window.statusBarColor = Color.parseColor("#F5F5F5")

        et_pass = findViewById(R.id.signin_password)
        et_email = findViewById(R.id.signin_email)

        btn_signin = findViewById(R.id.signin_button)
        tv_signup = findViewById(R.id.signup_link)



        tv_signup.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }

        btn_signin.setOnClickListener {
            val email = et_email.text.toString()
            val pass = et_pass.text.toString()

            if (email.isNotEmpty() && pass.isNotEmpty()) {

                firebaseAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener {
                    if (it.isSuccessful) {
                        saveFCMTokenToFirebase()
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)

                    } else {
                        Toast.makeText(this, it.exception.toString(), Toast.LENGTH_SHORT).show()

                    }
                }
            } else {
                Toast.makeText(this, "Empty Fields Are not Allowed !!", Toast.LENGTH_SHORT).show()

            }
        }
    }

    override fun onStart() {
        super.onStart()
        if(firebaseAuth.currentUser != null){
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }
    fun saveFCMTokenToFirebase() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                val userId = FirebaseAuth.getInstance().currentUser?.uid

                if (userId != null) {
                    db.collection("users").document(userId).update("fcmToken",token).addOnCompleteListener(){
                        Log.d("TOKEN", "Token saved successfully: $token")
                        Toast.makeText(applicationContext, "Token saved", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.e("TOKEN", "User is not authenticated")
                }
            } else {
                Log.e("TOKEN", "Fetching FCM token failed", task.exception)
            }
        }
    }

}
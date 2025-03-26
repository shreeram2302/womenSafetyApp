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
import com.example.womensafetyapp.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SignUpActivity : AppCompatActivity() {
    private lateinit var et_number: EditText
    private lateinit var et_username: EditText
    private lateinit var et_email: EditText
    private lateinit var et_pass: EditText
    private lateinit var btn_confirm: EditText
    private lateinit var btn_signup: Button
    private lateinit var tv_signin: TextView
//    private lateinit var number: String

    //    private lateinit var progressBar: ProgressBar
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)
        window.statusBarColor = Color.parseColor("#F5F5F5")


        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        et_number = findViewById(R.id.number)
        et_username = findViewById(R.id.username)
        et_email = findViewById(R.id.signup_email)
        et_pass = findViewById(R.id.signup_password)
        btn_confirm = findViewById(R.id.confirm_pass)
        btn_signup = findViewById(R.id.signup_button)
        tv_signin = findViewById(R.id.signin_link)


        tv_signin.setOnClickListener {
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
        }
        btn_signup.setOnClickListener {
            val email = et_email.text.toString()
            val pass = et_pass.text.toString()
            val username = et_username.text.toString()
            val number = et_number.text.toString()
            val confirmPass = btn_confirm.text.toString()

            if (email.isNotEmpty() && pass.isNotEmpty() && confirmPass.isNotEmpty()) {
                if (pass == confirmPass) {
                    firebaseAuth.createUserWithEmailAndPassword(email, pass)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val userId =
                                    firebaseAuth.currentUser?.uid ?: return@addOnCompleteListener
                                val user = User(userId, username, email,number)

                                // Save user to Firestore
                                firestore.collection("users").document(userId)
                                    .set(user)
                                    .addOnSuccessListener {
                                        Log.d("SignUpActivity", "User added to Firestore")
                                        Toast.makeText(
                                            this,
                                            "Signup successful!",
                                            Toast.LENGTH_SHORT
                                        ).show()

                                        // Navigate to SignInActivity
                                        val intent = Intent(this, SignInActivity::class.java)
                                        startActivity(intent)
                                        finish()
                                    }
                                    .addOnFailureListener { e ->
                                        Log.e("SignUpActivity", "Error adding user to Firestore", e)
                                        Toast.makeText(
                                            this,
                                            "Error saving user data!",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                            } else {
                                Toast.makeText(this, task.exception?.message, Toast.LENGTH_SHORT)
                                    .show()
                                Log.e("SignUpActivity", "Signup failed", task.exception)
                            }
                        }
                } else {
                    Toast.makeText(this, "Passwords do not match!", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Empty fields are not allowed!", Toast.LENGTH_SHORT).show()
            }
        }


    }

}
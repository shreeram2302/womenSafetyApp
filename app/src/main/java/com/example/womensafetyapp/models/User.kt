package com.example.womensafetyapp.models

data class User(
    val userId: String = "",   // Firestore UID
    val username: String = "",
    val email: String = "",     // Add email field
    val phoneNumber: String = "",
    val FCMToken: String = ""
)

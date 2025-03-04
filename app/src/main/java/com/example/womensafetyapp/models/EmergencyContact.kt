package com.example.womensafetyapp.models

data class EmergencyContact(
    val name: String = "",
    val phone: String = "",
    val uid: String = "" // Store the contact's UID to enable messaging
)

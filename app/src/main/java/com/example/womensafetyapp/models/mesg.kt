package com.example.womensafetyapp.models

data class mesg(
    val chatId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val text: String? = "",
    val fileUrl: String? = null,  // Add file URL field
    val type: String = "text",    // Add message type field (text, image, audio)
    val timestamp: Long = 0
)

package com.example.womensafetyapp.models

data class ChatRoom(
    val chatId: String = "",
    val chatName: String = "",
    val participants: List<String> = emptyList()
)

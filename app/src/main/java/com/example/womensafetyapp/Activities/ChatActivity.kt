package com.example.womensafetyapp.Activities

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.womensafetyapp.Adapters.ChatAdapter
import com.example.womensafetyapp.R
import com.example.womensafetyapp.models.Message
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions

class ChatActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ChatAdapter
    private lateinit var etMessage: EditText
    private lateinit var btnSend: Button

    private val messageList = ArrayList<Message>()
    private val db = FirebaseFirestore.getInstance()
    private var chatId: String? = null
    private var currentUserId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        recyclerView = findViewById(R.id.recyclerMessages)
        etMessage = findViewById(R.id.etMessage)
        btnSend = findViewById(R.id.btnSend)

        chatId = intent.getStringExtra("chatId")
        currentUserId = FirebaseAuth.getInstance().currentUser?.uid

        adapter = ChatAdapter(messageList)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        btnSend.setOnClickListener { sendMessage(chatId.toString(),etMessage.text.toString()) }

        if (chatId != null) {
            fetchMessages()
        }
    }

    fun sendMessage(chatId: String, message: String) {
        etMessage.text.clear()
        val chatRef = FirebaseFirestore.getInstance().collection("chats").document(chatId)

        chatRef.get().addOnSuccessListener { document ->
            if (!document.exists()) {
                // 🔹 If chat doesn't exist, create it first
                val receiverId = intent.getStringExtra("receiverId") // Get receiverId from Intent

                val chatData = hashMapOf(
                    "participants" to mapOf(
                        currentUserId to true,
                        receiverId to true  // ✅ Add receiver
                    ),
                    "createdAt" to System.currentTimeMillis()
                )


                chatRef.set(chatData, SetOptions.merge()).addOnSuccessListener {
                    Log.d("Firestore", "Chat created successfully")

                    sendChatMessage(chatRef, message)

                }.addOnFailureListener { e ->
                    Log.e("FirestoreError", "Chat creation failed: ${e.message}")
                }

            } else {
                // 🔹 If chat exists, directly send the message
                sendChatMessage(chatRef, message)
            }
        }.addOnFailureListener { e ->
            Log.e("FirestoreError", "Failed to check chat existence: ${e.message}")
        }
    }

    fun sendChatMessage(chatRef: DocumentReference, message: String) {
        val messageData = hashMapOf(
            "senderId" to FirebaseAuth.getInstance().currentUser?.uid,
            "text" to message,
            "timestamp" to System.currentTimeMillis()
        )

        chatRef.collection("messages").add(messageData)
            .addOnSuccessListener {
                Log.d("Firestore", "Message sent successfully")

            }
            .addOnFailureListener { e ->
                Log.e("FirestoreError", "Message sending failed: ${e.message}")
            }
    }



    private fun fetchMessages() {
        db.collection("chats").document(chatId!!)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshots, _ ->
                if (snapshots != null) {
                    messageList.clear()
                    for (doc in snapshots.documents) {
                        val message = doc.toObject(Message::class.java)
                        if (message != null) {
                            messageList.add(message)
                        }
                    }
                    adapter.notifyDataSetChanged()
                    recyclerView.scrollToPosition(messageList.size - 1)
                }
            }
    }
}

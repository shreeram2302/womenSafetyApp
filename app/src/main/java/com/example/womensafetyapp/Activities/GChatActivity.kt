package com.example.womensafetyapp.Activities

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.womensafetyapp.Adapters.GChatAdapter
import com.example.womensafetyapp.R
import com.example.womensafetyapp.models.mesg
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions

import android.location.Location
import android.widget.TextView

import com.google.android.gms.location.*

import java.util.*

class GChatActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: GChatAdapter
    private lateinit var etMessage: EditText
    private lateinit var tv_name: TextView

    private lateinit var btnSend: Button
    private lateinit var btnLoc: Button
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val messageList = ArrayList<mesg>()
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var chatId: String? = null
    private var chatName: String? = null
    private var currentUserId: String? = null
    private var emergencyContacts: List<String>? = null  // ✅ Emergency contacts

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
// intent get
        chatId = intent.getStringExtra("chatId")
        chatName = intent.getStringExtra("chatName")




        recyclerView = findViewById(R.id.recyclerMessages)
        etMessage = findViewById(R.id.etMessage)
        btnSend = findViewById(R.id.btnSend)
        btnLoc = findViewById(R.id.btnLoc)

        tv_name = findViewById(R.id.tv_name)
        tv_name.setText(chatName)

        currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        emergencyContacts = intent.getStringArrayListExtra("emergencyContacts") // ✅ Get emergency contacts

        adapter = GChatAdapter(messageList)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
        btnSend.setOnClickListener { sendMessage(chatId.toString(), etMessage.text.toString()) }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        btnLoc.setOnClickListener {

            val intent = Intent(this,GoogleMapActivity::class.java)
            intent.putExtra("location",currentUserId.toString())
            startActivity(intent)
        }

        if (chatId != null) {
            fetchMessages()
        }
    }


    fun sendMessage(chatId: String, message: String) {
        etMessage.text.clear()
        val chatRef = db.collection("chats").document(chatId)

        chatRef.get().addOnSuccessListener { document ->
            if (!document.exists()) {
                // 🔹 If chat doesn't exist, create it
                val receiverId = intent.getStringExtra("receiverId") // ✅ Get receiverId from Intent

                // ✅ Include emergency contacts in chat
                val allParticipants = emergencyContacts?.toMutableList() ?: mutableListOf()
                allParticipants.add(currentUserId!!)
                allParticipants.add(receiverId!!)

                val chatData = hashMapOf(
                    "participants" to allParticipants.distinct(),  // ✅ Add emergency contacts
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
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            // Fetch username from Firestore
            FirebaseFirestore.getInstance().collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    val senderName = document.getString("username") ?: "Unknown"  // Get sender's username

                    val messageData = hashMapOf(
                        "chatId" to chatRef.id,
                        "senderId" to userId,
                        "senderName" to senderName,  // 🔥 Add username to message
                        "text" to message,
                        "timestamp" to System.currentTimeMillis()
                    )

                    chatRef.collection("messages").add(messageData)
                        .addOnSuccessListener {
                            Log.d("Firestore", "Message sent successfully with sender name")
                        }
                        .addOnFailureListener { e ->
                            Log.e("FirestoreError", "Message sending failed: ${e.message}")
                        }
                }
                .addOnFailureListener { e ->
                    Log.e("FirestoreError", "Failed to fetch sender name: ${e.message}")
                }
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
                        val message = doc.toObject(mesg::class.java)
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
//    fun sendChatMessage(chatRef: DocumentReference, message: String) {
//        val messageData = hashMapOf(
//            "chatId" to chatRef.id,  // ✅ Associate message with chat room
//            "senderId" to FirebaseAuth.getInstance().currentUser?.uid,
//            "text" to message,
//            "timestamp" to System.currentTimeMillis()
//        )
//
//        chatRef.collection("messages").add(messageData)
//            .addOnSuccessListener {
//                Log.d("Firestore", "Message sent successfully")
//            }
//            .addOnFailureListener { e ->
//                Log.e("FirestoreError", "Message sending failed: ${e.message}")
//            }
//    }

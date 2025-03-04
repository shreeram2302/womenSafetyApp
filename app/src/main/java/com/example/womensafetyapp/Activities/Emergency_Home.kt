package com.example.womensafetyapp.Activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.womensafetyapp.Adapters.EmergencyContactsAdapter
import com.example.womensafetyapp.R
import com.example.womensafetyapp.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class Emergency_Home : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var adapter: EmergencyContactsAdapter
    private val emergencyContactsList = ArrayList<User>()
    private val db = FirebaseFirestore.getInstance()
    private lateinit var btnRecentChats: Button

    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_emergency_home)
        btnRecentChats = findViewById(R.id.btnRecentChats)
        recyclerView = findViewById(R.id.recyclerEmergencyContacts)
        progressBar = findViewById(R.id.pgBar1)
        adapter = EmergencyContactsAdapter(emergencyContactsList) { user ->
            openChat(user)
        }
        println(emergencyContactsList)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        fetchEmergencyContacts()
    }

    private fun fetchEmergencyContacts() {
        progressBar.visibility = View.VISIBLE

        currentUserId?.let { uid ->
            db.collection("users").document(uid).collection("emergency_contacts")
                .get()
                .addOnSuccessListener { documents ->
                    emergencyContactsList.clear()
                    for (doc in documents) {
                        val user = doc.toObject(User::class.java).copy(userId = doc.id)
                        emergencyContactsList.add(user)
                    }
                    adapter.notifyDataSetChanged()
                    progressBar.visibility = View.GONE
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    progressBar.visibility = View.GONE
                }
        }
    }

    private fun openChat(user: User) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val receiverId = user.userId

        // Create a unique chatId based on user IDs (sorted to maintain consistency)
        val chatId = if (currentUserId < receiverId) "$currentUserId-$receiverId" else "$receiverId-$currentUserId"

        val intent = Intent(this, ChatActivity::class.java).apply {
            putExtra("chatId", chatId)
            putExtra("receiverId", receiverId)
            putExtra("receiverName", user.username)
        }
        startActivity(intent)
    }


    private fun goToChatActivity(chatId: String, receiverName: String) {
        val intent = Intent(this, ChatActivity::class.java).apply {
            putExtra("chatId", chatId)
            putExtra("receiverName", receiverName)
        }
        startActivity(intent)
    }


}

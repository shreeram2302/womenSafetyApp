package com.example.womensafetyapp.Activities

import android.app.AlertDialog
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.womensafetyapp.R
import com.example.womensafetyapp.models.ChatRoom
import com.example.womensafetyapp.Adapters.ChatRoomAdapter
import com.example.womensafetyapp.utils.SharedPreferencesHelper.getFromSharedPrefs
import com.example.womensafetyapp.utils.SharedPreferencesHelper.saveEmergencyChatroomIdSharedPrefs
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class ChatRoomsActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ChatRoomAdapter
    private val chatRoomsList = ArrayList<ChatRoom>()
    private val db = FirebaseFirestore.getInstance()
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    private var userId=currentUserId.toString()
    private lateinit var backBtn: ImageView



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_rooms)

        window.statusBarColor = Color.parseColor("#002F6C")


        backBtn = findViewById(R.id.btn_back)
        backBtn.setOnClickListener {
            finish()  // Finishes current activity and moves to the previous one
        }


        recyclerView = findViewById(R.id.recyclerChatRooms)
        val fab = findViewById<FloatingActionButton>(R.id.fabCreateJoinChat)

        adapter = ChatRoomAdapter(chatRoomsList)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        fab.setOnClickListener { showChatRoomDialog() }

        fetchChatRooms()
    }

    private fun fetchChatRooms() {
        currentUserId?.let { uid ->
            db.collection("chats")
                .whereArrayContains("participants", uid)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshots, e ->
                    if (e != null) {
                        Log.e("ChatRooms", "Error fetching chat rooms", e)
                        return@addSnapshotListener
                    }

                    chatRoomsList.clear()
                    snapshots?.forEach { doc ->
                        val chatRoom = doc.toObject(ChatRoom::class.java)
                        chatRoomsList.add(chatRoom)
                    }
                    adapter.notifyDataSetChanged()
                }
        }
    }

    private fun showChatRoomDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_chatroom_options, null)
        val builder = AlertDialog.Builder(this)
            .setView(dialogView)
            .setTitle("Create or Join Chat Room")

        val dialog = builder.create()
        dialog.show()

        val btnCreate = dialogView.findViewById<Button>(R.id.btnCreateChat)
        val btnJoin = dialogView.findViewById<Button>(R.id.btnJoinChat)

        btnCreate.setOnClickListener {
            dialog.dismiss()
            showCreateChatDialog()
        }

        btnJoin.setOnClickListener {
            dialog.dismiss()
            showJoinChatDialog()
        }
    }

    private fun showCreateChatDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_create_chat, null)
        val builder = AlertDialog.Builder(this)
            .setView(dialogView)
            .setTitle("Create Chat Room")

        val dialog = builder.create()
        dialog.show()

//        val etChatId = dialogView.findViewById<EditText>(R.id.etChatId)
        val etChatName = dialogView.findViewById<EditText>(R.id.etChatName)
        val btnCreate = dialogView.findViewById<Button>(R.id.btnConfirmCreate)

        btnCreate.setOnClickListener {
//            val chatId = etChatId.text.toString().trim()
            val chatName = etChatName.text.toString().trim()

//            if (chatId.isEmpty() || chatName.isEmpty()) {
//                Toast.makeText(this, "Please enter all details", Toast.LENGTH_SHORT).show()
//                return@setOnClickListener
//            }

            createChatRoom(chatName)
            dialog.dismiss()
            val rootView = findViewById<View>(android.R.id.content)

            val snackbar = Snackbar.make(rootView, "Group Created", Snackbar.LENGTH_LONG)
            snackbar.setBackgroundTint(resources.getColor(R.color.primaryColor))
            snackbar.setTextColor(resources.getColor(R.color.white))
            snackbar.setActionTextColor(resources.getColor(R.color.red))
            snackbar.setAction("OK") { /* Action here */ }
            snackbar.show()

        }
    }

    private fun createChatRoom(chatName: String) {
        val chatRef = db.collection("chats").document(userId)



        chatRef.get().addOnSuccessListener { document ->
            if (!document.exists()) {
                val chatData = hashMapOf(
                    "chatId" to userId,
                    "chatName" to chatName,
                    "participants" to listOf(currentUserId),  // Store as a list
                    "timestamp" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                )

                chatRef.set(chatData)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Chat Room Created Successfully", Toast.LENGTH_SHORT).show()

                        fetchChatRooms()
                    }
                    .addOnFailureListener { e ->
                        Log.e("Firestore", "Error creating chat room", e)
                        Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, "Chat ID already exists!", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener { e ->
            Log.e("Firestore", "Error checking document", e)
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }



    private fun showJoinChatDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_join_chat, null)
        val builder = AlertDialog.Builder(this)
            .setView(dialogView)
            .setTitle("Join Chat Room")

        val dialog = builder.create()
        dialog.show()

        val etChatId = dialogView.findViewById<EditText>(R.id.etChatIdJoin)
        val btnJoin = dialogView.findViewById<Button>(R.id.btnConfirmJoin)

        btnJoin.setOnClickListener {
            val chatId = etChatId.text.toString().trim()

            if (chatId.isEmpty()) {
                Toast.makeText(this, "Enter Chat ID", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            joinChatRoom(chatId)
            dialog.dismiss()
            val rootView = findViewById<View>(android.R.id.content)

            val snackbar = Snackbar.make(rootView, "Group Joined", Snackbar.LENGTH_LONG)
            snackbar.setBackgroundTint(resources.getColor(R.color.primaryColor))
            snackbar.setTextColor(resources.getColor(R.color.white))
            snackbar.setActionTextColor(resources.getColor(R.color.red))
            snackbar.setAction("OK") { /* Action here */ }
            snackbar.show()

        }
    }


    private fun joinChatRoom(chatId: String) {
        val chatRef = db.collection("chats").document(chatId)

        chatRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                val currentParticipants = document.get("participants") as? List<String> ?: listOf()
                if (!currentParticipants.contains(currentUserId)) {  // Check before updating
                    chatRef.update("participants", com.google.firebase.firestore.FieldValue.arrayUnion(currentUserId))
                        .addOnSuccessListener {
                            Toast.makeText(this, "Joined Chat Room", Toast.LENGTH_SHORT).show()

                            // 🔥 Fetch chat rooms again to update UI immediately
                            fetchChatRooms()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(this, "You are already in the chat room", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Chat Room Not Found!", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener { e ->
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }


}

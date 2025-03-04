package com.example.womensafetyapp.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.womensafetyapp.R
import com.example.womensafetyapp.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class  UsersAdapter(private var userList: ArrayList<User>) :
    RecyclerView.Adapter<UsersAdapter.UserViewHolder>() {

    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    private val db = FirebaseFirestore.getInstance()
    private val emergencyContacts = mutableSetOf<String>()

    init {
        fetchEmergencyContacts()
    }

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvUsername: TextView = itemView.findViewById(R.id.tvUsername)
        private val tvEmail: TextView = itemView.findViewById(R.id.tvEmail)
        private val btnAddEmergency: Button = itemView.findViewById(R.id.btnAddEmergency)

        fun bind(user: User) {
            tvUsername.text = user.username
            tvEmail.text = user.email

            val isAdded = emergencyContacts.contains(user.userId)
            btnAddEmergency.text = if (isAdded) "Remove" else "Add"

            btnAddEmergency.setOnClickListener {
                if (isAdded) {
                    removeEmergencyContact(user)
                } else {
                    addEmergencyContact(user)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(userList[position])
    }

    override fun getItemCount() = userList.size

    fun updateList(newList: ArrayList<User>) {
        userList = newList
        notifyDataSetChanged()
    }

    private fun fetchEmergencyContacts() {
        currentUserId?.let { uid ->
            db.collection("users").document(uid).collection("emergency_contacts")
                .get()
                .addOnSuccessListener { documents ->
                    emergencyContacts.clear()
                    for (doc in documents) {
                        emergencyContacts.add(doc.id)
                    }
                    notifyDataSetChanged()
                }
        }
    }

    private fun addEmergencyContact(user: User) {
        currentUserId?.let { uid ->
            val contactRef = db.collection("users").document(uid).collection("emergency_contacts").document(user.userId)
            val contactData = mapOf(
                "userId" to user.userId,
                "username" to user.username,
                "email" to user.email,
                "number" to user.phoneNumber
            )

            contactRef.set(contactData).addOnSuccessListener {
                emergencyContacts.add(user.userId)
                notifyDataSetChanged()
            }
        }
    }

    private fun removeEmergencyContact(user: User) {
        currentUserId?.let { uid ->
            val contactRef = db.collection("users").document(uid).collection("emergency_contacts").document(user.userId)

            contactRef.delete().addOnSuccessListener {
                emergencyContacts.remove(user.userId)
                notifyDataSetChanged()
            }
        }
    }
}

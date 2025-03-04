package com.example.womensafetyapp.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.womensafetyapp.R
import com.example.womensafetyapp.models.User

class EmergencyContactsAdapter(
    private val contactList: ArrayList<User>,
    private val onItemClick: (User) -> Unit
) : RecyclerView.Adapter<EmergencyContactsAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvUsername: TextView = view.findViewById(R.id.tvUsernameE)
        private val tvPhone: TextView = view.findViewById(R.id.tvPhone)

        fun bind(user: User) {
            tvUsername.text = user.username
            tvPhone.text = user.phoneNumber
            itemView.setOnClickListener { onItemClick(user) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_emergency_contact, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(contactList[position])
    }

    override fun getItemCount() = contactList.size
}

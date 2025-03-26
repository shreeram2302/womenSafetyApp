package com.example.womensafetyapp.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.womensafetyapp.R

class EmergencyNumbersAdapter(
    private val contacts: List<Pair<String, String>>,
    private val deleteContact: (Pair<String, String>) -> Unit
) : RecyclerView.Adapter<EmergencyNumbersAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_emergency, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val contact = contacts[position]
        holder.nameTextView.text = contact.first
        holder.phoneTextView.text = contact.second

        holder.deleteButton.setOnClickListener {
            deleteContact(contact)
        }
    }

    override fun getItemCount(): Int = contacts.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameTextView: TextView = view.findViewById(R.id.tvContactName)
        val phoneTextView: TextView = view.findViewById(R.id.tvContactPhone)
        val deleteButton: Button = view.findViewById(R.id.btnDelete)
    }
}

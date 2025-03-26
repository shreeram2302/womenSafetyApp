package com.example.womensafetyapp.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.womensafetyapp.R
import com.example.womensafetyapp.models.SafetyTip

class SafetyTipsAdapter(private val tipsList: List<SafetyTip>) :
    RecyclerView.Adapter<SafetyTipsAdapter.SafetyTipViewHolder>() {

    class SafetyTipViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val tipTitle: TextView = view.findViewById(R.id.tipTitle)
        val tipDescription: TextView = view.findViewById(R.id.tipDescription)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SafetyTipViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_safety_tip, parent, false)
        return SafetyTipViewHolder(view)
    }

    override fun onBindViewHolder(holder: SafetyTipViewHolder, position: Int) {
        val tip = tipsList[position]
        holder.tipTitle.text = tip.title
        holder.tipDescription.text = tip.description
    }

    override fun getItemCount() = tipsList.size
}

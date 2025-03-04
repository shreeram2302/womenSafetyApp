package com.example.womensafetyapp.Adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.womensafetyapp.Activities.ChatActivity
import com.example.womensafetyapp.Activities.GChatActivity
import com.example.womensafetyapp.R
import com.example.womensafetyapp.models.ChatRoom

class ChatRoomAdapter(private val chatRooms: List<ChatRoom>) :
    RecyclerView.Adapter<ChatRoomAdapter.ChatRoomViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatRoomViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat_room, parent, false)
        return ChatRoomViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatRoomViewHolder, position: Int) {
        val chatRoom = chatRooms[position]
        holder.bind(chatRoom)
    }

    override fun getItemCount(): Int {
        return chatRooms.size
    }

    class ChatRoomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvChatName: TextView = itemView.findViewById(R.id.tvChatRoomName)
        private val tvChatId: TextView = itemView.findViewById(R.id.tvChatRoomId)

        fun bind(chatRoom: ChatRoom) {
            tvChatName.text = chatRoom.chatName
            tvChatId.text = "ID: ${chatRoom.chatId}"

            itemView.setOnClickListener {
                val context = itemView.context
                val intent = Intent(context, GChatActivity::class.java).apply {
                    putExtra("chatId", chatRoom.chatId)
                    putExtra("chatName", chatRoom.chatName)
                }
                context.startActivity(intent)
            }
        }
    }
    }
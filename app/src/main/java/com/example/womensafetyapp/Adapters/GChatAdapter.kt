package com.example.womensafetyapp.Adapters
import android.content.Context
import android.media.MediaPlayer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.bumptech.glide.Glide
import com.example.womensafetyapp.R
import com.example.womensafetyapp.models.mesg
import com.google.firebase.auth.FirebaseAuth


class GChatAdapter(private val messages: ArrayList<mesg>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
//    private var mediaPlayer: MediaPlayer? = null
    private var isPlaying = false

    companion object {
        private const val VIEW_TYPE_SENT = 1
        private const val VIEW_TYPE_RECEIVED = 2
    }

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].senderId == currentUserId) {
            VIEW_TYPE_SENT  // Sent by the current user
        } else {
            VIEW_TYPE_RECEIVED  // Received from another user
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_SENT) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_chat_sent, parent, false)
            SentMessageViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_chat_received, parent, false)
            ReceivedMessageViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]

        if (holder is SentMessageViewHolder) {
            holder.bind(message)
        } else if (holder is ReceivedMessageViewHolder) {
            holder.bind(message)
        }
    }

    override fun getItemCount() = messages.size

    inner class SentMessageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private var mediaPlayer: MediaPlayer? = null

        private val tvMessage: TextView = view.findViewById(R.id.messageTextView)
        private val imgMessage: ImageView = view.findViewById(R.id.imageMessageView)
        private val btnAudio: LottieAnimationView = view.findViewById(R.id.audioMessageButton)

        fun bind(message: mesg) {

           if (message.type=="text"){
               tvMessage.text=message.text
           }else{
            tvMessage.visibility = View.GONE
           }

            imgMessage.visibility = View.GONE
            btnAudio.visibility = View.GONE

            if (!message.fileUrl.isNullOrEmpty()){
                if (message.type=="image"){
                    imgMessage.visibility = View.VISIBLE
                    Glide.with(itemView.context).load(message.fileUrl).into(imgMessage)
                }else{
                    btnAudio.visibility = View.VISIBLE
                    mediaPlayer = MediaPlayer()
                    mediaPlayer?.setDataSource(message.fileUrl)  // Change path accordingly
                    mediaPlayer?.prepareAsync()
                    btnAudio.setOnClickListener {
//                    mediaPlayer?.release()
//                    mediaPlayer = MediaPlayer().apply {
//                        setDataSource(message.fileUrl)
//                        prepare()
//                        start()
//                    }
                        if (!isPlaying) {
                            // Play Audio
//                            mediaPlayer?.prepare()
                            mediaPlayer?.start()
                            btnAudio.playAnimation() // Play animation
                            isPlaying = true
                        } else {
                            // Pause Audio
                            mediaPlayer?.pause()
                            btnAudio.reverseAnimationSpeed()
                            btnAudio.playAnimation() // Reverse animation to pause
                            isPlaying = false
                        }

                        mediaPlayer?.setOnCompletionListener {
                            btnAudio.reverseAnimationSpeed()
                            btnAudio.playAnimation()
                            isPlaying = false
                        }

                    }
                }

            }
        }
    }

    inner class ReceivedMessageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvMessage: TextView = view.findViewById(R.id.messageTextView)
        private val tvSender: TextView = view.findViewById(R.id.tv_sender)
        private val imgMessage: ImageView = view.findViewById(R.id.imageMessageView)
        private val btnAudio: LottieAnimationView = view.findViewById(R.id.audioMessageButton)
        private var mediaPlayer: MediaPlayer? = null

        fun bind(message: mesg) {
            tvMessage.text = message.text
            tvSender.text = message.senderName
            imgMessage.visibility = View.GONE
            btnAudio.visibility = View.GONE

            if (!message.fileUrl.isNullOrEmpty()){
                if (message.type=="image"){
                    tvMessage.visibility = View.GONE
                    imgMessage.visibility = View.VISIBLE
                    Glide.with(itemView.context).load(message.fileUrl).into(imgMessage)
                }else{
//                    tvMessage.visibility = View.GONE
                    btnAudio.visibility = View.VISIBLE
                    mediaPlayer = MediaPlayer()
                    mediaPlayer?.setDataSource(message.fileUrl)  // Change path accordingly
                    mediaPlayer?.prepareAsync()
                    btnAudio.setOnClickListener {
//                    mediaPlayer?.release()
//                    mediaPlayer = MediaPlayer().apply {
//                        setDataSource(message.fileUrl)
//                        prepare()
//                        start()
//                    }
                        if (!isPlaying) {
                            // Play Audio
//                            mediaPlayer?.prepare()
                            mediaPlayer?.start()
                            btnAudio.playAnimation() // Play animation
                            isPlaying = true
                        } else {
                            // Pause Audio
                            mediaPlayer?.pause()
                            btnAudio.reverseAnimationSpeed()
                            btnAudio.playAnimation() // Reverse animation to pause
                            isPlaying = false
                        }

                        mediaPlayer?.setOnCompletionListener {
                            btnAudio.reverseAnimationSpeed()
                            btnAudio.playAnimation()
                            isPlaying = false
                        }

                    }
                }

            }
        }
    }
}

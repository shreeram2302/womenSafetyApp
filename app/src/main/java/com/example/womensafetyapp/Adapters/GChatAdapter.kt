package com.example.womensafetyapp.Adapters
import android.content.Context
import android.media.MediaPlayer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
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
        private val img_bg: CardView = view.findViewById(R.id.img_bg)


        fun bind(message: mesg) {
            tvMessage.visibility = View.VISIBLE // Reset visibility before checking type
            imgMessage.visibility = View.GONE
            btnAudio.visibility = View.GONE
            img_bg.visibility = View.GONE

            if (message.type == "text") {
                tvMessage.text = message.text
            } else {
                tvMessage.visibility = View.GONE
            }

            if (!message.fileUrl.isNullOrEmpty()) {
                if (message.type == "image") {
                    img_bg.visibility = View.VISIBLE
                    imgMessage.visibility = View.VISIBLE
                    Glide.with(itemView.context)
                        .load(message.fileUrl)
                        .apply(RequestOptions().transform(CenterCrop(), RoundedCorners(20))) // CenterCrop ensures correct rounding
                        .into(imgMessage)
                } else { // It's an audio message
                    btnAudio.visibility = View.VISIBLE

                    mediaPlayer = MediaPlayer().apply {
                        setDataSource(message.fileUrl)
                        prepareAsync()
                    }

                    btnAudio.setOnClickListener {
                        if (!isPlaying) {
                            mediaPlayer?.start()

                            // Start looping animation
                            btnAudio.repeatCount = LottieDrawable.INFINITE
                            btnAudio.playAnimation()

                            isPlaying = true
                        } else {
                            mediaPlayer?.pause()

                            // Stop animation when button is clicked again
                            btnAudio.cancelAnimation()
                            btnAudio.progress = 0f // Reset to start frame

                            isPlaying = false
                        }

                        // Stop animation when audio completes
                        mediaPlayer?.setOnCompletionListener {
                            btnAudio.cancelAnimation()
                            btnAudio.progress = 0f // Reset animation
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
        private val img_bg: CardView = view.findViewById(R.id.img_bg)

        private var mediaPlayer: MediaPlayer? = null

        fun bind(message: mesg) {
            tvSender.text=message.senderName
            tvMessage.visibility = View.VISIBLE // Reset visibility before checking type
            imgMessage.visibility = View.GONE
            btnAudio.visibility = View.GONE
            img_bg.visibility = View.GONE

            if (message.type == "text") {
                tvMessage.text = message.text

            } else {
                tvMessage.visibility = View.GONE
            }

            if (!message.fileUrl.isNullOrEmpty()) {
                if (message.type == "image") {
                    img_bg.visibility = View.VISIBLE
                    imgMessage.visibility = View.VISIBLE
                    Glide.with(itemView.context)
                        .load(message.fileUrl)
                        .apply(RequestOptions().transform(CenterCrop(), RoundedCorners(20))) // CenterCrop ensures correct rounding
                        .into(imgMessage)
                } else { // It's an audio message
                    btnAudio.visibility = View.VISIBLE
                    mediaPlayer = MediaPlayer().apply {
                        setDataSource(message.fileUrl)
                        prepareAsync()
                    }

                    btnAudio.setOnClickListener {
                        if (!isPlaying) {
                            mediaPlayer?.start()

                            // Start looping animation
                            btnAudio.repeatCount = LottieDrawable.INFINITE
                            btnAudio.playAnimation()

                            isPlaying = true
                        } else {
                            mediaPlayer?.pause()

                            // Stop animation when button is clicked again
                            btnAudio.cancelAnimation()
                            btnAudio.progress = 0f // Reset to start frame

                            isPlaying = false
                        }

                        // Stop animation when audio completes
                        mediaPlayer?.setOnCompletionListener {
                            btnAudio.cancelAnimation()
                            btnAudio.progress = 0f // Reset animation
                            isPlaying = false
                        }
                    }

                }
            }
        }

    }

}

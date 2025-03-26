package com.example.womensafetyapp.utils

import android.util.Log
import android.widget.Toast
import com.example.womensafetyapp.models.Notification
import com.example.womensafetyapp.models.NotificationData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FCM_send_notification{
private val db = FirebaseFirestore.getInstance()

    fun initParticipants(chatRef: DocumentReference,senderName: String,message: String){
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        chatRef.get().addOnSuccessListener { chatDoc ->
            val participants = chatDoc.get("participants") as? List<String> ?: return@addOnSuccessListener
            val recipients = participants.filter { it != userId } // Exclude sender

            recipients.forEach { participantId ->
                db.collection("users").document(participantId).get()
                    .addOnSuccessListener { document ->
                        if (document.exists()) {
                            val fcmToken = document.getString("fcmToken")
//                            Log.e("FCM", "FCM token not found for user: $fcmToken")


                            if (!fcmToken.isNullOrBlank()) {
                                sendNotification(fcmToken, senderName, message)
                            } else {
                                Log.e("FCM", "FCM token not found for user: $participantId")
//                                Toast.makeText(
//                                    this,
//                                    "FCM token not found for user: $participantId",
//                                    Toast.LENGTH_SHORT
//                                ).show()
                            }
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e("FCM", "Error fetching FCM token", e)
                    }
            }
        }
    }

    fun sendNotification(fcmToken: String, senderName: String, message: String) {
        val notificationData = NotificationData(
            token = fcmToken,
            data = hashMapOf(
                "title" to "New Message from $senderName",
                "body" to message
            )
        )

        val notification = Notification(message = notificationData)

        val api = NotificationApi.create()
        val call = api.sendNotification(notification, "Bearer ${AccessToken.getAccessToken()}")

        call.enqueue(object : Callback<Notification> {
            override fun onResponse(call: Call<Notification>, response: Response<Notification>) {
                if (response.isSuccessful) {
                    Log.d("FCM", "Notification sent successfully")

                } else {
                    Log.e("FCM", "Failed to send notification: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<Notification>, t: Throwable) {
                Log.e("FCM", "Error sending notification: ${t.message}")
            }
        })
    }
}

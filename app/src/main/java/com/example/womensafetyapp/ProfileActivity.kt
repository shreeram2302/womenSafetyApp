package com.example.womensafetyapp

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64

import android.os.Bundle
import android.provider.MediaStore
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.makeramen.roundedimageview.RoundedImageView
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

class ProfileActivity : AppCompatActivity() {
    private lateinit var tvUsername: TextView
    private lateinit var tvUsername1: TextView
    private lateinit var tvEmail: TextView
    private lateinit var tvNumber: TextView
    private lateinit var tvChatID: TextView
    private lateinit var ImgProfile: RoundedImageView
    private lateinit var editProfileImage: ImageView
    private lateinit var backBtn: ImageView
    private lateinit var share: LinearLayout
    private val db = FirebaseFirestore.getInstance()  // Firestore instance
    private val auth = FirebaseAuth.getInstance()
    private var currentUserId: String? = null


    private val PICK_IMAGE_REQUEST = 1
    private val FILE_NAME = "profile_image.jpg"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)
        currentUserId = auth.currentUser?.uid ?: return

        tvUsername = findViewById(R.id.tvUsername)
        tvUsername1 = findViewById(R.id.tv_hey)
        tvEmail = findViewById(R.id.tvGmail)
        tvNumber = findViewById(R.id.tvNumber)
        tvChatID = findViewById(R.id.tvChatID)
        ImgProfile = findViewById(R.id.profileImage)
        editProfileImage = findViewById(R.id.editProfileImage)
        share = findViewById(R.id.share)
        fetchUserData()

        backBtn = findViewById(R.id.btn_back)
        backBtn.setOnClickListener {
            finish()  // Finishes current activity and moves to the previous one
        }

        editProfileImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }

        val savedBitmap = getImageFromSharedPrefs(this)
        if (savedBitmap != null) {
            ImgProfile.setImageBitmap(savedBitmap)
        }
        share.setOnClickListener {
            val textToShare = "${tvChatID.text.toString()}"

            // Create an intent to share text
            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, textToShare)
                type = "text/plain"
            }

            // Show the chooser dialog
            startActivity(Intent.createChooser(shareIntent, "Share via I'm Safe"))
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            val selectedImageUri = data.data
            selectedImageUri?.let {
                ImgProfile.setImageURI(it) // Show the selected image
//                saveImageToInternalStorage(it) // Save image to internal storage
                val bitmap = uriToBitmap(it)

                // Save image to SharedPreferences
                saveImageToSharedPrefs(this, bitmap)


            }
        }
    }
    fun uriToBitmap(uri: Uri): Bitmap {
        return MediaStore.Images.Media.getBitmap(contentResolver, uri)
    }


    fun saveImageToSharedPrefs(context: Context, bitmap: Bitmap) {
        val sharedPreferences = context.getSharedPreferences("ProfileImg", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        // Convert Bitmap to Base64 String
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        val encodedImage = Base64.encodeToString(byteArray, Base64.DEFAULT)

        // Save Base64 String to SharedPreferences
        editor.putString("profile_image", encodedImage)
        editor.apply()
    }

    fun getImageFromSharedPrefs(context: Context): Bitmap? {
        val sharedPreferences = context.getSharedPreferences("ProfileImg", Context.MODE_PRIVATE)
        val encodedImage = sharedPreferences.getString("profile_image", null)

        return if (encodedImage != null) {
            val byteArray = Base64.decode(encodedImage, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
        } else {
            null  // No image found
        }
    }
//
//    // Save Image to Internal Storage
//    private fun saveImageToInternalStorage(uri: Uri) {
//        try {
//            val inputStream = contentResolver.openInputStream(uri)
//            val file = File(filesDir, FILE_NAME)
//            val outputStream = FileOutputStream(file)
//            inputStream?.copyTo(outputStream)
//            inputStream?.close()
//            outputStream.close()
////            Toast.makeText(this, "Image Saved", Toast.LENGTH_SHORT).show()
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//    }
//
//    // Load Image from Internal Storage
//    private fun loadImageFromStorage() {
//        try {
//            val file = File(filesDir, FILE_NAME)
//            if (file.exists()) {
//                val bitmap = BitmapFactory.decodeFile(file.absolutePath)
//                ImgProfile.setImageBitmap(bitmap)
//            }
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//    }
    private fun fetchUserData() {
        db.collection("users").document(currentUserId!!).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val username = document.getString("username") ?: "N/A"
                    val email = document.getString("email") ?: "N/A"
                    val number = document.getString("phoneNumber") ?: "N/A"
                    val chatId = document.getString("userId") ?: "N/A"

                    // Set data to views
                    tvUsername1.text = "Heyyy!! $username"
                    tvUsername.text = "$username"

                    tvEmail.text = "$email"
                    tvNumber.text = "$number"
                    tvChatID.text = "$chatId"
                } else {
//                    Toast.makeText(this, "User data not found!", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
//                Toast.makeText(this, "Error fetching data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
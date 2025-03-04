package com.example.womensafetyapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.womensafetyapp.MainActivity
import com.example.womensafetyapp.R
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
//import com.example.womensafetyapp.firebaseServices.PhoneAuthManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthMissingActivityForRecaptchaException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import java.util.concurrent.TimeUnit


class PhoneAuthActivity : AppCompatActivity() {

//    private lateinit var phoneAuthManager: PhoneAuthManager
    private lateinit var phoneInput: EditText
    private lateinit var sendOtpButton: Button
    private lateinit var number: String
    private lateinit var auth : FirebaseAuth
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_phone_auth)

        init()


        sendOtpButton.setOnClickListener {
           number=phoneInput.text.trim().toString()
            if (number.isNotEmpty()){
                if (number.length==10){
                        number="+91$number"
                    val options = PhoneAuthOptions.newBuilder(auth)
                        .setPhoneNumber(number) // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(this) // Activity (for callback binding)
                        .setCallbacks(callbacks) // OnVerificationStateChangedCallbacks
                        .build()
                    PhoneAuthProvider.verifyPhoneNumber(options)
                }else{
                    Toast.makeText(this,"Invalid Number",Toast.LENGTH_SHORT)
                }
            }else{
                Toast.makeText(this,"Please EnterNumber",Toast.LENGTH_SHORT)
            }
        }



    }
    fun init(){
        auth = FirebaseAuth.getInstance()

        phoneInput = findViewById(R.id.phoneInput)

        sendOtpButton = findViewById(R.id.sendOtpButton)

        progressBar = findViewById(R.id.mProgress)
        progressBar.visibility=View.INVISIBLE
    }
    private fun saveUserToFirestore(phoneNumber: String) {
        // Firebase logic to save user details (phone number) to Firestore
        val user = hashMapOf("phone" to phoneNumber)
        FirebaseFirestore.getInstance().collection("users")
            .document(FirebaseAuth.getInstance().currentUser!!.uid)
            .set(user)
            .addOnSuccessListener {
                Toast.makeText(this, "User saved to Firestore", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error saving user: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }


    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
//                    Log.d(TAG, "signInWithCredential:success")

                    val user = task.result?.user
                } else {
                    // Sign in failed, display a message and update the UI
//                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        // The verification code entered was invalid
                    }
                    // Update UI
                }
            }
    }

  private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            // This callback will be invoked in two situations:
            // 1 - Instant verification. In some cases the phone number can be instantly
            //     verified without needing to send or enter a verification code.
            // 2 - Auto-retrieval. On some devices Google Play services can automatically
            //     detect the incoming verification SMS and perform verification without
            //     user action.
//            Log.d(TAG, "onVerificationCompleted:$credential")
            signInWithPhoneAuthCredential(credential)
        }

        override fun onVerificationFailed(e: FirebaseException) {
            // This callback is invoked in an invalid request for verification is made,
            // for instance if the the phone number format is not valid.


            if (e is FirebaseAuthInvalidCredentialsException) {
                Log.d("TAG","onVerificationFailed: ${e.toString()}")
                // Invalid request
            } else if (e is FirebaseTooManyRequestsException) {
                Log.d("TAG","onVerificationFailed: ${e.toString()}")

                // The SMS quota for the project has been exceeded
            } else if (e is FirebaseAuthMissingActivityForRecaptchaException) {
                Log.d("TAG","onVerificationFailed: ${e.toString()}")

                // reCAPTCHA verification attempted with null Activity
            }

            // Show a message and update the UI
        }

        override fun onCodeSent(
            verificationId: String,
            token: PhoneAuthProvider.ForceResendingToken,
        ) {
            // The SMS verification code has been sent to the provided phone number, we
            // now need to ask the user to enter the code and then construct a credential
            // by combining the code with a verification ID.
//            Log.d(TAG, "onCodeSent:$verificationId")

            // Save verification ID and resending token so we can use them later
                val intent =Intent(this@PhoneAuthActivity,OTPActivity::class.java)
                intent.putExtra("OTP",verificationId)
                intent.putExtra("resendToken",token)
            startActivity(intent)
            progressBar.visibility=View.INVISIBLE
        }
    }
}


package com.example.womensafetyapp.Activities

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.womensafetyapp.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class GoogleMapActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var mMap: GoogleMap
    private var userMarker: Marker? = null
    private var chatId: String? = null  // Replace with actual chatroom ID
    private val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "anonymous"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_google_map)

        chatId = intent.getStringExtra("location")
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        fetchLiveLocation()
    }

    private fun fetchLiveLocation() {
        val locationRef = FirebaseFirestore.getInstance()
            .collection("chats").document(chatId!!)
            .collection("messages").document(chatId!!)

        locationRef.addSnapshotListener { snapshot, _ ->
            if (snapshot != null && snapshot.exists()) {
                val latitude = snapshot.getDouble("latitude") ?: return@addSnapshotListener
                val longitude = snapshot.getDouble("longitude") ?: return@addSnapshotListener

                updateMarker(LatLng(latitude, longitude))
            }
        }
    }

    private fun updateMarker(location: LatLng) {
        if (userMarker == null) {
            userMarker = mMap.addMarker(
                MarkerOptions().position(location)
                    .title("Live Location")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
            )
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))
        } else {
            userMarker!!.position = location
        }
    }
}
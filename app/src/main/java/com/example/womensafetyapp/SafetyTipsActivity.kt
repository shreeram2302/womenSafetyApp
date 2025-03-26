package com.example.womensafetyapp

import android.graphics.Color
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat


import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.womensafetyapp.Adapters.SafetyTipsAdapter
import com.example.womensafetyapp.models.SafetyTip

class SafetyTipsActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SafetyTipsAdapter
    private lateinit var backBtn: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_safety_tips)
        window.statusBarColor = Color.parseColor("#002F6C")
        backBtn = findViewById(R.id.btn_back)
        backBtn.setOnClickListener {
            finish()  // Finishes current activity and moves to the previous one
        }

        recyclerView = findViewById(R.id.recyclerViewSafetyTips)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val safetyTips = listOf(
            SafetyTip( "Enable Location", "Always keep location services on for emergencies."),
            SafetyTip( "Keep Mobile Data On", "Ensure internet is active for emergency alerts."),
            SafetyTip( "Activate Shake Detection", "Enable shake detection for quick alerts."),
            SafetyTip( "Check Battery & Network", "Low battery may affect emergency features."),
            SafetyTip("Add Trusted Contacts", "Save emergency contacts for instant alerts.")
        )

        adapter = SafetyTipsAdapter(safetyTips)
        recyclerView.adapter = adapter
    }
}

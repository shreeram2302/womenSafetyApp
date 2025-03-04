package com.example.womensafetyapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.ContactsContract
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.womensafetyapp.Adapters.EmergencyNumbersAdapter
import com.example.womensafetyapp.utils.SharedPreferencesHelper
import com.google.android.material.floatingactionbutton.FloatingActionButton

class EmergencyContactActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var fabAddContact: FloatingActionButton
    private val emergencyContacts = mutableListOf<Pair<String, String>>()  // Store contacts as (name, phone)

    private val contactPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            openContactPicker()
        } else {
            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_emergency_contact)

        // Initialize views
        recyclerView = findViewById(R.id.rvEmergencyContacts)
        fabAddContact = findViewById(R.id.fabAddContact)

        // Load saved emergency contacts
        loadSavedContacts()

        // Setup RecyclerView with adapter
        recyclerView.layoutManager = LinearLayoutManager(this)
        val adapter = EmergencyNumbersAdapter(emergencyContacts, ::deleteContact)
        recyclerView.adapter = adapter

        // FloatingActionButton click listener
        fabAddContact.setOnClickListener {
            checkPermissionAndPickContacts()
        }
    }

    private fun loadSavedContacts() {
        val savedContacts = SharedPreferencesHelper.loadEmergencyContacts(this)
        emergencyContacts.clear()
        emergencyContacts.addAll(savedContacts)
    }

    private fun checkPermissionAndPickContacts() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            openContactPicker()
        } else {
            contactPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
        }
    }

    private fun openContactPicker() {
        val intent = Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)
        intent.type = ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE
        startActivityForResult(intent, CONTACT_PICKER_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CONTACT_PICKER_REQUEST && resultCode == RESULT_OK) {
            val contactUri = data?.data
            val cursor = contactUri?.let { contentResolver.query(it, null, null, null, null) }
            cursor?.apply {
                if (moveToFirst()) {
                    val nameIndex = getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
                    val phoneNumberIndex = getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)

                    val name = getString(nameIndex)
                    val phoneNumber = getString(phoneNumberIndex)

                    // Add the contact to the list
                    emergencyContacts.add(Pair(name, phoneNumber))

                    // Save the updated contact list
                    SharedPreferencesHelper.saveEmergencyContacts(this@EmergencyContactActivity, emergencyContacts)

                    // Notify the adapter that data has changed
                    recyclerView.adapter?.notifyDataSetChanged()
                }
                close()
            }
        }
    }

    private fun deleteContact(contact: Pair<String, String>) {
        emergencyContacts.remove(contact)
        SharedPreferencesHelper.saveEmergencyContacts(this, emergencyContacts)
        recyclerView.adapter?.notifyDataSetChanged()
    }



    companion object {
        const val CONTACT_PICKER_REQUEST = 1
    }
}

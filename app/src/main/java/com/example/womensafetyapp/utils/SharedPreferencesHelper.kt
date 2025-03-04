package com.example.womensafetyapp.utils

import android.content.Context
import android.content.SharedPreferences

object SharedPreferencesHelper {

    private const val PREF_NAME = "emergency_contacts_pref"
    private const val CONTACTS_KEY = "emergency_contacts"

    fun saveEmergencyContacts(context: Context, contacts: List<Pair<String, String>>) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()

        // Convert the list of contacts to a list of strings, where each string is "name|phone"
        val contactsList = contacts.map { "${it.first}|${it.second}" }
        editor.putStringSet(CONTACTS_KEY, contactsList.toSet())
        editor.apply()
    }

    fun loadEmergencyContacts(context: Context): List<Pair<String, String>> {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val contactsSet = prefs.getStringSet(CONTACTS_KEY, emptySet()) ?: emptySet()

        // Convert the list of strings back to a list of Pair<String, String>
        return contactsSet.map { contact ->
            val parts = contact.split("|")
            Pair(parts[0], parts[1])
        }
    }

    fun saveEmergencyChatroomIdSharedPrefs(context: Context, key: String, value: String) {
        val sharedPreferences: SharedPreferences = context.getSharedPreferences("ChatRoomID", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString(key, value)
        editor.apply() // Asynchronously saves data
    }

    fun getFromSharedPrefs(context: Context, key: String): String? {
        val sharedPreferences: SharedPreferences = context.getSharedPreferences("ChatRoomID", Context.MODE_PRIVATE)
        return sharedPreferences.getString(key, null) // Default value is null if key is not found
    }

}

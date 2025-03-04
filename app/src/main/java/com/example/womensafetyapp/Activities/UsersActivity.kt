package com.example.womensafetyapp.Activities

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.womensafetyapp.Adapters.UsersAdapter
import com.example.womensafetyapp.R
import com.example.womensafetyapp.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class UsersActivity : AppCompatActivity() {
    private lateinit var userAdapter: UsersAdapter
    private var userList: ArrayList<User> = ArrayList()
    private lateinit var searchView: EditText
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var btnEmergencyContacts: Button

    private val db = FirebaseFirestore.getInstance()
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_users)

        initViews()
        setupRecyclerView()
        setupSearchView()
        fetchUsers()

        btnEmergencyContacts.setOnClickListener{
            val intent= Intent(this,Emergency_Home::class.java)
            startActivity(intent)
        }
    }

    private fun initViews() {
        searchView = findViewById(R.id.searchView)
        btnEmergencyContacts = findViewById(R.id.btn_openEmergency)
        recyclerView = findViewById(R.id.recyclerView)
        progressBar = findViewById(R.id.progressBar)
    }

    private fun setupRecyclerView() {
        userAdapter = UsersAdapter(userList)
        recyclerView.apply {
            adapter = userAdapter
            layoutManager = LinearLayoutManager(this@UsersActivity)
        }
    }

    private fun setupSearchView() {
        searchView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                filterUsers(s.toString())
            }
        })
    }

    private fun fetchUsers() {
        progressBar.visibility = View.VISIBLE

        db.collection("users")
            .get()
            .addOnSuccessListener { documents ->
                userList.clear()
                for (document in documents) {
                    val user = document.toObject(User::class.java).copy(userId = document.id)

                    // Exclude current user
                    if (user.userId != currentUserId) {
                        userList.add(user)
                    }
                }
                userAdapter.updateList(userList)
                progressBar.visibility = View.GONE
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error fetching users: ${exception.message}", Toast.LENGTH_LONG).show()
                progressBar.visibility = View.GONE
            }
    }


    private fun filterUsers(searchText: String) {
        val filteredList = userList.filter {
            it.username.contains(searchText, ignoreCase = true) ||
                    it.phoneNumber.contains(searchText)
        }
        userAdapter.updateList(ArrayList(filteredList))
    }
}

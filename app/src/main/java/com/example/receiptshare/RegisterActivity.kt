package com.example.receiptshare

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.example.receiptshare.Models.User
import com.example.receiptshare.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class RegisterActivity : AppCompatActivity() {
    private lateinit var  auth: FirebaseAuth

    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        // Write a message to the database
        val myRef = DbConn.dbRef.getReference("message")

        myRef.setValue("Hello, World!")
        Log.d("ProfileFragment", "Hello world sent to DB)")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth= FirebaseAuth.getInstance()
    }
    fun register(view: View){
        val email = binding.editTextEmailAddress.text.toString()
        val password = binding.editTextPassword.text.toString()
        val username = binding.editTextUserName.text.toString()

        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if(task.isSuccessful){
                // Get the user's UID
                val userId = auth.currentUser?.uid

                // Create a user object
                val user = User(userId ?: "", email, username)



                // Add the user object to the database
                userId?.let {
                    DbConn.usersRef.child(it).setValue(user).addOnCompleteListener {
                        if (it.isSuccessful) {
                            Log.d("RegisterActivity", "User added to DB")
                        } else {
                            Log.d("RegisterActivity", "Failed to add user to DB")
                        }
                    }
                }

                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }.addOnFailureListener { exception ->
            Toast.makeText(applicationContext, exception.localizedMessage, Toast.LENGTH_LONG).show()
        }
    }
    fun goToLogin(view: View){
        val intent= Intent(this,LoginActivity::class.java)
        startActivity(intent)
    }
}
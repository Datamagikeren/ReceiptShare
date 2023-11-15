package com.example.receiptshare.Repositories

import androidx.lifecycle.MutableLiveData
import com.example.receiptshare.DbConn
import com.example.receiptshare.Models.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class UsersRepo {

    val usernameLiveData: MutableLiveData<String> = MutableLiveData()

    fun fetchUsername(userId: String) {
        DbConn.usersRef.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val username = dataSnapshot.child("username").value.toString()
                usernameLiveData.postValue(username)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle possible errors here
            }
        })
    }
    // Inside your ViewModel
    fun updateUserData(users: List<User>, newItemId: String, priceDivided: Double, groupId: String) {
        // The code provided goes here...
    }



}
package com.example.receiptshare.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth

class HomeViewModel : ViewModel() {

    private var auth: FirebaseAuth

    private val _text = MutableLiveData<String>().apply {
        value = "This is home Fragment"
    }
    private val _email = MutableLiveData<String>().apply{
        auth = FirebaseAuth.getInstance()
        value = auth.currentUser?.email.toString()
    }
    val text: LiveData<String> = _text
    val email: LiveData<String> = _email
}
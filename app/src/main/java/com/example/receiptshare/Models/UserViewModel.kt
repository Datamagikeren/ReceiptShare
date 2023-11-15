package com.example.receiptshare.Models

import androidx.lifecycle.ViewModel
import com.example.receiptshare.Repositories.UsersRepo

class UserViewModel : ViewModel() {

    private val repo = UsersRepo()

    fun fetchUsername(userId: String){
        repo.fetchUsername(userId)
    }

}
package com.example.receiptshare.ui.groupInfo

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.receiptshare.Models.User
import com.example.receiptshare.Repositories.GroupsRepo

class GroupInfoViewModel : ViewModel() {

    private val repo = GroupsRepo.instance
    val users: LiveData<List<User>> = repo.users

    fun fetchUsersForGroup(groupId: String){
        repo.fetchUsersForGroup(groupId)
    }
}
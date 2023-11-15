package com.example.receiptshare.Models

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.receiptshare.Repositories.GroupsRepo
import com.example.receiptshare.SingleLiveEvent
import com.google.android.gms.tasks.Task

class GroupsViewModel : ViewModel() {

    private val repo = GroupsRepo.instance

    val CRUDGroupResultMessageLiveData: SingleLiveEvent<String> = repo.CRUDGroupResultMessageLiveData
    val groupsLiveData: MutableLiveData<List<Group>> = repo.groupsLiveData
    val usersInGroupLiveData: MutableLiveData<List<User>> = repo.usersInGroupLiveData

    fun getUsersInGroup(groupId: String, callback: (List<String>) -> Unit) {
        repo.getUsersInGroup(groupId, callback)
    }
    fun getUsersInGroup(groupId: String){
        repo.getUsersInGroup(groupId)
    }

    fun addGroup(groupName: String) {
        repo.addGroup(groupName)
            .addOnSuccessListener {
                val successMessage = "Successfully added group"
                CRUDGroupResultMessageLiveData.postValue(successMessage)
            }
            .addOnFailureListener {
                CRUDGroupResultMessageLiveData.postValue("Failed to add group")
            }
    }

    fun getAllGroupsForUser() {
        repo.getAllGroupsForUser()
    }

    fun deleteGroup(groupId: String) {
        Log.d("DeleteGroup", "Delete group triggered in viewModel")
        repo.deleteGroup(groupId).addOnSuccessListener {
            Log.d("DeleteGroup", "Delete group triggered successfully from Firebase")
            CRUDGroupResultMessageLiveData.postValue("Successfully deleted group")
        }
            .addOnFailureListener {
                CRUDGroupResultMessageLiveData.postValue("Failed to delete group")
                Log.d("deleteResult", "Failed to delete")
            }
    }

    fun addUserToGroupByUserName(groupId: String, username: String) {
        repo.addUserToGroupByUserName(groupId, username).addOnSuccessListener {
            CRUDGroupResultMessageLiveData.postValue("Successfully added user to group")
        }
            .addOnFailureListener {
                CRUDGroupResultMessageLiveData.postValue("Failed to add user to group")
            }
    }
}
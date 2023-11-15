package com.example.receiptshare.Repositories

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.receiptshare.DbConn
import com.example.receiptshare.Models.Group
import com.example.receiptshare.Models.User
import com.example.receiptshare.SingleLiveEvent
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class GroupsRepo private constructor() {

    private val auth = FirebaseAuth.getInstance()
    val groupsLiveData: MutableLiveData<List<Group>> = MutableLiveData<List<Group>>()
    val usersInGroupLiveData: MutableLiveData<List<User>> = MutableLiveData<List<User>>()
    val CRUDGroupResultMessageLiveData: SingleLiveEvent<String> = SingleLiveEvent()
    private val _users = MutableLiveData<List<User>>()
    val users: LiveData<List<User>> get() = _users



    fun getUsersInGroup(groupId: String, callback: (List<String>) -> Unit) {
        val groupRef = DbConn.groupsRef.child(groupId).child("users")
        groupRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val userIds = dataSnapshot.children.mapNotNull { it.key }
                callback(userIds)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle possible errors.
            }
        })
    }

    // Add a new group to the database
    fun addGroup(groupName: String): Task<Void> {
        val groupId = DbConn.groupsRef.push().key  // Generate a unique ID for the group
        val groupData = mapOf(
            "groupName" to groupName,
            "users" to mapOf(auth.currentUser?.uid to true),  // Add the current user as a member
            "groupId" to groupId
        )
        return DbConn.groupsRef.child(groupId!!).setValue(groupData)
    }

    fun getAllGroupsForUser() {
        val userId = auth.currentUser?.uid

        DbConn.groupsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val groupsList = mutableListOf<Group>()

                for (groupSnapshot in dataSnapshot.children) {
                    val group = groupSnapshot.getValue(Group::class.java)
                    if (group?.users?.containsKey(userId) == true) {
                        groupsList.add(group)
                    }
                }

                groupsLiveData.postValue(groupsList)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle possible errors.
            }
        })
    }

    fun fetchUsersForGroup(groupId: String) {
        val groupRef = DbConn.groupsRef.child(groupId).child("users")

        groupRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val userIds = dataSnapshot.children.mapNotNull { it.key }
                fetchUsersByIds(userIds)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("GroupInfoViewModel", "Error fetching users: ${databaseError.message}")
            }
        })

    }

    private fun fetchUsersByIds(userIds: List<String>) {
        val tasks = userIds.map { userId ->
            DbConn.usersRef.child(userId).get()
        }

        Tasks.whenAllComplete(tasks).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val userList = tasks.mapNotNull { it.result?.getValue(User::class.java) }.toList()
                _users.postValue(userList)
            } else {
                Log.e("GroupInfoViewModel", "Error fetching user details")
            }
        }
    }

    fun deleteGroup(groupId: String): Task<Void> {
        Log.d("DeleteGroup", "Delete group triggered")
        return DbConn.groupsRef.child(groupId).removeValue()
    }
    fun addUserToGroup(groupId: String, userId: String): Task<Void> {
        // Get a reference to the specific group's users node
        val groupUsersRef = DbConn.groupsRef.child(groupId).child("users")

        // Update the users node with the new user's ID
        return groupUsersRef.child(userId).setValue(true)
    }

    fun addUserToGroupByUserName(groupId: String, username: String): Task<Void> {
        // First, fetch the user's ID using their username
        val usersRef = DbConn.usersRef.orderByChild("username").equalTo(username)

        val taskCompletionSource = TaskCompletionSource<Void>()
        val task = taskCompletionSource.task

        usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Assuming each user has a unique username, there should be only one child
                    val userSnapshot = dataSnapshot.children.iterator().next()
                    val userId = userSnapshot.key

                    if (userId != null) {
                        // Get a reference to the specific group's users node
                        val groupUsersRef = DbConn.groupsRef.child(groupId).child("users")

                        // Update the users node with the user's ID
                        groupUsersRef.child(userId).setValue(true).addOnCompleteListener { taskResult ->
                            if (taskResult.isSuccessful) {
                                taskCompletionSource.setResult(null)
                            } else {
                                taskCompletionSource.setException(taskResult.exception!!)
                            }
                        }
                    } else {
                        taskCompletionSource.setException(Exception("User ID not found"))
                    }
                } else {
                    taskCompletionSource.setException(Exception("Username not found"))
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                taskCompletionSource.setException(databaseError.toException())
            }
        })

        return task
    }
    fun getUsersInGroup(groupId: String) {
        val usersRef = DbConn.groupsRef.child(groupId).child("users")

        usersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val usersList = mutableListOf<User>()

                for (userSnapshot in dataSnapshot.children) {
                    val user = userSnapshot.getValue(User::class.java)
                    user?.let {
                        usersList.add(it)
                    }
                }

                usersInGroupLiveData.postValue(usersList)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle possible errors.
            }
        })
    }

    fun assignItemDividedPrice(itemId: String, itemPriceDivided: Double){
        val itemId = itemId
        val itemPriceDivided = itemPriceDivided
        val assignMap = mapOf(
            "itemId" to itemId,
            "itemPriceDivided" to itemPriceDivided
        )


    }


    companion object {
        // Lazy initialization of the singleton instance
        val instance: GroupsRepo by lazy { GroupsRepo() }
    }

}
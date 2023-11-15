package com.example.receiptshare.Repositories

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.example.receiptshare.DbConn
import com.example.receiptshare.Models.Group
import com.example.receiptshare.Models.GroupsViewModel
import com.example.receiptshare.Models.Item
import com.example.receiptshare.Models.User
import com.example.receiptshare.ui.groupInfo.GroupInfoViewModel
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlin.concurrent.thread

class ItemsRepo  {

    val usersLiveData = GroupsRepo.instance.users
    private val groupsInfo = GroupsViewModel()
    val receiptsRepo = ReceiptsRepo.instance.receiptsLiveData
    private val groupInfoRepo = GroupsRepo.instance

    val itemsLiveData: MutableLiveData<List<Item>> = MutableLiveData<List<Item>>()
    private val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser!!.uid

    val usersInItemLiveData: MutableLiveData<List<User>> = MutableLiveData<List<User>>()

    fun addItem(receiptId: String, groupId: String, itemName: String, itemPrice: Double): String {
        // Generate a random item ID
        val itemId =
            DbConn.groupsRef.child(groupId).child("receipts").child(receiptId).child("items")
                .push().key
                ?: throw IllegalStateException("Failed to generate a new item ID.")

        val itemUserDistribution = mutableMapOf<String, Double>()

        // Create a new item object
        val newItem = Item(itemId, receiptId, itemName, itemPrice, itemUserDistribution)

        val priceDivided = itemPrice / groupInfoRepo.users.value!!.count()

        // Add the item to the database under the specified receiptId and return the itemId
        DbConn.groupsRef.child(groupId).child("receipts").child(receiptId).child("items")
            .child(itemId).setValue(newItem)
        groupInfoRepo.fetchUsersForGroup(groupId)
        val itemRef = DbConn.groupsRef.child(groupId).child("receipts").child(receiptId).child("items").child(newItem.itemId).child("userItemDistribution")
        val receiptRef = DbConn.groupsRef.child(groupId).child("receipts").child(receiptId).child("userReceiptDistribution")
        val groupDistRef = DbConn.groupsRef.child(groupId).child("userGroupDistribution")
        groupInfoRepo.users.value?.let { users ->
            for (user in users) {
                val itemUserDistribution = hashMapOf<String, Any>(user.userId to priceDivided)
                itemRef.updateChildren(itemUserDistribution)

                receiptsRepo.value?.let{receipts ->
                    for (receipt in receipts){
                        receipt.userReceiptDistribution?.get(user.userId) ?: 0.0
                        val userReceiptDistribution = receipt.userReceiptDistribution ?: emptyMap()

                        val updates = mutableMapOf<String, Any>()
                        for (user in users) {
                            val newAmount = (userReceiptDistribution[user.userId] ?: 0.0) + priceDivided
                            updates["${user.userId}"] = newAmount
                        }
                        receiptRef.updateChildren(updates)
                    }
                }
                groupInfoRepo.groupsLiveData.value?.let{groups ->
                    for (group in groups){
                        group.userGroupDistribution?.get(user.userId) ?: 0.0
                        val userGroupDistribution = group.userGroupDistribution ?: emptyMap()
                        val updates = mutableMapOf<String, Any>()
                        for (user in users) {
                            val newAmount = (userGroupDistribution[user.userId] ?: 0.0) + priceDivided
                            updates["${user.userId}"] = newAmount
                        }
                        groupDistRef.updateChildren(updates)
                    }
                }
            }
        }
                return newItem.itemId
    }

    fun addItemBulk(receiptId: String, groupId: String, itemsJson: String) {
        // Parse the JSON string to a List of Item objects
        val gson = Gson()
        val itemType = object : TypeToken<List<Item>>() {}.type
        val itemsList: List<Item> = gson.fromJson(itemsJson, itemType)

        // Iterate through each item and add it to the database
        itemsList.forEach { item ->
            addItem(receiptId, groupId, item.name, item.price)


        }

    }
    fun getAllItemsForReceipt(receiptId: String, groupId: String) {
        val receiptItemsRef =
            DbConn.groupsRef.child(groupId).child("receipts").child(receiptId).child("items")
        receiptItemsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val itemsList = mutableListOf<Item>()
                for (itemSnapshot in dataSnapshot.children) {
                    val item = itemSnapshot.getValue(Item::class.java)
                    item?.let {
                        itemsList.add(it)
                    }
                }
                itemsLiveData.postValue(itemsList)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle possible errors.
            }
        })
    }

    fun recalculateAndDistributeItemCost(groupId: String, receiptId: String, itemId: String) {
        val itemRef =
            DbConn.groupsRef.child(groupId).child("receipts").child(receiptId).child("items")
                .child(itemId)

        itemRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val item = snapshot.getValue(Item::class.java)
                val itemCost = item?.price ?: 0.0
                distributeItemCostAmongUsers(groupId, receiptId, itemId, itemCost)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ReceiptFragment", "Error fetching item details: ${error.message}")
            }
        })
    }

    private fun distributeItemCostAmongUsers(
        groupId: String,
        receiptId: String,
        itemId: String,
        itemCost: Double
    ) {
        val usersRef =
            DbConn.groupsRef.child(groupId).child("receipts").child(receiptId).child("items")
                .child(itemId).child("userItemDistribution")
        usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val userCount = snapshot.childrenCount
                val costPerUser = itemCost / userCount
                val updatedCosts = mutableMapOf<String, Double>()
                val oldCosts = mutableMapOf<String, Double>()

                snapshot.children.forEach { userSnapshot ->
                    val userId = userSnapshot.key!!
                    val oldUserCost = userSnapshot.getValue(Double::class.java) ?: 0.0
                    oldCosts[userId] = oldUserCost
                }

                snapshot.children.forEach { userSnapshot ->
                    val userId = userSnapshot.key
                    val userItemDistributionRef = usersRef.child(userId.toString())
                    userItemDistributionRef.setValue(costPerUser)
                    userId?.let { updatedCosts[it] = costPerUser }
                }
                updateUserReceiptDistribution(groupId, receiptId, updatedCosts, oldCosts)
                updateUserGroupDistribution(groupId, updatedCosts, oldCosts)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ReceiptFragment", "Error fetching users: ${error.message}")
            }
        })




    }

    fun updateUserItemDistribution(
        groupId: String,
        receiptId: String,
        itemId: String,
        userId: String,
        included: Boolean
    ) {
        val userItemDistributionRef = DbConn.groupsRef
            .child(groupId)
            .child("receipts")
            .child(receiptId)
            .child("items")
            .child(itemId)
            .child("userItemDistribution")
            .child(userId)

        val userReceiptDistributionRef = DbConn.groupsRef
            .child(groupId)
            .child("receipts")
            .child(receiptId)
            .child("userReceiptDistribution")
            .child(userId)

        val userGroupDistRef = DbConn.groupsRef.child(groupId).child("userGroupDistribution").child(userId)

        if (included) {
            userItemDistributionRef.setValue(0)
        } else {
            // Fetch the current userItemDistribution amount
            userItemDistributionRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(userItemSnapshot: DataSnapshot) {
                    val currentUserItemDistribution =
                        userItemSnapshot.getValue(Double::class.java) ?: 0.0
                    // Fetch the current userReceiptDistribution amount
                    userReceiptDistributionRef.addListenerForSingleValueEvent(object :
                        ValueEventListener {
                        override fun onDataChange(userReceiptSnapshot: DataSnapshot) {
                            val currentUserReceiptDistribution =
                                userReceiptSnapshot.getValue(Double::class.java) ?: 0.0
                            val newUserReceiptDistribution =
                                currentUserReceiptDistribution - currentUserItemDistribution

                            DbConn.groupsRef
                                .child(groupId)
                                .child("receipts")
                                .child(receiptId).child("userReceiptDistribution").child(userId)
                                .setValue(newUserReceiptDistribution)
                            userReceiptDistributionRef.addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(userReceiptSnapshot: DataSnapshot) {
                                    val currentUserReceiptDistribution =
                                        userReceiptSnapshot.getValue(Double::class.java) ?: 0.0
                                    // Fetch the current userGroupDistribution amount
                                    userGroupDistRef.addListenerForSingleValueEvent(object :
                                        ValueEventListener {
                                        override fun onDataChange(userGroupSnapshot: DataSnapshot) {
                                            val currentUserGroupDistribution =
                                                userGroupSnapshot.getValue(Double::class.java) ?: 0.0
                                            val newUserGroupDistribution =
                                                currentUserGroupDistribution - currentUserItemDistribution

                                            DbConn.groupsRef
                                                .child(groupId).child("userGroupDistribution").child(userId).setValue(newUserGroupDistribution)
                                        }

                                        override fun onCancelled(databaseError: DatabaseError) {
                                            Log.e(
                                                "ReceiptFragment",
                                                "Error fetching user receipt distribution: ${databaseError.message}"
                                            )
                                        }
                                    })
                                }

                                override fun onCancelled(databaseError: DatabaseError) {
                                    Log.e(
                                        "ReceiptFragment",
                                        "Error fetching user item distribution: ${databaseError.message}"
                                    )
                                }
                            })
                        }

                        override fun onCancelled(databaseError: DatabaseError) {
                            Log.e(
                                "ReceiptFragment",
                                "Error fetching user receipt distribution: ${databaseError.message}"
                            )
                        }
                    })
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.e(
                        "ReceiptFragment",
                        "Error fetching user item distribution: ${databaseError.message}"
                    )
                }
            })
            userItemDistributionRef.setValue(null)
        }
    }
    private fun updateUserGroupDistribution(
        groupId: String,
        updatedCosts: Map<String, Double>,
        oldCosts: Map<String, Double>?
    ) {
        val groupDistRef = DbConn.groupsRef.child(groupId).child("userGroupDistribution")
        groupDistRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Read the current distribution
                val currentDistribution = snapshot.children.associate { child ->
                    child.key to (child.getValue(Double::class.java) ?: 0.0)
                }.toMutableMap()

                // Adjust the distribution based on new costs and old costs
                updatedCosts.forEach { (userId, newCost) ->
                    val oldCost = oldCosts?.get(userId) ?: 0.0
                    val currentTotal = currentDistribution[userId] ?: 0.0
                    val newTotal = currentTotal - oldCost + newCost  // Subtract old cost, add new cost
                    currentDistribution[userId] = newTotal
                }
                // Set the updated values back to the database
                currentDistribution.forEach { (userId, newTotal) ->
                    userId?.let {
                        groupDistRef.child(it).setValue(newTotal)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ReceiptFragment", "Error updating user receipt distribution: ${error.message}")
            }
        })
    }

    private fun updateUserReceiptDistribution(
        groupId: String,
        receiptId: String,
        updatedCosts: Map<String, Double>,
        oldCosts: Map<String, Double>?
    ) {
        val receiptDistRef = DbConn.groupsRef.child(groupId).child("receipts").child(receiptId)
            .child("userReceiptDistribution")

        receiptDistRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Read the current distribution
                val currentDistribution = snapshot.children.associate { child ->
                    child.key to (child.getValue(Double::class.java) ?: 0.0)
                }.toMutableMap()

                // Adjust the distribution based on new costs and old costs
                updatedCosts.forEach { (userId, newCost) ->
                    val oldCost = oldCosts?.get(userId) ?: 0.0
                    val currentTotal = currentDistribution[userId] ?: 0.0
                    val newTotal = currentTotal - oldCost + newCost  // Subtract old cost, add new cost
                    currentDistribution[userId] = newTotal
                }
                // If a user has been removed, their old cost needs to be subtracted
                oldCosts?.forEach { (userId, oldCost) ->
                    if (!updatedCosts.containsKey(userId)) {
                        val currentTotal = currentDistribution[userId] ?: 0.0
                        currentDistribution[userId] = currentTotal - oldCost
                    }
                }
                // Set the updated values back to the database
                currentDistribution.forEach { (userId, newTotal) ->
                    userId?.let {
                        receiptDistRef.child(it).setValue(newTotal)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ReceiptFragment", "Error updating user receipt distribution: ${error.message}")
            }
        })

    }
}





package com.example.receiptshare.Repositories

import androidx.lifecycle.MutableLiveData
import com.example.receiptshare.DbConn
import com.example.receiptshare.Models.Item
import com.example.receiptshare.Models.Receipt
import com.google.android.gms.tasks.Task
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class ReceiptsRepo private constructor()  {

    val receiptsLiveData: MutableLiveData<List<Receipt>> = MutableLiveData<List<Receipt>>()

    fun updateReceiptTotalPrice(receiptId: String, groupId: String) {
        val receiptItemsRef =
            DbConn.groupsRef.child(groupId).child("receipts").child(receiptId).child("items")

        receiptItemsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                var total = 0.0
                for (itemSnapshot in dataSnapshot.children) {
                    val item = itemSnapshot.getValue(Item::class.java)
                    item?.let {
                        total += it.price
                    }
                }
                // Update the receipt's total price in the database
                DbConn.groupsRef.child(groupId).child("receipts").child(receiptId).child("total")
                    .setValue(total)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle possible errors.
            }
        })
    }


    fun addReceipt(groupId: String, receiptName: String, payer: String): Task<Void> {
        // Generate a random receipt ID
        val receiptId = DbConn.groupsRef.child(groupId).child("receipts").push().key!!

        val total = 0.0

        // Create a new receipt object
        val newReceipt = Receipt(receiptId, groupId, receiptName, total, payer = payer)

        // Add the receipt to the database under the specified groupId
        return DbConn.groupsRef.child(groupId).child("receipts").child(receiptId)
            .setValue(newReceipt)
    }

    fun getAllReceiptsForGroup(groupId: String) {
        // Reference to the specific group's receipts
        val groupReceiptsRef = DbConn.groupsRef.child(groupId).child("receipts")

        groupReceiptsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val receiptsList = mutableListOf<Receipt>()

                for (receiptSnapshot in dataSnapshot.children) {
                    val receipt = receiptSnapshot.getValue(Receipt::class.java)
                    receipt?.let {
                        receiptsList.add(it)
                    }
                }

                receiptsLiveData.postValue(receiptsList)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle possible errors.
            }
        })
    }
    companion object {
        // Lazy initialization of the singleton instance
        val instance: ReceiptsRepo by lazy { ReceiptsRepo() }
    }
}

package com.example.receiptshare.Models

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.receiptshare.Repositories.ItemsRepo

class ItemsViewModel : ViewModel()  {

    val repo = ItemsRepo()
    val itemsLiveData: MutableLiveData<List<Item>> = repo.itemsLiveData

    fun addItem(receiptId: String, groupId: String, itemName: String, itemPrice: Double): String {
        return repo.addItem(receiptId, groupId, itemName, itemPrice)
    }

    fun addItemBulk(receiptId: String, groupId: String, itemsJson: String){
        repo.addItemBulk(receiptId, groupId, itemsJson)
    }


    fun getAllItemsForReceipt(receiptId: String, groupId: String) {
        repo.getAllItemsForReceipt(receiptId, groupId)
    }
    fun recalculateAndDistributeItemCost(groupId: String, receiptId: String, itemId: String){
        repo.recalculateAndDistributeItemCost(groupId, receiptId, itemId)
    }
    fun updateUserItemDistribution(groupId: String, receiptId: String, itemId: String, userId: String, included: Boolean) {
        repo.updateUserItemDistribution(groupId, receiptId, itemId, userId, included)
    }



}
package com.example.receiptshare.Models

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.receiptshare.Repositories.ReceiptsRepo
import com.example.receiptshare.SingleLiveEvent

class ReceiptsViewModel : ViewModel() {
    private val repo = ReceiptsRepo.instance

    val CRUDReceiptResultMessageLiveData: SingleLiveEvent<String> = SingleLiveEvent()
    val receiptsLiveData: MutableLiveData<List<Receipt>> = repo.receiptsLiveData

    val receiptTotalPriceLiveData = MutableLiveData<Double>()
    fun updateReceiptTotalPrice(receiptId: String, groupId: String) {
        repo.updateReceiptTotalPrice(receiptId, groupId)
    }

    fun addReceipt(groupId: String, receiptName: String, isOwner: String){
        repo.addReceipt(groupId, receiptName, isOwner).addOnSuccessListener {
            val successMessage = "Successfully added receipt"
            CRUDReceiptResultMessageLiveData.postValue(successMessage)
        }
            .addOnFailureListener {
                CRUDReceiptResultMessageLiveData.postValue("Failed to add receipt")
            }
    }

    fun getAllReceiptsForGroup(groupId: String){
        repo.getAllReceiptsForGroup(groupId)
    }


}
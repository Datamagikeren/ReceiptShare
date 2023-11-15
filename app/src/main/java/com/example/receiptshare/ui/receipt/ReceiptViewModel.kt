package com.example.receiptshare.ui.receipt

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.receiptshare.Models.Receipt
import com.example.receiptshare.Repositories.ReceiptsRepo

class ReceiptViewModel : ViewModel() {
    private val repo = ReceiptsRepo.instance
    val receipts: LiveData<List<Receipt>> = repo.receiptsLiveData

}
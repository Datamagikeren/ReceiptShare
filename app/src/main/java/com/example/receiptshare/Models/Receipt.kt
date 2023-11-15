package com.example.receiptshare.Models

data class Receipt(
    val receiptId: String = "",
    val groupId: String = "",
    val receiptName: String = "",
    val total: Double = 0.0,
    val userReceiptDistribution: Map<String, Double>? = null,
    val payer: String = ""
) {


}
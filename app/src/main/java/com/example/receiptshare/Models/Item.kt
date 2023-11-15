package com.example.receiptshare.Models

data class Item(
    val itemId: String = "",
    val receiptId: String = "",
    val name: String = "",
    val price: Double = 0.0,
    val userItemDistribution: MutableMap<String, Double>? = null,
)

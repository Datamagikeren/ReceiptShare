package com.example.receiptshare.Models

data class User(
    var userId: String = "",
    var email: String = "",
    var username: String = "",
    val groups: Map<String, Boolean>? = null, // This map will have groupId as key and 'true' as value for each group the user is a part of.
    val itemsAssigned: Map<String, Double>? = null, // This map will have itemId as key and the value that the user "owes" in the item.
    val receiptStatus: Map<String,Double>? = null,
    val groupStatus: Map<String,Double>? = null,

)
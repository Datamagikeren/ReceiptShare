package com.example.receiptshare.Models

class Group(
    var groupId: String = "",
    var groupName: String = "",
    val users: Map<String, Boolean>? = null, // This map will have groupId as key and 'true' as value for each group the user is a part of.
    val userGroupDistribution: Map<String, Double>? = null
) {

}
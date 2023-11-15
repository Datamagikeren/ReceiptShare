package com.example.receiptshare.Models

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.receiptshare.R

class UserReceiptDistributionAdapter(
    private var users: List<User>,
    private var receiptUserDistribution: Map<String, Double> // This map contains user IDs and the amounts they owe for the receipt.
) : RecyclerView.Adapter<UserReceiptDistributionAdapter.UserReceiptViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserReceiptViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_item_users, parent, false)
        return UserReceiptViewHolder(view)
    }

    override fun getItemCount(): Int = users.size

    override fun onBindViewHolder(holder: UserReceiptViewHolder, position: Int) {
        val user = users[position]
        val userId = user.userId
        val amountOwed = receiptUserDistribution[userId] ?: 0.0
        holder.bind(user, amountOwed)
    }

    class UserReceiptViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvUserName: TextView = itemView.findViewById(R.id.tvUserName)
        private val tvReceiptValue: TextView = itemView.findViewById(R.id.tvItemValue) // Consider renaming the ID to reflect receipt context

        fun bind(user: User, amountOwed: Double) {
            tvUserName.text = user.username
            tvReceiptValue.text = String.format("%.2f", amountOwed) // Formats the amount owed to two decimal places.
        }
    }

    fun updateData(newUsers: List<User>, newReceiptUserDistribution: Map<String, Double>) {
        users = newUsers
        receiptUserDistribution = newReceiptUserDistribution
        notifyDataSetChanged()
    }
}

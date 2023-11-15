package com.example.receiptshare.Models

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.receiptshare.R

class UserItemDistributionAdapter(
    private var users: List<User>,
    private var itemUserDistribution: MutableMap<String, Double>, // Made this mutable for easy updates.
    private val onUserSwitchToggle: (String, Boolean) -> Unit // Added a callback parameter.
) : RecyclerView.Adapter<UserItemDistributionAdapter.UserItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_item_users, parent, false)
        return UserItemViewHolder(view)
    }

    override fun getItemCount(): Int = users.size

    override fun onBindViewHolder(holder: UserItemViewHolder, position: Int) {
        val user = users[position]
        val userId = user.userId
        val amountOwed = itemUserDistribution[userId] ?: 0.0
        holder.bind(user, amountOwed)

    }

    inner class UserItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvUserName: TextView = itemView.findViewById(R.id.tvUserName)
        private val tvItemValue: TextView = itemView.findViewById(R.id.tvItemValue)
        private val isAssignedToItem: Switch = itemView.findViewById(R.id.isAssignedToItem)

        fun bind(user: User, amountOwed: Double) {
            tvUserName.text = user.username
            tvItemValue.text = String.format("%.2f", amountOwed)
            isAssignedToItem.isChecked = amountOwed > 0

            // Attach a listener to the switch
            isAssignedToItem.setOnCheckedChangeListener { _, isChecked ->
                onUserSwitchToggle(user.userId, isChecked)
                itemUserDistribution[user.userId] = if (isChecked) amountOwed else 0.0
                tvItemValue.text = String.format("%.2f", itemUserDistribution[user.userId] ?: 0.0)
            }
        }
    }

    fun recalculateDistribution(newDistribution: Map<String, Double>) {
        itemUserDistribution.clear()
        itemUserDistribution.putAll(newDistribution)
        notifyDataSetChanged()
    }
}

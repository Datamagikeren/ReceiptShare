package com.example.receiptshare.Models

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import android.widget.TextView
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.RecyclerView
import com.example.receiptshare.R

class UsersAdapter(private var users: List<User>) : RecyclerView.Adapter<UsersAdapter.UserViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_item_users, parent, false)
        return UserViewHolder(view)
    }

    override fun getItemCount(): Int = users.size

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(users[position])
    }

    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvUserName: TextView = itemView.findViewById(R.id.tvUserName)
        private val tvItemValue: TextView = itemView.findViewById(R.id.tvItemValue) // You should have a TextView in your layout for this
        private val isAssignedToItem: Switch = itemView.findViewById(R.id.isAssignedToItem)

        fun bind(user: User) {
            tvUserName.text = user.username
            isAssignedToItem.isChecked = true
            // Get the value associated with the itemId for this user and set it on the TextView
            // val itemValue = item.userItemDistribution?.get(itemId) ?: 0.0
            // tvItemValue.text = itemValue.toString()
        }
    }
    fun updateData(newUsers: List<User>) {
        users = newUsers
        notifyDataSetChanged()
    }
}


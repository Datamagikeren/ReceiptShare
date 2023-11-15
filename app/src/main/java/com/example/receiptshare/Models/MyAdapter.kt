package com.example.receiptshare.Models

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.receiptshare.R

class MyAdapter<T>(
    private val items: MutableList<T>,
    private val onItemClicked: (position: Int) -> Unit,
    private val bindData: (item: T, viewHolder: MyViewHolder) -> Unit
) : RecyclerView.Adapter<MyAdapter.MyViewHolder>() {

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.list_group_card, viewGroup, false)
        return MyViewHolder(view, onItemClicked)
    }

    override fun onBindViewHolder(viewHolder: MyViewHolder, position: Int) {
        val item = items[position]
        bindData(item, viewHolder)
    }

    class MyViewHolder(itemView: View, private val onItemClicked: (position: Int) -> Unit) :
        RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val textView: TextView = itemView.findViewById(R.id.textview_list_item)
        val groupNameTextView: TextView = itemView.findViewById(R.id.textview_group_name)
        val editIcon: ImageView = itemView.findViewById(R.id.edit_icon)

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(view: View) {
            val position = bindingAdapterPosition
            onItemClicked(position)
        }
    }

    fun updateData(newList: List<T>) {
        items.clear()
        items.addAll(newList)
        notifyDataSetChanged()
    }
}

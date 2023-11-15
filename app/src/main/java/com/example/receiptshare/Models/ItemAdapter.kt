package com.example.receiptshare.Models

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.receiptshare.R

class ItemAdapter(
    private val items: MutableList<Item>,
    private val onItemClicked: (position: Int) -> Unit,
    private val bindData: (item: Item, viewHolder: ItemViewHolder) -> Unit
) : RecyclerView.Adapter<ItemAdapter.ItemViewHolder>() {

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.list_item_card, viewGroup, false) // Assuming you have a layout named list_item_card for individual items
        return ItemViewHolder(view, onItemClicked)
    }

    override fun onBindViewHolder(viewHolder: ItemViewHolder, position: Int) {
        val item = items[position]
        bindData(item, viewHolder)
    }

    class ItemViewHolder(itemView: View, private val onItemClicked: (position: Int) -> Unit) :
        RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val nameTextView: TextView = itemView.findViewById(R.id.textview_item_name)
        val priceTextView: TextView = itemView.findViewById(R.id.textview_item_price)

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(view: View) {
            val position = bindingAdapterPosition
            onItemClicked(position)
        }
    }

    fun updateData(newList: List<Item>) {
        items.clear()
        items.addAll(newList)
        notifyDataSetChanged()
    }
}

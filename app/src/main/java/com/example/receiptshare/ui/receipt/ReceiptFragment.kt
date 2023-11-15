package com.example.receiptshare.ui.receipt

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.receiptshare.DbConn
import com.example.receiptshare.MainActivity
import com.example.receiptshare.Models.GroupsViewModel
import com.example.receiptshare.Models.Item
import com.example.receiptshare.Models.ItemAdapter
import com.example.receiptshare.Models.ItemsViewModel
import com.example.receiptshare.Models.Receipt
import com.example.receiptshare.Models.ReceiptsViewModel
import com.example.receiptshare.R
import com.example.receiptshare.databinding.FragmentReceiptBinding
import com.example.receiptshare.ui.groupInfo.GroupInfoViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class ReceiptFragment : Fragment() {

    private val itemsViewModel: ItemsViewModel by activityViewModels()
    private val receiptsViewModel: ReceiptsViewModel by activityViewModels()
    private val groupInfoViewModel: GroupInfoViewModel by activityViewModels()
    private val groupsViewModel: GroupsViewModel by activityViewModels()
    private var _binding: FragmentReceiptBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = FragmentReceiptBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val args: ReceiptFragmentArgs by navArgs()

        val receiptId = args.receiptId
        val receiptName = args.receiptName
        val groupId = args.groupId
        val receiptRef = DbConn.groupsRef.child(groupId).child("receipts").child(receiptId)
        val mainActivity = activity as MainActivity
        val toolbarTitle = mainActivity.findViewById<Toolbar>(R.id.toolbar)

        itemsViewModel.getAllItemsForReceipt(args.receiptId, args.groupId)

        receiptRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val receipt = dataSnapshot.getValue(Receipt::class.java)
                val receiptName = receipt?.receiptName
                toolbarTitle.title = receiptName
                toolbarTitle.isClickable = true
            }
            override fun onCancelled(databaseError: DatabaseError) {
                // Handle possible errors.
            }
        })
        val itemsRecyclerView: RecyclerView = view.findViewById(R.id.recyclerView_items)

        val linearLayoutManager = LinearLayoutManager(context)
        linearLayoutManager.reverseLayout = true
        linearLayoutManager.stackFromEnd = true
        itemsRecyclerView.setLayoutManager(linearLayoutManager)

        val itemList = mutableListOf<Item>()
        val itemAdapter = ItemAdapter(itemList, { position ->
            val item = itemList[position]
            val action = ReceiptFragmentDirections.actionReceiptFragmentToItemInfoFragment(
                item.itemId,
                args.receiptId,
                groupId
            )
            findNavController().navigate(action)
        }) { item, viewHolder ->
            viewHolder.nameTextView.text = item.name
            viewHolder.priceTextView.text = item.price.toString()
        }
        itemsRecyclerView.adapter = itemAdapter
        itemsViewModel.itemsLiveData.observe(viewLifecycleOwner) { items ->
            itemAdapter.updateData(items)
            scrollToTop(itemsRecyclerView, itemAdapter.itemCount - 1)
        }

        toolbarTitle?.setOnClickListener {
            val action = ReceiptFragmentDirections.actionReceiptFragmentToReceiptInfoFragment(receiptId, receiptName, groupId)
            findNavController().navigate(action)
        }

        binding.scanReceiptButton.setOnClickListener{
            val action = ReceiptFragmentDirections.actionReceiptFragmentToReceiptScannerFragment(receiptId, groupId)
            findNavController().navigate(action)
        }
        binding.editTextItemPrice.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val priceString = binding.editTextItemPrice.text.toString().trim()
                val priceDouble: Double? = priceString.toDoubleOrNull()
                val itemName = binding.editTextItemName.text.toString().trim()
                if (priceDouble != null) {
                    val item = Item(receiptId, groupId, itemName, priceDouble)
                    val newItemId = itemsViewModel.addItem(receiptId, groupId, itemName, priceDouble)
                    receiptsViewModel.updateReceiptTotalPrice(receiptId, groupId)
                    binding.editTextItemName.text.clear()
                    binding.editTextItemPrice.text.clear()
                    binding.editTextItemName.requestFocus()
                }

                binding.buttonAddItem.performClick() // This simulates a button click
                true
            } else {
                false
            }
        }
    }
    private fun scrollToTop(recyclerView: RecyclerView, position: Int) {
        // If you're using reverse layout to show the latest items at the bottom,
        // then you don't need to reverse it or stack from end to scroll to the top.
        val layoutManager = recyclerView.layoutManager as LinearLayoutManager
        layoutManager.reverseLayout = false
        layoutManager.stackFromEnd = false
        recyclerView.layoutManager = layoutManager
        recyclerView.scrollToPosition(position)

        // If you want to reset to original reverse layout after scrolling, you can set them back to true.
        layoutManager.reverseLayout = true
        layoutManager.stackFromEnd = true
    }
}
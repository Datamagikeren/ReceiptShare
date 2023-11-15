package com.example.receiptshare.ui.receipt

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.receiptshare.Models.ItemsViewModel
import com.example.receiptshare.Models.UserItemDistributionAdapter
import com.example.receiptshare.R
import com.example.receiptshare.databinding.FragmentItemInfoBinding
import com.example.receiptshare.ui.groupInfo.GroupInfoViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class ItemInfoFragment : BottomSheetDialogFragment() {

    private val groupInfoViewModel: GroupInfoViewModel by activityViewModels()
    private val itemsViewModel: ItemsViewModel by activityViewModels()
    private var _binding: FragmentItemInfoBinding? = null
    private val binding get() = _binding!!


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val args: ItemInfoFragmentArgs by navArgs()

        binding.testView.text = args.itemId

        val recyclerViewUsers = view.findViewById<RecyclerView>(R.id.recyclerViewUsersItem)
        recyclerViewUsers.layoutManager = LinearLayoutManager(context)

        itemsViewModel.itemsLiveData.observe(viewLifecycleOwner) { items ->
            val specificItem = items.find { it.itemId == args.itemId }
            val itemDistribution: MutableMap<String, Double> = specificItem?.userItemDistribution!!

            groupInfoViewModel.users.observe(viewLifecycleOwner) { users ->
                val adapter = UserItemDistributionAdapter(users, itemDistribution) { userId, isChecked ->
                   itemsViewModel.updateUserItemDistribution(args.groupId, args.receiptId, args.itemId, userId, isChecked)
                   itemsViewModel.recalculateAndDistributeItemCost(args.groupId, args.receiptId, args.itemId)
                }
                recyclerViewUsers.adapter = adapter
            }
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = FragmentItemInfoBinding.inflate(inflater, container, false)
        return binding.root

    }
}
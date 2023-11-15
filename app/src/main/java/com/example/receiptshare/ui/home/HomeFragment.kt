package com.example.receiptshare.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.receiptshare.Models.Group
import com.example.receiptshare.Models.GroupsViewModel
import com.example.receiptshare.Models.MyAdapter
import com.example.receiptshare.R
import com.example.receiptshare.databinding.FragmentHomeBinding
import com.example.receiptshare.ui.AddGroupBottomSheetFragment


class HomeFragment : Fragment() {

    private val groupsViewModel: GroupsViewModel by activityViewModels()
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        groupsViewModel.CRUDGroupResultMessageLiveData.observe(viewLifecycleOwner) { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }

        groupsViewModel.getAllGroupsForUser()

        val recyclerView: RecyclerView = view.findViewById(R.id.recyclerView_groups)
        recyclerView.layoutManager = LinearLayoutManager(context)
        val groupList = mutableListOf<Group>()
        val adapter = MyAdapter(groupList, { position ->
            val group = groupList[position]
            val action = HomeFragmentDirections.actionNavHomeToGroupFragment(group.groupId)
            findNavController().navigate(action)
        }) { group, viewHolder ->
            viewHolder.textView.text = group.toString()
            viewHolder.groupNameTextView.text = group.groupName
        }
        recyclerView.adapter = adapter
        groupsViewModel.groupsLiveData.observe(viewLifecycleOwner) { groups ->
            adapter.updateData(groups)
        }

        binding.fab.setOnClickListener {
            val bottomSheetFragment = AddGroupBottomSheetFragment()
            bottomSheetFragment.show(childFragmentManager, bottomSheetFragment.tag)
        }


    }

}
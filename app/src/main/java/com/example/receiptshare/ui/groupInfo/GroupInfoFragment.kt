package com.example.receiptshare.ui.groupInfo

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.receiptshare.databinding.FragmentGroupInfoBinding // Import the correct binding class
import com.example.receiptshare.Models.GroupsViewModel
import com.example.receiptshare.Models.UserReceiptDistributionAdapter
import com.example.receiptshare.Models.UsersAdapter
import com.example.receiptshare.R

class GroupInfoFragment : Fragment() {

    private val groupsViewModel: GroupsViewModel by activityViewModels()
    private val groupInfoViewModel: GroupInfoViewModel by activityViewModels()
    private var _binding: FragmentGroupInfoBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentGroupInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val args: GroupInfoFragmentArgs by navArgs()
        val groupId = args.groupId

        val recyclerViewUsers = view.findViewById<RecyclerView>(R.id.recyclerViewUsers)
        binding.recyclerViewUsers.layoutManager = LinearLayoutManager(context)

        groupsViewModel.groupsLiveData.observe(viewLifecycleOwner) { groups ->

            val specificGroup = groups.find { it.groupId == groupId }
            val userReceiptDistribution = specificGroup?.userGroupDistribution ?: emptyMap()

            groupInfoViewModel.users.observe(viewLifecycleOwner) { users ->
                val adapter = UserReceiptDistributionAdapter(users, userReceiptDistribution)
                recyclerViewUsers.adapter = adapter
            }
        }

        groupsViewModel.CRUDGroupResultMessageLiveData.observe(viewLifecycleOwner) { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }

        binding.deleteButton.setOnClickListener {
            // Create an AlertDialog to confirm the deletion
            AlertDialog.Builder(requireContext())
                .setTitle("Delete Group")
                .setMessage("Are you sure you want to delete this group?")
                .setPositiveButton("Yes") { _, _ ->
                    groupsViewModel.deleteGroup(groupId)
                    findNavController().navigate(R.id.action_groupInfo_to_nav_home)
                }
                .setNegativeButton("No", null) // Do nothing when "No" is clicked
                .show()
        }

        binding.addUserButton.setOnClickListener {
            val bottomSheetFragment = AddUserToGroupBottomSheetFragment()
            bottomSheetFragment.arguments = Bundle().apply {
                putString("groupId", groupId)
            }
            bottomSheetFragment.show(childFragmentManager, bottomSheetFragment.tag)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

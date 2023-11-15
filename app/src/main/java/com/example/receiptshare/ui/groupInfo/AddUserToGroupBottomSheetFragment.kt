package com.example.receiptshare.ui.groupInfo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.example.receiptshare.Models.GroupsViewModel
import com.example.receiptshare.databinding.FragmentAddUserToGroupBottomSheetBinding // Import your generated binding class
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class AddUserToGroupBottomSheetFragment : BottomSheetDialogFragment() {

    private val groupsViewModel: GroupsViewModel by activityViewModels()
    private val groupInfoViewModel: GroupInfoViewModel by activityViewModels()
    private var _binding: FragmentAddUserToGroupBottomSheetBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = FragmentAddUserToGroupBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val groupId = arguments?.getString("groupId")

        binding.buttonAddUserToGroup.setOnClickListener {
            val userName = binding.editTextUserName.text.toString().trim()
            if (groupId != null) {
                groupsViewModel.addUserToGroupByUserName(groupId, userName)
                groupInfoViewModel.fetchUsersForGroup(groupId)
                dismiss()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // This is to avoid memory leak
    }
}

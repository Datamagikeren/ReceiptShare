package com.example.receiptshare.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.activityViewModels
import com.example.receiptshare.Models.GroupsViewModel
import com.example.receiptshare.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


class AddGroupBottomSheetFragment : BottomSheetDialogFragment() {
    private lateinit var editTextGroupName: EditText
    private lateinit var buttonAddGroup: Button
    private val groupsViewModel: GroupsViewModel by activityViewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_add_group_bottom_sheet, container, false)

        // Initialize the views
        editTextGroupName = view.findViewById(R.id.editTextGroupName)
        buttonAddGroup = view.findViewById(R.id.buttonAddGroup)

        buttonAddGroup.setOnClickListener{
            val groupName = editTextGroupName.text.toString().trim()
            groupsViewModel.addGroup(groupName)

            dismiss()
        }
        return view
    }

    // Add other necessary code/logic for adding a group here
}

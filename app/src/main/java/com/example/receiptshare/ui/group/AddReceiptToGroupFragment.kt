package com.example.receiptshare.ui.group

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.example.receiptshare.databinding.FragmentAddReceiptToGroupBinding
import com.example.receiptshare.Models.ReceiptsViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth

class AddReceiptToGroupFragment : BottomSheetDialogFragment() {

    private val auth = FirebaseAuth.getInstance()
    private val receiptViewModel: ReceiptsViewModel by activityViewModels()
    private var _binding: FragmentAddReceiptToGroupBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAddReceiptToGroupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val groupId = arguments?.getString("groupId") ?: return // or handle the null case

        val payer = auth.currentUser?.uid

        binding.buttonCreate.setOnClickListener {
            val receiptName = binding.editTextReceiptName.text.toString().trim()
            if (groupId.isNotEmpty() && receiptName.isNotEmpty()) {
                receiptViewModel.addReceipt(groupId, receiptName, payer!!)
                dismiss()
            } else {
                // Handle empty groupId or receiptName
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

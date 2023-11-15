package com.example.receiptshare.ui.receipt

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.receiptshare.MainActivity
import com.example.receiptshare.Models.ReceiptsViewModel
import com.example.receiptshare.Models.UserReceiptDistributionAdapter
import com.example.receiptshare.R
import com.example.receiptshare.databinding.FragmentReceiptBinding
import com.example.receiptshare.databinding.FragmentReceiptInfoBinding
import com.example.receiptshare.ui.groupInfo.GroupInfoViewModel

class ReceiptInfoFragment : Fragment() {

    private val groupInfoViewModel: GroupInfoViewModel by activityViewModels()
    private val receiptsViewModel: ReceiptsViewModel by activityViewModels()
    private var _binding: FragmentReceiptInfoBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentReceiptInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val args: ReceiptInfoFragmentArgs by navArgs()

        val receiptId = args.receiptId

        val mainActivity = activity as MainActivity
        val toolbarTitle = mainActivity.findViewById<Toolbar>(R.id.toolbar)

        toolbarTitle.title = args.receiptName

        val recyclerViewUsers = view.findViewById<RecyclerView>(R.id.recyclerViewUsersReceiptsDistribution)
        recyclerViewUsers.layoutManager = LinearLayoutManager(context)

        receiptsViewModel.receiptsLiveData.observe(viewLifecycleOwner) { receipts ->

                val specificReceipt = receipts.find { it.receiptId == receiptId }
                val userReceiptDistribution = specificReceipt?.userReceiptDistribution ?: emptyMap()

                groupInfoViewModel.users.observe(viewLifecycleOwner) { users ->
                    val adapter = UserReceiptDistributionAdapter(users, userReceiptDistribution)
                    recyclerViewUsers.adapter = adapter
                }
            }
    }
}
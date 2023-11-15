package com.example.receiptshare.ui.group


import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.receiptshare.DbConn
import com.example.receiptshare.MainActivity
import com.example.receiptshare.Models.Group
import com.example.receiptshare.Models.MyAdapter
import com.example.receiptshare.Models.Receipt
import com.example.receiptshare.Models.ReceiptsViewModel
import com.example.receiptshare.R
import com.example.receiptshare.databinding.FragmentGroupBinding
import com.example.receiptshare.ui.groupInfo.GroupInfoViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class GroupFragment : Fragment() {

    private val groupInfoViewModel: GroupInfoViewModel by activityViewModels()
    private val receiptsViewModel: ReceiptsViewModel by activityViewModels()
    private var _binding: FragmentGroupBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentGroupBinding.inflate(inflater, container, false)
        return binding.root

    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val args: GroupFragmentArgs by navArgs()
        val mainActivity = activity as MainActivity
        val groupId = args.groupId
        val groupRef = DbConn.groupsRef.child(groupId)
        val toolbarTitle = mainActivity.findViewById<Toolbar>(R.id.toolbar)

        receiptsViewModel.CRUDReceiptResultMessageLiveData.observe(viewLifecycleOwner) { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }

        receiptsViewModel.getAllReceiptsForGroup(groupId)
        groupInfoViewModel.fetchUsersForGroup(groupId)
        binding.groupId.text = "Group Id: $groupId"

        groupRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val group = dataSnapshot.getValue(Group::class.java)
                val groupName = group?.groupName
                toolbarTitle.title = groupName
                toolbarTitle.isClickable = true
            }
            override fun onCancelled(databaseError: DatabaseError) {
                // Handle possible errors.
            }
        })

        val receiptsRecyclerView: RecyclerView = view.findViewById(R.id.recyclerView_receipts)
        receiptsRecyclerView.layoutManager = LinearLayoutManager(context)
        val receiptList = mutableListOf<Receipt>()
        val receiptsAdapter = MyAdapter(receiptList, { position ->
            val receipt = receiptList[position]
            val action = GroupFragmentDirections.actionGroupFragmentToReceiptFragment(receipt.receiptId, groupId, receipt.receiptName)
            findNavController().navigate(action)
        }) { receipt, viewHolder ->
            viewHolder.groupNameTextView.text = receipt.receiptName
            viewHolder.textView.text = "Total price: ${receipt.total}"
            viewHolder.editIcon.setOnClickListener {
                Log.d("EditButton", "Edit button clicked for ${receipt.receiptName}")
            }
        }

        receiptsRecyclerView.adapter = receiptsAdapter

        receiptsViewModel.receiptsLiveData.observe(viewLifecycleOwner) { receipts ->
            receiptsAdapter.updateData(receipts)
        }

        toolbarTitle?.setOnClickListener {
            val action = GroupFragmentDirections.actionGroupFragmentToGroupInfo(groupId)
            findNavController().navigate(action)
        }

        binding.addReceiptButton.setOnClickListener{
            val bottomSheetFragment = AddReceiptToGroupFragment()
            val bundle = Bundle()
            bundle.putString("groupId", groupId)
            bottomSheetFragment.arguments = bundle
            bottomSheetFragment.show(childFragmentManager, bottomSheetFragment.tag)
        }

    }
    override fun onDestroyView() {
        super.onDestroyView()
        val mainActivity = activity as MainActivity
        val toolbarTitle = mainActivity.findViewById<Toolbar>(R.id.toolbar)
        toolbarTitle.isClickable = false
    }
}
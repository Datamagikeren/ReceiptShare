package com.example.receiptshare.ui.group

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.example.receiptshare.Models.ItemsViewModel
import com.example.receiptshare.Models.ReceiptsViewModel
import com.example.receiptshare.SocketManager
import com.example.receiptshare.databinding.FragmentImagePickerBinding
import com.google.android.material.button.MaterialButton
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import java.io.ByteArrayOutputStream
import java.util.Base64

class ImagePickerFragment : Fragment() {

    private val itemsViewModel: ItemsViewModel by activityViewModels()
    private val receiptsViewModel: ReceiptsViewModel by activityViewModels()

    private lateinit var imageView: ImageView
    private lateinit var pickImage: MaterialButton
    private val storage by lazy { FirebaseStorage.getInstance() }
    private val storageRef by lazy { storage.reference }
    private var _binding: FragmentImagePickerBinding? = null
    private val binding get() = _binding!!
    private lateinit var socketManager: SocketManager
    private val jsonResponseLiveData = MutableLiveData<String>()

    private val launcher: ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri == null) {
                Toast.makeText(activity, "No image Selected", Toast.LENGTH_SHORT).show()
            } else {
                Glide.with(requireContext()).load(uri).into(imageView)
                promptForImageName(uri)
            }
        }
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        // Initialize socketManager here when the Activity is created and context is available
        socketManager = SocketManager("192.168.86.47", 1234)


    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {



        _binding = FragmentImagePickerBinding.inflate(inflater, container, false)
        imageView = binding.imagePickerImageView
        pickImage = binding.pickImage

        pickImage.setOnClickListener { launcher.launch("image/*") }

        binding.connectButton.setOnClickListener {
            Thread(Runnable {
                socketManager.connect()
            }).start()
        }


        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val testjson = "[\n" +
                "  {\"name\": \"PEPSI MAX\", \"price\": \"19.00\"},\n" +
                "  {\"name\": \"RABAT\", \"price\": \"-3.00\"},\n" +
                "  {\"name\": \"PANT\", \"price\": \"3.00\"},\n" +
                "  {\"name\": \"PEPSI MAX\", \"price\": \"19.00\"},\n" +
                "  {\"name\": \"RABAT\", \"price\": \"-3.00\"},\n" +
                "  {\"name\": \"PANT\", \"price\": \"3.00\"},\n" +
                "  {\"name\": \"KIMS SALTEDE MANDLER\", \"price\": \"80.95\"},\n" +
                "  {\"name\": \"RABAT\", \"price\": \"-10.95\"},\n" +
                "  {\"name\": \"1000 STORIES GOLD\", \"price\": \"199.95\"},\n" +
                "  {\"name\": \"RABAT\", \"price\": \"-64.95\"}\n" +
                "]\n"

        // Observe changes to the jsonResponseLiveData
        jsonResponseLiveData.observe(viewLifecycleOwner, Observer { response ->
            binding.responseTextView.text = response
            itemsViewModel.addItemBulk("-NiqOixpVIsG-jK-yHW_", "-NifA6seJ5Ra0ZE717Du", testjson)
        })


        val testReceiptId =
            "-NiqSbaT6PEd-TxTHAm1"
        val testGroupId = "-NifA6seJ5Ra0ZE717Du"
        binding.sendButton.setOnClickListener{
            itemsViewModel.addItemBulk(testReceiptId, testGroupId, testjson)
            receiptsViewModel.updateReceiptTotalPrice(testReceiptId, testGroupId)

        }
    }
    private fun promptForImageName(imageUri: Uri) {
        val input = EditText(context)
        AlertDialog.Builder(requireContext())
            .setTitle("Name Your Image")
            .setMessage("Please enter a name for your image:")
            .setView(input)
            .setPositiveButton("Upload") { dialog, whichButton ->
                val imageName = input.text.toString()
                uploadImage(imageUri, imageName)
            }
            .setNegativeButton("Cancel") { dialog, whichButton -> dialog.dismiss() }
            .show()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun uploadImage(imageUri: Uri, imageName: String) {
        if (imageName.isNotEmpty()) {
            val imageRef = storageRef.child("images/$imageName.jpg")
            val uploadTask: UploadTask = imageRef.putFile(imageUri)

            uploadTask.addOnSuccessListener { taskSnapshot ->
                Toast.makeText(activity, "Image uploaded successfully", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener { e ->
                Toast.makeText(activity, "Upload failed", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(activity, "Image name cannot be empty", Toast.LENGTH_SHORT).show()
        }
        // Add this to convert and send the image after uploading
        val bitmap = if (Build.VERSION.SDK_INT < 28) {
            MediaStore.Images.Media.getBitmap(requireContext().contentResolver, imageUri)
        } else {
            val source = ImageDecoder.createSource(requireContext().contentResolver, imageUri)
            ImageDecoder.decodeBitmap(source)
        }

        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        val byteArray = outputStream.toByteArray()
        val base64Image = Base64.getEncoder().encodeToString(byteArray)
        Log.d("EncodedImage", base64Image)


        Thread {
            try {
                socketManager.sendMessage(base64Image)
                val response = socketManager.receiveMessage()
                activity?.runOnUiThread {
                    val cleanedJsonString = response
                        .replace("```json", "")
                        .replace("```", "")
                        .trim()
                    jsonResponseLiveData.postValue(cleanedJsonString)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                activity?.runOnUiThread {
                    Toast.makeText(activity, "Failed to communicate with server", Toast.LENGTH_SHORT).show()
                }
            } finally {
                socketManager.close() // Close the connection here
            }
        }.start()
    }
}

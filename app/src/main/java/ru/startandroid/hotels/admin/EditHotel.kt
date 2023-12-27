package ru.startandroid.hotels.admin

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import ru.startandroid.hotels.home.HomeScreenActivity
import ru.startandroid.hotels.R
import ru.startandroid.hotels.databinding.ActivityEditHotelBinding

class EditHotel : AppCompatActivity() {

    private lateinit var storageReference: StorageReference
    private lateinit var firebaseFirestore: FirebaseFirestore
    private lateinit var binding: ActivityEditHotelBinding
    private lateinit var idArray: ArrayList<String>
    private var imageUri: Uri? = null
    var hotelId: String = ""
    private var isImageSelected = false
    private var TAG = "EditHotel"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityEditHotelBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initVars()

        binding.activityEditHotelGoBack.setOnClickListener {
            val intentHomeActivity = Intent(this, HomeScreenActivity::class.java)
            startActivity(intentHomeActivity)
            finish()
        }

        binding.activityEditHotelEditHotel.setOnClickListener {
            if (hotelId.isNotEmpty()) {
                editData(hotelId)
            } else {
                Toast.makeText(this, "Выберите отель", Toast.LENGTH_SHORT).show()
            }
        }

        binding.activityEditHotelHotelImage.setOnClickListener {
            isImageSelected = true
            resultLauncher.launch("image/*")
        }

        idArray = ArrayList()
        idArray.add("")
        fillIdList()

        val adapter = ArrayAdapter(this@EditHotel, android.R.layout.simple_spinner_item, idArray)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.activityEditHotelIdList.adapter = adapter
        binding.activityEditHotelIdList.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {}
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    if (parent?.getItemAtPosition(position).toString().isNotEmpty()) {
                        hotelId = parent?.getItemAtPosition(position).toString()
                        getData(hotelId)
                        binding.activityEditHotelHotelName.isEnabled = true
                        binding.activityEditHotelHotelCost.isEnabled = true
                        binding.activityEditHotelFeedbackQuantity.isEnabled = true
                        binding.activityEditHotelHotelGrade.isEnabled = true
                        Log.d(TAG, hotelId)
                    }
                }
            }
    }

    private val resultLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) {
        imageUri = it
        binding.activityEditHotelHotelImage.setImageURI(it)
    }

    private fun initVars() {
        storageReference = FirebaseStorage.getInstance().reference.child("images/")
        firebaseFirestore = FirebaseFirestore.getInstance()
    }

    private fun fillIdList() = CoroutineScope(Dispatchers.IO).launch {
        val hotelQuery = firebaseFirestore.collection("hotels")
            .get().await()
        for (document in hotelQuery.documents) {
            idArray.add(document.id)
        }
    }

    private fun getData(hotelId: String) {
        firebaseFirestore.collection("hotels").document(hotelId)
            .get().addOnSuccessListener { document ->
                with(binding) {
                    activityEditHotelHotelName.setText(document["name"].toString())
                    activityEditHotelHotelCost.setText(document["cost"].toString())
                    activityEditHotelFeedbackQuantity.setText(document["feedbackQuantity"].toString())
                    activityEditHotelHotelGrade.setText(document["grade"].toString())
                }
            }
    }

    private fun editData(hotelId: String) {
        val newHotelName = binding.activityEditHotelHotelName.text.toString()
        val newHotelCost = binding.activityEditHotelHotelCost.text.toString()
        val newFeedbackQuantity = binding.activityEditHotelFeedbackQuantity.text.toString()
        val newHotelGrade = binding.activityEditHotelHotelGrade.text.toString()
        if (newHotelName.isNotEmpty() && newHotelCost.isNotEmpty() && newFeedbackQuantity.isNotEmpty() && newHotelGrade.isNotEmpty() && isImageSelected) {
            val imageName = System.currentTimeMillis().toString()
            storageReference = storageReference.child(imageName)
            imageUri?.let {
                storageReference.putFile(it).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        storageReference.downloadUrl.addOnSuccessListener {
                            val newHotelData = mapOf(
                                "name" to newHotelName,
                                "cost" to newHotelCost.toInt(),
                                "feedbackQuantity" to newFeedbackQuantity.toInt(),
                                "grade" to newHotelGrade.toDouble(),
                                "image" to imageName
                            )
                            firebaseFirestore.collection("hotels").document(hotelId)
                                .update(newHotelData).addOnSuccessListener {
                                    with(binding) {
                                        activityEditHotelHotelName.text.clear()
                                        activityEditHotelHotelCost.text.clear()
                                        activityEditHotelFeedbackQuantity.text.clear()
                                        activityEditHotelHotelGrade.text.clear()
                                        activityEditHotelHotelImage.setImageResource(R.drawable.baseline_add_photo_alternate_24)
                                        activityEditHotelHotelName.isEnabled = false
                                        activityEditHotelHotelCost.isEnabled = false
                                        activityEditHotelFeedbackQuantity.isEnabled = false
                                        activityEditHotelHotelGrade.isEnabled = false
                                        activityEditHotelIdList.setSelection(0)
                                    }
                                    Toast.makeText(this, "Информация обновлена", Toast.LENGTH_SHORT)
                                        .show()
                                    Log.d(TAG, "Data is successfully updated")
                                }
                                .addOnFailureListener { e ->
                                    Log.d(TAG, "Failed to update data ${e.message}")
                                }
                        }
                    } else {
                        Toast.makeText(this, task.exception?.message, Toast.LENGTH_SHORT).show()
                        binding.activityEditHotelHotelImage.setImageResource(R.drawable.baseline_add_photo_alternate_24)
                    }
                }
            }
        } else {
            Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show()
        }
    }
}
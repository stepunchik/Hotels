package ru.startandroid.hotels.admin

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import ru.startandroid.hotels.home.HomeScreenActivity
import ru.startandroid.hotels.databinding.ActivityRemoveHotelBinding

class RemoveHotel : AppCompatActivity() {

    private lateinit var binding: ActivityRemoveHotelBinding
    private lateinit var storageReference: StorageReference
    private lateinit var firebaseFirestore: FirebaseFirestore
    private lateinit var idArray: ArrayList<String>
    var hotelId: String = ""
    var TAG = "RemoveHotel"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRemoveHotelBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initVars()

        binding.activityRemoveHotelGoBack.setOnClickListener {
            val intentHomeActivity = Intent(this, HomeScreenActivity::class.java)
            startActivity(intentHomeActivity)
            finish()
        }

        binding.activityRemoveHotelRemoveHotel.setOnClickListener {
            if (hotelId.isNotEmpty()) {
                removeData(hotelId)
            } else {
                Toast.makeText(this, "Выберите отель", Toast.LENGTH_SHORT).show()
            }
        }

        idArray = ArrayList()
        idArray.add("")
        Log.d(TAG, idArray.toString())
        fillIdList()
        Log.d(TAG, idArray.toString())

        val adapter = ArrayAdapter(this@RemoveHotel, android.R.layout.simple_spinner_item, idArray)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.activityRemoveHotelIdList.adapter = adapter
        binding.activityRemoveHotelIdList.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {

                }

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    if (parent?.getItemAtPosition(position).toString().isNotEmpty()) {
                        hotelId = parent?.getItemAtPosition(position).toString()
                        getData(hotelId)
                        Log.d(TAG, hotelId)
                    }
                }
            }
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
                binding.activityRemoveHotelHotelName.setText(document["name"].toString())
                binding.activityRemoveHotelHotelCost.setText(document["cost"].toString())
                binding.activityRemoveHotelFeedbackQuantity.setText(document["feedbackQuantity"].toString())
                binding.activityRemoveHotelHotelGrade.setText(document["grade"].toString())
            }
    }

    private fun removeData(hotelId: String) = CoroutineScope(Dispatchers.IO).launch {
        val feedbacksGetQuery = firebaseFirestore.collection("feedbacks")
            .whereEqualTo("hotel_id", hotelId)
            .get().await()
        for (feedback in feedbacksGetQuery.documents) {
            firebaseFirestore.collection("feedbacks").document(feedback.id)
                .delete().addOnSuccessListener {
                    Log.d(TAG, "Feedback data is successfully removed")
                }
                .addOnFailureListener { e ->
                    Log.d(TAG, "Failed to remove feedback data: ${e.message}")
                }
        }
        val favouritesGetQuery = firebaseFirestore.collection("favourites")
            .whereEqualTo("hotel_id", hotelId)
            .get().await()
        for (favourite in favouritesGetQuery.documents) {
            firebaseFirestore.collection("favourites").document(favourite.id)
                .delete().addOnSuccessListener {
                    Log.d(TAG, "Favourites data is successfully removed")
                }
                .addOnFailureListener { e ->
                    Log.d(TAG, "Failed to remove favourite data: ${e.message}")
                }
        }
        val bookingGetQuery = firebaseFirestore.collection("booking")
            .whereEqualTo("hotel_id", hotelId)
            .get().await()
        for (booking in bookingGetQuery.documents) {
            firebaseFirestore.collection("booking").document(booking.id)
                .delete().addOnSuccessListener {
                    Log.d(TAG, "Booking data is successfully removed")
                }
                .addOnFailureListener { e ->
                    Log.d(TAG, "Failed to remove booking data: ${e.message}")
                }
        }
        firebaseFirestore.collection("hotels").document(hotelId)
            .delete().addOnSuccessListener {
                with(binding) {
                    binding.activityRemoveHotelHotelName.text.clear()
                    binding.activityRemoveHotelHotelCost.text.clear()
                    binding.activityRemoveHotelFeedbackQuantity.text.clear()
                    binding.activityRemoveHotelHotelGrade.text.clear()
                    binding.activityRemoveHotelIdList.setSelection(0)
                }
                Toast.makeText(this@RemoveHotel, "Информация удалена", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "Data is successfully removed")
            }
            .addOnFailureListener { e ->
                Log.d(TAG, "Failed to remove data ${e.message}")
            }
    }
}
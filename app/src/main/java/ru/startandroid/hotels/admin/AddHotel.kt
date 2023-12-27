package ru.startandroid.hotels.admin

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import ru.startandroid.hotels.home.HomeScreenActivity
import ru.startandroid.hotels.R
import ru.startandroid.hotels.databinding.ActivityAddHotelBinding

class AddHotel : AppCompatActivity() {

    private lateinit var firebaseFirestore: FirebaseFirestore
    private lateinit var storageReference: StorageReference
    private lateinit var binding: ActivityAddHotelBinding
    private var isImageSelected = false
    private var imageUri: Uri? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAddHotelBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initVars()

        binding.activityAddHotelGoBack.setOnClickListener {
            val intentHomeActivity = Intent(this, HomeScreenActivity::class.java)
            startActivity(intentHomeActivity)
            finish()
        }

        binding.activityAddHotelHotelImage.setOnClickListener {
            isImageSelected = true
            resultLauncher.launch("image/*")
        }

        binding.activityAddHotelAddHotel.setOnClickListener {
            setData()
        }
    }

    private val resultLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) {
        imageUri = it
        binding.activityAddHotelHotelImage.setImageURI(it)
    }

    private fun initVars() {
        storageReference = FirebaseStorage.getInstance().reference.child("images/")
        firebaseFirestore = FirebaseFirestore.getInstance()
    }

    private fun setData() {
        val hotelName = binding.activityAddHotelHotelName.text.toString()
        val hotelCost = binding.activityAddHotelHotelCost.text.toString()
        val feedbackQuantity = binding.activityAddHotelFeedbackQuantity.text.toString()
        val hotelGrade = binding.activityAddHotelHotelGrade.text.toString()
        if (hotelName.isNotEmpty() && hotelCost.isNotEmpty() && feedbackQuantity.isNotEmpty() && hotelGrade.isNotEmpty() && isImageSelected) {
            val imageName = System.currentTimeMillis().toString()
            val localStorageReference = storageReference.child(imageName)
            imageUri?.let {
                localStorageReference.putFile(it).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        localStorageReference.downloadUrl.addOnSuccessListener {
                            val hotelData = hashMapOf(
                                "name" to hotelName,
                                "cost" to hotelCost.toInt(),
                                "feedbackQuantity" to feedbackQuantity.toInt(),
                                "grade" to hotelGrade.toDouble(),
                                "image" to imageName
                            )
                            firebaseFirestore.collection("hotels").document()
                                .set(hotelData).addOnCompleteListener { firestoreTask ->
                                    if (firestoreTask.isSuccessful) {
                                        Toast.makeText(
                                            this,
                                            "Информация добавлена",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } else {
                                        Toast.makeText(
                                            this,
                                            firestoreTask.exception?.message,
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                    with(binding) {
                                        activityAddHotelHotelImage.setImageResource(R.drawable.baseline_add_photo_alternate_24)
                                        activityAddHotelHotelName.text.clear()
                                        activityAddHotelHotelCost.text.clear()
                                        activityAddHotelFeedbackQuantity.text.clear()
                                        activityAddHotelHotelGrade.text.clear()
                                    }
                                }
                        }
                    } else {
                        Toast.makeText(this, task.exception?.message, Toast.LENGTH_SHORT).show()
                        binding.activityAddHotelHotelImage.setImageResource(R.drawable.baseline_add_photo_alternate_24)
                    }
                }
            }
        } else {
            Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show()
        }
    }
}

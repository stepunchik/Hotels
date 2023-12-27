package ru.startandroid.hotels.home

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.util.Pair
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SnapHelper
import com.bumptech.glide.Glide
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import ru.startandroid.hotels.R
import ru.startandroid.hotels.databinding.ActivityHotelInfoBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

class HotelInfo : AppCompatActivity() {

    private lateinit var firebaseFirestore: FirebaseFirestore
    private lateinit var binding: ActivityHotelInfoBinding
    private lateinit var storageReference: StorageReference
    private lateinit var feedbackList: ArrayList<Feedback>
    private lateinit var feedbackAdapter: FeedbackAdapter
    private lateinit var userName: String
    private var TAG = "HotelInfo"

    @SuppressLint("NotifyDataSetChanged", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hotel_info)

        binding = ActivityHotelInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initVars()
        getFeedbackData()

        binding.activityHotelInfoDetailed.setOnClickListener {
            closeKeyboard(binding.activityHotelInfoDetailed)
        }

        binding.activityHotelInfoDateCheckInCheckOut.setOnClickListener {
            val picker = MaterialDatePicker.Builder.dateRangePicker()
                .setTheme(R.style.ThemeMaterialCalendar)
                .setTitleText("Заезд - выезд")
                .setSelection(Pair(null, null))
                .build()
            picker.show(this.supportFragmentManager, "PickerShow")
            picker.addOnPositiveButtonClickListener {
                binding.activityHotelInfoDateCheckInCheckOut.setText(
                    convertTimeToDate(it.first) + " - " + convertTimeToDate(
                        it.second
                    )
                )
            }
            picker.addOnNegativeButtonClickListener {
                picker.dismiss()
            }
        }

        val user = FirebaseAuth.getInstance().currentUser
        val userId = user?.uid

        isAlreadyInFavourites(intent.getStringExtra("id"), userId)

        binding.activityHotelInfoAddToFavourites.setOnClickListener {
            if (user != null) {
                addOrDeleteFromFavourites(intent.getStringExtra("id"), userId)
            } else {
                Toast.makeText(this, "Необходимо быть авторизованным", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        binding.activityHotelInfoBookButton.setOnClickListener {
            if (user != null) {
                val enteredPersonQuantity =
                    binding.activityHotelInfoManQuantity.text.toString()
                val enteredCheckInCheckOutDate =
                    binding.activityHotelInfoDateCheckInCheckOut.text.split(" - ")
                if (enteredPersonQuantity.isNotEmpty() && enteredCheckInCheckOutDate.isNotEmpty()) {
                    val bookingData = hashMapOf(
                        "check_in" to enteredCheckInCheckOutDate[0],
                        "check_out" to enteredCheckInCheckOutDate[1],
                        "person_quantity" to enteredPersonQuantity.toInt(),
                        "user_id" to userId,
                        "hotel_id" to intent.getStringExtra("id"),
                        "hotel_name" to intent.getStringExtra("name")
                    )
                    firebaseFirestore.collection("booking").document()
                        .set(bookingData)
                        .addOnSuccessListener {
                            Log.d(
                                TAG,
                                "DocumentSnapshot successfully written!"
                            )
                        }
                        .addOnFailureListener { e -> Log.w(TAG, "Error writing document", e) }
                    Toast.makeText(
                        this,
                        "Стоимость от ${
                            intent.getIntExtra(
                                "cost",
                                0
                            ) * enteredPersonQuantity.toInt() 
                        }",
                        Toast.LENGTH_SHORT
                    ).show()
                    binding.activityHotelInfoManQuantity.text.clear()
                    binding.activityHotelInfoDateCheckInCheckOut.text.clear()
                } else {
                    Toast.makeText(this, "Заполните все поля для бронирования", Toast.LENGTH_SHORT)
                        .show()
                }
            } else {
                Toast.makeText(this, "Необходимо быть авторизованным", Toast.LENGTH_SHORT).show()
            }
        }

        val feedbacksQuery = firebaseFirestore.collection("feedbacks")
            .whereEqualTo("user_id", userId)
            .whereEqualTo("hotel_id", intent.getStringExtra("id"))
            .get()
        binding.activityHotelInfoSendFeedback.setOnClickListener {
            if (user != null) {
                val enteredFeedback =
                    binding.activityHotelInfoFeedbackTextInput.text.toString()
                if (enteredFeedback.isNotEmpty()) {
                    val feedbackData = hashMapOf(
                        "user_id" to userId,
                        "user_name" to userName,
                        "text" to enteredFeedback,
                        "hotel_id" to intent.getStringExtra("id")
                    )
                    if (!feedbacksQuery.result.isEmpty) {
                        for (document in feedbacksQuery.result.documents) {
                            firebaseFirestore.collection("feedbacks")
                                .document(document.id)
                                .set(feedbackData).addOnSuccessListener {
                                    binding.activityHotelInfoFeedbackTextInput.text.clear()
                                    Toast.makeText(
                                        this,
                                        "Текст вашего отзыва обновлен",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                }
                                .addOnFailureListener { e ->
                                    Log.d(TAG, e.message.toString())
                                }
                        }
                    } else {
                        firebaseFirestore.collection("feedbacks").document()
                            .set(feedbackData).addOnSuccessListener {
                                binding.activityHotelInfoFeedbackTextInput.text.clear()
                                Toast.makeText(
                                    this,
                                    "Отзыв добавлен",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            .addOnFailureListener { e ->
                                Log.d(TAG, e.message.toString())
                            }
                    }
                } else {
                    Toast.makeText(
                        this,
                        "Заполните текст отзыва",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                Toast.makeText(this, "Необходимо быть авторизованным", Toast.LENGTH_SHORT).show()
            }
        }

        with(binding) {
            activityHotelInfoHotelName.text = intent.getStringExtra("name")
            activityHotelInfoGrade.text = intent.getDoubleExtra("grade", 0.0).toString()
            activityHotelInfoFeedbackQuantity.text = buildString {
                append(intent.getIntExtra("feedbackQuantity", 0).toString())
                append(" отзывов")
            }

            storageReference =
                FirebaseStorage.getInstance().reference.child("images/" + intent.getStringExtra("image"))
            storageReference.downloadUrl.addOnSuccessListener { uri: Uri ->
                Log.d("HotelAdapter", "$uri")
                Glide.with(activityHotelInfoDetailed).load(uri).into(activityHotelInfoHotelImage)
            }
        }
    }

    private fun initVars() {
        firebaseFirestore = FirebaseFirestore.getInstance()
        binding.activityHotelInfoFeedbacks.setHasFixedSize(true)
        binding.activityHotelInfoFeedbacks.layoutManager =
            LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        val snapHelper: SnapHelper = LinearSnapHelper()
        snapHelper.attachToRecyclerView(binding.activityHotelInfoFeedbacks)
        feedbackList = ArrayList()
        feedbackAdapter = FeedbackAdapter(feedbackList)
        binding.activityHotelInfoFeedbacks.adapter = feedbackAdapter

        val user = FirebaseAuth.getInstance().currentUser
        val docRef = user?.let { firebaseFirestore.collection("users").document(it.uid) }
        docRef?.get()?.addOnSuccessListener { document ->
            if (document != null) {
                userName = document["name"].toString()
                Log.d(TAG, "DocumentSnapshot data: ${document["name"]}")
            } else {
                Log.d(TAG, "No such document")
            }
        }
    }



    @SuppressLint("NotifyDataSetChanged")
    private fun getFeedbackData() {
        val currentHotelId = intent.getStringExtra("id")
        firebaseFirestore.collection("feedbacks")
            .whereEqualTo("hotel_id", currentHotelId)
            .get().addOnSuccessListener {
                for (entry in it) {
                    if (entry != null) {
                        feedbackList.add(
                            Feedback(
                                entry["user_id"].toString(),
                                entry["hotel_id"].toString(),
                                entry["user_name"].toString(),
                                entry["text"].toString()
                            )
                        )
                        Log.d(TAG, "DocumentSnapshot data: ${entry.data}")
                    } else {
                        Log.d(TAG, "No such document")
                    }
                }
                feedbackAdapter.notifyDataSetChanged()
            }
    }

    private fun convertTimeToDate(time: Long): String {
        val utc = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        utc.timeInMillis = time
        val format = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        return format.format(utc.time)
    }

    private fun closeKeyboard(view: View) {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun isAlreadyInFavourites(hotelId: String?, userId: String?) =
        CoroutineScope(Dispatchers.IO).launch {
            val favouritesQuery = firebaseFirestore.collection("favourites")
                .whereEqualTo("hotel_id", hotelId)
                .whereEqualTo("user_id", userId)
                .get()
                .await()
            if (favouritesQuery.documents.isNotEmpty()) {
                binding.activityHotelInfoAddToFavourites.background =
                    ContextCompat.getDrawable(this@HotelInfo, R.drawable.baseline_favorite_24)
            } else {
                binding.activityHotelInfoAddToFavourites.background = ContextCompat.getDrawable(
                    this@HotelInfo,
                    R.drawable.baseline_favorite_border_24
                )
            }
        }

    private fun addOrDeleteFromFavourites(hotelId: String?, userId: String?) =
        CoroutineScope(Dispatchers.IO).launch {
            val favouritesQuery = firebaseFirestore.collection("favourites")
                .whereEqualTo("hotel_id", hotelId)
                .whereEqualTo("user_id", userId)
                .get()
                .await()
            if (favouritesQuery.documents.isNotEmpty()) {
                for (document in favouritesQuery) {
                    try {
                        firebaseFirestore.collection("favourites").document(document.id)
                            .delete().await()
                        Log.d(TAG, "Successfully deleted from favourite")
                        binding.activityHotelInfoAddToFavourites.background =
                            ContextCompat.getDrawable(
                                this@HotelInfo,
                                R.drawable.baseline_favorite_border_24
                            )
                    } catch (e: Exception) {
                        Log.d(TAG, "Failed to delete ${e.message}")
                    }
                }
            } else {
                val favouriteData = hashMapOf(
                    "hotel_id" to hotelId,
                    "user_id" to userId
                )
                firebaseFirestore.collection("favourites").document()
                    .set(favouriteData).await()
                Log.d(TAG, "Successfully added to favourite")
                binding.activityHotelInfoAddToFavourites.background =
                    ContextCompat.getDrawable(this@HotelInfo, R.drawable.baseline_favorite_24)
            }
        }
}
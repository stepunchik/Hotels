package ru.startandroid.hotels.account

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import ru.startandroid.hotels.R
import ru.startandroid.hotels.databinding.ActivityShowBookingBinding
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ShowBooking : AppCompatActivity() {

    private lateinit var binding: ActivityShowBookingBinding
    private lateinit var firebaseFirestore: FirebaseFirestore
    private lateinit var bookingList: ArrayList<Booking>
    private lateinit var bookingAdapter: BookingAdapter
    private lateinit var firebaseAuth: FirebaseAuth
    private var TAG = "ShowBooking"

    @SuppressLint("NotifyDataSetChanged")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_booking)

        binding = ActivityShowBookingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initVars()
        getBookingData(firebaseAuth.currentUser?.uid.toString())

        bookingAdapter.onDeleteItemClick = {
            for (booking in bookingList) {
                if (it.id == booking.id) {
                    bookingList.remove(booking)
                    firebaseFirestore.collection("booking").document(booking.id.toString())
                        .delete().addOnSuccessListener {
                            Log.d(TAG, "Successfully deleted from booking list")
                        }
                        .addOnFailureListener { e ->
                            Log.d(TAG, "Failed to delete ${e.message}")
                        }
                    break
                }
            }
            bookingAdapter.notifyDataSetChanged()
        }
    }

    private fun initVars() {
        firebaseFirestore = FirebaseFirestore.getInstance()
        firebaseAuth = FirebaseAuth.getInstance()
        binding.activityShowBookingRecyclerView.setHasFixedSize(true)
        binding.activityShowBookingRecyclerView.layoutManager = LinearLayoutManager(this)
        bookingList = ArrayList()
        bookingAdapter = BookingAdapter(bookingList)
        binding.activityShowBookingRecyclerView.adapter = bookingAdapter
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun convertStringToTimestamp(date: String): LocalDate? {
        return LocalDate.parse(date, DateTimeFormatter.ofPattern("dd.MM.yyyy"))
    }

    @SuppressLint("NotifyDataSetChanged")
    @RequiresApi(Build.VERSION_CODES.O)
    private fun getBookingData(userId: String?) {
        firebaseFirestore.collection("booking")
            .whereEqualTo("user_id", userId)
            .get().addOnSuccessListener {
                for (entry in it) {
                    if (entry != null) {
                        val checkIn = convertStringToTimestamp(entry["check_in"].toString())
                        val checkOut = convertStringToTimestamp(entry["check_out"].toString())
                        bookingList.add(
                            Booking(
                                entry.id,
                                entry["hotel_name"].toString(),
                                entry["person_quantity"].toString().toInt(),
                                checkIn,
                                checkOut,
                                entry["user_id"].toString(),
                                entry["hotel_id"].toString()
                            )
                        )
                        Log.d(TAG, "DocumentSnapshot data: ${entry.data}")
                    } else {
                        Log.d(TAG, "No such document")
                    }
                }
                bookingAdapter.notifyDataSetChanged()
            }
    }
}
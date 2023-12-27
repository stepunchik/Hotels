package ru.startandroid.hotels.home

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import ru.startandroid.hotels.account.AccountInfo
import ru.startandroid.hotels.admin.AdminPanel
import ru.startandroid.hotels.databinding.ActivityHomeScreenBinding

class HomeScreenActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeScreenBinding
    private lateinit var firebaseFirestore: FirebaseFirestore
    private lateinit var hotelList: ArrayList<Hotel>
    private lateinit var hotelAdapter: HotelAdapter
    private lateinit var firebaseAuth: FirebaseAuth
    private var isAdmin = false
    private var TAG = "HomeScreen"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHomeScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initVars()
        getHotelData()

        checkAccessLevel(firebaseAuth.currentUser?.uid.toString())
        binding.activityHomeScreenAccountButton.setOnClickListener {
            if (isAdmin) {
                val intentAdmin = Intent(this, AdminPanel::class.java)
                startActivity(intentAdmin)
            } else {
                val intent = Intent(this, AccountInfo::class.java)
                startActivity(intent)
            }
        }

        binding.activityHomeScreenSearch.clearFocus()
        binding.activityHomeScreenSearch.setOnQueryTextListener(object :
            SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterList(newText)
                return true
            }
        })

        hotelAdapter.onItemClick = {
            val hotelDetailed = hashMapOf(
                "id" to it.id,
                "name" to it.name,
                "grade" to it.grade,
                "feedbackQuantity" to it.feedbackQuantity,
                "cost" to it.cost,
                "image" to it.image
            )
            val intentDetailed = Intent(this, HotelInfo::class.java)
            intentDetailed.putExtra("id", hotelDetailed["id"])
            intentDetailed.putExtra("name", hotelDetailed["name"])
            intentDetailed.putExtra("grade", hotelDetailed["grade"])
            intentDetailed.putExtra("feedbackQuantity", hotelDetailed["feedbackQuantity"])
            intentDetailed.putExtra("cost", hotelDetailed["cost"])
            intentDetailed.putExtra("image", hotelDetailed["image"])
            startActivity(intentDetailed)
        }
    }

    private fun filterList(text: String?) {
        val filteredList = ArrayList<Hotel>()
        for (hotel in hotelList) {
            if (text != null) {
                if (hotel.name?.lowercase()?.contains(text.lowercase()) == true) {
                    filteredList.add(hotel)
                }
            }
        }
        if (filteredList.isEmpty()) {
            Toast.makeText(this, "Ничего не найдено", Toast.LENGTH_SHORT).show()
        } else {
            hotelAdapter.setFilteredList(filteredList)
        }
    }

    private fun initVars() {
        firebaseFirestore = FirebaseFirestore.getInstance()
        firebaseAuth = FirebaseAuth.getInstance()
        binding.activityHomeScreenRecyclerView.setHasFixedSize(true)
        binding.activityHomeScreenRecyclerView.layoutManager = LinearLayoutManager(this)
        hotelList = ArrayList()
        hotelAdapter = HotelAdapter(hotelList)
        binding.activityHomeScreenRecyclerView.adapter = hotelAdapter
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun getHotelData() {
        firebaseFirestore.collection("hotels")
            .get().addOnSuccessListener {
                for (entry in it) {
                    if (entry != null) {
                        hotelList.add(
                            Hotel(
                                entry.id,
                                entry["name"].toString(),
                                entry["grade"] as Double,
                                entry["feedbackQuantity"].toString().toInt(),
                                entry["cost"].toString().toInt(),
                                entry["image"].toString()
                            )
                        )
                        Log.d(TAG, "DocumentSnapshot data: ${entry.data}")
                    } else {
                        Log.d(TAG, "No such document")
                    }
                }
                hotelAdapter.notifyDataSetChanged()
            }
    }

    private fun checkAccessLevel(uid: String) = CoroutineScope(Dispatchers.IO).launch {
        val userDocRef = firebaseFirestore.collection("users").document(uid).get().await()
        if (userDocRef["permission"] == "admin") {
            isAdmin = true
        }
    }
}

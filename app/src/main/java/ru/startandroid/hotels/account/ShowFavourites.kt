package ru.startandroid.hotels.account

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import ru.startandroid.hotels.databinding.ActivityShowFavouritesBinding
import ru.startandroid.hotels.home.Hotel
import ru.startandroid.hotels.home.HotelAdapter
import ru.startandroid.hotels.home.HotelInfo

class ShowFavourites : AppCompatActivity() {

    private lateinit var binding: ActivityShowFavouritesBinding
    private lateinit var firebaseFirestore: FirebaseFirestore
    private lateinit var favouriteHotelsList: ArrayList<Hotel>
    private lateinit var hotelFavouritesAdapter: HotelAdapter
    private var TAG = "ShowFavourites"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityShowFavouritesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initVars()

        val userId = FirebaseAuth.getInstance().currentUser?.uid
        getFavouritesData(userId)

        hotelFavouritesAdapter.onItemClick = {
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

    private fun initVars() {
        firebaseFirestore = FirebaseFirestore.getInstance()
        binding.activityShowFavouritesRecyclerView.setHasFixedSize(true)
        binding.activityShowFavouritesRecyclerView.layoutManager = LinearLayoutManager(this)
        favouriteHotelsList = ArrayList()
        hotelFavouritesAdapter = HotelAdapter(favouriteHotelsList)
        binding.activityShowFavouritesRecyclerView.adapter = hotelFavouritesAdapter
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun getFavouritesData(userId: String?) {
        firebaseFirestore.collection("favourites")
            .whereEqualTo("user_id", userId)
            .get().addOnSuccessListener { favouritesDocuments ->
                for (document in favouritesDocuments) {
                    firebaseFirestore.collection("hotels").document(document["hotel_id"].toString())
                        .get().addOnSuccessListener { hotelDocument ->
                            if (hotelDocument.exists()) {
                                favouriteHotelsList.add(
                                    Hotel(
                                        hotelDocument.id,
                                        hotelDocument["name"].toString(),
                                        hotelDocument["grade"] as Double,
                                        hotelDocument["feedbackQuantity"].toString().toInt(),
                                        hotelDocument["cost"].toString().toInt(),
                                        hotelDocument["image"].toString()
                                    )
                                )
                                Log.d(TAG, "DocumentSnapshot data: ${hotelDocument.data}")
                            } else {
                                Log.d(TAG, "No such document")
                            }
                            hotelFavouritesAdapter.notifyDataSetChanged()
                        }
                }
            }
    }
}

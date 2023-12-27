package ru.startandroid.hotels.account

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import ru.startandroid.hotels.authentication.SignInActivity
import ru.startandroid.hotels.databinding.ActivityAccountInfoBinding

class AccountInfo : AppCompatActivity() {
    private lateinit var db: FirebaseFirestore
    private lateinit var binding: ActivityAccountInfoBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val TAG = "AccountInfo"

        binding = ActivityAccountInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.activityAccountInfoShowFavourites.setOnClickListener {
            val intent = Intent(this, ShowFavourites::class.java)
            startActivity(intent)
        }

        binding.activityAccountInfoShowBooking.setOnClickListener {
            val intent = Intent(this, ShowBooking::class.java)
            startActivity(intent)
        }

        initVars()
        val user = FirebaseAuth.getInstance().currentUser

        if (user != null) {
            val docRef = db.collection("users").document(user.uid)
            docRef.get().addOnSuccessListener { document ->
                if (document != null) {
                    binding.activityAccountInfoUserName.text = document["name"].toString()
                    Log.d(TAG, "DocumentSnapshot data: ${document["name"]}")
                } else {
                    Log.d(TAG, "No such document")
                }
            }
                .addOnFailureListener { exception ->
                    Log.d(TAG, "get failed with ", exception)
                }
            binding.activityAccountInfoUserEmail.text = user.email.toString()
            binding.activityAccountInfoSignOut.setOnClickListener {
                firebaseAuth.signOut()
                val intent = Intent(this, SignInActivity::class.java)
                startActivity(intent)
                finish()
            }
        } else {
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun initVars() {
        firebaseAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
    }
}
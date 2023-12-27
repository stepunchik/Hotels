package ru.startandroid.hotels.admin

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import ru.startandroid.hotels.authentication.SignInActivity
import ru.startandroid.hotels.databinding.ActivityAdminPanelBinding

class AdminPanel : AppCompatActivity() {
    private lateinit var db: FirebaseFirestore
    private lateinit var binding: ActivityAdminPanelBinding
    private lateinit var firebaseAuth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAdminPanelBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val TAG = "AdminPanel"

        binding.activityAdminPanelAddHotel.setOnClickListener {
            val intentAddHotel = Intent(this, AddHotel::class.java)
            startActivity(intentAddHotel)
        }

        binding.activityAdminPanelEditHotel.setOnClickListener {
            val intentEditHotel = Intent(this, EditHotel::class.java)
            startActivity(intentEditHotel)
        }

        binding.activityAdminPanelRemoveHotel.setOnClickListener {
            val intentRemoveHotel = Intent(this, RemoveHotel::class.java)
            startActivity(intentRemoveHotel)
        }

        initVars()
        val user = FirebaseAuth.getInstance().currentUser

        if (user != null) {
            val docRef = db.collection("users").document(user.uid)
            docRef.get().addOnSuccessListener { document ->
                if (document != null) {
                    binding.activityAdminPanelUserName.text = document["name"].toString()
                    Log.d(TAG, "DocumentSnapshot data: ${document["name"]}")
                } else {
                    Log.d(TAG, "No such document")
                }
            }
                .addOnFailureListener { exception ->
                    Log.d(TAG, "get failed with ", exception)
                }
            binding.activityAdminPanelUserEmail.text = user.email.toString()
            binding.activityAdminPanelSignOut.setOnClickListener {
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
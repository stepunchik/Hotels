package ru.startandroid.hotels.authentication

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.FirebaseFirestore
import ru.startandroid.hotels.account.AccountInfo
import ru.startandroid.hotels.databinding.ActivitySignUpBinding

class SignUpActivity : AppCompatActivity() {

    private lateinit var TAG: String
    private lateinit var binding: ActivitySignUpBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initVars()

        binding.activitySignUpToSignInActivity.setOnClickListener {
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.activitySignUpLayout.setOnClickListener {
            closeKeyboard(binding.activitySignUpLayout)
        }

        val db = FirebaseFirestore.getInstance()

        binding.activitySignUpSubmitRegistrationButton.setOnClickListener {
            val enteredLogin = binding.activitySignUpLogin.text.toString()
            val enteredName = binding.activitySignUpName.text.toString()
            val enteredPassword = binding.activitySignUpPassword.text.toString()
            val enteredConfirmPassword = binding.activitySignUpConfirmPassword.text.toString()

            if (enteredLogin.isNotEmpty() && enteredPassword.isNotEmpty() && enteredConfirmPassword.isNotEmpty() && enteredName.isNotEmpty()) {
                if (enteredPassword == enteredConfirmPassword) {
                    firebaseAuth.createUserWithEmailAndPassword(enteredLogin, enteredPassword)
                        .addOnCompleteListener {
                            if (it.isSuccessful) {
                                val user = FirebaseAuth.getInstance().currentUser
                                val userId = user?.uid
                                val userData = hashMapOf(
                                    "email" to enteredLogin,
                                    "name" to enteredName,
                                    "permission" to "user"
                                )
                                db.collection("users").document(userId.toString())
                                    .set(userData)
                                    .addOnSuccessListener {
                                        Log.d(
                                            TAG,
                                            "DocumentSnapshot successfully written!"
                                        )
                                    }
                                    .addOnFailureListener { e ->
                                        Log.w(
                                            TAG,
                                            "Error writing document",
                                            e
                                        )
                                    }
                                val intent = Intent(this, AccountInfo::class.java)
                                startActivity(intent)
                                finish()
                            } else {
                                Log.d("Exception", it.exception.toString())
                                when (it.exception) {
                                    is FirebaseAuthUserCollisionException -> {
                                        Toast.makeText(
                                            applicationContext,
                                            "Пользователь с таким именем уже существует",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }

                                    is FirebaseAuthWeakPasswordException -> {
                                        Toast.makeText(
                                            applicationContext,
                                            "Пароль слишком простой",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }

                                    is FirebaseAuthInvalidCredentialsException -> {
                                        Toast.makeText(
                                            applicationContext,
                                            "Недопустимый формат почты",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }

                                    else -> {
                                        Toast.makeText(
                                            applicationContext,
                                            "Недопустимые данные",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            }
                        }
                } else {
                    Toast.makeText(this, "Пароли не совпадают", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Пожалуйста, заполните все поля", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun initVars() {
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        TAG = "SignUpActivity"
        firebaseAuth = FirebaseAuth.getInstance()
    }

    private fun closeKeyboard(view: View) {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
}
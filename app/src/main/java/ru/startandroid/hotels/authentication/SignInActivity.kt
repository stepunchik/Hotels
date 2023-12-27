package ru.startandroid.hotels.authentication

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.firestore.FirebaseFirestore
import ru.startandroid.hotels.home.HomeScreenActivity
import ru.startandroid.hotels.databinding.ActivitySignInBinding

class SignInActivity : AppCompatActivity() {

    private lateinit var TAG: String
    private lateinit var binding: ActivitySignInBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseFirestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initVars()

        binding.activitySignInToSignUpActivity.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.activitySignInLayout.setOnClickListener {
            closeKeyboard(binding.activitySignInLayout)
        }

        binding.activitySignInSubmitEnterButton.setOnClickListener {
            val enteredLogin = binding.activitySignInLogin.text.toString()
            val enteredPassword = binding.activitySignInPassword.text.toString()
            if (enteredLogin.isNotEmpty() && enteredPassword.isNotEmpty()) {
                firebaseAuth.signInWithEmailAndPassword(enteredLogin, enteredPassword)
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            val intentUser = Intent(this, HomeScreenActivity::class.java)
                            startActivity(intentUser)
                            finish()
                        } else {
                            when (it.exception) {
                                is FirebaseAuthInvalidCredentialsException -> {
                                    Toast.makeText(
                                        applicationContext,
                                        "Неверный пароль или логин",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }

                                is FirebaseAuthInvalidUserException -> {
                                    Toast.makeText(
                                        applicationContext,
                                        "Такого аккаунта не существует",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }

                                else -> {
                                    Toast.makeText(
                                        applicationContext,
                                        "Невозможно выполнить вход",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                    }
            } else {
                Toast.makeText(this, "Пожалуйста, заполните все поля", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun initVars() {
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)
        TAG = "SignInActivity"
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseFirestore = FirebaseFirestore.getInstance()
    }

    private fun closeKeyboard(view: View) {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
}
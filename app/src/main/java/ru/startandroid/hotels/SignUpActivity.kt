package ru.startandroid.hotels

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import ru.startandroid.hotels.databinding.ActivitySignUpBinding

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        binding.activitySignUpToSignInActivity.setOnClickListener {
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.activitySignUpSubmitRegistrationButton.setOnClickListener {
            val enteredLogin = binding.activitySignUpLogin.text.toString()
            val enteredPassword = binding.activitySignUpPassword.text.toString()
            val enteredConfirmPassword = binding.activitySignUpConfirmPassword.text.toString()

            if(enteredLogin.isNotEmpty() && enteredPassword.isNotEmpty() && enteredConfirmPassword.isNotEmpty()){
                if(enteredPassword == enteredConfirmPassword){
                    firebaseAuth.createUserWithEmailAndPassword(enteredLogin, enteredPassword).addOnCompleteListener {
                        if(it.isSuccessful){
                            val intent = Intent(this, SignInActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                        else {
                            Log.d("Exception", it.exception.toString())
                            when(it.exception) {
                                is FirebaseAuthUserCollisionException -> {
                                    Toast.makeText(applicationContext, "Пользователь с таким именем уже существует", Toast.LENGTH_SHORT).show()
                                }
                                is FirebaseAuthInvalidCredentialsException -> {
                                    Toast.makeText(applicationContext, "Невозможно создать пользователя с такими данными", Toast.LENGTH_SHORT).show()
                                }
                                is FirebaseAuthWeakPasswordException -> {
                                    Toast.makeText(applicationContext, "Пароль должен содержать минимум 6 символов", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                }
                else {
                    Toast.makeText(this, "Пароли не совпадают", Toast.LENGTH_SHORT).show()
                }
            }
            else {
                Toast.makeText(this, "Пожалуйста, заполните все поля", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
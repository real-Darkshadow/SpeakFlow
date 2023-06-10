package com.app.speak.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.speak.repository.dataSourceImpl.MainRepository
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val MainRepository: MainRepository,
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
) : ViewModel() {


    val documentWriteResult: MutableLiveData<Boolean> = MutableLiveData()
    private val _signInResult = MutableLiveData<Result<AuthResult>>()
    val emailSignInResult: LiveData<Result<AuthResult>> get() = _signInResult

    private val _signUpResult = MutableLiveData<Result<AuthResult>>()
    val emailSignUpResult: LiveData<Result<AuthResult>> get() = _signUpResult

    fun emailSignIn(email: String, password: String) {
        MainRepository.emailSignIn(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                _signInResult.value = Result.success(task.result)
            } else {
                val errorMessage = task.exception?.message ?: "Unknown error occurred"
                _signInResult.value = Result.failure(Exception(errorMessage))
            }
        }


    }

    fun emailSignUp(email: String, password: String) {
            MainRepository.emailSignUp(email, password).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _signUpResult.value = Result.success(task.result)
                } else {
                    val errorMessage = task.exception?.message ?: "Unknown error occurred"
                    _signUpResult.value = Result.failure(Exception(errorMessage))
                }
            }
    }

    fun storeDetailFireBase(name: String, uid: String, email: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val userMap = hashMapOf(
                "userId" to uid,
                "name" to name,
                "email" to email,
                "tokens" to 100  // or however many tokens new users should start with
            )
            try {
                firestore.collection("users").document(uid)
                    .set(userMap)
                    .addOnSuccessListener {
                        documentWriteResult.value = true
                        Log.d("TAG", "User document successfully written!")
                    }
                    .addOnFailureListener { e ->
                        documentWriteResult.value = false
                        Log.w("TAG", "Error writing user document", e)
                    }
            } catch (e: Exception) {
                Log.d("Tag", e.toString())
            }
        }


    }
}
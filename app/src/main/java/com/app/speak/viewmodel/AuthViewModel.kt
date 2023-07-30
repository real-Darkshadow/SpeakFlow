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
) : ViewModel() {


    val emailSignInResult = MutableLiveData<Result<AuthResult>>()
    val emailSignUpResult = MutableLiveData<Result<AuthResult>>()

    fun emailSignIn(email: String, password: String) {
        viewModelScope.launch {
            MainRepository.emailSignIn(email, password).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    emailSignInResult.value = Result.success(task.result)
                } else {
                    val errorMessage = task.exception?.message ?: "Unknown error occurred"
                    emailSignInResult.value = Result.failure(Exception(errorMessage))
                }
            }
        }


    }

    fun emailSignUp(email: String, password: String) {
        viewModelScope.launch {
            MainRepository.emailSignUp(email, password).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    emailSignUpResult.value = Result.success(task.result)
                } else {
                    val errorMessage = task.exception?.message ?: "Unknown error occurred"
                    emailSignUpResult.value = Result.failure(Exception(errorMessage))
                }
            }
        }

    }

    fun storeDetailFireBase(name: String, uid: String, email: String) {
        viewModelScope.launch(Dispatchers.IO) {
            MainRepository.storeDetailsInFirebase(name, uid, email)
        }


    }
}
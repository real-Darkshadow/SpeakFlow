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


    val documentWriteResult: MutableLiveData<Boolean> = MutableLiveData()
    private val _signInResult = MutableLiveData<Result<AuthResult>>()
    val emailSignInResult: LiveData<Result<AuthResult>> get() = _signInResult

    private val _signUpResult = MutableLiveData<Result<AuthResult>>()
    val emailSignUpResult: LiveData<Result<AuthResult>> get() = _signUpResult

    fun emailSignIn(email: String, password: String) {
        viewModelScope.launch {
            MainRepository.emailSignIn(email, password).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _signInResult.value = Result.success(task.result)
                } else {
                    val errorMessage = task.exception?.message ?: "Unknown error occurred"
                    _signInResult.value = Result.failure(Exception(errorMessage))
                }
            }
        }


    }

    fun emailSignUp(email: String, password: String) {
        viewModelScope.launch {
            MainRepository.emailSignUp(email, password).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _signUpResult.value = Result.success(task.result)
                } else {
                    val errorMessage = task.exception?.message ?: "Unknown error occurred"
                    _signUpResult.value = Result.failure(Exception(errorMessage))
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
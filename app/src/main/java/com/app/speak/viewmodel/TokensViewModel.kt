package com.app.speak.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.speak.models.PromptModel
import com.app.speak.models.planPrices
import com.app.speak.repository.dataSourceImpl.MainRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class TokensViewModel @Inject constructor(
    private val repository: MainRepository,
    private val firestore: FirebaseFirestore
) : ViewModel() {
    val userData = MutableLiveData<Map<String, Any>?>()
    var tokens = 0L


    val planPrices: MutableLiveData<List<planPrices>> by lazy {
        MutableLiveData<List<planPrices>>()
    }

    val error: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    fun getPrices() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.getPrices(onSuccess = {
                planPrices.value = it
            },
                onFailure = { exception ->
                    error.value = "Error fetching prompts: ${exception.message}"
                }
            )
        }
    }

    fun userDataListener(uId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = repository.getUserData(uId).addOnSuccessListener {
                userData.value = it.data
            }.addOnFailureListener { exception ->
                // Error occurred while retrieving the document
                Log.d("tag", "Error getting document: ${exception.message}")
            }
        }

    }


}
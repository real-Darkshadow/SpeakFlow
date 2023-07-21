package com.app.speak.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.speak.models.LiveVoice
import com.app.speak.models.PlanPrices
import com.app.speak.repository.dataSourceImpl.MainRepository
import com.google.firebase.firestore.FirebaseFirestore
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


    val planPrices=MutableLiveData<List<PlanPrices>>()

    fun getPrices() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.getPrices().addOnSuccessListener {
            for (document in it) {
            val availablePlans = document.data["AvailablePlans"] as? List<Map<String, Any>>
            if (availablePlans != null) {
                // Map the data into a list of PlanPrices objects
                val availablePlansList: List<PlanPrices> = availablePlans.map { dataMap ->
                    PlanPrices(
                        planName = dataMap["planName"].toString(),
                        id = dataMap["id"].toString(),
                        planPrice = dataMap["planPrice"].toString()
                    )
                }
                planPrices.postValue(availablePlansList)
            }
            }}.addOnFailureListener {
                Log.e("e",it.toString())
            }
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
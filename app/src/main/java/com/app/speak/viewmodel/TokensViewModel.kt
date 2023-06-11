package com.app.speak.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.speak.models.PromptModel
import com.app.speak.models.planPrices
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

    val planPrices: MutableLiveData<List<planPrices>> by lazy {
        MutableLiveData<List<planPrices>>()
    }

    val error: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }
    fun getPrices(){
        viewModelScope.launch(Dispatchers.IO){
            repository.getPrices(onSuccess ={
                planPrices.value=it
            },
                onFailure = { exception ->
                    error.value = "Error fetching prompts: ${exception.message}"
                }
            )
        }
    }

}
package com.app.speak.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.speak.repository.MainRepository
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: MainRepository,
    private val firestore: FirebaseFirestore
) : ViewModel() {
    private val _data = MutableLiveData<DocumentSnapshot>()
    val data: LiveData<DocumentSnapshot> = _data

    fun setUser(uid: String, email: String) {
        repository.setUser(uid, email)
    }

    fun fetchData(documentId: String) {
        repository.getDocument(documentId)
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    // Document exists, retrieve the data
                    _data.value = documentSnapshot
                    // Process the data as needed
                } else {
                    // Document does not exist
                }
            }
            .addOnFailureListener { exception ->
                // Error occurred while retrieving the document
                Log.d("TAG", "Error getting document: ${exception.message}")
            }
    }
}

package com.app.speak.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.speak.models.Task
import com.app.speak.repository.dataSourceImpl.MainRepository
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: MainRepository,
    private val firestore: FirebaseFirestore
) : ViewModel() {
    private val _data = MutableLiveData<DocumentSnapshot>()
    val data: LiveData<DocumentSnapshot> = _data

    private val _task = MutableLiveData<String>()
    val taskResult: LiveData<String> = _task
    private val db = Firebase.firestore
    val lastTaskId = MutableLiveData<String>()
    fun addTask(task: Task) {
        db.collection("Tasks")
            .add(task)
            .addOnSuccessListener { documentReference ->
                Log.d("TAG", "DocumentSnapshot added with ID: ${documentReference.id}")
                lastTaskId.value = documentReference.id
                taskListener()
            }
            .addOnFailureListener { e ->
                Log.w("TAG", "Error adding document", e)
            }
    }

    fun setUser(uid: String, email: String, name: String) {
        repository.setUser(uid, email, name)
    }

    fun fetchData(documentId: String) {
        repository.getDocument(documentId)
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    // Document exists, retrieve the data
                    _data.value = documentSnapshot
                    Log.d("tag", "success")
                    // Process the data as needed
                } else {
                    // Document does not exist
                    Log.d("tag", "unsuccess")
                }
            }
            .addOnFailureListener { exception ->
                // Error occurred while retrieving the document
                Log.d("tag", "Error getting document: ${exception.message}")
            }
    }

    fun taskListener() {
        val docRef = db.collection("Tasks").document(lastTaskId.value.toString())
        docRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w("tag", "Listen failed.", e)
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                val data = snapshot.data
                val status = data?.get("status") as? String // Assuming 'status' is a String field
                _task.value = status.toString()
                Log.d("tag", "Current data: ${snapshot.data}")
            } else {
                Log.d("tag", "Current data: null")
            }
        }
    }


}

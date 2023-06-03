package com.app.speak.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.speak.models.Task
import com.app.speak.repository.dataSourceImpl.MainRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: MainRepository,
    private val firestore: FirebaseFirestore
) : ViewModel() {
    //    private val _data = MutableLiveData<DocumentSnapshot?>()
    val data = MutableLiveData<Map<String, Any>?>()
    val promptHistory = MutableLiveData<ArrayList<String>>()


    val taskResult = MutableLiveData<Map<String, Any>?>()


    private val db = Firebase.firestore
    val lastTaskId = MutableLiveData<String>()
    fun addTask(task: Task) {
        viewModelScope.launch(Dispatchers.IO) {
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

    }

    fun setUser(uid: String) {
        repository.setUser(uid)
    }

    fun fetchData(documentId: String) {
        viewModelScope.launch {
            repository.getDocument(documentId)
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        // Document exists, retrieve the data
                        data.value = documentSnapshot.data
                        setUser(documentId)
                        promptHistory.value = documentSnapshot.get("prompts") as? ArrayList<String>
                        userListener()
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
    }

    fun addPrompt(documentId: String, prompt: String) {
        val userRef = db.collection("Users").document(documentId)

        userRef.get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val data = documentSnapshot.data
                    val existingPrompts =
                        data?.get("prompts") as? ArrayList<String> ?: arrayListOf()
                    val existingTaskIds = data?.get("taskId") as? ArrayList<String> ?: arrayListOf()

                    existingPrompts.add(prompt)
                    existingTaskIds.add(lastTaskId.value.toString())

                    val dataToUpdate: MutableMap<String, Any> = HashMap()
                    dataToUpdate["prompts"] = existingPrompts
                    dataToUpdate["taskId"] = existingTaskIds

                    viewModelScope.launch {
                        userRef.update(dataToUpdate)
                            .addOnSuccessListener {
                                println("Prompts and additional field updated successfully.")
                            }
                            .addOnFailureListener { exception ->
                                println("Failed to update prompts and additional field: $exception")
                            }
                    }
                } else {
                    println("User document does not exist.")
                }
            }
            .addOnFailureListener { exception ->
                println("Failed to retrieve document: $exception")
            }
    }

    private fun taskListener() {
        val docRef = db.collection("Tasks").document(lastTaskId.value.toString())
        docRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w("tag", "Listen failed.", e)
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                val data = snapshot.data
                val status = data?.get("status") as? String // Assuming 'status' is a String field
                taskResult.value = data
                Log.d("tag", "Current data: ${snapshot.data}")
            } else {
                Log.d("tag", "Current data: null")
            }
        }
    }

    fun userListener() {
        val auth = FirebaseAuth.getInstance()
        val docRef = db.collection("Users").document(auth.uid.toString())
        docRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w("tag", "Listen failed.", e)
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                val datak = snapshot.data
                data.value = datak
                Log.d("tag", "Current data: ${snapshot.data}")
            } else {
                Log.d("tag", "Current data: null")
            }
        }
    }


}

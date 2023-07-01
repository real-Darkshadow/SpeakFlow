package com.app.speak.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.speak.models.PromptModel
import com.app.speak.models.Task
import com.app.speak.models.TransactionHistory
import com.app.speak.repository.dataSourceImpl.MainRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject


@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: MainRepository,
    private val firestore: FirebaseFirestore
) : ViewModel() {
    //    private val _data = MutableLiveData<DocumentSnapshot?>()
    val userData = MutableLiveData<Map<String, Any>?>()
    val promptHistory = MutableLiveData<ArrayList<String>>()
    var selectedVoice: String = ""

    private var uri: Uri? = null
    private var _code = MutableLiveData<String>()
    val code: LiveData<String> get() = _code
    val taskResult = MutableLiveData<Map<String, Any>?>()
    val profileOptionList = mapOf(
        0 to "Your Transactions",
        1 to "Tokens",
        2 to "Spread the word",
        3 to "Terms of Use",
        4 to "Privacy policy",
        5 to "More Apps",
        6 to "Delete Account"
    )

    private val db = Firebase.firestore
    val lastTaskId = MutableLiveData<String>()

    val transactionHistory = MutableLiveData<List<TransactionHistory>>()
    fun addTask(task: Task) {
        viewModelScope.launch(Dispatchers.IO) {
            db.collection("prompts")
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


    fun getUserData(documentId: String) {
        viewModelScope.launch {
            repository.getUserData(documentId)
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        // Document exists, retrieve the data
                        userData.value = documentSnapshot.data
                       // setUser(documentId)
//                        userDataListener()
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

    val prompts: MutableLiveData<List<PromptModel>> by lazy {
        MutableLiveData<List<PromptModel>>()
    }

    val error: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    fun fetchPrompts(userId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.getPromptsByUser(userId,
                onSuccess = { promptsList ->
                    prompts.value = promptsList
                },
                onFailure = { exception ->
                    error.value = "Error fetching prompts: ${exception.message}"
                }
            )
        }

    }

    private fun taskListener() {
        val docRef = db.collection("prompts").document(lastTaskId.value.toString())
        docRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w("tag", "Listen failed.", e)
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                val data = snapshot.data
                taskResult.value = data
                Log.d("tag", "Current data: ${snapshot.data}")
            } else {
                Log.d("tag", "Current data: null")
            }
        }
    }

    fun userDataListener(uId: String) {
        val docRef = db.collection("users").document(uId)
        viewModelScope.launch (Dispatchers.IO){
            docRef.addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w("tag", "Listen failed.", e)
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    userData.value = snapshot.data
                    repository.setUser(uId)
                    Log.d("tag", "Current data: ${snapshot.data}")
                } else {
                    Log.d("tag", "Current data: null")
                }
            }
        }

    }

    fun uerLogout() {
        viewModelScope.launch {
            repository.userLogout()
        }
    }

    fun getTransactions() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.getTransactions(onSuccess = { transactions ->
                transactionHistory.value = transactions
            },
                onFailure = { exception ->
                    error.value = "Error fetching prompts: ${exception.message}"
                })
        }
    }


    fun codeFromUri(bitmap: InputImage) {
        viewModelScope.launch {
            // worked bt not so good : Wil use Cloud vision api :P
            val textRecognizer: com.google.mlkit.vision.text.TextRecognizer =
                TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            val task = textRecognizer.process(bitmap)
            task.addOnCompleteListener {
                _code.value = it.result.text
                Log.d("tag", it.result.text)
            }.addOnFailureListener {
                Log.e("ERROR", "Exception : " + it.message)
            }

        }

    }


}

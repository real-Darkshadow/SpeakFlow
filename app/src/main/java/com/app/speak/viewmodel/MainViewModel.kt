package com.app.speak.viewmodel

import android.media.AudioManager
import android.media.MediaPlayer
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.speak.models.LiveVoice
import com.app.speak.models.PromptModel
import com.app.speak.models.TransactionHistory
import com.app.speak.repository.dataSourceImpl.MainRepository
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: MainRepository,
) : ViewModel() {
    val regeneratePrompt = MutableLiveData<String?>()
    var stabilityPercentage = 50
    var clarityPercentage = 75
    var audioLink = ""
    val userData = MutableLiveData<Map<String, Any>?>()
    var selectedVoiceId: String = ""
    var imageText = MutableLiveData<String>()
    val prompts: MutableLiveData<List<PromptModel>> by lazy {
        MutableLiveData<List<PromptModel>>()
    }
    val error: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }
    val taskResult = MutableLiveData<Map<String, Any>?>()
    val profileOptionList = mapOf(
        0 to Pair("Your Transactions", "View and manage your transaction history."),
        1 to Pair("Tokens", "Explore and track your token balances."),
        2 to Pair("Spread the word", "Share our app with others and earn rewards."),
        3 to Pair("Terms of Use", "Read and understand our terms and conditions."),
        4 to Pair("Privacy Policy", "Review our privacy policy to learn how we handle your data."),
        5 to Pair("Delete Account", "Permanently delete your account and all associated data.")
    )
    private val db = Firebase.firestore
    val lastTaskId = MutableLiveData<String>()
    val transactionHistory = MutableLiveData<List<TransactionHistory>>()
    val voicesList = MutableLiveData<List<LiveVoice>>()


    fun getUserData(documentId: String) {
        viewModelScope.launch {
            repository.getUserData(documentId)
                .addOnSuccessListener { documentSnapshot ->
                    userData.value = documentSnapshot.data
                    val data = documentSnapshot.data
                    repository.setUser(
                        documentId,
                        data?.get("name").toString(),
                        data?.get("email").toString()
                    )

                }
                .addOnFailureListener { exception ->
                    // Error occurred while retrieving the document
                    Log.e("tag", "Error getting document: ${exception.message}")
                }
        }
    }


    fun fetchPrompts(userId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.getPromptsByUser(userId).addOnSuccessListener { querySnapshot ->
                val promptList = mutableListOf<PromptModel>()
                for (document in querySnapshot) {
                    val promptText = document.getString("prompt") ?: ""
                    val audioUrl = document.getString("signedUrl") ?: ""
                    val prompt = PromptModel(promptText, audioUrl)
                    promptList.add(prompt)
                }
                prompts.postValue(promptList)

            }
        }

    }

    fun taskListener() {
        val docRef = db.collection("prompts").document(lastTaskId.value.toString())
        docRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.d("tag", "Listen failed.", e)
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


    fun uerLogout() {
        viewModelScope.launch {
            repository.userLogout()
        }
    }

    fun getTransactions() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.getTransactions().addOnSuccessListener {
                val transactionHistoryList = mutableListOf<TransactionHistory>()

                for (document in it) {
                    val transactionName = document.getString("planId") ?: ""
                    val transactionDate = document.getString("transactionDate") ?: ""
                    val transactionStatus = document.getString("paymentStatus") ?: ""
                    val transactionId = document.getString("checkoutSessionId") ?: ""
                    val amount = document.getLong("amount") ?: 0

                    val trans = TransactionHistory(
                        transactionName,
                        transactionDate,
                        transactionStatus,
                        transactionId,
                        amount
                    )
                    transactionHistoryList.add(trans)
                }
// Update the MutableLiveData with the list of TransactionHistory objects
                transactionHistory.postValue(transactionHistoryList)
            }
        }

    }

    fun codeFromUri(bitmap: InputImage) {
        viewModelScope.launch(Dispatchers.IO) {
            val textRecognizer: com.google.mlkit.vision.text.TextRecognizer =
                TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            val task = textRecognizer.process(bitmap)
            task.addOnCompleteListener {
                imageText.postValue(it.result.text)
                Log.d("tag", it.result.text)
            }.addOnFailureListener {
                Log.e("ERROR", "Exception : " + it.message)
            }
        }
    }

    fun getVoices() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.getVoices().addOnSuccessListener {
                for (document in it) {
                    val liveVoicesData =
                        (document.data.get("liveVoices") as? List<Map<String, Any>>)
                    if (liveVoicesData != null) {
                        // Map the data into a list of LiveVoice objects
                        val liveVoicesList: List<LiveVoice> = liveVoicesData.map { dataMap ->
                            LiveVoice(
                                name = dataMap["name"].toString(),
                                id = dataMap["id"].toString()
                            )
                        }
                        voicesList.postValue(liveVoicesList)
                    }
                }

            }.addOnFailureListener {
                Log.e("tag", it.toString())
            }
        }
    }

    fun createNewProcess(data: HashMap<String, String>) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.createNewProcess(data).addOnSuccessListener {
                    lastTaskId.postValue(it.id)
                }.addOnFailureListener {
                    Log.e("tag", it.toString())

                }
            } catch (e: Exception) {

            }
        }
    }


    private var player: MediaPlayer? = null
    private val _isPlaying = MutableLiveData<Boolean>()
    val isPlaying: LiveData<Boolean> = _isPlaying

    private val _currentProgress = MutableLiveData<Int>()
    val currentProgress: LiveData<Int> = _currentProgress

    fun initializeMediaPlayer(audioLink: String) {
        if (player == null) {
            player = MediaPlayer()
            player?.setAudioStreamType(AudioManager.STREAM_MUSIC)

            player?.setDataSource(audioLink)
            player?.prepare()
            player?.setOnCompletionListener {
                _isPlaying.value = false
                _currentProgress.value = 0
            }
        }
    }

    fun togglePlayback() {
        player?.let { mediaPlayer ->
            if (mediaPlayer.isPlaying) {
                mediaPlayer.pause()
            } else {
                mediaPlayer.start()
            }
            _isPlaying.value = mediaPlayer.isPlaying
        }
    }

    fun seekTo(progress: Int) {
        player?.seekTo(progress)
    }

    fun getCurrentPosition(): Int {
        return player?.currentPosition ?: 0
    }

    fun getDuration(): Int {
        return player?.duration ?: 0
    }

    override fun onCleared() {
        super.onCleared()
        player?.release()
        player = null
    }

}




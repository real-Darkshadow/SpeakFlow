package com.app.speak.repository.dataSourceImpl

import android.util.Log
import com.app.speak.Speak
import com.app.speak.api.ApiService
import com.app.speak.db.AppPrefManager
import com.app.speak.models.PromptModel
import com.app.speak.models.TransactionHistory
import com.app.speak.models.planPrices
import com.app.speak.repository.dataSource.MainRepositoryInterface
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Named

class MainRepository @Inject constructor(
    val context: Speak,
    val apiService: ApiService,
    val appPrefManager: AppPrefManager,
    @Named("device_id")
    private val deviceID: String,
    private val firestore: FirebaseFirestore,

    ) : MainRepositoryInterface {
    private var mAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db = Firebase.firestore

    fun getUserData(documentId: String): Task<DocumentSnapshot> {
        val documentRef = db.collection("users").document(documentId)
        return documentRef.get()
    }

    fun emailSignIn(email: String, password: String): Task<AuthResult> {
        return mAuth.signInWithEmailAndPassword(email, password)
    }

    fun emailSignUp(email: String, password: String): Task<AuthResult> {
        return mAuth.createUserWithEmailAndPassword(email, password)
    }

    fun setUser(uid: String) {
        appPrefManager.setUserData(uid)
    }

    suspend fun getPromptsByUser(userId: String, onSuccess: (List<PromptModel>) -> Unit, onFailure: (Exception) -> Unit) {
        withContext(Dispatchers.IO){
            db.collection("prompts")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    val prompts = mutableListOf<PromptModel>()
                    for (document in querySnapshot) {
                        val promptText = document.getString("promptText") ?: ""
                        val prompt = PromptModel( promptText)
                        prompts.add(prompt)
                    }
                    onSuccess(prompts)
                }
                .addOnFailureListener { e ->
                    onFailure(e)
                }
        }

    }



    suspend fun getPrices(onSuccess: (List<planPrices>) -> Unit, onFailure: (Exception) -> Unit) {
        withContext(Dispatchers.IO){
            db.collection("plans").whereEqualTo("valid",true).get()
                .addOnSuccessListener { querySnapshot ->
                    val plans = mutableListOf<planPrices>()
                    for (document in querySnapshot) {
                        val planName = document.getString("planName") ?: ""
                        val price = document.getString("price") ?: ""
                        val plan = planPrices(planName, price)
                        plans.add(plan)
                    }
                    onSuccess(plans)
                }
                .addOnFailureListener { e ->
                    onFailure(e)
                }
        }
    }

    suspend fun getTransactions(
        onSuccess: (List<TransactionHistory>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        withContext(Dispatchers.IO) {
            db.collection("transactions").whereEqualTo("uid", mAuth.uid).get()
                .addOnSuccessListener { querySnapshot ->
                    val transactions = mutableListOf<TransactionHistory>()
                    for (document in querySnapshot) {
                        val transactionName = document.getString("transactionName") ?: ""
                        val transactionDate = document.getString("transactionDate") ?: ""
                        val transactionStatus = document.getString("transactionStatus") ?: ""
                        val transactionId = document.getString("transactionId") ?: ""

                        val trans = TransactionHistory(
                            transactionName,
                            transactionDate,
                            transactionStatus,
                            transactionId
                        )
                        transactions.add(trans)
                    }
                    onSuccess(transactions)
                }
                .addOnFailureListener { e ->
                    onFailure(e)
                }
        }
    }
    suspend fun getVoices(){
        withContext(Dispatchers.IO) {
            db.collection("Voices").get()
                .addOnSuccessListener { querySnapshot ->
                    val transactions = mutableListOf<TransactionHistory>()
                    for (document in querySnapshot) {
                        val transactionName = document.getString("transactionName") ?: ""
                        val transactionDate = document.getString("transactionDate") ?: ""
                        val transactionStatus = document.getString("transactionStatus") ?: ""
                        val transactionId = document.getString("transactionId") ?: ""

                        val trans = TransactionHistory(
                            transactionName,
                            transactionDate,
                            transactionStatus,
                            transactionId
                        )
                        transactions.add(trans)
                    }
                }
                .addOnFailureListener { e ->
                }
        }
    }

    suspend fun userLogout() {
        withContext(Dispatchers.IO) {
            mAuth.signOut()
        }
    }

    fun storeDetailsInFirebase(name: String, uid: String, email: String) {
        val userMap = hashMapOf(
            "userId" to uid,
            "name" to name,
            "email" to email,
            "tokens" to 100  // or however many tokens new users should start with
        )
        try {
            firestore.collection("users").document(uid)
                .set(userMap)
                .addOnSuccessListener {
                    return@addOnSuccessListener
                }
                .addOnFailureListener { e ->
                    return@addOnFailureListener
                }
        } catch (e: Exception) {
            Log.d("Tag", e.toString())
        }
    }
}
package com.app.speak.repository.dataSourceImpl

import com.app.speak.Speak
import com.app.speak.api.ApiService
import com.app.speak.db.AppPrefManager
import com.app.speak.models.PromptModel
import com.app.speak.models.planPrices
import com.app.speak.repository.dataSource.MainRepositoryInterface
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
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

    suspend fun userLogout() {
        withContext(Dispatchers.IO) {
            mAuth.signOut()
        }
    }
}
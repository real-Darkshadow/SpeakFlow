package com.app.speak.repository.dataSourceImpl

import android.util.Log
import com.app.speak.Speak
import com.app.speak.db.AppPrefManager
import com.app.speak.repository.dataSource.MainRepositoryInterface
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.HttpsCallableResult
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject


class MainRepository @Inject constructor(
    val context: Speak,
    val appPrefManager: AppPrefManager,
    private val firestore: FirebaseFirestore,
    private val functions: FirebaseFunctions,
    firebaseAuth: FirebaseAuth,

    ) : MainRepositoryInterface {
    private var mAuth: FirebaseAuth = firebaseAuth
    private val db = Firebase.firestore

    suspend fun getUserData(documentId: String): Task<DocumentSnapshot> {
        return withContext(Dispatchers.IO) {
            val documentRef = db.collection("users").document(documentId)
            documentRef.get()
        }

    }

    fun emailSignIn(email: String, password: String): Task<AuthResult> {
        return mAuth.signInWithEmailAndPassword(email, password)
    }

    fun emailSignUp(email: String, password: String): Task<AuthResult> {
        return mAuth.createUserWithEmailAndPassword(email, password)
    }

    fun setUser(uid: String, name: String, email: String) {
        appPrefManager.setUserData(uid, name, email)
    }

    suspend fun getPromptsByUser(
        userId: String,
    ): Task<QuerySnapshot> {
        return withContext(Dispatchers.IO) {
            db.collection("prompts").whereEqualTo("uid", userId).limit(5).orderBy(
                "createdAt",
                Query.Direction.DESCENDING
            ).get()
        }
    }


    suspend fun getPrices(): Task<QuerySnapshot> {
        return withContext(Dispatchers.IO) {
            db.collection("plans").get()
        }
    }

    suspend fun getTransactions(): Task<QuerySnapshot> {
        return withContext(Dispatchers.IO) {
            db.collection("orders").orderBy("transactionDate", Query.Direction.DESCENDING)
                .whereEqualTo("uid", mAuth.uid).get()
        }
    }

    suspend fun getVoices(): Task<QuerySnapshot> {
        return withContext(Dispatchers.IO) {
            val doc = db.collection("Voices")
            doc.get()
        }
    }

    suspend fun userLogout() {
        withContext(Dispatchers.IO) {
            mAuth.signOut()
        }
    }

    suspend fun deleteUser(): Task<Void> {
        return withContext(Dispatchers.IO) {
            mAuth.currentUser!!.delete()
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

    override suspend fun createNewProcess(data: HashMap<String, String>): Task<DocumentReference> {
        return withContext(Dispatchers.IO) {
            firestore.collection("prompts").add(data)
        }

    }

    override suspend fun createStripeCheckout(data: HashMap<String, String>): Task<HttpsCallableResult> {
        return withContext(Dispatchers.IO) {
            functions.getHttpsCallable("createStripeCheckout").call(data)

        }
    }


}



package com.app.speak.repository.dataSourceImpl

import com.app.speak.Speak
import com.app.speak.api.ApiService
import com.app.speak.db.AppPrefManager
import com.app.speak.repository.dataSource.MainRepositoryInterface
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
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

    fun getDocument(documentId: String): Task<DocumentSnapshot> {
        val documentRef = db.collection("Users").document(documentId)
        return documentRef.get()
    }

    fun emailSignIn(email: String, password: String): Task<AuthResult> {
        return mAuth.signInWithEmailAndPassword(email, password)
    }

    fun emailSignUp(email: String, password: String): Task<AuthResult> {
        return mAuth.createUserWithEmailAndPassword(email, password)
    }

    fun setUser(uid: String, email: String) {
        appPrefManager.setUserData(uid, email)
    }
}
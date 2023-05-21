package com.app.speak.repository

import com.app.speak.db.AppPrefManager
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class MainRepository(val appPrefManager: AppPrefManager) {
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
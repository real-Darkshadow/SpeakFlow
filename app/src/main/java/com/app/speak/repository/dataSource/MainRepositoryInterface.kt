package com.app.speak.repository.dataSource

import com.app.speak.models.StripeResponse
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.functions.HttpsCallableResult

interface MainRepositoryInterface {
    suspend fun createNewProcess(data: HashMap<String, String>): Task<DocumentReference>
    suspend fun createStripeCheckout(): Task<HttpsCallableResult>
}
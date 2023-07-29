package com.app.speak.repository.dataSource

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentReference

interface MainRepositoryInterface {
    suspend fun createNewProcess(data: HashMap<String, String>): Task<DocumentReference>
}
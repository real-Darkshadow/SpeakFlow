package com.app.speak.viewmodel

import androidx.lifecycle.ViewModel
import com.app.speak.repository.dataSourceImpl.MainRepository
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class TokensViewModel @Inject constructor(
    private val repository: MainRepository,
    private val firestore: FirebaseFirestore
) : ViewModel() {
    val key="sk_test_51NAAScSAZV5GhijfSc11r2Yy5Ug1fGBUpw50qHqeKCGCf1bW9JnGpq6Pj46TlSoSHfGTQg2FShhjASeTGOxxKmAx00rMAFEaPJ"


}
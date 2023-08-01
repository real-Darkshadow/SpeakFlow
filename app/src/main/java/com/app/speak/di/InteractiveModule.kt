package com.app.speak.di

import com.app.speak.Speak
import com.app.speak.api.ApiService
import com.app.speak.db.AppPrefManager
import com.app.speak.repository.dataSource.MainRepositoryInterface
import com.app.speak.repository.dataSourceImpl.MainRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Named
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object InteractiveModule {

    @Provides
    @Singleton
    fun provideMainRepository(
        context: Speak,
        api: Retrofit,
        appPrefManager: AppPrefManager,
        @Named("device_id")
        deviceId: String,
        firebaseAuth: FirebaseAuth,
        firestore: FirebaseFirestore,
        functions: FirebaseFunctions
    ): MainRepositoryInterface {
        val apiInterface = api.create(ApiService::class.java)
        return MainRepository(
            context,
            apiInterface,
            appPrefManager,
            deviceId,
            firestore,
            functions
        )
    }

    @Provides
    @Singleton
    fun provideAppPrefManager(context: Speak): AppPrefManager = AppPrefManager(context)

    @Provides
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    @Provides
    fun provideFirebaseFunctions(): FirebaseFunctions {
        return FirebaseFunctions.getInstance()
    }

    @Provides
    fun provideFirebaseFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }
}
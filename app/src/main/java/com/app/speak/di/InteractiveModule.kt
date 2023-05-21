package com.app.speak.di

import com.app.speak.Speak
import com.app.speak.db.AppPrefManager
import com.app.speak.repository.MainRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object InteractiveModule {

    @Provides
    @Singleton
    fun provideMainRepository(
        appPrefManager: AppPrefManager,
    ): MainRepository {
        return MainRepository(
            appPrefManager,
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
    fun provideFirebaseFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }
}
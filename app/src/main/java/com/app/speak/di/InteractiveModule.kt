package com.app.speak.di

import com.app.speak.Speak
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
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object InteractiveModule {

    @Provides
    @Singleton
    fun provideMainRepository(
        context: Speak,
        appPrefManager: AppPrefManager,
        firebaseAuth: FirebaseAuth,
        fireStore: FirebaseFirestore,
        functions: FirebaseFunctions,
    ): MainRepositoryInterface {
        return MainRepository(
            context,
            appPrefManager,
            fireStore,
            functions,
            firebaseAuth,
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
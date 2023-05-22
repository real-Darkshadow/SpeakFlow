package com.app.speak.di


import com.app.speak.BuildConfig
import com.app.speak.Speak
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object EngagementModule {

    @Provides
    @Singleton
    fun providesFirebaseAnalytics(context: Speak): FirebaseAnalytics {
        FirebaseAnalytics.getInstance(context).setAnalyticsCollectionEnabled(!BuildConfig.DEBUG)
        return FirebaseAnalytics.getInstance(context)
    }
}
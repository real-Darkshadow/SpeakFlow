package com.app.speak.di

import android.provider.Settings
import com.app.speak.Speak
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object DeviceModule {
    @Provides
    @Singleton
    @Named("device_id")
    fun providesDeviceId(context: Speak): String {
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    }
}
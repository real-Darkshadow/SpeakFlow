package com.app.speak.api

import com.app.speak.BuildConfig


object ApiConfig {

    private var BASE_URL = BuildConfig.BASE_URL

    fun getBaseUrl(): String {
        return BASE_URL
    }

}
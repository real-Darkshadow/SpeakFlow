package com.app.speak.api

import android.util.Log
import com.app.speak.BuildConfig
import com.app.speak.api.ApiConfig.getBaseUrl
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import com.google.gson.JsonSyntaxException
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.TimeZone
import java.util.concurrent.TimeUnit

object ApiClient {
    private val okHttpClient = OkHttpClient()
    private val convertor = GsonConverterFactory.create(GsonBuilder().setLenient().create())

    val httpClientBuilder = OkHttpClient.Builder()
    val httpLoggingInterceptor = HttpLoggingInterceptor(ApiLogger()).apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    private val interceptor = HttpLoggingInterceptor().apply {
        level =
            if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
    }

    val client = Retrofit.Builder()
        .baseUrl(getBaseUrl())
        .addConverterFactory(convertor)
        .client(
            okHttpClient.newBuilder()
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS)
                .addNetworkInterceptor { chain ->
                    val request = chain.request()
                        .newBuilder()
                        .addHeader("platform", "android")
                        .addHeader("app-version", BuildConfig.VERSION_NAME)
                        .addHeader("app-code", BuildConfig.VERSION_CODE.toString())
                        .addHeader("timezone", TimeZone.getDefault().id)
                        .build()
                    return@addNetworkInterceptor chain.proceed(request)
                }
                .addInterceptor(
                    httpLoggingInterceptor
                )
                .build()
        )
        .build()

}

class ApiLogger : HttpLoggingInterceptor.Logger {
    override fun log(message: String) {
        val logName = "ApiLogger"
        if (message.startsWith("{") || message.startsWith("[")) {
            try {
                val prettyPrintJson = GsonBuilder().setPrettyPrinting()
                    .create().toJson(JsonParser().parse(message))
                Log.d(logName, prettyPrintJson)
            } catch (m: JsonSyntaxException) {
                Log.d(logName, message)
            }
        } else {
            Log.d(logName, message)
            return
        }
    }
}
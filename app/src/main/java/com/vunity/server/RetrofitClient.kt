package com.vunity.server

import android.annotation.SuppressLint
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.vunity.general.Application
import com.vunity.general.getToken
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

@SuppressLint("StaticFieldLeak")
object RetrofitClient {

    private const val HOST = "http://3.6.116.252:1358"
    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    //With Authorization token
    class SupportInterceptor : Interceptor {
        //Interceptor class for setting of the headers for every request
        override fun intercept(chain: Interceptor.Chain): Response {
            var request = chain.request()
            request = request.newBuilder()
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "application/json")
                .addHeader(
                    "Authorization",
                    getToken(
                        "user_token",
                        Application.appContext
                    ).toString()
                )
                .build()
            return chain.proceed(request)
        }
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(1, TimeUnit.MINUTES)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .addInterceptor(SupportInterceptor())
        .build()

    val instanceClient: ResponseService by lazy {
        val retrofit = Retrofit.Builder()
            .client(client)
            .baseUrl(HOST)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .addCallAdapterFactory(CoroutineCallAdapterFactory())
            .build()
//        Log.d("Interceptor", getToken("user_token", Application.appContext) + " vijay")
        retrofit.create(ResponseService::class.java)
    }

    //Without Authorization token
    val instanceClientWithoutToken: ResponseService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(HOST)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .addCallAdapterFactory(CoroutineCallAdapterFactory())
            .build()
        retrofit.create(ResponseService::class.java)
    }

}
package com.vunity.server

import android.annotation.SuppressLint
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.vunity.banner.BannerResponse
import com.vunity.book.BookResponse
import com.vunity.category.CategoryResponse
import com.vunity.favourite.FavouriteResponse
import com.vunity.general.Application
import com.vunity.general.getToken
import com.vunity.report.ReportResponse
import com.vunity.user.UserResponse
import com.vunity.video.VideoResponse
import com.vunity.vunity.VunityResponse
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

@SuppressLint("StaticFieldLeak")
object RetrofitClient {

    private const val HOST = "http://3.6.116.252:7433"
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

    private val retrofit = Retrofit.Builder()
        .baseUrl(HOST)
        .client(client)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .addCallAdapterFactory(CoroutineCallAdapterFactory())
        .build()

    val bannerClient: BannerResponse by lazy {
        retrofit.create(BannerResponse::class.java)
    }

    val bookClient: BookResponse by lazy {
        retrofit.create(BookResponse::class.java)
    }

    val categoryClient: CategoryResponse by lazy {
        retrofit.create(CategoryResponse::class.java)
    }

    val favouriteClient: FavouriteResponse by lazy {
        retrofit.create(FavouriteResponse::class.java)
    }

    val userClient: UserResponse by lazy {
        retrofit.create(UserResponse::class.java)
    }

    val videoClient: VideoResponse by lazy {
        retrofit.create(VideoResponse::class.java)
    }

    val vunityClient: VunityResponse by lazy {
        retrofit.create(VunityResponse::class.java)
    }

    val reportClient: ReportResponse by lazy {
        retrofit.create(ReportResponse::class.java)
    }
}
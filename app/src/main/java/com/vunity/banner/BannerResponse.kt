package com.vunity.banner

import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.*

interface BannerResponse {

    @Multipart
    @POST("banner/create")
    fun addBanner(@Part image: MultipartBody.Part): Call<BannerDto>

    @Multipart
    @PUT("banner/update/{id}")
    fun updateBanner(@Path("id") id: String, @Part image: MultipartBody.Part): Call<BannerDto>

    @GET("banner/list")
    fun getBanners(): Call<BannerListDto>

}
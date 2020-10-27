package com.vunity.banner

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

interface BannerResponse {

    @Multipart
    @PUT("banner/update/{id}")
    fun updateBanner(
        @Path("id") id: String,
        @Part image: MultipartBody.Part,
        @Part("textField") text: RequestBody
    ): Call<BannerDto>

    @GET("banner/list")
    fun getBanners(): Call<BannerListDto>

}
package com.vunity.category

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface CategoryResponse {

    @GET("category/list")
    fun category(): Call<CategoryListDto>

    @Multipart
    @POST("category/create")
    fun addCategory(
        @Part category: MultipartBody.Part,
        @Part("textField") text: RequestBody
    ): Call<CategoryDto>

}
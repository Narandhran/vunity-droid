package com.vunity.category

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

interface CategoryResponse {

    @GET("category/list/{key}")
    fun category(@Path("key") key: String): Call<CategoryListDto>

    @Multipart
    @POST("category/create")
    fun addCategory(
        @Part category: MultipartBody.Part,
        @Part("textField") text: RequestBody
    ): Call<CategoryDto>

}
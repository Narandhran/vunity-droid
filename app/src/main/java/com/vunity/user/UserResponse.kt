package com.vunity.user

import com.vunity.category.GenreDto
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.*

interface UserResponse {

    @FormUrlEncoded
    @POST("user/register")
    fun register(@FieldMap params: Map<String, @JvmSuppressWildcards Any>): Call<ResDto>

    @GET("user/requestOtp/{number}")
    fun requestOtp(@Path("number") id: String): Call<ResDto>

    @POST("user/login")
    fun login(@Body loginBody: LoginBody): Call<LoginDto>

    @GET("user/my_profile")
    fun profile(): Call<ProDto>

    @GET("user/list")
    fun listOfUsers(): Call<ProListDto>

    @GET("user/filter_status/{value}")
    fun filterUsers(@Path("value") value: String): Call<ProListDto>

    @Multipart
    @PUT("user/update_dp")
    fun updateDp(@Part dp: MultipartBody.Part): Call<ResDto>

    @FormUrlEncoded
    @PUT("user/update_profile")
    fun updateProfile(@FieldMap params: Map<String, @JvmSuppressWildcards Any>): Call<ResDto>

    @FormUrlEncoded
    @POST("genre/add")
    fun addGenre(@FieldMap params: Map<String, @JvmSuppressWildcards Any>): Call<ResDto>

    @GET("genre/list")
    fun genres(): Call<GenreDto>

    @FormUrlEncoded
    @PUT("cms/review")
    fun cmsReview(@FieldMap params: Map<String, @JvmSuppressWildcards String>): Call<ResDto>

    @FormUrlEncoded
    @POST("cms/announcement")
    fun cmsAnnouncement(@FieldMap params: Map<String, @JvmSuppressWildcards String>): Call<ResDto>

    @FormUrlEncoded
    @POST("donation/donate")
    fun donate(@FieldMap params: Map<String, @JvmSuppressWildcards Any>): Call<DonateDto>

    @POST("donation/verify")
    fun verifyPayment(@Body verifyPayment: VerifyPayment): Call<ResDto>

    @GET("about")
    fun about(): Call<ResDto>

}
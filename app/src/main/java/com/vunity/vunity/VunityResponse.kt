package com.vunity.vunity

import com.vunity.user.ResDto
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.*

interface VunityResponse {

    @POST("vunity/create")
    fun createVunity(@Body body: VunityBody): Call<ResDto>

    @PUT("vunity/update/{id}")
    fun updateVunity(@Path("id") id: String, @Body body: VunityBody): Call<ResDto>

    @FormUrlEncoded
    @PUT("vunity/mobile_visible_update/{id}")
    fun mobileVisibility(
        @Path("id") id: String,
        @FieldMap params: Map<String, @JvmSuppressWildcards Any>
    ): Call<ResDto>

    @Multipart
    @PUT("vunity/update_photo")
    fun updateVunityPhoto(@Part dp: MultipartBody.Part): Call<ResDto>

    @GET("vunity/get_by_user/{id}")
    fun getVunityUserById(@Path("id") id: String): Call<VunityDto>

    @GET("vunity/list")
    fun listOfVunityUsers(): Call<VunityListDto>

    @POST("vunity/filter")
    fun filterVunityUsers(@Body filterBody: FilterBody): Call<VunityListDto>

    @GET("vunity/search/{value}")
    fun searchVunityUsers(@Path("value") value: String): Call<VunityListDto>

    @GET("city/search/{value}")
    fun searchCities(@Path("value") value: String): Call<CityDto>

}
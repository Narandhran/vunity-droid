package com.vunity.video

import com.vunity.user.ResDto
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

interface VideoResponse {

    @Multipart
    @POST("video/create")
    fun addVideo(
        @Part thumbnail: MultipartBody.Part,
        @Part("textField") text: RequestBody
    ): Call<ResDto>

    @PUT("video/update/{id}")
    fun updateVideoDetails(@Path("id") id: String, @Body reqVideoBody: ReqVideoBody): Call<ResDto>

    @Multipart
    @PUT("video/update_thumb/{id}")
    fun updateVideoThumb(@Path("id") id: String, @Part thumbnail: MultipartBody.Part): Call<ResDto>

    @GET("video/homepage")
    fun getHome(): Call<HomeDto>

    @GET("video/list")
    fun getAllVideos(): Call<VideoListDto>

    @GET("video/search/{value}")
    fun searchVideos(@Path("value") value: String): Call<VideoListDto>

    @GET("video/list_category/{id}")
    fun getVideoByCategory(@Path("id") id: String): Call<VideoListDto>

    @GET("video/list_genre/{name}")
    fun getVideoByGenre(@Path("name") id: String): Call<VideoListDto>

    @POST("video/get_one")
    fun getOneVideo(@Body videoReq: ReqSingleVideoBody): Call<VideoDto>

}
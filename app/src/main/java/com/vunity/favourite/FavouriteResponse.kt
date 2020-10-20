package com.vunity.favourite

import com.vunity.user.ResDto
import retrofit2.Call
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface FavouriteResponse {

    @POST("fav/add/{id}")
    fun addFavourite(@Path("id") id: String): Call<ResDto>

    @DELETE("fav/remove/{id}")
    fun removeFavourite(@Path("id") id: String): Call<ResDto>

    @GET("fav/list")
    fun listFavourite(): Call<FavListDto>

}
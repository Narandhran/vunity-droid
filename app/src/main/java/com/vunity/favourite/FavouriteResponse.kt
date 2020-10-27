package com.vunity.favourite

import com.vunity.user.ResDto
import retrofit2.Call
import retrofit2.http.*

interface FavouriteResponse {

    @POST("fav/add")
    fun addFavourite(@Body reqFavBody: ReqFavBody): Call<ResDto>

    @HTTP(method = "DELETE", path = "fav/remove", hasBody = true)
    fun removeFavourite(@Body reqFavBody: ReqFavBody): Call<ResDto>

    @GET("fav/list")
    fun listFavourite(): Call<FavListDto>

}
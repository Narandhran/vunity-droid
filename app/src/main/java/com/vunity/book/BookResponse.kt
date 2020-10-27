package com.vunity.book

import com.vunity.user.ResDto
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

interface BookResponse {

    @Multipart
    @POST("library/create")
    fun addBook(
        @Part thumbnail: MultipartBody.Part,
        @Part pdf: MultipartBody.Part,
        @Part("textField") text: RequestBody
    ): Call<ResDto>

    @PUT("library/update/{id}")
    fun updateBook(@Path("id") id: String, @Body reqBookBody: ReqBookBody): Call<ResDto>

    @Multipart
    @PUT("library/update_thumb/{id}")
    fun updateBookImage(@Path("id") id: String, @Part thumbnail: MultipartBody.Part): Call<ResDto>

    @Multipart
    @PUT("library/update_book/{id}")
    fun updateBookPdf(@Path("id") id: String, @Part pdf: MultipartBody.Part): Call<ResDto>

    @GET("library/homepage")
    fun getHome(): Call<HomeDto>

    @GET("library/list")
    fun getAllBooks(): Call<BookListDto>

    @GET("library/search/{value}")
    fun searchBooks(@Path("value") value: String): Call<BookListDto>

    @GET("library/list_category/{id}")
    fun getBookByCategory(@Path("id") id: String): Call<BookListDto>

    @GET("library/list_genre/{name}")
    fun getBookByGenre(@Path("name") id: String): Call<BookListDto>

    @POST("library/get_one")
    fun getOneBook(@Body bookReq: ReqSingleBookBody): Call<BookDto>

}
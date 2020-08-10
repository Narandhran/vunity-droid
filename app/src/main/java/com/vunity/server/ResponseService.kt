package com.vunity.server

import com.vunity.banner.BannerDto
import com.vunity.banner.BannerListDto
import com.vunity.book.*
import com.vunity.category.CategoryDto
import com.vunity.category.CategoryListDto
import com.vunity.category.GenreDto
import com.vunity.family.*
import com.vunity.favourite.FavListDto
import com.vunity.user.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

interface ResponseService {

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

    @Multipart
    @PUT("user/update_dp")
    fun updateDp(@Part dp: MultipartBody.Part): Call<ProfileDto>

    @FormUrlEncoded
    @PUT("user/update_profile")
    fun updateProfile(@FieldMap params: Map<String, @JvmSuppressWildcards Any>): Call<ResDto>

    @GET("category/list")
    fun category(): Call<CategoryListDto>

    @Multipart
    @POST("category/create")
    fun addCategory(
        @Part category: MultipartBody.Part,
        @Part("textField") text: RequestBody
    ): Call<CategoryDto>

    @FormUrlEncoded
    @POST("genre/add")
    fun addGenre(@FieldMap params: Map<String, @JvmSuppressWildcards Any>): Call<ResDto>

    @GET("genre/list")
    fun genres(): Call<GenreDto>

    @Multipart
    @POST("library/create")
    fun addBook(
        @Part thumbnail: MultipartBody.Part,
        @Part pdf: MultipartBody.Part,
        @Part("textField") text: RequestBody
    ): Call<ResDto>

    @PUT("library/update/{id}")
    fun updateBook(@Path("id") id: String, @Body bookBody: BookBody): Call<ResDto>

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
    fun getOneBook(@Body book: SingleBook): Call<BookDto>

    @POST("fav/add/{id}")
    fun addFavourite(@Path("id") id: String): Call<ResDto>

    @DELETE("fav/remove/{id}")
    fun removeFavourite(@Path("id") id: String): Call<ResDto>

    @GET("fav/list")
    fun listFavourite(): Call<FavListDto>

    @GET("family/list_by_user/{id}")
    fun listOfFamily(@Path("id") id: String): Call<FamilyDto>

    @POST("family/create_update_personal_info/{id}")  // To create or update personal info
    fun personal(@Path("id") id: String, @Body personalInfo: PersonalInfo): Call<FamilyDto>

    @POST("family/create_family_tree/{id}")  // To create family tree
    fun familyTree(@Path("id") id: String, @Body bodyFamilyTree: FamilyBody): Call<FamilyDto>

    @POST("family/update_family_tree")  // To update family tree
    fun familyTree(
        @Query("userId") userId: String,
        @Query("treeId") treeId: String,
        @Body bodyFamilyTree: FamilyBody
    ): Call<ResDto>

    @POST("family/create_update_family_info/{id}")  // To create or update family info
    fun family(@Path("id") id: String, @Body familyInfo: FamilyInfo): Call<FamilyDto>

    @POST("family/create_update_gothram/{id}")  // To create or update gothram
    fun gothram(@Path("id") id: String, @Body gothram: Gothram): Call<FamilyDto>

    @POST("family/create_thithi/{id}")  // To create thithi
    fun thithi(@Path("id") id: String, @Body thithiData: ThithiData): Call<FamilyDto>

    @POST("family/update_thithi")  // To  update thithi
    fun thithi(
        @Query("userId") userId: String,
        @Query("thithiId") thithiId: String,
        @Body thithiData: ThithiData
    ): Call<ResDto>

    @POST("family/create_update_name/{id}")  // To create or update name
    fun name(@Path("id") id: String, @Body name: Name): Call<FamilyDto>

    @POST("family/create_update_samayal/{id}")  // To create or update samayal
    fun samayal(@Path("id") id: String, @Body samayal: Samayal): Call<FamilyDto>

    @POST("family/create_update_vazhkam/{id}")  // To create or update vazhakkam
    fun vazhakkam(@Path("id") id: String, @Body vazhakkam: Vazhakkam): Call<FamilyDto>

    @Multipart
    @POST("banner/create")
    fun addBanner(@Part image: MultipartBody.Part): Call<BannerDto>

    @Multipart
    @PUT("banner/update/{id}")
    fun updateBanner(@Path("id") id: String, @Part image: MultipartBody.Part): Call<BannerDto>

    @GET("banner/list")
    fun getBanners(): Call<BannerListDto>

    @FormUrlEncoded
    @POST("donation/donate")
    fun donate(@FieldMap params: Map<String, @JvmSuppressWildcards Any>): Call<DonateDto>

    @POST("donation/verify")
    fun verifyPayment(@Body verifyPayment: VerifyPayment): Call<ResDto>

    @GET("about")
    fun about(): Call<ResDto>
}
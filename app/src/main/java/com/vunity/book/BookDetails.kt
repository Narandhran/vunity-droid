package com.vunity.book

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.squareup.picasso.Picasso
import com.vunity.R
import com.vunity.favourite.ReqFavBody
import com.vunity.general.*
import com.vunity.reader.Reader
import com.vunity.server.InternetDetector
import com.vunity.server.RetrofitClient
import com.vunity.server.RetrofitWithBar
import com.vunity.user.ErrorMsgDto
import com.vunity.user.Login
import com.vunity.user.ResDto
import kotlinx.android.synthetic.main.act_book_details.*
import kotlinx.android.synthetic.main.toolbar.txt_title
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class BookDetails : AppCompatActivity() {

    private var internet: InternetDetector? = null
    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
    private var favorite = true
    private var books: Call<BookDto>? = null
    var isLoggedIn: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.act_book_details)

        val window: Window = this.window
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.statusBarColor = Color.TRANSPARENT
        }

        layout_refresh.setOnRefreshListener {
            finish()
            reloadActivity(this@BookDetails)
            layout_refresh.isRefreshing = false
        }

        internet = InternetDetector.getInstance(this@BookDetails)
        isLoggedIn = getData("logged_user", applicationContext).toString()

        try {
            val data = intent.getStringExtra("data")
            if (data != null) {
                val book = ReqSingleBookBody(
                    libraryId = data,
                    userId = getData("user_id", applicationContext).toString(),
                    isVideo = false
                )
                books(book)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            showMessage(lay_root, getString(R.string.unable_to_collect))
        }

        val role = getData(Enums.Role.value, applicationContext)
        if (role == Enums.Admin.value) {
            img_edit.visibility = View.VISIBLE
        } else {
            img_edit.visibility = View.GONE
        }
    }

    private fun books(bookReq: ReqSingleBookBody) {
        books = RetrofitClient.bookClient.getOneBook(bookReq)
        books?.enqueue(object : Callback<BookDto> {
            @SuppressLint("DefaultLocale", "SetTextI18n")
            override fun onResponse(
                call: Call<BookDto>,
                response: Response<BookDto>
            ) {
                when {
                    response.code() == 200 -> {
                        when (response.body()?.status) {
                            200 -> {
                                txt_title.text = response.body()!!.data.name
                                txt_author.text = response.body()!!.data.author
                                txt_description.text = response.body()!!.data.description
                                Picasso.get()
                                    .load(
                                        getData(
                                            "rootPath",
                                            this@BookDetails
                                        ) + Enums.Book.value + response.body()!!.data.thumbnail
                                    )
                                    .error(R.drawable.img_place_holder)
                                    .placeholder(R.drawable.img_place_holder)
                                    .fit()
                                    .into(img_book)

                                val isBookmark = response.body()!!.data.isBookmark
                                if (isBookmark != null) {
                                    if (response.body()!!.data.isBookmark!!) {
                                        img_bookmark.setImageDrawable(
                                            ContextCompat.getDrawable(
                                                this@BookDetails,
                                                R.drawable.ic_heart_fill
                                            )
                                        )
                                        favorite = false
                                    }
                                }

                                img_bookmark.setOnClickListener {
                                    if (isLoggedIn == getString(R.string.skip)) {
                                        val intent = Intent(this@BookDetails, Login::class.java)
                                        intent.putExtra(
                                            getString(R.string.data),
                                            getString(R.string.new_user)
                                        )
                                        startActivity(intent)
                                    } else {
                                        if (favorite) {
                                            img_bookmark.setImageDrawable(
                                                ContextCompat.getDrawable(
                                                    this@BookDetails,
                                                    R.drawable.ic_heart_fill
                                                )
                                            )
                                            addFav(response.body()!!.data._id.toString())
                                            favorite = false
                                        } else {
                                            img_bookmark.setImageDrawable(
                                                ContextCompat.getDrawable(
                                                    this@BookDetails,
                                                    R.drawable.ic_heart
                                                )
                                            )
                                            removeFav(response.body()!!.data._id.toString())
                                            favorite = true
                                        }
                                    }
                                }

                                btn_read.setOnClickListener {
                                    if (isLoggedIn == getString(R.string.skip)) {
                                        val intent = Intent(this@BookDetails, Login::class.java)
                                        intent.putExtra(
                                            getString(R.string.data),
                                            getString(R.string.new_user)
                                        )
                                        startActivity(intent)
                                    } else {
                                        val intent = Intent(this@BookDetails, Reader::class.java)
                                        intent.putExtra(
                                            this@BookDetails.getString(R.string.data),
                                            response.body()!!.data.content
                                        )
                                        startActivity(intent)
                                    }
                                }

                                img_edit.setOnClickListener {
                                    try {
                                        val jsonAdapter: JsonAdapter<BookData> =
                                            moshi.adapter(BookData::class.java)
                                        val json: String =
                                            jsonAdapter.toJson(response.body()!!.data)
                                        val intent = Intent(this@BookDetails, AddBook::class.java)
                                        intent.putExtra("data", json)
                                        startActivityForResult(intent, 1)
                                        overridePendingTransition(
                                            R.anim.fade_in,
                                            R.anim.fade_out
                                        )
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }
                            }
                            else -> {
                                showErrorMessage(
                                    lay_root,
                                    response.message()
                                )
                            }
                        }
                    }

                    response.code() == 422 || response.code() == 400 -> {
                        try {
                            val adapter: JsonAdapter<ErrorMsgDto> =
                                moshi.adapter(ErrorMsgDto::class.java)
                            val errorResponse =
                                adapter.fromJson(response.errorBody()!!.string())
                            if (errorResponse != null) {
                                if (errorResponse.status == 400) {
                                    showErrorMessage(
                                        lay_root,
                                        errorResponse.message
                                    )
                                } else {
                                    showErrorMessage(
                                        lay_root,
                                        errorResponse.message
                                    )
                                }

                            } else {
                                showErrorMessage(
                                    lay_root,
                                    getString(R.string.msg_something_wrong)
                                )
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            showErrorMessage(
                                lay_root,
                                getString(R.string.msg_something_wrong)
                            )
                        }

                    }

                    response.code() == 401 -> {
                        sessionExpired(this@BookDetails)
                    }
                    else -> {
                        showErrorMessage(
                            lay_root,
                            response.message()
                        )
                    }
                }
            }

            override fun onFailure(call: Call<BookDto>, t: Throwable) {
                if (!call.isCanceled) {
                    showErrorMessage(
                        lay_root,
                        getString(R.string.msg_something_wrong)
                    )
                }
            }
        })
    }

    private fun addFav(id: String) {
        if (internet?.checkMobileInternetConn(applicationContext)!!) {
            val reqFavBody = ReqFavBody(isVideo = false, libraryId = id)
            val addFav = RetrofitClient.favouriteClient.addFavourite(reqFavBody)
            addFav.enqueue(
                RetrofitWithBar(this@BookDetails, object : Callback<ResDto> {
                    @SuppressLint("SimpleDateFormat")
                    @RequiresApi(Build.VERSION_CODES.O)
                    override fun onResponse(call: Call<ResDto>, response: Response<ResDto>) {
                        if (response.code() == 200) {
                            when (response.body()?.status) {
                                200 -> {

                                }
                                else -> {
                                    showErrorMessage(
                                        lay_root,
                                        response.message()
                                    )
                                }
                            }

                        } else if (response.code() == 422 || response.code() == 400) {
                            try {
                                val adapter: JsonAdapter<ErrorMsgDto> =
                                    moshi.adapter(ErrorMsgDto::class.java)
                                val errorResponse =
                                    adapter.fromJson(response.errorBody()!!.string())
                                if (errorResponse != null) {
                                    if (errorResponse.status == 400) {
                                        showErrorMessage(
                                            lay_root,
                                            errorResponse.message
                                        )
                                    } else {
                                        showErrorMessage(
                                            lay_root,
                                            errorResponse.message
                                        )
                                    }

                                } else {
                                    showErrorMessage(
                                        lay_root,
                                        getString(R.string.msg_something_wrong)
                                    )
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                                showErrorMessage(
                                    lay_root,
                                    getString(R.string.msg_something_wrong)
                                )
                            }

                        } else if (response.code() == 401) {
                            sessionExpired(
                                this@BookDetails
                            )
                        } else {
                            showErrorMessage(
                                lay_root,
                                response.message()
                            )
                        }
                    }

                    override fun onFailure(call: Call<ResDto>, t: Throwable) {
                        showErrorMessage(
                            lay_root,
                            getString(R.string.msg_something_wrong)
                        )
                    }
                })
            )

        } else {
            showErrorMessage(
                lay_root,
                getString(R.string.msg_no_internet)
            )
        }
    }

    private fun removeFav(id: String) {
        if (internet?.checkMobileInternetConn(applicationContext)!!) {
            val reqFavBody = ReqFavBody(isVideo = false, libraryId = id)
            val removeFav = RetrofitClient.favouriteClient.removeFavourite(reqFavBody)
            removeFav.enqueue(
                RetrofitWithBar(this@BookDetails, object : Callback<ResDto> {
                    @SuppressLint("SimpleDateFormat")
                    @RequiresApi(Build.VERSION_CODES.O)
                    override fun onResponse(
                        call: Call<ResDto>,
                        response: Response<ResDto>
                    ) {
                        if (response.code() == 200) {
                            when (response.body()?.status) {
                                200 -> {

                                }
                                else -> {
                                    showErrorMessage(
                                        lay_root,
                                        response.message()
                                    )
                                }
                            }

                        } else if (response.code() == 422 || response.code() == 400) {
                            try {
                                val adapter: JsonAdapter<ErrorMsgDto> =
                                    moshi.adapter(ErrorMsgDto::class.java)
                                val errorResponse =
                                    adapter.fromJson(response.errorBody()!!.string())
                                if (errorResponse != null) {
                                    if (errorResponse.status == 400) {
                                        showErrorMessage(
                                            lay_root,
                                            errorResponse.message
                                        )
                                    } else {
                                        showErrorMessage(
                                            lay_root,
                                            errorResponse.message
                                        )
                                    }

                                } else {
                                    showErrorMessage(
                                        lay_root,
                                        getString(R.string.msg_something_wrong)
                                    )
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                                showErrorMessage(
                                    lay_root,
                                    getString(R.string.msg_something_wrong)
                                )
                            }

                        } else if (response.code() == 401) {
                            sessionExpired(
                                this@BookDetails
                            )
                        } else {
                            showErrorMessage(
                                lay_root,
                                response.message()
                            )
                        }
                    }

                    override fun onFailure(call: Call<ResDto>, t: Throwable) {
                        showErrorMessage(
                            lay_root,
                            getString(R.string.msg_something_wrong)
                        )
                    }
                })
            )

        } else {
            showErrorMessage(
                lay_root,
                getString(R.string.msg_no_internet)
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                if (data != null) {
                    try {
                        val id = data.getStringExtra(getString(R.string.data))
                        if (id != null) {
                            val book = ReqSingleBookBody(
                                libraryId = id,
                                userId = getData("user_id", applicationContext).toString(),
                                isVideo = false
                            )
                            books(book)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    override fun onRestart() {
        super.onRestart()
        try {
            val data = intent.getStringExtra("data")
            if (data != null) {
                val book = ReqSingleBookBody(
                    libraryId = data,
                    userId = getData("user_id", applicationContext).toString(),
                    isVideo = false
                )
                books(book)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            showMessage(lay_root, getString(R.string.unable_to_collect))
        }
    }

    override fun onBackPressed() {
        val intent = Intent()
        setResult(Activity.RESULT_OK, intent)
        super.onBackPressed()
    }
}

package com.vunity.book

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.GridLayoutManager
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.vunity.R
import com.vunity.general.Home
import com.vunity.general.coordinatorErrorMessage
import com.vunity.general.reloadActivity
import com.vunity.general.sessionExpired
import com.vunity.server.InternetDetector
import com.vunity.server.RetrofitClient
import com.vunity.user.ErrorMsgDto
import kotlinx.android.synthetic.main.act_search.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class BookSearch : AppCompatActivity() {

    private var internet: InternetDetector? = null
    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
    private var books: Call<BookListDto>? = null

    val spanCount = 3 //  columns
    val spacing = 15 // pixel
    val includeEdge = true

    private var queryTextListener: SearchView.OnQueryTextListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.act_search)

        layout_refresh.setOnRefreshListener {
            finish()
            reloadActivity(this@BookSearch)
            layout_refresh.isRefreshing = false
        }

        im_back.setOnClickListener {
            val intent = Intent(this@BookSearch, Home::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.bottom_up, R.anim.nothing)
        }

        internet = InternetDetector.getInstance(this@BookSearch)
        allBooks()

        queryTextListener = object : SearchView.OnQueryTextListener {
            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText.equals("")) {
                    allBooks()
                } else {
                    if (internet?.checkMobileInternetConn(this@BookSearch)!!) {
                        searchInBooks(newText!!)
                    } else {
                        coordinatorErrorMessage(layout_refresh, getString(R.string.msg_no_internet))
                    }
                }
                return true
            }

            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query.equals("")) {
                    allBooks()
                } else {
                    if (internet?.checkMobileInternetConn(this@BookSearch)!!) {
                        searchInBooks(query!!)
                    } else {
                        coordinatorErrorMessage(layout_refresh, getString(R.string.msg_no_internet))
                    }
                }
                return true
            }
        }
        search_book.setOnQueryTextListener(queryTextListener)

    }

    override fun onStop() {
        super.onStop()
        if (books != null) {
            books?.cancel()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.bottom_up, R.anim.nothing)
    }

    private fun allBooks() {
        if (!lay_shimmer.isShimmerStarted) {
            lay_shimmer.startShimmer()
        }
        if (internet?.checkMobileInternetConn(applicationContext)!!) {
            books = RetrofitClient.bookClient.getAllBooks()
            books?.enqueue(object : Callback<BookListDto> {
                @SuppressLint("DefaultLocale", "SetTextI18n")
                override fun onResponse(
                    call: Call<BookListDto>,
                    response: Response<BookListDto>
                ) {
                    when {
                        response.code() == 200 -> {
                            when (response.body()?.status) {
                                200 -> {
                                    val bookData = response.body()!!.data.toMutableList()
                                    lay_no_data.visibility = View.GONE
                                    lay_no_internet.visibility = View.GONE
                                    lay_data.visibility = View.VISIBLE
                                    view_search?.apply {
                                        view_search?.layoutManager = GridLayoutManager(
                                            applicationContext,
                                            spanCount,
                                            GridLayoutManager.VERTICAL,
                                            false
                                        )
                                        view_search?.setHasFixedSize(true)
                                        val searchAdapter =
                                            BookSearchAdapter(bookData, this@BookSearch)
                                        view_search?.adapter = searchAdapter
                                    }
                                }
                                204 -> {
                                    lay_no_data.visibility = View.VISIBLE
                                    lay_data.visibility = View.GONE
                                    lay_no_internet.visibility = View.GONE
                                }
                                else -> {
                                    coordinatorErrorMessage(
                                        layout_refresh,
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
                                        coordinatorErrorMessage(
                                            layout_refresh,
                                            errorResponse.message
                                        )
                                    } else {
                                        coordinatorErrorMessage(
                                            layout_refresh,
                                            errorResponse.message
                                        )
                                    }

                                } else {
                                    coordinatorErrorMessage(
                                        layout_refresh,
                                        getString(R.string.msg_something_wrong)
                                    )
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                                coordinatorErrorMessage(
                                    layout_refresh,
                                    getString(R.string.msg_something_wrong)
                                )
                            }

                        }

                        response.code() == 401 -> {
                            sessionExpired(this@BookSearch)
                        }
                        else -> {
                            coordinatorErrorMessage(
                                layout_refresh,
                                response.message()
                            )
                        }
                    }
                    lay_shimmer.visibility = View.GONE
                    lay_shimmer.stopShimmer()
                }

                override fun onFailure(call: Call<BookListDto>, t: Throwable) {
                    if (!call.isCanceled) {
                        coordinatorErrorMessage(
                            layout_refresh,
                            getString(R.string.msg_something_wrong)
                        )
                        lay_shimmer.visibility = View.GONE
                        lay_shimmer.stopShimmer()
                    }
                }
            })

        } else {
            lay_shimmer.visibility = View.GONE
            lay_shimmer.stopShimmer()
            lay_no_data.visibility = View.GONE
            lay_data.visibility = View.GONE
            lay_no_internet.visibility = View.VISIBLE
        }
    }

    private fun searchInBooks(value: String) {
        if (!lay_shimmer.isShimmerStarted) {
            lay_shimmer.startShimmer()
        }
        if (internet?.checkMobileInternetConn(applicationContext)!!) {
            books = RetrofitClient.bookClient.searchBooks(value)
            books?.enqueue(object : Callback<BookListDto> {
                @SuppressLint("DefaultLocale", "SetTextI18n")
                override fun onResponse(
                    call: Call<BookListDto>,
                    response: Response<BookListDto>
                ) {
                    when {
                        response.code() == 200 -> {
                            when (response.body()?.status) {
                                200 -> {
                                    val bookData = response.body()!!.data.toMutableList()
                                    lay_no_data.visibility = View.GONE
                                    lay_no_internet.visibility = View.GONE
                                    lay_data.visibility = View.VISIBLE
                                    view_search?.apply {
                                        view_search?.layoutManager = GridLayoutManager(
                                            applicationContext,
                                            spanCount,
                                            GridLayoutManager.VERTICAL,
                                            false
                                        )
                                        view_search?.setHasFixedSize(true)
                                        val searchAdapter =
                                            BookSearchAdapter(bookData, this@BookSearch)
                                        view_search?.adapter = searchAdapter
                                    }
                                }
                                204 -> {
                                    lay_no_data.visibility = View.VISIBLE
                                    lay_data.visibility = View.GONE
                                    lay_no_internet.visibility = View.GONE
                                }
                                else -> {
                                    coordinatorErrorMessage(
                                        layout_refresh,
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
                                        coordinatorErrorMessage(
                                            layout_refresh,
                                            errorResponse.message
                                        )
                                    } else {
                                        coordinatorErrorMessage(
                                            layout_refresh,
                                            errorResponse.message
                                        )
                                    }

                                } else {
                                    coordinatorErrorMessage(
                                        layout_refresh,
                                        getString(R.string.msg_something_wrong)
                                    )
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                                coordinatorErrorMessage(
                                    layout_refresh,
                                    getString(R.string.msg_something_wrong)
                                )
                            }

                        }

                        response.code() == 401 -> {
                            sessionExpired(this@BookSearch)
                        }
                        else -> {
                            coordinatorErrorMessage(
                                layout_refresh,
                                response.message()
                            )
                        }
                    }
                    lay_shimmer.visibility = View.GONE
                    lay_shimmer.stopShimmer()
                }

                override fun onFailure(call: Call<BookListDto>, t: Throwable) {
                    if (!call.isCanceled) {
                        coordinatorErrorMessage(
                            layout_refresh,
                            getString(R.string.msg_something_wrong)
                        )
                        lay_shimmer.visibility = View.GONE
                        lay_shimmer.stopShimmer()
                    }
                }
            })

        } else {
            lay_shimmer.visibility = View.GONE
            lay_shimmer.stopShimmer()
            lay_no_data.visibility = View.GONE
            lay_data.visibility = View.GONE
            lay_no_internet.visibility = View.VISIBLE
        }
    }
}

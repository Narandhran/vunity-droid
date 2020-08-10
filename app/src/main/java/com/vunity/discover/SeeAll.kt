package com.vunity.discover

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.vunity.R
import com.vunity.book.BookData
import com.vunity.book.BookListDto
import com.vunity.category.CategoryAdapter
import com.vunity.category.CategoryData
import com.vunity.category.CategoryListDto
import com.vunity.general.GridSpacingItemDecoration
import com.vunity.general.reloadActivity
import com.vunity.general.sessionExpired
import com.vunity.general.showErrorMessage
import com.vunity.server.InternetDetector
import com.vunity.server.RetrofitClient
import com.vunity.user.ErrorMsgDto
import kotlinx.android.synthetic.main.act_see_all.*
import org.apache.commons.lang3.StringUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class SeeAll : AppCompatActivity() {

    private var internet: InternetDetector? = null
    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
    var loadCategory: Call<CategoryListDto>? = null
    var categoryData: MutableList<CategoryData> = arrayListOf()
    lateinit var categoryAdapter: CategoryAdapter
    var loadBooks: Call<BookListDto>? = null
    var bookData: MutableList<BookData> = arrayListOf()
    lateinit var searchAdapter: SearchAdapter

    val spanCount = 3 //  columns
    val spacing = 15 // pixel
    val includeEdge = true
    private var ascending = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.act_see_all)

        layout_refresh.setOnRefreshListener {
            finish()
            reloadActivity(this@SeeAll)
            layout_refresh.isRefreshing = false
        }

        im_back.setOnClickListener {
            onBackPressed()
            overridePendingTransition(R.anim.bottom_up, R.anim.nothing)
        }

        internet = InternetDetector.getInstance(this@SeeAll)

        try {
            val data = intent.getStringExtra(getString(R.string.data))
            if (data != null) {
                when (data) {
                    getString(R.string.loadAllCategory) -> {
                        if (!lay_shimmer.isShimmerStarted) {
                            lay_shimmer.startShimmer()
                        }
                        loadCategory()
                    }
                    getString(R.string.loadBooksByGenre) -> {
                        if (!lay_shimmer.isShimmerStarted) {
                            lay_shimmer.startShimmer()
                        }
                        val title = intent.getStringExtra(getString(R.string.title))
                        if (title != null) {
                            loadBooksByGenre(title)
                        }
                    }

                    getString(R.string.loadBooksByCategory) -> {
                        if (!lay_shimmer.isShimmerStarted) {
                            lay_shimmer.startShimmer()
                        }
                        val title = intent.getStringExtra(getString(R.string.name))
                        val id = intent.getStringExtra(getString(R.string.id))
                        if (title != null && id != null) {
                            loadBookByCategory(title = title, id = id)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            println("Timer Ex: $e")
        }
    }

    override fun onStop() {
        super.onStop()
        if (loadCategory != null) {
            loadCategory?.cancel()
        }
        if (loadBooks != null) {
            loadBooks?.cancel()
        }
    }

    private fun loadCategory() {
        if (internet?.checkMobileInternetConn(applicationContext)!!) {
            loadCategory = RetrofitClient.instanceClient.category()
            loadCategory?.enqueue(object : Callback<CategoryListDto> {
                @SuppressLint("DefaultLocale", "SetTextI18n")
                override fun onResponse(
                    call: Call<CategoryListDto>,
                    response: Response<CategoryListDto>
                ) {
                    Log.e("onResponse", response.toString())
                    when {
                        response.code() == 200 -> {
                            when (response.body()?.status) {
                                200 -> {
                                    txt_title.text = getString(R.string.category)
                                    categoryData = response.body()!!.data.toMutableList()
                                    lay_no_data.visibility = View.GONE
                                    lay_no_internet.visibility = View.GONE
                                    lay_data.visibility = View.VISIBLE
                                    view_more?.apply {
                                        view_more?.layoutManager = GridLayoutManager(
                                            applicationContext,
                                            spanCount,
                                            GridLayoutManager.VERTICAL,
                                            false
                                        )
                                        view_more?.addItemDecoration(
                                            GridSpacingItemDecoration(
                                                spanCount,
                                                spacing,
                                                includeEdge
                                            )
                                        )
                                        view_more?.setHasFixedSize(true)
                                        categoryAdapter =
                                            CategoryAdapter(categoryData, this@SeeAll)
                                        view_more?.adapter = categoryAdapter

                                        im_sort.setOnClickListener {
                                            im_sort.animate()
                                                .rotationX(360f).rotationY(360f)
                                                .setDuration(500)
                                                .setInterpolator(LinearInterpolator())
                                                .setListener(object : AnimatorListenerAdapter() {
                                                    override fun onAnimationEnd(animator: Animator) {
                                                        im_sort.rotationX = 0f
                                                        im_sort.rotationY = 0f
                                                    }
                                                })
                                            if (ascending) {
                                                categoryData.sortBy { it.name }
                                                categoryAdapter =
                                                    CategoryAdapter(categoryData, this@SeeAll)
                                                view_more?.adapter = categoryAdapter
                                                ascending = false
                                            } else {
                                                categoryData.reverse()
                                                categoryAdapter =
                                                    CategoryAdapter(categoryData, this@SeeAll)
                                                view_more?.adapter = categoryAdapter
                                                ascending = true
                                            }
                                        }
                                    }
                                }
                                204 -> {
                                    lay_no_data.visibility = View.VISIBLE
                                    lay_data.visibility = View.GONE
                                    lay_no_internet.visibility = View.GONE
                                }
                                else -> {
                                    showErrorMessage(
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
                                        showErrorMessage(
                                            layout_refresh,
                                            errorResponse.message
                                        )
                                    } else {
                                        showErrorMessage(
                                            layout_refresh,
                                            errorResponse.message
                                        )
                                    }

                                } else {
                                    showErrorMessage(
                                        layout_refresh,
                                        getString(R.string.msg_something_wrong)
                                    )
                                    Log.e(
                                        "Response",
                                        response.body()!!.toString()
                                    )
                                }
                            } catch (e: Exception) {
                                showErrorMessage(
                                    layout_refresh,
                                    getString(R.string.msg_something_wrong)
                                )
                                Log.e("Exception", e.toString())
                            }

                        }

                        response.code() == 401 -> {
                            sessionExpired(this@SeeAll)
                        }
                        else -> {
                            showErrorMessage(
                                layout_refresh,
                                response.message()
                            )
                        }
                    }
                    lay_shimmer.visibility = View.GONE
                    lay_shimmer.stopShimmer()
                }

                override fun onFailure(call: Call<CategoryListDto>, t: Throwable) {
                    Log.e("onFailure", t.message.toString())
                    if (!call.isCanceled) {
                        showErrorMessage(
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

    private fun loadBooksByGenre(title: String) {
        if (internet?.checkMobileInternetConn(applicationContext)!!) {
            loadBooks = RetrofitClient.instanceClient.getBookByGenre(title)
            loadBooks?.enqueue(object : Callback<BookListDto> {
                @SuppressLint("DefaultLocale", "SetTextI18n")
                override fun onResponse(
                    call: Call<BookListDto>,
                    response: Response<BookListDto>
                ) {
                    Log.e("onResponse", response.toString())
                    when {
                        response.code() == 200 -> {
                            when (response.body()?.status) {
                                200 -> {
                                    txt_title.text = StringUtils.capitalize(title)
                                    bookData = response.body()!!.data.toMutableList()
                                    lay_no_data.visibility = View.GONE
                                    lay_no_internet.visibility = View.GONE
                                    lay_data.visibility = View.VISIBLE
                                    view_more?.apply {
                                        view_more?.layoutManager = GridLayoutManager(
                                            applicationContext,
                                            spanCount,
                                            GridLayoutManager.VERTICAL,
                                            false
                                        )
                                        view_more?.addItemDecoration(
                                            GridSpacingItemDecoration(
                                                spanCount,
                                                spacing,
                                                includeEdge
                                            )
                                        )
                                        view_more?.setHasFixedSize(true)
                                        searchAdapter =
                                            SearchAdapter(bookData, this@SeeAll)
                                        view_more?.adapter = searchAdapter

                                        im_sort.setOnClickListener {
                                            im_sort.animate()
                                                .rotationX(360f).rotationY(360f)
                                                .setDuration(500)
                                                .setInterpolator(LinearInterpolator())
                                                .setListener(object : AnimatorListenerAdapter() {
                                                    override fun onAnimationEnd(animator: Animator) {
                                                        im_sort.rotationX = 0f
                                                        im_sort.rotationY = 0f
                                                    }
                                                })
                                            if (ascending) {
                                                bookData.sortBy { it.name }
                                                searchAdapter =
                                                    SearchAdapter(bookData, this@SeeAll)
                                                view_more?.adapter = searchAdapter
                                                ascending = false
                                            } else {
                                                bookData.reverse()
                                                searchAdapter =
                                                    SearchAdapter(bookData, this@SeeAll)
                                                view_more?.adapter = searchAdapter
                                                ascending = true
                                            }
                                        }
                                    }
                                }
                                204 -> {
                                    lay_no_data.visibility = View.VISIBLE
                                    lay_data.visibility = View.GONE
                                    lay_no_internet.visibility = View.GONE
                                }
                                else -> {
                                    showErrorMessage(
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
                                        showErrorMessage(
                                            layout_refresh,
                                            errorResponse.message
                                        )
                                    } else {
                                        showErrorMessage(
                                            layout_refresh,
                                            errorResponse.message
                                        )
                                    }

                                } else {
                                    showErrorMessage(
                                        layout_refresh,
                                        getString(R.string.msg_something_wrong)
                                    )
                                    Log.e(
                                        "Response",
                                        response.body()!!.toString()
                                    )
                                }
                            } catch (e: Exception) {
                                showErrorMessage(
                                    layout_refresh,
                                    getString(R.string.msg_something_wrong)
                                )
                                Log.e("Exception", e.toString())
                            }

                        }

                        response.code() == 401 -> {
                            sessionExpired(this@SeeAll)
                        }
                        else -> {
                            showErrorMessage(
                                layout_refresh,
                                response.message()
                            )
                        }
                    }
                    lay_shimmer.visibility = View.GONE
                    lay_shimmer.stopShimmer()
                }

                override fun onFailure(call: Call<BookListDto>, t: Throwable) {
                    Log.e("onFailure", t.message.toString())
                    if (!call.isCanceled) {
                        showErrorMessage(
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

    private fun loadBookByCategory(title: String, id: String) {
        if (internet?.checkMobileInternetConn(applicationContext)!!) {
            loadBooks = RetrofitClient.instanceClient.getBookByCategory(id)
            loadBooks?.enqueue(object : Callback<BookListDto> {
                @SuppressLint("DefaultLocale", "SetTextI18n")
                override fun onResponse(
                    call: Call<BookListDto>,
                    response: Response<BookListDto>
                ) {
                    Log.e("onResponse", response.toString())
                    when {
                        response.code() == 200 -> {
                            when (response.body()?.status) {
                                200 -> {
                                    txt_title.text = title
                                    bookData = response.body()!!.data.toMutableList()
                                    lay_no_data.visibility = View.GONE
                                    lay_no_internet.visibility = View.GONE
                                    lay_data.visibility = View.VISIBLE
                                    view_more?.apply {
                                        view_more?.layoutManager = GridLayoutManager(
                                            applicationContext,
                                            spanCount,
                                            GridLayoutManager.VERTICAL,
                                            false
                                        )
                                        view_more?.addItemDecoration(
                                            GridSpacingItemDecoration(
                                                spanCount,
                                                spacing,
                                                includeEdge
                                            )
                                        )
                                        view_more?.setHasFixedSize(true)
                                        searchAdapter =
                                            SearchAdapter(bookData, this@SeeAll)
                                        view_more?.adapter = searchAdapter

                                        im_sort.setOnClickListener {
                                            im_sort.animate()
                                                .rotationX(360f).rotationY(360f)
                                                .setDuration(500)
                                                .setInterpolator(LinearInterpolator())
                                                .setListener(object : AnimatorListenerAdapter() {
                                                    override fun onAnimationEnd(animator: Animator) {
                                                        im_sort.rotationX = 0f
                                                        im_sort.rotationY = 0f
                                                    }
                                                })
                                            if (ascending) {
                                                bookData.sortBy { it.name }
                                                searchAdapter =
                                                    SearchAdapter(bookData, this@SeeAll)
                                                view_more?.adapter = searchAdapter
                                                ascending = false
                                            } else {
                                                bookData.reverse()
                                                searchAdapter =
                                                    SearchAdapter(bookData, this@SeeAll)
                                                view_more?.adapter = searchAdapter
                                                ascending = true
                                            }
                                        }
                                    }
                                }
                                204 -> {
                                    lay_no_data.visibility = View.VISIBLE
                                    lay_data.visibility = View.GONE
                                    lay_no_internet.visibility = View.GONE
                                }
                                else -> {
                                    showErrorMessage(
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
                                        showErrorMessage(
                                            layout_refresh,
                                            errorResponse.message
                                        )
                                    } else {
                                        showErrorMessage(
                                            layout_refresh,
                                            errorResponse.message
                                        )
                                    }

                                } else {
                                    showErrorMessage(
                                        layout_refresh,
                                        getString(R.string.msg_something_wrong)
                                    )
                                    Log.e(
                                        "Response",
                                        response.body()!!.toString()
                                    )
                                }
                            } catch (e: Exception) {
                                showErrorMessage(
                                    layout_refresh,
                                    getString(R.string.msg_something_wrong)
                                )
                                Log.e("Exception", e.toString())
                            }

                        }

                        response.code() == 401 -> {
                            sessionExpired(this@SeeAll)
                        }
                        else -> {
                            showErrorMessage(
                                layout_refresh,
                                response.message()
                            )
                        }
                    }
                    lay_shimmer.visibility = View.GONE
                    lay_shimmer.stopShimmer()
                }

                override fun onFailure(call: Call<BookListDto>, t: Throwable) {
                    Log.e("onFailure", t.message.toString())
                    if (!call.isCanceled) {
                        showErrorMessage(
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

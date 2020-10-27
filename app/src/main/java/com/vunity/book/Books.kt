package com.vunity.book

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.squareup.picasso.Picasso
import com.vunity.R
import com.vunity.banner.BannerListDto
import com.vunity.category.CategoryAdapter
import com.vunity.category.CategoryData
import com.vunity.category.CategoryListDto
import com.vunity.general.*
import com.vunity.interfaces.IOnBackPressed
import com.vunity.server.InternetDetector
import com.vunity.server.RetrofitClient
import com.vunity.user.ErrorMsgDto
import com.vunity.user.Login
import com.vunity.user.Profile
import kotlinx.android.synthetic.main.act_home.*
import kotlinx.android.synthetic.main.frag_discover.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*


class Books : Fragment(), IOnBackPressed {

    private var category: Call<CategoryListDto>? = null
    private var banner: Call<BannerListDto>? = null
    private var books: Call<HomeDto>? = null
    var internetDetector: InternetDetector? = null
    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
    var categoryData: MutableList<CategoryData> = arrayListOf()
    var homeData: MutableList<HomeData> = arrayListOf()

    val handler = Handler()
    private var timer: Timer? = Timer()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.frag_discover, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().navigationView.visibility = View.VISIBLE
        view.layout_refresh.setOnRefreshListener {
            reloadFragment(
                activity?.supportFragmentManager!!,
                this@Books
            )
            view.layout_refresh.isRefreshing = false
        }

        Picasso.get()
            .load(
                getData(
                    "rootPath",
                    requireContext()
                ) + Enums.Dp.value + getData("dp", requireContext())
            )
            .error(R.drawable.ic_dummy_profile)
            .placeholder(R.drawable.ic_dummy_profile)
            .into(view.img_profile)

        view.img_profile.setOnClickListener {
            val isLoggedIn = getData("logged_user", requireContext())
            if (isLoggedIn == getString(R.string.skip)) {
                val intent = Intent(requireActivity(), Login::class.java)
                intent.putExtra(getString(R.string.data), getString(R.string.new_user))
                requireActivity().startActivity(intent)
            } else {
                requireActivity().startActivity(Intent(requireActivity(), Profile::class.java))
            }
        }

        internetDetector = InternetDetector.getInstance(activity!!)
        if (internetDetector?.checkMobileInternetConn(requireActivity())!!) {
            category(view)
            books(view)
            banner(view)
        } else {
            view.lay_shimmer.visibility = View.GONE
            view.lay_shimmer.stopShimmer()
            view.lay_no_data.visibility = View.GONE
            view.lay_data.visibility = View.GONE
            view.lay_no_internet.visibility = View.VISIBLE
        }

        view.btn_seeall.setOnClickListener {
            val intent = Intent(requireActivity(), BookSeeAll::class.java)
            intent.putExtra(getString(R.string.data), getString(R.string.loadAllCategory))
            startActivity(intent)
        }

        view.txt_search.setOnClickListener {
            val intent = Intent(requireActivity(), BookSearch::class.java)
            startActivity(intent)
            requireActivity().overridePendingTransition(R.anim.bottom_up, R.anim.nothing)
        }
    }

    companion object {
        fun newInstance(): Books = Books()
    }

    override fun onBackPressed(): Boolean {
        return false
    }

    private fun banner(view: View) {
        if (!view.lay_shimmer.isShimmerStarted) {
            view.lay_shimmer.startShimmer()
        }
        banner = RetrofitClient.bannerClient.getBanners()
        banner?.enqueue(object : Callback<BannerListDto> {
            @SuppressLint("DefaultLocale", "SetTextI18n")
            override fun onResponse(
                call: Call<BannerListDto>,
                response: Response<BannerListDto>
            ) {
                when {
                    response.code() == 200 -> {
                        when (response.body()?.status) {
                            200 -> {
                                val banner = arrayListOf<String>()
                                for (i in response.body()!!.data?.toMutableList()!!) {
                                    banner.add(i.banner.toString())
                                }
                                view.view_pager.adapter = ViewPagerAdapter(
                                    requireActivity(),
                                    response.body()!!.data?.toMutableList()!!
                                )
                                view.worm_dots_indicator.setViewPager(view.view_pager)
                                try {
                                    var currentPage = 0
                                    val update = Runnable {
                                        if (currentPage == banner.size) {
                                            currentPage = 0
                                        }
                                        view.view_pager.setCurrentItem(currentPage++, true)
                                    }
                                    timer?.schedule(object : TimerTask() {
                                        override fun run() {
                                            handler.post(update)
                                        }
                                    }, 1000, 3000)
                                } catch (exception: java.lang.Exception) {
                                    exception.printStackTrace()
                                }
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
                                        view.lay_root,
                                        errorResponse.message
                                    )
                                } else {
                                    coordinatorErrorMessage(
                                        view.lay_root,
                                        errorResponse.message
                                    )
                                }

                            } else {
                                coordinatorErrorMessage(
                                    view.lay_root,
                                    getString(R.string.msg_something_wrong)
                                )
                            }
                        } catch (e: Exception) {
                            coordinatorErrorMessage(
                                view.lay_root,
                                getString(R.string.msg_something_wrong)
                            )
                        }

                    }

                    response.code() == 401 -> {
                        sessionExpired(activity!!)
                    }
                    else -> {
                        coordinatorErrorMessage(
                            view.lay_root,
                            response.message()
                        )
                    }
                }
                view.lay_shimmer.visibility = View.GONE
                view.lay_shimmer.stopShimmer()
            }

            override fun onFailure(call: Call<BannerListDto>, t: Throwable) {
                if (!call.isCanceled) {
                    coordinatorErrorMessage(
                        view.lay_root,
                        getString(R.string.msg_something_wrong)
                    )
                    view.lay_shimmer.visibility = View.GONE
                    view.lay_shimmer.stopShimmer()
                }
            }
        })
    }

    private fun category(view: View) {
        if (!view.lay_shimmer.isShimmerStarted) {
            view.lay_shimmer.startShimmer()
        }
        category = RetrofitClient.categoryClient.category()
        category?.enqueue(object : Callback<CategoryListDto> {
            @SuppressLint("DefaultLocale", "SetTextI18n")
            override fun onResponse(
                call: Call<CategoryListDto>,
                response: Response<CategoryListDto>
            ) {
                when {
                    response.code() == 200 -> {
                        when (response.body()?.status) {
                            200 -> {
                                categoryData = response.body()!!.data.toMutableList()
                                view.lay_no_data.visibility = View.GONE
                                view.lay_no_internet.visibility = View.GONE
                                view.lay_data.visibility = View.VISIBLE
                                view.view_category?.apply {
                                    view.view_category?.layoutManager =
                                        LinearLayoutManager(
                                            requireContext(),
                                            LinearLayoutManager.HORIZONTAL,
                                            false
                                        )
                                    view.view_category?.setHasFixedSize(true)
                                    val categoryAdapter =
                                        CategoryAdapter(categoryData, requireActivity(), true)
                                    view.view_category?.adapter = categoryAdapter
                                }
                            }
                            204 -> {
                                view.lay_no_data.visibility = View.VISIBLE
                                view.lay_data.visibility = View.GONE
                                view.lay_no_internet.visibility = View.GONE
                            }
//                            else -> {
//                                coordinatorErrorMessage(
//                                    view.lay_root,
//                                    response.message()
//                                )
//                            }
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
                                        view.lay_root,
                                        errorResponse.message
                                    )
                                } else {
                                    coordinatorErrorMessage(
                                        view.lay_root,
                                        errorResponse.message
                                    )
                                }

                            } else {
                                coordinatorErrorMessage(
                                    view.lay_root,
                                    getString(R.string.msg_something_wrong)
                                )
                            }
                        } catch (e: Exception) {
                            coordinatorErrorMessage(
                                view.lay_root,
                                getString(R.string.msg_something_wrong)
                            )
                        }

                    }

                    response.code() == 401 -> {
                        sessionExpired(activity!!)
                    }
                    else -> {
                        coordinatorErrorMessage(
                            view.lay_root,
                            response.message()
                        )
                    }
                }
                view.lay_shimmer.visibility = View.GONE
                view.lay_shimmer.stopShimmer()
            }

            override fun onFailure(call: Call<CategoryListDto>, t: Throwable) {
                if (!call.isCanceled) {
                    coordinatorErrorMessage(
                        view.lay_root,
                        getString(R.string.msg_something_wrong)
                    )
                    view.lay_shimmer.visibility = View.GONE
                    view.lay_shimmer.stopShimmer()
                }
            }
        })
    }

    private fun books(view: View) {
        if (!view.lay_shimmer.isShimmerStarted) {
            view.lay_shimmer.startShimmer()
        }
        books = RetrofitClient.bookClient.getHome()
        books?.enqueue(object : Callback<HomeDto> {
            @SuppressLint("DefaultLocale", "SetTextI18n")
            override fun onResponse(
                call: Call<HomeDto>,
                response: Response<HomeDto>
            ) {
                when {
                    response.code() == 200 -> {
                        when (response.body()?.status) {
                            200 -> {
                                homeData = response.body()!!.data.toMutableList()
                                view.view_book_parent?.apply {
                                    view.view_book_parent?.layoutManager =
                                        LinearLayoutManager(
                                            requireContext(),
                                            LinearLayoutManager.VERTICAL,
                                            false
                                        )
                                    view.view_book_parent?.setHasFixedSize(true)
                                    val bookParentAdapter =
                                        BookParentAdapter(homeData, requireActivity())
                                    view.view_book_parent?.adapter = bookParentAdapter
                                }
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
                                        view.lay_root,
                                        errorResponse.message
                                    )
                                } else {
                                    coordinatorErrorMessage(
                                        view.lay_root,
                                        errorResponse.message
                                    )
                                }

                            } else {
                                coordinatorErrorMessage(
                                    view.lay_root,
                                    getString(R.string.msg_something_wrong)
                                )
                            }
                        } catch (e: Exception) {
                            coordinatorErrorMessage(
                                view.lay_root,
                                getString(R.string.msg_something_wrong)
                            )
                        }

                    }

                    response.code() == 401 -> {
                        sessionExpired(activity!!)
                    }
                    else -> {
                        coordinatorErrorMessage(
                            view.lay_root,
                            response.message()
                        )
                    }
                }
                view.lay_shimmer.visibility = View.GONE
                view.lay_shimmer.stopShimmer()
            }

            override fun onFailure(call: Call<HomeDto>, t: Throwable) {
                if (!call.isCanceled) {
                    coordinatorErrorMessage(
                        view.lay_root,
                        getString(R.string.msg_something_wrong)
                    )
                    view.lay_shimmer.visibility = View.GONE
                    view.lay_shimmer.stopShimmer()
                }
            }
        })
    }

    override fun onStop() {
        super.onStop()
        try {
            timer!!.cancel()
            timer!!.purge()
            timer = null
        } catch (e: Exception) {
            println("Timer Ex: $e")
        }

        if (category != null) {
            category?.cancel()
        }
        if (books != null) {
            books?.cancel()
        }
        if (banner != null) {
            banner?.cancel()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            timer!!.cancel()
            timer!!.purge()
            timer = null
        } catch (e: Exception) {
            println("Timer Ex: $e")
        }
        if (category != null) {
            category?.cancel()
        }
        if (books != null) {
            books?.cancel()
        }
        if (banner != null) {
            banner?.cancel()
        }
    }

    override fun onPause() {
        super.onPause()
        try {
            timer!!.cancel()
            timer!!.purge()
            timer = null
        } catch (e: Exception) {
            println("Timer Ex: $e")
        }
        if (category != null) {
            category?.cancel()
        }
        if (books != null) {
            books?.cancel()
        }
        if (banner != null) {
            banner?.cancel()
        }
    }

}
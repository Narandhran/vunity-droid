package com.vunity.discover

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
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
import com.vunity.banner.Banner
import com.vunity.banner.BannerListDto
import com.vunity.book.BookParentAdapter
import com.vunity.book.HomeData
import com.vunity.book.HomeDto
import com.vunity.category.CategoryAdapter
import com.vunity.category.CategoryData
import com.vunity.category.CategoryListDto
import com.vunity.general.*
import com.vunity.interfaces.IOnBackPressed
import com.vunity.server.InternetDetector
import com.vunity.server.RetrofitClient
import com.vunity.user.ErrorMsgDto
import com.vunity.user.Login
import com.vunity.user.ProDto
import com.vunity.user.Profile
import kotlinx.android.synthetic.main.frag_discover.view.*
import org.apache.commons.lang3.StringUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*


class Discover : Fragment(), IOnBackPressed {

    private var profile: Call<ProDto>? = null
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
        view.layout_refresh.setOnRefreshListener {
            reloadFragment(
                activity?.supportFragmentManager!!,
                this@Discover
            )
            view.layout_refresh.isRefreshing = false
        }

        internetDetector = InternetDetector.getInstance(activity!!)
        if (internetDetector?.checkMobileInternetConn(requireActivity())!!) {
            category(view)
            books(view)
            loadProfileInfo(view)
            banner(view)
        } else {
            view.lay_shimmer.visibility = View.GONE
            view.lay_shimmer.stopShimmer()
            view.lay_no_data.visibility = View.GONE
            view.lay_data.visibility = View.GONE
            view.lay_no_internet.visibility = View.VISIBLE
        }

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

        view.btn_seeall.setOnClickListener {
            val intent = Intent(requireActivity(), SeeAll::class.java)
            intent.putExtra(getString(R.string.data), getString(R.string.loadAllCategory))
            startActivity(intent)
        }

        view.txt_search.setOnClickListener {
            val intent = Intent(requireActivity(), Search::class.java)
            startActivity(intent)
            requireActivity().overridePendingTransition(R.anim.bottom_up, R.anim.nothing)
        }
    }

    companion object {
        fun newInstance(): Discover = Discover()
    }

    override fun onBackPressed(): Boolean {
        return false
    }

    private fun banner(view: View) {
        if (!view.lay_shimmer.isShimmerStarted) {
            view.lay_shimmer.startShimmer()
        }
        banner = RetrofitClient.instanceClient.getBanners()
        banner?.enqueue(object : Callback<BannerListDto> {
            @SuppressLint("DefaultLocale", "SetTextI18n")
            override fun onResponse(
                call: Call<BannerListDto>,
                response: Response<BannerListDto>
            ) {
                Log.e("onResponse", response.toString())
                when {
                    response.code() == 200 -> {
                        when (response.body()?.status) {
                            200 -> {
                                val banner = arrayListOf<String>()
                                for (i in response.body()!!.data?.toMutableList()!!) {
                                    banner.add(i.banner.toString())
                                }
                                view.view_pager.adapter = ViewPagerAdapter(requireContext(), banner)
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
                                    Log.e("Exception", exception.message.toString())
                                }
                                view.view_pager.setOnItemClickListener(object :
                                    ClickableViewPager.OnItemClickListener {
                                    override fun onItemClick(position: Int) {
                                        val role = getData(Enums.Role.value, requireContext())
                                        if (role == Enums.Admin.value) {
                                            startActivity(
                                                Intent(
                                                    requireActivity(),
                                                    Banner::class.java
                                                )
                                            )
                                        }
                                        if (role == Enums.User.value) {
                                            try {
                                                if (position == 1) {
                                                    val intent =
                                                        Intent(activity, Profile::class.java)
                                                    intent.putExtra(
                                                        getString(R.string.data),
                                                        getString(R.string.profile)
                                                    )
                                                    startActivity(intent)
                                                }
                                            } catch (exception: java.lang.Exception) {
                                                Log.e("Exception", exception.message.toString())
                                            }
                                        }
                                    }
                                })
                            }
                            else -> {
                                coordinatorErrorMessage(
                                    view.lay_root,
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
                                Log.e(
                                    "Response",
                                    response.body()!!.toString()
                                )
                            }
                        } catch (e: Exception) {
                            coordinatorErrorMessage(
                                view.lay_root,
                                getString(R.string.msg_something_wrong)
                            )
                            Log.e("Exception", e.toString())
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
                Log.e("onFailure", t.message.toString())
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
        category = RetrofitClient.instanceClient.category()
        category?.enqueue(object : Callback<CategoryListDto> {
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
                                        CategoryAdapter(categoryData, requireActivity())
                                    view.view_category?.adapter = categoryAdapter
                                }
                            }
                            204 -> {
                                view.lay_no_data.visibility = View.VISIBLE
                                view.lay_data.visibility = View.GONE
                                view.lay_no_internet.visibility = View.GONE
                            }
                            else -> {
                                coordinatorErrorMessage(
                                    view.lay_root,
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
                                Log.e(
                                    "Response",
                                    response.body()!!.toString()
                                )
                            }
                        } catch (e: Exception) {
                            coordinatorErrorMessage(
                                view.lay_root,
                                getString(R.string.msg_something_wrong)
                            )
                            Log.e("Exception", e.toString())
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
                Log.e("onFailure", t.message.toString())
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
        books = RetrofitClient.instanceClientWithoutToken.getHome()
        books?.enqueue(object : Callback<HomeDto> {
            @SuppressLint("DefaultLocale", "SetTextI18n")
            override fun onResponse(
                call: Call<HomeDto>,
                response: Response<HomeDto>
            ) {
                Log.e("onResponse", response.toString())
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
                            204 -> {
                                saveData(
                                    "default_address",
                                    "false",
                                    activity!!
                                )
                            }
                            else -> {
                                coordinatorErrorMessage(
                                    view.lay_root,
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
                                Log.e(
                                    "Response",
                                    response.body()!!.toString()
                                )
                            }
                        } catch (e: Exception) {
                            coordinatorErrorMessage(
                                view.lay_root,
                                getString(R.string.msg_something_wrong)
                            )
                            Log.e("Exception", e.toString())
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
                Log.e("onFailure", t.message.toString())
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

    private fun loadProfileInfo(view: View) {
        if (!view.lay_shimmer.isShimmerStarted) {
            view.lay_shimmer.startShimmer()
        }
        profile = RetrofitClient.instanceClient.profile()
        profile?.enqueue(object : Callback<ProDto> {
            @SuppressLint("DefaultLocale", "SetTextI18n")
            override fun onResponse(
                call: Call<ProDto>,
                response: Response<ProDto>
            ) {
                Log.e("onResponse", response.toString())
                when {
                    response.code() == 200 -> {
                        when (response.body()?.status) {
                            200 -> {
                                try {
                                    saveData(
                                        "fullname",
                                        StringUtils.capitalize(response.body()?.data?.fname?.toLowerCase()) + " " + StringUtils.capitalize(
                                            response.body()?.data?.lname?.toLowerCase()
                                        ),
                                        requireContext()
                                    )
                                    saveData(
                                        "mobile",
                                        response.body()?.data?.mobile.toString(),
                                        requireContext()
                                    )
                                    saveData(
                                        "username",
                                        response.body()?.data?.email.toString(),
                                        requireContext()
                                    )
                                    saveData(
                                        "dp",
                                        response.body()?.data?.dp.toString(),
                                        requireContext()
                                    )
                                    saveData(
                                        "user_id",
                                        response.body()?.data?._id.toString(),
                                        requireContext()
                                    )

                                    Picasso.get()
                                        .load(
                                            getData(
                                                "rootPath",
                                                requireContext()
                                            ) + Enums.Dp.value + response.body()?.data?.dp
                                        )
                                        .error(R.drawable.ic_dummy_profile)
                                        .placeholder(R.drawable.ic_dummy_profile)
                                        .into(view.img_profile)
                                } catch (e: Exception) {
                                    Log.d("Profile", e.toString())
                                    e.printStackTrace()
                                }
                            }
                            else -> {
                                Log.e("Response", response.message())
                            }
                        }
                    }
                    response.code() == 422 || response.code() == 400 -> {
                        try {
                            val moshi: Moshi = Moshi.Builder().build()
                            val adapter: JsonAdapter<ErrorMsgDto> =
                                moshi.adapter(ErrorMsgDto::class.java)
                            val errorResponse =
                                adapter.fromJson(response.errorBody()!!.string())
                            if (errorResponse != null) {
                                if (errorResponse.status == 400) {
                                    Log.e("Response", errorResponse.message)
                                } else {
                                    Log.e("Response", errorResponse.message)
                                }

                            } else {
                                Log.e("Response", response.body()!!.toString())
                            }
                        } catch (e: Exception) {
                            Log.e("Exception", e.toString())
                        }
                    }
                    response.code() == 401 -> {
                        sessionExpired(requireActivity())
                    }
                    else -> {
                        Log.e("Response", response.message().toString())
                    }
                }
            }

            override fun onFailure(call: Call<ProDto>, t: Throwable) {
                Log.e("onFailure", t.message.toString())
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

        if (profile != null) {
            profile?.cancel()
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

        if (profile != null) {
            profile?.cancel()
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

        if (profile != null) {
            profile?.cancel()
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
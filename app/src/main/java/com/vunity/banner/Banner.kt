package com.vunity.banner

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.vunity.R
import com.vunity.general.reloadActivity
import com.vunity.general.sessionExpired
import com.vunity.general.showErrorMessage
import com.vunity.interfaces.OnBannerEditClickListener
import com.vunity.interfaces.OnBannerPlayClickListener
import com.vunity.reader.Player
import com.vunity.server.InternetDetector
import com.vunity.server.RetrofitClient
import com.vunity.user.ErrorMsgDto
import kotlinx.android.synthetic.main.act_banner.*
import kotlinx.android.synthetic.main.toolbar.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class Banner : AppCompatActivity() {

    private var internet: InternetDetector? = null
    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
    private var getBanner: Call<BannerListDto>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.act_banner)
        txt_title.text = getString(R.string.banner)
        txt_edit.visibility = View.GONE
        im_back.setOnClickListener {
            onBackPressed()
        }
        layout_refresh.setOnRefreshListener {
            finish()
            reloadActivity(this@Banner)
            layout_refresh.isRefreshing = false
        }

        internet = InternetDetector.getInstance(this@Banner)
        getBanner()
    }

    private fun getBanner() {
        if (!lay_shimmer.isShimmerStarted) {
            lay_shimmer.startShimmer()
        }
        if (internet?.checkMobileInternetConn(applicationContext)!!) {
            getBanner = RetrofitClient.bannerClient.getBanners()
            getBanner?.enqueue(object : Callback<BannerListDto> {
                @SuppressLint("DefaultLocale", "SetTextI18n")
                override fun onResponse(
                    call: Call<BannerListDto>,
                    response: Response<BannerListDto>
                ) {
                    when {
                        response.code() == 200 -> {
                            when (response.body()?.status) {
                                200 -> {
                                    lay_no_data.visibility = View.GONE
                                    lay_no_internet.visibility = View.GONE
                                    lay_data.visibility = View.VISIBLE
                                    val bannerData = response.body()!!.data?.toMutableList()
                                    view_banner?.apply {
                                        view_banner?.layoutManager = LinearLayoutManager(
                                            this@Banner,
                                            LinearLayoutManager.VERTICAL,
                                            false
                                        )
                                        view_banner?.setHasFixedSize(true)
                                        val bannerAdapter = BannerAdapter(
                                            bannerData!!, this@Banner,
                                            editClickListener = object : OnBannerEditClickListener {
                                                override fun onItemClick(item: BannerData?) {
                                                    try {
                                                        val jsonAdapter: JsonAdapter<BannerData> =
                                                            moshi.adapter(BannerData::class.java)
                                                        val json: String =
                                                            jsonAdapter.toJson(item)
                                                        val intent = Intent(
                                                            this@Banner,
                                                            UpdateBanner::class.java
                                                        )
                                                        intent.putExtra(
                                                            getString(R.string.data),
                                                            json
                                                        )
                                                        startActivity(intent)
                                                        overridePendingTransition(
                                                            R.anim.fade_in,
                                                            R.anim.fade_out
                                                        )
                                                    } catch (e: Exception) {
                                                        e.printStackTrace()
                                                    }
                                                }
                                            },
                                            playClickListener = object : OnBannerPlayClickListener {
                                                override fun onItemClick(item: BannerData?) {
                                                    Log.e("playClickListener",item.toString())
                                                    if (item?.video != null) {
                                                        try {
                                                            val intent = Intent(
                                                                this@Banner,
                                                                Player::class.java
                                                            )
                                                            intent.putExtra(
                                                                this@Banner.getString(
                                                                    R.string.data
                                                                ), item.video.toString()
                                                            )
                                                            startActivity(intent)
                                                            overridePendingTransition(
                                                                R.anim.fade_in,
                                                                R.anim.fade_out
                                                            )
                                                        } catch (e: Exception) {
                                                            e.printStackTrace()
                                                        }
                                                    }
                                                }
                                            })
                                        view_banner?.adapter = bannerAdapter
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
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                                showErrorMessage(
                                    layout_refresh,
                                    getString(R.string.msg_something_wrong)
                                )
                            }

                        }

                        response.code() == 401 -> {
                            sessionExpired(this@Banner)
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

                override fun onFailure(call: Call<BannerListDto>, t: Throwable) {
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

    override fun onStop() {
        super.onStop()
        if (getBanner != null) {
            getBanner?.cancel()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (getBanner != null) {
            getBanner?.cancel()
        }
    }
}

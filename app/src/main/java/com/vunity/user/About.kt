package com.vunity.user

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.vunity.BuildConfig
import com.vunity.R
import com.vunity.general.reloadActivity
import com.vunity.general.sessionExpired
import com.vunity.general.showErrorMessage
import com.vunity.server.InternetDetector
import com.vunity.server.RetrofitClient
import com.vunity.server.RetrofitWithBar
import kotlinx.android.synthetic.main.act_about.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class About : AppCompatActivity() {

    private var internet: InternetDetector? = null
    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
    private var about: Call<ResDto>? = null

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.act_about)
        txt_title.text = getString(R.string.app_name)
        val versionName = BuildConfig.VERSION_NAME
        txt_version.text = "-v $versionName"
        im_back.setOnClickListener {
            onBackPressed()
        }
        layout_refresh.setOnRefreshListener {
            finish()
            reloadActivity(this@About)
            layout_refresh.isRefreshing = false
        }

        internet = InternetDetector.getInstance(this@About)
        about()
    }

    private fun about() {
        if (internet?.checkMobileInternetConn(this@About)!!) {
            try {
                about = RetrofitClient.userClient.about()
                about!!.enqueue(
                    RetrofitWithBar(this@About, object : Callback<ResDto> {
                        @SuppressLint("SimpleDateFormat")
                        @RequiresApi(Build.VERSION_CODES.O)
                        override fun onResponse(
                            call: Call<ResDto>,
                            response: Response<ResDto>
                        ) {
                            if (response.code() == 200) {
                                when (response.body()?.status) {
                                    200 -> {
                                        txt_about.text = response.body()!!.data.toString()
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
                                    showErrorMessage(
                                        lay_root,
                                        getString(R.string.msg_something_wrong)
                                    )
                                }

                            } else if (response.code() == 401) {
                                sessionExpired(
                                    this@About
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

            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            showErrorMessage(
                lay_root,
                getString(R.string.msg_no_internet)
            )
        }
    }

    override fun onStop() {
        super.onStop()
        if (about != null) {
            about?.cancel()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (about != null) {
            about?.cancel()
        }
    }

}

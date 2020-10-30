package com.vunity.report

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.vunity.R
import com.vunity.general.ProgressBarAnimation
import com.vunity.general.reloadActivity
import com.vunity.general.sessionExpired
import com.vunity.general.showErrorMessage
import com.vunity.server.InternetDetector
import com.vunity.server.RetrofitClient
import com.vunity.user.ErrorMsgDto
import im.dacer.androidcharts.LineView
import kotlinx.android.synthetic.main.act_report.*
import kotlinx.android.synthetic.main.toolbar.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class Report : AppCompatActivity() {

    private var cmsReport: Call<ReportDto>? = null
    private var internet: InternetDetector? = null
    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.act_report)

        txt_title.text = getString(R.string.report)
        txt_edit.visibility = View.GONE
        im_back.setOnClickListener {
            onBackPressed()
        }

        layout_refresh.setOnRefreshListener {
            finish()
            reloadActivity(this@Report)
            layout_refresh.isRefreshing = false
        }

        internet = InternetDetector.getInstance(this@Report)
        cmsReport()
    }

    private fun cmsReport() {
        if (!lay_shimmer.isShimmerStarted) {
            lay_shimmer.startShimmer()
        }
        if (internet?.checkMobileInternetConn(applicationContext)!!) {
            cmsReport = RetrofitClient.reportClient.cmsReport()
            cmsReport?.enqueue(object : Callback<ReportDto> {
                @SuppressLint("DefaultLocale", "SetTextI18n")
                override fun onResponse(
                    call: Call<ReportDto>,
                    response: Response<ReportDto>
                ) {
                    when {
                        response.code() == 200 -> {
                            when (response.body()?.status) {
                                200 -> {

                                    val reportData = response.body()?.data?.report?.toMutableList()
                                    if (reportData.isNullOrEmpty()) {
                                        lay_no_data.visibility = View.VISIBLE
                                        lay_data.visibility = View.GONE
                                        lay_no_internet.visibility = View.GONE
                                    } else {
                                        lay_no_data.visibility = View.GONE
                                        lay_no_internet.visibility = View.GONE
                                        lay_data.visibility = View.VISIBLE
                                        loadPieChart(reportData)
                                    }

                                    try {
                                        val totalUsers = response.body()?.data?.totalUser as Int
                                        val presentUsers =
                                            response.body()?.data?.activeUser as Int
                                        txt_total.text = totalUsers.toString()
                                        txt_active.text = presentUsers.toString()
                                        val percent = presentUsers * 100.0f / totalUsers
                                        val anim = ProgressBarAnimation(
                                            bar_active,
                                            00.0f,
                                            percent
                                        )
                                        anim.duration = 1500
                                        bar_active.startAnimation(anim)

                                        val sdf =
                                            SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                                        val date: String = sdf.format(Date())
                                        txt_last_update.text = "Last update at $date"

                                    } catch (e: Exception) {
                                        e.printStackTrace()
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
                                showErrorMessage(
                                    layout_refresh,
                                    getString(R.string.msg_something_wrong)
                                )
                            }

                        }

                        response.code() == 401 -> {
                            sessionExpired(this@Report)
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

                override fun onFailure(call: Call<ReportDto>, t: Throwable) {
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

    fun loadPieChart(listOfData: MutableList<ReportData>) {
        val lineView = findViewById<View>(R.id.line_view) as LineView
        lineView.setDrawDotLine(true) //optional
        lineView.setShowPopup(LineView.SHOW_POPUPS_All) //optional
        lineView.setColorArray(intArrayOf(ContextCompat.getColor(this@Report, R.color.theme_main)))
        val dataList: ArrayList<Int> = ArrayList()
        val dataLists: ArrayList<ArrayList<Int>> = ArrayList()
        val stringList = ArrayList<String>()

        listOfData.forEach { data ->
            stringList.add(data.date.toString())
            dataList.add(data.activeUsers!!)
        }
        lineView.setBottomTextList(stringList)
        dataLists.add(dataList)
        lineView.setDataList(dataLists)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (cmsReport != null) {
            cmsReport?.cancel()
        }
    }

    override fun onStop() {
        super.onStop()
        if (cmsReport != null) {
            cmsReport?.cancel()
        }
    }
}

package com.vunity.report

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.vunity.R
import com.vunity.general.reloadActivity
import com.vunity.general.sessionExpired
import com.vunity.general.showErrorMessage
import com.vunity.server.InternetDetector
import com.vunity.server.RetrofitClient
import com.vunity.user.ErrorMsgDto
import im.dacer.androidcharts.PieHelper
import kotlinx.android.synthetic.main.act_report.*
import kotlinx.android.synthetic.main.toolbar.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


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
                                    val reportData = response.body()?.data?.toMutableList()
                                    lay_no_data.visibility = View.GONE
                                    lay_no_internet.visibility = View.GONE
                                    lay_data.visibility = View.VISIBLE
                                    if (reportData != null) {
                                        loadPieChart(reportData)
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
        val totalCount = 4
        val pieHelper = ArrayList<PieHelper>()
        val resultList: MutableList<ListReportData> = arrayListOf()

        listOfData.forEachIndexed { index, data ->
            val result: Double = data.activeUsers!!.toDouble() / totalCount * 100f
            Log.e("percent", "$result ${data.date}")
            when (index) {
                0 -> {
                    pieHelper.add(
                        PieHelper(
                            result.toFloat(),
                            ContextCompat.getColor(applicationContext, R.color.dayOne)
                        )
                    )
                    resultList.add(
                        ListReportData(
                            data.activeUsers!!.toInt(),
                            data.date.toString(),
                            R.color.dayOne,
                        )
                    )
                }
                1 -> {
                    pieHelper.add(
                        PieHelper(
                            result.toFloat(),
                            ContextCompat.getColor(applicationContext, R.color.dayTwo)
                        )
                    )
                    resultList.add(
                        ListReportData(
                            data.activeUsers!!.toInt(),
                            data.date.toString(),
                            R.color.dayTwo
                        )
                    )
                }
                2 -> {
                    pieHelper.add(
                        PieHelper(
                            result.toFloat(),
                            ContextCompat.getColor(applicationContext, R.color.dayThree)
                        )
                    )
                    resultList.add(
                        ListReportData(
                            data.activeUsers!!.toInt(),
                            data.date.toString(),
                            R.color.dayThree
                        )
                    )
                }
                3 -> {
                    pieHelper.add(
                        PieHelper(
                            result.toFloat(),
                            ContextCompat.getColor(applicationContext, R.color.dayFour)
                        )
                    )
                    resultList.add(
                        ListReportData(
                            data.activeUsers!!.toInt(),
                            data.date.toString(),
                            R.color.dayFour
                        )
                    )
                }
                4 -> {
                    pieHelper.add(
                        PieHelper(
                            result.toFloat(),
                            ContextCompat.getColor(applicationContext, R.color.dayFive)
                        )
                    )
                    resultList.add(
                        ListReportData(
                            data.activeUsers!!.toInt(),
                            data.date.toString(),
                            R.color.dayFive
                        )
                    )
                }

                5 -> {
                    pieHelper.add(
                        PieHelper(
                            result.toFloat(),
                            ContextCompat.getColor(applicationContext, R.color.daySix)
                        )
                    )
                    resultList.add(
                        ListReportData(
                            data.activeUsers!!.toInt(),
                            data.date.toString(),
                            R.color.daySix
                        )
                    )
                }

                6 -> {
                    pieHelper.add(
                        PieHelper(
                            result.toFloat(),
                            ContextCompat.getColor(applicationContext, R.color.daySeven)
                        )
                    )
                    resultList.add(
                        ListReportData(
                            data.activeUsers!!.toInt(),
                            data.date.toString(),
                            R.color.daySeven
                        )
                    )
                }
            }
        }

        Log.e("resultList", "$resultList ")
        view_report?.apply {
            view_report?.layoutManager =
                LinearLayoutManager(
                    this@Report,
                    LinearLayoutManager.VERTICAL,
                    false
                )
            view_report?.adapter = ReportAdapter(resultList, this@Report)
        }

        pie_report.setDate(pieHelper)
        pie_report.selectedPie(0)
        pie_report.showPercentLabel(true)
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

package com.vunity.user

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.vunity.R
import com.vunity.general.coordinatorErrorMessage
import com.vunity.general.reloadActivity
import com.vunity.general.sessionExpired
import com.vunity.server.InternetDetector
import com.vunity.server.RetrofitClient
import kotlinx.android.synthetic.main.act_users.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*


class Users : AppCompatActivity() {

    private var internet: InternetDetector? = null
    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private var users: Call<ProListDto>? = null
    var userData: MutableList<ProData> = arrayListOf()
    private lateinit var userAdapter: UserAdapter
    private var queryTextListener: SearchView.OnQueryTextListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.act_users)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)

        layout_refresh.setOnRefreshListener {
            finish()
            reloadActivity(this@Users)
            layout_refresh.isRefreshing = false
        }

        im_back.setOnClickListener {
            onBackPressed()
            overridePendingTransition(R.anim.bottom_up, R.anim.nothing)
        }

        internet = InternetDetector.getInstance(this@Users)
        allUsers()

        queryTextListener = object : SearchView.OnQueryTextListener {
            override fun onQueryTextChange(newText: String?): Boolean {
                filter(newText!!)
                return true
            }

            override fun onQueryTextSubmit(query: String?): Boolean {
                filter(query!!)
                return true
            }
        }
        search_book.setOnQueryTextListener(queryTextListener)
    }

    override fun onStop() {
        super.onStop()
        if (users != null) {
            users?.cancel()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.bottom_up, R.anim.nothing)
    }

    private fun filter(text: String) {
        val filteredTeam: MutableList<ProData> = arrayListOf()
        if (userData.isNotEmpty()) {
            for (data in userData) {
                if (data.fname.toLowerCase(Locale.getDefault())
                        .contains(text.toLowerCase(Locale.getDefault()))
                    || data.lname.toLowerCase(Locale.getDefault())
                        .contains(text.toLowerCase(Locale.getDefault()))
                ) {
                    filteredTeam.add(data)
                }
            }
            userAdapter.filterList(filteredTeam)
        }
    }

    private fun allUsers() {
        if (!lay_shimmer.isShimmerStarted) {
            lay_shimmer.startShimmer()
        }
        if (internet?.checkMobileInternetConn(applicationContext)!!) {
            users = RetrofitClient.instanceClient.listOfUsers()
            users?.enqueue(object : Callback<ProListDto> {
                @SuppressLint("DefaultLocale", "SetTextI18n")
                override fun onResponse(
                    call: Call<ProListDto>,
                    response: Response<ProListDto>
                ) {
                    Log.e("onResponse", response.toString())
                    when {
                        response.code() == 200 -> {
                            when (response.body()?.status) {
                                200 -> {
                                    userData = response.body()!!.data.toMutableList()
                                    lay_no_data.visibility = View.GONE
                                    lay_no_internet.visibility = View.GONE
                                    lay_data.visibility = View.VISIBLE
                                    txt_total_user.visibility = View.VISIBLE
                                    txt_total_user.text = userData.size.toString()
                                    view_users?.apply {
                                        view_users?.layoutManager = LinearLayoutManager(
                                            this@Users,
                                            LinearLayoutManager.VERTICAL,
                                            false
                                        )
                                        view_users?.setHasFixedSize(true)
                                        userAdapter = UserAdapter(userData, this@Users)
                                        view_users?.adapter = userAdapter
                                    }
                                }
                                204 -> {
                                    lay_no_data.visibility = View.VISIBLE
                                    lay_data.visibility = View.GONE
                                    lay_no_internet.visibility = View.GONE
                                    txt_total_user.visibility = View.GONE
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
                                    Log.e(
                                        "Response",
                                        response.body()!!.toString()
                                    )
                                }
                            } catch (e: Exception) {
                                coordinatorErrorMessage(
                                    layout_refresh,
                                    getString(R.string.msg_something_wrong)
                                )
                                Log.e("Exception", e.toString())
                            }

                        }

                        response.code() == 401 -> {
                            sessionExpired(this@Users)
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

                override fun onFailure(call: Call<ProListDto>, t: Throwable) {
                    Log.e("onFailure", t.message.toString())
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

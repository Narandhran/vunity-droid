package com.vunity.user

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.vunity.R
import com.vunity.general.Enums
import com.vunity.general.coordinatorErrorMessage
import com.vunity.general.reloadActivity
import com.vunity.general.sessionExpired
import com.vunity.server.InternetDetector
import com.vunity.server.RetrofitClient
import kotlinx.android.synthetic.main.act_users.*
import kotlinx.android.synthetic.main.content_users.*
import kotlinx.android.synthetic.main.filter_users.*
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
    private var status: String? = null

    @SuppressLint("WrongConstant")
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

        img_filter.setOnClickListener {
            drawer_users.openDrawer(Gravity.END)
            val actionBarDrawerToggle =
                object :
                    ActionBarDrawerToggle(this@Users, drawer_users, R.string.open, R.string.close) {
                    private val scaleFactor = 8f

                    override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                        super.onDrawerSlide(drawerView, slideOffset)
                        val slideX = drawerView.width * slideOffset
                        content_users.translationX = -slideX
                        content_users.scaleX = 1 - slideOffset / scaleFactor
                        content_users.scaleY = 1 - slideOffset / scaleFactor
                    }
                }
            drawer_users.setScrimColor(Color.TRANSPARENT)
            drawer_users.drawerElevation = 0f
            drawer_users.addDrawerListener(actionBarDrawerToggle)
        }

        radio_users.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.radio_approved -> {
                    status = Enums.Approved.value
                }
                R.id.radio_pending -> {
                    status = Enums.Pending.value
                }
                R.id.radio_blocked -> {
                    status = Enums.Blocked.value
                }
            }
        }

        btn_apply.setOnClickListener {
            if (radio_users.checkedRadioButtonId == -1) {
                coordinatorErrorMessage(
                    layout_refresh,
                    "Please select the given filter option to search."
                )
            } else {
                drawer_users.closeDrawer(Gravity.END)
                allUsers()
            }
        }

        btn_clear.setOnClickListener {
            drawer_users.closeDrawer(Gravity.END)
            Handler().postDelayed({
                radio_users.clearCheck()
                reloadActivity(this@Users)
            }, 200)
        }

        allUsers()
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
                if (data.fname?.toLowerCase(Locale.getDefault())
                        ?.contains(text.toLowerCase(Locale.getDefault()))!!
                    || data.lname?.toLowerCase(Locale.getDefault())
                        ?.contains(text.toLowerCase(Locale.getDefault()))!!
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
            users = if (status == null) {
                RetrofitClient.userClient.listOfUsers()
            } else {
                RetrofitClient.userClient.filterUsers(status.toString())
            }
            users?.enqueue(object : Callback<ProListDto> {
                @SuppressLint("DefaultLocale", "SetTextI18n")
                override fun onResponse(
                    call: Call<ProListDto>,
                    response: Response<ProListDto>
                ) {
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
                                        userAdapter = UserAdapter(
                                            dataList = userData,
                                            activity = this@Users,
                                            view = layout_refresh
                                        )
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
                                }
                            } catch (e: Exception) {
                                coordinatorErrorMessage(
                                    layout_refresh,
                                    getString(R.string.msg_something_wrong)
                                )
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

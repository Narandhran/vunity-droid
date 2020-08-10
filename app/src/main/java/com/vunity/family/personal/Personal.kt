package com.vunity.family.personal

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.vunity.R
import com.vunity.family.FamilyData
import com.vunity.family.FamilyDto
import com.vunity.general.getData
import com.vunity.general.reloadActivity
import com.vunity.general.sessionExpired
import com.vunity.general.showErrorMessage
import com.vunity.server.InternetDetector
import com.vunity.server.RetrofitClient
import com.vunity.user.ErrorMsgDto
import kotlinx.android.synthetic.main.act_personal.*
import kotlinx.android.synthetic.main.toolbar.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class Personal : AppCompatActivity() {

    private var personal: Call<FamilyDto>? = null
    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
    var internetDetector: InternetDetector? = null
    var userId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.act_personal)
        txt_title.text = getString(R.string.personal_information)
        txt_edit.visibility = View.GONE
        layout_refresh.setOnRefreshListener {
            reloadActivity(this@Personal)
            layout_refresh.isRefreshing = false
        }
        im_back.setOnClickListener {
            onBackPressed()
        }

        userId = intent.getStringExtra(getString(R.string.userId))
        personal()

        btn_add.setOnClickListener {
            val intent = Intent(this@Personal, AddPersonal::class.java)
            intent.putExtra(getString(R.string.userId), userId)
            startActivity(intent)
        }
    }

    private fun personal() {
        internetDetector = InternetDetector.getInstance(this@Personal)
        if (internetDetector?.checkMobileInternetConn(applicationContext)!!) {
            personal = if (userId != null) {
                RetrofitClient.instanceClient.listOfFamily(userId!!)
            } else {
                RetrofitClient.instanceClient.listOfFamily(
                    getData("user_id", applicationContext).toString()
                )
            }
            personal?.enqueue(object : Callback<FamilyDto> {
                @SuppressLint("DefaultLocale", "SetTextI18n")
                override fun onResponse(
                    call: Call<FamilyDto>,
                    response: Response<FamilyDto>
                ) {
                    Log.e("onResponse", response.toString())
                    when {
                        response.code() == 200 -> {
                            when (response.body()?.status) {
                                200 -> {
                                    lay_no_data.visibility = View.GONE
                                    lay_no_internet.visibility = View.GONE
                                    lay_data.visibility = View.VISIBLE

                                    txt_name.text = response.body()!!.data?.personalInfo!!.name
                                    txt_sharma.text = response.body()!!.data?.personalInfo!!.sharma
                                    txt_dob.text =
                                        response.body()!!.data?.personalInfo!!.dateOfBirth
                                    txt_time.text =
                                        response.body()!!.data?.personalInfo!!.timeOfBirth
                                    txt_place.text =
                                        response.body()!!.data?.personalInfo!!.placeOfBirth
                                    txt_rashi.text = response.body()!!.data?.personalInfo!!.rashi
                                    txt_nakshathram.text =
                                        response.body()!!.data?.personalInfo!!.nakshathram
                                    txt_padham.text = response.body()!!.data?.personalInfo!!.padham
                                    txt_city.text = response.body()!!.data?.personalInfo!!.city
                                    txt_mobile.text =
                                        response.body()!!.data?.personalInfo!!.mobileNumber
                                    txt_gender.text = response.body()!!.data?.personalInfo!!.gender
                                    txt_email.text = response.body()!!.data?.personalInfo!!.email
                                    txt_status.text =
                                        response.body()!!.data?.personalInfo!!.maritalStatus
                                    txt_edit.visibility = View.VISIBLE
                                    txt_edit.setOnClickListener {
                                        val jsonAdapter: JsonAdapter<FamilyData> =
                                            moshi.adapter(FamilyData::class.java)
                                        val json = jsonAdapter.toJson(response.body()!!.data)
                                        val intent = Intent(this@Personal, AddPersonal::class.java)
                                        intent.putExtra(getString(R.string.userId), userId)
                                        intent.putExtra(getString(R.string.data), json)
                                        startActivity(intent)
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
                            sessionExpired(this@Personal)
                        }
                        else -> {
                            showErrorMessage(
                                layout_refresh,
                                response.message()
                            )
                        }
                    }
                }

                override fun onFailure(call: Call<FamilyDto>, t: Throwable) {
                    Log.e("onFailure", t.message.toString())
                    if (!call.isCanceled) {
                        showErrorMessage(
                            layout_refresh,
                            getString(R.string.msg_something_wrong)
                        )
                    }
                }
            })

        } else {
            lay_no_data.visibility = View.GONE
            lay_data.visibility = View.GONE
            lay_no_internet.visibility = View.VISIBLE
        }
    }

    override fun onRestart() {
        super.onRestart()
        personal()
    }

    override fun onStop() {
        if (personal != null) {
            personal?.cancel()
        }
        super.onStop()
    }
}
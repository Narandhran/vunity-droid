package com.vunity.vunity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.squareup.picasso.Picasso
import com.vunity.R
import com.vunity.book.StringAdapter
import com.vunity.general.*
import com.vunity.server.InternetDetector
import com.vunity.server.RetrofitClient
import com.vunity.user.ErrorMsgDto
import kotlinx.android.synthetic.main.act_detailsof_vunity.*
import kotlinx.android.synthetic.main.toolbar.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class DetailsOfVunity : AppCompatActivity() {

    private var getByUser: Call<VunityDto>? = null
    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
    var internetDetector: InternetDetector? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.act_detailsof_vunity)

        layout_refresh.setOnRefreshListener {
            finish()
            reloadActivity(this@DetailsOfVunity)
            layout_refresh.isRefreshing = false
        }

        txt_title.text = getString(R.string.app_name)

        val role = getData(Enums.Role.value, applicationContext)
        if (role == Enums.Admin.value) {
            txt_edit.visibility = View.VISIBLE
        } else {
            txt_edit.visibility = View.GONE
        }

        im_back.setOnClickListener {
            onBackPressed()
        }

        internetDetector = InternetDetector(applicationContext)
        try {
            val receivedId = intent.getStringExtra(getString(R.string.userId))
            if (receivedId != null) {
                getByUser(receivedId)
            }
        } catch (e: Exception) {
            println("Timer Ex: $e")
        }
    }

    private fun getByUser(receivedId: String) {
        if (internetDetector?.checkMobileInternetConn(applicationContext)!!) {
            getByUser = RetrofitClient.vunityClient.getVunityUserById(receivedId)
            getByUser?.enqueue(object : Callback<VunityDto> {
                @SuppressLint("DefaultLocale", "SetTextI18n")
                override fun onResponse(
                    call: Call<VunityDto>,
                    response: Response<VunityDto>
                ) {
                    when {
                        response.code() == 200 -> {
                            when (response.body()?.status) {
                                200 -> {
                                    lay_no_data.visibility = View.GONE
                                    lay_no_internet.visibility = View.GONE
                                    lay_data.visibility = View.VISIBLE

                                    Picasso.get().load(
                                        getData(
                                            "rootPath",
                                            applicationContext
                                        ) + Enums.Dp.value + response.body()!!.data?.photo
                                    ).placeholder(R.drawable.ic_dummy_profile).into(img_profile)

                                    txt_name.text = response.body()!!.data?.name.toString()
                                    txt_mobile.text = response.body()!!.data?.mobile.toString()
                                    txt_city.text = response.body()!!.data?.city.toString()
                                    txt_vedham.text = response.body()!!.data?.vedham.toString()
                                    txt_sampradhayam.text =
                                        response.body()!!.data?.samprdhayam.toString()

                                    if (response.body()!!.data?.isMobileVisible!!) {
                                        lay_mobile.visibility = View.VISIBLE
                                    } else {
                                        lay_mobile.visibility = View.GONE
                                    }

                                    val shaka = response.body()!!.data?.shakha!!
                                    if (shaka.isNotEmpty()) {
                                        view_shaka?.apply {
                                            view_shaka?.layoutManager = LinearLayoutManager(
                                                applicationContext,
                                                LinearLayoutManager.HORIZONTAL,
                                                false
                                            )
                                            view_shaka?.setHasFixedSize(true)
                                            val genreAdapter =
                                                StringAdapter(
                                                    getString(R.string.view),
                                                    shaka,
                                                    this@DetailsOfVunity
                                                )
                                            view_shaka?.adapter = genreAdapter
                                        }
                                    }

                                    val vedhaAdhyayanam: MutableList<Any> =
                                        response.body()!!.data?.vedha_adhyayanam!!
                                    if (vedhaAdhyayanam.isNotEmpty()) {
                                        view_vedha_adhyayanam?.apply {
                                            view_vedha_adhyayanam?.layoutManager =
                                                LinearLayoutManager(
                                                    applicationContext,
                                                    LinearLayoutManager.HORIZONTAL,
                                                    false
                                                )
                                            view_vedha_adhyayanam?.setHasFixedSize(true)
                                            val genreAdapter = StringAdapter(
                                                getString(R.string.view),
                                                vedhaAdhyayanam,
                                                this@DetailsOfVunity
                                            )
                                            view_vedha_adhyayanam?.adapter = genreAdapter
                                        }
                                    }

                                    txt_shadanga_adhyayanam.text =
                                        response.body()!!.data?.shadanga_adhyayanam.toString()

                                    val shastraAdhyayanam: MutableList<Any> =
                                        response.body()!!.data?.shastra_adhyayanam!!
                                    if (shastraAdhyayanam.isNotEmpty()) {
                                        view_shastra_adhyayanam?.apply {
                                            view_shastra_adhyayanam?.layoutManager =
                                                LinearLayoutManager(
                                                    applicationContext,
                                                    LinearLayoutManager.HORIZONTAL,
                                                    false
                                                )
                                            view_shastra_adhyayanam?.setHasFixedSize(true)
                                            val genreAdapter =
                                                StringAdapter(
                                                    getString(R.string.view),
                                                    shastraAdhyayanam,
                                                    this@DetailsOfVunity
                                                )
                                            view_shastra_adhyayanam?.adapter = genreAdapter
                                        }
                                    }

                                    val prayogam: MutableList<Any> =
                                        response.body()!!.data?.prayogam!!
                                    if (prayogam.isNotEmpty()) {
                                        view_prayogam?.apply {
                                            view_prayogam?.layoutManager = LinearLayoutManager(
                                                applicationContext,
                                                LinearLayoutManager.HORIZONTAL,
                                                false
                                            )
                                            view_prayogam?.setHasFixedSize(true)
                                            val genreAdapter =
                                                StringAdapter(
                                                    getString(R.string.view),
                                                    prayogam,
                                                    this@DetailsOfVunity
                                                )
                                            view_prayogam?.adapter = genreAdapter
                                        }
                                    }

                                    txt_marital_status.text =
                                        response.body()!!.data?.marital_status.toString()
                                    txt_mothertongue.text =
                                        response.body()!!.data?.mother_tongue.toString()

                                    txt_edit.setOnClickListener {
                                        val jsonAdapter: JsonAdapter<VunityData> =
                                            moshi.adapter(VunityData::class.java)
                                        val json = jsonAdapter.toJson(response.body()!!.data)
                                        val intent =
                                            Intent(this@DetailsOfVunity, AddVunity::class.java)
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
                                }
                            } catch (e: Exception) {
                                showErrorMessage(
                                    layout_refresh,
                                    getString(R.string.msg_something_wrong)
                                )
                            }

                        }

                        response.code() == 401 -> {
                            sessionExpired(this@DetailsOfVunity)
                        }
                        else -> {
                            showErrorMessage(
                                layout_refresh,
                                response.message()
                            )
                        }
                    }
                }

                override fun onFailure(call: Call<VunityDto>, t: Throwable) {
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
        try {
            val receivedId = intent.getStringExtra(getString(R.string.userId))
            if (receivedId != null) {
                getByUser(receivedId)
            }
        } catch (e: Exception) {
            println("Timer Ex: $e")
        }
    }
}


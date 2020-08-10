package com.vunity.family.familyInfo

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
import com.vunity.book.StringAdapter
import com.vunity.family.FamilyData
import com.vunity.family.FamilyDto
import com.vunity.general.getData
import com.vunity.general.reloadActivity
import com.vunity.general.sessionExpired
import com.vunity.general.showErrorMessage
import com.vunity.server.InternetDetector
import com.vunity.server.RetrofitClient
import com.vunity.user.ErrorMsgDto
import kotlinx.android.synthetic.main.act_familyinfo.*
import kotlinx.android.synthetic.main.toolbar.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class FamilyInfo : AppCompatActivity() {

    private var family: Call<FamilyDto>? = null
    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
    private var internetDetector: InternetDetector? = null
    var userId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.act_familyinfo)
        txt_title.text = getString(R.string.family_information)
        txt_edit.visibility = View.GONE
        layout_refresh.setOnRefreshListener {
            reloadActivity(this@FamilyInfo)
            layout_refresh.isRefreshing = false
        }
        im_back.setOnClickListener {
            onBackPressed()
        }
        userId = intent.getStringExtra(getString(R.string.userId))
        family()

        btn_add.setOnClickListener {
            val intent = Intent(this@FamilyInfo, AddFamilyInfo::class.java)
            intent.putExtra(getString(R.string.userId), userId)
            startActivity(intent)
        }
    }

    private fun family() {
        internetDetector = InternetDetector.getInstance(this@FamilyInfo)
        if (internetDetector?.checkMobileInternetConn(applicationContext)!!) {
            family = if (userId != null) {
                RetrofitClient.instanceClient.listOfFamily(userId.toString())
            } else {
                RetrofitClient.instanceClient.listOfFamily(
                    getData("user_id", applicationContext).toString()
                )
            }
            family?.enqueue(object : Callback<FamilyDto> {
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

                                    txt_nativity.text =
                                        response.body()!!.data?.familyInfo!!.nativity.toString()
                                    txt_kula_theivam.text =
                                        response.body()!!.data?.familyInfo!!.kulatheivam.toString()
                                    txt_mother_tonge.text =
                                        response.body()!!.data?.familyInfo!!.motherTongue.toString()
                                    txt_sampradhayam.text =
                                        response.body()!!.data?.familyInfo!!.sampradhayam.toString()
                                    txt_smartha_subsect.text =
                                        response.body()!!.data?.familyInfo!!.smarthaSubsect.toString()
                                    txt_smartha_subsect_telugu.text =
                                        response.body()!!.data?.familyInfo!!.smarthaSubsectTelugu.toString()
                                    txt_shree_vaishnavam.text =
                                        response.body()!!.data?.familyInfo!!.vaishnavam.toString()
                                    txt_shree_vaishnavam_telugu.text =
                                        response.body()!!.data?.familyInfo!!.vaishnavamTelugu.toString()
                                    txt_madhava.text =
                                        response.body()!!.data?.familyInfo!!.madhava.toString()
                                    txt_gothram.text =
                                        response.body()!!.data?.familyInfo!!.gothram.toString()
                                    txt_rushi.text =
                                        response.body()!!.data?.familyInfo!!.rushi.toString()
                                    txt_parava.text =
                                        response.body()!!.data?.familyInfo!!.pravara.toString()
                                    txt_soothram.text =
                                        response.body()!!.data?.familyInfo!!.soothram.toString()
                                    txt_vedham.text =
                                        response.body()!!.data?.familyInfo!!.vedham.toString()
                                    val poojaList: MutableList<Any> =
                                        response.body()!!.data?.familyInfo!!.poojas!!
                                    if (poojaList.isNotEmpty()) {
                                        view_poojas?.apply {
                                            view_poojas?.layoutManager = LinearLayoutManager(
                                                applicationContext,
                                                LinearLayoutManager.HORIZONTAL,
                                                false
                                            )
                                            view_poojas?.setHasFixedSize(true)
                                            val genreAdapter =
                                                StringAdapter(
                                                    getString(R.string.view),
                                                    response.body()!!.data?.familyInfo!!.poojas!!,
                                                    this@FamilyInfo
                                                )
                                            view_poojas?.adapter = genreAdapter
                                        }
                                    }
                                    txt_pondugal_name.text =
                                        response.body()!!.data?.familyInfo!!.pondugalName.toString()
                                    txt_panchangam.text =
                                        response.body()!!.data?.familyInfo!!.panchangam.toString()
                                    txt_thilakam.text =
                                        response.body()!!.data?.familyInfo!!.thilakam.toString()

                                    txt_edit.visibility = View.VISIBLE
                                    txt_edit.setOnClickListener {
                                        val jsonAdapter: JsonAdapter<FamilyData> = moshi.adapter(
                                            FamilyData::class.java
                                        )
                                        val json = jsonAdapter.toJson(response.body()!!.data)
                                        val intent =
                                            Intent(this@FamilyInfo, AddFamilyInfo::class.java)
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
                            sessionExpired(this@FamilyInfo)
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
        family()
    }

    override fun onStop() {
        if (family != null) {
            family?.cancel()
        }
        super.onStop()
    }
}
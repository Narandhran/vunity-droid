package com.vunity.family.shraddha.gothram

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.vunity.R
import com.vunity.family.FamilyData
import com.vunity.family.FamilyDto
import com.vunity.general.getData
import com.vunity.general.sessionExpired
import com.vunity.general.showErrorMessage
import com.vunity.general.showMessage
import com.vunity.server.InternetDetector
import com.vunity.server.RetrofitClient
import com.vunity.server.RetrofitWithBar
import com.vunity.user.ErrorMsgDto
import kotlinx.android.synthetic.main.act_add_gothram.*
import kotlinx.android.synthetic.main.toolbar.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*


class AddGothram : AppCompatActivity() {

    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
    var internet: InternetDetector? = null
    private var family: Call<FamilyDto>? = null
    var userId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.act_add_gothram)

        internet = InternetDetector.getInstance(applicationContext)
        txt_title.text = getString(R.string.gothram)
        txt_edit.visibility = View.GONE
        im_back.setOnClickListener {
            onBackPressed()
        }

        userId = intent.getStringExtra(getString(R.string.userId))
        try {
            val data = intent.getStringExtra(getString(R.string.data))
            if (data != null) {
                val jsonAdapter: JsonAdapter<FamilyData> =
                    moshi.adapter(FamilyData::class.java)
                val familyData: FamilyData? = jsonAdapter.fromJson(data.toString())
                println(familyData)
                edt_pithru_gothram.setText(familyData?.shraardhaInfo?.gothram?.pithruGothram.toString())
                edt_mathru_gothram.setText(familyData?.shraardhaInfo?.gothram?.mathruGothram.toString())
            }
        } catch (exception: Exception) {
            Log.e("Exception", exception.toString())
        }

        btn_update.setOnClickListener {
            update()
        }
    }

    private fun update() {
        lay_pithru_gothram.error = null

        when {
            edt_pithru_gothram.length() < 3 -> {
                lay_pithru_gothram.error = "Pithru gothram's minimum character is 3."
            }
            else -> {
                val gothram = com.vunity.family.Gothram(
                    pithruGothram = edt_pithru_gothram.text.toString()
                        .toLowerCase(Locale.getDefault()),
                    mathruGothram = edt_mathru_gothram.text.toString()
                        .toLowerCase(Locale.getDefault())
                )
                gothram(gothram)
            }
        }
    }

    private fun gothram(data: com.vunity.family.Gothram) {
        if (internet!!.checkMobileInternetConn(this@AddGothram)) {
            try {
                Log.e("data", data.toString())
                family = if (userId != null) {
                    RetrofitClient.instanceClient.gothram(
                        id = userId!!,
                        gothram = data
                    )
                } else {
                    RetrofitClient.instanceClient.gothram(
                        id = getData("user_id", applicationContext).toString(),
                        gothram = data
                    )
                }
                family!!.enqueue(
                    RetrofitWithBar(this@AddGothram, object : Callback<FamilyDto> {
                        @SuppressLint("SimpleDateFormat")
                        @RequiresApi(Build.VERSION_CODES.O)
                        override fun onResponse(
                            call: Call<FamilyDto>,
                            response: Response<FamilyDto>
                        ) {
                            Log.e("onResponse", response.toString())
                            if (response.code() == 200) {
                                when (response.body()?.status) {
                                    200 -> {
                                        showMessage(lay_root, response.body()!!.message.toString())
                                        Handler().postDelayed({
                                            onBackPressed()
                                            finish()
                                        }, 200)
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
                                        Log.e(
                                            "Response",
                                            response.body()!!.toString()
                                        )
                                    }
                                } catch (e: Exception) {
                                    showErrorMessage(
                                        lay_root,
                                        getString(R.string.msg_something_wrong)
                                    )
                                    Log.e("Exception", e.toString())
                                }

                            } else if (response.code() == 401) {
                                sessionExpired(
                                    this@AddGothram
                                )
                            } else {
                                showErrorMessage(
                                    lay_root,
                                    response.message()
                                )
                            }
                        }

                        override fun onFailure(call: Call<FamilyDto>, t: Throwable) {
                            Log.e("onResponse", t.message.toString())
                            showErrorMessage(
                                lay_root,
                                getString(R.string.msg_something_wrong)
                            )
                        }
                    })
                )

            } catch (e: Exception) {
                Log.d("ParseException", e.toString())
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
    }

    override fun onDestroy() {
        super.onDestroy()

    }
}


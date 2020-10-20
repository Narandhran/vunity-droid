package com.vunity.user

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.iid.FirebaseInstanceId
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.vunity.R
import com.vunity.general.*
import com.vunity.server.InternetDetector
import com.vunity.server.RetrofitClient
import com.vunity.server.RetrofitWithBar
import kotlinx.android.synthetic.main.act_login.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class Login : AppCompatActivity() {

    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.act_login)

        val data = intent.getStringExtra(getString(R.string.data))
        if (data == getString(R.string.new_user)) {
            lay_skip.visibility = View.GONE
        }

        im_back.setOnClickListener {
            onBackPressed()
        }

        btn_continue.setOnClickListener {
            lay_mobile.error = null
            if (edt_mobile.length() != 10) {
                lay_mobile.error = "Enter the valid mobile number."
            } else {
                requestOtp(edt_mobile.text.toString())
            }
        }

        btn_register.setOnClickListener {
            startActivity(Intent(this@Login, Register::class.java))
            finish()
        }

        btn_skip.setOnClickListener {
            startActivity(Intent(this@Login, Home::class.java))
            saveData("logged_user", "skip", applicationContext)
            saveData(
                "rootPath",
                "https://vunity.s3.ap-south-1.amazonaws.com/",
                applicationContext
            )
            finish()
        }

        try {
            FirebaseInstanceId.getInstance().instanceId
                .addOnCompleteListener(OnCompleteListener { task ->
                    if (!task.isSuccessful) {
                        Log.d("FirebaseInstanceId", "getInstanceId failed", task.exception)
                        return@OnCompleteListener
                    }
                    // Get new Instance ID token
                    saveData("fcm_token", task.result?.token.toString(), applicationContext)
                })
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
    }

    private fun requestOtp(mobile: String) {
        val internet = InternetDetector.getInstance(this@Login)
        if (internet.checkMobileInternetConn(this@Login)) {
            try {
                val requestOtp = RetrofitClient.userClient.requestOtp(mobile)
                requestOtp.enqueue(
                    RetrofitWithBar(this@Login, object : Callback<ResDto> {
                        @SuppressLint("SimpleDateFormat")
                        @RequiresApi(Build.VERSION_CODES.O)
                        override fun onResponse(
                            call: Call<ResDto>,
                            response: Response<ResDto>
                        ) {
                            if (response.code() == 200) {
                                when (response.body()?.status) {
                                    200 -> {
                                        showMessage(
                                            lay_root,
                                            response.body()!!.message
                                        )
                                        Handler().postDelayed({
                                            val intent =
                                                Intent(this@Login, Verification::class.java)
                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                            intent.putExtra("mobile", mobile)
                                            startActivity(intent)
                                            this@Login.overridePendingTransition(
                                                R.anim.fade_in,
                                                R.anim.fade_out
                                            )
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
                                    }
                                } catch (e: Exception) {
                                    showErrorMessage(
                                        lay_root,
                                        getString(R.string.msg_something_wrong)
                                    )
                                }

                            } else if (response.code() == 401) {
                                sessionExpired(
                                    this@Login
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
}

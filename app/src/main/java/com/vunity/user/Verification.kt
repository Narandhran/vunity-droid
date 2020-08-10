package com.vunity.user

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.vunity.Home
import com.vunity.R
import com.vunity.general.*
import com.vunity.server.InternetDetector
import com.vunity.server.RetrofitClient
import com.vunity.server.RetrofitWithBar
import kotlinx.android.synthetic.main.act_verification.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.TimeUnit


class Verification : AppCompatActivity() {

    var mobile = ""
    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.act_verification)

        if (intent.getStringExtra("mobile") != null) {
            mobile = intent.getStringExtra("mobile")!!.toString()
        }

        edt_code_one.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                charSequence: CharSequence,
                i: Int,
                i1: Int,
                i2: Int
            ) {
            }

            override fun onTextChanged(
                charSequence: CharSequence,
                i: Int,
                i1: Int,
                i2: Int
            ) {
            }

            override fun afterTextChanged(editable: Editable) {
                if (edt_code_one.length() == 1) {
                    edt_code_two.requestFocus()
                }
            }
        })

        edt_code_two.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                charSequence: CharSequence,
                i: Int,
                i1: Int,
                i2: Int
            ) {
            }

            override fun onTextChanged(
                charSequence: CharSequence,
                i: Int,
                i1: Int,
                i2: Int
            ) {
                if (edt_code_two.length() == 0) {
                    edt_code_one.requestFocus()
                }
            }

            override fun afterTextChanged(editable: Editable) {
                if (edt_code_two.length() == 1) {
                    edt_code_three.requestFocus()
                }
            }
        })

        edt_code_three.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                charSequence: CharSequence,
                i: Int,
                i1: Int,
                i2: Int
            ) {
            }

            override fun onTextChanged(
                charSequence: CharSequence,
                i: Int,
                i1: Int,
                i2: Int
            ) {
                if (edt_code_three.length() == 0) {
                    edt_code_two.requestFocus()
                }
            }

            override fun afterTextChanged(editable: Editable) {
                if (edt_code_three.length() == 1) {
                    edt_code_four.requestFocus()
                }
            }
        })

        edt_code_four.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                charSequence: CharSequence,
                i: Int,
                i1: Int,
                i2: Int
            ) {
            }

            override fun onTextChanged(
                charSequence: CharSequence,
                i: Int,
                i1: Int,
                i2: Int
            ) {
                if (edt_code_four.length() == 0) {
                    edt_code_three.requestFocus()
                }
            }

            override fun afterTextChanged(editable: Editable) {
                // We can call api to verify the OTP here or on an explicit button click
            }
        })

        im_back.setOnClickListener {
            onBackPressed()
        }

        btn_verify.setOnClickListener {
            val otp =
                edt_code_one.text.toString() + edt_code_two.text + edt_code_three.text + edt_code_four.text
            if (otp.length != 4) {
                showMessage(
                    lay_root,
                    "Enter your verification code (OTP)."
                )
            } else {
                val loginBody = LoginBody(mobile = mobile, otp = otp.toInt())
                login(loginBody)
            }
        }
        startTimer()
    }

    private fun startTimer() {
        lay_timer.visibility = View.VISIBLE
        txt_resend.visibility = View.GONE
        object : CountDownTimer(60000, 1000) {
            @SuppressLint("DefaultLocale")
            override fun onTick(millisUntilFinished: Long) {
                //Convert milliseconds into hour,minute and seconds
                val hms = java.lang.String.format(
                    "%02d:%02d",
                    TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) - TimeUnit.HOURS.toMinutes(
                        TimeUnit.MILLISECONDS.toHours(millisUntilFinished)
                    ),
                    TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) - TimeUnit.MINUTES.toSeconds(
                        TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished)
                    )
                )
                txt_timer.text = hms
            }

            override fun onFinish() {
                lay_timer.visibility = View.GONE
                txt_resend.visibility = View.VISIBLE
                txt_resend.setOnClickListener {
                    requestOtp(mobile)
                }
            }
        }.start()
    }

    private fun requestOtp(mobile: String) {
        val internet = InternetDetector.getInstance(this@Verification)
        if (internet.checkMobileInternetConn(this@Verification)) {
            try {
                Log.e("mobile", mobile)
                val requestOtp = RetrofitClient.instanceClientWithoutToken.requestOtp(mobile)
                requestOtp.enqueue(
                    RetrofitWithBar(this@Verification, object : Callback<ResDto> {
                        @SuppressLint("SimpleDateFormat")
                        @RequiresApi(Build.VERSION_CODES.O)
                        override fun onResponse(
                            call: Call<ResDto>,
                            response: Response<ResDto>
                        ) {
                            Log.e("onResponse", response.toString())
                            if (response.code() == 200) {
                                when (response.body()?.status) {
                                    200 -> {
                                        showMessage(
                                            lay_root,
                                            response.body()!!.message
                                        )
                                        startTimer()
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
                                    this@Verification
                                )
                            } else {
                                showErrorMessage(
                                    lay_root,
                                    response.message()
                                )
                            }
                        }

                        override fun onFailure(call: Call<ResDto>, t: Throwable) {
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

    private fun login(loginBody: LoginBody) {
        Log.e("loginBody", loginBody.toString())
        val internet = InternetDetector.getInstance(this@Verification)
        if (internet.checkMobileInternetConn(this@Verification)) {
            try {
                val login = RetrofitClient.instanceClientWithoutToken.login(loginBody)
                login.enqueue(
                    RetrofitWithBar(this@Verification, object : Callback<LoginDto> {
                        @SuppressLint("SimpleDateFormat")
                        @RequiresApi(Build.VERSION_CODES.O)
                        override fun onResponse(
                            call: Call<LoginDto>,
                            response: Response<LoginDto>
                        ) {
                            Log.e("onResponse", response.toString())
                            if (response.code() == 200) {
                                when (response.body()?.status) {
                                    200 -> {
                                        showMessage(
                                            lay_root,
                                            response.body()!!.message
                                        )
                                        saveData(
                                            "logged_user",
                                            "true",
                                            applicationContext
                                        )
                                        saveToken(
                                            "user_token",
                                            response.body()!!.data.token,
                                            applicationContext
                                        )
                                        saveData(
                                            Enums.Role.value,
                                            response.body()!!.data.role,
                                            applicationContext
                                        )
                                        saveData(
                                            "rootPath",
                                            response.body()!!.data.rpath,
                                            applicationContext
                                        )

                                        Handler().postDelayed({
                                            val intent = Intent(this@Verification, Home::class.java)
                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                            startActivity(intent)
                                            this@Verification.overridePendingTransition(
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
                                    this@Verification
                                )
                            } else {
                                showErrorMessage(
                                    lay_root,
                                    response.message()
                                )
                            }
                        }

                        override fun onFailure(call: Call<LoginDto>, t: Throwable) {
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

}

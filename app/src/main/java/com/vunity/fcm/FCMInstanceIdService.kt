package com.vunity.fcm

import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.google.firebase.messaging.FirebaseMessagingService
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.vunity.server.RetrofitClient
import com.vunity.user.ErrorMsgDto
import com.vunity.user.ResDto
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*


class FCMInstanceIdService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        sendRegistrationToServer(token)
    }

    private fun sendRegistrationToServer(token: String) {
        val mapData: HashMap<String, String> = HashMap()
        mapData["fcm"] = token
        Log.e("data", mapData.toString())
        val updateToken = RetrofitClient.instanceClient.updateProfile(mapData)
        updateToken.enqueue(object : Callback<ResDto> {
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
                            Log.e("onNewToken", response.body()!!.message.toString())
                        }
                        else -> {
                            Log.e("onNewToken", response.message().toString())
                        }
                    }
                } else if (response.code() == 422 || response.code() == 400) {
                    try {
                        val moshi: Moshi = Moshi.Builder().build()
                        val adapter: JsonAdapter<ErrorMsgDto> =
                            moshi.adapter(ErrorMsgDto::class.java)
                        val errorResponse =
                            adapter.fromJson(response.errorBody()!!.string())
                        if (errorResponse != null) {
                            if (errorResponse.status == 400) {
                                Log.e("onNewToken", errorResponse.message.toString())
                            } else {
                                Log.e("onNewToken", errorResponse.message.toString())
                            }

                        } else {
                            Log.e("onNewToken", response.body().toString())
                        }
                    } catch (e: Exception) {
                        Log.e("Exception", e.toString())
                    }

                } else {
                    Log.e("onNewToken", response.message().toString())
                }
            }

            override fun onFailure(call: Call<ResDto>, t: Throwable) {
                Log.e("onResponse", t.message.toString())
            }
        })
    }
}
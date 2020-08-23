package com.vunity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.iid.FirebaseInstanceId
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.vunity.discover.Discover
import com.vunity.general.getData
import com.vunity.general.saveData
import com.vunity.interfaces.IOnBackPressed
import com.vunity.server.RetrofitClient
import com.vunity.user.ErrorMsgDto
import com.vunity.user.Login
import com.vunity.user.ResDto
import com.vunity.vunity.UsersVunity
import com.vunity.vunity.Vunity
import kotlinx.android.synthetic.main.act_home.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.system.exitProcess

class Home : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.act_home)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        navigationView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        navigationView.selectedItemId = R.id.action_discover

        try {
            FirebaseInstanceId.getInstance().instanceId
                .addOnCompleteListener(OnCompleteListener { task ->
                    if (!task.isSuccessful) {
                        Log.w("FirebaseInstanceId", "getInstanceId failed", task.exception)
                        return@OnCompleteListener
                    }
                    // Get new Instance ID token
                    sendRegistrationToServer(task.result?.token.toString())
                    Log.e("FirebaseInstanceId", task.result?.token.toString())
                })
        } catch (exception: Exception) {
            Log.e("Exception from Login", exception.toString())
        }
    }

    private val mOnNavigationItemSelectedListener =
        BottomNavigationView.OnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.action_discover -> {
                    val fragment = Discover.newInstance()
                    openFragment(fragment)
                    return@OnNavigationItemSelectedListener true
                }

                R.id.action_users -> {
                    val isLoggedIn = getData("logged_user", applicationContext)
                    if (isLoggedIn == getString(R.string.skip)) {
                        val intent = Intent(this@Home, Login::class.java)
                        intent.putExtra(getString(R.string.data), getString(R.string.new_user))
                        startActivity(intent)
                    } else {
                        val fragment = UsersVunity.newInstance()
                        openFragment(fragment)
                        return@OnNavigationItemSelectedListener true
                    }
                }

                R.id.action_vunity -> {
                    val isLoggedIn = getData("logged_user", applicationContext)
                    if (isLoggedIn == getString(R.string.skip)) {
                        val intent = Intent(this@Home, Login::class.java)
                        intent.putExtra(getString(R.string.data), getString(R.string.new_user))
                        startActivity(intent)
                    } else {
                        val fragment = Vunity.newInstance()
                        openFragment(fragment)
                        return@OnNavigationItemSelectedListener true
                    }
                }
            }
            false
        }

    private fun openFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.container, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    override fun onBackPressed() {
        val fragment = this.supportFragmentManager.findFragmentById(R.id.container)
        val backPressed = (fragment as IOnBackPressed).onBackPressed()
        val selectedItemId = navigationView.selectedItemId
        if (!backPressed) {
            when {
                selectedItemId != R.id.action_discover -> {
                    navigationView.selectedItemId = R.id.action_discover
                }
                selectedItemId == R.id.action_discover -> {
                    exitApp()
                }
            }
        }
    }

    private fun exitApp() {
        val builder = AlertDialog.Builder(this@Home)
        builder.setTitle("Leave " + getString(R.string.app_name) + "?")
        builder.setMessage("Are you sure you want to exit?")
        builder.setPositiveButton("YES") { dialog, which ->
            dialog.cancel()
            finish()
            exitProcess(0)
        }
        builder.setNegativeButton("No") { dialog, which ->
            dialog.cancel()
        }
        val dialog: AlertDialog = builder.create()
        dialog.show()
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
                            saveData("fcm_token", token, applicationContext)
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

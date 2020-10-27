package com.vunity.general

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.iid.FirebaseInstanceId
import com.hbisoft.pickit.PickiT
import com.hbisoft.pickit.PickiTCallbacks
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.vunity.R
import com.vunity.book.BookDetails
import com.vunity.book.Books
import com.vunity.interfaces.IOnBackPressed
import com.vunity.server.RetrofitClient
import com.vunity.user.ErrorMsgDto
import com.vunity.user.Login
import com.vunity.user.ProDto
import com.vunity.user.ResDto
import com.vunity.video.VideoUploadService
import com.vunity.video.Videos
import com.vunity.vunity.Vunity
import com.vunity.vunity.VunityUsers
import kotlinx.android.synthetic.main.act_home.*
import kotlinx.android.synthetic.main.dialog_show_announcement.*
import org.apache.commons.lang3.StringUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.util.*

class Home : AppCompatActivity(), PickiTCallbacks {

    var pickiT: PickiT? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.act_home)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        navigationView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        navigationView.selectedItemId = R.id.action_books
        loadProfileInfo()

        pickiT = PickiT(this, this, this)

        try {
            FirebaseInstanceId.getInstance().instanceId
                .addOnCompleteListener(OnCompleteListener { task ->
                    if (!task.isSuccessful) {
                        Log.e("FirebaseInstanceId", "getInstanceId failed " + task.exception)
                        return@OnCompleteListener
                    }
                    // Get new Instance ID token
                    sendRegistrationToServer(task.result?.token.toString())
                })
        } catch (exception: Exception) {
            exception.printStackTrace()
        }

        val fcmTitle: String? = intent.getStringExtra("title")
        val fcmBody: String? = intent.getStringExtra("body")
        val fcmBookId: String? = intent.getStringExtra("bookId")

        try {
            if (fcmTitle != null && fcmBody != null) {
                if (fcmTitle == getString(R.string.vunity_notifier)) {
                    val intent = Intent(this@Home, BookDetails::class.java)
                    intent.putExtra(getString(R.string.data), fcmBookId.toString())
                    startActivityForResult(intent, 1)
                } else {
                    val dialog = Dialog(this@Home, R.style.DialogTheme)
                    dialog.setContentView(R.layout.dialog_show_announcement)
                    dialog.txt_title.text = fcmTitle
                    dialog.txt_body.text = fcmBody
                    dialog.btn_ok.setOnClickListener {
                        dialog.dismiss()
                    }
                    dialog.show()
                }
            }
        } catch (e: InterruptedException) {
            // Process exception
            e.printStackTrace()
        }
    }

    private val mOnNavigationItemSelectedListener =
        BottomNavigationView.OnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.action_books -> {
                    val fragment = Books.newInstance()
                    openFragment(fragment)
                    return@OnNavigationItemSelectedListener true
                }

                R.id.action_vunity_users -> {
                    val isLoggedIn = getData("logged_user", applicationContext)
                    if (isLoggedIn == getString(R.string.skip)) {
                        val intent = Intent(this@Home, Login::class.java)
                        intent.putExtra(getString(R.string.data), getString(R.string.new_user))
                        startActivity(intent)
                    } else {
                        val fragment = VunityUsers.newInstance()
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

                R.id.action_videos -> {
                    val fragment = Videos.newInstance()
                    openFragment(fragment)
                    return@OnNavigationItemSelectedListener true
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
                selectedItemId != R.id.action_books -> {
                    navigationView.selectedItemId = R.id.action_books
                }
                selectedItemId == R.id.action_books -> {
                    finish()
                }
            }
        }
    }

    private fun sendRegistrationToServer(token: String) {
        val mapData: HashMap<String, String> = HashMap()
        mapData["fcm"] = token
        val updateToken = RetrofitClient.userClient.updateProfile(mapData)
        updateToken.enqueue(object : Callback<ResDto> {
            @SuppressLint("SimpleDateFormat")
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(
                call: Call<ResDto>,
                response: Response<ResDto>
            ) {
                if (response.code() == 200) {
                    when (response.body()?.status) {
                        200 -> {
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
                        e.printStackTrace()
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

    private fun loadProfileInfo() {
        val profile = RetrofitClient.userClient.profile()
        profile.enqueue(object : Callback<ProDto> {
            @SuppressLint("DefaultLocale", "SetTextI18n")
            override fun onResponse(
                call: Call<ProDto>,
                response: Response<ProDto>
            ) {
                when {
                    response.code() == 200 -> {
                        when (response.body()?.status) {
                            200 -> {
                                try {
                                    saveData(
                                        "fullname",
                                        StringUtils.capitalize(response.body()?.data?.fname?.toLowerCase()) + " " + StringUtils.capitalize(
                                            response.body()?.data?.lname?.toLowerCase()
                                        ),
                                        applicationContext
                                    )
                                    saveData(
                                        "mobile",
                                        response.body()?.data?.mobile.toString(),
                                        applicationContext
                                    )
                                    saveData(
                                        "username",
                                        response.body()?.data?.email.toString(),
                                        applicationContext
                                    )
                                    saveData(
                                        "dp",
                                        response.body()?.data?.dp.toString(),
                                        applicationContext
                                    )
                                    saveData(
                                        "user_id",
                                        response.body()?.data?._id.toString(),
                                        applicationContext
                                    )

                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                            else -> {
                                Log.e("Response", response.message())
                            }
                        }
                    }
                    response.code() == 422 || response.code() == 400 -> {
                        try {
                            val moshi: Moshi = Moshi.Builder().build()
                            val adapter: JsonAdapter<ErrorMsgDto> =
                                moshi.adapter(ErrorMsgDto::class.java)
                            val errorResponse =
                                adapter.fromJson(response.errorBody()!!.string())
                            if (errorResponse != null) {
                                if (errorResponse.status == 400) {
                                    Log.e("Response", errorResponse.message)
                                } else {
                                    Log.e("Response", errorResponse.message)
                                }

                            } else {
                                Log.e("Response", response.body()!!.toString())
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    response.code() == 401 -> {
                        sessionExpired(this@Home)
                    }
                    else -> {
                        Log.e("Response", response.message().toString())
                    }
                }
            }

            override fun onFailure(call: Call<ProDto>, t: Throwable) {
                Log.e("onFailure", t.message.toString())
            }
        })
    }

    override fun onResume() {
        super.onResume()
//        if (!isMyServiceRunning(applicationContext, VideoUploadService::class.java)) {
//            val path = applicationContext?.getDir(getString(R.string.app_name), MODE_PRIVATE)
//            val tempFiles = File(path, "temp")
//            deleteTemps(tempFiles)
//            pickiT?.deleteTemporaryFile()
//        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                intent.replaceExtras(Bundle())
                intent.action = ""
                intent.data = null
                intent.flags = 0
                reloadActivity(this@Home)
            }

            if (resultCode == Activity.RESULT_CANCELED) {
                //Write your code if there's no result
                intent.replaceExtras(Bundle())
                intent.action = ""
                intent.data = null
                intent.flags = 0
                reloadActivity(this@Home)
            }
        }
    }

    override fun PickiTonUriReturned() {
        TODO("Not yet implemented")
    }

    override fun PickiTonStartListener() {
        TODO("Not yet implemented")
    }

    override fun PickiTonProgressUpdate(progress: Int) {
        TODO("Not yet implemented")
    }

    override fun PickiTonCompleteListener(
        path: String?,
        wasDriveFile: Boolean,
        wasUnknownProvider: Boolean,
        wasSuccessful: Boolean,
        Reason: String?
    ) {
        TODO("Not yet implemented")
    }
}

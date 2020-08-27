package com.vunity.user

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.DialogFragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.squareup.picasso.Picasso
import com.vunity.R
import com.vunity.general.*
import com.vunity.general.Constants.PERMISSIONS
import com.vunity.general.Constants.PERMISSION_ALL
import com.vunity.server.InternetDetector
import com.vunity.server.RetrofitClient
import com.vunity.server.RetrofitWithBar
import kotlinx.android.synthetic.main.act_register.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.util.*

class Register : AppCompatActivity() {

    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
    var id: String = ""
    var internet: InternetDetector? = null
    var isVaidhika = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.act_register)

        internet = InternetDetector.getInstance(this@Register)
        LocalBroadcastManager.getInstance(applicationContext!!).registerReceiver(
            fileLocationReceiver,
            IntentFilter(Constants.fileLocation)
        )

        im_back.setOnClickListener {
            onBackPressed()
        }

        img_profile.setOnClickListener {
            if (!hasPermissions(
                    applicationContext,
                    *PERMISSIONS
                )
            ) {
                ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL)
            } else {
                val builder = AlertDialog.Builder(this@Register)
                builder.setTitle("Profile Pic:")
                    .setMessage(
                        "This is your photo ID, Please upload a formal pic: " + " " + getEmoji(
                            0x1F60A
                        )
                    )
                    .setCancelable(false)
                    .setPositiveButton("OK") { dialog, id ->
                        dialog.dismiss()
                        val permissionResult =
                            checkPermission(this@Register)
                        if (permissionResult) {
                            val bottomSheetDialog = DialogChoosePhoto.instance
                            bottomSheetDialog.setStyle(
                                DialogFragment.STYLE_NORMAL,
                                R.style.CustomBottomSheetDialogTheme
                            )
                            this@Register.supportFragmentManager.let { it1 ->
                                bottomSheetDialog.show(
                                    it1,
                                    "Custom Bottom Sheet"
                                )
                            }
                        } else {
                            showErrorMessage(
                                lay_root,
                                "Permission denied, Please grant permission to access camera!"
                            )
                        }
                    }
                    .setNegativeButton(
                        R.string.cancel
                    ) { dialog, id -> dialog.cancel() }
                val alert = builder.create()
                alert.show()
            }
        }

        try {
            val data = intent.getStringExtra("data")
            if (data != null) {
                val jsonAdapter: JsonAdapter<ProData> =
                    moshi.adapter(ProData::class.java)
                val profileData: ProData? = jsonAdapter.fromJson(data.toString())
                println(profileData)
                if (profileData != null) {
                    lay_msg.visibility = View.GONE
                    id = profileData._id.toString()
                    btn_register.text = getString(R.string.update)
                    edt_fname.setText(profileData.fname)
                    edt_lname.setText(profileData.lname)
                    edt_email.setText(profileData.email)
                    edt_mobile.setText(profileData.mobile)

                    Picasso.get()
                        .load(
                            getData(
                                "rootPath",
                                this@Register
                            ) + Enums.Dp.value + profileData.dp
                        )
                        .error(R.drawable.ic_dummy_profile)
                        .placeholder(R.drawable.ic_dummy_profile)
                        .fit()
                        .into(img_profile)
                }
            }
        } catch (e: Exception) {
            Log.e("Exception", e.toString())
            showMessage(
                lay_root,
                getString(R.string.unable_to_fetch)
            )
        }

        group_vaidhika.setOnCheckedChangeListener { radioGroup, i ->
            when (i) {
                R.id.radio_yes -> {
                    isVaidhika = true
                }
                R.id.radio_no -> {
                    isVaidhika = false
                }
            }
        }

        btn_register.setOnClickListener {
            lay_fname.error = null
            lay_lname.error = null
            lay_email.error = null
            lay_mobile.error = null
            lay_soothram.error = null

            if (edt_fname.length() < 3) {
                lay_fname.error = "First name minimum character is 3."
            } else if (edt_lname.length() < 3) {
                lay_lname.error = "Last name minimum character is 3."
            } else if (edt_email.length() != 0) {
                if (!isValidEmail(edt_email.text)) {
                    lay_email.error = "Please enter the valid email."
                } else if (edt_mobile.length() != 10) {
                    lay_mobile.error = "Enter the valid mobile number."
                } else if (edt_soothram.length() < 3) {
                    lay_soothram.error = "Soothram's minimum character is 3."
                } else if (group_vaidhika.checkedRadioButtonId == -1) {
                    showMessage(lay_root, "Please select vaidhika!")
                } else {
                    val mapData: HashMap<String, Any> = HashMap()
                    mapData["fname"] = edt_fname.text.toString().toLowerCase(Locale.getDefault())
                    mapData["lname"] = edt_lname.text.toString().toLowerCase(Locale.getDefault())
                    mapData["email"] = edt_email.text.toString().toLowerCase(Locale.getDefault())
                    mapData["mobile"] = edt_mobile.text.toString().toLowerCase(Locale.getDefault())
                    mapData["soothram"] = edt_soothram.text.toString().toLowerCase(Locale.getDefault())
                    mapData["vaidhika"] = isVaidhika
                    if (id.isEmpty()) {
                        register(mapData)
                    } else {
                        update(mapData)
                    }
                }
            } else if (edt_mobile.length() != 10) {
                lay_mobile.error = "Enter the valid mobile number."
            } else if (edt_soothram.length() < 3) {
                lay_soothram.error = "Soothram's minimum character is 3."
            } else if (group_vaidhika.checkedRadioButtonId == -1) {
                showMessage(lay_root, "Please select vaidhika!")
            } else {
                val mapData: HashMap<String, Any> = HashMap()
                mapData["fname"] = edt_fname.text.toString().toLowerCase(Locale.getDefault())
                mapData["lname"] = edt_lname.text.toString().toLowerCase(Locale.getDefault())
                mapData["mobile"] = edt_mobile.text.toString().toLowerCase(Locale.getDefault())
                mapData["soothram"] = edt_soothram.text.toString().toLowerCase(Locale.getDefault())
                mapData["vaidhika"] = isVaidhika
                if (id.isEmpty()) {
                    register(mapData)
                } else {
                    update(mapData)
                }
            }
        }

        btn_login.setOnClickListener {
            startActivity(Intent(this@Register, Login::class.java))
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(applicationContext)
            .unregisterReceiver(fileLocationReceiver)
    }

    private val fileLocationReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            //TODO extract extras from intent
            if (intent.extras != null) {
                val uri: Uri = intent.getParcelableExtra("file_location")!!
                val file = File(uri.path!!)
                val fileReqBody = RequestBody.create(MediaType.parse("image/*"), file)
                val part: MultipartBody.Part =
                    MultipartBody.Part.createFormData("dp", file.name, fileReqBody)

                Log.e("fileLocationReceiver", uri.toString())
                if (internet?.checkMobileInternetConn(context)!!) {
                    val uploadProfile = RetrofitClient.instanceClient.updateDp(part)
                    uploadProfile.enqueue(
                        RetrofitWithBar(this@Register, object : Callback<ResDto> {
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
                                            Picasso.get()
                                                .load(
                                                    getData(
                                                        "rootPath",
                                                        this@Register
                                                    ) + Enums.Dp.value + response.body()!!.data.toString()
                                                )
                                                .error(R.drawable.ic_dummy_profile)
                                                .placeholder(R.drawable.ic_dummy_profile)
                                                .into(img_profile)
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
                                        val moshi: Moshi = Moshi.Builder().build()
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
                                        this@Register
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
                } else {
                    showErrorMessage(
                        lay_root,
                        getString(R.string.msg_no_internet)
                    )
                }
            }
        }
    }

    private fun register(mapData: HashMap<String, Any>) {
        if (internet!!.checkMobileInternetConn(this@Register)) {
            try {
                Log.e("mapData", mapData.toString())
                val register = RetrofitClient.instanceClientWithoutToken.register(mapData)
                register.enqueue(
                    RetrofitWithBar(this@Register, object : Callback<ResDto> {
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
                                        Handler().postDelayed({
                                            startActivity(
                                                Intent(
                                                    this@Register,
                                                    Login::class.java
                                                )
                                            )
                                            this@Register.overridePendingTransition(
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
                                    this@Register
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

    private fun update(mapData: HashMap<String, Any>) {
        if (internet!!.checkMobileInternetConn(this@Register)) {
            try {
                Log.e("mapData", mapData.toString())
                val register = RetrofitClient.instanceClient.updateProfile(mapData)
                register.enqueue(
                    RetrofitWithBar(this@Register, object : Callback<ResDto> {
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
                                    this@Register
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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        when (requestCode) {
            1 -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {
                    Toast.makeText(
                        this@Register,
                        "Permission denied",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                return
            }
        }
    }
}

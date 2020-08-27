package com.vunity.user

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.leinardi.android.speeddial.SpeedDialActionItem
import com.leinardi.android.speeddial.SpeedDialView
import com.razorpay.Checkout
import com.razorpay.PaymentData
import com.razorpay.PaymentResultWithDataListener
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.squareup.picasso.Picasso
import com.vunity.BuildConfig
import com.vunity.R
import com.vunity.book.AddBook
import com.vunity.category.AddCategory
import com.vunity.favourite.Favourite
import com.vunity.general.*
import com.vunity.server.InternetDetector
import com.vunity.server.RetrofitClient
import com.vunity.server.RetrofitWithBar
import kotlinx.android.synthetic.main.act_profile.*
import kotlinx.android.synthetic.main.dialog_amount.*
import kotlinx.android.synthetic.main.dialog_create_announcement.*
import kotlinx.android.synthetic.main.dialog_thanks.*
import org.apache.commons.lang3.StringUtils
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*


class Profile : AppCompatActivity(), PaymentResultWithDataListener {

    var myApp: Application? = null
    private var profile: Call<ProDto>? = null
    var profileData: MutableList<ProData> = arrayListOf()
    var internet: InternetDetector? = null
    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    @RequiresApi(Build.VERSION_CODES.M)
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.act_profile)

        im_back.setOnClickListener {
            onBackPressed()
        }

        layout_refresh.setOnRefreshListener {
            finish()
            overridePendingTransition(0, 0)
            startActivity(intent)
            overridePendingTransition(0, 0)
            layout_refresh.isRefreshing = false
        }

        myApp = application as Application
        internet = InternetDetector.getInstance(this@Profile)

        val versionName = BuildConfig.VERSION_NAME
        txt_version.text = "-v $versionName"
        img_edit.setOnClickListener {
            try {
                val jsonAdapter: JsonAdapter<ProData> =
                    moshi.adapter(ProData::class.java)
                val json: String = jsonAdapter.toJson(profileData[0])
                val intent = Intent(this@Profile, Register::class.java)
                intent.putExtra(getString(R.string.data), json)
                this.startActivity(intent)
                this.overridePendingTransition(
                    R.anim.fade_in,
                    R.anim.fade_out
                )
            } catch (e: Exception) {
                Log.e("Exception", e.toString())
            }
        }

        lay_logout.setOnClickListener {
            val builder = AlertDialog.Builder(this@Profile)
            builder.setTitle("Sign out")
                .setMessage("Signing out of your account will clear your session on this phone.")
                .setCancelable(false)
                .setPositiveButton("OK") { dialog, id ->
                    clearSession(this@Profile)
                    myApp!!.clearApplicationData()
                    val intent = Intent(this@Profile, Login::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    this@Profile.overridePendingTransition(
                        R.anim.fade_in,
                        R.anim.fade_out
                    )
                    this@Profile.finish()
                    dialog.dismiss()
                }
                .setNegativeButton(
                    R.string.cancel
                ) { dialog, id -> dialog.cancel() }
            val alert = builder.create()
            alert.show()
        }

        lay_bookmarks.setOnClickListener {
            startActivity(Intent(this@Profile, Favourite::class.java))
        }

        lay_edit.setOnClickListener {
            startActivity(Intent(this@Profile, About::class.java))
        }

        val root = intent.getStringExtra(getString(R.string.data))
        if (root != null) {
            if (root == getString(R.string.profile)) {
                donate()
            }
        }
        lay_donate.setOnClickListener {
            donate()
        }

        if (internet?.checkMobileInternetConn(this)!!) {
            loadProfileInfo()
        } else {
            showErrorMessage(
                layout_refresh,
                getString(R.string.msg_no_internet)
            )
        }
        initSpeedDial()
        val role = getData(Enums.Role.value, applicationContext)
        if (role == Enums.Admin.value) {
            fab_admin.visibility = View.VISIBLE
        } else {
            fab_admin.visibility = View.GONE
        }
    }

    private fun loadProfileInfo(): MutableList<ProData> {
        profile = RetrofitClient.instanceClient.profile()
        profile?.enqueue(object : Callback<ProDto> {
            @SuppressLint("DefaultLocale", "SetTextI18n")
            override fun onResponse(
                call: Call<ProDto>,
                response: Response<ProDto>
            ) {
                Log.e("onResponse", response.toString())
                when {
                    response.code() == 200 -> {
                        when (response.body()?.status) {
                            200 -> {
                                try {
                                    profileData.clear()
                                    profileData.add(response.body()?.data!!)
                                    txt_fullname.text =
                                        StringUtils.capitalize(response.body()?.data?.fname?.toLowerCase()) + " " +
                                                StringUtils.capitalize(response.body()?.data?.lname?.toLowerCase())
                                    saveData(
                                        "fullname",
                                        txt_fullname.text.toString(),
                                        this@Profile.applicationContext
                                    )

                                    txt_mobile.text = response.body()?.data?.mobile
                                    saveData(
                                        "mobile",
                                        txt_mobile.text.toString(),
                                        this@Profile.applicationContext
                                    )
                                    val email = response.body()?.data?.email
                                    if (email.isNullOrEmpty()) {
                                        txt_email.visibility = View.GONE
                                    } else {
                                        txt_email.visibility = View.VISIBLE
                                        txt_email.text = email
                                        saveData(
                                            "username",
                                            email,
                                            this@Profile.applicationContext
                                        )
                                    }

                                    Picasso.get()
                                        .load(
                                            getData(
                                                "rootPath",
                                                this@Profile
                                            ) + Enums.Dp.value + response.body()?.data?.dp
                                        )
                                        .error(R.drawable.ic_dummy_profile)
                                        .placeholder(R.drawable.ic_dummy_profile)
                                        .into(img_profile)
                                    saveData(
                                        "dp",
                                        response.body()?.data?.dp.toString(),
                                        this@Profile.applicationContext
                                    )
                                    saveData(
                                        "user_id", response.body()?.data?._id.toString(),
                                        this@Profile.applicationContext
                                    )
                                } catch (e: Exception) {
                                    Log.d("Profile", e.toString())
                                    e.printStackTrace()
                                }
                            }
                            else -> {
                                showMessage(
                                    layout_refresh,
                                    response.message()
                                )
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
                        sessionExpired(this@Profile)
                    }
                    else -> {
                        showMessage(
                            layout_refresh,
                            response.message()
                        )
                    }
                }
            }

            override fun onFailure(call: Call<ProDto>, t: Throwable) {
                Log.e("onFailure", t.message.toString())
                if (!call.isCanceled) {
                    showErrorMessage(
                        layout_refresh,
                        getString(R.string.msg_something_wrong)
                    )
                }
            }
        })
        return profileData
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun initSpeedDial() {

        fab_admin.addActionItem(
            SpeedDialActionItem.Builder(R.id.fab_category, R.drawable.ic_category)
                .setLabel(getString(R.string.add_category))
                .setFabImageTintColor(ResourcesCompat.getColor(resources, R.color.white, theme))
                .setLabelColor(getColor(R.color.theme_dark_grey))
                .setTheme(R.style.FabTheme)
                .create()
        )

        fab_admin.addActionItem(
            SpeedDialActionItem.Builder(R.id.fab_genre, R.drawable.ic_theatre)
                .setLabel(getString(R.string.add_genre))
                .setFabImageTintColor(ResourcesCompat.getColor(resources, R.color.white, theme))
                .setLabelColor(getColor(R.color.theme_dark_grey))
                .setTheme(R.style.FabTheme)
                .create()
        )


        fab_admin.addActionItem(
            SpeedDialActionItem.Builder(R.id.fab_book, R.drawable.ic_book)
                .setLabel(getString(R.string.add_book))
                .setFabImageTintColor(ResourcesCompat.getColor(resources, R.color.white, theme))
                .setLabelColor(getColor(R.color.theme_dark_grey))
                .setTheme(R.style.FabTheme)
                .create()
        )

        fab_admin.addActionItem(
            SpeedDialActionItem.Builder(R.id.fab_user, R.drawable.ic_add_user)
                .setLabel(getString(R.string.user))
                .setFabImageTintColor(ResourcesCompat.getColor(resources, R.color.white, theme))
                .setLabelColor(getColor(R.color.theme_dark_grey))
                .setTheme(R.style.FabTheme)
                .create()
        )

        fab_admin.addActionItem(
            SpeedDialActionItem.Builder(R.id.fab_vunity_announcement, R.drawable.ic_megaphone)
                .setLabel(getString(R.string.announcement))
                .setFabImageTintColor(ResourcesCompat.getColor(resources, R.color.white, theme))
                .setLabelColor(getColor(R.color.theme_dark_grey))
                .setTheme(R.style.FabTheme)
                .create()
        )


        fab_admin.setOnActionSelectedListener(SpeedDialView.OnActionSelectedListener { actionItem ->
            when (actionItem.id) {
                R.id.fab_category -> {
                    startActivity(Intent(this@Profile, AddCategory::class.java))
                    fab_admin.close() // To close the Speed Dial with animation
                    return@OnActionSelectedListener true // false will close it without animation
                }

                R.id.fab_book -> {
                    startActivity(Intent(this@Profile, AddBook::class.java))
                    fab_admin.close()
                    return@OnActionSelectedListener true
                }

                R.id.fab_genre -> {
                    addGenre()
                    fab_admin.close()
                    return@OnActionSelectedListener true
                }

                R.id.fab_user -> {
                    startActivity(Intent(this@Profile, Users::class.java))
                    fab_admin.close()
                    return@OnActionSelectedListener true
                }

                R.id.fab_vunity_announcement -> {
                    fab_admin.close()
                    vunityAnnouncement()
                    return@OnActionSelectedListener true
                }
            }
            false
        })
    }

    private fun addGenre() {
        val dialog = Dialog(this@Profile, R.style.DialogTheme)
        dialog.setContentView(R.layout.dialog_add_genre)
        val layName: TextInputLayout = dialog.findViewById(R.id.lay_name)
        val edtName: TextInputEditText = dialog.findViewById(R.id.edt_name)
        val btnCreate: MaterialButton = dialog.findViewById(R.id.btn_create)
        btnCreate.setOnClickListener {
            if (internet!!.checkMobileInternetConn(this@Profile)) {
                try {
                    layName.error = null
                    if (edtName.length() < 2) {
                        layName.error = "AddName minimum character is 2."
                    } else {
                        val mapData: HashMap<String, String> = HashMap()
                        mapData["genre"] = edtName.text.toString().toLowerCase(Locale.getDefault())
                        Log.e("mapData", mapData.toString())
                        val addGenre = RetrofitClient.instanceClient.addGenre(mapData)
                        addGenre.enqueue(
                            RetrofitWithBar(this@Profile, object : Callback<ResDto> {
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
                                                    layName,
                                                    response.body()!!.message
                                                )
                                                edtName.let { v ->
                                                    val imm =
                                                        getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                                                    imm?.hideSoftInputFromWindow(v.windowToken, 0)
                                                }
                                                Handler().postDelayed({
                                                    edtName.setText("")
                                                }, 200)
                                            }
                                            else -> {
                                                showErrorMessage(
                                                    layName,
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
                                                        layName,
                                                        errorResponse.message
                                                    )
                                                } else {
                                                    showErrorMessage(
                                                        layName,
                                                        errorResponse.message
                                                    )
                                                }

                                            } else {
                                                showErrorMessage(
                                                    layName,
                                                    getString(R.string.msg_something_wrong)
                                                )
                                                Log.e(
                                                    "Response",
                                                    response.body()!!.toString()
                                                )
                                            }
                                        } catch (e: Exception) {
                                            showErrorMessage(
                                                layName,
                                                getString(R.string.msg_something_wrong)
                                            )
                                            Log.e("Exception", e.toString())
                                        }

                                    } else if (response.code() == 401) {
                                        sessionExpired(
                                            this@Profile
                                        )
                                    } else {
                                        showErrorMessage(
                                            layName,
                                            response.message()
                                        )
                                    }
                                }

                                override fun onFailure(call: Call<ResDto>, t: Throwable) {
                                    Log.e("onResponse", t.message.toString())
                                    showErrorMessage(
                                        layName,
                                        getString(R.string.msg_something_wrong)
                                    )
                                }
                            })
                        )

                    }

                } catch (e: Exception) {
                    Log.d("ParseException", e.toString())
                    e.printStackTrace()
                }
            } else {
                showErrorMessage(
                    layName,
                    getString(R.string.msg_no_internet)
                )
            }
        }
        dialog.show()
    }

    override fun onStop() {
        super.onStop()
        if (profile != null) {
            profile?.cancel()
        }
    }

    override fun onRestart() {
        super.onRestart()
        loadProfileInfo()
    }

    private fun donate() {
        val dialog = Dialog(this@Profile, R.style.DialogTheme)
        dialog.setContentView(R.layout.dialog_amount)
        dialog.btn_next.setOnClickListener {
            dialog.lay_amount.error = null
            if (dialog.edt_amount.length() < 1) {
                dialog.lay_amount.error = "Please enter the amount."
            } else {
                if (internet?.checkMobileInternetConn(this@Profile)!!) {
                    try {
                        val mapData: HashMap<String, Int> = HashMap()
                        mapData["amount"] = dialog.edt_amount.text.toString().toInt() * 100
                        Log.e("mapData", mapData.toString())
                        val donate = RetrofitClient.instanceClient.donate(mapData)
                        donate.enqueue(
                            RetrofitWithBar(this@Profile, object :
                                Callback<DonateDto> {
                                @SuppressLint("SetTextI18n")
                                override fun onResponse(
                                    call: Call<DonateDto>,
                                    response: Response<DonateDto>
                                ) {
                                    Log.e("onResponse", response.body().toString())
                                    when {
                                        response.code() == 200 -> {
                                            if (response.body()?.status == 200) {
                                                dialog.edt_amount.let { v ->
                                                    val imm =
                                                        getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                                                    imm?.hideSoftInputFromWindow(v.windowToken, 0)
                                                }
                                                dialog.cancel()
                                                startPayment(
                                                    order_id = response.body()!!.data?.id.toString(),
                                                    amount = response.body()!!.data?.amount.toString()
                                                )
                                            } else {
                                                showErrorMessage(
                                                    dialog.lay_amount,
                                                    getString(R.string.msg_something_wrong)
                                                )
                                                Log.e("Response", response.body().toString())
                                            }
                                        }

                                        response.code() == 422 || response.code() == 400 -> {
                                            try {
                                                val moshi: Moshi = Moshi.Builder().build()
                                                val adapter: JsonAdapter<ErrorMsgDto> =
                                                    moshi.adapter(ErrorMsgDto::class.java)
                                                val errorResponse =
                                                    adapter.fromJson(
                                                        response.errorBody()!!.string()
                                                    )
                                                if (errorResponse != null) {
                                                    if (errorResponse.status == 422) {
                                                        showErrorMessage(
                                                            dialog.lay_amount,
                                                            errorResponse.message
                                                        )
                                                    } else {
                                                        showErrorMessage(
                                                            dialog.lay_amount,
                                                            errorResponse.message
                                                        )
                                                    }

                                                } else {
                                                    showErrorMessage(
                                                        dialog.lay_amount,
                                                        getString(R.string.msg_something_wrong)
                                                    )
                                                    Log.e("Response", response.body()!!.toString())
                                                }
                                            } catch (e: Exception) {
                                                showErrorMessage(
                                                    dialog.lay_amount,
                                                    getString(R.string.msg_something_wrong)
                                                )
                                                Log.e("Mapping Exception", e.toString())
                                            }
                                        }

                                        response.code() == 401 -> {
                                            sessionExpired(this@Profile)
                                        }
                                        else -> {
                                            showErrorMessage(dialog.lay_amount, response.message())
                                        }
                                    }
                                }

                                override fun onFailure(call: Call<DonateDto>, t: Throwable) {
                                    Log.e("onFailure", t.toString())
                                    showErrorMessage(
                                        dialog.lay_amount,
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
                    showErrorMessage(dialog.lay_amount, getString(R.string.msg_no_internet))
                }
            }
        }
        dialog.show()
    }

    private fun startPayment(order_id: String, amount: String) {
        val checkout = Checkout()
        checkout.setKeyID(getString(R.string.razorpay_key))
        val activity: Activity = this

        try {
            val options = JSONObject()
            options.put("name", getString(R.string.app_name))
            options.put("description", "Donation")
            options.put("image", "https://s3.amazonaws.com/rzp-mobile/images/rzp.png")
            options.put("order_id", order_id)
            options.put("currency", "INR")
            val prefill = JSONObject()
            prefill.put("email", getData("username", applicationContext))
            prefill.put("contact", getData("mobile", applicationContext))
            options.put("prefill", prefill)
            /**
             * Amount is always passed in currency subunits
             * Eg: "500" = INR 5.00
             */
            options.put("amount", amount)
            checkout.open(activity, options)

        } catch (e: Exception) {
            Toast.makeText(activity, "Error in payment: " + e.message, Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }

    private fun vunityAnnouncement() {
        val dialog = Dialog(this@Profile, R.style.DialogTheme)
        dialog.setContentView(R.layout.dialog_create_announcement)
        dialog.btn_send.setOnClickListener {
            dialog.lay_title.error = null
            dialog.lay_description.error = null
            when {
                dialog.edt_title.length() < 3 -> {
                    dialog.lay_title.error = "Title's minimum character is 3."
                }
                dialog.edt_description.length() < 3 -> {
                    dialog.lay_description.error = "Description's minimum character is 3."
                }
                else -> {
                    if (internet?.checkMobileInternetConn(this@Profile)!!) {
                        try {
                            val mapData: HashMap<String, String> = HashMap()
                            mapData["title"] = dialog.edt_title.text.toString()
                            mapData["message"] = dialog.edt_description.text.toString()
                            Log.e("mapData", mapData.toString())
                            val announcement =
                                RetrofitClient.instanceClient.cmsAnnouncement(mapData)
                            announcement.enqueue(
                                RetrofitWithBar(this@Profile, object :
                                    Callback<ResDto> {
                                    @SuppressLint("SetTextI18n")
                                    override fun onResponse(
                                        call: Call<ResDto>,
                                        response: Response<ResDto>
                                    ) {
                                        Log.e("onResponse", response.toString())
                                        when {
                                            response.code() == 200 -> {
                                                if (response.body()?.status == 200) {
                                                    showMessage(
                                                        dialog.lay_title,
                                                        response.body()!!.message
                                                    )
                                                    dialog.edt_title.let { v ->
                                                        val imm =
                                                            getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                                                        imm?.hideSoftInputFromWindow(
                                                            v.windowToken,
                                                            0
                                                        )
                                                    }

                                                    dialog.edt_description.let { v ->
                                                        val imm =
                                                            getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                                                        imm?.hideSoftInputFromWindow(
                                                            v.windowToken,
                                                            0
                                                        )
                                                    }

                                                    Handler().postDelayed({
                                                        dialog.cancel()
                                                    }, 500)
                                                } else {
                                                    showErrorMessage(
                                                        dialog.lay_title,
                                                        getString(R.string.msg_something_wrong)
                                                    )
                                                    Log.e("Response", response.body().toString())
                                                }
                                            }

                                            response.code() == 422 || response.code() == 400 -> {
                                                try {
                                                    val moshi: Moshi = Moshi.Builder().build()
                                                    val adapter: JsonAdapter<ErrorMsgDto> =
                                                        moshi.adapter(ErrorMsgDto::class.java)
                                                    val errorResponse =
                                                        adapter.fromJson(
                                                            response.errorBody()!!.string()
                                                        )
                                                    if (errorResponse != null) {
                                                        if (errorResponse.status == 422) {
                                                            showErrorMessage(
                                                                dialog.lay_title,
                                                                errorResponse.message
                                                            )
                                                        } else {
                                                            showErrorMessage(
                                                                dialog.lay_title,
                                                                errorResponse.message
                                                            )
                                                        }

                                                    } else {
                                                        showErrorMessage(
                                                            dialog.lay_title,
                                                            getString(R.string.msg_something_wrong)
                                                        )
                                                        Log.e(
                                                            "Response",
                                                            response.body()!!.toString()
                                                        )
                                                    }
                                                } catch (e: Exception) {
                                                    showErrorMessage(
                                                        dialog.lay_title,
                                                        getString(R.string.msg_something_wrong)
                                                    )
                                                    Log.e("Mapping Exception", e.toString())
                                                }
                                            }

                                            response.code() == 401 -> {
                                                sessionExpired(this@Profile)
                                            }
                                            else -> {
                                                showErrorMessage(
                                                    dialog.lay_title,
                                                    response.message()
                                                )
                                            }
                                        }
                                    }

                                    override fun onFailure(call: Call<ResDto>, t: Throwable) {
                                        Log.e("onFailure", t.toString())
                                        showErrorMessage(
                                            dialog.lay_title,
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
                        showErrorMessage(dialog.lay_title, getString(R.string.msg_no_internet))
                    }
                }
            }
        }
        dialog.show()
    }

    override fun onPaymentError(
        errorCode: Int,
        errorDescription: String?,
        paymentData: PaymentData?
    ) {
        Log.e(
            "onPaymentError",
            "onError: $errorCode : $errorDescription : ${paymentData?.data.toString()}"
        )
    }

    override fun onPaymentSuccess(rzpPaymentId: String?, paymentData: PaymentData?) {
        Log.e(
            "onPaymentSuccess",
            "\n --- orderId : ${paymentData?.orderId} " +
                    "\n --- paymentId : ${paymentData?.paymentId}" +
                    "\n --- signature : ${paymentData?.signature}"
        )
        val verifyPaymentData =
            VerifyPayment(
                razorpay_order_id = paymentData?.orderId!!,
                razorpay_payment_id = paymentData.paymentId!!,
                razorpay_signature = paymentData.signature!!
            )
        Log.e("verifyPaymentData", verifyPaymentData.toString())
        verifyPayment(verifyPaymentData)
    }

    private fun verifyPayment(verify: VerifyPayment) {
        if (internet?.checkMobileInternetConn(this@Profile)!!) {
            try {
                val verifyPayment = RetrofitClient.instanceClient.verifyPayment(verify)
                verifyPayment.enqueue(
                    RetrofitWithBar(this@Profile, object :
                        Callback<ResDto> {
                        @SuppressLint("SetTextI18n")
                        override fun onResponse(call: Call<ResDto>, response: Response<ResDto>) {
                            Log.e("onResponse", response.toString())
                            when {
                                response.code() == 200 -> {
                                    if (response.body()?.status == 200) {
                                        val dialog = Dialog(this@Profile, R.style.DialogTheme)
                                        dialog.setContentView(R.layout.dialog_thanks)
                                        dialog.btn_ok.setOnClickListener {
                                            dialog.cancel()
                                        }
                                        dialog.show()
//                                        Handler().postDelayed({
//                                            startActivity(Intent(this@Profile, Home::class.java))
//                                            this@Profile.overridePendingTransition(
//                                                R.anim.fade_in,
//                                                R.anim.fade_out
//                                            )
//                                            finish()
//                                        }, 200)
                                    } else {
                                        showErrorMessage(
                                            layout_refresh,
                                            getString(R.string.msg_something_wrong)
                                        )
                                        Log.e("Response", response.body().toString())
                                    }
                                }

                                response.code() == 422 || response.code() == 400 -> {
                                    try {
                                        val moshi: Moshi = Moshi.Builder().build()
                                        val adapter: JsonAdapter<ErrorMsgDto> =
                                            moshi.adapter(ErrorMsgDto::class.java)
                                        val errorResponse =
                                            adapter.fromJson(
                                                response.errorBody()!!.string()
                                            )
                                        if (errorResponse != null) {
                                            if (errorResponse.status == 422) {
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
                                            Log.e("Response", response.body()!!.toString())
                                        }
                                    } catch (e: Exception) {
                                        showErrorMessage(
                                            layout_refresh,
                                            getString(R.string.msg_something_wrong)
                                        )
                                        Log.e("Mapping Exception", e.toString())
                                    }
                                }

                                response.code() == 401 -> {
                                    sessionExpired(
                                        this@Profile
                                    )
                                }
                                else -> {
                                    showErrorMessage(
                                        layout_refresh,
                                        response.message()
                                    )
                                }
                            }
                        }

                        override fun onFailure(call: Call<ResDto>, t: Throwable) {
                            Log.e("onFailure", t.toString())
                            showErrorMessage(
                                layout_refresh,
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
            showErrorMessage(layout_refresh, getString(R.string.msg_no_internet))
        }
    }
}
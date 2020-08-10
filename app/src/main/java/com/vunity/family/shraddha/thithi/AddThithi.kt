package com.vunity.family.shraddha.thithi

import `in`.galaxyofandroid.spinerdialog.SpinnerDialog
import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.textview.MaterialTextView
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.vunity.R
import com.vunity.family.FamilyDto
import com.vunity.family.ThithiData
import com.vunity.general.*
import com.vunity.server.InternetDetector
import com.vunity.server.RetrofitClient
import com.vunity.server.RetrofitWithBar
import com.vunity.user.ErrorMsgDto
import com.vunity.user.ResDto
import kotlinx.android.synthetic.main.act_add_thithi.*
import kotlinx.android.synthetic.main.toolbar.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*


class AddThithi : AppCompatActivity() {

    private var mYear = 0
    private var mMonth = 0
    private var mDay = 0
    private var dob: String = ""
    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
    var internet: InternetDetector? = null
    var userId: String? = null
    var thithiId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.act_add_thithi)

        internet = InternetDetector.getInstance(applicationContext)
        txt_title.text = getString(R.string.thithi)
        txt_edit.visibility = View.GONE
        im_back.setOnClickListener {
            onBackPressed()
        }

        userId = intent.getStringExtra(getString(R.string.userId))
        try {
            val data = intent.getStringExtra(getString(R.string.data))
            if (data != null) {
                val jsonAdapter: JsonAdapter<com.vunity.family.Thithi> =
                    moshi.adapter(com.vunity.family.Thithi::class.java)
                val thithiData: com.vunity.family.Thithi? =
                    jsonAdapter.fromJson(data.toString())
                println(thithiData)
                thithiId = thithiData?._id.toString()
                edt_relationship.setText(thithiData?.relationship)
                edt_name.setText(thithiData?.name)
                edt_masam_sauramanam.setText(thithiData?.masamSauramanam)
                edt_masam_chandramanam.setText(thithiData?.masamChandramanam)
                edt_paksham.setText(thithiData?.paksham)
                edt_thithi.setText(thithiData?.thithi)
                edt_date.setText(thithiData?.date)
                edt_time.setText(thithiData?.time)
            }
        } catch (exception: Exception) {
            Log.e("Exception", exception.toString())
        }

        edt_relationship.setOnClickListener {
            val relationship: ArrayList<String> = arrayListOf()
            relationship.addAll(resources.getStringArray(R.array.thithi_relationship))
            val spinnerDialog = SpinnerDialog(
                this@AddThithi,
                relationship,
                "Relationship.",
                R.style.DialogAnimations_SmileWindow, // For slide animation
                "Cancel"
            )
            spinnerDialog.setCancellable(true) // for cancellable
            spinnerDialog.setShowKeyboard(false)// for open keyboard by default
            spinnerDialog.bindOnSpinerListener { item, position ->
                if (item == getString(R.string.other)) {
                    val dialog = Dialog(this@AddThithi, R.style.DialogTheme)
                    dialog.setContentView(R.layout.dialog_add_genre)
                    val txtTips: MaterialTextView = dialog.findViewById(R.id.txt_tips)
                    val layName: TextInputLayout = dialog.findViewById(R.id.lay_name)
                    val edtName: TextInputEditText = dialog.findViewById(R.id.edt_name)
                    val btnCreate: MaterialButton = dialog.findViewById(R.id.btn_create)
                    txtTips.visibility = View.GONE
                    layName.hint = "Relationship"
                    btnCreate.text = getString(R.string.add)
                    btnCreate.setOnClickListener {
                        try {
                            layName.error = null
                            if (edtName.length() < 2) {
                                layName.error = "Relationship's minimum character is 2."
                            } else {
                                edt_relationship.text = edtName.text
                                edtName.let { v ->
                                    val imm =
                                        getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                                    imm?.hideSoftInputFromWindow(v.windowToken, 0)
                                }
                                Handler().postDelayed({
                                    edtName.setText("")
                                }, 200)
                                dialog.cancel()
                            }

                        } catch (e: Exception) {
                            Log.d("ParseException", e.toString())
                            e.printStackTrace()
                        }
                    }
                    dialog.show()
                } else {
                    edt_relationship.setText(item)
                }
            }
            spinnerDialog.showSpinerDialog()
        }

        edt_masam_sauramanam.setOnClickListener {
            val sauramanam: ArrayList<String> = arrayListOf()
            sauramanam.addAll(resources.getStringArray(R.array.masam_sauramanam))
            val spinnerDialog = SpinnerDialog(
                this@AddThithi,
                sauramanam,
                "Masam (sauramanam).",
                R.style.DialogAnimations_SmileWindow, // For slide animation
                "Cancel"
            )
            spinnerDialog.setCancellable(true) // for cancellable
            spinnerDialog.setShowKeyboard(false)// for open keyboard by default
            spinnerDialog.bindOnSpinerListener { item, position ->
                edt_masam_sauramanam.setText(item)
            }
            spinnerDialog.showSpinerDialog()
        }

        edt_masam_chandramanam.setOnClickListener {
            val chandramanam: ArrayList<String> = arrayListOf()
            chandramanam.addAll(resources.getStringArray(R.array.masam_chandramanam))
            val spinnerDialog = SpinnerDialog(
                this@AddThithi,
                chandramanam,
                "Masam (chandramanam).",
                R.style.DialogAnimations_SmileWindow, // For slide animation
                "Cancel"
            )
            spinnerDialog.setCancellable(true) // for cancellable
            spinnerDialog.setShowKeyboard(false)// for open keyboard by default
            spinnerDialog.bindOnSpinerListener { item, position ->
                edt_masam_chandramanam.setText(item)
            }
            spinnerDialog.showSpinerDialog()
        }

        edt_paksham.setOnClickListener {
            val paksham: ArrayList<String> = arrayListOf()
            paksham.addAll(resources.getStringArray(R.array.paksham))
            val spinnerDialog = SpinnerDialog(
                this@AddThithi,
                paksham,
                "Paksham.",
                R.style.DialogAnimations_SmileWindow, // For slide animation
                "Cancel"
            )
            spinnerDialog.setCancellable(true) // for cancellable
            spinnerDialog.setShowKeyboard(false)// for open keyboard by default
            spinnerDialog.bindOnSpinerListener { item, position ->
                edt_paksham.setText(item)
            }
            spinnerDialog.showSpinerDialog()
        }

        edt_thithi.setOnClickListener {
            val thithi: ArrayList<String> = arrayListOf()
            thithi.addAll(resources.getStringArray(R.array.thithi))
            val spinnerDialog = SpinnerDialog(
                this@AddThithi,
                thithi,
                "AddThithi.",
                R.style.DialogAnimations_SmileWindow, // For slide animation
                "Cancel"
            )
            spinnerDialog.setCancellable(true) // for cancellable
            spinnerDialog.setShowKeyboard(false)// for open keyboard by default
            spinnerDialog.bindOnSpinerListener { item, position ->
                edt_thithi.setText(item)
            }
            spinnerDialog.showSpinerDialog()
        }

        edt_date.setOnClickListener {
            // Get Current Date
            val c = Calendar.getInstance()
            mYear = c[Calendar.YEAR]
            mMonth = c[Calendar.MONTH]
            mDay = c[Calendar.DAY_OF_MONTH]
            val datePickerDialog = DatePickerDialog(
                this@AddThithi,
                DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                    edt_date.text = Editable.Factory.getInstance().newEditable(
                        String.format("%02d", dayOfMonth) + "/" +
                                String.format("%02d", monthOfYear + 1) + "/" + "$year"
                    )
                    try {
                        val format = SimpleDateFormat("dd/MM/yyyy")
                        val date: Date? = format.parse(edt_date.text.toString())
                        dob = inputDateFormat.format(date!!)
                    } catch (e: ParseException) {
                        e.printStackTrace()
                    }

                }, mYear, mMonth, mDay
            )
//            val min = System.currentTimeMillis() - 1000
//            datePickerDialog.datePicker.minDate = min
            datePickerDialog.setTitle("")
            datePickerDialog.show()
        }

        edt_time.setOnClickListener {
            val currentTime = Calendar.getInstance()
            val hour = currentTime[Calendar.HOUR_OF_DAY]
            val minute = currentTime[Calendar.MINUTE]
            val mTimePicker: TimePickerDialog
            mTimePicker = TimePickerDialog(
                this@AddThithi,
                TimePickerDialog.OnTimeSetListener { _, selectedHour, selectedMinute ->
                    edt_time.setText(
                        "$selectedHour:$selectedMinute"
                    )
                }, hour, minute, true
            )
            mTimePicker.setTitle("")
            mTimePicker.show()
        }


        btn_update.setOnClickListener {
            thithi()
        }
    }

    private fun thithi() {
        lay_name.error = null
        when {
            edt_name.length() < 3 -> {
                lay_name.error = "AddName's minimum character is 3."
            }
            else -> {
                val thithiData = ThithiData(
                    relationship = edt_relationship.text.toString()
                        .toLowerCase(Locale.getDefault()),
                    name = edt_name.text.toString().toLowerCase(Locale.getDefault()),
                    masamSauramanam = edt_masam_sauramanam.text.toString()
                        .toLowerCase(Locale.getDefault()),
                    masamChandramanam = edt_masam_chandramanam.text.toString()
                        .toLowerCase(Locale.getDefault()),
                    paksham = edt_paksham.text.toString().toLowerCase(Locale.getDefault()),
                    thithi = edt_thithi.text.toString().toLowerCase(Locale.getDefault()),
                    date = edt_date.text.toString().toLowerCase(Locale.getDefault()),
                    time = edt_time.text.toString().toLowerCase(Locale.getDefault())
                )
                if (internet!!.checkMobileInternetConn(this@AddThithi)) {
                    if (thithiId.isNullOrEmpty()) {
                        if (userId != null) {
                            create(
                                userId = userId!!,
                                data = thithiData
                            )
                        } else {
                            create(
                                userId = getData("user_id", applicationContext).toString(),
                                data = thithiData
                            )
                        }

                    } else {
                        if (userId != null) {
                            update(
                                userId = userId!!,
                                thithiId = thithiId.toString(),
                                data = thithiData
                            )
                        } else {
                            update(
                                userId = getData("user_id", applicationContext).toString(),
                                thithiId = thithiId.toString(),
                                data = thithiData
                            )
                        }
                    }
                } else {
                    showErrorMessage(
                        lay_root,
                        getString(R.string.msg_no_internet)
                    )
                }
            }
        }
    }

    private fun create(userId: String, data: ThithiData) {
        if (internet!!.checkMobileInternetConn(this@AddThithi)) {
            try {
                Log.e("body", data.toString())
                val familyCreate =
                    RetrofitClient.instanceClient.thithi(id = userId, thithiData = data)
                familyCreate.enqueue(
                    RetrofitWithBar(this@AddThithi, object : Callback<FamilyDto> {
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
                                    this@AddThithi
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

    private fun update(userId: String, thithiId: String, data: ThithiData) {
        if (internet!!.checkMobileInternetConn(this@AddThithi)) {
            try {
                Log.e("body", "$thithiId $data")
                val familyCreate = RetrofitClient.instanceClient.thithi(
                    userId = userId,
                    thithiId = thithiId,
                    thithiData = data
                )
                familyCreate.enqueue(
                    RetrofitWithBar(this@AddThithi, object : Callback<ResDto> {
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
                                    this@AddThithi
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

    override fun onStop() {
        super.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()

    }

    companion object {
        fun newInstance(): AddThithi =
            AddThithi()
    }
}


package com.vunity.family.shraddha.vazhakkam

import `in`.galaxyofandroid.spinerdialog.SpinnerDialog
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.Handler
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
import com.vunity.family.FamilyData
import com.vunity.family.FamilyDto
import com.vunity.family.Vazhakkam
import com.vunity.general.getData
import com.vunity.general.sessionExpired
import com.vunity.general.showErrorMessage
import com.vunity.general.showMessage
import com.vunity.server.InternetDetector
import com.vunity.server.RetrofitClient
import com.vunity.server.RetrofitWithBar
import com.vunity.user.ErrorMsgDto
import kotlinx.android.synthetic.main.act_add_vazhakkam.*
import kotlinx.android.synthetic.main.toolbar.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*


class AddVazhakkam : AppCompatActivity() {

    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
    var internet: InternetDetector? = null
    private var family: Call<FamilyDto>? = null
    var userId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.act_add_vazhakkam)

        internet = InternetDetector.getInstance(applicationContext)
        txt_title.text = getString(R.string.vazhakkam)
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

                edt_koorcham.setText(
                    familyData?.shraardhaInfo!!.shraddha_vazhakkam?.koorcham.toString()
                )
                edt_tharpana_koorcham.setText(
                    familyData.shraardhaInfo!!.shraddha_vazhakkam?.tharpanaKoorcham.toString()
                )
                edt_pindam_count.setText(
                    familyData.shraardhaInfo!!.shraddha_vazhakkam?.pindamCount.toString()
                )
                edt_krusaram.setText(
                    familyData.shraardhaInfo!!.shraddha_vazhakkam?.krusaram.toString()
                )
                edt_pundra_dharanam.setText(
                    familyData.shraardhaInfo!!.shraddha_vazhakkam?.pundraDharanam.toString()
                )
                edt_other.setText(
                    familyData.shraardhaInfo!!.shraddha_vazhakkam?.other.toString()
                )

            }
        } catch (exception: Exception) {
            Log.e("Exception", exception.toString())
        }

        edt_koorcham.setOnClickListener {
            val koorcham: ArrayList<String> = arrayListOf()
            koorcham.addAll(resources.getStringArray(R.array.koorcham))
            val spinnerDialog = SpinnerDialog(
                this@AddVazhakkam,
                koorcham,
                "Select koorcham.",
                R.style.DialogAnimations_SmileWindow, // For slide animation
                "Cancel"
            )
            spinnerDialog.setCancellable(true) // for cancellable
            spinnerDialog.setShowKeyboard(false)// for open keyboard by default
            spinnerDialog.bindOnSpinerListener { item, position ->
                if (item == getString(R.string.other)) {
                    val dialog = Dialog(this@AddVazhakkam, R.style.DialogTheme)
                    dialog.setContentView(R.layout.dialog_add_genre)
                    val txtTips: MaterialTextView = dialog.findViewById(R.id.txt_tips)
                    val layName: TextInputLayout = dialog.findViewById(R.id.lay_name)
                    val edtName: TextInputEditText = dialog.findViewById(R.id.edt_name)
                    val btnCreate: MaterialButton = dialog.findViewById(R.id.btn_create)
                    txtTips.visibility = View.GONE
                    layName.hint = "Koorcham"
                    btnCreate.text = getString(R.string.add)
                    btnCreate.setOnClickListener {
                        try {
                            layName.error = null
                            if (edtName.length() < 2) {
                                layName.error = "Koorcham's minimum character is 2."
                            } else {
                                edt_koorcham.text = edtName.text
                                edtName.let { v ->
                                    val imm =
                                        this@AddVazhakkam.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
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
                    edt_koorcham.setText(item)
                }
            }
            spinnerDialog.showSpinerDialog()
        }

        edt_tharpana_koorcham.setOnClickListener {
            val tharpanakoorcham: ArrayList<String> = arrayListOf()
            tharpanakoorcham.addAll(resources.getStringArray(R.array.tharpanakoorcham))
            val spinnerDialog = SpinnerDialog(
                this@AddVazhakkam,
                tharpanakoorcham,
                "Select Tharpana koorcham.",
                R.style.DialogAnimations_SmileWindow, // For slide animation
                "Cancel"
            )
            spinnerDialog.setCancellable(true) // for cancellable
            spinnerDialog.setShowKeyboard(false)// for open keyboard by default
            spinnerDialog.bindOnSpinerListener { item, position ->
                if (item == getString(R.string.other)) {
                    val dialog = Dialog(this@AddVazhakkam, R.style.DialogTheme)
                    dialog.setContentView(R.layout.dialog_add_genre)
                    val txtTips: MaterialTextView = dialog.findViewById(R.id.txt_tips)
                    val layName: TextInputLayout = dialog.findViewById(R.id.lay_name)
                    val edtName: TextInputEditText = dialog.findViewById(R.id.edt_name)
                    val btnCreate: MaterialButton = dialog.findViewById(R.id.btn_create)
                    txtTips.visibility = View.GONE
                    layName.hint = "Tharpana koorcham"
                    btnCreate.text = getString(R.string.add)
                    btnCreate.setOnClickListener {
                        try {
                            layName.error = null
                            if (edtName.length() < 2) {
                                layName.error = "Tharpana koorcham's minimum character is 2."
                            } else {
                                edt_tharpana_koorcham.text = edtName.text
                                edtName.let { v ->
                                    val imm =
                                        this@AddVazhakkam.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
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
                    edt_tharpana_koorcham.setText(item)
                }
            }
            spinnerDialog.showSpinerDialog()
        }

        edt_pindam_count.setOnClickListener {
            val pindam: ArrayList<String> = arrayListOf()
            pindam.addAll(resources.getStringArray(R.array.pindam_count))
            val spinnerDialog = SpinnerDialog(
                this@AddVazhakkam,
                pindam,
                "Select Pindam.",
                R.style.DialogAnimations_SmileWindow, // For slide animation
                "Cancel"
            )
            spinnerDialog.setCancellable(true) // for cancellable
            spinnerDialog.setShowKeyboard(false)// for open keyboard by default
            spinnerDialog.bindOnSpinerListener { item, position ->
                if (item == getString(R.string.other)) {
                    val dialog = Dialog(this@AddVazhakkam, R.style.DialogTheme)
                    dialog.setContentView(R.layout.dialog_add_genre)
                    val txtTips: MaterialTextView = dialog.findViewById(R.id.txt_tips)
                    val layName: TextInputLayout = dialog.findViewById(R.id.lay_name)
                    val edtName: TextInputEditText = dialog.findViewById(R.id.edt_name)
                    val btnCreate: MaterialButton = dialog.findViewById(R.id.btn_create)
                    txtTips.visibility = View.GONE
                    layName.hint = "Pindam"
                    btnCreate.text = getString(R.string.add)
                    btnCreate.setOnClickListener {
                        try {
                            layName.error = null
                            if (edtName.length() < 2) {
                                layName.error = "Pindam's minimum character is 2."
                            } else {
                                edt_pindam_count.text = edtName.text
                                edtName.let { v ->
                                    val imm =
                                        this@AddVazhakkam.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
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
                    edt_pindam_count.setText(item)
                }
            }
            spinnerDialog.showSpinerDialog()
        }

        edt_krusaram.setOnClickListener {
            val krusaram: ArrayList<String> = arrayListOf()
            krusaram.addAll(resources.getStringArray(R.array.krusaram))
            val spinnerDialog = SpinnerDialog(
                this@AddVazhakkam,
                krusaram,
                "Select Krusaram.",
                R.style.DialogAnimations_SmileWindow, // For slide animation
                "Cancel"
            )
            spinnerDialog.setCancellable(true) // for cancellable
            spinnerDialog.setShowKeyboard(false)// for open keyboard by default
            spinnerDialog.bindOnSpinerListener { item, position ->
                if (item == getString(R.string.other)) {
                    val dialog = Dialog(this@AddVazhakkam, R.style.DialogTheme)
                    dialog.setContentView(R.layout.dialog_add_genre)
                    val txtTips: MaterialTextView = dialog.findViewById(R.id.txt_tips)
                    val layName: TextInputLayout = dialog.findViewById(R.id.lay_name)
                    val edtName: TextInputEditText = dialog.findViewById(R.id.edt_name)
                    val btnCreate: MaterialButton = dialog.findViewById(R.id.btn_create)
                    txtTips.visibility = View.GONE
                    layName.hint = "Krusaram"
                    btnCreate.text = getString(R.string.add)
                    btnCreate.setOnClickListener {
                        try {
                            layName.error = null
                            if (edtName.length() < 2) {
                                layName.error = "Krusaram's minimum character is 2."
                            } else {
                                edt_krusaram.text = edtName.text
                                edtName.let { v ->
                                    val imm =
                                        this@AddVazhakkam.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
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
                    edt_krusaram.setText(item)
                }
            }
            spinnerDialog.showSpinerDialog()
        }

        edt_pundra_dharanam.setOnClickListener {
            val pundraDharanam: ArrayList<String> = arrayListOf()
            pundraDharanam.addAll(resources.getStringArray(R.array.pundra_dharanam))
            val spinnerDialog = SpinnerDialog(
                this@AddVazhakkam,
                pundraDharanam,
                "Select Pundra dharanam.",
                R.style.DialogAnimations_SmileWindow, // For slide animation
                "Cancel"
            )
            spinnerDialog.setCancellable(true) // for cancellable
            spinnerDialog.setShowKeyboard(false)// for open keyboard by default
            spinnerDialog.bindOnSpinerListener { item, position ->
                if (item == getString(R.string.other)) {
                    val dialog = Dialog(this@AddVazhakkam, R.style.DialogTheme)
                    dialog.setContentView(R.layout.dialog_add_genre)
                    val txtTips: MaterialTextView = dialog.findViewById(R.id.txt_tips)
                    val layName: TextInputLayout = dialog.findViewById(R.id.lay_name)
                    val edtName: TextInputEditText = dialog.findViewById(R.id.edt_name)
                    val btnCreate: MaterialButton = dialog.findViewById(R.id.btn_create)
                    txtTips.visibility = View.GONE
                    layName.hint = "Pundra dharanam"
                    btnCreate.text = getString(R.string.add)
                    btnCreate.setOnClickListener {
                        try {
                            layName.error = null
                            if (edtName.length() < 2) {
                                layName.error = "Pundra dharanam's minimum character is 2."
                            } else {
                                edt_pundra_dharanam.text = edtName.text
                                edtName.let { v ->
                                    val imm =
                                        this@AddVazhakkam.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
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
                    edt_pundra_dharanam.setText(item)
                }
            }
            spinnerDialog.showSpinerDialog()
        }

        btn_update.setOnClickListener {
            update()
        }

    }

    private fun update() {
        lay_koorcham.error = null
        when {
            edt_koorcham.length() < 1 -> {
                lay_koorcham.error = "Koorcham is required."
            }
            else -> {
                val vazhakkam = Vazhakkam(
                    koorcham = edt_koorcham.text.toString().toLowerCase(Locale.getDefault()),
                    tharpanaKoorcham = edt_tharpana_koorcham.text.toString()
                        .toLowerCase(Locale.getDefault()),
                    pindamCount = edt_pindam_count.text.toString().toLowerCase(Locale.getDefault()),
                    krusaram = edt_krusaram.text.toString().toLowerCase(Locale.getDefault()),
                    pundraDharanam = edt_pundra_dharanam.text.toString()
                        .toLowerCase(Locale.getDefault()),
                    other = edt_other.text.toString().toLowerCase(Locale.getDefault())
                )
                vazhakkam(vazhakkam)
            }
        }
    }

    private fun vazhakkam(data: Vazhakkam) {
        if (internet!!.checkMobileInternetConn(this@AddVazhakkam)) {
            try {
                Log.e("body", data.toString())
                family = if (userId != null) {
                    RetrofitClient.instanceClient.vazhakkam(
                        id = userId!!,
                        vazhakkam = data
                    )
                } else {
                    RetrofitClient.instanceClient.vazhakkam(
                        id = getData("user_id", applicationContext).toString(),
                        vazhakkam = data
                    )
                }
                family!!.enqueue(
                    RetrofitWithBar(this@AddVazhakkam, object : Callback<FamilyDto> {
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
                                    this@AddVazhakkam
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

    companion object {
        fun newInstance(): AddVazhakkam =
            AddVazhakkam()
    }
}


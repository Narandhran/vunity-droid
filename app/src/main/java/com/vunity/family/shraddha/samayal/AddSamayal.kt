package com.vunity.family.shraddha.samayal

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
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.textview.MaterialTextView
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.vunity.R
import com.vunity.book.StringAdapter
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
import kotlinx.android.synthetic.main.act_add_samayal.*
import kotlinx.android.synthetic.main.toolbar.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*


class AddSamayal : AppCompatActivity() {

    var kariList: MutableList<Any> = arrayListOf()
    var bhakshanamList: MutableList<Any> = arrayListOf()
    var thugayalList: MutableList<Any> = arrayListOf()
    var urugaList: MutableList<Any> = arrayListOf()
    var pazhangalList: MutableList<Any> = arrayListOf()

    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
    var internet: InternetDetector? = null
    private var family: Call<FamilyDto>? = null
    var userId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.act_add_samayal)

        internet = InternetDetector.getInstance(applicationContext)
        txt_title.text = getString(R.string.samayal)
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
                edt_payasam.setText(familyData?.shraardhaInfo!!.samayal?.payasam.toString())
                edt_thyir_pachchadi.setText(familyData.shraardhaInfo!!.samayal?.thyirPachchadi.toString())
                edt_sweet_pachchadi.setText(familyData.shraardhaInfo!!.samayal?.sweetPachchadi.toString())

                kariList = familyData.shraardhaInfo!!.samayal?.kari!!
                if (kariList.isNotEmpty()) {
                    view_kari?.apply {
                        view_kari?.layoutManager = LinearLayoutManager(
                            applicationContext,
                            LinearLayoutManager.HORIZONTAL,
                            false
                        )
                        view_kari?.setHasFixedSize(true)
                        val genreAdapter =
                            StringAdapter(
                                getString(R.string.view),
                                kariList,
                                this@AddSamayal
                            )
                        view_kari?.adapter = genreAdapter
                    }
                }
                edt_puli_kuttu.setText(familyData.shraardhaInfo!!.samayal?.puliKuttu.toString())
                edt_morkuzhambu.setText(familyData.shraardhaInfo!!.samayal?.morkuzhambu.toString())
                edt_rasam.setText(familyData.shraardhaInfo!!.samayal?.rasam.toString())
                edt_poruchchakuttu.setText(familyData.shraardhaInfo!!.samayal?.poruchchakuttu.toString())

                bhakshanamList = familyData.shraardhaInfo!!.samayal?.bhakshanam!!
                if (bhakshanamList.isNotEmpty()) {
                    view_bhakshanam?.apply {
                        view_bhakshanam?.layoutManager = LinearLayoutManager(
                            applicationContext,
                            LinearLayoutManager.HORIZONTAL,
                            false
                        )
                        view_bhakshanam?.setHasFixedSize(true)
                        val genreAdapter =
                            StringAdapter(
                                getString(R.string.view),
                                bhakshanamList,
                                this@AddSamayal
                            )
                        view_bhakshanam?.adapter = genreAdapter
                    }
                }

                thugayalList = familyData.shraardhaInfo!!.samayal?.thugayal!!
                if (thugayalList.isNotEmpty()) {
                    view_thugayal?.apply {
                        view_thugayal?.layoutManager = LinearLayoutManager(
                            applicationContext,
                            LinearLayoutManager.HORIZONTAL,
                            false
                        )
                        view_thugayal?.setHasFixedSize(true)
                        val genreAdapter =
                            StringAdapter(
                                getString(R.string.view),
                                thugayalList,
                                this@AddSamayal
                            )
                        view_thugayal?.adapter = genreAdapter
                    }
                }

                urugaList = familyData.shraardhaInfo!!.samayal?.uruga!!
                if (urugaList.isNotEmpty()) {
                    view_uruga?.apply {
                        view_uruga?.layoutManager = LinearLayoutManager(
                            applicationContext,
                            LinearLayoutManager.HORIZONTAL,
                            false
                        )
                        view_uruga?.setHasFixedSize(true)
                        val genreAdapter =
                            StringAdapter(
                                getString(R.string.view),
                                urugaList,
                                this@AddSamayal
                            )
                        view_uruga?.adapter = genreAdapter
                    }
                }

                pazhangalList = familyData.shraardhaInfo!!.samayal?.pazhanga!!
                if (pazhangalList.isNotEmpty()) {
                    view_pazhangal?.apply {
                        view_pazhangal?.layoutManager = LinearLayoutManager(
                            applicationContext,
                            LinearLayoutManager.HORIZONTAL,
                            false
                        )
                        view_pazhangal?.setHasFixedSize(true)
                        val genreAdapter =
                            StringAdapter(
                                getString(R.string.view),
                                pazhangalList,
                                this@AddSamayal
                            )
                        view_pazhangal?.adapter = genreAdapter
                    }
                }

                edt_samayal_type.setText(
                    familyData.shraardhaInfo!!.samayal?.samayalType.toString()
                )
                edt_other.setText(
                    familyData.shraardhaInfo!!.samayal?.other.toString()
                )
            }
        } catch (exception: Exception) {
            Log.e("Exception", exception.toString())
        }

        edt_payasam.setOnClickListener {
            val payasam: ArrayList<String> = arrayListOf()
            payasam.addAll(resources.getStringArray(R.array.payasam))
            val spinnerDialog = SpinnerDialog(
                this@AddSamayal,
                payasam,
                "Select payasam.",
                R.style.DialogAnimations_SmileWindow, // For slide animation
                "Cancel"
            )
            spinnerDialog.setCancellable(true) // for cancellable
            spinnerDialog.setShowKeyboard(false)// for open keyboard by default
            spinnerDialog.bindOnSpinerListener { item, position ->
                if (item == getString(R.string.other)) {
                    val dialog = Dialog(this@AddSamayal, R.style.DialogTheme)
                    dialog.setContentView(R.layout.dialog_add_genre)
                    val txtTips: MaterialTextView = dialog.findViewById(R.id.txt_tips)
                    val layName: TextInputLayout = dialog.findViewById(R.id.lay_name)
                    val edtName: TextInputEditText = dialog.findViewById(R.id.edt_name)
                    val btnCreate: MaterialButton = dialog.findViewById(R.id.btn_create)
                    txtTips.visibility = View.GONE
                    layName.hint = "Payasam"
                    btnCreate.text = getString(R.string.add)
                    btnCreate.setOnClickListener {
                        try {
                            layName.error = null
                            if (edtName.length() < 2) {
                                layName.error = "Payasam's minimum character is 2."
                            } else {
                                edt_payasam.text = edtName.text
                                edtName.let { v ->
                                    val imm =
                                        this@AddSamayal.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
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
                    edt_payasam.setText(item)
                }
            }
            spinnerDialog.showSpinerDialog()
        }

        edt_thyir_pachchadi.setOnClickListener {
            val thyirPachchadi: ArrayList<String> = arrayListOf()
            thyirPachchadi.addAll(resources.getStringArray(R.array.thayir_pachchadi))
            val spinnerDialog = SpinnerDialog(
                this@AddSamayal,
                thyirPachchadi,
                "Select thayir pachchadi.",
                R.style.DialogAnimations_SmileWindow, // For slide animation
                "Cancel"
            )
            spinnerDialog.setCancellable(true) // for cancellable
            spinnerDialog.setShowKeyboard(false)// for open keyboard by default
            spinnerDialog.bindOnSpinerListener { item, position ->

                if (item == getString(R.string.other)) {
                    val dialog = Dialog(this@AddSamayal, R.style.DialogTheme)
                    dialog.setContentView(R.layout.dialog_add_genre)
                    val txtTips: MaterialTextView = dialog.findViewById(R.id.txt_tips)
                    val layName: TextInputLayout = dialog.findViewById(R.id.lay_name)
                    val edtName: TextInputEditText = dialog.findViewById(R.id.edt_name)
                    val btnCreate: MaterialButton = dialog.findViewById(R.id.btn_create)
                    txtTips.visibility = View.GONE
                    layName.hint = "Thayir pachchadi"
                    btnCreate.text = getString(R.string.add)
                    btnCreate.setOnClickListener {
                        try {
                            layName.error = null
                            if (edtName.length() < 2) {
                                layName.error = "Thayir pachchadi's minimum character is 2."
                            } else {
                                edt_thyir_pachchadi.text = edtName.text
                                edtName.let { v ->
                                    val imm =
                                        this@AddSamayal.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
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
                    edt_thyir_pachchadi.setText(item)
                }
            }
            spinnerDialog.showSpinerDialog()
        }

        edt_sweet_pachchadi.setOnClickListener {
            val sweetPachchadi: ArrayList<String> = arrayListOf()
            sweetPachchadi.addAll(resources.getStringArray(R.array.sweet_pachchadi))
            val spinnerDialog = SpinnerDialog(
                this@AddSamayal,
                sweetPachchadi,
                "Select Sweet pachchadi.",
                R.style.DialogAnimations_SmileWindow, // For slide animation
                "Cancel"
            )
            spinnerDialog.setCancellable(true) // for cancellable
            spinnerDialog.setShowKeyboard(false)// for open keyboard by default
            spinnerDialog.bindOnSpinerListener { item, position ->

                if (item == getString(R.string.other)) {
                    val dialog = Dialog(this@AddSamayal, R.style.DialogTheme)
                    dialog.setContentView(R.layout.dialog_add_genre)
                    val txtTips: MaterialTextView = dialog.findViewById(R.id.txt_tips)
                    val layName: TextInputLayout = dialog.findViewById(R.id.lay_name)
                    val edtName: TextInputEditText = dialog.findViewById(R.id.edt_name)
                    val btnCreate: MaterialButton = dialog.findViewById(R.id.btn_create)
                    txtTips.visibility = View.GONE
                    layName.hint = "Sweet pachchadi"
                    btnCreate.text = getString(R.string.add)
                    btnCreate.setOnClickListener {
                        try {
                            layName.error = null
                            if (edtName.length() < 2) {
                                layName.error = "Sweet pachchadi's minimum character is 2."
                            } else {
                                edt_sweet_pachchadi.text = edtName.text
                                edtName.let { v ->
                                    val imm =
                                        this@AddSamayal.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
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
                    edt_sweet_pachchadi.setText(item)
                }

            }
            spinnerDialog.showSpinerDialog()
        }

        btn_add_kari.setOnClickListener {
            val kari: ArrayList<String> = arrayListOf()
            kari.addAll(resources.getStringArray(R.array.kari))
            val spinnerDialog = SpinnerDialog(
                this@AddSamayal,
                kari,
                "Select kari.",
                R.style.DialogAnimations_SmileWindow, // For slide animation
                "Cancel"
            )
            spinnerDialog.setCancellable(true) // for cancellable
            spinnerDialog.setShowKeyboard(false)// for open keyboard by default
            spinnerDialog.bindOnSpinerListener { item, position ->

                if (item == getString(R.string.other)) {
                    val dialog = Dialog(this@AddSamayal, R.style.DialogTheme)
                    dialog.setContentView(R.layout.dialog_add_genre)
                    val txtTips: MaterialTextView = dialog.findViewById(R.id.txt_tips)
                    val layName: TextInputLayout = dialog.findViewById(R.id.lay_name)
                    val edtName: TextInputEditText = dialog.findViewById(R.id.edt_name)
                    val btnCreate: MaterialButton = dialog.findViewById(R.id.btn_create)
                    txtTips.visibility = View.GONE
                    layName.hint = "kari"
                    btnCreate.text = getString(R.string.add)
                    btnCreate.setOnClickListener {
                        try {
                            layName.error = null
                            if (edtName.length() < 2) {
                                layName.error = "kari's minimum character is 2."
                            } else {
                                kariList.add(edtName.text.toString())
                                view_kari?.apply {
                                    view_kari?.layoutManager =
                                        LinearLayoutManager(
                                            this@AddSamayal,
                                            LinearLayoutManager.HORIZONTAL,
                                            false
                                        )
                                    view_kari?.setHasFixedSize(true)
                                    val genreAdapter =
                                        StringAdapter("create", kariList, this@AddSamayal)
                                    view_kari?.adapter = genreAdapter
                                }
                                edtName.let { v ->
                                    val imm =
                                        this@AddSamayal.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
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
                    kariList.add(item)
                    view_kari?.apply {
                        view_kari?.layoutManager =
                            LinearLayoutManager(
                                this@AddSamayal,
                                LinearLayoutManager.HORIZONTAL,
                                false
                            )
                        view_kari?.setHasFixedSize(true)
                        val genreAdapter =
                            StringAdapter("create", kariList, this@AddSamayal)
                        view_kari?.adapter = genreAdapter
                    }
                }
            }
            spinnerDialog.showSpinerDialog()
        }

        edt_puli_kuttu.setOnClickListener {
            val puliKuttu: ArrayList<String> = arrayListOf()
            puliKuttu.addAll(resources.getStringArray(R.array.puli_kuttu))
            val spinnerDialog = SpinnerDialog(
                this@AddSamayal,
                puliKuttu,
                "Select pulikkoottu.",
                R.style.DialogAnimations_SmileWindow, // For slide animation
                "Cancel"
            )
            spinnerDialog.setCancellable(true) // for cancellable
            spinnerDialog.setShowKeyboard(false)// for open keyboard by default
            spinnerDialog.bindOnSpinerListener { item, position ->

                if (item == getString(R.string.other)) {
                    val dialog = Dialog(this@AddSamayal, R.style.DialogTheme)
                    dialog.setContentView(R.layout.dialog_add_genre)
                    val txtTips: MaterialTextView = dialog.findViewById(R.id.txt_tips)
                    val layName: TextInputLayout = dialog.findViewById(R.id.lay_name)
                    val edtName: TextInputEditText = dialog.findViewById(R.id.edt_name)
                    val btnCreate: MaterialButton = dialog.findViewById(R.id.btn_create)
                    txtTips.visibility = View.GONE
                    layName.hint = "Pulikkoottu"
                    btnCreate.text = getString(R.string.add)
                    btnCreate.setOnClickListener {
                        try {
                            layName.error = null
                            if (edtName.length() < 2) {
                                layName.error = "Pulikkoottu's minimum character is 2."
                            } else {
                                edt_puli_kuttu.text = edtName.text
                                edtName.let { v ->
                                    val imm =
                                        this@AddSamayal.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
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
                    edt_puli_kuttu.setText(item)
                }

            }
            spinnerDialog.showSpinerDialog()
        }

        edt_morkuzhambu.setOnClickListener {
            val morkuzhambu: ArrayList<String> = arrayListOf()
            morkuzhambu.addAll(resources.getStringArray(R.array.morkuzhambu))
            val spinnerDialog = SpinnerDialog(
                this@AddSamayal,
                morkuzhambu,
                "Select Morkuzhambu.",
                R.style.DialogAnimations_SmileWindow, // For slide animation
                "Cancel"
            )
            spinnerDialog.setCancellable(true) // for cancellable
            spinnerDialog.setShowKeyboard(false)// for open keyboard by default
            spinnerDialog.bindOnSpinerListener { item, position ->

                if (item == getString(R.string.other)) {
                    val dialog = Dialog(this@AddSamayal, R.style.DialogTheme)
                    dialog.setContentView(R.layout.dialog_add_genre)
                    val txtTips: MaterialTextView = dialog.findViewById(R.id.txt_tips)
                    val layName: TextInputLayout = dialog.findViewById(R.id.lay_name)
                    val edtName: TextInputEditText = dialog.findViewById(R.id.edt_name)
                    val btnCreate: MaterialButton = dialog.findViewById(R.id.btn_create)
                    txtTips.visibility = View.GONE
                    layName.hint = "Morkuzhambu"
                    btnCreate.text = getString(R.string.add)
                    btnCreate.setOnClickListener {
                        try {
                            layName.error = null
                            if (edtName.length() < 2) {
                                layName.error = "Morkuzhambu's minimum character is 2."
                            } else {
                                edt_morkuzhambu.text = edtName.text
                                edtName.let { v ->
                                    val imm =
                                        this@AddSamayal.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
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
                    edt_morkuzhambu.setText(item)
                }

            }
            spinnerDialog.showSpinerDialog()
        }

        edt_rasam.setOnClickListener {
            val rasam: ArrayList<String> = arrayListOf()
            rasam.addAll(resources.getStringArray(R.array.rasam))
            val spinnerDialog = SpinnerDialog(
                this@AddSamayal,
                rasam,
                "Select Rasam.",
                R.style.DialogAnimations_SmileWindow, // For slide animation
                "Cancel"
            )
            spinnerDialog.setCancellable(true) // for cancellable
            spinnerDialog.setShowKeyboard(false)// for open keyboard by default
            spinnerDialog.bindOnSpinerListener { item, position ->

                if (item == getString(R.string.other)) {
                    val dialog = Dialog(this@AddSamayal, R.style.DialogTheme)
                    dialog.setContentView(R.layout.dialog_add_genre)
                    val txtTips: MaterialTextView = dialog.findViewById(R.id.txt_tips)
                    val layName: TextInputLayout = dialog.findViewById(R.id.lay_name)
                    val edtName: TextInputEditText = dialog.findViewById(R.id.edt_name)
                    val btnCreate: MaterialButton = dialog.findViewById(R.id.btn_create)
                    txtTips.visibility = View.GONE
                    layName.hint = "Rasam"
                    btnCreate.text = getString(R.string.add)
                    btnCreate.setOnClickListener {
                        try {
                            layName.error = null
                            if (edtName.length() < 2) {
                                layName.error = "Rasam's minimum character is 2."
                            } else {
                                edt_rasam.text = edtName.text
                                edtName.let { v ->
                                    val imm =
                                        this@AddSamayal.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
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
                    edt_rasam.setText(item)
                }

            }
            spinnerDialog.showSpinerDialog()
        }

        edt_poruchchakuttu.setOnClickListener {
            val poruchchakuttu: ArrayList<String> = arrayListOf()
            poruchchakuttu.addAll(resources.getStringArray(R.array.poruchchakuttu))
            val spinnerDialog = SpinnerDialog(
                this@AddSamayal,
                poruchchakuttu,
                "Select porittha koottu.",
                R.style.DialogAnimations_SmileWindow, // For slide animation
                "Cancel"
            )
            spinnerDialog.setCancellable(true) // for cancellable
            spinnerDialog.setShowKeyboard(false)// for open keyboard by default
            spinnerDialog.bindOnSpinerListener { item, position ->
                if (item == getString(R.string.other)) {
                    val dialog = Dialog(this@AddSamayal, R.style.DialogTheme)
                    dialog.setContentView(R.layout.dialog_add_genre)
                    val txtTips: MaterialTextView = dialog.findViewById(R.id.txt_tips)
                    val layName: TextInputLayout = dialog.findViewById(R.id.lay_name)
                    val edtName: TextInputEditText = dialog.findViewById(R.id.edt_name)
                    val btnCreate: MaterialButton = dialog.findViewById(R.id.btn_create)
                    txtTips.visibility = View.GONE
                    layName.hint = "Porittha koottu"
                    btnCreate.text = getString(R.string.add)
                    btnCreate.setOnClickListener {
                        try {
                            layName.error = null
                            if (edtName.length() < 2) {
                                layName.error = "Porittha koottu's minimum character is 2."
                            } else {
                                edt_poruchchakuttu.text = edtName.text
                                edtName.let { v ->
                                    val imm =
                                        this@AddSamayal.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
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
                    edt_poruchchakuttu.setText(item)
                }

            }
            spinnerDialog.showSpinerDialog()
        }

        btn_add_bhakshanam.setOnClickListener {
            val bhakshanam: ArrayList<String> = arrayListOf()
            bhakshanam.addAll(resources.getStringArray(R.array.bhakshanam))
            val spinnerDialog = SpinnerDialog(
                this@AddSamayal,
                bhakshanam,
                "Select Bhakshanam.",
                R.style.DialogAnimations_SmileWindow, // For slide animation
                "Cancel"
            )
            spinnerDialog.setCancellable(true) // for cancellable
            spinnerDialog.setShowKeyboard(false)// for open keyboard by default
            spinnerDialog.bindOnSpinerListener { item, position ->

                if (item == getString(R.string.other)) {
                    val dialog = Dialog(this@AddSamayal, R.style.DialogTheme)
                    dialog.setContentView(R.layout.dialog_add_genre)
                    val txtTips: MaterialTextView = dialog.findViewById(R.id.txt_tips)
                    val layName: TextInputLayout = dialog.findViewById(R.id.lay_name)
                    val edtName: TextInputEditText = dialog.findViewById(R.id.edt_name)
                    val btnCreate: MaterialButton = dialog.findViewById(R.id.btn_create)
                    txtTips.visibility = View.GONE
                    layName.hint = "Bhakshanam"
                    btnCreate.text = getString(R.string.add)
                    btnCreate.setOnClickListener {
                        try {
                            layName.error = null
                            if (edtName.length() < 2) {
                                layName.error = "Bhakshanam's minimum character is 2."
                            } else {
                                bhakshanamList.add(edtName.text.toString())
                                view_bhakshanam?.apply {
                                    view_bhakshanam?.layoutManager =
                                        LinearLayoutManager(
                                            this@AddSamayal,
                                            LinearLayoutManager.HORIZONTAL,
                                            false
                                        )
                                    view_bhakshanam?.setHasFixedSize(true)
                                    val genreAdapter =
                                        StringAdapter("create", bhakshanamList, this@AddSamayal)
                                    view_bhakshanam?.adapter = genreAdapter
                                }
                                edtName.let { v ->
                                    val imm =
                                        this@AddSamayal.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
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
                    bhakshanamList.add(item)
                    view_bhakshanam?.apply {
                        view_bhakshanam?.layoutManager =
                            LinearLayoutManager(
                                this@AddSamayal,
                                LinearLayoutManager.HORIZONTAL,
                                false
                            )
                        view_bhakshanam?.setHasFixedSize(true)
                        val genreAdapter =
                            StringAdapter("create", bhakshanamList, this@AddSamayal)
                        view_bhakshanam?.adapter = genreAdapter
                    }
                }
            }
            spinnerDialog.showSpinerDialog()
        }

        btn_add_thugayal.setOnClickListener {
            val thugayal: ArrayList<String> = arrayListOf()
            thugayal.addAll(resources.getStringArray(R.array.thugayal))
            val spinnerDialog = SpinnerDialog(
                this@AddSamayal,
                thugayal,
                "Select thuvaiyal.",
                R.style.DialogAnimations_SmileWindow, // For slide animation
                "Cancel"
            )
            spinnerDialog.setCancellable(true) // for cancellable
            spinnerDialog.setShowKeyboard(false)// for open keyboard by default
            spinnerDialog.bindOnSpinerListener { item, position ->

                if (item == getString(R.string.other)) {
                    val dialog = Dialog(this@AddSamayal, R.style.DialogTheme)
                    dialog.setContentView(R.layout.dialog_add_genre)
                    val txtTips: MaterialTextView = dialog.findViewById(R.id.txt_tips)
                    val layName: TextInputLayout = dialog.findViewById(R.id.lay_name)
                    val edtName: TextInputEditText = dialog.findViewById(R.id.edt_name)
                    val btnCreate: MaterialButton = dialog.findViewById(R.id.btn_create)
                    txtTips.visibility = View.GONE
                    layName.hint = "Thuvaiyal"
                    btnCreate.text = getString(R.string.add)
                    btnCreate.setOnClickListener {
                        try {
                            layName.error = null
                            if (edtName.length() < 2) {
                                layName.error = "Thuvaiyal's minimum character is 2."
                            } else {
                                thugayalList.add(edtName.text.toString())
                                view_thugayal?.apply {
                                    view_thugayal?.layoutManager =
                                        LinearLayoutManager(
                                            this@AddSamayal,
                                            LinearLayoutManager.HORIZONTAL,
                                            false
                                        )
                                    view_thugayal?.setHasFixedSize(true)
                                    val genreAdapter =
                                        StringAdapter("create", thugayalList, this@AddSamayal)
                                    view_thugayal?.adapter = genreAdapter
                                }
                                edtName.let { v ->
                                    val imm =
                                        this@AddSamayal.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
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
                    thugayalList.add(item)
                    view_thugayal?.apply {
                        view_thugayal?.layoutManager =
                            LinearLayoutManager(
                                this@AddSamayal,
                                LinearLayoutManager.HORIZONTAL,
                                false
                            )
                        view_thugayal?.setHasFixedSize(true)
                        val genreAdapter =
                            StringAdapter("create", thugayalList, this@AddSamayal)
                        view_thugayal?.adapter = genreAdapter
                    }
                }
            }
            spinnerDialog.showSpinerDialog()
        }

        btn_add_uruga.setOnClickListener {
            val uruga: ArrayList<String> = arrayListOf()
            uruga.addAll(resources.getStringArray(R.array.uruga))
            val spinnerDialog = SpinnerDialog(
                this@AddSamayal,
                uruga,
                "Select Oorukai.",
                R.style.DialogAnimations_SmileWindow, // For slide animation
                "Cancel"
            )
            spinnerDialog.setCancellable(true) // for cancellable
            spinnerDialog.setShowKeyboard(false)// for open keyboard by default
            spinnerDialog.bindOnSpinerListener { item, position ->

                if (item == getString(R.string.other)) {
                    val dialog = Dialog(this@AddSamayal, R.style.DialogTheme)
                    dialog.setContentView(R.layout.dialog_add_genre)
                    val txtTips: MaterialTextView = dialog.findViewById(R.id.txt_tips)
                    val layName: TextInputLayout = dialog.findViewById(R.id.lay_name)
                    val edtName: TextInputEditText = dialog.findViewById(R.id.edt_name)
                    val btnCreate: MaterialButton = dialog.findViewById(R.id.btn_create)
                    txtTips.visibility = View.GONE
                    layName.hint = "Oorukai"
                    btnCreate.text = getString(R.string.add)
                    btnCreate.setOnClickListener {
                        try {
                            layName.error = null
                            if (edtName.length() < 2) {
                                layName.error = "Oorukai's minimum character is 2."
                            } else {
                                urugaList.add(edtName.text.toString())
                                view_uruga?.apply {
                                    view_uruga?.layoutManager =
                                        LinearLayoutManager(
                                            this@AddSamayal,
                                            LinearLayoutManager.HORIZONTAL,
                                            false
                                        )
                                    view_uruga?.setHasFixedSize(true)
                                    val genreAdapter =
                                        StringAdapter("create", urugaList, this@AddSamayal)
                                    view_uruga?.adapter = genreAdapter
                                }
                                edtName.let { v ->
                                    val imm =
                                        this@AddSamayal.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
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
                    urugaList.add(item)
                    view_uruga?.apply {
                        view_uruga?.layoutManager =
                            LinearLayoutManager(
                                this@AddSamayal,
                                LinearLayoutManager.HORIZONTAL,
                                false
                            )
                        view_uruga?.setHasFixedSize(true)
                        val genreAdapter =
                            StringAdapter("create", urugaList, this@AddSamayal)
                        view_uruga?.adapter = genreAdapter
                    }
                }
            }
            spinnerDialog.showSpinerDialog()
        }

        btn_add_pazhangal.setOnClickListener {
            val pazhangal: ArrayList<String> = arrayListOf()
            pazhangal.addAll(resources.getStringArray(R.array.pazhangal))
            val spinnerDialog = SpinnerDialog(
                this@AddSamayal,
                pazhangal,
                "Select Pazhangal.",
                R.style.DialogAnimations_SmileWindow, // For slide animation
                "Cancel"
            )
            spinnerDialog.setCancellable(true) // for cancellable
            spinnerDialog.setShowKeyboard(false)// for open keyboard by default
            spinnerDialog.bindOnSpinerListener { item, position ->

                if (item == getString(R.string.other)) {
                    val dialog = Dialog(this@AddSamayal, R.style.DialogTheme)
                    dialog.setContentView(R.layout.dialog_add_genre)
                    val txtTips: MaterialTextView = dialog.findViewById(R.id.txt_tips)
                    val layName: TextInputLayout = dialog.findViewById(R.id.lay_name)
                    val edtName: TextInputEditText = dialog.findViewById(R.id.edt_name)
                    val btnCreate: MaterialButton = dialog.findViewById(R.id.btn_create)
                    txtTips.visibility = View.GONE
                    layName.hint = "Pazhangal"
                    btnCreate.text = getString(R.string.add)
                    btnCreate.setOnClickListener {
                        try {
                            layName.error = null
                            if (edtName.length() < 2) {
                                layName.error = "Pazhangal's minimum character is 2."
                            } else {
                                pazhangalList.add(edtName.text.toString())
                                view_pazhangal?.apply {
                                    view_pazhangal?.layoutManager =
                                        LinearLayoutManager(
                                            this@AddSamayal,
                                            LinearLayoutManager.HORIZONTAL,
                                            false
                                        )
                                    view_pazhangal?.setHasFixedSize(true)
                                    val genreAdapter =
                                        StringAdapter("create", pazhangalList, this@AddSamayal)
                                    view_pazhangal?.adapter = genreAdapter
                                }
                                edtName.let { v ->
                                    val imm =
                                        this@AddSamayal.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
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
                    pazhangalList.add(item)
                    view_pazhangal?.apply {
                        view_pazhangal?.layoutManager =
                            LinearLayoutManager(
                                this@AddSamayal,
                                LinearLayoutManager.HORIZONTAL,
                                false
                            )
                        view_pazhangal?.setHasFixedSize(true)
                        val genreAdapter =
                            StringAdapter("create", pazhangalList, this@AddSamayal)
                        view_pazhangal?.adapter = genreAdapter
                    }
                }
            }
            spinnerDialog.showSpinerDialog()
        }

        edt_samayal_type.setOnClickListener {
            val samayalType: ArrayList<String> = arrayListOf()
            samayalType.addAll(resources.getStringArray(R.array.samayal_type))
            val spinnerDialog = SpinnerDialog(
                this@AddSamayal,
                samayalType,
                "Select AddSamayal type.",
                R.style.DialogAnimations_SmileWindow, // For slide animation
                "Cancel"
            )
            spinnerDialog.setCancellable(true) // for cancellable
            spinnerDialog.setShowKeyboard(false)// for open keyboard by default
            spinnerDialog.bindOnSpinerListener { item, position ->
                if (item == getString(R.string.other)) {
                    val dialog = Dialog(this@AddSamayal, R.style.DialogTheme)
                    dialog.setContentView(R.layout.dialog_add_genre)
                    val txtTips: MaterialTextView = dialog.findViewById(R.id.txt_tips)
                    val layName: TextInputLayout = dialog.findViewById(R.id.lay_name)
                    val edtName: TextInputEditText = dialog.findViewById(R.id.edt_name)
                    val btnCreate: MaterialButton = dialog.findViewById(R.id.btn_create)
                    txtTips.visibility = View.GONE
                    layName.hint = "AddSamayal type"
                    btnCreate.text = getString(R.string.add)
                    btnCreate.setOnClickListener {
                        try {
                            layName.error = null
                            if (edtName.length() < 2) {
                                layName.error = "AddSamayal type's minimum character is 2."
                            } else {
                                edt_samayal_type.text = edtName.text
                                edtName.let { v ->
                                    val imm =
                                        this@AddSamayal.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
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
                    edt_samayal_type.setText(item)
                }
            }
            spinnerDialog.showSpinerDialog()
        }

        btn_update.setOnClickListener {
            update()
        }
    }

    private fun update() {

        lay_payasam.error = null
        when {
            edt_payasam.length() < 1 -> {
                lay_payasam.error = "Payasam is required."
            }
            else -> {
                val samayal = com.vunity.family.Samayal(
                    payasam = edt_payasam.text.toString().toLowerCase(Locale.getDefault()),
                    thyirPachchadi = edt_thyir_pachchadi.text.toString()
                        .toLowerCase(Locale.getDefault()),
                    sweetPachchadi = edt_sweet_pachchadi.text.toString()
                        .toLowerCase(Locale.getDefault()),
                    kari = kariList,
                    bhakshanam = bhakshanamList,
                    thugayal = thugayalList,
                    uruga = urugaList,
                    pazhanga = pazhangalList,
                    puliKuttu = edt_puli_kuttu.text.toString().toLowerCase(Locale.getDefault()),
                    morkuzhambu = edt_morkuzhambu.text.toString().toLowerCase(Locale.getDefault()),
                    rasam = edt_rasam.text.toString().toLowerCase(Locale.getDefault()),
                    poruchchakuttu = edt_poruchchakuttu.text.toString()
                        .toLowerCase(Locale.getDefault()),
                    samayalType = edt_samayal_type.text.toString().toLowerCase(Locale.getDefault()),
                    other = edt_other.text.toString().toLowerCase(Locale.getDefault())
                )
                samayal(samayal)
            }
        }
    }

    private fun samayal(data: com.vunity.family.Samayal) {
        if (internet!!.checkMobileInternetConn(this@AddSamayal)) {
            try {
                Log.e("body", data.toString())
                family = if (userId != null) {
                    RetrofitClient.instanceClient.samayal(
                        id = userId!!,
                        samayal = data
                    )
                } else {
                    RetrofitClient.instanceClient.samayal(
                        id = getData("user_id", applicationContext).toString(),
                        samayal = data
                    )
                }
                family!!.enqueue(
                    RetrofitWithBar(this@AddSamayal, object : Callback<FamilyDto> {
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
                                    this@AddSamayal
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
        fun newInstance(): AddSamayal =
            AddSamayal()
    }

}


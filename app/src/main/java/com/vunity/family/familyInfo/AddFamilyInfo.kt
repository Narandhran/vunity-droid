package com.vunity.family.familyInfo

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
import com.vunity.family.FamilyInfo
import com.vunity.general.getData
import com.vunity.general.sessionExpired
import com.vunity.general.showErrorMessage
import com.vunity.general.showMessage
import com.vunity.server.InternetDetector
import com.vunity.server.RetrofitClient
import com.vunity.server.RetrofitWithBar
import com.vunity.user.ErrorMsgDto
import kotlinx.android.synthetic.main.act_add_familyinfo.*
import kotlinx.android.synthetic.main.toolbar.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class AddFamilyInfo : AppCompatActivity() {

    private var family: Call<FamilyDto>? = null
    var poojaList: MutableList<Any> = arrayListOf()
    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
    var internet: InternetDetector? = null
    var userId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.act_add_familyinfo)

        internet = InternetDetector.getInstance(this@AddFamilyInfo)
        txt_edit.visibility = View.GONE
        txt_title.text = getString(R.string.family_information)
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
                edt_nativity.setText(familyData?.familyInfo?.nativity.toString())
                edt_kulatheivam.setText(familyData?.familyInfo?.kulatheivam.toString())
                edt_tongue.setText(familyData?.familyInfo?.motherTongue.toString())
                edt_sampradhayam.setText(familyData?.familyInfo?.sampradhayam.toString())
                edt_subsect.setText(familyData?.familyInfo?.smarthaSubsect.toString())
                edt_telugu_subsect.setText(familyData?.familyInfo?.smarthaSubsectTelugu.toString())
                edt_vaishnavam.setText(familyData?.familyInfo?.vaishnavam.toString())
                edt_vaishnavam_telugu.setText(familyData?.familyInfo?.vaishnavamTelugu.toString())
                edt_madhava.setText(familyData?.familyInfo?.madhava.toString())
                edt_gothram.setText(familyData?.familyInfo?.gothram.toString())
                edt_rushi.setText(familyData?.familyInfo?.rushi.toString())
                edt_pravara.setText(familyData?.familyInfo?.pravara.toString())
                edt_soothram.setText(familyData?.familyInfo?.soothram.toString())
                edt_vedham.setText(familyData?.familyInfo?.vedham.toString())
                poojaList = familyData?.familyInfo?.poojas!!

                if (poojaList.isNotEmpty()) {
                    view_poojas?.apply {
                        view_poojas?.layoutManager =
                            LinearLayoutManager(
                                this@AddFamilyInfo,
                                LinearLayoutManager.HORIZONTAL,
                                false
                            )
                        view_poojas?.setHasFixedSize(true)
                        val adapter =
                            StringAdapter(getString(R.string.create), poojaList, this@AddFamilyInfo)
                        view_poojas?.adapter = adapter
                    }
                }
                edt_pondugal_name.setText(familyData.familyInfo?.pondugalName.toString())
                edt_panchangam.setText(familyData.familyInfo?.panchangam.toString())
                edt_thilakam.setText(familyData.familyInfo?.thilakam.toString())
            }
        } catch (exception: Exception) {
            Log.e("Exception", exception.toString())
        }

        edt_tongue.setOnClickListener {
            val tongue: ArrayList<String> = arrayListOf()
            tongue.addAll(resources.getStringArray(R.array.mother_tongue))
            val spinnerDialog = SpinnerDialog(
                this@AddFamilyInfo,
                tongue,
                "Select mother tongue.",
                R.style.DialogAnimations_SmileWindow, // For slide animation
                "Cancel"
            )
            spinnerDialog.setCancellable(true) // for cancellable
            spinnerDialog.setShowKeyboard(false)// for open keyboard by default
            spinnerDialog.bindOnSpinerListener { item, position ->

                if (item == getString(R.string.other)) {
                    val dialog = Dialog(this@AddFamilyInfo, R.style.DialogTheme)
                    dialog.setContentView(R.layout.dialog_add_genre)
                    val txtTips: MaterialTextView = dialog.findViewById(R.id.txt_tips)
                    val layName: TextInputLayout = dialog.findViewById(R.id.lay_name)
                    val edtName: TextInputEditText = dialog.findViewById(R.id.edt_name)
                    val btnCreate: MaterialButton = dialog.findViewById(R.id.btn_create)
                    txtTips.visibility = View.GONE
                    layName.hint = "Mother tongue"
                    btnCreate.text = getString(R.string.add)
                    btnCreate.setOnClickListener {
                        try {
                            layName.error = null
                            if (edtName.length() < 2) {
                                layName.error = "Mother tongue's minimum character is 2."
                            } else {
                                edt_tongue.setText(edtName.text)
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
                    edt_tongue.setText(item)
                }
            }
            spinnerDialog.showSpinerDialog()
        }

        edt_sampradhayam.setOnClickListener {
            val sampradhayam: ArrayList<String> = arrayListOf()
            sampradhayam.addAll(resources.getStringArray(R.array.sampradhayam))
            val spinnerDialog = SpinnerDialog(
                this@AddFamilyInfo,
                sampradhayam,
                "Select sampradhayam.",
                R.style.DialogAnimations_SmileWindow, // For slide animation
                "Cancel"
            )
            spinnerDialog.setCancellable(true) // for cancellable
            spinnerDialog.setShowKeyboard(false)// for open keyboard by default
            spinnerDialog.bindOnSpinerListener { item, position ->

                if (item == getString(R.string.other)) {
                    val dialog = Dialog(this@AddFamilyInfo, R.style.DialogTheme)
                    dialog.setContentView(R.layout.dialog_add_genre)
                    val txtTips: MaterialTextView = dialog.findViewById(R.id.txt_tips)
                    val layName: TextInputLayout = dialog.findViewById(R.id.lay_name)
                    val edtName: TextInputEditText = dialog.findViewById(R.id.edt_name)
                    val btnCreate: MaterialButton = dialog.findViewById(R.id.btn_create)
                    txtTips.visibility = View.GONE
                    layName.hint = "Sampradhayam"
                    btnCreate.text = getString(R.string.add)
                    btnCreate.setOnClickListener {
                        try {
                            layName.error = null
                            if (edtName.length() < 2) {
                                layName.error = "Sampradhayam's minimum character is 2."
                            } else {
                                edt_sampradhayam.text = edtName.text
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
                    edt_sampradhayam.setText(item)
                }
            }
            spinnerDialog.showSpinerDialog()
        }

        edt_subsect.setOnClickListener {
            val subsect: ArrayList<String> = arrayListOf()
            subsect.addAll(resources.getStringArray(R.array.subsect))
            val spinnerDialog = SpinnerDialog(
                this@AddFamilyInfo,
                subsect,
                "Select subsect.",
                R.style.DialogAnimations_SmileWindow, // For slide animation
                "Cancel"
            )
            spinnerDialog.setCancellable(true) // for cancellable
            spinnerDialog.setShowKeyboard(false)// for open keyboard by default
            spinnerDialog.bindOnSpinerListener { item, position ->
                if (item == getString(R.string.other)) {
                    val dialog = Dialog(this@AddFamilyInfo, R.style.DialogTheme)
                    dialog.setContentView(R.layout.dialog_add_genre)
                    val txtTips: MaterialTextView = dialog.findViewById(R.id.txt_tips)
                    val layName: TextInputLayout = dialog.findViewById(R.id.lay_name)
                    val edtName: TextInputEditText = dialog.findViewById(R.id.edt_name)
                    val btnCreate: MaterialButton = dialog.findViewById(R.id.btn_create)
                    txtTips.visibility = View.GONE
                    layName.hint = "Subsect"
                    btnCreate.text = getString(R.string.add)
                    btnCreate.setOnClickListener {
                        try {
                            layName.error = null
                            if (edtName.length() < 2) {
                                layName.error = "Subsect's minimum character is 2."
                            } else {
                                edt_subsect.text = edtName.text
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
                    edt_subsect.setText(item)
                }
            }
            spinnerDialog.showSpinerDialog()
        }

        edt_telugu_subsect.setOnClickListener {
            val subsect: ArrayList<String> = arrayListOf()
            subsect.addAll(resources.getStringArray(R.array.telugu_subsect))
            val spinnerDialog = SpinnerDialog(
                this@AddFamilyInfo,
                subsect,
                "Select subsect.",
                R.style.DialogAnimations_SmileWindow, // For slide animation
                "Cancel"
            )
            spinnerDialog.setCancellable(true) // for cancellable
            spinnerDialog.setShowKeyboard(false)// for open keyboard by default
            spinnerDialog.bindOnSpinerListener { item, position ->
                if (item == getString(R.string.other)) {
                    val dialog = Dialog(this@AddFamilyInfo, R.style.DialogTheme)
                    dialog.setContentView(R.layout.dialog_add_genre)
                    val txtTips: MaterialTextView = dialog.findViewById(R.id.txt_tips)
                    val layName: TextInputLayout = dialog.findViewById(R.id.lay_name)
                    val edtName: TextInputEditText = dialog.findViewById(R.id.edt_name)
                    val btnCreate: MaterialButton = dialog.findViewById(R.id.btn_create)
                    txtTips.visibility = View.GONE
                    layName.hint = "Telugu subsect"
                    btnCreate.text = getString(R.string.add)
                    btnCreate.setOnClickListener {
                        try {
                            layName.error = null
                            if (edtName.length() < 2) {
                                layName.error = "Telugu Subsect's minimum character is 2."
                            } else {
                                edt_telugu_subsect.text = edtName.text
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
                    edt_telugu_subsect.setText(item)
                }
            }
            spinnerDialog.showSpinerDialog()
        }

        edt_vaishnavam.setOnClickListener {
            val vaishnavam: ArrayList<String> = arrayListOf()
            vaishnavam.addAll(resources.getStringArray(R.array.vaishnavam))
            val spinnerDialog = SpinnerDialog(
                this@AddFamilyInfo,
                vaishnavam,
                "Select vaishnavam.",
                R.style.DialogAnimations_SmileWindow, // For slide animation
                "Cancel"
            )
            spinnerDialog.setCancellable(true) // for cancellable
            spinnerDialog.setShowKeyboard(false)// for open keyboard by default
            spinnerDialog.bindOnSpinerListener { item, position ->
                if (item == getString(R.string.other)) {
                    val dialog = Dialog(this@AddFamilyInfo, R.style.DialogTheme)
                    dialog.setContentView(R.layout.dialog_add_genre)
                    val txtTips: MaterialTextView = dialog.findViewById(R.id.txt_tips)
                    val layName: TextInputLayout = dialog.findViewById(R.id.lay_name)
                    val edtName: TextInputEditText = dialog.findViewById(R.id.edt_name)
                    val btnCreate: MaterialButton = dialog.findViewById(R.id.btn_create)
                    txtTips.visibility = View.GONE
                    layName.hint = "Vaishnavam"
                    btnCreate.text = getString(R.string.add)
                    btnCreate.setOnClickListener {
                        try {
                            layName.error = null
                            if (edtName.length() < 2) {
                                layName.error = "Vaishnavam minimum character is 2."
                            } else {
                                edt_vaishnavam.text = edtName.text
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
                    edt_vaishnavam.setText(item)
                }
            }
            spinnerDialog.showSpinerDialog()
        }

        edt_vaishnavam_telugu.setOnClickListener {
            val vaishnavam: ArrayList<String> = arrayListOf()
            vaishnavam.addAll(resources.getStringArray(R.array.vaishnavam))
            val spinnerDialog = SpinnerDialog(
                this@AddFamilyInfo,
                vaishnavam,
                "Select vaishnavam.",
                R.style.DialogAnimations_SmileWindow, // For slide animation
                "Cancel"
            )
            spinnerDialog.setCancellable(true) // for cancellable
            spinnerDialog.setShowKeyboard(false)// for open keyboard by default
            spinnerDialog.bindOnSpinerListener { item, position ->
                if (item == getString(R.string.other)) {
                    val dialog = Dialog(this@AddFamilyInfo, R.style.DialogTheme)
                    dialog.setContentView(R.layout.dialog_add_genre)
                    val txtTips: MaterialTextView = dialog.findViewById(R.id.txt_tips)
                    val layName: TextInputLayout = dialog.findViewById(R.id.lay_name)
                    val edtName: TextInputEditText = dialog.findViewById(R.id.edt_name)
                    val btnCreate: MaterialButton = dialog.findViewById(R.id.btn_create)
                    txtTips.visibility = View.GONE
                    layName.hint = "Vaishnavam"
                    btnCreate.text = getString(R.string.add)
                    btnCreate.setOnClickListener {
                        try {
                            layName.error = null
                            if (edtName.length() < 2) {
                                layName.error = "Vaishnavam minimum character is 2."
                            } else {
                                edt_vaishnavam_telugu.text = edtName.text
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
                    edt_vaishnavam_telugu.setText(item)
                }
            }
            spinnerDialog.showSpinerDialog()
        }

        edt_madhava.setOnClickListener {
            val madhava: ArrayList<String> = arrayListOf()
            madhava.addAll(resources.getStringArray(R.array.madhava))
            val spinnerDialog = SpinnerDialog(
                this@AddFamilyInfo,
                madhava,
                "Select madhava.",
                R.style.DialogAnimations_SmileWindow, // For slide animation
                "Cancel"
            )
            spinnerDialog.setCancellable(true) // for cancellable
            spinnerDialog.setShowKeyboard(false)// for open keyboard by default
            spinnerDialog.bindOnSpinerListener { item, position ->
                if (item == getString(R.string.other)) {
                    val dialog = Dialog(this@AddFamilyInfo, R.style.DialogTheme)
                    dialog.setContentView(R.layout.dialog_add_genre)
                    val txtTips: MaterialTextView = dialog.findViewById(R.id.txt_tips)
                    val layName: TextInputLayout = dialog.findViewById(R.id.lay_name)
                    val edtName: TextInputEditText = dialog.findViewById(R.id.edt_name)
                    val btnCreate: MaterialButton = dialog.findViewById(R.id.btn_create)
                    txtTips.visibility = View.GONE
                    layName.hint = "Madhava"
                    btnCreate.text = getString(R.string.add)
                    btnCreate.setOnClickListener {
                        try {
                            layName.error = null
                            if (edtName.length() < 2) {
                                layName.error = "Madhava minimum character is 2."
                            } else {
                                edt_madhava.text = edtName.text
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
                    edt_madhava.setText(item)
                }
            }
            spinnerDialog.showSpinerDialog()
        }

        edt_gothram.setOnClickListener {
            val gothram: ArrayList<String> = arrayListOf()
            gothram.addAll(resources.getStringArray(R.array.gothram))
            val spinnerDialog = SpinnerDialog(
                this@AddFamilyInfo,
                gothram,
                "Select gothram.",
                R.style.DialogAnimations_SmileWindow, // For slide animation
                "Cancel"
            )
            spinnerDialog.setCancellable(true) // for cancellable
            spinnerDialog.setShowKeyboard(false)// for open keyboard by default
            spinnerDialog.bindOnSpinerListener { item, position ->
                if (item == getString(R.string.other)) {
                    val dialog = Dialog(this@AddFamilyInfo, R.style.DialogTheme)
                    dialog.setContentView(R.layout.dialog_add_genre)
                    val txtTips: MaterialTextView = dialog.findViewById(R.id.txt_tips)
                    val layName: TextInputLayout = dialog.findViewById(R.id.lay_name)
                    val edtName: TextInputEditText = dialog.findViewById(R.id.edt_name)
                    val btnCreate: MaterialButton = dialog.findViewById(R.id.btn_create)
                    txtTips.visibility = View.GONE
                    layName.hint = "Gothram"
                    btnCreate.text = getString(R.string.add)
                    btnCreate.setOnClickListener {
                        try {
                            layName.error = null
                            if (edtName.length() < 2) {
                                layName.error = "Gothram minimum character is 2."
                            } else {
                                edt_gothram.text = edtName.text
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
                    edt_gothram.setText(item)
                }
            }
            spinnerDialog.showSpinerDialog()
        }

        edt_rushi.setOnClickListener {
            val rushi: ArrayList<String> = arrayListOf()
            rushi.addAll(resources.getStringArray(R.array.rushi))
            val spinnerDialog = SpinnerDialog(
                this@AddFamilyInfo,
                rushi,
                "Select rushi.",
                R.style.DialogAnimations_SmileWindow, // For slide animation
                "Cancel"
            )
            spinnerDialog.setCancellable(true) // for cancellable
            spinnerDialog.setShowKeyboard(false)// for open keyboard by default
            spinnerDialog.bindOnSpinerListener { item, position ->
                if (item == getString(R.string.other)) {
                    val dialog = Dialog(this@AddFamilyInfo, R.style.DialogTheme)
                    dialog.setContentView(R.layout.dialog_add_genre)
                    val txtTips: MaterialTextView = dialog.findViewById(R.id.txt_tips)
                    val layName: TextInputLayout = dialog.findViewById(R.id.lay_name)
                    val edtName: TextInputEditText = dialog.findViewById(R.id.edt_name)
                    val btnCreate: MaterialButton = dialog.findViewById(R.id.btn_create)
                    txtTips.visibility = View.GONE
                    layName.hint = "Rushi"
                    btnCreate.text = getString(R.string.add)
                    btnCreate.setOnClickListener {
                        try {
                            layName.error = null
                            if (edtName.length() < 2) {
                                layName.error = "Rushi minimum character is 2."
                            } else {
                                edt_rushi.text = edtName.text
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
                    edt_rushi.setText(item)
                }
            }
            spinnerDialog.showSpinerDialog()
        }

        edt_soothram.setOnClickListener {
            val soothram: ArrayList<String> = arrayListOf()
            soothram.addAll(resources.getStringArray(R.array.soothram))
            val spinnerDialog = SpinnerDialog(
                this@AddFamilyInfo,
                soothram,
                "Select soothram.",
                R.style.DialogAnimations_SmileWindow, // For slide animation
                "Cancel"
            )
            spinnerDialog.setCancellable(true) // for cancellable
            spinnerDialog.setShowKeyboard(false)// for open keyboard by default
            spinnerDialog.bindOnSpinerListener { item, position ->

                if (item == getString(R.string.other)) {
                    val dialog = Dialog(this@AddFamilyInfo, R.style.DialogTheme)
                    dialog.setContentView(R.layout.dialog_add_genre)
                    val txtTips: MaterialTextView = dialog.findViewById(R.id.txt_tips)
                    val layName: TextInputLayout = dialog.findViewById(R.id.lay_name)
                    val edtName: TextInputEditText = dialog.findViewById(R.id.edt_name)
                    val btnCreate: MaterialButton = dialog.findViewById(R.id.btn_create)
                    txtTips.visibility = View.GONE
                    layName.hint = "Soothram"
                    btnCreate.text = getString(R.string.add)
                    btnCreate.setOnClickListener {
                        try {
                            layName.error = null
                            if (edtName.length() < 2) {
                                layName.error = "Soothram minimum character is 2."
                            } else {
                                edt_soothram.text = edtName.text
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
                    edt_soothram.setText(item)
                }
            }
            spinnerDialog.showSpinerDialog()
        }

        edt_vedham.setOnClickListener {
            val vedham: ArrayList<String> = arrayListOf()
            vedham.addAll(resources.getStringArray(R.array.vedham))
            val spinnerDialog = SpinnerDialog(
                this@AddFamilyInfo,
                vedham,
                "Select vedham.",
                R.style.DialogAnimations_SmileWindow, // For slide animation
                "Cancel"
            )
            spinnerDialog.setCancellable(true) // for cancellable
            spinnerDialog.setShowKeyboard(false)// for open keyboard by default
            spinnerDialog.bindOnSpinerListener { item, position ->
                edt_vedham.setText(item)
            }
            spinnerDialog.showSpinerDialog()
        }

        btn_add_poojas.setOnClickListener {
            val poojas: ArrayList<String> = arrayListOf()
            poojas.addAll(resources.getStringArray(R.array.poojas))
            val spinnerDialog = SpinnerDialog(
                this@AddFamilyInfo,
                poojas,
                "Select poojas.",
                R.style.DialogAnimations_SmileWindow, // For slide animation
                "Cancel"
            )
            spinnerDialog.setCancellable(true) // for cancellable
            spinnerDialog.setShowKeyboard(false)// for open keyboard by default
            spinnerDialog.bindOnSpinerListener { item, position ->

                if (item == getString(R.string.other)) {
                    val dialog = Dialog(this@AddFamilyInfo, R.style.DialogTheme)
                    dialog.setContentView(R.layout.dialog_add_genre)
                    val txtTips: MaterialTextView = dialog.findViewById(R.id.txt_tips)
                    val layName: TextInputLayout = dialog.findViewById(R.id.lay_name)
                    val edtName: TextInputEditText = dialog.findViewById(R.id.edt_name)
                    val btnCreate: MaterialButton = dialog.findViewById(R.id.btn_create)
                    txtTips.visibility = View.GONE
                    layName.hint = "Poojas"
                    btnCreate.text = getString(R.string.add)
                    btnCreate.setOnClickListener {
                        try {
                            layName.error = null
                            if (edtName.length() < 2) {
                                layName.error = "Poojas minimum character is 2."
                            } else {
                                poojaList.add(edtName.text.toString())
                                view_poojas?.apply {
                                    view_poojas?.layoutManager =
                                        LinearLayoutManager(
                                            this@AddFamilyInfo,
                                            LinearLayoutManager.HORIZONTAL,
                                            false
                                        )
                                    view_poojas?.setHasFixedSize(true)
                                    val genreAdapter =
                                        StringAdapter("create", poojaList, this@AddFamilyInfo)
                                    view_poojas?.adapter = genreAdapter
                                }
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
                    poojaList.add(item)
                    view_poojas?.apply {
                        view_poojas?.layoutManager =
                            LinearLayoutManager(
                                this@AddFamilyInfo,
                                LinearLayoutManager.HORIZONTAL,
                                false
                            )
                        view_poojas?.setHasFixedSize(true)
                        val genreAdapter =
                            StringAdapter("create", poojaList, this@AddFamilyInfo)
                        view_poojas?.adapter = genreAdapter
                    }
                }
            }
            spinnerDialog.showSpinerDialog()
        }

        edt_panchangam.setOnClickListener {
            val panchangam: ArrayList<String> = arrayListOf()
            panchangam.addAll(resources.getStringArray(R.array.panchangam))
            val spinnerDialog = SpinnerDialog(
                this@AddFamilyInfo,
                panchangam,
                "Select Panchangam.",
                R.style.DialogAnimations_SmileWindow, // For slide animation
                "Cancel"
            )
            spinnerDialog.setCancellable(true) // for cancellable
            spinnerDialog.setShowKeyboard(false)// for open keyboard by default
            spinnerDialog.bindOnSpinerListener { item, position ->

                if (item == getString(R.string.other)) {
                    val dialog = Dialog(this@AddFamilyInfo, R.style.DialogTheme)
                    dialog.setContentView(R.layout.dialog_add_genre)
                    val txtTips: MaterialTextView = dialog.findViewById(R.id.txt_tips)
                    val layName: TextInputLayout = dialog.findViewById(R.id.lay_name)
                    val edtName: TextInputEditText = dialog.findViewById(R.id.edt_name)
                    val btnCreate: MaterialButton = dialog.findViewById(R.id.btn_create)
                    txtTips.visibility = View.GONE
                    layName.hint = "Panchangam"
                    btnCreate.text = getString(R.string.add)
                    btnCreate.setOnClickListener {
                        try {
                            layName.error = null
                            if (edtName.length() < 2) {
                                layName.error = "Panchangam minimum character is 2."
                            } else {
                                edt_panchangam.text = edtName.text
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
                    edt_panchangam.setText(item)
                }
            }
            spinnerDialog.showSpinerDialog()
        }

        edt_thilakam.setOnClickListener {
            val thilakam: ArrayList<String> = arrayListOf()
            thilakam.addAll(resources.getStringArray(R.array.thilakam))
            val spinnerDialog = SpinnerDialog(
                this@AddFamilyInfo,
                thilakam,
                "Select Thilakam.",
                R.style.DialogAnimations_SmileWindow, // For slide animation
                "Cancel"
            )
            spinnerDialog.setCancellable(true) // for cancellable
            spinnerDialog.setShowKeyboard(false)// for open keyboard by default
            spinnerDialog.bindOnSpinerListener { item, position ->

                if (item == getString(R.string.other)) {
                    val dialog = Dialog(this@AddFamilyInfo, R.style.DialogTheme)
                    dialog.setContentView(R.layout.dialog_add_genre)
                    val txtTips: MaterialTextView = dialog.findViewById(R.id.txt_tips)
                    val layName: TextInputLayout = dialog.findViewById(R.id.lay_name)
                    val edtName: TextInputEditText = dialog.findViewById(R.id.edt_name)
                    val btnCreate: MaterialButton = dialog.findViewById(R.id.btn_create)
                    txtTips.visibility = View.GONE
                    layName.hint = "Thilakam"
                    btnCreate.text = getString(R.string.add)
                    btnCreate.setOnClickListener {
                        try {
                            layName.error = null
                            if (edtName.length() < 2) {
                                layName.error = "Thilakam minimum character is 2."
                            } else {
                                edt_thilakam.text = edtName.text
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
                    edt_thilakam.setText(item)
                }
            }
            spinnerDialog.showSpinerDialog()
        }

        btn_update.setOnClickListener {
            update()
        }
    }

    private fun update() {
        lay_nativity.error = null

        when {
            edt_nativity.length() < 3 -> {
                lay_nativity.error = "Nativity's minimum character is 3."
            }
            else -> {
                val familyInfo = FamilyInfo(
                    nativity = edt_nativity.text.toString().toLowerCase(Locale.getDefault()),
                    kulatheivam = edt_kulatheivam.text.toString().toLowerCase(Locale.getDefault()),
                    motherTongue = edt_tongue.text.toString().toLowerCase(Locale.getDefault()),
                    sampradhayam = edt_sampradhayam.text.toString()
                        .toLowerCase(Locale.getDefault()),
                    smarthaSubsect = edt_subsect.text.toString().toLowerCase(Locale.getDefault()),
                    smarthaSubsectTelugu = edt_telugu_subsect.text.toString()
                        .toLowerCase(Locale.getDefault()),
                    vaishnavam = edt_vaishnavam.text.toString().toLowerCase(Locale.getDefault()),
                    vaishnavamTelugu = edt_vaishnavam_telugu.text.toString()
                        .toLowerCase(Locale.getDefault()),
                    madhava = edt_madhava.text.toString().toLowerCase(Locale.getDefault()),
                    gothram = edt_gothram.text.toString().toLowerCase(Locale.getDefault()),
                    rushi = edt_rushi.text.toString().toLowerCase(Locale.getDefault()),
                    pravara = edt_pravara.text.toString().toLowerCase(Locale.getDefault()),
                    soothram = edt_soothram.text.toString().toLowerCase(Locale.getDefault()),
                    vedham = edt_vedham.text.toString().toLowerCase(Locale.getDefault()),
                    poojas = poojaList,
                    pondugalName = edt_pondugal_name.text.toString()
                        .toLowerCase(Locale.getDefault()),
                    panchangam = edt_panchangam.text.toString().toLowerCase(Locale.getDefault()),
                    thilakam = edt_thilakam.text.toString().toLowerCase(Locale.getDefault())
                )
                family(familyInfo)
            }
        }
    }

    private fun family(familyInfo: FamilyInfo) {
        if (internet!!.checkMobileInternetConn(this@AddFamilyInfo)) {
            try {
                Log.e("familyInfo", familyInfo.toString())
                family = if (userId != null) {
                    RetrofitClient.instanceClient.family(
                        id = userId!!,
                        familyInfo = familyInfo
                    )
                } else {
                    RetrofitClient.instanceClient.family(
                        id = getData("user_id", applicationContext).toString(),
                        familyInfo = familyInfo
                    )
                }
                family!!.enqueue(
                    RetrofitWithBar(this@AddFamilyInfo, object : Callback<FamilyDto> {
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
                                    this@AddFamilyInfo
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
        fun newInstance(): AddFamilyInfo =
            AddFamilyInfo()
    }
}
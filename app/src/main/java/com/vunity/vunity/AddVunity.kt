package com.vunity.vunity

import `in`.galaxyofandroid.spinerdialog.SpinnerDialog
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.DialogFragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.textview.MaterialTextView
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.squareup.picasso.Picasso
import com.vunity.R
import com.vunity.book.StringAdapter
import com.vunity.general.*
import com.vunity.server.InternetDetector
import com.vunity.server.RetrofitClient
import com.vunity.server.RetrofitWithBar
import com.vunity.user.DialogChoosePhoto
import com.vunity.user.ErrorMsgDto
import com.vunity.user.ResDto
import kotlinx.android.synthetic.main.act_add_vunity.*
import kotlinx.android.synthetic.main.toolbar.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.apache.commons.lang3.StringUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.util.*


class AddVunity : AppCompatActivity() {

    var shakhaList: MutableList<Any> = arrayListOf()
    var vedhaAdhyayanamList: MutableList<Any> = arrayListOf()
    var shastraAdhyayanamList: MutableList<Any> = arrayListOf()
    var prayogamList: MutableList<Any> = arrayListOf()
    var isShadangaAdhyayanam = false

    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
    var internet: InternetDetector? = null
    private var unityFrom: Call<ResDto>? = null
    var updateId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.act_add_vunity)

        internet = InternetDetector.getInstance(applicationContext)
        LocalBroadcastManager.getInstance(applicationContext!!).registerReceiver(
            fileLocationReceiver,
            IntentFilter(Constants.fileLocation)
        )

        txt_title.text = getString(R.string.app_name)
        txt_edit.visibility = View.GONE
        im_back.setOnClickListener {
            onBackPressed()
        }

        img_profile.setOnClickListener {
            if (!hasPermissions(
                    applicationContext,
                    *Constants.PERMISSIONS
                )
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    Constants.PERMISSIONS,
                    Constants.PERMISSION_ALL
                )
            } else {
                val builder = AlertDialog.Builder(this@AddVunity)
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
                            checkPermission(this@AddVunity)
                        if (permissionResult) {
                            val bottomSheetDialog = DialogChoosePhoto.instance
                            bottomSheetDialog.setStyle(
                                DialogFragment.STYLE_NORMAL,
                                R.style.CustomBottomSheetDialogTheme
                            )
                            this@AddVunity.supportFragmentManager.let { it1 ->
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
            val data = intent.getStringExtra(getString(R.string.data))
            if (data != null) {
                val jsonAdapter: JsonAdapter<VunityData> =
                    moshi.adapter(VunityData::class.java)
                val vunityData: VunityData? = jsonAdapter.fromJson(data.toString())
                println(vunityData)
                updateId = vunityData?._id.toString()

                Picasso.get().load(
                    getData(
                        "rootPath",
                        applicationContext
                    ) + Enums.Dp.value + vunityData?.photo
                ).placeholder(R.drawable.ic_dummy_profile).into(img_profile)

                edt_fullname.setText(vunityData?.name.toString())
                edt_mobile.setText(vunityData?.mobile.toString())
                edt_city.setText(vunityData?.city.toString())
                edt_vedham.setText(vunityData?.vedham.toString())
                edt_sampradhayam.setText(vunityData?.samprdhayam.toString())

                shakhaList = vunityData?.shakha?.toMutableList()!!
                if (shakhaList.isNotEmpty()) {
                    view_shaka?.apply {
                        view_shaka?.layoutManager = LinearLayoutManager(
                            applicationContext,
                            LinearLayoutManager.HORIZONTAL,
                            false
                        )
                        view_shaka?.setHasFixedSize(true)
                        val genreAdapter =
                            StringAdapter(
                                getString(R.string.create),
                                shakhaList,
                                this@AddVunity
                            )
                        view_shaka?.adapter = genreAdapter
                    }
                }

                vedhaAdhyayanamList = vunityData.vedha_adhyayanam?.toMutableList()!!
                if (vedhaAdhyayanamList.isNotEmpty()) {
                    view_vedha_adhyayanam?.apply {
                        view_vedha_adhyayanam?.layoutManager = LinearLayoutManager(
                            applicationContext,
                            LinearLayoutManager.HORIZONTAL,
                            false
                        )
                        view_vedha_adhyayanam?.setHasFixedSize(true)
                        val genreAdapter =
                            StringAdapter(
                                getString(R.string.create),
                                vedhaAdhyayanamList,
                                this@AddVunity
                            )
                        view_vedha_adhyayanam?.adapter = genreAdapter
                    }
                }

                edt_shadanga_adhyayanam?.setText(vunityData.shadanga_adhyayanam.toString())

                shastraAdhyayanamList = vunityData.shastra_adhyayanam?.toMutableList()!!
                if (shastraAdhyayanamList.isNotEmpty()) {
                    view_shastra_adhyayanam?.apply {
                        view_shastra_adhyayanam?.layoutManager = LinearLayoutManager(
                            applicationContext,
                            LinearLayoutManager.HORIZONTAL,
                            false
                        )
                        view_shastra_adhyayanam?.setHasFixedSize(true)
                        val genreAdapter =
                            StringAdapter(
                                getString(R.string.create),
                                shastraAdhyayanamList,
                                this@AddVunity
                            )
                        view_shastra_adhyayanam?.adapter = genreAdapter
                    }
                }

                prayogamList = vunityData.prayogam?.toMutableList()!!
                if (prayogamList.isNotEmpty()) {
                    view_prayogam?.apply {
                        view_prayogam?.layoutManager = LinearLayoutManager(
                            applicationContext,
                            LinearLayoutManager.HORIZONTAL,
                            false
                        )
                        view_prayogam?.setHasFixedSize(true)
                        val genreAdapter =
                            StringAdapter(
                                getString(R.string.create),
                                prayogamList,
                                this@AddVunity
                            )
                        view_prayogam?.adapter = genreAdapter
                    }
                }

                edt_marital_status.setText(vunityData.marital_status.toString())
                edt_mothertongue.setText(vunityData.mother_tongue.toString())
            }
        } catch (exception: Exception) {
            Log.e("Exception", exception.toString())
        }

        edt_vedham.setOnClickListener {
            val data: ArrayList<String> = arrayListOf()
            data.addAll(resources.getStringArray(R.array.vedham))
            val spinnerDialog = SpinnerDialog(
                this@AddVunity,
                data,
                "Select vedham.",
                R.style.DialogAnimations_SmileWindow, // For slide animation
                "Cancel"
            )
            spinnerDialog.setCancellable(true) // for cancellable
            spinnerDialog.setShowKeyboard(false)// for open keyboard by default
            spinnerDialog.bindOnSpinerListener { item, position ->
                if (item == getString(R.string.other)) {
                    val dialog = Dialog(this@AddVunity, R.style.DialogTheme)
                    dialog.setContentView(R.layout.dialog_add_genre)
                    val txtTips: MaterialTextView = dialog.findViewById(R.id.txt_tips)
                    val layName: TextInputLayout = dialog.findViewById(R.id.lay_name)
                    val edtName: TextInputEditText = dialog.findViewById(R.id.edt_name)
                    val btnCreate: MaterialButton = dialog.findViewById(R.id.btn_create)
                    txtTips.visibility = View.GONE
                    layName.hint = "Vedham"
                    btnCreate.text = getString(R.string.add)
                    btnCreate.setOnClickListener {
                        try {
                            layName.error = null
                            if (edtName.length() < 2) {
                                layName.error = "Vedham's minimum character is 2."
                            } else {
                                edt_vedham.text = edtName.text
                                edtName.let { v ->
                                    val imm =
                                        this@AddVunity.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
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
                    edt_vedham.setText(item)
                }
            }
            spinnerDialog.showSpinerDialog()
        }

        edt_sampradhayam.setOnClickListener {
            val data: ArrayList<String> = arrayListOf()
            data.addAll(resources.getStringArray(R.array.sampradhayam))
            val spinnerDialog = SpinnerDialog(
                this@AddVunity,
                data,
                "Select sampradhayam.",
                R.style.DialogAnimations_SmileWindow, // For slide animation
                "Cancel"
            )
            spinnerDialog.setCancellable(true) // for cancellable
            spinnerDialog.setShowKeyboard(false)// for open keyboard by default
            spinnerDialog.bindOnSpinerListener { item, position ->

                if (item == getString(R.string.other)) {
                    val dialog = Dialog(this@AddVunity, R.style.DialogTheme)
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

        btn_add_shaka.setOnClickListener {
            val data: ArrayList<String> = arrayListOf()
            data.addAll(resources.getStringArray(R.array.shakha))
            val spinnerDialog = SpinnerDialog(
                this@AddVunity,
                data,
                "Select Shakha.",
                R.style.DialogAnimations_SmileWindow, // For slide animation
                "Cancel"
            )
            spinnerDialog.setCancellable(true) // for cancellable
            spinnerDialog.setShowKeyboard(false)// for open keyboard by default
            spinnerDialog.bindOnSpinerListener { item, position ->

                if (item == getString(R.string.other)) {
                    val dialog = Dialog(this@AddVunity, R.style.DialogTheme)
                    dialog.setContentView(R.layout.dialog_add_genre)
                    val txtTips: MaterialTextView = dialog.findViewById(R.id.txt_tips)
                    val layName: TextInputLayout = dialog.findViewById(R.id.lay_name)
                    val edtName: TextInputEditText = dialog.findViewById(R.id.edt_name)
                    val btnCreate: MaterialButton = dialog.findViewById(R.id.btn_create)
                    txtTips.visibility = View.GONE
                    layName.hint = "Shakha"
                    btnCreate.text = getString(R.string.add)
                    btnCreate.setOnClickListener {
                        try {
                            layName.error = null
                            if (edtName.length() < 2) {
                                layName.error = "Shakha's minimum character is 2."
                            } else {
                                shakhaList.add(StringUtils.capitalize(edtName.text.toString()))
                                view_shaka?.apply {
                                    view_shaka?.layoutManager =
                                        LinearLayoutManager(
                                            this@AddVunity,
                                            LinearLayoutManager.HORIZONTAL,
                                            false
                                        )
                                    view_shaka?.setHasFixedSize(true)
                                    val genreAdapter =
                                        StringAdapter(
                                            getString(R.string.create),
                                            shakhaList,
                                            this@AddVunity
                                        )
                                    view_shaka?.adapter = genreAdapter
                                }
                                edtName.let { v ->
                                    val imm =
                                        this@AddVunity.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
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
                    shakhaList.add(StringUtils.capitalize(item))
                    view_shaka?.apply {
                        view_shaka?.layoutManager =
                            LinearLayoutManager(
                                this@AddVunity,
                                LinearLayoutManager.HORIZONTAL,
                                false
                            )
                        view_shaka?.setHasFixedSize(true)
                        val genreAdapter =
                            StringAdapter(getString(R.string.create), shakhaList, this@AddVunity)
                        view_shaka?.adapter = genreAdapter
                    }
                }
            }
            spinnerDialog.showSpinerDialog()
        }

        btn_add_vedha_adhyayanam.setOnClickListener {
            val data: ArrayList<String> = arrayListOf()
            data.addAll(resources.getStringArray(R.array.vedha_adhyayanam))
            val spinnerDialog = SpinnerDialog(
                this@AddVunity,
                data,
                "Select Vedha adhyayanam.",
                R.style.DialogAnimations_SmileWindow, // For slide animation
                "Cancel"
            )
            spinnerDialog.setCancellable(true) // for cancellable
            spinnerDialog.setShowKeyboard(false)// for open keyboard by default
            spinnerDialog.bindOnSpinerListener { item, position ->

                if (item == getString(R.string.other)) {
                    val dialog = Dialog(this@AddVunity, R.style.DialogTheme)
                    dialog.setContentView(R.layout.dialog_add_genre)
                    val txtTips: MaterialTextView = dialog.findViewById(R.id.txt_tips)
                    val layName: TextInputLayout = dialog.findViewById(R.id.lay_name)
                    val edtName: TextInputEditText = dialog.findViewById(R.id.edt_name)
                    val btnCreate: MaterialButton = dialog.findViewById(R.id.btn_create)
                    txtTips.visibility = View.GONE
                    layName.hint = "Vedha adhyayanam"
                    btnCreate.text = getString(R.string.add)
                    btnCreate.setOnClickListener {
                        try {
                            layName.error = null
                            if (edtName.length() < 2) {
                                layName.error = "Vedha adhyayanam's minimum character is 2."
                            } else {
                                vedhaAdhyayanamList.add(StringUtils.capitalize(edtName.text.toString()))
                                view_vedha_adhyayanam?.apply {
                                    view_vedha_adhyayanam?.layoutManager =
                                        LinearLayoutManager(
                                            this@AddVunity,
                                            LinearLayoutManager.HORIZONTAL,
                                            false
                                        )
                                    view_vedha_adhyayanam?.setHasFixedSize(true)
                                    val genreAdapter =
                                        StringAdapter(
                                            getString(R.string.create),
                                            vedhaAdhyayanamList,
                                            this@AddVunity
                                        )
                                    view_vedha_adhyayanam?.adapter = genreAdapter
                                }
                                edtName.let { v ->
                                    val imm =
                                        this@AddVunity.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
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
                    vedhaAdhyayanamList.add(StringUtils.capitalize(item))
                    view_vedha_adhyayanam?.apply {
                        view_vedha_adhyayanam?.layoutManager =
                            LinearLayoutManager(
                                this@AddVunity,
                                LinearLayoutManager.HORIZONTAL,
                                false
                            )
                        view_vedha_adhyayanam?.setHasFixedSize(true)
                        val genreAdapter =
                            StringAdapter(
                                getString(R.string.create),
                                vedhaAdhyayanamList,
                                this@AddVunity
                            )
                        view_vedha_adhyayanam?.adapter = genreAdapter
                    }
                }
            }
            spinnerDialog.showSpinerDialog()
        }

        edt_shadanga_adhyayanam.setOnClickListener {
            val data: ArrayList<String> = arrayListOf()
            data.addAll(resources.getStringArray(R.array.shadanga_adhyayanam))
            val spinnerDialog = SpinnerDialog(
                this@AddVunity,
                data,
                "Select Shadanga adhyayanam.",
                R.style.DialogAnimations_SmileWindow, // For slide animation
                "Cancel"
            )
            spinnerDialog.setCancellable(true) // for cancellable
            spinnerDialog.setShowKeyboard(false)// for open keyboard by default
            spinnerDialog.bindOnSpinerListener { item, position ->

                if (item == getString(R.string.other)) {
                    val dialog = Dialog(this@AddVunity, R.style.DialogTheme)
                    dialog.setContentView(R.layout.dialog_add_genre)
                    val txtTips: MaterialTextView = dialog.findViewById(R.id.txt_tips)
                    val layName: TextInputLayout = dialog.findViewById(R.id.lay_name)
                    val edtName: TextInputEditText = dialog.findViewById(R.id.edt_name)
                    val btnCreate: MaterialButton = dialog.findViewById(R.id.btn_create)
                    txtTips.visibility = View.GONE
                    layName.hint = "Shadanga adhyayanam"
                    btnCreate.text = getString(R.string.add)
                    btnCreate.setOnClickListener {
                        try {
                            layName.error = null
                            if (edtName.length() < 2) {
                                layName.error = "Shadanga adhyayanam's minimum character is 2."
                            } else {
                                edt_shadanga_adhyayanam.text = edtName.text
                                edtName.let { v ->
                                    val imm =
                                        this@AddVunity.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
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
                    isShadangaAdhyayanam = item == "Yes"
                    edt_shadanga_adhyayanam.setText(item)
                }
            }
            spinnerDialog.showSpinerDialog()
        }

        btn_add_shastra_adhyayanam.setOnClickListener {
            val data: ArrayList<String> = arrayListOf()
            data.addAll(resources.getStringArray(R.array.shastra_adhyayana))
            val spinnerDialog = SpinnerDialog(
                this@AddVunity,
                data,
                "Select Shastra adhyayanam.",
                R.style.DialogAnimations_SmileWindow, // For slide animation
                "Cancel"
            )
            spinnerDialog.setCancellable(true) // for cancellable
            spinnerDialog.setShowKeyboard(false)// for open keyboard by default
            spinnerDialog.bindOnSpinerListener { item, position ->

                if (item == getString(R.string.other)) {
                    val dialog = Dialog(this@AddVunity, R.style.DialogTheme)
                    dialog.setContentView(R.layout.dialog_add_genre)
                    val txtTips: MaterialTextView = dialog.findViewById(R.id.txt_tips)
                    val layName: TextInputLayout = dialog.findViewById(R.id.lay_name)
                    val edtName: TextInputEditText = dialog.findViewById(R.id.edt_name)
                    val btnCreate: MaterialButton = dialog.findViewById(R.id.btn_create)
                    txtTips.visibility = View.GONE
                    layName.hint = "Shastra adhyayana"
                    btnCreate.text = getString(R.string.add)
                    btnCreate.setOnClickListener {
                        try {
                            layName.error = null
                            if (edtName.length() < 2) {
                                layName.error = "Shastra adhyayana's minimum character is 2."
                            } else {
                                shastraAdhyayanamList.add(StringUtils.capitalize(edtName.text.toString()))
                                view_shastra_adhyayanam?.apply {
                                    view_shastra_adhyayanam?.layoutManager =
                                        LinearLayoutManager(
                                            this@AddVunity,
                                            LinearLayoutManager.HORIZONTAL,
                                            false
                                        )
                                    view_shastra_adhyayanam?.setHasFixedSize(true)
                                    val genreAdapter =
                                        StringAdapter(
                                            getString(R.string.create),
                                            shastraAdhyayanamList,
                                            this@AddVunity
                                        )
                                    view_shastra_adhyayanam?.adapter = genreAdapter
                                }
                                edtName.let { v ->
                                    val imm =
                                        this@AddVunity.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
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
                    shastraAdhyayanamList.add(StringUtils.capitalize(item))
                    view_shastra_adhyayanam?.apply {
                        view_shastra_adhyayanam?.layoutManager =
                            LinearLayoutManager(
                                this@AddVunity,
                                LinearLayoutManager.HORIZONTAL,
                                false
                            )
                        view_shastra_adhyayanam?.setHasFixedSize(true)
                        val genreAdapter =
                            StringAdapter(
                                getString(R.string.create),
                                shastraAdhyayanamList,
                                this@AddVunity
                            )
                        view_shastra_adhyayanam?.adapter = genreAdapter
                    }
                }
            }
            spinnerDialog.showSpinerDialog()
        }

        btn_add_prayogam.setOnClickListener {
            val data: ArrayList<String> = arrayListOf()
            data.addAll(resources.getStringArray(R.array.prayogam))
            val spinnerDialog = SpinnerDialog(
                this@AddVunity,
                data,
                "Select Prayogam.",
                R.style.DialogAnimations_SmileWindow, // For slide animation
                "Cancel"
            )
            spinnerDialog.setCancellable(true) // for cancellable
            spinnerDialog.setShowKeyboard(false)// for open keyboard by default
            spinnerDialog.bindOnSpinerListener { item, position ->

                if (item == getString(R.string.other)) {
                    val dialog = Dialog(this@AddVunity, R.style.DialogTheme)
                    dialog.setContentView(R.layout.dialog_add_genre)
                    val txtTips: MaterialTextView = dialog.findViewById(R.id.txt_tips)
                    val layName: TextInputLayout = dialog.findViewById(R.id.lay_name)
                    val edtName: TextInputEditText = dialog.findViewById(R.id.edt_name)
                    val btnCreate: MaterialButton = dialog.findViewById(R.id.btn_create)
                    txtTips.visibility = View.GONE
                    layName.hint = "Prayogam"
                    btnCreate.text = getString(R.string.add)
                    btnCreate.setOnClickListener {
                        try {
                            layName.error = null
                            if (edtName.length() < 2) {
                                layName.error = "Prayogam's minimum character is 2."
                            } else {
                                prayogamList.add(StringUtils.capitalize(edtName.text.toString()))
                                view_prayogam?.apply {
                                    view_prayogam?.layoutManager =
                                        LinearLayoutManager(
                                            this@AddVunity,
                                            LinearLayoutManager.HORIZONTAL,
                                            false
                                        )
                                    view_prayogam?.setHasFixedSize(true)
                                    val genreAdapter =
                                        StringAdapter(
                                            getString(R.string.create),
                                            prayogamList,
                                            this@AddVunity
                                        )
                                    view_prayogam?.adapter = genreAdapter
                                }
                                edtName.let { v ->
                                    val imm =
                                        this@AddVunity.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
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
                    prayogamList.add(StringUtils.capitalize(item))
                    view_prayogam?.apply {
                        view_prayogam?.layoutManager =
                            LinearLayoutManager(
                                this@AddVunity,
                                LinearLayoutManager.HORIZONTAL,
                                false
                            )
                        view_prayogam?.setHasFixedSize(true)
                        val genreAdapter =
                            StringAdapter(getString(R.string.create), prayogamList, this@AddVunity)
                        view_prayogam?.adapter = genreAdapter
                    }
                }
            }
            spinnerDialog.showSpinerDialog()
        }

        edt_marital_status.setOnClickListener {
            val data: ArrayList<String> = arrayListOf()
            data.addAll(resources.getStringArray(R.array.marital_status))
            val spinnerDialog = SpinnerDialog(
                this@AddVunity,
                data,
                "Select Marital status.",
                R.style.DialogAnimations_SmileWindow, // For slide animation
                "Cancel"
            )
            spinnerDialog.setCancellable(true) // for cancellable
            spinnerDialog.setShowKeyboard(false)// for open keyboard by default
            spinnerDialog.bindOnSpinerListener { item, position ->

                if (item == getString(R.string.other)) {
                    val dialog = Dialog(this@AddVunity, R.style.DialogTheme)
                    dialog.setContentView(R.layout.dialog_add_genre)
                    val txtTips: MaterialTextView = dialog.findViewById(R.id.txt_tips)
                    val layName: TextInputLayout = dialog.findViewById(R.id.lay_name)
                    val edtName: TextInputEditText = dialog.findViewById(R.id.edt_name)
                    val btnCreate: MaterialButton = dialog.findViewById(R.id.btn_create)
                    txtTips.visibility = View.GONE
                    layName.hint = "Marital status"
                    btnCreate.text = getString(R.string.add)
                    btnCreate.setOnClickListener {
                        try {
                            layName.error = null
                            if (edtName.length() < 2) {
                                layName.error = "Marital status's minimum character is 2."
                            } else {
                                edt_marital_status.text = edtName.text
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
                    edt_marital_status.setText(item)
                }
            }
            spinnerDialog.showSpinerDialog()
        }


        edt_mothertongue.setOnClickListener {
            val data: ArrayList<String> = arrayListOf()
            data.addAll(resources.getStringArray(R.array.mother_tongue))
            val spinnerDialog = SpinnerDialog(
                this@AddVunity,
                data,
                "Select Mother tongue.",
                R.style.DialogAnimations_SmileWindow, // For slide animation
                "Cancel"
            )
            spinnerDialog.setCancellable(true) // for cancellable
            spinnerDialog.setShowKeyboard(false)// for open keyboard by default
            spinnerDialog.bindOnSpinerListener { item, position ->

                if (item == getString(R.string.other)) {
                    val dialog = Dialog(this@AddVunity, R.style.DialogTheme)
                    dialog.setContentView(R.layout.dialog_add_genre)
                    val txtTips: MaterialTextView = dialog.findViewById(R.id.txt_tips)
                    val layName: TextInputLayout = dialog.findViewById(R.id.lay_name)
                    val edtName: TextInputEditText = dialog.findViewById(R.id.edt_name)
                    val btnCreate: MaterialButton = dialog.findViewById(R.id.btn_create)
                    txtTips.visibility = View.GONE
                    layName.hint = "Marital tongue"
                    btnCreate.text = getString(R.string.add)
                    btnCreate.setOnClickListener {
                        try {
                            layName.error = null
                            if (edtName.length() < 2) {
                                layName.error = "Marital tongue's minimum character is 2."
                            } else {
                                edt_mothertongue.text = edtName.text
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
                    edt_mothertongue.setText(item)
                }
            }
            spinnerDialog.showSpinerDialog()
        }

        btn_update.setOnClickListener {
            update()
        }
    }

    private fun update() {
        lay_fullname.error = null
        lay_mobile.error = null

        when {
            edt_fullname.length() < 3 -> {
                lay_fullname.error = "Name's minimum character is 3."
            }
            edt_mobile.length() != 10 -> {
                lay_mobile.error = "Enter the valid mobile number."
            }
            else -> {
                val data = VunityBody(
                    user_id = getData("user_id", applicationContext),
                    name = StringUtils.capitalize(
                        edt_fullname.text.toString().toLowerCase(Locale.getDefault())
                    ),
                    mobile = edt_mobile.text.toString().toLowerCase(Locale.getDefault()),
                    city = StringUtils.capitalize(
                        edt_city.text.toString().toLowerCase(Locale.getDefault())
                    ),
                    vedham = StringUtils.capitalize(
                        edt_vedham.text.toString().toLowerCase(Locale.getDefault())
                    ),
                    samprdhayam = StringUtils.capitalize(
                        edt_sampradhayam.text.toString().toLowerCase(Locale.getDefault())
                    ),
                    shakha = shakhaList,
                    vedha_adhyayanam = vedhaAdhyayanamList,
                    shadanga_adhyayanam = isShadangaAdhyayanam,
                    shastra_adhyayanam = shastraAdhyayanamList,
                    prayogam = prayogamList,
                    marital_status = StringUtils.capitalize(
                        edt_marital_status.text.toString()
                            .toLowerCase(Locale.getDefault())
                    ),
                    mother_tongue = StringUtils.capitalize(
                        edt_mothertongue.text.toString()
                            .toLowerCase(Locale.getDefault())
                    )
                )
                create(data)
            }
        }
    }

    private fun create(data: VunityBody) {
        if (internet!!.checkMobileInternetConn(this@AddVunity)) {
            try {
                Log.e("body", data.toString())
                unityFrom = if (updateId == null) {
                    RetrofitClient.instanceClient.createVunity(body = data)
                } else {
                    RetrofitClient.instanceClient.updateVunity(
                        id = updateId.toString(),
                        body = data
                    )
                }
                unityFrom!!.enqueue(
                    RetrofitWithBar(this@AddVunity, object : Callback<ResDto> {
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
                                    this@AddVunity
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
                    val uploadProfile = RetrofitClient.instanceClient.updateVunityPhoto(part)
                    uploadProfile.enqueue(
                        RetrofitWithBar(this@AddVunity, object : Callback<ResDto> {
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
                                                        this@AddVunity
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
                                        this@AddVunity
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
}


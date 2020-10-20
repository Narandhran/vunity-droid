package com.vunity.book

import `in`.galaxyofandroid.spinerdialog.SpinnerDialog
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.textview.MaterialTextView
import com.hbisoft.pickit.PickiT
import com.hbisoft.pickit.PickiTCallbacks
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.vunity.R
import com.vunity.category.CategoryData
import com.vunity.category.CategoryListDto
import com.vunity.category.GenreDto
import com.vunity.general.*
import com.vunity.general.Constants.PERMISSIONS
import com.vunity.server.InternetDetector
import com.vunity.server.RetrofitClient
import com.vunity.server.RetrofitWithBar
import com.vunity.user.ErrorMsgDto
import com.vunity.user.ResDto
import com.yalantis.ucrop.UCrop
import kotlinx.android.synthetic.main.act_add_book.*
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
import kotlin.collections.ArrayList


class AddBook : AppCompatActivity(), PickiTCallbacks {

    private var internet: InternetDetector? = null
    private var imagePath: String? = null
    private var pdfPath: String? = null
    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    var category: Call<CategoryListDto>? = null
    var genres: Call<GenreDto>? = null
    var genresList: MutableList<Any> = arrayListOf()
    var keywordList: MutableList<Any> = arrayListOf()
    private lateinit var categoryId: String
    var pickiT: PickiT? = null
    var bookId: String = ""
    var announcement = false

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.act_add_book)

        txt_title.text = getString(R.string.book)
        txt_edit.visibility = View.GONE
        internet = InternetDetector.getInstance(this@AddBook)
        pickiT = PickiT(this, this, this)

        val categories = categories()
        val genres = genres()

        im_back.setOnClickListener {
            onBackPressed()
        }

        try {
            val data = intent.getStringExtra("data")
            if (data != null) {
                val jsonAdapter: JsonAdapter<BookData> =
                    moshi.adapter(BookData::class.java)
                val bookData: BookData? = jsonAdapter.fromJson(data.toString())
                println(bookData)
                if (bookData != null) {
                    btn_create.text = getString(R.string.update)
                    bookId = bookData._id.toString()

                    img_image_delete.visibility = View.VISIBLE
                    img_image_add.setImageDrawable(
                        ContextCompat.getDrawable(applicationContext, R.drawable.ic_image_checked)
                    )
                    txt_image_filename.text =
                        getString(R.string.star) + StringUtils.right(bookData.thumbnail, 7)

                    img_pdf_delete.visibility = View.VISIBLE
                    img_pdf_add.setImageDrawable(
                        ContextCompat.getDrawable(applicationContext, R.drawable.ic_pdf_checked)
                    )
                    txt_pdf_filename.text =
                        getString(R.string.star) + StringUtils.right(bookData.content, 7)

                    edt_category.setText(bookData.categoryId?.name)
                    categoryId = bookData.categoryId?._id.toString()
                    edt_name.setText(bookData.name)
                    genresList = bookData.genre?.toMutableList()!!
                    if (genresList.isNotEmpty()) {
                        view_genre?.apply {
                            view_genre?.layoutManager =
                                LinearLayoutManager(
                                    applicationContext,
                                    LinearLayoutManager.HORIZONTAL,
                                    false
                                )
                            view_genre?.setHasFixedSize(true)
                            val genreAdapter =
                                StringAdapter("create", genresList, this@AddBook)
                            view_genre?.adapter = genreAdapter
                        }
                    }
                    edt_author.setText(bookData.author)
                    keywordList = bookData.keywords?.toMutableList()!!
                    if (keywordList.isNotEmpty()) {
                        view_keyword?.apply {
                            view_keyword?.layoutManager =
                                LinearLayoutManager(
                                    applicationContext,
                                    LinearLayoutManager.HORIZONTAL,
                                    false
                                )
                            view_keyword?.setHasFixedSize(true)
                            val genreAdapter =
                                StringAdapter("create", keywordList, this@AddBook)
                            view_keyword?.adapter = genreAdapter
                        }
                    }
                    edt_publish.setText(bookData.yearOfPublish)
                    edt_description.setText(bookData.description)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            showMessage(
                lay_root,
                getString(R.string.unable_to_fetch)
            )
        }

        swt_announcement.setOnCheckedChangeListener { buttonView, isChecked ->
            announcement = isChecked
        }

        img_image_add.setOnClickListener {
            if (!hasPermissions(
                    applicationContext,
                    *PERMISSIONS
                )
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    PERMISSIONS,
                    Constants.PERMISSION_ALL
                )
            } else {
                val permissionResult = checkPermission(this@AddBook)
                if (permissionResult) {
                    openPhotos()
                } else {
                    showErrorMessage(
                        lay_root,
                        "Permission denied, Please grant permission to access photos!"
                    )
                }
            }
        }

        img_pdf_add.setOnClickListener {
            if (!hasPermissions(
                    applicationContext,
                    *PERMISSIONS
                )
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    PERMISSIONS,
                    Constants.PERMISSION_ALL
                )
            } else {
                val permissionResult =
                    checkPermission(this@AddBook)
                if (permissionResult) {
                    openPdf()
                } else {
                    showErrorMessage(
                        lay_root,
                        "Permission denied, Please grant permission to access files!"
                    )
                }
            }
        }

        edt_category.setOnClickListener {
            val categoryName = ArrayList(categories.values)
            if (categoryName.isNotEmpty()) {
                val spinnerDialog = SpinnerDialog(
                    this@AddBook,
                    categoryName,
                    "Select category.",
                    R.style.DialogAnimations_SmileWindow, // For slide animation
                    "Cancel"
                )
                spinnerDialog.setCancellable(true) // for cancellable
                spinnerDialog.setShowKeyboard(false)// for open keyboard by default
                spinnerDialog.bindOnSpinerListener { item, position ->
                    edt_category.text = Editable.Factory.getInstance().newEditable(item)
                    categoryId = ArrayList(categories.keys)[position]
                }

                spinnerDialog.showSpinerDialog()
            } else {
                showErrorMessage(
                    lay_root,
                    "Sorry!, There is no categories found or try again later!"
                )
            }
        }

        btn_add_genre.setOnClickListener {
            if (genres.isNotEmpty()) {
                val spinnerDialog = SpinnerDialog(
                    this@AddBook,
                    genres,
                    "Select genres.",
                    R.style.DialogAnimations_SmileWindow, // For slide animation
                    "Cancel"
                )
                spinnerDialog.setCancellable(true) // for cancellable
                spinnerDialog.setShowKeyboard(false)// for open keyboard by default
                spinnerDialog.bindOnSpinerListener { item, position ->
                    genresList.add(item)
                    view_genre?.apply {
                        view_genre?.layoutManager =
                            LinearLayoutManager(
                                applicationContext,
                                LinearLayoutManager.HORIZONTAL,
                                false
                            )
                        view_genre?.setHasFixedSize(true)
                        val genreAdapter =
                            StringAdapter("create", genresList, this@AddBook)
                        view_genre?.adapter = genreAdapter
                    }
                }

                spinnerDialog.showSpinerDialog()
            } else {
                showErrorMessage(
                    lay_root,
                    "Sorry!, There is no genres found or try again later!"
                )
            }
        }

        btn_add_keyword.setOnClickListener {
            addKeywords()
        }

        btn_create.setOnClickListener {
            if (internet?.checkMobileInternetConn(applicationContext)!!) {
                if (bookId.isEmpty()) {
                    createBook()
                } else {
                    updateBook()
                }
            } else {
                showErrorMessage(
                    lay_root,
                    getString(R.string.msg_no_internet)
                )
            }
        }

        img_image_delete.setOnClickListener {
            imagePath = null
            img_image_delete.visibility = View.GONE
            img_image_add.setImageDrawable(
                ContextCompat.getDrawable(applicationContext, R.drawable.ic_image_add)
            )
            txt_image_filename.text = getString(R.string.sample_jpg)
        }

        img_pdf_delete.setOnClickListener {
            pdfPath = null
            img_pdf_delete.visibility = View.GONE
            img_pdf_add.setImageDrawable(
                ContextCompat.getDrawable(applicationContext, R.drawable.ic_pdf_add)
            )
            txt_pdf_filename.text = getString(R.string.sample_jpg)
        }
    }

    private fun createBook() {
        lay_category.error = null
        lay_name.error = null
        lay_author.error = null
        lay_publish.error = null
        lay_description.error = null
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)

        when {
            imagePath == null -> {
                showErrorMessage(
                    lay_root,
                    "BookDetails images required!, Please tap on empty place holder to select image!"
                )
            }
            pdfPath == null -> {
                showErrorMessage(
                    lay_root,
                    "BookDetails pdf required!, Please tap on empty place holder to select pdf!"
                )
            }

            edt_category.length() < 1 -> {
                lay_category.error = "Categories are required!, Please select category."
            }

            edt_name.length() < 3 -> {
                lay_name.error = "AddName's minimum character is 3."
            }

            genresList.size == 0 -> {
                showErrorMessage(
                    lay_root,
                    "Genres are required!, Please add a genres!"
                )
            }

            edt_author.length() < 3 -> {
                lay_author.error = "Author's minimum character is 3."
            }

            edt_publish.length() != 4 -> {
                lay_publish.error = "Published year is required."
            }

            edt_publish.text.toString().toInt() > currentYear -> {
                lay_publish.error = "Published year should be less than current year."
            }

            keywordList.size == 0 -> {
                showErrorMessage(
                    lay_root,
                    "Keywords are required!, Please add a keywords!"
                )
            }
            edt_description.length() < 3 -> {
                lay_description.error = "Description's minimum character is 3."
            }
            else -> {
                try {
                    val bookBody = ReqBookBody(
                        categoryId = categoryId,
                        name = edt_name.text.toString(),
                        genre = genresList,
                        author = edt_author.text.toString(),
                        yearOfPublish = edt_publish.text.toString(),
                        keywords = keywordList,
                        description = edt_description.text.toString(),
                        makeAnnouncement = announcement
                    )
                    val jsonAdapter: JsonAdapter<ReqBookBody> =
                        moshi.adapter(ReqBookBody::class.java)
                    val json: String = jsonAdapter.toJson(bookBody)

                    val imageFile = File(imagePath.toString())
                    val imageReqBody = RequestBody.create(MediaType.parse("image/*"), imageFile)
                    val imagePart: MultipartBody.Part =
                        MultipartBody.Part.createFormData("pdf-thumb", imageFile.name, imageReqBody)
                    val pdfFile = File(pdfPath.toString())
                    val pdfReqBody = RequestBody.create(MediaType.parse("application/pdf"), pdfFile)
                    val pdfPart: MultipartBody.Part =
                        MultipartBody.Part.createFormData("content", pdfFile.name, pdfReqBody)
                    val text = RequestBody.create(MediaType.parse("text/plain"), json)

                    if (internet?.checkMobileInternetConn(applicationContext)!!) {
                        val create =
                            RetrofitClient.bookClient.addBook(
                                thumbnail = imagePart,
                                pdf = pdfPart,
                                text = text
                            )
                        create.enqueue(
                            RetrofitWithBar(this@AddBook, object : Callback<ResDto> {
                                @SuppressLint("SimpleDateFormat")
                                @RequiresApi(Build.VERSION_CODES.O)
                                override fun onResponse(
                                    call: Call<ResDto>,
                                    response: Response<ResDto>
                                ) {
                                    if (response.code() == 200) {
                                        when (response.body()?.status) {
                                            200 -> {
                                                showMessage(
                                                    lay_root,
                                                    response.body()!!.message
                                                )
                                                Handler().postDelayed({
                                                    finish()
                                                    reloadActivity(this@AddBook)
                                                }, 400)
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
                                            }
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                            showErrorMessage(
                                                lay_root,
                                                getString(R.string.msg_something_wrong)
                                            )
                                        }

                                    } else if (response.code() == 401) {
                                        sessionExpired(this@AddBook)
                                    } else {
                                        showErrorMessage(
                                            lay_root,
                                            response.message()
                                        )
                                    }
                                }

                                override fun onFailure(call: Call<ResDto>, t: Throwable) {
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
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun updateBook() {
        lay_category.error = null
        lay_name.error = null
        lay_author.error = null
        lay_publish.error = null
        lay_description.error = null
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)

        when {
            edt_category.length() < 1 -> {
                lay_category.error = "Categories are required!, Please select category."
            }

            edt_name.length() < 3 -> {
                lay_name.error = "AddName's minimum character is 3."
            }

            genresList.size == 0 -> {
                showErrorMessage(
                    lay_root,
                    "Genres are required!, Please add a genres!"
                )
            }

            edt_author.length() < 3 -> {
                lay_author.error = "Author's minimum character is 3."
            }

            edt_publish.length() != 4 -> {
                lay_publish.error = "Published year is required."
            }

            edt_publish.text.toString().toInt() > currentYear -> {
                lay_publish.error = "Published year should be less than current year."
            }

            keywordList.size == 0 -> {
                showErrorMessage(
                    lay_root,
                    "Keywords are required!, Please add a keywords!"
                )
            }
            edt_description.length() < 3 -> {
                lay_description.error = "Description's minimum character is 3."
            }
            else -> {
                try {
                    val bookBody = ReqBookBody(
                        categoryId = categoryId,
                        name = edt_name.text.toString(),
                        genre = genresList,
                        author = edt_author.text.toString(),
                        yearOfPublish = edt_publish.text.toString(),
                        keywords = keywordList,
                        description = edt_description.text.toString(),
                        makeAnnouncement = announcement
                    )
                    if (internet?.checkMobileInternetConn(applicationContext)!!) {
                        val update =
                            RetrofitClient.bookClient.updateBook(
                                id = bookId,
                                reqBookBody = bookBody
                            )
                        update.enqueue(
                            RetrofitWithBar(this@AddBook, object : Callback<ResDto> {
                                @SuppressLint("SimpleDateFormat")
                                @RequiresApi(Build.VERSION_CODES.O)
                                override fun onResponse(
                                    call: Call<ResDto>,
                                    response: Response<ResDto>
                                ) {
                                    if (response.code() == 200) {
                                        when (response.body()?.status) {
                                            200 -> {
                                                showMessage(
                                                    lay_root,
                                                    response.body()!!.message
                                                )
                                                Handler().postDelayed({
                                                    backToPrevious(bookId)
                                                }, 400)
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
                                            }
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                            showErrorMessage(
                                                lay_root,
                                                getString(R.string.msg_something_wrong)
                                            )
                                        }

                                    } else if (response.code() == 401) {
                                        sessionExpired(this@AddBook)
                                    } else {
                                        showErrorMessage(
                                            lay_root,
                                            response.message()
                                        )
                                    }
                                }

                                override fun onFailure(call: Call<ResDto>, t: Throwable) {
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
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun updatePdf(bookId: String, pdfPath: String) {
        val pdfFile = File(pdfPath)
        val pdfReqBody = RequestBody.create(MediaType.parse("application/pdf"), pdfFile)
        val pdfPart: MultipartBody.Part =
            MultipartBody.Part.createFormData("content", pdfFile.name, pdfReqBody)
        if (internet?.checkMobileInternetConn(applicationContext)!!) {
            val updatePdf = RetrofitClient.bookClient.updateBookPdf(pdf = pdfPart, id = bookId)
            updatePdf.enqueue(
                RetrofitWithBar(this@AddBook, object : Callback<ResDto> {
                    @SuppressLint("SimpleDateFormat")
                    @RequiresApi(Build.VERSION_CODES.O)
                    override fun onResponse(
                        call: Call<ResDto>,
                        response: Response<ResDto>
                    ) {
                        if (response.code() == 200) {
                            when (response.body()?.status) {
                                200 -> {
                                    showMessage(
                                        lay_root,
                                        response.body()!!.message
                                    )
                                    Handler().postDelayed({
                                        finish()
                                        reloadActivity(this@AddBook)
                                    }, 400)
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
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                                showErrorMessage(
                                    lay_root,
                                    getString(R.string.msg_something_wrong)
                                )
                            }

                        } else if (response.code() == 401) {
                            sessionExpired(this@AddBook)
                        } else {
                            showErrorMessage(
                                lay_root,
                                response.message()
                            )
                        }
                    }

                    override fun onFailure(call: Call<ResDto>, t: Throwable) {
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

    private fun updateImage(bookId: String, imagePath: String) {
        val imageFile = File(imagePath)
        val imageReqBody = RequestBody.create(MediaType.parse("image/*"), imageFile)
        val imagePart: MultipartBody.Part =
            MultipartBody.Part.createFormData("pdf-thumb", imageFile.name, imageReqBody)
        if (internet?.checkMobileInternetConn(applicationContext)!!) {
            val updatePdf =
                RetrofitClient.bookClient.updateBookImage(thumbnail = imagePart, id = bookId)
            updatePdf.enqueue(
                RetrofitWithBar(this@AddBook, object : Callback<ResDto> {
                    @SuppressLint("SimpleDateFormat")
                    @RequiresApi(Build.VERSION_CODES.O)
                    override fun onResponse(
                        call: Call<ResDto>,
                        response: Response<ResDto>
                    ) {
                        if (response.code() == 200) {
                            when (response.body()?.status) {
                                200 -> {
                                    showMessage(
                                        lay_root,
                                        response.body()!!.message
                                    )
                                    Handler().postDelayed({
                                        finish()
                                        reloadActivity(this@AddBook)
                                    }, 400)
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
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                                showErrorMessage(
                                    lay_root,
                                    getString(R.string.msg_something_wrong)
                                )
                            }

                        } else if (response.code() == 401) {
                            sessionExpired(this@AddBook)
                        } else {
                            showErrorMessage(
                                lay_root,
                                response.message()
                            )
                        }
                    }

                    override fun onFailure(call: Call<ResDto>, t: Throwable) {
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

    private fun addKeywords() {
        val dialog = Dialog(this@AddBook, R.style.DialogTheme)
        dialog.setContentView(R.layout.dialog_add_genre)
        val txtTips: MaterialTextView = dialog.findViewById(R.id.txt_tips)
        val layName: TextInputLayout = dialog.findViewById(R.id.lay_name)
        val edtName: TextInputEditText = dialog.findViewById(R.id.edt_name)
        val btnCreate: MaterialButton = dialog.findViewById(R.id.btn_create)

        txtTips.text = getString(R.string.msg_keyword)
        btnCreate.text = getString(R.string.add)
        btnCreate.setOnClickListener {
            try {
                layName.error = null
                if (edtName.length() < 2) {
                    layName.error = "Keywords minimum character is 2."
                } else {
                    keywordList.add(edtName.text.toString())
                    view_keyword?.apply {
                        view_keyword?.layoutManager =
                            LinearLayoutManager(
                                applicationContext,
                                LinearLayoutManager.HORIZONTAL,
                                false
                            )
                        view_keyword?.setHasFixedSize(true)
                        val genreAdapter =
                            StringAdapter("create", keywordList, this@AddBook)
                        view_keyword?.adapter = genreAdapter
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
                e.printStackTrace()
            }
        }
        dialog.show()
    }

    private fun categories(): HashMap<String, String> {
        val hashCategory = HashMap<String, String>()
        if (internet?.checkMobileInternetConn(this@AddBook)!!) {
            category = RetrofitClient.categoryClient.category()
            category?.enqueue(object : Callback<CategoryListDto> {
                @SuppressLint("DefaultLocale", "SetTextI18n")
                override fun onResponse(
                    call: Call<CategoryListDto>,
                    response: Response<CategoryListDto>
                ) {
                    when {
                        response.code() == 200 -> {
                            when (response.body()?.status) {
                                200 -> {
                                    val categoryData: List<CategoryData> = response.body()!!.data
                                    for (list in categoryData) {
                                        hashCategory[list._id] = list.name
                                    }
                                }
                                204 -> {
                                    showErrorMessage(
                                        lay_root,
                                        response.message()
                                    )
                                }
                                else -> {
                                    showErrorMessage(
                                        lay_root,
                                        response.message()
                                    )
                                }
                            }
                        }

                        response.code() == 422 || response.code() == 400 -> {
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
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }

                        }
                        response.code() == 401 -> {
                            sessionExpired(this@AddBook)
                        }
                        else -> {
                            showErrorMessage(
                                lay_root,
                                response.message()
                            )
                        }
                    }
                }

                override fun onFailure(call: Call<CategoryListDto>, t: Throwable) {
                    if (!call.isCanceled) {
                        showErrorMessage(
                            lay_root,
                            getString(R.string.msg_something_wrong)
                        )
                    }
                }
            })

        } else {
            showErrorMessage(
                lay_root,
                getString(R.string.msg_no_internet)
            )
        }
        return hashCategory
    }

    private fun genres(): ArrayList<String> {
        val genreNames: ArrayList<String> = arrayListOf()
        if (internet?.checkMobileInternetConn(this@AddBook)!!) {
            genres = RetrofitClient.userClient.genres()
            genres?.enqueue(object : Callback<GenreDto> {
                @SuppressLint("DefaultLocale", "SetTextI18n")
                override fun onResponse(
                    call: Call<GenreDto>,
                    response: Response<GenreDto>
                ) {
                    when {
                        response.code() == 200 -> {
                            when (response.body()?.status) {
                                200 -> {
                                    val genreData = response.body()!!.data
                                    for (item in genreData) {
                                        genreNames.add(item.genre)
                                    }
                                }
                                204 -> {
                                    showErrorMessage(
                                        lay_root,
                                        response.message()
                                    )
                                }
                                else -> {
                                    showErrorMessage(
                                        lay_root,
                                        response.message()
                                    )
                                }
                            }

                        }

                        response.code() == 422 || response.code() == 400 -> {
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
                                }
                            } catch (e: Exception) {
                                showErrorMessage(
                                    lay_root,
                                    getString(R.string.msg_something_wrong)
                                )
                            }

                        }
                        response.code() == 401 -> {
                            sessionExpired(this@AddBook)
                        }
                        else -> {
                            showErrorMessage(
                                lay_root,
                                response.message()
                            )
                        }
                    }
                }

                override fun onFailure(call: Call<GenreDto>, t: Throwable) {
                    if (!call.isCanceled) {
                        showErrorMessage(
                            lay_root,
                            getString(R.string.msg_something_wrong)
                        )
                    }
                }
            })

        } else {
            showErrorMessage(
                lay_root,
                getString(R.string.msg_no_internet)
            )
        }
        return genreNames
    }

    override fun onStop() {
        super.onStop()
        if (category != null) {
            category?.cancel()
        }
        if (genres != null) {
            genres?.cancel()
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Constants.PICK_PDF_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            val sourceUri = data.data // 1
            if (sourceUri != null) {
                pickiT?.getPath(data.data, Build.VERSION.SDK_INT)
                val name = StringUtils.right(getFileName(sourceUri, applicationContext), 7)
                img_pdf_delete.visibility = View.VISIBLE
                img_pdf_add.setImageDrawable(
                    ContextCompat.getDrawable(applicationContext, R.drawable.ic_pdf_checked)
                )
                txt_pdf_filename.text = getString(R.string.star) + name
            }  // 4
        } else if (requestCode == Constants.PICK_IMAGE_GALLERY_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            val sourceUri = data.data // 1
            val file = getTempFile(applicationContext) // 2
            val destinationUri = Uri.fromFile(file)  // 3
            if (sourceUri != null) {
                openCropActivity(sourceUri, destinationUri)
            }  // 4
        } else if (resultCode == Activity.RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            val resultUri = UCrop.getOutput(data!!)
            if (bookId.isNotEmpty()) {
                updateImage(bookId = bookId, imagePath = resultUri?.path.toString())
                val name = StringUtils.right(resultUri?.pathSegments?.last(), 7)
                img_image_delete.visibility = View.VISIBLE
                img_image_add.setImageDrawable(
                    ContextCompat.getDrawable(applicationContext, R.drawable.ic_image_checked)
                )
                txt_image_filename.text = getString(R.string.star) + name
            } else {
                imagePath = resultUri?.path
                val name = StringUtils.right(resultUri?.pathSegments?.last(), 7)
                img_image_delete.visibility = View.VISIBLE
                img_image_add.setImageDrawable(
                    ContextCompat.getDrawable(applicationContext, R.drawable.ic_image_checked)
                )
                txt_image_filename.text = getString(R.string.star) + name
            }
        } else if (resultCode == UCrop.RESULT_ERROR) {
            val cropError = UCrop.getError(data!!)
        } else if (resultCode == Activity.RESULT_CANCELED) {
            showMessage(lay_root, "Fetching files cancelled.")
            // User Cancelled the action
        }
    }

    private fun openPhotos() {
        val pictureIntent = Intent(Intent.ACTION_GET_CONTENT)
        pictureIntent.type = "image/*"  // 1
        pictureIntent.addCategory(Intent.CATEGORY_OPENABLE)  // 2
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            val mimeTypes = arrayOf("image/jpeg", "image/png")  // 3
            pictureIntent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
        }
        startActivityForResult(
            Intent.createChooser(pictureIntent, "Select Picture"),
            Constants.PICK_IMAGE_GALLERY_REQUEST_CODE
        ) // 4
    }

    private fun openPdf() {
        val pictureIntent = Intent(Intent.ACTION_GET_CONTENT)
        pictureIntent.type = "application/pdf"  // 1
        pictureIntent.addCategory(Intent.CATEGORY_OPENABLE)  // 2
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            val mimeTypes = arrayOf("application/pdf")  // 3
            pictureIntent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
        }
        startActivityForResult(
            Intent.createChooser(pictureIntent, "Select Pdf"),
            Constants.PICK_PDF_REQUEST_CODE
        ) // 4
    }

    private fun openCropActivity(sourceUri: Uri, destinationUri: Uri) {
        val options = UCrop.Options()
        options.setToolbarColor(ContextCompat.getColor(applicationContext, R.color.colorPrimary))
        options.setStatusBarColor(ContextCompat.getColor(applicationContext, R.color.colorPrimary))
        options.setActiveWidgetColor(ContextCompat.getColor(applicationContext, R.color.colorPrimary))
        this@AddBook.let {
            UCrop.of(sourceUri, destinationUri)
                .withOptions(options)
                .withAspectRatio(5f, 5f)
                .start(this@AddBook)
        }
    }


    override fun PickiTonUriReturned() {
    }

    override fun PickiTonProgressUpdate(progress: Int) {
    }

    override fun PickiTonStartListener() {
    }

    override fun PickiTonCompleteListener(
        path: String?,
        wasDriveFile: Boolean,
        wasUnknownProvider: Boolean,
        wasSuccessful: Boolean,
        Reason: String?
    ) {
        if (bookId.isNotEmpty()) {
            updatePdf(bookId = bookId, pdfPath = path.toString())
        } else {
            pdfPath = path
        }
    }

    override fun onBackPressed() {
        pickiT?.deleteTemporaryFile()
        if (bookId.isNotEmpty()) {
            backToPrevious(bookId)
        } else {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (!isChangingConfigurations) {
            pickiT?.deleteTemporaryFile()
        }
    }

    fun backToPrevious(id: String) {
        Handler().postDelayed({
            val intent = Intent()
            intent.putExtra(getString(R.string.data), id)
            setResult(RESULT_OK, intent)
            finish()
        }, 200)
    }
}

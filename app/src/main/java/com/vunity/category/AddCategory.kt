package com.vunity.category

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.squareup.picasso.Picasso
import com.vunity.R
import com.vunity.general.*
import com.vunity.server.InternetDetector
import com.vunity.server.RetrofitClient
import com.vunity.server.RetrofitWithBar
import com.vunity.user.ErrorMsgDto
import com.yalantis.ucrop.UCrop
import kotlinx.android.synthetic.main.act_add_category.*
import kotlinx.android.synthetic.main.toolbar.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class AddCategory : AppCompatActivity() {

    private var internet: InternetDetector? = null
    private var uri: Uri? = null
    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.act_add_category)

        txt_title.text = getString(R.string.category)
        txt_edit.visibility = View.GONE
        internet = InternetDetector.getInstance(this@AddCategory)

        im_back.setOnClickListener {
            onBackPressed()
        }

        img_category.setOnClickListener {
            val permissionResult =
                checkPermission(this@AddCategory)
            if (permissionResult) {
                openPhotos()
            } else {
                showErrorMessage(
                    lay_root,
                    "Permission denied, Please grant permission to access photos!"
                )
            }
        }

        btn_create.setOnClickListener {
            createCategory()
        }
    }

    private fun createCategory() {
        lay_name.error = null
        lay_description.error = null

        if (edt_name.length() < 3) {
            lay_name.error = "Category name minimum character is 3."
        } else if (edt_description.length() < 3) {
            lay_description.error = "Description minimum character is 3."
        } else if (uri == null) {
            showErrorMessage(lay_root, "Please select a image.")
        } else {
            val categoryData =
                CategoryBody(
                    name = edt_name.text.toString(),
                    description = edt_description.text.toString()
                )
            val jsonAdapter: JsonAdapter<CategoryBody> =
                moshi.adapter(CategoryBody::class.java)
            val json: String = jsonAdapter.toJson(categoryData)
            Log.e("CategoryBody", uri.toString() + " " + json)

            val file = File(uri!!.path!!)
            val fileReqBody = RequestBody.create(MediaType.parse("image/*"), file)
            val part: MultipartBody.Part =
                MultipartBody.Part.createFormData("category", file.name, fileReqBody)
            val text = RequestBody.create(MediaType.parse("text/plain"), json)

            if (internet?.checkMobileInternetConn(applicationContext)!!) {
                val category = RetrofitClient.instanceClient.addCategory(part, text)
                category.enqueue(
                    RetrofitWithBar(this@AddCategory, object : Callback<CategoryDto> {
                        @SuppressLint("SimpleDateFormat")
                        @RequiresApi(Build.VERSION_CODES.O)
                        override fun onResponse(
                            call: Call<CategoryDto>,
                            response: Response<CategoryDto>
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
                                            finish()
                                            overridePendingTransition(0, 0)
                                            startActivity(intent)
                                            overridePendingTransition(0, 0)
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
                                    this@AddCategory
                                )
                            } else {
                                showErrorMessage(
                                    lay_root,
                                    response.message()
                                )
                            }
                        }

                        override fun onFailure(call: Call<CategoryDto>, t: Throwable) {
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Constants.PICK_IMAGE_GALLERY_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            val sourceUri = data.data // 1
            val file = getImageFile() // 2
            val destinationUri = Uri.fromFile(file)  // 3
            if (sourceUri != null) {
                openCropActivity(sourceUri, destinationUri)
            }  // 4
            Log.e("FROM_GALLERY", destinationUri.toString())

        } else if (resultCode == Activity.RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            val resultUri = UCrop.getOutput(data!!)
            Log.e("FROM_UCROP", resultUri.toString())
            txt_tips.visibility = View.GONE
            uri = resultUri
            Picasso.get().load(resultUri)
                .error(R.drawable.img_place_holder)
                .placeholder(R.drawable.img_place_holder)
                .into(img_category)
        } else if (resultCode == UCrop.RESULT_ERROR) {

            val cropError = UCrop.getError(data!!)
            Log.e("FROM_UCROP", cropError.toString())
        } else if (resultCode == Activity.RESULT_CANCELED) {

            Toast.makeText(
                applicationContext,
                "Taking picture failed.",
                Toast.LENGTH_SHORT
            ).show()
            Log.e("FROM_CAMERA", data?.data.toString() + " RESULT_CANCELLED")
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

    var currentPhotoPath = ""
    private fun getImageFile(): File {
        val imageFileName = "JPEG_" + System.currentTimeMillis() + "_"
        val mydir = applicationContext?.getDir("get2basket", Context.MODE_PRIVATE)
        val profile = File(mydir, "profile")
        if (!profile.exists()) {
            profile.mkdirs()
        }
        val file = File.createTempFile(
            imageFileName, ".jpg", profile
        )
        currentPhotoPath = "file:" + file.absolutePath
        return file
    }

    private fun openCropActivity(sourceUri: Uri, destinationUri: Uri) {
        this@AddCategory.let {
            UCrop.of(sourceUri, destinationUri)
                .withAspectRatio(5f, 5f)
                .start(this@AddCategory)
        }
    }

}

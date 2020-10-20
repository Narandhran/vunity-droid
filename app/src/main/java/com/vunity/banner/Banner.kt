package com.vunity.banner

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.vunity.R
import com.vunity.general.*
import com.vunity.general.Constants.PERMISSIONS
import com.vunity.interfaces.OnBannerClickListener
import com.vunity.server.InternetDetector
import com.vunity.server.RetrofitClient
import com.vunity.server.RetrofitWithBar
import com.vunity.user.ErrorMsgDto
import com.yalantis.ucrop.UCrop
import kotlinx.android.synthetic.main.act_banner.*
import kotlinx.android.synthetic.main.toolbar.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File


class Banner : AppCompatActivity() {

    private var internet: InternetDetector? = null
    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
    private var banner: Call<BannerDto>? = null
    private var getBanner: Call<BannerListDto>? = null
    private var bannerId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.act_banner)
        txt_title.text = getString(R.string.banner)
        txt_edit.text = getString(R.string.add)
        im_back.setOnClickListener {
            onBackPressed()
        }
        layout_refresh.setOnRefreshListener {
            finish()
            reloadActivity(this@Banner)
            layout_refresh.isRefreshing = false
        }
        txt_edit.setOnClickListener {
            selectImage()
        }
        internet = InternetDetector.getInstance(this@Banner)
        getBanner()
    }

    private fun getBanner() {
        if (!lay_shimmer.isShimmerStarted) {
            lay_shimmer.startShimmer()
        }
        if (internet?.checkMobileInternetConn(applicationContext)!!) {
            getBanner = RetrofitClient.bannerClient.getBanners()
            getBanner?.enqueue(object : Callback<BannerListDto> {
                @SuppressLint("DefaultLocale", "SetTextI18n")
                override fun onResponse(
                    call: Call<BannerListDto>,
                    response: Response<BannerListDto>
                ) {
                    when {
                        response.code() == 200 -> {
                            when (response.body()?.status) {
                                200 -> {
                                    val bannerData = response.body()!!.data?.toMutableList()
                                    if (bannerData?.size!! < 5) {
                                        txt_edit.visibility = View.VISIBLE
                                    } else {
                                        txt_edit.visibility = View.GONE
                                    }
                                    lay_no_data.visibility = View.GONE
                                    lay_no_internet.visibility = View.GONE
                                    lay_data.visibility = View.VISIBLE
                                    view_banner?.apply {
                                        view_banner?.layoutManager = LinearLayoutManager(
                                            this@Banner,
                                            LinearLayoutManager.VERTICAL,
                                            false
                                        )
                                        view_banner?.setHasFixedSize(true)
                                        val bannerAdapter = BannerAdapter(
                                            bannerData, this@Banner,
                                            object : OnBannerClickListener {
                                                override fun onItemClick(item: BannerData?) {
                                                    bannerId = item?._id.toString()
                                                    selectImage()
                                                }
                                            })
                                        view_banner?.adapter = bannerAdapter
                                    }
                                }
                                204 -> {
                                    lay_no_data.visibility = View.VISIBLE
                                    lay_data.visibility = View.GONE
                                    lay_no_internet.visibility = View.GONE
                                }
                                else -> {
                                    showErrorMessage(
                                        layout_refresh,
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
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                                showErrorMessage(
                                    layout_refresh,
                                    getString(R.string.msg_something_wrong)
                                )
                            }

                        }

                        response.code() == 401 -> {
                            sessionExpired(this@Banner)
                        }
                        else -> {
                            showErrorMessage(
                                layout_refresh,
                                response.message()
                            )
                        }
                    }
                    lay_shimmer.visibility = View.GONE
                    lay_shimmer.stopShimmer()
                }

                override fun onFailure(call: Call<BannerListDto>, t: Throwable) {
                    if (!call.isCanceled) {
                        showErrorMessage(
                            layout_refresh,
                            getString(R.string.msg_something_wrong)
                        )
                        lay_shimmer.visibility = View.GONE
                        lay_shimmer.stopShimmer()
                    }
                }
            })

        } else {
            lay_shimmer.visibility = View.GONE
            lay_shimmer.stopShimmer()
            lay_no_data.visibility = View.GONE
            lay_data.visibility = View.GONE
            lay_no_internet.visibility = View.VISIBLE
        }
    }

    private fun selectImage() {
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
                checkPermission(this@Banner)
            if (permissionResult) {
                openPhotos()
            } else {
                showErrorMessage(
                    layout_refresh,
                    "Permission denied, Please grant permission to access files!"
                )
            }
        }
    }

    private fun banner(imagePath: String) {
        try {
            val imageFile = File(imagePath)
            val imageReqBody = RequestBody.create(MediaType.parse("image/*"), imageFile)
            val imagePart: MultipartBody.Part =
                MultipartBody.Part.createFormData("banner", imageFile.name, imageReqBody)
            if (internet?.checkMobileInternetConn(applicationContext)!!) {
                banner = if (bannerId == null) {
                    RetrofitClient.bannerClient.addBanner(image = imagePart)
                } else {
                    RetrofitClient.bannerClient.updateBanner(id = bannerId!!, image = imagePart)
                }
                banner!!.enqueue(
                    RetrofitWithBar(this@Banner, object : Callback<BannerDto> {
                        @SuppressLint("SimpleDateFormat")
                        @RequiresApi(Build.VERSION_CODES.O)
                        override fun onResponse(
                            call: Call<BannerDto>,
                            response: Response<BannerDto>
                        ) {
                            if (response.code() == 200) {
                                when (response.body()?.status) {
                                    200 -> {
                                        showMessage(
                                            layout_refresh,
                                            response.body()!!.message.toString()
                                        )
                                        Handler().postDelayed({
                                            finish()
                                            reloadActivity(this@Banner)
                                        }, 200)
                                    }
                                    else -> {
                                        showErrorMessage(
                                            layout_refresh,
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
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    showErrorMessage(
                                        layout_refresh,
                                        getString(R.string.msg_something_wrong)
                                    )
                                }

                            } else if (response.code() == 401) {
                                sessionExpired(this@Banner)
                            } else {
                                showErrorMessage(
                                    layout_refresh,
                                    response.message()
                                )
                            }
                        }

                        override fun onFailure(call: Call<BannerDto>, t: Throwable) {
                            showErrorMessage(
                                layout_refresh,
                                getString(R.string.msg_something_wrong)
                            )
                        }
                    })
                )
            } else {
                showErrorMessage(
                    layout_refresh,
                    getString(R.string.msg_no_internet)
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Constants.PICK_IMAGE_GALLERY_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            val sourceUri = data.data // 1
            val file = getTempFile(applicationContext) // 2
            val destinationUri = Uri.fromFile(file)  // 3
            if (sourceUri != null) {
                openCropActivity(sourceUri, destinationUri)
            } // 4
        } else if (resultCode == Activity.RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            val resultUri = UCrop.getOutput(data!!)
            banner(imagePath = resultUri?.path.toString())
        } else if (resultCode == UCrop.RESULT_ERROR) {
            val cropError = UCrop.getError(data!!)
        } else if (resultCode == Activity.RESULT_CANCELED) {
            showMessage(layout_refresh, "Fetching files cancelled.")
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

    private fun openCropActivity(sourceUri: Uri, destinationUri: Uri) {
        val options = UCrop.Options()
        options.setToolbarColor(ContextCompat.getColor(applicationContext, R.color.colorPrimary))
        options.setStatusBarColor(ContextCompat.getColor(applicationContext, R.color.colorPrimary))
        options.setActiveWidgetColor(
            ContextCompat.getColor(
                applicationContext,
                R.color.colorPrimary
            )
        )
        this@Banner.let {
            UCrop.of(sourceUri, destinationUri)
                .withOptions(options)
                .withAspectRatio(5f, 5f)
                .start(this@Banner)
        }
    }

    override fun onStop() {
        super.onStop()
        if (banner != null) {
            banner?.cancel()
        }
        if (getBanner != null) {
            getBanner?.cancel()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (banner != null) {
            banner?.cancel()
        }
        if (getBanner != null) {
            getBanner?.cancel()
        }
    }
}

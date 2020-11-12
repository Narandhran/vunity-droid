package com.vunity.banner

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.OpenableColumns
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.hbisoft.pickit.PickiT
import com.hbisoft.pickit.PickiTCallbacks
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.vunity.R
import com.vunity.general.*
import com.vunity.general.Constants.PERMISSIONS
import com.vunity.server.InternetDetector
import com.yalantis.ucrop.UCrop
import kotlinx.android.synthetic.main.act_update_banner.*
import kotlinx.android.synthetic.main.toolbar.*
import org.apache.commons.lang3.StringUtils


class UpdateBanner : AppCompatActivity(), PickiTCallbacks {

    private var internet: InternetDetector? = null
    private var imagePath: String? = null
    private var videoPath: String? = null
    private var pickIt: PickiT? = null
    private var bannerId: String? = null

    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.act_update_banner)

        txt_title.text = getString(R.string.video)
        txt_edit.visibility = View.GONE
        internet = InternetDetector.getInstance(this@UpdateBanner)
        pickIt = PickiT(this, this, this)

        im_back.setOnClickListener {
            onBackPressed()
        }

        try {
            val data = intent.getStringExtra("data")
            if (data != null) {
                val jsonAdapter: JsonAdapter<BannerData> =
                    moshi.adapter(BannerData::class.java)
                val bannerData: BannerData? = jsonAdapter.fromJson(data.toString())
                println(bannerData)
                if (bannerData != null) {
                    btn_create.text = getString(R.string.update)
                    bannerId = bannerData._id.toString()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            showMessage(lay_root, getString(R.string.unable_to_collect))
        }


        img_image_add.setOnClickListener {
            if (!hasPermissions(applicationContext, *PERMISSIONS)) {
                ActivityCompat.requestPermissions(this, PERMISSIONS, Constants.PERMISSION_ALL)
            } else {
                val permissionResult = checkPermission(this@UpdateBanner)
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

        img_video_add.setOnClickListener {
            if (!hasPermissions(applicationContext, *PERMISSIONS)) {
                ActivityCompat.requestPermissions(this, PERMISSIONS, Constants.PERMISSION_ALL)
            } else {
                val permissionResult = checkPermission(this@UpdateBanner)
                if (permissionResult) {
                    openVideo()
                } else {
                    showErrorMessage(
                        lay_root,
                        "Permission denied, Please grant permission to access files!"
                    )
                }
            }
        }

        btn_create.setOnClickListener {
            if (internet?.checkMobileInternetConn(applicationContext)!!) {
                when (imagePath) {
                    null -> {
                        showMessage(
                            lay_root,
                            "Updating banner image is required, Please tap on empty place holder to select image."
                        )
                    }
                    else -> {
                        val serviceIntent =
                            Intent(this@UpdateBanner, UpdateBannerService::class.java)
                        val mBundle = Bundle()
                        mBundle.putString("imagePath", imagePath)
                        mBundle.putString("videoPath", videoPath)
                        mBundle.putString("bannerId", bannerId)
                        serviceIntent.putExtras(mBundle)
                        ContextCompat.startForegroundService(this@UpdateBanner, serviceIntent)

                        showMessage(lay_root, "Updating progress has started in the background.")
                        Handler().postDelayed({
                            val homeIndent = Intent(this@UpdateBanner, Home::class.java)
                            homeIndent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            homeIndent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            startActivity(homeIndent)
                            this@UpdateBanner.overridePendingTransition(
                                R.anim.fade_in,
                                R.anim.fade_out
                            )
                            finish()
                        }, Constants.DELAY_MILLIS)
                    }
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

        img_video_delete.setOnClickListener {
            videoPath = null
            img_video_delete.visibility = View.GONE
            img_video_add.setImageDrawable(
                ContextCompat.getDrawable(applicationContext, R.drawable.ic_video)
            )
            txt_video_filename.text = getString(R.string.sample_video)
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Constants.PICK_VIDEO_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            val sourceUri = data.data // 1
            if (sourceUri != null) {
                pickIt?.getPath(data.data, Build.VERSION.SDK_INT)
                val name = StringUtils.right(getName(sourceUri, applicationContext), 7)
                img_video_delete.visibility = View.VISIBLE
                img_video_add.setImageDrawable(
                    ContextCompat.getDrawable(applicationContext, R.drawable.ic_video)
                )
                txt_video_filename.text = getString(R.string.star) + name
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
            imagePath = resultUri?.path
            val name = StringUtils.right(resultUri?.pathSegments?.last(), 7)
            img_image_delete.visibility = View.VISIBLE
            img_image_add.setImageDrawable(
                ContextCompat.getDrawable(applicationContext, R.drawable.ic_image_checked)
            )
            txt_image_filename.text = getString(R.string.star) + name
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

    private fun openVideo() {
        val videoIntent = Intent(Intent.ACTION_GET_CONTENT)
        videoIntent.type = "video/*"  // 1
        videoIntent.addCategory(Intent.CATEGORY_OPENABLE)  // 2
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            val mimeTypes = arrayOf("video/*")  // 3
            videoIntent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
        }
        startActivityForResult(
            Intent.createChooser(videoIntent, "Select Video"),
            Constants.PICK_VIDEO_REQUEST_CODE
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
        this@UpdateBanner.let {
            UCrop.of(sourceUri, destinationUri)
                .withOptions(options)
                .withAspectRatio(5f, 5f)
                .start(this@UpdateBanner)
        }
    }

    private fun getName(uri: Uri, context: Context): String {
        val returnCursor = context.contentResolver.query(uri, null, null, null, null)
        val nameIndex = returnCursor?.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        returnCursor?.moveToFirst()
        val fileName = nameIndex?.let { returnCursor.getString(it) }
        returnCursor?.close()
        return fileName.toString()
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
        videoPath = path
    }

    override fun onBackPressed() {
        if (bannerId?.isNotEmpty()!!) {
            backToPrevious(bannerId!!)
        } else {
            super.onBackPressed()
        }
    }

    private fun backToPrevious(id: String) {
        Handler().postDelayed({
            val intent = Intent()
            intent.putExtra(getString(R.string.data), id)
            setResult(RESULT_OK, intent)
            finish()
        }, 200)
    }
}

package com.vunity.general

import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider.getUriForFile
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.vunity.BuildConfig
import com.vunity.R
import com.yalantis.ucrop.UCrop
import kotlinx.android.synthetic.main.frag_choose_photo.*
import java.io.File


open class DialogChoosePhoto : BottomSheetDialogFragment() {

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.frag_choose_photo, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lay_takephoto.setOnClickListener {
            openCamera()
        }

        lay_choose.setOnClickListener {
            openPhotos()
        }

        lay_cancel.setOnClickListener {
            dismiss()
        }
    }

    companion object {
        val instance: DialogChoosePhoto
            get() = DialogChoosePhoto()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (requestCode == Constants.CAMERA_ACTION_PICK_REQUEST_CODE && resultCode == RESULT_OK) {
            val uri = Uri.parse(currentPhotoPath)
            openCropActivity(uri, uri)

        } else if (requestCode == Constants.PICK_IMAGE_GALLERY_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            val sourceUri = data.data // 1
            val file = getImageFile() // 2
            val destinationUri = Uri.fromFile(file)  // 3
            if (sourceUri != null) {
                openCropActivity(sourceUri, destinationUri)
            }  // 4

        } else if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            val resultUri = UCrop.getOutput(data!!)
            val location = Intent(Constants.fileLocation)
            location.putExtra("file_location", resultUri)
            context?.let { LocalBroadcastManager.getInstance(it).sendBroadcast(location) }
            dismiss()

        } else if (resultCode == UCrop.RESULT_ERROR) {
            val cropError = UCrop.getError(data!!)
        } else if (resultCode == RESULT_CANCELED) {
            Toast.makeText(
                activity?.applicationContext,
                "Taking picture failed.",
                Toast.LENGTH_SHORT
            ).show()
            // User Cancelled the action
        }

    }

    private fun openCamera() {
        val pictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val file = getImageFile() // 1
        val uri: Uri
        uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) // 2
            activity?.let { getUriForFile(it, BuildConfig.APPLICATION_ID + ".provider", file) }!!
        else
            Uri.fromFile(file) // 3
        pictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri) // 4
        pictureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        pictureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        startActivityForResult(
            pictureIntent,
            Constants.CAMERA_ACTION_PICK_REQUEST_CODE
        )
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
        val fileName = System.currentTimeMillis().toString() + ".jpg"
        val path =
            context?.getDir(context?.getString(R.string.app_name), AppCompatActivity.MODE_PRIVATE)
        val tempFiles = File(path, "temp")
        if (!tempFiles.exists()) {
            tempFiles.mkdirs()
        }
        val file = File(tempFiles, fileName)
        currentPhotoPath = "file:" + file.absolutePath
        return file
    }

    private fun openCropActivity(sourceUri: Uri, destinationUri: Uri) {
        activity?.let {
            val options = UCrop.Options()
            options.setToolbarColor(ContextCompat.getColor(context!!, R.color.colorPrimary))
            options.setStatusBarColor(ContextCompat.getColor(context!!, R.color.colorPrimary))
            options.setActiveWidgetColor(ContextCompat.getColor(context!!, R.color.colorPrimary))
            UCrop.of(sourceUri, destinationUri)
                .withAspectRatio(5f, 5f)
                .start(context!!, this, UCrop.REQUEST_CROP)
        }
    }

}
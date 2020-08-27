package com.vunity.general

import android.Manifest

object Constants {

    const val PUSH_NOTIFICATION = "pushNotification"

    // Permissions
    const val PERMISSION_ALL = 1
    val PERMISSIONS = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    // ImageUpload
    const val CAMERA_ACTION_PICK_REQUEST_CODE = 1888
    const val PICK_IMAGE_GALLERY_REQUEST_CODE = 41
    const val fileLocation = "FILE_LOCATION"
    const val MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 123
    const val MY_CAMERA_REQUEST_CODE = 100

    // PdfUpload
    const val PICK_PDF_REQUEST_CODE = 23

    // SharedPreferences
    const val PUBLIC_KEY = "com.vunity"

    // Cryptography
    const val ALGORITHM = "AES"
    const val KEY = "7961616d6c697465"

}
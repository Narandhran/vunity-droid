package com.vunity.server

import android.app.Activity
import android.app.ProgressDialog
import android.view.WindowManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@Suppress("DEPRECATION")
class RetrofitWithBar<T>(private val activity: Activity, private val mCallback: Callback<T>) :
    Callback<T> {
    private val mProgressDialog: ProgressDialog = ProgressDialog(activity)

    init {
        activity.window.setFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        )
        mProgressDialog.isIndeterminate = false
        mProgressDialog.setTitle("Processing...")
        mProgressDialog.setMessage("Hang on a moment...")
        mProgressDialog.setCanceledOnTouchOutside(false)
        mProgressDialog.show()
    }

    override fun onResponse(call: Call<T>, response: Response<T>) {
        mCallback.onResponse(call, response)
        if (mProgressDialog.isShowing) {
            mProgressDialog.dismiss()
            activity.window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        }
    }

    override fun onFailure(call: Call<T>, t: Throwable) {
        mCallback.onFailure(call, t)
        if (mProgressDialog.isShowing) {
            mProgressDialog.dismiss()
            activity.window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        }
    }
}

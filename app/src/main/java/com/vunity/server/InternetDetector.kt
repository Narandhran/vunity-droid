package com.vunity.server

import android.content.Context
import android.net.ConnectivityManager

@Suppress("DEPRECATION")
class InternetDetector(applicationContext: Context) {

    fun checkMobileInternetConn(ctx: Context): Boolean {
        val connectivity = ctx
            .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val mobInfo = connectivity.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
        val wifiInfo = connectivity.getNetworkInfo(ConnectivityManager.TYPE_WIFI)

        if (mobInfo != null || wifiInfo != null) {
            return mobInfo!!.isConnected || wifiInfo!!.isConnected
        }
        return false
    }

    companion object {

        private var mInstance: InternetDetector? = null

        fun getInstance(ctx: Context): InternetDetector {
            if (mInstance == null) {
                mInstance =
                    InternetDetector(ctx.applicationContext)
            }
            return mInstance as InternetDetector
        }
    }
}
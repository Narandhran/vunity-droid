package com.vunity.interfaces

import com.vunity.banner.BannerData

interface IOnBackPressed {
    fun onBackPressed(): Boolean
}

interface OnBannerClickListener {
    fun onItemClick(item: BannerData?)
}

interface AsyncResponse {
    fun processFinish(output: String)
}
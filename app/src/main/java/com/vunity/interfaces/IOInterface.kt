package com.vunity.interfaces

import com.vunity.banner.BannerData

interface IOnBackPressed {
    fun onBackPressed(): Boolean
}

interface OnBannerEditClickListener {
    fun onItemClick(item: BannerData?)
}

interface OnBannerPlayClickListener {
    fun onItemClick(item: BannerData?)
}

interface AsyncResponse {
    fun processFinish(output: String)
}
package com.vunity.interfaces

import com.vunity.banner.BannerData
import com.vunity.family.FamilyTreeData

interface IOnBackPressed {
    fun onBackPressed(): Boolean
}

interface OnBannerClickListener {
    fun onItemClick(item: BannerData?)
}

interface OnFamilyClickListener {
    fun onItemClick(item: FamilyTreeData?)
}

interface OnThithiClickListener {
    fun onItemClick(item: com.vunity.family.Thithi?)
}

interface AsyncResponse {
    fun processFinish(output: String)
}
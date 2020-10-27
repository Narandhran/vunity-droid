package com.vunity.banner

data class BannerDto(
    var contentFound: Boolean?,
    var `data`: BannerData?,
    var message: String?,
    var status: Int?
)

data class BannerListDto(
    var contentFound: Boolean?,
    var `data`: List<BannerData>?,
    var message: String?,
    var status: Int?
)

data class BannerData(
    var __v: Int?,
    var _id: String?,
    var banner: Any?,
    var video: Any?,
    var createdAt: String?,
    var updatedAt: String?
)
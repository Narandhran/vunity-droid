package com.vunity.vunity

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class VunityBody(
    var city: String?,
    var marital_status: String?,
    var mobile: String?,
    var mother_tongue: String?,
    var name: String?,
    var prayogam: MutableList<Any>?,
    var samprdhayam: String?,
    var shadanga_adhyayanam: Boolean?,
    var shakha: MutableList<Any>?,
    var shastra_adhyayanam: MutableList<Any>?,
    var user_id: String?,
    var vedha_adhyayanam: MutableList<Any>?,
    var vedham: String?
)

@JsonClass(generateAdapter = true)
data class VunityDto(
    var contentFound: Boolean?,
    var `data`: VunityData?,
    var message: String?,
    var status: Int?
)

@JsonClass(generateAdapter = true)
data class VunityListDto(
    var contentFound: Boolean?,
    var `data`: List<VunityData>?,
    var message: String?,
    var status: Int?
)

@JsonClass(generateAdapter = true)
data class VunityData(
    var __v: Int?,
    var _id: String?,
    var city: String?,
    var createdAt: String?,
    var isMobileVisible: Boolean?,
    var marital_status: String?,
    var mobile: String?,
    var mother_tongue: String?,
    var name: String?,
    var photo: Any?,
    var prayogam: MutableList<Any>?,
    var samprdhayam: String?,
    var shadanga_adhyayanam: Boolean?,
    var shakha: MutableList<Any>?,
    var shastra_adhyayanam: MutableList<Any>?,
    var updatedAt: String?,
    var user_id: UserId?,
    var vedha_adhyayanam: MutableList<Any>?,
    var vedham: String?
)

@JsonClass(generateAdapter = true)
data class UserId(
    var __v: Int?,
    var _id: String?,
    var createdAt: String?,
    var dp: Any?,
    var email: String?,
    var fcmToken: String?,
    var fname: String?,
    var lname: String?,
    var mobile: String?,
    var role: String?,
    var soothram: String?,
    var status: String?,
    var updatedAt: String?,
    var vaidhika: Boolean?,
    var verify: Verify?
)

@JsonClass(generateAdapter = true)
data class Verify(
    var expire: String?,
    var otp: Int?
)







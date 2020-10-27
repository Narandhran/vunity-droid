package com.vunity.user

import com.squareup.moshi.JsonClass

// Login
data class LoginBody(
    var mobile: String,
    val fcmToken: String,
    var otp: Int,
    var deviceId: String
)

data class LoginDto(
    var contentFound: Boolean,
    var `data`: LoginData,
    var message: String,
    var status: Int
)

data class LoginData(
    var rpath: String,
    var token: String,
    var role: String
)

// Registration
data class ResDto(
    var contentFound: Boolean,
    var `data`: Any,
    var message: String,
    var status: Int
)

// for error handling
@JsonClass(generateAdapter = true)
data class ErrorMsgDto(
    var error: String,
    var message: String,
    var status: Int
)

// for get profile
data class ProDto(
    var contentFound: Boolean,
    var `data`: ProData,
    var message: String,
    var status: Int
)

data class ProListDto(
    var contentFound: Boolean,
    var `data`: List<ProData>,
    var message: String,
    var status: Int
)

data class ProData(
    var _id: String?,
    var dp: String?,
    var email: String?,
    var fname: String?,
    var lname: String?,
    var mobile: String?,
    var status: String?
)

// for get update profile
data class ProfileDto(
    var contentFound: Boolean,
    var `data`: ProfileData,
    var message: String,
    var status: Int
)

data class ProfileData(
    var __v: Int,
    var _id: String,
    var active: Boolean,
    var createdAt: String,
    var dp: String,
    var email: String,
    var fname: String,
    var lname: String,
    var mobile: String,
    var role: String,
    var updatedAt: String,
    var verify: Verify
)

data class Verify(
    var expire: String,
    var otp: Int
)

data class DonateDto(
    var contentFound: Boolean?,
    var `data`: DonateData?,
    var message: String?,
    var status: Int?
)

data class DonateData(
    var amount: Int?,
    var id: String?
)

data class VerifyPayment(
    var razorpay_order_id: String,
    var razorpay_payment_id: String,
    var razorpay_signature: String
)

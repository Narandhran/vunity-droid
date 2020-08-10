package com.vunity.general

import android.annotation.SuppressLint
import android.util.Log
import java.text.SimpleDateFormat
import java.util.*

@SuppressLint("SimpleDateFormat")
val outputDateFormat = SimpleDateFormat("dd MMM yyyy hh:mm aaa")

@SuppressLint("SimpleDateFormat")
val outputTimeFormat = SimpleDateFormat("kk:mm:ss")

@SuppressLint("ConstantLocale")
val inputDateFormat =
    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())

fun dateFromLong(timestamp: Long): String {
    return outputDateFormat.format(Date(timestamp))
}

fun timeFromLong(timestamp: Long): String {
    return outputTimeFormat.format(Date(timestamp))
}

fun currentDate(): String {
    val date = Date()
    return inputDateFormat.format(date)
}

fun getDate(value: String, outputDateFormat: SimpleDateFormat): String {
    var date: Date? = null
    try {
        date = inputDateFormat.parse(value)
    } catch (exception: Exception) {
        Log.e("Exception from date fun", exception.toString())
    }
    return outputDateFormat.format(date!!)
}

fun getTime(value: String): String {
    var date: Date? = null
    try {
        date = inputDateFormat.parse(value)
    } catch (exception: Exception) {
        Log.e("Exception from date fun", exception.toString())
    }
    return outputTimeFormat.format(date!!)
}
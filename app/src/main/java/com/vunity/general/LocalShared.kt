package com.vunity.general

import android.app.Activity
import android.content.Context
import com.vunity.general.Constants.PUBLIC_KEY


fun saveData(key: String, value: String, context: Context) {
    val editor = context.getSharedPreferences(PUBLIC_KEY, Activity.MODE_PRIVATE).edit()
    editor.putString(key, encrypt(value))
    editor.apply()
}

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
fun getData(key: String, context: Context): String? {
    val prefs = context.getSharedPreferences(PUBLIC_KEY, Activity.MODE_PRIVATE)
    val value = prefs.getString(key, "")
    return if (value.isNullOrBlank()) {
        value
    } else {
        decrypt(value)
    }
}

fun getToken(key: String, context: Context): String? {
    val prefs = context.getSharedPreferences(PUBLIC_KEY, Activity.MODE_PRIVATE)
    return prefs.getString(key, "")
}

fun saveToken(key: String, value: String, context: Context) {
    val editor = context.getSharedPreferences(PUBLIC_KEY, Activity.MODE_PRIVATE).edit()
    editor.putString(key, value)
    editor.apply()
}

fun removeKey(key: String, context: Context) {
    val editor = context.getSharedPreferences(PUBLIC_KEY, Activity.MODE_PRIVATE).edit()
    editor.remove(key)
    editor.apply()
}

fun clearSession(context: Context) {
    val editor = context.getSharedPreferences(PUBLIC_KEY, Activity.MODE_PRIVATE).edit()
    editor.clear()
    editor.apply()
}
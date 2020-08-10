@file:Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")

package com.vunity.general

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.text.TextUtils
import android.util.Patterns
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.github.siyamed.shapeimageview.mask.PorterShapeImageView
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.squareup.picasso.Picasso
import com.vunity.R
import com.vunity.user.Login


fun isValidEmail(target: CharSequence?): Boolean {
    return !TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches()
}

fun coordinatorErrorMessage(view: View, message: String) {
    val snack = Snackbar.make(view, message, Snackbar.LENGTH_LONG)
    val v = snack.view
    val params = v.layoutParams as CoordinatorLayout.LayoutParams
    params.gravity = Gravity.TOP
    v.layoutParams = params
    val tv = v.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
    tv.setTextColor(Color.parseColor("#E45544"))
    snack.animationMode = BaseTransientBottomBar.ANIMATION_MODE_FADE
    snack.show()
}

fun coordinatorMessage(view: View, message: String) {
    val snack = Snackbar.make(view, message, Snackbar.LENGTH_LONG)
    val v = snack.view
    val params = v.layoutParams as CoordinatorLayout.LayoutParams
    params.gravity = Gravity.TOP
    v.layoutParams = params
    snack.show()
}

fun showErrorMessage(view: View, message: String) {
    val snack = Snackbar.make(view, message, Snackbar.LENGTH_LONG)
    val v = snack.view
    val params = v.layoutParams as FrameLayout.LayoutParams
    params.gravity = Gravity.TOP
    v.layoutParams = params
    val tv = v.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
    tv.setTextColor(Color.parseColor("#E45544"))
    snack.animationMode = BaseTransientBottomBar.ANIMATION_MODE_FADE
    snack.show()
}

fun showMessage(view: View, message: String) {
    val snack = Snackbar.make(view, message, Snackbar.LENGTH_LONG)
    val v = snack.view
    val params = v.layoutParams as FrameLayout.LayoutParams
    params.gravity = Gravity.TOP
    v.layoutParams = params
    snack.show()
}

fun reloadFragment(fragmentManager: FragmentManager, fragment: Fragment) {
    val ft = fragmentManager.beginTransaction()
    ft.detach(fragment).attach(fragment).commit()
}

fun reloadActivity(activity: Activity) {
    activity.finish()
    activity.overridePendingTransition(0, 0)
    activity.startActivity(activity.intent)
    activity.overridePendingTransition(0, 0)
}

fun getEmoji(unicode: Int): String {
    return String(Character.toChars(unicode))
}

fun sessionExpired(activity: Activity) {
    val builder = AlertDialog.Builder(activity)
        .setTitle("Session expired!")
        .setMessage("Please re-login to renew your session.")
        .setCancelable(false)
        .setPositiveButton(
            "OK"
        ) { dialog, _ ->
            val myApp = activity.application as Application
            clearSession(activity)
            myApp.clearApplicationData()
            val intent = Intent(activity, Login::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            activity.startActivity(intent)
            activity.overridePendingTransition(
                R.anim.fade_in,
                R.anim.fade_out
            )
            activity.finish()
            dialog.dismiss()
        }
    val alert = builder.create()
    alert.show()
}

fun loadImage(context: Context, path: String, image: AppCompatImageView) {
    Picasso.get()
        .load(getData("rootPath", context) + path)
        .placeholder(R.drawable.img_place_holder)
        .into(image)
}

fun loadImage(context: Context, path: String, image: PorterShapeImageView) {
    Picasso.get()
        .load(getData("rootPath", context) + path)
        .placeholder(R.drawable.img_place_holder)
        .into(image)
}
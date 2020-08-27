package com.vunity.general

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.vunity.R
import com.vunity.user.Login

class Splash : AppCompatActivity() {
    private var isLoggedIn: String? = null
    var fcmTitle: String? = null
    var fcmBody: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isLoggedIn = getData(
            "logged_user",
            applicationContext
        )
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    override fun onResume() {
        val bundle = intent.extras
        if (bundle != null) {
            for (key in bundle.keySet()!!) {
                val value = bundle[key]
                Log.e("Splash", "$key $value")
                if (key == "title") {
                    fcmTitle = bundle[key].toString()
                } else if (key == "body") {
                    fcmBody = bundle[key].toString()
                }
            }
        }
        if (isLoggedIn != null) {
            Log.d("isLoggedIn", "$isLoggedIn ")
            when (isLoggedIn) {
                "true" -> {
                    Handler().postDelayed({
                        val homeIndent = Intent(this@Splash, Home::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        Log.e("fcmTitle", "$fcmTitle $fcmBody")
                        homeIndent.putExtra("title", fcmTitle)
                        homeIndent.putExtra("body", fcmBody)
                        startActivity(homeIndent)
                        this@Splash.overridePendingTransition(
                            R.anim.fade_in,
                            R.anim.fade_out
                        )
                        finish()
                    }, 2000)
                }
                "skip" -> {
                    Handler().postDelayed({
                        startActivity(Intent(this@Splash, Home::class.java))
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        this@Splash.overridePendingTransition(
                            R.anim.fade_in,
                            R.anim.fade_out
                        )
                        finish()
                    }, 2000)
                }
                else -> {
                    Handler().postDelayed({
                        startActivity(Intent(this@Splash, Login::class.java))
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        this@Splash.overridePendingTransition(
                            R.anim.fade_in,
                            R.anim.fade_out
                        )
                        finish()
                    }, 2000)
                }
            }
        } else {
            Handler().postDelayed({
                startActivity(Intent(this@Splash, Login::class.java))
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                this@Splash.overridePendingTransition(
                    R.anim.fade_in,
                    R.anim.fade_out
                )
                finish()
            }, 2000)
        }
        super.onResume()
    }
}
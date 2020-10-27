package com.vunity.user

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.vunity.R
import com.vunity.general.Home
import com.vunity.general.getData

class Splash : AppCompatActivity() {
    private var isLoggedIn: String? = null
    private var fcmTitle: String? = null
    private var fcmBody: String? = null
    private var fcmBookId: String? = null

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
                when (key) {
                    "title" -> {
                        fcmTitle = bundle[key].toString()
                    }
                    "body" -> {
                        fcmBody = bundle[key].toString()
                    }
                    "bookId" -> {
                        fcmBookId = bundle[key].toString()
                    }
                }
            }
        }
        if (isLoggedIn != null) {
            when (isLoggedIn) {
                "true" -> {
                    Handler().postDelayed({
                        val homeIndent = Intent(this@Splash, Home::class.java)
                        homeIndent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        homeIndent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        homeIndent.putExtra("title", fcmTitle)
                        homeIndent.putExtra("body", fcmBody)
                        homeIndent.putExtra("bookId", fcmBookId)
                        startActivity(homeIndent)
                        this@Splash.overridePendingTransition(
                            R.anim.fade_in,
                            R.anim.fade_out
                        )
                        finish()
                    }, 2000)
                }
                else -> {
                    Handler().postDelayed({
                        val loginIntent = Intent(this@Splash, Login::class.java)
                        loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(loginIntent)
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
                val loginIntent = Intent(this@Splash, Login::class.java)
                loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(loginIntent)
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
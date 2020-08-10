package com.vunity

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.vunity.general.getData
import com.vunity.user.Login

class Splash : AppCompatActivity() {
    private var isLoggedIn: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isLoggedIn = getData(
            "logged_user",
            applicationContext
        )
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    override fun onResume() {
        if (isLoggedIn != null) {
            Log.d("isLoggedIn", "$isLoggedIn ")
            when (isLoggedIn) {
                "true" -> {
                    Handler().postDelayed({
                        val indent = Intent(this@Splash, Home::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(indent)

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
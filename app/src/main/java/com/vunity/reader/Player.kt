package com.vunity.reader

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelector
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.vunity.R
import com.vunity.general.Enums
import com.vunity.general.getData
import com.vunity.general.showMessage
import kotlinx.android.synthetic.main.act_player.*
import kotlinx.android.synthetic.main.toolbar.*

class Player : AppCompatActivity() {

    private var params: ConstraintLayout.LayoutParams? = null

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.act_player)

        txt_title.text = getString(R.string.app_name)
        txt_edit.visibility = View.GONE

        im_back.setOnClickListener {
            onBackPressed()
        }

        try {

            val data = intent.getStringExtra("data")
            if (data != null) {
                val url = getData("rootPath", this@Player) + Enums.Videos.value + data
                val trackSelectorDef: TrackSelector = DefaultTrackSelector()
                val absPlayerInternal =
                    ExoPlayerFactory.newSimpleInstance(applicationContext, trackSelectorDef)
                val userAgent =
                    Util.getUserAgent(applicationContext, getString(R.string.app_name))
                val sourceFactory =
                    DefaultDataSourceFactory(applicationContext, userAgent)
//                val uriOfContentUrl: Uri =
//                    Uri.parse("https://vunity.s3.ap-south-1.amazonaws.com/Anatomy+of+HTML+Tag+Explained+in+Tamil+(+720+X+1280+).mp4")
                val uriOfContentUrl: Uri = Uri.parse(url)
                val mediaSource: MediaSource =
                    ProgressiveMediaSource.Factory(sourceFactory).createMediaSource(uriOfContentUrl)
                absPlayerInternal.prepare(mediaSource)
                absPlayerInternal.playWhenReady = true

                params = lay_main.layoutParams as ConstraintLayout.LayoutParams
                params!!.height = 0
                params!!.matchConstraintPercentHeight = 0.35f
                lay_main.layoutParams = params
                viewPlayer.player = absPlayerInternal
            }
        } catch (e: Exception) {
            e.printStackTrace()
            showMessage(lay_main, getString(R.string.unable_to_collect))
        }

        img_mode.setOnClickListener {
            val orientation = resources.configuration.orientation
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                img_mode.setImageResource(R.drawable.ic_exit_fullscreen)
            } else {
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
                img_mode.setImageResource(R.drawable.ic_fullscreen)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        try {
            viewPlayer.player.stop()
            viewPlayer.player.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onPause() {
        super.onPause()
        try {
            viewPlayer.player.stop()
            viewPlayer.player.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            img_mode.setImageResource(R.drawable.ic_exit_fullscreen)
            window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            window.clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN)
            supportActionBar?.hide()
            params!!.height = ViewGroup.LayoutParams.MATCH_PARENT
            lay_main.layoutParams = params
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            img_mode.setImageResource(R.drawable.ic_fullscreen)
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            window.addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN)
            window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            supportActionBar?.show()
            params!!.height = 0
            params!!.matchConstraintPercentHeight = 0.35f
            lay_main.layoutParams = params
        }
    }

}

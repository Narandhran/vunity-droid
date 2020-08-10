@file:Suppress("DEPRECATION")

package com.vunity.reader

import android.app.ProgressDialog
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener
import com.github.barteksc.pdfviewer.listener.OnPageErrorListener
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.vunity.R
import com.vunity.general.Enums
import com.vunity.general.coordinatorMessage
import com.vunity.general.getData
import kotlinx.android.synthetic.main.act_reader.*
import kotlinx.android.synthetic.main.toolbar.*
import java.io.InputStream

class Reader : AppCompatActivity(), OnPageChangeListener, OnLoadCompleteListener,
    OnPageErrorListener {
    var pageNumber = 0
    lateinit var mProgressDialog: ProgressDialog
    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.act_reader)
        txt_title.text = getString(R.string.book)
        txt_edit.visibility = View.GONE
        im_back.setOnClickListener {
            onBackPressed()
        }

        mProgressDialog = ProgressDialog(this@Reader)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        )
        mProgressDialog.isIndeterminate = false
        mProgressDialog.setTitle("Processing...")
        mProgressDialog.setMessage("Hang on a moment...")
        mProgressDialog.setCanceledOnTouchOutside(false)
        mProgressDialog.show()

        try {
            val data = intent.getStringExtra("data")
            if (data != null) {
                val url = getData("rootPath", this@Reader) + Enums.Book.value + data
//                    val url = "https://swadharmaa.s3.ap-south-1.amazonaws.com/book/5_6305423766222537117.pdf"
                val stream = RetrievePDQStream().execute(url).get()
                loadFromStream(stream)
            }
        } catch (e: Exception) {
            Log.e("Exception", e.toString())
            coordinatorMessage(lay_root, getString(R.string.unable_to_fetch))
        }
    }

    private fun loadFromStream(stream: InputStream) {
        pdf_reader.fromStream(stream)
            .defaultPage(pageNumber)
            .onPageChange(this@Reader)
            .enableAnnotationRendering(true)
            .onLoad(this@Reader)
            .scrollHandle(DefaultScrollHandle(this@Reader))
            .spacing(10) // in dp
            .onPageError(this@Reader)
            .load()
    }

    override fun onPageChanged(page: Int, pageCount: Int) {

    }

    override fun loadComplete(nbPages: Int) {
        if (mProgressDialog.isShowing) {
            mProgressDialog.dismiss()
            window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        }
    }

    override fun onPageError(page: Int, t: Throwable?) {

    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            lay_toolbar.visibility = View.GONE
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            lay_toolbar.visibility = View.VISIBLE
        }
    }

}

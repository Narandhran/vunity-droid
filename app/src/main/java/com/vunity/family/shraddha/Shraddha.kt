package com.vunity.family.shraddha

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.tabs.TabLayout
import com.vunity.R
import com.vunity.general.reloadActivity
import com.vunity.server.InternetDetector
import kotlinx.android.synthetic.main.act_shraddha.*
import kotlinx.android.synthetic.main.toolbar.*

class Shraddha : AppCompatActivity() {

    var internetDetector: InternetDetector? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.act_shraddha)

        txt_title.text = getString(R.string.shraddha_information)
        txt_edit.visibility = View.GONE
        im_back.setOnClickListener {
            onBackPressed()
        }

        internetDetector = InternetDetector.getInstance(applicationContext)
        val shraardhaPagerAdapter = ShraddhaPagerAdapter(
            activity = this@Shraddha,
            fm = this@Shraddha.supportFragmentManager,
            tabCount = tab_shraardha.tabCount
        )
        view_pager_shraardha.adapter = shraardhaPagerAdapter
        tab_shraardha.addOnTabSelectedListener(
            TabLayout.ViewPagerOnTabSelectedListener(
                view_pager_shraardha
            )
        )
        view_pager_shraardha.addOnPageChangeListener(
            TabLayout.TabLayoutOnPageChangeListener(
                tab_shraardha
            )
        )
    }

    override fun onRestart() {
        reloadActivity(this@Shraddha)
        super.onRestart()
    }

    companion object {
        fun newInstance(): Shraddha = Shraddha()
    }
}
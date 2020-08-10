package com.vunity.family

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.vunity.R
import com.vunity.family.familyInfo.FamilyInfo
import com.vunity.family.familytree.FamilyTree
import com.vunity.family.personal.Personal
import com.vunity.family.shraddha.Shraddha
import com.vunity.general.reloadActivity
import kotlinx.android.synthetic.main.frag_family.*
import kotlinx.android.synthetic.main.toolbar.*

class ActFamily : AppCompatActivity() {

    var userId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.frag_family)

        layout_refresh.setOnRefreshListener {
            reloadActivity(this@ActFamily)
            layout_refresh.isRefreshing = false
        }

        txt_title.text = getString(R.string.family)
        txt_edit.visibility = View.GONE
        im_back.setOnClickListener {
            onBackPressed()
        }

        userId = intent.getStringExtra(getString(R.string.userId))

        lay_personal.setOnClickListener {
            val intent = Intent(this, Personal::class.java)
            intent.putExtra(getString(R.string.userId), userId)
            startActivity(intent)
        }

        lay_family.setOnClickListener {
            val intent = Intent(this, FamilyInfo::class.java)
            intent.putExtra(getString(R.string.userId), userId)
            startActivity(intent)
        }

        lay_family_tree.setOnClickListener {
            val intent = Intent(this, FamilyTree::class.java)
            intent.putExtra(getString(R.string.userId), userId)
            startActivity(intent)
        }

        lay_shraddha.setOnClickListener {
            val intent = Intent(this, Shraddha::class.java)
            intent.putExtra(getString(R.string.userId), userId)
            startActivity(intent)
        }
    }
}
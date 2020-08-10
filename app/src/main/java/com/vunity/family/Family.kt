package com.vunity.family

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.vunity.R
import com.vunity.family.familyInfo.FamilyInfo
import com.vunity.family.familytree.FamilyTree
import com.vunity.family.personal.Personal
import com.vunity.family.shraddha.Shraddha
import com.vunity.general.reloadFragment
import com.vunity.interfaces.IOnBackPressed
import kotlinx.android.synthetic.main.frag_family.*
import kotlinx.android.synthetic.main.toolbar.*

class Family : Fragment(), IOnBackPressed {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.frag_family, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        layout_refresh.setOnRefreshListener {
            reloadFragment(
                activity?.supportFragmentManager!!,
                this@Family
            )
            layout_refresh.isRefreshing = false
        }

        txt_title.text = getString(R.string.family)
        im_back.visibility = View.GONE
        txt_edit.visibility = View.GONE

        lay_personal.setOnClickListener {
            startActivity(Intent(requireActivity(), Personal::class.java))
        }

        lay_family.setOnClickListener {
            startActivity(Intent(requireActivity(), FamilyInfo::class.java))
        }

        lay_family_tree.setOnClickListener {
            startActivity(Intent(requireActivity(), FamilyTree::class.java))
        }

        lay_shraddha.setOnClickListener {
            startActivity(Intent(requireActivity(), Shraddha::class.java))
        }
    }

    override fun onDestroy() {
        super.onDestroy()

    }

    companion object {
        fun newInstance(): Family = Family()
    }

    override fun onBackPressed(): Boolean {
        return false
    }
}
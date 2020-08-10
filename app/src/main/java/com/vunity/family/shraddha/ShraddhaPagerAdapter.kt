package com.vunity.family.shraddha

import android.app.Activity
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.vunity.R
import com.vunity.family.shraddha.gothram.Gothram
import com.vunity.family.shraddha.name.Name
import com.vunity.family.shraddha.samayal.Samayal
import com.vunity.family.shraddha.thithi.Thithi
import com.vunity.family.shraddha.vazhakkam.Vazhakkam

@Suppress("DEPRECATION")
class ShraddhaPagerAdapter(
    var activity: Activity,
    fm: FragmentManager,
    var tabCount: Int
) :
    FragmentStatePagerAdapter(fm) {
    override fun getItem(position: Int): Fragment {
        val userId = activity.intent.getStringExtra(activity.getString(R.string.userId))
        val bundle = Bundle()
        bundle.putString(activity.getString(R.string.userId), userId)
        when (position) {
            0 -> {
                val fragment = Gothram()
                fragment.arguments = bundle
                return fragment
            }
            1 -> {
                val fragment = Name()
                fragment.arguments = bundle
                return fragment
            }
            2 -> {
                val fragment = Thithi()
                fragment.arguments = bundle
                return fragment
            }
            3 -> {
                val fragment = Samayal()
                fragment.arguments = bundle
                return fragment
            }
            else -> {
                val fragment = Vazhakkam()
                fragment.arguments = bundle
                return fragment
            }
        }
    }

    override fun getCount(): Int {
        return tabCount
    }
}
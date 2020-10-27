package com.vunity.general

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.squareup.picasso.Picasso
import com.vunity.R
import com.vunity.banner.Banner
import com.vunity.banner.BannerData
import com.vunity.reader.Player
import com.vunity.user.Profile


class ViewPagerAdapter(val activity: Activity, val data: MutableList<BannerData>) :
    PagerAdapter() {
    private var layoutInflater: LayoutInflater? = null

    override fun getCount(): Int {
        return data.size
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object`
    }

    @SuppressLint("InflateParams")
    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        layoutInflater =
            activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater?
        val view: View = layoutInflater?.inflate(R.layout.custom_image_banner, null)!!
        val imageView: ImageView = view.findViewById(R.id.img_banner) as ImageView
        Picasso.get()
            .load(getData("rootPath", activity) + Enums.Banner.value + data[position].banner)
            .error(R.drawable.im_banner_holder)
            .placeholder(R.drawable.im_banner_holder)
            .fit()
            .into(imageView)

        val role = getData(Enums.Role.value, activity.applicationContext)
        imageView.setOnClickListener {
            try {
                if (role == Enums.Admin.value) {
                    activity.startActivity(Intent(activity, Banner::class.java))
                } else {
                    if (data.size > 1 && position == 1) {
                        val intent = Intent(activity, Profile::class.java)
                        intent.putExtra(
                            activity.getString(R.string.data),
                            activity.getString(R.string.profile)
                        )
                        activity.startActivity(intent)
                    } else if (data[position].video != null) {
                        val intent = Intent(activity, Player::class.java)
                        intent.putExtra(
                            activity.getString(R.string.data),
                            data[position].video.toString()
                        )
                        activity.startActivity(intent)
                        activity.overridePendingTransition(
                            R.anim.fade_in,
                            R.anim.fade_out
                        )
                    }
                }
            } catch (exception: java.lang.Exception) {
                exception.printStackTrace()
            }
        }

        val vp = container as ViewPager
        vp.addView(view)
        return view
    }

    override fun destroyItem(
        container: ViewGroup,
        position: Int,
        `object`: Any
    ) {
        val vp = container as ViewPager
        val view: View = `object` as View
        vp.removeView(view)
    }
}
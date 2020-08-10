package com.vunity.general

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.squareup.picasso.Picasso
import com.vunity.R


class ViewPagerAdapter(private val context: Context, private val uri: MutableList<String>) :
    PagerAdapter() {
    private var layoutInflater: LayoutInflater? = null

    override fun getCount(): Int {
        return uri.size
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object`
    }

    @SuppressLint("InflateParams")
    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        layoutInflater =
            context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater?
        val view: View = layoutInflater?.inflate(R.layout.custom_image_banner, null)!!
        val imageView: ImageView = view.findViewById(R.id.img_banner) as ImageView
        Picasso.get().load(getData("rootPath", context) + Enums.Banner.value + uri[position])
            .error(R.drawable.im_banner_holder)
            .placeholder(R.drawable.im_banner_holder)
            .fit()
            .into(imageView)
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
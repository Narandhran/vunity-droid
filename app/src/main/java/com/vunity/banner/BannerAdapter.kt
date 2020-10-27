package com.vunity.banner

import android.annotation.SuppressLint
import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.squareup.picasso.Picasso
import com.vunity.R
import com.vunity.general.Enums
import com.vunity.general.getData
import com.vunity.interfaces.OnBannerEditClickListener
import com.vunity.interfaces.OnBannerPlayClickListener

class BannerAdapter(
    private var dataList: MutableList<BannerData>,
    private val activity: Activity,
    private val editClickListener: OnBannerEditClickListener,
    private val playClickListener: OnBannerPlayClickListener

) :
    RecyclerView.Adapter<BannerAdapter.Holder>() {
    lateinit var data: BannerData

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_banner, parent, false)
        return Holder(itemView)
    }

    @SuppressLint("SetTextI18n", "SimpleDateFormat", "ResourceAsColor")
    override fun onBindViewHolder(holder: Holder, @SuppressLint("RecyclerView") position: Int) {
        try {
            data = dataList[position]
            Picasso.get().load(
                getData(
                    "rootPath",
                    activity.applicationContext
                ) + Enums.Banner.value + data.banner
            ).placeholder(R.drawable.im_banner_holder)
                .into(holder.imgBanner)
            holder.bind(data, editClickListener, playClickListener)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    inner class Holder(view: View) : RecyclerView.ViewHolder(view) {
        var imgBanner: AppCompatImageView = view.findViewById(R.id.img_banner)
        private var imgEdit: AppCompatImageView = view.findViewById(R.id.img_edit)
        var cardMain: MaterialCardView = view.findViewById(R.id.card_main)
        fun bind(
            item: BannerData,
            editClickListener: OnBannerEditClickListener,
            playClickListener: OnBannerPlayClickListener
        ) {
            imgEdit.setOnClickListener { editClickListener.onItemClick(item) }
            cardMain.setOnClickListener { playClickListener.onItemClick(item) }
        }
    }
}

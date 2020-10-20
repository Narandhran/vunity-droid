package com.vunity.banner

import android.annotation.SuppressLint
import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import com.vunity.R
import com.vunity.general.Enums
import com.vunity.general.getData
import com.vunity.interfaces.OnBannerClickListener

class BannerAdapter(
    private var dataList: MutableList<BannerData>,
    private val activity: Activity,
    private val listener: OnBannerClickListener

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
            holder.bind(data, listener)
            val role = getData(Enums.Role.value, activity.applicationContext)
            if (role == Enums.Admin.value) {
                holder.imgEdit.visibility = View.VISIBLE
            } else {
                holder.imgEdit.visibility = View.GONE
            }
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
        var imgEdit: AppCompatImageView = view.findViewById(R.id.img_edit)
        fun bind(item: BannerData, listener: OnBannerClickListener) {
            imgEdit.setOnClickListener { listener.onItemClick(item) }
        }
    }
}

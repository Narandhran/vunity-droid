package com.vunity.vunity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.github.siyamed.shapeimageview.CircularImageView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textview.MaterialTextView
import com.squareup.picasso.Picasso
import com.vunity.R
import com.vunity.general.Enums
import com.vunity.general.getData
import org.apache.commons.lang3.StringUtils

class VunityAdapter(
    private var dataList: List<VunityData>,
    private val activity: Activity

) :
    RecyclerView.Adapter<VunityAdapter.Holder>() {
    lateinit var data: VunityData

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_vunity, parent, false)
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
                ) + Enums.Dp.value + data.photo
            ).placeholder(R.drawable.ic_dummy_profile).into(holder.imgProfile)
            Log.e(
                "PicassoPath",
                getData(
                    "rootPath",
                    activity.applicationContext
                ) + Enums.Dp.value + data.photo.toString()
            )
            holder.txtName.text =
                StringUtils.capitalize(data.user_id?.fname) + " " + StringUtils.capitalize(data.user_id?.lname)
            holder.txtCity.text = data.city
            holder.txtMobile.text = data.mobile
            holder.txtVedham.text = data.vedham
            holder.txtSampradhayam.text = data.samprdhayam
            holder.cardUser.setOnClickListener {
                data = dataList[position]
                val intent = Intent(activity, DetailsOfVunity::class.java)
                intent.putExtra(activity.getString(R.string.userId), data.user_id?._id)
                activity.startActivity(intent)
            }
        } catch (e: Exception) {
            Log.d("Exception", e.toString())
            e.printStackTrace()
        }
    }

    fun filterList(filteredList: MutableList<VunityData>) {
        this.dataList = filteredList
        notifyDataSetChanged()
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
        var imgProfile: CircularImageView = view.findViewById(R.id.img_profile)
        var txtName: MaterialTextView = view.findViewById(R.id.txt_fullname)
        var txtCity: MaterialTextView = view.findViewById(R.id.txt_city)
        var txtMobile: MaterialTextView = view.findViewById(R.id.txt_mobile)
        var txtVedham: MaterialTextView = view.findViewById(R.id.txt_vedham)
        var txtSampradhayam: MaterialTextView = view.findViewById(R.id.txt_sampradhayam)
        var cardUser: MaterialCardView = view.findViewById(R.id.card_user)
    }
}

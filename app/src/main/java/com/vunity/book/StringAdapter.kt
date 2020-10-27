package com.vunity.book

import android.annotation.SuppressLint
import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textview.MaterialTextView
import com.vunity.R
import org.apache.commons.lang3.StringUtils


class StringAdapter(
    private var flag: String,
    private var dataList: MutableList<Any>,
    private val activity: Activity

) :
    RecyclerView.Adapter<StringAdapter.Holder>() {
    lateinit var data: Any

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_round_text, parent, false)
        return Holder(itemView)
    }

    @SuppressLint("SetTextI18n", "SimpleDateFormat", "ResourceAsColor")
    override fun onBindViewHolder(holder: Holder, @SuppressLint("RecyclerView") position: Int) {
        data = dataList[position]
        try {
            data = dataList[position]
            holder.txtName.text = StringUtils.capitalize(data.toString())
            if (flag == activity.applicationContext.getString(R.string.view)) {
                holder.imgClose.visibility = View.GONE
            } else {
                holder.imgClose.visibility = View.VISIBLE
            }
            holder.imgClose.setOnClickListener {
                dataList.removeAt(position)
                notifyDataSetChanged()
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
        var imgClose: AppCompatImageView = view.findViewById(R.id.img_close)
        var txtName: MaterialTextView = view.findViewById(R.id.txt_name)
    }
}

package com.vunity.book

import android.annotation.SuppressLint
import android.app.Activity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.recyclerview.widget.RecyclerView
import com.vunity.R
import org.apache.commons.lang3.StringUtils


class CheckBoxAdapter(
    private var dataList: MutableList<Any>,
    private val activity: Activity

) :
    RecyclerView.Adapter<CheckBoxAdapter.Holder>() {
    lateinit var data: Any

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_round_checkbox, parent, false)
        return Holder(itemView)
    }

    @SuppressLint("SetTextI18n", "SimpleDateFormat", "ResourceAsColor")
    override fun onBindViewHolder(holder: Holder, @SuppressLint("RecyclerView") position: Int) {
        data = dataList[position]
        try {
            data = dataList[position]
            holder.checkBox.text = StringUtils.capitalize(data.toString())
        } catch (e: Exception) {
            Log.d("Exception", e.toString())
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
        var checkBox: AppCompatCheckBox = view.findViewById(R.id.custom_checkbox)
    }
}

package com.vunity.report

import android.annotation.SuppressLint
import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textview.MaterialTextView
import com.vunity.R


class ReportAdapter(
    private val dataList: List<ListReportData>,
    private val activity: Activity

) :
    RecyclerView.Adapter<ReportAdapter.Holder>() {
    lateinit var data: ListReportData

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {

        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_report_chart, parent, false)

        return Holder(itemView)
    }

    @SuppressLint("SetTextI18n", "SimpleDateFormat", "ResourceAsColor")
    override fun onBindViewHolder(holder: Holder, @SuppressLint("RecyclerView") position: Int) {
        try {
            data = dataList[position]
            holder.viewColour.setBackgroundColor(ContextCompat.getColor(activity, data.color))
            holder.txtDate.text = data.date
            val user = if (data.activeUsers < 1) {
                "User"
            } else {
                "User's"
            }
            holder.txtCount.text = data.activeUsers.toString() + " " + user
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
        var txtDate: MaterialTextView = view.findViewById(R.id.txt_date)
        var txtCount: MaterialTextView = view.findViewById(R.id.txt_usercount)
        var viewColour: View = view.findViewById(R.id.view_colour)
    }
}

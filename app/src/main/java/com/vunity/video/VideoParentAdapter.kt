package com.vunity.video

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textview.MaterialTextView
import com.vunity.R
import org.apache.commons.lang3.StringUtils


class VideoParentAdapter(
    private var dataList: List<HomeData>,
    private val activity: Activity

) :
    RecyclerView.Adapter<VideoParentAdapter.Holder>() {
    lateinit var data: HomeData

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_book_parent, parent, false)
        return Holder(itemView)
    }

    @SuppressLint("SetTextI18n", "SimpleDateFormat", "ResourceAsColor")
    override fun onBindViewHolder(holder: Holder, @SuppressLint("RecyclerView") position: Int) {
        try {
            data = dataList[position]
            holder.txtGenreName.text = StringUtils.capitalize(data.genre)
            holder.viewVideoChild.apply {
                holder.viewVideoChild.layoutManager =
                    LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
                holder.viewVideoChild.setHasFixedSize(true)
                val videoChildAdapter = VideoChildAdapter(data.books, activity)
                holder.viewVideoChild.adapter = videoChildAdapter
            }

            holder.btnMore.setOnClickListener {
                data = dataList[position]
                val intent = Intent(activity, VideoSeeAll::class.java)
                intent.putExtra(
                    activity.getString(R.string.data),
                    activity.getString(R.string.loadBooksByGenre)
                )
                intent.putExtra(activity.getString(R.string.title), data.genre)
                activity.startActivity(intent)
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
        var viewVideoChild: RecyclerView = view.findViewById(R.id.view_book_child)
        var txtGenreName: MaterialTextView = view.findViewById(R.id.txt_genre)
        var btnMore: MaterialTextView = view.findViewById(R.id.btn_more)
    }
}

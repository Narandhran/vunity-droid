package com.vunity.book

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textview.MaterialTextView
import com.vunity.R
import com.vunity.discover.SeeAll
import org.apache.commons.lang3.StringUtils


class BookParentAdapter(
    private var dataList: List<HomeData>,
    private val activity: Activity

) :
    RecyclerView.Adapter<BookParentAdapter.Holder>() {
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
            holder.viewBookChild.apply {
                holder.viewBookChild.layoutManager =
                    LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
                holder.viewBookChild.setHasFixedSize(true)
                val bookChildAdapter = BookChildAdapter(data.books, activity)
                holder.viewBookChild.adapter = bookChildAdapter
            }

            holder.btnMore.setOnClickListener {
                data = dataList[position]
                val intent = Intent(activity, SeeAll::class.java)
                intent.putExtra(
                    activity.getString(R.string.data),
                    activity.getString(R.string.loadBooksByGenre)
                )
                intent.putExtra(activity.getString(R.string.title), data.genre)
                activity.startActivity(intent)
            }
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
        var viewBookChild: RecyclerView = view.findViewById(R.id.view_book_child)
        var txtGenreName: MaterialTextView = view.findViewById(R.id.txt_genre)
        var btnMore: MaterialTextView = view.findViewById(R.id.btn_more)
    }
}

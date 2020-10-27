package com.vunity.category

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.github.siyamed.shapeimageview.mask.PorterShapeImageView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textview.MaterialTextView
import com.vunity.R
import com.vunity.book.BookSeeAll
import com.vunity.general.Enums
import com.vunity.general.loadImage
import com.vunity.video.VideoSeeAll
import org.apache.commons.lang3.StringUtils


class CategoryVideoAdapter(
    private var dataList: List<CategoryData>,
    private val activity: Activity,
    private var isBook: Boolean

) :
    RecyclerView.Adapter<CategoryVideoAdapter.Holder>() {
    lateinit var data: CategoryData

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_category_video, parent, false)
        return Holder(itemView)
    }

    @SuppressLint("SetTextI18n", "SimpleDateFormat", "ResourceAsColor")
    override fun onBindViewHolder(holder: Holder, @SuppressLint("RecyclerView") position: Int) {
        try {
            data = dataList[position]
            holder.txtName.text = StringUtils.capitalize(data.name)
            loadImage(
                activity.applicationContext,
                Enums.Category.value + data.thumbnail,
                holder.imgCategory
            )
            holder.cardCategory.setOnClickListener {
                data = dataList[position]
                if (isBook) {
                    val intent = Intent(activity, BookSeeAll::class.java)
                    intent.putExtra(
                        activity.getString(R.string.data),
                        activity.getString(R.string.loadBooksByCategory)
                    )
                    intent.putExtra(activity.getString(R.string.id), data._id)
                    intent.putExtra(activity.getString(R.string.name), data.name)
                    activity.startActivity(intent)
                } else {
                    val intent = Intent(activity, VideoSeeAll::class.java)
                    intent.putExtra(
                        activity.getString(R.string.data),
                        activity.getString(R.string.loadBooksByCategory)
                    )
                    intent.putExtra(activity.getString(R.string.id), data._id)
                    intent.putExtra(activity.getString(R.string.name), data.name)
                    activity.startActivity(intent)
                }
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
        var imgCategory: PorterShapeImageView = view.findViewById(R.id.img_category)
        var txtName: MaterialTextView = view.findViewById(R.id.txt_name)
        var cardCategory: MaterialCardView = view.findViewById(R.id.card_category)
    }
}

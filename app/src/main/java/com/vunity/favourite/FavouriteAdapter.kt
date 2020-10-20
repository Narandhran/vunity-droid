package com.vunity.favourite

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.RecyclerView
import com.github.siyamed.shapeimageview.mask.PorterShapeImageView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textview.MaterialTextView
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.squareup.picasso.Picasso
import com.vunity.R
import com.vunity.book.BookDetails
import com.vunity.general.*
import com.vunity.server.InternetDetector
import com.vunity.server.RetrofitClient
import com.vunity.server.RetrofitWithBar
import com.vunity.user.ErrorMsgDto
import com.vunity.user.ResDto
import org.apache.commons.lang3.StringUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class FavouriteAdapter(
    private var dataList: List<FavData>,
    private val activity: Activity,
    private val view: View

) :
    RecyclerView.Adapter<FavouriteAdapter.Holder>() {
    lateinit var data: FavData
    private var internet: InternetDetector? = null
    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_favourite, parent, false)
        return Holder(itemView)
    }

    @SuppressLint("SetTextI18n", "SimpleDateFormat", "ResourceAsColor")
    override fun onBindViewHolder(holder: Holder, @SuppressLint("RecyclerView") position: Int) {
        try {
            data = dataList[position]
            internet = InternetDetector.getInstance(activity)
            holder.txtName.text = StringUtils.capitalize(data.libraryId.name)
            Picasso.get().load(
                getData(
                    "rootPath",
                    activity.applicationContext
                ) + Enums.Book.value + data.libraryId.thumbnail
            ).placeholder(R.drawable.img_place_holder)
                .fit().into(holder.imgBook)

            holder.cardFav.setOnClickListener {
                data = dataList[position]
                val intent = Intent(activity, BookDetails::class.java)
                intent.putExtra(activity.getString(R.string.data), data.libraryId._id)
                activity.startActivity(intent)
            }

            holder.imgBookMark.setOnClickListener {
                removeFav(data.libraryId._id)
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
        var imgBook: PorterShapeImageView = view.findViewById(R.id.img_book)
        var imgBookMark: AppCompatImageView = view.findViewById(R.id.img_bookmark)
        var txtName: MaterialTextView = view.findViewById(R.id.txt_name)
        var cardFav: MaterialCardView = view.findViewById(R.id.card_favourite)
    }

    private fun removeFav(id: String) {
        if (internet?.checkMobileInternetConn(activity)!!) {
            val removeFav = RetrofitClient.favouriteClient.removeFavourite(id)
            removeFav.enqueue(
                RetrofitWithBar(activity, object : Callback<ResDto> {
                    @SuppressLint("SimpleDateFormat")
                    @RequiresApi(Build.VERSION_CODES.O)
                    override fun onResponse(
                        call: Call<ResDto>,
                        response: Response<ResDto>
                    ) {
                        if (response.code() == 200) {
                            when (response.body()?.status) {
                                200 -> {
                                    showMessage(view, response.body()!!.message)
                                    Handler().postDelayed({
                                        activity.finish()
                                        reloadActivity(activity)
                                    }, 300)
                                }
                                else -> {
                                    showErrorMessage(
                                        view,
                                        response.message()
                                    )
                                }
                            }

                        } else if (response.code() == 422 || response.code() == 400) {
                            try {
                                val adapter: JsonAdapter<ErrorMsgDto> =
                                    moshi.adapter(ErrorMsgDto::class.java)
                                val errorResponse =
                                    adapter.fromJson(response.errorBody()!!.string())
                                if (errorResponse != null) {
                                    if (errorResponse.status == 400) {
                                        showErrorMessage(
                                            view,
                                            errorResponse.message
                                        )
                                    } else {
                                        showErrorMessage(
                                            view,
                                            errorResponse.message
                                        )
                                    }

                                } else {
                                    showErrorMessage(
                                        view,
                                        activity.getString(R.string.msg_something_wrong)
                                    )
                                }
                            } catch (e: Exception) {
                                showErrorMessage(
                                    view,
                                    activity.getString(R.string.msg_something_wrong)
                                )
                            }

                        } else if (response.code() == 401) {
                            sessionExpired(
                                activity
                            )
                        } else {
                            showErrorMessage(
                                view,
                                response.message()
                            )
                        }
                    }

                    override fun onFailure(call: Call<ResDto>, t: Throwable) {
                        showErrorMessage(
                            view,
                            activity.getString(R.string.msg_something_wrong)
                        )
                    }
                })
            )

        } else {
            showErrorMessage(
                view,
                activity.getString(R.string.msg_no_internet)
            )
        }
    }
}

package com.vunity.user

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.SwitchCompat
import androidx.recyclerview.widget.RecyclerView
import com.github.siyamed.shapeimageview.CircularImageView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textview.MaterialTextView
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.squareup.picasso.Picasso
import com.vunity.R
import com.vunity.general.*
import com.vunity.server.InternetDetector
import com.vunity.server.RetrofitClient
import com.vunity.server.RetrofitWithBar
import com.vunity.vunity.DetailsOfVunity
import org.apache.commons.lang3.StringUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class UserAdapter(
    private var dataList: List<ProData>,
    private val activity: Activity,
    private val view: View

) :
    RecyclerView.Adapter<UserAdapter.Holder>() {
    lateinit var data: ProData
    private var internet: InternetDetector? = null
    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_user, parent, false)
        return Holder(itemView)
    }

    @SuppressLint("SetTextI18n", "SimpleDateFormat", "ResourceAsColor")
    override fun onBindViewHolder(holder: Holder, @SuppressLint("RecyclerView") position: Int) {
        try {
            internet = InternetDetector.getInstance(activity)
            data = dataList[position]
            Picasso.get().load(
                getData(
                    "rootPath",
                    activity.applicationContext
                ) + Enums.Dp.value + data.dp
            ).placeholder(R.drawable.ic_dummy_profile).into(holder.imgProfile)
            holder.txtName.text =
                StringUtils.capitalize(data.fname) + " " + StringUtils.capitalize(data.lname)
            holder.txtEmail.text = data.email
            holder.txtMobile.text = data.mobile

            val role = getData(Enums.Role.value, activity.applicationContext)
            if (role == Enums.Admin.value) {
                holder.layCMS.visibility = View.VISIBLE
                if (data.status?.equals(Enums.Pending.value)!!) {
                    holder.btnApprove.visibility = View.VISIBLE
                } else {
                    holder.btnApprove.visibility = View.GONE
                }
            } else {
                holder.layCMS.visibility = View.GONE
            }

            if (data.status == Enums.Blocked.value) {
                holder.swtBlock.isChecked = true
            }

            holder.swtBlock.setOnCheckedChangeListener { _, isChecked ->
                data = dataList[position]
                if (isChecked) {
                    // The toggle is enabled
                    val mapData: HashMap<String, String> = HashMap()
                    mapData["id"] = data._id.toString()
                    mapData["status"] = Enums.Blocked.value
                    reviewUser(mapData)
                } else {
                    // The toggle is disabled
                    val mapData: HashMap<String, String> = HashMap()
                    mapData["id"] = data._id.toString()
                    mapData["status"] = Enums.Approved.value
                    reviewUser(mapData)
                }
            }

            holder.btnApprove.setOnClickListener {
                data = dataList[position]
                val mapData: HashMap<String, String> = HashMap()
                mapData["id"] = data._id.toString()
                mapData["status"] = Enums.Approved.value
                reviewUser(mapData)
            }

            holder.cardUser.setOnClickListener {
                data = dataList[position]
                val intent = Intent(activity, DetailsOfVunity::class.java)
                intent.putExtra(activity.getString(R.string.userId), data._id)
                activity.startActivity(intent)
                activity.finish()
            }

        } catch (e: Exception) {
            Log.d("Exception", e.toString())
            e.printStackTrace()
        }
    }

    fun filterList(filteredList: MutableList<ProData>) {
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

    class Holder(view: View) : RecyclerView.ViewHolder(view) {
        var imgProfile: CircularImageView = view.findViewById(R.id.img_profile)
        var txtName: MaterialTextView = view.findViewById(R.id.txt_fullname)
        var txtEmail: MaterialTextView = view.findViewById(R.id.txt_email)
        var txtMobile: MaterialTextView = view.findViewById(R.id.txt_mobile)
        var btnApprove: MaterialTextView = view.findViewById(R.id.btn_approve)
        var swtBlock: SwitchCompat = view.findViewById(R.id.swt_block)
        var cardUser: MaterialCardView = view.findViewById(R.id.card_user)
        var layCMS: RelativeLayout = view.findViewById(R.id.lay_cms)
    }

    private fun reviewUser(data: HashMap<String, String>) {
        Log.e("data", data.toString())
        if (internet?.checkMobileInternetConn(activity)!!) {
            val reviewUser = RetrofitClient.instanceClient.cmsReview(data)
            reviewUser.enqueue(
                RetrofitWithBar(activity, object : Callback<ResDto> {
                    @SuppressLint("SimpleDateFormat")
                    @RequiresApi(Build.VERSION_CODES.O)
                    override fun onResponse(
                        call: Call<ResDto>,
                        response: Response<ResDto>
                    ) {
                        Log.e("onResponse", response.toString())
                        if (response.code() == 200) {
                            when (response.body()?.status) {
                                200 -> {
                                    coordinatorMessage(view, response.body()!!.message)
                                    Handler().postDelayed({
                                        activity.finish()
                                        reloadActivity(activity)
                                    }, 300)
                                }
                                else -> {
                                    coordinatorErrorMessage(
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
                                        coordinatorErrorMessage(
                                            view,
                                            errorResponse.message
                                        )
                                    } else {
                                        coordinatorErrorMessage(
                                            view,
                                            errorResponse.message
                                        )
                                    }

                                } else {
                                    coordinatorErrorMessage(
                                        view,
                                        activity.getString(R.string.msg_something_wrong)
                                    )
                                    Log.e(
                                        "Response",
                                        response.body()!!.toString()
                                    )
                                }
                            } catch (e: Exception) {
                                coordinatorErrorMessage(
                                    view,
                                    activity.getString(R.string.msg_something_wrong)
                                )
                                Log.e("Exception", e.toString())
                            }

                        } else if (response.code() == 401) {
                            sessionExpired(
                                activity
                            )
                        } else {
                            coordinatorErrorMessage(
                                view,
                                response.message()
                            )
                        }
                    }

                    override fun onFailure(call: Call<ResDto>, t: Throwable) {
                        Log.e("onResponse", t.message.toString())
                        coordinatorErrorMessage(
                            view,
                            activity.getString(R.string.msg_something_wrong)
                        )
                    }
                })
            )

        } else {
            coordinatorErrorMessage(
                view,
                activity.getString(R.string.msg_no_internet)
            )
        }
    }
}

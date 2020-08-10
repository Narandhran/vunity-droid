package com.vunity.family.shraddha.thithi

import android.annotation.SuppressLint
import android.app.Activity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.textview.MaterialTextView
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.vunity.R
import com.vunity.interfaces.OnThithiClickListener
import org.apache.commons.lang3.StringUtils


class ThithiAdapter(
    private var dataList: List<com.vunity.family.Thithi>,
    private val activity: Activity,
    private val listener: OnThithiClickListener

) :
    RecyclerView.Adapter<ThithiAdapter.Holder>() {
    lateinit var data: com.vunity.family.Thithi
    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_thithi, parent, false)
        return Holder(itemView)
    }

    @SuppressLint("SetTextI18n", "SimpleDateFormat", "ResourceAsColor")
    override fun onBindViewHolder(holder: Holder, @SuppressLint("RecyclerView") position: Int) {
        data = dataList[position]
        try {
            data = dataList[position]
            holder.txtRelationship.text = StringUtils.capitalize(data.relationship)
            holder.txtName.text = StringUtils.capitalize(data.name)
            holder.txtMasamSauramanam.text = StringUtils.capitalize(data.masamSauramanam)
            holder.txtMasamChandramanam.text = StringUtils.capitalize(data.masamChandramanam)
            holder.txtPaksham.text = StringUtils.capitalize(data.paksham)
            holder.txtThithi.text = StringUtils.capitalize(data.thithi)
            holder.txtDate.text = StringUtils.capitalize(data.date)
            holder.txtTime.text = StringUtils.capitalize(data.time)
            holder.bind(data, listener)

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
        var txtRelationship: MaterialTextView = view.findViewById(R.id.txt_relationship)
        var txtName: MaterialTextView = view.findViewById(R.id.txt_name)
        var txtMasamSauramanam: MaterialTextView = view.findViewById(R.id.txt_masam_sauramanam)
        var txtMasamChandramanam: MaterialTextView = view.findViewById(R.id.txt_masam_chandramanam)
        var txtPaksham: MaterialTextView = view.findViewById(R.id.txt_paksham)
        var txtThithi: MaterialTextView = view.findViewById(R.id.txt_thithi)
        var txtDate: MaterialTextView = view.findViewById(R.id.txt_date)
        var txtTime: MaterialTextView = view.findViewById(R.id.txt_time)
        var btnEdit: MaterialButton = view.findViewById(R.id.btn_edit)
        fun bind(item: com.vunity.family.Thithi, listener: OnThithiClickListener) {
            btnEdit.setOnClickListener { listener.onItemClick(item) }
        }
    }
}

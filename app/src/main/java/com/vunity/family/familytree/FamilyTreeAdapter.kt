package com.vunity.family.familytree

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
import com.vunity.family.FamilyTreeData
import com.vunity.interfaces.OnFamilyClickListener
import org.apache.commons.lang3.StringUtils


class FamilyTreeAdapter(
    private var dataList: List<FamilyTreeData>,
    private val activity: Activity,
    private val listener: OnFamilyClickListener

) :
    RecyclerView.Adapter<FamilyTreeAdapter.Holder>() {
    lateinit var data: FamilyTreeData
    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_family_tree, parent, false)
        return Holder(itemView)
    }

    @SuppressLint("SetTextI18n", "SimpleDateFormat", "ResourceAsColor")
    override fun onBindViewHolder(holder: Holder, @SuppressLint("RecyclerView") position: Int) {
        try {
            data = dataList[position]
            Log.e("Exception", data.toString())
            holder.txtName.text = StringUtils.capitalize(data.name)
            holder.txtRelationship.text = StringUtils.capitalize(data.relationship)
            holder.txtDob.text = StringUtils.capitalize(data.dateOfBirth)
            holder.txtRashi.text = StringUtils.capitalize(data.rashi)
            holder.txtNakshathram.text = StringUtils.capitalize(data.nakshathram)
            holder.txtPadham.text = StringUtils.capitalize(data.padham)
            holder.txtCity.text = StringUtils.capitalize(data.city)
            holder.txtMobile.text = StringUtils.capitalize(data.mobileNumber)
            holder.bind(data, listener)

        } catch (e: Exception) {
            Log.e("Exception", e.toString())
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
        var txtName: MaterialTextView = view.findViewById(R.id.txt_name)
        var txtRelationship: MaterialTextView = view.findViewById(R.id.txt_relationship)
        var txtDob: MaterialTextView = view.findViewById(R.id.txt_dob)
        var txtRashi: MaterialTextView = view.findViewById(R.id.txt_rashi)
        var txtNakshathram: MaterialTextView = view.findViewById(R.id.txt_nakshathram)
        var txtPadham: MaterialTextView = view.findViewById(R.id.txt_padham)
        var txtCity: MaterialTextView = view.findViewById(R.id.txt_city)
        var txtMobile: MaterialTextView = view.findViewById(R.id.txt_mobile)
        var btnEdit: MaterialButton = view.findViewById(R.id.btn_edit)
        fun bind(item: FamilyTreeData, listener: OnFamilyClickListener) {
            btnEdit.setOnClickListener { listener.onItemClick(item) }
        }
    }
}

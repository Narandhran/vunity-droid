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
import com.vunity.general.prayogamList
import com.vunity.general.shakhaList
import com.vunity.general.shastraAdhyayanamList
import com.vunity.general.vedhaAdhyayanamList
import org.apache.commons.lang3.StringUtils


class CheckBoxAdapter(
    private var dataList: MutableList<Any>,
    private val activity: Activity,
    private val title: String

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
        try {
            data = dataList[position]
            holder.checkBox.text = StringUtils.capitalize(data.toString())
            holder.checkBox.setOnCheckedChangeListener { button, b ->
                if (button.isChecked) {
                    data = dataList[position]
                    when (title) {
                        activity.getString(R.string.shaka) -> {
                            shakhaList.add(data.toString())
                        }
                        activity.getString(R.string.vedha_adhyayanam) -> {
                            vedhaAdhyayanamList.add(data.toString())
                        }
                        activity.getString(R.string.shastra_adhyayanam) -> {
                            shastraAdhyayanamList.add(data.toString())
                        }
                        activity.getString(R.string.prayogam) -> {
                            prayogamList.add(data.toString())
                        }
                    }
                } else {
                    data = dataList[position]
                    when (title) {
                        activity.getString(R.string.shaka) -> {
                            shakhaList.remove(data.toString())
                        }
                        activity.getString(R.string.vedha_adhyayanam) -> {
                            vedhaAdhyayanamList.remove(data.toString())
                        }
                        activity.getString(R.string.shastra_adhyayanam) -> {
                            shastraAdhyayanamList.remove(data.toString())
                        }
                        activity.getString(R.string.prayogam) -> {
                            prayogamList.remove(data.toString())
                        }
                    }
                }
                Log.e(
                    "dataList",
                    "$shakhaList $vedhaAdhyayanamList $prayogamList $shastraAdhyayanamList"
                )
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
        var checkBox: AppCompatCheckBox = view.findViewById(R.id.custom_checkbox)
    }
}

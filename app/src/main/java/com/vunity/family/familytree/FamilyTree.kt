package com.vunity.family.familytree

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.vunity.R
import com.vunity.family.FamilyDto
import com.vunity.family.FamilyTreeData
import com.vunity.general.getData
import com.vunity.general.reloadActivity
import com.vunity.general.sessionExpired
import com.vunity.general.showErrorMessage
import com.vunity.interfaces.OnFamilyClickListener
import com.vunity.server.InternetDetector
import com.vunity.server.RetrofitClient
import com.vunity.user.ErrorMsgDto
import kotlinx.android.synthetic.main.act_family_tree.*
import kotlinx.android.synthetic.main.toolbar.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class FamilyTree : AppCompatActivity() {

    private var family: Call<FamilyDto>? = null
    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
    var internetDetector: InternetDetector? = null
    var userId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.act_family_tree)
        txt_title.text = getString(R.string.family_tree)
        txt_edit.text = getString(R.string.add)
        layout_refresh.setOnRefreshListener {
            reloadActivity(this@FamilyTree)
            layout_refresh.isRefreshing = false
        }
        im_back.setOnClickListener {
            onBackPressed()
        }
        userId = intent.getStringExtra(getString(R.string.userId))
        family()

        btn_add.setOnClickListener {
            val intent = Intent(this@FamilyTree, AddFamilyTree::class.java)
            intent.putExtra(getString(R.string.userId), userId)
            startActivity(intent)
        }

        txt_edit.setOnClickListener {
            val intent = Intent(this@FamilyTree, AddFamilyTree::class.java)
            intent.putExtra(getString(R.string.userId), userId)
            startActivity(intent)
        }
    }

    private fun family() {
        internetDetector = InternetDetector.getInstance(this@FamilyTree)
        if (internetDetector?.checkMobileInternetConn(applicationContext)!!) {
            family = if (userId != null) {
                RetrofitClient.instanceClient.listOfFamily(userId!!)
            } else {
                RetrofitClient.instanceClient.listOfFamily(
                    getData("user_id", applicationContext).toString()
                )
            }
            family?.enqueue(object : Callback<FamilyDto> {
                @SuppressLint("DefaultLocale", "SetTextI18n")
                override fun onResponse(
                    call: Call<FamilyDto>,
                    response: Response<FamilyDto>
                ) {
                    Log.e("onResponse", response.toString())
                    when {
                        response.code() == 200 -> {
                            when (response.body()?.status) {
                                200 -> {
                                    lay_no_data.visibility = View.GONE
                                    lay_no_internet.visibility = View.GONE
                                    view_familytree.visibility = View.VISIBLE

                                    val familyTreeList: MutableList<FamilyTreeData> =
                                        response.body()!!.data?.familyTree!!.toMutableList()

                                    if (familyTreeList.isNotEmpty()) {
                                        view_familytree?.apply {
                                            view_familytree?.layoutManager = LinearLayoutManager(
                                                applicationContext,
                                                LinearLayoutManager.VERTICAL,
                                                false
                                            )
                                            view_familytree?.setHasFixedSize(true)
                                            val familyTreeAdapter =
                                                FamilyTreeAdapter(familyTreeList, this@FamilyTree,
                                                    object : OnFamilyClickListener {
                                                        override fun onItemClick(item: FamilyTreeData?) {
                                                            val jsonAdapter: JsonAdapter<FamilyTreeData> =
                                                                moshi.adapter(FamilyTreeData::class.java)
                                                            val json = jsonAdapter.toJson(item)
                                                            val intent = Intent(
                                                                this@FamilyTree,
                                                                AddFamilyTree::class.java
                                                            )
                                                            intent.putExtra(
                                                                getString(R.string.data),
                                                                json
                                                            )
                                                            intent.putExtra(
                                                                getString(R.string.userId),
                                                                userId
                                                            )
                                                            startActivity(intent)
                                                        }
                                                    }
                                                )
                                            view_familytree?.adapter = familyTreeAdapter
                                        }
                                    } else {
                                        lay_no_data.visibility = View.VISIBLE
                                        view_familytree.visibility = View.GONE
                                        lay_no_internet.visibility = View.GONE
                                    }
                                }
                                204 -> {
                                    lay_no_data.visibility = View.VISIBLE
                                    view_familytree.visibility = View.GONE
                                    lay_no_internet.visibility = View.GONE
                                }
                                else -> {
                                    showErrorMessage(
                                        layout_refresh,
                                        response.message()
                                    )
                                }
                            }
                        }

                        response.code() == 422 || response.code() == 400 -> {
                            try {
                                val adapter: JsonAdapter<ErrorMsgDto> =
                                    moshi.adapter(ErrorMsgDto::class.java)
                                val errorResponse =
                                    adapter.fromJson(response.errorBody()!!.string())
                                if (errorResponse != null) {
                                    if (errorResponse.status == 400) {
                                        showErrorMessage(
                                            layout_refresh,
                                            errorResponse.message
                                        )
                                    } else {
                                        showErrorMessage(
                                            layout_refresh,
                                            errorResponse.message
                                        )
                                    }

                                } else {
                                    showErrorMessage(
                                        layout_refresh,
                                        getString(R.string.msg_something_wrong)
                                    )
                                    Log.e(
                                        "Response",
                                        response.body()!!.toString()
                                    )
                                }
                            } catch (e: Exception) {
                                showErrorMessage(
                                    layout_refresh,
                                    getString(R.string.msg_something_wrong)
                                )
                                Log.e("Exception", e.toString())
                            }

                        }

                        response.code() == 401 -> {
                            sessionExpired(this@FamilyTree)
                        }
                        else -> {
                            showErrorMessage(
                                layout_refresh,
                                response.message()
                            )
                        }
                    }
                }

                override fun onFailure(call: Call<FamilyDto>, t: Throwable) {
                    Log.e("onFailure", t.message.toString())
                    if (!call.isCanceled) {
                        showErrorMessage(
                            layout_refresh,
                            getString(R.string.msg_something_wrong)
                        )
                    }
                }
            })

        } else {
            lay_no_data.visibility = View.GONE
            view_familytree.visibility = View.GONE
            lay_no_internet.visibility = View.VISIBLE
        }
    }

    override fun onRestart() {
        super.onRestart()
        family()
    }

    override fun onStop() {
        if (family != null) {
            family?.cancel()
        }
        super.onStop()
    }
}
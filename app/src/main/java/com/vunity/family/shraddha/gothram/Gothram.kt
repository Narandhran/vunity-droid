package com.vunity.family.shraddha.gothram

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.vunity.R
import com.vunity.family.FamilyData
import com.vunity.family.FamilyDto
import com.vunity.general.getData
import com.vunity.general.reloadFragment
import com.vunity.general.sessionExpired
import com.vunity.general.showErrorMessage
import com.vunity.interfaces.IOnBackPressed
import com.vunity.server.InternetDetector
import com.vunity.server.RetrofitClient
import com.vunity.user.ErrorMsgDto
import kotlinx.android.synthetic.main.frag_gothram.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class Gothram : Fragment(), IOnBackPressed {

    private var family: Call<FamilyDto>? = null
    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
    var internetDetector: InternetDetector? = null
    var userId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.frag_gothram, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        internetDetector = InternetDetector.getInstance(requireContext())
        layout_refresh.setOnRefreshListener {
            reloadFragment(
                activity?.supportFragmentManager!!,
                this@Gothram
            )
            layout_refresh.isRefreshing = false
        }

        userId = this.arguments!!.getString(getString(R.string.userId))
        family()

        btn_add.setOnClickListener {
            val intent = Intent(Intent(requireActivity(), AddGothram::class.java))
            intent.putExtra(getString(R.string.userId), userId)
            startActivity(intent)
        }
    }

    private fun family() {
        if (internetDetector?.checkMobileInternetConn(requireContext())!!) {
            family = if (userId != null) {
                RetrofitClient.instanceClient.listOfFamily(userId!!)
            } else {
                RetrofitClient.instanceClient.listOfFamily(
                    getData("user_id", requireContext()).toString()
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
                                    lay_data.visibility = View.VISIBLE

                                    txt_pithru_gothram.text =
                                        response.body()!!.data?.shraardhaInfo!!.gothram?.pithruGothram.toString()
                                    txt_mathru_gothram.text =
                                        response.body()!!.data?.shraardhaInfo!!.gothram?.mathruGothram.toString()
                                    btn_edit.setOnClickListener {
                                        val jsonAdapter: JsonAdapter<FamilyData> =
                                            moshi.adapter(FamilyData::class.java)
                                        val json = jsonAdapter.toJson(response.body()!!.data)
                                        val intent =
                                            Intent(requireActivity(), AddGothram::class.java)
                                        intent.putExtra(getString(R.string.data), json)
                                        intent.putExtra(getString(R.string.userId), userId)
                                        startActivity(intent)
                                    }
                                }
                                204 -> {
                                    lay_no_data.visibility = View.VISIBLE
                                    lay_data.visibility = View.GONE
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
                            sessionExpired(requireActivity())
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
            lay_data.visibility = View.GONE
            lay_no_internet.visibility = View.VISIBLE
        }
    }

    override fun onStop() {
        super.onStop()
        if (family != null) {
            family?.cancel()
        }
    }

    override fun onDestroy() {
        super.onDestroy()

    }

    companion object {
        fun newInstance(): Gothram =
            Gothram()
    }

    override fun onBackPressed(): Boolean {
        return false
    }
}


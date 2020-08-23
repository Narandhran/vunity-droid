package com.vunity.vunity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.squareup.picasso.Picasso
import com.vunity.R
import com.vunity.book.StringAdapter
import com.vunity.general.*
import com.vunity.interfaces.IOnBackPressed
import com.vunity.server.InternetDetector
import com.vunity.server.RetrofitClient
import com.vunity.server.RetrofitWithBar
import com.vunity.user.ErrorMsgDto
import com.vunity.user.Login
import com.vunity.user.ProDto
import com.vunity.user.Profile
import kotlinx.android.synthetic.main.frag_vunity.*
import org.apache.commons.lang3.StringUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class Vunity : Fragment(), IOnBackPressed {

    private var profile: Call<ProDto>? = null
    private var getByUser: Call<VunityDto>? = null
    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
    var internetDetector: InternetDetector? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.frag_vunity, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        layout_refresh.setOnRefreshListener {
            reloadFragment(
                activity?.supportFragmentManager!!,
                this@Vunity
            )
            layout_refresh.isRefreshing = false
        }

        img_profile.setOnClickListener {
            val isLoggedIn = getData("logged_user", requireContext())
            if (isLoggedIn == getString(R.string.skip)) {
                val intent = Intent(requireActivity(), Login::class.java)
                intent.putExtra(getString(R.string.data), getString(R.string.new_user))
                requireActivity().startActivity(intent)
            } else {
                requireActivity().startActivity(Intent(requireActivity(), Profile::class.java))
            }
        }

        txt_title.text = getString(R.string.app_name)
        internetDetector = InternetDetector(requireContext())
        btn_add.setOnClickListener {
            startActivity(Intent(requireActivity(), AddVunity::class.java))
        }

        if (internetDetector?.checkMobileInternetConn(requireActivity())!!) {
            loadProfileInfo()
        } else {
            lay_no_data.visibility = View.GONE
            lay_data.visibility = View.GONE
            lay_no_internet.visibility = View.VISIBLE
        }
        getByUser()
    }

    private fun getByUser() {
        if (internetDetector?.checkMobileInternetConn(requireContext())!!) {
            val userId = getData("user_id", requireContext()).toString()
            getByUser = RetrofitClient.instanceClient.vunityGetByUser(userId)
            getByUser?.enqueue(RetrofitWithBar(requireActivity(), object : Callback<VunityDto> {
                @SuppressLint("DefaultLocale", "SetTextI18n")
                override fun onResponse(
                    call: Call<VunityDto>,
                    response: Response<VunityDto>
                ) {
                    Log.e("onResponse", response.toString())
                    when {
                        response.code() == 200 -> {
                            when (response.body()?.status) {
                                200 -> {
                                    lay_no_data.visibility = View.GONE
                                    lay_no_internet.visibility = View.GONE
                                    lay_data.visibility = View.VISIBLE

                                    Picasso.get().load(
                                        getData(
                                            "rootPath",
                                            requireContext()
                                        ) + Enums.Dp.value + response.body()!!.data?.photo
                                    ).placeholder(R.drawable.ic_dummy_profile).into(img_vprofile)

                                    Log.e(
                                        "Picasso", getData(
                                            "rootPath",
                                            requireContext()
                                        ) + Enums.Dp.value + response.body()!!.data?.photo
                                    )

                                    txt_name.text = response.body()!!.data?.name.toString()
                                    txt_mobile.text = response.body()!!.data?.mobile.toString()
                                    txt_city.text = response.body()!!.data?.city.toString()
                                    txt_vedham.text = response.body()!!.data?.vedham.toString()
                                    txt_sampradhayam.text =
                                        response.body()!!.data?.samprdhayam.toString()

                                    val shaka = response.body()!!.data?.shakha!!
                                    if (shaka.isNotEmpty()) {
                                        view_shaka?.apply {
                                            view_shaka?.layoutManager = LinearLayoutManager(
                                                requireContext(),
                                                LinearLayoutManager.HORIZONTAL,
                                                false
                                            )
                                            view_shaka?.setHasFixedSize(true)
                                            val genreAdapter =
                                                StringAdapter(
                                                    getString(R.string.view),
                                                    shaka,
                                                    requireActivity()
                                                )
                                            view_shaka?.adapter = genreAdapter
                                        }
                                    }

                                    val vedhaAdhyayanam: MutableList<Any> =
                                        response.body()!!.data?.vedha_adhyayanam!!
                                    if (vedhaAdhyayanam.isNotEmpty()) {
                                        view_vedha_adhyayanam?.apply {
                                            view_vedha_adhyayanam?.layoutManager =
                                                LinearLayoutManager(
                                                    requireContext(),
                                                    LinearLayoutManager.HORIZONTAL,
                                                    false
                                                )
                                            view_vedha_adhyayanam?.setHasFixedSize(true)
                                            val genreAdapter = StringAdapter(
                                                getString(R.string.view),
                                                vedhaAdhyayanam,
                                                requireActivity()
                                            )
                                            view_vedha_adhyayanam?.adapter = genreAdapter
                                        }
                                    }

                                    txt_shadanga_adhyayanam.text =
                                        response.body()!!.data?.shadanga_adhyayanam.toString()

                                    val shastraAdhyayanam: MutableList<Any> =
                                        response.body()!!.data?.shastra_adhyayanam!!
                                    if (shastraAdhyayanam.isNotEmpty()) {
                                        view_shastra_adhyayanam?.apply {
                                            view_shastra_adhyayanam?.layoutManager =
                                                LinearLayoutManager(
                                                    requireContext(),
                                                    LinearLayoutManager.HORIZONTAL,
                                                    false
                                                )
                                            view_shastra_adhyayanam?.setHasFixedSize(true)
                                            val genreAdapter =
                                                StringAdapter(
                                                    getString(R.string.view),
                                                    shastraAdhyayanam,
                                                    requireActivity()
                                                )
                                            view_shastra_adhyayanam?.adapter = genreAdapter
                                        }
                                    }

                                    val prayogam: MutableList<Any> =
                                        response.body()!!.data?.prayogam!!
                                    if (prayogam.isNotEmpty()) {
                                        view_prayogam?.apply {
                                            view_prayogam?.layoutManager = LinearLayoutManager(
                                                requireContext(),
                                                LinearLayoutManager.HORIZONTAL,
                                                false
                                            )
                                            view_prayogam?.setHasFixedSize(true)
                                            val genreAdapter =
                                                StringAdapter(
                                                    getString(R.string.view),
                                                    prayogam,
                                                    requireActivity()
                                                )
                                            view_prayogam?.adapter = genreAdapter
                                        }
                                    }

                                    txt_marital_status.text =
                                        response.body()!!.data?.marital_status.toString()
                                    txt_mothertongue.text =
                                        response.body()!!.data?.mother_tongue.toString()

                                    img_edit.setOnClickListener {
                                        val jsonAdapter: JsonAdapter<VunityData> =
                                            moshi.adapter(VunityData::class.java)
                                        val json = jsonAdapter.toJson(response.body()!!.data)
                                        val intent =
                                            Intent(requireActivity(), AddVunity::class.java)
                                        intent.putExtra(getString(R.string.data), json)
                                        startActivity(intent)
                                    }
                                }
                                204 -> {
                                    lay_no_data.visibility = View.VISIBLE
                                    lay_data.visibility = View.GONE
                                    lay_no_internet.visibility = View.GONE
                                }
                                else -> {
                                    coordinatorErrorMessage(
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
                                        coordinatorErrorMessage(
                                            layout_refresh,
                                            errorResponse.message
                                        )
                                    } else {
                                        coordinatorErrorMessage(
                                            layout_refresh,
                                            errorResponse.message
                                        )
                                    }

                                } else {
                                    coordinatorErrorMessage(
                                        layout_refresh,
                                        getString(R.string.msg_something_wrong)
                                    )
                                    Log.e(
                                        "Response",
                                        response.body()!!.toString()
                                    )
                                }
                            } catch (e: Exception) {
                                coordinatorErrorMessage(
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
                            coordinatorErrorMessage(
                                layout_refresh,
                                response.message()
                            )
                        }
                    }
                }

                override fun onFailure(call: Call<VunityDto>, t: Throwable) {
                    Log.e("onFailure", t.message.toString())
                    if (!call.isCanceled) {
                        coordinatorErrorMessage(
                            layout_refresh,
                            getString(R.string.msg_something_wrong)
                        )
                    }
                }
            }))

        } else {
            lay_no_data.visibility = View.GONE
            lay_data.visibility = View.GONE
            lay_no_internet.visibility = View.VISIBLE
        }
    }

    private fun loadProfileInfo() {
        profile = RetrofitClient.instanceClient.profile()
        profile?.enqueue(object : Callback<ProDto> {
            @SuppressLint("DefaultLocale", "SetTextI18n")
            override fun onResponse(
                call: Call<ProDto>,
                response: Response<ProDto>
            ) {
                Log.e("onResponse", response.toString())
                when {
                    response.code() == 200 -> {
                        when (response.body()?.status) {
                            200 -> {
                                try {
                                    saveData(
                                        "fullname",
                                        StringUtils.capitalize(response.body()?.data?.fname?.toLowerCase()) + " " + StringUtils.capitalize(
                                            response.body()?.data?.lname?.toLowerCase()
                                        ),
                                        requireContext()
                                    )
                                    saveData(
                                        "mobile",
                                        response.body()?.data?.mobile.toString(),
                                        requireContext()
                                    )
                                    saveData(
                                        "username",
                                        response.body()?.data?.email.toString(),
                                        requireContext()
                                    )
                                    saveData(
                                        "dp",
                                        response.body()?.data?.dp.toString(),
                                        requireContext()
                                    )
                                    saveData(
                                        "user_id",
                                        response.body()?.data?._id.toString(),
                                        requireContext()
                                    )

                                    Picasso.get()
                                        .load(
                                            getData(
                                                "rootPath",
                                                requireContext()
                                            ) + Enums.Dp.value + response.body()?.data?.dp
                                        )
                                        .error(R.drawable.ic_dummy_profile)
                                        .placeholder(R.drawable.ic_dummy_profile)
                                        .into(img_profile)
                                } catch (e: Exception) {
                                    Log.d("Profile", e.toString())
                                    e.printStackTrace()
                                }
                            }
                            else -> {
                                Log.e("Response", response.message())
                            }
                        }
                    }
                    response.code() == 422 || response.code() == 400 -> {
                        try {
                            val moshi: Moshi = Moshi.Builder().build()
                            val adapter: JsonAdapter<ErrorMsgDto> =
                                moshi.adapter(ErrorMsgDto::class.java)
                            val errorResponse =
                                adapter.fromJson(response.errorBody()!!.string())
                            if (errorResponse != null) {
                                if (errorResponse.status == 400) {
                                    Log.e("Response", errorResponse.message)
                                } else {
                                    Log.e("Response", errorResponse.message)
                                }

                            } else {
                                Log.e("Response", response.body()!!.toString())
                            }
                        } catch (e: Exception) {
                            Log.e("Exception", e.toString())
                        }
                    }
                    response.code() == 401 -> {
                        sessionExpired(requireActivity())
                    }
                    else -> {
                        Log.e("Response", response.message().toString())
                    }
                }
            }

            override fun onFailure(call: Call<ProDto>, t: Throwable) {
                Log.e("onFailure", t.message.toString())
            }
        })
    }

    companion object {
        fun newInstance(): Vunity = Vunity()
    }

    override fun onBackPressed(): Boolean {
        return false
    }

    override fun onDestroy() {
        super.onDestroy()
        if (getByUser != null) {
            getByUser?.cancel()
        }
    }

    override fun onStop() {
        super.onStop()
        if (getByUser != null) {
            getByUser?.cancel()
        }
    }

}


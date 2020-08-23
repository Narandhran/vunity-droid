@file:Suppress("DEPRECATION")

package com.vunity.vunity

import `in`.galaxyofandroid.spinerdialog.SpinnerDialog
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.squareup.picasso.Picasso
import com.vunity.R
import com.vunity.book.CheckBoxAdapter
import com.vunity.general.*
import com.vunity.interfaces.IOnBackPressed
import com.vunity.server.InternetDetector
import com.vunity.server.RetrofitClient
import com.vunity.user.ErrorMsgDto
import com.vunity.user.Login
import com.vunity.user.ProDto
import com.vunity.user.Profile
import kotlinx.android.synthetic.main.act_home.*
import kotlinx.android.synthetic.main.bottomsheet_vunity_filter.*
import kotlinx.android.synthetic.main.content_list_vunity.*
import org.apache.commons.lang3.StringUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class UsersVunity : Fragment(), IOnBackPressed {

    private var profile: Call<ProDto>? = null
    private var loadUsers: Call<VunityListDto>? = null
    var internetDetector: InternetDetector? = null
    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
    var userData: MutableList<VunityData> = arrayListOf()
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<ConstraintLayout>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.frag_users_vuinty, container, false)
    }

    @SuppressLint("WrongConstant")
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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

        layout_refresh.setOnRefreshListener {
            reloadFragment(
                activity?.supportFragmentManager!!,
                this@UsersVunity
            )
            layout_refresh.isRefreshing = false
        }

        internetDetector = InternetDetector.getInstance(activity!!)
        loadUsers()

        if (internetDetector?.checkMobileInternetConn(requireActivity())!!) {
            loadProfileInfo()
        } else {
            lay_shimmer.visibility = View.GONE
            lay_shimmer.stopShimmer()
            lay_no_data.visibility = View.GONE
            lay_data.visibility = View.GONE
            lay_no_internet.visibility = View.VISIBLE
        }

        bottomSheetBehavior = BottomSheetBehavior.from(bottomsheet_filter)
        bottomSheetBehavior.setBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {}
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_COLLAPSED -> {
                        requireActivity().navigationView.visibility = View.VISIBLE
                    }
                    BottomSheetBehavior.STATE_HIDDEN -> {

                    }
                    BottomSheetBehavior.STATE_EXPANDED -> {
                        requireActivity().navigationView.visibility = View.GONE
                    }
                    BottomSheetBehavior.STATE_DRAGGING -> {

                    }
                    BottomSheetBehavior.STATE_SETTLING -> {

                    }
                    BottomSheetBehavior.STATE_HALF_EXPANDED -> {
                        TODO()
                    }
                }
            }
        })

//        val staggeredGridLayoutManager = StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL)

        edt_city.setOnClickListener {
            val data: ArrayList<String> = arrayListOf()
            data.addAll(resources.getStringArray(R.array.city))
            val spinnerDialog = SpinnerDialog(
                requireActivity(),
                data,
                "Select city.",
                R.style.DialogAnimations_SmileWindow, // For slide animation
                "Cancel"
            )
            spinnerDialog.setCancellable(true) // for cancellable
            spinnerDialog.setShowKeyboard(false)// for open keyboard by default
            spinnerDialog.bindOnSpinerListener { item, position ->
                edt_city.setText(item)
            }
            spinnerDialog.showSpinerDialog()
        }

        edt_vedham.setOnClickListener {
            val data: ArrayList<String> = arrayListOf()
            data.addAll(resources.getStringArray(R.array.vedham))
            val spinnerDialog = SpinnerDialog(
                requireActivity(),
                data,
                "Select vedham.",
                R.style.DialogAnimations_SmileWindow, // For slide animation
                "Cancel"
            )
            spinnerDialog.setCancellable(true) // for cancellable
            spinnerDialog.setShowKeyboard(false)// for open keyboard by default
            spinnerDialog.bindOnSpinerListener { item, position ->
                edt_vedham.setText(item)
            }
            spinnerDialog.showSpinerDialog()
        }

        edt_sampradhayam.setOnClickListener {
            val data: ArrayList<String> = arrayListOf()
            data.addAll(resources.getStringArray(R.array.sampradhayam))
            val spinnerDialog = SpinnerDialog(
                requireActivity(),
                data,
                "Select sampradhayam.",
                R.style.DialogAnimations_SmileWindow, // For slide animation
                "Cancel"
            )
            spinnerDialog.setCancellable(true) // for cancellable
            spinnerDialog.setShowKeyboard(false)// for open keyboard by default
            spinnerDialog.bindOnSpinerListener { item, position ->
                edt_sampradhayam.setText(item)
            }
            spinnerDialog.showSpinerDialog()
        }

        view_shaka?.apply {
            view_shaka?.layoutManager = LinearLayoutManager(
                requireContext(),
                LinearLayoutManager.HORIZONTAL,
                false
            )
            view_shaka?.setHasFixedSize(true)
            val adapter =
                CheckBoxAdapter(
                    resources.getStringArray(R.array.shakha).toMutableList(),
                    requireActivity()
                )
            view_shaka?.adapter = adapter
        }

        view_vedha_adhyayanam?.apply {
            view_vedha_adhyayanam?.layoutManager = LinearLayoutManager(
                requireContext(),
                LinearLayoutManager.HORIZONTAL,
                false
            )
            view_vedha_adhyayanam?.setHasFixedSize(true)
            val adapter =
                CheckBoxAdapter(
                    resources.getStringArray(R.array.vedha_adhyayanam).toMutableList(),
                    requireActivity()
                )
            view_vedha_adhyayanam?.adapter = adapter
        }

        edt_shadanga_adhyayanam.setOnClickListener {
            val data: ArrayList<String> = arrayListOf()
            data.addAll(resources.getStringArray(R.array.shadanga_adhyayanam))
            val spinnerDialog = SpinnerDialog(
                requireActivity(),
                data,
                "Select Shadanga adhyayanam.",
                R.style.DialogAnimations_SmileWindow, // For slide animation
                "Cancel"
            )
            spinnerDialog.setCancellable(true) // for cancellable
            spinnerDialog.setShowKeyboard(false)// for open keyboard by default
            spinnerDialog.bindOnSpinerListener { item, position ->
                edt_shadanga_adhyayanam.setText(item)
            }
            spinnerDialog.showSpinerDialog()
        }

        view_shastra_adhyayanam?.apply {
            view_shastra_adhyayanam?.layoutManager = LinearLayoutManager(
                requireContext(),
                LinearLayoutManager.HORIZONTAL,
                false
            )
            view_shastra_adhyayanam?.setHasFixedSize(true)
            val adapter =
                CheckBoxAdapter(
                    resources.getStringArray(R.array.shastra_adhyayana).toMutableList(),
                    requireActivity()
                )
            view_shastra_adhyayanam?.adapter = adapter
        }

        view_prayogam?.apply {
            view_prayogam?.layoutManager = LinearLayoutManager(
                requireContext(),
                LinearLayoutManager.HORIZONTAL,
                false
            )
            view_prayogam?.setHasFixedSize(true)
            val adapter =
                CheckBoxAdapter(
                    resources.getStringArray(R.array.prayogam).toMutableList(),
                    requireActivity()
                )
            view_prayogam?.adapter = adapter
        }

        edt_marital_status.setOnClickListener {
            val data: ArrayList<String> = arrayListOf()
            data.addAll(resources.getStringArray(R.array.marital_status))
            val spinnerDialog = SpinnerDialog(
                requireActivity(),
                data,
                "Select Marital status.",
                R.style.DialogAnimations_SmileWindow, // For slide animation
                "Cancel"
            )
            spinnerDialog.setCancellable(true) // for cancellable
            spinnerDialog.setShowKeyboard(false)// for open keyboard by default
            spinnerDialog.bindOnSpinerListener { item, position ->
                edt_marital_status.setText(item)
            }
            spinnerDialog.showSpinerDialog()
        }

        edt_mothertongue.setOnClickListener {
            val data: ArrayList<String> = arrayListOf()
            data.addAll(resources.getStringArray(R.array.mother_tongue))
            val spinnerDialog = SpinnerDialog(
                requireActivity(),
                data,
                "Select Mother tongue.",
                R.style.DialogAnimations_SmileWindow, // For slide animation
                "Cancel"
            )
            spinnerDialog.setCancellable(true) // for cancellable
            spinnerDialog.setShowKeyboard(false)// for open keyboard by default
            spinnerDialog.bindOnSpinerListener { item, position ->
                edt_mothertongue.setText(item)
            }
            spinnerDialog.showSpinerDialog()
        }

        img_filter.setOnClickListener {
            if (bottomSheetBehavior.state != BottomSheetBehavior.STATE_EXPANDED) {
                bottomSheetBehavior.state =
                    BottomSheetBehavior.STATE_EXPANDED
            }
        }

        btn_apply.setOnClickListener {
            if (bottomSheetBehavior.state != BottomSheetBehavior.STATE_COLLAPSED) {
                bottomSheetBehavior.state =
                    BottomSheetBehavior.STATE_COLLAPSED
            }
        }

        btn_clear.setOnClickListener {
            if (bottomSheetBehavior.state != BottomSheetBehavior.STATE_COLLAPSED) {
                bottomSheetBehavior.state =
                    BottomSheetBehavior.STATE_COLLAPSED
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (profile != null) {
            profile?.cancel()
        }
        if (loadUsers != null) {
            loadUsers?.cancel()
        }
    }

    override fun onStop() {
        super.onStop()
        if (profile != null) {
            profile?.cancel()
        }
        if (loadUsers != null) {
            loadUsers?.cancel()
        }
    }

    companion object {
        fun newInstance(): UsersVunity = UsersVunity()
    }

    override fun onBackPressed(): Boolean {
        return false
    }

    private fun loadUsers() {
        if (internetDetector?.checkMobileInternetConn(requireActivity())!!) {
            if (!lay_shimmer.isShimmerStarted) {
                lay_shimmer.startShimmer()
            }
            loadUsers = RetrofitClient.instanceClient.vunityAllUsers()
            loadUsers?.enqueue(object : Callback<VunityListDto> {
                @SuppressLint("DefaultLocale", "SetTextI18n")
                override fun onResponse(
                    call: Call<VunityListDto>,
                    response: Response<VunityListDto>
                ) {
                    Log.e("onResponse", response.toString())
                    when {
                        response.code() == 200 -> {
                            when (response.body()?.status) {
                                200 -> {
                                    userData = response.body()!!.data?.toMutableList()!!
                                    lay_no_data.visibility = View.GONE
                                    lay_no_internet.visibility = View.GONE
                                    lay_data.visibility = View.VISIBLE
                                    view_vunity?.apply {
                                        view_vunity?.layoutManager =
                                            LinearLayoutManager(
                                                requireContext(),
                                                LinearLayoutManager.VERTICAL,
                                                false
                                            )
                                        view_vunity?.setHasFixedSize(true)
                                        val vunityAdapter =
                                            VunityAdapter(userData, requireActivity())
                                        view_vunity?.adapter = vunityAdapter
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
                            sessionExpired(activity!!)
                        }
                        else -> {
                            coordinatorErrorMessage(
                                layout_refresh,
                                response.message()
                            )
                        }
                    }
                    lay_shimmer.visibility = View.GONE
                    lay_shimmer.stopShimmer()
                }

                override fun onFailure(call: Call<VunityListDto>, t: Throwable) {
                    Log.e("onFailure", t.message.toString())
                    if (!call.isCanceled) {
                        coordinatorErrorMessage(
                            layout_refresh,
                            getString(R.string.msg_something_wrong)
                        )
                        lay_shimmer.visibility = View.GONE
                        lay_shimmer.stopShimmer()
                    }
                }
            })
        } else {
            lay_shimmer.visibility = View.GONE
            lay_shimmer.stopShimmer()
            lay_no_data.visibility = View.GONE
            lay_data.visibility = View.GONE
            lay_no_internet.visibility = View.VISIBLE
        }
    }

    private fun loadProfileInfo() {
        if (!lay_shimmer.isShimmerStarted) {
            lay_shimmer.startShimmer()
        }
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
}
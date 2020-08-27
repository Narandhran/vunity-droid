@file:Suppress("DEPRECATION")

package com.vunity.vunity

import `in`.galaxyofandroid.spinerdialog.SpinnerDialog
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.SearchView
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
import com.vunity.user.Profile
import kotlinx.android.synthetic.main.act_home.*
import kotlinx.android.synthetic.main.content_vunity_users.*
import kotlinx.android.synthetic.main.filter_vunity_users.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class VunityUsers : Fragment(), IOnBackPressed {

    private var vunityUsers: Call<VunityListDto>? = null
    var internetDetector: InternetDetector? = null
    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
    var userData: MutableList<VunityData> = arrayListOf()
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<ConstraintLayout>
    var adapter: ArrayAdapter<String>? = null
    val listOfCities = arrayListOf<String>()
    private var queryTextListener: SearchView.OnQueryTextListener? = null
    private var queryValue: String? = null
    private var filterBody: FilterBody? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.frag_vuinty_users, container, false)
    }

    @SuppressLint("WrongConstant")
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        shakhaList.clear()
        vedhaAdhyayanamList.clear()
        shastraAdhyayanamList.clear()
        prayogamList.clear()

        Picasso.get()
            .load(
                getData(
                    "rootPath",
                    requireContext()
                ) + Enums.Dp.value + getData("dp", requireContext())
            )
            .error(R.drawable.ic_dummy_profile)
            .placeholder(R.drawable.ic_dummy_profile)
            .into(img_profile)

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
                this@VunityUsers
            )
            layout_refresh.isRefreshing = false
        }

        internetDetector = InternetDetector.getInstance(activity!!)

        queryTextListener = object : SearchView.OnQueryTextListener {
            override fun onQueryTextChange(newText: String?): Boolean {
                when {
                    newText.toString().length >= 3 -> {
                        filterBody = null
                        queryValue = newText.toString()
                        loadUsers()
                    }
                    else -> {
                        queryValue = null
                        filterBody = null
                        loadUsers()
                    }
                }
                return true
            }

            override fun onQueryTextSubmit(newText: String?): Boolean {
                when {
                    newText.toString().length >= 3 -> {
                        filterBody = null
                        queryValue = newText.toString()
                        loadUsers()
                    }
                    else -> {
                        queryValue = null
                        filterBody = null
                        loadUsers()
                    }
                }
                return true
            }
        }
        requireActivity().search.setOnQueryTextListener(queryTextListener)

        bottomSheetBehavior = BottomSheetBehavior.from(bottomsheet_filter)
        bottomSheetBehavior.setBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {}
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_COLLAPSED -> {
                        hideKeyboardFrom(requireContext(), requireActivity().search)
                        requireActivity().navigationView.visibility = View.VISIBLE
                        lay_main.alpha = 1.0f
                    }
                    BottomSheetBehavior.STATE_HIDDEN -> {

                    }
                    BottomSheetBehavior.STATE_EXPANDED -> {
                        hideKeyboardFrom(requireContext(), requireActivity().search)
                        requireActivity().navigationView.visibility = View.GONE
                        lay_main.alpha = 0.5f
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

        edt_city.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(value: CharSequence?, arg1: Int, arg2: Int, arg3: Int) {
                if (value.toString().length >= 3) {
                    searchCities(value.toString())
                } else {
                    listOfCities.clear()
                    adapter?.clear()
                    adapter?.notifyDataSetChanged()
                }
            }

            override fun beforeTextChanged(arg0: CharSequence?, arg1: Int, arg2: Int, arg3: Int) {
            }

            override fun afterTextChanged(arg0: Editable?) {
            }
        })

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
                    requireActivity(),
                    getString(R.string.shaka)
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
                    requireActivity(),
                    getString(R.string.vedha_adhyayanam)
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
                    resources.getStringArray(R.array.shastra_adhyayanam).toMutableList(),
                    requireActivity(),
                    getString(R.string.shastra_adhyayanam)
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
                    requireActivity(),
                    getString(R.string.prayogam)
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
            filterBody = FilterBody(
                city = edt_city.text.toString(),
                mother_tongue = edt_mothertongue.text.toString(),
                marital_status = edt_marital_status.text.toString(),
                prayogam = prayogamList,
                shastra_adhyayanam = shastraAdhyayanamList,
                shadanga_adhyayanam = edt_shadanga_adhyayanam.text.toString(),
                vedha_adhyayanam = vedhaAdhyayanamList,
                shakha = shakhaList,
                samprdhayam = edt_sampradhayam.text.toString(),
                vedham = edt_vedham.text.toString()
            )
            loadUsers()
        }

        btn_clear.setOnClickListener {
            if (bottomSheetBehavior.state != BottomSheetBehavior.STATE_COLLAPSED) {
                bottomSheetBehavior.state =
                    BottomSheetBehavior.STATE_COLLAPSED
            }
            filterBody = null
            loadUsers()
        }

        loadUsers()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (vunityUsers != null) {
            vunityUsers?.cancel()
        }
    }

    override fun onStop() {
        super.onStop()
        if (vunityUsers != null) {
            vunityUsers?.cancel()
        }
    }

    companion object {
        fun newInstance(): VunityUsers = VunityUsers()
    }

    override fun onBackPressed(): Boolean {
        return false
    }

    private fun loadUsers() {
        if (internetDetector?.checkMobileInternetConn(requireActivity())!!) {
            if (!lay_shimmer.isShimmerStarted) {
                lay_shimmer.startShimmer()
            }
            vunityUsers = when {
                filterBody != null -> {
                    RetrofitClient.instanceClient.filterVunityUsers(filterBody = filterBody!!)
                }
                queryValue != null -> {
                    RetrofitClient.instanceClient.searchVunityUsers(queryValue.toString())
                }
                else -> {
                    RetrofitClient.instanceClient.listOfVunityUsers()
                }
            }
            vunityUsers?.enqueue(object : Callback<VunityListDto> {
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

    private fun searchCities(value: String) {
        if (internetDetector?.checkMobileInternetConn(requireContext())!!) {
            try {
                val requestOtp = RetrofitClient.instanceClient.searchCities(value)
                requestOtp.enqueue(object : Callback<CityDto> {
                    @SuppressLint("SimpleDateFormat")
                    @RequiresApi(Build.VERSION_CODES.O)
                    override fun onResponse(
                        call: Call<CityDto>,
                        response: Response<CityDto>
                    ) {
                        if (response.code() == 200) {
                            when (response.body()?.status) {
                                200 -> {
                                    listOfCities.clear()
                                    adapter = ArrayAdapter(
                                        requireActivity(),
                                        android.R.layout.select_dialog_item,
                                        listOfCities
                                    )
                                    for (i in response.body()?.data!!) {
                                        listOfCities.add(i.city.toString())
                                    }
                                    if (listOfCities.size < 40) edt_city.threshold = 1
                                    else edt_city.threshold = 2
                                    edt_city.setAdapter(adapter)
                                    adapter?.notifyDataSetChanged()
                                    Log.e("listOfCities", listOfCities.toString())
                                }
                                204 -> {
                                    listOfCities.clear()
                                    adapter?.clear()
                                    adapter?.notifyDataSetChanged()
                                }
                                else -> {
                                    coordinatorErrorMessage(
                                        layout_refresh,
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

                        } else if (response.code() == 401) {
                            sessionExpired(
                                requireActivity()
                            )
                        } else {
                            coordinatorErrorMessage(
                                layout_refresh,
                                response.message()
                            )
                        }
                    }

                    override fun onFailure(call: Call<CityDto>, t: Throwable) {
                        Log.e("onResponse", t.message.toString())
                        coordinatorErrorMessage(
                            layout_refresh,
                            getString(R.string.msg_something_wrong)
                        )
                    }
                })

            } catch (e: Exception) {
                Log.d("ParseException", e.toString())
                e.printStackTrace()
            }
        } else {
            coordinatorErrorMessage(
                layout_refresh,
                getString(R.string.msg_no_internet)
            )
        }
    }
}
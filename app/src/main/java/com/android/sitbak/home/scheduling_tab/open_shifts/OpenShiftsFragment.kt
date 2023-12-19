package com.android.sitbak.home.scheduling_tab.open_shifts

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.format.DateFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.sitbak.R
import com.android.sitbak.base.AppController
import com.android.sitbak.home.maps.DeliveryZoneActivity
import com.android.sitbak.base.ViewModelFactory
import com.android.sitbak.databinding.FragmentOpenShiftsBinding
import com.android.sitbak.home.scheduling_tab.WeekDayAdapter
import com.android.sitbak.home.scheduling_tab.WeekDaysClickListener
import com.android.sitbak.network.Api
import com.android.sitbak.network.ApiTags
import com.android.sitbak.network.RetrofitClient
import com.android.sitbak.utils.*
import com.android.sitbak.utils.local_models.CalendarModel
import com.google.gson.Gson
import okhttp3.ResponseBody
import retrofit2.Call
import java.util.*
import kotlin.collections.ArrayList


class OpenShiftsFragment : Fragment() {
    private lateinit var binding: FragmentOpenShiftsBinding
    private lateinit var openShiftsAdapter: OpenShiftsAdapter
    private lateinit var weekDayAdapter: WeekDayAdapter
    private var openShiftsList: MutableList<OpenShiftsData?> = ArrayList()
    private val days = ArrayList<CalendarModel>()
    var selectedDate = ""
    var selectedShiftId = -1

    private lateinit var viewModel: OpenShiftsFragmentVM
    private lateinit var mContext: Context
    private lateinit var retrofitClient: Api
    private lateinit var apiCall: Call<ResponseBody>

    private var skip = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_open_shifts,
            container,
            false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mContext = requireContext()
        retrofitClient = RetrofitClient.getClient(mContext).create(Api::class.java)

        initVM()
        initObservers()
        weekDaysAdapter()
        initAdapter()
        initListeners()

    }

    private fun getOpenShifts(date: String) {
        apiCall = retrofitClient.getOpenShifts(date)
        viewModel.getOpenShifts(apiCall)
    }

    private fun addOpenShifts(id: Int) {
        apiCall = retrofitClient.addAvailabilityFromShifts(id)
        viewModel.addOpenShifts(apiCall)
    }

    private fun initVM() {
        viewModel = ViewModelProviders.of(this, ViewModelFactory()).get(OpenShiftsFragmentVM::class.java)
        binding.viewModel = this.viewModel

        binding.executePendingBindings()
        binding.lifecycleOwner = this
    }

    private fun initObservers() {
        viewModel.getApiResponse().observe(requireActivity(), {
            when (it.status) {
                ApiStatus.LOADING -> {
                    Loader.showLoader(mContext)
                    Loader.progressKHUD?.setCancellable {
                        if (this::apiCall.isInitialized)
                            apiCall.cancel()
                    }
                }
                ApiStatus.ERROR -> {
                    Loader.hideLoader()
                    AppUtils.showToast(requireActivity(), it.message!!, false)
                }
                ApiStatus.SUCCESS -> {
                    Loader.hideLoader()
                    when (it.tag) {
                        ApiTags.GET_OPEN_SHIFTS -> {
                            val model = Gson().fromJson(it.data.toString(), OpenShiftsModel::class.java)
                            handleResponse(model)
                        }
                        ApiTags.ADD_OPEN_SHIFTS -> {
                            AppUtils.showToast(requireActivity(), it.data?.getString("message")!!, true)
                        }

                    }
                }
            }
        })
    }

    private fun handleResponse(model: OpenShiftsModel) {
        binding.pullToRefresh.isRefreshing = false
        val list = model.data
        if (list.isNullOrEmpty()) {
            binding.llNoData.viewVisible()
            binding.rvScheduleShift.viewGone()
            binding.tvDateView.viewInVisible()
        } else {


//            if (openShiftsList.size > 0)
//                openShiftsList.removeAt(openShiftsList.size - 1)

            openShiftsList.addAll(list)
//
//            if (list.size >= AppController.pageCount) {
//                skip += AppController.pageCount
//                openShiftsList.add(null)
//            }

            if (openShiftsList.isEmpty()) {
                binding.rvScheduleShift.viewGone()
                binding.tvDateView.viewVisible()
                binding.llNoData.viewVisible()
            } else {
                binding.tvDateView.viewInVisible()
                binding.llNoData.viewGone()
                binding.rvScheduleShift.viewVisible()
            }
            openShiftsAdapter.notifyDataSetChanged()
        }
    }

    private fun initListeners() {
        binding.pullToRefresh.setOnRefreshListener {
            openShiftsList.clear()
            getOpenShifts(selectedDate)
        }
    }

    private fun initAdapter() {
        openShiftsAdapter = OpenShiftsAdapter(selectedShiftId, requireContext(), openShiftsList, object :
            OpenShiftsAdapter.OpenShiftAdapterClickListener {
            override fun onLocationClicked(position: Int, model: OpenShiftsData) {
                selectedShiftId = model.id
                val intent = Intent(mContext, DeliveryZoneActivity::class.java)
                intent.putExtra("type", "openShifts")
                intent.putExtra("data", model)
                startActivity(intent)
            }

            override fun onAcceptShiftClicked(position: Int, model: OpenShiftsData) {
                selectedShiftId = model.id
                openShiftsAdapter.notifyDataSetChanged()
                addOpenShifts(selectedShiftId)
            }

            override fun onLoadMoreClicked() {
                getOpenShifts(selectedDate)
            }

        })
        binding.rvScheduleShift.layoutManager = LinearLayoutManager(context)
        binding.rvScheduleShift.adapter = openShiftsAdapter

    }

    @SuppressLint("SetTextI18n")
    private fun weekDaysAdapter() {
        val calendar = Calendar.getInstance()
        val today = Calendar.getInstance()
        calendar.firstDayOfWeek = Calendar.MONDAY
        calendar[Calendar.DAY_OF_WEEK] = Calendar.MONDAY
        var toDayAdded = false
        //  val days = arrayOfNulls<String>(7)
        for (i in 0..6) {
            //days[i] = format.format(calendar.time)
            if (calendar.get(Calendar.DAY_OF_MONTH) == today.get(Calendar.DAY_OF_MONTH)) {
                toDayAdded = true
                days.add(CalendarModel(calendar.time, isEnabled = true, isSelected = true))

                val dayOfTheWeek = DateFormat.format("EEEE", days[i].date) as String // Thursday
                val day = DateFormat.format("dd", days[i].date) as String // 20
                val monthString = DateFormat.format("MMM", days[i].date) as String // Jun
                val monthNumber = DateFormat.format("MM", days[i].date) as String // 06
                val year = DateFormat.format("yyyy", days[i].date) as String // 2013

                selectedDate = "$year-$monthNumber-$day"
                binding.tvDateView.text = "$monthString $day, $dayOfTheWeek"

            } else {
                if (toDayAdded)
                    days.add(CalendarModel(calendar.time, isEnabled = true, isSelected = false))
                else
                    days.add(CalendarModel(calendar.time, isEnabled = true, isSelected = false))
            }

            calendar.add(Calendar.DAY_OF_MONTH, 1)
            Log.i("adsf", "" + days[i])
        }

        weekDayAdapter = WeekDayAdapter(requireContext(), days, object : WeekDaysClickListener {
            override fun onWeekClick(position: Int, item: CalendarModel) {
                if (!item.isSelected) {
                    days.forEachIndexed { _, calendarModel ->
                        calendarModel.isSelected = false
                    }
                    days[position].isSelected = !days[position].isSelected
                    weekDayAdapter.setItems(days)
                    weekDayAdapter.notifyDataSetChanged()
                    val dayOfTheWeek = DateFormat.format("EEEE", item.date) as String // Thursday
                    val day = DateFormat.format("dd", item.date) as String // 20
                    val monthString = DateFormat.format("MMMM", item.date) as String // Jun
                    val monthNumber = DateFormat.format("MM", item.date) as String // 06
                    val year = DateFormat.format("yyyy", item.date) as String // 2013

                    selectedDate = "$year-$monthNumber-$day"
                    binding.tvDateView.text = "$monthString $day, $dayOfTheWeek"

                    if (selectedDate != "") {
                        openShiftsList.clear()
                        getOpenShifts(selectedDate)
                    }
                }
            }
        })

        binding.rvWeekDays.adapter = weekDayAdapter

    }


    companion object {
        fun getInstance(): OpenShiftsFragment {
            val openShiftsFragment = OpenShiftsFragment()

            return openShiftsFragment
        }
    }

    override fun onResume() {
        super.onResume()
        openShiftsList.clear()
        getOpenShifts(selectedDate)
    }
}
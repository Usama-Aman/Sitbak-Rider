package com.android.sitbak.home.scheduling_tab.availability

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.format.DateFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil.inflate
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.android.sitbak.R
import com.android.sitbak.home.maps.DeliveryZoneActivity
import com.android.sitbak.base.ViewModelFactory
import com.android.sitbak.databinding.FragmentAvailibiltyBinding
import com.android.sitbak.home.maps.AddAvailabilityActivity
import com.android.sitbak.home.popups.DeleteShiftPopup
import com.android.sitbak.home.scheduling_tab.AdjustableScheduleAdapter
import com.android.sitbak.home.scheduling_tab.AvailabilityAdapterClickListener
import com.android.sitbak.home.scheduling_tab.WeekDayAdapter
import com.android.sitbak.home.scheduling_tab.WeekDaysClickListener
import com.android.sitbak.home.scheduling_tab.my_shifts.AvailabilitiesData
import com.android.sitbak.home.scheduling_tab.my_shifts.AvailabilitiesModel
import com.android.sitbak.home.scheduling_tab.my_shifts.MyShiftsFragmentVM
import com.android.sitbak.network.Api
import com.android.sitbak.network.ApiTags
import com.android.sitbak.network.RetrofitClient
import com.android.sitbak.utils.*
import com.android.sitbak.utils.local_models.CalendarModel
import com.astritveliu.boom.Boom
import com.google.gson.Gson
import okhttp3.ResponseBody
import retrofit2.Call
import java.util.*
import kotlin.collections.ArrayList

class AvailabilitiesFragment : Fragment() {
    lateinit var binding: FragmentAvailibiltyBinding
    private lateinit var adjustableScheduleAdapter: AdjustableScheduleAdapter
    private lateinit var weekDayAdapter: WeekDayAdapter
    private var availabilityList: MutableList<AvailabilitiesData> = ArrayList()
    private var selectedPosition = -1
    private val days = ArrayList<CalendarModel>()
    var selectedDate = ""

    private lateinit var viewModel: MyShiftsFragmentVM
    private lateinit var mContext: Context
    private lateinit var retrofitClient: Api
    private lateinit var apiCall: Call<ResponseBody>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = inflate(
            inflater,
            R.layout.fragment_availibilty,
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
                val monthString = DateFormat.format("MMMM", days[i].date) as String // Jun
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

                    binding.tvDateView.text = "$monthString $day, $dayOfTheWeek"
                    selectedDate = "$year-$monthNumber-$day"

                    if (selectedDate != "") {
                        availabilityList.clear()
                        getAvailabilities(selectedDate)
                    }
                }
            }
        })

        binding.rvWeekDays.adapter = weekDayAdapter

    }

    private fun getAvailabilities(date: String) {
        apiCall = retrofitClient.getAvailabilities(date)
        viewModel.getAvailabilities(apiCall)
    }

    private fun initVM() {
        viewModel = ViewModelProviders.of(this, ViewModelFactory()).get(MyShiftsFragmentVM::class.java)
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
                        ApiTags.GET_AVAILABILITIES -> {
                            val model = Gson().fromJson(it.data.toString(), AvailabilitiesModel::class.java)
                            handleResponse(model)
                        }
                        ApiTags.DELETE_SHIFTS -> {
                            AppUtils.showToast(requireActivity(), it.data?.getString("message")!!, true)
                            availabilityList.removeAt(selectedPosition)
                            adjustableScheduleAdapter.notifyDataSetChanged()
                            selectedPosition = -1
                        }
                    }
                }
            }
        })
    }

    private fun handleResponse(model: AvailabilitiesModel) {
        binding.pullToRefresh.isRefreshing = false
        val list = model.data
        if (list.isNullOrEmpty()) {
            binding.llNoData.viewVisible()
            binding.tvDateView.viewInVisible()
            binding.rvScheduleShift.viewGone()
        } else {
            binding.tvDateView.viewVisible()
            binding.llNoData.viewGone()
            binding.rvScheduleShift.viewVisible()
            availabilityList.addAll(list)
            adjustableScheduleAdapter.notifyDataSetChanged()
        }
    }

    private fun deleteAvailabilityItem(position: Int) {
        apiCall = retrofitClient.deleteShift(availabilityList[position].id)
        viewModel.deleteShifts(apiCall)
    }

    private fun initListeners() {
        Boom(binding.btnAddAvailability)
        binding.pullToRefresh.setOnRefreshListener {
            availabilityList.clear()
            getAvailabilities(selectedDate)
        }
        binding.btnAddAvailability.setOnClickListener {
            val intent = Intent(mContext, AddAvailabilityActivity::class.java)
            intent.putExtra("type", "addAvailability")
            startActivity(intent)
        }
    }

    private fun initAdapter() {
        adjustableScheduleAdapter = AdjustableScheduleAdapter(requireContext(), availabilityList, object : AvailabilityAdapterClickListener {
            override fun onDeleteItemClicked(position: Int, data: AvailabilitiesData) {
                selectedPosition = position

                val dialog = DeleteShiftPopup(availabilityList[position])
                dialog.setListener(object : DeleteShiftPopup.DeleteShiftPopUpClickListener {
                    override fun onDeleteClicked() {
                        deleteAvailabilityItem(selectedPosition)
                    }
                })
                dialog.show(childFragmentManager, "DeleteShiftPopup")
            }

            override fun onEditClicked(position: Int, data: AvailabilitiesData) {
                selectedPosition = position
                val intent = Intent(mContext, AddAvailabilityActivity::class.java)
                intent.putExtra("data", data)
                intent.putExtra("type", "editAvailability")
                startActivity(intent)
            }

            override fun onLocationClicked(position: Int, data: AvailabilitiesData) {
                selectedPosition = position
                val intent = Intent(mContext, DeliveryZoneActivity::class.java)
                intent.putExtra("data", data)
                intent.putExtra("type", "availabilities")
                startActivity(intent)
            }

        })
        binding.rvScheduleShift.adapter = adjustableScheduleAdapter
    }

    companion object {
        fun getInstance(): AvailabilitiesFragment {
            val availabilitiesFragment = AvailabilitiesFragment()
            return availabilitiesFragment
        }
    }

    override fun onResume() {
        super.onResume()
        availabilityList.clear()
        getAvailabilities(selectedDate)
    }
}
package com.android.sitbak.home.completed_shifts

import android.content.Context
import android.os.Bundle
import androidx.lifecycle.ViewModelProviders
import com.android.sitbak.R
import com.android.sitbak.base.BaseActivity
import com.android.sitbak.base.ViewModelFactory
import com.android.sitbak.databinding.ActivityScheduleShiftsBinding
import com.android.sitbak.network.Api
import com.android.sitbak.network.ApiTags
import com.android.sitbak.network.RetrofitClient
import com.android.sitbak.utils.*
import com.google.gson.Gson
import okhttp3.ResponseBody
import retrofit2.Call

class CompletedShiftsActivity : BaseActivity() {

    private lateinit var binding: ActivityScheduleShiftsBinding
    private lateinit var completedShiftAdapter: CompletedShiftsAdapter
    private lateinit var viewModel: CompletedShiftsVM
    private lateinit var mContext: Context
    private lateinit var retrofitClient: Api
    private lateinit var apiCall: Call<ResponseBody>

    private var completedShiftsData: MutableList<CompletedShiftsData?> = ArrayList()

    private var skip = 0
    private var selectedDate = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScheduleShiftsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mContext = this
        retrofitClient = RetrofitClient.getClient(mContext).create(Api::class.java)

        changeStatusBarColor(R.color.heavy_metal)

        initVM()
        initObservers()
        initViews()
        initAdapter()
    }

    private fun initVM() {
        viewModel = ViewModelProviders.of(this, ViewModelFactory()).get(CompletedShiftsVM::class.java)
        binding.viewModel = this.viewModel

        binding.executePendingBindings()
        binding.lifecycleOwner = this
    }

    private fun initObservers() {
        viewModel.getApiResponse().observe(this, {
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
                    AppUtils.showToast(this, it.message!!, false)
                }
                ApiStatus.SUCCESS -> {
                    Loader.hideLoader()
                    when (it.tag) {
                        ApiTags.GET_COMPLETED_SHIFTS -> {
                            val model = Gson().fromJson(it.data.toString(), CompletedShiftsModel::class.java)
                            handleShiftsResponse(model.data)
                        }
                    }
                }
            }
        })
    }

    private fun handleShiftsResponse(data: List<CompletedShiftsData>) {
        if (completedShiftsData.size > 0)
            completedShiftsData.removeAt(completedShiftsData.size - 1)

        completedShiftsData.addAll(data)

//            if (data.size >= AppController.pageCount) {
//                skip += AppController.pageCount
//                completedShiftsData.add(null)
//            }

        if (completedShiftsData.isEmpty()) {
            binding.shiftsRecycler.viewGone()
            binding.llNoData.viewVisible()
        } else {
            binding.llNoData.viewGone()
            binding.shiftsRecycler.viewVisible()
        }

        completedShiftAdapter.notifyDataSetChanged()

    }

    private fun initViews() {
        selectedDate = intent?.getStringExtra("date")!!
        binding.tvTitle.text = selectedDate

        binding.ivBack.setOnClickListener { finish() }

        getCompletedShifts()
    }

    private fun getCompletedShifts() {
        apiCall = retrofitClient.getCompletedDeliveries(selectedDate, skip)
        viewModel.getCompletedShifts(apiCall)
    }

    private fun initAdapter() {
        completedShiftAdapter = CompletedShiftsAdapter(completedShiftsData,
            object : CompletedShiftsAdapter.CompletedShiftsInterface {
                override fun onLoadMoreClicked() {
                    getCompletedShifts()
                }
            })
        binding.shiftsRecycler.adapter = completedShiftAdapter
    }

}


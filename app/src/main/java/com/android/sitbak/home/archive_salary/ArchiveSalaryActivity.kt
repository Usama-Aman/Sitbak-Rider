package com.android.sitbak.home.archive_salary

import android.content.Context
import android.os.Bundle
import androidx.lifecycle.ViewModelProviders
import com.android.sitbak.R
import com.android.sitbak.base.BaseActivity
import com.android.sitbak.base.ViewModelFactory
import com.android.sitbak.databinding.ActivityArchiveSalaryBinding
import com.android.sitbak.network.Api
import com.android.sitbak.network.ApiTags
import com.android.sitbak.network.RetrofitClient
import com.android.sitbak.utils.*
import com.google.gson.Gson
import okhttp3.ResponseBody
import retrofit2.Call

class ArchiveSalaryActivity : BaseActivity() {

    private lateinit var binding: ActivityArchiveSalaryBinding
    private lateinit var archiveSalaryAdapter: ArchiveSalaryAdapter
    private val archiveSalaryData: MutableList<ArchiveSalaryData?> = ArrayList()
    private lateinit var viewModel: ArchiveSalaryVM
    private lateinit var mContext: Context
    private lateinit var retrofitClient: Api
    private lateinit var apiCall: Call<ResponseBody>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityArchiveSalaryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mContext = this
        retrofitClient = RetrofitClient.getClient(mContext).create(Api::class.java)

        changeStatusBarColor(R.color.heavy_metal)

        initVM()
        initObservers()
        initViews()
        initAdapter()

        getArchiveSalary()
    }

    private fun getArchiveSalary() {
        apiCall = retrofitClient.getArchiveSalary()
        viewModel.getArchiveSalary(apiCall)
    }


    private fun initVM() {
        viewModel = ViewModelProviders.of(this, ViewModelFactory()).get(ArchiveSalaryVM::class.java)
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
                        ApiTags.GET_ARCHIVE_SALARY -> {
                            val model = Gson().fromJson(it.data.toString(), ArchiveSalaryModel::class.java)
                            handleShiftsResponse(model.data)
                        }
                    }
                }
            }
        })
    }

    private fun handleShiftsResponse(data: List<ArchiveSalaryData>) {
        if (archiveSalaryData.size > 0)
            archiveSalaryData.removeAt(archiveSalaryData.size - 1)

        archiveSalaryData.addAll(data)

        if (archiveSalaryData.isEmpty()) {
            binding.rvArchiveSalary.viewGone()
            binding.llNoData.viewVisible()
        } else {
            binding.llNoData.viewGone()
            binding.rvArchiveSalary.viewVisible()
        }

        archiveSalaryAdapter.notifyDataSetChanged()

    }


    private fun initAdapter() {
        archiveSalaryAdapter = ArchiveSalaryAdapter(archiveSalaryData, object : ArchiveSalaryAdapter.ArchiveSalaryInterface {
            override fun onLoadMoreClicked() {

            }
        })
        binding.rvArchiveSalary.adapter = archiveSalaryAdapter
    }

    private fun initViews() {
        binding.ivBack.setOnClickListener {
            super.onBackPressed()
        }
    }
}



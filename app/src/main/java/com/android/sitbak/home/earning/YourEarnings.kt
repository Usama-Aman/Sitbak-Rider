package com.android.sitbak.home.earning

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import com.android.sitbak.R
import com.android.sitbak.base.BaseActivity
import com.android.sitbak.base.ViewModelFactory
import com.android.sitbak.databinding.ActivityYourEarningsBinding
import com.android.sitbak.databinding.LayoutEarningBinding
import com.android.sitbak.home.archive_salary.ArchiveSalaryActivity
import com.android.sitbak.home.completed_shifts.CompletedShiftsActivity
import com.android.sitbak.network.Api
import com.android.sitbak.network.ApiTags
import com.android.sitbak.network.RetrofitClient
import com.android.sitbak.utils.ApiStatus
import com.android.sitbak.utils.AppUtils
import com.android.sitbak.utils.J_startActivity
import com.android.sitbak.utils.Loader
import com.astritveliu.boom.Boom
import com.google.gson.Gson
import okhttp3.ResponseBody
import retrofit2.Call

class YourEarnings : BaseActivity() {
    private lateinit var adapter: YourEarningsAdapter
    lateinit var binding: ActivityYourEarningsBinding
    private lateinit var viewModel: YourEarningsVM
    private lateinit var mContext: Context
    private lateinit var retrofitClient: Api
    private lateinit var apiCall: Call<ResponseBody>
    private var yourEarnings: MutableList<Earning> = ArrayList()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityYourEarningsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mContext = this
        retrofitClient = RetrofitClient.getClient(mContext).create(Api::class.java)

        changeStatusBarColor(R.color.heavy_metal)

        initVM()
        initObservers()
        listener()
        setRecycler()

        getYourEarnings()
    }

    private fun getYourEarnings() {
        apiCall = retrofitClient.getYourEarnings()
        viewModel.getYourEarnings(apiCall)
    }

    private fun initVM() {
        viewModel = ViewModelProviders.of(this, ViewModelFactory()).get(YourEarningsVM::class.java)
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
                        ApiTags.GET_YOUR_EARNINGS -> {
                            val model = Gson().fromJson(it.data.toString(), YourEarningModel::class.java)
                            handleEarningsResponse(model)
                        }
                        ApiTags.GET_MONEY -> {
                            AppUtils.showToast(this, it.data?.getString("message")!!, true)
                            Handler(Looper.getMainLooper()).postDelayed({
                                yourEarnings.clear()
                                getYourEarnings()
                            }, 1000)
                        }
                    }
                }
            }
        })
    }

    private fun handleEarningsResponse(data: YourEarningModel) {
        binding.pullToRefresh.isRefreshing = false
        val loginData = AppUtils.getUserDetails(this)
        loginData.available_earning = data.data.available_earning
        AppUtils.saveUserModel(mContext, loginData)

        binding.tvEarned.text = "$${data.data.available_earning}"
        if (data != null) {
            val list = data.data.earning
            yourEarnings.addAll(list)
            adapter.notifyDataSetChanged()
        }
    }

    private fun setRecycler() {
        adapter = YourEarningsAdapter(mContext, yourEarnings)
        binding.rvEarnings.adapter = adapter
    }

    private fun listener() {
        Boom(binding.tvGetMoney)
        binding.tvGetMoney.setOnClickListener {
            apiCall = retrofitClient.getMoney()
            viewModel.getMoney(apiCall)
        }

        binding.pullToRefresh.setOnRefreshListener {
            yourEarnings.clear()
            getYourEarnings()
        }

        binding.ivBack.setOnClickListener {
            super.onBackPressed()
        }

        binding.tvArchive.setOnClickListener {
            J_startActivity(ArchiveSalaryActivity::class.java)
        }

    }
}

class YourEarningsAdapter(var context: Context, val list: MutableList<Earning>) : RecyclerView.Adapter<YourEarningsAdapter.ViewHolder>() {

    private lateinit var mContext: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        mContext = parent.context
        val binding: LayoutEarningBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context), R.layout.layout_earning, parent, false
        )

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBind(list[position])
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class ViewHolder(var binding: LayoutEarningBinding) : RecyclerView.ViewHolder(binding.root) {
        fun onBind(model: Earning) {
            binding.tvDate.text = model.date
            binding.tvMoney.text = "$${model.value}"

            binding.parentLayout.setOnClickListener {
                val intent = Intent(mContext, CompletedShiftsActivity::class.java)
                intent.putExtra("date", model.date)
                mContext.startActivity(intent)
            }
        }
    }

}
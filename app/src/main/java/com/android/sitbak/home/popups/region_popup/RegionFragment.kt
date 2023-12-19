package com.android.sitbak.home.popups.region_popup

import android.content.Context
import android.graphics.Color
import android.graphics.Point
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProviders
import com.android.sitbak.base.ViewModelFactory
import com.android.sitbak.databinding.LayoutRegionBinding
import com.android.sitbak.network.Api
import com.android.sitbak.network.ApiTags
import com.android.sitbak.network.RetrofitClient
import com.android.sitbak.utils.ApiStatus
import com.android.sitbak.utils.AppUtils
import com.android.sitbak.utils.Loader
import com.google.gson.Gson
import okhttp3.ResponseBody
import retrofit2.Call

class RegionFragment : DialogFragment() {
    private lateinit var binding: LayoutRegionBinding
    private lateinit var adapter: RegionAdapter
    private var regionList: MutableList<RegionData> = ArrayList()
    private var selectedRegion: RegionData? = null
    private lateinit var regionFragmentClickListener: RegionFragmentClickListener

    private lateinit var viewModel: RegionFragmentVM
    private lateinit var mContext: Context
    private lateinit var retrofitClient: Api
    private lateinit var apiCall: Call<ResponseBody>

    override fun onResume() {
        val window: Window? = dialog!!.window
        val size = Point()
        // Store dimensions of the screen in `size`
        // Store dimensions of the screen in `size`
        val display: Display = window!!.windowManager.defaultDisplay
        display.getSize(size)
        // Set the width of the dialog proportional to 75% of the screen width
        // Set the width of the dialog proportional to 75% of the screen width
        window.setLayout((size.x * 0.90).toInt(), WindowManager.LayoutParams.WRAP_CONTENT)
        window.setGravity(Gravity.CENTER)
        // Call super onResume after sizing
        // Call super onResume after sizing
        super.onResume()
    }

    companion object {

        var selectedZoneId = -1

        fun getInstance(selectedZoneId: Int): RegionFragment {
            var jProgressDialog = RegionFragment()
            this.selectedZoneId = selectedZoneId
            return jProgressDialog
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = LayoutRegionBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        mContext = requireContext()
        retrofitClient = RetrofitClient.getClient(mContext).create(Api::class.java)

        initVM()
        initObservers()
        setRecycler()
        getRegions()
        initViews()
        initListener()

    }

    private fun initViews() {

    }

    private fun getRegions() {
        apiCall = retrofitClient.getRegions()
        viewModel.getRegions(apiCall)
    }

    private fun initVM() {
        viewModel = ViewModelProviders.of(this, ViewModelFactory()).get(RegionFragmentVM::class.java)
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
                    AppUtils.showToast(requireActivity(), it.message!!, false)
                }
                ApiStatus.SUCCESS -> {
                    Loader.hideLoader()
                    when (it.tag) {
                        ApiTags.GET_REGION -> {
                            val model = Gson().fromJson(it.data.toString(), RegionModel::class.java)
                            handleRegionResponse(model)
                        }
                    }
                }
            }
        })
    }

    private fun handleRegionResponse(data: RegionModel) {
        val list = data.data
        regionList.addAll(list)
        adapter.notifyDataSetChanged()
    }

    private fun initListener() {
        binding.btnOK.setOnClickListener {
            if (selectedRegion != null) {
                regionFragmentClickListener.onOkClicked(selectedRegion)
                dismiss()
            } else {
                AppUtils.showToast(requireActivity(), "Region is not selected!", false)
            }
        }
    }

    private fun setRecycler() {
        adapter = RegionAdapter(selectedZoneId, requireContext(), regionList)
        binding.rvRegion.adapter = adapter

        adapter.setAdapterListener(object : RegionClickListener {
            override fun onItemClicked(position: Int, data: RegionData) {
                selectedRegion = data
                adapter.notifyDataSetChanged()
            }

        })

    }

    fun setListener(listener: RegionFragmentClickListener) {
        regionFragmentClickListener = listener
    }

    interface RegionFragmentClickListener {
        fun onOkClicked(selectedRegionData: RegionData?)
    }
}


package com.android.sitbak.home.tickets_tab.available

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.android.sitbak.R
import com.android.sitbak.base.AppController
import com.android.sitbak.base.ViewModelFactory
import com.android.sitbak.databinding.FragmentAvailableTicketsBinding
import com.android.sitbak.home.popups.DeliveryDetailsPopup
import com.android.sitbak.home.popups.OrderClientInfo
import com.android.sitbak.home.popups.TimeAndPaymentPopUp
import com.android.sitbak.home.tickets_tab.TicketsData
import com.android.sitbak.home.tickets_tab.TicketsFragment
import com.android.sitbak.home.tickets_tab.TicketsModel
import com.android.sitbak.network.Api
import com.android.sitbak.network.ApiTags
import com.android.sitbak.network.RetrofitClient
import com.android.sitbak.network.SocketIO
import com.android.sitbak.utils.*
import com.google.gson.Gson
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call


class AvailableTicketsFragment : Fragment(), AvailableTicketsAdapter.AvailableOrderInterface {

    private lateinit var availableTicketsAdapter: AvailableTicketsAdapter
    private lateinit var binding: FragmentAvailableTicketsBinding
    private lateinit var viewModel: AvailableTicketsVM
    private lateinit var mContext: Context
    private lateinit var retrofitClient: Api
    private lateinit var apiCall: Call<ResponseBody>

    private var availableTicketsData: MutableList<TicketsData?> = ArrayList()

    private var skip = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_available_tickets, container, false)
        mContext = requireContext()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        retrofitClient = RetrofitClient.getClient(mContext).create(Api::class.java)

        initVM()
        initObservers()
        initViews()
        initListeners()
        initAdapter()

    }

    private fun getAvailableTickets() {
        apiCall = retrofitClient.getAvailableTickets(skip)
        viewModel.getAvailableTickets(apiCall)
    }


    private fun initVM() {
        viewModel = ViewModelProviders.of(this, ViewModelFactory()).get(AvailableTicketsVM::class.java)
        binding.viewModel = viewModel

        binding.executePendingBindings()
        binding.lifecycleOwner = this
    }

    private fun initObservers() {
        viewModel.getApiResponse().observe(viewLifecycleOwner, {
            when (it.status) {
                ApiStatus.LOADING -> {
                    if (it.tag != ApiTags.GET_AVAILABLE_TICKETS)
                        Loader.showLoader(mContext)
                    Loader.progressKHUD?.setCancellable {
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
                        ApiTags.GET_AVAILABLE_TICKETS -> {
                            val model = Gson().fromJson(it.data.toString(), TicketsModel::class.java)
                            handleTicketsResponse(model.data, model.count)
                        }
                        ApiTags.ACCEPT_ORDER -> {
                            AppUtils.showToast(requireActivity(), it.data?.getString("message")!!, true)
                            Handler(Looper.getMainLooper()).postDelayed({
                                (parentFragment as TicketsFragment).goToProcessingTab()
                            }, 1000)
                        }
                    }
                }
            }
        })
    }

    private fun handleTicketsResponse(data: List<TicketsData>, count: Int) {
        binding.pullToRefresh.isRefreshing = false
        (parentFragment as TicketsFragment).updateAvailableCounts(count)

        if (availableTicketsData.size > 0)
            availableTicketsData.removeAt(availableTicketsData.size - 1)

        availableTicketsData.addAll(data)

        if (data.size >= AppController.pageCount) {
            skip += AppController.pageCount
            availableTicketsData.add(null)
        }

        if (availableTicketsData.isEmpty()) {
            binding.availableRecyclerView.viewGone()
            binding.llNoData.viewVisible()
        } else {
            binding.llNoData.viewGone()
            binding.availableRecyclerView.viewVisible()
        }

        availableTicketsAdapter.notifyDataSetChanged()

    }


    private fun initViews() {

    }

    private fun initListeners() {

        binding.pullToRefresh.setOnRefreshListener {
            availableTicketsData.clear()
            skip = 0
            getAvailableTickets()
        }
    }

    private fun initAdapter() {
        availableTicketsAdapter = AvailableTicketsAdapter(this, availableTicketsData)
        binding.availableRecyclerView.adapter = availableTicketsAdapter
    }

    override fun onAcceptOrder(position: Int) {
        apiCall = retrofitClient.acceptOrder(availableTicketsData[position]?.id!!)
        viewModel.acceptOrder(apiCall)

        val userData = AppUtils.getUserDetails(mContext)
        val jsonObject = JSONObject()
            .put("user_id", availableTicketsData[position]?.user?.id)
            .put("order_id", availableTicketsData[position]?.id)
            .put("store_id", availableTicketsData[position]?.store?.id)
            .put("sender_name", userData.name)
            .put("sender_id", userData.id)
            .put("notification_type", "DriverAcceptDelivery")
            .put("sender_type", "driver")

        AppController.socket.emit("send_notification", jsonObject)
    }

    override fun onTimePaymentClicked(position: Int) {
        val dialog = TimeAndPaymentPopUp(availableTicketsData[position])
        dialog.show(childFragmentManager, "TimeAndPaymentPopUp")
    }

    override fun onDeliveryDetailClicked(position: Int) {
        val dialog = DeliveryDetailsPopup(availableTicketsData[position])
        dialog.show(childFragmentManager, "DeliveryDetailsPopup")
    }

    override fun onLoadMoreClicked() {
        getAvailableTickets()
    }

    override fun onClientInfoClicked(position: Int) {
        val dialog = OrderClientInfo(availableTicketsData[position])
        dialog.show(childFragmentManager, "OrderClientInfo")
    }

    override fun onResume() {
        super.onResume()
        availableTicketsData.clear()
        skip = 0
        getAvailableTickets()
    }
}
package com.android.sitbak.home.tickets_tab.processing

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.android.sitbak.service.LocationService
import com.android.sitbak.R
import com.android.sitbak.base.AppController
import com.android.sitbak.base.BaseActivity
import com.android.sitbak.base.ViewModelFactory
import com.android.sitbak.databinding.FragmentProcessingTicketsBinding
import com.android.sitbak.home.chat.ChatActivity
import com.android.sitbak.home.popups.DeliveryDetailsPopup
import com.android.sitbak.home.popups.IDVerificationPopup
import com.android.sitbak.home.popups.OrderClientInfo
import com.android.sitbak.home.popups.TimeAndPaymentPopUp
import com.android.sitbak.home.tickets_tab.ProcessingTicketsModel
import com.android.sitbak.home.tickets_tab.TicketsData
import com.android.sitbak.network.Api
import com.android.sitbak.network.ApiTags
import com.android.sitbak.network.RetrofitClient
import com.android.sitbak.network.SocketIO
import com.android.sitbak.utils.*
import com.android.sitbak.utils.OrdersDeliveryStatus.BEING_PREPARED
import com.android.sitbak.utils.OrdersDeliveryStatus.CANCELLED_BY_DRIVER
import com.android.sitbak.utils.OrdersDeliveryStatus.DRIVER_AT_STORE
import com.android.sitbak.utils.OrdersDeliveryStatus.DRIVER_AT_USER_PLACE
import com.android.sitbak.utils.OrdersDeliveryStatus.DRIVER_ON_WAY
import com.android.sitbak.utils.OrdersDeliveryStatus.FULFILLED
import com.android.sitbak.utils.OrdersDeliveryStatus.PENDING
import com.android.sitbak.utils.OrdersDeliveryStatus.PICKED_BY_DRIVER
import com.android.sitbak.utils.OrdersDeliveryStatus.READY_FOR_PICKUP
import com.astritveliu.boom.Boom
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsStatusCodes
import com.google.gson.Gson
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call


class ProcessingTicketsFragment : Fragment(), IDVerificationPopup.VerifyIDInterface {

    private lateinit var binding: FragmentProcessingTicketsBinding
    private lateinit var viewModel: ProcessingTicketsVM
    private lateinit var mContext: Context
    private lateinit var retrofitClient: Api
    private lateinit var apiCall: Call<ResponseBody>

    private lateinit var processingTicketsData: TicketsData
    private var isOnPointOrVerify = ""

    companion object {
        const val DriverFencingBroadCast = "driverFencingBroadCast"
    }

    private var driverFencingBroadCast = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent != null) {
                val status = intent.getStringExtra("status")
                val orderId = intent.getIntExtra("orderId", -1)
                if (!status.isNullOrBlank() && orderId != -1) {
                    if (::processingTicketsData.isInitialized) {
                        if (processingTicketsData.id == orderId) {
                            processingTicketsData.status = status
                            handleTicketsResponse(processingTicketsData)
                        }
                    }
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_processing_tickets, container, false)
        mContext = requireContext()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        retrofitClient = RetrofitClient.getClient(mContext).create(Api::class.java)

        LocalBroadcastManager.getInstance(mContext).registerReceiver(driverFencingBroadCast, IntentFilter(DriverFencingBroadCast))

        initVM()
        initObservers()
        initListeners()
    }

    private fun getProcessingTicket() {
        apiCall = retrofitClient.getProcessingTickets()
        viewModel.getProcessingTickets(apiCall)
    }


    private fun initVM() {
        viewModel = ViewModelProviders.of(this, ViewModelFactory()).get(ProcessingTicketsVM::class.java)
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
                        ApiTags.GET_PROCESSING_TICKETS -> {
                            val model = Gson().fromJson(it.data.toString(), ProcessingTicketsModel::class.java)
                            handleTicketsResponse(model.data)
                        }
                        ApiTags.CANCEL_DELIVERY -> {
                            AppUtils.showToast(requireActivity(), it.data?.getString("message")!!, true)
                            binding.swipeRevealLayout.viewGone()

//                            SharedPreference.saveBoolean(mContext, Constants.isDriverPickedOrder, false)
//                            SharedPreference.saveBoolean(mContext, Constants.isDriverEnterStoreRegion, false)
//                            SharedPreference.saveBoolean(mContext, Constants.isDriverExitedStoreRegion, false)

                            sendDeliveryStatusSocket(CANCELLED_BY_DRIVER)
                            stopService()

                            Handler(Looper.getMainLooper()).postDelayed({
                                getProcessingTicket()
                            }, 1000)
                        }
                        ApiTags.START_DELIVERY -> {
//                            SharedPreference.saveBoolean(mContext, Constants.isDriverPickedOrder, true)
                            AppUtils.showToast(requireActivity(), it.data?.getString("message")!!, true)

                            sendDeliveryStatusSocket(PICKED_BY_DRIVER)

                            Handler(Looper.getMainLooper()).postDelayed({
                                getProcessingTicket()
                            }, 1000)
                        }
                        ApiTags.COMPLETE_DELIVERY -> {
//                            SharedPreference.saveBoolean(mContext, Constants.isDriverPickedOrder, false)
//                            SharedPreference.saveBoolean(mContext, Constants.isDriverEnterStoreRegion, false)
//                            SharedPreference.saveBoolean(mContext, Constants.isDriverExitedStoreRegion, false)
                            AppUtils.showToast(requireActivity(), it.data?.getString("message")!!, true)

                            sendDeliveryStatusSocket(FULFILLED)
                            stopService()

                            Handler(Looper.getMainLooper()).postDelayed({
                                getProcessingTicket()
                            }, 1000)
                        }
                    }
                }
            }
        })

        viewModel.getOrderStatusResponse().observe(viewLifecycleOwner, {
            val orderId = it.getInt("id")
            val userId = it.getInt("user_id")
            val status = it.getString("status")

            Toast.makeText(mContext, status, Toast.LENGTH_SHORT).show()

            if (::processingTicketsData.isInitialized)
                if (processingTicketsData.id == orderId) {
                    processingTicketsData.status = status
                    handleTicketsResponse(processingTicketsData)
                }
        })
    }

    private fun stopService() {
        val mServiceIntent = Intent(mContext, LocationService::class.java)
        if (isMyServiceRunning(LocationService::class.java)) {
            mContext.stopService(mServiceIntent)
        }
    }

    private fun sendDeliveryStatusSocket(status: String) {
        val jsonObject = JSONObject()
            .put("id", processingTicketsData.id)
            .put("user_id", processingTicketsData.user.id)
            .put("store_id", processingTicketsData.store.id)
            .put("store_name", processingTicketsData.store.name)
            .put("status", status)
        AppController.socket.emit(SocketIO.socketOrderSendStatus, jsonObject)
    }

    @SuppressLint("SetTextI18n")
    private fun handleTicketsResponse(data: TicketsData?) {
        try {
            binding.pullToRefresh.isRefreshing = false
            if (data != null) {
                processingTicketsData = data

                binding.tvDelivery.text = "$${processingTicketsData.delivery_charges}"
                binding.tvKM.text = "${processingTicketsData.distance}\nkm"
                binding.tvIncludeTip.text = mContext.resources.getString(
                    R.string.included_tip,
                    "$${processingTicketsData.delivery_charges}"
                )
                binding.tvTime.text =
                    "${processingTicketsData.start_time}- ${processingTicketsData.end_time}"

                binding.tvStoreLocation.text = processingTicketsData.store_location.address
                binding.tvAddress1.text = processingTicketsData.delivery_location.address
                binding.tvClientName.text = processingTicketsData.user.name
                binding.tvOrderName.text = mContext.resources.getString(
                    R.string.order_items_no,
                    processingTicketsData.items_count.toString()
                )
                binding.tvNote.text = if (processingTicketsData.delivery_note == null) "" else processingTicketsData.delivery_note
                binding.tvOrderNumber.text = processingTicketsData.order_number

                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N) {
                    binding.tvTimeLeft.text =
                        "left " +
                                (mContext as BaseActivity).diffTimeForHours(
                                    processingTicketsData.start_time,
                                    processingTicketsData.end_time
                                ).toString() + "h" + (mContext as BaseActivity).diffTimeForMin(
                            processingTicketsData.start_time,
                            processingTicketsData.end_time
                        ).toString() + "m"
                } else
                    binding.tvTimeLeft.text = ""

                binding.tvNote.text = if (processingTicketsData.delivery_note.isNullOrBlank()) "N/A" else processingTicketsData.delivery_note

                binding.swipeRevealLayout.viewVisible()
                binding.llNoData.viewGone()

                when (processingTicketsData.status) {
                    PENDING, BEING_PREPARED -> {
                        binding.swipeRevealLayout.setLockDrag(false)
                        binding.topLay.setBackgroundColor(ContextCompat.getColor(mContext, R.color.delivery_status_pending_background))
                        binding.tvStatus.setTextColor(ContextCompat.getColor(mContext, R.color.delivery_status_pending))
                        binding.tvOrderNumber.setTextColor(ContextCompat.getColor(mContext, R.color.delivery_status_pending_text))
                        binding.tvStatus.text = mContext.resources.getString(R.string.delivery_status_pending)

                        binding.tvGoToStore.viewGone()
                        binding.btnAcceptedForDelivery.viewGone()
                        binding.groupCallMessageBtn.viewGone()
                    }
                    READY_FOR_PICKUP -> {
                        binding.swipeRevealLayout.setLockDrag(false)
                        binding.topLay.setBackgroundColor(ContextCompat.getColor(mContext, R.color.delivery_status_ready_background))
                        binding.tvStatus.setTextColor(ContextCompat.getColor(mContext, R.color.delivery_status_ready))
                        binding.tvOrderNumber.setTextColor(ContextCompat.getColor(mContext, R.color.delivery_status_ready_text))
                        binding.tvStatus.text = mContext.resources.getString(R.string.delivery_status_ready)

                        binding.tvGoToStore.viewVisible()
                        binding.btnAcceptedForDelivery.viewGone()
                        binding.groupCallMessageBtn.viewGone()

                        //Initializing Service
                        checkForPermissions()
                    }
                    DRIVER_AT_STORE -> {
                        binding.swipeRevealLayout.setLockDrag(false)
                        binding.topLay.setBackgroundColor(ContextCompat.getColor(mContext, R.color.delivery_status_picked_background))
                        binding.tvStatus.setTextColor(ContextCompat.getColor(mContext, R.color.delivery_status_picked))
                        binding.tvOrderNumber.setTextColor(ContextCompat.getColor(mContext, R.color.delivery_status_picked_text))
                        binding.tvStatus.text = mContext.resources.getString(R.string.delivery_status_picked)


                        binding.tvGoToStore.viewGone()
                        binding.btnAcceptedForDelivery.viewVisible()
                        binding.groupCallMessageBtn.viewGone()


                        //Initializing Service
                        checkForPermissions()
                    }
                    PICKED_BY_DRIVER, DRIVER_ON_WAY -> {
                        binding.swipeRevealLayout.setLockDrag(true)
                        binding.topLay.setBackgroundColor(ContextCompat.getColor(mContext, R.color.delivery_status_picked_background))
                        binding.tvStatus.setTextColor(ContextCompat.getColor(mContext, R.color.delivery_status_picked))
                        binding.tvOrderNumber.setTextColor(ContextCompat.getColor(mContext, R.color.delivery_status_picked_text))
                        binding.tvStatus.text = mContext.resources.getString(R.string.delivery_status_picked)

                        binding.tvGoToStore.viewGone()
                        binding.btnAcceptedForDelivery.viewGone()
                        binding.groupCallMessageBtn.viewVisible()
                        binding.btnOnPointOrVerifyID.text = "On Point"
                        isOnPointOrVerify = "On Point"


                        //Initializing Service
                        checkForPermissions()
                    }
                    DRIVER_AT_USER_PLACE -> {
                        binding.swipeRevealLayout.setLockDrag(true)
                        binding.topLay.setBackgroundColor(ContextCompat.getColor(mContext, R.color.delivery_status_picked_background))
                        binding.tvStatus.setTextColor(ContextCompat.getColor(mContext, R.color.delivery_status_picked))
                        binding.tvOrderNumber.setTextColor(ContextCompat.getColor(mContext, R.color.delivery_status_picked_text))
                        binding.tvStatus.text = mContext.resources.getString(R.string.delivery_status_picked)

                        binding.tvGoToStore.viewGone()
                        binding.btnAcceptedForDelivery.viewGone()
                        binding.groupCallMessageBtn.viewVisible()
                        binding.btnOnPointOrVerifyID.text = "Verify ID"
                        isOnPointOrVerify = "Verify ID"
                    }
                }
            } else {
                binding.swipeRevealLayout.viewGone()
                binding.llNoData.viewVisible()
            }
        } catch (e: Exception) {
            binding.swipeRevealLayout.viewGone()
            e.printStackTrace()
        }
    }

    private fun checkForPermissions() {
        val permissionStrings: MutableList<String> = ArrayList()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            permissionStrings.add(Manifest.permission.ACCESS_FINE_LOCATION)
            permissionStrings.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        } else {
            permissionStrings.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        Dexter.withContext(mContext)
            .withPermissions(
                permissionStrings
            ).withListener(object : MultiplePermissionsListener {
                @SuppressLint("MissingPermission")
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    when {
                        report?.areAllPermissionsGranted() == true -> {
                            if (!isLocationEnabled(mContext)) {
                                enableLoc()
                            } else {
                                startService()
                            }
                        }
                        report?.isAnyPermissionPermanentlyDenied == true -> {
                            val intent = Intent(
                                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.fromParts("package", mContext.packageName, null)
                            )
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            startActivity(intent)
                        }
                        else -> {
                            Toast.makeText(mContext, "Permission are required for app to work.", Toast.LENGTH_SHORT).show()
                            val intent = Intent(
                                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.fromParts("package", mContext.packageName, null)
                            )
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            startActivity(intent)
                        }
                    }
                }

                override fun onPermissionRationaleShouldBeShown(p0: MutableList<PermissionRequest>?, p1: PermissionToken?) {
                    p1?.continuePermissionRequest()
                }
            })
            .check()
    }

    private fun startService() {
        //Initializing Service
        val mServiceIntent = Intent(mContext, LocationService::class.java)
        mServiceIntent.putExtra("orderId", processingTicketsData.id)
        mServiceIntent.putExtra("userId", processingTicketsData.user.id)
        mServiceIntent.putExtra("storeId", processingTicketsData.store.id)
        mServiceIntent.putExtra("storeLat", processingTicketsData.store_location.latitude.toDouble())
        mServiceIntent.putExtra("storeLng", processingTicketsData.store_location.longitude.toDouble())
        mServiceIntent.putExtra("orderUserLat", processingTicketsData.delivery_location.latitude.toDouble())
        mServiceIntent.putExtra("orderUserLng", processingTicketsData.delivery_location.longitude.toDouble())
        mServiceIntent.putExtra("orderStatus", processingTicketsData.status)
        if (!isMyServiceRunning(LocationService::class.java)) {
            mContext.startService(mServiceIntent)
        } else {
            mContext.stopService(mServiceIntent)
            mContext.startService(mServiceIntent)
        }
    }

    private fun initListeners() {
        Boom(binding.viewTimeAndDelivery)
        Boom(binding.viewLocationDetails)
        Boom(binding.viewClientInfo)
        Boom(binding.llDelete)
        Boom(binding.llCalToStore)
        Boom(binding.btnAcceptedForDelivery)
        Boom(binding.btnOnPointOrVerifyID)

        binding.pullToRefresh.setOnRefreshListener {
            getProcessingTicket()
        }

        binding.viewTimeAndDelivery.setOnClickListener {
            val dialog = TimeAndPaymentPopUp(processingTicketsData)
            dialog.show(childFragmentManager, "TimeAndPaymentPopUp")
        }
        binding.viewLocationDetails.setOnClickListener {
            val dialog = DeliveryDetailsPopup(processingTicketsData)
            dialog.show(childFragmentManager, "DeliveryDetailsPopup")
        }

        binding.viewClientInfo.setOnClickListener {
            val dialog = OrderClientInfo(processingTicketsData)
            dialog.show(childFragmentManager, "OrderClientInfo")
        }

        binding.llDelete.setOnClickListener {
            binding.swipeRevealLayout.close(true)
            apiCall = retrofitClient.cancelDelivery(processingTicketsData.id)
            viewModel.cancelOrders(apiCall)
        }

        binding.llCalToStore.setOnClickListener {
            binding.swipeRevealLayout.close(true)
            phoneNumberCall(processingTicketsData.store.phone_number)
        }

        binding.btnCall.setOnClickListener {
            binding.swipeRevealLayout.close(true)
            phoneNumberCall(processingTicketsData.user.phone_number)
        }

        binding.btnMessage.setOnClickListener {
            val intent = Intent(mContext, ChatActivity::class.java)
            intent.putExtra("orderId", processingTicketsData.id)
            intent.putExtra("senderName", processingTicketsData.user.name)
            intent.putExtra("senderPhoto", processingTicketsData.user.photo_path)
            mContext.startActivity(intent)
            binding.swipeRevealLayout.close(true)
        }

        binding.btnAcceptedForDelivery.setOnClickListener {
            apiCall = retrofitClient.startDelivery(processingTicketsData.id)
            viewModel.startDelivery(apiCall)
        }

        binding.btnOnPointOrVerifyID.setOnClickListener {

            when (isOnPointOrVerify) {
                "On Point" -> {
                    val jsonObject = JSONObject()
                        .put("id", processingTicketsData.id)
                        .put("user_id", processingTicketsData.user.id)
                        .put("store_id", processingTicketsData.store.id)
                        .put("status", DRIVER_AT_USER_PLACE)
                    AppController.socket.emit(SocketIO.socketOrderSendStatus, jsonObject)
                    getProcessingTicket()
                    stopService()
                }
                "Verify ID" -> {
                    val dialog = IDVerificationPopup(processingTicketsData, this)
                    dialog.show(childFragmentManager, "IDVerificationPopup")
                }
            }


        }

    }

    fun isLocationEnabled(context: Context): Boolean {
        val locationManager: LocationManager =
            context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }


    private fun enableLoc() {
        val locationRequest = LocationRequest.create()
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            .setInterval(30 * 1000)
            .setFastestInterval(5 * 1000)

        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
            .setAlwaysShow(true)

        val pendingResult = LocationServices
            .getSettingsClient(requireActivity())
            .checkLocationSettings(builder.build())

        pendingResult.addOnCompleteListener { task ->
            if (task.isSuccessful.not()) {
                task.exception?.let {
                    if (it is ApiException && it.statusCode == LocationSettingsStatusCodes.RESOLUTION_REQUIRED) {
                        // Show the dialog by calling startResolutionForResult(),
                        // and check the result in onActivityResult().

                        startIntentSenderForResult(
                            it.status.resolution.intentSender,
                            2000, null, 0, 0, 0, null
                        )
                    }
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            2000 -> when (resultCode) {
                Activity.RESULT_OK, Activity.RESULT_CANCELED -> {
                    checkForPermissions()
                }
            }
        }
    }


    override fun completeOrder(id: Int) {
        apiCall = retrofitClient.completeDelivery(processingTicketsData.id)
        viewModel.completeDelivery(apiCall)
    }

    @Suppress("DEPRECATION")
    private fun isMyServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = mContext.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager?
        for (service in manager!!.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                Log.i("Service status", "Running")
                return true
            }
        }
        Log.i("Service status", "Not running")
        return false
    }

    override fun onResume() {
        super.onResume()
        getProcessingTicket()
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(driverFencingBroadCast)
    }

}
package com.android.sitbak.home.maps

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProviders
import com.android.sitbak.R
import com.android.sitbak.base.BaseActivity
import com.android.sitbak.base.ViewModelFactory
import com.android.sitbak.databinding.ActivityAddAvailabilityBinding
import com.android.sitbak.driver.adapters.*
import com.android.sitbak.home.popups.region_popup.RegionData
import com.android.sitbak.home.popups.region_popup.RegionModel
import com.android.sitbak.home.scheduling_tab.my_shifts.AvailabilitiesData
import com.android.sitbak.network.Api
import com.android.sitbak.network.ApiTags
import com.android.sitbak.network.RetrofitClient
import com.android.sitbak.utils.ApiStatus
import com.android.sitbak.utils.AppUtils
import com.android.sitbak.utils.Loader
import com.astritveliu.boom.Boom
import com.google.android.gms.location.LocationListener
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.gson.Gson
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog
import kotlinx.android.synthetic.main.activity_add_availability.*
import okhttp3.ResponseBody
import retrofit2.Call
import java.util.*
import kotlin.collections.ArrayList


class AddAvailabilityActivity : BaseActivity(), OnMapReadyCallback, LocationListener {

    private lateinit var binding: ActivityAddAvailabilityBinding
    private lateinit var locationZoneAdapter: LocationZoneAdapter
    private lateinit var statusAdapter: AddAvailabilityStatusAdapter
    private lateinit var mMap: GoogleMap
    private val LOCATION_PERMISSION_REQUEST = 1000
    private lateinit var locationManager: LocationManager
    private lateinit var mContext: Context
    private var mCalendar: Calendar = Calendar.getInstance()
    var startTime = "10:00 AM"
    var endTime = "10:00 PM"
    var selectedStatus = ""
    var selectedStatusId = -1
    var type = ""
    var selectedZone = ""
    var selectedRegionID = -1
//    private var polygonPointsAll: ArrayList<LatLng> = ArrayList()
//    private var polygonList1: ArrayList<LatLng> = ArrayList()
//    private var polygonList2: ArrayList<LatLng> = ArrayList()

    private var statusList: ArrayList<AvailabilityStatusModel> = ArrayList()

    //    private var polygon1: Polygon? = null
//    private var polygon2: Polygon? = null
    private var availabilityData: AvailabilitiesData? = null
    private var regionList: MutableList<RegionData> = ArrayList()

    private lateinit var viewModel: AddAvailabilityActivityVM
    private lateinit var retrofitClient: Api
    private lateinit var apiCall: Call<ResponseBody>

    private lateinit var polygonOptions: PolygonOptions

    private val polygon: MutableList<PolygonModel> = ArrayList()
    val polygonsList: MutableList<PolygonOptions> = ArrayList()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddAvailabilityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        changeStatusBarColor(R.color.heavy_metal)
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        mContext = this
        retrofitClient = RetrofitClient.getClient(mContext).create(Api::class.java)

        initVM()
        initObservers()
        setIntentData()
        initVar()
        setLocationTagsRecView()
        setAvailabilityStatusRecView()
        listener()

    }

    private fun setIntentData() {
        type = intent.getStringExtra("type").toString()
        when (type) {
            "addAvailability" -> {
                binding.btnAddAvailability.text = getString(R.string.add_availability)
                binding.tvTitle.text = getString(R.string.add_availability)

                binding.tvStartTime.text = startTime
                binding.tvEndTime.text = endTime
            }
            "editAvailability" -> {
                binding.btnAddAvailability.text = getString(R.string.edit_availability)
                binding.tvTitle.text = getString(R.string.edit_availability)

                availabilityData = intent.getSerializableExtra("data") as AvailabilitiesData

                if (availabilityData != null) {
                    selectedStatus = availabilityData?.type ?: ""
                    selectedRegionID = availabilityData?.region_id!!
                    startTime = availabilityData?.start_time ?: ""
                    endTime = availabilityData?.end_time ?: ""
                }

                binding.tvStartTime.text = startTime
                binding.tvEndTime.text = endTime
            }
        }

    }

    private fun getRegions() {
        apiCall = retrofitClient.getRegions()
        viewModel.getRegions(apiCall)
    }

    private fun initVM() {
        viewModel = ViewModelProviders.of(this, ViewModelFactory()).get(AddAvailabilityActivityVM::class.java)
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
                        ApiTags.GET_REGION -> {
                            val model = Gson().fromJson(it.data.toString(), RegionModel::class.java)
                            handleRegionResponse(model)
                        }
                        ApiTags.ADD_AVAILABILITY -> {
                            AppUtils.showToast(this, it.data?.getString("message")!!, true)
                            Handler(Looper.getMainLooper()).postDelayed({
                                finish()
                            }, 1000)
                        }
                        ApiTags.UPDATE_AVAILABILITY -> {
                            AppUtils.showToast(this, it.data?.getString("message")!!, true)
                            Handler(Looper.getMainLooper()).postDelayed({
                                finish()
                            }, 1000)
                        }
                    }
                }
            }
        })
    }

    private fun handleRegionResponse(data: RegionModel) {
        regionList.addAll(data.data)
        locationZoneAdapter.notifyDataSetChanged()

        polygon.clear()
        for (i in regionList.indices) {
            val polygonData: MutableList<LatLng> = ArrayList()
            polygonData.add(
                LatLng(
                    regionList[i].latitude1.toDouble(),
                    regionList[i].longitude1.toDouble()
                )
            )
            polygonData.add(
                LatLng(
                    regionList[i].latitude2.toDouble(),
                    regionList[i].longitude2.toDouble()
                )
            )
            polygonData.add(
                LatLng(
                    regionList[i].latitude3.toDouble(),
                    regionList[i].longitude3.toDouble()
                )
            )
            polygonData.add(
                LatLng(
                    regionList[i].latitude4.toDouble(),
                    regionList[i].longitude4.toDouble()
                )
            )

            polygon.add(PolygonModel(polygonData))

        }

        Log.d("", "")

        addPolygonOnMap()

    }

    private fun addPolygonOnMap() {
        polygonsList.clear()
        val ll: MutableList<LatLng> = ArrayList()
        ll.clear()

        for (i in polygon.indices) {
            polygonOptions = PolygonOptions()
            for (p in polygon[i].polygonModel.indices) {
                polygonOptions.add(polygon[i].polygonModel[p])
                polygonOptions.strokeColor(ContextCompat.getColor(this, R.color.app_main_text_white))
                polygonOptions.fillColor(ContextCompat.getColor(this, R.color.blue_grey_900))
                polygonOptions.strokeWidth(2F)

                ll.add(polygon[i].polygonModel[p])
            }
            polygonsList.add(polygonOptions)
            mMap.addPolygon(polygonOptions)
        }

        boundGoogleMap(ll)

    }

    private fun initVar() {
        Boom(binding.btnAddAvailability)

        statusList.add(AvailabilityStatusModel("ONE-TIME"))
        statusList.add(AvailabilityStatusModel("EVERYDAY"))
        statusList.add(AvailabilityStatusModel("EVERY-SATURDAY"))
        statusList.add(AvailabilityStatusModel("EVERY-SUNDAY"))
        statusList.add(AvailabilityStatusModel("EVERY-MONDAY"))
        statusList.add(AvailabilityStatusModel("EVERY-TUESDAY"))
        statusList.add(AvailabilityStatusModel("EVERY-WEDNESDAY"))
        statusList.add(AvailabilityStatusModel("EVERY-THURSDAY"))
        statusList.add(AvailabilityStatusModel("EVERY-FRIDAY"))

        statusList.forEach {
            if (it.type == selectedStatus) {
                it.isChecked = true
            }
        }

    }

    private fun setAvailabilityStatusRecView() {
        statusAdapter = AddAvailabilityStatusAdapter(this, statusList, object : AvailabilityStatusClickListener {
            override fun onStatusClick(position: Int, regionData: AvailabilityStatusModel) {
                selectedStatus = regionData.type
                if (!regionData.isChecked) {
                    statusList.forEachIndexed { _, availabilityStatusModel ->
                        availabilityStatusModel.isChecked = false
                    }
                    statusList[position].isChecked = !statusList[position].isChecked
                }
            }
        })
        binding.rvStatus.adapter = statusAdapter
    }

    private fun setLocationTagsRecView() {
        locationZoneAdapter =
            LocationZoneAdapter(selectedRegionID, this@AddAvailabilityActivity, regionList, object : LocationZoneClickListener {
                override fun onZoneClick(position: Int, regionData: RegionData) {
                    selectedZone = regionData.address
                    selectedRegionID = regionData.id
                    
                    boundGoogleMap(polygonsList[position].points)
                    polygonsList[position].fillColor(ContextCompat.getColor(this@AddAvailabilityActivity, R.color.blue_grey_900))

                    /* if (selectedRegionID == regionData.id) {

                         polygonOptions.strokeColor(ContextCompat.getColor(this@AddAvailabilityActivity, R.color.blue_grey_900))
                         polygonOptions.fillColor(ContextCompat.getColor(this@AddAvailabilityActivity, R.color.app_main_text_white))
                         polygonOptions.strokeWidth(2F)
                     } else {
                         polygonOptions.strokeColor(ContextCompat.getColor(this@AddAvailabilityActivity, R.color.app_main_text_white))
                         polygonOptions.fillColor(ContextCompat.getColor(this@AddAvailabilityActivity, R.color.blue_grey_900))
                         polygonOptions.strokeWidth(2F)
                     }*/
                }

            })
        binding.rvLocations.adapter = locationZoneAdapter
    }

    private fun listener() {
        binding.ivBack.setOnClickListener {
            onBackPressed()
        }
        binding.tvStartTime.setOnClickListener {
            showTimerClock { sTime, oTime ->
                binding.tvStartTime.text = sTime
                startTime = sTime
            }
        }
        binding.tvEndTime.setOnClickListener {
            showTimerClock(true) { sTime, oTime ->
                binding.tvEndTime.text = sTime
                endTime = sTime
            }
        }
        binding.btnAddAvailability.setOnClickListener {
            when (type) {
                "addAvailability" -> {
                    apiCall = retrofitClient.addAvailability(selectedRegionID.toString(), selectedStatus, startTime, endTime)
                    viewModel.addAvailability(apiCall)
                }
                "editAvailability" -> {
                    apiCall =
                        retrofitClient.updateAvailability(availabilityData?.id ?: -1, selectedRegionID.toString(), selectedStatus, startTime, endTime)
                    viewModel.addAvailability(apiCall)
                }
            }
        }
    }

    @SuppressLint("WrongConstant")
    private fun showTimerClock(
        setMin: Boolean = false,
        callback: (String, String) -> Unit
    ) {
        // var miniTimeCalendar = Calendar.getInstance()

        val dpd = TimePickerDialog.newInstance(
            { _, hourOfDay, minute, second ->
                val h = String.format("%02d", hourOfDay)
                val m = String.format("%02d", minute)
                val s = String.format("%02d", second)
                mCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                mCalendar.set(Calendar.MINUTE, minute)
                mCalendar.set(Calendar.SECOND, second)
                val time = "$h:$m:$s"                // current time

                var AM_PM = " AM"
                var mm_precede = ""
                var hourr = hourOfDay
                if (hourOfDay >= 12) {
                    AM_PM = " PM"
                    if (hourOfDay in 13..23) {
                        hourr -= 12
                    } else {
                        hourr = 12
                    }
                } else if (hourOfDay == 0) {
                    hourr = 12
                }
                if (minute < 10) {
                    mm_precede = "0"
                }
                val selectedTime = "$hourr:$mm_precede$minute$AM_PM"
                callback(selectedTime, time)
            },
            mCalendar.get(Calendar.HOUR_OF_DAY),
            mCalendar.get(Calendar.MINUTE),
            mCalendar.get(Calendar.SECOND),
            false
        )

        Log.i("time", setMin.toString())
        if (setMin) {
            dpd.setMinTime(
                mCalendar.get(Calendar.HOUR_OF_DAY),
                (mCalendar.get(Calendar.MINUTE) + 1),
                mCalendar.get(Calendar.SECOND)
            )
        }
        dpd.vibrate(false)
        dpd.setStyle(R.style.MyTimePickerLight, R.style.DialogTheme)
        dpd.accentColor = ContextCompat.getColor(this, R.color.green_900_off_27)
        dpd.version = TimePickerDialog.Version.VERSION_1
        //dpd.setTimeInterval(1)
        dpd.show(supportFragmentManager, "DatePickerDialog")
    }


    override fun onMapReady(p0: GoogleMap) {
        mMap = p0
        getLocationAccess()
        mMap.isBuildingsEnabled = true
        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.style_json))

        getRegions()
    }

    private fun getLocationAccess() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            mMap.isMyLocationEnabled = true
        } else
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ),
                LOCATION_PERMISSION_REQUEST
            )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return
                }
                mMap.isMyLocationEnabled = true

                locationManager =
                    mContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager

                /*locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 2000, 10f, this)*/
            } else {
                Toast.makeText(
                    this,
                    "Please grant permission to continue",
                    Toast.LENGTH_LONG
                ).show()
                finish()
            }
        }
    }

    override fun onLocationChanged(location: Location) {
        /* mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(location.latitude, location.longitude), 15f))
         locationManager.removeUpdates(this)

         mMap.addMarker(
             MarkerOptions()
                 .position(LatLng(location.latitude, location.longitude))
                 .icon(Common.bitmapDescriptorFromVector(mContext, R.drawable.ic_marker_pin))
         )*/
    }

    private fun boundGoogleMap(list: List<LatLng>) {
        val builder = LatLngBounds.Builder()
        list.forEach {
            builder.include(it)
        }
        val bounds = builder.build()
        val cu = CameraUpdateFactory.newLatLngBounds(bounds, 300)
        mMap.animateCamera(cu)
    }

}

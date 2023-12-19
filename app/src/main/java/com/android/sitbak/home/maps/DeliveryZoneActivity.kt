package com.android.sitbak.home.maps

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.android.sitbak.R
import com.android.sitbak.base.BaseActivity
import com.android.sitbak.databinding.ActivityDeliveryZoneBinding
import com.android.sitbak.home.scheduling_tab.my_shifts.AvailabilitiesData
import com.android.sitbak.home.scheduling_tab.open_shifts.OpenShiftsData
import com.google.android.gms.location.LocationListener
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*

class DeliveryZoneActivity : BaseActivity(), OnMapReadyCallback, LocationListener {
    private lateinit var binding: ActivityDeliveryZoneBinding
    private lateinit var mMap: GoogleMap
    private val LOCATION_PERMISSION_REQUEST = 1000
    private var polygon1: Polygon? = null
    private var polygonPointsAll: ArrayList<LatLng> = ArrayList()

    private var openShiftsData: OpenShiftsData? = null
    private var availabilitiesData: AvailabilitiesData? = null
    var type = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDeliveryZoneBinding.inflate(layoutInflater)
        setContentView(binding.root)
        changeStatusBarColor(R.color.heavy_metal)
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.googleMap) as SupportMapFragment
        mapFragment.getMapAsync(this)
        listener()
        initVar()
    }

    @SuppressLint("SetTextI18n")
    private fun initVar() {
        type = intent.getStringExtra("type").toString()
        when (type) {
            "openShifts" -> {
                openShiftsData = intent.getSerializableExtra("data") as OpenShiftsData
                if (openShiftsData != null) {
                    val regionData = openShiftsData?.region
                    binding.tvLocation.text = regionData?.address + ", " + regionData?.name
                    polygonPointsAll.add(LatLng(regionData?.latitude1!!.toDouble(), regionData.longitude1.toDouble()))
                    polygonPointsAll.add(LatLng(regionData.latitude2.toDouble(), regionData.longitude2.toDouble()))
                    polygonPointsAll.add(LatLng(regionData.latitude3.toDouble(), regionData.longitude3.toDouble()))
                    polygonPointsAll.add(LatLng(regionData.latitude4.toDouble(), regionData.longitude4.toDouble()))
                }
            }
            "availabilities", "myShifts" -> {
                availabilitiesData = intent.getSerializableExtra("data") as AvailabilitiesData
                if (availabilitiesData != null) {
                    val data = availabilitiesData?.region
                    binding.tvLocation.text = data?.address + ", " + data?.name
                    polygonPointsAll.add(LatLng(data?.latitude1!!.toDouble(), data.longitude1.toDouble()))
                    polygonPointsAll.add(LatLng(data.latitude2.toDouble(), data.longitude2.toDouble()))
                    polygonPointsAll.add(LatLng(data.latitude3.toDouble(), data.longitude3.toDouble()))
                    polygonPointsAll.add(LatLng(data.latitude4.toDouble(), data.longitude4.toDouble()))
                }
            }
        }
    }

    private fun listener() {
        binding.tvLocation.setOnClickListener {
            boundGoogleMap(polygonPointsAll)
        }
        binding.ivBack.setOnClickListener {
            onBackPressed()
        }
        binding.btnOk.setOnClickListener {
            finish()
        }
    }

    override fun onMapReady(p0: GoogleMap) {
        mMap = p0
        getLocationAccess()
        mMap.isBuildingsEnabled = true
        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.style_json))
        mMap.uiSettings.isMyLocationButtonEnabled = false


        polygon1 = mMap.addPolygon(
            PolygonOptions()
                .addAll(
                    polygonPointsAll
                )
                .strokeColor(ContextCompat.getColor(this, R.color.app_main_text_white))
                .fillColor(ContextCompat.getColor(this, R.color.blue_grey_900))
                .strokeWidth(2F)
        )
        mMap.setOnMapLoadedCallback { boundGoogleMap(polygonPointsAll) }
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

    private fun boundGoogleMap(list: ArrayList<LatLng>) {
        val builder = LatLngBounds.Builder()
        list.forEach {
            builder.include(it)
        }
        val bounds = builder.build()

        val cu = CameraUpdateFactory.newLatLngBounds(bounds, 300)
        mMap.animateCamera(cu)
    }

    override fun onLocationChanged(p0: Location?) {

    }
}
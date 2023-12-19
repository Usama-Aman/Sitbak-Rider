package com.android.sitbak.home.scheduling_tab.my_shifts

import java.io.Serializable

data class AvailabilitiesModel(
    val `data`: List<AvailabilitiesData>,
    val message: String,
    val status: Boolean
) : Serializable

data class AvailabilitiesData(
    val driver_id: Int,
    val end: String,
    val end_time: String,
    val id: Int,
    val region: Region,
    val region_id: Int,
    val start: String,
    val start_time: String,
    val type: String,
    val start_date_formatted: String
) : Serializable

data class Region(
    val address: String,
    val city: String,
    val country: String,
    var id: Int,
    val latitude1: String,
    val longitude1: String,
    val latitude2: String,
    val longitude2: String,
    val latitude3: String,
    val longitude3: String,
    val latitude4: String,
    val longitude4: String,
    val name: String,
    val radius: Int
) : Serializable
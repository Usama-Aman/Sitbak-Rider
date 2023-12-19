package com.android.sitbak.home.popups.region_popup

data class RegionModel(
    val `data`: List<RegionData>,
    val message: String,
    val status: Boolean
)

data class RegionData(
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
)
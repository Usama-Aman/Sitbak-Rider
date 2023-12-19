package com.android.sitbak.home.earning

data class YourEarningModel(
    val `data`: YourEarningData,
    val message: String,
    val status: Boolean
)
data class YourEarningData(
    val available_earning: String,
    val earning: List<Earning>
)
data class Earning(
    val date: String,
    val value: String
)
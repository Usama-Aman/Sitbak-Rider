package com.android.sitbak.home.completed_shifts

data class CompletedShiftsModel(
    val `data`: List<CompletedShiftsData>,
    val message: String,
    val status: Boolean
)

data class CompletedShiftsData(
    val created_at: String,
    val delivery_charges: String,
    val delivery_location: CompletedShiftsDeliveryLocation,
    val delivery_note: String?,
    val distance: String,
    val driver_amount: String,
    val driver_tip: String,
    val end_time: String,
    val id: Int,
    val items: List<CompletedShiftsItem>,
    val items_count: Int,
    val order_number: String,
    val start_time: String,
    val status: String,
    val store: CompletedShiftsStore,
    val store_location: CompletedShiftsStoreLocation,
    val user: CompletedShiftsUser
)

data class CompletedShiftsDeliveryLocation(
    val address: String,
    val latitude: String,
    val longitude: String
)

data class CompletedShiftsItem(
    val product_name: String,
    val quantity: Int,
    val weight: Int
)

data class CompletedShiftsStore(
    val email: String,
    val id: Int,
    val name: String,
    val phone_number: String
)

data class CompletedShiftsStoreLocation(
    val address: String,
    val latitude: String,
    val longitude: String
)

data class CompletedShiftsUser(
    val email: String,
    val id: Int,
    val name: String,
    val phone_number: String,
    val photo_path: String
)
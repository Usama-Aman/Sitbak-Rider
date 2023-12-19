package com.android.sitbak.home.tickets_tab

data class TicketsModel(
    val count: Int,
    val `data`: List<TicketsData>,
    val message: String,
    val status: Boolean
)

data class ProcessingTicketsModel(
    val count: Int,
    val `data`: TicketsData?,
    val message: String,
    val status: Boolean
)

data class TicketsData(
    val created_at: String,
    val delivery_charges: String,
//    val delivery_status: String,
    val delivery_location: TicketsDeliveryLocation,
    val delivery_note: String?,
    val distance: String,
    val driver_tip: String,
    val end_time: String,
    val id: Int,
    val items: List<TicketsItem>,
    val items_count: Int,
    val order_number: String,
    val start_time: String,
    var status: String,
    val store: TicketsStore,
    val store_location: TicketsStoreLocation,
    val user: TicketsUser
)

data class TicketsDeliveryLocation(
    val address: String,
    val latitude: String,
    val longitude: String
)

data class TicketsItem(
    val product_name: String,
    val quantity: Int,
    val weight: Int
)

data class TicketsStore(
    val email: String,
    val id: Int,
    val name: String,
    val phone_number: String
)

data class TicketsStoreLocation(
    val address: String,
    val latitude: String,
    val longitude: String
)

data class TicketsUser(
    val email: String,
    val id: Int,
    val name: String,
    val phone_number: String,
    val photo_path: String
)
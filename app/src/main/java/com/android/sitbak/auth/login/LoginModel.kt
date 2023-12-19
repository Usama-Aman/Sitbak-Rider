package com.android.sitbak.auth.login

import com.google.gson.annotations.SerializedName

data class LoginModel(
    val access_token: String,
    val `data`: LoginData,
    val message: String,
    val status: Boolean,
    val verification_data: VerificationData
)

data class LoginData(
    var email: String,
    var email_verified_at: String?,
    val id: Int,
    var is_available: Int,
    val location: Any,
    val name: String,
    var phone_number: String?,
    var phone_verified_at: String?,
    var photo_path: String?,
    var region: String,
    var region_name: String,
    var region_id: Int,
    var is_active: Int,
    @SerializedName("verify_photo_path") var id_photo_path: String?,
    var available_earning: String?,
    var is_bank_account_verified: Int,
    var stripe_express_dashboard_url: String?,
    var country_code: String?,
)

data class VerificationData(
    val email: String,
    val email_verified: Int,
    val otp: Int
)
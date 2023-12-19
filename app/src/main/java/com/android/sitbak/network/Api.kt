package com.android.sitbak.network

import com.android.sitbak.base.AppController
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*


interface Api {

    @FormUrlEncoded
    @POST("register_driver")
    fun register(
        @Field("name") name: String,
        @Field("email") email: String,
        @Field("password") password: String,
        @Field("password_confirmation") password_confirmation: String,
    ): Call<ResponseBody>

    @FormUrlEncoded
    @POST("resend_otp_driver")
    fun sendPhoneOTP(
        @Field("phone_number") phone_number: String,
        @Field("email") email: String,
        @Field("type") type: String,
    ): Call<ResponseBody>

    @FormUrlEncoded
    @POST("driver/verify")
    fun emailPhoneConfirmation(
        @Field("type") type: String,
        @Field("otp") otp: String,
    ): Call<ResponseBody>

    @FormUrlEncoded
    @POST("driver/update_phone_number")
    fun addUpdatePhoneNumber(
        @Field("country_code") country_code: String,
        @Field("phone_number") phone_number: String,
    ): Call<ResponseBody>

    @FormUrlEncoded
    @POST("driver/switch_availability")
    fun switchAvailability(
        @Field("is_available") is_available: Int,
    ): Call<ResponseBody>


    @FormUrlEncoded
    @POST("login_driver")
    fun loginUser(
        @Field("email") email: String,
        @Field("password") password: String,
    ): Call<ResponseBody>

    @Multipart
    @POST("driver/update_photos")
    fun addUpdateUserPhotos(
        @Part("type") photo_type: RequestBody,
        @Part photo: MultipartBody.Part,
    ): Call<ResponseBody>

    @FormUrlEncoded
    @POST("reset_password_driver/send_otp")
    fun senOTPForgotPassword(
        @Field("phone_number") phone_number: String,
        @Field("email") email: String,
        @Field("type") type: String,
    ): Call<ResponseBody>

    @FormUrlEncoded
    @POST("reset_password_driver")
    fun resetPassword(
        @Field("password") password: String,
        @Field("password_confirmation") password_confirmation: String,
        @Field("email") email: String,
        @Field("phone_number") phone_number: String,
        @Field("otp") otp: String,
        @Field("type") type: String
    ): Call<ResponseBody>

    @FormUrlEncoded
    @POST("driver/update_email")
    fun updateUserEmail(
        @Field("email") email: String,
    ): Call<ResponseBody>

    @FormUrlEncoded
    @POST("driver/password")
    fun updateUserPassword(
        @Field("old_password") old_password: String,
        @Field("password") password: String,
        @Field("password_confirmation") password_confirmation: String,
    ): Call<ResponseBody>

    @FormUrlEncoded
    @POST("driver/availability")
    fun addAvailability(
        @Field("region") region: String,
        @Field("type") type: String,
        @Field("start") start: String,
        @Field("end") end: String,
    ): Call<ResponseBody>

    @FormUrlEncoded
    @POST("driver/accept_delivery")
    fun acceptOrder(@Field("order_id") order_id: Int): Call<ResponseBody>


    @POST("driver/availability/{shifts_id}")
    fun addAvailabilityFromShifts(@Path("shifts_id") shifts_id: Int): Call<ResponseBody>

    @FormUrlEncoded
    @POST("driver/update_availability/{availability_id}")
    fun updateAvailability(
        @Path("availability_id") availability_id: Int,
        @Field("region") region: String,
        @Field("type") type: String,
        @Field("start") start: String,
        @Field("end") end: String,
    ): Call<ResponseBody>

    @FormUrlEncoded
    @POST("driver/update_profile")
    fun updateProfile(
        @Field("name") name: String,
        @Field("region") region: Int
    ): Call<ResponseBody>

    @GET("driver/get_profile")
    fun getUserProfile(): Call<ResponseBody>

    @GET("driver/available_deliveries")
    fun getAvailableTickets(
        @Query("skip") skip: Int,
        @Query("take") take: Int = AppController.pageCount
    ): Call<ResponseBody>

    @GET("driver/accepted_delivery")
    fun getProcessingTickets(): Call<ResponseBody>

    @GET("driver/earning")
    fun getYourEarnings(): Call<ResponseBody>

    @GET("driver/get_money")
    fun getMoney(): Call<ResponseBody>

    @GET("driver/regions")
    fun getRegions(): Call<ResponseBody>

    @GET("driver/availabilities")
    fun getAvailabilities(@Query("date") date: String): Call<ResponseBody>

    @GET("driver/shifts")
    fun getOpenShifts(@Query("date") date: String): Call<ResponseBody>

    @GET("driver/cancel_delivery/{order_id}")
    fun cancelDelivery(@Path("order_id") order_id: Int): Call<ResponseBody>

    @GET("driver/chats/{order_id}")
    fun getChats(@Path("order_id") order_id: Int): Call<ResponseBody>

    @GET("driver/start_delivery/{order_id}")
    fun startDelivery(@Path("order_id") order_id: Int): Call<ResponseBody>

    @GET("driver/complete_delivery/{order_id}")
    fun completeDelivery(@Path("order_id") order_id: Int): Call<ResponseBody>

    @FormUrlEncoded
    @POST("driver/chats")
    fun sendChatMessage(
        @Field("type") type: String = "text",
        @Field("order_id") order_id: Int,
        @Field("message") message: String,
        @Field("sender_type") sender_type: String = "driver",
    ): Call<ResponseBody>

    @DELETE("driver/availability/{availability_id}")
    fun deleteShift(@Path("availability_id") availability_id: Int): Call<ResponseBody>

    @GET("driver/completed_deliveries")
    fun getCompletedDeliveries(
        @Query("date") date: String,
        @Query("skip") skip: Int,
    ): Call<ResponseBody>

    @GET("driver/salary_archive")
    fun getArchiveSalary(): Call<ResponseBody>

}






















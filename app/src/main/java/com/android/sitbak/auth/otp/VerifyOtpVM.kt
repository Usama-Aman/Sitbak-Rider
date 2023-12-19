package com.android.sitbak.auth.otp

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.android.sitbak.network.ApiTags
import com.android.sitbak.network.ResponseCallBack
import com.android.sitbak.network.RetrofitClient
import com.android.sitbak.utils.Resource
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call

class VerifyOtpVM : ViewModel(), ResponseCallBack {

    private val apiResponse = MutableLiveData<Resource<JSONObject>>()

    fun emailConfirmation(call: Call<ResponseBody>) {
        apiResponse.value = Resource.loading()
        RetrofitClient.apiCall(call, this, ApiTags.EMAIL_PHONE_CONFIRMATION)
    }

    fun resendOTP(call: Call<ResponseBody>) {
        apiResponse.value = Resource.loading()
        RetrofitClient.apiCall(call, this, ApiTags.RESEND_OTP)
    }

    override fun onSuccess(jsonObject: JSONObject, tag: String) {
        when (tag) {
            ApiTags.EMAIL_PHONE_CONFIRMATION -> {
                apiResponse.postValue(Resource.success(jsonObject, ApiTags.EMAIL_PHONE_CONFIRMATION))
            }
            ApiTags.RESEND_OTP -> {
                apiResponse.postValue(Resource.success(jsonObject, ApiTags.RESEND_OTP))
            }
        }
    }

    override fun onError(error: String, tag: String) {
        when (tag) {
            ApiTags.EMAIL_PHONE_CONFIRMATION -> {
                apiResponse.postValue(Resource.error(error, null, ApiTags.EMAIL_PHONE_CONFIRMATION))
            }
            ApiTags.RESEND_OTP -> {
                apiResponse.postValue(Resource.error(error, null, ApiTags.RESEND_OTP))
            }
        }
    }


    fun getApiResponse(): LiveData<Resource<JSONObject>> = apiResponse

}
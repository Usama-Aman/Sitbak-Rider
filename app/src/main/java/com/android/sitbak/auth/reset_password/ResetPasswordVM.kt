package com.android.sitbak.auth.reset_password

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

class ResetPasswordVM : ViewModel(), ResponseCallBack {
    private val apiResponse = MutableLiveData<Resource<JSONObject>>()

    fun resetPassword(call: Call<ResponseBody>) {
        apiResponse.value = Resource.loading()
        RetrofitClient.apiCall(call, this, ApiTags.RESET_PASSWORD)
    }

    override fun onSuccess(jsonObject: JSONObject, tag: String) {
        when (tag) {
            ApiTags.RESET_PASSWORD -> {
                apiResponse.postValue(Resource.success(jsonObject, ApiTags.RESET_PASSWORD))
            }
        }
    }

    override fun onError(error: String, tag: String) {
        when (tag) {
            ApiTags.RESET_PASSWORD -> {
                apiResponse.postValue(Resource.error(error, null, ApiTags.RESET_PASSWORD))
            }
        }
    }

    fun getApiResponse(): LiveData<Resource<JSONObject>> = apiResponse
}
package com.android.sitbak.home.maps

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

class AddAvailabilityActivityVM : ViewModel(), ResponseCallBack {

    private val apiResponse = MutableLiveData<Resource<JSONObject>>()

    fun addAvailability(call: Call<ResponseBody>) {
        apiResponse.value = Resource.loading()
        RetrofitClient.apiCall(call, this, ApiTags.ADD_AVAILABILITY)
    }

    fun getRegions(call: Call<ResponseBody>) {
        apiResponse.value = Resource.loading()
        RetrofitClient.apiCall(call, this, ApiTags.GET_REGION)
    }

    fun updateAvailability(call: Call<ResponseBody>) {
        apiResponse.value = Resource.loading()
        RetrofitClient.apiCall(call, this, ApiTags.UPDATE_AVAILABILITY)
    }

    override fun onSuccess(jsonObject: JSONObject, tag: String) {
        when (tag) {
            ApiTags.ADD_AVAILABILITY -> {
                apiResponse.postValue(Resource.success(jsonObject, ApiTags.ADD_AVAILABILITY))
            }
            ApiTags.GET_REGION -> {
                apiResponse.postValue(Resource.success(jsonObject, ApiTags.GET_REGION))
            }
            ApiTags.UPDATE_AVAILABILITY -> {
                apiResponse.postValue(Resource.success(jsonObject, ApiTags.UPDATE_AVAILABILITY))
            }
        }

    }

    override fun onError(error: String, tag: String) {
        when (tag) {
            ApiTags.ADD_AVAILABILITY -> {
                apiResponse.postValue(Resource.error(error, null, ApiTags.ADD_AVAILABILITY))
            }
            ApiTags.GET_REGION -> {
                apiResponse.postValue(Resource.error(error, null, ApiTags.GET_REGION))
            }
            ApiTags.UPDATE_AVAILABILITY -> {
                apiResponse.postValue(Resource.error(error, null, ApiTags.UPDATE_AVAILABILITY))
            }
        }
    }

    fun getApiResponse(): LiveData<Resource<JSONObject>> = apiResponse
}
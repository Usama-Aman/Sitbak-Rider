package com.android.sitbak.home.scheduling_tab.my_shifts

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

class MyShiftsFragmentVM : ViewModel(), ResponseCallBack {
    private val apiResponse = MutableLiveData<Resource<JSONObject>>()

    fun getMyShifts(call: Call<ResponseBody>) {
        apiResponse.value = Resource.loading()
        RetrofitClient.apiCall(call, this, ApiTags.GET_MY_SHIFTS)
    }

    fun getAvailabilities(call: Call<ResponseBody>) {
        apiResponse.value = Resource.loading()
        RetrofitClient.apiCall(call, this, ApiTags.GET_AVAILABILITIES)
    }

    fun deleteShifts(call: Call<ResponseBody>) {
        apiResponse.value = Resource.loading()
        RetrofitClient.apiCall(call, this, ApiTags.DELETE_SHIFTS)
    }

    override fun onSuccess(jsonObject: JSONObject, tag: String) {
        when (tag) {
            ApiTags.GET_MY_SHIFTS -> {
                apiResponse.postValue(Resource.success(jsonObject, ApiTags.GET_MY_SHIFTS))
            }
            ApiTags.GET_AVAILABILITIES -> {
                apiResponse.postValue(Resource.success(jsonObject, ApiTags.GET_AVAILABILITIES))
            }
            ApiTags.DELETE_SHIFTS -> {
                apiResponse.postValue(Resource.success(jsonObject, ApiTags.DELETE_SHIFTS))
            }
        }
    }

    override fun onError(error: String, tag: String) {
        when (tag) {
            ApiTags.GET_MY_SHIFTS -> {
                apiResponse.postValue(Resource.error(error, null, ApiTags.GET_MY_SHIFTS))
            }
            ApiTags.GET_AVAILABILITIES -> {
                apiResponse.postValue(Resource.error(error, null, ApiTags.GET_AVAILABILITIES))
            }
            ApiTags.DELETE_SHIFTS -> {
                apiResponse.postValue(Resource.error(error, null, ApiTags.DELETE_SHIFTS))
            }
        }
    }

    fun getApiResponse(): LiveData<Resource<JSONObject>> = apiResponse

}
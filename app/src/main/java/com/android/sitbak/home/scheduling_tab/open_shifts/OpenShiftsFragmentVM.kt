package com.android.sitbak.home.scheduling_tab.open_shifts

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

class OpenShiftsFragmentVM : ViewModel(), ResponseCallBack {

    private val apiResponse = MutableLiveData<Resource<JSONObject>>()

    fun getOpenShifts(call: Call<ResponseBody>) {
        apiResponse.value = Resource.loading()
        RetrofitClient.apiCall(call, this, ApiTags.GET_OPEN_SHIFTS)
    }

    fun addOpenShifts(call: Call<ResponseBody>) {
        apiResponse.value = Resource.loading()
        RetrofitClient.apiCall(call, this, ApiTags.ADD_OPEN_SHIFTS)
    }

    override fun onSuccess(jsonObject: JSONObject, tag: String) {
        when (tag) {
            ApiTags.GET_OPEN_SHIFTS -> {
                apiResponse.postValue(Resource.success(jsonObject, ApiTags.GET_OPEN_SHIFTS))
            }
            ApiTags.ADD_OPEN_SHIFTS -> {
                apiResponse.postValue(Resource.success(jsonObject, ApiTags.ADD_OPEN_SHIFTS))
            }
        }
    }

    override fun onError(error: String, tag: String) {
        when (tag) {
            ApiTags.GET_OPEN_SHIFTS -> {
                apiResponse.postValue(Resource.error(error, null, ApiTags.GET_OPEN_SHIFTS))

            }
            ApiTags.ADD_OPEN_SHIFTS -> {
                apiResponse.postValue(Resource.error(error, null, ApiTags.ADD_OPEN_SHIFTS))

            }
        }
    }

    fun getApiResponse(): LiveData<Resource<JSONObject>> = apiResponse

}
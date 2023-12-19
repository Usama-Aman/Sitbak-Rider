package com.android.sitbak.home.completed_shifts

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

class CompletedShiftsVM : ViewModel(), ResponseCallBack {
    private var apiResponse = MutableLiveData<Resource<JSONObject>>()

    fun getCompletedShifts(call: Call<ResponseBody>) {
        apiResponse.value = Resource.loading()
        RetrofitClient.apiCall(call, this, ApiTags.GET_COMPLETED_SHIFTS)
    }

    override fun onSuccess(jsonObject: JSONObject, tag: String) {
        when (tag) {
            ApiTags.GET_COMPLETED_SHIFTS -> {
                apiResponse.postValue(Resource.success(jsonObject, ApiTags.GET_COMPLETED_SHIFTS))
            }
        }
    }

    override fun onError(error: String, tag: String) {
        when (tag) {
            ApiTags.GET_COMPLETED_SHIFTS -> {
                apiResponse.postValue(Resource.error(error, null, ApiTags.GET_COMPLETED_SHIFTS))
            }
        }

    }
    fun getApiResponse(): LiveData<Resource<JSONObject>> = apiResponse
}




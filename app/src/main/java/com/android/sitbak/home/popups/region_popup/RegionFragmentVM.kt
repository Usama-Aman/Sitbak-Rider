package com.android.sitbak.home.popups.region_popup

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

class RegionFragmentVM : ViewModel(), ResponseCallBack {
    private var apiResponse = MutableLiveData<Resource<JSONObject>>()

    fun getRegions(call: Call<ResponseBody>) {
        apiResponse.value = Resource.loading()
        RetrofitClient.apiCall(call, this, ApiTags.GET_REGION)
    }

    override fun onSuccess(jsonObject: JSONObject, tag: String) {
        when (tag) {
            ApiTags.GET_REGION -> {
                apiResponse.postValue(Resource.success(jsonObject, ApiTags.GET_REGION))
            }
        }
    }

    override fun onError(error: String, tag: String) {
        when (tag) {
            ApiTags.GET_REGION -> {
                apiResponse.postValue(Resource.error(error, null, ApiTags.GET_REGION))
            }
        }
    }

    fun getApiResponse(): LiveData<Resource<JSONObject>> = apiResponse
}
package com.android.sitbak.home.earning

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

class YourEarningsVM : ViewModel(), ResponseCallBack {
    private var apiResponse = MutableLiveData<Resource<JSONObject>>()

    fun getYourEarnings(call: Call<ResponseBody>) {
        apiResponse.value = Resource.loading()
        RetrofitClient.apiCall(call, this, ApiTags.GET_YOUR_EARNINGS)
    }

    fun getMoney(call: Call<ResponseBody>) {
        apiResponse.value = Resource.loading()
        RetrofitClient.apiCall(call, this, ApiTags.GET_MONEY)
    }

    override fun onSuccess(jsonObject: JSONObject, tag: String) {
        when (tag) {
            ApiTags.GET_YOUR_EARNINGS -> {
                apiResponse.postValue(Resource.success(jsonObject, ApiTags.GET_YOUR_EARNINGS))
            }
            ApiTags.GET_MONEY -> {
                apiResponse.postValue(Resource.success(jsonObject, ApiTags.GET_MONEY))
            }
        }
    }

    override fun onError(error: String, tag: String) {
        apiResponse.postValue(Resource.error(error, null, ""))
    }

    fun getApiResponse(): LiveData<Resource<JSONObject>> = apiResponse
}





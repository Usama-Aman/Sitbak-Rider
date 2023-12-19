package com.android.sitbak.home.tickets_tab.available

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

class AvailableTicketsVM : ViewModel(), ResponseCallBack {

    private var apiResponse = MutableLiveData<Resource<JSONObject>>()

    fun getAvailableTickets(call: Call<ResponseBody>) {
        apiResponse.value = Resource.loading()
        RetrofitClient.apiCall(call, this, ApiTags.GET_AVAILABLE_TICKETS)
    }

    fun acceptOrder(call: Call<ResponseBody>) {
        apiResponse.value = Resource.loading()
        RetrofitClient.apiCall(call, this, ApiTags.ACCEPT_ORDER)
    }


    override fun onSuccess(jsonObject: JSONObject, tag: String) {
        when (tag) {
            ApiTags.GET_AVAILABLE_TICKETS -> {
                apiResponse.postValue(Resource.success(jsonObject, ApiTags.GET_AVAILABLE_TICKETS))
            }
            ApiTags.ACCEPT_ORDER -> {
                apiResponse.postValue(Resource.success(jsonObject, ApiTags.ACCEPT_ORDER))
            }
        }
    }

    override fun onError(error: String, tag: String) {
        apiResponse.postValue(Resource.error(error, null, ""))
    }

    fun getApiResponse(): LiveData<Resource<JSONObject>> = apiResponse

}
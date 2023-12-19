package com.android.sitbak.home.tickets_tab.processing

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.android.sitbak.base.AppController
import com.android.sitbak.network.ApiTags
import com.android.sitbak.network.ResponseCallBack
import com.android.sitbak.network.RetrofitClient
import com.android.sitbak.network.SocketIO
import com.android.sitbak.utils.Resource
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call

class ProcessingTicketsVM() : ViewModel(), ResponseCallBack {

    private var apiResponse = MutableLiveData<Resource<JSONObject>>()
    private val orderStatusResponse = MutableLiveData<JSONObject>()

    init {
        listenSocket()
    }

    private fun listenSocket() {
        try {
            AppController.socket.on(SocketIO.socketOrderGetStatus) { obj ->
                Log.e("socket order", obj.toString())
                try {
                    val jsonObject = JSONObject(obj.toString())
                    orderStatusResponse.postValue(jsonObject)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getProcessingTickets(call: Call<ResponseBody>) {
        apiResponse.value = Resource.loading()
        RetrofitClient.apiCall(call, this, ApiTags.GET_PROCESSING_TICKETS)
    }

    fun cancelOrders(call: Call<ResponseBody>) {
        apiResponse.value = Resource.loading()
        RetrofitClient.apiCall(call, this, ApiTags.CANCEL_DELIVERY)
    }


    fun startDelivery(call: Call<ResponseBody>) {
        apiResponse.value = Resource.loading()
        RetrofitClient.apiCall(call, this, ApiTags.START_DELIVERY)
    }

    fun completeDelivery(call: Call<ResponseBody>) {
        apiResponse.value = Resource.loading()
        RetrofitClient.apiCall(call, this, ApiTags.COMPLETE_DELIVERY)
    }

    override fun onSuccess(jsonObject: JSONObject, tag: String) {
        when (tag) {
            ApiTags.GET_PROCESSING_TICKETS -> {
                apiResponse.postValue(Resource.success(jsonObject, ApiTags.GET_PROCESSING_TICKETS))
            }
            ApiTags.CANCEL_DELIVERY -> {
                apiResponse.postValue(Resource.success(jsonObject, ApiTags.CANCEL_DELIVERY))
            }
            ApiTags.START_DELIVERY -> {
                apiResponse.postValue(Resource.success(jsonObject, ApiTags.START_DELIVERY))
            }
            ApiTags.COMPLETE_DELIVERY -> {
                apiResponse.postValue(Resource.success(jsonObject, ApiTags.COMPLETE_DELIVERY))
            }
        }
    }

    override fun onError(error: String, tag: String) {
        apiResponse.postValue(Resource.error(error, null, ""))
    }

    fun getApiResponse(): LiveData<Resource<JSONObject>> = apiResponse
    fun getOrderStatusResponse(): LiveData<JSONObject> = orderStatusResponse

}
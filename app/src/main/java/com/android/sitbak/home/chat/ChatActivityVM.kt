package com.android.sitbak.home.chat

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
import com.google.gson.Gson
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call

class ChatActivityVM : ViewModel(), ResponseCallBack {

    private val apiResponse = MutableLiveData<Resource<JSONObject>>()
    private val socketReceiveMessage = MutableLiveData<ChatData>()

    init {
        listenSocket()
    }

    private fun listenSocket() {
        try {

            AppController.socket.on(SocketIO.socketGetMessage) { obj ->
                Log.e("socketReceiveMessage", obj.toString())
                try {
                    val obj = JSONObject(obj.toString())
                    val data = Gson().fromJson(obj.toString(), ChatData::class.java)
                    socketReceiveMessage.postValue(data)

                } catch (e: Exception) {
                    e.printStackTrace()
                }
                null
            }


        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    fun sendMessage(call: Call<ResponseBody>) {
        apiResponse.value = Resource.loading(ApiTags.SEND_CHAT)
        RetrofitClient.apiCall(call, this, ApiTags.SEND_CHAT)
    }

    fun getChats(call: Call<ResponseBody>) {
        apiResponse.value = Resource.loading()
        RetrofitClient.apiCall(call, this, ApiTags.GET_CHAT)
    }

    fun emitChatMessage(jsonObject: JSONObject) {
        try {
            AppController.socket.emit(SocketIO.socketSendMessage, jsonObject)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    override fun onSuccess(jsonObject: JSONObject, tag: String) {
        when (tag) {
            ApiTags.SEND_CHAT -> {
                apiResponse.postValue(Resource.success(jsonObject, ApiTags.SEND_CHAT))
            }
            ApiTags.GET_CHAT -> {
                apiResponse.postValue(Resource.success(jsonObject, ApiTags.GET_CHAT))
            }
        }
    }

    override fun onError(error: String, tag: String) {
        when (tag) {
            ApiTags.SEND_CHAT -> {
                apiResponse.postValue(Resource.error(error, null, ApiTags.SEND_CHAT))
            }
            ApiTags.GET_CHAT -> {
                apiResponse.postValue(Resource.error(error, null, ApiTags.GET_CHAT))
            }
        }
    }

    fun getApiResponse(): LiveData<Resource<JSONObject>> = apiResponse
    fun getSocketReceiveMessage(): LiveData<ChatData> = socketReceiveMessage
}
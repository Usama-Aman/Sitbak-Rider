package com.android.sitbak.home.profile_tab

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

class ProfileFragmentVM : ViewModel(), ResponseCallBack {
    private val apiResponse = MutableLiveData<Resource<JSONObject>>()

    fun switchAvailability(call: Call<ResponseBody>) {
        apiResponse.value = Resource.loading()
        RetrofitClient.apiCall(call, this, ApiTags.SWITCH_AVAILABILITY)
    }

    fun getProfile(call: Call<ResponseBody>) {
        apiResponse.value = Resource.loading()
        RetrofitClient.apiCall(call, this, ApiTags.GET_PROFILE)
    }

    fun updateProfile(call: Call<ResponseBody>) {
        apiResponse.value = Resource.loading()
        RetrofitClient.apiCall(call, this, ApiTags.UPDATE_PROFILE)
    }

    override fun onSuccess(jsonObject: JSONObject, tag: String) {
        when (tag) {
            ApiTags.LOGOUT -> {
                apiResponse.postValue(Resource.success(jsonObject, ApiTags.LOGOUT))
            }
            ApiTags.GET_PROFILE -> {
                apiResponse.postValue(Resource.success(jsonObject, ApiTags.GET_PROFILE))
            }
            ApiTags.SWITCH_AVAILABILITY -> {
                apiResponse.postValue(Resource.success(jsonObject, ApiTags.SWITCH_AVAILABILITY))
            }
            ApiTags.UPDATE_PROFILE -> {
                apiResponse.postValue(Resource.success(jsonObject, ApiTags.UPDATE_PROFILE))
            }
        }
    }

    override fun onError(error: String, tag: String) {
        when (tag) {
            ApiTags.LOGOUT -> {
                apiResponse.postValue(Resource.error(error, null, ApiTags.LOGOUT))
            }
            ApiTags.GET_PROFILE -> {
                apiResponse.postValue(Resource.error(error, null, ApiTags.GET_PROFILE))
            }
            ApiTags.SWITCH_AVAILABILITY -> {
                apiResponse.postValue(Resource.error(error, null, ApiTags.SWITCH_AVAILABILITY))
            }
            ApiTags.UPDATE_PROFILE -> {
                apiResponse.postValue(Resource.error(error, null, ApiTags.UPDATE_PROFILE))
            }
        }
    }

    fun getApiResponse(): LiveData<Resource<JSONObject>> = apiResponse

}
package com.android.sitbak.auth.password_recovery

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

class PasswordRecoveryVM : ViewModel(), ResponseCallBack {

    private var apiResponse = MutableLiveData<Resource<JSONObject>>()


    fun recoverByEmail(call: Call<ResponseBody>) {
        apiResponse.value = Resource.loading()
        RetrofitClient.apiCall(call, this, ApiTags.RECOVER_BY_EMAIL)

    }

    fun updateUserEmail(call: Call<ResponseBody>) {
        apiResponse.value = Resource.loading()
        RetrofitClient.apiCall(call, this, ApiTags.UPDATE_EMAIL)
    }

    override fun onSuccess(jsonObject: JSONObject, tag: String) {
        when (tag) {
            ApiTags.RECOVER_BY_EMAIL -> {
                apiResponse.postValue(Resource.success(jsonObject, ApiTags.RECOVER_BY_EMAIL))
            }
            ApiTags.UPDATE_EMAIL -> {
                apiResponse.postValue(Resource.success(jsonObject, ApiTags.UPDATE_EMAIL))
            }
        }
    }

    override fun onError(error: String, tag: String) {
        when (tag) {
            ApiTags.RECOVER_BY_EMAIL -> {
                apiResponse.postValue(Resource.error(error, null, ApiTags.RECOVER_BY_EMAIL))
            }
            ApiTags.UPDATE_EMAIL -> {
                apiResponse.postValue(Resource.error(error, null, ApiTags.UPDATE_EMAIL))
            }
        }
    }


    fun getApiResponse(): LiveData<Resource<JSONObject>> = apiResponse
}
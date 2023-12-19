package com.android.sitbak.auth.login


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

class LoginVM() : ViewModel(), ResponseCallBack {

    private val apiResponse = MutableLiveData<Resource<JSONObject>>()

    fun loginUser(call: Call<ResponseBody>) {
        apiResponse.value = Resource.loading()
        RetrofitClient.apiCall(call, this, ApiTags.LOGIN)

    }

    override fun onSuccess(jsonObject: JSONObject, tag: String) {
        when (tag) {
            ApiTags.LOGIN -> {
                apiResponse.postValue(Resource.success(jsonObject, ApiTags.LOGIN))
            }
        }
    }


    override fun onError(error: String, tag: String) {
        when (tag) {
            ApiTags.LOGIN -> {
                apiResponse.postValue(Resource.error(error, null, ApiTags.LOGIN))

            }
        }
    }

    fun getApiResponse(): LiveData<Resource<JSONObject>> = apiResponse

}
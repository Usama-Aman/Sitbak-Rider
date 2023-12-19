package com.android.sitbak.auth.add_photo

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


class AddPhotoVM : ViewModel(), ResponseCallBack {

    private val apiResponse = MutableLiveData<Resource<JSONObject>>()


    fun addUpdatePhoto(call: Call<ResponseBody>) {
        apiResponse.value = Resource.loading()
        RetrofitClient.apiCall(call, this, ApiTags.ADD_UPDATE_PHOTO)
    }

    override fun onSuccess(jsonObject: JSONObject, tag: String) {
        when (tag) {

            ApiTags.ADD_UPDATE_PHOTO -> {
                apiResponse.postValue(Resource.success(jsonObject, ApiTags.ADD_UPDATE_PHOTO))
            }
        }
    }

    override fun onError(error: String, tag: String) {
        when (tag) {

            ApiTags.ADD_UPDATE_PHOTO -> {
                apiResponse.postValue(Resource.error(error, null, ApiTags.ADD_UPDATE_PHOTO))
            }
        }
    }

    fun getApiResponse(): LiveData<Resource<JSONObject>> = apiResponse

}
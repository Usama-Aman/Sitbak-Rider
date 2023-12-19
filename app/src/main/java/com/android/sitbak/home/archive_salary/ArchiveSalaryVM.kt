package com.android.sitbak.home.archive_salary

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

class ArchiveSalaryVM : ViewModel(), ResponseCallBack {
    private var apiResponse = MutableLiveData<Resource<JSONObject>>()

    fun getArchiveSalary(call: Call<ResponseBody>) {
        apiResponse.value = Resource.loading()
        RetrofitClient.apiCall(call, this, ApiTags.GET_ARCHIVE_SALARY)
    }

    override fun onSuccess(jsonObject: JSONObject, tag: String) {
        when (tag) {
            ApiTags.GET_ARCHIVE_SALARY -> {
                apiResponse.postValue(Resource.success(jsonObject, ApiTags.GET_ARCHIVE_SALARY))
            }
        }
    }

    override fun onError(error: String, tag: String) {
        when (tag) {
            ApiTags.GET_ARCHIVE_SALARY -> {
                apiResponse.postValue(Resource.error(error, null, ApiTags.GET_ARCHIVE_SALARY))
            }
        }

    }

    fun getApiResponse(): LiveData<Resource<JSONObject>> = apiResponse
}




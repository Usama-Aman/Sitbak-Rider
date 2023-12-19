package com.android.sitbak.utils


data class Resource<out T>(val status: ApiStatus, val data: T?, val message: String?, val tag: String?) {

    companion object {

        fun <T> success(data: T?, tag: String): Resource<T> {
            return Resource(ApiStatus.SUCCESS, data, null, tag)
        }

        fun <T> error(msg: String, data: T?, tag: String): Resource<T> {
            return Resource(ApiStatus.ERROR, data, msg, tag)
        }

        fun <T> loading(tag : String? = null): Resource<T> {
            return Resource(ApiStatus.LOADING, null, null, tag)
        }

    }

}
package com.android.sitbak.utils

import android.content.Context
import android.widget.Toast

object Common {


    fun showToast(context: Context,message:String){
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}
package com.android.sitbak.utils

import android.app.Activity
import android.content.Context
import android.graphics.Typeface
import android.net.ConnectivityManager
import android.os.Handler
import android.os.Looper
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import com.android.sitbak.R
import com.android.sitbak.auth.login.LoginData
import com.google.gson.Gson
import com.tapadoo.alerter.Alerter


object AppUtils {


    fun hideKeyBoardFromEditText(editText: EditText, context: Context) {
        val imm: InputMethodManager =
            context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(editText.windowToken, 0)
    }


    fun showToast(activity: Activity, text: String, isSuccess: Boolean) {
        if (isSuccess) {
            Alerter.create(activity)
                .setTitle(activity.resources.getString(R.string.toast_success))
                .setTitleTypeface(Typeface.createFromAsset(activity.assets, "inter_bold.ttf"))
                .setTextTypeface(Typeface.createFromAsset(activity.assets, "inter_regular.ttf"))
                .setText(text)
                .setIcon(R.drawable.ic_toast_success)
                .setBackgroundColorRes(R.color.green_900)
                .setDuration(1000)
                .show()
        } else {
            Alerter.create(activity)
                .setTitle(activity.resources.getString(R.string.toast_error))
                .setTitleTypeface(Typeface.createFromAsset(activity.assets, "inter_bold.ttf"))
                .setTextTypeface(Typeface.createFromAsset(activity.assets, "inter_regular.ttf"))
                .setText(text)
                .setIcon(R.drawable.ic_toast_error)
                .setBackgroundColorRes(R.color.red)
                .setDuration(1000)
                .show()
        }
    }

    fun isNetworkAvailable(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val netInfo = cm.activeNetworkInfo
        return (netInfo != null && netInfo.isConnectedOrConnecting
                && cm.activeNetworkInfo!!.isAvailable
                && cm.activeNetworkInfo!!.isConnected)
    }

    fun preventDoubleClick(view: View) {
        view.isEnabled = false
        Handler(Looper.getMainLooper()).postDelayed({
            view.isEnabled = true
        }, 200)
    }

    fun touchScreenEnableDisable(context: Context, isTouchEnable: Boolean) {
        if (isTouchEnable)
            (context as Activity).window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        else
            (context as Activity).window.setFlags(
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
            )
    }

    fun hideShowPassword(editText: EditText, toggle: ImageView) {
        if (editText.transformationMethod.equals(PasswordTransformationMethod.getInstance())) {
            toggle.setImageResource(R.drawable.ic_hide_password);
            editText.transformationMethod = HideReturnsTransformationMethod.getInstance();
        } else {
            toggle.setImageResource(R.drawable.ic_show_password);
            editText.transformationMethod = PasswordTransformationMethod.getInstance();
        }
        editText.setSelection(editText.text.toString().length)
    }

    fun getUserDetails(context: Context): LoginData {
        val data = SharedPreference.getSimpleString(context, Constants.userModel)
        return Gson().fromJson(data, LoginData::class.java)
    }

    fun saveUserModel(mContext: Context, data: LoginData) {
        SharedPreference.saveSimpleString(mContext, Constants.userModel, Gson().toJson(data))
    }


}
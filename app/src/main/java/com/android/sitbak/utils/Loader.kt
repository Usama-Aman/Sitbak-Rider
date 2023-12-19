package com.android.sitbak.utils

import android.annotation.SuppressLint
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import com.kaopiz.kprogresshud.KProgressHUD
import java.lang.Exception

@SuppressLint("StaticFieldLeak")
class Loader {

    companion object {
        var mContext: Context? = null
        var progressKHUD: KProgressHUD? = null
        fun showLoader(context: Context) {
            try {

                if (progressKHUD != null && progressKHUD!!.isShowing) {
                    return
                }
                mContext = context
                AppUtils.touchScreenEnableDisable(mContext!!, false)

                if ((context as AppCompatActivity).isFinishing)
                    return
                try {
                    if (!(context).isFinishing) {
                        progressKHUD = KProgressHUD.create(context)
                            .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                            .setCancellable(true)
                            .setAnimationSpeed(2)
                            .setDimAmount(10f).show()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }

        fun hideLoader() {
            try {
                AppUtils.touchScreenEnableDisable(mContext!!, true)
                if (progressKHUD != null && progressKHUD!!.isShowing) {
                    progressKHUD!!.dismiss()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

}
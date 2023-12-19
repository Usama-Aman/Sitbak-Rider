package com.android.sitbak.network

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.util.Log


class NetworkChangeReceiver(val connectivityReceiverListener: ConnectivityReceiverListener) : BroadcastReceiver() {


    override fun onReceive(context: Context?, intent: Intent?) {
        try {
            if (isOnline(context!!)) {
                Log.e("InternetCheck", "Online Connect Internet ")
            } else {
                Log.e("InternetCheck", "Connectivity Failure !!! ")
            }
            connectivityReceiverListener.onNetworkConnectionChanged(isOnline(context))
        } catch (e: NullPointerException) {
            e.printStackTrace()
        }
    }

    private fun isOnline(context: Context): Boolean {
        return try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val netInfo = cm.activeNetworkInfo
            //should check null because in airplane mode it will be null
            netInfo != null && netInfo.isConnected
        } catch (e: java.lang.NullPointerException) {
            e.printStackTrace()
            false
        }
    }

    interface ConnectivityReceiverListener {
        fun onNetworkConnectionChanged(isConnected: Boolean)
    }


}
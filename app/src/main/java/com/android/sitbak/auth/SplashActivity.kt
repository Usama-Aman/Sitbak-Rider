package com.android.sitbak.auth

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.android.sitbak.R
import com.android.sitbak.auth.login.LoginActivity
import com.android.sitbak.base.AppController
import com.android.sitbak.base.BaseActivity
import com.android.sitbak.home.HomeActivity
import com.android.sitbak.utils.Constants
import com.android.sitbak.utils.SharedPreference

class SplashActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        changeStatusBarColor(R.color.heavy_metal)

        Handler(Looper.getMainLooper()).postDelayed({
            if (SharedPreference.getBoolean(this, Constants.isUserLoggedIn))
                startActivity(Intent(this, HomeActivity::class.java))
            else {
                getLinkFromIntent(intent)
            }
            finish()
        }, 2000)

    }

    private fun getLinkFromIntent(intent: Intent?) {
        val data = intent?.data
        AppController.inviteReferralCode = data?.getQueryParameter(Constants.referralCode).toString()
        startActivity(Intent(this, LoginActivity::class.java))
    }


    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        getLinkFromIntent(intent)
    }

}
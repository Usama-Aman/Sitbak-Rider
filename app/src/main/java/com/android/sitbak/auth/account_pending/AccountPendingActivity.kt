package com.android.sitbak.auth.account_pending

import android.content.Intent
import android.os.Bundle
import com.android.sitbak.R
import com.android.sitbak.base.BaseActivity
import com.android.sitbak.databinding.ActivityAccountPendingBinding
import com.android.sitbak.home.HomeActivity
import com.android.sitbak.utils.Constants
import com.android.sitbak.utils.SharedPreference
import com.astritveliu.boom.Boom

class AccountPendingActivity : BaseActivity() {
    private lateinit var binding: ActivityAccountPendingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAccountPendingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        changeStatusBarColor(R.color.heavy_metal)
        initListener()


    }

    private fun initListener() {
        Boom(binding.btnContinue)
        binding.btnContinue.setOnClickListener {
            intent = Intent(this, HomeActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            SharedPreference.saveBoolean(this, Constants.isUserLoggedIn, true)
            startActivity(intent)
        }

        binding.ivBack.setOnClickListener {
            finish()
        }

    }
}
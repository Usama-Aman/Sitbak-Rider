package com.android.sitbak.auth.verification_successful

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.android.sitbak.R
import com.android.sitbak.auth.account_pending.AccountPendingActivity
import com.android.sitbak.auth.add_photo.AddYourPhotoActivity
import com.android.sitbak.auth.license_verification.LicenseVerificationActivity
import com.android.sitbak.auth.otp.VerifyOtpActivity
import com.android.sitbak.auth.phone_number.PhoneVerification
import com.android.sitbak.base.BaseActivity
import com.android.sitbak.databinding.ActivityVerificationSuccessfulBinding
import com.android.sitbak.home.HomeActivity
import com.android.sitbak.utils.AppUtils
import com.android.sitbak.utils.Constants
import com.android.sitbak.utils.SharedPreference
import com.astritveliu.boom.Boom

class VerificationSuccessfulActivity : BaseActivity() {
    private lateinit var binding: ActivityVerificationSuccessfulBinding
    private lateinit var mContext: Context
    var type = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVerificationSuccessfulBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mContext = this
        changeStatusBarColor(R.color.heavy_metal)
        initVar()
        listener()
    }

    private fun initVar() {
        Boom(binding.btnNext)
        type = intent.getStringExtra("type").toString()

        when (type) {
            "email" -> {
                binding.ivSuccess.setImageResource(R.drawable.ic_email_success)
                binding.tvSuccessTitle.text = resources.getString(R.string.email_verified)
                binding.tvSuccessDescription.text = resources.getString(R.string.your_email_address_has_been_verified)
            }
            "phone" -> {
                binding.ivSuccess.setImageResource(R.drawable.ic_phone_success)
                binding.tvSuccessTitle.text = resources.getString(R.string.phone_number_verified)
                binding.tvSuccessDescription.text = resources.getString(R.string.your_phone_number_has_been_verified)
            }
        }
    }

    private fun listener() {
        binding.ivBack.setOnClickListener {
            onBackPressed()
        }
        binding.btnNext.setOnClickListener {
            val data = AppUtils.getUserDetails(mContext)
            val intent: Intent
            when {
                data.email_verified_at == null -> {
                    intent = Intent(mContext, VerifyOtpActivity::class.java)
                    intent.putExtra("type", "email")
                    intent.putExtra("phoneOrEmail", data.email)
                }
                data.phone_verified_at == null -> {
                    intent = Intent(mContext, PhoneVerification::class.java)
                    intent.putExtra("type", "phone")

                }
                data.photo_path == null -> {
                    intent = Intent(mContext, AddYourPhotoActivity::class.java)
                }
                data.id_photo_path == null -> {
                    intent = Intent(mContext, LicenseVerificationActivity::class.java)
                }
                data.is_active == 0 -> {
                    intent = Intent(mContext, AccountPendingActivity::class.java)
                }
                else -> {
                    intent = Intent(mContext, HomeActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    SharedPreference.saveBoolean(mContext, Constants.isUserLoggedIn, true)
                }
            }
            Handler(Looper.getMainLooper()).postDelayed({
                startActivity(intent)
                finish()
            }, 1000)
        }

    }
}
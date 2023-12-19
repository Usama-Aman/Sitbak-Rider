package com.android.sitbak.auth.password_recovery

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.ViewModelProviders
import com.android.sitbak.R
import com.android.sitbak.auth.otp.VerifyOtpActivity
import com.android.sitbak.auth.phone_number.PhoneVerification
import com.android.sitbak.base.AppController
import com.android.sitbak.base.BaseActivity
import com.android.sitbak.base.ViewModelFactory
import com.android.sitbak.databinding.ActivityDriverPasswordRecoveryBinding
import com.android.sitbak.network.Api
import com.android.sitbak.network.ApiTags
import com.android.sitbak.network.RetrofitClient
import com.android.sitbak.utils.*
import com.astritveliu.boom.Boom
import okhttp3.ResponseBody
import retrofit2.Call

class PasswordRecovery : BaseActivity() {

    lateinit var binding: ActivityDriverPasswordRecoveryBinding
    private lateinit var viewModel: PasswordRecoveryVM
    private lateinit var mContext: Context
    private lateinit var retrofitClient: Api
    private lateinit var apiCall: Call<ResponseBody>

    private var fromProfile = false
    private var type = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDriverPasswordRecoveryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mContext = this
        retrofitClient = RetrofitClient.getClient(mContext).create(Api::class.java)

        changeStatusBarColor(R.color.heavy_metal)

        initVM()
        initObservers()
        listeners()
        initViews()
    }

    private fun initViews() {
        Boom(binding.btnSend)
        Boom(binding.btnRecoverByPhone)

        type = intent.getStringExtra("type").toString()
        fromProfile = intent.getBooleanExtra("fromProfile", false)

        if (fromProfile) {
            binding.llRecoverByPhone.viewGone()
            binding.tvTitle.text = getString(R.string.change_email)
            binding.tvDescription.viewGone()
            binding.btnSend.text = getString(R.string.change_email)

            val userData = AppUtils.getUserDetails(mContext)
            binding.etEmail.setText(userData.email)

        } else {
            binding.llRecoverByPhone.viewVisible()
            binding.tvTitle.text = getString(R.string.recovery_by_email)
            binding.tvDescription.viewVisible()
            binding.btnSend.text = getString(R.string.send)
        }
    }

    private fun initVM() {
        viewModel = ViewModelProviders.of(this, ViewModelFactory()).get(PasswordRecoveryVM::class.java)
        binding.viewModels = this.viewModel

        binding.executePendingBindings()
        binding.lifecycleOwner = this
    }

    private fun initObservers() {
        viewModel.getApiResponse().observe(this, {
            when (it.status) {
                ApiStatus.LOADING -> {
                    Loader.showLoader(mContext)
                    Loader.progressKHUD?.setCancellable {
                        if (this::apiCall.isInitialized)
                            apiCall.cancel()
                    }
                }
                ApiStatus.ERROR -> {
                    Loader.hideLoader()
                    AppUtils.showToast(this, it.message!!, false)
                }
                ApiStatus.SUCCESS -> {
                    Loader.hideLoader()
                    when (it.tag) {
                        ApiTags.RECOVER_BY_EMAIL -> {
                            AppUtils.showToast(this, it.data?.getString("message")!!, true)
                            val data = it.data.getJSONObject("data")
                            AppController.resetOTP = data.getInt("otp")
                            val intent = Intent(mContext, VerifyOtpActivity::class.java)
                            intent.putExtra("phoneOrEmail", binding.etEmail.text.toString())
                            intent.putExtra("type", "resetPasswordByEmail")
                            intent.putExtra("fromProfile", fromProfile)
                            Handler(Looper.getMainLooper()).postDelayed({
                                startActivity(intent)
                                finish()
                            }, 1000)
                        }
                        ApiTags.UPDATE_EMAIL -> {
                            AppUtils.showToast(this, it.data?.getString("message")!!, true)
                            val intent = Intent(mContext, VerifyOtpActivity::class.java)
                            intent.putExtra("phoneOrEmail", binding.etEmail.text.toString())
                            intent.putExtra("type", type)
                            intent.putExtra("fromProfile", fromProfile)
                            Handler(Looper.getMainLooper()).postDelayed({
                                startActivity(intent)
                                finish()
                            }, 1000)
                        }
                    }
                }
            }
        })
    }

    private fun listeners() {

        binding.ivBack.setOnClickListener {
            super.onBackPressed()
        }
        binding.btnSend.setOnClickListener {
            if (validate()) {
                when (type) {
                    "changeEmail" -> {
                        apiCall = retrofitClient.updateUserEmail(binding.etEmail.text.toString())
                        viewModel.updateUserEmail(apiCall)
                    }
                    "forgotPassword" -> {
                        apiCall = retrofitClient.senOTPForgotPassword("", binding.etEmail.text.toString(), "email")
                        viewModel.recoverByEmail(apiCall)
                    }
                }
            }
        }
        binding.btnRecoverByPhone.setOnClickListener {
            val intent = Intent(this@PasswordRecovery, PhoneVerification::class.java)
            intent.putExtra("type", "resetPasswordByPhone")
            intent.putExtra("fromProfile", fromProfile)
            startActivity(intent)
        }
    }

    private fun validate(): Boolean {
        if (binding.etEmail.text.toString().isNullOrEmpty()) {
            AppUtils.showToast(this, "Email is required!", false)
            return false
        } else if (!binding.etEmail.text.toString().isValidEmail()) {
            AppUtils.showToast(this, "Email is invalid!", false)
            return false
        }
        return true
    }

}
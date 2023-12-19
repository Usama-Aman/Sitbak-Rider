package com.android.sitbak.auth.otp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import androidx.lifecycle.ViewModelProviders
import com.android.sitbak.R
import com.android.sitbak.auth.reset_password.ResetPasswordActivity
import com.android.sitbak.auth.verification_successful.VerificationSuccessfulActivity
import com.android.sitbak.base.AppController
import com.android.sitbak.base.BaseActivity
import com.android.sitbak.databinding.ActivityVerifyOtpBinding
import com.android.sitbak.network.Api
import com.android.sitbak.network.ApiTags
import com.android.sitbak.network.RetrofitClient
import com.android.sitbak.utils.*
import com.astritveliu.boom.Boom
import okhttp3.ResponseBody
import retrofit2.Call

class VerifyOtpActivity : BaseActivity() {

    private lateinit var binding: ActivityVerifyOtpBinding
    private lateinit var viewModel: VerifyOtpVM
    private lateinit var mContext: Context
    private lateinit var retrofitClient: Api
    private lateinit var apiCall: Call<ResponseBody>

    var type = ""
    var phoneOrEmail = ""
    var fromProfile = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVerifyOtpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mContext = this
        retrofitClient = RetrofitClient.getClient(mContext).create(Api::class.java)
        changeStatusBarColor(R.color.heavy_metal)

        initVar()
        initVM()
        initObservers()
        listener()

    }

    private fun initVar() {
        Boom(binding.btnVerifyNow)
        Boom(binding.tvResend)

        type = intent.getStringExtra("type").toString()
        phoneOrEmail = intent.getStringExtra("phoneOrEmail").toString()
        fromProfile = intent.getBooleanExtra("fromProfile", false)

        if (type == "resetPasswordByEmail" || type == "resetPasswordByPhone" || type == "phone" || fromProfile) {
            binding.tvResend.viewGone()
        } else {
            binding.tvResend.viewVisible()
        }

        binding.tvEmail.text = phoneOrEmail
    }

    private fun initVM() {
        viewModel = ViewModelProviders.of(this, ViewModelFactory()).get(VerifyOtpVM::class.java)
        binding.viewModel = viewModel

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
                        ApiTags.EMAIL_PHONE_CONFIRMATION -> {
                            when (type) {
                                "email", "phone" -> {
                                    handleResponse(it?.data?.getString("message")!!)
                                }
                                "changePhone" -> {
                                    AppUtils.showToast(this, it?.data?.getString("message")!!, true)
                                    val userData = AppUtils.getUserDetails(mContext)
                                    userData.phone_number = phoneOrEmail
                                    AppUtils.saveUserModel(mContext, userData)
                                    Handler(Looper.getMainLooper()).postDelayed({
                                        finish()
                                    }, 1000)
                                }
                                "changeEmail" -> {
                                    AppUtils.showToast(this, it?.data?.getString("message")!!, true)
                                    val userData = AppUtils.getUserDetails(mContext)
                                    userData.email = phoneOrEmail
                                    AppUtils.saveUserModel(mContext, userData)
                                    Handler(Looper.getMainLooper()).postDelayed({
                                        finish()
                                    }, 1000)
                                }
                            }
                        }
                        ApiTags.RESEND_OTP -> {
                            AppUtils.showToast(this, it?.data?.getString("message")!!, true)
                        }
                    }
                }
            }
        })
    }

    private fun handleResponse(message: String) {
        AppUtils.showToast(this, message, true)

        val data = AppUtils.getUserDetails(mContext)
        if (type == "email")
            data.email_verified_at = "verified"
        if (type == "phone")
            data.phone_verified_at = "verified"
        AppUtils.saveUserModel(mContext, data)

        val intent = Intent(mContext, VerificationSuccessfulActivity::class.java)
        intent.putExtra("type", type)
        intent.putExtra("phoneOrEmail", phoneOrEmail)
        SharedPreference.saveBoolean(mContext, Constants.isUserLoggedIn, true)

        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(intent)
            finish()
        }, 1000)
    }

    private fun listener() {
        binding.btnVerifyNow.setOnClickListener {
            when (type) {
                "email", "phone" -> {
                    apiCall = retrofitClient.emailPhoneConfirmation(type, binding.etCode.text.toString())
                    viewModel.emailConfirmation(apiCall)
                }
                "changePhone" -> {
                    apiCall = retrofitClient.emailPhoneConfirmation("phone", binding.etCode.text.toString())
                    viewModel.emailConfirmation(apiCall)
                }
                "changeEmail" -> {
                    apiCall = retrofitClient.emailPhoneConfirmation("email", binding.etCode.text.toString())
                    viewModel.emailConfirmation(apiCall)
                }
                "resetPasswordByEmail" -> {
                    if (AppController.resetOTP.toString() == binding.etCode.text.toString()) {
                        val intent = Intent(mContext, ResetPasswordActivity::class.java)
                        intent.putExtra("type", "email")
                        intent.putExtra("phoneOrEmail", phoneOrEmail)
                        startActivity(intent)
                        finish()
                    } else {
                        AppUtils.showToast(this, "OTP is invalid", false)
                    }
                }
                "resetPasswordByPhone" -> {
                    if (AppController.resetOTP.toString() == binding.etCode.text.toString()) {
                        val intent = Intent(mContext, ResetPasswordActivity::class.java)
                        intent.putExtra("type", "phone_number")
                        intent.putExtra("phoneOrEmail", phoneOrEmail)
                        startActivity(intent)
                        finish()
                    } else {
                        AppUtils.showToast(this, "OTP is invalid", false)
                    }
                }
            }
        }

        binding.tvResend.setOnClickListener {
            apiCall = retrofitClient.sendPhoneOTP(phoneOrEmail, phoneOrEmail, type)
            viewModel.resendOTP(apiCall)
        }

        binding.ivBack.setOnClickListener {
            onBackPressed()
        }

        binding.etCode.addTextChangedListener(
            object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    if (s != null)
                        setOtpNumbers(otp = s.toString())
                    else
                        setOtpNumbers(otp = "")
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                }
            })
    }

    private fun setOtpNumbers(otp: String) {
        when {
            otp.isEmpty() -> {
                binding.tvDigit1.text = ""
                binding.tvDigit2.text = ""
                binding.tvDigit3.text = ""
                binding.tvDigit4.text = ""
            }
            otp.length == 1 -> {
                binding.tvDigit1.text = otp
                binding.tvDigit2.text = ""
                binding.tvDigit3.text = ""
                binding.tvDigit4.text = ""
            }
            otp.length == 2 -> {
                binding.tvDigit1.text = otp.substring(0, 1)
                binding.tvDigit2.text = otp.substring(1, 2)
                binding.tvDigit3.text = ""
                binding.tvDigit4.text = ""
            }
            otp.length == 3 -> {
                binding.tvDigit1.text = otp.substring(0, 1)
                binding.tvDigit2.text = otp.substring(1, 2)
                binding.tvDigit3.text = otp.substring(2, 3)
                binding.tvDigit4.text = ""
            }
            otp.length == 4 -> {
                binding.tvDigit1.text = otp.substring(0, 1)
                binding.tvDigit2.text = otp.substring(1, 2)
                binding.tvDigit3.text = otp.substring(2, 3)
                binding.tvDigit4.text = otp.substring(3, 4)
            }
        }
    }
}
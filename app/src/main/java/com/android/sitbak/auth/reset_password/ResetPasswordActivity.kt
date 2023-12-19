package com.android.sitbak.auth.reset_password

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.ViewModelProviders
import com.android.sitbak.R
import com.android.sitbak.auth.login.LoginActivity
import com.android.sitbak.base.AppController
import com.android.sitbak.base.BaseActivity
import com.android.sitbak.base.ViewModelFactory
import com.android.sitbak.databinding.ActivityResetPasswordBinding
import com.android.sitbak.network.Api
import com.android.sitbak.network.ApiTags
import com.android.sitbak.network.RetrofitClient
import com.android.sitbak.utils.*
import com.astritveliu.boom.Boom
import okhttp3.ResponseBody
import retrofit2.Call

class ResetPasswordActivity : BaseActivity() {
    private lateinit var binding: ActivityResetPasswordBinding
    private lateinit var viewModel: ResetPasswordVM
    private lateinit var retrofitClient: Api
    private lateinit var mContext: Context
    private lateinit var apiCall: Call<ResponseBody>

    private var fromProfile = false
    private var type = ""
    private var phoneOrEmail = ""
    private var otp = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResetPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mContext = this
        retrofitClient = RetrofitClient.getClient(mContext).create(Api::class.java)

        changeStatusBarColor(R.color.heavy_metal)
        onSetupViewGroup(binding.content)

        initVM()
        initObservers()
        initVar()
        listener()
    }

    private fun initVar() {
        Boom(binding.btnResetPassword)
        type = intent.getStringExtra("type").toString()
        phoneOrEmail = intent.getStringExtra("phoneOrEmail").toString()
        otp = AppController.resetOTP.toString()
        Log.i("otp", otp)

    }

    private fun initVM() {
        viewModel = ViewModelProviders.of(this, ViewModelFactory()).get(ResetPasswordVM::class.java)
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
                        ApiTags.RESET_PASSWORD -> {
                            when (type) {
                                "email" -> {
                                    AppUtils.showToast(this, it.data?.getString("message")!!, true)
                                    SharedPreference.saveBoolean(this, Constants.isUserLoggedIn, false)
                                    val intent = Intent(this, LoginActivity::class.java)
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                    Handler(Looper.getMainLooper()).postDelayed({
                                        startActivity(intent)
                                        finish()
                                    }, 1000)
                                }
                                "phone_number" -> {
                                    AppUtils.showToast(this, it.data?.getString("message")!!, true)
                                    SharedPreference.saveBoolean(this, Constants.isUserLoggedIn, false)
                                    val intent = Intent(this, LoginActivity::class.java)
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                    Handler(Looper.getMainLooper()).postDelayed({
                                        startActivity(intent)
                                        finish()
                                    }, 1000)
                                }
                            }

                        }
                    }
                }
            }
        })
    }


    private fun listener() {
        binding.ivBack.setOnClickListener {
            onBackPressed()
        }
        binding.ivPasswordToggle.setOnClickListener { AppUtils.hideShowPassword(binding.etPassword, binding.ivPasswordToggle) }
        binding.ivConfirmPasswordToggle.setOnClickListener { AppUtils.hideShowPassword(binding.etConfirmPassword, binding.ivConfirmPasswordToggle) }

        binding.btnResetPassword.setOnClickListener {
            if (validate()) {
                apiCall = retrofitClient.resetPassword(
                    binding.etPassword.text.toString(), binding.etConfirmPassword.text.toString(),
                    phoneOrEmail, phoneOrEmail, otp, type
                )
                viewModel.resetPassword(apiCall)
            }
        }
    }

    private fun validate(): Boolean {
        if (binding.etPassword.text.toString().isNullOrEmpty()) {
            AppUtils.showToast(this, "Password is required!", false)
            return false
        } else if (binding.etPassword.text.toString().length < 8) {
            AppUtils.showToast(this, "Password must be at least 8 digits!", false)
            return false
        } else if (binding.etConfirmPassword.text.toString() != binding.etPassword.text.toString()) {
            AppUtils.showToast(this, "Password does'nt match!", false)
            return false
        }
        return true
    }
}
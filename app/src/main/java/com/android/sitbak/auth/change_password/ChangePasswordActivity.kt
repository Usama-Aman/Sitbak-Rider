package com.android.sitbak.auth.change_password

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.ViewModelProviders
import com.android.sitbak.R
import com.android.sitbak.base.BaseActivity
import com.android.sitbak.base.ViewModelFactory
import com.android.sitbak.databinding.ActivityChangePasswordBinding
import com.android.sitbak.network.Api
import com.android.sitbak.network.ApiTags
import com.android.sitbak.network.RetrofitClient
import com.android.sitbak.utils.ApiStatus
import com.android.sitbak.utils.AppUtils
import com.android.sitbak.utils.Loader
import com.astritveliu.boom.Boom
import okhttp3.ResponseBody
import retrofit2.Call

class ChangePasswordActivity : BaseActivity() {
    private lateinit var binding: ActivityChangePasswordBinding
    private lateinit var viewModel: ChangePasswordVM
    private lateinit var retrofitClient: Api
    private lateinit var mContext: Context
    private lateinit var apiCall: Call<ResponseBody>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChangePasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mContext = this
        retrofitClient = RetrofitClient.getClient(mContext).create(Api::class.java)

        changeStatusBarColor(R.color.heavy_metal)
        onSetupViewGroup(binding.content)

        initVM()
        initObservers()
        initViews()
        initListeners()
    }

    private fun initViews() {
        Boom(binding.btnChangePassword)
    }

    private fun initVM() {
        viewModel = ViewModelProviders.of(this, ViewModelFactory()).get(ChangePasswordVM::class.java)
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
                        ApiTags.CHANGE_PASSWORD -> {
                            AppUtils.showToast(this, it.data?.getString("message")!!, true)
                            Handler(Looper.getMainLooper()).postDelayed({
                                finish()
                            }, 1000)
                        }
                    }
                }
            }
        })
    }

    private fun initListeners() {
        binding.ivOldPasswordToggle.setOnClickListener { AppUtils.hideShowPassword(binding.etOldPassword, binding.ivOldPasswordToggle) }
        binding.ivNewPasswordToggle.setOnClickListener { AppUtils.hideShowPassword(binding.etNewPassword, binding.ivNewPasswordToggle) }
        binding.ivConfirmPasswordToggle.setOnClickListener { AppUtils.hideShowPassword(binding.etConfirmPassword, binding.ivConfirmPasswordToggle) }

        binding.ivBack.setOnClickListener {
            onBackPressed()
        }

        binding.btnChangePassword.setOnClickListener {
            if (validate()) {
                apiCall = retrofitClient.updateUserPassword(
                    binding.etOldPassword.text.toString(),
                    binding.etNewPassword.text.toString(),
                    binding.etConfirmPassword.text.toString()
                )
                viewModel.changePassword(apiCall)
            }
        }
    }

    private fun validate(): Boolean {
        if (binding.etNewPassword.text.toString().length < 8) {
            AppUtils.showToast(mContext as Activity, "Password must be at least 8 digits!", false)
            return false
        } else if (binding.etOldPassword.text.toString() == binding.etNewPassword.text.toString()) {
            AppUtils.showToast(mContext as Activity, "New password cannot be same as current password!", false)
            return false
        } else if (binding.etConfirmPassword.text.toString() != binding.etNewPassword.text.toString()) {
            AppUtils.showToast(mContext as Activity, "Password doesn't match!", false)
            return false
        }

        return true
    }
}
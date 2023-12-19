package com.android.sitbak.auth.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.biometric.BiometricPrompt
import androidx.lifecycle.ViewModelProviders
import com.android.sitbak.R
import com.android.sitbak.auth.account_pending.AccountPendingActivity
import com.android.sitbak.auth.add_photo.AddYourPhotoActivity
import com.android.sitbak.auth.license_verification.LicenseVerificationActivity
import com.android.sitbak.auth.otp.VerifyOtpActivity
import com.android.sitbak.auth.password_recovery.PasswordRecovery
import com.android.sitbak.auth.phone_number.PhoneVerification
import com.android.sitbak.auth.signup.SignUpActivity
import com.android.sitbak.base.BaseActivity
import com.android.sitbak.base.ViewModelFactory
import com.android.sitbak.biometric.BioMetric
import com.android.sitbak.databinding.ActivityDriverLoginBinding
import com.android.sitbak.home.HomeActivity
import com.android.sitbak.network.Api
import com.android.sitbak.network.ApiTags
import com.android.sitbak.network.RetrofitClient
import com.android.sitbak.utils.*
import com.astritveliu.boom.Boom
import com.google.gson.Gson
import okhttp3.ResponseBody
import retrofit2.Call


class LoginActivity : BaseActivity() {

    private lateinit var binding: ActivityDriverLoginBinding
    private lateinit var viewModel: LoginVM
    private lateinit var mContext: Context
    private lateinit var retrofitClient: Api
    private lateinit var apiCall: Call<ResponseBody>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDriverLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mContext = this
        retrofitClient = RetrofitClient.getClientNoToken(mContext).create(Api::class.java)


        changeStatusBarColor(R.color.heavy_metal)
        onSetupViewGroup(binding.loginMainConstraint)

        initVM()
        initObserver()
        listener()


        Boom(binding.btnSignIn)
        Boom(binding.tvSignUp)
        Boom(binding.tvForgotPassword)
    }

    private fun initVM() {
        viewModel = ViewModelProviders.of(this, ViewModelFactory()).get(LoginVM::class.java)
        binding.viewModel = this.viewModel
        binding.lifecycleOwner = this
        binding.executePendingBindings()
    }

    private fun initObserver() {
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
                        ApiTags.LOGIN -> {
                            val model = Gson().fromJson(it.data.toString(), LoginModel::class.java)
                            AppUtils.showToast(this, model.message, true)

                            val data = model.data
                            SharedPreference.saveSimpleString(
                                mContext,
                                Constants.accessToken,
                                model.access_token
                            )
                            AppUtils.saveUserModel(mContext, model.data)
                            checkForUserData(data)
                        }
                    }
                }
            }
        })
    }

    private fun checkForUserData(data: LoginData) {
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
        }, 1000)
    }

    private fun listener() {
        binding.ivPasswordToggle.setOnClickListener {
            AppUtils.hideShowPassword(binding.etPassword, binding.ivPasswordToggle)
        }

        binding.tvForgotPassword.setOnClickListener {
            val intent = Intent(mContext, PasswordRecovery::class.java)
            intent.putExtra("type", "forgotPassword")
            intent.putExtra("fromProfile", false)
            startActivity(intent)
        }

        binding.tvSignUp.setOnClickListener {
            J_startActivity(SignUpActivity::class.java)
        }

        binding.btnSignIn.setOnClickListener {
            if (validate()) {
                apiCall = retrofitClient.loginUser(
                    binding.etEmail.text.toString(),
                    binding.etPassword.text.toString(),
                )
                viewModel.loginUser(apiCall)
            }
        }

        binding.ivFaceLogin.setOnClickListener {

            val bioMetric = BioMetric(this, object : BioMetric.BioMetricInterface {
                override fun isDeviceCompatible(isDeviceCompatible: Boolean) {
                    if (!isDeviceCompatible)
                        AppUtils.showToast(this@LoginActivity, "Device incompatible", false)
                }

                override fun bioMetricSuccess(result: BiometricPrompt.AuthenticationResult) {
                    if (!SharedPreference.getSimpleString(mContext, Constants.accessToken).isNullOrEmpty()) {
                        AppUtils.showToast(this@LoginActivity, "User logged in successfully", true)
                        Handler(Looper.getMainLooper()).postDelayed({
                            SharedPreference.saveBoolean(mContext, Constants.isUserLoggedIn, true)
                            intent = Intent(mContext, HomeActivity::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            startActivity(intent)
                            finish()
                        }, 1000)
                    } else
                        AppUtils.showToast(this@LoginActivity, "Please log in one to use biometric", false)
                }

                override fun bioMetricFailed(errorCode: Int, errorString: String) {
                    when (errorCode) {
                        BiometricPrompt.ERROR_NO_BIOMETRICS -> {
                            AppUtils.showToast(this@LoginActivity, "Please enable biometric from phone settings first", false)
                        }
                        BiometricPrompt.ERROR_USER_CANCELED -> {
                        }
                        else -> {
                            AppUtils.showToast(this@LoginActivity, "$errorCode Bio Metric Error $errorString", false)
                        }
                    }
                }
            })
            bioMetric.onTouchIdClick()
        }
    }

    private fun validate(): Boolean {
        if (binding.etEmail.text.toString().isNullOrEmpty()) {
            AppUtils.showToast(this, "Email is required!", false)
            return false
        } else if (!binding.etEmail.text.toString().isValidEmail()) {
            AppUtils.showToast(this, "Email is invalid!", false)
            return false
        } else if (binding.etPassword.text.toString().isNullOrEmpty()) {
            AppUtils.showToast(this, "Password is required!", false)
            return false
        } else if (binding.etPassword.text.toString().length < 8) {
            AppUtils.showToast(this, "Password must be at least 8 digits!", false)
            return false
        }
        return true
    }


}
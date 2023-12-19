package com.android.sitbak.auth.signup

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.ViewModelProviders
import com.android.sitbak.R
import com.android.sitbak.auth.login.LoginActivity
import com.android.sitbak.auth.login.LoginData
import com.android.sitbak.auth.login.LoginModel
import com.android.sitbak.auth.otp.VerifyOtpActivity
import com.android.sitbak.auth.phone_number.PhoneVerification
import com.android.sitbak.base.BaseActivity
import com.android.sitbak.databinding.ActivitySignUpBinding
import com.android.sitbak.home.HomeActivity
import com.android.sitbak.network.Api
import com.android.sitbak.network.ApiTags
import com.android.sitbak.network.RetrofitClient
import com.android.sitbak.utils.*
import com.astritveliu.boom.Boom
import com.google.gson.Gson
import okhttp3.ResponseBody
import retrofit2.Call

class SignUpActivity : BaseActivity() {

    private lateinit var binding: ActivitySignUpBinding
    private lateinit var viewModel: SignUpVM
    private lateinit var mContext: Context
    private lateinit var retrofitClient: Api
    private lateinit var apiCall: Call<ResponseBody>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mContext = this
        retrofitClient = RetrofitClient.getClientNoToken(mContext).create(Api::class.java)

        changeStatusBarColor(R.color.heavy_metal)
        initVar()
        initVM()
        initObservers()
        listener()


    }

    private fun initVar() {
        Boom(binding.btnRegisterNow)
        Boom(binding.btnSignIn)
    }

    private fun initVM() {
        viewModel = ViewModelProviders.of(this, ViewModelFactory()).get(SignUpVM::class.java)
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
                        ApiTags.REGISTER -> {
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

    private fun listener() {
        binding.ivPasswordToggle.setOnClickListener {
            AppUtils.hideShowPassword(binding.etPassword, binding.ivPasswordToggle)
        }
        binding.ivConfirmPasswordToggle.setOnClickListener {
            AppUtils.hideShowPassword(binding.etConfirmPassword, binding.ivConfirmPasswordToggle)
        }
        binding.btnSignIn.setOnClickListener {
            J_startActivity(LoginActivity::class.java)
        }

        binding.btnRegisterNow.setOnClickListener {
            if (validate()) {
                apiCall = retrofitClient.register(
                    binding.etFullName.text.toString(),
                    binding.etEmail.text.toString(),
                    binding.etPassword.text.toString(),
                    binding.etConfirmPassword.text.toString()
                )
                viewModel.register(apiCall)
            }
        }
    }

    private fun validate(): Boolean {
        if (binding.etFullName.text.toString().isNullOrEmpty()) {
            AppUtils.showToast(this, "Your full name is required!", false)
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
        } else if (binding.etConfirmPassword.text.toString() != binding.etPassword.text.toString()) {
            AppUtils.showToast(this, "Password doesn't match!", false)
            return false
        }
        return true
    }
}
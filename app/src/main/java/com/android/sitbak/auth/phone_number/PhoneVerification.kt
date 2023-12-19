package com.android.sitbak.auth.phone_number

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.ViewModelProviders
import com.android.sitbak.R
import com.android.sitbak.auth.otp.VerifyOtpActivity
import com.android.sitbak.base.AppController
import com.android.sitbak.base.BaseActivity
import com.android.sitbak.databinding.ActivityPhoneVerificationBinding
import com.android.sitbak.network.Api
import com.android.sitbak.network.ApiTags
import com.android.sitbak.network.RetrofitClient
import com.android.sitbak.utils.*
import com.astritveliu.boom.Boom
import okhttp3.ResponseBody
import retrofit2.Call

class PhoneVerification : BaseActivity() {
    lateinit var binding: ActivityPhoneVerificationBinding
    private lateinit var viewModel: PhoneVerificationVM
    private lateinit var mContext: Context
    private lateinit var retrofitClient: Api
    private lateinit var apiCall: Call<ResponseBody>

    var type = ""
    var phoneOrEmail = ""
    var fromProfile = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPhoneVerificationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mContext = this
        retrofitClient = RetrofitClient.getClient(mContext).create(Api::class.java)
        changeStatusBarColor(R.color.heavy_metal)

        initVar()
        initVM()
        initObservers()
        listeners()
    }

    private fun initVar() {
        Boom(binding.btnSend)
        binding.countryCodePicker.registerCarrierNumberEditText(binding.etPhone)

        type = intent.getStringExtra("type").toString()
        phoneOrEmail = intent.getStringExtra("phoneOrEmail").toString()
        fromProfile = intent.getBooleanExtra("fromProfile", false)

        if (fromProfile) {
            binding.tvTitle.text = resources.getString(R.string.change_phone)
            binding.tvDescription.viewGone()
            binding.btnSend.text = resources.getString(R.string.change_phone)

            val userData = AppUtils.getUserDetails(mContext)
            binding.etPhone.setText(userData.phone_number.toString().substring(userData.country_code.toString().length))
            binding.countryCodePicker.setCountryForPhoneCode(userData.country_code.toString().toInt())

        } else {
            binding.tvTitle.text = getString(R.string.phone)
            binding.tvDescription.viewVisible()
            binding.btnSend.text = resources.getString(R.string.send_code)
        }

        when (type) {
            "phone" -> {
                binding.tvTitle.text = getString(R.string.phone)
                binding.llRecoverByEmail.viewGone()
                binding.tvDescription.viewGone()
            }
            "resetPasswordByPhone" -> {
                binding.tvTitle.text = getString(R.string.recover_by_phone)
                binding.llRecoverByEmail.viewVisible()
                binding.tvDescription.viewVisible()
            }
        }
    }

    private fun initVM() {
        viewModel = ViewModelProviders.of(this, ViewModelFactory()).get(PhoneVerificationVM::class.java)
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
                        ApiTags.ADD_UPDATE_PHONE -> {
                            AppUtils.showToast(this, it.data?.getString("message")!!, true)
                            Handler(Looper.getMainLooper()).postDelayed({
                                val intent = Intent(mContext, VerifyOtpActivity::class.java)
                                intent.putExtra("type", type)
                                intent.putExtra("fromProfile", fromProfile)
                                intent.putExtra(
                                    "phoneOrEmail",
                                    binding.countryCodePicker.selectedCountryCodeWithPlus + binding.etPhone.text.toString()
                                )
                                startActivity(intent)
                                finish()
                            }, 1000)
                        }
                        ApiTags.RECOVER_BY_PHONE -> {
                            AppUtils.showToast(this, it.data?.getString("message")!!, true)
                            val data = it.data.getJSONObject("data")
                            AppController.resetOTP = data.getInt("otp")
                            val intent = Intent(mContext, VerifyOtpActivity::class.java)
                            intent.putExtra("phoneOrEmail", binding.countryCodePicker.selectedCountryCodeWithPlus + binding.etPhone.text.toString())
                            intent.putExtra("type", "resetPasswordByPhone")
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
            onBackPressed()
        }
        binding.llRecoverByEmail.setOnClickListener {
            finish()
        }

        binding.btnSend.setOnClickListener {
            if (validate()) {
                when (type) {
                    "phone", "changePhone" -> {
                        apiCall =
                            retrofitClient.addUpdatePhoneNumber(
                                binding.countryCodePicker.selectedCountryCodeWithPlus,
                                binding.countryCodePicker.selectedCountryCodeWithPlus + binding.etPhone.text.toString().replace(" ", "")
                                    .replace("-", "")
                            )
                        viewModel.addUpdatePhoneNumber(apiCall)
                    }
                    "resetPasswordByPhone" -> {
                        apiCall = retrofitClient.senOTPForgotPassword(
                            binding.countryCodePicker.selectedCountryCodeWithPlus + binding.etPhone.text.toString().replace(" ", "").replace("-", ""),
                            "",
                            "phone_number"
                        )
                        viewModel.recoverByPhone(apiCall)
                    }
                }
            }
        }
    }

    private fun validate(): Boolean {
        if (binding.etPhone.text.toString().isNullOrEmpty()) {
            AppUtils.showToast(this, "Phone number is required!", false)
            return false
        } else if (!binding.etPhone.text.toString().isValidMobileNumber()) {
            AppUtils.showToast(this, "Phone number is invalid!", false)
            return false
        }
        return true
    }
}
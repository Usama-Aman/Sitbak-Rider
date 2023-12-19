package com.android.sitbak.home.profile_tab

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.android.sitbak.R
import com.android.sitbak.auth.change_password.ChangePasswordActivity
import com.android.sitbak.auth.login.LoginData
import com.android.sitbak.auth.login.LoginModel
import com.android.sitbak.auth.password_recovery.PasswordRecovery
import com.android.sitbak.auth.phone_number.PhoneVerification
import com.android.sitbak.base.ViewModelFactory
import com.android.sitbak.databinding.FragmentProfileBinding
import com.android.sitbak.home.HomeActivity
import com.android.sitbak.home.ManageAccountsWebView
import com.android.sitbak.home.earning.YourEarnings
import com.android.sitbak.home.popups.LogoutFragment
import com.android.sitbak.home.popups.region_popup.RegionData
import com.android.sitbak.home.popups.region_popup.RegionFragment
import com.android.sitbak.network.Api
import com.android.sitbak.network.ApiTags
import com.android.sitbak.network.RetrofitClient
import com.android.sitbak.service.LocationService
import com.android.sitbak.utils.*
import com.astritveliu.boom.Boom
import com.bumptech.glide.Glide
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import retrofit2.Call
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL


class ProfileFragment : Fragment() {
    private lateinit var binding: FragmentProfileBinding
    private var selectedRegion: RegionData? = null
    var selectedZoneId = -1

    private lateinit var viewModel: ProfileFragmentVM
    private lateinit var mContext: Context
    private lateinit var retrofitClient: Api
    private lateinit var apiCall: Call<ResponseBody>

    private lateinit var userData: LoginData

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_profile, container, false)
        mContext = requireContext()
        return binding.root
    }

    companion object {

        fun getInstance(): ProfileFragment {

            val fragment = ProfileFragment()
            return fragment
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        retrofitClient = RetrofitClient.getClient(mContext).create(Api::class.java)

        initVM()
        initObservers()
        boomViews()
    }

    private fun boomViews() {
        Boom(binding.idAvailLay)
        Boom(binding.constraintManageAccounts)
        Boom(binding.layPhone)
        Boom(binding.layEmail)
        Boom(binding.layPassword)
        Boom(binding.layRegion)
        Boom(binding.laySupport)
        Boom(binding.layLogout)
        Boom(binding.layEarn)
    }

    private fun initVM() {
        viewModel = ViewModelProviders.of(this, ViewModelFactory()).get(ProfileFragmentVM::class.java)
        binding.viewModel = viewModel

        binding.executePendingBindings()
        binding.lifecycleOwner = this
    }

    private fun initObservers() {
        viewModel.getApiResponse().observe(viewLifecycleOwner, {
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
                    AppUtils.showToast(requireActivity(), it.message!!, false)
                }
                ApiStatus.SUCCESS -> {
                    Loader.hideLoader()
                    when (it.tag) {
                        ApiTags.LOGOUT -> {
                            AppUtils.showToast(requireActivity(), it.data?.getString("message")!!, true)
                        }
                        ApiTags.GET_PROFILE -> {
                            val model = Gson().fromJson(it.data.toString(), LoginModel::class.java)
                            updateProfileView(model)
                        }
                        ApiTags.SWITCH_AVAILABILITY -> {
                            AppUtils.showToast(requireActivity(), it.data?.getString("message")!!, true)
                        }
                        ApiTags.UPDATE_PROFILE -> {
                            AppUtils.showToast(requireActivity(), it.data?.getString("message")!!, true)
                            Handler(Looper.getMainLooper()).postDelayed({
                                getProfileData()
                            }, 1000)
                        }
                    }
                }
            }
        })
    }

    @SuppressLint("SetTextI18n")
    private fun updateProfileView(loginData: LoginModel) {

        try {
            userData = loginData.data

            binding.tvEarned.text = "$${userData.available_earning}"

            if (userData.name.isNotBlank())
                binding.tvUsername.text = userData.name
            else
                binding.tvUsername.text = ""

            if (userData.photo_path!!.isNotBlank()) {
                binding.ivPlaceHolder.viewGone()
                Glide.with(mContext).load(userData.photo_path).placeholder(R.drawable.ic_person).into(binding.ivProfile)
            } else
                binding.ivPlaceHolder.viewVisible()

            if (userData.phone_number!!.isNotBlank())
                binding.tvPhoneNumber.text = userData.phone_number
            else
                binding.tvPhoneNumber.text = ""

            if (userData.email.isNotBlank())
                binding.tvEmail.text = userData.email
            else
                binding.tvEmail.text = userData.email

            if (userData.region != null) {
                binding.tvRegion.text = userData.region_name
                selectedZoneId = userData.region.toInt()
            } else
                binding.tvRegion.text = ""

            initListeners()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    private fun initListeners() {
        binding.switchBtn.isChecked = userData.is_available == 1
        binding.switchBtn.addSwitchObserver { switchView, isChecked ->
            if (isChecked) {
                switchAvailability(1)
                SharedPreference.saveBoolean(mContext, Constants.isAvailable, isChecked)
                userData.is_available = 1
                AppUtils.saveUserModel(mContext, userData)
            } else {
                switchAvailability(0)
                SharedPreference.saveBoolean(mContext, Constants.isAvailable, isChecked)
                userData.is_available = 0
                AppUtils.saveUserModel(mContext, userData)
            }
        }
        binding.layPassword.setOnClickListener {
            J_startActivity(ChangePasswordActivity::class.java)
        }

        binding.layPhone.setOnClickListener {
            val intent = Intent(activity, PhoneVerification::class.java)
            intent.putExtra("fromProfile", true)
            intent.putExtra("type", "changePhone")
            mContext.startActivity(Intent(intent))
        }
        binding.layEmail.setOnClickListener {
            val intent = Intent(activity, PasswordRecovery::class.java)
            intent.putExtra("fromProfile", true)
            intent.putExtra("type", "changeEmail")
            mContext.startActivity(Intent(intent))
        }

        binding.layRegion.setOnClickListener {
            val dialog = RegionFragment.getInstance(selectedZoneId)
            dialog.setListener(object : RegionFragment.RegionFragmentClickListener {
                override fun onOkClicked(selectedRegionData: RegionData?) {
                    selectedRegion = selectedRegionData
                    selectedZoneId = selectedRegionData?.id!!
                    updateProfileData()
                }
            })
            dialog.show(childFragmentManager, "RegionFragment")
        }

        binding.layEarn.setOnClickListener {

            if (userData.is_bank_account_verified == 0) {
                AppUtils.showToast(requireActivity(), "Go to -> Manage Accounts and add your payout account first", false)
            } else {
                J_startActivity(YourEarnings::class.java)
            }
        }

        binding.constraintManageAccounts.setOnClickListener {

            if (userData.is_bank_account_verified == 0) {
                getUrl("http://178.128.29.7/sitbak/connect-account/${userData.id}")
            } else {
                val intent = Intent(mContext, ManageAccountsWebView::class.java)
                intent.putExtra("url", userData.stripe_express_dashboard_url)
                mContext.startActivity(intent)
            }

        }

        binding.layLogout.setOnClickListener {
            val dialog = LogoutFragment(object : LogoutFragment.LogoutInterface {
                override fun logoutAccount() {
                    stopService()
                    SharedPreference.saveBoolean(mContext, Constants.isUserLoggedIn, false)
                    (activity as HomeActivity).gotoLogin()
                }

            })
            dialog.show(childFragmentManager, "LogoutFragment")
        }
    }

    private fun stopService() {
        val mServiceIntent = Intent(mContext, LocationService::class.java)
        if (isMyServiceRunning(LocationService::class.java)) {
            mContext.stopService(mServiceIntent)
        }
    }

    @Suppress("DEPRECATION")
    private fun isMyServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = mContext.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager?
        for (service in manager!!.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                Log.i("Service status", "Running")
                return true
            }
        }
        Log.i("Service status", "Not running")
        return false
    }

    private fun getUrl(stringUrl: String) {
        Loader.showLoader(mContext)
        CoroutineScope(Dispatchers.IO).launch {
            val response = StringBuilder()
            val url = URL(stringUrl)
            val httpconn: HttpURLConnection = url.openConnection() as HttpURLConnection
            if (httpconn.responseCode === HttpURLConnection.HTTP_OK) {
                val input = BufferedReader(InputStreamReader(httpconn.inputStream), 8192)
                var strLine: String? = null
                while (input.readLine().also { strLine = it } != null) {
                    response.append(strLine)
                }
                input.close()
            }

            withContext(Dispatchers.Main) {
                Loader.hideLoader()
                val intent = Intent(mContext, ManageAccountsWebView::class.java)
                intent.putExtra("url", response.toString())
                mContext.startActivity(intent)
            }

        }
    }

    override fun onResume() {
        super.onResume()
        getProfileData()
    }

    private fun getProfileData() {
        apiCall = retrofitClient.getUserProfile()
        viewModel.getProfile(apiCall)
    }

    private fun updateProfileData() {
        val loginData = AppUtils.getUserDetails(mContext)
        apiCall = retrofitClient.updateProfile(loginData.name, selectedZoneId)
        viewModel.updateProfile(apiCall)
    }

    private fun switchAvailability(type: Int) {
        apiCall = retrofitClient.switchAvailability(type)
        viewModel.switchAvailability(apiCall)
    }
}
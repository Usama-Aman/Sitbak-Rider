package com.android.sitbak.home

import android.content.BroadcastReceiver
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.android.sitbak.R
import com.android.sitbak.auth.login.LoginActivity
import com.android.sitbak.base.AppController
import com.android.sitbak.base.AppController.Companion.socket
import com.android.sitbak.base.BaseActivity
import com.android.sitbak.databinding.ActivityDriverHomeBinding
import com.android.sitbak.home.profile_tab.ProfileFragment
import com.android.sitbak.home.scheduling_tab.ScheduleFragment
import com.android.sitbak.home.tickets_tab.TicketsFragment
import com.android.sitbak.network.NetworkChangeReceiver
import com.android.sitbak.network.SocketIO
import com.android.sitbak.onesignal.OneSignalNotificationManager
import com.android.sitbak.utils.AppUtils
import com.android.sitbak.utils.Loader.Companion.mContext
import com.google.android.material.snackbar.Snackbar
import org.jetbrains.anko.imageResource
import org.jetbrains.anko.textColor

class HomeActivity : BaseActivity(), NetworkChangeReceiver.ConnectivityReceiverListener {

    lateinit var binding: ActivityDriverHomeBinding
    private var mNetworkReceiver: BroadcastReceiver? = null
    private lateinit var snackBar: Snackbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDriverHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        changeStatusBarColor(R.color.heavy_metal)

        listener()
        setViewPager()

        AppController.isHomeActive = true
        AppController.userDetails = AppUtils.getUserDetails(this)
        OneSignalNotificationManager.sendUserTags(this)

        //Initializing socket for the first time
        socket = SocketIO()

        mNetworkReceiver = NetworkChangeReceiver(this)
        registerNetworkBroadcastForNougat()
    }

    private lateinit var vpf: ViewPagerFragmentAdapter


    private fun setViewPager() {
        vpf = ViewPagerFragmentAdapter(
            this@HomeActivity
        )
        binding.viewPager.adapter = vpf
        binding.viewPager.registerOnPageChangeCallback(pageChangeCallback)
        binding.viewPager.isUserInputEnabled = false
    }

    private var pageChangeCallback: ViewPager2.OnPageChangeCallback =
        object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                colorControl(position)
            }
        }

    private fun listener() {

        binding.layTickets.setOnClickListener {
            binding.viewPager.setCurrentItem(0, false)
        }

        binding.laySchedule.setOnClickListener {
            binding.viewPager.setCurrentItem(1, false)
        }

        binding.layProfile.setOnClickListener {
            binding.viewPager.setCurrentItem(2, false)
        }

        binding.layTickets.performClick()

    }

    private fun colorControl(i: Int) {

        binding.ivTicket.imageResource = if (i == 0) {
            R.drawable.ic_green_ticket
        } else {
            R.drawable.ic_ticket_white
        }
        binding.ivSchedule.imageResource = if (i == 1) {
            R.drawable.ic_green_schedule
        } else {
            R.drawable.ic_white_schedule
        }
        binding.ivProfile.imageResource = if (i == 2) {
            R.drawable.ic_green_profile_driver
        } else {
            R.drawable.ic_white_profile_driver
        }

        binding.tvTicket.textColor =
            resources.getColor(if (i == 0) R.color.green_900 else R.color.tasman7)
        binding.tvSchedule.textColor =
            resources.getColor(if (i == 1) R.color.green_900 else R.color.tasman7)
        binding.tvProfile.textColor =
            resources.getColor(if (i == 2) R.color.green_900 else R.color.tasman7)

    }

    fun gotoLogin() {
        val intent = Intent(mContext, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
        finish()
    }

    private class ViewPagerFragmentAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> TicketsFragment()
                1 -> ScheduleFragment()
                else -> ProfileFragment()
            }
        }

        override fun getItemCount(): Int {
            return 3
        }
    }

    private fun registerNetworkBroadcastForNougat() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            registerReceiver(mNetworkReceiver, IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            registerReceiver(mNetworkReceiver, IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))
        }
    }

    private fun unregisterNetworkChanges() {
        try {
            unregisterReceiver(mNetworkReceiver)
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (AppController.isSocketInitialized())
            socket.disconnect()
        unregisterNetworkChanges()
    }

    override fun onResume() {
        super.onResume()
        AppController.isHomeActive = true
        AppController.isHomeActive = false
    }

    override fun onNetworkConnectionChanged(isConnected: Boolean) {
        if (isConnected) {
            if (this::snackBar.isInitialized)
                if (snackBar.isShown)
                    snackBar.dismiss()
        } else {
            snackBar = Snackbar.make(findViewById(android.R.id.content), "No Internet Connection", Snackbar.LENGTH_INDEFINITE)
            snackBar.show()
        }
    }
}
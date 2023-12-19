package com.android.sitbak.home.tickets_tab

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.android.sitbak.R
import com.android.sitbak.databinding.FragmentTicketsBinding
import com.android.sitbak.home.tickets_tab.available.AvailableTicketsFragment
import com.android.sitbak.home.tickets_tab.processing.ProcessingTicketsFragment
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.textColor

class TicketsFragment : Fragment() {

    private lateinit var binding: FragmentTicketsBinding
    private lateinit var mContext: Context

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mContext = requireContext()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTicketsBinding.inflate(inflater, container, false)
        return binding.root
    }

    companion object {
        fun getInstance(): TicketsFragment {
            val fragment = TicketsFragment()
            return fragment
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        listeners()
        setViewPager()

    }

    private fun listeners() {

        binding.layProcessing.setOnClickListener {
            binding.viewPager.setCurrentItem(0, false)
        }

        binding.layAvailable.setOnClickListener {
            binding.viewPager.setCurrentItem(1, false)
        }

        controlLay(0)

    }

    fun controlLay(i: Int) {

        binding.tvProcessing.textColor =
            resources.getColor(if (i == 0) R.color.green_900 else R.color.finlandia)
        binding.tvAvailable.textColor =
            resources.getColor(if (i == 1) R.color.green_900 else R.color.finlandia)

        binding.viewProcessing.backgroundColor =
            resources.getColor(if (i == 0) R.color.green_900 else R.color.finlandia)
        binding.viewAvailable.backgroundColor =
            resources.getColor(if (i == 1) R.color.green_900 else R.color.finlandia)

    }


    private fun setViewPager() {

        binding.viewPager.adapter = ViewPagerFragmentAdapter(childFragmentManager)
        binding.viewPager.offscreenPageLimit = 1

        binding.viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {

            }

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                controlLay(position)
            }

        })
    }

    fun goToProcessingTab() {
        binding.viewPager.setCurrentItem(0, true)
    }

    @SuppressLint("SetTextI18n")
    fun updateAvailableCounts(size: Int) {
        binding.tvAvailable.text = "${mContext.resources.getString(R.string.available_tickets_tab)} $size"
    }


    private class ViewPagerFragmentAdapter(
        fragmentManager: FragmentManager
    ) :
        FragmentPagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

        override fun getItem(position: Int): Fragment {

            return when (position) {
                0 -> ProcessingTicketsFragment()
                else -> AvailableTicketsFragment()
            }
        }

        override fun getCount(): Int {
            return 2
        }

    }


}
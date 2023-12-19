package com.android.sitbak.home.scheduling_tab

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.android.sitbak.R
import com.android.sitbak.databinding.FragmentScheduleBinding
import com.android.sitbak.home.scheduling_tab.availability.AvailabilitiesFragment
import com.android.sitbak.home.scheduling_tab.my_shifts.MyShiftsFragment
import com.android.sitbak.home.scheduling_tab.open_shifts.OpenShiftsFragment
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.textColor


class ScheduleFragment : Fragment() {


    lateinit var binding: FragmentScheduleBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }


    companion object {

        fun getInstance(): ScheduleFragment {

            val fragment = ScheduleFragment()
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_schedule, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        listeners()
        setViewPager()

    }

    private fun listeners() {

        binding.layMyShifting.setOnClickListener {
            binding.viewPager.setCurrentItem(0,false)
        }

        binding.layOpenShift.setOnClickListener {
            binding.viewPager.setCurrentItem(1,false)
        }

        binding.layProcessing.setOnClickListener {
            binding.viewPager.setCurrentItem(2,false)
        }

        controlLay(0)
    }

    fun controlLay(i: Int){

        binding.tvShifting.textColor = resources.getColor(if(i==0) R.color.green_900 else R.color.finlandia)
        binding.tvOpenShift.textColor = resources.getColor(if(i==1) R.color.green_900 else R.color.finlandia)
        binding.tvProcessing.textColor = resources.getColor(if(i==2) R.color.green_900 else R.color.finlandia)

        binding.viewShifting.backgroundColor = resources.getColor(if(i==0) R.color.green_900 else R.color.finlandia)
        binding.viewOpenShift.backgroundColor = resources.getColor(if(i==1) R.color.green_900 else R.color.finlandia)
        binding.viewProcessing.backgroundColor = resources.getColor(if(i==2) R.color.green_900 else R.color.finlandia)

    }


    private fun setViewPager() {

        binding.viewPager.adapter = ViewPagerFragmentAdapter(childFragmentManager)
        binding.viewPager.offscreenPageLimit = 3

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


    private class ViewPagerFragmentAdapter(
        fragmentManager: FragmentManager
    ) :
        FragmentPagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

        override fun getItem(position: Int): Fragment {

            val myShiftsFragment = MyShiftsFragment.getInstance()
            val openShiftsFragment = OpenShiftsFragment.getInstance()
            val availbilitysFragment = AvailabilitiesFragment.getInstance()


            when (position) {
                0 -> return myShiftsFragment
                1 -> return openShiftsFragment
                2 -> return availbilitysFragment
            }
            return myShiftsFragment
        }

        override fun getCount(): Int {
            return 3
        }

    }

}
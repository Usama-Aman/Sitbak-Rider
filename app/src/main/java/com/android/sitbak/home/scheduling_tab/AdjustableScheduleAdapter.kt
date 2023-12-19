package com.android.sitbak.home.scheduling_tab

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.android.sitbak.R
import com.android.sitbak.base.BaseActivity
import com.android.sitbak.databinding.ItemAdustableScheduleBinding
import com.android.sitbak.home.scheduling_tab.my_shifts.AvailabilitiesData


class AdjustableScheduleAdapter(var context: Context, var list: MutableList<AvailabilitiesData>, var listener: AvailabilityAdapterClickListener) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        val binding: ItemAdustableScheduleBinding =
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.item_adustable_schedule, parent, false
            )

        return AdjustableScheduleViewHolder(binding, this, context)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        if (holder is AdjustableScheduleViewHolder)
            holder.bind(position)

    }

    override fun getItemCount(): Int = list.size

    inner class AdjustableScheduleViewHolder(
        _binding: ItemAdustableScheduleBinding,
        var adapter: AdjustableScheduleAdapter,
        var context: Context
    ) :
        RecyclerView.ViewHolder(_binding.root) {
        private val binding = _binding

        @SuppressLint("NewApi", "SetTextI18n")
        fun bind(position: Int) {

            val data = list[position]
            binding.tvTime.text = (data.start_time + " - " + data.end_time)
            binding.tvStatus.text = data.type
            binding.tvAddress.text = data.region?.address
            binding.tvTotalTime.text = (context as BaseActivity).diffTimeForHours(data.start_time!!, data.end_time!!)
                .toString() + "h" + (context as BaseActivity).diffTimeForMin(data.start_time, data.end_time).toString() + "m"

            binding.llDelete.setOnClickListener {
                binding.swipeRevealLayout.close(true)
                listener.onDeleteItemClicked(position, data)
            }
            binding.tvAddress.setOnClickListener {
                listener.onLocationClicked(position, data)
            }

            binding.ivEdit.setOnClickListener {
                listener.onEditClicked(position, data)
            }
        }
    }
}

interface AvailabilityAdapterClickListener {
    fun onLocationClicked(position: Int, data: AvailabilitiesData)
    fun onDeleteItemClicked(position: Int, data: AvailabilitiesData)
    fun onEditClicked(position: Int, data: AvailabilitiesData)
}



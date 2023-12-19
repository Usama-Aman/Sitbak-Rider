package com.android.sitbak.home.scheduling_tab.my_shifts

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.android.sitbak.R
import com.android.sitbak.base.BaseActivity
import com.android.sitbak.databinding.ItemMyShiftsBinding
import com.astritveliu.boom.Boom
import com.chauthai.swipereveallayout.ViewBinderHelper


class MyShiftsAdapter(
    var context: Context,
    var list: MutableList<AvailabilitiesData>,
    var clickListener: MyShiftSwipeDeleteClickListener
) : RecyclerView.Adapter<MyShiftsAdapter.ViewHolder>() {
    private val viewBinderHelper = ViewBinderHelper()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding: ItemMyShiftsBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.item_my_shifts,
            parent,
            false
        )
        return ViewHolder(binding, context, this@MyShiftsAdapter)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBind(position)
        viewBinderHelper.bind(holder.binding.swipeRevealLayout, list[position].id.toString())

    }

    private fun setListener(listener: MyShiftSwipeDeleteClickListener) {
        this.clickListener = listener
    }

    private fun removeAt(position: Int) {
        notifyItemRemoved(position)

    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class ViewHolder(
        var binding: ItemMyShiftsBinding,
        var context: Context,
        var adapter: MyShiftsAdapter
    ) : RecyclerView.ViewHolder(binding.root) {
        @RequiresApi(Build.VERSION_CODES.N)
        @SuppressLint("SimpleDateFormat", "SetTextI18n")
        fun onBind(position: Int) {

            Boom(binding.llDelete)

            val data = list[position]

            binding.tvTime.text = (data.start_time + " - " + data.end_time)
            binding.tvStatus.text = data.type
            binding.tvAddress.text = data.region?.address
            binding.tvTotalTime.text = (context as BaseActivity).diffTimeForHours(data.start_time!!, data.end_time!!)
                .toString() + "h" + (context as BaseActivity).diffTimeForMin(data.start_time, data.end_time).toString() + "m"

            binding.llDelete.setOnClickListener {
                adapter.clickListener.onDeleteClick(adapterPosition, list[position])
                binding.swipeRevealLayout.close(true)
            }
            binding.tvAddress.setOnClickListener {
                adapter.clickListener.onLocationClicked(position, data)
            }
        }
    }
}

interface MyShiftSwipeDeleteClickListener {
    fun onDeleteClick(position: Int, data: AvailabilitiesData)
    fun onLocationClicked(position: Int, data: AvailabilitiesData)
}
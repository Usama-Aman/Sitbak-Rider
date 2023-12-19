package com.android.sitbak.home.scheduling_tab.open_shifts

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.android.sitbak.R
import com.android.sitbak.base.BaseActivity
import com.android.sitbak.databinding.ItemLoadMoreBinding
import com.android.sitbak.databinding.ItemOpenShiftsBinding
import com.android.sitbak.home.tickets_tab.available.AvailableTicketsAdapter
import com.astritveliu.boom.Boom

class OpenShiftsAdapter(
    var selectedShiftId: Int,
    var context: Context,
    var list: MutableList<OpenShiftsData?>,
    var clickListener: OpenShiftAdapterClickListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    interface OpenShiftAdapterClickListener {
        fun onLocationClicked(position: Int, model: OpenShiftsData)
        fun onAcceptShiftClicked(position: Int, model: OpenShiftsData)
        fun onLoadMoreClicked()
    }


    companion object {
        private const val ITEM_TYPE = 0
        private const val LOAD_MORE = 1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == ITEM_TYPE) {
            ViewHolder(
                DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.item_open_shifts, parent, false
                )
            )
        } else {
            LoadMore(
                DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.item_load_more, parent, false
                )
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ViewHolder -> holder.onBind(position)
            is LoadMore -> holder.bind()
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }


    override fun getItemViewType(position: Int): Int {
        return if (list[position] == null) LOAD_MORE else ITEM_TYPE
    }

    inner class ViewHolder(var binding: ItemOpenShiftsBinding) : RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n", "NewApi")
        fun onBind(position: Int) {


            val data = list[position]

            binding.tvTime.text = (data?.start_time + " - " + data?.end_time)
            binding.tvStatus.text = data?.type
            binding.tvAddress.text = data?.region?.address
            binding.tvTotalTime.text = (context as BaseActivity).diffTimeForHours(data?.start_time!!, data.end_time)
                .toString() + "h" + (context as BaseActivity).diffTimeForMin(data.start_time, data.end_time).toString() + "m"

            binding.tvAddress.setOnClickListener {
                clickListener.onLocationClicked(position, data)
            }
            binding.btnAcceptShift.setOnClickListener {
                selectedShiftId = data.id
                clickListener.onAcceptShiftClicked(position, data)
                if (selectedShiftId == data.id) {
                    binding.content.alpha = 0.4F
                    binding.btnAcceptShift.text = "✓ Added to your schedule"
                    binding.btnAcceptShift.setTextColor(ContextCompat.getColor(context, R.color.app_main_text_white))
                    binding.btnAcceptShift.setBackgroundResource(R.drawable.bg_btn_gray)
                    binding.btnAcceptShift.isEnabled = false
                } else {
                    binding.content.alpha = 1F
                    binding.btnAcceptShift.text = "+ Accept shift"
                    binding.btnAcceptShift.setTextColor(ContextCompat.getColor(context, R.color.green_900))
                    binding.btnAcceptShift.setBackgroundResource(R.drawable.green_circle)
                    binding.btnAcceptShift.isEnabled = true
                }
            }
        }
    }

    inner class LoadMore(val binding: ItemLoadMoreBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind() {
            binding.loadMoreItem.setOnClickListener {
                clickListener.onLoadMoreClicked()
            }
        }

    }


}

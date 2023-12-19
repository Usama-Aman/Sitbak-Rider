package com.android.sitbak.home.tickets_tab.available

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
import com.android.sitbak.databinding.ItemAvailableTicketsBinding
import com.android.sitbak.databinding.ItemLoadMoreBinding
import com.android.sitbak.home.tickets_tab.TicketsData
import com.astritveliu.boom.Boom

class AvailableTicketsAdapter(val availableOrderInterface: AvailableOrderInterface, val availableTicketsData: MutableList<TicketsData?>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private lateinit var mContext: Context

    interface AvailableOrderInterface {
        fun onAcceptOrder(position: Int)
        fun onTimePaymentClicked(position: Int)
        fun onDeliveryDetailClicked(position: Int)
        fun onClientInfoClicked(position: Int)
        fun onLoadMoreClicked()
    }

    companion object {
        private const val ITEM_TYPE = 0
        private const val LOAD_MORE = 1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        mContext = parent.context

        return if (viewType == ITEM_TYPE) {
            ShiftScheduleViewHolder(
                DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.item_available_tickets, parent, false
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
            is ShiftScheduleViewHolder -> holder.bind(position)
            is LoadMore -> holder.bind()
        }
    }

    override fun getItemCount(): Int = availableTicketsData.size

    override fun getItemViewType(position: Int): Int {
        return if (availableTicketsData[position] == null) LOAD_MORE else ITEM_TYPE
    }

    inner class ShiftScheduleViewHolder(val binding: ItemAvailableTicketsBinding) : RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(position: Int) {

            Boom(binding.viewTimeAndDelivery)
            Boom(binding.viewLocationDetails)
            Boom(binding.viewClientInfo)
            Boom(binding.btnAccetOrder)

            binding.tvStatus.text = "New"
            binding.tvOrderNumber.text = availableTicketsData[position]?.order_number
            binding.tvDelivery.text = "$${availableTicketsData[position]?.delivery_charges}"
            binding.tvKM.text = "${availableTicketsData[position]?.distance}\nkm"
            binding.tvIncludeTip.text = mContext.resources.getString(
                R.string.included_tip,
                "$${availableTicketsData[position]?.delivery_charges}"
            )
            binding.tvTime.text =
                "${availableTicketsData[position]?.start_time}-${availableTicketsData[position]?.end_time}"

            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N) {
                binding.tvTimeLeft.text =
                    "left " +
                            (mContext as BaseActivity).diffTimeForHours(
                                availableTicketsData[position]?.start_time!!,
                                availableTicketsData[position]?.end_time!!
                            ).toString() + "h" + (mContext as BaseActivity).diffTimeForMin(
                        availableTicketsData[position]?.start_time!!,
                        availableTicketsData[position]?.end_time!!
                    ).toString() + "m"
            } else
                binding.tvTimeLeft.text = ""


            binding.tvNote.text =
                if (availableTicketsData[position]?.delivery_note.isNullOrBlank()) "N/A" else availableTicketsData[position]?.delivery_note
            binding.tvStoreLocation.text = availableTicketsData[position]?.store_location?.address
            binding.tvClientName.text = availableTicketsData[position]?.user?.name
            binding.tvOrderName.text = mContext.resources.getString(
                R.string.order_items_no,
                availableTicketsData[position]?.items_count.toString()
            )
            binding.tvAddress1.text = availableTicketsData[position]?.delivery_location?.address


            binding.btnAccetOrder.setOnClickListener {
                availableOrderInterface.onAcceptOrder(position)
            }

            binding.viewTimeAndDelivery.setOnClickListener {
                availableOrderInterface.onTimePaymentClicked(position)
            }

            binding.viewLocationDetails.setOnClickListener {
//                availableOrderInterface.onDeliveryDetailClicked(position)
            }

            binding.viewClientInfo.setOnClickListener {
                availableOrderInterface.onClientInfoClicked(position)
            }

        }

    }

    inner class LoadMore(val binding: ItemLoadMoreBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind() {
            binding.loadMoreItem.setOnClickListener {
                availableOrderInterface.onLoadMoreClicked()
            }
        }

    }


}
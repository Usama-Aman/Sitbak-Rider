package com.android.sitbak.home.completed_shifts

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.android.sitbak.R
import com.android.sitbak.base.BaseActivity
import com.android.sitbak.databinding.ItemLoadMoreBinding
import com.android.sitbak.databinding.ItemTicketShiftDoneBinding

class CompletedShiftsAdapter(
    private val completedShiftsData: MutableList<CompletedShiftsData?>,
    private val completedShiftsInterface: CompletedShiftsInterface
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    companion object {
        private const val ITEM_TYPE = 0
        private const val LOAD_MORE = 1
    }

    interface CompletedShiftsInterface {
        fun onLoadMoreClicked()
    }

    private lateinit var mContext: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        mContext = parent.context
        return if (viewType == ITEM_TYPE) {
            Item(
                DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.item_ticket_shift_done, parent, false
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

    override fun getItemCount(): Int = completedShiftsData.size

    override fun getItemViewType(position: Int): Int {
        return if (completedShiftsData[position] == null) LOAD_MORE else ITEM_TYPE
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is Item -> holder.bind(position)
            is LoadMore -> holder.bind()
        }
    }

    inner class Item(val binding: ItemTicketShiftDoneBinding) : RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(position: Int) {


            binding.tvStatus.text = "Done"
            binding.tvNumber.text = completedShiftsData[position]?.order_number
            binding.tvPrice.text = "$${completedShiftsData[position]?.delivery_charges}"
            binding.tvKM.text = "${completedShiftsData[position]?.distance}\nkm"
            binding.tvIncludeTip.text = mContext.resources.getString(
                R.string.included_tip,
                "$${completedShiftsData[position]?.delivery_charges}"
            )
            binding.tvTime.text =
                "${completedShiftsData[position]?.start_time}-${completedShiftsData[position]?.end_time}"

            binding.tvNote.text =
                if (completedShiftsData[position]?.delivery_note.isNullOrBlank()) "N/A" else completedShiftsData[position]?.delivery_note
            binding.tvAddress.text = completedShiftsData[position]?.store_location?.address
            binding.tvClientName.text = completedShiftsData[position]?.user?.name
            binding.tvOrderName.text = mContext.resources.getString(
                R.string.order_items_no,
                completedShiftsData[position]?.items_count.toString()
            )
            binding.tvAddress1.text = completedShiftsData[position]?.delivery_location?.address

            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N) {
                binding.tvDateView.text =
                    "left " +
                            (mContext as BaseActivity).diffTimeForHours(
                                completedShiftsData[position]?.start_time!!,
                                completedShiftsData[position]?.end_time!!
                            ).toString() + "h" + (mContext as BaseActivity).diffTimeForMin(
                        completedShiftsData[position]?.start_time!!,
                        completedShiftsData[position]?.end_time!!
                    ).toString() + "m"
            } else
                binding.tvDateView.text = ""

        }

    }

    inner class LoadMore(val binding: ItemLoadMoreBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind() {
            binding.loadMoreItem.setOnClickListener {
                completedShiftsInterface.onLoadMoreClicked()
            }
        }

    }

}
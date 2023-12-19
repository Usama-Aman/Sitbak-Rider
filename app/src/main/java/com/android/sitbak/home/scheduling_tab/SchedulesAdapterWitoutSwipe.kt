package com.android.sitbak.home.scheduling_tab

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.android.sitbak.R
import com.android.sitbak.databinding.ItemScheduleWithoutSwipeBinding

class SchedulesAdapterWitoutSwipe(var context: Context,
                                  var clickListener: MyLocationClickListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        val binding: ItemScheduleWithoutSwipeBinding =
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.item_schedule_without_swipe, parent, false
            )

        return ShiftScheduleViewHolder(binding,this)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        if (holder is ShiftScheduleViewHolder)
            holder.bind(position)
    }

    private fun setListener(listener: MyLocationClickListener){
        this.clickListener = listener
    }

    override fun getItemCount(): Int = 2

    fun removeAt(position: Int) {
        notifyItemRemoved(position)
//        showSuccessToast(context,"item deleted")
    }


    inner class ShiftScheduleViewHolder(_binding: ItemScheduleWithoutSwipeBinding, var adapter : SchedulesAdapterWitoutSwipe) :
        RecyclerView.ViewHolder(_binding.root) {

        private val binding = _binding

        fun bind(position: Int) {

            binding.tvAddress.setOnClickListener {
                adapter.clickListener.onLocationClicked(adapterPosition)
            }
        }

    }


}

interface MyLocationClickListener {
    fun onLocationClicked(position: Int)
}
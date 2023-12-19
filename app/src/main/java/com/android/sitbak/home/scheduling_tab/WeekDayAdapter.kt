package com.android.sitbak.home.scheduling_tab

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.android.sitbak.R
import com.android.sitbak.databinding.ItemWeekDayBinding
import com.android.sitbak.utils.getDay
import com.android.sitbak.utils.getDayName
import com.android.sitbak.utils.local_models.CalendarModel
import org.jetbrains.anko.textColor
import java.util.*

class WeekDayAdapter(
    var context: Context,
    var dataList: ArrayList<CalendarModel>,
    var clickListener: WeekDaysClickListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var selected = -1


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        val binding: ItemWeekDayBinding =
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.item_week_day, parent, false
            )

        return WeekDaysViewHolder(binding)
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        if (holder is WeekDaysViewHolder) {
            val date = dataList[position]
            holder.bind(position, date)

        }
    }

    fun setItems(items: ArrayList<CalendarModel>) {
        dataList = items
    }

    override fun getItemCount(): Int = dataList.size

    inner class WeekDaysViewHolder(var binding: ItemWeekDayBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int, date: CalendarModel) {
            binding.tvDay.text = getDayName(date.date!!)
            binding.tvDate.text = getDay(date.date!!)

            binding.llDate.setOnClickListener {
                clickListener.onWeekClick(position, date)
            }

            if (date.isSelected) {
                binding.tvDate.textColor =
                    context.resources.getColor(R.color.app_main_text_white)
                binding.tvDay.textColor =
                    context.resources.getColor(R.color.app_main_text_white)
                binding.llDate.setBackgroundResource(R.drawable.bg_btn_green_main)

            } else {
                binding.tvDay.textColor =
                    context.resources.getColor(R.color.green_900)
                binding.tvDate.textColor =
                    context.resources.getColor(R.color.green_900)
                binding.llDate.setBackgroundResource(R.drawable.bg_btn_main_green_stroke)

            }
        }
    }

}

interface WeekDaysClickListener {
    fun onWeekClick(position: Int, item: CalendarModel)
}

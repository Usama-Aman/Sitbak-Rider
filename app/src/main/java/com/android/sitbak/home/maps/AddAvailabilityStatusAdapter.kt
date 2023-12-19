package com.android.sitbak.home.maps

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.android.sitbak.R
import com.android.sitbak.databinding.ItemAddAvailabilityStatusBinding
import com.astritveliu.boom.Boom
import org.jetbrains.anko.textColor

class AddAvailabilityStatusAdapter(
    var context: Context,
    var list: ArrayList<AvailabilityStatusModel>,
    var clickListener: AvailabilityStatusClickListener
) : RecyclerView.Adapter<AddAvailabilityStatusAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding: ItemAddAvailabilityStatusBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.item_add_availability_status,
            parent,
            false
        )
        return ViewHolder(context, binding, this@AddAvailabilityStatusAdapter)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBind(position)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class ViewHolder(
        var context: Context,
        var binding: ItemAddAvailabilityStatusBinding,
        var adapter: AddAvailabilityStatusAdapter
    ) : RecyclerView.ViewHolder(binding.root) {
        fun onBind(position: Int) {
            Boom(binding.root)

            val data = list[position]

            binding.tvStatus.text = data.type

            binding.tvStatus.setOnClickListener {
                clickListener.onStatusClick(adapterPosition, list[adapterPosition])
                notifyDataSetChanged()
            }
            if (data.isChecked) {
                binding.tvStatus.textColor =
                    context.resources.getColor(R.color.white)
                binding.tvStatus.setBackgroundResource(R.drawable.bg_btn_green_main)

            } else {
                binding.tvStatus.textColor =
                    context.resources.getColor(R.color.green_900)
                binding.tvStatus.setBackgroundResource(R.drawable.bg_btn_main_green_stroke)
            }
        }
    }
}


interface AvailabilityStatusClickListener {
    fun onStatusClick(position: Int, regionData: AvailabilityStatusModel)
}

data class AvailabilityStatusModel(var type: String = "", var isChecked: Boolean = false)
package com.android.sitbak.driver.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.android.sitbak.R
import com.android.sitbak.databinding.ItemLocationTagsBinding
import com.android.sitbak.home.popups.region_popup.RegionData
import com.astritveliu.boom.Boom
import org.jetbrains.anko.textColor

class LocationZoneAdapter(
    var selectedZoneId: Int,
    var context: Context,
    var list: MutableList<RegionData>,
    var clickListener: LocationZoneClickListener
) : RecyclerView.Adapter<LocationZoneAdapter.ViewHolder>() {

    private var selected = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding: ItemLocationTagsBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.item_location_tags,
            parent,
            false
        )
        return ViewHolder(context, binding, this@LocationZoneAdapter)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBind(position)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class ViewHolder(
        var context: Context,
        var binding: ItemLocationTagsBinding,
        var adapter: LocationZoneAdapter
    ) : RecyclerView.ViewHolder(binding.root) {
        fun onBind(position: Int) {
            Boom(binding.root)

            val data = list[position]

            binding.tvLocation.text = data.name

            binding.tvLocation.setOnClickListener {
                clickListener.onZoneClick(adapterPosition, list[adapterPosition])
                selectedZoneId = data.id
                notifyDataSetChanged()
            }
            if (selectedZoneId == data.id) {
                binding.tvLocation.textColor =
                    context.resources.getColor(R.color.white)
                binding.tvLocation.setBackgroundResource(R.drawable.bg_btn_green_main)

            } else {
                binding.tvLocation.textColor =
                    context.resources.getColor(R.color.green_900)
                binding.tvLocation.setBackgroundResource(R.drawable.bg_btn_main_green_stroke)
            }
        }
    }
}


interface LocationZoneClickListener {
    fun onZoneClick(position: Int, regionData: RegionData)
}
package com.android.sitbak.home.popups.region_popup

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.android.sitbak.R
import com.android.sitbak.databinding.RegionItemLayoutBinding
import org.jetbrains.anko.textColor


class RegionAdapter(var selectedZoneId: Int, var context: Context, var regionList: MutableList<RegionData>) :
    RecyclerView.Adapter<RegionAdapter.ViewHolder>() {

    var listener: RegionClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding: RegionItemLayoutBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context), R.layout.region_item_layout, parent, false
        )

        return ViewHolder(binding)
    }


    fun setAdapterListener(clickL: RegionClickListener) {
        listener = clickL
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        if (holder is ViewHolder) {
            val binding = holder.binding
            holder.onBind(position)
        }

    }

    fun setItems(list: MutableList<RegionData>) {
        this.regionList = list
    }

    override fun getItemCount(): Int {
        return regionList.size
    }

    inner class ViewHolder(
        var binding: RegionItemLayoutBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun onBind(position: Int) {
            val model = regionList[position]

            binding.ivSelect.visibility = if (selectedZoneId == model.id) View.VISIBLE else View.GONE
            val color = context.resources.getColor(if (selectedZoneId == model.id) R.color.green_900 else R.color.tasman)
            binding.tvRegion.textColor = color

            binding.tvRegion.text = model.name

            binding.parentLayout.setOnClickListener {
                selectedZoneId = model.id
                listener?.onItemClicked(position, regionList[position])
                notifyDataSetChanged()
            }

        }
    }
}

interface RegionClickListener {
    fun onItemClicked(position: Int, data: RegionData)
}
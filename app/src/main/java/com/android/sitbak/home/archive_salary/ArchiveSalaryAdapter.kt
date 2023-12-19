package com.android.sitbak.home.archive_salary

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.android.sitbak.R
import com.android.sitbak.databinding.ArchiveSalaryItemLayoutBinding
import com.android.sitbak.databinding.ItemLoadMoreBinding

class ArchiveSalaryAdapter(
    val archiveSalaryData: MutableList<ArchiveSalaryData?>,
    val archiveSalaryInterface: ArchiveSalaryInterface
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val ITEM_TYPE = 0
        private const val LOAD_MORE = 1
    }

    interface ArchiveSalaryInterface {
        fun onLoadMoreClicked()
    }

    private lateinit var mContext: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        mContext = parent.context
        return if (viewType == ITEM_TYPE) {
            Item(
                DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.archive_salary_item_layout, parent, false
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

    override fun getItemCount(): Int = archiveSalaryData.size

    override fun getItemViewType(position: Int): Int {
        return if (archiveSalaryData[position] == null) LOAD_MORE else ITEM_TYPE
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is Item -> holder.bind(position)
            is LoadMore -> holder.bind()
        }
    }

    inner class Item(val binding: ArchiveSalaryItemLayoutBinding) : RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(position: Int) {
            binding.date.text = archiveSalaryData[position]?.date
            binding.tvEarned.text = "$${archiveSalaryData[position]?.money}"

        }

    }

    inner class LoadMore(val binding: ItemLoadMoreBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind() {
            binding.loadMoreItem.setOnClickListener {
                archiveSalaryInterface.onLoadMoreClicked()
            }
        }

    }

}

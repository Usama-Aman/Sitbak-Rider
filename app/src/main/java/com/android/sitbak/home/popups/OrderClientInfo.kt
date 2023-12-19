package com.android.sitbak.home.popups

import android.content.Context
import android.graphics.Color
import android.graphics.Point
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.RecyclerView
import com.android.sitbak.R
import com.android.sitbak.databinding.DialogueOrderInfoBinding
import com.android.sitbak.databinding.ItemDiaglogueOrdeerBinding
import com.android.sitbak.home.tickets_tab.TicketsData
import com.android.sitbak.home.tickets_tab.TicketsItem
import com.astritveliu.boom.Boom
import com.bumptech.glide.Glide

class OrderClientInfo(val data: TicketsData?) : DialogFragment() {
    private lateinit var binding: DialogueOrderInfoBinding
    private lateinit var orderInfoAdapter: OrderInfoAdapter
    private lateinit var mContext: Context

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mContext = requireContext()
    }

    override fun onResume() {
        val window: Window? = dialog!!.window
        val size = Point()
        // Store dimensions of the screen in `size`
        // Store dimensions of the screen in `size`
        val display: Display = window!!.windowManager.defaultDisplay
        display.getSize(size)
        // Set the width of the dialog proportional to 75% of the screen width
        // Set the width of the dialog proportional to 75% of the screen width
        window.setLayout((size.x * 0.90).toInt(), (size.y * 0.80).toInt())
        window.setGravity(Gravity.CENTER)
        // Call super onResume after sizing
        // Call super onResume after sizing
        super.onResume()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.dialogue_order_info, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        initViews()
        listener()
    }

    private fun initViews() {
        Boom(binding.btnBottom)
        if (data != null) {

            Glide.with(mContext)
                .load(data.user.photo_path)
                .placeholder(R.drawable.ic_image_placeholder)
                .into(binding.ivUserImage)
            binding.tvUserName.text = data.user.name
            binding.tvDeliveryNote.text = data.delivery_note
//            binding.tvTotalAmount.text = data.

            initAdapter(data.items)
        }

    }

    private fun initAdapter(items: List<TicketsItem>) {
        orderInfoAdapter = OrderInfoAdapter(items)
        binding.rvOrderItem.adapter = orderInfoAdapter
    }

    private fun listener() {
        binding.btnBottom.setOnClickListener {
            dismiss()
        }
    }


    inner class OrderInfoAdapter(val items: List<TicketsItem>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

            val binding: ItemDiaglogueOrdeerBinding =
                DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.item_diaglogue_ordeer, parent, false
                )

            return OrderInfoViewHolder(binding)
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

            if (holder is OrderInfoViewHolder)
                holder.bind(position)
        }

        override fun getItemCount(): Int = items.size

        inner class OrderInfoViewHolder(val binding: ItemDiaglogueOrdeerBinding) :
            RecyclerView.ViewHolder(binding.root) {

            fun bind(position: Int) {
                binding.tvItemName.text = items[position].product_name
                binding.tvItemQuantity.text = items[position].quantity.toString()
            }

        }


    }
}
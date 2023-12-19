package com.android.sitbak.home.popups

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.Point
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import androidx.fragment.app.DialogFragment
import com.android.sitbak.R
import com.android.sitbak.databinding.FragmentTimeAndPaymentPopUpBinding
import com.android.sitbak.home.tickets_tab.TicketsData
import com.astritveliu.boom.Boom


class TimeAndPaymentPopUp(val data: TicketsData?) : DialogFragment() {

    private lateinit var binding: FragmentTimeAndPaymentPopUpBinding
    private lateinit var mContext: Context

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentTimeAndPaymentPopUpBinding.inflate(layoutInflater, container, false)
        mContext = requireContext()
        return binding.root
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
        window.setLayout((size.x * 0.90).toInt(), WindowManager.LayoutParams.WRAP_CONTENT)
        window.setGravity(Gravity.CENTER)
        // Call super onResume after sizing
        // Call super onResume after sizing
        super.onResume()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        initVar()
        listener()
    }

    private fun listener() {
        binding.btnOk.setOnClickListener {
            dismiss()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun initVar() {
        Boom(binding.btnOk)
        if (data != null) {
            binding.tvDeliveryTime.text =
                "${data.start_time.replace("AM", "").replace("PM", "")}- ${data.end_time}"

            binding.tvRemainingTime.text = mContext.resources.getString(R.string.time_left_for_delivery, "0")
            binding.tvDeliveryAmount.text = "$${data.delivery_charges}"
            binding.tvTipAmount.text = "$${data.driver_tip}"
            val total = data.delivery_charges.toDouble() + data.driver_tip.toDouble()
            binding.tvTotalAmount.text = "$$total"

        }

    }
}
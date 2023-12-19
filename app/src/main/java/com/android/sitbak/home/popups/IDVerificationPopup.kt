package com.android.sitbak.home.popups

import android.graphics.Color
import android.graphics.Point
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import androidx.fragment.app.DialogFragment
import com.android.sitbak.R
import com.android.sitbak.databinding.FragmentIDVerificationPopupBinding
import com.android.sitbak.home.tickets_tab.TicketsData
import com.astritveliu.boom.Boom
import com.bumptech.glide.Glide


class IDVerificationPopup(val ticketsData: TicketsData, private val verifyIDInterface: VerifyIDInterface) : DialogFragment() {

    private lateinit var binding: FragmentIDVerificationPopupBinding

    interface VerifyIDInterface {
        fun completeOrder(id: Int)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentIDVerificationPopupBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onResume() {
        val window: Window? = dialog!!.window
        val size = Point()
        // Store dimensions of the screen in `size`
        // Store dimensions of the screen in `size`
        val display: Display = window!!.windowManager.getDefaultDisplay()
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

        initViews()
        initListeners()
    }

    private fun initViews() {
        Boom(binding.btnCancel)
        Boom(binding.btnCompleteOrder)

        Glide.with(requireContext())
            .load(ticketsData.user.photo_path)
            .placeholder(R.drawable.ic_image_placeholder)
            .into(binding.ivIDCard)

    }

    private fun initListeners() {
        binding.btnCancel.setOnClickListener {
            dismiss()
        }

        binding.btnCompleteOrder.setOnClickListener {
            verifyIDInterface.completeOrder(ticketsData.id)
            dismiss()
        }
    }

}
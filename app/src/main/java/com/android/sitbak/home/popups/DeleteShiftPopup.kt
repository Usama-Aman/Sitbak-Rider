package com.android.sitbak.home.popups

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.Point
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import androidx.fragment.app.DialogFragment
import com.android.sitbak.base.BaseActivity
import com.android.sitbak.databinding.FragmentDeleteShiftPopupBinding
import com.android.sitbak.home.scheduling_tab.my_shifts.AvailabilitiesData
import com.astritveliu.boom.Boom


class DeleteShiftPopup(val data: AvailabilitiesData?) : DialogFragment() {

    private lateinit var binding: FragmentDeleteShiftPopupBinding
    private var clickListener: DeleteShiftPopUpClickListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentDeleteShiftPopupBinding.inflate(layoutInflater, container, false)
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

    fun setListener(listener: DeleteShiftPopUpClickListener) {
        this.clickListener = listener
    }

    private fun listener() {
        binding.btnCancel.setOnClickListener {
            dismiss()
        }
        binding.btnDelete.setOnClickListener {
            clickListener?.onDeleteClicked()
            dismiss()
        }

    }

    @SuppressLint("SetTextI18n", "NewApi")
    private fun initVar() {
        Boom(binding.btnDelete)
        Boom(binding.btnCancel)

        if (data != null) {
            binding.tvDate.text = data.start_date_formatted
            binding.tvTimeDuration.text = (data.start_time + " - " + data.end_time)
            binding.tvLocation.text = data.region?.address
            binding.tvTotalTime.text = (context as BaseActivity).diffTimeForHours(data.start_time!!, data.end_time!!)
                .toString() + "h" + (context as BaseActivity).diffTimeForMin(data.start_time, data.end_time).toString() + "m"
        }
    }

    interface DeleteShiftPopUpClickListener {
        fun onDeleteClicked()
    }
}


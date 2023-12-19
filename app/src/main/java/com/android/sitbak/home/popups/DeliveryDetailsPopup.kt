package com.android.sitbak.home.popups

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Point
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.*
import androidx.fragment.app.DialogFragment
import com.android.sitbak.databinding.FragmentDeliveryDetailsPopupBinding
import com.android.sitbak.home.tickets_tab.TicketsData
import com.astritveliu.boom.Boom


class DeliveryDetailsPopup(val data: TicketsData?) : DialogFragment() {

    private lateinit var binding: FragmentDeliveryDetailsPopupBinding
    private lateinit var mContext: Context

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mContext = requireContext()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentDeliveryDetailsPopupBinding.inflate(layoutInflater, container, false)
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
    }

    @SuppressLint("SetTextI18n")
    private fun initVar() {
        Boom(binding.btnRouteFromYouToStore)
        Boom(binding.btnRouteFromStoreToClient)

        if (data != null) {

            binding.tvStoreName.text = data.store.name
            binding.tvStoreAddress.text = data.store_location.address
            binding.tvUserLocation.text = data.delivery_location.address
            binding.tvDistance.text = "${data.distance} km"

            binding.btnRouteFromYouToStore.setOnClickListener {
                val intent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("http://maps.google.com/maps?daddr=${data.store_location.latitude},${data.store_location.longitude}")
                )
                startActivity(intent)
                dismiss()
            }

            binding.btnRouteFromStoreToClient.setOnClickListener {
                val intent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("http://maps.google.com/maps?saddr=${data.store_location.latitude},${data.store_location.longitude}" +
                            "&daddr=${data.delivery_location.latitude},${data.delivery_location.longitude}")
                )
                startActivity(intent)
                dismiss ()
            }
        }
    }

}
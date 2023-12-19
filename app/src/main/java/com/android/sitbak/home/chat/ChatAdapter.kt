package com.android.sitbak.home.chat

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.android.sitbak.R
import com.android.sitbak.databinding.ItemChatLeftTextBinding
import com.android.sitbak.databinding.ItemChatRightTextBinding

class ChatAdapter(private val chatList: MutableList<ChatData>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private lateinit var mContext: Context

    companion object {
        const val TEXT_ITEM_RIGHT = 0
        const val TEXT_ITEM_LEFT = 1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        mContext = parent.context
        if (viewType == TEXT_ITEM_RIGHT)
            return TextItemRight(
                DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.item_chat_right_text, parent, false
                )
            )
        else (viewType == TEXT_ITEM_LEFT)
        return TextItemLeft(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.item_chat_left_text, parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is TextItemLeft -> holder.bind(position)
            is TextItemRight -> holder.bind(position)
        }

    }

    override fun getItemCount(): Int = chatList.size

    override fun getItemViewType(position: Int): Int {
        return if (chatList[position].sender_type == "user")
            TEXT_ITEM_LEFT
        else
            TEXT_ITEM_RIGHT
    }

    inner class TextItemLeft(_binding: ItemChatLeftTextBinding) : RecyclerView.ViewHolder(_binding.root) {

        private val binding = _binding

        fun bind(position: Int) {
            binding.tvChatText.text = chatList[position].message
            binding.tvChatTime.text = chatList[position].created_at
        }

    }

    inner class TextItemRight(_binding: ItemChatRightTextBinding) : RecyclerView.ViewHolder(_binding.root) {

        private val binding = _binding

        fun bind(position: Int) {
            binding.tvChatText.text = chatList[position].message
            binding.tvChatTime.text = chatList[position].created_at
        }

    }
}
package com.android.sitbak.home.chat

data class ChatModel(
    val `data`: ArrayList<ChatData>,
    val message: String,
    val status: Boolean,
    val user_name: String
)


data class ChatData(
    val chat_id: Int,
    val created_at: String?,
    val file_path: String?,
    val id: Int,
    val is_delivered: Int,
    val is_seen: Int,
    val message: String?,
    val receiver_id: Int,
    val sender_id: Int,
    val sender_type: String?,
    val type: String?
)
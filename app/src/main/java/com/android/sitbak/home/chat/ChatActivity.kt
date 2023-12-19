package com.android.sitbak.home.chat

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.sitbak.R
import com.android.sitbak.base.AppController
import com.android.sitbak.base.BaseActivity
import com.android.sitbak.base.ViewModelFactory
import com.android.sitbak.databinding.ActivityChatBinding
import com.android.sitbak.home.HomeActivity
import com.android.sitbak.network.Api
import com.android.sitbak.network.ApiTags
import com.android.sitbak.network.RetrofitClient
import com.android.sitbak.utils.*
import com.bumptech.glide.Glide
import com.google.gson.Gson
import okhttp3.ResponseBody
import retrofit2.Call

class ChatActivity : BaseActivity() {

    private lateinit var binding: ActivityChatBinding
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var mContext: Context
    private var chatList: MutableList<ChatData> = ArrayList()
    private lateinit var viewModel: ChatActivityVM
    private lateinit var retrofitClient: Api
    private lateinit var apiCall: Call<ResponseBody>

    private var orderId = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_chat)
        mContext = this
        retrofitClient = RetrofitClient.getClient(this).create(Api::class.java)

        changeStatusBarColor(R.color.heavy_metal)

        initVM()
        initObservers()
        initViews()
        initListeners()
        initAdapter()

        getUserChats()
    }

    private fun getUserChats() {
        apiCall = retrofitClient.getChats(orderId)
        viewModel.getChats(apiCall)
    }


    private fun initVM() {
        viewModel = ViewModelProviders.of(this, ViewModelFactory()).get(ChatActivityVM::class.java)
        binding.viewModel = viewModel

        binding.executePendingBindings()
        binding.lifecycleOwner = this
    }

    private fun initObservers() {
        viewModel.getApiResponse().observe(this, {
            when (it.status) {
                ApiStatus.LOADING -> {
                    if (it.tag != ApiTags.SEND_CHAT)
                        Loader.showLoader(this)
                    Loader.progressKHUD?.setCancellable {
                        if (this::apiCall.isInitialized)
                            apiCall.cancel()
                    }
                }
                ApiStatus.ERROR -> {
                    if (it.tag == ApiTags.SEND_CHAT) {
                        binding.progressBar.viewGone()
                        binding.ivSendChat.viewVisible()
                    }
                    Loader.hideLoader()
                    AppUtils.showToast(this, it.message!!, false)
                }
                ApiStatus.SUCCESS -> {
                    Loader.hideLoader()
                    when (it.tag) {
                        ApiTags.SEND_CHAT -> {
                            binding.ivSendChat.viewVisible()
                            binding.progressBar.viewGone()

                            Log.d(ApiTags.SEND_CHAT, "Api : Message Sent ")
                            if (it?.data?.has("data") == true)
                                if (it?.data?.getJSONObject("data") != null) {
                                    val jsonObjectData = it.data.getJSONObject("data")
                                    val data = Gson().fromJson(jsonObjectData.toString(), ChatData::class.java)

                                    chatList.add(data)
                                    chatAdapter.notifyDataSetChanged()
                                    binding.chatRecyclerView.smoothScrollToPosition(chatList.size)

                                    viewModel.emitChatMessage(jsonObjectData)

                                    binding.llNoData.viewGone()
                                    binding.chatRecyclerView.viewVisible()
                                }

                        }
                        ApiTags.GET_CHAT -> {
                            val model = Gson().fromJson(it.data.toString(), ChatModel::class.java)
                            handleChatResponse(model.data)
                        }
                    }
                }
            }
        })


        viewModel.getSocketReceiveMessage().observe(this, {
            if (it != null) {
                chatList.add(
                    ChatData(
                        it.chat_id, it.created_at, it.file_path, it.id, it.is_delivered, it.is_seen,
                        it.message, it.receiver_id, it.sender_id, it.sender_type, it.type
                    )
                )
                chatAdapter.notifyDataSetChanged()
                binding.chatRecyclerView.smoothScrollToPosition(chatList.size)
            }
        })
    }

    private fun handleChatResponse(data: ArrayList<ChatData>) {

        if (data.isNotEmpty()) {
            chatList.clear()
            chatList.addAll(data)
            chatAdapter.notifyDataSetChanged()
            binding.chatRecyclerView.viewVisible()
            binding.llNoData.viewGone()
        } else {
            binding.llNoData.viewVisible()
            binding.chatRecyclerView.viewGone()
        }
    }


    private fun initViews() {
        checkForIntent(intent)

    }

    private fun initListeners() {
        binding.ivBack.setOnClickListener {
            finish()
        }

        binding.etChat.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (s.toString().isEmpty()) {
                    binding.ivSendChat.setImageResource(R.drawable.ic_chat_send_grey)
                    binding.ivSendChat.isEnabled = false
                } else {
                    binding.ivSendChat.setImageResource(R.drawable.ic_chat_send_filled)
                    binding.ivSendChat.isEnabled = true
                }
            }

        })

        binding.ivSendChat.setOnClickListener {
            if (binding.etChat.text.toString().isNotEmpty()) {
                if (isOnline()) {
                    binding.ivSendChat.viewGone()
                    binding.progressBar.viewVisible()
                    apiCall = retrofitClient.sendChatMessage("text", orderId, binding.etChat.text.toString())
                    viewModel.sendMessage(apiCall)
                    binding.etChat.setText("")
                } else {
                    AppUtils.showToast(this, "No Internet", false)
                }
            }
        }
    }

    private fun initAdapter() {
        chatAdapter = ChatAdapter(chatList)
        val lm = LinearLayoutManager(mContext)
        lm.stackFromEnd = true
        lm.reverseLayout = false
        binding.chatRecyclerView.layoutManager = lm
        binding.chatRecyclerView.adapter = chatAdapter
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        checkForIntent(intent)
    }

    private fun checkForIntent(intent: Intent?) {

        if (intent != null) {
            if (intent.hasExtra(Constants.orderId)) {
                val oid = intent.getIntExtra(Constants.orderId, -1)
                if (oid != orderId) {
                    orderId = oid
                    chatList.clear()
                    getUserChats()
                } else
                    orderId = oid
            }

            if (intent.hasExtra("senderName"))
                binding.tvTitle.text = intent.getStringExtra("senderName")

            if (intent.hasExtra("senderPhoto"))
                Glide.with(mContext)
                    .load(intent.getStringExtra("senderPhoto"))
                    .placeholder(R.drawable.ic_image_placeholder)
                    .into(binding.ivUserImage)
        }
    }

    override fun onBackPressed() {
        if (AppController.isHomeActive) {
            super.onBackPressed()
        } else {
            val intent = Intent(mContext, HomeActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
            finish()
        }
    }


}
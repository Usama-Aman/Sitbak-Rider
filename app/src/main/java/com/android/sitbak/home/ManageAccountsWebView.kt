package com.android.sitbak.home

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.databinding.DataBindingUtil
import com.android.sitbak.R
import com.android.sitbak.base.BaseActivity
import com.android.sitbak.databinding.ActivityManageAccountBinding
import com.android.sitbak.utils.Loader

class ManageAccountsWebView : BaseActivity() {

    private lateinit var binding: ActivityManageAccountBinding
    private lateinit var mContext: Context

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_manage_account)
        mContext = this

        changeStatusBarColor(R.color.heavy_metal)

        initViews()
        initListeners()
    }

    @SuppressLint("SetJavaScriptEnabled", "JavascriptInterface")
    private fun initViews() {
        val url: String? = intent.getStringExtra("url")

        if (url.isNullOrBlank())
            finish()

        binding.webView.settings.javaScriptEnabled = true
        binding.webView.loadUrl(url!!)


        binding.webView.webViewClient = object : WebViewClient() {

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                Loader.showLoader(mContext)
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                Loader.hideLoader()
            }

            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                view?.loadUrl(url.toString())
                return false
            }
        }

    }

    private fun initListeners() {
        binding.ivBack.setOnClickListener {
            finish()
        }
    }

    override fun onBackPressed() {
        if (binding.webView.canGoBack())
            binding.webView.goBack()
        else
            finish()
    }

}
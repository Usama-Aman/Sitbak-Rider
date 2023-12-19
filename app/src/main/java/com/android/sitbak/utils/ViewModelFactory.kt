package com.android.sitbak.utils

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

import com.android.sitbak.auth.login.LoginVM
import com.android.sitbak.auth.otp.VerifyOtpVM
import com.android.sitbak.auth.phone_number.PhoneVerificationVM
import com.android.sitbak.auth.signup.SignUpVM

class ViewModelFactory : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(LoginVM::class.java) -> {
                LoginVM() as T
            }
            modelClass.isAssignableFrom(SignUpVM::class.java) -> {
                SignUpVM() as T
            }
            modelClass.isAssignableFrom(VerifyOtpVM::class.java) -> {
                VerifyOtpVM() as T
            }
            modelClass.isAssignableFrom(PhoneVerificationVM::class.java) -> {
                PhoneVerificationVM() as T
            }

            else -> throw IllegalArgumentException("Unknown class name")
        }
    }
}

package com.android.sitbak.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.android.sitbak.auth.add_photo.AddPhotoVM
import com.android.sitbak.auth.change_password.ChangePasswordVM

import com.android.sitbak.auth.login.LoginVM
import com.android.sitbak.auth.otp.VerifyOtpVM
import com.android.sitbak.auth.password_recovery.PasswordRecoveryVM
import com.android.sitbak.auth.reset_password.ResetPasswordVM
import com.android.sitbak.auth.signup.SignUpVM
import com.android.sitbak.home.archive_salary.ArchiveSalaryVM
import com.android.sitbak.home.chat.ChatActivityVM
import com.android.sitbak.home.completed_shifts.CompletedShiftsVM
import com.android.sitbak.home.earning.YourEarningsVM
import com.android.sitbak.home.maps.AddAvailabilityActivityVM
import com.android.sitbak.home.popups.region_popup.RegionFragmentVM
import com.android.sitbak.home.profile_tab.ProfileFragmentVM
import com.android.sitbak.home.scheduling_tab.my_shifts.MyShiftsFragmentVM
import com.android.sitbak.home.scheduling_tab.open_shifts.OpenShiftsFragmentVM
import com.android.sitbak.home.tickets_tab.available.AvailableTicketsVM
import com.android.sitbak.home.tickets_tab.processing.ProcessingTicketsVM


@Suppress("UNCHECKED_CAST")
class ViewModelFactory : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(LoginVM::class.java) -> {
                LoginVM() as T
            }
            modelClass.isAssignableFrom(SignUpVM::class.java) -> {
                SignUpVM() as T
            }
            modelClass.isAssignableFrom(PasswordRecoveryVM::class.java) -> {
                PasswordRecoveryVM() as T
            }
            modelClass.isAssignableFrom(VerifyOtpVM::class.java) -> {
                VerifyOtpVM() as T
            }
            modelClass.isAssignableFrom(SignUpVM::class.java) -> {
                SignUpVM() as T
            }
            modelClass.isAssignableFrom(AddPhotoVM::class.java) -> {
                AddPhotoVM() as T
            }
            modelClass.isAssignableFrom(PasswordRecoveryVM::class.java) -> {
                PasswordRecoveryVM() as T
            }
            modelClass.isAssignableFrom(ResetPasswordVM::class.java) -> {
                ResetPasswordVM() as T
            }
            modelClass.isAssignableFrom(ProfileFragmentVM::class.java) -> {
                ProfileFragmentVM() as T
            }
            modelClass.isAssignableFrom(AvailableTicketsVM::class.java) -> {
                AvailableTicketsVM() as T
            }
            modelClass.isAssignableFrom(ProcessingTicketsVM::class.java) -> {
                ProcessingTicketsVM() as T
            }
            modelClass.isAssignableFrom(ChatActivityVM::class.java) -> {
                ChatActivityVM() as T
            }
            modelClass.isAssignableFrom(YourEarningsVM::class.java) -> {
                YourEarningsVM() as T
            }
            modelClass.isAssignableFrom(ChangePasswordVM::class.java) -> {
                ChangePasswordVM() as T
            }
            modelClass.isAssignableFrom(RegionFragmentVM::class.java) -> {
                RegionFragmentVM() as T
            }
            modelClass.isAssignableFrom(MyShiftsFragmentVM::class.java) -> {
                MyShiftsFragmentVM() as T
            }
            modelClass.isAssignableFrom(AddAvailabilityActivityVM::class.java) -> {
                AddAvailabilityActivityVM() as T
            }
            modelClass.isAssignableFrom(OpenShiftsFragmentVM::class.java) -> {
                OpenShiftsFragmentVM() as T
            }
            modelClass.isAssignableFrom(CompletedShiftsVM::class.java) -> {
                CompletedShiftsVM() as T
            }
            modelClass.isAssignableFrom(ArchiveSalaryVM::class.java) -> {
                ArchiveSalaryVM() as T
            }
            else -> throw IllegalArgumentException("Unknown class name")
        }
    }
}

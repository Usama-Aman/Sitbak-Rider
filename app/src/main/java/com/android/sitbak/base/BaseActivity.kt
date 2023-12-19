package com.android.sitbak.base

import android.Manifest
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_DIAL
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.icu.text.SimpleDateFormat
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.Settings
import android.util.Patterns
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.android.sitbak.R
import com.android.sitbak.utils.HideUtil
import es.dmoral.toasty.Toasty
import java.util.*


open class BaseActivity : AppCompatActivity() {
    lateinit var mViewGroup: ViewGroup
    var fontBold: Typeface? = null
    var fontSemiBold: Typeface? = null
    var fontRegular: Typeface? = null

//    open lateinit var sp: SharedPreferences

    open fun onSetupViewGroup(mVG: ViewGroup) {
        mViewGroup = mVG
        HideUtil.init(this, mViewGroup)
    }

    @SuppressLint("ObjectAnimatorBinding")
    fun hoverEffect1(customView: Any): AnimatorSet {

        var animatorSet = AnimatorSet();
        var fadeOut: ObjectAnimator = ObjectAnimator.ofFloat(customView, "alpha", 1.0f, 0.1f)
        fadeOut.duration = 100
        var fadeIn: ObjectAnimator = ObjectAnimator.ofFloat(customView, "alpha", 0.1f, 1.0f)
        fadeIn.duration = 100
        animatorSet.play(fadeIn).after(fadeOut)


        return animatorSet

    }

    fun phoneNumnberCall() {
        val intent = Intent(ACTION_DIAL)
        intent.data = Uri.parse("tel:0123456789")
        startActivity(intent)
    }

/*
    private open fun initActivityScreenOrientPortrait() {
        // Avoid screen rotations (use the manifests android:screenOrientation setting)
        // Set this to nosensor or potrait

        // Set window fullscreen
        this.activity.getWindow().setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        val metrics = DisplayMetrics()
        this.activity.getWindowManager().getDefaultDisplay().getMetrics(metrics)

        // Test if it is VISUAL in portrait mode by simply checking it's size
        val bIsVisualPortrait = metrics.heightPixels >= metrics.widthPixels
        if (!bIsVisualPortrait) {
            // Swap the orientation to match the VISUAL portrait mode
            if (this.activity.getResources()
                    .getConfiguration().orientation === Configuration.ORIENTATION_PORTRAIT
            ) {
                this.activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
            } else {
                this.activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
            }
        } else {
            this.activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR)
        }
    }
*/

    @SuppressLint("ObjectAnimatorBinding")
    fun hoverEffect2(customView: Any) {

        var animatorSet = AnimatorSet()
        var fadeOut: ObjectAnimator = ObjectAnimator.ofFloat(customView, "alpha", 1.0f, 0.1f)
        fadeOut.duration = 100
        var fadeIn: ObjectAnimator = ObjectAnimator.ofFloat(customView, "alpha", 0.1f, 1.0f)
        fadeIn.duration = 100
        animatorSet.play(fadeOut)
        animatorSet.start()

    }

    fun makeVibration() {
        // Get instance of Vibrator from current Context
        // Get instance of Vibrator from current Context
        val v = getSystemService(VIBRATOR_SERVICE) as Vibrator

// Vibrate for 400 milliseconds

// Vibrate for 400 milliseconds
        v.vibrate(400)
    }

    // Vibrate for 150 milliseconds
    private fun shakeItBaby(context: Context) {
        if (Build.VERSION.SDK_INT >= 26) {
            (context.getSystemService(VIBRATOR_SERVICE) as Vibrator).vibrate(
                VibrationEffect.createOneShot(
                    150,
                    VibrationEffect.DEFAULT_AMPLITUDE
                )
            )
        } else {
            (context.getSystemService(VIBRATOR_SERVICE) as Vibrator).vibrate(150)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        supportActionBar?.hide()
        super.onCreate(savedInstanceState)
        initFonts()

    }

    public fun hideStatusBar() {
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        );
    }


    fun fullScreen() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        /*  val actionBar: ActionBar? = supportActionBar
          actionBar?.hide()*/
    }

    private fun initFonts() {
        fontBold = ResourcesCompat.getFont(this, R.font.inter_bold)
        fontSemiBold = ResourcesCompat.getFont(this, R.font.inter_semibold)
        fontRegular =
            ResourcesCompat.getFont(this, R.font.inter_regular)
    }

    fun clearLightStatusBar(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val window: Window = activity.window
            window.setStatusBarColor(
                ContextCompat
                    .getColor(activity, R.color.black)
            )
        }
    }

    fun blackStatusBarIcons() {
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR)
    }

    fun showInfoToast(context: Context, message: String) {
        Toasty.info(context, "" + message, Toast.LENGTH_SHORT, true).show();
    }

    @SuppressLint("InlinedApi")
    fun changeStatusBarColor(resourseColor: Int) {

        val window: Window = window

        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = ContextCompat.getColor(applicationContext, resourseColor)
        }
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);     //  Fixed Portrait orientation

    }


    open fun hideKeyboard() {
        val imm: InputMethodManager =
            getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        //Find the currently focused view, so we can grab the correct window token from it.
        var view: View? = currentFocus
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = View(this)
        }
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    fun <T> List<T>.toArrayList(): ArrayList<T> {
        return ArrayList(this)
    }

    fun isLocationEnabled(context: Context): Boolean {
        val locationManager: LocationManager =
            context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    fun showGPSNotEnabledDialog(context: Context) {
        AlertDialog.Builder(context)
            .setTitle("Enable Gps")
            .setMessage("Please enable the ")
            .setCancelable(false)
            .setPositiveButton("Enable Now!") { _, _ ->
                context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
            .show()
    }

    fun isAccessFineLocationGranted(context: Context): Boolean {
        return ContextCompat
            .checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
    }

    fun String.isValidEmail(): Boolean =
        this.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(this).matches()

    fun String.isValidMobileNumber(): Boolean = this.isNotEmpty() && Patterns.PHONE.matcher(this).matches()

    fun isOnline(): Boolean {
        try {
            val p1 = Runtime.getRuntime().exec("ping -c 1 www.google.com")
            val returnVal = p1.waitFor()
            return returnVal == 0
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    @RequiresApi(Build.VERSION_CODES.N)
    @SuppressLint("SimpleDateFormat")
    open fun diffTimeForMin(startTime: String, endTime: String): Long {
        var min: Long = 0
        val difference: Long
        try {
            val simpleDateFormat = SimpleDateFormat("hh:mm aa") // for 12-hour system, hh should be used instead of HH
            // There is no minute different between the two, only 8 hours difference. We are not considering Date, So minute will always remain 0
            val date1: Date = simpleDateFormat.parse(startTime)
            val date2: Date = simpleDateFormat.parse(endTime)
            difference = (date2.time - date1.time) / 1000
            val hours = difference % (24 * 3600) / 3600 // Calculating Hours
            val minute = difference % 3600 / 60 // Calculating minutes if there is any minutes difference
            min = minute // This will be our final minutes. Multiplying by 60 as 1 hour contains 60 mins
        } catch (e: Throwable) {
            e.printStackTrace()
        }
        return min
    }
    @SuppressLint("NewApi")
    open fun diffTimeForHours(startTime: String, endTime: String): Long {
        var min: Long = 0
        var hour: Long = 0
        val difference: Long
        try {
            val simpleDateFormat = SimpleDateFormat("hh:mm aa") // for 12-hour system, hh should be used instead of HH
            // There is no minute different between the two, only 8 hours difference. We are not considering Date, So minute will always remain 0
            val date1: Date = simpleDateFormat.parse(startTime)
            val date2: Date = simpleDateFormat.parse(endTime)
            difference = (date2.time - date1.time) / 1000
            var hours = difference % (24 * 3600) / 3600 // Calculating Hours
            val minute = difference % 3600 / 60 // Calculating minutes if there is any minutes difference
            min = minute + hours * 60 // This will be our final minutes. Multiplying by 60 as 1 hour contains 60 mins
            hour = if (hours < 0) -hours else hours
        } catch (e: Throwable) {
            e.printStackTrace()
        }
        return hour
    }



}
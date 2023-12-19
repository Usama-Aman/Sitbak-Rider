package com.android.sitbak.utils


import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_DIAL
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.text.Spanned
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import es.dmoral.toasty.Toasty
import java.text.SimpleDateFormat
import java.util.*


/*Functions of toast*/
fun showSuccessToast(context: Context, message: String) {
//    Toasty.success(context, "" + message, Toast.LENGTH_SHORT, true).show();
    context.jSuccessToast(message)
}

fun Context.htmlText(text: String): Spanned {

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        return Html.fromHtml(text, Html.FROM_HTML_MODE_COMPACT);
    } else {
        return Html.fromHtml(text)
    }

}

fun Fragment.htmlText(text: String): Spanned {

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        return Html.fromHtml(text, Html.FROM_HTML_MODE_COMPACT);
    } else {
        return Html.fromHtml(text)
    }

}


fun Context.isNetworkConnected(): Boolean {
    val cm = getSystemService(AppCompatActivity.CONNECTIVITY_SERVICE) as ConnectivityManager
    return cm.activeNetworkInfo != null && cm.activeNetworkInfo!!.isConnected
}


fun View.viewVisible() {
    this.visibility = View.VISIBLE
}

fun View.viewGone() {
    this.visibility = View.GONE
}

fun View.viewInVisible() {
    this.visibility = View.INVISIBLE
}

fun Fragment.isNetworkConnected(): Boolean {
    val cm =
        requireContext().getSystemService(AppCompatActivity.CONNECTIVITY_SERVICE) as ConnectivityManager
    return cm.activeNetworkInfo != null && cm.activeNetworkInfo!!.isConnected
}

fun Fragment.showKeyboardFrom() {

    val imm =
        requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
}

fun Context.showKeyboardFrom() {
    val imm =
        getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
}

fun Fragment.hideKeyboardFrom(view: View) {
    val imm = requireContext().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(view.windowToken, 0)
}

fun Context.hideKeyboardFrom(view: View) {
    val imm = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(view.windowToken, 0)
}

fun showErrorToast(context: Context, message: String) {

    context.jErrorToast(message)

//    Toasty.error(context, "" + message, Toast.LENGTH_SHORT, true).show();
}


fun Fragment.jErrorToast(message: String) {

    Toasty.error(requireContext(), "" + message, Toast.LENGTH_SHORT, true).show();

//    CustomToastView.makeErrorToast(requireContext(),message,1).show()
}

fun Fragment.jSuccessToast(message: String) {
//    CustomToastView.makeSuccessToast(requireContext(),message,1).show()
    Toasty.success(requireContext(), "" + message, Toast.LENGTH_SHORT, true).show();
}

fun Context.jErrorToast(message: String) {
    Toasty.error(this, "" + message, Toast.LENGTH_SHORT, true).show();
//    CustomToastView.makeErrorToast(this,message,1).show()
}

fun Context.jSuccessToast(message: String) {
//    CustomToastView.makeSuccessToast(this,message,1).show()
    Toasty.success(this, "" + message, Toast.LENGTH_SHORT, true).show();
}


public fun AppCompatActivity.J_startActivityForResult(
    javaClass: Any,
    code: Int,
    bundle: Bundle = Bundle()
) {

    var intent = Intent(this, javaClass as Class<*>)
    intent.putExtra(Utils.jbundle, bundle)
    startActivityForResult(intent, code)
}


public fun Activity.J_startActivityForResult(javaClass: Any, code: Int, bundle: Bundle = Bundle()) {

    var intent = Intent(this, javaClass as Class<*>)
    intent.putExtra(Utils.jbundle, bundle)
    startActivityForResult(intent, code)
}


public fun Fragment.J_startActivity(javaClass: Any, bundle: Bundle = Bundle()) {

    var intent = Intent(requireContext(), javaClass as Class<*>)
    intent.putExtra("jbundle", bundle)
    startActivity(intent)
}

//private fun Intent.putExtra(s: String, any: Any) {
//
//}

public fun Context.J_startActivity(javaClass: Any, bundle: Bundle = Bundle()) {

    var intent = Intent(this, javaClass as Class<*>)

    intent.putExtra(Utils.jbundle, bundle)
    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

    startActivity(intent)

}

fun Fragment.phoneNumberCall(number: String) {
    val intent = Intent(ACTION_DIAL)
    intent.data = Uri.parse("tel:${number}")
    startActivity(intent)
}

fun Context.phoneNumberCall(number: String) {
    val intent = Intent(ACTION_DIAL)
    intent.data = Uri.parse("tel:${number}")
    startActivity(intent)
}

fun Fragment.openNavigationMap(lat: String, lng: String) {
    val intent = Intent(
        Intent.ACTION_VIEW,
        Uri.parse("http://maps.google.com/maps?daddr=$lat,$lng")
    )
    startActivity(intent)
}

fun Context.openNavigationMap(location: String) {
    val gmmIntentUri = Uri.parse("google.navigation:q=${location}")
    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
    mapIntent.setPackage("com.google.android.apps.maps")
    startActivity(mapIntent)

}
@SuppressLint("SimpleDateFormat")
fun getDayName(date: Date) : String {
    val outFormat = SimpleDateFormat("EEE")
    val day: String = outFormat.format(date)
    return day
}

@SuppressLint("SimpleDateFormat")
fun getDay(date: Date) : String {
    val inFormat = SimpleDateFormat("dd")
    val day: String = inFormat.format(date)
    return day
}




package com.android.sitbak.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.android.sitbak.R
import com.android.sitbak.home.tickets_tab.processing.ProcessingTicketsFragment
import com.android.sitbak.network.SocketIO
import com.android.sitbak.utils.AppUtils
import com.android.sitbak.utils.Constants
import com.android.sitbak.utils.OrdersDeliveryStatus
import com.android.sitbak.utils.OrdersDeliveryStatus.DRIVER_AT_STORE
import com.android.sitbak.utils.OrdersDeliveryStatus.DRIVER_AT_USER_PLACE
import com.android.sitbak.utils.OrdersDeliveryStatus.DRIVER_ON_WAY
import com.android.sitbak.utils.OrdersDeliveryStatus.PICKED_BY_DRIVER
import com.android.sitbak.utils.OrdersDeliveryStatus.READY_FOR_PICKUP
import com.android.sitbak.utils.SharedPreference
import com.firebase.geofire.GeoFire
import com.firebase.geofire.GeoLocation
import com.firebase.geofire.GeoQueryEventListener
import com.google.android.gms.location.*
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import org.json.JSONObject
import java.util.*


class LocationService : Service(), LocationListener {

    companion object {
        private const val LOCATION_INTERVAL: Long = 1000
        private const val LOCATION_DISTANCE: Float = .5f
        private const val GEO_FENCE_RADIUS: Double = 1.0 /* .5 = 500meters, .5km*/

        //Notification Channel id
        private const val NOTIFICATION_CHANNEL_ID = "android.sitbak.rider"

        //Notification Channel name
        private const val CHANNEL_NAME = "Background Service"

        private const val TAG = "Location Service"
    }

    private lateinit var mLocationManager: LocationManager

    //Firebase data base and geofire
    private lateinit var databaseReference: DatabaseReference
    private lateinit var geoFire: GeoFire

    private var orderId: Int = -1
    private var userId = -1
    private var driverId = -1
    private var storeId = -1
    private var storeLat = 0.0
    private var storeLng = 0.0
    private var orderUserLat = 0.0
    private var orderUserLng = 0.0
    private var orderStatus = ""

    private lateinit var socket: SocketIO

//    private var isDriverEnterStoreRegion = false
//    private var isDriverPickedOrder = false
//    private var isDriverExitedStoreRegion = false

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        Log.e(TAG, "onCreate")

        driverId = AppUtils.getUserDetails(this).id
        socket = SocketIO()
        initializeLocationManager()

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O)
            startMyOwnForeground()
        else
            startForeground(1, Notification())
    }

    private fun initializeLocationManager() {
        if (!::mLocationManager.isInitialized) {
            mLocationManager = this.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun startMyOwnForeground() {
        val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH)
        channel.lightColor = Color.GREEN
        channel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        val manager = (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
        manager.createNotificationChannel(channel)
        val notificationBuilder: NotificationCompat.Builder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
        val notification: Notification = notificationBuilder.setOngoing(true)
            .setContentTitle("App is running in background")
            .setSmallIcon(R.drawable.ic_stat_onesignal_default)
            .setPriority(NotificationManager.IMPORTANCE_MIN)
            .setCategory(Notification.CATEGORY_SERVICE)
            .build()
        startForeground(System.currentTimeMillis().toInt(), notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.e(TAG, "onStartCommand")
        super.onStartCommand(intent, flags, startId)

        if (intent != null) {
            orderId = intent.getIntExtra("orderId", -1)
            userId = intent.getIntExtra("userId", -1)
            storeId = intent.getIntExtra("storeId", -1)
            storeLat = intent.getDoubleExtra("storeLat", 0.0)
            storeLng = intent.getDoubleExtra("storeLng", 0.0)
            orderUserLat = intent.getDoubleExtra("orderUserLat", 0.0)
            orderUserLng = intent.getDoubleExtra("orderUserLng", 0.0)

            orderStatus = intent.getStringExtra("orderStatus")!!
        } else {
            stopSelf()
        }

        try {
            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE, this@LocationService)

            databaseReference = FirebaseDatabase.getInstance().getReference("Locations")
            geoFire = GeoFire(databaseReference)

        } catch (ex: SecurityException) {
            Log.i(TAG, "fail to request location update, ignore", ex)
        } catch (ex: IllegalArgumentException) {
            Log.d(TAG, "network provider does not exist, " + ex.message)
        }

        checkForGeoFencing()

        return START_STICKY
    }

    private fun checkForGeoFencing() {
        val storeGeoQuery = geoFire.queryAtLocation(GeoLocation(storeLat, storeLng), GEO_FENCE_RADIUS)
        storeGeoQuery.addGeoQueryEventListener(object : GeoQueryEventListener {
            override fun onKeyEntered(key: String?, location: GeoLocation?) {
                Log.e(TAG, "onKeyEntered:")

//                isDriverEnterStoreRegion = SharedPreference.getBoolean(applicationContext, Constants.isDriverEnterStoreRegion)

//                if (!isDriverEnterStoreRegion) {
                if (orderStatus == READY_FOR_PICKUP) {
                    val jsonObject = JSONObject()
                        .put("id", orderId)
                        .put("user_id", userId)
                        .put("store_id", storeId)
                        .put("status", DRIVER_AT_STORE)
                    socket.emit(SocketIO.socketOrderSendStatus, jsonObject)

                    val intent = Intent(ProcessingTicketsFragment.DriverFencingBroadCast)
                    intent.putExtra("status", DRIVER_AT_STORE)
                    intent.putExtra("orderId", orderId)
                    LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)

                    orderStatus = DRIVER_AT_STORE

//                    SharedPreference.saveBoolean(applicationContext, Constants.isDriverEnterStoreRegion, true)
                }
            }

            override fun onKeyExited(key: String?) {
                Log.e(TAG, "onKeyExited:")
//                isDriverPickedOrder = SharedPreference.getBoolean(applicationContext, Constants.isDriverPickedOrder)
//                isDriverExitedStoreRegion = SharedPreference.getBoolean(applicationContext, Constants.isDriverExitedStoreRegion)

//                if (isDriverPickedOrder && !isDriverExitedStoreRegion) {
                if (orderStatus == PICKED_BY_DRIVER) {
                    val jsonObject = JSONObject()
                        .put("id", orderId)
                        .put("user_id", userId)
                        .put("store_id", storeId)
                        .put("status", OrdersDeliveryStatus.DRIVER_ON_WAY)
                    socket.emit(SocketIO.socketOrderSendStatus, jsonObject)

//                    SharedPreference.saveBoolean(applicationContext, Constants.isDriverExitedStoreRegion, true)

                    orderStatus = DRIVER_ON_WAY

                }
            }

            override fun onKeyMoved(key: String?, location: GeoLocation?) {
                Log.e(TAG, "onKeyMoved:")
            }

            override fun onGeoQueryReady() {
                Log.e(TAG, "onGeoQueryReady:")
            }

            override fun onGeoQueryError(error: DatabaseError?) {
                Log.e(TAG, "onGeoQueryError: $error")
            }
        })


        val orderUserGeoQuery = geoFire.queryAtLocation(GeoLocation(orderUserLat, orderUserLng), GEO_FENCE_RADIUS)
        orderUserGeoQuery.addGeoQueryEventListener(object : GeoQueryEventListener {
            override fun onKeyEntered(key: String?, location: GeoLocation?) {
                Log.e(TAG, "onKeyEntered:")

//                isDriverExitedStoreRegion = SharedPreference.getBoolean(applicationContext, Constants.isDriverExitedStoreRegion)
//                isDriverPickedOrder = SharedPreference.getBoolean(applicationContext, Constants.isDriverPickedOrder)

//                if (isDriverExitedStoreRegion && isDriverPickedOrder) {
                if (orderStatus == DRIVER_ON_WAY) {
                    val jsonObject = JSONObject()
                        .put("id", orderId)
                        .put("user_id", userId)
                        .put("store_id", storeId)
                        .put("status", OrdersDeliveryStatus.DRIVER_AT_USER_PLACE)
                    socket.emit(SocketIO.socketOrderSendStatus, jsonObject)


                    val intent = Intent(ProcessingTicketsFragment.DriverFencingBroadCast)
                    intent.putExtra("status", OrdersDeliveryStatus.DRIVER_AT_USER_PLACE)
                    intent.putExtra("orderId", orderId)
                    LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)

                    orderStatus = DRIVER_AT_USER_PLACE

//                    SharedPreference.saveBoolean(applicationContext, Constants.isDriverPickedOrder, false)
//                    SharedPreference.saveBoolean(applicationContext, Constants.isDriverEnterStoreRegion, false)
//                    SharedPreference.saveBoolean(applicationContext, Constants.isDriverExitedStoreRegion, false)

                    stopSelf()
                }
            }

            override fun onKeyExited(key: String?) {
                Log.e(TAG, "onKeyExited:")
            }

            override fun onKeyMoved(key: String?, location: GeoLocation?) {
                Log.e(TAG, "onKeyMoved:")
            }

            override fun onGeoQueryReady() {
                Log.e(TAG, "onGeoQueryReady:")
            }

            override fun onGeoQueryError(error: DatabaseError?) {
                Log.e(TAG, "onGeoQueryError: $error")
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::mLocationManager.isInitialized) {
            mLocationManager.removeUpdates(this)
        }
    }

    override fun onLocationChanged(location: Location) {
//        Toast.makeText(this, "Location : ${location.latitude},${location.longitude}", Toast.LENGTH_SHORT).show()
        Log.e(TAG, "Location : ${location.latitude},${location.longitude}")

        //Sending location to firebase
        geoFire.setLocation("$driverId", GeoLocation(location.latitude, location.longitude))

        val j = JSONObject()
        j.put("id", driverId) /*Driver id*/
        j.put("user_id", userId) /*Customer user id*/
        j.put("order_id", orderId) /*Order id */
        j.put("latitude", location.latitude)
        j.put("longitude", location.longitude)
        socket.emit(SocketIO.socketSendLocation, j)
    }

    override fun onProviderDisabled(provider: String) {
        Log.e(TAG, "onProviderDisabled: $provider")
    }

    override fun onProviderEnabled(provider: String) {
        Log.e(TAG, "onProviderEnabled: $provider")
    }

    override fun onStatusChanged(provider: String, status: Int, extras: Bundle?) {
        Log.e(TAG, "onStatusChanged: $provider")
    }

}
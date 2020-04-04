package ee.taltech.likutt.iti0213_2019s_hw02

import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.Location
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.location.*
import java.util.*

class LocationService : Service() {
    companion object {
        private val TAG = this::class.java.declaringClass!!.simpleName
    }

    // The desired intervals for location updates. Inexact. Updates may be more or less frequent.
    private val UPDATE_INTERVAL_IN_MILLISECONDS: Long = 2000
    private val FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS / 2

    private val broadcastReceiver = InnerBroadcastReceiver()
    private val broadcastReceiverIntentFilter: IntentFilter = IntentFilter()

    private val mLocationRequest: LocationRequest = LocationRequest()
    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private var mLocationCallback: LocationCallback? = null

    // last received location
    private var currentLocation: Location? = null

    private var distanceOverallDirect = 0f
    private var distanceOverallTotal = 0f
    private var locationStart: Location? = null

    private var distanceCPDirect = 0f
    private var distanceCPTotal = 0f
    private var locationCP: Location? = null

    private var distanceWPDirect = 0f
    private var distanceWPTotal = 0f
    private var locationWP: Location? = null

    val timer = Timer()
    var curSpentTime: Int? = null
    var curWPSet = false
    var curWPStartTime: Date? = null
    var curCPSet = false
    var curCPStartTime: Date? = null

    val task = object: TimerTask() {
        var startTime = Date()
        override fun run() {
            curSpentTime = ((Date().time - startTime.time) * 0.001).toInt()
            showNotification()
        }
    }

    override fun onCreate() {
        Log.d(TAG, "onCreate")
        super.onCreate()

        broadcastReceiverIntentFilter.addAction(C.NOTIFICATION_ACTION_CP)
        broadcastReceiverIntentFilter.addAction(C.NOTIFICATION_ACTION_WP)
        broadcastReceiverIntentFilter.addAction(C.LOCATION_UPDATE_ACTION)


        registerReceiver(broadcastReceiver, broadcastReceiverIntentFilter)


        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        mLocationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                onNewLocation(locationResult.lastLocation)
            }
        }

        getLastLocation()

        createLocationRequest()
        requestLocationUpdates()

    }

    fun requestLocationUpdates() {
        Log.i(TAG, "Requesting location updates")

        try {
            mFusedLocationClient.requestLocationUpdates(
                mLocationRequest,
                mLocationCallback, Looper.myLooper()
            )
        } catch (unlikely: SecurityException) {
            Log.e(
                TAG,
                "Lost location permission. Could not request updates. $unlikely"
            )
        }
    }

    private fun onNewLocation(location: Location) {
        Log.i(TAG, "New location: $location")
        if (currentLocation == null){
            locationStart = location
            //locationCP = location
            //locationWP = location
        } else {
            distanceOverallDirect = location.distanceTo(locationStart)
            distanceOverallTotal += location.distanceTo(currentLocation)

            if (locationCP != null) {
                distanceCPDirect = location.distanceTo(locationCP)
                distanceCPTotal += location.distanceTo(currentLocation)
            }

            if (locationWP != null) {
                distanceWPDirect = location.distanceTo(locationWP)
                distanceWPTotal += location.distanceTo(currentLocation)
            }

        }
        // save the location for calculations
        currentLocation = location

        showNotification()

        // broadcast new location to UI
        val intent = Intent(C.LOCATION_UPDATE_ACTION)
        intent.putExtra(C.LOCATION_UPDATE_ACTION_LATITUDE, location.latitude)
        intent.putExtra(C.LOCATION_UPDATE_ACTION_LONGITUDE, location.longitude)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)

    }

    private fun createLocationRequest() {
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS)
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS)
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
        mLocationRequest.setMaxWaitTime(UPDATE_INTERVAL_IN_MILLISECONDS)
    }

    private fun getLastLocation() {
        try {
            mFusedLocationClient.lastLocation
                .addOnCompleteListener { task -> if (task.isSuccessful) {
                    Log.w(TAG, "task successfull");
                    if (task.result != null){
                        onNewLocation(task.result!!)
                    }
                } else {

                    Log.w(TAG, "Failed to get location." + task.exception)
                }}
        } catch (unlikely: SecurityException) {
            Log.e(TAG, "Lost location permission.$unlikely")
        }
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy")
        super.onDestroy()

        //stop location updates
        mFusedLocationClient.removeLocationUpdates(mLocationCallback)

        // remove notifications
        NotificationManagerCompat.from(this).cancelAll()


        // don't forget to unregister brodcast receiver!!!!
        unregisterReceiver(broadcastReceiver)


        // broadcast stop to UI
        val intent = Intent(C.LOCATION_UPDATE_ACTION)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)

        timer.cancel()
        timer.purge()
        curSpentTime = null
        curCPStartTime = null
        curWPStartTime = null
        curCPSet = false
        curWPSet = false
    }

    override fun onLowMemory() {
        Log.d(TAG, "onLowMemory")
        super.onLowMemory()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand")

        // set counters and locations to 0/null
        currentLocation = null
        locationStart = null
        locationCP = null
        locationWP = null

        distanceOverallDirect = 0f
        distanceOverallTotal = 0f
        distanceCPDirect = 0f
        distanceCPTotal = 0f
        distanceWPDirect = 0f
        distanceWPTotal = 0f

        timer.schedule(task, 0, 1000)

        showNotification()

        return START_STICKY
        //return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent?): IBinder? {
        Log.d(TAG, "onBind")
        TODO("not implemented")
    }

    override fun onRebind(intent: Intent?) {
        Log.d(TAG, "onRebind")
        super.onRebind(intent)
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.d(TAG, "onUnbind")
        return super.onUnbind(intent)

    }

    fun showNotification(){
        val intentCp = Intent(C.NOTIFICATION_ACTION_CP)
        val intentWp = Intent(C.NOTIFICATION_ACTION_WP)

        val pendingIntentCp = PendingIntent.getBroadcast(this, 0, intentCp, 0)
        val pendingIntentWp = PendingIntent.getBroadcast(this, 0, intentWp, 0)

        val notifyview = RemoteViews(packageName,
            R.layout.track_control
        )

        notifyview.setOnClickPendingIntent(R.id.imageButtonCP, pendingIntentCp)
        notifyview.setOnClickPendingIntent(R.id.imageButtonWP, pendingIntentWp)

        notifyview.setTextViewText(R.id.textViewOverallTotal, "%.2f".format(distanceOverallTotal))

        val intent = Intent(C.LOCATION_UPDATE_ACTION)

        intent.putExtra(C.STATISTICS_UPDATE_OVERALL_TOTAL, "%.2f".format(distanceOverallTotal))
        if (curSpentTime != null) {
            val duration = findTimeStringFromSeconds(curSpentTime!!.toDouble(), true)
            val tempo = findTimeStringFromSeconds(calcMinutesPerKm(curSpentTime!!, distanceOverallTotal), false)

            notifyview.setTextViewText(R.id.textViewOverallDuration, duration)
            notifyview.setTextViewText(R.id.textViewOverallTempo, tempo)

            intent.putExtra(C.STATISTICS_UPDATE_OVERALL_DURATION, duration)
            intent.putExtra(C.STATISTICS_UPDATE_OVERALL_TEMPO, tempo)
        }

        if (curWPSet && curWPStartTime != null) {
            val time : Int = ((Date().time - curWPStartTime!!.time)*0.001).toInt()
            val wpTempoString = findTimeStringFromSeconds(calcMinutesPerKm(time, distanceWPTotal), false)

            notifyview.setTextViewText(R.id.textViewWPDirect, "%.2f".format(distanceWPDirect))
            notifyview.setTextViewText(R.id.textViewWPTotal, "%.2f".format(distanceWPTotal))
            notifyview.setTextViewText(R.id.textViewWPTempo, wpTempoString)

            intent.putExtra(C.STATISTICS_UPDATE_WP_DIRECT, "%.2f".format(distanceWPDirect))
            intent.putExtra(C.STATISTICS_UPDATE_WP_TOTAL, "%.2f".format(distanceWPTotal))
            intent.putExtra(C.STATISTICS_UPDATE_WP_TEMPO, wpTempoString)
        }

        if (curCPSet && curCPStartTime != null) {
            val time : Int = ((Date().time - curCPStartTime!!.time)*0.001).toInt()
            val cpTempoString = findTimeStringFromSeconds(calcMinutesPerKm(time, distanceCPTotal), false)

            notifyview.setTextViewText(R.id.textViewCPTotal, "%.2f".format(distanceCPTotal))
            notifyview.setTextViewText(R.id.textViewCPDirect, "%.2f".format(distanceCPDirect))
            notifyview.setTextViewText(R.id.textViewCPTempo, cpTempoString)

            intent.putExtra(C.STATISTICS_UPDATE_CP_TOTAL, "%.2f".format(distanceCPTotal))
            intent.putExtra(C.STATISTICS_UPDATE_CP_DIRECT, "%.2f".format(distanceCPDirect))
            intent.putExtra(C.STATISTICS_UPDATE_CP_TEMPO, cpTempoString)

        }

        /*
        intent.putExtra(C.RESTORE_CPS, checkpoints)
        */
        if (locationWP != null) {
            //intent.putExtra(C.RESTORE_WP_LATITUDE, locationWP!!.latitude)
            //intent.putExtra(C.RESTORE_WP_LONGITUDE, locationWP!!.longitude)
            sendWPdata()
        }


        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)

        // construct and show notification
        var builder = NotificationCompat.Builder(applicationContext,
            C.NOTIFICATION_CHANNEL
        )
            .setSmallIcon(R.drawable.baseline_gps_fixed_24)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        builder.setContent(notifyview)

        // Super important, start as foreground service - ie android considers this as an active app. Need visual reminder - notification.
        // must be called within 5 secs after service starts.
        startForeground(C.NOTIFICATION_ID, builder.build())

    }

    private fun calcMinutesPerKm(seconds: Int, distance: Float) : Double {
        return (distance / seconds.toFloat()) * 60 * 16.6666667
    }

    private fun sendWPdata() {
        val intent = Intent(C.LOCATION_UPDATE_ACTION)
        intent.putExtra(C.CURRENT_WP_LATITUDE, locationWP!!.latitude)
        intent.putExtra(C.CURRENT_WP_LONGITUDE, locationWP!!.longitude)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    private fun sendCPdata() {
        val intent = Intent(C.LOCATION_UPDATE_ACTION)
        intent.putExtra(C.NEW_CP_LATITUDE, locationCP!!.latitude)
        intent.putExtra(C.NEW_CP_LONGITUDE, locationCP!!.longitude)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    private var checkpoints = arrayListOf<Location>()

    private inner class InnerBroadcastReceiver: BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.d(TAG, intent!!.action)
            when(intent!!.action){
                C.NOTIFICATION_ACTION_WP -> {
                    curWPSet = true
                    curWPStartTime = Date()
                    locationWP = currentLocation
                    distanceWPDirect = 0f
                    distanceWPTotal = 0f

                    sendWPdata()
                    showNotification()
                }
                C.NOTIFICATION_ACTION_CP -> {
                    curCPSet = true
                    curCPStartTime = Date()
                    locationCP = currentLocation
                    distanceCPDirect = 0f
                    distanceCPTotal = 0f

                    checkpoints.add(locationCP!!)
                    sendCPdata()
                    showNotification()
                }
            }
        }

    }

    private fun findTimeStringFromSeconds(seconds: Double?, showHours: Boolean) : String{
        if (seconds!=null) {
            var secondsTemp = seconds
            val hours = (secondsTemp / 3600).toInt()
            secondsTemp %= 3600
            val minutes = (secondsTemp / 60).toInt()
            secondsTemp %= 60
            val secondsReturn = (secondsTemp).toInt()
            if (hours != 0 || showHours) {
                return "$hours:$minutes:$secondsReturn"
            }
            return "$minutes:$secondsReturn"
        }
        return ""
    }

}
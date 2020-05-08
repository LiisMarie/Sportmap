package ee.taltech.likutt.iti0213_2019s_hw02

import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.google.android.gms.location.*
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class LocationService : Service() {
    companion object {
        private val TAG = this::class.java.declaringClass!!.simpleName
    }

    // The desired intervals for location updates. Inexact. Updates may be more or less frequent.
    private var UPDATE_INTERVAL_IN_MILLISECONDS: Long = 2000
    private var FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS / 2

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

    private val timer = Timer()
    var curSpentTime: Long = 0L
    var curWPStartTime: Date? = null
    var curCPStartTime: Date? = null

    private var checkpoints: ArrayList<Location> = arrayListOf()

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.getDefault())

    private var minSpeed: Double = (6*60).toDouble()  // 1 km within 6 min  -- minspeed=6*60 km / per sec
    private var maxSpeed: Double = (18*60).toDouble()

    private var prevLocation: Location? = null
    private var prevTime: Long? = null

    // for backend
    private var mJwt: String? = null
    private var trackingSessionId: String? = null

    // for local database
    private lateinit var repo: Repository
    private var localTrackingSessionId: Long? = null

    val task = object: TimerTask() {
        var startTime = Date()
        override fun run() {
            curSpentTime = Date().time - startTime.time
            showNotification()
        }
    }

    override fun onCreate() {
        Log.d(TAG, "onCreate")
        super.onCreate()

        repo = Repository(this).open()

        val settings = repo.getSettings()

        if (settings != null) {
            UPDATE_INTERVAL_IN_MILLISECONDS = settings.gpsUpdateFrequency
            FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS / 2
        }

        broadcastReceiverIntentFilter.addAction(C.NOTIFICATION_ACTION_CP)
        broadcastReceiverIntentFilter.addAction(C.NOTIFICATION_ACTION_WP)
        broadcastReceiverIntentFilter.addAction(C.LOCATION_UPDATE_ACTION)
        broadcastReceiverIntentFilter.addAction(C.UPDATE_SETTINGS)

        registerReceiver(broadcastReceiver, broadcastReceiverIntentFilter)

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        mLocationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                onNewLocation(locationResult.lastLocation)
            }
        }

        getRestToken()

        /*
        // todo syncing
        if (mJwt != null && trackingSessionId != null) {
            startLocalTrackingSession(C.SYNCING_SYNCED)
        } else {
            startLocalTrackingSession(C.SYNCING_NO_NEED_TO_SYNC)
        }*/
        //startLocalTrackingSession(C.SYNCING_SYNCED)

        getLastLocation()

        createLocationRequest()
        requestLocationUpdates()
    }

    private fun requestLocationUpdates() {
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
            saveLocalLocation(location, C.LOCAL_LOCATION_TYPE_START, 0.toDouble(), C.SYNCING_SYNCED)  // todo synced
            prevLocation = location
            prevTime = Date().time
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

        // broadcast new location to UI
        val intent = Intent(C.LOCATION_UPDATE_ACTION)
        intent.putExtra(C.LOCATION_UPDATE_ACTION_LATITUDE, location.latitude)
        intent.putExtra(C.LOCATION_UPDATE_ACTION_LONGITUDE, location.longitude)

        if (prevLocation != null && prevTime != null) {
            val speed = Helpers.getSpeed(Date().time-prevTime!!, location.distanceTo(prevLocation))
            speedFromPrevLoc = speed
            //saveLocalLocation(location, C.LOCAL_LOCATION_TYPE_LOC, speed, C.SYNCING_SYNCED)  // todo is synced or not
            prevLocation = location
            prevTime = Date().time

            intent.putExtra(C.LOCATION_UPDATE_ACTION_SPEED, speedFromPrevLoc)
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)

        // save the location for calculations
        currentLocation = location

        showNotification()

        saveRestLocation(location, C.REST_LOCATIONID_LOC)

    }

    var speedFromPrevLoc = 0.toDouble()

    private fun createLocationRequest() {
        mLocationRequest.interval = UPDATE_INTERVAL_IN_MILLISECONDS    // setting interval
        mLocationRequest.fastestInterval = FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.maxWaitTime = UPDATE_INTERVAL_IN_MILLISECONDS
    }

    private fun getLastLocation() {
        try {
            mFusedLocationClient.lastLocation
                .addOnCompleteListener { task -> if (task.isSuccessful) {
                    Log.w(TAG, "task successfull")
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

        // update data of session in local database
        //    fun updateSessionDurationSpeedDistance(id: String, duration: Long, speed: String, distance: Float) {
        repo.updateSessionDurationSpeedDistance(localTrackingSessionId!!, curSpentTime, Helpers.getPaceAsString(curSpentTime, distanceOverallTotal), distanceOverallTotal)

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
        curSpentTime = 0L
        curCPStartTime = null
        curWPStartTime = null
        checkpoints = arrayListOf()
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

        checkpoints = arrayListOf()

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

        val intent = Intent(C.STATISTICS_UPDATE_ACTION)

        // handle overall changes
        val duration = Helpers.getTimeString(curSpentTime)
        val tempo = Helpers.getPaceAsString(curSpentTime, distanceOverallTotal)

        notifyview.setTextViewText(R.id.textViewOverallTotal, "%.2f".format(distanceOverallTotal))
        notifyview.setTextViewText(R.id.textViewOverallDuration, duration)
        notifyview.setTextViewText(R.id.textViewOverallTempo, tempo)

        intent.putExtra(C.STATISTICS_UPDATE_OVERALL_TOTAL, "%.2f".format(distanceOverallTotal))
        intent.putExtra(C.STATISTICS_UPDATE_OVERALL_DURATION, duration)
        intent.putExtra(C.STATISTICS_UPDATE_OVERALL_TEMPO, tempo)

        // handle wp changes
        if (locationWP != null && curWPStartTime != null) {
            val time : Long = Date().time - curWPStartTime!!.time
            val wpTempoString = Helpers.getPaceAsString(time, distanceWPTotal)

            notifyview.setTextViewText(R.id.textViewWPDirect, "%.2f".format(distanceWPDirect))
            notifyview.setTextViewText(R.id.textViewWPTotal, "%.2f".format(distanceWPTotal))
            notifyview.setTextViewText(R.id.textViewWPTempo, wpTempoString)

            intent.putExtra(C.STATISTICS_UPDATE_WP_DIRECT, "%.2f".format(distanceWPDirect))
            intent.putExtra(C.STATISTICS_UPDATE_WP_TOTAL, "%.2f".format(distanceWPTotal))
            intent.putExtra(C.STATISTICS_UPDATE_WP_TEMPO, wpTempoString)
        }

        // handle cp changes
        if (locationCP != null && curCPStartTime != null) {
            val time : Long = Date().time - curCPStartTime!!.time
            val cpTempoString = Helpers.getPaceAsString(time, distanceCPTotal)

            notifyview.setTextViewText(R.id.textViewCPTotal, "%.2f".format(distanceCPTotal))
            notifyview.setTextViewText(R.id.textViewCPDirect, "%.2f".format(distanceCPDirect))
            notifyview.setTextViewText(R.id.textViewCPTempo, cpTempoString)

            intent.putExtra(C.STATISTICS_UPDATE_CP_TOTAL, "%.2f".format(distanceCPTotal))
            intent.putExtra(C.STATISTICS_UPDATE_CP_DIRECT, "%.2f".format(distanceCPDirect))
            intent.putExtra(C.STATISTICS_UPDATE_CP_TEMPO, cpTempoString)
        }

        if (locationWP != null) {
            intent.putExtra(C.CURRENT_WP_LATITUDE, locationWP!!.latitude)
            intent.putExtra(C.CURRENT_WP_LONGITUDE, locationWP!!.longitude)
        }

        intent.putExtra(C.CURRENT_SESSION_ID, localTrackingSessionId)

        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)

        // construct and show notification
        val builder = NotificationCompat.Builder(applicationContext,
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

    private fun sendWPdata() {
        val intent = Intent(C.STATISTICS_UPDATE_ACTION)
        intent.putExtra(C.CURRENT_WP_LATITUDE, locationWP!!.latitude)
        intent.putExtra(C.CURRENT_WP_LONGITUDE, locationWP!!.longitude)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    private fun sendCPdata() {
        val intent = Intent(C.STATISTICS_UPDATE_ACTION)
        intent.putExtra(C.NEW_CP_LATITUDE, locationCP!!.latitude)
        intent.putExtra(C.NEW_CP_LONGITUDE, locationCP!!.longitude)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    private inner class InnerBroadcastReceiver: BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.d(TAG, intent!!.action)
            Log.d(TAG, "gpsUpdateFrequency intent")

            when(intent.action){
                C.NOTIFICATION_ACTION_WP -> {
                    curWPStartTime = Date()
                    locationWP = currentLocation
                    distanceWPDirect = 0f
                    distanceWPTotal = 0f
                    saveRestLocation(locationWP!!, C.REST_LOCATIONID_WP)
                    sendWPdata()
                    showNotification()
                }
                C.NOTIFICATION_ACTION_CP -> {
                    curCPStartTime = Date()
                    locationCP = currentLocation
                    distanceCPDirect = 0f
                    distanceCPTotal = 0f

                    checkpoints.add(locationCP!!)
                    saveRestLocation(locationCP!!, C.REST_LOCATIONID_CP)
                    //saveLocalLocation(locationCP!!, C.LOCAL_LOCATION_TYPE_CP, null, syncedValue)  // todo syncing
                    sendCPdata()
                    showNotification()
                }
                C.UPDATE_SETTINGS -> {
                    // todo logic
                    // C.GPS_UPDATE_FRQUENCY
                    // C.SYNCING_INTERVAL
                    val gpsUpdateFrequency = intent.getLongExtra(C.GPS_UPDATE_FRQUENCY, 2000)
                    /*
                    mLocationRequest.interval = UPDATE_INTERVAL_IN_MILLISECONDS    // setting interval
                    mLocationRequest.fastestInterval = FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS
                    mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                    mLocationRequest.maxWaitTime = UPDATE_INTERVAL_IN_MILLISECONDS
                     */
                    mLocationRequest.setInterval(gpsUpdateFrequency)
                    mLocationRequest.setMaxWaitTime(gpsUpdateFrequency)
                    mLocationRequest.setFastestInterval(gpsUpdateFrequency / 2)

                    mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper())

                            //LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);

                    Log.d(TAG, "gpsUpdateFrequency " + gpsUpdateFrequency)
                }

            }
        }
    }

    // LOCAL DATABASE ACTIONS

    private fun startLocalTrackingSession(synced: Int, date: Date) {
        Log.d(TAG, "startLocalTrackingSession")
        localTrackingSessionId = repo.addSession(
                date.toString(),
                date.toString(),
                dateFormat.format(date),
                0,
                0.toString(),
                0f,
                minSpeed,
                maxSpeed,
                synced
        )
    }

    private fun saveLocalLocation(location: Location, location_type: String, speed: Double?, synced: Int) {
        if (localTrackingSessionId != null) {
            repo.addLocation(
                    location.latitude,
                    location.longitude,
                    localTrackingSessionId!!,
                    location_type,
                    speed,
                    dateFormat.format(Date(location.time)),
                    synced
            )
        }

    }

    // BACKEND DATABASE ACTIONS

    private fun getRestToken() {

        Log.d(TAG, "getRestToken")
        val handler = WebApiSingletonHandler.getInstance(applicationContext)

        val user = repo.getUser()
        if (user == null) {
            startLocalTrackingSession(C.SYNCING_NO_NEED_TO_SYNC, Date())
            return
        }
        // if no user has been saved then return

        val requestJsonParameters = JSONObject()
        requestJsonParameters.put("email", user.email)
        requestJsonParameters.put("password", user.password)

        val date = Date()

        val httpRequest = JsonObjectRequest(
            Request.Method.POST,
            C.REST_BASE_URL + "account/login",
            requestJsonParameters,
            Response.Listener { response ->
                Log.d(TAG, response.toString())
                Log.d(TAG, "DATABASE RESPONSE: " + response.toString())
                mJwt = response.getString("token")
                startRestTrackingSession(date)
            },
            Response.ErrorListener { error ->
                Log.d(TAG, "ERROR: " + error.toString())

                startLocalTrackingSession(C.SYNCING_NO_NEED_TO_SYNC, date)
            }
        )

        handler.addToRequestQueue(httpRequest)
    }

    private fun startRestTrackingSession(date: Date) {
        Log.d(TAG, "startRestTrackingSession")
        val handler = WebApiSingletonHandler.getInstance(applicationContext)
        val requestJsonParameters = JSONObject()
        requestJsonParameters.put("name", date.toString())
        requestJsonParameters.put("description", date.toString())
        requestJsonParameters.put("paceMin", minSpeed)
        requestJsonParameters.put("paceMax", maxSpeed)

        requestJsonParameters.put("recordedAt", dateFormat.format(date))

        val httpRequest = object : JsonObjectRequest(
            Method.POST,
            C.REST_BASE_URL + "GpsSessions",
            requestJsonParameters,
            Response.Listener { response ->
                Log.d(TAG, response.toString())
                Log.d(TAG, "DATABASE RESPONSE: " + response.toString())
                trackingSessionId = response.getString("id")

                startLocalTrackingSession(C.SYNCING_SYNCED, date)
            },
            Response.ErrorListener { error ->
                Log.d(TAG, "ERROR: " + error.toString())

                startLocalTrackingSession(C.SYNCING_NOT_SYNCED, date)
            }
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                for ((key, value) in super.getHeaders()) {
                    headers[key] = value
                }
                headers["Authorization"] = "Bearer " + mJwt!!
                return headers
            }
        }
        handler.addToRequestQueue(httpRequest)

    }

    private fun saveRestLocation(location: Location, location_type: String) {
        Log.d(TAG, "saveRestLocation")

        var speed = 0.toDouble()
        if (prevLocation != null && prevTime != null) {
            speed = Helpers.getSpeed(location.time-prevTime!!, location.distanceTo(prevLocation))
        }

        Log.d(TAG, "speedFromPrevLoc " + speedFromPrevLoc)

        if (mJwt == null || trackingSessionId == null) {
            saveLocalLocation(location, location_type, speedFromPrevLoc, C.SYNCING_NOT_SYNCED)
            return
        }

        val handler = WebApiSingletonHandler.getInstance(applicationContext)
        val requestJsonParameters = JSONObject()

        requestJsonParameters.put("recordedAt", dateFormat.format(Date(location.time)))

        requestJsonParameters.put("latitude", location.latitude)
        requestJsonParameters.put("longitude", location.longitude)
        requestJsonParameters.put("accuracy", location.accuracy)
        requestJsonParameters.put("altitude", location.altitude)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            requestJsonParameters.put("verticalAccuracy", location.verticalAccuracyMeters)
        }
        requestJsonParameters.put("gpsSessionId", trackingSessionId)
        requestJsonParameters.put("gpsLocationTypeId", location_type)

        val httpRequest = object : JsonObjectRequest(
            Method.POST,
            C.REST_BASE_URL + "GpsLocations",
            requestJsonParameters,
            Response.Listener { response ->
                Log.d(TAG, response.toString())
                Log.d(TAG, "DATABASE RESPONSE: " + response.toString())

                saveLocalLocation(location, location_type, speedFromPrevLoc, C.SYNCING_SYNCED)
            },
            Response.ErrorListener { error ->
                Log.d(TAG, "ERROR: " + error.toString())

                saveLocalLocation(location, location_type, speedFromPrevLoc, C.SYNCING_NOT_SYNCED)
            }
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                for ((key, value) in super.getHeaders()) {
                    headers[key] = value
                }
                headers["Authorization"] = "Bearer " + mJwt!!
                return headers
            }
        }
        handler.addToRequestQueue(httpRequest)
    }

}
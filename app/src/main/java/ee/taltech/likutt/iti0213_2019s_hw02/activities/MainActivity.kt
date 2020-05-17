package ee.taltech.likutt.iti0213_2019s_hw02.activities

// do not import this! never! If this get inserted automatically when pasting java code, remove it
//import android.R

import android.Manifest
import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.*
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.hardware.Sensor
import android.hardware.Sensor.TYPE_ACCELEROMETER
import android.hardware.Sensor.TYPE_MAGNETIC_FIELD
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.hardware.SensorManager.SENSOR_DELAY_GAME
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.animation.Animation.RELATIVE_TO_SELF
import android.view.animation.RotateAnimation
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.snackbar.Snackbar
import ee.taltech.likutt.iti0213_2019s_hw02.*
import ee.taltech.likutt.iti0213_2019s_hw02.database.Repository
import ee.taltech.likutt.iti0213_2019s_hw02.helpers.C
import ee.taltech.likutt.iti0213_2019s_hw02.helpers.Helpers
import ee.taltech.likutt.iti0213_2019s_hw02.helpers.LocationService
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.track_control.*
import java.lang.Math.toDegrees


class MainActivity : AppCompatActivity(), OnMapReadyCallback, SensorEventListener {
    companion object {
        private val TAG = this::class.java.declaringClass!!.simpleName
    }

    private lateinit var mMap: GoogleMap

    private lateinit var repo: Repository

    private val broadcastReceiver = InnerBroadcastReceiver()
    private val broadcastReceiverIntentFilter: IntentFilter = IntentFilter()

    private var locationServiceActive = false

    private var mapCentered = true
    private var compassSet = true
    private var trackingSet = false
    private var mapDirection = "North-up"

    private var curWP : LatLng? = null
    private var wpMarker : Marker? = null

    private var mapUpdated = false

    // for track segment coloring
    private var prevLatLng : LatLng? = null
    private var minSpeed: Double? = null
    private var maxSpeed: Double? = null
    private var colorMap: Map<List<Double>, Int>? = null

    // for compass  https://github.com/andreas-mausch/compass
    private lateinit var sensorManager: SensorManager
    private lateinit var image: ImageView
    private lateinit var accelerometer: Sensor
    private lateinit var magnetometer: Sensor
    private var currentDegree = 0.0f
    private var lastAccelerometer = FloatArray(3)
    private var lastMagnetometer = FloatArray(3)
    private var lastAccelerometerSet = false
    private var lastMagnetometerSet = false


    // ============================================== MAIN ENTRY - ONCREATE =============================================
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate")
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        // safe to call every time
        createNotificationChannel()

        if (!checkPermissions()) {
            requestPermissions()
        }

        broadcastReceiverIntentFilter.addAction(C.LOCATION_UPDATE_ACTION)
        broadcastReceiverIntentFilter.addAction(C.STATISTICS_UPDATE_ACTION)


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // set onclicklisteners for add cp and wp buttons
        imageButtonCP.setOnClickListener {buttonCPOnClick()}
        imageButtonWP.setOnClickListener {buttonWPOnClick()}

        buttonStartStop.setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.colorStartButton))

        mapUpdated = true

        // compass
        image = findViewById(R.id.imageViewCompass)
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(TYPE_ACCELEROMETER)
        magnetometer = sensorManager.getDefaultSensor(TYPE_MAGNETIC_FIELD)

        // take currently used settings from database
        // if there are no settings yet then initialize them
        repo = Repository(this).open()
        var settings = repo.getSettings()
        if (settings == null) {
            repo.addSettings((6*60).toDouble(), (18*60).toDouble(), 2000, 0)
        }
        settings = repo.getSettings()
        minSpeed = settings!!.minSpeed
        maxSpeed = settings.maxSpeed
        if (minSpeed != null && maxSpeed != null) {
            colorMap = Helpers.generateColorsForSpeeds(minSpeed!!, maxSpeed!!)
        }

        setOnClickListeners()
    }

    // if map is ready
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.isMyLocationEnabled = true

        val currentLatLng = LatLng(59.4367, 24.7533)  // todo set another starting point
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 17f))  // zooms in on given loc
    }

    // ============================================== LIFECYCLE CALLBACKS =============================================
    override fun onStart() {
        Log.d(TAG, "onStart")
        super.onStart()
    }

    override fun onResume() {
        Log.d(TAG, "onResume")
        super.onResume()

        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, broadcastReceiverIntentFilter)

        // compass
        if (compassSet) {
            sensorManager.registerListener(this, accelerometer, SENSOR_DELAY_GAME)
            sensorManager.registerListener(this, magnetometer, SENSOR_DELAY_GAME)
        }

        prevLatLng = null
        mapUpdated = false

        val settings = repo.getSettings()
        if (settings != null) {
            minSpeed = settings.minSpeed
            maxSpeed = settings.maxSpeed
            colorMap = Helpers.generateColorsForSpeeds(minSpeed!!, maxSpeed!!)
        }
    }

    override fun onPause() {
        Log.d(TAG, "onPause")
        super.onPause()

        // compass
        if (compassSet) {
            sensorManager.unregisterListener(this, accelerometer)
            sensorManager.unregisterListener(this, magnetometer)
        }

        prevLatLng = null
        mapUpdated = false
    }

    override fun onStop() {
        Log.d(TAG, "onStop")
        super.onStop()

        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver)
        prevLatLng = null
        mapUpdated = false
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy")
        super.onDestroy()
        prevLatLng = null
        mapUpdated = false
    }

    override fun onRestart() {
        Log.d(TAG, "onRestart")
        super.onRestart()

        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, broadcastReceiverIntentFilter)
        prevLatLng = null
        mapUpdated = false
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        Log.d(TAG, "lifecycle onSaveInstanceState")

        // save current ui state
        outState.putBoolean(C.RESTORE_COMPASS_SET, compassSet)
        outState.putBoolean(C.RESTORE_MAP_CENTERED_SET, mapCentered)
        outState.putBoolean(C.RESTORE_TRACKING_SET, trackingSet)
        outState.putString(C.RESTORE_MAP_DIRECTION, mapDirection)
        outState.putBoolean(C.RESTORE_LOCATION_SERVICE_ACTIVE, locationServiceActive)

    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        Log.d(TAG, "lifecycle onRestoreInstanceState")

        // recover ui state
        compassSet = savedInstanceState.getBoolean(C.RESTORE_COMPASS_SET, true)
        mapCentered = savedInstanceState.getBoolean(C.RESTORE_MAP_CENTERED_SET, true)
        mapDirection = savedInstanceState.getString(C.RESTORE_MAP_DIRECTION, getString(R.string.activity_main_button_direction_text_north_up))
        locationServiceActive = savedInstanceState.getBoolean(C.RESTORE_LOCATION_SERVICE_ACTIVE, false)

        mapUpdated = false

        restoreUI()
    }

    // ============================================== HELPERS =============================================

    // for initializing tracking
    private fun startTracking() {
        resetUI()
        prevLatLng = null

        if (Build.VERSION.SDK_INT >= 26) {
            // starting the FOREGROUND service
            // service has to display non-dismissable notification within 5 secs
            startForegroundService(Intent(this, LocationService::class.java))
        } else {
            startService(Intent(this, LocationService::class.java))
        }
        buttonStartStop.text = getString(R.string.activity_main_button_startstop_text_stop)
        buttonStartStop.setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.colorStopButton))

        trackingSet = true
        locationServiceActive = !locationServiceActive
    }

    // for stopping tracking
    private fun stopTracking() {
        // stopping the service
        stopService(Intent(this, LocationService::class.java))

        buttonStartStop.text = getString(R.string.activity_main_button_startstop_text_start)
        buttonStartStop.setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.colorStartButton))

        trackingSet = false
        locationServiceActive = !locationServiceActive
    }

    private fun makeCompassVisible() {
        compassSet = true
        imageButtonBack.setImageResource(R.drawable.baseline_explore_white_24)
        sensorManager.registerListener(this, accelerometer, SENSOR_DELAY_GAME)
        sensorManager.registerListener(this, magnetometer, SENSOR_DELAY_GAME)
        includeCompass.visibility = View.VISIBLE
    }

    private fun makeCompassInvisible() {
        compassSet = false
        imageButtonBack.setImageResource(R.drawable.baseline_explore_off_white_24)
        sensorManager.unregisterListener(this, accelerometer)
        sensorManager.unregisterListener(this, magnetometer)
        includeCompass.visibility = View.INVISIBLE
    }

    private fun makeMapCentered() {
        buttonCentered.text = getString(R.string.activity_main_button_centered_text_centered)
        mapCentered = true
    }

    private fun makeMapNotCentered() {
        buttonCentered.text = getString(R.string.activity_main_button_centered_text_not_centered)
        mapCentered = false
    }

    // adds waypoint on the map, if there is then removes the previous one
    private fun handleNewWaypoint(wpLatLng: LatLng) {
        curWP = wpLatLng
        if (wpMarker != null) {
            wpMarker!!.remove()
            wpMarker = null
        }
        drawWaypoint(wpLatLng)
        //Toast.makeText(this@MainActivity, "Waypoint updated", Toast.LENGTH_SHORT).show()
    }

    // ============================================== NOTIFICATION CHANNEL CREATION =============================================
    private fun createNotificationChannel() {
        // when on 8 Oreo or higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                    C.NOTIFICATION_CHANNEL,
                "Default channel",
                NotificationManager.IMPORTANCE_LOW
            )
            //.setShowBadge(false).setSound(null, null);
            channel.description = "Default channel"

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }


    // ============================================== PERMISSION HANDLING =============================================
    // Returns the current state of the permissions needed.
    private fun checkPermissions(): Boolean {
        return PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    private fun requestPermissions() {
        val shouldProvideRationale =
            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            Log.i(
                    TAG,
                "Displaying permission rationale to provide additional context."
            )
            Snackbar.make(
                findViewById(R.id.activity_main),
                "Hey, i really need to access GPS!",
                Snackbar.LENGTH_INDEFINITE
            )
                .setAction("OK") {
                    // Request permission
                    ActivityCompat.requestPermissions(
                            this,
                            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                            C.REQUEST_PERMISSIONS_REQUEST_CODE
                    )
                }
                    .show()
        } else {
            Log.i(TAG, "Requesting permission")
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    C.REQUEST_PERMISSIONS_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        Log.i(TAG, "onRequestPermissionResult")
        if (requestCode === C.REQUEST_PERMISSIONS_REQUEST_CODE) {
            when {
                grantResults.count() <= 0 -> { // If user interaction was interrupted, the permission request is cancelled and you
                    // receive empty arrays.
                    Log.i(TAG, "User interaction was cancelled.")
                    Toast.makeText(this, "User interaction was cancelled.", Toast.LENGTH_SHORT).show()
                }
                grantResults[0] === PackageManager.PERMISSION_GRANTED -> {// Permission was granted.
                    Log.i(TAG, "Permission was granted")
                    Toast.makeText(this, "Permission was granted", Toast.LENGTH_SHORT).show()
                }
                else -> { // Permission denied.
                    Snackbar.make(
                            findViewById(R.id.activity_main),
                            "You denied GPS! What can I do?",
                            Snackbar.LENGTH_INDEFINITE
                    )
                            .setAction("Settings") {
                                // Build intent that displays the App settings screen.
                                val intent = Intent()
                                intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                                val uri: Uri = Uri.fromParts(
                                        "package",
                                        BuildConfig.APPLICATION_ID, null
                                )
                                intent.data = uri
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                startActivity(intent)
                            }
                            .show()
                }
            }
        }

    }



    // ============================================== CLICK HANDLERS =============================================

    private fun setOnClickListeners() {
        buttonStartStop.setOnClickListener {
            buttonStartStopOnClick()
        }
        buttonCentered.setOnClickListener {
            buttonCenteredOnClick()
        }
        buttonDirection.setOnClickListener {
            buttonDirectionOnClick()
        }
        imageButtonBack.setOnClickListener {
            buttonCompassOnClick()
        }
        buttonMenu.setOnClickListener {
            buttonMenuOnClick()
        }
        imageButtonSettings.setOnClickListener {
            buttonSettingsOnClick()
        }
    }

    private fun buttonStartStopOnClick() {
        Log.d(TAG, "buttonStartStopOnClick. locationServiceActive: $locationServiceActive")
        // try to start/stop the background service

        if (locationServiceActive) {
            AlertDialog.Builder(this)
                .setTitle("Warning")
                .setMessage("Do you want to stop tracking?")
                .setIcon(R.drawable.twotone_warning_24)
                .setPositiveButton("YES") { _, _ -> stopTracking() }
                    .setNegativeButton("NO", null).show()

        } else {
            startTracking()
        }

    }

    private fun buttonWPOnClick() {
        Log.d(TAG, "buttonWPOnClick")
        sendBroadcast(Intent(C.NOTIFICATION_ACTION_WP))
    }

    private fun buttonCPOnClick() {
        Log.d(TAG, "buttonCPOnClick")
        sendBroadcast(Intent(C.NOTIFICATION_ACTION_CP))
    }

    private fun buttonCenteredOnClick() {
        Log.d(TAG, "buttonCenteredOnClick " + buttonCentered.text)
        if (!mapCentered) {
            makeMapCentered()
        } else {
            makeMapNotCentered()
        }
    }

    private fun buttonDirectionOnClick() {
        Log.d(TAG, "buttonDirectionOnClick " + buttonDirection.text)
        // todo logic
        when (buttonDirection.text) {
            getString(R.string.activity_main_button_direction_text_north_up) -> {
                buttonDirection.text = getString(R.string.activity_main_button_direction_text_direction_up)
                mapDirection = getString(R.string.activity_main_button_direction_text_direction_up)
            }
            getString(R.string.activity_main_button_direction_text_direction_up) -> {
                buttonDirection.text = getString(R.string.activity_main_button_direction_text_user_chosen_up)
                mapDirection = getString(R.string.activity_main_button_direction_text_user_chosen_up)
            }
            else -> {
                buttonDirection.text = getString(R.string.activity_main_button_direction_text_north_up)
                mapDirection = getString(R.string.activity_main_button_direction_text_north_up)
            }
        }

    }

    private fun buttonCompassOnClick() {
        Log.d(TAG, "buttonCompassOnClick " + compassSet)
        if (compassSet) {
            makeCompassInvisible()
        } else {
            makeCompassVisible()
        }
    }

    private fun buttonMenuOnClick() {
        Log.d(TAG, "buttonMenuOnClick")
        val intent = Intent(this, MenuActivity::class.java)
        startActivity(intent)
    }

    private fun buttonSettingsOnClick() {
        Log.d(TAG, "buttonSettingsOnClick")
        val intent = Intent(this, SettingsActivity::class.java)
        intent.putExtra(C.FROM_WHERE_TO_SETTINGS, "MAP")
        startActivity(intent)
    }

    fun dealWithTracking() {
        if (trackingSet) {
            buttonStartStop.text = getString(R.string.activity_main_button_startstop_text_stop)
            buttonStartStop.setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.colorStopButton))

            locationServiceActive = true
        } else {
            buttonStartStop.text = getString(R.string.activity_main_button_startstop_text_start)
            buttonStartStop.setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.colorStartButton))

            locationServiceActive = false
        }
    }

    // ============================================== UI =============================================

    // for restoring the ui after unlock, rotating phone etc
    private fun restoreUI() {
        if (compassSet) { makeCompassVisible() }
        else { makeCompassInvisible() }

        if (mapCentered) { makeMapCentered() }
        else { makeMapNotCentered() }

        // todo logic behind this
        when (mapDirection) {
            getString(R.string.activity_main_button_direction_text_north_up) -> {
                buttonDirection.text = getString(R.string.activity_main_button_direction_text_north_up)
            }
            getString(R.string.activity_main_button_direction_text_direction_up) -> {
                buttonDirection.text = getString(R.string.activity_main_button_direction_text_direction_up)
            }
            getString(R.string.activity_main_button_direction_text_user_chosen_up) -> {
                buttonDirection.text = getString(R.string.activity_main_button_direction_text_user_chosen_up)
            }
        }
    }

    // updates statistics
    fun updateUI (intent: Intent) {
        val distanceOverallTotal = intent.getStringExtra(C.STATISTICS_UPDATE_OVERALL_TOTAL)
        val distanceOverallDuration = intent.getStringExtra(C.STATISTICS_UPDATE_OVERALL_DURATION)
        val distanceOverallTempo = intent.getStringExtra(C.STATISTICS_UPDATE_OVERALL_TEMPO)
        if (distanceOverallTotal != null) { textViewOverallTotal.text = distanceOverallTotal }
        if (distanceOverallDuration != null) { textViewOverallDuration.text = distanceOverallDuration }
        if (distanceOverallTempo != null) { textViewOverallTempo.text = distanceOverallTempo }

        val distanceWPTotal = intent.getStringExtra(C.STATISTICS_UPDATE_WP_TOTAL)
        val distanceWPDirect = intent.getStringExtra(C.STATISTICS_UPDATE_WP_DIRECT)
        val distanceWPTempo = intent.getStringExtra(C.STATISTICS_UPDATE_WP_TEMPO)
        if (distanceWPTotal != null) { textViewWPTotal.text = distanceWPTotal }
        if (distanceWPDirect != null) { textViewWPDirect.text = distanceWPDirect }
        if (distanceWPTempo != null) { textViewWPTempo.text = distanceWPTempo }

        val distanceCPTotal = intent.getStringExtra(C.STATISTICS_UPDATE_CP_TOTAL)
        val distanceCPDirect = intent.getStringExtra(C.STATISTICS_UPDATE_CP_DIRECT)
        val distanceCPTempo = intent.getStringExtra(C.STATISTICS_UPDATE_CP_TEMPO)
        if (distanceCPTotal != null) { textViewCPTotal.text = distanceCPTotal }
        if (distanceCPDirect != null) { textViewCPDirect.text = distanceCPDirect }
        if (distanceCPTempo != null) { textViewCPTempo.text = distanceCPTempo }
    }

    // resets ui
    private fun resetUI () {
        textViewOverallDuration.text = ""
        textViewOverallDuration.text = ""
        textViewOverallTempo.text = ""
        textViewWPTotal.text = ""
        textViewWPDirect.text = ""
        textViewWPTempo.text = ""
        textViewCPTotal.text = ""
        textViewCPDirect.text = ""
        textViewCPTempo.text = ""
        curWP = null
        wpMarker = null
        mMap.clear()
    }

    // draws waypoint on map
    private fun drawWaypoint(wpLatLng: LatLng) {
        val circleDrawable: Drawable = ResourcesCompat.getDrawable(resources, R.drawable.baseline_flag_black_24, null) ?: return
        val markerIcon: BitmapDescriptor = Helpers.getMarkerIconFromDrawable(circleDrawable)
        wpMarker = mMap.addMarker(
                MarkerOptions()
                        .position(wpLatLng)
                        //.title("WP")
                        .icon(markerIcon)
        )
    }

    // draws checkpoint on map
    private fun drawCheckpoint(cpLatLng: LatLng) {
        val circleDrawable: Drawable = ResourcesCompat.getDrawable(resources, R.drawable.baseline_beenhere_black_24, null) ?: return
        val markerIcon: BitmapDescriptor = Helpers.getMarkerIconFromDrawable(circleDrawable)
        mMap.addMarker(
                MarkerOptions()
                        .position(cpLatLng)
                        //.title("CP")
                        .icon(markerIcon)
        )
    }

    // draws polyline between two locations
    private fun makePolylineBetweenTwoPlaces(curLatLng: LatLng, prevLatLng: LatLng, speed: Double) {
        if (colorMap != null && minSpeed != null && maxSpeed != null) {
            mMap.addPolyline(PolylineOptions().add(curLatLng, prevLatLng).width(10f).color(Helpers.getColorForSpeed(colorMap!!, speed.times(60), minSpeed!!, maxSpeed!!)))
        }
    }

    // draws track, cps and wp on the map
    private fun restoreMap(intent: Intent) {
        mMap.clear()
        val sessionId = intent.getLongExtra(C.CURRENT_SESSION_ID, Long.MIN_VALUE)
        if (sessionId != Long.MIN_VALUE) {
            if (minSpeed != null && maxSpeed != null) {
                repo.updateSessionMinMaxSpeed(sessionId, minSpeed!!, maxSpeed!!)
            }
            
            val locations = repo.getLocationsForGivenSession(sessionId)
            var i = 0
            var prevLoc : LatLng? = null
            while (i < locations.size) {
                val loc = locations[i]
                val curLatLng = LatLng(loc.latitude, loc.longitude)

                if (loc.type == C.LOCAL_LOCATION_TYPE_CP) {
                    drawCheckpoint(curLatLng)

                } else if (loc.type == C.LOCAL_LOCATION_TYPE_LOC){
                    var speed : Double = 0.toDouble()
                    if (loc.speed != null) {
                        speed = loc.speed!!
                    }

                    if (prevLoc != null) {
                        makePolylineBetweenTwoPlaces(curLatLng, prevLoc, speed)
                    }
                    prevLoc = curLatLng
                }

                i += 1
            }
        }
    }

    // ============================================== BROADCAST RECEIVER =============================================
    private inner class InnerBroadcastReceiver: BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            // intent has been received
            if (intent != null) {
                Log.d(TAG, intent!!.action)

                if (C.LOCATION_UPDATE_ACTION == intent.action){
                    trackingSet = true

                    if (!intent.getDoubleExtra(C.LOCATION_UPDATE_ACTION_LATITUDE, Double.NaN).isNaN() &&
                        !intent.getDoubleExtra(C.LOCATION_UPDATE_ACTION_LONGITUDE, Double.NaN).isNaN()) {
                        val currentLatLng = LatLng(intent.getDoubleExtra(C.LOCATION_UPDATE_ACTION_LATITUDE, 0.0), intent.getDoubleExtra(C.LOCATION_UPDATE_ACTION_LONGITUDE, 0.0))
                        if (mapCentered) {
                            //mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 17f))  // zooms in to cur loc
                            mMap.animateCamera(CameraUpdateFactory.newLatLng(currentLatLng))
                        }

                        if (prevLatLng != null && !intent.getDoubleExtra(C.LOCATION_UPDATE_ACTION_LONGITUDE, Double.NaN).isNaN()) {
                            makePolylineBetweenTwoPlaces(currentLatLng, prevLatLng!!, intent.getDoubleExtra(C.LOCATION_UPDATE_ACTION_SPEED, Double.NaN))
                        }
                        prevLatLng = currentLatLng
                    }

                }

                if (C.STATISTICS_UPDATE_ACTION == intent.action){
                    updateUI(intent)

                    if (!intent.getDoubleExtra(C.CURRENT_WP_LATITUDE, Double.NaN).isNaN() &&
                        !intent.getDoubleExtra(C.CURRENT_WP_LONGITUDE, Double.NaN).isNaN()) {

                        handleNewWaypoint(LatLng(intent.getDoubleExtra(C.CURRENT_WP_LATITUDE, Double.NaN), intent.getDoubleExtra(C.CURRENT_WP_LONGITUDE, Double.NaN)))
                    }

                    if (!intent.getDoubleExtra(C.NEW_CP_LATITUDE, Double.NaN).isNaN() &&
                        !intent.getDoubleExtra(C.NEW_CP_LONGITUDE, Double.NaN).isNaN()) {

                        drawCheckpoint(LatLng(intent.getDoubleExtra(C.NEW_CP_LATITUDE, Double.NaN), intent.getDoubleExtra(C.NEW_CP_LONGITUDE, Double.NaN)))
                    }

                    if (!mapUpdated) {
                        mapUpdated = true

                        restoreMap(intent)

                        trackingSet = true
                        dealWithTracking()
                    }
                }
            }

        }

    }

    // ============================================== COMPASS =============================================
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        Log.d(TAG, "onAccuracyChanged")
    }

    override fun onSensorChanged(event: SensorEvent?) {
        Log.d(TAG, "onSensorChanged")
        if (compassSet) {
            if (event != null) {
                if (event.sensor === accelerometer) {
                    lowPass(event.values, lastAccelerometer)
                    lastAccelerometerSet = true
                } else if (event.sensor === magnetometer) {
                    lowPass(event.values, lastMagnetometer)
                    lastMagnetometerSet = true
                }
            }

            if (lastAccelerometerSet && lastMagnetometerSet) {
                val r = FloatArray(9)
                if (SensorManager.getRotationMatrix(r, null, lastAccelerometer, lastMagnetometer)) {
                    val orientation = FloatArray(3)
                    SensorManager.getOrientation(r, orientation)
                    val degree = (toDegrees(orientation[0].toDouble()) + 360).toFloat() % 360

                    val rotateAnimation = RotateAnimation(
                        currentDegree,
                        -degree,
                        RELATIVE_TO_SELF, 0.5f,
                        RELATIVE_TO_SELF, 0.5f)
                    rotateAnimation.duration = 1000
                    rotateAnimation.fillAfter = true

                    image.startAnimation(rotateAnimation)
                    currentDegree = -degree
                }
            }
        }


    }

    private fun lowPass(input: FloatArray, output: FloatArray) {
        val alpha = 0.05f

        for (i in input.indices) {
            output[i] = output[i] + alpha * (input[i] - output[i])
        }
    }



}

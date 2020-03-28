package ee.taltech.likutt.iti0213_2019s_hw02

// do not import this! never! If this get inserted automatically when pasting java code, remove it
//import android.R

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
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
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.track_control.*
import java.lang.Math.toDegrees


class MainActivity : AppCompatActivity(), OnMapReadyCallback, SensorEventListener {
    companion object {
        private val TAG = this::class.java.declaringClass!!.simpleName
    }

    private lateinit var mMap: GoogleMap

    private val broadcastReceiver = InnerBroadcastReceiver()
    private val broadcastReceiverIntentFilter: IntentFilter = IntentFilter()


    private var locationServiceActive = false

    private var mapCentered = true
    private var compassOn = true

    private val startButtonColor = Color.parseColor("#9ccc65")
    private val stopButtonColor = Color.parseColor("#e57373")

    // compass start  https://github.com/andreas-mausch/compass
    private lateinit var sensorManager: SensorManager
    private lateinit var image: ImageView
    private lateinit var accelerometer: Sensor
    private lateinit var magnetometer: Sensor

    private var currentDegree = 0.0f
    private var lastAccelerometer = FloatArray(3)
    private var lastMagnetometer = FloatArray(3)
    private var lastAccelerometerSet = false
    private var lastMagnetometerSet = false
    //compass end

    private var curWP : LatLng? = null
    private var wpMarker : Marker? = null


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

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        imageButtonCP.setOnClickListener {buttonCPOnClick()}
        imageButtonWP.setOnClickListener {buttonWPOnClick()}

        buttonStartStop.setBackgroundColor(startButtonColor)

        // compass
        image = findViewById(R.id.imageViewCompass) as ImageView
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(TYPE_ACCELEROMETER)
        magnetometer = sensorManager.getDefaultSensor(TYPE_MAGNETIC_FIELD)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Add a marker in Sydney and move the camera
        //val sydney = LatLng(-34.0, 151.0)
        //mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))

        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.isMyLocationEnabled = true

        val currentLatLng = LatLng(59.4367, 24.7533)
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
        if (compassOn) {
            sensorManager.registerListener(this, accelerometer, SENSOR_DELAY_GAME)
            sensorManager.registerListener(this, magnetometer, SENSOR_DELAY_GAME)
        }
    }

    override fun onPause() {
        Log.d(TAG, "onPause")
        super.onPause()

        // compass
        if (compassOn) {
            sensorManager.unregisterListener(this, accelerometer)
            sensorManager.unregisterListener(this, magnetometer)
        }
    }

    override fun onStop() {
        Log.d(TAG, "onStop")
        super.onStop()

        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver)
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy")
        super.onDestroy()
    }

    override fun onRestart() {
        Log.d(TAG, "onRestart")
        super.onRestart()
    }

    // ============================================== NOTIFICATION CHANNEL CREATION =============================================
    private fun createNotificationChannel() {
        // when on 8 Oreo or higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                C.NOTIFICATION_CHANNEL,
                "Default channel",
                NotificationManager.IMPORTANCE_LOW
            );

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
                .setAction("OK", View.OnClickListener {
                    // Request permission
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        C.REQUEST_PERMISSIONS_REQUEST_CODE
                    )
                })
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
            if (grantResults.count() <= 0) { // If user interaction was interrupted, the permission request is cancelled and you
                // receive empty arrays.
                Log.i(TAG, "User interaction was cancelled.")
                Toast.makeText(this, "User interaction was cancelled.", Toast.LENGTH_SHORT).show()
            } else if (grantResults[0] === PackageManager.PERMISSION_GRANTED) {// Permission was granted.
                Log.i(TAG, "Permission was granted")
                Toast.makeText(this, "Permission was granted", Toast.LENGTH_SHORT).show()
            } else { // Permission denied.
                Snackbar.make(
                    findViewById(R.id.activity_main),
                    "You denied GPS! What can I do?",
                    Snackbar.LENGTH_INDEFINITE
                )
                    .setAction("Settings", View.OnClickListener {
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
                    })
                    .show()
            }
        }

    }



    // ============================================== CLICK HANDLERS =============================================
    fun buttonStartStopOnClick(view: View) {
        Log.d(TAG, "buttonStartStopOnClick. locationServiceActive: $locationServiceActive")
        // try to start/stop the background service

        if (locationServiceActive) {
            // stopping the service
            stopService(Intent(this, LocationService::class.java))

            buttonStartStop.text = "START"
            buttonStartStop.setBackgroundColor(startButtonColor)

        } else {
            resetUI()
            if (Build.VERSION.SDK_INT >= 26) {
                // starting the FOREGROUND service
                // service has to display non-dismissable notification within 5 secs
                startForegroundService(Intent(this, LocationService::class.java))
            } else {
                startService(Intent(this, LocationService::class.java))
            }
            buttonStartStop.text = "STOP"
            buttonStartStop.setBackgroundColor(stopButtonColor)
        }

        locationServiceActive = !locationServiceActive
    }

    fun buttonWPOnClick() {
        Log.d(TAG, "buttonWPOnClick")
        sendBroadcast(Intent(C.NOTIFICATION_ACTION_WP))
    }

    fun buttonCPOnClick() {
        Log.d(TAG, "buttonCPOnClick")
        sendBroadcast(Intent(C.NOTIFICATION_ACTION_CP))
    }

    fun buttonCenteredOnClick(view: View) {
        Log.d(TAG, "buttonCenteredOnClick " + buttonCentered.text)
        if (!mapCentered) {
            buttonCentered.text = "Centered"
            mapCentered = true
        } else {
            buttonCentered.text = "Not centered"
            mapCentered = false
        }
    }

    fun buttonDirectionOnClick(view: View) {
        Log.d(TAG, "buttonDirectionOnClick " + buttonDirection.text)
        // todo logic
        if (buttonDirection.text == "North-up") {
            buttonDirection.text = "Direction up"
        } else if (buttonDirection.text == "Direction up") {
            buttonDirection.text = "User chosen-up"
        } else {
            buttonDirection.text = "North-up"
        }

    }


    fun buttonCompassOnClick(view: View) {
        Log.d(TAG, "buttonCompassOnClick " + compassOn)
        if (compassOn) {
            compassOn = false
            imageButton.setImageResource(R.drawable.baseline_explore_off_white_24)
            sensorManager.unregisterListener(this, accelerometer)
            sensorManager.unregisterListener(this, magnetometer)
            includeCompass.visibility = View.INVISIBLE
        } else {
            compassOn = true
            imageButton.setImageResource(R.drawable.baseline_explore_white_24)
            sensorManager.registerListener(this, accelerometer, SENSOR_DELAY_GAME)
            sensorManager.registerListener(this, magnetometer, SENSOR_DELAY_GAME)
            includeCompass.visibility = View.VISIBLE
        }
    }

    fun buttonMenuOnClick(view: View) {
        Log.d(TAG, "buttonMenuOnClick")
        // todo open menu
    }

    // ============================================== UI =============================================

    fun updateUI (intent: Intent) {
        val distanceOverallTotal = intent.getStringExtra(C.DISTANCE_OVERALL_TOTAL)
        val distanceOverallDuration = intent.getStringExtra(C.DISTANCE_OVERALL_DURATION)
        val distanceOverallTempo = intent.getStringExtra(C.DISTANCE_OVERALL_TEMPO)
        if (distanceOverallTotal != null) { textViewOverallTotal.text = distanceOverallTotal }
        if (distanceOverallDuration != null) { textViewOverallDuration.text = distanceOverallDuration }
        if (distanceOverallTempo != null) { textViewOverallTempo.text = distanceOverallTempo }

        val distanceWPTotal = intent.getStringExtra(C.DISTANCE_WP_TOTAL)
        val distanceWPDirect = intent.getStringExtra(C.DISTANCE_WP_DIRECT)
        val distanceWPTempo = intent.getStringExtra(C.DISTANCE_WP_TEMPO)
        if (distanceWPTotal != null) { textViewWPTotal.text = distanceWPTotal }
        if (distanceWPDirect != null) { textViewWPDirect.text = distanceWPDirect }
        if (distanceWPTempo != null) { textViewWPTempo.text = distanceWPTempo }

        val distanceCPTotal = intent.getStringExtra(C.DISTANCE_CP_TOTAL)
        val distanceCPDirect = intent.getStringExtra(C.DISTANCE_CP_DIRECT)
        val distanceCPTempo = intent.getStringExtra(C.DISTANCE_CP_TEMPO)
        if (distanceCPTotal != null) { textViewCPTotal.text = distanceCPTotal }
        if (distanceCPDirect != null) { textViewCPDirect.text = distanceCPDirect }
        if (distanceCPTempo != null) { textViewCPTempo.text = distanceCPTempo }
    }

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
    }

    private fun handleNewWaypoint(wpLatLng: LatLng) {
        curWP = wpLatLng
        if (wpMarker != null) {
            wpMarker!!.remove()
            wpMarker = null
        }
        val circleDrawable: Drawable = resources.getDrawable(R.drawable.baseline_flag_black_24)
        val markerIcon: BitmapDescriptor = getMarkerIconFromDrawable(circleDrawable)
        wpMarker = mMap.addMarker(
            MarkerOptions()
                .position(wpLatLng)
                .title("My Marker")
                .icon(markerIcon)
        )
    }

    private fun getMarkerIconFromDrawable(drawable: Drawable): BitmapDescriptor {
        val canvas = Canvas()
        val bitmap: Bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        canvas.setBitmap(bitmap)
        drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
        drawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    // ============================================== BROADCAST RECEIVER =============================================
    private inner class InnerBroadcastReceiver: BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.d(TAG, intent!!.action)
            when (intent!!.action){
                C.LOCATION_UPDATE_ACTION -> {
                    //textViewLatitude.text = intent.getDoubleExtra(C.LOCATION_UPDATE_ACTION_LATITUDE, 0.0).toString()
                    //textViewLongitude.text = intent.getDoubleExtra(C.LOCATION_UPDATE_ACTION_LONGITUDE, 0.0).toString()

                    if (!intent.getDoubleExtra(C.LOCATION_UPDATE_ACTION_LATITUDE, Double.NaN).isNaN() &&
                        !intent.getDoubleExtra(C.LOCATION_UPDATE_ACTION_LONGITUDE, Double.NaN).isNaN()) {
                        val currentLatLng = LatLng(intent.getDoubleExtra(C.LOCATION_UPDATE_ACTION_LATITUDE, 0.0), intent.getDoubleExtra(C.LOCATION_UPDATE_ACTION_LONGITUDE, 0.0))
                        if (mapCentered) {
                            //mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 17f))  // zooms in to cur loc
                            mMap.animateCamera(CameraUpdateFactory.newLatLng(currentLatLng))
                        }
                    }

                    updateUI(intent)

                    if (!intent.getDoubleExtra(C.CURRENT_WP_LATITUDE, Double.NaN).isNaN() &&
                        !intent.getDoubleExtra(C.CURRENT_WP_LONGITUDE, Double.NaN).isNaN()) {

                        handleNewWaypoint(LatLng(intent.getDoubleExtra(C.CURRENT_WP_LATITUDE, Double.NaN), intent.getDoubleExtra(C.CURRENT_WP_LONGITUDE, Double.NaN)))
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
        if (compassOn) {
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

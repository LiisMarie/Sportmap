package ee.taltech.likutt.iti0213_2019s_hw02

import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import kotlinx.android.synthetic.main.activity_view_old_session.*
import kotlinx.android.synthetic.main.statistics_view_old_session.*


class ViewOldSessionActivity : AppCompatActivity(), OnMapReadyCallback {
    companion object {
        private val TAG = this::class.java.declaringClass!!.simpleName
    }

    private lateinit var repo: Repository

    private var session: TrackingSession? = null

    private lateinit var mMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_old_session)

        // clear the track on map
        Helpers.clearMapPolylineOptions()

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val id = intent.getLongExtra(C.OLD_SESSION_ID, Long.MIN_VALUE)

        repo = Repository(this).open()

        session = repo.getSessionById(id)

        if (session == null) {
            openHistory()
        } else {
            textViewHeader.text = session!!.name
            toolbar.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
            toolbar.requestLayout()
            textViewDistance.text = String.format("%.2f", session!!.distance)
            textViewSpeed.text = session!!.speed
            textViewDuration.text = Helpers.getTimeString(session!!.duration)
        }

        setOnClickListeners()
    }

    private fun setOnClickListeners() {
        buttonSpeed.setOnClickListener {
            val intent = Intent(this, OldSessionSettingsActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            if (session != null) {
                intent.putExtra(C.OLD_SESSION_ID, session!!.id)
            }
            startActivity(intent)
            finish()
        }
        imageButtonBack.setOnClickListener {
            openHistory()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.isMyLocationEnabled = false

        if (session != null) {
            displayTrack()
        }
    }

    private fun displayTrack() {
        Log.d(TAG, "displayTrack")
        val locations = repo.getLocationsForGivenSession(session!!.id)
        val colorMap = Helpers.generateColorsForSpeeds(session!!.minSpeed, session!!.maxSpeed)

        Log.d(TAG, "LOCATIONS " + locations)

        var i = 0
        var prevLoc : LatLng? = null
        while (i < locations.size) {
            val loc = locations[i]

            val curLatLng = LatLng(loc.latitude, loc.longitude)

            when (loc.type) {
                C.LOCAL_LOCATION_TYPE_START -> {
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(curLatLng, 17f))  // zooms in on start location
                    drawStart(curLatLng)
                    prevLoc = curLatLng
                }
                C.LOCAL_LOCATION_TYPE_CP -> {
                    drawCheckpoint(curLatLng)

                }
                C.LOCAL_LOCATION_TYPE_LOC -> {
                    var speedSecPerKm : Double = 0.toDouble()
                    if (loc.speed != null) {
                        speedSecPerKm = loc.speed!!.times(60)
                    }

                    if (prevLoc != null) {
                        mMap.addPolyline(PolylineOptions().add(curLatLng, prevLoc).width(10f).color(Helpers.getColorForSpeed(colorMap, speedSecPerKm, session!!.minSpeed, session!!.maxSpeed)))
                    }

                    prevLoc = curLatLng
                }
            }

            i += 1
        }
    }

    private fun drawStart(startLatLng: LatLng) {
        mMap.addMarker(
                MarkerOptions()
                        .position(startLatLng)
                        .title("START")
        )
    }

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

    private fun openHistory() {
        val intent = Intent(this, HistoryActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
        finish()
    }
}

package ee.taltech.likutt.iti0213_2019s_hw02

import android.content.Intent
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

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
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val id = intent.getLongExtra(C.OLD_SESSION_ID, Long.MIN_VALUE)

        repo = Repository(this).open()

        session = repo.getSessionById(id)

        if (session == null) {
            openHistory()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.isMyLocationEnabled = true

        if (session != null) {
            displayTrack()
        }
    }

    fun displayTrack() {
        Log.d(TAG, "displayTrack")
        val locations = repo.getLocationsForGivenSession(session!!.id)
        for (loc in locations) {
            Log.d(TAG, "LOCATION: " + loc.toString())
            val locLatLng = LatLng(loc.latitude.toDouble(), loc.longitude.toDouble())

            if (loc.type == C.LOCAL_LOCATION_TYPE_START) {
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(locLatLng, 17f))  // zooms in on start location
                drawStart(locLatLng)
            } else if (loc.type == C.LOCAL_LOCATION_TYPE_CP) {
                drawCheckpoint(locLatLng)
            }
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
        val circleDrawable: Drawable = resources.getDrawable(R.drawable.baseline_beenhere_black_24)
        val markerIcon: BitmapDescriptor = Helpers.getMarkerIconFromDrawable(circleDrawable)

        mMap.addMarker(
                MarkerOptions()
                        .position(cpLatLng)
                        //.title("CP")
                        .icon(markerIcon)
        )
    }

    fun openHistoryView(view: View) {
        openHistory()
    }

    private fun openHistory() {
        val intent = Intent(this, HistoryActivity::class.java)
        startActivity(intent)
    }
}

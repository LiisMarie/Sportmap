package ee.taltech.likutt.iti0213_2019s_hw02.database

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.location.Location
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import ee.taltech.likutt.iti0213_2019s_hw02.R
import ee.taltech.likutt.iti0213_2019s_hw02.activities.HistoryActivity
import ee.taltech.likutt.iti0213_2019s_hw02.activities.RenameOldSessionActivity
import ee.taltech.likutt.iti0213_2019s_hw02.activities.ViewOldSessionActivity
import ee.taltech.likutt.iti0213_2019s_hw02.api.WebApiSingletonHandler
import ee.taltech.likutt.iti0213_2019s_hw02.classes.TrackingLocation
import ee.taltech.likutt.iti0213_2019s_hw02.classes.TrackingSession
import ee.taltech.likutt.iti0213_2019s_hw02.classes.User
import ee.taltech.likutt.iti0213_2019s_hw02.helpers.C
import ee.taltech.likutt.iti0213_2019s_hw02.helpers.Helpers
import ee.taltech.likutt.iti0213_2019s_hw02.helpers.LocationService
import kotlinx.android.synthetic.main.recycler_old_session.view.*
import org.json.JSONObject
import java.io.File
import java.io.FileWriter
import java.util.*


class DataRecyclerViewAdapterSessions (val context: Context, private val oldSessions: List<TrackingSession>) :
    RecyclerView.Adapter<DataRecyclerViewAdapterSessions.ViewHolder>() {

    companion object {
        private val TAG = this::class.java.declaringClass!!.simpleName
    }

    private lateinit var repo: Repository

    // for backend
    private var mJwt: String? = null
    private var trackingSessionId: String? = null

    // opening views
    private val inflater: LayoutInflater = LayoutInflater.from(context)

    // creates views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val rowView = inflater.inflate(R.layout.recycler_old_session, parent, false)
        return ViewHolder(rowView)
    }

    // how many rows are in the table
    override fun getItemCount(): Int {
        return oldSessions.count()
    }

    // now the view is on the screen
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val session = oldSessions[position]  // gives position that is currently on the screen

        // sets text in the views
        holder.itemView.textViewHeader.text = session.name
        holder.itemView.textViewSessionDescription.text = session.description
        holder.itemView.textViewRecordedAt.text = session.recordedAt
        holder.itemView.textViewDistance.text = String.format("%.2f", session.distance)
        holder.itemView.textViewDuration.text = Helpers.getTimeString(session.duration)
        holder.itemView.textViewSpeed.text = session.speed

        repo = Repository(context).open()
        val user = repo.getUser()
        if (user == null) {
            holder.itemView.buttonSynced.visibility = View.GONE
        } else {
            if (session.synced == C.SYNCING_SYNCED) {
                holder.itemView.buttonSynced.text = "SYNCED"
                holder.itemView.buttonSynced.setBackgroundColor(ContextCompat.getColor(context, R.color.colorDisabledButton))
            } else {
                holder.itemView.buttonSynced.text = "SYNC"
                holder.itemView.buttonSynced.setBackgroundColor(ContextCompat.getColor(context, R.color.colorControlButton))
                holder.itemView.buttonSynced.setTextColor(ContextCompat.getColor(context, R.color.colorWhiteText))
            }
        }



        // takes to session view
        holder.itemView.buttonRegister.setOnClickListener {
            val intent = Intent(context, ViewOldSessionActivity::class.java)
            intent.putExtra(C.OLD_SESSION_ID, session.id)
            context.startActivity(intent)
        }

        // takes to session editing view
        holder.itemView.buttonEdit.setOnClickListener {
            val intent = Intent(context, RenameOldSessionActivity::class.java)
            intent.putExtra(C.OLD_SESSION_ID, session.id)
            context.startActivity(intent)
        }

        // session deletion
        holder.itemView.buttonDelete.setOnClickListener {
            deleteSession(session.id)
        }

        // sending session as a gpx file to email
        holder.itemView.buttonSend.setOnClickListener {
            sendEmail(session)
        }

        // sync locations afterwards
        holder.itemView.buttonSynced.setOnClickListener {
            if (session.synced != C.SYNCING_SYNCED) {
                syncSession(session, user!!, holder)
            }
        }
    }

    // handles caching and deletion
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    // session export as email
    private fun sendEmail(session: TrackingSession) {
        var edittext = EditText(context);

        AlertDialog.Builder(context)
                .setTitle("Exporting session as GPX file")
                .setMessage("Enter email where to export")
                .setIcon(R.drawable.twotone_email_24)
                .setView(edittext)
                .setPositiveButton("Export") { _, _ ->

                    val email = edittext.text.toString()

                    val gpxString = generateGpx(session)
                    val fileName = session.recordedAt

                    val tempFile = File.createTempFile(fileName, ".gpx", context!!.externalCacheDir)
                    val fw = FileWriter(tempFile)

                    fw.write(gpxString)

                    fw.flush()
                    fw.close()

                    val mailTo = "mailto:" + email +
                            "?&subject=" + Uri.encode("GPX file") +
                            "&body=" + Uri.encode("See attachments")
                    val emailIntent = Intent(Intent.ACTION_VIEW)
                    emailIntent.data = Uri.parse(mailTo)
                    emailIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(tempFile))
                    context.startActivity(Intent.createChooser(emailIntent, "Pick an Email provider"))
                }
                .setNegativeButton("Cancel", null).show()
    }

    // makes gpx string
    private fun generateGpx(session: TrackingSession) : String {
        var gpxString = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<gpx version=\"1.1\" creator=\"likuttSportsmap\">\n" +
                "  <metadata>\n" +
                "    <time>" + session.recordedAt + "</time>\n" +
                "  </metadata>\n"
        var wpString = ""
        var trackString = "  <trk>\n" +
                "    <trkseg>"

        val repo = Repository(context).open()
        val locations = repo.getLocationsForGivenSession(session.id)
        for (location in locations) {
            if (location.type == C.REST_LOCATIONID_CP) {
                wpString += "  <wpt lat=\"" + location.latitude + "\" lon=\"" + location.longitude + "\">\n" +
                        "    <time>" + location.recordedAt +"</time>\n" +
                        "  </wpt>\n"
            } else if (location.type == C.REST_LOCATIONID_LOC) {
                trackString += "      <trkpt lat=\"" + location.latitude + "\" lon=\"" + location.longitude + "\">\n" +
                        "        <time>" + location.recordedAt + "</time>\n" +
                        "      </trkpt>\n"
            }
        }
        gpxString += wpString
        gpxString += trackString
        gpxString += "    </trkseg>\n" +
                "  </trk>\n" +
                "</gpx>"
        return gpxString
    }

    // session deletion
    private fun deleteSession(sessionId : Long) {
        AlertDialog.Builder(context)
                .setTitle("Warning")
                .setMessage("Do you want to delete this session?")
                .setIcon(R.drawable.twotone_warning_24)
                .setPositiveButton("YES") { _, _ ->
                    val repo = Repository(context).open()
                    repo.deleteSessionWithItsLocations(sessionId)

                    val intent = Intent(context, HistoryActivity::class.java)

                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                    context.startActivity(intent)
                    (context as Activity).finish()
                }
                .setNegativeButton("NO", null).show()
    }

    // syncing session
    private fun syncSession(session: TrackingSession, user: User, holder: ViewHolder) {
        if (isNetworkAvailable()) {
            getRestToken(user, session, holder)

            /*
            Log.d(TAG, "mJwt    " + mJwt)

            if (mJwt != null) {
                if (session.restSessionId == "") {
                    startRestTrackingSession(session)
                }

                Log.d(TAG, "SESSION ID    " + trackingSessionId)
                val locations = repo.getLocationsForGivenSession(session.id)
                if (locations != null) {
                    for (location in locations) {
                        if (location.synced != C.SYNCING_SYNCED) {
                            saveRestLocation(location)
                        }
                    }

                    var allSynced = true
                    for (location in locations) {
                        if (location.synced != C.SYNCING_SYNCED) {
                            allSynced = false
                        }
                    }

                    if (allSynced) {
                        repo.updateSessionSynced(session.id, C.SYNCING_SYNCED)
                        holder.itemView.buttonSynced.text = "SYNCED"
                        holder.itemView.buttonSynced.setBackgroundColor(ContextCompat.getColor(context, R.color.colorDisabledButton))
                        Toast.makeText(context, "Syncing was successful!", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(context, "Couldn't sync!", Toast.LENGTH_SHORT).show()
            }*/
        } else {
            Toast.makeText(context, "Make sure that You have internet connection!", Toast.LENGTH_SHORT).show()
        }

    }

    private fun getRestToken(user: User, session: TrackingSession, holder: ViewHolder) {
        val handler = WebApiSingletonHandler.getInstance(context)

        val requestJsonParameters = JSONObject()
        requestJsonParameters.put("email", user.email)
        requestJsonParameters.put("password", user.password)

        val httpRequest = JsonObjectRequest(
                Request.Method.POST,
                C.REST_BASE_URL + "account/login",
                requestJsonParameters,
                Response.Listener { response ->
                    mJwt = response.getString("token")






                    /*

                     */
                    Log.d(TAG, "mJwt    " + mJwt)

                    if (session.restSessionId == "") {
                        startRestTrackingSession(session, holder)
                    } else {
                        syncLocations(session, holder)
                    }







                },
                Response.ErrorListener { error ->
                }
        )

        handler.addToRequestQueue(httpRequest)
    }

    private fun syncLocations(session: TrackingSession, holder: ViewHolder) {
        Log.d(TAG, "SESSION ID    " + trackingSessionId)
        val locations = repo.getLocationsForGivenSession(session.id)
        if (locations != null) {
            for (location in locations) {
                if (location.synced != C.SYNCING_SYNCED) {
                    saveRestLocation(location)
                }
            }

            var allSynced = true
            for (location in locations) {
                if (location.synced != C.SYNCING_SYNCED) {
                    allSynced = false
                }
            }

            if (allSynced) {
                repo.updateSessionSynced(session.id, C.SYNCING_SYNCED)
                holder.itemView.buttonSynced.text = "SYNCED"
                holder.itemView.buttonSynced.setBackgroundColor(ContextCompat.getColor(context, R.color.colorDisabledButton))
                Toast.makeText(context, "Syncing was successful!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun startRestTrackingSession(session: TrackingSession, holder: ViewHolder) {
        val handler = WebApiSingletonHandler.getInstance(context)
        val requestJsonParameters = JSONObject()

        requestJsonParameters.put("name", session.name)
        requestJsonParameters.put("description", session.description)
        requestJsonParameters.put("paceMin", session.minSpeed)
        requestJsonParameters.put("paceMax", session.maxSpeed)
        requestJsonParameters.put("recordedAt", session.recordedAt)

        val httpRequest = object : JsonObjectRequest(
                Method.POST,
                C.REST_BASE_URL + "GpsSessions",
                requestJsonParameters,
                Response.Listener { response ->
                    trackingSessionId = response.getString("id")

                    syncLocations(session, holder)
                },
                Response.ErrorListener { error ->
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

    private fun saveRestLocation(location: TrackingLocation) {

        val handler = WebApiSingletonHandler.getInstance(context)
        val requestJsonParameters = JSONObject()

        requestJsonParameters.put("recordedAt", location.recordedAt)
        requestJsonParameters.put("latitude", location.latitude)
        requestJsonParameters.put("longitude", location.longitude)
        requestJsonParameters.put("accuracy", location.accuracy)
        requestJsonParameters.put("altitude", location.altitude)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            requestJsonParameters.put("verticalAccuracy", location.verticalAccuracy)
        }
        requestJsonParameters.put("gpsSessionId", trackingSessionId)
        requestJsonParameters.put("gpsLocationTypeId", location.type)

        val httpRequest = object : JsonObjectRequest(
                Method.POST,
                C.REST_BASE_URL + "GpsLocations",
                requestJsonParameters,
                Response.Listener { response ->
                    repo.updateLocationsSynced(location.id, C.SYNCING_SYNCED)
                },
                Response.ErrorListener { error ->
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

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetworkInfo
        return activeNetwork?.isConnectedOrConnecting == true
    }

}
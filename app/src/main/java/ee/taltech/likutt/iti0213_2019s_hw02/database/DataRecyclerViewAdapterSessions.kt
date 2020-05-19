package ee.taltech.likutt.iti0213_2019s_hw02.database

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.recyclerview.widget.RecyclerView
import ee.taltech.likutt.iti0213_2019s_hw02.R
import ee.taltech.likutt.iti0213_2019s_hw02.activities.HistoryActivity
import ee.taltech.likutt.iti0213_2019s_hw02.activities.RenameOldSessionActivity
import ee.taltech.likutt.iti0213_2019s_hw02.activities.ViewOldSessionActivity
import ee.taltech.likutt.iti0213_2019s_hw02.classes.TrackingSession
import ee.taltech.likutt.iti0213_2019s_hw02.helpers.C
import ee.taltech.likutt.iti0213_2019s_hw02.helpers.Helpers
import kotlinx.android.synthetic.main.recycler_old_session.view.*
import java.io.File
import java.io.FileWriter


class DataRecyclerViewAdapterSessions (val context: Context, private val oldSessions: List<TrackingSession>) :
    RecyclerView.Adapter<DataRecyclerViewAdapterSessions.ViewHolder>() {

    companion object {
        private val TAG = this::class.java.declaringClass!!.simpleName
    }

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
}
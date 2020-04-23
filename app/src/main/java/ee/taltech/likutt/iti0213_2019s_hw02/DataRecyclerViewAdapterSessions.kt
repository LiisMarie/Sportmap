package ee.taltech.likutt.iti0213_2019s_hw02

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.recycler_old_session.view.*
import org.json.JSONObject
import java.math.RoundingMode

class DataRecyclerViewAdapterSessions (val context: Context, private val oldSessions: List<TrackingSession>) :
    RecyclerView.Adapter<DataRecyclerViewAdapterSessions.ViewHolder>() {

    // vaadete lahti tegemine
    private val inflater: LayoutInflater = LayoutInflater.from(context)

    // loob vaated
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val rowView = inflater.inflate(R.layout.recycler_old_session, parent, false)
        return ViewHolder(rowView)
    }

    // mitu rida meil tabelis on
    override fun getItemCount(): Int {
        return oldSessions.count()
    }

    // nyyd see view jõudis ekraanile
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val session = oldSessions[position]  // annab positsiooni mis ekraanil lahti hetkel on
        holder.itemView.textViewSessionName.text = session.name
        holder.itemView.textViewSessionDescription.text = session.description
        holder.itemView.textViewRecordedAt.text = session.recordedAt
        holder.itemView.textViewDistance.text = String.format("%.2f", session.distance)
        holder.itemView.textViewDuration.text = Helpers.getTimeString(session.duration)
        holder.itemView.textViewSpeed.text = session.speed

        holder.itemView.buttonLoad.setOnClickListener {
            context.startActivity(Intent(context, ViewOldSessionActivity::class.java))
        }

        holder.itemView.buttonEdit.setOnClickListener {

        }

        holder.itemView.buttonDelete.setOnClickListener {

        }
    }

    // tegeleb asjade cacheimisega ja kustutamisega
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}
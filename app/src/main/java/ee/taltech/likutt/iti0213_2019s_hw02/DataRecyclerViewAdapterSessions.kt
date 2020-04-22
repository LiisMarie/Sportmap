package ee.taltech.likutt.iti0213_2019s_hw02

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.recycler_old_session.view.*
import org.json.JSONObject

class DataRecyclerViewAdapterSessions (context: Context, private val oldSessions: List<JSONObject>) :
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
        holder.itemView.textViewSessionName.text = session.getString("name")
        holder.itemView.textViewSessionDescription.text = session.getString("description")
        holder.itemView.textViewRecordedAt.text = session.getString("recordedAt")
        holder.itemView.textViewDistance.text = session.getLong("duration").toString()
        holder.itemView.textViewDuration.text = session.getLong("speed").toString()
        holder.itemView.textViewSpeed.text = session.getLong("distance").toString()
    }

    // tegeleb asjade cacheimisega ja kustutamisega
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}
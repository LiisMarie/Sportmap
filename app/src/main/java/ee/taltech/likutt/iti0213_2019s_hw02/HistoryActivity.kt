package ee.taltech.likutt.iti0213_2019s_hw02

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import kotlinx.android.synthetic.main.activity_history.*
import org.json.JSONObject


class HistoryActivity : AppCompatActivity() {
    companion object {
        private val TAG = this::class.java.declaringClass!!.simpleName
    }

    private lateinit var repo: Repository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        repo = Repository(this).open()

        val locationss = repo.getAllLocations()
        for (loc in locationss) {
            Log.d("LOCATION: ", loc.toString())
        }

        // layoutManager - hoolitseb kuidas rowsid vahetada
        recyclerViewOldSessions.layoutManager = LinearLayoutManager(this)
        // adapter - joonistab asja välja (tuleb ise luua)
        recyclerViewOldSessions.adapter = DataRecyclerViewAdapterSessions(this, repo.getAllSessions())
    }

    fun openMenu(view: View) {
        val intent = Intent(this, MenuActivity::class.java)
        startActivity(intent)
    }

}

package ee.taltech.likutt.iti0213_2019s_hw02

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity

class ViewOldSessionActivity: AppCompatActivity() {
    companion object {
        private val TAG = this::class.java.declaringClass!!.simpleName
    }

    private lateinit var repo: Repository

    private var session: TrackingSession? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_old_session)

        val id = intent.getLongExtra(C.OLD_SESSION_ID, Long.MIN_VALUE)

        repo = Repository(this).open()

        session = repo.getSessionById(id)

        if (session == null) {
            openHistory()
        } else {
            displayTrack()
        }
    }

    fun displayTrack() {
        val locations = repo.getLocationsForGivenSession(session!!.id)
        for (loc in locations) {
            Log.d(TAG, "LOCATION: " + loc.toString())
        }

    }

    fun openHistoryView(view: View) {
        openHistory()
    }

    private fun openHistory() {
        val intent = Intent(this, HistoryActivity::class.java)
        startActivity(intent)
    }
}

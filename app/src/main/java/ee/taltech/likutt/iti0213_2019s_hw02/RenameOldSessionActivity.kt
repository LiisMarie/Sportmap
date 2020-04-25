package ee.taltech.likutt.iti0213_2019s_hw02

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_rename_old_session.*

class RenameOldSessionActivity : AppCompatActivity() {
    companion object {
        private val TAG = this::class.java.declaringClass!!.simpleName
    }

    private lateinit var repo: Repository

    private var session: TrackingSession? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rename_old_session)

        val id = intent.getLongExtra(C.OLD_SESSION_ID, Long.MIN_VALUE)

        repo = Repository(this).open()

        session = repo.getSessionById(id)

        if (session == null) {
            openHistory()
        } else {
            textViewRecordedAt.text = session!!.recordedAt
            textViewDistance.text = String.format("%.2f", session!!.distance)
            textViewDuration.text = Helpers.getTimeString(session!!.duration)
            textViewSpeed.text = session!!.speed
            editTextSessionName.setText(session!!.name)
            editTextSessionDescription.setText(session!!.description)
        }
    }

    fun openHistoryView(view: View) {
        openHistory()
    }

    private fun openHistory() {
        val intent = Intent(this, HistoryActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP;
        startActivity(intent)
        finish()
    }

    fun saveSessionChanges(view: View) {
        if (session != null) {
            repo.updateSessionNameDescription(session!!.id, editTextSessionName.text.toString(), editTextSessionDescription.text.toString())
            var intent = Intent(this, RenameOldSessionActivity::class.java)
            intent.putExtra(C.OLD_SESSION_ID, session!!.id)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP;
            startActivity(intent)
            finish()
        } else {
            Toast.makeText(this, "Couldn't update session", Toast.LENGTH_SHORT).show()
        }

    }
}

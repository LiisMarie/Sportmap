package ee.taltech.likutt.iti0213_2019s_hw02.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import ee.taltech.likutt.iti0213_2019s_hw02.*
import ee.taltech.likutt.iti0213_2019s_hw02.classes.TrackingSession
import ee.taltech.likutt.iti0213_2019s_hw02.database.Repository
import ee.taltech.likutt.iti0213_2019s_hw02.helpers.C
import ee.taltech.likutt.iti0213_2019s_hw02.helpers.Helpers
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

        // gets session which data is to be changed
        session = repo.getSessionById(id)
        // if no session is found then returns to history
        if (session == null) {
            openHistory()
        } else {
            // displays current information about the session
            textViewRecordedAt.text = session!!.recordedAt
            textViewDistance.text = String.format("%.2f", session!!.distance)
            textViewDuration.text = Helpers.getTimeString(session!!.duration)
            textViewSpeed.text = session!!.speed
            editTextSessionName.setText(session!!.name)
            editTextSessionDescription.setText(session!!.description)
        }

        setOnClickListeners()
    }

    // sets on click listeners
    private fun setOnClickListeners() {
        imageButtonBack.setOnClickListener {
            openHistory()
        }
        buttonRegister.setOnClickListener {
            saveSessionChanges()
        }
    }

    // opens history view
    private fun openHistory() {
        val intent = Intent(this, HistoryActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
        finish()
    }

    // saves changes that user made to the session
    private fun saveSessionChanges() {
        if (session != null) {
            repo.updateSessionNameDescription(session!!.id, editTextSessionName.text.toString(), editTextSessionDescription.text.toString())

            val intent = Intent(this, RenameOldSessionActivity::class.java)
            intent.putExtra(C.OLD_SESSION_ID, session!!.id)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
        } else {
            Toast.makeText(this, "Couldn't update session", Toast.LENGTH_SHORT).show()
        }

    }
}

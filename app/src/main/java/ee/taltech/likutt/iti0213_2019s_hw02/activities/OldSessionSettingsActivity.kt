package ee.taltech.likutt.iti0213_2019s_hw02.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import ee.taltech.likutt.iti0213_2019s_hw02.*
import ee.taltech.likutt.iti0213_2019s_hw02.database.Repository
import ee.taltech.likutt.iti0213_2019s_hw02.helpers.C
import ee.taltech.likutt.iti0213_2019s_hw02.helpers.Helpers
import kotlinx.android.synthetic.main.activity_old_session_settings.*

class OldSessionSettingsActivity : AppCompatActivity() {

    private lateinit var repo: Repository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_old_session_settings)

        // gets session id which settings have to be changed
        val sessionId = intent.getLongExtra(C.OLD_SESSION_ID, Long.MIN_VALUE)
        if (sessionId == Long.MIN_VALUE) {
            goToOldSessionView(sessionId)
        }

        repo = Repository(this).open()

        // get session, if session couldn't be received go back to previous view
        val session = repo.getSessionById(sessionId)
        if (session == null) {
            goToOldSessionView(sessionId)
        }
        // fill edittexts with current speeds
        editTextMinSpeed.setText((session!!.minSpeed / 60).toString())
        editTextMaxSpeed.setText((session.maxSpeed / 60).toString())

        setOnClickListeners(sessionId)
    }

    // sets onclicklisteners
    private fun setOnClickListeners(sessionId: Long) {
        // takes back to session view
        imageButtonBack.setOnClickListener {
            goToOldSessionView(sessionId)
        }

        // updates speed
        buttonUpdateSpeed.setOnClickListener {

            if (editTextMinSpeed.text.toString() != "" && editTextMaxSpeed.text.toString() != "") {
                // user input is in minutes, backend needs it to be in seconds
                val minSpeed : Double = editTextMinSpeed.text.toString().toDouble() * 60
                val maxSpeed : Double = editTextMaxSpeed.text.toString().toDouble() * 60

                if (Helpers.validateSpeedInput(minSpeed, maxSpeed)) {

                    repo.updateSessionMinMaxSpeed(sessionId, minSpeed, maxSpeed)

                    Toast.makeText(this, "Speed updated", Toast.LENGTH_SHORT).show()

                    goToOldSessionView(sessionId)

                } else {
                    Toast.makeText(this, "Recheck speed", Toast.LENGTH_SHORT).show()
                }

            }

        }
    }

    // takes user back to session view
    private fun goToOldSessionView(sessionId: Long) {
        val intent = Intent(this, ViewOldSessionActivity::class.java)
        intent.putExtra(C.OLD_SESSION_ID, sessionId)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
        finish()
    }
}

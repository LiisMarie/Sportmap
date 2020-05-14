package ee.taltech.likutt.iti0213_2019s_hw02.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import ee.taltech.likutt.iti0213_2019s_hw02.*
import ee.taltech.likutt.iti0213_2019s_hw02.database.Repository
import ee.taltech.likutt.iti0213_2019s_hw02.helpers.C
import kotlinx.android.synthetic.main.activity_menu.*

class MenuActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        setOnClickListeners()
    }

    // sets onclicklisteners for the buttons
    private fun setOnClickListeners() {
        imageButtonBack.setOnClickListener {
            openMapView()
        }
        buttonHistory.setOnClickListener {
            openHistoryView()
        }
        buttonProfile.setOnClickListener {
            openProfileView()
        }
        buttonSettings.setOnClickListener {
            openSettingsView()
        }
    }

    // opens main activity/map
    private fun openMapView() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
        finish()
    }

    // opens history containing old sessions
    private fun openHistoryView() {
        val intent = Intent(this, HistoryActivity::class.java)
        startActivity(intent)
    }

    // opens user profile view
    private fun openProfileView() {
        val repo = Repository(this).open()
        val user = repo.getUser()

        // if user is logged in then show contact data
        if (user != null) {
            val intent = Intent(this, AccountActivity::class.java)
            startActivity(intent)
        } else {
            // user isnt logged in, display login view
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

    }

    // opens settings view
    private fun openSettingsView() {
        val intent = Intent(this, SettingsActivity::class.java)
        intent.putExtra(C.FROM_WHERE_TO_SETTINGS, "MENU")
        startActivity(intent)
    }
}

package ee.taltech.likutt.iti0213_2019s_hw02

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_settings.*

class SettingsActivity : AppCompatActivity() {
    companion object {
        private val TAG = this::class.java.declaringClass!!.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // TODO...... display current settings
        //  make changing settings possible
        //  you can change syncing interval in options (ala when received, once in 10 sec, once in 30, sec, etc..).
        //  It is possible to change gps update frequency
        //  setting speed for track segment coloring in ACTIVE session

        buttonUpdateSettings.setOnClickListener {
            Log.d(TAG, editTextMinSpeed.text.toString())
            Log.d(TAG, editTextMaxSpeed.text.toString())
            Log.d(TAG, editTextGpsUpdateFrequency.text.toString())
            Log.d(TAG, editTextSyncingInterval.text.toString())

        }
    }

    fun openMenu(view: View) {
        val intent = Intent(this, MenuActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP;
        startActivity(intent)
        finish()
    }
}

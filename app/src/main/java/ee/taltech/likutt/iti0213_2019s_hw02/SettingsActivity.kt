package ee.taltech.likutt.iti0213_2019s_hw02

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_settings.*

class SettingsActivity : AppCompatActivity() {
    companion object {
        private val TAG = this::class.java.declaringClass!!.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val repo = Repository(this).open()
        val settings = repo.getSettings()

        if (settings != null) {
            editTextMinSpeed.setText((settings.minSpeed / 60).toString())
            editTextMaxSpeed.setText((settings.maxSpeed / 60).toString())
            editTextGpsUpdateFrequency.setText((settings.gpsUpdateFrequency / 1000).toString())
            editTextSyncingInterval.setText((settings.syncingInterval / 1000).toString())
        }
        // TODO...... display current settings
        //  you can change syncing interval in options (ala when received, once in 10 sec, once in 30, sec, etc..).
        //  It is possible to change gps update frequency

        buttonUpdateSettings.setOnClickListener {
            Log.d(TAG, editTextMinSpeed.text.toString()) // x > 0
            Log.d(TAG, editTextMaxSpeed.text.toString()) // x > min
            Log.d(TAG, editTextGpsUpdateFrequency.text.toString()) // x > 0
            Log.d(TAG, editTextSyncingInterval.text.toString())  // x > 0

            if (editTextMinSpeed.text.toString() != "" && editTextMaxSpeed.text.toString() != "") {
                // user input is in minutes, backend needs it to be in seconds
                val minSpeed : Double = editTextMinSpeed.text.toString().toDouble() * 60
                val maxSpeed : Double = editTextMaxSpeed.text.toString().toDouble() * 60

                if (Helpers.validateSpeedInput(minSpeed, maxSpeed)) {

                    // todo save in backend for active session
                    repo.deleteSettings()
                    repo.addSettings(minSpeed, maxSpeed, 2000, 2000)

                    Toast.makeText(this, "Settings updated", Toast.LENGTH_SHORT).show()

                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                    startActivity(intent)
                    finish()


                    /*
                    todo in the future
                    if (gpsUpdateFrequency OK) {
                        if (syncingInterval OK) {

                            // todo saving logic when everything is ok
                            // todo if went to settings from map then return to map

                            // FOR gpsUpdateFrequency and syncingInterval
                            // user input * 1000
                            // user input is in seconds, but in backend its stored in milliseconds

                        } else {
                            Toast.makeText(this, "Recheck syncing interval", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this, "Recheck GPS update frequency", Toast.LENGTH_SHORT).show()
                    }
                    */

                } else {
                    Toast.makeText(this, "Recheck speed", Toast.LENGTH_SHORT).show()
                }

            }
        }
    }

    fun openMenu(view: View) {
        val intent = Intent(this, MenuActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
        finish()
    }
}

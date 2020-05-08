package ee.taltech.likutt.iti0213_2019s_hw02

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
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

        var fromWhere = intent.getStringExtra(C.FROM_WHERE_TO_SETTINGS)
        if (fromWhere == null) {
            fromWhere = "HOME"
        }

        imageButtonBack.setOnClickListener {
            if (fromWhere == "MAP") {
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
                finish()
            } else {
                val intent = Intent(this, MenuActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
                finish()
            }
        }

        val repo = Repository(this).open()
        val settings = repo.getSettings()

        if (settings != null) {
            editTextMinSpeed.setText((settings.minSpeed / 60).toString())
            editTextMaxSpeed.setText((settings.maxSpeed / 60).toString())
            editTextGpsUpdateFrequency.setText((settings.gpsUpdateFrequency).toString())
            editTextSyncingInterval.setText((settings.syncingInterval / 1000).toString())
        }

        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            val serv = service.service.className
            Log.d(TAG, "SERVICEserv " + serv)
            if (serv == "ee.taltech.likutt.iti0213_2019s_hw02.LocationService") {
                //todo
            }
        }

        // TODO...... display current settings
        //  you can change syncing interval in options (ala when received, once in 10 sec, once in 30, sec, etc..).
        //  It is possible to change gps update frequency

        buttonUpdateSettings.setOnClickListener {
            Log.d(TAG, editTextMinSpeed.text.toString()) // x > 0
            Log.d(TAG, editTextMaxSpeed.text.toString()) // x > min
            Log.d(TAG, editTextGpsUpdateFrequency.text.toString()) // x > 0  // in milliseconds  MAX = 1s
            Log.d(TAG, editTextSyncingInterval.text.toString())  // x > 0  // in seconds  MAX = 60s

            if (editTextMinSpeed.text.toString() != "" && editTextMaxSpeed.text.toString() != "") {
                // user input is in minutes, backend needs it to be in seconds
                val minSpeed : Double = editTextMinSpeed.text.toString().toDouble() * 60
                val maxSpeed : Double = editTextMaxSpeed.text.toString().toDouble() * 60

                if (Helpers.validateSpeedInput(minSpeed, maxSpeed)) {

                    if (editTextGpsUpdateFrequency.text.toString().toLong() in 2..5000) {

                        repo.deleteSettings()
                        repo.addSettings(minSpeed, maxSpeed, editTextGpsUpdateFrequency.text.toString().toLong(), 2000)

                        // sending changed settings data to LocationService
                        val intent = Intent(C.UPDATE_SETTINGS)
                        intent.putExtra(C.GPS_UPDATE_FRQUENCY, editTextGpsUpdateFrequency.text.toString().toLong())
                        // intent.putExtra(C.SYNCING_INTERVAL, editTextSyncingInterval.text.toString().toLong() * 1000)
                        sendBroadcast(intent)

                        Toast.makeText(this, "Settings updated", Toast.LENGTH_SHORT).show()

                        if (fromWhere == "MAP") {
                            val intent = Intent(this, MainActivity::class.java)
                            startActivity(intent)
                            finishAffinity()  // closes all previous views
                        }
                        /*
                        if (editTextSyncingInterval.text.toString().toLong() * 1000 <= 60000) {

                            // todo saving logic when everything is ok
                            // todo if went to settings from map then return to map

                            // FOR gpsUpdateFrequency and syncingInterval
                            // user input * 1000
                            // user input is in seconds, but in backend its stored in milliseconds

                        } else {
                            Toast.makeText(this, "Recheck syncing interval", Toast.LENGTH_SHORT).show()
                        }*/
                    } else {
                        Toast.makeText(this, "Recheck GPS update frequency", Toast.LENGTH_SHORT).show()
                    }


                } else {
                    Toast.makeText(this, "Recheck speed", Toast.LENGTH_SHORT).show()
                }

            }
        }
    }

}

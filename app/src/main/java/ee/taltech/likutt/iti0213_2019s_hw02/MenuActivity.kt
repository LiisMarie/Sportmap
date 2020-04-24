package ee.taltech.likutt.iti0213_2019s_hw02

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View

class MenuActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)
    }

    fun openMapView(view: View) {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    fun openHistoryView(view: View) {
        val intent = Intent(this, HistoryActivity::class.java)
        startActivity(intent)
    }

    fun openProfileView(view: View) {}

    fun openSettingsView(view: View) {}
}
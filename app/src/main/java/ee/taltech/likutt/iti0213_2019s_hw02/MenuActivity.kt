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
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP;
        startActivity(intent)
        finish()
    }

    fun openHistoryView(view: View) {
        val intent = Intent(this, HistoryActivity::class.java)
        startActivity(intent)
    }

    fun openProfileView(view: View) {
        val repo = Repository(this).open()
        val user = repo.getUser()
        if (user != null) {  // if user is logged in then show contact data
            val intent = Intent(this, AccountActivity::class.java)
            startActivity(intent)
        } else {  // user isnt logged in, display login view
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

    }

    fun openSettingsView(view: View) {
        val intent = Intent(this, SettingsActivity::class.java)
        intent.putExtra(C.FROM_WHERE_TO_SETTINGS, "MENU")
        startActivity(intent)
    }
}

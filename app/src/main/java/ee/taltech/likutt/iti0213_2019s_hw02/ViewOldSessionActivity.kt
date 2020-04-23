package ee.taltech.likutt.iti0213_2019s_hw02

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity

class ViewOldSessionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_old_session)
    }

    fun openHistoryView(view: View) {
        val intent = Intent(this, HistoryActivity::class.java)
        startActivity(intent)
    }
}

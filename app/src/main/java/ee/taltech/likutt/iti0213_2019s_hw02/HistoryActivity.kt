package ee.taltech.likutt.iti0213_2019s_hw02

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_history.*


class HistoryActivity : AppCompatActivity() {

    private lateinit var repo: Repository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        repo = Repository(this).open()

        // layoutManager - hoolitseb kuidas rowsid vahetada
        recyclerViewOldSessions.layoutManager = LinearLayoutManager(this)
        // adapter - joonistab asja välja (tuleb ise luua)
        recyclerViewOldSessions.adapter = DataRecyclerViewAdapterSessions(this, repo.getAllSessions())

        setOnClickListeners()
    }

    private fun setOnClickListeners() {
        imageButtonBack.setOnClickListener {
            val intent = Intent(this, MenuActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
        }
    }

}

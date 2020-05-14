package ee.taltech.likutt.iti0213_2019s_hw02.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import ee.taltech.likutt.iti0213_2019s_hw02.database.DataRecyclerViewAdapterSessions
import ee.taltech.likutt.iti0213_2019s_hw02.R
import ee.taltech.likutt.iti0213_2019s_hw02.database.Repository
import kotlinx.android.synthetic.main.activity_history.*


class HistoryActivity : AppCompatActivity() {

    private lateinit var repo: Repository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        repo = Repository(this).open()

        // layoutManager - takes care of changing rows
        recyclerViewOldSessions.layoutManager = LinearLayoutManager(this)
        // adapter - draws the view
        recyclerViewOldSessions.adapter = DataRecyclerViewAdapterSessions(this, repo.getAllSessions())

        setOnClickListeners()
    }

    private fun setOnClickListeners() {
        // takes user back to the menu
        imageButtonBack.setOnClickListener {
            val intent = Intent(this, MenuActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
        }
    }

}

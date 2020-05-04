package ee.taltech.likutt.iti0213_2019s_hw02

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_account.*

class AccountActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account)

        val repo = Repository(this).open()
        val user = repo.getUser()
        if (user != null) {
            textViewEmail.text = user.email
        } else {
            textViewEmail.text = "NO USER AVAILABLE"
        }

        buttonLogOut.setOnClickListener {
            repo.deleteUser()

            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finishAffinity()  // closes all previous views
        }
    }

    fun openMenu(view: View) {
        val intent = Intent(this, MenuActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP;
        startActivity(intent)
        finish()
    }
}

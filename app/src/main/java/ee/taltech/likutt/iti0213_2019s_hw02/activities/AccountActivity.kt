package ee.taltech.likutt.iti0213_2019s_hw02.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import ee.taltech.likutt.iti0213_2019s_hw02.*
import ee.taltech.likutt.iti0213_2019s_hw02.api.WebApiSingletonHandler
import ee.taltech.likutt.iti0213_2019s_hw02.database.Repository
import ee.taltech.likutt.iti0213_2019s_hw02.helpers.C
import kotlinx.android.synthetic.main.activity_account.*
import org.json.JSONObject

class AccountActivity : AppCompatActivity() {

    companion object {
        private val TAG = this::class.java.declaringClass!!.simpleName
    }

    private lateinit var repo: Repository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account)

        repo = Repository(this).open()

        // get current user from repo and display data
        val user = repo.getUser()
        if (user != null) {
            editTextEmail.setText(user.email)
            editTextFirstName.setText(user.firstName)
            editTextLastName.setText(user.lastName)
        }

        setOnClickListeners()
    }

    // sets onclicklisteners on buttons
    private fun setOnClickListeners() {
        // logout button logs out current user and deletes his data from the database
        buttonLogOut.setOnClickListener {
            repo.deleteUser()

            // displays login activity
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
        }

        // button for updating user data
        buttonUpdateData.setOnClickListener {
            // validation of inputs
            if (editTextFirstName.text.toString() != "" && editTextLastName.text.toString() != "" && editTextEmail.text.toString() != "" && editTextPassword.text.toString() != "") {
                if (editTextPassword.text.toString() == editTextRepeatPassword.text.toString()) {
                    // if inputs are valid, go to create user
                    createUser(editTextFirstName.text.toString(), editTextLastName.text.toString(), editTextEmail.text.toString(), editTextPassword.text.toString())
                } else {
                    Toast.makeText(this, "Provided passwords don't match!", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Not all fields set!", Toast.LENGTH_SHORT).show()
            }
        }

        // takes user back to menu
        imageButtonBack.setOnClickListener {
            val intent = Intent(this, MenuActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
        }
    }

    // method for creating an account for user in backend
    private fun createUser(firstName: String, lastName: String, email: String, password: String) {
        Log.d(TAG, "createUser")

        val handler = WebApiSingletonHandler.getInstance(applicationContext)
        val requestJsonParams = JSONObject()
        requestJsonParams.put("firstName", firstName)
        requestJsonParams.put("lastName", lastName)
        requestJsonParams.put("email", email)
        requestJsonParams.put("password", password)

        val httpRequest = JsonObjectRequest(
                Request.Method.POST,
                C.REST_BASE_URL + "account/register",
                requestJsonParams,
                Response.Listener { response ->
                    Log.d(TAG, response.toString())

                    val repo = Repository(this).open()
                    repo.deleteUser()
                    repo.addUser(email, password, firstName, lastName)

                    val intent = Intent(this, AccountActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                    startActivity(intent)
                    finish()

                },
                Response.ErrorListener { error ->
                    Log.d(TAG, error.toString())
                    Toast.makeText(this, "Couldn't update data, try again!", Toast.LENGTH_SHORT).show()
                }
        )

        handler.addToRequestQueue(httpRequest)
    }

}

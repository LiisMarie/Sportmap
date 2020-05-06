package ee.taltech.likutt.iti0213_2019s_hw02

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import kotlinx.android.synthetic.main.activity_login.*
import org.json.JSONObject

class LoginActivity : AppCompatActivity() {

    companion object {
        private val TAG = this::class.java.declaringClass!!.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        setOnClickListeners()
    }

    private fun setOnClickListeners() {
        buttonRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
        }

        buttonLogin.setOnClickListener {
            if (editTextEmail.text.toString() != "" && editTextPassword.text.toString() != "") {
                hideKeyboard()
                loginUser(editTextEmail.text.toString(), editTextPassword.text.toString())
            } else {
                Toast.makeText(this, "Not all fields set!", Toast.LENGTH_SHORT).show()
            }
        }

        imageButtonBack.setOnClickListener {
            val intent = Intent(this, MenuActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
        }
    }

    private fun loginUser(email: String, password: String) {
        Log.d(TAG, "loginUser")
        val handler = WebApiSingletonHandler.getInstance(applicationContext)
        val requestJsonParams = JSONObject()
        requestJsonParams.put("email", email)
        requestJsonParams.put("password", password)

        val httpRequest = JsonObjectRequest(
                Request.Method.POST,
                C.REST_BASE_URL + "account/login",
                requestJsonParams,
                Response.Listener { response ->
                    Log.d(TAG, response.toString())

                    Log.d(TAG, "TOKEN  " + response.getString("token"))

                    val repo = Repository(this).open()
                    repo.deleteUser()
                    repo.addUser(email, password, response.getString("firstName"), response.getString("lastName"))

                    val intent = Intent(this, AccountActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                    startActivity(intent)
                    finish()
                },
                Response.ErrorListener { error ->
                    Log.d(TAG, error.toString())
                    Toast.makeText(this, "Couldn't log in!", Toast.LENGTH_SHORT).show()
                }
        )

        handler.addToRequestQueue(httpRequest)
    }

    fun Fragment.hideKeyboard() {
        view?.let { activity?.hideKeyboard(it)}
    }

    private fun Activity.hideKeyboard() {
        hideKeyboard(currentFocus ?: View(this))
    }

    private fun Context.hideKeyboard(view: View) {
        val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }
}

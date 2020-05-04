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
import kotlinx.android.synthetic.main.activity_register.*
import org.json.JSONObject

class RegisterActivity : AppCompatActivity() {

    companion object {
        private val TAG = this::class.java.declaringClass!!.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        buttonLogin.setOnClickListener {
            var intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP;
            startActivity(intent)
            finish()
        }

        buttonRegister.setOnClickListener {
            if (editTextFirstName.text.toString() != "" && editTextLastName.text.toString() != "" && editTextEmail.text.toString() != "" && editTextPassword.text.toString() != "") {
                hideKeyboard()
                createUser(editTextFirstName.text.toString(), editTextLastName.text.toString(), editTextEmail.text.toString(), editTextPassword.text.toString())
            } else {
                Toast.makeText(this, "Not all fields set!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun createUser(firstName: String, lastName: String, email: String, password: String) {
        Log.d(TAG, "createUser")
        var handler = WebApiSingletonHandler.getInstance(applicationContext)
        val requestJsonParams = JSONObject()
        requestJsonParams.put("firstName", firstName)
        requestJsonParams.put("lastName", lastName)
        requestJsonParams.put("email", email)
        requestJsonParams.put("password", password)

        var httpRequest = JsonObjectRequest(
                Request.Method.POST,  // mis meetod
                C.REST_BASE_URL + "account/register",  // kuhu kohta
                requestJsonParams,  // body
                Response.Listener { response ->
                    Log.d(TAG, response.toString())

                    Log.d(TAG, "TOKEN " + response.getString("token"))  // küsitav väli

                    val repo = Repository(this).open()
                    repo.deleteUser()
                    repo.addUser(email, password, firstName, lastName)
                    repo.close()

                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP;
                    startActivity(intent)
                    finish()

                },  // kõik läks hästi, võtab tulemused vastu
                Response.ErrorListener { error ->
                    Log.d(TAG, error.toString())
                    Toast.makeText(this, "Couldn't make account, try again!", Toast.LENGTH_SHORT).show()
                }  // ei läinud hästi
        )

        handler.addToRequestQueue(httpRequest)
    }

    fun Fragment.hideKeyboard() {
        view?.let { activity?.hideKeyboard(it)}
    }

    fun Activity.hideKeyboard() {
        hideKeyboard(currentFocus ?: View(this))
    }

    fun Context.hideKeyboard(view: View) {
        val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }
}

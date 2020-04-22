package ee.taltech.likutt.iti0213_2019s_hw02

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import kotlinx.android.synthetic.main.activity_history.*
import org.json.JSONObject


class HistoryActivity : AppCompatActivity() {
    companion object {
        private val TAG = this::class.java.declaringClass!!.simpleName
    }
    private var mJwt: String? = null

    private var oldSessions = mutableListOf<JSONObject>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        getRestToken()
    }

    private fun getAllSessions() {
        Log.d(TAG, "getAllSessions")
        var handler = WebApiSingletonHandler.getInstance(applicationContext)

        var httpRequest = JsonArrayRequest(
            Request.Method.GET,
            C.REST_BASE_URL + "GpsSessions",
            null,

            Response.Listener { response ->
                Log.d(TAG, "All gps sessions: " + response.toString())
                var i = 0
                while (i < response.length()) {
                    Log.d(TAG, "sessssiooon: " + response[i])
                    oldSessions.add(response[i] as JSONObject)
                    i++
                }

                /*
                i = 0
                while (i < oldSessions.size) {
                    Log.d(TAG, " \nid: " + oldSessions[i].getString("id"))
                    Log.d(TAG, " \nname: " + oldSessions[i].getString("name"))
                    Log.d(TAG, " \ndescription: " + oldSessions[i].getString("description"))
                    Log.d(TAG, " \nrecordedAt: " + oldSessions[i].getString("recordedAt"))
                    Log.d(TAG, " \nduration: " + oldSessions[i].getLong("duration"))
                    Log.d(TAG, " \nspeed: " + oldSessions[i].getLong("speed"))
                    Log.d(TAG, " \ndistance: " + oldSessions[i].getLong("distance"))


                    i++
                }*/

                // layoutManager - hoolitseb kuidas rowsid vahetada
                recyclerViewOldSessions.layoutManager = LinearLayoutManager(this)
                // adapter - joonistab asja välja (tuleb ise luua)
                recyclerViewOldSessions.adapter = DataRecyclerViewAdapterSessions(this, oldSessions)

            },
            Response.ErrorListener { error ->
                Log.d(TAG, "ERROR: " + error.toString())
            }
        )

        handler.addToRequestQueue(httpRequest)
    }

    private fun getRestToken() {
        Log.d(TAG, "getRestToken")
        var handler = WebApiSingletonHandler.getInstance(applicationContext)

        val requestJsonParameters = JSONObject()
        requestJsonParameters.put("email", C.REST_USERNAME)
        requestJsonParameters.put("password", C.REST_PASSWORD)

        var httpRequest = JsonObjectRequest(
            Request.Method.POST,
            C.REST_BASE_URL + "account/login",
            requestJsonParameters,
            Response.Listener { response ->
                Log.d(TAG, "DATABASE RESPONSE: " + response.toString())
                mJwt = response.getString("token")

                getAllSessions()
            },
            Response.ErrorListener { error ->
                Log.d(TAG, "ERROR: " + error.toString())
            }
        )

        handler.addToRequestQueue(httpRequest)
    }

    fun openMenu(view: View) {
        val intent = Intent(this, MenuActivity::class.java)
        startActivity(intent)
    }
}

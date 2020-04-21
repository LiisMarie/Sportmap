import android.content.Context
import android.text.TextUtils
import android.util.Log
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley

class WebApiSingletonHandler {

    companion object {
        private val TAG = this::class.java.declaringClass!!.simpleName
        // keeping reference, so that if it exists then new one wont be created and old one will be returned
        private var instance: WebApiSingletonHandler? = null
        private var mContext: Context? = null

        @Synchronized
        fun getInstance(context: Context): WebApiSingletonHandler {
            if (instance == null) {
                instance = WebApiSingletonHandler(context)
            }
            return instance!!
        }
    }

    private constructor(context: Context) {
        mContext = context
    }

    // üks päringute järjekord, mida hakatakse järjest täitma
    private var requestQueue: RequestQueue? = null
    get() {
        if (field == null) {
            field = Volley.newRequestQueue(mContext)
        }
        return null
    }

    // takes in requests
    fun <T> addToRequestQueue(request: Request<T>, tag: String? = null) {
        Log.d(TAG, request.url)
        request.tag = if (tag == null || TextUtils.isEmpty(tag)) TAG else tag
        requestQueue?.add(request)
    }

    fun cancelPendingRequests(tag: String? = null) {
        if (requestQueue != null) {
            requestQueue!!.cancelAll(if (tag == null || TextUtils.isEmpty(tag)) TAG else tag)
        }
    }
}
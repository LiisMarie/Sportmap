package ee.taltech.likutt.iti0213_2019s_hw02

import android.content.Context
import android.text.TextUtils
import android.util.Log
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley

class WebApiSingletonHandler {
    companion object {
        private val TAG = this::class.java.declaringClass!!.simpleName
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

    constructor(context: Context) {
        mContext = context
    }

    private var requestQueue: RequestQueue? = null
        get() {
            if (field == null) {
                field = Volley.newRequestQueue(mContext)
            }
            return field
        }

    fun <T> addToRequestQueue(request: Request<T>, tag: String) {
        Log.d(TAG, request.url)
        request.tag = if (TextUtils.isEmpty(tag)) TAG else tag
        requestQueue?.add(request)
    }

    fun <T> addToRequestQueue(request: Request<T>) {
        Log.d(TAG, request.url)
        request.tag = TAG
        requestQueue?.add(request)
    }

    fun cancelPendingRequest(tag: String) {
        if (requestQueue != null) {

            requestQueue!!.cancelAll(if (TextUtils.isEmpty(tag)) TAG else tag)
        }
    }
}
package com.yusuffahrudin.masuyamobileapp.firebase

import android.content.Context
import android.util.Log
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.yusuffahrudin.masuyamobileapp.R
import com.yusuffahrudin.masuyamobileapp.controller.AppController
import com.yusuffahrudin.masuyamobileapp.util.Server
import org.json.JSONException

class SendNotification {
    fun pushNotif(context: Context, kdkota: String, title: String, message: String, topic: String, level: String){
        val a = Server(kdkota)
        val url = a.URL() + "tools/send_push_notification.php"

        val strReq = object : StringRequest(Method.POST, url, Response.Listener { response ->
            try {
                Log.d("SEND_NOTIF", response)
            } catch (e: JSONException) {
                e.printStackTrace()
                Log.e("SEND_NOTIF", e.toString())
            }
        }, Response.ErrorListener { error ->
            error.printStackTrace()
            Log.e("SEND_NOTIF", error.toString())
        }) {
            override fun getParams(): Map<String, String> {
                val params = HashMap<String, String>()
                params["title"] = title
                params["message"] = message
                params["topic"] = topic
                params["level"] = level
                println("================ level "+level)
                return params
            }
        }
        strReq.retryPolicy = DefaultRetryPolicy(0, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        AppController.getInstance().addToRequestQueue(strReq, context.resources.getString(R.string.tag_json_obj))
    }

    fun pushNotifSales(context: Context, kdkota: String, title: String, message: String, topic: String, sales: String){
        val a = Server(kdkota)
        val url = a.URL() + "tools/send_push_notification_sales.php"

        val strReq = object : StringRequest(Method.POST, url, Response.Listener { response ->
            try {
                Log.d("SEND_NOTIF", response)
            } catch (e: JSONException) {
                e.printStackTrace()
                Log.e("SEND_NOTIF", e.toString())
            }
        }, Response.ErrorListener { error ->
            error.printStackTrace()
            Log.e("SEND_NOTIF", error.toString())
        }) {
            override fun getParams(): Map<String, String> {
                val params = HashMap<String, String>()
                params["title"] = title
                params["message"] = message
                params["topic"] = topic
                params["sales"] = sales
                println("================ sales "+sales)
                return params
            }
        }
        strReq.retryPolicy = DefaultRetryPolicy(0, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        AppController.getInstance().addToRequestQueue(strReq, context.resources.getString(R.string.tag_json_obj))
    }
}
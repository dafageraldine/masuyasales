package com.yusuffahrudin.masuyamobileapp.salesorder

import android.app.ProgressDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.yusuffahrudin.masuyamobileapp.R
import com.yusuffahrudin.masuyamobileapp.controller.AppController
import com.yusuffahrudin.masuyamobileapp.controller.DialogAlert
import com.yusuffahrudin.masuyamobileapp.data.ArrayTampung
import com.yusuffahrudin.masuyamobileapp.data.sales_order.SalesOrder
import com.yusuffahrudin.masuyamobileapp.util.Server
import com.yusuffahrudin.masuyamobileapp.util.SessionManager
import org.json.JSONException
import org.json.JSONObject
import java.util.*
import kotlin.collections.HashMap
import kotlin.collections.set

class SignatureFragment: Fragment() {
    private lateinit var sessionManager: SessionManager
    private lateinit var kdkota: String
    private lateinit var kdcust: String
    private lateinit var imageSignature: ImageView
    private lateinit var listHeader: ArrayList<SalesOrder>
    private lateinit var nobukti: String

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_signature, container, false)

        sessionManager = SessionManager(context)
        val user = sessionManager.userDetails
        kdkota = user[SessionManager.kdkota].toString()
        listHeader = ArrayTampung.getListHeaderSO()

        imageSignature = rootView.findViewById(R.id.img_signature)
        val i = this.activity!!.intent
        nobukti = i.extras?.getString("nobukti").toString()

        selectSalesOrder()

        return rootView
    }

    private fun setSignature() {
        if  (kdcust != null || kdcust == ""){
            val a = Server(kdkota)

            Glide.with(requireContext())
                    .load(a.URL() + "salesorder/signature/$nobukti-$kdcust.jpg")
                    .apply(RequestOptions()
                            .placeholder(R.mipmap.ic_launcher)
                            .error(R.drawable.img_not_found)
                            .override(300, 400)
                            .fitCenter())
                    .into(imageSignature)
        }
    }

    //fungsi untuk select data dari database
    fun selectSalesOrder() {
        listHeader.clear()
        val a = Server(kdkota)
        val urlSelect = a.URL() + "salesorder/select_so_header.php"

        val progressDialog = ProgressDialog.show(this.context, "", "Please Wait...")
        object : Thread() {
            override fun run() {
                try { sleep(10000) }
                catch (e: Exception) { Log.e("tag", e.message.toString()) }
            }
        }.start()

        val strReq = object : StringRequest(Method.POST, urlSelect, Response.Listener { response ->
            try {
                Log.d(this.tag, "Response : $response")
                val jsonObject = JSONObject(response)
                val result = jsonObject.getJSONArray("result")
                for (i in 0 until result.length()) {
                    val obj = result.getJSONObject(i)
                    kdcust = obj.getString("KdCust")
                    setSignature()
                }
            } catch (e: JSONException) {
                e.printStackTrace()
                FirebaseCrashlytics.getInstance().recordException(e)
            }
            // dismiss the progress dialog
            progressDialog.dismiss()
        }, Response.ErrorListener { error ->
            DialogAlert(error.message.toString(), "error", this.requireActivity())
            FirebaseCrashlytics.getInstance().recordException(error)
            // dismiss the progress dialog
            progressDialog.dismiss()
        }) {
            override fun getParams(): Map<String, String> {
                //posting parameter ke post url
                val params = HashMap<String, String>()
                params["no_order"] = nobukti
                return params
            }
        }
        AppController.getInstance().addToRequestQueue(strReq, resources.getString(R.string.tag_json_obj))

    }
}
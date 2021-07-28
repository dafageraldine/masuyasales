package com.yusuffahrudin.masuyamobileapp

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.telephony.TelephonyManager
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.AuthFailureError
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.iid.FirebaseInstanceId.getInstance
import com.yusuffahrudin.masuyamobileapp.api.API
import com.yusuffahrudin.masuyamobileapp.api.ApiService
import com.yusuffahrudin.masuyamobileapp.controller.AppController
import com.yusuffahrudin.masuyamobileapp.controller.DialogAlert
import com.yusuffahrudin.masuyamobileapp.data.CekVersiResponse
import com.yusuffahrudin.masuyamobileapp.data.UserAkses
import com.yusuffahrudin.masuyamobileapp.databinding.ActivityLoginBinding
import com.yusuffahrudin.masuyamobileapp.util.AESUtil
import com.yusuffahrudin.masuyamobileapp.util.DataHelper
import com.yusuffahrudin.masuyamobileapp.util.Server
import com.yusuffahrudin.masuyamobileapp.util.SessionManager
import org.jetbrains.anko.toast
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import java.util.*

class LoginActivity : AppCompatActivity() {
    internal var success: Int = 0
    internal lateinit var user: String
    internal lateinit var pass: String
    internal lateinit var message: String
    internal lateinit var level: String
    internal lateinit var kota: String
    internal lateinit var kdkota: String
    internal lateinit var underCost: String
    internal lateinit var underBottomSP: String
    internal lateinit var underBottomSO: String
    internal lateinit var merk: String
    internal lateinit var model: String
    internal var sdk: Int = 0
    internal lateinit var androidVersi: String
    internal lateinit var apkVersi: String
    internal lateinit var carrier: String
    internal lateinit var intent: Intent
    internal lateinit var sessionManager: SessionManager
    private lateinit var adapterkota: ArrayAdapter<String>
    private lateinit var db: DataHelper
    private var exit: Boolean? = false
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private val idFirebase = "1001"
    private lateinit var token: String
    private lateinit var binding: ActivityLoginBinding

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        FirebaseApp.initializeApp(this)
        firebaseAnalytics = FirebaseAnalytics.getInstance(this)
        sessionManager = SessionManager(applicationContext)
        db = DataHelper(this)
        val kotaArray = arrayOf("SBY", "MLG", "MKS", "BPN", "SMG", "YGY", "MND", "JKT", "TES")
        adapterkota = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, kotaArray)
        binding.spinKota.adapter = adapterkota

        getInstance().instanceId.addOnSuccessListener( this) { instanceIdResult ->
            token = instanceIdResult.token
            Log.e("newToken", token)
        }

        binding.btnLogin.setOnClickListener {
            binding.progressbar?.visibility = View.VISIBLE
            val bundle = Bundle()
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, idFirebase)
            bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "Press button login")
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle)

            getInfo()
            logIn()
        }
    }

    private fun cekversiandroid() {
        val a = Server(kdkota)
        var link = a.URL() + "tools/cek_versi.php"





//        val params = HashMap<String?, String?>()
//        params["versi"] = applicationVersionName()
////        params["versi"] = "2.56.88"
//        API.instance().create(ApiService::class.java)
//            .versioncheck(params)?.enqueue(object : Callback<CekVersiResponse?> {
//                override fun onResponse(
//                    call: Call<CekVersiResponse?>,
//                    response: retrofit2.Response<CekVersiResponse?>
//                ) {
//                    println("======cek versi dulu==========")
//                    println(response.body()?.result?.get(0)?.pesan.toString())
//                    val pesan = response.body()?.result?.get(0)?.pesan.toString()
//                    println("======selesai cek versi==========")
//                    if (pesan == "latest"){
//                        println("sudah update")
////                        sessionManager.checkLogin()
////                        finish()
//
//                    } else if (pesan == "silahkan update aplikasi anda !"){
//                        DialogAlert(pesan, "error", this@LoginActivity)
//                        println("belum update")
////                        finish()
//                    }
//                    else{
////                        sessionManager.checkLogin()
////                        finish()
//                        DialogAlert("error API", "error", this@LoginActivity)
////                        println("error API")
//                    }
//                }
//
//                override fun onFailure(call: Call<CekVersiResponse?>, t: Throwable) {
//                    DialogAlert(t.message, "error", this@LoginActivity)
//                }
//
//            })
    }

    private fun getInfo(){
        val tm = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        merk = Build.MANUFACTURER
        model = Build.MODEL
        sdk = Build.VERSION.SDK_INT
        androidVersi = Build.VERSION.RELEASE
        apkVersi = applicationVersionName()
        carrier = tm.networkOperatorName
    }

    private fun logIn() {
        user = binding.edtUsername.text.toString()
        pass = binding.edtPassword.text.toString()
        kdkota = binding.spinKota.selectedItem.toString().toLowerCase(Locale.getDefault())
        val a = Server(kdkota)
        url_login = a.URL() + "tools/login.php"
        Log.v(TAG, "url login : $url_login")

        val strReq = object : StringRequest(Method.POST, url_login, Response.Listener { response ->
            Log.d(TAG, "Response : $response")

            try {
                val jObj = JSONObject(response)
                success = jObj.getInt(TAG_SUCCESS)
                message = jObj.getString(TAG_MESSAGE)
                level = jObj.getString("Level")
                kota = jObj.getString("KdKota")
                underCost = jObj.getString("UnderCost")
                underBottomSP = jObj.getString("UnderBottomSP")
                underBottomSO = jObj.getString("UnderBottomSO")
                // dismiss the progress dialog

                //cek error node pada JSON
                if (success == 1) {
                    cekAkses()
                } else {
                    DialogAlert(message, "error", this@LoginActivity)
                    binding.progressbar?.visibility = View.GONE
                }
            } catch (e: JSONException) {
                e.printStackTrace()
                DialogAlert(e.message.toString(), "error", this@LoginActivity)
                binding.progressbar?.visibility = View.GONE
            }
        }, Response.ErrorListener { error ->
            error.printStackTrace()
            DialogAlert(error.message.toString(), "error", this@LoginActivity)
            binding.progressbar?.visibility = View.GONE
        }) {

            override fun getParams(): Map<String, String> {
                //Posting parameter ke post url
                val params = HashMap<String, String>()
                params["user"] = user
                params["pass"] = pass
                params["passEnkrip"] = AESUtil.encrypt(pass)
                params["merk"] = merk
                params["model"] = model
                params["sdk"] = sdk.toString()
                params["android"] = androidVersi
                params["versi"] = apkVersi
                params["carrier"] = carrier
                params["token"] = token

                return params
            }
        }
        strReq.retryPolicy = DefaultRetryPolicy(0, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        AppController.getInstance().addToRequestQueue(strReq, resources.getString(R.string.tag_json_obj))
    }

    override fun onBackPressed() {
        if (exit!!) {
            finish()
        } else {
            toast("Press Back again to Exit..")
            exit = true
            Handler().postDelayed({ exit = false }, (3 * 1000).toLong())
        }

    }

    companion object {
        private val TAG = LoginActivity::class.java.simpleName
        private var url_login: String? = null
        private const val TAG_SUCCESS = "success"
        private const val TAG_MESSAGE = "message"
    }

    private fun cekAkses() {
        val a = Server(kdkota)
        val urlAkses = a.URL() + "tools/cek_akses.php"
        Log.v(TAG, "url cek akses : $urlAkses")
        val strReq = object : StringRequest(Method.POST, urlAkses, Response.Listener { response ->
            Log.d(::HomeActivity.name, "Response : $response")
            try {
                db.insertDB()
                val jsonObject = JSONObject(response)
                val result = jsonObject.getJSONArray("result")
                for (i in 0 until result.length()) {
                    try {
                        val obj = result.getJSONObject(i)
                        Log.v("akses ", obj.toString())
                        val item = UserAkses()
                        item.modul = obj.getString("Modul")
                        item.akses = obj.getInt("Akses")
                        item.add = obj.getInt("Add")
                        item.edit = obj.getInt("Edit")
                        item.delete = obj.getInt("Delete")
                        item.post = obj.getInt("Post")
                        //menambah item ke array
                        db.addUser(item)
                    } catch (e: JSONException) { e.printStackTrace() }
                }
                db.setTransactionSuccess()
            }
            catch (e: JSONException) {
                e.printStackTrace()
                binding.progressbar?.visibility = View.GONE
            } finally {
                db.closeDB()
                sessionManager.createSession(user, level, kota, kdkota, underCost, underBottomSP, underBottomSO)
                binding.progressbar?.visibility = View.GONE
                intent = Intent(this@LoginActivity, HomeActivity::class.java)
                intent.putExtra("pass", pass)
                startActivity(intent)
                finish()
            }
        }, Response.ErrorListener { toast(message) }) {
            @Throws(AuthFailureError::class)
            override fun getParams(): Map<String, String> {
                //Posting parameter ke post url
                val params = HashMap<String, String>()
                params["level"] = level
                return params
            }
        }
        AppController.getInstance().addToRequestQueue(strReq, resources.getString(R.string.tag_json_obj))
    }

    //Programmatically get the current version Name
    private fun applicationVersionName(): String{
        val packageInfo = packageManager.getPackageInfo(packageName, 0)
        return packageInfo.versionName
    }

}

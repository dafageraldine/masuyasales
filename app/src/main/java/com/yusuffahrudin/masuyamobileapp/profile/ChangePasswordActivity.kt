package com.yusuffahrudin.masuyamobileapp.profile

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.yusuffahrudin.masuyamobileapp.R
import com.yusuffahrudin.masuyamobileapp.controller.AppController
import com.yusuffahrudin.masuyamobileapp.controller.DialogAlert
import com.yusuffahrudin.masuyamobileapp.util.AESUtil
import com.yusuffahrudin.masuyamobileapp.util.Server
import com.yusuffahrudin.masuyamobileapp.util.SessionManager
import org.json.JSONException
import org.json.JSONObject

class ChangePasswordActivity: AppCompatActivity() {
    private lateinit var edtOldPass: EditText
    private lateinit var edtNewPass: EditText
    private lateinit var edtConfirmPass: EditText
    private lateinit var user: String
    private lateinit var pass: String
    private lateinit var kdkota: String
    private lateinit var imm: InputMethodManager
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_password)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        if (supportActionBar != null) {
            supportActionBar!!.setDisplayShowHomeEnabled(true)
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        }
        title = resources.getString(R.string.change_password)
        sessionManager = SessionManager(this)
        val session = sessionManager.userDetails
        kdkota = session[SessionManager.kdkota].toString()

        val i = intent
        user = i.extras?.getString("user").toString()
        pass = i.extras?.getString("pass").toString()
        edtOldPass = findViewById(R.id.edt_old_pass)
        edtNewPass = findViewById(R.id.edt_new_pass)
        edtConfirmPass = findViewById(R.id.edt_confirm_pass)
        val btnSave = findViewById<Button>(R.id.btn_simpan)
        imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        btnSave.setOnClickListener { _ ->
            updateData()
        }

        edtOldPass.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                if (edtOldPass.text.toString().equals(pass, false)){
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        edtOldPass.background = getDrawable(R.drawable.edittext_success)
                        edtOldPass.setHintTextColor(resources.getColor(R.color.flatui_emerald))
                    }
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        edtOldPass.background = getDrawable(R.drawable.edittext_error)
                        edtOldPass.setHintTextColor(resources.getColor(R.color.flatui_alizarin))
                    }
                    DialogAlert("incorrect password", "error", this)
                }
            }
            false
        }

        edtOldPass.onFocusChangeListener = object : OnFocusChangeListener {
            override fun onFocusChange(v: View, hasFocus: Boolean) {
                if (!hasFocus) {
                    if (edtOldPass.text.toString().equals(pass, false)){
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            edtOldPass.background = getDrawable(R.drawable.edittext_success)
                            edtOldPass.setHintTextColor(resources.getColor(R.color.flatui_emerald))
                        }
                    } else {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            edtOldPass.background = getDrawable(R.drawable.edittext_error)
                            edtOldPass.setHintTextColor(resources.getColor(R.color.flatui_alizarin))
                        }
                        DialogAlert("incorrect password", "error", this@ChangePasswordActivity)
                    }
                }
            }
        }

        edtConfirmPass.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                if (edtConfirmPass.text.toString().equals(edtNewPass.text.toString(), false)){
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        edtConfirmPass.background = getDrawable(R.drawable.edittext_success)
                        edtConfirmPass.setHintTextColor(resources.getColor(R.color.flatui_emerald))
                    }
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        edtConfirmPass.background = getDrawable(R.drawable.edittext_error)
                        edtConfirmPass.setHintTextColor(resources.getColor(R.color.flatui_alizarin))
                    }
                    DialogAlert("password doesn't match", "error", this)
                }
            }
            false
        }

        edtConfirmPass.onFocusChangeListener = object : OnFocusChangeListener {
            override fun onFocusChange(v: View, hasFocus: Boolean) {
                if (!hasFocus) {
                    if (edtConfirmPass.text.toString().equals(edtNewPass.text.toString(), false)) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            edtConfirmPass.background = getDrawable(R.drawable.edittext_success)
                            edtConfirmPass.setHintTextColor(resources.getColor(R.color.flatui_emerald))
                        }
                    } else if (edtConfirmPass.text.toString().equals("")){
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            edtConfirmPass.background = getDrawable(R.drawable.edittext_round)
                            edtConfirmPass.setHintTextColor(resources.getColor(R.color.colorAccent))
                        }
                    }else {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            edtConfirmPass.background = getDrawable(R.drawable.edittext_error)
                            edtConfirmPass.setHintTextColor(resources.getColor(R.color.flatui_alizarin))
                        }
                        DialogAlert("password doesn't match", "error", this@ChangePasswordActivity)
                    }
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == android.R.id.home) {
            this.finish()
        }

        return super.onOptionsItemSelected(item)
    }

    private fun updateData() {
        val a = Server(kdkota)
        val url = a.URL() + "tools/update_password.php"

        val strReq = object : StringRequest(Method.POST, url, Response.Listener { response ->
            Log.d(localClassName, "Response : $response")

            try {
                val jObj = JSONObject(response)
                val success = jObj.getInt("success")
                val message = jObj.getString("message")

                //cek error node pada JSON
                if (success == 1) {
                    DialogAlert(message, "success-reply", this)
                } else {
                    DialogAlert(message, "error", this)
                }
            } catch (e: JSONException) {
                e.printStackTrace()
                DialogAlert(e.message.toString(), "error", this)
            }
        }, Response.ErrorListener { error ->
            error.printStackTrace()
            DialogAlert(error.message.toString(), "error", this)
        }) {

            override fun getParams(): Map<String, String> {
                //Posting parameter ke post url
                val params = HashMap<String, String>()
                params["user"] = user
                params["pass"] = edtConfirmPass.text.toString()
                params["passEncrypt"] = AESUtil.encrypt(edtConfirmPass.text.toString())

                return params
            }
        }
        strReq.retryPolicy = DefaultRetryPolicy(0, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        AppController.getInstance().addToRequestQueue(strReq, resources.getString(R.string.tag_json_obj))
    }
}
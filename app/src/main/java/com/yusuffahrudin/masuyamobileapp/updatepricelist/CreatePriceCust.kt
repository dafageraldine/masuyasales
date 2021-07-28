package com.yusuffahrudin.masuyamobileapp.updatepricelist

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.MenuItem
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.ViewModelProviders
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.yusuffahrudin.masuyamobileapp.R
import com.yusuffahrudin.masuyamobileapp.controller.AppController
import com.yusuffahrudin.masuyamobileapp.controller.DialogAlert
import com.yusuffahrudin.masuyamobileapp.data.informasi_barang.Product
import com.yusuffahrudin.masuyamobileapp.databinding.InputPricelistBinding
import com.yusuffahrudin.masuyamobileapp.informasibarang.ListBarangActivity
import com.yusuffahrudin.masuyamobileapp.util.Server
import com.yusuffahrudin.masuyamobileapp.util.SessionManager
import org.json.JSONException
import org.json.JSONObject
import java.text.NumberFormat
import java.util.*

/**
 * Created by yusuf fahrudin on 17-05-2017.
 */

class CreatePriceCust : AppCompatActivity() {

    private var success: Int = 0
    private lateinit var kdcust: String
    private lateinit var kdbrgx: String
    private lateinit var message: String
    private lateinit var kdkota: String
    private var jnsPpn = "00"
    private lateinit var sessionManager: SessionManager
    private val nf = NumberFormat.getInstance(Locale("in", "ID"))
    private val REQUEST_BARANG = 1

    companion object {
        private var hpp: Double = 0.0
        private var harga: Double = 0.0
        private var hargaincppn: Double = 0.0
        private var disc1: Double = 0.0
        private var disc2: Double = 0.0
        private var disc3: Double = 0.0

        private val TAG = CreatePriceCust::class.java.simpleName
        private var url_insert: String? = null
        private var url_update: String? = null
        private var url_cek_hpp: String? = null
        private const val TAG_SUCCESS = "success"
        private const val TAG_MESSAGE = "message"
    }
    private lateinit var binding: InputPricelistBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = InputPricelistBinding.inflate(layoutInflater)
        setContentView(binding.root)
        this.title = "Input Pricelist Produk"
        setSupportActionBar(binding.toolbar)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        sessionManager = SessionManager(applicationContext)
        val user = sessionManager.userDetails
        kdkota = user[SessionManager.kdkota].toString()

        kdcust = intent.extras?.getString("kdcust").toString()
        this.title = kdcust
        jnsPpn = intent.extras?.getString("jnsppn").toString()
        kdbrgx = intent.extras?.getString("kdbrg").toString()
        binding.edtKdbrg.setText(intent.extras?.getString("kdbrg").toString())
        binding.edtBrg.setText(intent.extras?.getString("nmbrg").toString())
        binding.edtSatuan.setText(intent.extras?.getString("satuan").toString())
        binding.edtHarga.setText(nf.format(intent.extras?.getDouble("hrg")))
        binding.edtHrgIncPpn.setText(nf.format(intent.extras?.getDouble("hrgincppn")))
        binding.edtDiskon1.setText(nf.format(intent.extras?.getDouble("diskon1")))
        binding.edtDiskon2.setText(nf.format(intent.extras?.getDouble("diskon2")))
        binding.edtDiskon3.setText(nf.format(intent.extras?.getDouble("diskon3")))
        when (jnsPpn) {
            "01" -> binding.edtJenisppn.setText(getString(R.string.string_pppn))
            "02" -> binding.edtJenisppn.setText(getString(R.string.string_pnbkp))
            "03" -> binding.edtJenisppn.setText(getString(R.string.string_pbbs))
        }

        binding.edtHrgIncPpn.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus){
                prosesCek()
            }
        }

        binding.edtHrgIncPpn.doAfterTextChanged {
            try {
                if (TextUtils.isEmpty(it)){
                    binding.edtHrgIncPpn.error = getString(R.string.string_blank)
                    binding.btnSimpan.isEnabled = false
                } else {
                    if (jnsPpn == "01") {
                        var hrg = ubahAngka(binding.edtHrgIncPpn.text.toString()).toDouble() / 1.1
                        binding.edtHarga.setText(nf.format(hrg))

                        hrg = ubahAngka(binding.edtHarga.text.toString()).toDouble()
                        val disc1 = ubahAngka(binding.edtDiskon1.text.toString()).toDouble()
                        val disc2 = ubahAngka(binding.edtDiskon2.text.toString()).toDouble()
                        val disc3 = ubahAngka(binding.edtDiskon3.text.toString()).toDouble()
                        val hrgnet = (((hrg * (1 - (disc1/100))) * (1 - (disc2/100))) * (1 - (disc3/100)))
                        binding.edtHrgnet.setText(nf.format(hrgnet))
                        binding.edtHrgnetIncPpn.setText(nf.format((hrgnet * 1.1)))
                    } else {
                        binding.edtHarga.setText(binding.edtHrgIncPpn.text.toString())
                        val hrg = ubahAngka(binding.edtHarga.text.toString()).toDouble()

                        val disc1 = ubahAngka(binding.edtDiskon1.text.toString()).toDouble()
                        val disc2 = ubahAngka(binding.edtDiskon2.text.toString()).toDouble()
                        val disc3 = ubahAngka(binding.edtDiskon3.text.toString()).toDouble()
                        val hrgnet = (((hrg * (1 - (disc1/100))) * (1 - (disc2/100))) * (1 - (disc3/100)))
                        binding.edtHrgnet.setText(nf.format(hrgnet))
                        binding.edtHrgnetIncPpn.setText(nf.format(hrgnet))
                    }
                    binding.btnSimpan.isEnabled = true
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        binding.edtDiskon1.doAfterTextChanged {
            try {
                if (TextUtils.isEmpty(it)){
                    binding.edtDiskon1.error = getString(R.string.string_blank)
                    binding.btnSimpan.isEnabled = false
                } else {
                    if (jnsPpn == "01") {
                        val hrg = ubahAngka(binding.edtHarga.text.toString()).toDouble()

                        val disc1 = ubahAngka(binding.edtDiskon1.text.toString()).toDouble()
                        val disc2 = ubahAngka(binding.edtDiskon2.text.toString()).toDouble()
                        val disc3 = ubahAngka(binding.edtDiskon3.text.toString()).toDouble()
                        val hrgnet = (((hrg * (1 - (disc1/100))) * (1 - (disc2/100))) * (1 - (disc3/100)))
                        binding.edtHrgnet.setText(nf.format(hrgnet))
                        binding.edtHrgnetIncPpn.setText(nf.format((hrgnet * 1.1)))
                    } else {
                        val hrg = ubahAngka(binding.edtHarga.text.toString()).toDouble()

                        val disc1 = ubahAngka(binding.edtDiskon1.text.toString()).toDouble()
                        val disc2 = ubahAngka(binding.edtDiskon2.text.toString()).toDouble()
                        val disc3 = ubahAngka(binding.edtDiskon3.text.toString()).toDouble()
                        val hrgnet = (((hrg * (1 - (disc1/100))) * (1 - (disc2/100))) * (1 - (disc3/100)))
                        binding.edtHrgnet.setText(nf.format(hrgnet))
                        binding.edtHrgnetIncPpn.setText(nf.format(hrgnet))
                    }
                    binding.btnSimpan.isEnabled = true
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        binding.edtDiskon2.doAfterTextChanged {
            try {
                if (TextUtils.isEmpty(it)){
                    binding.edtDiskon2.error = getString(R.string.string_blank)
                    binding.btnSimpan.isEnabled = false
                } else {
                    if (jnsPpn == "01") {
                        val hrg = ubahAngka(binding.edtHarga.text.toString()).toDouble()

                        val disc1 = ubahAngka(binding.edtDiskon1.text.toString()).toDouble()
                        val disc2 = ubahAngka(binding.edtDiskon2.text.toString()).toDouble()
                        val disc3 = ubahAngka(binding.edtDiskon3.text.toString()).toDouble()
                        val hrgnet = (((hrg * (1 - (disc1/100))) * (1 - (disc2/100))) * (1 - (disc3/100)))
                        binding.edtHrgnet.setText(nf.format(hrgnet))
                        binding.edtHrgnetIncPpn.setText(nf.format((hrgnet * 1.1)))
                    } else {
                        val hrg = ubahAngka(binding.edtHarga.text.toString()).toDouble()

                        val disc1 = ubahAngka(binding.edtDiskon1.text.toString()).toDouble()
                        val disc2 = ubahAngka(binding.edtDiskon2.text.toString()).toDouble()
                        val disc3 = ubahAngka(binding.edtDiskon3.text.toString()).toDouble()
                        val hrgnet = (((hrg * (1 - (disc1/100))) * (1 - (disc2/100))) * (1 - (disc3/100)))
                        binding.edtHrgnet.setText(nf.format(hrgnet))
                        binding.edtHrgnetIncPpn.setText(nf.format(hrgnet))
                    }
                    binding.btnSimpan.isEnabled = true
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        binding.edtDiskon3.doAfterTextChanged {
            try {
                if (TextUtils.isEmpty(it)){
                    binding.edtDiskon3.error = getString(R.string.string_blank)
                    binding.btnSimpan.isEnabled = false
                } else {
                    if (jnsPpn == "01") {
                        val hrg = ubahAngka(binding.edtHarga.text.toString()).toDouble()

                        val disc1 = ubahAngka(binding.edtDiskon1.text.toString()).toDouble()
                        val disc2 = ubahAngka(binding.edtDiskon2.text.toString()).toDouble()
                        val disc3 = ubahAngka(binding.edtDiskon3.text.toString()).toDouble()
                        val hrgnet = (((hrg * (1 - (disc1/100))) * (1 - (disc2/100))) * (1 - (disc3/100)))
                        binding.edtHrgnet.setText(nf.format(hrgnet))
                        binding.edtHrgnetIncPpn.setText(nf.format((hrgnet * 1.1)))
                    } else {
                        val hrg = ubahAngka(binding.edtHarga.text.toString()).toDouble()

                        val disc1 = ubahAngka(binding.edtDiskon1.text.toString()).toDouble()
                        val disc2 = ubahAngka(binding.edtDiskon2.text.toString()).toDouble()
                        val disc3 = ubahAngka(binding.edtDiskon3.text.toString()).toDouble()
                        val hrgnet = (((hrg * (1 - (disc1/100))) * (1 - (disc2/100))) * (1 - (disc3/100)))
                        binding.edtHrgnet.setText(nf.format(hrgnet))
                        binding.edtHrgnetIncPpn.setText(nf.format(hrgnet))
                    }
                    binding.btnSimpan.isEnabled = true
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        binding.edtKdbrg.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                select(binding.edtKdbrg.text.toString().toUpperCase(Locale.getDefault()))
                binding.edtKdbrg.setText(binding.edtKdbrg.text.toString().toUpperCase(Locale.getDefault()))
                selectDataHPP(binding.edtKdbrg.text.toString().toUpperCase(Locale.getDefault()))
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }

        binding.edtDiskon3.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                binding.btnSimpan.performClick()
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }

        binding.btnSearch.setOnClickListener {
            intent = Intent(this, ListBarangActivity::class.java)
            intent.putExtra("kelas", this.localClassName)
            startActivityForResult(intent, REQUEST_BARANG)
        }

        binding.btnSimpan.setOnClickListener {
            if (kdbrgx.isEmpty())
                insert(binding.edtKdbrg.text.toString())
            else
                update(binding.edtKdbrg.text.toString())
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_BARANG && resultCode == RESULT_OK) {
            jnsPpn = data!!.getStringExtra(ListBarangActivity.JNSPPN).toString()
            binding.edtKdbrg.setText(data.getStringExtra(ListBarangActivity.KDBRG))
            binding.edtBrg.setText(data.getStringExtra(ListBarangActivity.NMBRG))
            binding.edtSatuan.setText(data.getStringExtra(ListBarangActivity.SATUAN))
            binding.edtHarga.setText(nf.format(data.getDoubleExtra(ListBarangActivity.HRGEXC, 0.0)))
            binding.edtHrgIncPpn.setText(nf.format(data.getDoubleExtra(ListBarangActivity.HRGINC, 0.0)))
            binding.edtDiskon1.setText("0")
            binding.edtDiskon2.setText("0")
            binding.edtDiskon3.setText("0")
            println("---------------------------- jnsppn $jnsPpn")
            when (jnsPpn) {
                "01" -> binding.edtJenisppn.setText(getString(R.string.string_pppn))
                "02" -> binding.edtJenisppn.setText(getString(R.string.string_pnbkp))
                "03" -> binding.edtJenisppn.setText(getString(R.string.string_pbbs))
            }
            selectDataHPP(binding.edtKdbrg.text.toString().toUpperCase(Locale.getDefault()))
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == android.R.id.home) { this.finish() }
        return super.onOptionsItemSelected(item)
    }

    //region select
    private fun select(kdbrg: String) {
        val model = ViewModelProviders.of(this).get(AddPriceViewModel::class.java)
        model.getBarang(this, kdbrg).observe(this, { brgList: ArrayList<Product> ->
            run {
                for (i in 0 until brgList.size) {
                    jnsPpn = brgList[i].jnsPpn.toString()
                    binding.edtBrg.setText(brgList[i].nmbrg)
                    binding.edtSatuan.setText(brgList[i].satuan)
                    binding.edtHarga.setText(nf.format(brgList[i].hrg))
                    binding.edtHrgIncPpn.setText(nf.format(brgList[i].hrgIncPpn))
                    binding.edtDiskon1.setText("0")
                    binding.edtDiskon2.setText("0")
                    binding.edtDiskon3.setText("0")
                }
                when (jnsPpn) {
                    "01" -> binding.edtJenisppn.setText(getString(R.string.string_pppn))
                    "02" -> binding.edtJenisppn.setText(getString(R.string.string_pnbkp))
                    "03" -> binding.edtJenisppn.setText(getString(R.string.string_pbbs))
                }
            }
        })
    }
    //endregion

    //region selectDataHPP
    private fun selectDataHPP(kdbrg: String) {
        val a = Server(kdkota)
        url_cek_hpp = a.URL() + "updateprice/select_hpp.php"
        val strReq = object : StringRequest(Method.POST, url_cek_hpp, Response.Listener { response ->
            Log.v(TAG, "Response : $response")
            try {
                val jObj = JSONObject(response)
                hpp = jObj.getDouble("hpp")
            } catch (e: JSONException) { e.printStackTrace() }
        }, Response.ErrorListener { e ->
            Log.d("GetDataHPPUpdatePrclist", "Error $e")
            FirebaseCrashlytics.getInstance().recordException(e)
            DialogAlert("${getString(R.string.error_pengambilan_data)} ${e.message}", "error", this@CreatePriceCust)
        }) {
            override fun getParams(): Map<String, String> {
                //Posting parameter ke post url
                val params = HashMap<String, String>()
                params["kdbrg"] = kdbrg
                return params
            }
        }
        AppController.getInstance().addToRequestQueue(strReq, resources.getString(R.string.tag_json_obj))
    }
    //endregion

    //region simpan
    private fun prosesCek() {
        try {
            harga = ubahAngka(binding.edtHarga.text.toString()).toDouble()
            hargaincppn = ubahAngka(binding.edtHrgIncPpn.text.toString()).toDouble()
            disc1 = ubahAngka(binding.edtDiskon1.text.toString()).toDouble()
            disc2 = ubahAngka(binding.edtDiskon2.text.toString()).toDouble()
            disc3 = ubahAngka(binding.edtDiskon3.text.toString()).toDouble()
        } catch (e: Exception) { e.printStackTrace() }

        val hpp50 = (1 * hpp) + hpp

        if (hpp > harga)
            DialogAlert("Harga dibawah HPP", "attention", this@CreatePriceCust)
        else if (hpp50 < harga)
            DialogAlert("Harga 100% diatas HPP", "attention", this@CreatePriceCust)
        else if (disc1 >= 100 || disc2 >= 100 || disc3 >= 100)
            DialogAlert("Diskon 1,2,3 tidak boleh >= 100%", "attention", this@CreatePriceCust)
    }
    //endregion

    //region insert
    fun insert(kdbrg: String) {
        val a = Server(kdkota)
        url_insert = a.URL() + "updateprice/customer/insert_pricelist_produk_cust.php"
        val strReq = object : StringRequest(Method.POST, url_insert, Response.Listener { response ->
            Log.d(TAG, "Response : $response")
            try {
                val jObj = JSONObject(response)
                success = jObj.getInt(TAG_SUCCESS)
                message = jObj.getString(TAG_MESSAGE)
                //cek error node pada JSON
                if (success == 1) { DialogAlert(message, "success-reply", this@CreatePriceCust)
                } else { DialogAlert(message, "error", this@CreatePriceCust) }
            } catch (e: JSONException) { e.printStackTrace() }
        }, Response.ErrorListener { e ->
            Log.d("Insert Pricelist", "Error $e")
            FirebaseCrashlytics.getInstance().recordException(e)
            DialogAlert("${getString(R.string.error_pengambilan_data)} ${e.message}", "error", this@CreatePriceCust)
        }) {
            override fun getParams(): Map<String, String> {
                //posting parameter ke post url
                val params = HashMap<String, String>()
                params["kdcust"] = kdcust
                params["kdbrg"] = kdbrg
                params["satuan"] = binding.edtSatuan.text.toString()
                params["hrg"] = ubahAngka(binding.edtHarga.text.toString())
                params["hrgincppn"] = ubahAngka(binding.edtHrgIncPpn.text.toString())
                params["diskon1"] = ubahAngka(binding.edtDiskon1.text.toString())
                params["diskon2"] = ubahAngka(binding.edtDiskon2.text.toString())
                params["diskon3"] = ubahAngka(binding.edtDiskon3.text.toString())

                return params
            }
        }
        strReq.retryPolicy = DefaultRetryPolicy(0, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        AppController.getInstance().addToRequestQueue(strReq, resources.getString(R.string.tag_json_obj))
    }
    //endregion

    //region update
    fun update(kdbrg: String) {
        val a = Server(kdkota)
        url_update = a.URL() + "updateprice/customer/insert_pricelist_produk_cust.php"
        val strReq = object : StringRequest(Method.POST, url_update, Response.Listener { response ->
            Log.d(TAG, "Response : $response")
            try {
                val jObj = JSONObject(response)
                success = jObj.getInt(TAG_SUCCESS)
                message = jObj.getString(TAG_MESSAGE)
                //cek error node pada JSON
                if (success == 1) { DialogAlert(message, "success-reply", this@CreatePriceCust)
                } else { DialogAlert(message, "error", this@CreatePriceCust) }
            } catch (e: JSONException) { e.printStackTrace() }
        }, Response.ErrorListener { e ->
            Log.d("Update Pricelist", "Error $e")
            FirebaseCrashlytics.getInstance().recordException(e)
            DialogAlert("${getString(R.string.error_pengambilan_data)} ${e.message}", "error", this@CreatePriceCust)
        }) {
            override fun getParams(): Map<String, String> {
                //posting parameter ke post url
                val params = HashMap<String, String>()
                params["kdcust"] = kdcust
                params["kdbrg"] = kdbrg
                params["satuan"] = binding.edtSatuan.text.toString()
                params["hrg"] = ubahAngka(binding.edtHarga.text.toString())
                params["hrgincppn"] = ubahAngka(binding.edtHrgIncPpn.text.toString())
                params["diskon1"] = ubahAngka(binding.edtDiskon1.text.toString())
                params["diskon2"] = ubahAngka(binding.edtDiskon2.text.toString())
                params["diskon3"] = ubahAngka(binding.edtDiskon3.text.toString())

                return params
            }
        }
        strReq.retryPolicy = DefaultRetryPolicy(0, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        AppController.getInstance().addToRequestQueue(strReq, resources.getString(R.string.tag_json_obj))
    }
    //endregion

    //region ubahAngka
    private fun ubahAngka(angka: String): String {
        val buangRibuan = angka.replace(".", "")
        return buangRibuan.replace(",", ".")
    }
    //endregion
}

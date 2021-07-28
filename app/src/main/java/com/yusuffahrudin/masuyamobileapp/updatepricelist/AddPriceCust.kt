package com.yusuffahrudin.masuyamobileapp.updatepricelist

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.KeyEvent
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.ViewModelProviders
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.yusuffahrudin.masuyamobileapp.R
import com.yusuffahrudin.masuyamobileapp.controller.AppController
import com.yusuffahrudin.masuyamobileapp.controller.DialogAlert
import com.yusuffahrudin.masuyamobileapp.data.ArrayTampung
import com.yusuffahrudin.masuyamobileapp.data.informasi_barang.Product
import com.yusuffahrudin.masuyamobileapp.databinding.InputPricelistBinding
import com.yusuffahrudin.masuyamobileapp.informasibarang.ListBarangActivity
import com.yusuffahrudin.masuyamobileapp.util.Server
import com.yusuffahrudin.masuyamobileapp.util.SessionManager
import org.json.JSONException
import org.json.JSONObject
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by yusuf fahrudin on 17-05-2017.
 */
class AddPriceCust : AppCompatActivity() {
    private lateinit var kdbrg: String
    private lateinit var kdcustx: String
    private lateinit var kdkota: String
    private var jnsPpn: String = "00"
    private val tag_json_obj = "json_obj_req"
    private val nf = NumberFormat.getInstance(Locale("in", "ID"))
    private lateinit var binding: InputPricelistBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = InputPricelistBinding.inflate(layoutInflater)
        this.title = "Input Pricelist Barang"
        kdcustx = intent.extras?.getString("kdcust").toString()
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        if (supportActionBar != null) {
            supportActionBar!!.setDisplayShowHomeEnabled(true)
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        }
        val sessionManager = SessionManager(applicationContext)
        val user = sessionManager.userDetails
        kdkota = user[SessionManager.kdkota].toString()

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

        binding.edtKdbrg.setOnEditorActionListener { _: TextView?, actionId: Int, _: KeyEvent? ->
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                kdbrg = binding.edtKdbrg.text.toString().toUpperCase(Locale.getDefault())
                select()
                binding.edtKdbrg.setText(kdbrg)
                getHPP()
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(binding.edtKdbrg.windowToken, 0)
                return@setOnEditorActionListener true
            }
            false
        }

        binding.edtDiskon3.setOnEditorActionListener { _: TextView?, actionId: Int, _: KeyEvent? ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                binding.btnSimpan.performClick()
                return@setOnEditorActionListener true
            }
            false
        }

        binding.btnSearch.setOnClickListener {
            val intent = Intent(this, ListBarangActivity::class.java)
            intent.putExtra("kelas", this.localClassName)
            startActivityForResult(intent, REQUEST_BARANG)
        }

        binding.btnSimpan.setOnClickListener {
            insert()
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(binding.edtDiskon3.windowToken, 0)
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
            when(jnsPpn) {
                "01" -> binding.edtJenisppn.setText(getString(R.string.string_pppn))
                "02" -> binding.edtJenisppn.setText(getString(R.string.string_pnbkp))
                "03" -> binding.edtJenisppn.setText(getString(R.string.string_pbbs))
            }
            getHPP()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    //fungsi untuk select data dari database
    fun select() {
        val model = ViewModelProviders.of(this).get(AddPriceViewModel::class.java)
        model.getBarang(this, kdbrg).observe(this, { brgList: ArrayList<Product> ->
            for (i in brgList.indices) {
                jnsPpn = brgList[i].jnsPpn.toString()
                binding.edtBrg.setText(brgList[i].nmbrg)
                val harga = nf.format(brgList[i].hrg)
                val hargaincppn = nf.format(brgList[i].hrgIncPpn)
                binding.edtHarga.setText(harga)
                binding.edtHrgIncPpn.setText(hargaincppn)
                binding.edtDiskon1.setText("0")
                binding.edtDiskon2.setText("0")
                binding.edtDiskon3.setText("0")
                binding.edtSatuan.setText(brgList[i].satuan)
            }
            println("---------------------------- jnsppn $jnsPpn")
            when (jnsPpn) {
                "01" -> binding.edtJenisppn.setText(getString(R.string.string_pppn))
                "02" -> binding.edtJenisppn.setText(getString(R.string.string_pnbkp))
                "03" -> binding.edtJenisppn.setText(getString(R.string.string_pbbs))
            }
        })
    }

    //fungsi untuk select data dari database
    fun insert() {
        val listData = ArrayTampung.getListData()
        val item = Product()
        val tgl: String
        val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        tgl = sdf.format(Date())

        item.hrg = java.lang.Double.valueOf(ubahAngka(binding.edtHarga.text.toString()))
        item.hrgIncPpn = java.lang.Double.valueOf(ubahAngka(binding.edtHrgIncPpn.text.toString()))
        item.diskon1 = java.lang.Double.valueOf(ubahAngka(binding.edtDiskon1.text.toString()))
        item.diskon2 = java.lang.Double.valueOf(ubahAngka(binding.edtDiskon2.text.toString()))
        item.diskon3 = java.lang.Double.valueOf(ubahAngka(binding.edtDiskon3.text.toString()))
        item.kdbrg = kdbrg
        item.nmbrg = binding.edtBrg.text.toString()
        item.tgl = tgl
        item.satuan = binding.edtSatuan.text.toString()

        //menambah item ke array
        listData.add(item)
        DialogAlert("Data berhasil ditambahkan", "success", this@AddPriceCust)
        val intent = Intent(this@AddPriceCust, DetailAutoPricelist::class.java)
        intent.putExtra("kdcust", kdcustx)
        startActivityForResult(intent, PICK_CONTACT_REQUEST)
        finish()
    }

    private fun prosesCek(){
        val hpp50 = 1 * hpp + hpp
        if (hpp > ubahAngka(binding.edtHarga.text.toString()).toDouble())
            DialogAlert("Harga dibawah HPP", "attention", this)
        else if (hpp50 < ubahAngka(binding.edtHarga.text.toString()).toDouble())
            DialogAlert("Harga 100% diatas HPP", "attention", this)
        else if (ubahAngka(binding.edtDiskon1.text.toString()).toDouble() >= 100 || ubahAngka(binding.edtDiskon2.text.toString()).toDouble() >= 100 || ubahAngka(binding.edtDiskon3.text.toString()).toDouble() >= 100)
            DialogAlert("Diskon 1,2,3 tidak boleh >= 100%", "attention", this)
    }

    private fun getHPP() {
        val a = Server(kdkota)
        val url_cek_hpp = a.URL() + "updateprice/select_hpp.php"
        val strReq: StringRequest = object : StringRequest(Method.POST, url_cek_hpp, Response.Listener { response: String ->
            Log.v(TAG, "Response : $response")
            try {
                val jObj = JSONObject(response)
                hpp = jObj.getDouble("hpp")
                insert()
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }, Response.ErrorListener { error: VolleyError -> DialogAlert(error.message, "error", this@AddPriceCust) }) {
            override fun getParams(): Map<String, String> {
                val params: MutableMap<String, String> = HashMap()
                params["kdbrg"] = kdbrg
                return params
            }
        }
        AppController.getInstance().addToRequestQueue(strReq, tag_json_obj)
    }

    private fun ubahAngka(angka: String): String {
        val buangRibuan = angka.replace(".", "")
        return buangRibuan.replace(",", ".")
    }

    companion object {
        private var hpp = 0.0
        private val TAG = AddPriceCust::class.java.simpleName
        private const val PICK_CONTACT_REQUEST = 1
        private const val REQUEST_BARANG = 2
    }
}
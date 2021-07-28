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
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
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

class AddPriceProduk : AppCompatActivity() {

    private lateinit var kdbrg: String
    private lateinit var kdkota: String
    private var jnsPpn = "00"
    private lateinit var sessionManager: SessionManager
    private val nf = NumberFormat.getInstance(Locale("in", "ID"))
    private val REQUEST_BARANG = 1

    companion object {
        private var hpp: Double = 0.0
        private val TAG = AddPriceProduk::class.java.simpleName
        private var url_cek_hpp: String? = null
    }
    private lateinit var binding: InputPricelistBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = InputPricelistBinding.inflate(layoutInflater)
        setContentView(binding.root)
        this.title = "Input Pricelist Produk"
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        sessionManager = SessionManager(applicationContext)
        val user = sessionManager.userDetails
        kdkota = user[SessionManager.kdkota].toString()
        binding.btnSimpan.text = getString(R.string.next)

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

        binding.edtHrgIncPpn.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus){
                prosesCek()
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
                kdbrg = binding.edtKdbrg.text.toString().toUpperCase(Locale.getDefault())
                select()
                binding.edtKdbrg.setText(kdbrg)
                cekHPP()
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
            val intent = Intent(this@AddPriceProduk, UpdatePricelistBrgActivity::class.java)
            intent.putExtra("kdbrg", binding.edtKdbrg.text.toString())
            intent.putExtra("nmbrg", binding.edtBrg.text.toString())
            intent.putExtra("satuan", binding.edtSatuan.text.toString())
            intent.putExtra("hrg", ubahAngka(binding.edtHarga.text.toString()).toDouble())
            intent.putExtra("hrgincppn", ubahAngka(binding.edtHrgIncPpn.text.toString()).toDouble())
            intent.putExtra("diskon1", ubahAngka(binding.edtDiskon1.text.toString()).toDouble())
            intent.putExtra("diskon2", ubahAngka(binding.edtDiskon2.text.toString()).toDouble())
            intent.putExtra("diskon3", ubahAngka(binding.edtDiskon3.text.toString()).toDouble())
            startActivity(intent)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_BARANG && resultCode == RESULT_OK) {
            jnsPpn = data!!.getStringExtra(ListBarangActivity.JNSPPN).toString()
            kdbrg = data.getStringExtra(ListBarangActivity.KDBRG).toString()
            binding.edtKdbrg.setText(data.getStringExtra(ListBarangActivity.KDBRG))
            binding.edtBrg.setText(data.getStringExtra(ListBarangActivity.NMBRG))
            binding.edtSatuan.setText(data.getStringExtra(ListBarangActivity.SATUAN))
            binding.edtHarga.setText(nf.format(data.getDoubleExtra(ListBarangActivity.HRGEXC, 0.0)))
            binding.edtHrgIncPpn.setText(nf.format(data.getDoubleExtra(ListBarangActivity.HRGINC, 0.0)))
            println("------------------------ hrg2 " + data.getDoubleExtra(ListBarangActivity.HRGEXC, 0.0))
            println("------------------------ hrgIncPpn2 " + data.getDoubleExtra(ListBarangActivity.HRGINC, 0.0))
            binding.edtDiskon1.setText("0")
            binding.edtDiskon2.setText("0")
            binding.edtDiskon3.setText("0")
            when (jnsPpn) {
                "01" -> binding.edtJenisppn.setText(getString(R.string.string_pppn))
                "02" -> binding.edtJenisppn.setText(getString(R.string.string_pnbkp))
                "03" -> binding.edtJenisppn.setText(getString(R.string.string_pbbs))
            }
            cekHPP()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == android.R.id.home) { this.finish() }
        return super.onOptionsItemSelected(item)
    }

    //region select
    fun select() {
        val model = ViewModelProviders.of(this).get(AddPriceViewModel::class.java)
        model.getBarang(this, kdbrg).observe(this, { brgList: ArrayList<Product> ->
            run {
                for (i in 0 until brgList.size) {
                    jnsPpn = brgList[i].jnsPpn.toString()
                    binding.edtBrg.setText(brgList[i].nmbrg)
                    binding.edtSatuan.setText(brgList[i].satuan)
                    binding.edtHarga.setText(nf.format(brgList[i].hrg!!.toDouble()))
                    binding.edtHrgIncPpn.setText(nf.format(brgList[i].hrgIncPpn!!.toDouble()))
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

    //region cekHPP
    private fun cekHPP() {
        val a = Server(kdkota)
        url_cek_hpp = a.URL() + "updateprice/select_hpp.php"
        val strReq = object : StringRequest(Method.POST, url_cek_hpp, Response.Listener { response ->
            Log.v(TAG, "Response : $response")
            try {
                val jObj = JSONObject(response)
                hpp = jObj.getDouble("hpp")
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }, Response.ErrorListener { error -> DialogAlert(error.message.toString(), "error", this@AddPriceProduk) }) {
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

    //region search
    private fun prosesCek() {
        val hrg     = ubahAngka(binding.edtHarga.text.toString()).toDouble()
        val diskon1 = ubahAngka(binding.edtDiskon1.text.toString()).toDouble()
        val diskon2 = ubahAngka(binding.edtDiskon2.text.toString()).toDouble()
        val diskon3 = ubahAngka(binding.edtDiskon3.text.toString()).toDouble()
        val hpp50   = (1 * hpp) + hpp

        if (hpp > hrg) {
            DialogAlert("Harga dibawah HPP", "attention", this@AddPriceProduk)
        }
        else if (hpp50 < hrg) {
            DialogAlert("Harga 100% diatas HPP", "attention", this@AddPriceProduk)
        }
        else if (diskon1 >= 100 || diskon2 >= 100 || diskon3 >= 100) {
            DialogAlert("Diskon 1,2,3 tidak boleh >= 100%", "attention", this@AddPriceProduk)
        }
    }
    //endregion

    //region ubahAngka
    private fun ubahAngka(angka: String): String {
        val buangRibuan = angka.replace(".", "")

        return buangRibuan.replace(",", ".")
    }
    //endregion
}

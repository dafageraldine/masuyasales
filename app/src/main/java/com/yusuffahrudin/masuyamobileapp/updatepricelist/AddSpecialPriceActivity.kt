package com.yusuffahrudin.masuyamobileapp.updatepricelist

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.ViewModelProviders
import com.yusuffahrudin.masuyamobileapp.R
import com.yusuffahrudin.masuyamobileapp.api.API
import com.yusuffahrudin.masuyamobileapp.api.ApiService
import com.yusuffahrudin.masuyamobileapp.controller.DialogAlert
import com.yusuffahrudin.masuyamobileapp.data.Result
import com.yusuffahrudin.masuyamobileapp.data.informasi_barang.Product
import com.yusuffahrudin.masuyamobileapp.databinding.ActivityAddSpecialPriceBinding
import com.yusuffahrudin.masuyamobileapp.informasibarang.ListBarangActivity
import com.yusuffahrudin.masuyamobileapp.util.SessionManager
import org.jetbrains.anko.doAsync
import retrofit2.Call
import retrofit2.Callback
import java.text.NumberFormat
import java.util.*

class AddSpecialPriceActivity : AppCompatActivity() {
    private val nf = NumberFormat.getInstance(Locale("in", "ID"))
    private var listTipePrice: MutableList<String> = mutableListOf("Under Bottom", "Under Cost")
    private var jnsPpn = "00"
    private var kdcust = ""
    private var hrgPokok = 0.0
    private lateinit var sessionManager: SessionManager
    private lateinit var name: String
    private var underCost = false
    private var underBottomSP = false
    private val REQUEST_BARANG = 1
    companion object {
        val PARCELPRODUCT = "parcelable_product"
    }
    private lateinit var adapterSpin: ArrayAdapter<String>
    private lateinit var binding: ActivityAddSpecialPriceBinding

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddSpecialPriceBinding.inflate(layoutInflater)
        setContentView(binding.root)
        this.title = "Input SpecialPrice"
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.progressbar.visibility = View.GONE
        sessionManager = SessionManager(applicationContext)
        val user = sessionManager.userDetails
        name = user[SessionManager.kunci_email].toString()
        if (user[SessionManager.underCost].toString() == "1") underCost = true
        if (user[SessionManager.underBottomSP].toString() == "1") underBottomSP = true

        adapterSpin = ArrayAdapter(this, R.layout.spinner_black, listTipePrice)
        binding.spinTipePrice.adapter = adapterSpin
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)+1
        val day = c.get(Calendar.DAY_OF_MONTH)
        binding.edtStartDate.setText("$year-${if (month < 10) "0$month" else month}-${if (day < 10) "0$day" else day}")
        binding.edtEndDate.setText("$year-${if (month < 10) "0$month" else month}-${if (day < 10) "0$day" else day}")

        val extras = intent.extras
        if (extras != null) {
            kdcust = extras.getString("kdcust", "").toString()
            val dataProduk = intent.getParcelableExtra<Product>(PARCELPRODUCT)
            if (dataProduk != null) {
                editBarang(dataProduk)
            }
        }
        if (kdcust == "") binding.btnSimpan.setText(R.string.next) else binding.btnSimpan.setText(R.string.simpan)

        binding.edtStartDate.setOnClickListener {
            val mYear = c.get(Calendar.YEAR)
            val mMonth = c.get(Calendar.MONTH)
            val mDay = c.get(Calendar.DAY_OF_MONTH)
            c.add(Calendar.DAY_OF_MONTH, 7)
            val datePickerDialog = DatePickerDialog(this, { _, year, monthOfYear, dayOfMonth ->
                binding.edtStartDate.setText("$year-${if (monthOfYear+1 < 10) "0${monthOfYear+1}" else monthOfYear+1}-${if (dayOfMonth < 10) "0$dayOfMonth" else dayOfMonth}")}, mYear, mMonth, mDay)
            datePickerDialog.datePicker.minDate = System.currentTimeMillis() - 1000
            //datePickerDialog.datePicker.maxDate = c.timeInMillis
            datePickerDialog.show()
        }
        binding.edtEndDate.setOnClickListener {
            val mYear = c.get(Calendar.YEAR)
            val mMonth = c.get(Calendar.MONTH)
            val mDay = c.get(Calendar.DAY_OF_MONTH)
            c.add(Calendar.DAY_OF_MONTH, 7)
            val datePickerDialog = DatePickerDialog(this, { _, year, monthOfYear, dayOfMonth ->
                binding.edtEndDate.setText("$year-${if (monthOfYear+1 < 10) "0${monthOfYear+1}" else monthOfYear+1}-${if (dayOfMonth < 10) "0$dayOfMonth" else dayOfMonth}")
            }, mYear, mMonth, mDay)
            datePickerDialog.datePicker.minDate = System.currentTimeMillis() - 1000
            //datePickerDialog.datePicker.maxDate = c.timeInMillis
            datePickerDialog.show()
        }

        binding.edtKdbrg.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                val kdbrg = binding.edtKdbrg.text.toString().toUpperCase(Locale.getDefault())
                getBarang(kdbrg)
                binding.edtKdbrg.setText(kdbrg)
                binding.btnSimpan.isEnabled = true
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }

        binding.edtKdbrg.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus){
                binding.btnSimpan.isEnabled = false
            } else {
                val kdbrg = binding.edtKdbrg.text.toString().toUpperCase(Locale.getDefault())
                getBarang(kdbrg)
                binding.edtKdbrg.setText(kdbrg)
                binding.btnSimpan.isEnabled = true
            }
        }

        binding.edtHrgIncPpn.doAfterTextChanged {
            try {
                if (TextUtils.isEmpty(it)){
                    binding.edtHrgIncPpn.error = getString(R.string.string_blank)
                    binding.btnSimpan.isEnabled = false
                } else {
                    if (jnsPpn == "01") {
                        var hrg = 0.0
                        if (binding.edtHrgIncPpn.text.toString() != "0") {
                            hrg = ubahAngka(binding.edtHrgIncPpn.text.toString()).toDouble() / 1.1
                            binding.edtHarga.setText(nf.format(hrg))
                        }

                        hrg = ubahAngka(binding.edtHarga.text.toString()).toDouble()
                        val disc1 = ubahAngka(binding.edtDiskon1.text.toString()).toDouble()
                        val disc2 = ubahAngka(binding.edtDiskon2.text.toString()).toDouble()
                        val disc3 = ubahAngka(binding.edtDiskon3.text.toString()).toDouble()
                        val hrgnet = (((hrg * (1 - (disc1/100))) * (1 - (disc2/100))) * (1 - (disc3/100)))
                        binding.edtHrgnet.setText(nf.format(hrgnet))
                        binding.edtHrgnetIncPpn.setText(nf.format((hrgnet * 1.1)))
                    } else {
                        if (binding.edtHrgIncPpn.text.toString() != "0") {
                            binding.edtHarga.setText(binding.edtHrgIncPpn.text.toString())
                        }

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

        binding.edtQtyKuota.doAfterTextChanged {
            if (TextUtils.isEmpty(it)){
                binding.edtQtyKuota.error = getString(R.string.string_blank)
                binding.btnSimpan.isEnabled = false
            } else if (it.toString() == "0") {
                binding.edtQtyKuota.error = "Qty Kuota tidak boleh 0"
            } else {
                binding.btnSimpan.isEnabled = true
            }
        }

        binding.btnSimpan.setOnClickListener {
            if (binding.edtQtyKuota.text.toString() == "0"){
                DialogAlert("Qty Kuota tidak boleh 0", "attention", this@AddSpecialPriceActivity)
            } else if(binding.edtBrg.text.toString() == "") {
                DialogAlert("Tidak dapat save special price, Barang tidak sesuai!", "error", this@AddSpecialPriceActivity)
            }else {
                if (binding.spinTipePrice.selectedItem.toString().equals("Under Cost", true) && underCost)
                    if (kdcust == ""){
                        intent = Intent(this, AddSpecialPriceCustActivity::class.java)
                        val item = Product()
                        item.kdbrg = binding.edtKdbrg.text.toString()
                        item.satuan = binding.edtSatuan.text.toString()
                        item.hrg = ubahAngka(binding.edtHarga.text.toString()).toDouble()
                        item.diskon1 = ubahAngka(binding.edtDiskon1.text.toString()).toDouble()
                        item.diskon2 = ubahAngka(binding.edtDiskon2.text.toString()).toDouble()
                        item.diskon3 = ubahAngka(binding.edtDiskon3.text.toString()).toDouble()
                        item.hrgIncPpn = ubahAngka(binding.edtHrgIncPpn.text.toString()).toDouble()
                        item.qtyKuota = ubahAngka(binding.edtQtyKuota.text.toString()).toDouble()
                        item.startDate = binding.edtStartDate.text.toString()
                        item.endDate = binding.edtEndDate.text.toString()
                        item.createBy = name
                        item.tipePrice = binding.spinTipePrice.selectedItem.toString()
                        intent.putExtra(PARCELPRODUCT, item)
                        startActivity(intent)
                    } else
                        insertData()
                else if (binding.spinTipePrice.selectedItem.toString().equals("Under Bottom", true) && underBottomSP)
                    if (kdcust == ""){
                        intent = Intent(this, AddSpecialPriceCustActivity::class.java)
                        val item = Product()
                        item.kdbrg = binding.edtKdbrg.text.toString()
                        item.satuan = binding.edtSatuan.text.toString()
                        item.hrg = ubahAngka(binding.edtHarga.text.toString()).toDouble()
                        item.diskon1 = ubahAngka(binding.edtDiskon1.text.toString()).toDouble()
                        item.diskon2 = ubahAngka(binding.edtDiskon2.text.toString()).toDouble()
                        item.diskon3 = ubahAngka(binding.edtDiskon3.text.toString()).toDouble()
                        item.hrgIncPpn = ubahAngka(binding.edtHrgIncPpn.text.toString()).toDouble()
                        item.qtyKuota = ubahAngka(binding.edtQtyKuota.text.toString()).toDouble()
                        item.startDate = binding.edtStartDate.text.toString()
                        item.endDate = binding.edtEndDate.text.toString()
                        item.createBy = name
                        item.tipePrice = binding.spinTipePrice.selectedItem.toString()
                        intent.putExtra(PARCELPRODUCT, item)
                        startActivity(intent)
                    } else
                        insertData()
                else
                    DialogAlert(getString(R.string.tidak_mempunyai_hak_akses), "attention", this@AddSpecialPriceActivity)
            }

        }

        binding.btnSearch.setOnClickListener {
            intent = Intent(this, ListBarangActivity::class.java)
            intent.putExtra("kelas", this.localClassName)
            startActivityForResult(intent, REQUEST_BARANG)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_BARANG && resultCode == RESULT_OK) {
            /*jnsPpn = data!!.getStringExtra(ListBarangActivity.JNSPPN).toString()
            binding.edtBrg.setText(data.getStringExtra(ListBarangActivity.NMBRG))
            binding.edtSatuan.setText(data.getStringExtra(ListBarangActivity.SATUAN))
            binding.edtHarga.setText(nf.format(data.getDoubleExtra(ListBarangActivity.HRGEXC, 0.0)))
            binding.edtHrgIncPpn.setText(nf.format(data.getDoubleExtra(ListBarangActivity.HRGINC, 0.0)))
            binding.edtDiskon1.setText("0")
            binding.edtDiskon2.setText("0")
            binding.edtDiskon3.setText("0")
            when (jnsPpn) {
                "01" -> binding.edtJenisppn.setText(getString(R.string.string_pppn))
                "02" -> binding.edtJenisppn.setText(getString(R.string.string_pnbkp))
                "03" -> binding.edtJenisppn.setText(getString(R.string.string_pbbs))
            }*/
            binding.edtKdbrg.setText(data!!.getStringExtra(ListBarangActivity.KDBRG))
            getBarang(data.getStringExtra(ListBarangActivity.KDBRG).toString())
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == android.R.id.home) { this.finish() }
        return super.onOptionsItemSelected(item)
    }

    private fun getBarang(kdbrg: String){
        val model = ViewModelProviders.of(this).get(AddPriceViewModel::class.java)
        model.getBarangPricelist(this, kdcust, kdbrg).observe(this, { brgList: ArrayList<Product> ->
            run {
                println("------------------------------ getBarang")
                if (brgList.isNotEmpty()) {
                    for (i in 0 until brgList.size) {
                        jnsPpn = brgList[i].jnsPpn.toString()
                        binding.edtBrg.setText(brgList[i].nmbrg)
                        binding.edtSatuan.setText(brgList[i].satuan)
                        println("-------------------- hrg ${nf.format(brgList[i].hrg)}")
                        binding.edtHarga.setText(nf.format(brgList[i].hrg))
                        binding.edtHrgIncPpn.setText(nf.format(brgList[i].hrgIncPpn))
                        binding.edtDiskon1.setText(nf.format(brgList[i].diskon1))
                        binding.edtDiskon2.setText(nf.format(brgList[i].diskon2))
                        binding.edtDiskon3.setText(nf.format(brgList[i].diskon3))
                        hrgPokok = brgList[i].hrgPokok!!.toDouble()
                        binding.edtHrgpokok.setText(nf.format(brgList[i].hrgPokok))
                        binding.edtHrgmin.setText(nf.format(brgList[i].hrgJualMin))
                    }
                    when (jnsPpn) {
                        "01" -> binding.edtJenisppn.setText(getString(R.string.string_pppn))
                        "02" -> binding.edtJenisppn.setText(getString(R.string.string_pnbkp))
                        "03" -> binding.edtJenisppn.setText(getString(R.string.string_pbbs))
                    }
                } else {
                    jnsPpn = ""
                    binding.edtBrg.setText("")
                    binding.edtSatuan.setText("")
                    binding.edtHarga.setText("0")
                    binding.edtHrgIncPpn.setText("0")
                    binding.edtDiskon1.setText("0")
                    binding.edtDiskon2.setText("0")
                    binding.edtDiskon3.setText("0")
                    binding.edtQtyKuota.setText("0")
                    binding.edtHrgnet.setText("0")
                    binding.edtHrgnetIncPpn.setText("0")
                    hrgPokok = 0.0
                    binding.edtHrgpokok.setText("0")
                    binding.edtHrgmin.setText("0")
                    DialogAlert("Barang tidak ditemukan!", "error", this@AddSpecialPriceActivity)
                }
            }
        })
    }

    @SuppressLint("SimpleDateFormat")
    private fun editBarang(dataProduk: Product){
        binding.edtKdbrg.setText(dataProduk.kdbrg)
        binding.edtBrg.setText(dataProduk.nmbrg)
        binding.edtHarga.setText(nf.format(dataProduk.hrg))
        binding.edtHrgIncPpn.setText(nf.format(dataProduk.hrgIncPpn))
        binding.edtDiskon1.setText(nf.format(dataProduk.diskon1))
        binding.edtDiskon2.setText(nf.format(dataProduk.diskon2))
        binding.edtDiskon3.setText(nf.format(dataProduk.diskon3))
        if (dataProduk.startDate != "null") {
            binding.edtStartDate.setText(dataProduk.startDate.toString())
        }
        if (dataProduk.endDate != "null") {
            binding.edtEndDate.setText(dataProduk.endDate.toString())
        }
        binding.edtQtyKuota.setText(nf.format(dataProduk.qtyKuota))
        binding.edtSatuan.setText(dataProduk.satuan)
        binding.spinTipePrice.setSelection(adapterSpin.getPosition(dataProduk.tipePrice))
        jnsPpn = dataProduk.jnsPpn.toString()
        hrgPokok = dataProduk.hrgPokok!!.toDouble()
        binding.edtHrgpokok.setText(nf.format(dataProduk.hrgPokok))
        binding.edtHrgmin.setText(nf.format(dataProduk.hrgJualMin))
        when (jnsPpn) {
            "01" -> binding.edtJenisppn.setText(getString(R.string.string_pppn))
            "02" -> binding.edtJenisppn.setText(getString(R.string.string_pnbkp))
            "03" -> binding.edtJenisppn.setText(getString(R.string.string_pbbs))
        }
    }

    //region ubahAngka
    private fun ubahAngka(angka: String): String {
        val buangRibuan = angka.replace(".", "")

        return buangRibuan.replace(",", ".")
    }
    //endregion

    private fun insertData(){
        binding.progressbar.visibility = View.VISIBLE
        doAsync {
            val params = HashMap<String?, String?>()
            params["kdcust"] = kdcust
            params["kdbrg"] = binding.edtKdbrg.text.toString()
            params["satuan"] = binding.edtSatuan.text.toString()
            params["harga"] = ubahAngka(binding.edtHarga.text.toString())
            params["diskon1"] = ubahAngka(binding.edtDiskon1.text.toString())
            params["diskon2"] = ubahAngka(binding.edtDiskon2.text.toString())
            params["diskon3"] = ubahAngka(binding.edtDiskon3.text.toString())
            params["hrgIncPpn"] = ubahAngka(binding.edtHrgIncPpn.text.toString())
            params["qtykuota"] = ubahAngka(binding.edtQtyKuota.text.toString())
            params["startdate"] = binding.edtStartDate.text.toString()
            params["enddate"] = binding.edtEndDate.text.toString()
            params["createby"] = name
            params["tipe"] = binding.spinTipePrice.selectedItem.toString()
            API.instance().create(ApiService::class.java)
                    .saveSpecialPrice(params)
                    ?.enqueue(object : Callback<Result?> {
                        override fun onFailure(call: Call<Result?>, e: Throwable) {
                            binding.progressbar.visibility = View.GONE
                            DialogAlert(e.message, "error", this@AddSpecialPriceActivity)
                        }

                        override fun onResponse(call: Call<Result?>, response: retrofit2.Response<Result?>) {
                            if (response.isSuccessful) {
                                val message = response.body()?.message.toString()
                                if (response.body()?.success == 1) {
                                    binding.progressbar.visibility = View.GONE
                                    DialogAlert(message, "success", this@AddSpecialPriceActivity)
                                } else {
                                    binding.progressbar.visibility = View.GONE
                                    DialogAlert(message, "error", this@AddSpecialPriceActivity)
                                }
                            }
                        }
                    })
        }
    }
}
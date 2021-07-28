package com.yusuffahrudin.masuyamobileapp.updatepricelist

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.yusuffahrudin.masuyamobileapp.data.ArrayTampung
import com.yusuffahrudin.masuyamobileapp.data.UserAkses
import com.yusuffahrudin.masuyamobileapp.databinding.ActivityUpdatePricelistBinding
import com.yusuffahrudin.masuyamobileapp.util.SessionManager

class UpdatePricelistActivity : AppCompatActivity() {

    internal var listAks: List<UserAkses> = ArrayTampung.getListAkses()
    private lateinit var binding: ActivityUpdatePricelistBinding
    private lateinit var sessionManager: SessionManager
    private var underCost = false
    private var underBottomSP = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.title = "Update Pricelist"
        binding = ActivityUpdatePricelistBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        sessionManager = SessionManager(applicationContext)
        val user = sessionManager.userDetails
        if (user[SessionManager.underCost].toString() == "1") underCost = true
        if (user[SessionManager.underBottomSP].toString() == "1") underBottomSP = true

        cekAkses()
        if (underCost || underBottomSP) binding.btnUpdatePriceHpp.isEnabled = true

        binding.btnUpdatePriceCust.setOnClickListener {
            val intent = Intent(this@UpdatePricelistActivity, ListCustomerActivity::class.java)
            startActivity(intent)
        }
        binding.btnUpdatePriceBrg.setOnClickListener {
            val intent = Intent(this@UpdatePricelistActivity, AddPriceProduk::class.java)
            startActivity(intent)
        }
        binding.btnUpdatePriceAuto.setOnClickListener {
            val intent = Intent(this@UpdatePricelistActivity, AutoPricelistActivity::class.java)
            startActivity(intent)
        }
        binding.btnUpdatePriceHpp.setOnClickListener {
            val intent = Intent(this@UpdatePricelistActivity, ListCustomerSpecialPriceActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == android.R.id.home) {
            this.finish()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun cekAkses() {
        for (i in listAks.indices) {
            val str = listAks[i].modul
            val modul = str.substring(str.indexOf("-") + 1)

            if (modul.equals("Customer", ignoreCase = true)) {
                binding.btnUpdatePriceCust.isEnabled = listAks[i].akses == 1
            }
            if (modul.equals("Produk", ignoreCase = true)) {
                binding.btnUpdatePriceBrg.isEnabled = listAks[i].akses == 1
            }
            if (modul.equals("History Penjualan", ignoreCase = true)) {
                binding.btnUpdatePriceAuto.isEnabled = listAks[i].akses == 1
            }
        }
    }

}

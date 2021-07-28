package com.yusuffahrudin.masuyamobileapp.salesorder

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.fragment.app.FragmentStatePagerAdapter
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.yusuffahrudin.masuyamobileapp.R
import com.yusuffahrudin.masuyamobileapp.controller.DialogAlert
import com.yusuffahrudin.masuyamobileapp.data.ArrayTampung
import com.yusuffahrudin.masuyamobileapp.databinding.ActivitySalesOrderBinding
import com.yusuffahrudin.masuyamobileapp.databinding.DialogFilterListSalesOrderBinding
import com.yusuffahrudin.masuyamobileapp.util.SessionManager
import java.util.*

class SalesOrderActivity : AppCompatActivity() {
    private val listAkses = ArrayTampung.getListAkses()
    private lateinit var adapterAutho: ArrayAdapter<String>
    private lateinit var adapterStatusAutho: ArrayAdapter<String>
    private var nobukti: String      = ""
    private var cust: String         = ""
    private var autho: String        = "-"
    private var status: String       = "All"
    private var item: String         = "All"
    private lateinit var binding: ActivitySalesOrderBinding
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        println("======SOA====")
        super.onCreate(savedInstanceState)
        this.title = getString(R.string.list_sales_order)
        binding = ActivitySalesOrderBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        if (supportActionBar != null) {
            supportActionBar!!.setDisplayShowHomeEnabled(true)
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        }
        sessionManager = SessionManager(this)
        val session = sessionManager.userDetails
        if (session[SessionManager.underCost].toString() == "1"){
            autho = "OtoUC"
        } else if (session[SessionManager.underBottomSP].toString() == "1"){
            autho = "OtoODSL"
        } else if (session[SessionManager.underBottomSO].toString() == "1"){
            autho = "OtoUB"
        } else if (session[SessionManager.level].toString() == "10101020" || session[SessionManager.level].toString() == "1010102010"){
            autho = "OtoODAR"
        }

        val mSectionsPagerAdapter = SectionsPagerAdapter(supportFragmentManager)
        binding.containerSo.offscreenPageLimit = 3
        binding.containerSo.adapter = mSectionsPagerAdapter
        binding.tabLayout.setupWithViewPager(binding.containerSo)
        binding.fab.setOnClickListener {
            var akses = false
            for (i in listAkses.indices) {
                if (listAkses[i].modul.equals("Sales Order", ignoreCase = true) &&
                        listAkses[i].add == 1) {
                    akses = true
                }
            }
            if (akses) {
                val intent = Intent(this@SalesOrderActivity, CreateSOActivity::class.java)
                intent.putExtra("status_order", "Create Order")
                intent.putExtra("nomor_so", "AUTO")
                startActivity(intent)
            } else {
                DialogAlert(getString(R.string.tidak_mempunyai_hak_akses), "error", this@SalesOrderActivity)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_refresh_filter, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
            }
            R.id.action_refresh -> {
                when (binding.containerSo.currentItem) {
                    0 -> {
                        val fragment = binding.containerSo.adapter!!.instantiateItem(binding.containerSo, binding.containerSo.currentItem) as OpenSOFragment
                        fragment.reload()
                    }
                    1 -> {
                        val fragment = binding.containerSo.adapter!!.instantiateItem(binding.containerSo, binding.containerSo.currentItem) as PendingSOFragment
                        fragment.reload()
                    }
                    2 -> {
                        val fragment = binding.containerSo.adapter!!.instantiateItem(binding.containerSo, binding.containerSo.currentItem) as ClosedSOFragment
                        fragment.reload()
                    }
                    else -> {
                        val fragment = binding.containerSo.adapter!!.instantiateItem(binding.containerSo, binding.containerSo.currentItem) as AllSOFragment
                        fragment.reload()
                    }
                }
            }
            R.id.action_filter -> {
                bottomFilter()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * A [FragmentPagerAdapter] that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    inner class SectionsPagerAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
        override fun getItem(position: Int): Fragment {
            return when (position) {
                0 -> OpenSOFragment()
                1 -> PendingSOFragment()
                2 -> ClosedSOFragment()
                else -> AllSOFragment()
            }
        }

        override fun getCount(): Int {
            return 4
        }

        override fun getPageTitle(position: Int): CharSequence? {
            when (position) {
                0 -> return "Open"
                1 -> return "Pending"
                2 -> return "Close"
                3 -> return "All"
            }
            return null
        }
    }

    private fun bottomFilter(){
        val dialogFilter = BottomSheetDialog(this, R.style.SheetDialog)
        val bindingDialogFilter = DialogFilterListSalesOrderBinding.inflate(layoutInflater)
        val bottomLayout = bindingDialogFilter.root
        val slideUp = AnimationUtils.loadAnimation(this, R.anim.show_from_bottom)
        dialogFilter.setContentView(bottomLayout)

        //mengisi spinner autho
        val listAutho = mutableListOf("-", resources.getString(R.string.oto_odar), resources.getString(R.string.oto_odsl), resources.getString(R.string.oto_uc), resources.getString(R.string.oto_ub))
        var listStatusAutho = mutableListOf("All, Belum Oto, Approved, Rejected")
        val listItem = mutableListOf("All", "Timbangan")
        adapterAutho = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, listAutho)
        adapterStatusAutho = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, listStatusAutho)
        val adapterItem = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, listItem)
        bindingDialogFilter.spinAutho.setAdapter(adapterAutho)
        bindingDialogFilter.spinAutho.setLabel("Authorization")
        bindingDialogFilter.spinAutho.setErrorEnabled(false)
        bindingDialogFilter.spinStatusAutho.setAdapter(adapterStatusAutho)
        bindingDialogFilter.spinStatusAutho.setLabel("Authorization Status")
        bindingDialogFilter.spinStatusAutho.setErrorEnabled(false)
        bindingDialogFilter.spinItem.setAdapter(adapterItem)
        bindingDialogFilter.spinItem.setLabel("Item")
        bindingDialogFilter.spinItem.setErrorEnabled(false)
        bindingDialogFilter.edtNobukti.setText(nobukti)
        bindingDialogFilter.edtCust.setText(cust)

        when(autho) {
            "-"         -> bindingDialogFilter.spinAutho.getSpinner().setSelection(0)
            "OtoODAR"   -> bindingDialogFilter.spinAutho.getSpinner().setSelection(1)
            "OtoODSL"   -> bindingDialogFilter.spinAutho.getSpinner().setSelection(2)
            "OtoUC"     -> bindingDialogFilter.spinAutho.getSpinner().setSelection(3)
            "OtoUB"     -> bindingDialogFilter.spinAutho.getSpinner().setSelection(4)
        }

        when(item){
            "All"       -> bindingDialogFilter.spinItem.getSpinner().setSelection(0)
            "Timbangan" -> bindingDialogFilter.spinItem.getSpinner().setSelection(1)
        }

        bindingDialogFilter.spinAutho.setItemSelectedListener(onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                when (position) {
                    0 -> listStatusAutho = mutableListOf("All")
                    1 -> listStatusAutho = mutableListOf("All", "Belum Oto", "Approved", "Rejected")
                    2 -> listStatusAutho = mutableListOf("All", "Belum Oto", "Approved", "Rejected")
                    3 -> listStatusAutho = mutableListOf("All", "Belum Oto", "Approved")
                    4 -> listStatusAutho = mutableListOf("All", "Belum Oto", "Approved")
                }
                adapterStatusAutho = ArrayAdapter(applicationContext, android.R.layout.simple_spinner_dropdown_item, listStatusAutho)
                adapterStatusAutho.notifyDataSetChanged()
                bindingDialogFilter.spinStatusAutho.setAdapter(adapterStatusAutho)
                when(status) {
                    "All"       -> bindingDialogFilter.spinStatusAutho.getSpinner().setSelection(0)
                    "Belum Oto" -> bindingDialogFilter.spinStatusAutho.getSpinner().setSelection(1)
                    "Approved"  -> { if(autho == "-") bindingDialogFilter.spinStatusAutho.getSpinner().setSelection(0)
                    else bindingDialogFilter.spinStatusAutho.getSpinner().setSelection(2) }
                    "Rejected"  -> bindingDialogFilter.spinStatusAutho.getSpinner().setSelection(3)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) { TODO("Not yet implemented") }
        })

        bindingDialogFilter.btnSearch.setOnClickListener {
            nobukti = bindingDialogFilter.edtNobukti.text.toString()
            cust = bindingDialogFilter.edtCust.text.toString()
            when(bindingDialogFilter.spinAutho.getSpinner().selectedItem.toString()) {
                "-"           -> autho = "-"
                resources.getString(R.string.oto_odar)  -> autho = "OtoODAR"
                resources.getString(R.string.oto_odsl)  -> autho = "OtoODSL"
                resources.getString(R.string.oto_uc)    -> autho = "OtoUC"
                resources.getString(R.string.oto_ub)    -> autho = "OtoUB"
            }
            item = bindingDialogFilter.spinItem.getSpinner().selectedItem.toString()
            status = bindingDialogFilter.spinStatusAutho.getSpinner().selectedItem.toString()
            when (binding.containerSo.currentItem) {
                0 -> {
                    val fragment = binding.containerSo.adapter!!.instantiateItem(binding.containerSo, binding.containerSo.currentItem) as OpenSOFragment
                    fragment.filter(nobukti, cust, autho, status, item)
                }
                1 -> {
                    val fragment = binding.containerSo.adapter!!.instantiateItem(binding.containerSo, binding.containerSo.currentItem) as PendingSOFragment
                    fragment.filter(nobukti, cust, autho, status, item)
                }
                2 -> {
                    val fragment = binding.containerSo.adapter!!.instantiateItem(binding.containerSo, binding.containerSo.currentItem) as ClosedSOFragment
                    fragment.filter(nobukti, cust, autho, status, item)
                }
                else -> {
                    val fragment = binding.containerSo.adapter!!.instantiateItem(binding.containerSo, binding.containerSo.currentItem) as AllSOFragment
                    fragment.filter(nobukti, cust, autho, status, item)
                }
            }
            dialogFilter.dismiss()
        }

        bindingDialogFilter.btnReset.setOnClickListener {
            bindingDialogFilter.edtNobukti.setText("")
            bindingDialogFilter.edtCust.setText("")
            bindingDialogFilter.spinAutho.getSpinner().setSelection(0)
            bindingDialogFilter.spinStatusAutho.getSpinner().setSelection(0)
            autho = "-"
            status = "All"
            item = "All"
        }

        bottomLayout.startAnimation(slideUp)
        dialogFilter.show()
    }
}
package com.yusuffahrudin.masuyamobileapp.updatepricelist

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.*
import android.view.animation.AnimationUtils
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.yusuffahrudin.masuyamobileapp.R
import com.yusuffahrudin.masuyamobileapp.controller.DialogAlert
import com.yusuffahrudin.masuyamobileapp.data.customer.Customer
import com.yusuffahrudin.masuyamobileapp.databinding.ActivityListCustomerBinding
import com.yusuffahrudin.masuyamobileapp.databinding.DialogPilihCustomerBinding
import com.yusuffahrudin.masuyamobileapp.databinding.RvCustomerBinding
import com.yusuffahrudin.masuyamobileapp.util.SessionManager
import java.util.ArrayList

class ListCustomerSpecialPriceActivity : AppCompatActivity() {

    private lateinit var adapterRV: AdapterRV
    private lateinit var adapterCust: AdapterCustomer
    private var listCust: MutableList<Customer> = ArrayList()
    private lateinit var listCustSearch: MutableList<Customer>
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var sessionManager: SessionManager
    private var i: Int = 0
    private var isSearch = false
    private var underCost = false
    private var underBottomSP = false
    private lateinit var binding: ActivityListCustomerBinding

    companion object {
        private var name: String? = null
        private var level: String? = null
        private var kdkota: String? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.title = "List Customer Special Price"
        binding = ActivityListCustomerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        sessionManager = SessionManager(applicationContext)
        val user = sessionManager.userDetails
        name = user[SessionManager.kunci_email]
        level = user[SessionManager.level]
        kdkota = user[SessionManager.kdkota]
        Toast.makeText(this, name, Toast.LENGTH_LONG).show()
        if (user[SessionManager.underCost].toString() == "1") underCost = true
        if (user[SessionManager.underBottomSP].toString() == "1") underBottomSP = true

        //menghubungkan variabel dengan layout view dan java
        binding.progressbar.visibility = View.GONE
        binding.rvUpdatePriceCust.setHasFixedSize(true)

        layoutManager = LinearLayoutManager(this)
        binding.rvUpdatePriceCust.layoutManager = layoutManager

        //untuk mengisi data dari JSON ke Adapter
        adapterRV = AdapterRV(this, listCust)
        binding.rvUpdatePriceCust.adapter = adapterRV

        if(listCust.isEmpty()) { selectCustomer(0) }

        binding.rvUpdatePriceCust.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
                if (firstVisibleItemPosition + visibleItemCount >= totalItemCount) {
                    if (i == 0) {
                        if(!isSearch){
                            selectCustomer(totalItemCount)
                            i = 1
                        }
                    }
                } else {
                    i = 0
                }
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.add_refresh, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        if (id == android.R.id.home) { this.finish() }
        if (id == R.id.action_refresh) {
            listCust.clear()
            selectCustomer(0)
        } else if (id == R.id.action_add) {
            if (underCost || underBottomSP) {
                dialogPilihCustomer()
            } else DialogAlert(getString(R.string.tidak_mempunyai_hak_akses), "error", this@ListCustomerSpecialPriceActivity)
        }

        return super.onOptionsItemSelected(item)
    }

    //fungsi untuk select data dari database
    private fun selectCustomer(itemCount: Int) {
        binding.progressbar.visibility = View.VISIBLE
        var tipe = ""
        if (underCost && underBottomSP) tipe = "All"
        else if (underBottomSP) tipe = "Under Bottom"
        else if (underCost) tipe = "Under Cost"
        val model = ViewModelProviders.of(this).get(ListCustomerViewModel::class.java)
        model.getCustomer(this, tipe, "", itemCount).observe(this, {
            custList: ArrayList<Customer> ->
                run {
                    isSearch = false
                    listCust.addAll(custList)
                    i++
                    adapterRV.notifyDataSetChanged()
                }
        })
        binding.progressbar.visibility = View.GONE
    }

    class AdapterRV(private val activity: Activity, private var rvCust: List<Customer>?) : RecyclerView.Adapter<AdapterRV.ViewHolder>(){

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = RvCustomerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.binding.cvMain.animation = AnimationUtils.loadAnimation(activity, R.anim.show_from_left)
            val kdcust = rvCust!![position].kdcust
            holder.binding.tvKdcust.text = rvCust!![position].kdcust
            holder.binding.tvNmcust.text = rvCust!![position].nmcust

            holder.binding.cvMain.setOnClickListener {
                val intent = Intent(activity, ListBarangSpecialPriceActivity::class.java)
                intent.putExtra("kdcust", kdcust)
                activity.startActivity(intent)
            }
        }

        override fun getItemCount(): Int {
            return rvCust!!.size
        }

        inner class ViewHolder(var binding: RvCustomerBinding) : RecyclerView.ViewHolder(binding.root)
    }

    @SuppressLint("InflateParams", "SetTextI18n")
    private fun dialogPilihCustomer(){
        val dialogPilihCust = BottomSheetDialog(this, R.style.SheetDialogNotif)
        val bindingDialogPilihCust = DialogPilihCustomerBinding.inflate(layoutInflater)
        val bottomSheetLayout = bindingDialogPilihCust.root
        dialogPilihCust.setContentView(bottomSheetLayout)
        dialogPilihCust.setCancelable(false)
        listCustSearch = ArrayList()

        bindingDialogPilihCust.progressbar.visibility = View.GONE
        bindingDialogPilihCust.tvTitle.text = "Input Customer"
        bindingDialogPilihCust.searchview.isActivated = true
        bindingDialogPilihCust.searchview.queryHint = resources.getString(R.string.kode_nama_customer)
        bindingDialogPilihCust.searchview.onActionViewExpanded()
        bindingDialogPilihCust.searchview.requestFocus()

        bindingDialogPilihCust.rvMain.setHasFixedSize(true)
        val layoutManager = LinearLayoutManager(this)
        bindingDialogPilihCust.rvMain.layoutManager = layoutManager
        adapterCust = AdapterCustomer(dialogPilihCust, this)
        bindingDialogPilihCust.rvMain.adapter = adapterCust

        bindingDialogPilihCust.searchview.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                searchCustomer(bindingDialogPilihCust.progressbar, query)
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                return false
            }
        })

        bindingDialogPilihCust.btnCancel.setOnClickListener { dialogPilihCust.dismiss() }

        dialogPilihCust.show()
    }

    private inner class AdapterCustomer(private var dialog: BottomSheetDialog, private var activity: Activity) : RecyclerView.Adapter<AdapterCustomer.ViewHolder>(){

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = RvCustomerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.binding.tvKdcust.text = listCustSearch[position].kdcust.toString()
            holder.binding.tvNmcust.text = listCustSearch[position].nmcust.toString()

            holder.binding.cvMain.setOnClickListener {
                val intent = Intent(activity, AddSpecialPriceActivity::class.java)
                intent.putExtra("kdcust", listCustSearch[position].kdcust)
                intent.removeExtra(AddSpecialPriceActivity.PARCELPRODUCT)
                dialog.dismiss()
                startActivity(intent)
            }
        }

        override fun getItemCount(): Int {
            return listCustSearch.size
        }

        inner class ViewHolder(val binding: RvCustomerBinding) : RecyclerView.ViewHolder(binding.root)
    }

    private fun searchCustomer(progressBar: ProgressBar, cust: String){
        progressBar.visibility = View.VISIBLE
        listCustSearch.clear()
        val model = ViewModelProviders.of(this).get(ListCustomerViewModel::class.java)
        model.getCustomer(this, "", cust, 0).observe(this, { custList: ArrayList<Customer> ->
            run {
                listCustSearch.addAll(custList)
                progressBar.visibility = View.GONE
                adapterCust.notifyDataSetChanged()
            }
        })
    }

}
package com.yusuffahrudin.masuyamobileapp.salesorder

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.yusuffahrudin.masuyamobileapp.R
import com.yusuffahrudin.masuyamobileapp.api.API
import com.yusuffahrudin.masuyamobileapp.api.ApiService
import com.yusuffahrudin.masuyamobileapp.controller.DialogAlert
import com.yusuffahrudin.masuyamobileapp.data.Result
import com.yusuffahrudin.masuyamobileapp.data.sales_order.NoteOtorisasi
import com.yusuffahrudin.masuyamobileapp.data.sales_order.NoteOtorisasiResponse
import com.yusuffahrudin.masuyamobileapp.data.sales_order.SalesOrder
import com.yusuffahrudin.masuyamobileapp.databinding.*
import com.yusuffahrudin.masuyamobileapp.firebase.SendNotification
import com.yusuffahrudin.masuyamobileapp.util.SessionManager
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class OtorisasiSOActivity : AppCompatActivity() {
    private lateinit var adapter: AdapterRVSO
    private lateinit var adapterAutho: ArrayAdapter<String>
    private lateinit var adapterStatusAutho: ArrayAdapter<String>
    private lateinit var name: String
    private lateinit var kdkota: String
    private var underCost = false
    private var i = 0
    private var nobukti: String     = ""
    private var cust: String        = ""
    private var autho: String       = "-"
    private var status: String       = "All"
    private var item: String       = "All"
    private lateinit var alasanARArray: MutableList<String>
    private lateinit var adapterAlasanAR: ArrayAdapter<String>
    private lateinit var alasanSLArray: MutableList<String>
    private lateinit var adapterAlasanSL: ArrayAdapter<String>
    private var listAlasan: ArrayList<NoteOtorisasi> = ArrayList()
    private lateinit var binding: ActivityOtorisasiSoBinding
    private lateinit var bindingDialog: DialogOtorisasiSoBinding
    companion object {
        lateinit var listSO: ArrayList<SalesOrder>
        lateinit var levelManager: String
        lateinit var levelSpv: String
        lateinit var level: String
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOtorisasiSoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        if (supportActionBar != null) {
            supportActionBar!!.setDisplayShowHomeEnabled(true)
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        }
        this.title = "List Otorisasi SO"
        println("============on-here-SO==========================")
        val sessionManager = SessionManager(applicationContext)
        val session = sessionManager.userDetails
        name = session[SessionManager.kunci_email].toString()
        level = session[SessionManager.level].toString()
        kdkota = session[SessionManager.kdkota].toString()
        if (session[SessionManager.underCost].toString() == "1"){
            autho = "OtoUC"
            underCost = true
        } else if (session[SessionManager.underBottomSP].toString() == "1"){
            autho = "OtoODSL"
        } else if (session[SessionManager.underBottomSO].toString() == "1"){
            autho = "OtoUB"
        } else if (session[SessionManager.level].toString() == "10101020" || session[SessionManager.level].toString() == "1010102010"){
            autho = "OtoODAR"
        }

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.rvMain.setHasFixedSize(true)
        val layoutManager = LinearLayoutManager(applicationContext)
        binding.rvMain.layoutManager = layoutManager
        binding.rvMain.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        binding.progressbar.visibility = View.GONE

        listSO = ArrayList()
        adapter = AdapterRVSO(this, underCost)
        binding.rvMain.adapter = adapter

        if (listSO.isEmpty()) {
            selectSO()
        }

        binding.btnAccept.setOnClickListener {
            if (level == "10101020" || level == "1010102010" || level == "101020" || level == "10102010" || level == "101030" || level == "10103010")
                bottomOto(binding.btnAccept.text.toString())
            else
                DialogAlert(getString(R.string.tidak_mempunyai_hak_akses), "attention", this)
        }
        binding.btnReject.setOnClickListener {
            if (level == "10101020" || level == "1010102010")
                bottomOto(binding.btnReject.text.toString())
            else
                DialogAlert(getString(R.string.tidak_mempunyai_hak_akses), "attention", this)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_refresh_filter_select_all, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        println("============on-here-OIS==========================")
        when (item.itemId) {
            android.R.id.home -> {
                finish()
            }
            R.id.action_refresh -> {
                reload()
            }
            R.id.action_filter -> {
                bottomFilter()
            }
            R.id.action_select_all -> {
                if (item.title.toString().equals("Select All", true)) {
                    for (i in 0 until listSO.size) {
                        listSO[i].ischecked = true
                        adapter.notifyDataSetChanged()
                    }
                    item.title = getString(R.string.unselect_all)
                } else {
                    for (i in 0 until listSO.size) {
                        listSO[i].ischecked = false
                        adapter.notifyDataSetChanged()
                    }
                    item.title = getString(R.string.select_all)
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun reload() {
        listSO.clear()
        selectSO()
    }

    private fun filter(nobukti: String, cust: String, autho: String, status: String, item: String){
        listSO.clear()
        this.nobukti = nobukti
        this.cust = cust
        this.autho = autho
        this.status = status
        this.item = item

        selectSO()
    }

    private fun selectSO(){
        binding.progressbar.visibility = View.VISIBLE
        println("================parsing json====================")
        var user = ""
        if (level.equals("1010201010", ignoreCase = true) || level.equals("1010301010", ignoreCase = true)) {
            user = name
        }
        val model = ViewModelProviders.of(this).get(SalesOrderViewModel::class.java)
        model.getSO(this, user, 0, "Otorisasi", nobukti, cust, autho, status, item).observe(this, { soList: ArrayList<SalesOrder> ->
            run {
                for (i in 0 until soList.size) {
                    println("===================SO LIST==================")
                    println(soList[i].nobukti)
                    println(soList[i].needOtoUB)
                    println(soList[i].otoUB)
                    println("===========================================")

                    //logic lama
//                    if (i == 0) {
//                        listSO.add(soList[i])
//
//
//                    }

                    if (i == 0) {
                        println("===========in i == 0==========")
                        listSO.add(soList[i])
                        if( listSO[0].otoUB == "0"){
                            listSO[0].otoUB = "null"
                            listSO[0].otoUBby = "null"
                        }
                        if (listSO[0].otoUC == "0"){
                            listSO[0].otoUC = "null"
                            listSO[0].otoUCby = "null"
                        }

                    } else {
                        println("==================no bukti====================")
                        println(soList[i].nobukti)
                        val index = listSO.size
                        val nobukti = soList[i].nobukti
                        val needOtoUc = soList[i].needOtoUC
                        val needOtoUb = soList[i].needOtoUB
                        val otoUc = soList[i].otoUC.toString()
                        val otoUb = soList[i].otoUB.toString()
                        print("needotouc ")
                        println(needOtoUc)
                        print("prevneedotouc ")
                        println(listSO[index - 1].needOtoUC)
                        print("needotoub ")
                        println(needOtoUb)
                        print("prevneedotoub ")
                        println(listSO[index - 1].needOtoUB)

                        //logic lama
//                        if (listSO[index - 1].nobukti == nobukti) {
//                            if (!listSO[index - 1].needOtoUC && needOtoUc) listSO[index - 1].needOtoUC = needOtoUc
//                            else if (listSO[index - 1].needOtoUC && needOtoUc) {
//                                if (listSO[index - 1].otoUC == "1" && otoUc == "null") {
//                                    listSO[index - 1].otoUC = otoUc
//                                    listSO[index - 1].otoUCby = otoUc
//                                }
//                            }
//                            if (!listSO[index - 1].needOtoUB && needOtoUb) {
//                                listSO[index - 1].needOtoUB = needOtoUb
//
//                            }
//                            else if (listSO[index - 1].needOtoUB && needOtoUb) {
//                                if (listSO[index - 1].otoUB == "1" && otoUb == "null") {
//                                    listSO[index - 1].otoUB = otoUb
//                                    listSO[index - 1].otoUBby = otoUb
//                                }
//
//                            }
//                        }

                        if (listSO[index - 1].nobukti == nobukti) {
                            if (!listSO[index - 1].needOtoUC && needOtoUc) {

                                listSO[index - 1].needOtoUC = needOtoUc
                                if (listSO[index - 1].otoUC == "1" && otoUc == "null" || listSO[index - 1].otoUC == "1" && otoUc == "0") {
                                    listSO[index - 1].otoUC = "null"
                                    listSO[index - 1].otoUCby = "null"
                                }
                                if (listSO[index - 1].otoUC == "0"){
                                    listSO[index - 1].otoUC = "null"
                                    listSO[index - 1].otoUCby = "null"
                                }
                                if(listSO[index -1].otoUC.toString() == "null" && otoUc =="1"){
                                    listSO[index - 1].otoUC = otoUc
                                    listSO[index - 1].otoUCby = soList[i].otoUCby.toString()

                                }
                            }
                            else if (listSO[index - 1].needOtoUC && needOtoUc) {
                                println("=======oto UC true true============")
                                if (listSO[index - 1].otoUC == "1" && otoUc == "null" || listSO[index - 1].otoUC == "1" && otoUc == "0") {
                                    listSO[index - 1].otoUC = "null"
                                    listSO[index - 1].otoUCby = "null"
                                }
                            }
                            if (!listSO[index - 1].needOtoUB && needOtoUb) {
//                                println(listSO[index -1].otoUB)
//                                println(otoUb)
                                listSO[index - 1].needOtoUB = needOtoUb
                                if (listSO[index - 1].otoUB == "1" && otoUb == "null" || listSO[index - 1].otoUB == "1" && otoUb == "0") {
                                    listSO[index - 1].otoUB = "null"
                                    listSO[index - 1].otoUBby = "null"
                                }
                                if (listSO[index - 1].otoUB == "0"){
                                    listSO[index - 1].otoUB = "null"
                                    listSO[index - 1].otoUBby = "null"
                                }
                                if(listSO[index -1].otoUB.toString() == "null" && otoUb =="1"){
                                    listSO[index - 1].otoUB = otoUb
                                    listSO[index - 1].otoUBby = soList[i].otoUBby.toString()

                                }

                            }
                            else if (listSO[index - 1].needOtoUB && needOtoUb) {
                                if (listSO[index - 1].otoUB == "1" && otoUb == "null" || listSO[index - 1].otoUB == "1" && otoUb == "0") {
                                    listSO[index - 1].otoUB = "null"
                                    listSO[index - 1].otoUBby = "null"
                                }

                            }
                        }

                        else {
                            println("==========ON ELSE=============")
                            listSO.add(soList[i])

                        }

//                        println("===finish======================")
                    }
                }
                i++
//                println("===========increment==========")
                //rvSo.recycledViewPool.clear()
                adapter.notifyDataSetChanged()
                binding.progressbar.visibility = View.GONE
            }
        })
    }

    private class AdapterRVSO(val activity: Activity, val uc: Boolean): RecyclerView.Adapter<AdapterRVSO.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = ListItemOtorisasiSoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(binding)
        }

        override fun getItemCount(): Int {
            return listSO.size
        }

        @SuppressLint("SetTextI18n")
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.binding.cvMain.animation = AnimationUtils.loadAnimation(activity, R.anim.show_from_left)
            holder.binding.line2.visibility = View.GONE
            holder.binding.imgOtoOdar.visibility = View.GONE
            holder.binding.tvOtoOdar.visibility = View.GONE
            holder.binding.imgOtoOdsl.visibility = View.GONE
            holder.binding.tvOtoOdsl.visibility = View.GONE
            holder.binding.line3.visibility = View.GONE
            holder.binding.imgOtoUc.visibility = View.GONE
            holder.binding.tvOtoUc.visibility = View.GONE
            holder.binding.imgOtoUb.visibility = View.GONE
            holder.binding.tvOtoUb.visibility = View.GONE
            holder.binding.cbOto.isEnabled = false
            println("=======debugging=ubautho==========")
            println(listSO[position].nobukti)
            println(listSO[position].needOtoUB)
            println(listSO[position].otoUB)
            println("==================================")

            when (listSO[position].kdLevel.toString().length) {
                10 -> {
                    levelManager = listSO[position].kdLevel.toString().substring(0, listSO[position].kdLevel.toString().length - 4)
                    levelSpv = listSO[position].kdLevel.toString().substring(0, listSO[position].kdLevel.toString().length - 2)
                }
                8 -> {
                    levelManager = listSO[position].kdLevel.toString().substring(0, listSO[position].kdLevel.toString().length - 2)
                    levelSpv = listSO[position].kdLevel.toString()
                }
                6 -> {
                    levelManager = listSO[position].kdLevel.toString()
                    levelSpv = "${listSO[position].kdLevel.toString()}10"
                }
                else -> {
                    levelManager = "101020"
                    levelSpv = "10102010"
                }
            }

            if (listSO[position].status.equals("Verified", ignoreCase = true) || listSO[position].status.equals("", ignoreCase = true)) {
                holder.binding.tvStatus.text = listSO[position].status
                holder.binding.tvStatus.setTextColor(ContextCompat.getColor(activity, R.color.flatui_nephritis))
            } else if (listSO[position].status!!.contains("!")) {
                holder.binding.tvStatus.text = activity.getString(R.string.peringatan)
                holder.binding.tvStatus.setTextColor(ContextCompat.getColor(activity, R.color.flatui_sun_flower))
                holder.binding.line2.visibility = View.VISIBLE
                holder.binding.imgOtoOdar.visibility = View.VISIBLE
                holder.binding.tvOtoOdar.visibility = View.VISIBLE
                //set status AR
                if (listSO[position].otoODAR == "1") {
                    holder.binding.imgOtoOdar.setImageResource(R.drawable.ic_ok_64)
                    holder.binding.tvOtoOdar.text = "${activity.getString(R.string.oto_by)} ${listSO[position].otoODARby?.toUpperCase(Locale.getDefault())}"
                    holder.binding.cbOto.isChecked = true;
                    if (listSO[position].ARSL.toString() == "1") {
                        //set status SM
                        holder.binding.imgOtoOdsl.visibility = View.VISIBLE
                        holder.binding.tvOtoOdsl.visibility = View.VISIBLE
                        when (listSO[position].otoODSL) {
                            "1" -> {
                                holder.binding.imgOtoOdsl.setImageResource(R.drawable.ic_ok_64)
                                holder.binding.tvOtoOdsl.text = "${activity.getString(R.string.oto_by)} ${listSO[position].otoODSLby?.toUpperCase(Locale.getDefault())}"
                            }
                            "0" -> {
                                holder.binding.imgOtoOdsl.setImageResource(R.drawable.ic_delete_48)
                                holder.binding.tvOtoOdsl.text = "Rejected by ${listSO[position].otoODSLby?.toUpperCase(Locale.getDefault())}"
                            }
                            else -> {
                                holder.binding.imgOtoOdsl.setImageResource(R.drawable.ic_error_48)
                                holder.binding.tvOtoOdsl.setText(R.string.required_oto_odsl)
                                holder.binding.cbOto.isEnabled = level == levelManager || level == levelSpv
                            }
                        }
                    }
                } else if(listSO[position].otoODAR == "0") {
                    holder.binding.imgOtoOdar.setImageResource(R.drawable.ic_delete_48)
                    holder.binding.tvOtoOdar.text = "Rejected by ${listSO[position].otoODARby?.toUpperCase(Locale.getDefault())}"
                    holder.binding.cbOto.isEnabled = level == "10101020" || level == "1010102010"
                    //set status SM
                    holder.binding.imgOtoOdsl.visibility = View.VISIBLE
                    holder.binding.tvOtoOdsl.visibility = View.VISIBLE
                    when (listSO[position].otoODSL) {
                        "1" -> {
                            holder.binding.imgOtoOdsl.setImageResource(R.drawable.ic_ok_64)
                            holder.binding.tvOtoOdsl.text = "${activity.getString(R.string.oto_by)} ${listSO[position].otoODSLby?.toUpperCase(Locale.getDefault())}"
                        }
                        "0" -> {
                            holder.binding.imgOtoOdsl.setImageResource(R.drawable.ic_delete_48)
                            holder.binding.tvOtoOdsl.text = "Rejected by ${listSO[position].otoODSLby?.toUpperCase(Locale.getDefault())}"
                        }
                        else -> {
                            holder.binding.imgOtoOdsl.setImageResource(R.drawable.ic_error_48)
                            holder.binding.tvOtoOdsl.setText(R.string.required_oto_odsl)
                            holder.binding.cbOto.isEnabled = level == levelManager
                        }
                    }
                } else {
                    holder.binding.imgOtoOdar.setImageResource(R.drawable.ic_error_48)
                    holder.binding.tvOtoOdar.setText(R.string.required_oto_odar)
                    println("---------------- nobukti ${listSO[position].nobukti}")
                    println("---------------- level $level")
                    holder.binding.cbOto.isEnabled = level == "10101020" || level == "1010102010"
                }

            }

            if (listSO[position].status!!.contains("harga jual min")){
                //set status SM
                holder.binding.tvStatus.text = activity.getString(R.string.peringatan)
                holder.binding.tvStatus.setTextColor(ContextCompat.getColor(activity, R.color.flatui_sun_flower))
                holder.binding.imgOtoUb.visibility = View.VISIBLE
                holder.binding.tvOtoUb.visibility = View.VISIBLE
                holder.binding.line3.visibility = View.VISIBLE
                println("no bukti = "+listSO[position].nobukti+"; needotoUB = "+listSO[position].needOtoUB+"; otoUB = "+listSO[position].otoUB)
                println("======kode barang============")
                println(listSO[position].kdbrg)
                println("=============================")
                if (listSO[position].needOtoUB && listSO[position].otoUB == "1") {
                    holder.binding.imgOtoUb.setImageResource(R.drawable.ic_ok_64)
                    holder.binding.tvOtoUb.text = "${activity.getString(R.string.oto_by)} ${listSO[position].otoUBby?.toUpperCase(Locale.getDefault())}"
                    if  (level == levelManager)
                        holder.binding.cbOto.isEnabled = true
                } else {
                    holder.binding.imgOtoUb.setImageResource(R.drawable.ic_error_48)
                    holder.binding.tvOtoUb.setText(R.string.required_oto_ub)
                }
            }

            if (listSO[position].status!!.contains("pokok")){
                holder.binding.tvStatus.text = activity.getString(R.string.peringatan)
                holder.binding.tvStatus.setTextColor(ContextCompat.getColor(activity, R.color.flatui_sun_flower))
                holder.binding.imgOtoUc.visibility = View.VISIBLE
                holder.binding.tvOtoUc.visibility = View.VISIBLE
                holder.binding.line3.visibility = View.VISIBLE
                //set status BM
                if (listSO[position].needOtoUC && listSO[position].otoUC == "1") {
                    holder.binding.imgOtoUc.setImageResource(R.drawable.ic_ok_64)
                    holder.binding.tvOtoUc.text = "${activity.getString(R.string.oto_by)} ${listSO[position].otoUCby?.toUpperCase(Locale.getDefault())}"
                    if (uc)
                        holder.binding.cbOto.isEnabled = true
                } else {
                    holder.binding.imgOtoUc.setImageResource(R.drawable.ic_error_48)
                    holder.binding.tvOtoUc.setText(R.string.required_oto_uc)
                }
            }

            val dateStr: String = listSO[position].tglcreate.toString()
            var sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            var date: Date? = null
            try {
                date = sdf.parse(dateStr)
            } catch (e: ParseException) {
                e.printStackTrace()
            }
            sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            val tgl = sdf.format(date!!)

            holder.binding.tvNobukti.text = listSO[position].nobukti
            holder.binding.tvNmcust.text = listSO[position].nmcust
            holder.binding.tvTgl.text = tgl
            holder.binding.tvNmsales.text = listSO[position].nmsales

            holder.binding.cvMain.setOnClickListener {
                val i = Intent(activity, DetailOtorisasiSOActivity::class.java)
                i.putExtra("nobukti", listSO[position].nobukti)
                i.putExtra("nmsales", listSO[position].nmsales)
                activity.startActivity(i)
            }

            holder.binding.tvStatus.setOnClickListener {
                println("==================()()()")
                if (!listSO[position].status.equals("Verified", ignoreCase = true) || !listSO[position].status.equals("", ignoreCase = true)) {
                    DialogAlert(listSO[position].status, "attention", activity)
                }
            }

            holder.binding.cbOto.setOnCheckedChangeListener { _, isChecked ->
                listSO[position].ischecked = isChecked
            }
            //listSO[position] selalu false, tidak bisa dijadikan acuhan untuk checked cbOto
            //holder.binding.cbOto.isChecked = listSO[position].ischecked!!
        }

        inner class ViewHolder(val binding: ListItemOtorisasiSoBinding) : RecyclerView.ViewHolder(binding.root)
    }

    @SuppressLint("SetTextI18n")
    private fun bottomOto(button: String){
        val dialogOto = Dialog(this)
        bindingDialog = DialogOtorisasiSoBinding.inflate(layoutInflater)
        val bottomLayout = bindingDialog.root
        val slideUp = AnimationUtils.loadAnimation(this, R.anim.show_from_bottom)
        dialogOto.setContentView(bottomLayout)

        if (button.equals(binding.btnAccept.text.toString(), true)) {
            if (level == "10101020" || level == "1010102010") getAlasanARAccept(this)
            else if (level == "101020" || level == "10102010" || level == "101030" || level == "10103010") getAlasanSLAccept(this)
        }
        else if (button.equals(binding.btnReject.text.toString(), true)) {
            if (level == "10101020" || level == "1010102010") getAlasanARReject(this)
        }
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)+1
        val day = c.get(Calendar.DAY_OF_MONTH)
        bindingDialog.edtTglAlasan.setText("$year-${if (month < 10) "0$month" else month}-${if (day < 10) "0$day" else day}")
        bindingDialog.edtTglAlasan.visibility = View.GONE
        bindingDialog.selectAlasanSo.setOnClickListener { bindingDialog.actAlasanOto.showDropDown() }
        bindingDialog.actAlasanOto.setOnItemClickListener { _, _, position, _ ->
            Toast.makeText(this, bindingDialog.actAlasanOto.adapter.getItem(position).toString(), Toast.LENGTH_LONG).show()
            if (bindingDialog.actAlasanOto.adapter.getItem(position).toString().equals("Schedule pembayaran mundur", false)) {
                bindingDialog.edtTglAlasan.visibility = View.VISIBLE
                //bindingDialog.edtTglAlasan.setText("")
            } else {
                bindingDialog.edtTglAlasan.visibility = View.GONE
            }
        }

        bindingDialog.edtTglAlasan.setOnClickListener {
            val cal = Calendar.getInstance()
            val mYear = cal.get(Calendar.YEAR)
            val mMonth = cal.get(Calendar.MONTH)+1
            val mDay = cal.get(Calendar.DAY_OF_MONTH)
            cal.add(Calendar.DAY_OF_MONTH, 30)
            val datePickerDialog = DatePickerDialog(this, { _, year, monthOfYear, dayOfMonth ->
                bindingDialog.edtTglAlasan.setText("$year-${if (monthOfYear + 1 < 10) "0${monthOfYear + 1}" else monthOfYear + 1}-${if (dayOfMonth < 10) "0$dayOfMonth" else dayOfMonth}")
            }, mYear, mMonth, mDay)
            datePickerDialog.datePicker.minDate = System.currentTimeMillis() - 1000
            datePickerDialog.datePicker.maxDate = cal.timeInMillis
            datePickerDialog.show()
        }

        bindingDialog.btnCancel.setOnClickListener {
            dialogOto.dismiss()
        }

        bindingDialog.btnOk.setOnClickListener {
            if (bindingDialog.actAlasanOto.text.toString() == "" || bindingDialog.actAlasanOto.text.toString() == "null"){
                DialogAlert("Alasan tidak boleh kosong..!", "attention", this)
            } else {
                val paramItem = JSONArray()
                var arrayItem: JSONObject?
                try {
                    for (i in listSO.indices) {
                        if (listSO[i].ischecked!!) {
                            arrayItem = JSONObject()
                            arrayItem.put("nobukti", listSO[i].nobukti)
                            arrayItem.put("nmcust", listSO[i].nmcust)
                            arrayItem.put("nmsales", listSO[i].nmsales)
                            paramItem.put(arrayItem)
                        }
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                    FirebaseCrashlytics.getInstance().recordException(e)
                }


                if (paramItem.length() == 0){
                    DialogAlert("Tidak ada SO yang dipilih!\n Pilih minimal satu SO!", "attention", this)
                } else {
                    if (button.equals(binding.btnAccept.text.toString(), true)) {
                        dialogOto.dismiss()
                        updateAutho(
                            "1",
                            name,
                            bindingDialog.actAlasanOto.text.toString(),
                            bindingDialog.edtKet.text.toString(),
                            bindingDialog.edtTglAlasan.text.toString(),
                            paramItem
                        )
                    }
                    else if (button.equals(binding.btnReject.text.toString(), true)) {
                        dialogOto.dismiss()
                        updateAutho(
                            "0",
                            name,
                            bindingDialog.actAlasanOto.text.toString(),
                            bindingDialog.edtKet.text.toString(),
                            bindingDialog.edtTglAlasan.text.toString(),
                            paramItem
                        )
                    }
                }
            }
        }

        bottomLayout.startAnimation(slideUp)
        dialogOto.show()
    }

    private fun updateAutho(oto: String, who: String, alasan: String, ket: String, tglAlasan: String, paramItem: JSONArray) {
        binding.progressbar.visibility = View.VISIBLE
        doAsync {
            val params = HashMap<String?, String?>()
            //JSONArray List Item Order
            params["oto"] = oto
            params["level"] = level
            params["who"] = who
            params["alasan"] = alasan
            params["ket"] = ket
            params["tglAlasan"] = tglAlasan
            params["listOto"] = paramItem.toString()
            API.instance().create(ApiService::class.java)
                    .updateOtoODMultiSO(params)
                    ?.enqueue(object : Callback<Result?> {
                        override fun onFailure(call: Call<Result?>, e: Throwable) {
                            binding.progressbar.visibility = View.GONE
                            DialogAlert(e.message, "error", this@OtorisasiSOActivity)
                        }

                        override fun onResponse(call: Call<Result?>, response: retrofit2.Response<Result?>) {
                            if (response.isSuccessful) {
                                val message = response.body()?.message.toString()
                                if (response.body()?.success == 1) {
                                    val notif = SendNotification()
                                    for (i in 0 until paramItem.length()) {
                                        val jsonObj: JSONObject = paramItem.getJSONObject(i)
                                        if (oto == "1") {
                                            notif.pushNotifSales(this@OtorisasiSOActivity, kdkota, "${jsonObj.getString("nobukti")} - SO Accepted by $who",
                                                    jsonObj.getString("nmcust"),
                                                    "SO",
                                                    jsonObj.getString("nmsales"))
                                        } else {
                                            notif.pushNotifSales(this@OtorisasiSOActivity, kdkota, "${jsonObj.getString("nobukti")} - SO Rejected by $who",
                                                    jsonObj.getString("nmcust"),
                                                    "SO",
                                                    jsonObj.getString("nmsales"))
                                        }
                                    }
                                    binding.progressbar.visibility = View.GONE

                                    DialogAlert(message, "success", this@OtorisasiSOActivity)
                                    listSO.clear()
                                    selectSO()
                                } else {
                                    binding.progressbar.visibility = View.GONE
                                    DialogAlert(message, "error", this@OtorisasiSOActivity)
                                }
                            }
                        }
                    })
        }
    }

    private fun getAlasanARAccept(activity: Activity){
        binding.progressbar.visibility = View.VISIBLE
        doAsync {
            listAlasan.clear()
            alasanARArray = mutableListOf()
            API.instance().create(ApiService::class.java)
                    .noteARAccept
                    ?.enqueue(object : Callback<NoteOtorisasiResponse?> {
                        override fun onFailure(call: Call<NoteOtorisasiResponse?>, e: Throwable) {
                            DialogAlert(e.message, "error", activity)
                            Log.d("Get Note AR", "Error $e")
                            FirebaseCrashlytics.getInstance().recordException(e)
                        }

                        override fun onResponse(call: Call<NoteOtorisasiResponse?>, response: retrofit2.Response<NoteOtorisasiResponse?>) {
                            if (response.code() == 200) {
                                listAlasan.addAll(response.body()?.result!!)
                                for (i in 0 until response.body()?.result!!.size) {
                                    alasanARArray.add(response.body()?.result!![i].note.toString())
                                }
                                adapterAlasanAR = ArrayAdapter(activity, R.layout.spinner_black, alasanARArray)
                                bindingDialog.actAlasanOto.setAdapter(adapterAlasanAR)
                                //getNoteSM()
                            }
                        }
                    })
            uiThread { binding.progressbar.visibility = View.GONE }
        }
    }

    private fun getAlasanARReject(activity: Activity){
        binding.progressbar.visibility = View.VISIBLE
        doAsync {
            listAlasan.clear()
            alasanARArray = mutableListOf()
            API.instance().create(ApiService::class.java)
                    .noteARReject
                    ?.enqueue(object : Callback<NoteOtorisasiResponse?> {
                        override fun onFailure(call: Call<NoteOtorisasiResponse?>, e: Throwable) {
                            DialogAlert(e.message, "error", activity)
                            Log.d("Get Note AR", "Error $e")
                            FirebaseCrashlytics.getInstance().recordException(e)
                        }

                        override fun onResponse(call: Call<NoteOtorisasiResponse?>, response: retrofit2.Response<NoteOtorisasiResponse?>) {
                            if (response.code() == 200) {
                                listAlasan.addAll(response.body()?.result!!)
                                for (i in 0 until response.body()?.result!!.size) {
                                    alasanARArray.add(response.body()?.result!![i].note.toString())
                                }
                                adapterAlasanAR = ArrayAdapter(activity, R.layout.spinner_black, alasanARArray)
                                bindingDialog.actAlasanOto.setAdapter(adapterAlasanAR)
                                //getNoteSM()
                            }
                        }
                    })
            uiThread { binding.progressbar.visibility = View.GONE }
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
        adapterAutho = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, listAutho)
        adapterStatusAutho = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, listStatusAutho)
        bindingDialogFilter.spinAutho.setAdapter(adapterAutho)
        bindingDialogFilter.spinAutho.setLabel("Sort By")
        bindingDialogFilter.spinAutho.setErrorEnabled(false)
        bindingDialogFilter.spinStatusAutho.setAdapter(adapterStatusAutho)
        bindingDialogFilter.spinStatusAutho.setLabel("Authorization Status")
        bindingDialogFilter.spinStatusAutho.setErrorEnabled(false)
        bindingDialogFilter.edtNobukti.setText(nobukti)
        bindingDialogFilter.edtCust.setText(cust)
        bindingDialogFilter.spinItem.visibility = View.GONE

        when(autho) {
            "-" -> bindingDialogFilter.spinAutho.getSpinner().setSelection(0)
            "OtoODAR" -> bindingDialogFilter.spinAutho.getSpinner().setSelection(1)
            "OtoODSL" -> bindingDialogFilter.spinAutho.getSpinner().setSelection(2)
            "OtoUC" -> bindingDialogFilter.spinAutho.getSpinner().setSelection(3)
            "OtoUB" -> bindingDialogFilter.spinAutho.getSpinner().setSelection(4)
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
                "-" -> autho = "-"
                resources.getString(R.string.oto_odar) -> autho = "OtoODAR"
                resources.getString(R.string.oto_odsl) -> autho = "OtoODSL"
                resources.getString(R.string.oto_uc) -> autho = "OtoUC"
                resources.getString(R.string.oto_ub) -> autho = "OtoUB"
            }
            status = bindingDialogFilter.spinStatusAutho.getSpinner().selectedItem.toString()
            filter(nobukti, cust, autho, status, item)
            dialogFilter.dismiss()
        }

        bindingDialogFilter.btnReset.setOnClickListener {
            bindingDialogFilter.edtNobukti.setText("")
            bindingDialogFilter.edtCust.setText("")
            bindingDialogFilter.spinAutho.getSpinner().setSelection(0)
            bindingDialogFilter.spinStatusAutho.getSpinner().setSelection(0)
            autho = "-"
            status = "All"
        }

        bottomLayout.startAnimation(slideUp)
        dialogFilter.show()
    }

    private fun getAlasanSLAccept(activity: Activity){
        binding.progressbar.visibility = View.VISIBLE
        doAsync {
            alasanSLArray = mutableListOf()
            API.instance().create(ApiService::class.java)
                    .noteSLAccept
                    ?.enqueue(object : Callback<NoteOtorisasiResponse?> {
                        override fun onFailure(call: Call<NoteOtorisasiResponse?>, e: Throwable) {
                            DialogAlert(e.message, "error", activity)
                            Log.d("Get Note SL", "Error $e")
                            FirebaseCrashlytics.getInstance().recordException(e)
                        }

                        override fun onResponse(call: Call<NoteOtorisasiResponse?>, response: retrofit2.Response<NoteOtorisasiResponse?>) {
                            if (response.code() == 200) {
                                for (i in 0 until response.body()?.result!!.size) {
                                    alasanSLArray.add(response.body()?.result!![i].note.toString())
                                }
                                adapterAlasanSL = ArrayAdapter(activity, R.layout.spinner_black, alasanSLArray)
                                bindingDialog.actAlasanOto.setAdapter(adapterAlasanSL)
                            }
                        }
                    })
            uiThread { binding.progressbar.visibility = View.GONE }
        }
    }
}
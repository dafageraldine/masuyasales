package com.yusuffahrudin.masuyamobileapp.historypenjualan

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.yusuffahrudin.masuyamobileapp.R
import com.yusuffahrudin.masuyamobileapp.data.ArrayTampung
import com.yusuffahrudin.masuyamobileapp.data.UserAkses
import com.yusuffahrudin.masuyamobileapp.data.history_penjualan.HistoryPenjualan
import com.yusuffahrudin.masuyamobileapp.databinding.ActivityListHistoryPenjualanBinding
import com.yusuffahrudin.masuyamobileapp.util.SessionManager
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class ListHistoryPenjualanActivity : AppCompatActivity() {
    private lateinit var nmbrg: String
    private lateinit var customer: String
    private lateinit var sales: String
    private lateinit var from_tgl: String
    private lateinit var to_tgl: String
    private lateinit var kdkota: String
    private lateinit var listAdapter: ExpandableAdapter
    private val listDataGroup: MutableList<String> = ArrayList()
    //private val listDataHeader: MutableList<List<HistoryPenjualan>> = ArrayList()
    //private val listDataChild : MutableList<LinkedHashMap<String, List<HistoryPenjualan>>> = ArrayList()
    private val listDataChild : LinkedHashMap<String, List<HistoryPenjualan>> = LinkedHashMap()
    private lateinit var binding: ActivityListHistoryPenjualanBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.title          = getString(R.string.list_histori_penjualan)
        binding             = ActivityListHistoryPenjualanBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        if (supportActionBar != null) {
            supportActionBar!!.setDisplayShowHomeEnabled(true)
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        }
        val sessionManager  = SessionManager(applicationContext)
        val user            = sessionManager.userDetails
        kdkota              = user[SessionManager.kdkota].toString()
        //get parameter dari intent
        val intent          = intent
        nmbrg               = intent.extras?.getString("nmbrg").toString()
        customer            = intent.extras?.getString("customer").toString()
        sales               = intent.extras?.getString("sales").toString()
        from_tgl            = intent.extras?.getString("from_tgl").toString()
        to_tgl              = intent.extras?.getString("to_tgl").toString()
        // get the listview
        binding.progressbar.visibility = View.GONE
        listAdapter         = ExpandableAdapter(listDataGroup, listDataChild)
        binding.lvExp.setAdapter(listAdapter)/*
        expListView.setOnGroupExpandListener {
            var previousGroup = -1
            if (it != previousGroup){
                expListView.collapseGroup(previousGroup)
                previousGroup = it
            }
        }*/
        // preparing list data
        selectHistoryPenjualan()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }
/*
    //fungsi untuk select data dari database
    private fun selectHistoryPenjualan() {
        listDataGroup.clear()
        listDataHeader.clear()
        listDataChild.clear()
        progressBar.visibility = View.VISIBLE
        val model = ViewModelProviders.of(this).get(HistoryPenjualanViewModel::class.java)
        model.getHistoryPenjualan(this, nmbrg, customer, sales, from_tgl, to_tgl, kdkota).observe(this, Observer {
            historyList: ArrayList<HistoryPenjualan> ->
            run {
                for (i in historyList.indices) {
                    if (!historyList[i].nmcust.toString().equals(nmcust, true)) {
                        listDataGroup.add(historyList[i].nmcust.toString())
                        nmcust = historyList[i].nmcust.toString()
                        val secondList: MutableList<HistoryPenjualan> = ArrayList()
                        val thirdHashMap: LinkedHashMap<String, List<HistoryPenjualan>> = LinkedHashMap()
                        for (j in historyList.indices) {
                            if (historyList[j].nmcust.toString().equals(nmcust, true) && !historyList[j].nobukti.toString().equals(invoice, true)){
                                invoice = historyList[j].nobukti.toString()
                                val secondItem = HistoryPenjualan()
                                val dateStr = historyList[j].tgl.toString()
                                var sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                                val date = sdf.parse(dateStr)
                                sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                                val tgl = sdf.format(date)
                                secondItem.nobukti = historyList[j].nobukti
                                secondItem.nopo = historyList[j].nopo
                                secondItem.tgl = tgl
                                secondItem.discfak = historyList[j].discfak
                                secondItem.total = historyList[j].total
                                secondItem.cetak = historyList[j].cetak
                                secondItem.kirim = historyList[j].kirim
                                secondItem.status = historyList[j].status
                                secondItem.penyiapan = historyList[j].penyiapan
                                secondItem.diterima = historyList[j].diterima
                                secondItem.kembali = historyList[j].kembali
                                secondItem.lunas = historyList[j].lunas
                                secondList.add(secondItem)
                                val thirdList: MutableList<HistoryPenjualan> = ArrayList()
                                for (k in historyList.indices){
                                    val thirdItem = HistoryPenjualan()
                                    if (historyList[k].nobukti.toString().equals(invoice, true)) {
                                        thirdItem.kdbrg = historyList[k].kdbrg
                                        thirdItem.nmbrg = historyList[k].nmbrg
                                        thirdItem.qty = historyList[k].qty
                                        thirdItem.satuan = historyList[k].satuan
                                        thirdItem.hrg = historyList[k].hrg
                                        thirdItem.diskon1 = historyList[k].diskon1
                                        thirdItem.diskon2 = historyList[k].diskon2
                                        thirdItem.diskon3 = historyList[k].diskon3
                                        //menambah item ke array
                                        thirdList.add(thirdItem)
                                    }
                                }
                                thirdHashMap[invoice] = thirdList
                            }
                        }
                        listDataChild.add(thirdHashMap)
                        listDataHeader.add(secondList)
                    }
                }
                progressBar.visibility = View.GONE
                listAdapter.notifyDataSetChanged()
            }
        })
    }

    private class FirstLevelAdapter(private val listDataGroup: List<String>,
                                    private val listDataHeader: List<List<HistoryPenjualan>>,
                                    private val listDataChild: List<LinkedHashMap<String, List<HistoryPenjualan>>>): BaseExpandableListAdapter(){
        override fun getGroup(groupPosition: Int): Any {
            return listDataGroup[groupPosition]
        }

        override fun getGroupId(groupPosition: Int): Long {
            return groupPosition.toLong()
        }

        override fun getGroupCount(): Int {
            return listDataGroup.size
        }

        override fun getGroupView(groupPosition: Int, p1: Boolean, convertView: View?, parent: ViewGroup?): View? {
            var view = convertView
            val headerTitle = getGroup(groupPosition) as String
            if (view == null) {
                view = LayoutInflater.from(parent?.context).inflate(R.layout.list_group_history, parent, false)
            }
            val tvCustHeader = view?.findViewById<TextView>(R.id.tv_customer)
            tvCustHeader?.text = headerTitle
            return view
        }

        override fun hasStableIds(): Boolean {
            return true
        }

        override fun isChildSelectable(p0: Int, p1: Int): Boolean {
            return true
        }

        override fun getChildrenCount(headerPosition: Int): Int {
            return 1
        }

        override fun getChild(groupPosition: Int, headerPosition: Int): Any {
            return listDataHeader[groupPosition]
        }

        override fun getChildId(groupPosition: Int, headerPosition: Int): Long {
            return groupPosition.toLong()
        }

        override fun getChildView(groupPosition: Int, childPosition: Int, p2: Boolean, p3: View?, parent: ViewGroup?): View {
            val secondLevelELV = SecondLevelExpandableListView(parent?.context)
            val listHeader = listDataHeader[groupPosition]
            val listChild: MutableList<List<HistoryPenjualan>> = ArrayList()
            val childData: LinkedHashMap<String, List<HistoryPenjualan>> = listDataChild[groupPosition]
            childData.forEach { (_, value) ->
                listChild.add(value)
            }
            secondLevelELV.setAdapter(SecondLevelAdapter(listHeader, listChild))
            secondLevelELV.setGroupIndicator(null)
            secondLevelELV.setOnGroupExpandListener {
                var previousGroup = -1
                if (it != previousGroup){
                    secondLevelELV.collapseGroup(previousGroup)
                    previousGroup = it
                }
            }
            return secondLevelELV
        }

    }

    private class SecondLevelExpandableListView(context: Context?) : ExpandableListView(context) {
        override fun onMeasure(width: Int, height: Int) {
            //999999 is a size in pixels. ExpandableListView requires a maximum height in order to do measurement calculations.
            val heightMeasureSpec = MeasureSpec.makeMeasureSpec(999999, MeasureSpec.AT_MOST)
            super.onMeasure(width, heightMeasureSpec)
        }
    }

    private class SecondLevelAdapter(private val listDataHeader: List<HistoryPenjualan>,
                                     private val listDataChild: List<List<HistoryPenjualan>>): BaseExpandableListAdapter() {

        private val nf = NumberFormat.getInstance(Locale("in", "ID"))
        private val listAkses: List<UserAkses> = ArrayTampung.getListAkses()

        override fun getGroup(headerPosition: Int): HistoryPenjualan {
            return listDataHeader[headerPosition]
        }

        override fun getGroupId(headerPosition: Int): Long {
            return headerPosition.toLong()
        }

        override fun getGroupCount(): Int {
            return listDataHeader.size
        }

        @SuppressLint("SetTextI18n")
        override fun getGroupView(headerPosition: Int, p1: Boolean, convertView: View?, parent: ViewGroup?): View? {
            var view = convertView
            if (view == null) {
                view = LayoutInflater.from(parent?.context).inflate(R.layout.list_header_history, parent, false)
            }
            val tvNomor = view?.findViewById<TextView>(R.id.tv_nomor)
            val tvTgl = view?.findViewById<TextView>(R.id.tv_tgl)
            val tvNopo = view?.findViewById<TextView>(R.id.tv_noPO)
            val tvDiscFak = view?.findViewById<TextView>(R.id.tv_discfak)
            val tvTotal = view?.findViewById<TextView>(R.id.tv_total)
            val tvCetak = view?.findViewById<TextView>(R.id.tv_cetak)
            val tvSiap = view?.findViewById<TextView>(R.id.tv_siap)
            val tvKirim = view?.findViewById<TextView>(R.id.tv_kirim)
            val tvTerima = view?.findViewById<TextView>(R.id.tv_terima)
            val tvKembali = view?.findViewById<TextView>(R.id.tv_kembali)
            val tvLunas = view?.findViewById<TextView>(R.id.tv_lunas)

            tvNomor?.text = getGroup(headerPosition).nobukti
            tvTgl?.text = getGroup(headerPosition).tgl
            tvNopo?.text = getGroup(headerPosition).nopo
            tvDiscFak?.text = "${parent?.context?.resources?.getString(R.string.disc_faktur)} ${nf.format(getGroup(headerPosition).discfak)}%"
            tvTotal?.text = nf.format(getGroup(headerPosition).total)
            //set status cetak
            if (getGroup(headerPosition).cetak == "0") {
                val top = ContextCompat.getDrawable(parent!!.context, R.drawable.icons8_print0)
                tvCetak?.setCompoundDrawablesWithIntrinsicBounds(null, top, null, null)
                tvCetak?.setTextColor(ContextCompat.getColor(parent.context, R.color.flatui_concrete))
            } else {
                val top = ContextCompat.getDrawable(parent!!.context, R.drawable.icons8_print1)
                tvCetak?.setCompoundDrawablesWithIntrinsicBounds(null, top, null, null)
                tvCetak?.setTextColor(ContextCompat.getColor(parent.context, R.color.colorAccent))
            }
            //set status kirim
            if (getGroup(headerPosition).kirim == "0") {
                val top = ContextCompat.getDrawable(parent.context, R.drawable.icons8_truck0)
                tvKirim?.text = getGroup(headerPosition).status
                tvKirim?.setCompoundDrawablesWithIntrinsicBounds(null, top, null, null)
                tvKirim?.setTextColor(ContextCompat.getColor(parent.context, R.color.flatui_concrete))
            } else {
                val top = ContextCompat.getDrawable(parent.context, R.drawable.icons8_truck1)
                if (!getGroup(headerPosition).status.equals("")){
                    tvKirim?.text = getGroup(headerPosition).status
                }
                tvKirim?.setCompoundDrawablesWithIntrinsicBounds(null, top, null, null)
                tvKirim?.setTextColor(ContextCompat.getColor(parent.context, R.color.colorAccent))
            }
            //set status penyiapan
            if (getGroup(headerPosition).penyiapan == "0") {
                val top = ContextCompat.getDrawable(parent.context, R.drawable.icons8_packing0)
                tvSiap?.setCompoundDrawablesWithIntrinsicBounds(null, top, null, null)
                tvSiap?.setTextColor(ContextCompat.getColor(parent.context, R.color.flatui_concrete))
            } else {
                val top = ContextCompat.getDrawable(parent.context, R.drawable.icons8_packing1)
                tvSiap?.setCompoundDrawablesWithIntrinsicBounds(null, top, null, null)
                tvSiap?.setTextColor(ContextCompat.getColor(parent.context, R.color.colorAccent))
            }
            //set status diterima
            if (getGroup(headerPosition).diterima == "0") {
                val top = ContextCompat.getDrawable(parent.context, R.drawable.icons8_goods_receive0)
                tvTerima?.setCompoundDrawablesWithIntrinsicBounds(null, top, null, null)
                tvTerima?.setTextColor(ContextCompat.getColor(parent.context, R.color.flatui_concrete))
            } else {
                val top = ContextCompat.getDrawable(parent.context, R.drawable.icons8_goods_receive1)
                tvTerima?.setCompoundDrawablesWithIntrinsicBounds(null, top, null, null)
                tvTerima?.setTextColor(ContextCompat.getColor(parent.context, R.color.colorAccent))
            }
            //set status kembali
            if (getGroup(headerPosition).kembali == "0") {
                val top = ContextCompat.getDrawable(parent.context, R.drawable.icons8_document_return0)
                tvKembali?.setCompoundDrawablesWithIntrinsicBounds(null, top, null, null)
                tvKembali?.setTextColor(ContextCompat.getColor(parent.context, R.color.flatui_concrete))
            } else {
                val top = ContextCompat.getDrawable(parent.context, R.drawable.icons8_document_return1)
                tvKembali?.setCompoundDrawablesWithIntrinsicBounds(null, top, null, null)
                tvKembali?.setTextColor(ContextCompat.getColor(parent.context, R.color.colorAccent))
            }
            //set status lunas
            if (getGroup(headerPosition).lunas == "0") {
                val top = ContextCompat.getDrawable(parent.context, R.drawable.icons8_invoice_paid0)
                tvLunas?.setCompoundDrawablesWithIntrinsicBounds(null, top, null, null)
                tvLunas?.setTextColor(ContextCompat.getColor(parent.context, R.color.flatui_concrete))
            } else {
                val top = ContextCompat.getDrawable(parent.context, R.drawable.icons8_invoice_paid1)
                tvLunas?.setCompoundDrawablesWithIntrinsicBounds(null, top, null, null)
                tvLunas?.setTextColor(ContextCompat.getColor(parent.context, R.color.colorAccent))
            }
            return view
        }

        override fun isChildSelectable(p0: Int, p1: Int): Boolean {
            return true
        }

        override fun hasStableIds(): Boolean {
            return true
        }

        override fun getChild(headerPosition: Int, childPosition: Int): HistoryPenjualan {
            val data = listDataChild[headerPosition]
            return data[childPosition]
        }

        override fun getChildId(headerPosition: Int, childPosition: Int): Long {
            return childPosition.toLong()
        }

        override fun getChildrenCount(headerPosition: Int): Int {
            return listDataChild[headerPosition].size
        }

        @SuppressLint("SetTextI18n")
        override fun getChildView(headerPosition: Int, childPosition: Int, isLastChild: Boolean, convertView: View?, parent: ViewGroup?): View? {
            var view = convertView
            val data = listDataChild[headerPosition][childPosition]
            if (view == null) {
                view = LayoutInflater.from(parent?.context).inflate(R.layout.list_item_history, parent, false)
            }
            val tvKdbrg = view?.findViewById<TextView>(R.id.tv_kdbrg)
            val tvNmbrg = view?.findViewById<TextView>(R.id.tv_nmbrg)
            val tvQty = view?.findViewById<TextView>(R.id.tv_qty)
            val tvSatuan = view?.findViewById<TextView>(R.id.tv_satuan)
            val tvDiskon1 = view?.findViewById<TextView>(R.id.tv_diskon1)
            val tvDiskon2 = view?.findViewById<TextView>(R.id.tv_diskon2)
            val tvDiskon3 = view?.findViewById<TextView>(R.id.tv_diskon3)
            val tvHarga = view?.findViewById<TextView>(R.id.tv_harga)

            tvKdbrg?.text = data.kdbrg
            tvNmbrg?.text = data.nmbrg
            tvSatuan?.text = data.satuan
            tvQty?.text = nf.format(data.qty)
            tvHarga?.text = nf.format(data.hrg)
            //set diskon 1
            tvDiskon1?.text = "Disc1 ${nf.format(data.diskon1)}%"
            if (nf.format(data.diskon1) != "0")
                tvDiskon1?.setTextColor(Color.RED)
            //set diskon 2
            tvDiskon2?.text = "Disc2 ${nf.format(data.diskon2)}%"
            if (nf.format(data.diskon2) != "0")
                tvDiskon2?.setTextColor(Color.RED)
            //set diskon 3
            tvDiskon3?.text = "Disc3 ${nf.format(data.diskon3)}%"
            if (nf.format(data.diskon3) != "0")
                tvDiskon3?.setTextColor(Color.RED)

            for (i in listAkses.indices) {
                val str = listAkses[i].modul
                val akses = str.substring(str.indexOf("-") + 1)
                if (akses.equals("Harga Jual", ignoreCase = true)) {
                    if (listAkses[i].akses == 1) {
                        tvHarga?.visibility = View.VISIBLE
                    } else {
                        tvHarga?.visibility = View.GONE
                    }
                }
                if (akses.equals("Diskon", ignoreCase = true)) {
                    if (listAkses[i].akses == 1) {
                        tvDiskon1?.visibility = View.VISIBLE
                        tvDiskon2?.visibility = View.VISIBLE
                        tvDiskon3?.visibility = View.VISIBLE
                    } else {
                        tvDiskon1?.visibility = View.GONE
                        tvDiskon2?.visibility = View.GONE
                        tvDiskon3?.visibility = View.GONE
                    }
                }
            }
            return view
        }
    } */

    //fungsi untuk select data dari database
    fun selectHistoryPenjualan() {
        listDataGroup.clear()
        listDataChild.clear()
        binding.progressbar.visibility = View.VISIBLE
        val model = ViewModelProviders.of(this).get(HistoryPenjualanViewModel::class.java)
        model.getHistoryPenjualan(this, nmbrg, customer, sales, from_tgl, to_tgl, kdkota).observe(this, Observer { historyList: ArrayList<HistoryPenjualan> ->
            run {
                var nmcust = ""
                for (i in historyList.indices) {
                    if (!historyList[i].nmcust.toString().equals(nmcust, true)) {
                        listDataGroup.add(historyList[i].nmcust.toString())
                        nmcust = historyList[i].nmcust.toString()
                        val secondList = ArrayList<HistoryPenjualan>()
                        for (j in historyList.indices) {
                            if (historyList[j].nmcust.toString().equals(nmcust, true)){
                                val secondItem = HistoryPenjualan()
                                var dateStr = historyList[j].tgl.toString()
                                var sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                                var date = sdf.parse(dateStr)
                                sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                                val tgl = sdf.format(date)
                                secondItem.nobukti      = historyList[j].nobukti
                                secondItem.nopo         = historyList[j].nopo
                                secondItem.tgl          = tgl
                                secondItem.discfak      = historyList[j].discfak
                                secondItem.total        = historyList[j].total
                                secondItem.cetak        = historyList[j].cetak
                                secondItem.kirim        = historyList[j].kirim
                                secondItem.status       = historyList[j].status
                                secondItem.penyiapan    = historyList[j].penyiapan
                                secondItem.diterima     = historyList[j].diterima
                                secondItem.kembali      = historyList[j].kembali
                                secondItem.lunas        = historyList[j].lunas
                                secondItem.kdbrg        = historyList[j].kdbrg
                                secondItem.nmbrg        = historyList[j].nmbrg
                                secondItem.qty          = historyList[j].qty
                                secondItem.satuan       = historyList[j].satuan
                                secondItem.hrg          = historyList[j].hrg
                                secondItem.diskon1      = historyList[j].diskon1
                                secondItem.diskon2      = historyList[j].diskon2
                                secondItem.diskon3      = historyList[j].diskon3

                                secondList.add(secondItem)
                            }
                        }
                        listDataChild[nmcust] = secondList
                    }
                }
                binding.progressbar.visibility = View.GONE
                listAdapter.notifyDataSetChanged()
            }
        })
    }

    private class ExpandableAdapter(private val listDataGroup: List<String>,
                                    private val listDataChild: LinkedHashMap<String, List<HistoryPenjualan>>): BaseExpandableListAdapter() {

        private val nf = NumberFormat.getInstance(Locale("in", "ID"))
        private val listAkses: List<UserAkses> = ArrayTampung.getListAkses()

        override fun getGroup(groupPosition: Int): Any {
            return listDataGroup[groupPosition]
        }

        override fun getGroupId(groupPosition: Int): Long {
            return groupPosition.toLong()
        }

        override fun getGroupCount(): Int {
            return listDataGroup.size
        }

        override fun getGroupView(groupPosition: Int, p1: Boolean, convertView: View?, parent: ViewGroup?): View? {
            var view        = convertView
            val headerTitle = getGroup(groupPosition) as String
            if (view == null) {
                view = LayoutInflater.from(parent?.context).inflate(R.layout.list_group_history, parent, false)
            }
            val tvCustHeader    = view?.findViewById<TextView>(R.id.tv_customer)
            tvCustHeader?.text  = headerTitle
            return view
        }

        override fun isChildSelectable(p0: Int, p1: Int): Boolean {
            return true
        }

        override fun hasStableIds(): Boolean {
            return true
        }

        override fun getChild(groupPosition: Int, childPosition: Int): HistoryPenjualan {
            val data = listDataChild[listDataGroup[groupPosition]]
            return data!![childPosition]
        }

        override fun getChildId(groupPosition: Int, childPosition: Int): Long {
            return childPosition.toLong()
        }

        override fun getChildrenCount(groupPosition: Int): Int {
            return listDataChild[listDataGroup[groupPosition]]!!.size
        }

        @SuppressLint("SetTextI18n")
        override fun getChildView(groupPosition: Int, childPosition: Int, isLastChild: Boolean, convertView: View?, parent: ViewGroup?): View? {
            var view        = convertView
            val data        = getChild(groupPosition, childPosition)
            if (view == null) {
                view = LayoutInflater.from(parent?.context).inflate(R.layout.list_item_history_dualevel, parent, false)
            }
            val tvNomor     = view?.findViewById<TextView>(R.id.tv_nomor)
            val tvTgl       = view?.findViewById<TextView>(R.id.tv_tgl)
            val tvNopo      = view?.findViewById<TextView>(R.id.tv_noPO)
            val tvDiscFak   = view?.findViewById<TextView>(R.id.tv_discfak)
            val tvTotal     = view?.findViewById<TextView>(R.id.tv_total)
            val tvCetak     = view?.findViewById<TextView>(R.id.tv_cetak)
            val tvSiap      = view?.findViewById<TextView>(R.id.tv_siap)
            val tvKirim     = view?.findViewById<TextView>(R.id.tv_kirim)
            val tvTerima    = view?.findViewById<TextView>(R.id.tv_terima)
            val tvKembali   = view?.findViewById<TextView>(R.id.tv_kembali)
            val tvLunas     = view?.findViewById<TextView>(R.id.tv_lunas)

            tvNomor?.text   = data.nobukti
            tvTgl?.text     = data.tgl
            tvNopo?.text    = data.nopo
            tvDiscFak?.text = "${parent?.context?.resources?.getString(R.string.disc_faktur)} ${nf.format(data.discfak)}%"
            tvTotal?.text   = nf.format(data.total!!.toDouble())
            //set status cetak
            if (data.cetak == "0") {
                val top = ContextCompat.getDrawable(parent!!.context, R.drawable.icons8_print0)
                tvCetak?.setCompoundDrawablesWithIntrinsicBounds(null, top, null, null)
                tvCetak?.setTextColor(ContextCompat.getColor(parent.context, R.color.flatui_concrete))
            } else {
                val top = ContextCompat.getDrawable(parent!!.context, R.drawable.icons8_print1)
                tvCetak?.setCompoundDrawablesWithIntrinsicBounds(null, top, null, null)
                tvCetak?.setTextColor(ContextCompat.getColor(parent.context, R.color.colorAccent))
            }
            //set status kirim
            if (data.kirim == "0") {
                val top = ContextCompat.getDrawable(parent.context, R.drawable.icons8_truck0)
                tvKirim?.text = data.status
                tvKirim?.setCompoundDrawablesWithIntrinsicBounds(null, top, null, null)
                tvKirim?.setTextColor(ContextCompat.getColor(parent.context, R.color.flatui_concrete))
            } else {
                val top = ContextCompat.getDrawable(parent.context, R.drawable.icons8_truck1)
                if (!data.status.equals("")){
                    tvKirim?.text = data.status
                }
                tvKirim?.setCompoundDrawablesWithIntrinsicBounds(null, top, null, null)
                tvKirim?.setTextColor(ContextCompat.getColor(parent.context, R.color.colorAccent))
            }
            //set status penyiapan
            if (data.penyiapan == "0") {
                val top = ContextCompat.getDrawable(parent.context, R.drawable.icons8_packing0)
                tvSiap?.setCompoundDrawablesWithIntrinsicBounds(null, top, null, null)
                tvSiap?.setTextColor(ContextCompat.getColor(parent.context, R.color.flatui_concrete))
            } else {
                val top = ContextCompat.getDrawable(parent.context, R.drawable.icons8_packing1)
                tvSiap?.setCompoundDrawablesWithIntrinsicBounds(null, top, null, null)
                tvSiap?.setTextColor(ContextCompat.getColor(parent.context, R.color.colorAccent))
            }
            //set status diterima
            if (data.diterima == "0") {
                val top = ContextCompat.getDrawable(parent.context, R.drawable.icons8_goods_receive0)
                tvTerima?.setCompoundDrawablesWithIntrinsicBounds(null, top, null, null)
                tvTerima?.setTextColor(ContextCompat.getColor(parent.context, R.color.flatui_concrete))
            } else {
                val top = ContextCompat.getDrawable(parent.context, R.drawable.icons8_goods_receive1)
                tvTerima?.setCompoundDrawablesWithIntrinsicBounds(null, top, null, null)
                tvTerima?.setTextColor(ContextCompat.getColor(parent.context, R.color.colorAccent))
            }
            //set status kembali
            if (data.kembali == "0") {
                val top = ContextCompat.getDrawable(parent.context, R.drawable.icons8_document_return0)
                tvKembali?.setCompoundDrawablesWithIntrinsicBounds(null, top, null, null)
                tvKembali?.setTextColor(ContextCompat.getColor(parent.context, R.color.flatui_concrete))
            } else {
                val top = ContextCompat.getDrawable(parent.context, R.drawable.icons8_document_return1)
                tvKembali?.setCompoundDrawablesWithIntrinsicBounds(null, top, null, null)
                tvKembali?.setTextColor(ContextCompat.getColor(parent.context, R.color.colorAccent))
            }
            //set status lunas
            if (data.lunas == "0") {
                val top = ContextCompat.getDrawable(parent.context, R.drawable.icons8_invoice_paid0)
                tvLunas?.setCompoundDrawablesWithIntrinsicBounds(null, top, null, null)
                tvLunas?.setTextColor(ContextCompat.getColor(parent.context, R.color.flatui_concrete))
            } else {
                val top = ContextCompat.getDrawable(parent.context, R.drawable.icons8_invoice_paid1)
                tvLunas?.setCompoundDrawablesWithIntrinsicBounds(null, top, null, null)
                tvLunas?.setTextColor(ContextCompat.getColor(parent.context, R.color.colorAccent))
            }

            val tvKdbrg     = view?.findViewById<TextView>(R.id.tv_kdbrg)
            val tvNmbrg     = view?.findViewById<TextView>(R.id.tv_nmbrg)
            val tvQty       = view?.findViewById<TextView>(R.id.tv_qty)
            val tvSatuan    = view?.findViewById<TextView>(R.id.tv_satuan)
            val tvDiskon1   = view?.findViewById<TextView>(R.id.tv_diskon1)
            val tvDiskon2   = view?.findViewById<TextView>(R.id.tv_diskon2)
            val tvDiskon3   = view?.findViewById<TextView>(R.id.tv_diskon3)
            val tvHarga     = view?.findViewById<TextView>(R.id.tv_harga)

            tvKdbrg?.text   = data.kdbrg
            tvNmbrg?.text   = data.nmbrg
            tvSatuan?.text  = data.satuan
            tvQty?.text     = nf.format(data.qty)
            tvHarga?.text   = nf.format(data.hrg)
            //set diskon 1
            tvDiskon1?.text = "Disc1 ${nf.format(data.diskon1)}%"
            if (nf.format(data.diskon1) != "0")
                tvDiskon1?.setTextColor(Color.RED)
            //set diskon 2
            tvDiskon2?.text = "Disc2 ${nf.format(data.diskon2)}%"
            if (nf.format(data.diskon2) != "0")
                tvDiskon2?.setTextColor(Color.RED)
            //set diskon 3
            tvDiskon3?.text = "Disc3 ${nf.format(data.diskon3)}%"
            if (nf.format(data.diskon3) != "0")
                tvDiskon3?.setTextColor(Color.RED)

            for (i in listAkses.indices) {
                val str = listAkses[i].modul
                val akses = str.substring(str.indexOf("-") + 1)
                if (str.contains("History", true)){
                    if (akses.equals("Harga Jual", ignoreCase = true)) {
                        if (listAkses[i].akses == 1) {
                            tvHarga?.visibility = View.VISIBLE
                            tvTotal?.visibility = View.VISIBLE
                        } else {
                            tvHarga?.visibility = View.GONE
                            tvTotal?.visibility = View.GONE
                        }
                    }
                    if (akses.equals("Diskon", ignoreCase = true)) {
                        if (listAkses[i].akses == 1) {
                            tvDiskon1?.visibility = View.VISIBLE
                            tvDiskon2?.visibility = View.VISIBLE
                            tvDiskon3?.visibility = View.VISIBLE
                            tvDiscFak?.visibility = View.VISIBLE
                        } else {
                            tvDiskon1?.visibility = View.GONE
                            tvDiskon2?.visibility = View.GONE
                            tvDiskon3?.visibility = View.GONE
                            tvDiscFak?.visibility = View.GONE
                        }
                    }
                }
            }

            return view
        }
    }
}

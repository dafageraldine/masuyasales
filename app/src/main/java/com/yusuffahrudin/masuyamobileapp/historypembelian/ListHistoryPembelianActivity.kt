package com.yusuffahrudin.masuyamobileapp.historypembelian

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.ExpandableListView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.yusuffahrudin.masuyamobileapp.R
import com.yusuffahrudin.masuyamobileapp.data.ArrayTampung
import com.yusuffahrudin.masuyamobileapp.data.UserAkses
import com.yusuffahrudin.masuyamobileapp.data.history_pembelian.HistoryPembelian
import com.yusuffahrudin.masuyamobileapp.util.SessionManager
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class ListHistoryPembelianActivity : AppCompatActivity() {
    private lateinit var nmbrg: String
    private lateinit var supplier: String
    private lateinit var from_tgl: String
    private lateinit var to_tgl: String
    private var invoice = ""
    private lateinit var kdkota: String
    private lateinit var sessionManager: SessionManager
    private lateinit var listAdapter: ExpandableAdapter
    private lateinit var progressBar: ProgressBar
    private val listDataGroup: MutableList<String> = ArrayList()
    //private val listDataHeader: MutableList<List<HistoryPembelian>> = ArrayList()
    //private val listDataChild : MutableList<LinkedHashMap<String, List<HistoryPembelian>>> = ArrayList()
    private val listDataChild : LinkedHashMap<String, List<HistoryPembelian>> = LinkedHashMap()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.title = getString(R.string.list_histori_pembelian)
        setContentView(R.layout.activity_list_history_penjualan)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        sessionManager  = SessionManager(applicationContext)
        val user        = sessionManager.userDetails
        kdkota          = user[SessionManager.kdkota].toString()
        //get parameter dari intent
        val i       = intent
        nmbrg       = i.extras?.getString("nmbrg").toString()
        supplier    = i.extras?.getString("supplier").toString()
        from_tgl    = i.extras?.getString("from_tgl").toString()
        to_tgl      = i.extras?.getString("to_tgl").toString()
        // get the listview
        progressBar = findViewById(R.id.progressbar)
        progressBar.visibility = View.INVISIBLE
        val expListView = findViewById<ExpandableListView>(R.id.lv_exp)
        listAdapter = ExpandableAdapter(listDataGroup, listDataChild)
        // setting list adapter
        expListView.setAdapter(listAdapter)
        // preparing list data
        selectHistoryPembelian()
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
    fun selectHistoryPembelian() {
        listDataGroup.clear()
        listDataHeader.clear()
        listDataChild.clear()

        progressBar.visibility = View.VISIBLE
        val model = ViewModelProviders.of(this).get(HistoryPembelianViewModel::class.java)
        model.getHistoryPemb(this, nmbrg, supplier, from_tgl, to_tgl, kdkota).observe(this, Observer { historyList: ArrayList<HistoryPembelian> ->
            run {
                for (i in historyList.indices) {
                    if (!historyList[i].nmsup.toString().equals(nmsup, true)) {
                        listDataGroup.add(historyList[i].nmsup.toString())
                        nmsup = historyList[i].nmsup.toString()
                        val secondList: MutableList<HistoryPembelian> = ArrayList()
                        val thirdHashMap: LinkedHashMap<String, List<HistoryPembelian>> = LinkedHashMap()
                        for (j in historyList.indices) {
                            if (historyList[j].nmsup.toString().equals(nmsup, true) && !historyList[j].nobukti.toString().equals(invoice, true)){
                                invoice = historyList[j].nobukti.toString()
                                val secondItem = HistoryPembelian()
                                var dateStr = historyList[j].tgl.toString()
                                var sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                                var date = sdf.parse(dateStr)
                                sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                                val tgl = sdf.format(date)
                                secondItem.nobukti  = historyList[j].nobukti
                                secondItem.tgl      = tgl
                                secondItem.total    = historyList[j].total
                                secondList.add(secondItem)

                                val thirdList: MutableList<HistoryPembelian> = ArrayList()
                                for (k in historyList.indices){
                                    val thirdItem = HistoryPembelian()
                                    if (historyList[k].nobukti.toString().equals(invoice, true)) {
                                        thirdItem.kdbrg         = historyList[k].kdbrg
                                        thirdItem.nmbrg         = historyList[k].nmbrg
                                        thirdItem.kdgd          = historyList[k].kdgd
                                        thirdItem.qty           = historyList[k].qty
                                        thirdItem.hrg           = historyList[k].hrg
                                        thirdItem.hrgNet        = historyList[k].hrgNet
                                        thirdItem.jumlah        = historyList[k].jumlah
                                        thirdItem.satuan        = historyList[k].satuan
                                        thirdItem.disc1         = historyList[k].disc1
                                        thirdItem.disc2         = historyList[k].disc2
                                        thirdItem.disc3         = historyList[k].disc3

                                        if (historyList[k].tglExpired.toString() != "null"){
                                            dateStr = historyList[k].tglExpired.toString()
                                            sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                                            date = sdf.parse(dateStr)
                                            sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                                            val tglExp = sdf.format(date)
                                            thirdItem.tglExpired    = tglExp
                                        } else {
                                            thirdItem.tglExpired    = historyList[k].tglExpired
                                        }

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
                                    private val listDataHeader: List<List<HistoryPembelian>>,
                                    private val listDataChild: List<LinkedHashMap<String, List<HistoryPembelian>>>): BaseExpandableListAdapter(){
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
                view = LayoutInflater.from(parent?.context).inflate(R.layout.list_group_historypemb, parent, false)
            }
            val tvSupHeader     = view?.findViewById<TextView>(R.id.tv_supplier)
            tvSupHeader?.text   = headerTitle
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
            val secondLevelELV  = SecondLevelExpandableListView(parent?.context)
            val listHeader      = listDataHeader[groupPosition]
            val listChild: MutableList<List<HistoryPembelian>> = ArrayList()
            val childData: LinkedHashMap<String, List<HistoryPembelian>> = listDataChild[groupPosition]
            childData.forEach { (_, value) ->
                listChild.add(value)
            }
            secondLevelELV.setAdapter(SecondLevelAdapter(listHeader, listChild))
            secondLevelELV.setGroupIndicator(null)
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

    private class SecondLevelAdapter(private val listDataHeader: List<HistoryPembelian>,
                                     private val listDataChild: List<List<HistoryPembelian>>): BaseExpandableListAdapter() {

        private val nf = NumberFormat.getInstance(Locale("in", "ID"))
        private val listAkses: List<UserAkses> = ArrayTampung.getListAkses()

        override fun getGroup(headerPosition: Int): HistoryPembelian {
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
            var view        = convertView
            if (view == null) {
                view        = LayoutInflater.from(parent?.context).inflate(R.layout.list_group_hutang_cust, parent, false)
            }
            val tvNoBukti   = view?.findViewById<TextView>(R.id.tv_nobukti)
            val tvTgl       = view?.findViewById<TextView>(R.id.tv_tgl)
            val tvTotal     = view?.findViewById<TextView>(R.id.tv_total)

            tvNoBukti?.text = getGroup(headerPosition).nobukti
            tvTgl?.text     = getGroup(headerPosition).tgl
            tvTotal?.text   = "Rp ${nf.format(getGroup(headerPosition).total)}"

            for (i in listAkses.indices) {
                val str     = listAkses[i].modul
                val akses   = str.substring(str.indexOf("-") + 1)
                if (akses.equals("Harga Beli", ignoreCase = true)) {
                    if (listAkses[i].akses == 1) {
                        tvTotal?.visibility = View.VISIBLE
                    } else {
                        tvTotal?.visibility = View.GONE
                    }
                }
            }

            return view
        }

        override fun isChildSelectable(p0: Int, p1: Int): Boolean {
            return true
        }

        override fun hasStableIds(): Boolean {
            return true
        }

        override fun getChild(headerPosition: Int, childPosition: Int): HistoryPembelian {
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
            var view        = convertView
            val data        = listDataChild[headerPosition][childPosition]
            if (view == null) {
                view = LayoutInflater.from(parent?.context).inflate(R.layout.list_item_historypemb, parent, false)
            }
            val tvKdbrg     = view?.findViewById<TextView>(R.id.tv_kdbrg)
            val tvNmbrg     = view?.findViewById<TextView>(R.id.tv_nmbrg)
            val tvKdGd      = view?.findViewById<TextView>(R.id.tv_kdgd)
            val tvTglExp    = view?.findViewById<TextView>(R.id.tvTglExp)
            val tvQty       = view?.findViewById<TextView>(R.id.tv_qty)
            val tvSatuan    = view?.findViewById<TextView>(R.id.tv_satuan)
            val tvHarga     = view?.findViewById<TextView>(R.id.tv_harga)
            val tvHargaNet  = view?.findViewById<TextView>(R.id.tvHargaNet)
            val tvJumlah  = view?.findViewById<TextView>(R.id.tvJumlah)
            val tvDisc1     = view?.findViewById<TextView>(R.id.tv_diskon1)
            val tvDisc2     = view?.findViewById<TextView>(R.id.tv_diskon2)
            val tvDisc3     = view?.findViewById<TextView>(R.id.tv_diskon3)

            tvKdbrg?.text       = data.kdbrg
            tvKdGd?.text        = data.kdgd
            tvTglExp?.text      = "Exp. ${data.tglExpired}"
            tvNmbrg?.text       = data.nmbrg
            tvQty?.text         = nf.format(data.qty)
            tvSatuan?.text      = data.satuan
            tvHarga?.text       = "Rp ${nf.format(data.hrg)}"
            tvHargaNet?.text    = "Rp ${nf.format(data.hrgNet)}"
            tvJumlah?.text      = "Rp ${nf.format(data.jumlah)}"
            tvDisc1?.text       = "Disc1 ${nf.format(data.disc1)}%"
            tvDisc2?.text       = "Disc2 ${nf.format(data.disc2)}%"
            tvDisc3?.text       = "Disc3 ${nf.format(data.disc3)}%"

            if (data.disc1 != 0.0)
                tvDisc1?.setTextColor(ContextCompat.getColor(parent!!.context, R.color.flatui_pomegranate))
            if (data.disc2 != 0.0)
                tvDisc2?.setTextColor(ContextCompat.getColor(parent!!.context, R.color.flatui_pomegranate))
            if (data.disc3 != 0.0)
                tvDisc3?.setTextColor(ContextCompat.getColor(parent!!.context, R.color.flatui_pomegranate))

            for (i in listAkses.indices) {
                val str = listAkses[i].modul
                val akses = str.substring(str.indexOf("-") + 1)
                if (akses.equals("Harga Beli", ignoreCase = true)) {
                    if (listAkses[i].akses == 1) {
                        tvHarga?.visibility     = View.VISIBLE
                        tvHargaNet?.visibility  = View.VISIBLE
                        tvJumlah?.visibility  = View.VISIBLE
                        tvDisc1?.visibility     = View.VISIBLE
                        tvDisc2?.visibility     = View.VISIBLE
                        tvDisc3?.visibility     = View.VISIBLE
                    } else {
                        tvHarga?.visibility     = View.GONE
                        tvHargaNet?.visibility  = View.GONE
                        tvJumlah?.visibility  = View.GONE
                        tvDisc1?.visibility     = View.GONE
                        tvDisc2?.visibility     = View.GONE
                        tvDisc3?.visibility     = View.GONE
                    }
                }
            }
            return view
        }
    } */

    //fungsi untuk select data dari database
    fun selectHistoryPembelian() {
        listDataGroup.clear()
        listDataChild.clear()
        progressBar.visibility = View.VISIBLE
        val model = ViewModelProviders.of(this).get(HistoryPembelianViewModel::class.java)
        model.getHistoryPemb(this, nmbrg, supplier, from_tgl, to_tgl, kdkota).observe(this, Observer { historyList: ArrayList<HistoryPembelian> ->
            run {
                var nmsup = ""
                for (i in historyList.indices) {
                    if (!historyList[i].nmsup.toString().equals(nmsup, true)) {
                        listDataGroup.add(historyList[i].nmsup.toString())
                        nmsup = historyList[i].nmsup.toString()
                        val secondList = ArrayList<HistoryPembelian>()
                        for (j in historyList.indices) {
                            if (historyList[j].nmsup.toString().equals(nmsup, true)){
                                invoice = historyList[j].nobukti.toString()
                                val secondItem = HistoryPembelian()
                                var dateStr = historyList[j].tgl.toString()
                                var sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                                var date = sdf.parse(dateStr)
                                sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                                val tgl = sdf.format(date)
                                secondItem.nobukti  = historyList[j].nobukti
                                secondItem.tgl      = tgl
                                secondItem.kdbrg         = historyList[j].kdbrg
                                secondItem.nmbrg         = historyList[j].nmbrg
                                secondItem.kdgd          = historyList[j].kdgd
                                secondItem.qty           = historyList[j].qty
                                secondItem.hrg           = historyList[j].hrg
                                secondItem.hrgNet        = historyList[j].hrgNet
                                secondItem.jumlah        = historyList[j].jumlah
                                secondItem.satuan        = historyList[j].satuan
                                secondItem.disc1         = historyList[j].disc1
                                secondItem.disc2         = historyList[j].disc2
                                secondItem.disc3         = historyList[j].disc3

                                if (historyList[j].tglExpired.toString() != "null"){
                                    dateStr = historyList[j].tglExpired.toString()
                                    sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                                    date = sdf.parse(dateStr)
                                    sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                                    val tglExp = sdf.format(date)
                                    secondItem.tglExpired    = tglExp
                                } else {
                                    secondItem.tglExpired    = historyList[j].tglExpired
                                }
                                secondList.add(secondItem)
                            }
                        }
                        listDataChild[nmsup] = secondList
                    }
                }
                progressBar.visibility = View.GONE
                listAdapter.notifyDataSetChanged()
            }
        })
    }

    private class ExpandableAdapter(private val listDataGroup: List<String>,
                                    private val listDataChild: LinkedHashMap<String, List<HistoryPembelian>>): BaseExpandableListAdapter() {

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
                view = LayoutInflater.from(parent?.context).inflate(R.layout.list_group_historypemb, parent, false)
            }
            val tvSupHeader     = view?.findViewById<TextView>(R.id.tv_supplier)
            tvSupHeader?.text   = headerTitle
            return view
        }

        override fun isChildSelectable(p0: Int, p1: Int): Boolean {
            return true
        }

        override fun hasStableIds(): Boolean {
            return true
        }

        override fun getChild(groupPosition: Int, childPosition: Int): HistoryPembelian {
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
                view = LayoutInflater.from(parent?.context).inflate(R.layout.list_item_historypemb_dualevel, parent, false)
            }
            val tvKdbrg     = view?.findViewById<TextView>(R.id.tv_kdbrg)
            val tvNoBukti   = view?.findViewById<TextView>(R.id.tv_nobukti)
            val tvTgl       = view?.findViewById<TextView>(R.id.tv_tgl)
            val tvNmbrg     = view?.findViewById<TextView>(R.id.tv_nmbrg)
            val tvKdGd      = view?.findViewById<TextView>(R.id.tv_kdgd)
            val tvTglExp    = view?.findViewById<TextView>(R.id.tvTglExp)
            val tvQty       = view?.findViewById<TextView>(R.id.tv_qty)
            val tvSatuan    = view?.findViewById<TextView>(R.id.tv_satuan)
            val tvHarga     = view?.findViewById<TextView>(R.id.tv_harga)
            val tvHargaNet  = view?.findViewById<TextView>(R.id.tvHargaNet)
            val tvDisc1     = view?.findViewById<TextView>(R.id.tv_diskon1)
            val tvDisc2     = view?.findViewById<TextView>(R.id.tv_diskon2)
            val tvDisc3     = view?.findViewById<TextView>(R.id.tv_diskon3)

            tvKdbrg?.text       = data.kdbrg
            tvNoBukti?.text     = data.nobukti
            tvTgl?.text         = data.tgl
            tvKdGd?.text        = data.kdgd
            tvTglExp?.text      = "Exp. ${data.tglExpired}"
            tvNmbrg?.text       = data.nmbrg
            tvQty?.text         = nf.format(data.qty)
            tvSatuan?.text      = data.satuan
            tvHarga?.text       = nf.format(data.hrg)
            tvHargaNet?.text    = nf.format(data.hrgNet)
            tvDisc1?.text       = "Disc1 ${nf.format(data.disc1)}%"
            tvDisc2?.text       = "Disc2 ${nf.format(data.disc2)}%"
            tvDisc3?.text       = "Disc3 ${nf.format(data.disc3)}%"

            if (data.disc1 != 0.0) {
                tvDisc1?.setTextColor(ContextCompat.getColor(parent!!.context, R.color.flatui_pomegranate))
            } else {
                tvDisc1?.setTextColor(ContextCompat.getColor(parent!!.context, R.color.colorTextSecondary))
            }
            if (data.disc2 != 0.0) {
                tvDisc2?.setTextColor(ContextCompat.getColor(parent!!.context, R.color.flatui_pomegranate))
            } else {
                tvDisc2?.setTextColor(ContextCompat.getColor(parent!!.context, R.color.colorTextSecondary))
            }
            if (data.disc3 != 0.0) {
                tvDisc3?.setTextColor(ContextCompat.getColor(parent!!.context, R.color.flatui_pomegranate))
            } else {
                tvDisc3?.setTextColor(ContextCompat.getColor(parent!!.context, R.color.colorTextSecondary))
            }

            for (i in listAkses.indices) {
                val str = listAkses[i].modul
                val akses = str.substring(str.indexOf("-") + 1)
                if (str.contains("History", true)){
                    if (akses.equals("Harga Beli", ignoreCase = true)) {
                        if (listAkses[i].akses == 1) {
                            tvHarga?.visibility     = View.VISIBLE
                            tvHargaNet?.visibility  = View.VISIBLE
                            tvDisc1?.visibility     = View.VISIBLE
                            tvDisc2?.visibility     = View.VISIBLE
                            tvDisc3?.visibility     = View.VISIBLE
                        } else {
                            tvHarga?.visibility     = View.GONE
                            tvHargaNet?.visibility  = View.GONE
                            tvDisc1?.visibility     = View.GONE
                            tvDisc2?.visibility     = View.GONE
                            tvDisc3?.visibility     = View.GONE
                        }
                    }
                }
            }
            return view
        }
    }
}
package com.yusuffahrudin.masuyamobileapp.informasibarang

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.ExpandableListView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.yusuffahrudin.masuyamobileapp.R
import com.yusuffahrudin.masuyamobileapp.api.API
import com.yusuffahrudin.masuyamobileapp.api.ApiService
import com.yusuffahrudin.masuyamobileapp.controller.DialogAlert
import com.yusuffahrudin.masuyamobileapp.data.ArrayTampung
import com.yusuffahrudin.masuyamobileapp.data.UserAkses
import com.yusuffahrudin.masuyamobileapp.data.history_pembelian.HistoryPembelian
import com.yusuffahrudin.masuyamobileapp.data.history_pembelian.HistoryPembelianResponse
import com.yusuffahrudin.masuyamobileapp.util.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class HistoryPembelianFragment: Fragment() {
    private lateinit var sessionManager: SessionManager
    private var kdbrg   = ""
    private var kota    = ""
    private var user    = ""
    private var level   = ""
    private var invoice = ""
    private lateinit var listAdapter: ExpandableAdapter
    private lateinit var progressBar: ProgressBar
    private val listDataGroup: MutableList<String> = ArrayList()
    //private val listDataHeader: MutableList<List<HistoryPembelian>> = ArrayList()
    //private val listDataChild : MutableList<LinkedHashMap<String, List<HistoryPembelian>>> = ArrayList()
    private val listDataChild : java.util.LinkedHashMap<String, List<HistoryPembelian>> = java.util.LinkedHashMap()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_history, container, false)

        val i = this.activity!!.intent
        kdbrg = i.extras?.getString(ListBarangActivity.KDBRG).toString()
        sessionManager = SessionManager(this.activity?.applicationContext)
        val cache = sessionManager.userDetails
        kota = cache[SessionManager.kdkota].toString()
        user = cache[SessionManager.kunci_email].toString()
        level = cache[SessionManager.level].toString()

        val expListView = view.findViewById<ExpandableListView>(R.id.lv_exp)
        progressBar = view.findViewById(R.id.progressbar)
        progressBar.visibility = View.GONE
        listAdapter = ExpandableAdapter(listDataGroup, listDataChild)
        expListView.setAdapter(listAdapter)

        selectHistoryPembelian(kdbrg, kota)

        return view
    }

    private fun selectHistoryPembelian(kdbrg: String, kdkota: String) {
        listDataGroup.clear()
        listDataChild.clear()
        progressBar.visibility = View.VISIBLE
        API.instance().create(ApiService::class.java)
                .getHistoryPembBrg(kdbrg, kdkota)
                ?.enqueue(object : Callback<HistoryPembelianResponse?> {
                    override fun onFailure(call: Call<HistoryPembelianResponse?>, e: Throwable) {
                        progressBar.visibility = View.GONE
                        Log.d("Get History Pemb Barang", "Error $e")
                        FirebaseCrashlytics.getInstance().recordException(e)
                        DialogAlert("${getString(R.string.error_pengambilan_data)} ${e.message}", "error", requireActivity())
                    }

                    override fun onResponse(call: Call<HistoryPembelianResponse?>, response: Response<HistoryPembelianResponse?>) {
                        if (response.isSuccessful) {
                            var nmsup = ""
                            for (i in response.body()?.result!!.indices) {
                                if (!response.body()?.result!![i].nmsup.toString().equals(nmsup, true)) {
                                    listDataGroup.add(response.body()?.result!![i].nmsup.toString())
                                    nmsup = response.body()?.result!![i].nmsup.toString()
                                    val secondList = ArrayList<HistoryPembelian>()
                                    for (j in response.body()?.result!!.indices) {
                                        if (response.body()?.result!![j].nmsup.toString().equals(nmsup, true)){
                                            invoice = response.body()?.result!![j].nobukti.toString()
                                            val secondItem = HistoryPembelian()
                                            var dateStr = response.body()?.result!![j].tgl.toString()
                                            var sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                                            var date = sdf.parse(dateStr)
                                            sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                                            val tgl = sdf.format(date)
                                            secondItem.nobukti       = response.body()?.result!![j].nobukti
                                            secondItem.tgl           = tgl
                                            secondItem.kdbrg         = response.body()?.result!![j].kdbrg
                                            secondItem.nmbrg         = response.body()?.result!![j].nmbrg
                                            secondItem.kdgd          = response.body()?.result!![j].kdgd
                                            secondItem.qty           = response.body()?.result!![j].qty
                                            secondItem.hrg           = response.body()?.result!![j].hrg
                                            secondItem.hrgNet        = response.body()?.result!![j].hrgNet
                                            secondItem.jumlah        = response.body()?.result!![j].jumlah
                                            secondItem.satuan        = response.body()?.result!![j].satuan
                                            secondItem.disc1         = response.body()?.result!![j].disc1
                                            secondItem.disc2         = response.body()?.result!![j].disc2
                                            secondItem.disc3         = response.body()?.result!![j].disc3

                                            if (response.body()?.result!![j].tglExpired.toString() != "null"){
                                                dateStr = response.body()?.result!![j].tglExpired.toString()
                                                sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                                                date = sdf.parse(dateStr)
                                                sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                                                val tglExp = sdf.format(date)
                                                secondItem.tglExpired    = tglExp
                                            } else {
                                                secondItem.tglExpired    = response.body()?.result!![j].tglExpired
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
                    }

                })
    }

    /*
    private class FirstLevelAdapter(private val listDataGroup: List<String>,
                                    private val listDataHeader: List<List<HistoryPembelian>>,
                                    private val listDataChild: List<java.util.LinkedHashMap<String, List<HistoryPembelian>>>): BaseExpandableListAdapter(){
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
            val listChild: MutableList<List<HistoryPembelian>> = java.util.ArrayList()
            val childData: java.util.LinkedHashMap<String, List<HistoryPembelian>> = listDataChild[groupPosition]
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

            println("------------------------------------ disc1 ${data.disc1}")
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
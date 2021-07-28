package com.yusuffahrudin.masuyamobileapp.informasibarang

import android.annotation.SuppressLint
import android.graphics.Color
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
import com.yusuffahrudin.masuyamobileapp.data.history_penjualan.HistoryPenjualan
import com.yusuffahrudin.masuyamobileapp.data.history_penjualan.HistoryPenjualanResponse
import com.yusuffahrudin.masuyamobileapp.util.SessionManager
import retrofit2.Call
import retrofit2.Callback
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.LinkedHashMap

class HistoryPenjualanFragment: Fragment() {
    private lateinit var sessionManager: SessionManager
    private lateinit var listAdapter: ExpandableAdapter
    private val listDataGroup: MutableList<String> = ArrayList()
    private val listDataChild : LinkedHashMap<String, List<HistoryPenjualan>> = java.util.LinkedHashMap()
    private lateinit var progressBar: ProgressBar

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_history, container, false)

        val i = this.activity!!.intent
        val kdbrg = i.extras?.getString(ListBarangActivity.KDBRG).toString()
        sessionManager = SessionManager(this.activity?.applicationContext)
        val cache = sessionManager.userDetails
        val kota = cache[SessionManager.kdkota].toString()
        val user = cache[SessionManager.kunci_email].toString()
        val level = cache[SessionManager.level].toString()

        val expListView = view.findViewById<ExpandableListView>(R.id.lv_exp)
        progressBar = view.findViewById(R.id.progressbar)
        progressBar.visibility = View.GONE
        listAdapter = ExpandableAdapter(listDataGroup, listDataChild)
        expListView.setAdapter(listAdapter)

        selectHistoryPenjualan(kdbrg, user, kota, level)

        return view
    }

    //fungsi untuk select data dari database
    private fun selectHistoryPenjualan(kdbrg: String, sales: String, kdkota: String, level: String) {
        listDataGroup.clear()
        listDataChild.clear()
        progressBar.visibility = View.VISIBLE
        API.instance().create(ApiService::class.java)
                .getHistoryPenjBrg(kdbrg, sales, level, kdkota)
                ?.enqueue(object : Callback<HistoryPenjualanResponse?> {
                    override fun onFailure(call: Call<HistoryPenjualanResponse?>, e: Throwable) {
                        progressBar.visibility = View.GONE
                        Log.d("Get History Penj Barang", "Error $e")
                        FirebaseCrashlytics.getInstance().recordException(e)
                        DialogAlert("${getString(R.string.error_pengambilan_data)} ${e.message}", "error", requireActivity())
                    }

                    override fun onResponse(call: Call<HistoryPenjualanResponse?>, response: retrofit2.Response<HistoryPenjualanResponse?>) {
                        if (response.isSuccessful) {
                            var nmcust = ""
                            for (i in response.body()?.result!!.indices) {
                                if (!response.body()?.result!![i].nmcust.toString().equals(nmcust, true)) {
                                    listDataGroup.add(response.body()?.result!![i].nmcust.toString())
                                    nmcust = response.body()?.result!![i].nmcust.toString()
                                    val secondList = java.util.ArrayList<HistoryPenjualan>()
                                    for (j in response.body()?.result!!.indices) {
                                        if (response.body()?.result!![j].nmcust.toString().equals(nmcust, true)){
                                            val secondItem = HistoryPenjualan()
                                            var dateStr = response.body()?.result!![j].tgl.toString()
                                            var sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                                            var date = sdf.parse(dateStr)
                                            sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                                            val tgl = sdf.format(date)
                                            secondItem.nobukti      = response.body()?.result!![j].nobukti
                                            secondItem.nopo         = response.body()?.result!![j].nopo
                                            secondItem.tgl          = tgl
                                            secondItem.discfak      = response.body()?.result!![j].discfak
                                            secondItem.total        = response.body()?.result!![j].total
                                            secondItem.cetak        = response.body()?.result!![j].cetak
                                            secondItem.kirim        = response.body()?.result!![j].kirim
                                            secondItem.status       = response.body()?.result!![j].status
                                            secondItem.penyiapan    = response.body()?.result!![j].penyiapan
                                            secondItem.diterima     = response.body()?.result!![j].diterima
                                            secondItem.kembali      = response.body()?.result!![j].kembali
                                            secondItem.lunas        = response.body()?.result!![j].lunas
                                            secondItem.kdbrg        = response.body()?.result!![j].kdbrg
                                            secondItem.nmbrg        = response.body()?.result!![j].nmbrg
                                            secondItem.qty          = response.body()?.result!![j].qty
                                            secondItem.satuan       = response.body()?.result!![j].satuan
                                            secondItem.hrg          = response.body()?.result!![j].hrg
                                            secondItem.diskon1      = response.body()?.result!![j].diskon1
                                            secondItem.diskon2      = response.body()?.result!![j].diskon2
                                            secondItem.diskon3      = response.body()?.result!![j].diskon3

                                            secondList.add(secondItem)
                                        }
                                    }
                                    listDataChild[nmcust] = secondList
                                }
                            }
                            progressBar.visibility = View.GONE
                            listAdapter.notifyDataSetChanged()
                        }
                    }

                })
    }

    private class ExpandableAdapter(private val listDataGroup: List<String>,
                                    private val listDataChild: java.util.LinkedHashMap<String, List<HistoryPenjualan>>): BaseExpandableListAdapter() {

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
            tvTotal?.text   = nf.format(data.total)
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
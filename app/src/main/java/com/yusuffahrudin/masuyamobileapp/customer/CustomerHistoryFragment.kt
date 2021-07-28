package com.yusuffahrudin.masuyamobileapp.customer

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
import com.yusuffahrudin.masuyamobileapp.data.Customer
import com.yusuffahrudin.masuyamobileapp.data.UserAkses
import com.yusuffahrudin.masuyamobileapp.data.history_penjualan.HistoryPenjualan
import com.yusuffahrudin.masuyamobileapp.data.history_penjualan.HistoryPenjualanResponse
import com.yusuffahrudin.masuyamobileapp.util.SessionManager
import retrofit2.Call
import retrofit2.Callback
import java.text.NumberFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class CustomerHistoryFragment: Fragment() {
    private lateinit var expandableListView: ExpandableListView
    private lateinit var progressBar: ProgressBar
    private lateinit var listAdapter: ExpandableAdapter
    private val listDataGroup: MutableList<HistoryPenjualan> = ArrayList()
    private val listDataChild : LinkedHashMap<String, List<HistoryPenjualan>> = LinkedHashMap()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view            = inflater.inflate(R.layout.fragment_history, container, false)
        expandableListView  = view.findViewById(R.id.lv_exp)
        progressBar         = view.findViewById(R.id.progressbar)

        val sessionManager  = SessionManager(context)
        val user            = sessionManager.userDetails
        val kdkota          = user[SessionManager.kdkota].toString()
        val level           = user[SessionManager.level].toString()

        val customer = requireActivity().intent.getParcelableExtra<Customer>(CustomerDetailFragment.EXTRA_CUSTOMER)!!

        progressBar.visibility  = View.GONE
        listAdapter             = ExpandableAdapter(listDataGroup, listDataChild)
        expandableListView.setAdapter(listAdapter)

        // preparing list data
        selectHistoryPenjualan(customer.kdcust, customer.kdsales, kdkota, level)

        return view
    }

    //fungsi untuk select data dari database
    private fun selectHistoryPenjualan(customer: String, sales: String, kdkota: String, level: String) {
        listDataGroup.clear()
        listDataChild.clear()
        progressBar.visibility = View.VISIBLE
        API.instance().create(ApiService::class.java)
                .getHistoryPenjCust(customer, sales, level, kdkota)
                ?.enqueue(object : Callback<HistoryPenjualanResponse?> {
                    override fun onFailure(call: Call<HistoryPenjualanResponse?>, e: Throwable) {
                        progressBar.visibility = View.GONE
                        Log.d("Get History Penj Cust", "Error $e")
                        e.printStackTrace()
                        FirebaseCrashlytics.getInstance().recordException(e)
                        DialogAlert("${getString(R.string.error_pengambilan_data)} ${e.message}", "error", requireActivity())
                    }

                    override fun onResponse(call: Call<HistoryPenjualanResponse?>, response: retrofit2.Response<HistoryPenjualanResponse?>) {
                        if (response.isSuccessful) {
                            println("====================================== berhasil")
                            var nobukti = ""
                            for (i in response.body()?.result!!.indices) {
                                if (!nobukti.equals(response.body()?.result!![i].nobukti.toString(), true)){
                                    val group = HistoryPenjualan()
                                    group.nobukti = response.body()?.result!![i].nobukti
                                    group.nopo = response.body()?.result!![i].nopo
                                    group.tgl = response.body()?.result!![i].tgl
                                    group.cetak = response.body()?.result!![i].cetak
                                    group.kirim = response.body()?.result!![i].kirim
                                    group.status = response.body()?.result!![i].status
                                    group.penyiapan = response.body()?.result!![i].penyiapan
                                    group.diterima = response.body()?.result!![i].diterima
                                    group.kembali = response.body()?.result!![i].kembali
                                    group.lunas = response.body()?.result!![i].lunas
                                    listDataGroup.add(group)
                                    nobukti = group.nobukti.toString()

                                    val listChild = ArrayList<HistoryPenjualan>()
                                    for (j in 0 until response.body()?.result!!.size){
                                        if (nobukti.equals(response.body()?.result!![j].nobukti.toString(), true)){
                                            val child = HistoryPenjualan()
                                            child.kdbrg = response.body()?.result!![j].kdbrg
                                            child.nmbrg = response.body()?.result!![j].nmbrg
                                            child.qty = response.body()?.result!![j].qty
                                            child.hrg = response.body()?.result!![j].hrg
                                            child.diskon1 = response.body()?.result!![j].diskon1
                                            child.diskon2 = response.body()?.result!![j].diskon2
                                            child.diskon3 = response.body()?.result!![j].diskon3
                                            child.discfak = response.body()?.result!![j].discfak
                                            listChild.add(child)
                                        }
                                    }
                                    listDataChild[nobukti] = listChild
                                }
                            }

                            progressBar.visibility = View.GONE
                            listAdapter.notifyDataSetChanged()
                        }
                    }

                })
    }

    private class ExpandableAdapter(private val listDataGroup: List<HistoryPenjualan>,
                                    private val listDataChild: LinkedHashMap<String, List<HistoryPenjualan>>): BaseExpandableListAdapter() {

        private val nf = NumberFormat.getInstance(Locale("in", "ID"))
        private val listAkses: List<UserAkses> = ArrayTampung.getListAkses()

        override fun getGroup(groupPosition: Int): HistoryPenjualan {
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
            if (view == null) {
                view = LayoutInflater.from(parent?.context).inflate(R.layout.list_header_history, parent, false)
            }
            val tvNomor = view?.findViewById<TextView>(R.id.tv_nomor)
            val tvTgl = view?.findViewById<TextView>(R.id.tv_tgl)
            val tvNopo = view?.findViewById<TextView>(R.id.tv_noPO)
            val tvCetak = view?.findViewById<TextView>(R.id.tv_cetak)
            val tvSiap = view?.findViewById<TextView>(R.id.tv_siap)
            val tvKirim = view?.findViewById<TextView>(R.id.tv_kirim)
            val tvTerima = view?.findViewById<TextView>(R.id.tv_terima)
            val tvKembali = view?.findViewById<TextView>(R.id.tv_kembali)
            val tvLunas = view?.findViewById<TextView>(R.id.tv_lunas)

            tvNomor?.text = getGroup(groupPosition).nobukti
            tvNopo?.text = getGroup(groupPosition).nopo
            val dateStr = getGroup(groupPosition).tgl.toString()
            var sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            var date: Date? = null
            try {
                date = sdf.parse(dateStr)
            } catch (e: ParseException) {
                e.printStackTrace()
            }
            sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            val tgl = sdf.format(date)
            tvNomor?.text = getGroup(groupPosition).nobukti
            tvTgl?.text = tgl

            //set status cetak
            if (getGroup(groupPosition).cetak == "0") {
                val top = ContextCompat.getDrawable(parent!!.context, R.drawable.icons8_print0)
                tvCetak?.setCompoundDrawablesWithIntrinsicBounds(null, top, null, null)
                tvCetak?.setTextColor(ContextCompat.getColor(parent.context, R.color.flatui_concrete))
            } else {
                val top = ContextCompat.getDrawable(parent!!.context, R.drawable.icons8_print1)
                tvCetak?.setCompoundDrawablesWithIntrinsicBounds(null, top, null, null)
                tvCetak?.setTextColor(ContextCompat.getColor(parent.context, R.color.colorAccent))
            }
            //set status kirim
            if (getGroup(groupPosition).kirim == "0") {
                val top = ContextCompat.getDrawable(parent.context, R.drawable.icons8_truck0)
                tvKirim?.text = getGroup(groupPosition).status
                tvKirim?.setCompoundDrawablesWithIntrinsicBounds(null, top, null, null)
                tvKirim?.setTextColor(ContextCompat.getColor(parent.context, R.color.flatui_concrete))
            } else {
                val top = ContextCompat.getDrawable(parent.context, R.drawable.icons8_truck1)
                if (!getGroup(groupPosition).status.equals("")){
                    tvKirim?.text = getGroup(groupPosition).status
                }
                tvKirim?.setCompoundDrawablesWithIntrinsicBounds(null, top, null, null)
                tvKirim?.setTextColor(ContextCompat.getColor(parent.context, R.color.colorAccent))
            }
            //set status penyiapan
            if (getGroup(groupPosition).penyiapan == "0") {
                val top = ContextCompat.getDrawable(parent.context, R.drawable.icons8_packing0)
                tvSiap?.setCompoundDrawablesWithIntrinsicBounds(null, top, null, null)
                tvSiap?.setTextColor(ContextCompat.getColor(parent.context, R.color.flatui_concrete))
            } else {
                val top = ContextCompat.getDrawable(parent.context, R.drawable.icons8_packing1)
                tvSiap?.setCompoundDrawablesWithIntrinsicBounds(null, top, null, null)
                tvSiap?.setTextColor(ContextCompat.getColor(parent.context, R.color.colorAccent))
            }
            //set status diterima
            if (getGroup(groupPosition).diterima == "0") {
                val top = ContextCompat.getDrawable(parent.context, R.drawable.icons8_goods_receive0)
                tvTerima?.setCompoundDrawablesWithIntrinsicBounds(null, top, null, null)
                tvTerima?.setTextColor(ContextCompat.getColor(parent.context, R.color.flatui_concrete))
            } else {
                val top = ContextCompat.getDrawable(parent.context, R.drawable.icons8_goods_receive1)
                tvTerima?.setCompoundDrawablesWithIntrinsicBounds(null, top, null, null)
                tvTerima?.setTextColor(ContextCompat.getColor(parent.context, R.color.colorAccent))
            }
            //set status kembali
            if (getGroup(groupPosition).kembali == "0") {
                val top = ContextCompat.getDrawable(parent.context, R.drawable.icons8_document_return0)
                tvKembali?.setCompoundDrawablesWithIntrinsicBounds(null, top, null, null)
                tvKembali?.setTextColor(ContextCompat.getColor(parent.context, R.color.flatui_concrete))
            } else {
                val top = ContextCompat.getDrawable(parent.context, R.drawable.icons8_document_return1)
                tvKembali?.setCompoundDrawablesWithIntrinsicBounds(null, top, null, null)
                tvKembali?.setTextColor(ContextCompat.getColor(parent.context, R.color.colorAccent))
            }
            //set status lunas
            if (getGroup(groupPosition).lunas == "0") {
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

        override fun getChild(groupPosition: Int, childPosition: Int): HistoryPenjualan {
            val data = listDataChild[listDataGroup[groupPosition].nobukti]
            return data!![childPosition]
        }

        override fun getChildId(groupPosition: Int, childPosition: Int): Long {
            return childPosition.toLong()
        }

        override fun getChildrenCount(groupPosition: Int): Int {
            return listDataChild[listDataGroup[groupPosition].nobukti]!!.size
        }

        @SuppressLint("SetTextI18n")
        override fun getChildView(groupPosition: Int, childPosition: Int, isLastChild: Boolean, convertView: View?, parent: ViewGroup?): View? {
            var view = convertView
            val data = getChild(groupPosition, childPosition)
            if (view == null) {
                view = LayoutInflater.from(parent?.context).inflate(R.layout.list_item_history, parent, false)
            }
            val tvKdbrg = view?.findViewById<TextView>(R.id.tv_kdbrg)
            val tvNmbrg = view?.findViewById<TextView>(R.id.tv_nmbrg)
            val tvQty = view?.findViewById<TextView>(R.id.tv_qty)
            val tvHarga = view?.findViewById<TextView>(R.id.tv_harga)
            val tvDiskon1 = view?.findViewById<TextView>(R.id.tv_diskon1)
            val tvDiskon2 = view?.findViewById<TextView>(R.id.tv_diskon2)
            val tvDiskon3 = view?.findViewById<TextView>(R.id.tv_diskon3)
            val tvDiscfak = view?.findViewById<TextView>(R.id.tv_discfak)

            tvKdbrg?.text = data.kdbrg
            tvNmbrg?.text = data.nmbrg
            tvQty?.text = nf.format(data.qty)
            tvHarga?.text = nf.format(data.hrg)
            //set diskon 1
            tvDiskon1?.text = "${nf.format(data.diskon1)} %"
            if (nf.format(data.diskon1) == "0") {
                tvDiskon1?.setTextColor(Color.BLACK)
            } else {
                tvDiskon1?.setTextColor(Color.RED)
            }
            //set diskon 2
            tvDiskon2?.text = "${nf.format(data.diskon2)} %"
            if (nf.format(data.diskon2) == "0") {
                tvDiskon2?.setTextColor(Color.BLACK)
            } else {
                tvDiskon2?.setTextColor(Color.RED)
            }
            //set diskon 3
            tvDiskon3?.text = "${nf.format(data.diskon3)} %"
            if (nf.format(data.diskon3) == "0") {
                tvDiskon3?.setTextColor(Color.BLACK)
            } else {
                tvDiskon3?.setTextColor(Color.RED)
            }
            //set diskon faktur
            tvDiscfak?.text = "${nf.format(data.discfak)} %"
            if (nf.format(data.discfak) == "0") {
                tvDiscfak?.setTextColor(Color.BLACK)
            } else {
                tvDiscfak?.setTextColor(Color.RED)
            }
            for (i in listAkses.indices) {
                val str = listAkses[i].modul
                val akses = str.substring(str.indexOf("-") + 1)
                if (str.contains("History", true)){
                    if (akses.equals("Harga Jual", ignoreCase = true)) {
                        if (listAkses[i].akses == 1) {
                            tvHarga?.visibility = View.VISIBLE
                        } else {
                            tvHarga?.visibility = View.GONE
                        }
                    }
                    if (akses.equals("Diskon", ignoreCase = true)) {
                        if (listAkses[i].akses == 1) {
                            tvDiscfak?.visibility = View.VISIBLE
                            tvDiskon1?.visibility = View.VISIBLE
                            tvDiskon2?.visibility = View.VISIBLE
                            tvDiskon3?.visibility = View.VISIBLE
                        } else {
                            tvDiscfak?.visibility = View.GONE
                            tvDiskon1?.visibility = View.GONE
                            tvDiskon2?.visibility = View.GONE
                            tvDiskon3?.visibility = View.GONE
                        }
                    }
                }
            }
            return view
        }
    }
}
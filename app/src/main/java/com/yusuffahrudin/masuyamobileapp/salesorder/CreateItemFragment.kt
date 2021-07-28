package com.yusuffahrudin.masuyamobileapp.salesorder

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.yusuffahrudin.masuyamobileapp.R
import com.yusuffahrudin.masuyamobileapp.controller.DialogAlert
import com.yusuffahrudin.masuyamobileapp.data.ArrayTampung
import com.yusuffahrudin.masuyamobileapp.data.sales_order.ItemOrder
import com.yusuffahrudin.masuyamobileapp.data.sales_order.SalesOrder
import com.yusuffahrudin.masuyamobileapp.databinding.LvActivityTroliBinding
import com.yusuffahrudin.masuyamobileapp.util.Server
import java.text.NumberFormat
import java.util.*

class CreateItemFragment: Fragment() {

    private lateinit var swipeRefresh: SwipeRefreshLayout
    lateinit var lvBrg: RecyclerView
    private lateinit var textSubtotalFinal: TextView
    private lateinit var textTotal: TextView
    private lateinit var textDiscfakTotal: TextView
    private lateinit var textPpnTotal: TextView
    private lateinit var editDiscfakPersen: EditText
    private lateinit var statusPajak: String
    private lateinit var layoutManager: LinearLayoutManager
    lateinit var adapter: AdapterRV
    private val listChart: ArrayList<ItemOrder> = ArrayTampung.getListChart()
    private val listHeader: ArrayList<SalesOrder> = ArrayTampung.getListHeaderSO()
    private val nf = NumberFormat.getInstance(Locale("in", "ID"))
    companion object {
        lateinit var editPpnPersen: EditText
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_so_item, container, false)

        swipeRefresh = rootView.findViewById(R.id.swipe_refresh)
        lvBrg = rootView.findViewById(R.id.lv_barang)
        textSubtotalFinal = rootView.findViewById(R.id.tv_subtotal)
        textTotal = rootView.findViewById(R.id.tv_total)
        textDiscfakTotal = rootView.findViewById(R.id.tv_discfak_total)
        editDiscfakPersen = rootView.findViewById(R.id.edt_discfak_persen)
        textPpnTotal = rootView.findViewById(R.id.tv_ppn_total)
        editPpnPersen = rootView.findViewById(R.id.edt_ppn_persen)
        statusPajak = "00"
        lvBrg.setHasFixedSize(true)
        layoutManager = LinearLayoutManager(this.requireContext())
        lvBrg.layoutManager = layoutManager

        adapter = AdapterRV(this.requireActivity())
        lvBrg.adapter = adapter

        if(listChart.size == 0){
            println("--------------------------- chart size 0")
        }

        swipeRefresh.setOnRefreshListener {
            adapter.notifyDataSetChanged()
            swipeRefresh.isRefreshing = false
        }

        editDiscfakPersen.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                var subtotal = ubahAngka(textSubtotalFinal.text.toString()).toDouble()
                val discfak = subtotal * ( ubahAngka(editDiscfakPersen.text.toString()).toDouble() / 100 )
                textDiscfakTotal.text = nf.format(discfak)
                subtotal -= discfak
                val total = subtotal + ubahAngka(textPpnTotal.text.toString()).toDouble()
                textTotal.text = nf.format(total)
            }
            false
        }

        editPpnPersen.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                var subtotal = ubahAngka(textSubtotalFinal.text.toString()).toDouble()
                val discfak = ubahAngka(textDiscfakTotal.text.toString()).toDouble()
                subtotal -= discfak
                val ppn = subtotal * ( ubahAngka(editPpnPersen.text.toString()).toDouble() / 100 )
                textPpnTotal.text = nf.format(ppn)
                val total = subtotal + ppn
                textTotal.text = nf.format(total)
            }
            false
        }

        return rootView
    }

    fun getTotal(){
        listHeader[0].subtotal = ubahAngka(textSubtotalFinal.text.toString()).toDouble()
        listHeader[0].disc = ubahAngka(editDiscfakPersen.text.toString()).toDouble()
        listHeader[0].jmldisc1 = ubahAngka(textDiscfakTotal.text.toString()).toDouble()
        listHeader[0].prsppn = ubahAngka(editPpnPersen.text.toString()).toDouble()
        listHeader[0].ppn = ubahAngka(textPpnTotal.text.toString()).toDouble()
        listHeader[0].total = ubahAngka(textTotal.text.toString()).toDouble()
    }

    inner class AdapterRV(private val activity: Activity): RecyclerView.Adapter<AdapterRV.ViewHolder>() {
        private var qty: Double = 0.toDouble()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = LvActivityTroliBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(binding)
        }

        override fun getItemCount(): Int {
            return listChart.size
        }

        @SuppressLint("SetTextI18n")
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.binding.tvKdbrg.text = listChart[position].kdBrg
            holder.binding.tvNmbrg.text = listChart[position].nmBrg
            holder.binding.tvSatuan.text = listChart[position].satuan
            holder.binding.edtQty.setText(nf.format(listChart[position].qtyOrder))
            val harga = listChart[position].harga!! * listChart[position].qtyOrder!!
            holder.binding.tvHarga.text = nf.format(listChart[position].harga)
            holder.binding.tvDiskon1.text = nf.format(listChart[position].diskon1) + " %"
            holder.binding.tvDiskon2.text = nf.format(listChart[position].diskon2) + " %"
            holder.binding.tvDiskon3.text = nf.format(listChart[position].diskon3) + " %"
            val subtotal = harga * (1 - listChart[position].diskon1!! / 100) * (1 - listChart[position].diskon2!! / 100) * (1 - listChart[position].diskon3!! / 100)
            holder.binding.tvSubtotal.text = nf.format(subtotal)
            listChart[position].subTotal = subtotal
            statusPajak = listHeader[0].kodeTax.toString()

            try {
                if(listHeader[0].statusorder == "OPEN"){
                    holder.binding.btnPlus.isEnabled = true
                    holder.binding.btnMinus.isEnabled = true
                    holder.binding.btnHapus.isEnabled = true
                } else {
                    holder.binding.btnPlus.isEnabled = false
                    holder.binding.btnMinus.isEnabled = false
                    holder.binding.btnHapus.isEnabled = false
                }
            }catch (e: Exception) {
                e.printStackTrace()
                FirebaseCrashlytics.getInstance().recordException(e)
            }

            val a = Server("")
            if (holder.binding.imgBrg != null) {
                Glide.with(this.activity)
                        .load(a.URL_IMAGE() + listChart[position].kdBrg + ".jpg")
                        .apply(RequestOptions()
                                .placeholder(R.mipmap.ic_launcher)
                                .error(R.drawable.img_not_found)
                                .override(150, 150)
                                .fitCenter())
                        .into(holder.binding.imgBrg)
            }

            holder.binding.edtQty.setOnEditorActionListener { v, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    try {
                        qty = ubahAngka(holder.binding.edtQty.text.toString()).toDouble()
                        holder.binding.tvSubtotal.text = nf.format(hitungSubtotalItem(listChart[position].harga!!, qty, listChart[position].diskon1!!, listChart[position].diskon2!!, listChart[position].diskon3!!))
                        listChart[position].qtyOrder = qty
                        listChart[position].subTotal = ubahAngka(holder.binding.tvSubtotal.text.toString()).toDouble()
                        adapter.notifyDataSetChanged()
                        hitungTotal(listChart)
                    } catch (e: Exception) {
                        FirebaseCrashlytics.getInstance().recordException(e)
                        DialogAlert(e.message.toString(), "error", this.activity)
                    }
                }
                false
            }

            holder.binding.btnPlus.setOnClickListener {
                try {
                    qty = ubahAngka(holder.binding.edtQty.text.toString()).toDouble() + 1
                    holder.binding.edtQty.setText(nf.format(qty))
                    holder.binding.tvSubtotal.text = nf.format(hitungSubtotalItem(listChart[position].harga!!, qty, listChart[position].diskon1!!, listChart[position].diskon2!!, listChart[position].diskon3!!))
                    listChart[position].qtyOrder = qty
                    listChart[position].subTotal = ubahAngka(holder.binding.tvSubtotal.text.toString()).toDouble()
                    adapter.notifyDataSetChanged()
                    hitungTotal(listChart)
                } catch (e: Exception) {
                    FirebaseCrashlytics.getInstance().recordException(e)
                    DialogAlert(e.message.toString(), "error", this.activity)
                }
            }
            holder.binding.btnMinus.setOnClickListener {
                try {
                    qty = ubahAngka(holder.binding.edtQty.text.toString()).toDouble() - 1
                    holder.binding.edtQty.setText(nf.format(qty))
                    holder.binding.tvSubtotal.text = nf.format(hitungSubtotalItem(listChart[position].harga!!, qty, listChart[position].diskon1!!, listChart[position].diskon2!!, listChart[position].diskon3!!))
                    listChart[position].qtyOrder = qty
                    listChart[position].subTotal = ubahAngka(holder.binding.tvSubtotal.text.toString()).toDouble()
                    adapter.notifyDataSetChanged()
                    hitungTotal(listChart)
                } catch (e: Exception) {
                    FirebaseCrashlytics.getInstance().recordException(e)
                    DialogAlert(e.message.toString(), "error", this.activity)
                }
            }
            holder.binding.btnHapus.setOnClickListener {
                try {
                    listChart.removeAt(position)
                    adapter.notifyDataSetChanged()
                    hitungTotal(listChart)
                } catch (e: Exception) {
                    FirebaseCrashlytics.getInstance().recordException(e)
                    DialogAlert(e.message.toString(), "error", this.activity)
                }
            }
            hitungTotal(listChart)
        }

        inner class ViewHolder(val binding: LvActivityTroliBinding) : RecyclerView.ViewHolder(binding.root)
    }

    private fun hitungSubtotalItem(harga: Double, qty: Double, disc1: Double, disc2: Double, disc3: Double): Double {
        var hrg = harga
        hrg *= (1 - disc1 / 100) * (1 - disc2 / 100) * (1 - disc3 / 100)
        val subtotal = harga * qty
        return subtotal
    }

    private fun hitungTotal(listTemp: List<ItemOrder>) {
        try {
            var subtotal = 0.0
            var ppn: Double
            //==================== Subtotal ======================
            for (i in listTemp.indices) {
                subtotal += listTemp[i].subTotal!!
            }
            textSubtotalFinal.text = nf.format(subtotal)
            //==================== Discfak ======================
            var discfak: Double = ubahAngka(editDiscfakPersen.text.toString()).toDouble() / 100
            discfak *=  subtotal
            textDiscfakTotal.text = nf.format(discfak)
            subtotal -= discfak
            //==================== Ppn ======================
            println("------------------------------------ statusPajak $statusPajak")
            if (statusPajak.equals("01", ignoreCase = true)) {
                //editPpnPersen.setText("10")
                ppn = ubahAngka(editPpnPersen.text.toString()).toDouble() / 100
                ppn *= subtotal
                textPpnTotal.text = nf.format(ppn)
            } else {
                //editPpnPersen.setText("0")
                ppn = ubahAngka(editPpnPersen.text.toString()).toDouble() / 100
                ppn *= subtotal
                textPpnTotal.text = nf.format(ppn)
            }
            //==================== Total ======================
            val total: Double = subtotal + ppn
            textTotal.text = nf.format(total)
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            DialogAlert("Internetmu lemot..! $e", "error", this.requireActivity())
        }
    }

    private fun ubahAngka(angka: String): String {
        val buangRibuan = angka.replace(".", "")

        return buangRibuan.replace(",", ".")
    }
}
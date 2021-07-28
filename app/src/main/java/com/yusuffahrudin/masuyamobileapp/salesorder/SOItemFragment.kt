package com.yusuffahrudin.masuyamobileapp.salesorder

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.yusuffahrudin.masuyamobileapp.R
import com.yusuffahrudin.masuyamobileapp.api.API
import com.yusuffahrudin.masuyamobileapp.api.ApiService
import com.yusuffahrudin.masuyamobileapp.controller.DialogAlert
import com.yusuffahrudin.masuyamobileapp.data.ArrayTampung
import com.yusuffahrudin.masuyamobileapp.data.Result
import com.yusuffahrudin.masuyamobileapp.data.sales_order.ItemOrder
import com.yusuffahrudin.masuyamobileapp.data.sales_order.SalesOrder
import com.yusuffahrudin.masuyamobileapp.databinding.FragmentSoItemBinding
import com.yusuffahrudin.masuyamobileapp.databinding.LvTroliBinding
import com.yusuffahrudin.masuyamobileapp.firebase.SendNotification
import com.yusuffahrudin.masuyamobileapp.util.Server
import com.yusuffahrudin.masuyamobileapp.util.SessionManager
import org.jetbrains.anko.doAsync
import retrofit2.Call
import retrofit2.Callback
import java.text.NumberFormat
import java.util.*

class SOItemFragment: Fragment() {

    private lateinit var nobukti: String
    private lateinit var userKota: String
    private lateinit var kdkota: String
    private lateinit var user: String
    private lateinit var statusPajak: String
    private var underCost = false
    private var underBottomSO= false
    private var underBottomSP= false
    private lateinit var sessionManager: SessionManager
    private val listChart: ArrayList<ItemOrder> = ArrayTampung.getListChart()
    private val listChartLama: ArrayList<ItemOrder> = ArrayTampung.getListChartLama()
    private lateinit var listSO: ArrayList<SalesOrder>
    private val nf = NumberFormat.getInstance(Locale("pt", "BR"))
    lateinit var adapter: AdapterRV
    private lateinit var layoutManager: LinearLayoutManager
    private var _binding: FragmentSoItemBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSoItemBinding.inflate(inflater, container, false)
        val rootView = binding.root

        sessionManager = SessionManager(this.context)
        val session = sessionManager.userDetails
        userKota = session[SessionManager.kota].toString()
        kdkota = session[SessionManager.kdkota].toString()
        user = session[SessionManager.kunci_email].toString()
        if (session[SessionManager.underCost].toString() == "1") underCost = true
        if (session[SessionManager.underBottomSO].toString() == "1") underBottomSO = true
        if (session[SessionManager.underBottomSP].toString() == "1") underBottomSP = true
        listSO = ArrayTampung.getListHeaderSO()

        val i = this.activity?.intent
        nobukti = i?.extras?.getString("nobukti").toString()

        statusPajak = "00"
        binding.lvBarang.setHasFixedSize(true)
        layoutManager = LinearLayoutManager(this.requireContext())
        binding.lvBarang.layoutManager = layoutManager

        adapter = AdapterRV(this.requireActivity())
        binding.lvBarang.adapter = adapter

        binding.swipeRefresh.setOnRefreshListener {
            if (listChart.isEmpty()){
                selectSalesOrder()
            } else {
                adapter.notifyDataSetChanged()
                binding.swipeRefresh.isRefreshing = false
            }
        }

        binding.edtDiscfakPersen.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                var subtotal = ubahAngka(binding.tvSubtotal.text.toString()).toDouble()
                val discfak = subtotal * (1 - ( ubahAngka(binding.edtDiscfakPersen.text.toString()).toDouble() / 100 ))
                binding.tvDiscfakTotal.text = nf.format(discfak)
                subtotal -= discfak
                val total = subtotal + ubahAngka(binding.tvPpnTotal.text.toString()).toDouble()
                binding.tvTotal.text = nf.format(total)
                true
            }
            false
        }

        binding.edtPpnPersen.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                var subtotal = ubahAngka(binding.tvSubtotal.text.toString()).toDouble()
                val discfak = ubahAngka(binding.tvDiscfakTotal.text.toString()).toDouble()
                subtotal -= discfak
                val ppn = subtotal * (1 - ( ubahAngka(binding.edtPpnPersen.text.toString()).toDouble() / 100 ))
                binding.tvPpnTotal.text = nf.format(ppn)
                val total = subtotal + ppn
                binding.tvTotal.text = nf.format(total)
            }
            false
        }

        return rootView
    }

    fun setView(){
        if(listSO[0].statusorder == "OPEN"){
            binding.edtDiscfakPersen.isEnabled = true
            binding.edtPpnPersen.isEnabled = true
        } else {
            binding.edtDiscfakPersen.isEnabled = false
            binding.edtPpnPersen.isEnabled = false
        }
        selectSalesOrder()
    }

    fun getTotal(){
        listSO[0].subtotal = ubahAngka(binding.tvSubtotal.text.toString()).toDouble()
        listSO[0].disc = ubahAngka(binding.edtDiscfakPersen.text.toString()).toDouble()
        listSO[0].jmldisc1 = ubahAngka(binding.tvDiscfakTotal.text.toString()).toDouble()
        listSO[0].prsppn = ubahAngka(binding.edtPpnPersen.text.toString()).toDouble()
        listSO[0].ppn = ubahAngka(binding.tvPpnTotal.text.toString()).toDouble()
        listSO[0].total = ubahAngka(binding.tvTotal.text.toString()).toDouble()
    }

    //fungsi untuk select data dari database
    fun selectSalesOrder() {
        listChart.clear()
        listChartLama.clear()

        val model = ViewModelProviders.of(this).get(DetailSOViewModel::class.java)
        model.getDetail(requireActivity(), nobukti).observe(this, { detailList: ArrayList<ItemOrder> ->
            run {
                for (list in detailList) {
                    statusPajak = list.jnsJualTax.toString()
                    val hrg = list.harga!! * list.qtyOrder!!
                    val subtotal = hrg * (1 - list.diskon1!! / 100) * (1 - list.diskon2!! / 100) * (1 - list.diskon3!! / 100)
                    list.subTotal = subtotal
                    listSO[0].kodeTax = statusPajak
                    listSO[0].jnsjualtax = list.jnsJualTax.toString()
                }
                listChart.addAll(detailList)
                listChartLama.addAll(detailList)
                hitungTotal(listChart)
                adapter.notifyDataSetChanged()
                binding.swipeRefresh.isRefreshing = false
            }
        })
    }

    inner class AdapterRV(private val activity: Activity): RecyclerView.Adapter<AdapterRV.ViewHolder>() {
        var qty: Double = 0.toDouble()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = LvTroliBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(binding)
        }

        override fun getItemCount(): Int {
            return listChart.size
        }

        @SuppressLint("SetTextI18n")
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.binding.cbOtoUc.visibility = View.GONE
            holder.binding.cbOtoUb.visibility = View.GONE
            holder.binding.tvKdbrg.text = listChart[position].kdBrg
            holder.binding.tvNmbrg.text = listChart[position].nmBrg
            holder.binding.edtQty.setText(nf.format(listChart[position].qtyOrder))
            holder.binding.tvHarga.text = nf.format(listChart[position].harga)
            holder.binding.tvDiskon1.text = "${nf.format(listChart[position].diskon1)} %"
            holder.binding.tvDiskon2.text = "${nf.format(listChart[position].diskon2)} %"
            holder.binding.tvDiskon3.text = "${nf.format(listChart[position].diskon3)} %"
            holder.binding.tvSubtotal.text = nf.format(listChart[position].subTotal)
            holder.binding.tvSatuan.text = listChart[position].satuan
            if (listChart[position].needOtoUC && listChart[position].needOtoUB){
                holder.binding.cbOtoUc.visibility = View.VISIBLE
                holder.binding.cbOtoUc.isChecked = listChart[position].otoUC.toString() == "1"
                if(listChart[position].otoUCby.toString() == "null") holder.binding.cbOtoUc.text = resources.getString(R.string.required_oto_uc)
                else holder.binding.cbOtoUc.text = "${resources.getString(R.string.oto_uc)} by ${listChart[position].otoUCby}"
                holder.binding.cbOtoUc.isEnabled = false
                if (underCost)
                    holder.binding.cbOtoUc.isEnabled = true

                holder.binding.cbOtoUb.visibility = View.VISIBLE
                holder.binding.cbOtoUb.isChecked = listChart[position].otoUB.toString() == "1"
                if(listChart[position].otoUBby.toString() == "null") holder.binding.cbOtoUb.text = resources.getString(R.string.required_oto_ub)
                else holder.binding.cbOtoUb.text = "${resources.getString(R.string.oto_ub)} by ${listChart[position].otoUBby}"
                holder.binding.cbOtoUb.isEnabled = false
                holder.binding.cbOtoUb.isEnabled = underBottomSP
            }
            if (!listChart[position].needOtoUC && listChart[position].needOtoUB){
                holder.binding.cbOtoUb.visibility = View.VISIBLE
                holder.binding.cbOtoUb.isChecked = listChart[position].otoUB.toString() == "1"
                if(listChart[position].otoUBby.toString() == "null") holder.binding.cbOtoUb.text = resources.getString(R.string.required_oto_ub)
                else holder.binding.cbOtoUb.text = "${resources.getString(R.string.oto_ub)} by ${listChart[position].otoUBby}"
                holder.binding.cbOtoUb.isEnabled = false
                if (underBottomSP)
                    holder.binding.cbOtoUb.isEnabled = true
                else holder.binding.cbOtoUb.isEnabled = underBottomSO
            }

            try {
                if(listSO[0].statusorder == "OPEN"){
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

            holder.binding.edtQty.setOnEditorActionListener { _, actionId, event ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    try {
                        qty = ubahAngka(holder.binding.edtQty.text.toString()).toDouble()
                        holder.binding.tvSubtotal.text = nf.format(hitungSubtotalItem(listChart[position].harga!!, qty, listChart[position].diskon1!!, listChart[position].diskon2!!, listChart[position].diskon3!!))
                        listChart[position].qtyOrder = qty
                        listChart[position].subTotal = ubahAngka(holder.binding.tvSubtotal.text.toString()).toDouble()
                        adapter.notifyDataSetChanged()
                        hitungTotal(listChart)
                    } catch (e: Exception) {
                        DialogAlert(e.message.toString(), "error", this.activity)
                        FirebaseCrashlytics.getInstance().recordException(e)
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
                    DialogAlert(e.message.toString(), "error", this.activity)
                    FirebaseCrashlytics.getInstance().recordException(e)
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
                    DialogAlert(e.message.toString(), "error", this.activity)
                    FirebaseCrashlytics.getInstance().recordException(e)
                }
            }
            holder.binding.btnHapus.setOnClickListener {
                try {
                    listChart.removeAt(position)
                    hitungTotal(listChart)
                    adapter.notifyDataSetChanged()
                } catch (e: Exception) {
                    DialogAlert(e.message.toString(), "error", this.activity)
                    FirebaseCrashlytics.getInstance().recordException(e)
                }
            }

            holder.binding.cbOtoUc.isEnabled = false
            holder.binding.cbOtoUb.isEnabled = false
            /*holder.binding.cbOtoUc.setOnClickListener {
                var oto = "0"
                if (holder.binding.cbOtoUc.isChecked) oto = "1"
                if (underCost) updateAutho("0", "0", "1", oto, user, holder.binding.tvKdbrg.text.toString())
            }

            holder.binding.cbOtoUb.setOnClickListener {
                var oto = "0"
                if (holder.binding.cbOtoUb.isChecked) oto = "1"
                if (underBottomSP) updateAutho("1", "0", "0", oto, user, holder.binding.tvKdbrg.text.toString())
                else if (underBottomSO) updateAutho("0", "1", "0", oto, user, holder.binding.tvKdbrg.text.toString())
            }*/

            hitungTotal(listChart)
        }

        inner class ViewHolder(val binding: LvTroliBinding) : RecyclerView.ViewHolder(binding.root)

    }

    private fun hitungSubtotalItem(harga: Double, qty: Double, disc1: Double, disc2: Double, disc3: Double): Double {
        var hrg = harga
        hrg *= (1 - disc1 / 100) * (1 - disc2 / 100) * (1 - disc3 / 100)
        val  subtotal = harga * qty
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
            binding.tvSubtotal.text = nf.format(subtotal)
            //==================== Discfak ======================
            var discfak: Double = ubahAngka(binding.edtDiscfakPersen.text.toString()).toDouble() / 100
            discfak *=  subtotal
            binding.tvDiscfakTotal.text = nf.format(discfak)
            subtotal -= discfak
            //==================== Ppn ======================
            if (statusPajak.equals("PPN", ignoreCase = true)) {
                binding.edtPpnPersen.setText("10")
                ppn = ubahAngka(binding.edtPpnPersen.text.toString()).toDouble() / 100
                ppn *= subtotal
                binding.tvPpnTotal.text = nf.format(ppn)
            } else {
                binding.edtPpnPersen.setText("0")
                ppn = ubahAngka(binding.edtPpnPersen.text.toString()).toDouble() / 100
                ppn *= subtotal
                binding.tvPpnTotal.text = nf.format(ppn)
            }
            //==================== Total ======================
            val total: Double = subtotal + ppn
            binding.tvTotal.text = nf.format(total)
        } catch (e: Exception) {
            DialogAlert(e.message.toString(), "error", this.requireActivity())
            FirebaseCrashlytics.getInstance().recordException(e)
        }
    }

    private fun ubahAngka(angka: String): String {
        val buangRibuan = angka.replace(".", "")

        return buangRibuan.replace(",", ".")
    }

    private fun updateAutho(underbottomsp: String, underbottomso: String, undercost: String, oto: String, who: String, kdbrg: String) {
        doAsync {
            API.instance().create(ApiService::class.java)
                    .updateOtoHargaSO(underbottomsp, underbottomso, undercost, oto, who, nobukti, kdbrg)
                    ?.enqueue(object : Callback<Result?> {
                        override fun onFailure(call: Call<Result?>, e: Throwable) {
                            DialogAlert(e.message, "error", requireActivity())
                        }

                        override fun onResponse(call: Call<Result?>, response: retrofit2.Response<Result?>) {
                            if (response.isSuccessful) {
                                val message = response.body()?.message.toString()
                                if (response.body()?.success == 1) {
                                    val notif = SendNotification()
                                    if (oto == "1") {
                                        notif.pushNotifSales(requireContext(), kdkota, "$nobukti - SO Accepted by $who",
                                            listSO[0].nmcust.toString(),
                                                "SO",
                                            listSO[0].nmsales.toString())
                                    }
                                    if (oto == "0") {
                                        notif.pushNotifSales(requireContext(), kdkota, "$nobukti - SO Rejected by $who",
                                            listSO[0].nmcust.toString(),
                                                "SO",
                                            listSO[0].nmsales.toString())
                                    }
                                    selectSalesOrder()

                                    DialogAlert(message, "success", requireActivity())
                                } else {
                                    DialogAlert(message, "error", requireActivity())
                                }
                            }
                        }
                    })
        }
    }
}
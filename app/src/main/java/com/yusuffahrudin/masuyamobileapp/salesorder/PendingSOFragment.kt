package com.yusuffahrudin.masuyamobileapp.salesorder

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.yusuffahrudin.masuyamobileapp.R
import com.yusuffahrudin.masuyamobileapp.controller.DialogAlert
import com.yusuffahrudin.masuyamobileapp.data.sales_order.SalesOrder
import com.yusuffahrudin.masuyamobileapp.databinding.FragmentSoAllBinding
import com.yusuffahrudin.masuyamobileapp.databinding.LvSoAllBinding
import com.yusuffahrudin.masuyamobileapp.util.DpToPx
import com.yusuffahrudin.masuyamobileapp.util.SessionManager
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class PendingSOFragment: Fragment() {
    private lateinit var adapter: AdapterSO
    private lateinit var name: String
    private lateinit var level: String
    private lateinit var kdkota: String
    private var i = 0
    private var nobukti: String     = ""
    private var cust: String        = ""
    private var autho: String       = "-"
    private var status: String       = "All"
    private var item: String       = "All"
    private var _binding: FragmentSoAllBinding? = null
    private val binding get() = _binding!!
    companion object {
        lateinit var listSO: ArrayList<SalesOrder>
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSoAllBinding.inflate(inflater, container, false)
        val rootView = binding.root

        val sessionManager = SessionManager(requireActivity().applicationContext)
        val user = sessionManager.userDetails
        name = user[SessionManager.kunci_email].toString()
        level = user[SessionManager.level].toString()
        kdkota = user[SessionManager.kdkota].toString()

        binding.rvMain.setHasFixedSize(true)
        val layoutManager = LinearLayoutManager(context)
        binding.rvMain.layoutManager = layoutManager
        binding.progressbar.visibility = View.GONE

        listSO = ArrayList()
        adapter = AdapterSO(listSO, requireActivity())
        binding.rvMain.adapter = adapter

        if (listSO.isEmpty()) {
            selectSO(0)
        }

        binding.rvMain.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
                if (firstVisibleItemPosition + visibleItemCount >= totalItemCount) {
                    if (i == 0) {
                        selectSO(totalItemCount)
                        i = 1
                    }
                } else {
                    i = 0
                }
            }
        })

        binding.swipeRefresh.setOnRefreshListener {
            listSO.clear()
            selectSO(0)
        }

        return rootView
    }

    fun reload() {
        listSO.clear()
        selectSO(0)
    }

    fun filter(nobukti: String, cust: String, autho: String, status: String, item: String){
        listSO.clear()
        this.nobukti = nobukti
        this.cust = cust
        this.autho = autho
        this.status = status
        this.item = item

        selectSO(0)
    }

    private fun selectSO(itemCount: Int){
        binding.progressbar.visibility = View.VISIBLE
        var user = ""
        if (level.equals("1010201010", ignoreCase = true) || level.equals("1010301010", ignoreCase = true)) {
            user = name
        }
        val model = ViewModelProviders.of(this).get(SalesOrderViewModel::class.java)
        model.getSO(requireActivity(), user, itemCount, "Pending", nobukti, cust, autho, status, item).observe(this, {
            soList: ArrayList<SalesOrder> ->
            run {
                for (i in 0 until soList.size) {
                    if (i == 0) {
                        listSO.add(soList[i])
                    } else {
                        val index = listSO.size
                        val nobukti = soList[i].nobukti
                        val needOtoUc = soList[i].needOtoUC
                        val needOtoUb = soList[i].needOtoUB
                        val otoUc = soList[i].otoUC.toString()
                        val otoUb = soList[i].otoUB.toString()
                        if (listSO[index - 1].nobukti == nobukti) {
                            if (!listSO[index - 1].needOtoUC && needOtoUc) listSO[index - 1].needOtoUC = needOtoUc
                            else if (listSO[index - 1].needOtoUC && needOtoUc) {
                                if (listSO[index - 1].otoUC == "1" && otoUc == "null") {
                                    listSO[index - 1].otoUC = otoUc
                                    listSO[index - 1].otoUCby = otoUc
                                }
                            }
                            if (!listSO[index - 1].needOtoUB && needOtoUb) listSO[index - 1].needOtoUB = needOtoUb
                            else if (listSO[index - 1].needOtoUB && needOtoUb) {
                                if (listSO[index - 1].otoUB == "1" && otoUb == "null") {
                                    listSO[index - 1].otoUB = otoUb
                                    listSO[index - 1].otoUBby = otoUb
                                }
                            }
                        } else {
                            listSO.add(soList[i])
                        }
                    }
                }
                i++
                //rvSo.recycledViewPool.clear()
                adapter.notifyDataSetChanged()
                binding.progressbar.visibility = View.GONE


                /*listSO.addAll(soList)
                binding.sectionLabel.visibility = View.GONE
                if (listSO.size == 0) {
                    binding.sectionLabel.visibility = View.VISIBLE
                }
                i++
                adapter.notifyDataSetChanged()*/
            }
        })
        binding.swipeRefresh.isRefreshing = false
        binding.progressbar.visibility = View.GONE
    }

    private class AdapterSO(val list_tampung: ArrayList<SalesOrder>, val activity: Activity): RecyclerView.Adapter <AdapterSO.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = LvSoAllBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(binding)
        }

        override fun getItemCount(): Int {
            return list_tampung.size
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

            if (listSO[position].status.equals("Verified", ignoreCase = true) || listSO[position].status.equals("", ignoreCase = true)) {
                holder.binding.viewStatus.requestLayout()
                holder.binding.tvStatus.text = listSO[position].status
                holder.binding.tvStatus.setTextColor(ContextCompat.getColor(activity, R.color.flatui_nephritis))
            } else if (listSO[position].status!!.contains("!")) {
                holder.binding.tvStatus.text = activity.getString(R.string.peringatan)
                holder.binding.tvStatus.setTextColor(ContextCompat.getColor(activity, R.color.flatui_sun_flower))
                holder.binding.viewStatus.requestLayout()
                holder.binding.line2.visibility = View.VISIBLE
                holder.binding.imgOtoOdar.visibility = View.VISIBLE
                holder.binding.tvOtoOdar.visibility = View.VISIBLE
                //set status AR
                if (listSO[position].otoODAR == "1") {
                    holder.binding.imgOtoOdar.setImageResource(R.drawable.ic_ok_64)
                    holder.binding.tvOtoOdar.text = "${activity.getString(R.string.oto_by)} ${listSO[position].otoODARby?.toUpperCase(Locale.getDefault())}"
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
                            }
                        }
                    }
                } else if(listSO[position].otoODAR == "0") {
                    holder.binding.imgOtoOdar.setImageResource(R.drawable.ic_delete_48)
                    holder.binding.tvOtoOdar.text = "Rejected by ${listSO[position].otoODARby?.toUpperCase(Locale.getDefault())}"
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
                        }
                    }
                } else {
                    holder.binding.imgOtoOdar.setImageResource(R.drawable.ic_error_48)
                    holder.binding.tvOtoOdar.setText(R.string.required_oto_odar)
                }

            }

            if (listSO[position].status!!.contains("harga jual min")){
                //set status SM
                holder.binding.tvStatus.text = activity.getString(R.string.peringatan)
                holder.binding.tvStatus.setTextColor(ContextCompat.getColor(activity, R.color.flatui_sun_flower))
                holder.binding.viewStatus.requestLayout()
                holder.binding.imgOtoUb.visibility = View.VISIBLE
                holder.binding.tvOtoUb.visibility = View.VISIBLE
                holder.binding.line3.visibility = View.VISIBLE
                if (listSO[position].needOtoUB && listSO[position].otoUB == "1") {
                    holder.binding.imgOtoUb.setImageResource(R.drawable.ic_ok_64)
                    holder.binding.tvOtoUb.text = "${activity.getString(R.string.oto_by)} ${listSO[position].otoUBby?.toUpperCase(Locale.getDefault())}"
                } else {
                    holder.binding.imgOtoUb.setImageResource(R.drawable.ic_error_48)
                    holder.binding.tvOtoUb.setText(R.string.required_oto_ub)
                }
            }

            if (listSO[position].status!!.contains("pokok")){
                holder.binding.tvStatus.text = activity.getString(R.string.peringatan)
                holder.binding.tvStatus.setTextColor(ContextCompat.getColor(activity, R.color.flatui_sun_flower))
                holder.binding.viewStatus.requestLayout()
                holder.binding.imgOtoUc.visibility = View.VISIBLE
                holder.binding.tvOtoUc.visibility = View.VISIBLE
                holder.binding.line3.visibility = View.VISIBLE
                //set status BM
                if (listSO[position].needOtoUC && listSO[position].otoUC == "1") {
                    holder.binding.imgOtoUc.setImageResource(R.drawable.ic_ok_64)
                    holder.binding.tvOtoUc.text = "${activity.getString(R.string.oto_by)} ${listSO[position].otoUCby?.toUpperCase(Locale.getDefault())}"
                } else {
                    holder.binding.imgOtoUc.setImageResource(R.drawable.ic_error_48)
                    holder.binding.tvOtoUc.setText(R.string.required_oto_uc)
                }
            }

            val dateStr: String = list_tampung[position].tglcreate.toString()
            var sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            var date: Date? = null
            try {
                date = sdf.parse(dateStr)
            } catch (e: ParseException) {
                e.printStackTrace()
            }
            sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            val tgl = sdf.format(date)

            holder.binding.tvNobukti.text = list_tampung[position].nobukti
            holder.binding.tvNmcust.text = list_tampung[position].nmcust
            holder.binding.tvTgl.text = tgl
            holder.binding.tvNmsales.text = list_tampung[position].nmsales
            holder.binding.viewStatus.setColorFilter(ContextCompat.getColor(activity, R.color.flatui_sun_flower))

            holder.binding.cvMain.setOnClickListener {
                //Intent i = new Intent(getActivity(), DetailSalesOrderActivity.class);
                val i = Intent(activity, DetailSOActivity::class.java)
                i.putExtra("nobukti", list_tampung[position].nobukti)
                i.putExtra("nmsales", list_tampung[position].nmsales)
                activity.startActivity(i)
            }

            holder.binding.tvStatus.setOnClickListener {
                if (!list_tampung[position].status.equals("Verified", ignoreCase = true) || !list_tampung[position].status.equals("", ignoreCase = true)) {
                    DialogAlert(list_tampung[position].status, "attention", activity)
                }
            }
        }

        inner class ViewHolder(val binding: LvSoAllBinding) : RecyclerView.ViewHolder(binding.root)
    }
}
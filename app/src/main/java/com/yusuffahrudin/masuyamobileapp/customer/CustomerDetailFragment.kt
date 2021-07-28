package com.yusuffahrudin.masuyamobileapp.customer

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.yusuffahrudin.masuyamobileapp.R
import com.yusuffahrudin.masuyamobileapp.api.API
import com.yusuffahrudin.masuyamobileapp.api.ApiService
import com.yusuffahrudin.masuyamobileapp.controller.DialogAlert
import com.yusuffahrudin.masuyamobileapp.data.Customer
import com.yusuffahrudin.masuyamobileapp.data.customer.HutangCust
import com.yusuffahrudin.masuyamobileapp.data.customer.HutangCustDtl
import com.yusuffahrudin.masuyamobileapp.data.customer.HutangCustResponse
import com.yusuffahrudin.masuyamobileapp.databinding.FragmentCustDetailBinding
import com.yusuffahrudin.masuyamobileapp.databinding.ListGroupHutangCustBinding
import retrofit2.Call
import retrofit2.Callback
import java.text.NumberFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.LinkedHashMap

class CustomerDetailFragment: Fragment() {
    private lateinit var kdcust: String
    private lateinit var top: String
    private lateinit var koordinat: String
    private val listDataGroup: MutableList<HutangCust> = ArrayList()
    private val listDataChild : LinkedHashMap<String, List<HutangCustDtl>> = LinkedHashMap()
    private lateinit var expandableAdapter : ExpandableAdapter
    private val nf = NumberFormat.getInstance(Locale("in", "ID"))
    private var _binding: FragmentCustDetailBinding? = null
    private val binding get() = _binding!!
    companion object {
        val EXTRA_CUSTOMER = "parcelable_customer"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentCustDetailBinding.inflate(inflater, container, false)
        val view = binding.root

        val customer = requireActivity().intent.getParcelableExtra<Customer>(EXTRA_CUSTOMER)!!
        setLayout(customer)

        binding.btnMap.setOnClickListener{
            val gmmIntentUri = Uri.parse("google.navigation:q=$koordinat")
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            mapIntent.setPackage("com.google.android.apps.maps")
            if (mapIntent.resolveActivity(requireActivity().packageManager) != null) {
                startActivity(mapIntent)
            }
        }

        binding.btnSaveLocation.setOnClickListener{
            if (koordinat == null || koordinat == "") {
                val intent = Intent(requireContext(), SimpanLokasiActivity::class.java)
                intent.putExtra("kdcust", binding.tvKdcust.text)
                intent.putExtra("nmcust", binding.tvNmcust.text)
                intent.putExtra("alm", binding.tvAlm1.text)
                startActivityForResult(intent, 1)
            } else {
                dialogPeringatan()
            }
        }

        binding.tvSaldo.setOnClickListener{ dialogHutangCust(top) }

        return view
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            println("Sukses")
            koordinat = data!!.getStringExtra("lat") + ", " + data.getStringExtra("lng")
            println(koordinat)
            binding.btnMap.isEnabled = true
            binding.tvKoordinat.text = koordinat
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setLayout(customer: Customer){
        kdcust      = customer.kdcust
        top         = customer.top

        binding.tvKdcust.text       = kdcust
        binding.tvNmcust.text       = customer.nmcust
        binding.tvTypecust.text     = customer.typecust
        binding.tvAlm1.text         = customer.alm1
        binding.tvKota.text         = customer.kota
        binding.tvTelp1.text        = customer.telp1
        binding.tvTelp2.text        = customer.telp2
        binding.tvSaldo.text        = getString(R.string.currency_id) + nf.format(customer.saldo)
        binding.tvSales.text        = customer.sales
        binding.tvLimit.text        = getString(R.string.currency_id) + nf.format(customer.kreditLimit)
        binding.tvKoordinat.text    = getString(R.string.titik_koordinat)+customer.koordinat
        koordinat                   = customer.koordinat
        binding.btnMap.isEnabled    = !(customer.koordinat == null || customer.koordinat.equals(""))
    }

    private fun dialogPeringatan() {
        val dPeringatan = BottomSheetDialog(requireActivity(), R.style.SheetDialog)
        dPeringatan.setContentView(R.layout.dialog_yes_or_no)
        val dialogView = dPeringatan.findViewById<FrameLayout>(R.id.design_bottom_sheet)
        dPeringatan.show()

        val btnYes = dialogView?.findViewById<Button>(R.id.btn_ya)
        val btnNo = dialogView?.findViewById<Button>(R.id.btn_tidak)
        val tvMessage = dialogView?.findViewById<TextView>(R.id.tv_message)
        tvMessage?.text = getString(R.string.replace_koordinat)

        btnYes?.setOnClickListener {
            val intent = Intent(requireContext(), SimpanLokasiActivity::class.java)
            intent.putExtra("kdcust", binding.tvKdcust.text)
            intent.putExtra("nmcust", binding.tvNmcust.text)
            intent.putExtra("alm", binding.tvAlm1.text)
            startActivityForResult(intent, 1)
            dPeringatan.dismiss()
        }

        btnNo?.setOnClickListener { dPeringatan.dismiss() }
    }

    // untuk menampilkan dialog
    @SuppressLint("InflateParams", "SetTextI18n")
    private fun dialogHutangCust(top: String) {
        val dHutangCust = BottomSheetDialog(requireActivity(), R.style.SheetDialog)
        val bottomSheetLayout = layoutInflater.inflate(R.layout.dialog_hutang_cust, null)
        dHutangCust.setContentView(bottomSheetLayout)
        dHutangCust.setCancelable(false)

        val tvTop = bottomSheetLayout.findViewById<TextView>(R.id.tv_top)
        val btnOK = bottomSheetLayout.findViewById<Button>(R.id.btn_ok)
        val expandableListView = bottomSheetLayout.findViewById<ExpandableListView>(R.id.lv_exp)
        val progressBar = bottomSheetLayout.findViewById<ProgressBar>(R.id.progressbar)
        tvTop.text = "TOP : $top hari"
        expandableAdapter = ExpandableAdapter(listDataGroup, listDataChild)
        expandableListView.setAdapter(expandableAdapter)

        selectHutangCust(progressBar)
        dHutangCust.show()

        btnOK.setOnClickListener {
            dHutangCust.dismiss()
        }
    }

    //fungsi untuk select data dari database
    private fun selectHutangCust(progressBar: ProgressBar) {
        progressBar.visibility = View.VISIBLE
        listDataGroup.clear()
        listDataChild.clear()
        API.instance().create(ApiService::class.java)
                .getHutangCust(kdcust)
                ?.enqueue(object : Callback<HutangCustResponse?> {
                    override fun onFailure(call: Call<HutangCustResponse?>, e: Throwable) {
                        progressBar.visibility = View.GONE
                        Log.d("Get Hutang Cust", "Error $e")
                        FirebaseCrashlytics.getInstance().recordException(e)
                        DialogAlert(e.message, "error", requireActivity())
                    }

                    override fun onResponse(call: Call<HutangCustResponse?>, response: retrofit2.Response<HutangCustResponse?>) {
                        if (response.isSuccessful) {
                            println("====================================== berhasil")
                            var nobukti = ""
                            for (i in 0 until response.body()?.result!!.size) {
                                println(response.body()?.result!![i].noBukti.toString())
                                if (!nobukti.equals(response.body()?.result!![i].noBukti.toString(), true)){
                                    val group = HutangCust()
                                    group.noBukti = response.body()?.result!![i].noBukti
                                    group.tgl = response.body()?.result!![i].tgl
                                    group.total = response.body()?.result!![i].total
                                    group.bayar = response.body()?.result!![i].bayar
                                    listDataGroup.add(group)
                                    nobukti = group.noBukti.toString()

                                    val listChild = ArrayList<HutangCustDtl>()
                                    for (j in 0 until response.body()?.result!!.size){
                                        if (nobukti.equals(response.body()?.result!![j].noBukti.toString(), true)){
                                            println(response.body()?.result!![j].kdBrg.toString())
                                            val child = HutangCustDtl()
                                            child.kdBrg = response.body()?.result!![j].kdBrg
                                            child.nmBrg = response.body()?.result!![j].nmBrg
                                            child.qty = response.body()?.result!![j].qty
                                            child.satuan = response.body()?.result!![j].satuan
                                            listChild.add(child)
                                        }
                                    }
                                    listDataChild[nobukti] = listChild
                                }
                            }

                            progressBar.visibility = View.GONE
                            expandableAdapter.notifyDataSetChanged()
                        }
                    }

                })
    }

    private class ExpandableAdapter(private val listDataGroup: List<HutangCust>,
                                     private val listDataChild: LinkedHashMap<String, List<HutangCustDtl>>): BaseExpandableListAdapter() {
        private val nf = NumberFormat.getInstance(Locale("in", "ID"))

        override fun getGroup(groupPosition: Int): HutangCust {
            println("====================================== GROUP")
            return listDataGroup[groupPosition]
        }

        override fun getGroupId(groupPosition: Int): Long {
            return groupPosition.toLong()
        }

        override fun getGroupCount(): Int {
            println("====================================== GROUP SIZE")
            return listDataGroup.size
        }

        override fun getGroupView(groupPosition: Int, p1: Boolean, convertView: View?, parent: ViewGroup?): View {
            var view = convertView
            val binding = ListGroupHutangCustBinding.inflate(LayoutInflater.from(parent?.context), parent, false)
            if (view == null) {
                view = binding.root
            }

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
            println(tgl)
            println(getGroup(groupPosition).total)
            binding.tvNobukti.text = getGroup(groupPosition).noBukti
            binding.tvTgl.text = tgl
            binding.tvTotal.text = "Total invoice : ${nf.format(getGroup(groupPosition).total)}"
            binding.tvBayar.text = "Sudah bayar : ${nf.format(getGroup(groupPosition).bayar)}"
            binding.tvSisa.text = "Sisa : ${nf.format(getGroup(groupPosition).total!!.toDouble() - getGroup(groupPosition).bayar!!.toDouble())}"

            return view
        }

        override fun isChildSelectable(p0: Int, p1: Int): Boolean {
            return true
        }

        override fun hasStableIds(): Boolean {
            return true
        }

        override fun getChild(groupPosition: Int, childPosition: Int): HutangCustDtl? {
            val data = listDataChild[listDataGroup[groupPosition].noBukti]
            println("====================================== CHILD")
            return data?.get(childPosition)
        }

        override fun getChildId(groupPosition: Int, childPosition: Int): Long {
            return childPosition.toLong()
        }

        override fun getChildrenCount(groupPosition: Int): Int {
            println("====================================== CHILD SIZE")
            println("-------------"+listDataChild[listDataGroup[groupPosition].noBukti]!!.size)
            return listDataChild[listDataGroup[groupPosition].noBukti]!!.size
        }

        @SuppressLint("SetTextI18n")
        override fun getChildView(groupPosition: Int, childPosition: Int, isLastChild: Boolean, convertView: View?, parent: ViewGroup?): View? {
            var view = convertView
            val data = getChild(groupPosition, childPosition)
            if (view == null) {
                view = LayoutInflater.from(parent?.context).inflate(R.layout.list_child_hutang_cust, parent, false)
            }
            val tvKdbrg = view?.findViewById<TextView>(R.id.tv_kdbrg)
            val tvNmbrg = view?.findViewById<TextView>(R.id.tv_nmbrg)
            val tvQty = view?.findViewById<TextView>(R.id.tv_qty)
            val tvSatuan = view?.findViewById<TextView>(R.id.tv_satuan)

            println("====================================== CHILD VIEW")
            println(data?.nmBrg)
            tvKdbrg?.text = data?.kdBrg
            tvNmbrg?.text = data?.nmBrg
            tvQty?.text = nf.format(data?.qty)
            tvSatuan?.text = data?.satuan

            return view
        }
    }
}
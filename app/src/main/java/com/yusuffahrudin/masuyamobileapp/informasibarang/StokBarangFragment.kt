package com.yusuffahrudin.masuyamobileapp.informasibarang

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.evrencoskun.tableview.TableView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.yusuffahrudin.masuyamobileapp.R
import com.yusuffahrudin.masuyamobileapp.adapter.AdapterTableView
import com.yusuffahrudin.masuyamobileapp.api.API
import com.yusuffahrudin.masuyamobileapp.api.ApiService
import com.yusuffahrudin.masuyamobileapp.controller.DialogAlert
import com.yusuffahrudin.masuyamobileapp.data.CellModel
import com.yusuffahrudin.masuyamobileapp.data.ColumnHeaderModel
import com.yusuffahrudin.masuyamobileapp.data.RowHeaderModel
import com.yusuffahrudin.masuyamobileapp.data.evaluasi_barang.KotaResponse
import com.yusuffahrudin.masuyamobileapp.data.informasi_barang.KartuStokResponse
import com.yusuffahrudin.masuyamobileapp.data.informasi_barang.Stok
import com.yusuffahrudin.masuyamobileapp.databinding.DialogStokRusakSelisihBinding
import com.yusuffahrudin.masuyamobileapp.databinding.FragmentStokBarangBinding
import com.yusuffahrudin.masuyamobileapp.util.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by yusuf fahrudin on 17-01-2018.
 */
class StokBarangFragment : Fragment() {
    private lateinit var kdkota: String
    private lateinit var kdbrg: String
    private var kota = ""
    private var user = ""
    private val listStok: MutableList<Stok> = ArrayList()
    private val listStokRusak: MutableList<Stok> = ArrayList()
    private lateinit var kotaArray: Array<String>
    private lateinit var listKota: MutableList<String>
    private val nf = NumberFormat.getInstance(Locale("in", "ID"))
    private lateinit var adapter: AdapterStok
    private lateinit var adapterRusak: AdapterStokRusak
    private var adapterSpinner: ArrayAdapter<String?>? = null
    private val colValues: MutableList<ColumnHeaderModel> = mutableListOf()
    private val rowValues: MutableList<RowHeaderModel> = mutableListOf()
    private val cellValues: MutableList<MutableList<CellModel>> = mutableListOf()
    private lateinit var addCache: MutableList<CellModel>
    private var isViewDialogKartuStok = false
    private var isViewDialogStokRusak = false
    private var _binding: FragmentStokBarangBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        _binding = FragmentStokBarangBinding.inflate(inflater, container, false)
        val rootView = binding.root

        val sessionManager = SessionManager(requireContext())
        val cache = sessionManager.userDetails
        kdkota = cache[SessionManager.kdkota].toString()
        val userKota = cache[SessionManager.kota].toString()
        user = cache.get(SessionManager.kunci_email).toString()
        val i = this.activity!!.intent
        kdbrg = i.extras?.getString(ListBarangActivity.KDBRG).toString()
        binding.progressbar.visibility = View.GONE

        val layoutManager = GridLayoutManager(activity, 3)
        binding.rvStok.setHasFixedSize(true)
        binding.rvStok.layoutManager = layoutManager
        adapter = AdapterStok(listStok)
        binding.rvStok.adapter = adapter
        listKota = ArrayList()
        binding.tvKdbrg.text = kdbrg
        when {
            userKota.equals("ALL", ignoreCase = true) -> {
                getKota()
            }
            userKota.equals("MLG", ignoreCase = true) -> {
                kotaArray = arrayOf("MLG", "SBY")
                adapterSpinner = ArrayAdapter(Objects.requireNonNull(requireContext()), android.R.layout.simple_spinner_dropdown_item, kotaArray)
                binding.spinKota.adapter = adapterSpinner
            }
            else -> {
                kotaArray = arrayOf(userKota)
                adapterSpinner = ArrayAdapter(Objects.requireNonNull(requireContext()), android.R.layout.simple_spinner_dropdown_item, kotaArray)
                binding.spinKota.adapter = adapterSpinner
            }
        }
        binding.spinKota.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View, position: Int, id: Long) {
                kota = binding.spinKota.selectedItem.toString()
                getStok()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                kota = binding.spinKota.selectedItem.toString()
                getStok()
            }
        }

        binding.swipeRefresh.setOnRefreshListener {
            kota = binding.spinKota.selectedItem.toString()
            getStok()
        }

        binding.btnShow.setOnClickListener { dialogStokRusak() }

        return rootView
    }

    private fun getStok(){
        listStok.clear()
        binding.progressbar.visibility = View.VISIBLE
        val model = ViewModelProviders.of(this).get(StokBarangViewModel::class.java)
        model.getStok(binding.swipeRefresh, requireActivity(), kdbrg, kota, user, false).observe(this, {
            stokList: ArrayList<Stok> ->
            run {
                for (i in stokList.indices){
                    val item = Stok()
                    item.kdgd = stokList[i].kdgd
                    item.nmgd = stokList[i].nmgd
                    item.qty = stokList[i].qty
                    listStok.add(item)
                    binding.tvNmbrg.text = stokList[i].nmbrg
                }
                adapter.notifyDataSetChanged()
                binding.swipeRefresh.isRefreshing = false
            }
        })
        binding.progressbar.visibility = View.GONE
    }

    private inner class AdapterStok(private val list_tampung: List<Stok>) : RecyclerView.Adapter<AdapterStok.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.rv_fragment_stok, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            //mengambil elemen dari arraylist pada posisi yang ditentukan
            //dan memasukkannya sebagai isi dari view recyclerview
            holder.tvStok.text = nf.format(list_tampung[position].qty)
            holder.tvKdgd.text = list_tampung[position].kdgd
            holder.tvNmgd.text = list_tampung[position].nmgd
            holder.cvMain.setOnClickListener { getDataTabelKartuStok(list_tampung[position].kdgd.toString()) }
        }

        override fun getItemCount(): Int {
            //mengembalikan jumlah data yang ada pada list recyclerview
            return list_tampung.size
        }

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var tvStok: TextView = itemView.findViewById(R.id.tv_stok)
            var tvKdgd: TextView = itemView.findViewById(R.id.tv_kdgd)
            var tvNmgd: TextView = itemView.findViewById(R.id.tv_nmgd)
            var cvMain: CardView = itemView.findViewById(R.id.cv_main)
        }

    }

    private fun getStokRusak(){
        listStokRusak.clear()
        binding.progressbar.visibility = View.VISIBLE
        val model = ViewModelProviders.of(this).get(StokBarangViewModel::class.java)
        model.getStok(binding.swipeRefresh, requireActivity(), kdbrg, kota, user, true).observe(this, {
            stokList: ArrayList<Stok> ->
            run {
                for (i in stokList.indices){
                    val item = Stok()
                    item.kdgd = stokList[i].kdgd
                    item.nmgd = stokList[i].nmgd
                    item.qty = stokList[i].qty
                    listStokRusak.add(item)
                }
                adapterRusak.notifyDataSetChanged()
            }
        })
        binding.progressbar.visibility = View.GONE
    }

    private inner class AdapterStokRusak(private val list_tampung: List<Stok>) : RecyclerView.Adapter<AdapterStokRusak.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.rv_fragment_stok, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            //mengambil elemen dari arraylist pada posisi yang ditentukan
            //dan memasukkannya sebagai isi dari view recyclerview
            holder.tvStok.text = nf.format(list_tampung[position].qty)
            holder.tvKdgd.text = list_tampung[position].kdgd
            holder.tvNmgd.text = list_tampung[position].nmgd
            holder.cvMain.setOnClickListener { getDataTabelKartuStok(list_tampung[position].kdgd.toString()) }
        }

        override fun getItemCount(): Int {
            //mengembalikan jumlah data yang ada pada list recyclerview
            return list_tampung.size
        }

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var tvStok: TextView = itemView.findViewById(R.id.tv_stok)
            var tvKdgd: TextView = itemView.findViewById(R.id.tv_kdgd)
            var tvNmgd: TextView = itemView.findViewById(R.id.tv_nmgd)
            var cvMain: CardView = itemView.findViewById(R.id.cv_main)
        }

    }

    private fun getKota(){
        listKota.clear()
        binding.progressbar.visibility = View.VISIBLE
        API.instance().create(ApiService::class.java).
        kota?.enqueue(object: Callback<KotaResponse?> {
            override fun onFailure(call: Call<KotaResponse?>, e: Throwable) {
                Log.d("Get Kota Modul Stok", "Error $e")
                FirebaseCrashlytics.getInstance().recordException(e)
                binding.progressbar.visibility = View.GONE
                DialogAlert("${getString(R.string.error_pengambilan_data)} ${e.message}", "error", activity!!.parent)
            }

            override fun onResponse(call: Call<KotaResponse?>, response: Response<KotaResponse?>) {
                if(response.code() == 200) {
                    for (i in 0 until response.body()?.result!!.size){
                        listKota.add(response.body()?.result!![i].kdkota.toString())
                    }
                    populateSpinner()
                    binding.progressbar.visibility = View.GONE
                }
            }
        })
    }

    private fun populateSpinner() {
        // Creating adapter for spinner
        kotaArray = listKota.toTypedArray()
        adapterSpinner = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, kotaArray)
        binding.spinKota.adapter = adapterSpinner
    }

    private fun getDataTabelKartuStok(kdgd: String){
        println("---------------------------- get data detail")
        binding.progressbar.visibility = View.VISIBLE
        colValues.clear()
        rowValues.clear()
        cellValues.clear()
        var saldo = 0.0
        API.instance().create(ApiService::class.java)
                .getKartuStok(kdbrg, kdgd)
                ?.enqueue(object: Callback<KartuStokResponse?> {
                    override fun onFailure(call: Call<KartuStokResponse?>, e: Throwable) {
                        Log.d("Get Kartu Stok", "Error $e")
                        FirebaseCrashlytics.getInstance().recordException(e)
                        binding.progressbar.visibility = View.GONE
                        DialogAlert("${getString(R.string.error_pengambilan_data)} ${e.message}", "error", requireActivity())
                    }

                    override fun onResponse(call: Call<KartuStokResponse?>, responseBrg: Response<KartuStokResponse?>) {
                        if(responseBrg.code() == 200) {
                            var totalMasuk = 0.0; var totalKeluar = 0.0
                            for (i in 0 until responseBrg.body()?.result!!.size){
                                var sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                                val date = sdf.parse(responseBrg.body()?.result!![i].tgl.toString())!!
                                sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                                val tgl = sdf.format(date)
                                rowValues.add(RowHeaderModel(tgl))
                                addCache = mutableListOf()
                                val xIndex = i+1

                                if(responseBrg.body()?.result!![i].noBukti.toString().equals("null", true)){
                                    addCache.add(CellModel("$xIndex"+"1", "-"))
                                } else {
                                    addCache.add(CellModel("$xIndex"+"1", responseBrg.body()?.result!![i].noBukti.toString()))
                                }
                                addCache.add(CellModel("$xIndex"+"2", responseBrg.body()?.result!![i].noFaktur.toString()))
                                addCache.add(CellModel("$xIndex"+"3", responseBrg.body()?.result!![i].ket.toString()))
                                addCache.add(CellModel("$xIndex"+"4", responseBrg.body()?.result!![i].kirim.toString()))
                                addCache.add(CellModel("$xIndex"+"5", nf.format(responseBrg.body()?.result!![i].qty)))
                                addCache.add(CellModel("$xIndex"+"6", responseBrg.body()?.result!![i].satuan.toString()))
                                addCache.add(CellModel("$xIndex"+"7", nf.format(responseBrg.body()?.result!![i].masuk)))
                                addCache.add(CellModel("$xIndex"+"8", nf.format(responseBrg.body()?.result!![i].keluar)))
                                saldo += responseBrg.body()?.result!![i].qty!!
                                addCache.add(CellModel("$xIndex"+"9", nf.format(saldo)))

                                cellValues.add(addCache)
                                totalMasuk += responseBrg.body()?.result!![i].masuk!!
                                totalKeluar += responseBrg.body()?.result!![i].keluar!!
                            }
                            rowValues.add(RowHeaderModel("TOTAL"))

                            addCache = mutableListOf()
                            addCache.add(CellModel("${responseBrg.body()?.result!!.size}", ""))
                            addCache.add(CellModel("${responseBrg.body()?.result!!.size}", ""))
                            addCache.add(CellModel("${responseBrg.body()?.result!!.size}", ""))
                            addCache.add(CellModel("${responseBrg.body()?.result!!.size}", ""))
                            addCache.add(CellModel("${responseBrg.body()?.result!!.size}", ""))
                            addCache.add(CellModel("${responseBrg.body()?.result!!.size}", ""))
                            addCache.add(CellModel("${responseBrg.body()?.result!!.size}"+"7", nf.format(totalMasuk)))
                            addCache.add(CellModel("${responseBrg.body()?.result!!.size}"+"8", nf.format(totalKeluar)))
                            addCache.add(CellModel("${responseBrg.body()?.result!!.size}", ""))
                            cellValues.add(addCache)

                            colValues.add(ColumnHeaderModel("No Bukti"))
                            colValues.add(ColumnHeaderModel("No Faktur"))
                            colValues.add(ColumnHeaderModel("Keterangan"))
                            colValues.add(ColumnHeaderModel("Kirim"))
                            colValues.add(ColumnHeaderModel("Qty"))
                            colValues.add(ColumnHeaderModel("Satuan"))
                            colValues.add(ColumnHeaderModel("Masuk"))
                            colValues.add(ColumnHeaderModel("Keluar"))
                            colValues.add(ColumnHeaderModel("Saldo"))

                            if (!isViewDialogKartuStok){ dialogTableKartuStok(kdgd) }

                        }
                    }
                })
    }

    @SuppressLint("SetTextI18n", "InflateParams")
    private fun dialogTableKartuStok(kdgd: String){
        println("---------------------------- show dialog kartu stok")
        isViewDialogKartuStok = true
        val adapterTable = AdapterTableView(requireActivity())
        val dialog = BottomSheetDialog(requireActivity(), R.style.SheetDialog)
        val bottomSheetLayout = layoutInflater.inflate(R.layout.dialog_evaluasi_loss_cust_table, null)
        dialog.setContentView(bottomSheetLayout)
        val behavior = BottomSheetBehavior.from(dialog.findViewById(R.id.bottomSheetEvaluasiLossCustTable)!!)
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
        dialog.setCancelable(false)
        binding.progressbar.visibility = View.GONE
        dialog.show()

        val tvKdBrg = dialog.findViewById<TextView>(R.id.tv_kdbrg)
        val tvTgl = dialog.findViewById<TextView>(R.id.tv_tgl)
        val tblView = dialog.findViewById<TableView>(R.id.container_table)
        val imgClose = dialog.findViewById<ImageView>(R.id.img_close)
        tvKdBrg?.text = kdbrg
        tvTgl?.text = kdgd
        tblView?.adapter = adapterTable
        adapterTable.setAllItems(colValues, rowValues, cellValues)

        dialog.setOnDismissListener {
            isViewDialogKartuStok = false
            println("---------------------------- dialog kartu stok dismiss")
        }
        imgClose?.setOnClickListener { dialog.dismiss() }
    }

    private fun dialogStokRusak(){
        isViewDialogStokRusak = true
        val dialog = BottomSheetDialog(requireActivity(), R.style.SheetDialog)
        val bindingDialogStokRusak = DialogStokRusakSelisihBinding.inflate(layoutInflater)
        val bottomSheetLayout = bindingDialogStokRusak.root
        val slideUp = AnimationUtils.loadAnimation(requireContext(), R.anim.show_from_bottom)
        dialog.setContentView(bottomSheetLayout)

        val layoutManager = GridLayoutManager(activity, 3)
        bindingDialogStokRusak.rvStok.setHasFixedSize(true)
        bindingDialogStokRusak.rvStok.layoutManager = layoutManager
        adapterRusak = AdapterStokRusak(listStokRusak)
        bindingDialogStokRusak.rvStok.adapter = adapterRusak

        getStokRusak()

        dialog.setOnDismissListener {
            isViewDialogKartuStok = false
            println("---------------------------- dialog kartu stok dismiss")
        }

        bottomSheetLayout.startAnimation(slideUp)
        dialog.show()
    }
}
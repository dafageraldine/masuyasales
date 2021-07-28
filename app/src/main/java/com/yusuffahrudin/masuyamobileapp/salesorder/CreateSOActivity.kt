package com.yusuffahrudin.masuyamobileapp.salesorder

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.tabs.TabLayout
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.yusuffahrudin.masuyamobileapp.R
import com.yusuffahrudin.masuyamobileapp.api.API
import com.yusuffahrudin.masuyamobileapp.api.APIError
import com.yusuffahrudin.masuyamobileapp.api.ApiService
import com.yusuffahrudin.masuyamobileapp.api.ErrorUtils
import com.yusuffahrudin.masuyamobileapp.controller.DialogAlert
import com.yusuffahrudin.masuyamobileapp.data.ArrayTampung
import com.yusuffahrudin.masuyamobileapp.data.Result
import com.yusuffahrudin.masuyamobileapp.data.sales_order.ItemJual
import com.yusuffahrudin.masuyamobileapp.data.sales_order.ItemOrder
import com.yusuffahrudin.masuyamobileapp.data.sales_order.SalesOrder
import com.yusuffahrudin.masuyamobileapp.data.sales_order.SalesOrderResponse
import com.yusuffahrudin.masuyamobileapp.databinding.*
import com.yusuffahrudin.masuyamobileapp.firebase.SendNotification
import com.yusuffahrudin.masuyamobileapp.updatepricelist.CreatePriceCust
import com.yusuffahrudin.masuyamobileapp.util.Server
import com.yusuffahrudin.masuyamobileapp.util.SessionManager
import org.jetbrains.anko.doAsync
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class CreateSOActivity: AppCompatActivity() {
    private lateinit var listHeader: ArrayList<SalesOrder>
    private lateinit var listCart: ArrayList<ItemOrder>
    private lateinit var listItem: ArrayList<ItemJual>
    private val listAkses = ArrayTampung.getListAkses()
    private var isadd = false
    private lateinit var kdcust: String
    private lateinit var level: String
    private lateinit var kdgd: String
    private lateinit var statusPajak: String
    private lateinit var kdkota: String
    private lateinit var adapter: AdapterBarang
    private val nf: NumberFormat = NumberFormat.getInstance(Locale("in", "ID"))
    private lateinit var kdbrg: String
    private lateinit var nmbrg: String
    private var stok: Double = 0.0
    private lateinit var satuan: String
    private lateinit var satuan3: String
    private var qtykvs3: Double = 0.0
    private var qtyout: Double = 0.0
    private var harga: Double = 0.0
    private var hrgJualMin: Double = 0.0
    private var hrgPokok: Double = 0.0
    private var diskon1: Double = 0.0
    private var diskon2: Double = 0.0
    private var diskon3: Double = 0.0
    private var m3: Double = 0.0
    private lateinit var satuanArray: MutableList<String>
    private lateinit var sessionManager: SessionManager
    private lateinit var name: String
    private lateinit var status: StringBuilder
    private var otoCust = false
    private var otoHarga = false
    private var otoHPP = false
    private var proses = false
    private var a = 0
    private var isSearch = false
    private lateinit var paramItem: JSONArray
    private lateinit var binding: ActivityDetailSoBinding
    private lateinit var bindingDialogPilihBarang: DialogPilihBarangBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        println("======createSO=======")
        binding = ActivityDetailSoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        this.title = getString(R.string.create_sales_order)

        sessionManager = SessionManager(applicationContext)
        val user = sessionManager.userDetails
        kdkota = user[SessionManager.kdkota].toString()
        name = user[SessionManager.kunci_email].toString()
        level = user[SessionManager.level].toString()

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.progressbar.progressTintList = ColorStateList.valueOf(Color.RED)
        binding.progressbar.visibility  = View.GONE
        binding.tvProgressbar.visibility   = View.GONE

        listHeader = ArrayTampung.getListHeaderSO()
        listCart = ArrayTampung.getListChart()
        listHeader.clear()
        listCart.clear()

        // Set up the ViewPager with adapter.
        binding.containerSo.offscreenPageLimit = 1
        setupViewPager(binding.containerSo)

        binding.tabLayout.setupWithViewPager(binding.containerSo)
        binding.tabLayout.getTabAt(0)?.setIcon(R.drawable.ic_sales_order_white_100)
        binding.tabLayout.getTabAt(1)?.setIcon(R.drawable.ic_shopping_cart_64)
        //binding.tabLayout.getTabAt(2)?.setIcon(R.drawable.ic_signature_white_100)
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.ViewPagerOnTabSelectedListener(binding.containerSo) {
            override fun onTabSelected(tab: TabLayout.Tab) {
                if (tab.position == 1) {
                    if (listCart.size == 0) {
                        dialogPilihBarang()
                    }
                }
            }
        })

        binding.btnSimpan.setOnClickListener {
            try {
                cekDouble()
            } catch (e: Exception) {
                FirebaseCrashlytics.getInstance().recordException(e)
                DialogAlert(e.message.toString(), "error", this@CreateSOActivity)
            }
        }
    }

    override fun onBackPressed() {
        if (listHeader.size > 0){
            listHeader.clear()
        }
        if (listCart.size > 0){
            listCart.clear()
        }
        finish()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_so_detail, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == android.R.id.home) {
            if (listHeader.size > 0){
                listHeader.clear()
            }
            if (listCart.size > 0){
                listCart.clear()
            }
            finish()
        } else if (id == R.id.action_add_item){
            dialogPilihBarang()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setupViewPager(viewPager: ViewPager) {
        val adapter = ViewPagerAdapter(supportFragmentManager)
        adapter.addFragment(CreateHeaderFragment(), "Header")
        adapter.addFragment(CreateItemFragment(), "Item")
        //adapter.addFragment(DrawSignatureFragment(), "Signature")
        viewPager.adapter = adapter
    }

    inner class ViewPagerAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
        private val mFragmentList = ArrayList<Fragment>()
        private val mFragmentTitleList = ArrayList<String>()

        override fun getItem(position: Int): Fragment {
            return mFragmentList[position]
        }

        override fun getCount(): Int {
            return mFragmentList.size
        }

        fun addFragment(fragment: Fragment, title: String) {
            mFragmentList.add(fragment)
            mFragmentTitleList.add(title)
        }

        override fun getPageTitle(position: Int): CharSequence {
            return mFragmentTitleList[position]
        }
    }

    private fun cekParameter(): Boolean{
        val fragmentHeader = binding.containerSo.adapter?.instantiateItem(binding.containerSo, 0) as CreateHeaderFragment
        val hasil: Boolean
        fragmentHeader.getHeader()
        if (listHeader.size == 0){
            DialogAlert("Header tidak ada data, isi dulu..", "error", this@CreateSOActivity)
            hasil = false
        } else {
            hasil = when {
                listHeader[0].kdcust == "" -> {
                    DialogAlert("Field customer masih kosong, diisi dulu..", "error", this@CreateSOActivity)
                    false
                }
                listHeader[0].kdgd.equals("gudang", true) -> {
                    DialogAlert("Field gudang belum dipilih, lebih teliti lah..", "error", this@CreateSOActivity)
                    false
                }
                else -> true
            }
        }
        return hasil
    }

    @SuppressLint("InflateParams", "SetTextI18n")
    private fun dialogPilihBarang(){
        val dialogPilihBrg = BottomSheetDialog(this, R.style.SheetDialogNotif)
        bindingDialogPilihBarang = DialogPilihBarangBinding.inflate(layoutInflater)
        val bottomSheetLayout = bindingDialogPilihBarang.root
        dialogPilihBrg.setContentView(bottomSheetLayout)
        dialogPilihBrg.setCancelable(false)
        listItem = ArrayList()
        if(cekParameter()){
            kdcust = listHeader[0].kdcust.toString()
            println("===================== ====================== alm1 ${listHeader[0].alm1}")
            println("===================== ====================== alm2 ${listHeader[0].alm2}")
            println("===================== ====================== alm3 ${listHeader[0].alm3}")
            statusPajak = listHeader[0].kodeTax.toString()
            kdgd = listHeader[0].kdgd.toString()
            dialogPilihBrg.setTitle("Barang " + listHeader[0].jnsjualtax + " (" + listCart.size + ")")

            bindingDialogPilihBarang.tvTitle.text = "Barang ${listHeader[0].jnsjualtax} (${listCart.size})"
            bindingDialogPilihBarang.progressbar.visibility = View.GONE
            bindingDialogPilihBarang.searchview.isActivated = true
            bindingDialogPilihBarang.searchview.queryHint = "Cari Barang"
            bindingDialogPilihBarang.searchview.onActionViewExpanded()
            bindingDialogPilihBarang.searchview.clearFocus()

            val layoutManager = GridLayoutManager(this, 2)
            bindingDialogPilihBarang.listBarang.setHasFixedSize(true)
            bindingDialogPilihBarang.listBarang.layoutManager = layoutManager
            adapter = AdapterBarang(this, listItem)
            bindingDialogPilihBarang.listBarang.adapter = adapter

            if(listItem.isEmpty()){ getData(0) }

            bindingDialogPilihBarang.tvAddPrice.setOnClickListener {
                cekAkses()
                if (isadd) {
                    val intent = Intent(this@CreateSOActivity, CreatePriceCust::class.java)
                    intent.putExtra("kdcust", kdcust)
                    intent.putExtra("kdbrg", "")
                    intent.putExtra("satuan", "")
                    intent.putExtra("jnsppn", "00")
                    intent.putExtra("hrg", "0")
                    intent.putExtra("hrgincppn", "0")
                    intent.putExtra("diskon1", "0")
                    intent.putExtra("diskon2", "0")
                    intent.putExtra("diskon3", "0")
                    startActivity(intent)
                } else {
                    DialogAlert("anda tidak mempunyai hak akses", "error", this@CreateSOActivity)
                }
            }

            bindingDialogPilihBarang.listBarang.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val visibleItemCount = layoutManager.childCount
                    val totalItemCount = layoutManager.itemCount
                    val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                    if (firstVisibleItemPosition + visibleItemCount >= totalItemCount) {
                        if (a == 0) {
                            if (!isSearch) {
                                getData(totalItemCount)
                                a = 1
                            }
                        }
                    } else {
                        a = 0
                    }
                }
            })

            bindingDialogPilihBarang.swipeRefresh.setOnRefreshListener {
                listItem.clear()
                getData(0)
                //UpdateCart()
            }

            bindingDialogPilihBarang.searchview.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String): Boolean {
                    searchData(query)
                    return true
                }

                override fun onQueryTextChange(newText: String): Boolean {
                    return false
                }
            })

            bindingDialogPilihBarang.btnOk.setOnClickListener {
                val fragment = binding.containerSo.adapter?.instantiateItem(binding.containerSo, 1) as CreateItemFragment
                fragment.lvBrg.adapter?.notifyDataSetChanged()
                dialogPilihBrg.dismiss()
            }

            bindingDialogPilihBarang.btnCancel.setOnClickListener { dialogPilihBrg.dismiss() }

            dialogPilihBrg.show()
        }
    }

    private fun getData(itemCount: Int) {
        bindingDialogPilihBarang.progressbar.visibility = View.VISIBLE

        val model = ViewModelProviders.of(this).get(CreateSOViewModel::class.java)
        model.getItem(this, kdcust, kdgd, statusPajak, itemCount).observe(this, { itemList: ArrayList<ItemJual> ->
            run {
                isSearch = false
                listItem.addAll(itemList)
                a++
                adapter.notifyDataSetChanged()
                bindingDialogPilihBarang.progressbar.visibility = View.GONE
                bindingDialogPilihBarang.swipeRefresh.isRefreshing = false
            }
        })
    }

    private fun searchData(brg: String) {
        bindingDialogPilihBarang.progressbar.visibility = View.VISIBLE
        listItem.clear()

        val model = ViewModelProviders.of(this).get(CreateSOViewModel::class.java)
        model.findItem(this, kdcust, kdgd, statusPajak, brg).observe(this, { itemList: ArrayList<ItemJual> ->
            run {
                isSearch = true
                listItem.addAll(itemList)
                a++
                adapter.notifyDataSetChanged()
                bindingDialogPilihBarang.progressbar.visibility = View.GONE
                bindingDialogPilihBarang.swipeRefresh.isRefreshing = false
            }
        })
    }

    private inner class AdapterBarang(private val activity: Activity, private var list_tampung: ArrayList<ItemJual>) : RecyclerView.Adapter<AdapterBarang.ViewHolder>(){

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = RvActivityPilihbarangBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val a = Server("")
            Glide.with(applicationContext)
                    .load(a.URL_IMAGE() + list_tampung[position].kdBrg + ".jpg")
                    .apply(RequestOptions()
                            .placeholder(R.mipmap.ic_launcher)
                            .error(R.drawable.img_not_found)
                            .override(300, 400)
                            .fitCenter())
                    .into(holder.binding.imageView)
            holder.binding.tvStok.text = nf.format(list_tampung[position].qty ?: 0.0)
            holder.binding.tvStokout.text = nf.format(list_tampung[position].qtyOut ?: 0.0)
            holder.binding.tvNmbrg.text = list_tampung[position].nmBrg
            if(listHeader[0].jnsjualtax.equals("PPN", ignoreCase = false)){
                holder.binding.tvHarga.text = nf.format(list_tampung[position].harga ?: 0.0 * 1.1)
            } else {
                holder.binding.tvHarga.text = nf.format(list_tampung[position].harga ?: 0.0)
            }

            holder.binding.btnAddToCart.setOnClickListener {
                kdbrg = list_tampung[position].kdBrg.toString()
                nmbrg = list_tampung[position].nmBrg.toString()
                stok = list_tampung[position].qty?:0.0
                satuan = list_tampung[position].satuan.toString()
                satuan3 = list_tampung[position].satuan3.toString()
                qtykvs3 = list_tampung[position].qtyKvs3?:0.0
                qtyout = list_tampung[position].qtyOut?:0.0
                harga = list_tampung[position].harga?:0.0
                diskon1 = list_tampung[position].diskon1?:0.0
                diskon2 = list_tampung[position].diskon2?:0.0
                diskon3 = list_tampung[position].diskon3?:0.0
                m3 = list_tampung[position].mKubik1?:0.0
                hrgJualMin = list_tampung[position].hrgJualMin?:0.0
                hrgPokok = list_tampung[position].hrgPokok?:0.0

                val listSatuan = ArrayList<String>()
                listSatuan.add(satuan)
                if(satuan.equals("KG", true)){
                    listSatuan.add("LOAF")
                    listSatuan.add("EKOR")
                }
                satuanArray = listSatuan
                dialogQty()
            }
        }

        override fun getItemCount(): Int {
            return list_tampung.size
        }

        inner class ViewHolder(val binding:RvActivityPilihbarangBinding) : RecyclerView.ViewHolder(binding.root)

        @SuppressLint("InflateParams", "SetTextI18n")
        private fun dialogQty() {
            val dialogQty = BottomSheetDialog(this.activity, R.style.SheetDialog)
            val bindingDialogQty = DialogSoQtyItemBinding.inflate(layoutInflater)
            val dialogView = bindingDialogQty.root
            dialogQty.setContentView(dialogView)

            bindingDialogQty.edtQty.setText("1")
            val a = Server("")
            Glide.with(applicationContext)
                    .load(a.URL_IMAGE() + kdbrg + ".jpg")
                    .apply(RequestOptions()
                            .placeholder(R.mipmap.ic_launcher)
                            .error(R.drawable.img_not_found)
                            .override(150, 150)
                            .fitCenter())
                    .into(bindingDialogQty.imageView)
            val adapterSatuan = ArrayAdapter(activity, R.layout.spinner_black, satuanArray)
            bindingDialogQty.spinSatuan.adapter = adapterSatuan

            bindingDialogQty.tvHarga.text = nf.format(harga)
            bindingDialogQty.tvDiskon1.setText(nf.format(diskon1))
            bindingDialogQty.tvDiskon2.text = nf.format(diskon2)
            bindingDialogQty.tvDiskon3.text = nf.format(diskon3)
/*
            spinSatuan.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                    if (id > 0) {
                        tvHarga.text = "0"
                    } else {
                        tvHarga.text = nf.format(java.lang.Float.parseFloat(harga).toDouble())
                        tvDiskon1.text = nf.format(java.lang.Double.parseDouble(diskon1))
                        tvDiskon2.text = nf.format(java.lang.Double.parseDouble(diskon2))
                        tvDiskon3.text = nf.format(java.lang.Double.parseDouble(diskon3))
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>) {

                }
            }
*/
            bindingDialogQty.btnMinus.setOnClickListener {
                if(bindingDialogQty.edtQty.text.toString().isEmpty()){
                    DialogAlert("Qty tidak boleh kosong!", "attention", this.activity)
                } else {
                    val qty = ubahAngka(bindingDialogQty.edtQty.text.toString()).toDouble()
                    if (qty > 0) {
                        bindingDialogQty.edtQty.setText(nf.format(qty - 1))
                    }
                }
            }

            bindingDialogQty.btnPlus.setOnClickListener {
                if(bindingDialogQty.edtQty.text.toString().isEmpty()){
                    DialogAlert("Qty tidak boleh kosong!", "attention", this.activity)
                } else {
                    val qty = ubahAngka(bindingDialogQty.edtQty.text.toString()).toDouble()
                    bindingDialogQty.edtQty.setText(nf.format(qty + 1))
                }
            }

            bindingDialogQty.btnOk.setOnClickListener {
                val qty = ubahAngka(bindingDialogQty.edtQty.text.toString()).toDouble()
                diskon1 = ubahAngka(bindingDialogQty.tvDiskon1.text.toString()).toDouble()
                diskon2 = ubahAngka(bindingDialogQty.tvDiskon2.text.toString()).toDouble()
                diskon3 = ubahAngka(bindingDialogQty.tvDiskon3.text.toString()).toDouble()
                /*if (qty > stok || stok == 0.0) {
                    DialogAlert("Stok tidak mencukupi..!", "attention", this.activity)
                } else if (harga == 0.0) {
                    DialogAlert("Harga Barang Rp0 \nInput harga barang di modul Update Pricelist", "attention", this.activity)
                } else {
                    val hrg: Double = harga * (1 - diskon1 / 100) * (1 - diskon2 / 100) * (1 - diskon3 / 100)
                    val subtotal = hrg * qty
                    val item = ItemOrder(kdbrg, nmbrg, qty, qtyout, qtykvs3, java.lang.Double.valueOf(harga),
                            java.lang.Double.valueOf(hrgPokok), java.lang.Double.valueOf(hrgJualMin),
                            java.lang.Double.valueOf(diskon1), java.lang.Double.valueOf(diskon2),
                            java.lang.Double.valueOf(diskon3), bindingDialogQty.spinSatuan.selectedItem.toString(), satuan3,
                            m3, subtotal)

                    listCart.add(item)
                    bindingDialogPilihBarang.tvTitle.text = "Barang ${listHeader[0].jnsjualtax} (${listCart.size})"
                    dialogQty.dismiss()
                }*/
                val hrg: Double = ((harga * (1 - (diskon1/100))) * (1 - (diskon2/100))) * (1 - (diskon3/100))
                val subtotal = hrg * qty
                val item = ItemOrder(kdbrg, nmbrg, qty, qtyout, qtykvs3, harga, hrgPokok, hrgJualMin,
                        diskon1, diskon2, diskon3, bindingDialogQty.spinSatuan.selectedItem.toString(),
                        satuan3, m3, subtotal)

                listCart.add(item)
                bindingDialogPilihBarang.tvTitle.text = "Barang ${listHeader[0].jnsjualtax} (${listCart.size})"
                dialogQty.dismiss()
            }

            bindingDialogQty.btnCancel.setOnClickListener { dialogQty.dismiss() }

            dialogQty.show()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun saveNewOrder(base64: String?) {
        binding.progressbar.visibility  = View.VISIBLE
        binding.tvProgressbar.visibility   = View.VISIBLE
        binding.tvProgressbar.text = "Proses save sales order..."
        doAsync {
                println("===========orderby and list item================")
                println(listHeader[0].orderby)
            println(paramItem.toString())
                println("====================================")
//            var flag = 0
//            for (i in listCart.indices) {
//                println(listCart[i].kdBrg)
//                val disc1 = listCart[i].diskon1!!.toDouble()
//                val disc2 = listCart[i].diskon2!!.toDouble()
//                val disc3 = listCart[i].diskon3!!.toDouble()
//                val hrg: Double = ((listCart[i].harga!! * (1 - (disc1/100))) * (1 - (disc2/100))) * (1 - (disc3/100))
//                if (hrg < listCart[i].hrgJualMin!!) {
//                    flag = 1
//                }
//                if (hrg < listCart[i].hrgPokok!!) {
//                    flag = 1
//                }
//                if (listCart[i].satuan.equals("EKOR", false) || listCart[i].satuan.equals("LOAF", false)) {
//                    flag = 1
//                }
//                if (flag == 1){
//                    println("==================SO Berikut berada pada baris pertama==============")
//                    println(listCart[i].kdBrg)
//                    println("================pengurutan SO========================")
//                    flag = 0
//                    listCart.add(0, listCart[i])
//                    listCart.removeAt(i+1)
//                }
//            }

            for (i in listCart.indices) {
                println(listCart[i].kdBrg)
                val disc1 = listCart[i].diskon1!!.toDouble()
                val disc2 = listCart[i].diskon2!!.toDouble()
                val disc3 = listCart[i].diskon3!!.toDouble()
                val hrg: Double = ((listCart[i].harga!! * (1 - (disc1/100))) * (1 - (disc2/100))) * (1 - (disc3/100))
                if (hrg < listCart[i].hrgJualMin!!) {
                    status.append(", ${listCart[i].kdBrg} dibawah harga jual min.")
                    otoHarga = true
                }
                if (hrg < listCart[i].hrgPokok!!) {
                    status.append(", ${listCart[i].kdBrg} dibawah harga pokok")
                    otoHPP = true
                }
                if (listCart[i].satuan.equals("EKOR", false) || listCart[i].satuan.equals("LOAF", false)) {
                    proses = true
                }
            }

            val params = HashMap<String?, String?>()

            //JSONArray List Item Order
            params["no_order"] = listHeader[0].nobukti
            params["kdcust"] = listHeader[0].kdcust
            params["nmcust"] = listHeader[0].nmcust.toString().replace("'", "''")
            params["kdkel"] = listHeader[0].kdkel
            params["alm1"] = listHeader[0].alm1.toString().replace("'", "''")
            params["alm2"] = listHeader[0].alm2.toString().replace("'", "''")
            params["alm3"] = listHeader[0].alm3.toString().replace("'", "''")
            params["kdsales"] = listHeader[0].kdsales
            params["pajak"] = listHeader[0].jnsjualtax
            params["tgl_create"] = listHeader[0].tglcreate
            println("------------------- tgl create ${listHeader[0].tglcreate}")
            params["tgl_kirim"] = listHeader[0].tglkirim
            params["tglPO"] = listHeader[0].tglPO
            params["cetak_note"] = listHeader[0].ket1
            params["kodePO"] = listHeader[0].noPO
            params["keterangan"] = listHeader[0].ket2
            params["kdgd"] = listHeader[0].kdgd
            params["createby"] = listHeader[0].createby
            params["orderby"] = listHeader[0].orderby
            params["subtotal"] = listHeader[0].subtotal?.toBigDecimal()?.toPlainString()
            params["discfak_persen"] = listHeader[0].disc?.toBigDecimal()?.toPlainString()
            params["discfak_total"] = listHeader[0].jmldisc1?.toBigDecimal()?.toPlainString()
            params["ppn_persen"] = listHeader[0].prsppn?.toBigDecimal()?.toPlainString()
            params["ppn_total"] = listHeader[0].ppn?.toBigDecimal()?.toPlainString()
            params["total"] = listHeader[0].total?.toBigDecimal()?.toPlainString()
            params["imgPO"] = listHeader[0].imgPO
            println("----------")
            println("----------")
            println("----------")
            println("----------")
            println("----------")
            println("imgPO ${listHeader[0].imgPO}")
            println("----------")
            println("----------")
            println("----------")
            println("----------")
            println("----------")
            params["status"] = "$status"
            if (base64 == null) {
                params["signature"] = ""
            } else {
                params["signature"] = base64
            }
            params["listItem"] = paramItem.toString()
            var totalM3 = 0.0
            for (i in listCart.indices) {
                totalM3 += listCart[i].qtyOrder!! * listCart[i].mKubik1!!
            }
            println("*******************    totalM3  = ${totalM3.toBigDecimal().toPlainString()}")
            params["totalM3"] = totalM3.toBigDecimal().toPlainString()

            val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val tglPO = format.parse(listHeader[0].tglPO.toString())
            val tglSO = format.parse(listHeader[0].tglkirim.toString())
            if (listHeader[0].jnsjualtax.equals("01") && listHeader[0].disc != 10.0){
                binding.progressbar.visibility = View.GONE
                binding.tvProgressbar.visibility = View.GONE
                window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                DialogAlert("SO ber-Ppn, nilai Ppn harus 10%", "attention", this@CreateSOActivity)
            } else if (!listHeader[0].jnsjualtax.equals("01") && listHeader[0].disc != 0.0){
                binding.progressbar.visibility = View.GONE
                binding.tvProgressbar.visibility = View.GONE
                window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                DialogAlert("SO tidak ber-Ppn, nilai Ppn harus 0", "attention", this@CreateSOActivity)
            } else if (tglPO.after(tglSO)) {
                binding.progressbar.visibility = View.GONE
                binding.tvProgressbar.visibility = View.GONE
                window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                DialogAlert("Tanggal PO tidak boleh lebih dari tanggal SO", "attention", this@CreateSOActivity)
            } else {
                API.instance().create(ApiService::class.java)
                        .saveSO(params)
                        ?.enqueue(object : Callback<SalesOrderResponse?> {
                            override fun onResponse(call: Call<SalesOrderResponse?>, response: retrofit2.Response<SalesOrderResponse?>) {
                                if (response.isSuccessful) {
                                    val notif = SendNotification()
                                    listHeader[0].nobukti = response.body()?.result?.get(0)?.nobukti.toString()
                                    for (i in listCart.indices) {
                                        val disc1 = listCart[i].diskon1!!.toDouble()
                                        val disc2 = listCart[i].diskon2!!.toDouble()
                                        val disc3 = listCart[i].diskon3!!.toDouble()
                                        val hrg: Double = ((listCart[i].harga!! * (1 - (disc1/100))) * (1 - (disc2/100))) * (1 - (disc3/100))
                                        if (hrg < listCart[i].hrgPokok!!) {
                                            val messageSP = cekSpecialPrice(listCart[i].kdBrg.toString(), listCart[i].qtyOrder!!.toDouble(), "hrgPokok",
                                                    listHeader[0].nobukti.toString(), listCart[i].harga!!.toDouble(), disc1, disc2, disc3)
                                            if (messageSP.equals("Special Price", true)) {
                                                otoHPP = false
                                            }
                                        } else if (hrg < listCart[i].hrgJualMin!!) {
                                            val messageSP = cekSpecialPrice(listCart[i].kdBrg.toString(), listCart[i].qtyOrder!!.toDouble(), "hrgJualMin",
                                                    listHeader[0].nobukti.toString(), listCart[i].harga!!.toDouble(), disc1, disc2, disc3)
                                            if (messageSP.equals("Special Price", true)) {
                                                otoHarga = false
                                            }
                                        }
                                    }

                                    var levelSM = ""
                                    var levelSpv = ""
                                    var levelBM = ""
                                    when (level.length) {
                                        6 -> {
                                            levelSM = level
                                            levelSpv = level + "10"
                                            levelBM = level.substring(0, level.length - 2)
                                        }
                                        8 -> {
                                            levelSM = level.substring(0, level.length - 2)
                                            levelSpv = level
                                            levelBM = level.substring(0, level.length - 4)
                                        }
                                        10 -> {
                                            levelSM = level.substring(0, level.length - 4)
                                            levelSpv = level.substring(0, level.length - 2)
                                            levelBM = level.substring(0, level.length - 6)
                                        }
                                    }
                                    val levelSpvAR = "10101020"
                                    val levelStaffAR = levelSpvAR + "10"
                                    val levelWare = "1010501010"
                                    val levelAdminWare = levelWare.substring(0, levelWare.length - 2)
                                    val levelKepalaWare = levelWare.substring(0, levelWare.length - 4)
                                    if (otoCust && otoHarga) {
                                        println("================ cust & harga")
                                        notif.pushNotif(this@CreateSOActivity, kdkota,
                                                "Ada SO yang perlu diotorisasi!",
                                                "${listHeader[0].nobukti} (${listHeader[0].nmsales}) - ${listHeader[0].nmcust}",
                                                "SO",
                                                "'$levelSpvAR','$levelStaffAR','$levelSM','$levelSpv'")
                                    } else if (!otoCust && otoHarga) {
                                        println("================ harga")
                                        notif.pushNotif(this@CreateSOActivity, kdkota,
                                                "Ada SO yang perlu diotorisasi!",
                                                "${listHeader[0].nobukti} (${listHeader[0].nmsales}) - ${listHeader[0].nmcust}",
                                                "SO",
                                                "'$levelSM','$levelSpv'")
                                    } else if (otoCust && !otoHarga) {
                                        println("================ cust")
                                        notif.pushNotif(this@CreateSOActivity, kdkota,
                                                "Ada SO yang perlu diotorisasi!",
                                                "${listHeader[0].nobukti} (${listHeader[0].nmsales}) - ${listHeader[0].nmcust}",
                                                "SO",
                                                "'$levelSpvAR','$levelStaffAR','$levelSM','$levelSpv'")
                                    }
                                    if (otoHPP) {
                                        println("================ hpp")
                                        notif.pushNotif(this@CreateSOActivity, kdkota,
                                                "Ada SO yang perlu diotorisasi!",
                                                "${listHeader[0].nobukti} (${listHeader[0].nmsales}) - ${listHeader[0].nmcust}",
                                                "SO",
                                                "'$levelBM'")
                                    }
                                    if (proses) {
                                        notif.pushNotif(this@CreateSOActivity, kdkota,
                                                "Ada SO yang perlu diproses timbang!",
                                                "${listHeader[0].nobukti} (${listHeader[0].nmsales}) - ${listHeader[0].nmcust}",
                                                "Timbangan",
                                                "'$levelKepalaWare','$levelAdminWare','$levelWare'")
                                    }
                                    binding.progressbar.visibility = View.GONE
                                    binding.tvProgressbar.visibility = View.GONE
                                    listHeader.clear()
                                    listCart.clear()
                                    listItem.clear()
                                    window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                                    DialogAlert("Sales Order berhasil disimpan!", "success-reply", this@CreateSOActivity)
                                } else {
                                    val apiError: APIError? = ErrorUtils.parseError(response)
                                    binding.progressbar.visibility = View.GONE
                                    binding.tvProgressbar.visibility = View.GONE
                                    window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                                    Log.d("-- Save New Sales Order", "Error ${apiError?.message()}")
                                    DialogAlert(apiError?.message(), "error", this@CreateSOActivity)
                                }
                            }

                            override fun onFailure(call: Call<SalesOrderResponse?>, t: Throwable) {
                                binding.progressbar.visibility = View.GONE
                                binding.tvProgressbar.visibility = View.GONE
                                window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                                Log.d("-- Save New Sales Order", "Error $t")
                                FirebaseCrashlytics.getInstance().recordException(t)
                                DialogAlert(t.message, "error", this@CreateSOActivity)
                            }
                        })
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun cekCustomer(){
        binding.progressbar.visibility  = View.VISIBLE
        binding.tvProgressbar.visibility   = View.VISIBLE
        binding.tvProgressbar.text = "Pengecekan customer..."
        doAsync {
            API.instance().create(ApiService::class.java)
                    .cekCustomerSO(listHeader[0].kdcust, listHeader[0].kdkel)
                    ?.enqueue(object : Callback<String?> {

                        override fun onResponse(call: Call<String?>, response: retrofit2.Response<String?>) {
                            if (response.isSuccessful) {
                                val message = response.body().toString()
                                binding.tvProgressbar.visibility = View.GONE
                                if (message.equals("Customer has been blacklisted!", true)) {
                                    binding.progressbar.visibility = View.GONE
                                    window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                                    DialogAlert(message, "error", this@CreateSOActivity)
                                } else {
                                    status = StringBuilder("")
                                    status.append(message)
                                    if (!message.equals("Verified", true)) {
                                        otoCust = true
                                    }
                                    val fragmentHeader = binding.containerSo.adapter?.instantiateItem(binding.containerSo, 0) as CreateHeaderFragment
                                    fragmentHeader.getHeader()
                                    listHeader[0].createby = name
                                    val fragmentItem = binding.containerSo.adapter?.instantiateItem(binding.containerSo, 1) as CreateItemFragment
                                    fragmentItem.getTotal()
                                    //val fragmentSignature = binding.containerSo.adapter?.instantiateItem(binding.containerSo, 2) as DrawSignatureFragment
                                    val fragmentSignature = ""
                                    saveNewOrder(fragmentSignature)
                                }
                            } else {
                                val apiError: APIError? = ErrorUtils.parseError(response)
                                binding.progressbar.visibility = View.GONE
                                binding.tvProgressbar.visibility = View.GONE
                                window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                                Log.d("-------- Cek Customer", "Error ${apiError?.message()}")
                                DialogAlert(apiError?.message(), "error", this@CreateSOActivity)
                            }
                        }

                        override fun onFailure(call: Call<String?>, e: Throwable) {
                            binding.progressbar.visibility = View.GONE
                            binding.tvProgressbar.visibility = View.GONE
                            window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                            DialogAlert(e.message, "error", this@CreateSOActivity)
                            Log.d("-------- Cek Customer", "Error $e")
                            FirebaseCrashlytics.getInstance().recordException(e)
                        }
                    })
        }
    }

    @SuppressLint("SetTextI18n")
    private fun cekDouble(){
        binding.progressbar.visibility  = View.VISIBLE
        binding.tvProgressbar.visibility   = View.VISIBLE
        binding.tvProgressbar.text = "Sedang melakukan pengecekan double input..."
        window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        doAsync {
            val params = HashMap<String?, String?>()
            params["noPo"] = listHeader[0].noPO
            params["kdcust"] = listHeader[0].kdcust

            //JSONArray List Item Order
            paramItem = JSONArray()
            var arrayItem: JSONObject?
            try {
                var flag = 0
            for (i in listCart.indices) {
                println(listCart[i].kdBrg)
                val disc1 = listCart[i].diskon1!!.toDouble()
                val disc2 = listCart[i].diskon2!!.toDouble()
                val disc3 = listCart[i].diskon3!!.toDouble()
                val hrg: Double = ((listCart[i].harga!! * (1 - (disc1/100))) * (1 - (disc2/100))) * (1 - (disc3/100))
                if (hrg < listCart[i].hrgJualMin!!) {
                    flag = 1
                }
                if (hrg < listCart[i].hrgPokok!!) {
                    flag = 1
                }
                if (listCart[i].satuan.equals("EKOR", false) || listCart[i].satuan.equals("LOAF", false)) {
                    flag = 1
                }
                if (flag == 1){
                    println("==================SO Berikut berada pada baris pertama==============")
                    println(listCart[i].kdBrg)
                    println("================pengurutan SO========================")
                    flag = 0
                    listCart.add(0, listCart[i])
                    listCart.removeAt(i+1)
                }
            }
                for (i in listCart.indices) {
                    println(listCart[i].kdBrg)
                    arrayItem = JSONObject()
                    arrayItem.put("kdbrg", listCart[i].kdBrg)
                    arrayItem.put("nmbrg", listCart[i].nmBrg.toString().replace("'", "''"))
                    arrayItem.put("satuan", listCart[i].satuan)
                    arrayItem.put("satuan3", listCart[i].satuan3)
                    arrayItem.put("qty", listCart[i].qtyOrder)
                    arrayItem.put("qtykvs3", listCart[i].qtyKvs3)
                    arrayItem.put("harga", listCart[i].harga)
                    arrayItem.put("diskon1", listCart[i].diskon1)
                    arrayItem.put("diskon2", listCart[i].diskon2)
                    arrayItem.put("diskon3", listCart[i].diskon3)
                    arrayItem.put("m3", listCart[i].mKubik1)
                    val disc1 = listCart[i].diskon1!!.toDouble()
                    val disc2 = listCart[i].diskon2!!.toDouble()
                    val disc3 = listCart[i].diskon3!!.toDouble()
                    val hrg: Double = ((listCart[i].harga!! * (1 - (disc1/100))) * (1 - (disc2/100))) * (1 - (disc3/100))
                    if (hrg < listCart[i].hrgJualMin!!) arrayItem.put("otoHarga", "1")
                    else arrayItem.put("otoHarga", "0")
                    if (hrg < listCart[i].hrgPokok!!) arrayItem.put("otoHPP", "1")
                    else arrayItem.put("otoHPP", "0")
                    paramItem.put(arrayItem)
                }
            } catch (e: JSONException) {
                e.printStackTrace()
                FirebaseCrashlytics.getInstance().recordException(e)
            }
            params["listItem"] = paramItem.toString()

            API.instance().create(ApiService::class.java)
                    .cekDoubleSO(params)
                    ?.enqueue(object : Callback<Result?> {

                        override fun onResponse(call: Call<Result?>, response: retrofit2.Response<Result?>) {
                            if (response.isSuccessful) {
                                val message = response.body()?.message.toString()
                                binding.tvProgressbar.visibility = View.GONE
                                if (message == "") {
                                    cekCustomer()
                                } else {
                                    dialogPeringatan(message)
                                }
                            } else {
                                val apiError: APIError? = ErrorUtils.parseError(response)
                                binding.progressbar.visibility = View.GONE
                                binding.tvProgressbar.visibility = View.GONE
                                window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                                Log.d("----------- Cek Double", "Error ${apiError?.message()}")
                                DialogAlert(apiError?.message(), "error", this@CreateSOActivity)
                            }
                        }

                        override fun onFailure(call: Call<Result?>, e: Throwable) {
                            binding.progressbar.visibility = View.GONE
                            binding.tvProgressbar.visibility = View.GONE
                            window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                            DialogAlert(e.message, "error", this@CreateSOActivity)
                            Log.d("----------- Cek Double", "Error $e")
                            FirebaseCrashlytics.getInstance().recordException(e)
                        }
                    })
        }
    }

    @SuppressLint("SetTextI18n")
    private fun cekSpecialPrice(kdbrg: String, qty: Double, tipe: String, nobukti: String, harga: Double, disc1: Double, disc2: Double, disc3: Double): String {
        var message = ""
        binding.tvProgressbar.text = "Sedang melakukan pengecekan special price..."
        doAsync {
            API.instance().create(ApiService::class.java)
                    .cekSpecialPrice(listHeader[0].kdcust.toString(), kdbrg, qty, listHeader[0].tglcreate.toString(), tipe, nobukti, harga, disc1, disc2, disc3)
                    ?.enqueue(object : Callback<Result?> {
                        override fun onResponse(call: Call<Result?>, response: retrofit2.Response<Result?>) {
                            if (response.isSuccessful) {
                                message = response.body()?.message.toString()
                                if (message != "") { DialogAlert(message, "attention", this@CreateSOActivity) }
                            } else {
                                message = response.body()?.message.toString()
                                binding.progressbar.visibility = View.GONE
                                binding.tvProgressbar.visibility = View.GONE
                                window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                                Log.d("----- Cek Special Price", "Error $message")
                                DialogAlert(message, "error", this@CreateSOActivity)
                            }
                        }

                        override fun onFailure(call: Call<Result?>, e: Throwable) {
                            binding.progressbar.visibility = View.GONE
                            binding.tvProgressbar.visibility = View.GONE
                            window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                            DialogAlert(e.message, "error", this@CreateSOActivity)
                            Log.d("----- Cek Special Price", "Error $e")
                            FirebaseCrashlytics.getInstance().recordException(e)
                        }
                    })
        }
        return message
    }

    @SuppressLint("InflateParams")
    private fun dialogPeringatan(message: String) {
        val dialogAttent = BottomSheetDialog(this, R.style.SheetDialogNotif)
        val bindingDialogYesOrNo = DialogYesOrNoBinding.inflate(layoutInflater)
        val dialogView = bindingDialogYesOrNo.root
        dialogAttent.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialogAttent.setContentView(dialogView)
        Objects.requireNonNull<Window>(dialogAttent.window).setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        bindingDialogYesOrNo.tvMessage.text = message

        dialogAttent.show()

        bindingDialogYesOrNo.btnYa.setOnClickListener {
            dialogAttent.dismiss()
            cekCustomer()
        }

        bindingDialogYesOrNo.btnTidak.setOnClickListener { dialogAttent.dismiss() }
    }

    private fun ubahAngka(angka: String): String {
        val buangRibuan = angka.replace(".", "")
        return buangRibuan.replace(",", ".")
    }

    private fun cekAkses() {
        for (i in listAkses.indices) {
            val str = listAkses[i].modul
            val modul = str.substring(str.indexOf("-") + 1)
            if (modul.equals("Customer", ignoreCase = true)) {
                if (listAkses[i].add == 1) {
                    isadd = true
                }
            }
        }
    }
}
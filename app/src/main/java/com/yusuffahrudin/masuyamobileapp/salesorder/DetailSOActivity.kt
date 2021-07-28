package com.yusuffahrudin.masuyamobileapp.salesorder

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Environment
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
import com.yusuffahrudin.masuyamobileapp.api.ApiService
import com.yusuffahrudin.masuyamobileapp.controller.DialogAlert
import com.yusuffahrudin.masuyamobileapp.data.ArrayTampung
import com.yusuffahrudin.masuyamobileapp.data.Result
import com.yusuffahrudin.masuyamobileapp.data.sales_order.ItemJual
import com.yusuffahrudin.masuyamobileapp.data.sales_order.ItemOrder
import com.yusuffahrudin.masuyamobileapp.data.sales_order.SalesOrder
import com.yusuffahrudin.masuyamobileapp.databinding.ActivityDetailSoBinding
import com.yusuffahrudin.masuyamobileapp.databinding.DialogCancelSoBinding
import com.yusuffahrudin.masuyamobileapp.databinding.DialogPilihBarangBinding
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
import java.io.File
import java.text.NumberFormat
import java.util.*

class DetailSOActivity: AppCompatActivity() {
    private lateinit var listHeader: ArrayList<SalesOrder>
    private lateinit var listBarang: ArrayList<ItemJual>
    private lateinit var listChartLama: ArrayList<ItemOrder>
    private lateinit var listCart: ArrayList<ItemOrder>
    private var isadd = false
    private lateinit var kdcust: String
    private lateinit var kdgd: String
    private lateinit var level: String
    private lateinit var statusPajak: String
    private lateinit var kdkota: String
    private lateinit var adapter: AdapterBarang
    private val nf = NumberFormat.getInstance(Locale("pt", "BR"))
    private lateinit var kdbrg: String
    private lateinit var nmbrg: String
    private var stok: Double = 0.0
    private lateinit var nobukti: String
    private lateinit var satuan: String
    private lateinit var satuan3: String
    private var qtykvs3: Double = 0.0
    private var qtyout: Double = 0.0
    private var harga: Double = 0.0
    private var hrgjualmin: Double = 0.0
    private var hrgpokok: Double = 0.0
    private var diskon1: Double = 0.0
    private var diskon2: Double = 0.0
    private var diskon3: Double = 0.0
    private var m3: Double = 0.0
    private lateinit var pdf: File
    private var reference: Long? = null
    private lateinit var satuanArray: MutableList<String>
    private lateinit var sessionManager: SessionManager
    private lateinit var name: String
    private var status: StringBuilder = StringBuilder("")
    private var otoCust = false
    private var otoHarga = false
    private var otoHPP = false
    private var proses = false
    private var a = 0
    private var isSearch = false
    private val listAkses = ArrayTampung.getListAkses()
    private var aksesSaveOrEdit: Boolean = true
    private lateinit var binding: ActivityDetailSoBinding
    private lateinit var bindingDialogPilihBarang: DialogPilihBarangBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailSoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        this.title = "Detail Sales Order"

        sessionManager = SessionManager(applicationContext)
        val user = sessionManager.userDetails
        kdkota = user[SessionManager.kdkota].toString()
        name = user[SessionManager.kunci_email].toString()
        level = user[SessionManager.level].toString()

        val i = this.intent
        nobukti = i.extras?.getString("nobukti").toString()

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.progressbar.progressTintList = ColorStateList.valueOf(Color.RED)
        binding.progressbar.visibility  = View.GONE
        binding.tvProgressbar.visibility   = View.GONE

        listHeader = ArrayTampung.getListHeaderSO()
        listCart = ArrayTampung.getListChart()
        listChartLama = ArrayTampung.getListChartLama()

        //mSectionsPagerAdapter = SectionsPagerAdapter(supportFragmentManager)

        // Set up the ViewPager with the sections adapter.
        binding.containerSo.offscreenPageLimit = 1
        setupViewPager(binding.containerSo)

        binding.tabLayout.setupWithViewPager(binding.containerSo)
        binding.tabLayout.getTabAt(0)?.setIcon(R.drawable.ic_sales_order_white_100)
        binding.tabLayout.getTabAt(1)?.setIcon(R.drawable.ic_shopping_cart_64)

        registerReceiver(onDownloadComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
        //binding.tabLayout.getTabAt(2)?.setIcon(R.drawable.ic_signature_white_100)
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.ViewPagerOnTabSelectedListener(binding.containerSo) {
            override fun onTabSelected(tab: TabLayout.Tab) {
                if (tab.position == 1) {
                    if (listCart.size == 0) {
                        if(listHeader[0].statusorder == "OPEN"){
                            dialogPilihBarang()
                        } else {
                            DialogAlert("Add item tidak diperbolehkan pada SO pending/close", "attention", this@DetailSOActivity)
                        }
                    }
                }
            }
        })

        cekAkses()

        binding.btnSimpan.setOnClickListener {
            if(aksesSaveOrEdit){
                try {
                    val fragmentHeader = binding.containerSo.adapter?.instantiateItem(binding.containerSo, 0) as SOHeaderFragment
                    fragmentHeader.getHeader()
                    cekCustomer()
                } catch (e: Exception) {
                    FirebaseCrashlytics.getInstance().recordException(e)
                    DialogAlert(e.message.toString(), "error", this@DetailSOActivity)
                }
            } else {
                DialogAlert(getString(R.string.tidak_mempunyai_hak_akses), "attention", this@DetailSOActivity)
            }
        }
    }

    private fun cekAkses(){
        for (i in 0 until listAkses.size){
            val str = listAkses[i].modul
            val akses = str.substring(str.indexOf("-") + 1)
            if (akses.equals("Sales Order", ignoreCase = true)) {
                if (listAkses[i].edit == 0) {
                    aksesSaveOrEdit = false
                }
            }
            if (akses.equals("Customer", ignoreCase = true)) {
                if (listAkses[i].add == 1) {
                    isadd = true
                }
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
        if (listChartLama.size > 0){
            listChartLama.clear()
        }
        finish()
    }

    fun setView(){
        if(listHeader[0].statusorder == "OPEN"){
            binding.btnSimpan.visibility = View.VISIBLE
        } else {
            binding.btnSimpan.visibility = View.GONE
        }
        listHeader[0].createby = name

        val fragmentItem = binding.containerSo.adapter?.instantiateItem(binding.containerSo, 1) as SOItemFragment
        fragmentItem.setView()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
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
            if (listChartLama.size > 0){
                listChartLama.clear()
            }
            finish()
        } else if (id == R.id.action_add_item){
            if(listHeader[0].statusorder == "OPEN"){
                dialogPilihBarang()
            } else {
                DialogAlert("Add item tidak diperbolehkan pada SO pending/close", "attention", this@DetailSOActivity)
            }
        } else if (id == R.id.action_cancel){
            if(listHeader[0].statusorder == "OPEN" || listHeader[0].statusorder == "PENDING"){
                if(aksesSaveOrEdit){
                    dialogCancel()
                } else {
                    DialogAlert(getString(R.string.tidak_mempunyai_hak_akses), "attention", this@DetailSOActivity)
                }
            } else {
                DialogAlert("SO sudah closed", "attention", this@DetailSOActivity)
            }
        } else if (id == R.id.action_pdf){
            val a = Server(kdkota)
            val url = a.URL() + "tcpdf/invoice_so.php?nobukti=$nobukti"
            downloadPDF(url)
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setupViewPager(viewPager: ViewPager) {
        val adapter = ViewPagerAdapter(supportFragmentManager)
        adapter.addFragment(SOHeaderFragment(), "Header")
        adapter.addFragment(SOItemFragment(), "Item")
        //adapter.addFragment(SignatureFragment(), "Signature")
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

    private fun downloadPDF(url: String){
        binding.progressbar.visibility = View.VISIBLE
        val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val uri = Uri.parse(url)

        pdf = File("${getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)?.absolutePath}/$nobukti.pdf")
        println("--------------------------- pdf ${pdf.absolutePath}")
        //cek file
        if(pdf.exists()){
            pdf.delete()
        }

        //show notification
        val request = DownloadManager.Request(uri)
        request.setTitle(nobukti)
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
        request.setDestinationInExternalFilesDir(this, Environment.DIRECTORY_DOCUMENTS, "$nobukti.pdf")
        //request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        reference = downloadManager.enqueue(request)
    }

    private val onDownloadComplete = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            //Checking if the received broadcast is for our enqueued download by matching download id
            if (reference == id) {
                binding.progressbar.visibility = View.GONE
                val intent = Intent(Intent.ACTION_VIEW)
                intent.setDataAndType(Uri.fromFile(pdf), "application/pdf")
                intent.flags = Intent.FLAG_ACTIVITY_NO_HISTORY
                startActivity(intent)
            }
        }
    }

    private fun cekParameter(): Boolean{
        val fragmentHeader = binding.containerSo.adapter?.instantiateItem(binding.containerSo, 0) as SOHeaderFragment
        val hasil: Boolean = if (listHeader.size == 0){
            DialogAlert("Header tidak ada data, isi dulu..", "error", this@DetailSOActivity)
            false
        } else {
            when {
                listHeader[0].kdcust == "" -> {
                    DialogAlert("Field customer masih kosong, diisi dulu..", "error", this@DetailSOActivity)
                    false
                }
                listHeader[0].kdgd.equals("gudang", true) -> {
                    DialogAlert("Field gudang belum dipilih, lebih teliti lah..", "error", this@DetailSOActivity)
                    false
                }
                else -> true
            }
        }
        fragmentHeader.getHeader()
        return hasil
    }

    @SuppressLint("InflateParams", "SetTextI18n")
    private fun dialogPilihBarang(){
        val dialogPilihBrg = BottomSheetDialog(this, R.style.SheetDialogNotif)
        bindingDialogPilihBarang = DialogPilihBarangBinding.inflate(layoutInflater)
        val bottomSheetLayout = bindingDialogPilihBarang.root
        dialogPilihBrg.setContentView(bottomSheetLayout)
        dialogPilihBrg.setCancelable(false)
        listBarang = ArrayList()
        if(cekParameter()){
            kdcust = listHeader[0].kdcust.toString()
            statusPajak = listHeader[0].kodeTax.toString()
            kdgd = listHeader[0].kdgd.toString()

            //swipeRefresh = dialogPilihBrg.findViewById(R.id.swipe_refresh)!!
            //val rvBarang = dialogPilihBrg.findViewById<RecyclerView>(R.id.list_barang)
            //searchview = dialogPilihBrg.findViewById(R.id.searchview)!!
            //progressDialog = dialogPilihBrg.findViewById(R.id.progressbar)!!
            //val btnOk = dialogPilihBrg.findViewById<Button>(R.id.btn_ok)
            //val btnCancel = dialogPilihBrg.findViewById<Button>(R.id.btn_cancel)
            //tvTitle = dialogPilihBrg.findViewById(R.id.tv_title)!!
            bindingDialogPilihBarang.tvTitle.text = "Barang ${listHeader[0].jnsjualtax} (${listCart.size})"
            bindingDialogPilihBarang.progressbar.visibility = View.GONE
            bindingDialogPilihBarang.searchview.isActivated = true
            bindingDialogPilihBarang.searchview.queryHint = "Cari Barang"
            bindingDialogPilihBarang.searchview.onActionViewExpanded()
            bindingDialogPilihBarang.searchview.clearFocus()

            val layoutManager = GridLayoutManager(this, 2)
            bindingDialogPilihBarang.listBarang.setHasFixedSize(true)
            bindingDialogPilihBarang.listBarang.layoutManager = layoutManager
            adapter = AdapterBarang(this, listBarang)
            bindingDialogPilihBarang.listBarang.adapter = adapter

            if(listBarang.isEmpty()){ getData(0) }

            bindingDialogPilihBarang.tvAddPrice.setOnClickListener {
                cekAkses()
                if (isadd) {
                    val intent = Intent(this@DetailSOActivity, CreatePriceCust::class.java)
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
                    DialogAlert("anda tidak mempunyai hak akses", "error", this@DetailSOActivity)
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
                listBarang.clear()
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
                dialogPilihBrg.dismiss()
                listBarang.clear()
                val fragment = binding.containerSo.adapter?.instantiateItem(binding.containerSo, 1) as SOItemFragment
                fragment.adapter.notifyDataSetChanged()
            }

            bindingDialogPilihBarang.btnCancel.setOnClickListener { dialogPilihBrg.dismiss() }

            dialogPilihBrg.show()
        }
    }

    private fun getData(itemCount: Int) {
        bindingDialogPilihBarang.progressbar.visibility = View.VISIBLE

        val model = ViewModelProviders.of(this).get(DetailSOViewModel::class.java)
        model.getItem(this, kdcust, kdgd, statusPajak, itemCount).observe(this, { itemList: ArrayList<ItemJual> ->
            run {
                isSearch = false
                listBarang.addAll(itemList)
                a++
                adapter.notifyDataSetChanged()
                bindingDialogPilihBarang.progressbar.visibility = View.GONE
                bindingDialogPilihBarang.swipeRefresh.isRefreshing = false
            }
        })
    }

    private fun searchData(brg: String) {
        bindingDialogPilihBarang.progressbar.visibility = View.VISIBLE
        listBarang.clear()

        val model = ViewModelProviders.of(this).get(CreateSOViewModel::class.java)
        model.findItem(this, kdcust, kdgd, statusPajak, brg).observe(this, { itemList: ArrayList<ItemJual> ->
            run {
                isSearch = true
                listBarang.addAll(itemList)
                a++
                adapter.notifyDataSetChanged()
                bindingDialogPilihBarang.progressbar.visibility = View.GONE
                bindingDialogPilihBarang.swipeRefresh.isRefreshing = false
            }
        })
    }

    private inner class AdapterBarang(private val activity: Activity, private var list_tampung: ArrayList<ItemJual>) : RecyclerView.Adapter<AdapterBarang.ViewHolder>(){
        //private var lvFilter: ArrayList<Data> = list_tampung

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.rv_activity_pilihbarang, parent, false)
            return ViewHolder(view)
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
                    .into(holder.imgBrg)
            holder.tvStokbrg.text = nf.format(list_tampung[position].qty)
            holder.tvStokout.text = nf.format(list_tampung[position].qtyOut)
            holder.tvNamabrg.text = list_tampung[position].nmBrg
            if(listHeader[0].jnsjualtax.equals("PPN", ignoreCase = false)){
                holder.tvHargabrg.text = nf.format(list_tampung[position].harga!! * 1.1)
            } else {
                holder.tvHargabrg.text = nf.format(list_tampung[position].harga)
            }

            holder.btnAddToCart.setOnClickListener {
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
                hrgjualmin = list_tampung[position].hrgJualMin?:0.0
                hrgpokok = list_tampung[position].hrgPokok?:0.0

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

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var imgBrg: ImageView = itemView.findViewById(R.id.imageView)
            var tvStokbrg: TextView = itemView.findViewById(R.id.tv_stok)
            var tvStokout: TextView = itemView.findViewById(R.id.tv_stokout)
            var tvNamabrg: TextView = itemView.findViewById(R.id.tv_nmbrg)
            var tvHargabrg: TextView = itemView.findViewById(R.id.tv_harga)
            var btnAddToCart: Button = itemView.findViewById(R.id.btn_add_to_cart)
        }

        @SuppressLint("InflateParams", "SetTextI18n")
        private fun dialogQty() {
            val dialogQty = BottomSheetDialog(this.activity, R.style.SheetDialog)
            val bottomLayout = LayoutInflater.from(this@DetailSOActivity).inflate(R.layout.dialog_so_qty_item, null)
            dialogQty.setContentView(bottomLayout)
            dialogQty.setCancelable(true)

            val imgBrg = bottomLayout.findViewById<ImageView>(R.id.imageView)
            val edtQty = bottomLayout.findViewById<EditText>(R.id.edt_qty)
            val btnMinus = bottomLayout.findViewById<ImageView>(R.id.btn_minus)
            val btnPlus = bottomLayout.findViewById<ImageView>(R.id.btn_plus)
            val tvHarga = bottomLayout.findViewById<TextView>(R.id.tv_harga)
            val tvDiskon1 = bottomLayout.findViewById<TextView>(R.id.tv_diskon1)
            val tvDiskon2 = bottomLayout.findViewById<TextView>(R.id.tv_diskon2)
            val tvDiskon3 = bottomLayout.findViewById<TextView>(R.id.tv_diskon3)
            val spinSatuan = bottomLayout.findViewById<Spinner>(R.id.spin_satuan)
            val btnOk = bottomLayout.findViewById<Button>(R.id.btn_ok)
            val btnCancel = bottomLayout.findViewById<Button>(R.id.btn_cancel)

            edtQty.setText("1")
            val a = Server("")
            Glide.with(applicationContext)
                    .load(a.URL_IMAGE() + kdbrg + ".jpg")
                    .apply(RequestOptions()
                            .placeholder(R.mipmap.ic_launcher)
                            .error(R.drawable.img_not_found)
                            .override(150, 150)
                            .fitCenter())
                    .into(imgBrg)
            val adapterSatuan = ArrayAdapter(activity, R.layout.spinner_black, satuanArray)
            spinSatuan.adapter = adapterSatuan

            tvHarga.text = nf.format(harga)
            tvDiskon1.text = nf.format(diskon1)
            tvDiskon2.text = nf.format(diskon2)
            tvDiskon3.text = nf.format(diskon3)
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
            btnMinus.setOnClickListener {
                val qty = java.lang.Double.valueOf(ubahAngka(edtQty.text.toString()))
                if (qty > 0) {
                    edtQty.setText(nf.format(qty - 1))
                }
            }

            btnPlus.setOnClickListener {
                val qty = java.lang.Double.valueOf(ubahAngka(edtQty.text.toString()))
                edtQty.setText(nf.format(qty + 1))
            }

            btnOk.setOnClickListener {
                val qty = java.lang.Double.parseDouble(ubahAngka(edtQty.text.toString()))
                diskon1 = java.lang.Double.parseDouble(ubahAngka(tvDiskon1.text.toString()))
                diskon2 = java.lang.Double.parseDouble(ubahAngka(tvDiskon2.text.toString()))
                diskon3 = java.lang.Double.parseDouble(ubahAngka(tvDiskon3.text.toString()))
                /*if (qty > stok || stok == 0.0) {
                    DialogAlert("Stok tidak mencukupi..!", "attention", this.activity)
                } else if (harga == 0.0) {
                    DialogAlert("Harga Barang Rp0 \nInput harga barang di modul Update Pricelist", "attention", this.activity)
                } else {
                    val hrg: Double = harga * (1 - diskon1 / 100) * (1 - diskon2 / 100) * (1 - diskon3 / 100)
                    val subtotal = hrg * qty
                    val item = ItemOrder(kdbrg, nmbrg, qty, qtyout, qtykvs3, java.lang.Double.valueOf(harga),
                            java.lang.Double.valueOf(hrgpokok), java.lang.Double.valueOf(hrgjualmin),
                            java.lang.Double.valueOf(diskon1), java.lang.Double.valueOf(diskon2),
                            java.lang.Double.valueOf(diskon3), spinSatuan.selectedItem.toString(), satuan3,
                            m3, subtotal)

                    listCart.add(item)
                    bindingDialogPilihBarang.tvTitle.text = "Barang ${listHeader[0].jnsjualtax} (${listCart.size})"
                    dialogQty.dismiss()
                }*/
                val hrg: Double = ((harga * (1 - (diskon1/100))) * (1 - (diskon2/100))) * (1 - (diskon3/100))
                val subtotal = hrg * qty
                val item = ItemOrder(kdbrg, nmbrg, qty, qtyout, qtykvs3, java.lang.Double.valueOf(harga),
                        java.lang.Double.valueOf(hrgpokok), java.lang.Double.valueOf(hrgjualmin),
                        java.lang.Double.valueOf(diskon1), java.lang.Double.valueOf(diskon2),
                        java.lang.Double.valueOf(diskon3), spinSatuan.selectedItem.toString(), satuan3,
                        m3, subtotal)

                listCart.add(item)
                bindingDialogPilihBarang.tvTitle.text = "Barang ${listHeader[0].jnsjualtax} (${listCart.size})"
                dialogQty.dismiss()
            }

            btnCancel.setOnClickListener { dialogQty.dismiss() }

            dialogQty.show()
        }
    }

    @SuppressLint("InflateParams")
    private fun dialogCancel() {
        val dialog = BottomSheetDialog(this, R.style.SheetDialogNotif)
        val bindingDialogCancel = DialogCancelSoBinding.inflate(layoutInflater)
        val bottomLayout = bindingDialogCancel.root
        dialog.setContentView(bottomLayout)
        listHeader = ArrayTampung.getListHeaderSO()
        listCart = ArrayTampung.getListChart()

        bindingDialogCancel.btnYa.setOnClickListener {
            cancelSalesOrder(bindingDialogCancel.edtKet.text.toString())
            dialog.dismiss()
        }
        bindingDialogCancel.btnTidak.setOnClickListener { dialog.dismiss() }

        dialog.show()
    }

    //fungsi untuk select data dari database
    @SuppressLint("SetTextI18n")
    private fun cancelSalesOrder(ket: String) {
        binding.progressbar.visibility  = View.VISIBLE
        binding.tvProgressbar.visibility   = View.VISIBLE
        binding.tvProgressbar.text = "Cancel sales order..."
        window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        doAsync {
            val params = HashMap<String?, String?>()
            params["no_order"] = nobukti
            params["ket"] = ket
            val paramItem = JSONArray()
            var arrayItem: JSONObject?
            try {
                for (i in listCart.indices) {
                    arrayItem = JSONObject()
                    arrayItem.put("kdbrg", listCart[i].kdBrg)
                    arrayItem.put("kdgd", listCart[i].kdGd)
                    arrayItem.put("qty", listCart[i].qtyOrder)
                    paramItem.put(arrayItem)
                }
            } catch (e: JSONException) {
                e.printStackTrace()
                FirebaseCrashlytics.getInstance().recordException(e)
            }
            params["listItem"] = paramItem.toString()

            API.instance().create(ApiService::class.java)
                    .cancelSO(params)
                    ?.enqueue(object : Callback<Result?> {
                        override fun onFailure(call: Call<Result?>, e: Throwable) {
                            binding.progressbar.visibility = View.GONE
                            binding.tvProgressbar.visibility = View.GONE
                            window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                            DialogAlert(e.message, "error", this@DetailSOActivity)
                            Log.d("----------- Cancel SO", "Error $e")
                            FirebaseCrashlytics.getInstance().recordException(e)
                        }

                        override fun onResponse(call: Call<Result?>, response: retrofit2.Response<Result?>) {
                            if (response.isSuccessful) {
                                binding.progressbar.visibility = View.GONE
                                binding.tvProgressbar.visibility = View.GONE
                                window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                                if (response.body()?.success == 1) {
                                    DialogAlert(response.body()?.message, "success", this@DetailSOActivity)
                                    val fragmentHeader = binding.containerSo.adapter?.instantiateItem(binding.containerSo, 0) as SOHeaderFragment
                                    val fragmentItem = binding.containerSo.adapter?.instantiateItem(binding.containerSo, 1) as SOItemFragment
                                    fragmentHeader.selectSalesOrder()
                                    fragmentItem.selectSalesOrder()
                                } else {
                                    DialogAlert(response.body()?.message, "error", this@DetailSOActivity)
                                }
                            }
                        }
                    })
        }
    }

    @SuppressLint("SetTextI18n")
    private fun saveEditOrder() {
        binding.progressbar.visibility  = View.VISIBLE
        binding.tvProgressbar.visibility   = View.VISIBLE
        binding.tvProgressbar.text = "Proses save sales order..."
        doAsync {
            val params = HashMap<String?, String?>()
            var totalM3 = 0.0
            val paramItemLama = JSONArray()
            var arrayItemLama: JSONObject?
            try {
                for (i in listChartLama.indices) {

                    arrayItemLama = JSONObject()
                    arrayItemLama.put("kdbrg", listChartLama[i].kdBrg)
                    arrayItemLama.put("nmbrg", listChartLama[i].nmBrg.toString().replace("'", "''"))
                    arrayItemLama.put("satuan", listChartLama[i].satuan)
                    arrayItemLama.put("satuan3", listChartLama[i].satuan3)
                    arrayItemLama.put("qty", listChartLama[i].qtyOrder)
                    arrayItemLama.put("qtykvs3", listChartLama[i].qtyKvs3)
                    arrayItemLama.put("harga", listChartLama[i].harga)
                    arrayItemLama.put("diskon1", listChartLama[i].diskon1)
                    arrayItemLama.put("diskon2", listChartLama[i].diskon2)
                    arrayItemLama.put("diskon3", listChartLama[i].diskon3)
                    arrayItemLama.put("m3", listChartLama[i].mKubik1)
                    paramItemLama.put(arrayItemLama)

                    totalM3 += (listChartLama[i].qtyOrder!! * listChartLama[i].mKubik1!!)
                }
            } catch (e: JSONException) {
                e.printStackTrace()
                FirebaseCrashlytics.getInstance().recordException(e)
            }

            //JSONArray List Item Order
            val paramItem = JSONArray()
            var arrayItem: JSONObject?
            try {
                for (i in listCart.indices) {
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
                    if(listCart[i].harga!! < listCart[i].hrgJualMin!! ){
                        status.append(", ${listCart[i].kdBrg} dibawah harga jual min.")
                        otoHarga = true
                    }
                    if(listCart[i].harga!! < listCart[i].hrgPokok!! ){
                        status.append(", ${listCart[i].kdBrg} dibawah harga pokok")
                        otoHPP = true
                    }
                    if(listCart[i].satuan.equals("EKOR", false)||listCart[i].satuan.equals("LOAF", false)){
                        proses = true
                    }
                    totalM3 += (listCart[i].qtyOrder!! * listCart[i].mKubik1!!)
                }
            } catch (e: JSONException) {
                e.printStackTrace()
                FirebaseCrashlytics.getInstance().recordException(e)
                DialogAlert(e.message, "error", this@DetailSOActivity)
            }

            params["no_order"] = listHeader[0].nobukti.toString()
            params["pajak"] = listHeader[0].jnsjualtax.toString()
            params["tgl_create"] = listHeader[0].tglcreate.toString()
            params["tgl_kirim"] = listHeader[0].tglkirim.toString()
            params["tglPO"] = listHeader[0].tglPO.toString()
            params["cetak_note"] = listHeader[0].ket1.toString()
            params["kodePO"] = listHeader[0].noPO.toString()
            params["keterangan"] = listHeader[0].ket2.toString()
            params["kdgd"] = listHeader[0].kdgd.toString()
            params["createby"] = listHeader[0].createby.toString()
            params["orderby"] = listHeader[0].orderby.toString()
            params["subtotal"] = listHeader[0].subtotal!!.toBigDecimal().toPlainString()
            params["discfak_persen"] = listHeader[0].disc!!.toBigDecimal().toPlainString()
            params["discfak_total"] = listHeader[0].jmldisc1!!.toBigDecimal().toPlainString()
            params["ppn_persen"] = listHeader[0].prsppn!!.toBigDecimal().toPlainString()
            params["ppn_total"] = listHeader[0].ppn!!.toBigDecimal().toPlainString()
            params["total"] = listHeader[0].total!!.toBigDecimal().toPlainString()
            params["status"] = "$status"
            params["listItemLama"] = paramItemLama.toString()
            params["listItem"] = paramItem.toString()
            params["totalM3"] = totalM3.toBigDecimal().toPlainString()
            params["imgPO"] = listHeader[0].imgPO
            println("imgPO ${listHeader[0].imgPO}")
            println("no_order "+listHeader[0].nobukti.toString())
            println("pajak "+listHeader[0].jnsjualtax.toString())
            println("tgl_create "+listHeader[0].tglcreate.toString())
            println("tgl_kirim "+listHeader[0].tglkirim.toString())
            println("tglPO "+listHeader[0].tglPO.toString())
            println("cetak_note "+listHeader[0].ket1.toString())
            println("kodePO "+listHeader[0].noPO.toString())
            println("keterangan "+listHeader[0].ket2.toString())
            println("kdgd "+listHeader[0].kdgd.toString())
            println("createby "+listHeader[0].createby.toString())
            println("orderby "+listHeader[0].orderby.toString())
            println("subtotal "+listHeader[0].subtotal!!.toBigDecimal().toPlainString())
            println("discfak_persen "+listHeader[0].disc!!.toBigDecimal().toPlainString())
            println("discfak_total "+listHeader[0].jmldisc1!!.toBigDecimal().toPlainString())
            println("ppn_persen "+listHeader[0].prsppn!!.toBigDecimal().toPlainString())
            println("ppn_total "+listHeader[0].ppn!!.toBigDecimal().toPlainString())
            println("total "+listHeader[0].total!!.toBigDecimal().toPlainString())
            println("status "+"$status")
            println("listItem "+paramItem.toString())
            println("listitemlama "+paramItemLama.toString())





            API.instance().create(ApiService::class.java)
                    .updateSO(params)
                    ?.enqueue(object : Callback<Result?> {
                        override fun onFailure(call: Call<Result?>, e: Throwable) {
                            binding.progressbar.visibility = View.GONE
                            binding.tvProgressbar.visibility = View.GONE
                            window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                            DialogAlert(e.message, "error", this@DetailSOActivity)
                            Log.d("----------- Update SO", "Error $e")
                            FirebaseCrashlytics.getInstance().recordException(e)
                        }

                        override fun onResponse(call: Call<Result?>, response: retrofit2.Response<Result?>) {
                            println("response "+response)
                            if (response.isSuccessful) {
                                binding.progressbar.visibility = View.GONE
                                binding.tvProgressbar.visibility = View.GONE
                                window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                                if (response.body()?.success == 1) {
                                    val notif = SendNotification()
                                    var levelSpv: String
                                    var levelSM = ""
                                    when (level.length) {
                                        10 -> {
                                            levelSM = level.substring(0, level.length - 4)
                                            levelSpv = level.substring(0, level.length - 2)
                                        }
                                        8 -> {
                                            levelSM = level.substring(0, level.length - 2)
                                            levelSpv = level
                                        }
                                        6 -> {
                                            levelSM = level
                                            levelSpv = "${level}10"
                                        }
                                        else -> {
                                            levelSM = "101020"
                                            levelSpv = "10102010"
                                        }
                                    }
                                    val levelSpvAR = "10101020"
                                    val levelStaffAR = levelSpvAR + "10"
                                    val levelWare = "1010501010"
                                    val levelAdminWare = levelWare.substring(0, levelWare.length - 2)
                                    val levelKepalaWare = levelWare.substring(0, levelWare.length - 4)
                                    println("level "+level)
                                    //error here
                                    val levelBM = level.substring(0, level.length - 6)
                                    if (otoCust && otoHarga) {
                                        println("================ cust & harga")
                                        notif.pushNotif(this@DetailSOActivity, kdkota,
                                                "Ada SO yang perlu diotorisasi!",
                                                "${listHeader[0].nobukti} (${listHeader[0].nmsales}) - ${listHeader[0].nmcust}",
                                                "SO",
                                                "'$levelSpvAR','$levelStaffAR','$levelSM','$levelSpv'")
                                    } else if (!otoCust && otoHarga) {
                                        println("================ harga")
                                        notif.pushNotif(this@DetailSOActivity, kdkota,
                                                "Ada SO yang perlu diotorisasi!",
                                                "${listHeader[0].nobukti} (${listHeader[0].nmsales}) - ${listHeader[0].nmcust}",
                                                "SO",
                                                "'$levelSM','$levelSpv'")
                                    } else if (otoCust && !otoHarga) {
                                        println("================ cust")
                                        notif.pushNotif(this@DetailSOActivity, kdkota,
                                                "Ada SO yang perlu diotorisasi!",
                                                "${listHeader[0].nobukti} (${listHeader[0].nmsales}) - ${listHeader[0].nmcust}",
                                                "SO",
                                                "'$levelSpvAR','$levelStaffAR','$levelSM','$levelSpv'")
                                    }
                                    if (otoHPP) {
                                        println("================ hpp")
                                        notif.pushNotif(this@DetailSOActivity, kdkota,
                                                "Ada SO yang perlu diotorisasi!",
                                                "${listHeader[0].nobukti} (${listHeader[0].nmsales}) - ${listHeader[0].nmcust}",
                                                "SO",
                                                "'$levelBM'")
                                    }
                                    if (proses) {
                                        notif.pushNotif(this@DetailSOActivity, kdkota,
                                                "Ada SO yang perlu diproses timbang!",
                                                "${listHeader[0].nobukti} (${listHeader[0].nmsales}) - ${listHeader[0].nmcust}",
                                                "Timbangan",
                                                "'$levelKepalaWare','$levelAdminWare','$levelWare'")
                                    }
                                    binding.progressbar.visibility = View.GONE
                                    binding.tvProgressbar.visibility = View.GONE
                                    window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                                    DialogAlert(response.body()?.message, "success-reply", this@DetailSOActivity)
                                    listHeader.clear()
                                    listCart.clear()
                                    listChartLama.clear()
                                } else {
                                    binding.progressbar.visibility = View.GONE
                                    binding.tvProgressbar.visibility = View.GONE
                                    window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                                    DialogAlert(response.body()?.message, "error", this@DetailSOActivity)
                                }
                            }
                        }
                    })
        }
    }

    private fun ubahAngka(angka: String): String {
        val buangRibuan = angka.replace(".", "")
        return buangRibuan.replace(",", ".")
    }

    @SuppressLint("SetTextI18n")
    private fun cekCustomer(){
        binding.progressbar.visibility  = View.VISIBLE
        binding.tvProgressbar.visibility   = View.VISIBLE
        binding.tvProgressbar.text = "Pengecekan customer..."
        window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        doAsync {
            API.instance().create(ApiService::class.java)
                    .cekCustomerSO(listHeader[0].kdcust, listHeader[0].kdkel)
                    ?.enqueue(object : Callback<String?> {
                        override fun onFailure(call: Call<String?>, e: Throwable) {
                            binding.progressbar.visibility = View.GONE
                            binding.tvProgressbar.visibility = View.GONE
                            window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                            DialogAlert(e.message, "error", this@DetailSOActivity)
                            Log.d("-------- Cek Customer", "Error $e")
                            FirebaseCrashlytics.getInstance().recordException(e)
                        }

                        override fun onResponse(call: Call<String?>, response: retrofit2.Response<String?>) {
                            if (response.code() == 200) {
                                val message = response.body().toString()
                                binding.tvProgressbar.visibility = View.GONE
                                if (message.equals("Customer has been blacklisted!", true)) {
                                    binding.progressbar.visibility = View.GONE
                                    window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                                    DialogAlert(message, "error", this@DetailSOActivity)
                                } else {
                                    status = StringBuilder("")
                                    status.append(message)
                                    if (!message.equals("Verified", true)) {
                                        otoCust = true
                                    }
                                    val fragmentHeader = binding.containerSo.adapter?.instantiateItem(binding.containerSo, 0) as SOHeaderFragment
                                    fragmentHeader.getHeader()
                                    listHeader[0].createby = name
                                    val fragmentItem = binding.containerSo.adapter?.instantiateItem(binding.containerSo, 1) as SOItemFragment
                                    fragmentItem.getTotal()

                                    saveEditOrder()
                                }
                            }
                        }
                    })
        }
    }
}
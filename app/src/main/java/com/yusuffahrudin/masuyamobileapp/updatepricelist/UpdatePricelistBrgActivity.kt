package com.yusuffahrudin.masuyamobileapp.updatepricelist

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.yusuffahrudin.masuyamobileapp.R
import com.yusuffahrudin.masuyamobileapp.controller.AppController
import com.yusuffahrudin.masuyamobileapp.controller.DialogAlert
import com.yusuffahrudin.masuyamobileapp.data.Pricelist
import com.yusuffahrudin.masuyamobileapp.databinding.ActivityUpdatePricelistBrgBinding
import com.yusuffahrudin.masuyamobileapp.util.Server
import com.yusuffahrudin.masuyamobileapp.util.SessionManager
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.text.NumberFormat
import java.text.ParseException
import java.util.*

class UpdatePricelistBrgActivity: AppCompatActivity() {

    //region variable
    private lateinit var adapter: AdapterPricelist
    private var listData: MutableList<Pricelist> = ArrayList()
    private var success: Int = 0
    private lateinit var kdbrgx: String
    private lateinit var nmbrgx: String
    private lateinit var satuanx: String
    private lateinit var hrgx: String
    private lateinit var hrgincppnx: String
    private var hrgMin:Double = 0.0
    private var hrgMax:Double = 0.0
    private lateinit var diskon1x: String
    private lateinit var diskon2x: String
    private lateinit var diskon3x: String
    private lateinit var name: String
    private lateinit var level: String
    private lateinit var message: String
    private lateinit var kdkota: String
    private lateinit var sessionManager: SessionManager
    private val nf = NumberFormat.getInstance(Locale("in", "ID"))
    private lateinit var layoutManager: LinearLayoutManager

    private val TAG = UpdatePriceBrgActivity::class.java.simpleName
    private val TAG_SUCCESS = "success"
    private val TAG_MESSAGE = "message"
    internal var tag_json_obj = "json_obj_req"
    private lateinit var itemMenu : MenuItem
    private lateinit var binding: ActivityUpdatePricelistBrgBinding
    //endregion

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUpdatePricelistBrgBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        val i = intent
        kdbrgx = i.extras?.getString("kdbrg").toString()
        nmbrgx = i.extras?.getString("nmbrg").toString()
        satuanx = i.extras?.getString("satuan").toString()
        this.title = kdbrgx.toUpperCase(Locale.getDefault())

        val nf = NumberFormat.getInstance()
        hrgx = nf.format(i.extras!!.getDouble("hrg"))
        hrgincppnx = nf.format(i.extras!!.getDouble("hrgincppn"))
        diskon1x = nf.format(i.extras!!.getDouble("diskon1"))
        diskon2x = nf.format(i.extras!!.getDouble("diskon2"))
        diskon3x = nf.format(i.extras!!.getDouble("diskon3"))

        sessionManager = SessionManager(applicationContext)
        val user = sessionManager.userDetails
        name = user[SessionManager.kunci_email].toString()
        level = user[SessionManager.level].toString()
        kdkota = user[SessionManager.kdkota].toString()
        Toast.makeText(this, name, Toast.LENGTH_LONG).show()

        binding.btnSimpan.setOnClickListener { simpan() }
        binding.progressbar.visibility = View.GONE

        binding.filterBar.setOnClickListener{
            dialogFilter()
        }
        binding.btnClearFilter.setOnClickListener {
            binding.tvFilter1.text = "Filter Harga"
            binding.tvFilter2.text = resources.getString(R.string.divide)
            itemMenu.title = "Select All"
            selectCust()
        }

        //menghubungkan variabel dengan layout view dan java
        binding.rvPricelistBrg.setHasFixedSize(true)

        layoutManager = LinearLayoutManager(this)
        binding.rvPricelistBrg.layoutManager = layoutManager
        adapter = AdapterPricelist(listData)
        binding.rvPricelistBrg.adapter = adapter

        selectCust()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {

        menuInflater.inflate(R.menu.search_select_all, menu)

        val search = menu.findItem(R.id.action_search)
        itemMenu = menu.findItem(R.id.action_select_all)
        val searchView = search.actionView as SearchView
        search(searchView)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        if (id == android.R.id.home) {
            this.finish()
        } else if (id == R.id.select_all){
            if (item.title.toString().equals("Select All", ignoreCase = false)){
                for (i in 0 until listData.size){
                    listData[i].status = true
                    adapter.notifyDataSetChanged()
                }
                item.title = getString(R.string.unselect_all)
            } else {
                for (i in 0 until listData.size){
                    listData[i].status = false
                    adapter.notifyDataSetChanged()
                }
                item.title = getString(R.string.select_all)
            }
        }

        return super.onOptionsItemSelected(item)
    }

    //region filter
    @SuppressLint("SetTextI18n", "InflateParams")
    private fun dialogFilter() {
        val dialog = BottomSheetDialog(this, R.style.SheetDialog)
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_filter_update_pricelist, null)
        dialog.setContentView(dialogView)

        val terapkan = dialogView.findViewById<Button>(R.id.btn_terapkan)
        val cancel = dialogView.findViewById<Button>(R.id.btn_reset)
        val EdtHrgMin = dialogView.findViewById<EditText>(R.id.edt_hrg_min)
        val EdtHrgMax = dialogView.findViewById<EditText>(R.id.edt_hrg_max)

        terapkan.setOnClickListener {
            try {
                hrgMin = java.lang.Double.valueOf(ubahAngka(EdtHrgMin.text.toString()))
                hrgMax = java.lang.Double.valueOf(ubahAngka(EdtHrgMax.text.toString()))

                selectFilter(hrgMin, hrgMax)

                binding.tvFilter2.text = "${nf.format(hrgMin)} - ${nf.format(hrgMax)}"
            } catch (e: Exception){
                DialogAlert("Batas harga tidak boleh kosong", "error", this)
            }

            dialog.dismiss()
        }

        cancel.setOnClickListener {
            selectCust()
            binding.tvFilter2.text = "-"
            dialog.dismiss()
        }

        dialog.show()
    }
    //endregion

    //region ubahAngka
    private fun ubahAngka(angka: String): String {
        val buangRibuan = angka.replace(".", "")

        return buangRibuan.replace(",", ".")
    }
    //endregion

    //region search
    private fun search(searchView: SearchView) {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                adapter.filter.filter(newText)
                return true
            }
        })
    }
    //endregion

    //region selectCust
    private fun selectCust() {
        binding.progressbar.visibility = View.VISIBLE
        listData.clear()
        adapter.notifyDataSetChanged()
        val a = Server(kdkota)
        val url = a.URL() + "updateprice/produk/select_customer_brg.php"
        //swipeRefreshLayout.setRefreshing(true);

        val strReq = object : StringRequest(Method.POST, url, Response.Listener { response ->
            Log.d(TAG, "Response : $response")
            setRV(response)
            //notifikasi adanya perubahan data pada adapter
            adapter.notifyDataSetChanged()
        }, Response.ErrorListener { error ->
            binding.progressbar.visibility = View.GONE
            Log.e(TAG, "Error Volley : " + error.message)
            //swipeRefreshLayout.setRefreshing(false);
            DialogAlert("Internetmu lemot..! $error", "error", this)
        }) {
            override fun getParams(): Map<String, String> {
                //posting parameter ke post url
                val params = HashMap<String, String>()

                if (level.equals("1010201010") || level.equals("1010301010")) {
                    params["user"] = name
                } else {
                    params["user"] = ""
                }
                params["kdbrg"] = kdbrgx

                return params
            }
        }

        AppController.getInstance().addToRequestQueue(strReq, resources.getString(R.string.tag_json_obj))
    }
    //endregion

    //region setRV
    private fun setRV(response: String) {
        try {
            val jsonObject = JSONObject(response)
            val result = jsonObject.getJSONArray("result")
            for (i in 0 until result.length()) {
                try {
                    val obj = result.getJSONObject(i)
                    val item = Pricelist()
                    item.kdcust = obj.getString("KdCust")
                    item.nmcust = obj.getString("NmCust")
                    item.harga = obj.getDouble("Harga")
                    item.hrgIncPpn = obj.getDouble("HrgIncPpn")
                    //menambah item ke array
                    listData.add(item)
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
            //notifikasi adanya perubahan data pada adapter
            adapter.notifyDataSetChanged()
            binding.progressbar.visibility = View.GONE
            //swipeRefreshLayout.setRefreshing(false);
        } catch (e: JSONException) {
            binding.progressbar.visibility = View.GONE
            e.printStackTrace()
        }
    }
    //endregion

    //region selectFilter
    private fun selectFilter(hrgmin: Double, hrgmax: Double) {
        listData.clear()
        adapter.notifyDataSetChanged()
        val a = Server(kdkota)
        val url = a.URL() + "updateprice/produk/select_customer_byfilter.php"

        val progressDialog = ProgressDialog.show(this, "", "Please Wait...")
        object : Thread() {
            override fun run() {
                try {
                    sleep(1000)
                } catch (e: Exception) {
                    Log.e("tag", e.message.toString())
                }
            }
        }.start()
        //swipeRefreshLayout.setRefreshing(true);

        val strReq = object : StringRequest(Method.POST, url, Response.Listener { response ->
            Log.d(TAG, "Response : $response")
            setRV(response)
            progressDialog.dismiss()

            //notifikasi adanya perubahan data pada adapter
            adapter.notifyDataSetChanged()
        }, Response.ErrorListener { error ->
            Log.e(TAG, "Error Volley : " + error.message)
            //swipeRefreshLayout.setRefreshing(false);
            DialogAlert("Internetmu lemot..! $error", "error", this)
            progressDialog.dismiss()
        }) {
            override fun getParams(): Map<String, String> {
                //posting parameter ke post url
                val params = HashMap<String, String>()

                params["user"] = name
                params["kdbrg"] = kdbrgx
                params["hrgmin"] = hrgmin.toString()
                params["hrgmax"] = hrgmax.toString()

                return params
            }
        }

        AppController.getInstance().addToRequestQueue(strReq, resources.getString(R.string.tag_json_obj))
    }
    //endregion

    //region simpan
    private fun simpan() {
        binding.progressbar.visibility = View.VISIBLE
        val a = Server(kdkota)
        val url = a.URL() + "updateprice/produk/insert_pricelist_produk.php"

        val strReq = object : StringRequest(Method.POST, url, Response.Listener { response ->
            Log.v(TAG, "Response : $response")

            try {
                val jObj = JSONObject(response)
                success = jObj.getInt(TAG_SUCCESS)
                message = jObj.getString(TAG_MESSAGE)

                //cek error node pada JSON
                if (success == 1) {
                    binding.progressbar.visibility = View.GONE
                    DialogAlert(message, "success", this)
                    if (binding.tvFilter2.equals("-")){
                        selectCust()
                    } else {
                        selectFilter(hrgMin, hrgMax)
                    }
                    adapter.notifyDataSetChanged()
                } else {
                    binding.progressbar.visibility = View.GONE
                    DialogAlert("Internetmu lemot..! $jObj", "error", this)
                    listData.clear()
                }
            } catch (e: JSONException) {
                binding.progressbar.visibility = View.GONE
                e.printStackTrace()
                DialogAlert("Internetmu lemot..! $e", "error", this)
            }
        }, Response.ErrorListener { error -> DialogAlert(error.message.toString(), "error", this) }) {
            override fun getParams(): Map<String, String> {
                //posting parameter ke post url
                val param = HashMap<String, String>()
                val jParams = JSONArray()
                var array: JSONObject
                try {
                    for (i in listData.indices) {
                        if (listData[i].status == true){
                            val harga = NumberFormat.getInstance().parse(hrgx)
                            val hrgincppn = NumberFormat.getInstance().parse(hrgincppnx)
                            val diskon1 = NumberFormat.getInstance().parse(diskon1x)
                            val diskon2 = NumberFormat.getInstance().parse(diskon2x)
                            val diskon3 = NumberFormat.getInstance().parse(diskon3x)

                            array = JSONObject()

                            array.put("kdcust", listData[i].kdcust)
                            array.put("kdbrg", kdbrgx.toUpperCase(Locale.getDefault()))
                            array.put("satuan", satuanx)
                            array.put("hrg", java.lang.Double.valueOf(harga.toString()))
                            array.put("hrgincppn", java.lang.Double.valueOf(hrgincppn.toString()))
                            array.put("diskon1", java.lang.Double.valueOf(diskon1.toString()))
                            array.put("diskon2", java.lang.Double.valueOf(diskon2.toString()))
                            array.put("diskon3", java.lang.Double.valueOf(diskon3.toString()))

                            jParams.put(array)
                        }
                    }
                } catch (e: JSONException) {
                    binding.progressbar.visibility = View.GONE
                    e.printStackTrace()
                } catch (e: ParseException) {
                    binding.progressbar.visibility = View.GONE
                    e.printStackTrace()
                }

                println("jparam "+jParams.toString())
                println("param "+param.toString())
                param["array"] = jParams.toString()
                Log.v("Response : ", param.toString())

                return param
            }
        }
        AppController.getInstance().addToRequestQueue(strReq, resources.getString(R.string.tag_json_obj))
    }
    //endregion

    //region adapter
    private inner class AdapterPricelist() : RecyclerView.Adapter<AdapterPricelist.ViewHolder>(), Filterable{
        //private var rvData: MutableList<Pricelist> = ArrayList()
        private var rvFilter: MutableList<Pricelist> = ArrayList()

        constructor(data: MutableList<Pricelist>) : this() {
            this.rvFilter = data
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.rv_customer_pricelist_layout, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.cvList.animation = AnimationUtils.loadAnimation(this@UpdatePricelistBrgActivity, R.anim.show_from_left)
            holder.tvKdCust.text = listData[position].kdcust
            holder.tvNmCust.text = listData[position].nmcust
            holder.tvHarga.text = nf.format(listData[position].hrgIncPpn)
            holder.cbCek.isChecked = listData[position].status!!

            holder.cbCek.setOnClickListener {
                listData[position].status = holder.cbCek.isChecked
            }

        }

        override fun getItemCount(): Int {
            return listData.size
        }

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

            var tvKdCust: TextView
            var tvNmCust: TextView
            var tvHarga: TextView
            var cbCek: CheckBox
            var cvList: androidx.cardview.widget.CardView

            init {
                tvKdCust = itemView.findViewById(R.id.tv_kdcust)
                tvNmCust = itemView.findViewById(R.id.tv_nmcust)
                tvHarga = itemView.findViewById(R.id.tv_harga)
                cbCek = itemView.findViewById(R.id.cb_cek)
                cvList = itemView.findViewById(R.id.cv_main)
            }
        }

        override fun getFilter(): Filter {
            return object : Filter() {
                override fun performFiltering(charSequence: CharSequence): FilterResults {
                    val charString = charSequence.toString()
                    if (charString.isEmpty()) {
                        for (i in 0 until rvFilter.size){
                            rvFilter[i].status = false
                        }
                        listData = rvFilter
                    } else {
                        val filteredList = ArrayList<Pricelist>()
                        for (pricelist in listData) {
                            if (pricelist.kdcust!!.toLowerCase(Locale.getDefault()).contains(charString) ||pricelist.nmcust!!.toLowerCase(Locale.getDefault()).contains(charString)) { filteredList.add(pricelist) }
                        }
                        listData = filteredList
                    }
                    val filterResults = FilterResults()
                    filterResults.values = listData
                    return filterResults
                }

                override fun publishResults(charSequence: CharSequence, filterResults: FilterResults) {
                    listData = filterResults.values as ArrayList<Pricelist>
                    notifyDataSetChanged()
                }
            }
        }
    }
    //endregion
}

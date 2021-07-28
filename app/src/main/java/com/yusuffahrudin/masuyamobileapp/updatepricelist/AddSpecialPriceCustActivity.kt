package com.yusuffahrudin.masuyamobileapp.updatepricelist

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.cardview.widget.CardView
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.yusuffahrudin.masuyamobileapp.R
import com.yusuffahrudin.masuyamobileapp.api.API
import com.yusuffahrudin.masuyamobileapp.api.ApiService
import com.yusuffahrudin.masuyamobileapp.controller.DialogAlert
import com.yusuffahrudin.masuyamobileapp.data.Result
import com.yusuffahrudin.masuyamobileapp.data.customer.Customer
import com.yusuffahrudin.masuyamobileapp.data.informasi_barang.Product
import com.yusuffahrudin.masuyamobileapp.databinding.ActivityAddSpecialPriceCustBinding
import com.yusuffahrudin.masuyamobileapp.util.SessionManager
import org.jetbrains.anko.doAsync
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import java.text.ParseException
import java.util.*

class AddSpecialPriceCustActivity: AppCompatActivity() {

    //region variable
    private lateinit var adapter: Adapter
    private var listData: MutableList<Customer> = ArrayList()
    private lateinit var dataProduk: Product
    private lateinit var name: String
    private lateinit var level: String
    private lateinit var kdkota: String
    private lateinit var sessionManager: SessionManager
    private lateinit var layoutManager: LinearLayoutManager

    internal var tag_json_obj = "json_obj_req"
    private lateinit var itemMenu : MenuItem
    private lateinit var binding: ActivityAddSpecialPriceCustBinding
    //endregion

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddSpecialPriceCustBinding.inflate(layoutInflater)
        setContentView(binding.root)
        this.title = "Customer"
        setSupportActionBar(binding.toolbar)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        dataProduk = intent.getParcelableExtra<Product>(AddSpecialPriceActivity.PARCELPRODUCT) as Product
        binding.progressbar.visibility = View.GONE

        sessionManager = SessionManager(applicationContext)
        val user = sessionManager.userDetails
        name = user[SessionManager.kunci_email].toString()
        level = user[SessionManager.level].toString()
        kdkota = user[SessionManager.kdkota].toString()
        Toast.makeText(this, name, Toast.LENGTH_LONG).show()

        binding.btnSimpan.setOnClickListener { simpan() }

        //menghubungkan variabel dengan layout view dan java
        binding.rvPricelistBrg.setHasFixedSize(true)

        layoutManager = LinearLayoutManager(this)
        binding.rvPricelistBrg.layoutManager = layoutManager
        adapter = Adapter(listData)
        binding.rvPricelistBrg.adapter = adapter

        selectCustomer()
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
                    listData[i].checklist = true
                    adapter.notifyDataSetChanged()
                }
                item.title = getString(R.string.unselect_all)
            } else {
                for (i in 0 until listData.size){
                    listData[i].checklist = false
                    adapter.notifyDataSetChanged()
                }
                item.title = getString(R.string.select_all)
            }
        }

        return super.onOptionsItemSelected(item)
    }

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

    //fungsi untuk select data dari database
    private fun selectCustomer() {
        binding.progressbar.visibility = View.VISIBLE
        listData.clear()
        val model = ViewModelProviders.of(this).get(ListCustomerViewModel::class.java)
        model.getCustomer(this, "", "", 0).observe(this, {
            custList: ArrayList<Customer> ->
            run {
                listData.addAll(custList)
                adapter.notifyDataSetChanged()
            }
        })
        binding.progressbar.visibility = View.GONE
    }

    //region simpan
    private fun simpan() {
        binding.progressbar.visibility = View.VISIBLE
        doAsync {
            val params = HashMap<String?, String?>()
            val jParams = JSONArray()
            var array: JSONObject
            try {
                for (i in listData.indices) {
                    if (listData[i].checklist == true){
                        array = JSONObject()
                        array.put("kdcust", listData[i].kdcust)
                        array.put("kdbrg", dataProduk.kdbrg?.toUpperCase(Locale.getDefault()))
                        array.put("satuan", dataProduk.satuan)
                        array.put("harga", dataProduk.hrg)
                        array.put("hrgIncPpn", dataProduk.hrgIncPpn)
                        array.put("diskon1", dataProduk.diskon1)
                        array.put("diskon2", dataProduk.diskon2)
                        array.put("diskon3", dataProduk.diskon3)
                        array.put("qtykuota", dataProduk.qtyKuota)
                        array.put("startdate", dataProduk.startDate)
                        array.put("enddate", dataProduk.endDate)
                        array.put("createby", dataProduk.createBy)
                        array.put("tipe", dataProduk.tipePrice)
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
            params["array"] = jParams.toString()
            Log.v("Response : ", params.toString())

            API.instance().create(ApiService::class.java)
                    .saveSpecialPriceCust(params)
                    ?.enqueue(object : Callback<Result?> {
                        override fun onFailure(call: Call<Result?>, e: Throwable) {
                            binding.progressbar.visibility = View.GONE
                            DialogAlert(e.message, "error", this@AddSpecialPriceCustActivity)
                        }

                        override fun onResponse(call: Call<Result?>, response: retrofit2.Response<Result?>) {
                            if (response.isSuccessful) {
                                val message = response.body()?.message.toString()
                                if (response.body()?.success == 1) {
                                    binding.progressbar.visibility = View.GONE
                                    DialogAlert(message, "success", this@AddSpecialPriceCustActivity)
                                } else {
                                    binding.progressbar.visibility = View.GONE
                                    DialogAlert(message, "error", this@AddSpecialPriceCustActivity)
                                }
                            }
                        }
                    })
        }
    }
    //endregion

    //region adapter
    private inner class Adapter() : RecyclerView.Adapter<Adapter.ViewHolder>(), Filterable{
        private var rvFilter: MutableList<Customer> = ArrayList()

        constructor(data: MutableList<Customer>) : this() {
            this.rvFilter = data
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.rv_customer_layout, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.cvList.animation = AnimationUtils.loadAnimation(this@AddSpecialPriceCustActivity, R.anim.show_from_left)
            holder.tvKdCust.text = listData[position].kdcust
            holder.tvNmCust.text = listData[position].nmcust
            holder.cbCek.isChecked = listData[position].checklist!!

            holder.cbCek.setOnClickListener {
                listData[position].checklist = holder.cbCek.isChecked
            }

        }

        override fun getItemCount(): Int {
            return listData.size
        }

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

            var tvKdCust: TextView = itemView.findViewById(R.id.tv_kdcust)
            var tvNmCust: TextView = itemView.findViewById(R.id.tv_nmcust)
            var cbCek: CheckBox = itemView.findViewById(R.id.cb_cek)
            var cvList: CardView = itemView.findViewById(R.id.cv_main)
        }

        override fun getFilter(): Filter {
            return object : Filter() {
                override fun performFiltering(charSequence: CharSequence): FilterResults {
                    val charString = charSequence.toString()
                    if (charString.isEmpty()) {
                        listData = rvFilter
                    } else {
                        val filteredList = ArrayList<Customer>()
                        for (custlist in listData) {
                            if (custlist.kdcust!!.toLowerCase(Locale.getDefault()).contains(charString) ||custlist.nmcust!!.toLowerCase(Locale.getDefault()).contains(charString)) { filteredList.add(custlist) }
                        }
                        listData = filteredList
                    }
                    val filterResults = FilterResults()
                    filterResults.values = listData
                    return filterResults
                }

                override fun publishResults(charSequence: CharSequence, filterResults: FilterResults) {
                    listData = filterResults.values as ArrayList<Customer>
                    notifyDataSetChanged()
                }
            }
        }
    }
    //endregion
}

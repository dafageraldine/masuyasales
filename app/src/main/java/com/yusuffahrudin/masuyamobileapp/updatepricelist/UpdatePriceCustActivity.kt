package com.yusuffahrudin.masuyamobileapp.updatepricelist

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.yusuffahrudin.masuyamobileapp.R
import com.yusuffahrudin.masuyamobileapp.controller.AppController
import com.yusuffahrudin.masuyamobileapp.controller.ButtonClickListener
import com.yusuffahrudin.masuyamobileapp.controller.DialogAlert
import com.yusuffahrudin.masuyamobileapp.data.ArrayTampung
import com.yusuffahrudin.masuyamobileapp.data.informasi_barang.Product
import com.yusuffahrudin.masuyamobileapp.databinding.ActivityUpdatePriceCustBinding
import com.yusuffahrudin.masuyamobileapp.util.ButtonHelper
import com.yusuffahrudin.masuyamobileapp.util.Server
import com.yusuffahrudin.masuyamobileapp.util.SessionManager
import com.yusuffahrudin.masuyamobileapp.util.SwipeHelper
import org.json.JSONException
import org.json.JSONObject
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class UpdatePriceCustActivity : AppCompatActivity() {
    //private lateinit var adapter: AdapterLVUpdatePriceCust
    private lateinit var adapterRV: AdapterRV
    private lateinit var layoutManager: LinearLayoutManager
    private val listData = ArrayList<Product>()
    private var success: Int = 0
    private lateinit var kdcust: String
    private lateinit var name: String
    private lateinit var level: String
    private lateinit var kdkota: String
    private var isadd = false
    private var isedit = false
    private var isdelete = false
    private lateinit var sessionManager: SessionManager
    private val listAkses = ArrayTampung.getListAkses()
    private lateinit var binding: ActivityUpdatePriceCustBinding
    private var i: Int = 0
    private var isSearch = false

    companion object {
        private val TAG = UpdatePriceCustActivity::class.java.simpleName
        private var url_delete: String? = null
        private val TAG_SUCCESS = "success"
        private val TAG_MESSAGE = "message"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUpdatePriceCustBinding.inflate(layoutInflater)
        kdcust = intent.extras?.getString("kdcust").toString()
        this.title = kdcust
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        sessionManager = SessionManager(applicationContext)
        val user = sessionManager.userDetails
        name = user[SessionManager.kunci_email].toString()
        level = user[SessionManager.level].toString()
        kdkota = user[SessionManager.kdkota].toString()
        binding.progressbar.visibility = View.GONE

        cekAkses()

        binding.lvUpdatePriceCust.setHasFixedSize(true)
        layoutManager = LinearLayoutManager(this)
        binding.lvUpdatePriceCust.layoutManager = layoutManager

        adapterRV = AdapterRV(this, listData)
        binding.lvUpdatePriceCust.adapter = adapterRV

        object: SwipeHelper(this, binding.lvUpdatePriceCust, 150) {
            override fun instantiateButtonHelper(viewHolder: RecyclerView.ViewHolder, buffer: MutableList<ButtonHelper>) {
                buffer.add(ButtonHelper(this@UpdatePriceCustActivity,
                        "Delete",
                        30,R.drawable.ic_delete_black, Color.parseColor("#FF3C30"),
                        object: ButtonClickListener {
                            override fun onClick(post: Int) {
                                if (isdelete)
                                    deleteBarang(listData[post].kdbrg.toString())
                                else
                                    DialogAlert(getString(R.string.tidak_mempunyai_hak_akses), "error", this@UpdatePriceCustActivity)
                            }
                        }))
                buffer.add(ButtonHelper(this@UpdatePriceCustActivity,
                        "Update",
                        30,R.drawable.ic_edit_24, Color.parseColor("#FF9502"),
                        object: ButtonClickListener {
                            override fun onClick(post: Int) {
                                if (isedit) {
                                    edit(listData[post].kdbrg.toString(), listData[post].nmbrg.toString(), listData[post].satuan.toString(), listData[post].jnsPpn.toString(),
                                            listData[post].hrg, listData[post].hrgIncPpn, listData[post].diskon1, listData[post].diskon1, listData[post].diskon1)
                                } else
                                    DialogAlert(getString(R.string.tidak_mempunyai_hak_akses), "error", this@UpdatePriceCustActivity)
                            }
                        }))
            }
        }

        if(listData.isEmpty()){ getBarang(0) }

        binding.lvUpdatePriceCust.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
                if (firstVisibleItemPosition + visibleItemCount >= totalItemCount) {
                    if (i == 0) {
                        if (!isSearch) {
                            getBarang(totalItemCount)
                            i = 1
                        }
                    }
                } else {
                    i = 0
                }
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.add_refresh, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == android.R.id.home) {
            this.finish()
        } else if (id == R.id.action_refresh){
            listData.clear()
            getBarang(0)
        } else if (id == R.id.action_add){
            if (isadd) {
                val intent = Intent(this@UpdatePriceCustActivity, CreatePriceCust::class.java)
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
                DialogAlert("anda tidak mempunyai hak akses", "error", this@UpdatePriceCustActivity)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    //fungsi untuk select data dari database
    fun getBarang(itemCount: Int) {
        binding.progressbar.visibility = View.VISIBLE
        listData.clear()
        val model = ViewModelProviders.of(this).get(ListBarangViewModel::class.java)
        model.getBarang(this, level, kdcust, itemCount).observe(this, { brgList: ArrayList<Product> ->
            run {
                isSearch = false
                listData.addAll(brgList)
                i++
                adapterRV.notifyDataSetChanged()
            }
        })
        binding.progressbar.visibility = View.GONE
    }

    /*
    //fungsi untuk select data dari database
    fun selectPricelistCust() {
        listData.clear()
        adapter.notifyDataSetChanged()
        val a = Server(kdkota)
        url_select = a.URL() + "updateprice/customer/select_pricelist_cust.php"
        val progressDialog = ProgressDialog.show(this@UpdatePriceCustActivity, "", "Please Wait...")
        object : Thread() {
            override fun run() {
                try {
                    sleep(10000)
                } catch (e: Exception) {
                    Log.e("tag", e.message.toString())
                }
            }
        }.start()
        //swipeRefreshLayout.setRefreshing(true);

        val strReq = object : StringRequest(Method.POST, url_select, Response.Listener { response ->
            Log.d(TAG, "Response : $response")
            setRV(response)
            progressDialog.dismiss()

            //notifikasi adanya perubahan data pada adapter
            adapter.notifyDataSetChanged()
        }, Response.ErrorListener { error ->
            Log.e(TAG, "Error Volley : " + error.message)
            //swipeRefreshLayout.setRefreshing(false);
            DialogAlert(error.message.toString(), "error", this@UpdatePriceCustActivity)
            progressDialog.dismiss()
        }) {
            override fun getParams(): Map<String, String> {
                //posting parameter ke post url
                val params = HashMap<String, String>()
                params["kdcust"] = kdcust
                return params
            }
        }
        AppController.getInstance().addToRequestQueue(strReq, resources.getString(R.string.tag_json_obj))
    }

    //fungsi untuk memasukkan data dari database ke dalam arraylist
    @SuppressLint("SimpleDateFormat")
    private fun setRV(response: String) {
        try {
            val jsonObject = JSONObject(response)
            val result = jsonObject.getJSONArray("result")
            for (i in 0 until result.length()) {
                try {
                    val obj = result.getJSONObject(i)
                    val item = Product()
                    var tgl = ""
                    if (obj.getString(TAG_TGL) != "null") {
                        val dateStr = obj.getString(TAG_TGL)
                        var sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                        val date = sdf.parse(dateStr)
                        sdf = SimpleDateFormat("dd-MM-yyyy")
                        tgl = sdf.format(date)
                    }
                    item.kdbrg = obj.getString(TAG_KDBRG)
                    item.nmbrg = obj.getString(TAG_NMBRG)
                    item.tgl = tgl
                    item.satuan = obj.getString(TAG_SATUAN)
                    item.hrg = obj.getDouble(TAG_HARGA)
                    item.hrgIncPpn = obj.getDouble(TAG_HRGINCPPN)
                    item.diskon1 = obj.getDouble(TAG_DISKON1)
                    item.diskon2 = obj.getDouble(TAG_DISKON2)
                    item.diskon3 = obj.getDouble(TAG_DISKON3)
                    item.jnsPpn = obj.getString("JnsPPN")

                    //menambah item ke array
                    listData.add(item)
                } catch (e: JSONException) { e.printStackTrace()
                } catch (e: ParseException) { e.printStackTrace() }
            }
            adapter.notifyDataSetChanged()
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }
    private fun Delete(kdbrgx: String) {
        val builder1 = AlertDialog.Builder(this@UpdatePriceCustActivity)
        builder1.setTitle("Delete")
        builder1.setMessage("Yakin untuk menghapus $kdbrgx ?")
        builder1.setCancelable(true)

        builder1.setPositiveButton(
                "Yes"
        ) { dialog, _ ->
            dialog.cancel()
            delete(kdbrgx)
        }

        builder1.setNegativeButton(
                "No"
        ) { dialog, _ -> dialog.cancel() }

        val alert11 = builder1.create()
        alert11.show()
    }

    // fungsi untuk menghapus
    private fun delete(kdbrgx: String) {
        val a = Server(kdkota)
        url_delete = a.URL() + "updateprice/customer/delete_pricelist_produk_cust.php"
        val strReq = object : StringRequest(Method.POST, url_delete, Response.Listener { response ->
            Log.d(TAG, "Response: $response")
            try {
                val jObj = JSONObject(response)
                success = jObj.getInt(TAG_SUCCESS)
                if (success == 1) {
                    Log.d("delete", jObj.toString())
                    selectPricelistCust()
                    DialogAlert(jObj.getString(TAG_MESSAGE), "success", this@UpdatePriceCustActivity)
                    adapter.notifyDataSetChanged()
                } else {
                    DialogAlert(jObj.getString(TAG_MESSAGE), "error", this@UpdatePriceCustActivity)
                }
            } catch (e: JSONException) {
                // JSON error
                e.printStackTrace()
            }
        }, Response.ErrorListener { error -> DialogAlert(error.message.toString(), "error", this@UpdatePriceCustActivity) }) {
            override fun getParams(): Map<String, String> {
                // Posting parameters ke post url
                val params = HashMap<String, String>()
                params["kdcust"] = kdcust
                params["kdbrg"] = kdbrgx

                return params
            }
        }
        AppController.getInstance().addToRequestQueue(strReq, resources.getString(R.string.tag_json_obj))
    }

*/
    // fungsi untuk get edit data
    private fun edit(kdbrgx: String, nmbrgx: String, satuanx: String, jnsppnx: String, hrgx: Double?, hrgincppnx: Double?, diskon1x: Double?, diskon2x: Double?, diskon3x: Double?) {
        val intent = Intent(this@UpdatePriceCustActivity, CreatePriceCust::class.java)
        intent.putExtra("kdcust", kdcust)
        intent.putExtra("kdbrg", kdbrgx)
        intent.putExtra("nmbrg", nmbrgx)
        intent.putExtra("satuan", satuanx)
        intent.putExtra("jnsppn", jnsppnx)
        intent.putExtra("hrg", hrgx)
        intent.putExtra("hrgincppn", hrgincppnx)
        intent.putExtra("diskon1", diskon1x)
        intent.putExtra("diskon2", diskon2x)
        intent.putExtra("diskon3", diskon3x)
        startActivity(intent)
    }

    private fun cekAkses() {
        for (i in listAkses.indices) {
            val str = listAkses[i].modul
            val modul = str.substring(str.indexOf("-") + 1)
            if (modul.equals("Customer", ignoreCase = true)) {
                if (listAkses[i].add == 1) {
                    isadd = true
                }
                if (listAkses[i].edit == 1) {
                    isedit = true
                }
                if (listAkses[i].delete == 1) {
                    isdelete = true
                }
            }
        }
    }

    private fun deleteBarang(kdbrgx: String) {
        val builder1 = AlertDialog.Builder(this)
        builder1.setTitle("Delete")
        builder1.setMessage("Yakin untuk menghapus $kdbrgx ?")
        builder1.setCancelable(true)

        builder1.setPositiveButton(
                "Yes"
        ) { dialog, _ ->
            dialog.cancel()
            delete(kdbrgx)
        }

        builder1.setNegativeButton(
                "No"
        ) { dialog, _ -> dialog.cancel() }

        val alert11 = builder1.create()
        alert11.show()
    }

    // fungsi untuk menghapus
    private fun delete(kdbrgx: String) {
        val a = Server(kdkota)
        url_delete = a.URL() + "updateprice/customer/delete_pricelist_produk_cust.php"
        val strReq = object : StringRequest(Method.POST, url_delete, Response.Listener { response ->
            Log.d(TAG, "Response: $response")
            try {
                val jObj = JSONObject(response)
                success = jObj.getInt(TAG_SUCCESS)
                if (success == 1) {
                    Log.d("delete", jObj.toString())
                    getBarang(0)
                    DialogAlert(jObj.getString(TAG_MESSAGE), "success", this)
                    adapterRV.notifyDataSetChanged()
                } else {
                    DialogAlert(jObj.getString(TAG_MESSAGE), "error", this)
                }
            } catch (e: JSONException) {
                // JSON error
                e.printStackTrace()
            }
        }, Response.ErrorListener { error -> DialogAlert(error.message.toString(), "error", this) }) {
            override fun getParams(): Map<String, String> {
                // Posting parameters ke post url
                val params = HashMap<String, String>()
                params["kdcust"] = kdcust
                params["kdbrg"] = kdbrgx
                return params
            }
        }
        AppController.getInstance().addToRequestQueue(strReq, resources.getString(R.string.tag_json_obj))
    }

    class AdapterRV(private val activity: Activity, private var listBrg: List<Product>?) : RecyclerView.Adapter<AdapterRV.ViewHolder>(){
        private val nf = NumberFormat.getInstance(Locale("id", "IN"))

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_update_pricelist, parent, false)
            return ViewHolder(view)
        }

        @SuppressLint("SimpleDateFormat", "SetTextI18n")
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.itemView.animation = AnimationUtils.loadAnimation(activity, R.anim.show_from_left)
            holder.tvKdBrg.text = listBrg!![position].kdbrg
            holder.tvNmBrg.text = listBrg!![position].nmbrg
            if (listBrg!![position].tgl != null){
                val dateStr = listBrg!![position].tgl.toString()
                var sdf = SimpleDateFormat("yyyy-MM-dd")
                val date = sdf.parse(dateStr)!!
                sdf = SimpleDateFormat("dd-MM-yyyy")
                var tgl = sdf.format(date)
                holder.tvTgl.text = "$tgl"
            } else {
                holder.tvTgl.text = ""
            }
            holder.tvSatuan.text = listBrg!![position].satuan
            holder.tvHrg.text = nf.format(listBrg!![position].hrg)
            holder.tvDisc1.text = "Disc1 : ${nf.format(listBrg!![position].diskon1!!.toDouble())}%"
            if (nf.format(listBrg!![position].diskon1!!.toDouble()).equals("0")){
                holder.tvDisc1.setTextColor(Color.RED)
            }
            holder.tvDisc2.text = "Disc2 : ${nf.format(listBrg!![position].diskon2!!.toDouble())}%"
            if (nf.format(listBrg!![position].diskon2!!.toDouble()).equals("0")){
                holder.tvDisc2.setTextColor(Color.RED)
            }
            holder.tvDisc3.text = "Disc3 : ${nf.format(listBrg!![position].diskon3!!.toDouble())}%"
            if (nf.format(listBrg!![position].diskon3!!.toDouble()).equals("0")){
                holder.tvDisc3.setTextColor(Color.RED)
            }
        }

        override fun getItemCount(): Int {
            println(" size list ${listBrg!!.size}")
            return listBrg!!.size
        }

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var tvKdBrg: TextView = itemView.findViewById(R.id.tv_kdbrg)
            var tvNmBrg: TextView = itemView.findViewById(R.id.tv_nmbrg)
            var tvTgl: TextView = itemView.findViewById(R.id.tv_tgl)
            var tvSatuan: TextView = itemView.findViewById(R.id.tv_satuan)
            var tvHrg: TextView = itemView.findViewById(R.id.tv_harga)
            var tvDisc1: TextView = itemView.findViewById(R.id.tv_diskon1)
            var tvDisc2: TextView = itemView.findViewById(R.id.tv_diskon2)
            var tvDisc3: TextView = itemView.findViewById(R.id.tv_diskon3)
        }
    }
}

package com.yusuffahrudin.masuyamobileapp.listtimbangan

import android.app.Activity
import android.app.ProgressDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.yusuffahrudin.masuyamobileapp.R
import com.yusuffahrudin.masuyamobileapp.controller.AppController
import com.yusuffahrudin.masuyamobileapp.controller.DialogAlert
import com.yusuffahrudin.masuyamobileapp.data.TimbanganDetail
import com.yusuffahrudin.masuyamobileapp.databinding.ActivityTimbanganDetailBinding
import com.yusuffahrudin.masuyamobileapp.util.Server
import com.yusuffahrudin.masuyamobileapp.util.SessionManager
import org.jetbrains.anko.longToast
import org.json.JSONException
import org.json.JSONObject
import java.text.NumberFormat
import java.util.*

class DetailTimbanganActivity: AppCompatActivity() {

    private lateinit var swipeRefresh: SwipeRefreshLayout
    private var listTimbangan: ArrayList<TimbanganDetail> = ArrayList()
    private lateinit var kdkota: String
    private lateinit var name: String
    private lateinit var sessionManager: SessionManager
    private lateinit var nobukti: String
    private lateinit var status: String
    private val nf = NumberFormat.getInstance()
    private lateinit var adapter: AdapterDetail
    private lateinit var binding: ActivityTimbanganDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTimbanganDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        this.title = getString(R.string.detail_timbangan)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        sessionManager = SessionManager(this)
        val user = sessionManager.userDetails
        kdkota = user[SessionManager.kdkota].toString()
        name = user[SessionManager.kunci_email].toString()

        val i = intent
        nobukti = i.extras?.getString("nobukti").toString()
        status = i.extras?.getString("status").toString()

        swipeRefresh = findViewById(R.id.swipe_refresh)
        binding.rvDetailTimb.setHasFixedSize(true)
        val layoutManager = LinearLayoutManager(this)
        binding.rvDetailTimb.layoutManager = layoutManager

        //untuk mengisi data dari JSON ke Adapter
        adapter = AdapterDetail(this, listTimbangan)
        binding.rvDetailTimb.adapter = adapter

        selectDetailTimbangan()

        swipeRefresh.setOnRefreshListener(SwipeRefreshLayout.OnRefreshListener {
            selectDetailTimbangan()
        })
    }

    override fun onBackPressed() {
        finish()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    //fungsi untuk select data dari database
    fun selectDetailTimbangan() {
        listTimbangan.clear()
        val a = Server(kdkota)
        var urlSelect: String
        if (status.equals("PROSES", false)){
            urlSelect = a.URL() + "listtimbangan/select_detail_proses.php"
        } else {
            urlSelect = a.URL() + "listtimbangan/select_detail_selesai.php"
        }

        val progressDialog = ProgressDialog.show(this, "", "Please Wait...")
        object : Thread() {
            override fun run() {
                try {
                    sleep(10000)
                } catch (e: Exception) {
                    Log.e("tag", e.message.toString())
                }
            }
        }.start()

        val strReq = object : StringRequest(Method.POST, urlSelect, Response.Listener { response ->
            Log.d(this.localClassName, "Response : $response")
            setRV(response)
            // dismiss the progress dialog
            progressDialog.dismiss()
        }, Response.ErrorListener { error ->
            swipeRefresh.isRefreshing = false
            DialogAlert(error.message.toString(), "error", this)
            // dismiss the progress dialog
            progressDialog.dismiss()
        }) {
            override fun getParams(): Map<String, String> {
                //posting parameter ke post url
                val params = HashMap<String, String>()

                params["nobukti"] = nobukti

                return params
            }
        }
        AppController.getInstance().addToRequestQueue(strReq, resources.getString(R.string.tag_json_obj))

    }

    //fungsi untuk memasukkan data dari database ke dalam arraylist
    private fun setRV(response: String) {
        try {
            val jsonObject = JSONObject(response)
            val result = jsonObject.getJSONArray("result")
            for (i in 0 until result.length()) {
                try {
                    val obj = result.getJSONObject(i)
                    val item = TimbanganDetail()
                    item.kdcust = obj.getString("KdCust")
                    item.kdgd = obj.getString("KdGd")
                    item.kdbrg = obj.getString("KdBrg")
                    item.nmbrg = obj.getString("NmBrg")
                    item.satuan = obj.getString("Satuan")
                    item.qty = obj.getDouble("Qty")
                    item.qtyTimb = obj.getDouble("QtyTimb")
                    item.hrg = obj.getDouble("Hrg")
                    item.m3 = obj.getDouble("M3")

                    //menambah item ke array
                    listTimbangan.add(item)
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
            adapter.notifyDataSetChanged()
            swipeRefresh.isRefreshing = false
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private inner class AdapterDetail(private val activity: Activity, private var list_tampung: List<TimbanganDetail>) : androidx.recyclerview.widget.RecyclerView.Adapter<AdapterDetail.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            //membuat view baru
            val view = LayoutInflater.from(parent.context).inflate(R.layout.rv_timbangan_detail, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val a = Server("")
            Glide.with(applicationContext)
                    .load(a.URL_IMAGE() + list_tampung[position].kdbrg + ".jpg")
                    .apply(RequestOptions()
                            .placeholder(R.mipmap.ic_launcher)
                            .error(R.drawable.img_not_found)
                            .override(100, 100)
                            .fitCenter())
                    .into(holder.imgProduk)
            holder.tvNmBrg.text = list_tampung[position].nmbrg
            holder.tvKdBrg.text = list_tampung[position].kdbrg
            holder.tvSatuan.text = list_tampung[position].satuan
            holder.tvSatuanTimb.text = "KG"
            holder.tvQty.text = nf.format(list_tampung[position].qty)
            if  (nf.format(list_tampung[position].qtyTimb).equals("0")){
                holder.edtQtyTimb.setText("")
            } else {
                holder.edtQtyTimb.setText(nf.format(list_tampung[position].qtyTimb))
                holder.btnSimpan.visibility = View.GONE
                holder.edtQtyTimb.isEnabled = false
                holder.edtQtyTimb.isFocusable = false
            }

            holder.btnSimpan.setOnClickListener { _->
                longToast(ubahAngka(holder.edtQtyTimb.text.toString()))
                saveHasilTimbangan(position, ubahAngka(holder.edtQtyTimb.text.toString()))
                holder.edtQtyTimb.isEnabled = false
            }
        }

        override fun getItemCount(): Int {
            //mengembalikan jumlah data yang ada pada list recyclerview
            return list_tampung.size
        }

        inner class ViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
            var tvNmBrg: TextView = itemView.findViewById(R.id.tv_nmbrg)
            var tvKdBrg: TextView = itemView.findViewById(R.id.tv_kdbrg)
            var tvSatuan: TextView = itemView.findViewById(R.id.tv_satuan)
            var tvQty: TextView = itemView.findViewById(R.id.tv_qty)
            var tvSatuanTimb: TextView = itemView.findViewById(R.id.tv_satuan_timb)
            var edtQtyTimb: EditText = itemView.findViewById(R.id.edt_qty_timb)
            var imgProduk: ImageView = itemView.findViewById(R.id.img_produk)
            var btnSimpan: Button = itemView.findViewById(R.id.btn_simpan)
        }
    }

    private fun saveHasilTimbangan(posisi:Int, qtyTimb:String) {
        val a = Server(kdkota)
        val url_insert = a.URL() + "listtimbangan/insert_hasil_timbangan.php"

        val progressDialog = ProgressDialog.show(this, "", "Please Wait...")
        object : Thread() {
            override fun run() {
                try {
                    Thread.sleep(1000)
                } catch (e: Exception) {
                    Log.e(this.name, e.message.toString())
                }

            }
        }.start()

        val strReq = object : StringRequest(Request.Method.POST, url_insert, Response.Listener { response ->
            Log.v(::DetailTimbanganActivity.name, "Response : $response")
            try {
                val jObj = JSONObject(response)
                val success = jObj.getInt("success")
                val message = jObj.getString("message")
                //cek error node pada JSON
                if (success == 1) {
                    DialogAlert(message, "success", this)
                    progressDialog.dismiss()
                } else {
                    DialogAlert(message, "error", this)
                    progressDialog.dismiss()
                }
            } catch (e: JSONException) {
                e.printStackTrace()
                progressDialog.dismiss()
            }
        }, Response.ErrorListener { error ->
            error.printStackTrace()
            DialogAlert("Error saat penyimpanan, cek koneksi speed internet anda! $error.toString()", "error", this)
            progressDialog.dismiss()
        }) {
            override fun getParams(): Map<String, String> {
                //posting parameter ke post url
                val params = HashMap<String, String>()

                val jumlah = listTimbangan[posisi].hrg * qtyTimb.toDouble()
                val jumlahM3 = listTimbangan[posisi].m3 * qtyTimb.toDouble()
                params["qty"] = listTimbangan[posisi].qty.toString()
                params["qtyTimb"] = qtyTimb
                params["jumlah"] = jumlah.toBigDecimal().toPlainString()
                params["jumlahM3"] = jumlahM3.toBigDecimal().toPlainString()
                params["nobukti"] = nobukti
                params["kdbrg"] = listTimbangan[posisi].kdbrg
                params["kdgd"] = listTimbangan[posisi].kdgd
                params["satuan"] = listTimbangan[posisi].satuan
                params["satuanTimb"] = "KG"
                params["createby"] = name

                return params
            }
        }
        strReq.retryPolicy = DefaultRetryPolicy(0, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        AppController.getInstance().addToRequestQueue(strReq, resources.getString(R.string.tag_json_obj))
    }

    private fun ubahAngka(angka: String): String {
        val buangRibuan = angka.replace(".", "")

        return buangRibuan.replace(",", ".")
    }
}
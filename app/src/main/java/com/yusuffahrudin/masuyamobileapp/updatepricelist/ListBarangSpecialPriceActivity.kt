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

class ListBarangSpecialPriceActivity: AppCompatActivity() {
    private lateinit var adapterRV: AdapterRV
    private lateinit var layoutManager: LinearLayoutManager
    private val listData = ArrayList<Product>()
    private var success: Int = 0
    private lateinit var kdcust: String
    private lateinit var name: String
    private lateinit var level: String
    private lateinit var kdkota: String
    private var underCost = false
    private var underBottomSP = false
    private lateinit var sessionManager: SessionManager
    private var i: Int = 0
    private var isSearch = false
    private lateinit var binding: ActivityUpdatePriceCustBinding

    companion object {
        private val TAG = UpdatePriceCustActivity::class.java.simpleName
        private var url_delete: String? = null
        private const val TAG_SUCCESS = "success"
        private const val TAG_MESSAGE = "message"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUpdatePriceCustBinding.inflate(layoutInflater)
        val intent = intent
        kdcust = intent?.extras?.getString("kdcust").toString()
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
        if (user[SessionManager.underCost].toString() == "1") underCost = true
        if (user[SessionManager.underBottomSP].toString() == "1") underBottomSP = true
        binding.progressbar.visibility = View.GONE

        binding.lvUpdatePriceCust.setHasFixedSize(true)
        layoutManager = LinearLayoutManager(this)
        binding.lvUpdatePriceCust.layoutManager = layoutManager

        adapterRV = AdapterRV(this, listData)
        binding.lvUpdatePriceCust.adapter = adapterRV

        object: SwipeHelper(this, binding.lvUpdatePriceCust, 150) {
            override fun instantiateButtonHelper(viewHolder: RecyclerView.ViewHolder, buffer: MutableList<ButtonHelper>) {
                buffer.add(ButtonHelper(this@ListBarangSpecialPriceActivity,
                        "Delete",
                        30,R.drawable.ic_delete_black, Color.parseColor("#FF3C30"),
                        object: ButtonClickListener{
                            override fun onClick(post: Int) {
                                if (listData[post].tipePrice.equals("Under Cost", true) && underCost) deleteBarang(listData[post].kdbrg.toString(), listData[post].tipePrice.toString())
                                else if (listData[post].tipePrice.equals("Under Bottom", true) && underBottomSP) deleteBarang(listData[post].kdbrg.toString(), listData[post].tipePrice.toString())
                                else DialogAlert(getString(R.string.tidak_mempunyai_hak_akses), "error", this@ListBarangSpecialPriceActivity)
                            }
                        }))
                buffer.add(ButtonHelper(this@ListBarangSpecialPriceActivity,
                        "Update",
                        30, R.drawable.ic_edit_24, Color.parseColor("#FF9502"),
                        object : ButtonClickListener {
                            override fun onClick(post: Int) {
                                if (listData[post].tipePrice.equals("Under Cost", true) && underCost) {
                                    val intentUpdate = Intent(this@ListBarangSpecialPriceActivity, AddSpecialPriceActivity::class.java)
                                    intentUpdate.putExtra("kdcust", kdcust)
                                    intentUpdate.putExtra(AddSpecialPriceActivity.PARCELPRODUCT, listData[post])
                                    startActivity(intentUpdate)
                                } else if (listData[post].tipePrice.equals("Under Bottom", true) && underBottomSP) {
                                    val intentUpdate = Intent(this@ListBarangSpecialPriceActivity, AddSpecialPriceActivity::class.java)
                                    intentUpdate.putExtra("kdcust", kdcust)
                                    intentUpdate.putExtra(AddSpecialPriceActivity.PARCELPRODUCT, listData[post])
                                    startActivity(intentUpdate)
                                } else DialogAlert(getString(R.string.tidak_mempunyai_hak_akses), "error", this@ListBarangSpecialPriceActivity)
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
        } else if (id == R.id.action_add){
            if (underCost || underBottomSP) {
                val intent = Intent(this, AddSpecialPriceActivity::class.java)
                intent.putExtra("kdcust", kdcust)
                intent.removeExtra(AddSpecialPriceActivity.PARCELPRODUCT)
                startActivity(intent)
            } else DialogAlert(getString(R.string.tidak_mempunyai_hak_akses), "error", this@ListBarangSpecialPriceActivity)
        } else if (id == R.id.action_refresh){
            listData.clear()
            getBarang(0)
        }
        return super.onOptionsItemSelected(item)
    }

    //fungsi untuk select data dari database
    fun getBarang(itemCount: Int) {
        binding.progressbar.visibility = View.VISIBLE
        listData.clear()
        var tipe = ""
        if (underCost && underBottomSP) tipe = "All"
        else if (underBottomSP) tipe = "Under Bottom"
        else if (underCost) tipe = "Under Cost"
        val model = ViewModelProviders.of(this).get(ListBarangViewModel::class.java)
        model.getBarang(this, tipe, kdcust, itemCount).observe(this, { brgList: ArrayList<Product> ->
            run {
                isSearch = false
                listData.addAll(brgList)
                i++
                adapterRV.notifyDataSetChanged()
            }
        })
        binding.progressbar.visibility = View.GONE
    }

    private fun deleteBarang(kdbrgx: String, tipex: String) {
        val builder1 = AlertDialog.Builder(this)
        builder1.setTitle("Delete")
        builder1.setMessage("Yakin untuk menghapus $kdbrgx ?")
        builder1.setCancelable(true)

        builder1.setPositiveButton(
                "Yes"
        ) { dialog, _ ->
            dialog.cancel()
            delete(kdbrgx, tipex)
        }

        builder1.setNegativeButton(
                "No"
        ) { dialog, _ -> dialog.cancel() }

        val alert11 = builder1.create()
        alert11.show()
    }

    // fungsi untuk menghapus
    private fun delete(kdbrgx: String, tipex: String) {
        val a = Server(kdkota)
        url_delete = a.URL() + "updateprice/specialprice/delete_special_price.php"
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
                params["tipe"] = tipex
                return params
            }
        }
        AppController.getInstance().addToRequestQueue(strReq, resources.getString(R.string.tag_json_obj))
    }

    class AdapterRV(private val activity: Activity, private var listBrg: List<Product>?) : RecyclerView.Adapter<AdapterRV.ViewHolder>(){
        private val nf = NumberFormat.getInstance(Locale("id", "IN"))

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_special_price, parent, false)
            return ViewHolder(view)
        }

        @SuppressLint("SimpleDateFormat", "SetTextI18n")
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.itemView.animation = AnimationUtils.loadAnimation(activity, R.anim.show_from_left)
            holder.tvKdBrg.text = listBrg!![position].kdbrg
            holder.tvNmBrg.text = listBrg!![position].nmbrg
            holder.tvTipePrice.text = listBrg!![position].tipePrice
            var startDate = ""
            var endDate = ""
            if (listBrg!![position].startDate != null) {
                val dateStr = listBrg!![position].startDate.toString()
                var sdf = SimpleDateFormat("yyyy-MM-dd")
                val date = sdf.parse(dateStr)!!
                sdf = SimpleDateFormat("dd-MM-yyyy")
                startDate = sdf.format(date)
            }
            if (listBrg!![position].endDate != null) {
                val dateStr = listBrg!![position].endDate.toString()
                var sdf = SimpleDateFormat("yyyy-MM-dd")
                val date = sdf.parse(dateStr)!!
                sdf = SimpleDateFormat("dd-MM-yyyy")
                endDate = sdf.format(date)
            }
            holder.tvTgl.text = "$startDate s/d $endDate"
            holder.tvSatuan.text = listBrg!![position].satuan
            holder.tvQtyKuota.text = nf.format(listBrg!![position].qtyKuota)
            if (nf.format(listBrg!![position].qtyKuota).equals("0")){
                holder.tvQtyKuota.setTextColor(Color.RED)
            }
            holder.tvHrg.text = nf.format(listBrg!![position].hrg)
            holder.tvDisc1.text = "Disc1 : ${nf.format(listBrg!![position].diskon1!!.toDouble())}%"
            if (nf.format(listBrg!![position].diskon1).equals("0")){
                holder.tvDisc1.setTextColor(Color.RED)
            }
            holder.tvDisc2.text = "Disc2 : ${nf.format(listBrg!![position].diskon2!!.toDouble())}%"
            if (nf.format(listBrg!![position].diskon2).equals("0")){
                holder.tvDisc2.setTextColor(Color.RED)
            }
            holder.tvDisc3.text = "Disc3 : ${nf.format(listBrg!![position].diskon3!!.toDouble())}%"
            if (nf.format(listBrg!![position].diskon3).equals("0")){
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
            var tvQtyKuota: TextView = itemView.findViewById(R.id.tv_qtyKuota)
            var tvTipePrice: TextView = itemView.findViewById(R.id.tv_tipe_price)
            var tvHrg: TextView = itemView.findViewById(R.id.tv_harga)
            var tvDisc1: TextView = itemView.findViewById(R.id.tv_diskon1)
            var tvDisc2: TextView = itemView.findViewById(R.id.tv_diskon2)
            var tvDisc3: TextView = itemView.findViewById(R.id.tv_diskon3)
        }
    }
}
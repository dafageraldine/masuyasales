package com.yusuffahrudin.masuyamobileapp.listtimbangan

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.yusuffahrudin.masuyamobileapp.R
import com.yusuffahrudin.masuyamobileapp.controller.AppController
import com.yusuffahrudin.masuyamobileapp.controller.DialogAlert
import com.yusuffahrudin.masuyamobileapp.data.Timbangan
import com.yusuffahrudin.masuyamobileapp.databinding.LvProsesBinding
import com.yusuffahrudin.masuyamobileapp.util.Server
import com.yusuffahrudin.masuyamobileapp.util.SessionManager
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class SelesaiFragment: Fragment() {
    private lateinit var userKota: String
    private lateinit var kdkota: String
    private lateinit var sessionManager: SessionManager
    private var listTimbangan: ArrayList<Timbangan> = ArrayList()
    private lateinit var adapter: AdapterRVSelesai
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var rvSelesai: RecyclerView
    private var i = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_proses, container, false)

        sessionManager = SessionManager(this.context)
        val user = sessionManager.userDetails
        userKota = user[SessionManager.kota].toString()
        kdkota = user[SessionManager.kdkota].toString()

        rvSelesai = rootView.findViewById(R.id.lv_proses)
        swipeRefresh = rootView.findViewById(R.id.swipe_refresh)
        progressBar = rootView.findViewById(R.id.progressbar)
        progressBar.visibility = View.GONE
        rvSelesai.setHasFixedSize(true)
        val layoutManager = LinearLayoutManager(requireContext())
        rvSelesai.layoutManager = layoutManager

        adapter = AdapterRVSelesai(requireActivity(), listTimbangan)
        rvSelesai.adapter = adapter

        if(listTimbangan.isEmpty()){ selectListProses(0) }

        rvSelesai.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
                if (firstVisibleItemPosition + visibleItemCount >= totalItemCount) {
                    if (i == 0) {
                        selectListProses(totalItemCount)
                        i = 1
                    }
                } else {
                    i = 0
                }
            }
        })

        swipeRefresh.setOnRefreshListener {
            listTimbangan.clear()
            selectListProses(0)
        }

        return rootView
    }

    fun selectListProses(itemCount: Int) {
        progressBar.visibility = View.VISIBLE
        val a = Server(kdkota)
        val urlSelect = a.URL() + "listtimbangan/select_selesai.php"
        val strReq = object: StringRequest(Method.POST, urlSelect, Response.Listener { response ->
            Log.d(this.tag, "Response proses selesai : $response")
            setRV(response)
        }, Response.ErrorListener { error ->
            progressBar.visibility = View.GONE
            swipeRefresh.isRefreshing = false
            DialogAlert(error.message.toString(), "error", this.requireActivity())
        }) {
            override fun getParams(): Map<String, String> {
                val params = HashMap<String, String>()
                params["itemCount"] = itemCount.toString()
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
                    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    val item = Timbangan()
                    item.noBukti = obj.getString("NoBukti")
                    item.tgl = sdf.parse(obj.getString("Tgl"))
                    item.tglkirim = sdf.parse(obj.getString("TglKirim"))
                    item.createtime = sdf.parse(obj.getString("CreateTime"))
                    item.kdgd = obj.getString("KdGd")
                    item.kdcust = obj.getString("KdCust")
                    item.nmcust = obj.getString("NmCust")
                    item.ket1 = obj.getString("Ket1")
                    item.ket2 = obj.getString("Ket2")
                    item.nmsales = obj.getString("NmSales")

                    //menambah item ke array
                    listTimbangan.add(item)
                } catch (e: JSONException) {
                    progressBar.visibility = View.GONE
                    e.printStackTrace()
                }
            }
            i++
            progressBar.visibility = View.GONE
            swipeRefresh.isRefreshing = false
            adapter.notifyDataSetChanged()
        } catch (e: JSONException) {
            progressBar.visibility = View.GONE
            e.printStackTrace()
        }
    }

    class AdapterRVSelesai(private val activity: Activity, private var rvTimbangan: List<Timbangan>): RecyclerView.Adapter<AdapterRVSelesai.ViewHolder>(){

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = LvProsesBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.binding.cardview?.animation = AnimationUtils.loadAnimation(activity, R.anim.show_from_left)
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
            val sdfKirim = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            holder.binding.tvNobukti.text = rvTimbangan[position].noBukti
            holder.binding.tvNmcust.text = rvTimbangan[position].nmcust
            holder.binding.tvKet.text = rvTimbangan[position].ket2
            holder.binding.tvTgl.text = sdf.format(rvTimbangan[position].createtime)
            holder.binding.tvTglKirim?.text = sdfKirim.format(rvTimbangan[position].createtime)
            holder.binding.tvNmsales.text = rvTimbangan[position].nmsales

            holder.binding.cardview?.setOnClickListener {
                val intent = Intent(activity, DetailTimbanganActivity::class.java)
                intent.putExtra("nobukti", holder.binding.tvNobukti.text.toString())
                intent.putExtra("status", "SELESAI")
                activity.startActivity(intent)
            }
        }

        override fun getItemCount(): Int {
            return rvTimbangan.size
        }

        inner class ViewHolder(val binding : LvProsesBinding) : RecyclerView.ViewHolder(binding.root)
    }
}
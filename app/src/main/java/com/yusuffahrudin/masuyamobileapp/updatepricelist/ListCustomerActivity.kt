package com.yusuffahrudin.masuyamobileapp.updatepricelist

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.animation.AnimationUtils
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.yusuffahrudin.masuyamobileapp.R
import com.yusuffahrudin.masuyamobileapp.controller.AppController
import com.yusuffahrudin.masuyamobileapp.controller.DialogAlert
import com.yusuffahrudin.masuyamobileapp.data.Customer
import com.yusuffahrudin.masuyamobileapp.util.Server
import com.yusuffahrudin.masuyamobileapp.util.SessionManager
import org.json.JSONException
import org.json.JSONObject
import java.util.*

/**
 * Created by yusuf fahrudin on 16-05-2017.
 */

class ListCustomerActivity : AppCompatActivity() {

    private lateinit var adapterRV: AdapterRV
    private lateinit var rv_update_price_cust: RecyclerView
    private var listCust: MutableList<Customer> = ArrayList()
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var sessionManager: SessionManager
    private lateinit var progressBar: ProgressBar
    private var i: Int = 0
    private var isSearch = false

    companion object {
        private var name: String? = null
        private var level: String? = null
        private var kdkota: String? = null

        private val TAG = ListCustomerActivity::class.java.simpleName

        val TAG_KDCUST = "KdCust"
        val TAG_NMCUST = "NmCust"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.title = "List Customer"
        setContentView(R.layout.activity_list_customer)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        sessionManager = SessionManager(applicationContext)
        val user = sessionManager.userDetails
        name = user[SessionManager.kunci_email]
        level = user[SessionManager.level]
        kdkota = user[SessionManager.kdkota]
        Toast.makeText(this@ListCustomerActivity, name, Toast.LENGTH_LONG).show()

        //menghubungkan variabel dengan layout view dan java
        progressBar = findViewById(R.id.progressbar)
        progressBar.visibility = View.GONE
        rv_update_price_cust = findViewById(R.id.rv_update_price_cust)
        rv_update_price_cust.setHasFixedSize(true)

        layoutManager = LinearLayoutManager(this)
        rv_update_price_cust.layoutManager = layoutManager

        //untuk mengisi data dari JSON ke Adapter
        adapterRV = AdapterRV(this@ListCustomerActivity, listCust)
        rv_update_price_cust.adapter = adapterRV

        if(listCust.isEmpty()){ selectCustomer(0) }

        rv_update_price_cust.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
                if (firstVisibleItemPosition + visibleItemCount >= totalItemCount) {
                    if (i == 0) {
                        if(!isSearch){
                            selectCustomer(totalItemCount)
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
        menuInflater.inflate(R.menu.search_refresh, menu)
        val search = menu.findItem(R.id.action_search)
        val searchView = search.actionView as SearchView
        search(searchView)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        if (id == android.R.id.home) {
            this.finish()
        }
        if (id == R.id.action_refresh) {
            listCust.clear()
            selectCustomer(0)
        }

        return super.onOptionsItemSelected(item)
    }

    private fun search(searchView: SearchView) {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                listCust.clear()
                searchCustomer(query)
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                return false
            }
        })
    }

    //fungsi untuk select data dari database
    private fun searchCustomer(cust: String) {
        progressBar.visibility = View.VISIBLE
        val a = Server(kdkota)
        val url = a.URL() + "customer/search_customer.php"
        val strReq = object : StringRequest(Method.POST, url, Response.Listener { response ->
            Log.d(TAG, "Response : $response")
            isSearch = true
            setRV(response)
        }, Response.ErrorListener { error ->
            progressBar.visibility = View.GONE
            DialogAlert(error.message.toString(), "error", this@ListCustomerActivity)
        }) {
            override fun getParams(): Map<String, String> {
                val params = HashMap<String, String>()
                if (level.equals("1010201010") || level.equals("1010301010")) {
                    params["user"] = name!!
                } else {
                    params["user"] = ""
                }
                params["cust"] = cust
                return params
            }
        }
        AppController.getInstance().addToRequestQueue(strReq, resources.getString(R.string.tag_json_obj))
    }

    //fungsi untuk select data dari database
    private fun selectCustomer(itemCount: Int) {
        progressBar.visibility = View.VISIBLE
        val a = Server(kdkota)
        val url = a.URL() + "customer/select_customer.php"
        val strReq = object : StringRequest(Method.POST, url, Response.Listener { response ->
            Log.d(TAG, "Response : $response")
            isSearch = false
            setRV(response)
        }, Response.ErrorListener { error ->
            progressBar.visibility = View.GONE
            DialogAlert(error.message.toString(), "error", this@ListCustomerActivity)
        }) {
            override fun getParams(): Map<String, String> {
                val params = HashMap<String, String>()
                if (level.equals("1010201010") || level.equals("1010301010")) {
                    params["user"] = name!!
                } else {
                    params["user"] = ""
                }
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
                    val item = Customer()
                    item.kdcust = obj.getString(TAG_KDCUST)
                    item.nmcust = obj.getString(TAG_NMCUST)

                    //menambah item ke array
                    listCust.add(item)
                } catch (e: JSONException) {
                    progressBar.visibility = View.GONE
                    e.printStackTrace()
                }
            }
            i++
            progressBar.visibility = View.GONE
            adapterRV.notifyDataSetChanged()
        } catch (e: JSONException) {
            progressBar.visibility = View.GONE
            e.printStackTrace()
        }
    }

    class AdapterRV(private val activity: Activity, private var rvCust: List<Customer>?) : RecyclerView.Adapter<AdapterRV.ViewHolder>(){

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.rv_customer, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.cvList.animation = AnimationUtils.loadAnimation(activity, R.anim.show_from_left)
            val kdcust = rvCust!![position].kdcust
            holder.tvKdCust.text = rvCust!![position].kdcust
            holder.tvNmCust.text = rvCust!![position].nmcust

            holder.cvList.setOnClickListener {
                val intent = Intent(activity, UpdatePriceCustActivity::class.java)
                intent.putExtra("kdcust", kdcust)
                activity.startActivity(intent)
            }
        }

        override fun getItemCount(): Int {
            return rvCust!!.size
        }

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var tvKdCust: TextView = itemView.findViewById(R.id.tv_kdcust)
            var tvNmCust: TextView = itemView.findViewById(R.id.tv_nmcust)
            var cvList: CardView = itemView.findViewById(R.id.cv_main)
        }
    }
}

package com.yusuffahrudin.masuyamobileapp.laporan

import android.annotation.SuppressLint
import android.app.Activity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.evrencoskun.tableview.TableView
import com.yusuffahrudin.masuyamobileapp.R
import com.yusuffahrudin.masuyamobileapp.adapter.AdapterTableView
import com.yusuffahrudin.masuyamobileapp.controller.AppController
import com.yusuffahrudin.masuyamobileapp.controller.DialogAlert
import com.yusuffahrudin.masuyamobileapp.data.CellModel
import com.yusuffahrudin.masuyamobileapp.data.ColumnHeaderModel
import com.yusuffahrudin.masuyamobileapp.data.RowHeaderModel
import com.yusuffahrudin.masuyamobileapp.util.Server
import com.yusuffahrudin.masuyamobileapp.util.SessionManager
import org.json.JSONException
import org.json.JSONObject
import java.text.NumberFormat
import java.util.*

class DetailPenjBrgCustQtyBln (val activity: Activity,
                               val bln: String,
                               val kdbrg: String,
                               val thn: String,
                               val kota: String,
                               val cbg: String) {

    private var sessionManager: SessionManager
    private var kdkota: String
    private lateinit var dialog: android.app.AlertDialog.Builder
    private lateinit var dialogView: View
    private lateinit var inflater: LayoutInflater
    private var colValues: MutableList<ColumnHeaderModel> = mutableListOf()
    private var rowValues: MutableList<RowHeaderModel> = mutableListOf()
    private var cellValues: MutableList<MutableList<CellModel>> = mutableListOf()
    private lateinit var addCache: MutableList<CellModel>
    private lateinit var tvKdBrg: TextView
    private lateinit var tvNmBrg: TextView
    private lateinit var tvTgl: TextView
    private lateinit var progress: ProgressBar
    private lateinit var table: TableView
    private val nf = NumberFormat.getInstance(Locale("pt", "BR"))
    private lateinit var adapterTable: AdapterTableView

    init {
        sessionManager = SessionManager(activity)
        val user = sessionManager.userDetails
        kdkota = user[SessionManager.kdkota].toString()

        showData()
    }

    @SuppressLint("InflateParams")
    private fun showData(){
        dialog = android.app.AlertDialog.Builder(activity)
        inflater = activity.layoutInflater
        dialogView = inflater.inflate(R.layout.dialog_detail_penj_brg_cust_qty_thn, null)
        dialog.setView(dialogView)

        tvKdBrg = dialogView.findViewById(R.id.tv_kdbrg)
        tvNmBrg = dialogView.findViewById(R.id.tv_nmbrg)
        tvTgl = dialogView.findViewById(R.id.tv_tgl)
        progress = dialogView.findViewById(R.id.progressbar)
        table = dialogView.findViewById(R.id.container_table)
        tvKdBrg.text = kdbrg
        tvTgl.text = "Bulan $bln $thn"

        getData()

        dialog.setPositiveButton("OK") { dialog, which ->
            dialog.dismiss()
        }

        val alert = dialog.create()
        alert.show()
    }

    private fun getData(){
        colValues.clear()
        rowValues.clear()
        cellValues.clear()

        val a = Server(kdkota)
        val urlSelect = a.URL() + "laporan/detail_penjcustbrgqtybln.php"
        progress.visibility = View.VISIBLE

        val strReq = object : StringRequest(Method.POST, urlSelect, Response.Listener { response ->
            Log.d(activity.localClassName, "Response : $response")
            setRV(response)
            // dismiss the progress dialog
            progress.visibility = View.GONE
        }, Response.ErrorListener { error ->
            DialogAlert(error.message.toString(), "error", activity)
            // dismiss the progress dialog
            progress.visibility = View.GONE
        }) {
            override fun getParams(): Map<String, String> {
                //posting parameter ke post url
                val params = HashMap<String, String>()

                params["thn"] = thn
                params["bln"] = bln
                params["brg"] = kdbrg
                params["cbg"] = cbg
                params["kota"] = kota
                return params
            }
        }
        AppController.getInstance().addToRequestQueue(strReq, activity.resources.getString(R.string.tag_json_obj))
    }

    private fun setRV(response: String) {
        try {
            val jsonObject = JSONObject(response)
            val result = jsonObject.getJSONArray("result")
            var total = 0.0
            for (i in 0 until result.length()) {
                try {
                    val obj = result.getJSONObject(i)
                    tvNmBrg.text = obj.getString("NmBrg")
                    rowValues.add(RowHeaderModel(obj.getString("KdCust")))
                    addCache = mutableListOf()
                    val xIndex = i+1

                    addCache.add(CellModel("$xIndex"+"1", obj.getString("NmCust")))
                    addCache.add(CellModel("$xIndex"+"2", nf.format(obj.getDouble("Qty"))))

                    cellValues.add(addCache)
                    total = total + obj.getDouble("Qty")
                    //menambah item ke array
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
            rowValues.add(RowHeaderModel("TOTAL"))

            addCache = mutableListOf<CellModel>()
            addCache.add(CellModel("$result.length()", ""))
            addCache.add(CellModel("$result.length()"+"1", nf.format(total)))
            cellValues.add(addCache)

            colValues.add(ColumnHeaderModel("Nama Customer"))
            colValues.add(ColumnHeaderModel("Qty"))

            adapterTable = AdapterTableView(activity.applicationContext)
            table.adapter = adapterTable
            adapterTable.setAllItems(colValues, rowValues, cellValues)
            //adapterTable.notifyDataSetChanged()
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }
}
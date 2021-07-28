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

class DetailPenjBrgCustQtyThn (val activity: Activity,
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
    private val nf = NumberFormat.getInstance(Locale("in", "ID"))
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
        tvTgl.text = "Tahun $thn"

        getData()

        dialog.setPositiveButton("OK") { dialog, _ ->
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
        val urlSelect = a.URL() + "laporan/detail_penjcustbrgqtythn.php"
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
            var totalM1 = 0.0; var totalM2 = 0.0; var totalM3 = 0.0; var totalM4 = 0.0
            var totalM5 = 0.0; var totalM6 = 0.0; var totalM7 = 0.0; var totalM8 = 0.0
            var totalM9 = 0.0; var totalM10 = 0.0; var totalM11 = 0.0; var totalM12 = 0.0
            var totalAll = 0.0
            for (i in 0 until result.length()) {
                try {
                    val obj = result.getJSONObject(i)
                    tvNmBrg.text = obj.getString("NmBrg")
                    rowValues.add(RowHeaderModel(obj.getString("KdCust")))
                    addCache = mutableListOf()
                    val xIndex = i+1

                    addCache.add(CellModel("$xIndex"+"1", obj.getString("NmCust")))
                    addCache.add(CellModel("$xIndex"+"2", nf.format(obj.getDouble("M1"))))
                    totalM1 = totalM1 + obj.getDouble("M1")
                    addCache.add(CellModel("$xIndex"+"3", nf.format(obj.getDouble("M2"))))
                    totalM2 = totalM2 + obj.getDouble("M2")
                    addCache.add(CellModel("$xIndex"+"4", nf.format(obj.getDouble("M3"))))
                    totalM3 = totalM3 + obj.getDouble("M3")
                    addCache.add(CellModel("$xIndex"+"5", nf.format(obj.getDouble("M4"))))
                    totalM4 = totalM4 + obj.getDouble("M4")
                    addCache.add(CellModel("$xIndex"+"6", nf.format(obj.getDouble("M5"))))
                    totalM5 = totalM5 + obj.getDouble("M5")
                    addCache.add(CellModel("$xIndex"+"7", nf.format(obj.getDouble("M6"))))
                    totalM6 = totalM6 + obj.getDouble("M6")
                    addCache.add(CellModel("$xIndex"+"8", nf.format(obj.getDouble("M7"))))
                    totalM7 = totalM7 + obj.getDouble("M7")
                    addCache.add(CellModel("$xIndex"+"9", nf.format(obj.getDouble("M8"))))
                    totalM8 = totalM8 + obj.getDouble("M8")
                    addCache.add(CellModel("$xIndex"+"10", nf.format(obj.getDouble("M9"))))
                    totalM9 = totalM9 + obj.getDouble("M9")
                    addCache.add(CellModel("$xIndex"+"11", nf.format(obj.getDouble("M10"))))
                    totalM10 = totalM10 + obj.getDouble("M10")
                    addCache.add(CellModel("$xIndex"+"12", nf.format(obj.getDouble("M11"))))
                    totalM11 = totalM11 + obj.getDouble("M11")
                    addCache.add(CellModel("$xIndex"+"13", nf.format(obj.getDouble("M12"))))
                    totalM12 = totalM12 + obj.getDouble("M12")
                    var totalRow = obj.getDouble("M1") + obj.getDouble("M2") + obj.getDouble("M3") +
                            obj.getDouble("M4") + obj.getDouble("M5") + obj.getDouble("M6") +
                            obj.getDouble("M7") + obj.getDouble("M8") + obj.getDouble("M9") +
                            obj.getDouble("M10") + obj.getDouble("M11") + obj.getDouble("M12")

                    addCache.add(CellModel("$xIndex"+"14", nf.format(totalRow)))
                    totalAll = totalAll + totalRow

                    cellValues.add(addCache)
                    //menambah item ke array
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
            rowValues.add(RowHeaderModel("TOTAL"))
            addCache = mutableListOf<CellModel>()
            addCache.add(CellModel("$result.length()", ""))
            addCache.add(CellModel("$result.length()"+"2", nf.format(totalM1)))
            addCache.add(CellModel("$result.length()"+"3", nf.format(totalM2)))
            addCache.add(CellModel("$result.length()"+"4", nf.format(totalM3)))
            addCache.add(CellModel("$result.length()"+"5", nf.format(totalM4)))
            addCache.add(CellModel("$result.length()"+"6", nf.format(totalM5)))
            addCache.add(CellModel("$result.length()"+"7", nf.format(totalM6)))
            addCache.add(CellModel("$result.length()"+"8", nf.format(totalM7)))
            addCache.add(CellModel("$result.length()"+"9", nf.format(totalM8)))
            addCache.add(CellModel("$result.length()"+"10", nf.format(totalM9)))
            addCache.add(CellModel("$result.length()"+"11", nf.format(totalM10)))
            addCache.add(CellModel("$result.length()"+"12", nf.format(totalM11)))
            addCache.add(CellModel("$result.length()"+"13", nf.format(totalM12)))
            addCache.add(CellModel("$result.length()"+"14", nf.format(totalAll)))
            cellValues.add(addCache)

            colValues.add(ColumnHeaderModel("Nama Customer"))
            colValues.add(ColumnHeaderModel("Jan"))
            colValues.add(ColumnHeaderModel("Feb"))
            colValues.add(ColumnHeaderModel("Mar"))
            colValues.add(ColumnHeaderModel("Apr"))
            colValues.add(ColumnHeaderModel("Mei"))
            colValues.add(ColumnHeaderModel("Jun"))
            colValues.add(ColumnHeaderModel("Jul"))
            colValues.add(ColumnHeaderModel("Agu"))
            colValues.add(ColumnHeaderModel("Sep"))
            colValues.add(ColumnHeaderModel("Okt"))
            colValues.add(ColumnHeaderModel("Nov"))
            colValues.add(ColumnHeaderModel("Des"))
            colValues.add(ColumnHeaderModel("Total"))

            adapterTable = AdapterTableView(activity.applicationContext)
            table.adapter = adapterTable
            adapterTable.setAllItems(colValues, rowValues, cellValues)
            //adapterTable.notifyDataSetChanged()
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }
}
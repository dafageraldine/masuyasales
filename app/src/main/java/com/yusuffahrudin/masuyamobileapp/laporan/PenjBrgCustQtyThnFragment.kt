package com.yusuffahrudin.masuyamobileapp.laporan

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.AsyncTask
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.evrencoskun.tableview.TableView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.yusuffahrudin.masuyamobileapp.R
import com.yusuffahrudin.masuyamobileapp.adapter.AdapterTableView
import com.yusuffahrudin.masuyamobileapp.controller.AppController
import com.yusuffahrudin.masuyamobileapp.controller.DialogAlert
import com.yusuffahrudin.masuyamobileapp.data.CellModel
import com.yusuffahrudin.masuyamobileapp.data.ColumnHeaderModel
import com.yusuffahrudin.masuyamobileapp.data.RowHeaderModel
import com.yusuffahrudin.masuyamobileapp.util.Server
import com.yusuffahrudin.masuyamobileapp.util.SessionManager
import com.yusuffahrudin.masuyamobileapp.util.YearPickerDialog
import org.jetbrains.anko.doAsync
import org.json.JSONException
import org.json.JSONObject
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class PenjBrgCustQtyThnFragment : Fragment() {

    private lateinit var sessionManager: SessionManager
    private lateinit var tabel: TableView
    private lateinit var tv_filter1: TextView
    private lateinit var tv_filter2: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var pickerDialog: YearPickerDialog
    private var list: MutableList<String> = mutableListOf()
    private lateinit var dialog: BottomSheetDialog
    private lateinit var spin: Spinner
    private lateinit var spinKota: Spinner
    private lateinit var listKota: MutableList<String>
    private lateinit var a: Server
    private lateinit var kotaArray: Array<String>
    private lateinit var adapterKota: ArrayAdapter<String>
    private lateinit var adapterTipe: ArrayAdapter<String>
    private lateinit var adapterTable: AdapterTableView
    private lateinit var userKota: String
    private lateinit var kdkota: String
    private var colValues: MutableList<ColumnHeaderModel> = mutableListOf()
    private var rowValues: MutableList<RowHeaderModel> = mutableListOf()
    private var cellValues: MutableList<MutableList<CellModel>> = mutableListOf()
    private lateinit var addCache: MutableList<CellModel>
    private val nf = NumberFormat.getInstance(Locale("in", "ID"))
    private lateinit var temp : MutableList<String>
    private var thn = ""
    private var cbg = ""
    private var kota = ""
    private lateinit var tipe: String
    private var sdf = SimpleDateFormat("yyyy", Locale.getDefault())
    companion object {
        private val STATE_CELL = "state_cell"
        private val STATE_ROW = "state_row"
        private val STATE_COL = "state_col"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_penj_brg_cust_qty_thn, container, false)

        tabel = rootView.findViewById(R.id.container_table)
        tv_filter1 = rootView.findViewById(R.id.tv_filter1)
        tv_filter2 = rootView.findViewById(R.id.tv_filter2)
        progressBar = rootView.findViewById(R.id.progressbar)
        progressBar.visibility = View.INVISIBLE
        val filter_bar = rootView.findViewById<CardView>(R.id.filter_bar)
        val btn_clear_filter = rootView.findViewById<ImageView>(R.id.btn_clear_filter)

        sessionManager = SessionManager(context)
        val user = sessionManager.userDetails
        kdkota = user[SessionManager.kdkota].toString()
        userKota = user[SessionManager.kota].toString()

        listKota = mutableListOf()
        temp = mutableListOf()

        a = Server(kdkota)

        filter_bar.setOnClickListener{
            if(thn.equals("")){
                thn = sdf.format(Date())
            }
            dialogFilter()
        }
        btn_clear_filter.setOnClickListener {
            tv_filter1.text = getString(R.string.all_tipe_barang)
            tv_filter2.text = getString(R.string.filter)
        }

        if (savedInstanceState != null) {
            cellValues = savedInstanceState.getParcelableArrayList<CellModel>(STATE_CELL) as MutableList<MutableList<CellModel>>
            rowValues = savedInstanceState.getParcelableArrayList<RowHeaderModel>(STATE_ROW) as MutableList<RowHeaderModel>
            colValues = savedInstanceState.getParcelableArrayList<ColumnHeaderModel>(STATE_COL) as MutableList<ColumnHeaderModel>
            adapterTable = AdapterTableView(requireContext())
            tabel.adapter = adapterTable
            adapterTable.setAllItems(colValues, rowValues, cellValues)
        }

        tabel.tableViewListener = PenjBrgCustQtyThnTableListener(this.requireActivity(), colValues, rowValues, temp)

        return rootView
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelableArrayList(STATE_CELL, cellValues as ArrayList<out Parcelable>)
        outState.putParcelableArrayList(STATE_ROW, rowValues as ArrayList<out Parcelable>)
        outState.putParcelableArrayList(STATE_COL, colValues as ArrayList<out Parcelable>)
    }

    /*fun isConnected(): Boolean {

        val koneksi: ConnectivityManager = activity?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val nInfo: NetworkInfo = koneksi.activeNetworkInfo!!
        return (nInfo.isConnected)

        return false
    }*/

    private fun getTipeBrg() {
        progressBar.visibility = View.VISIBLE
        doAsync {
            val urlSelectKota = a.URL() + "tools/select_tipe_brg.php"

            val strReq = StringRequest(Request.Method.POST, urlSelectKota, Response.Listener<String> { response ->
                Log.d(::PenjBrgCustQtyThnFragment.name, "Response : $response")
                try {
                    val jsonObject = JSONObject(response)
                    val result = jsonObject.getJSONArray("result")
                    list.clear()
                    for (i in 0 until result.length()) {
                        try {
                            val obj = result.getJSONObject(i)
                            //menambah item ke array
                            val item = obj.getString("NmType").trim()
                            list.add(item)
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    }
                    populateSpinner()
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }, Response.ErrorListener { error -> Log.e(::PenjBrgCustQtyThnFragment.name, "Error : " + error.message) })

            AppController.getInstance().addToRequestQueue(strReq, resources.getString(R.string.tag_json_obj))
        }
        progressBar.visibility = View.GONE
    }

    private fun populateSpinner() {
        // Creating adapter for spinner
        val tipeBrgArray = list.toTypedArray()
        adapterTipe = ArrayAdapter(requireContext(), R.layout.spinner_black, tipeBrgArray)
        spin.adapter = adapterTipe
    }


    // untuk menampilkan dialog password
    @SuppressLint("InflateParams")
    private fun dialogFilter() {
        temp.clear()
        dialog = BottomSheetDialog(requireContext(), R.style.SheetDialog)
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_filter_penj_brg_qty_thn, null)
        dialog.setContentView(dialogView)

        val tahun = dialogView.findViewById<TextView>(R.id.tv_thn)
        val search = dialogView.findViewById<Button>(R.id.btn_search)
        val cancel = dialogView.findViewById<Button>(R.id.btn_cancel)
        spin = dialogView.findViewById(R.id.spin_tipebrg)
        spinKota = dialogView.findViewById(R.id.spin_kota)
        val rCabang = dialogView.findViewById<RadioButton>(R.id.radio_cabang)
        val rNon = dialogView.findViewById<RadioButton>(R.id.radio_non)
        val rSemua = dialogView.findViewById<RadioButton>(R.id.radio_semua)
        adapterTipe = ArrayAdapter(requireContext(), R.layout.spinner_black)
        adapterKota = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item)

        getTipeBrg()
        tahun.text = thn

        //mengisi spinner kota
        when {
            userKota.equals("ALL", ignoreCase = true) -> GetKota().execute()
            userKota.equals("MLG", ignoreCase = true) -> {
                kotaArray = arrayOf("MLG", "SBY")
                adapterKota = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, kotaArray)
                spinKota.adapter = adapterKota
            }
            else -> {
                kotaArray = arrayOf(userKota)
                adapterKota = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, kotaArray)
                spinKota.adapter = adapterKota
            }
        }

        tahun.setOnClickListener {
            pickerDialog = YearPickerDialog()
            pickerDialog.setListener { _, year, _, _ -> tahun.text = year.toString() }
            pickerDialog.show(activity!!.supportFragmentManager, "MonthYearPickerDialog") }

        search.setOnClickListener {
            var status = ""
            when {
                rCabang.isChecked -> {
                    cbg = "1"
                    status = "cabang"
                }
                rNon.isChecked -> {
                    cbg = "0"
                    status = "non cabang"
                }
                rSemua.isChecked -> {
                    cbg = "2"
                    status = "semua"
                }
            }
            thn = tahun.text.toString()
            tipe = spin.selectedItem.toString()
            kota = spinKota.selectedItem.toString()
            temp.add(thn)
            temp.add(kota)
            temp.add(cbg)

            status = "$thn - $status"
            tv_filter1.text = tipe
            tv_filter2.text = status

            getReport()

            dialog.dismiss()
        }

        cancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    @SuppressLint("StaticFieldLeak")
    private inner class GetKota : AsyncTask<Void, Void, Void>() {
        override fun doInBackground(vararg arg0: Void): Void? {
            val urlSelectKota = a.URL() + "tools/select_kota.php"

            val strReq = StringRequest(Request.Method.POST, urlSelectKota, Response.Listener { response ->
                Log.d(::PenjBrgCustQtyThnFragment.name, "Response : $response")
                try {
                    val jsonObject = JSONObject(response)
                    val result = jsonObject.getJSONArray("result")
                    listKota.clear()
                    for (i in 0 until result.length()) {
                        try {
                            val obj = result.getJSONObject(i)
                            //menambah item ke array
                            listKota.add(obj.getString("KdKota"))
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    }
                    populateSpinnerKota()
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }, Response.ErrorListener { error -> Log.e(::PenjBrgCustQtyThnFragment.name, "Error : " + error.message) })

            AppController.getInstance().addToRequestQueue(strReq, resources.getString(R.string.tag_json_obj))

            return null
        }
    }

    private fun populateSpinnerKota() {
        // Creating adapter for spinner
        kotaArray = listKota.toTypedArray()
        adapterKota = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, kotaArray)
        spinKota.adapter = adapterKota
    }

    //fungsi untuk select data dari database
    private fun getReport() {
        colValues.clear()
        rowValues.clear()
        cellValues.clear()

        val a = Server(kdkota)
        val urlSelect = a.URL() + "laporan/penjcustbrgqtythn.php"
        progressBar.visibility = View.VISIBLE

        val strReq = object : StringRequest(Method.POST, urlSelect, Response.Listener { response ->
            Log.d(this.tag, "Response : $response")
            setRV(response)
            // dismiss the progress dialog
            progressBar.visibility = View.GONE
        }, Response.ErrorListener { error ->
            DialogAlert(error.message.toString(), "error", this.requireActivity())
            // dismiss the progress dialog
            progressBar.visibility = View.GONE
        }) {
            override fun getParams(): Map<String, String> {
                //posting parameter ke post url
                val params = HashMap<String, String>()

                params["thn"] = thn
                params["type"] = tipe
                params["cbg"] = cbg
                params["kota"] = kota
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
            var totalM1 = 0.0; var totalM2 = 0.0; var totalM3 = 0.0; var totalM4 = 0.0
            var totalM5 = 0.0; var totalM6 = 0.0; var totalM7 = 0.0; var totalM8 = 0.0
            var totalM9 = 0.0; var totalM10 = 0.0; var totalM11 = 0.0; var totalM12 = 0.0
            var totalAll = 0.0
            for (i in 0 until result.length()) {
                try {
                    val obj = result.getJSONObject(i)

                    rowValues.add(RowHeaderModel(obj.getString("KdBrg")))
                    addCache = mutableListOf()
                    val xIndex = i+1

                    addCache.add(CellModel("$xIndex"+"1", obj.getString("NmBrg")))
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
                    /*val totalItem = obj.getDouble("M1")+obj.getDouble("M2")+obj.getDouble("M3")+obj.getDouble("M4")+
                            obj.getDouble("M5")+obj.getDouble("M6")+obj.getDouble("M7")+obj.getDouble("M8")+
                            obj.getDouble("M9")+obj.getDouble("M10")+obj.getDouble("M11")+obj.getDouble("M12")
                    */
                    addCache.add(CellModel("$xIndex"+"14", nf.format(obj.getDouble("TOTAL"))))
                    totalAll = totalAll + obj.getDouble("TOTAL")

                    cellValues.add(addCache)
                    //menambah item ke array
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
            rowValues.add(RowHeaderModel("TOTAL"))
            addCache = mutableListOf<CellModel>()
            addCache.add(CellModel("$result.length()"+"1", ""))
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
            colValues.add(ColumnHeaderModel("NamaBrg"))
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

            adapterTable = AdapterTableView(activity!!.applicationContext)
            tabel.adapter = adapterTable
            adapterTable.setAllItems(colValues, rowValues, cellValues)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

}

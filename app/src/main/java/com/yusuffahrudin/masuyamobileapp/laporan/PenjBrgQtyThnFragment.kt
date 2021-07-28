package com.yusuffahrudin.masuyamobileapp.laporan

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.github.barteksc.pdfviewer.PDFView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.yusuffahrudin.masuyamobileapp.R
import com.yusuffahrudin.masuyamobileapp.controller.AppController
import com.yusuffahrudin.masuyamobileapp.controller.DialogAlert
import com.yusuffahrudin.masuyamobileapp.util.Server
import com.yusuffahrudin.masuyamobileapp.util.SessionManager
import com.yusuffahrudin.masuyamobileapp.util.YearPickerDialog
import org.jetbrains.anko.doAsync
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class PenjBrgQtyThnFragment : Fragment() {

    private lateinit var sessionManager: SessionManager
    private lateinit var mWebView: PDFView
    private lateinit var progressBar: ProgressBar
    private lateinit var downloadManager: DownloadManager
    private lateinit var pickerDialog: YearPickerDialog
    private lateinit var tvFilter1: TextView
    private lateinit var tvFilter2: TextView
    private var list: MutableList<String> = mutableListOf()
    private var reference: Long? = null
    private var pdf: File? = null
    private lateinit var dialog: BottomSheetDialog
    private lateinit var spin: Spinner
    private lateinit var spinKota: Spinner
    private lateinit var url: String
    private lateinit var listKota: MutableList<String>
    private lateinit var a: Server
    private lateinit var kotaArray: Array<String>
    private lateinit var adapterKota: ArrayAdapter<String>
    private lateinit var adapterTipe: ArrayAdapter<String>
    private lateinit var userKota: String
    private var thn = ""
    private var tipe = ""
    private var cbg = ""
    private var kota = ""
    private var sdf = SimpleDateFormat("yyyy", Locale.getDefault())

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_penj_brg_thn_qty, container, false)

        mWebView = rootView.findViewById(R.id.container_pdf)
        progressBar = rootView.findViewById(R.id.progressbar)
        progressBar.visibility = View.INVISIBLE
        tvFilter1 = rootView.findViewById(R.id.tv_filter1)
        tvFilter2 = rootView.findViewById(R.id.tv_filter2)
        val filter_bar = rootView.findViewById<CardView>(R.id.filter_bar)
        val btn_clear_filter = rootView.findViewById<ImageView>(R.id.btn_clear_filter)
        val btn_open_pdf = rootView.findViewById<ImageView>(R.id.btn_open_pdf)

        sessionManager = SessionManager(context)
        val user = sessionManager.userDetails
        val kdkota = user[SessionManager.kdkota]
        userKota = user[SessionManager.kota].toString()

        listKota = mutableListOf()

        a = Server(kdkota)
        url = a.URL() + "fpdfcreatepdf/penj_brg_qty_tahun.php"

        filter_bar.setOnClickListener{
            if(thn.equals("")){
                thn = sdf.format(Date())
            }
            dialogFilter()
        }
        btn_clear_filter.setOnClickListener {
            tvFilter1.text = "All Tipe Barang"
            tvFilter2.text = "Filter"
            tipe = ""
            cbg = ""
            kota = ""
        }
        btn_open_pdf.setOnClickListener {
            if(pdf == null){
                DialogAlert("No existing report to export PDF", "error", activity!!.parent)
            } else {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.setDataAndType(Uri.fromFile(pdf), "application/pdf")
                intent.flags = Intent.FLAG_ACTIVITY_NO_HISTORY
                startActivity(intent)
            }
        }

        activity?.registerReceiver(onDownloadComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))

        return rootView
    }

    private fun downloadPDF(url: String, thn: String, tipe: String){
        progressBar.visibility = View.VISIBLE
        downloadManager = activity?.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val uri = Uri.parse(url)

        pdf = File("${requireContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)?.absolutePath}/Laporan_Penjualan_${tipe}_Tahun_$thn.pdf")
        println("--------------------------- pdf ${pdf?.absolutePath}")
        //cek file
        if(pdf!!.exists()){
            pdf?.delete()
        }

        //show notification
        val request = DownloadManager.Request(uri)
        request.setTitle("Laporan Penjualan ${tipe} Tahun $thn")
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
        request.setDestinationInExternalFilesDir(requireContext(), Environment.DIRECTORY_DOCUMENTS, "Laporan_Penjualan_${tipe}_Tahun_$thn.pdf")
        //request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        reference = downloadManager.enqueue(request)
    }

    private val onDownloadComplete = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            //Checking if the received broadcast is for our enqueued download by matching download id
            if (reference == id) {
                progressBar.visibility = View.GONE
                mWebView.fromFile(pdf).spacing(10).load()
            }
        }
    }

    private fun getTipeBrg() {
        progressBar.visibility = View.VISIBLE
        doAsync {
            val urlSelectKota = a.URL() + "tools/select_tipe_brg.php"

            val strReq = StringRequest(Request.Method.POST, urlSelectKota, { response ->
                Log.d(::PenjBrgQtyThnFragment.name, "Response : $response")
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
            }, { error -> Log.e(::PenjBrgQtyThnFragment.name, "Error : " + error.message) })

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
    private fun dialogFilter() {
        dialog = BottomSheetDialog(requireContext(), R.style.SheetDialog)
        val dialogView = LayoutInflater.from(activity).inflate(R.layout.dialog_filter_penj_brg_qty_thn, null)
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
        var spinnerPosition = adapterTipe.getPosition(tipe)
        spin.setSelection(spinnerPosition)
        if (cbg.equals("0")){
            rNon.isChecked = true
        } else if (cbg.equals("1")){
            rCabang.isChecked = true
        } else {
            rSemua.isChecked = true
        }
        spinnerPosition = adapterKota.getPosition(kota)
        spinKota.setSelection(spinnerPosition)

        //mengisi spinner kota
        when {
            userKota.equals("ALL", ignoreCase = true) -> GetKota()
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

            status = thn+" - "+status
            println(status)
            tvFilter1.text = tipe
            tvFilter2.text = status

            val tipebrg = tipe.replace(" ","%20")
            url = url + "?thn="+thn+"&type="+tipebrg+"&cbg="+cbg+"&kota="+kota
            downloadPDF(url, thn, tipebrg)
            dialog.dismiss()
        }

        cancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun GetKota() {
        doAsync {
            val urlSelectKota = a.URL() + "tools/select_kota.php"
            val strReq = StringRequest(Request.Method.POST, urlSelectKota, { response ->
                Log.d(::PenjBrgQtyThnFragment.name, "Response : $response")
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
            }, { error -> Log.e(::PenjBrgQtyThnFragment.name, "Error : " + error.message) })
            AppController.getInstance().addToRequestQueue(strReq, resources.getString(R.string.tag_json_obj))
        }
    }

    private fun populateSpinnerKota() {
        // Creating adapter for spinner
        kotaArray = listKota.toTypedArray()
        adapterKota = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, kotaArray)
        spinKota.adapter = adapterKota
    }
}

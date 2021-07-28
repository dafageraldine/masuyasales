package com.yusuffahrudin.masuyamobileapp.informasibarang

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.anychart.AnyChart
import com.anychart.AnyChartView
import com.anychart.chart.common.dataentry.DataEntry
import com.anychart.chart.common.dataentry.ValueDataEntry
import com.anychart.chart.common.listener.Event
import com.anychart.chart.common.listener.ListenersInterface
import com.anychart.charts.Cartesian
import com.anychart.core.cartesian.series.Column
import com.anychart.core.cartesian.series.Line
import com.anychart.core.ui.Title
import com.anychart.data.Mapping
import com.anychart.data.Set
import com.anychart.enums.Anchor
import com.anychart.enums.HoverMode
import com.anychart.enums.MarkerType
import com.anychart.enums.TooltipPositionMode
import com.anychart.graphics.vector.Stroke
import com.dev.materialspinner.MaterialSpinner
import com.evrencoskun.tableview.TableView
import com.evrencoskun.tableview.listener.ITableViewListener
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.yusuffahrudin.masuyamobileapp.R
import com.yusuffahrudin.masuyamobileapp.adapter.AdapterTableView
import com.yusuffahrudin.masuyamobileapp.api.API
import com.yusuffahrudin.masuyamobileapp.api.ApiService
import com.yusuffahrudin.masuyamobileapp.controller.DialogAlert
import com.yusuffahrudin.masuyamobileapp.data.CellModel
import com.yusuffahrudin.masuyamobileapp.data.ColumnHeaderModel
import com.yusuffahrudin.masuyamobileapp.data.RowHeaderModel
import com.yusuffahrudin.masuyamobileapp.data.evaluasi_barang.*
import com.yusuffahrudin.masuyamobileapp.util.SessionManager
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.Objects.requireNonNull
import kotlin.collections.ArrayList

class EvaluasiBarangFragment : Fragment() {
    private lateinit var kdbrg: String
    private lateinit var progressBar: ProgressBar
    private lateinit var chartBar: AnyChartView
    private lateinit var spinKota: MaterialSpinner
    private lateinit var rbSemua: RadioButton
    private lateinit var rbNon: RadioButton
    private lateinit var rbCabang: RadioButton
    private lateinit var btnSearch: Button
    private lateinit var btnFilter: FloatingActionButton
    private lateinit var btnBarToLine: FloatingActionButton
    private lateinit var btnLossCust: FloatingActionButton
    private lateinit var cartesianBar: Cartesian
    private lateinit var columnBar: Column
    private lateinit var titleBar: Title
    private var list = ArrayList<DataEntry>()
    private var listLine = ArrayList<DataEntry>()
    private lateinit var adapterKota: ArrayAdapter<String>
    private lateinit var listKota: MutableList<String>
    private lateinit var sessionManager: SessionManager
    private var cbg = 0
    private var kota = ""
    private lateinit var userKota: String
    private var filter = false
    private var isViewDialogPerThn = false
    private var isViewDialogDetail = false
    private var isViewDialogLoss = false
    private val nf = NumberFormat.getInstance(Locale("in", "ID"))
    private val colValues: MutableList<ColumnHeaderModel> = mutableListOf()
    private val rowValues: MutableList<RowHeaderModel> = mutableListOf()
    private val cellValues: MutableList<MutableList<CellModel>> = mutableListOf()
    private lateinit var addCache: MutableList<CellModel>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_evaluasi_barang, container, false)

        val i = this.activity!!.intent
        kdbrg = i.extras?.getString(ListBarangActivity.KDBRG).toString()
        sessionManager = SessionManager(requireContext())
        val cache = sessionManager.userDetails
        kota = cache[SessionManager.kdkota].toString()
        userKota = cache[SessionManager.kota].toString()

        progressBar = view.findViewById(R.id.progressbar)
        chartBar = view.findViewById(R.id.chart_bar)
        btnFilter = view.findViewById(R.id.btn_filter)
        btnBarToLine = view.findViewById(R.id.btn_bar_to_line)
        btnLossCust = view.findViewById(R.id.btn_loss_cust)
        progressBar.visibility = View.GONE

        getDataBar()

        btnFilter.setOnClickListener{
            bottomFilter()
        }

        btnBarToLine.setOnClickListener {
            getDataLine()
        }

        btnLossCust.setOnClickListener {
            getDataTableLoss()
        }

        return view
    }

    private fun bottomFilter(){
        val dialogFilter = BottomSheetDialog(requireContext(), R.style.SheetDialog)
        val bottomLayout = this.layoutInflater.inflate(R.layout.dialog_filter_evaluasi_brg, null)
        val slideUp = AnimationUtils.loadAnimation(requireContext(), R.anim.show_from_bottom)
        dialogFilter.setContentView(bottomLayout)

        spinKota = bottomLayout.findViewById(R.id.spin_kota)
        rbSemua = bottomLayout.findViewById(R.id.radio_semua)
        rbNon = bottomLayout.findViewById(R.id.radio_non)
        rbCabang = bottomLayout.findViewById(R.id.radio_cabang)
        btnSearch = bottomLayout.findViewById(R.id.btn_search)
        adapterKota = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item)

        //mengisi spinner kota
        when {
            userKota.equals("ALL", ignoreCase = true) -> {
                listKota = mutableListOf()
                getKota()
            }
            userKota.equals("MLG", ignoreCase = true) -> {
                listKota = mutableListOf("MLG", "SBY")
                adapterKota = ArrayAdapter(requireNonNull(requireContext()), android.R.layout.simple_spinner_dropdown_item, listKota)
                spinKota.setAdapter(adapterKota)
            }
            else -> {
                listKota = mutableListOf(userKota)
                adapterKota = ArrayAdapter(requireNonNull(requireContext()), android.R.layout.simple_spinner_dropdown_item, listKota)
                spinKota.setAdapter(adapterKota)
            }
        }

        btnSearch.setOnClickListener {
            filter = true
            when {
                rbCabang.isChecked -> {
                    cbg = 1
                }
                rbNon.isChecked -> {
                    cbg = 0
                }
                rbSemua.isChecked -> {
                    cbg = 2
                }
            }
            kota = spinKota.getSpinner().selectedItem.toString()

            getDataBar()
            dialogFilter.dismiss()
        }

        bottomLayout.startAnimation(slideUp)
        dialogFilter.show()
    }

    private fun getKota(){
        progressBar.visibility = View.VISIBLE
        doAsync {
            listKota = mutableListOf()
            API.instance().create(ApiService::class.java)
                    .kota
                    ?.enqueue(object: Callback<KotaResponse?>{
                        override fun onFailure(call: Call<KotaResponse?>, e: Throwable) {
                            Log.d("Get Kota", "Error $e")
                            FirebaseCrashlytics.getInstance().recordException(e)
                            DialogAlert("${getString(R.string.error_pengambilan_data)} ${e.message}", "error", requireActivity())
                        }

                        override fun onResponse(call: Call<KotaResponse?>, response: Response<KotaResponse?>) {
                            if(response.code() == 200) {
                                for (i in 0 until response.body()?.result!!.size){
                                    listKota.add(response.body()?.result!![i].kdkota.toString())
                                }
                                adapterKota = ArrayAdapter(requireContext(), R.layout.spinner_black, listKota)
                                spinKota.setAdapter(adapterKota)
                            }
                        }

                    })

            uiThread {
                progressBar.visibility = View.GONE
            }
        }
    }

    private fun getDataBar(){
        progressBar.visibility = View.VISIBLE
        doAsync {
            list.clear()
            API.instance().create(ApiService::class.java)
                    .getEvaluasiBrgBar(kdbrg, kota, cbg)
                    ?.enqueue(object: Callback<EvaluasiBarangResponse?> {
                        override fun onFailure(call: Call<EvaluasiBarangResponse?>, e: Throwable) {
                            Log.d("Get Evaluasi BarChart", "Error $e")
                            FirebaseCrashlytics.getInstance().recordException(e)
                            DialogAlert("${getString(R.string.error_pengambilan_data)} ${e.message}", "error", activity!!.parent)
                        }

                        override fun onResponse(call: Call<EvaluasiBarangResponse?>, response: Response<EvaluasiBarangResponse?>) {
                            if(response.code() == 200) {
                                for (i in 0 until response.body()?.result!!.size){
                                    list.add(ValueDataEntry(response.body()?.result!![i].tahun, response.body()?.result!![i].qty))
                                }

                                uiThread {
                                    if (filter){
                                        titleBar.text(
                                                "Evaluasi barang $kdbrg"+
                                                        "<br><a style=\"color:#0000FF; font-size: 10px;\">"+
                                                        "${kota.toUpperCase(Locale.getDefault())} </a>"
                                        )
                                        cartesianBar.data(list)
                                        filter = false
                                    } else {
                                        drawChartBar(list)
                                    }
                                    progressBar.visibility = View.GONE
                                }
                            }
                        }
                    })
        }
    }

    private fun drawChartBar(data: ArrayList<DataEntry>) {
        cartesianBar = AnyChart.column()
        cartesianBar.animation(true)
        cartesianBar.title(true)
        titleBar = cartesianBar.title()
        titleBar.useHtml(true)
        titleBar.text(
                "Evaluasi barang $kdbrg"+
                        "<br><a style=\"color:#0000FF; font-size: 10px;\">"+
                        "${kota.toUpperCase(Locale.getDefault())} </a>"
        )
        titleBar.fontColor("#0000FF")

        columnBar = cartesianBar.column(data)
        columnBar.tooltip(false)

        columnBar.labels(true)
        columnBar.labels().format("{%Value}{groupsSeparator:.}")
        columnBar.labels().fontColor("#0000FF")

        cartesianBar.yScale().minimum(0.0)
        cartesianBar.yAxis(0).labels().format("{%Value}{groupsSeparator:.}")
        cartesianBar.yAxis(0).labels().rotation(-90)

        cartesianBar.tooltip().positionMode(TooltipPositionMode.POINT)
        cartesianBar.interactivity().hoverMode(HoverMode.BY_X)

        cartesianBar.xAxis(0).title("Tahun")
        cartesianBar.yAxis(0).title("Qty penjualan")

        cartesianBar.setOnClickListener(object : ListenersInterface.OnClickListener(arrayOf("x", "value")) {
            override fun onClick(event: Event) {
                println("---------------------------- click bar")
                getDataPerThn(event.data["x"].toString())
            }
        })

        chartBar.setChart(cartesianBar)
    }

    private fun getDataLine(){
        doAsync {
            listLine.clear()
            API.instance().create(ApiService::class.java)
                    .getEvaluasiBrgLine(kdbrg, kota, cbg)
                    ?.enqueue(object: Callback<EvaluasiBrgLineChartResponse?> {
                        override fun onFailure(call: Call<EvaluasiBrgLineChartResponse?>, e: Throwable) {
                            Log.d("Get Evaluasi DataLine", "Error $e")
                            FirebaseCrashlytics.getInstance().recordException(e)
                            DialogAlert("${getString(R.string.error_pengambilan_data)} ${e.message}", "error", activity!!.parent)
                        }

                        override fun onResponse(call: Call<EvaluasiBrgLineChartResponse?>, responseBrg: Response<EvaluasiBrgLineChartResponse?>) {
                            if(responseBrg.code() == 200) {
                                for (i in 0 until responseBrg.body()?.result!!.size){
                                    listLine.add(CustomDataEntry(responseBrg.body()?.result!![i].bulan, responseBrg.body()?.result!![i].qtyThn1, responseBrg.body()?.result!![i].qtyThn2, responseBrg.body()?.result!![i].qtyThn3))
                                }

                                uiThread {
                                    bottomLineChart()
                                }
                            }
                        }
                    })
        }
    }

    private fun bottomLineChart(){
        if (listLine.isNotEmpty()){
            val year = Calendar.getInstance().get(Calendar.YEAR)
            val dialogLineChart = BottomSheetDialog(requireContext(), R.style.SheetDialog)
            val bottomSheetLayout = layoutInflater.inflate(R.layout.dialog_evaluasi_linechart, null)
            dialogLineChart.setContentView(bottomSheetLayout)
            val behavior = BottomSheetBehavior.from(dialogLineChart.findViewById(R.id.bottomSheetEvaluasiLinechart)!!)
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            val slideUp = AnimationUtils.loadAnimation(requireContext(), R.anim.show_from_bottom)
            bottomSheetLayout.startAnimation(slideUp)
            dialogLineChart.show()

            val progressBar = bottomSheetLayout.findViewById<ProgressBar>(R.id.progressbar)
            progressBar?.visibility = View.VISIBLE
            val chartLine = bottomSheetLayout.findViewById<AnyChartView>(R.id.chart_line)

            val cartesianLine = AnyChart.line()
            cartesianLine.animation(true)
            cartesianLine.padding(10.0, 20.0, 5.0, 20.0)
            cartesianLine.crosshair().enabled(true)
            cartesianLine.crosshair()
                    .yLabel(true)
                    .yStroke(null as Stroke?, null, null, null as String?, null as String?)
            cartesianLine.tooltip().positionMode(TooltipPositionMode.POINT)
            cartesianLine.title(true)
            val titleLine = cartesianLine.title()
            titleLine.useHtml(true)
            titleLine.text(
                    "Evaluasi barang $kdbrg"+
                            "<br><a style=\"color:#0000FF; font-size: 10px;\">"+
                            "${kota.toUpperCase(Locale.getDefault())} </a>"
            )
            titleLine.fontColor("#0000FF")
            cartesianLine.yAxis(0).title("Qty penjualan")
            cartesianLine.yAxis(0).labels().rotation(-90)
            cartesianLine.xAxis(0).labels().padding(5.0, 5.0, 5.0, 5.0)

            val setLine = Set.instantiate()
            setLine.data(listLine)

            val series1Mapping: Mapping = setLine.mapAs("{ x: 'x', value: 'value' }")
            val series2Mapping: Mapping = setLine.mapAs("{ x: 'x', value: 'value2' }")
            val series3Mapping: Mapping = setLine.mapAs("{ x: 'x', value: 'value3' }")

            val series1: Line = cartesianLine.line(series1Mapping)
            series1.name((year-2).toString())
            series1.hovered().markers().enabled(true)
            series1.hovered().markers()
                    .type(MarkerType.CIRCLE)
                    .size(4.0)
            series1.tooltip()
                    .position("right")
                    .anchor(Anchor.LEFT_CENTER)
                    .offsetX(5.0)
                    .offsetY(5.0)
            series1.stroke("green")

            val series2: Line = cartesianLine.line(series2Mapping)
            series2.name((year-1).toString())
            series2.hovered().markers().enabled(true)
            series2.hovered().markers()
                    .type(MarkerType.CIRCLE)
                    .size(4.0)
            series2.tooltip()
                    .position("right")
                    .anchor(Anchor.LEFT_CENTER)
                    .offsetX(5.0)
                    .offsetY(5.0)

            val series3: Line = cartesianLine.line(series3Mapping)
            series3.name(year.toString())
            series3.hovered().markers().enabled(true)
            series3.hovered().markers()
                    .type(MarkerType.CIRCLE)
                    .size(4.0)
            series3.tooltip()
                    .position("right")
                    .anchor(Anchor.LEFT_CENTER)
                    .offsetX(5.0)
                    .offsetY(5.0)

            cartesianLine.legend().enabled(true)
            cartesianLine.legend().fontSize(10.0)
            cartesianLine.legend().padding(0.0, 0.0, 10.0, 0.0)

            progressBar?.visibility = View.GONE
            chartLine?.setChart(cartesianLine)
        }
    }

    private fun getDataPerThn(tahun: String){
        doAsync {
            println("---------------------------- get data perthn")
            list.clear()
            API.instance().create(ApiService::class.java)
                    .getEvaluasiBrgPerthn(kdbrg, kota, cbg, tahun)
                    ?.enqueue(object: Callback<EvaluasiBarangResponse?> {
                        override fun onFailure(call: Call<EvaluasiBarangResponse?>, e: Throwable) {
                            Log.d("Get Evaluasi Per Tahun", "Error $e")
                            FirebaseCrashlytics.getInstance().recordException(e)
                            DialogAlert("${getString(R.string.error_pengambilan_data)} ${e.message}", "error", activity!!.parent)
                        }

                        override fun onResponse(call: Call<EvaluasiBarangResponse?>, response: Response<EvaluasiBarangResponse?>) {
                            if(response.code() == 200) {
                                for (i in 0 until response.body()?.result!!.size){
                                    list.add(ValueDataEntry(response.body()?.result!![i].bulan, response.body()?.result!![i].qty))
                                }

                                uiThread {
                                    if (!isViewDialogPerThn){
                                        dialogPertahun(tahun)
                                    }
                                }
                            }
                        }
                    })
        }
    }

    private fun dialogPertahun(tahun: String){
        if (list.isNotEmpty()){
            println("---------------------------- show dialog perthn")
            isViewDialogPerThn = true
            val dialog = BottomSheetDialog(requireActivity(), R.style.SheetDialogNotif)
            val bottomSheetLayout = layoutInflater.inflate(R.layout.dialog_evaluasi_pertahun, null)
            dialog.setContentView(bottomSheetLayout)
            val behavior = BottomSheetBehavior.from(dialog.findViewById(R.id.bottomSheetEvaluasiPertahun)!!)
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            dialog.show()

            val progressBar = dialog.findViewById<ProgressBar>(R.id.progressbar)
            progressBar?.visibility = View.VISIBLE
            val chartPerthn = dialog.findViewById<AnyChartView>(R.id.chart_perthn)

            val cartesianPerthn = AnyChart.column()
            cartesianPerthn.animation(true)
            cartesianPerthn.title(true)
            val titlePerthn = cartesianPerthn.title()
            titlePerthn.useHtml(true)
            titlePerthn.text(
                    "Evaluasi barang $kdbrg"+
                            "<br><a style=\"color:#0000FF; font-size: 10px;\">"+
                            "Tahun $tahun, ${kota.toUpperCase(Locale.getDefault())} </a>"
            )
            titlePerthn.fontColor("#0000FF")

            val columnPerthn = cartesianPerthn.column(list)
            columnPerthn.tooltip(false)

            columnPerthn.labels(true)
            columnPerthn.labels().format("{%Value}{groupsSeparator:.}")
            columnPerthn.labels().fontColor("#0000FF")
            columnPerthn.labels().rotation(-45)

            cartesianPerthn.yScale().minimum(0.0)
            cartesianPerthn.yAxis(0).labels().format("{%Value}{groupsSeparator:.}")
            cartesianPerthn.yAxis(0).labels().rotation(-90)

            cartesianPerthn.tooltip().positionMode(TooltipPositionMode.POINT)
            cartesianPerthn.interactivity().hoverMode(HoverMode.BY_X)

            cartesianPerthn.xAxis(0).title("Bulan")
            cartesianPerthn.yAxis(0).title("Qty penjualan")

            cartesianPerthn.setOnClickListener(object : ListenersInterface.OnClickListener(arrayOf("x", "value")) {
                override fun onClick(event: Event) {
                    println("---------------------------- click perthn")
                    getDataTableDetail(tahun, event.data["x"].toString())
                }
            })

            progressBar?.visibility = View.GONE
            chartPerthn?.setChart(cartesianPerthn)

            dialog.setOnDismissListener {
                isViewDialogPerThn = false
                getDataBar()
                println("---------------------------- dialog perthn dismiss")
            }
        }
    }

    private class CustomDataEntry(x: String?, value: Double?, value2: Double?, value3: Double?) : ValueDataEntry(x, value) {
        init {
            setValue(x, value)
            setValue("value2", value2)
            setValue("value3", value3)
        }
    }

    private fun getDataTableDetail(tahun: String, bulan: String){
        doAsync {
            println("---------------------------- get data detail")
            colValues.clear()
            rowValues.clear()
            cellValues.clear()
            API.instance().create(ApiService::class.java)
                    .getEvaluasiBrgDetail(kdbrg, kota, cbg, tahun, bulan)
                    ?.enqueue(object: Callback<EvaluasiBrgDetailResponse?> {
                        override fun onFailure(call: Call<EvaluasiBrgDetailResponse?>, e: Throwable) {
                            Log.d("Get Evaluasi Detail", "Error $e")
                            FirebaseCrashlytics.getInstance().recordException(e)
                            DialogAlert("${getString(R.string.error_pengambilan_data)} ${e.message}", "error", activity!!.parent)
                        }

                        override fun onResponse(call: Call<EvaluasiBrgDetailResponse?>, responseBrg: Response<EvaluasiBrgDetailResponse?>) {
                            if(responseBrg.code() == 200) {
                                var totalQty = 0.0; var totalJumlah = 0.0
                                for (i in 0 until responseBrg.body()?.result!!.size){
                                    rowValues.add(RowHeaderModel(responseBrg.body()?.result!![i].kdCust!!))
                                    addCache = mutableListOf()
                                    val xIndex = i+1

                                    addCache.add(CellModel("$xIndex"+"1", responseBrg.body()?.result!![i].nmCust!!))
                                    addCache.add(CellModel("$xIndex"+"2", nf.format(responseBrg.body()?.result!![i].qty)))
                                    addCache.add(CellModel("$xIndex"+"3", nf.format(responseBrg.body()?.result!![i].jumlah)))

                                    cellValues.add(addCache)
                                    totalQty += responseBrg.body()?.result!![i].qty!!
                                    totalJumlah += responseBrg.body()?.result!![i].jumlah!!
                                }
                                rowValues.add(RowHeaderModel("TOTAL"))

                                addCache = mutableListOf()
                                addCache.add(CellModel("${responseBrg.body()?.result!!.size}", ""))
                                addCache.add(CellModel("${responseBrg.body()?.result!!.size}"+"1", nf.format(totalQty)))
                                addCache.add(CellModel("${responseBrg.body()?.result!!.size}"+"2", nf.format(totalJumlah)))
                                cellValues.add(addCache)

                                colValues.add(ColumnHeaderModel("Nama Customer"))
                                colValues.add(ColumnHeaderModel("Qty"))
                                colValues.add(ColumnHeaderModel("Jumlah"))

                                uiThread {
                                    if (!isViewDialogDetail){ dialogTableDetail(tahun, bulan) }
                                }

                            }
                        }
                    })
        }
    }

    @SuppressLint("SetTextI18n")
    private fun dialogTableDetail(tahun: String, bulan: String){
        println("---------------------------- show dialog detail")
        isViewDialogDetail = true
        val adapterTable = AdapterTableView(requireActivity())
        val dialog = BottomSheetDialog(requireActivity(), R.style.SheetDialogNotif)
        val bottomSheetLayout = layoutInflater.inflate(R.layout.dialog_evaluasi_detail_table, null)
        dialog.setContentView(bottomSheetLayout)
        val behavior = BottomSheetBehavior.from(dialog.findViewById(R.id.bottomSheetEvaluasiDetailTable)!!)
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
        dialog.show()

        val tvKdBrg = dialog.findViewById<TextView>(R.id.tv_kdbrg)
        val tvTgl = dialog.findViewById<TextView>(R.id.tv_tgl)
        val tblView = dialog.findViewById<TableView>(R.id.container_table)
        tvKdBrg?.text = kdbrg
        tvTgl?.text = "Bulan $bulan, $tahun"
        tblView?.adapter = adapterTable
        adapterTable.setAllItems(colValues, rowValues, cellValues)

        dialog.setOnDismissListener {
            isViewDialogDetail = false
            println("---------------------------- dialog detail dismiss")
        }
    }

    private fun getDataTableLoss(){
        doAsync {
            println("---------------------------- get data loss")
            colValues.clear()
            rowValues.clear()
            cellValues.clear()
            API.instance().create(ApiService::class.java)
                    .getEvaluasiLossCust(kdbrg, kota)
                    ?.enqueue(object: Callback<EvaluasiLossCustResponse?> {
                        override fun onFailure(call: Call<EvaluasiLossCustResponse?>, e: Throwable) {
                            Log.d("Get Table Loss", "Error $e")
                            FirebaseCrashlytics.getInstance().recordException(e)
                            DialogAlert("${getString(R.string.error_pengambilan_data)} ${e.message}", "error", requireActivity())
                        }

                        override fun onResponse(call: Call<EvaluasiLossCustResponse?>, response: Response<EvaluasiLossCustResponse?>) {
                            if(response.code() == 200) {
                                var totalNilFakturAvg = 0.0
                                for (i in 0 until response.body()?.result!!.size){
                                    rowValues.add(RowHeaderModel(response.body()?.result!![i].kdCust!!))
                                    addCache = mutableListOf()
                                    val xIndex = i+1

                                    addCache.add(CellModel("$xIndex"+"1", response.body()?.result!![i].nmCust!!))
                                    addCache.add(CellModel("$xIndex"+"2", response.body()?.result!![i].tipeCust!!))
                                    addCache.add(CellModel("$xIndex"+"3", response.body()?.result!![i].nmSales!!))
                                    addCache.add(CellModel("$xIndex"+"4", response.body()?.result!![i].blnTerakhir.toString()))
                                    addCache.add(CellModel("$xIndex"+"5", nf.format(response.body()?.result!![i].nilFakturAvg)))

                                    cellValues.add(addCache)
                                    totalNilFakturAvg += response.body()?.result!![i].nilFakturAvg!!
                                }
                                rowValues.add(RowHeaderModel("TOTAL"))

                                addCache = mutableListOf()
                                addCache.add(CellModel("${response.body()?.result!!.size}", ""))
                                addCache.add(CellModel("${response.body()?.result!!.size}"+"1", ""))
                                addCache.add(CellModel("${response.body()?.result!!.size}"+"2", ""))
                                addCache.add(CellModel("${response.body()?.result!!.size}"+"3", ""))
                                addCache.add(CellModel("${response.body()?.result!!.size}"+"4", ""))
                                addCache.add(CellModel("${response.body()?.result!!.size}"+"5", nf.format(totalNilFakturAvg)))
                                cellValues.add(addCache)

                                colValues.add(ColumnHeaderModel("Nama Customer"))
                                colValues.add(ColumnHeaderModel("Tipe Customer"))
                                colValues.add(ColumnHeaderModel("Nama Sales"))
                                colValues.add(ColumnHeaderModel("Bln Terakhir"))
                                colValues.add(ColumnHeaderModel("Avg NilFaktur"))

                                uiThread {
                                    if(!isViewDialogLoss){
                                        dialogTableLossCust()
                                    }
                                }

                            }
                        }
                    })
        }
    }

    @SuppressLint("SetTextI18n")
    private fun dialogTableLossCust(){
        println("---------------------------- show dialog loss cust")
        isViewDialogLoss = true
        val adapterTable = AdapterTableView(requireActivity())
        val dialog = BottomSheetDialog(requireActivity(), R.style.SheetDialogNotif)
        val bottomSheetLayout = layoutInflater.inflate(R.layout.dialog_evaluasi_loss_cust_table, null)
        dialog.setContentView(bottomSheetLayout)
        val behavior = BottomSheetBehavior.from(dialog.findViewById(R.id.bottomSheetEvaluasiLossCustTable)!!)
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
        dialog.setCancelable(false)
        dialog.show()

        val tvKdBrg = dialog.findViewById<TextView>(R.id.tv_kdbrg)
        val tvDetail = dialog.findViewById<TextView>(R.id.tv_tgl)
        val tblView = dialog.findViewById<TableView>(R.id.container_table)
        val imgClose = dialog.findViewById<ImageView>(R.id.img_close)
        tvKdBrg?.text = kdbrg
        tvDetail?.text = "Loss Customer"
        tblView?.adapter = adapterTable
        adapterTable.setAllItems(colValues, rowValues, cellValues)
        tblView?.tableViewListener = TableListener()

        dialog.setOnDismissListener { isViewDialogLoss = false }

        imgClose?.setOnClickListener { dialog.dismiss() }
    }

    private inner class TableListener: ITableViewListener {
        override fun onCellLongPressed(p0: RecyclerView.ViewHolder, p1: Int, p2: Int) { }
        override fun onColumnHeaderLongPressed(p0: RecyclerView.ViewHolder, p1: Int) { }
        override fun onColumnHeaderClicked(p0: RecyclerView.ViewHolder, p1: Int) { }
        override fun onRowHeaderLongPressed(p0: RecyclerView.ViewHolder, p1: Int) { }

        override fun onCellClicked(p0: RecyclerView.ViewHolder, p1: Int, p2: Int) {
            getDataTableHistory(rowValues[p2].mData)
        }

        override fun onRowHeaderClicked(p0: RecyclerView.ViewHolder, p1: Int) {
            getDataTableHistory(rowValues[p1].mData)
        }
    }

    private fun getDataTableHistory(kdcust: String){
        doAsync {
            println("---------------------------- get data history")
            colValues.clear()
            rowValues.clear()
            cellValues.clear()
            API.instance().create(ApiService::class.java)
                    .getEvaluasiBrgHistory(kdbrg, kdcust, kota)
                    ?.enqueue(object: Callback<EvaluasiBrgHistoryResponse?> {
                        override fun onFailure(call: Call<EvaluasiBrgHistoryResponse?>, e: Throwable) {
                            Log.d("Get Evaluasi History", "Error $e")
                            FirebaseCrashlytics.getInstance().recordException(e)
                            DialogAlert("${getString(R.string.error_pengambilan_data)} ${e.message}", "error", activity!!.parent)
                        }

                        override fun onResponse(call: Call<EvaluasiBrgHistoryResponse?>, response: Response<EvaluasiBrgHistoryResponse?>) {
                            if(response.code() == 200) {
                                var totalNilFaktur = 0.0
                                for (i in 0 until response.body()?.result!!.size){
                                    rowValues.add(RowHeaderModel(response.body()?.result!![i].noBukti!!))
                                    addCache = mutableListOf()
                                    val xIndex = i+1
                                    var sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                                    val date = sdf.parse(response.body()?.result!![i].tgl!!)
                                    sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                                    val tgl = sdf.format(date)

                                    addCache.add(CellModel("$xIndex"+"1", tgl))
                                    addCache.add(CellModel("$xIndex"+"2", nf.format(response.body()?.result!![i].nilFaktur)))

                                    cellValues.add(addCache)
                                    totalNilFaktur += response.body()?.result!![i].nilFaktur!!
                                }
                                rowValues.add(RowHeaderModel("TOTAL"))

                                addCache = mutableListOf()
                                addCache.add(CellModel("${response.body()?.result!!.size}", ""))
                                addCache.add(CellModel("${response.body()?.result!!.size}"+"1", nf.format(totalNilFaktur)))
                                cellValues.add(addCache)

                                colValues.add(ColumnHeaderModel("Tanggal"))
                                colValues.add(ColumnHeaderModel("NilFaktur"))

                                uiThread {
                                    dialogTableHistory(kdcust)
                                }

                            }
                        }
                    })
        }
    }

    @SuppressLint("SetTextI18n")
    private fun dialogTableHistory(kdcust: String){
        println("---------------------------- show dialog history")
        val adapterTable = AdapterTableView(requireActivity())
        val dialog = BottomSheetDialog(requireActivity(), R.style.SheetDialogNotif)
        val bottomSheetLayout = layoutInflater.inflate(R.layout.dialog_evaluasi_loss_cust_table, null)
        dialog.setContentView(bottomSheetLayout)
        val behavior = BottomSheetBehavior.from(dialog.findViewById(R.id.bottomSheetEvaluasiLossCustTable)!!)
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
        dialog.setCancelable(false)
        dialog.show()

        val tvKdBrg = dialog.findViewById<TextView>(R.id.tv_kdbrg)
        val tvDetail = dialog.findViewById<TextView>(R.id.tv_tgl)
        val tblView = dialog.findViewById<TableView>(R.id.container_table)
        val imgClose = dialog.findViewById<ImageView>(R.id.img_close)
        tvKdBrg?.text = kdcust
        tvDetail?.text = "History $kdbrg"
        tblView?.adapter = adapterTable
        adapterTable.setAllItems(colValues, rowValues, cellValues)

        dialog.setOnDismissListener { getDataTableLoss() }

        imgClose?.setOnClickListener { dialog.dismiss() }
    }
}

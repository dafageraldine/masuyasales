package com.yusuffahrudin.masuyamobileapp.customer

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.anychart.AnyChart
import com.anychart.AnyChartView
import com.anychart.chart.common.dataentry.DataEntry
import com.anychart.chart.common.dataentry.ValueDataEntry
import com.anychart.chart.common.listener.Event
import com.anychart.chart.common.listener.ListenersInterface
import com.anychart.charts.Cartesian
import com.anychart.core.cartesian.series.Line
import com.anychart.core.ui.Title
import com.anychart.data.Mapping
import com.anychart.data.Set
import com.anychart.enums.Anchor
import com.anychart.enums.HoverMode
import com.anychart.enums.MarkerType
import com.anychart.enums.TooltipPositionMode
import com.anychart.graphics.vector.Stroke
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
import com.yusuffahrudin.masuyamobileapp.data.Customer
import com.yusuffahrudin.masuyamobileapp.data.RowHeaderModel
import com.yusuffahrudin.masuyamobileapp.data.customer.*
import com.yusuffahrudin.masuyamobileapp.util.SessionManager
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class EvaluasiCustomerFragment: Fragment() {
    private lateinit var progressBar: ProgressBar
    private lateinit var chartBar: AnyChartView
    private lateinit var btnBarToLine: FloatingActionButton
    private lateinit var btnLossItem: FloatingActionButton
    private var list = ArrayList<DataEntry>()
    private var listLine = ArrayList<DataEntry>()
    private lateinit var cartesianBar: Cartesian
    private lateinit var titleBar: Title
    private lateinit var kdcust: String
    private lateinit var kota: String
    private var filter = false
    private var isViewDialogPerThn = false
    private var isViewDialogDetail = false
    private var isViewDialogLoss = false
    private val nf = NumberFormat.getInstance(Locale("in", "ID"))
    private val colValues: MutableList<ColumnHeaderModel> = mutableListOf()
    private val rowValues: MutableList<RowHeaderModel> = mutableListOf()
    private val cellValues: MutableList<MutableList<CellModel>> = mutableListOf()
    private lateinit var addCache: MutableList<CellModel>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_evaluasi_customer, container, false)

        progressBar = view.findViewById(R.id.progressbar)
        chartBar = view.findViewById(R.id.chart_bar)
        btnBarToLine = view.findViewById(R.id.btn_bar_to_line)
        btnLossItem = view.findViewById(R.id.btn_loss_item)
        progressBar.visibility = View.GONE

        val customer = requireActivity().intent.getParcelableExtra<Customer>(CustomerDetailFragment.EXTRA_CUSTOMER)!!
        kdcust = customer.kdcust
        val sessionManager = SessionManager(requireContext())
        val cache = sessionManager.userDetails
        kota = cache[SessionManager.kdkota].toString()
        getDataBar()

        btnBarToLine.setOnClickListener {
            getDataLine()
        }

        btnLossItem.setOnClickListener {
            getDataTableLoss()
        }

        return view
    }

    private fun getDataBar(){
        progressBar.visibility = View.VISIBLE
        doAsync {
            list.clear()
            API.instance().create(ApiService::class.java)
                    .getEvaluasiCustBar(kdcust)
                    ?.enqueue(object: Callback<EvaluasiCustomerResponse?> {
                        override fun onFailure(call: Call<EvaluasiCustomerResponse?>, e: Throwable) {
                            Log.d("Get Evaluasi BarChart", "Error $e")
                            FirebaseCrashlytics.getInstance().recordException(e)
                            DialogAlert(e.message, "error", requireActivity())
                        }

                        override fun onResponse(call: Call<EvaluasiCustomerResponse?>, response: Response<EvaluasiCustomerResponse?>) {
                            if(response.code() == 200) {
                                for (i in 0 until response.body()?.result!!.size){
                                    list.add(ValueDataEntry(response.body()?.result!![i].tahun, response.body()?.result!![i].jumlah))
                                }

                                uiThread {
                                    if (filter){
                                        titleBar.text("Evaluasi customer $kdcust")
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
        titleBar.text("Evaluasi customer $kdcust")
        titleBar.fontColor("#0000FF")

        val columnBar = cartesianBar.column(data)
        columnBar.tooltip(false)

        columnBar.labels(true)
        columnBar.labels().format("{%Value}{groupsSeparator:.}")
        columnBar.labels().fontColor("#0000FF")
        columnBar.labels().rotation(-90)

        cartesianBar.yScale().minimum(0.0)
        cartesianBar.yAxis(0).labels().format("{%Value}{groupsSeparator:.}")
        cartesianBar.yAxis(0).labels().rotation(-90)

        cartesianBar.tooltip().positionMode(TooltipPositionMode.POINT)
        cartesianBar.interactivity().hoverMode(HoverMode.BY_X)

        cartesianBar.xAxis(0).title("Tahun")
        cartesianBar.yAxis(0).title("In Rupiah")

        cartesianBar.setOnClickListener(object : ListenersInterface.OnClickListener(arrayOf("x", "value")) {
            override fun onClick(event: Event) {
                println("---------------------------- click bar")
                getDataPerThn(event.data["x"].toString())
            }
        })

        chartBar.setChart(cartesianBar)
    }

    private fun getDataPerThn(tahun: String){
        doAsync {
            println("---------------------------- get data perthn")
            list.clear()
            API.instance().create(ApiService::class.java)
                    .getEvaluasiCustPerthn(kdcust, tahun)
                    ?.enqueue(object: Callback<EvaluasiCustomerResponse?> {
                        override fun onFailure(call: Call<EvaluasiCustomerResponse?>, e: Throwable) {
                            Log.d("Get Evaluasi Per Tahun", "Error $e")
                            FirebaseCrashlytics.getInstance().recordException(e)
                            DialogAlert(e.message, "error", requireActivity())
                        }

                        override fun onResponse(call: Call<EvaluasiCustomerResponse?>, response: Response<EvaluasiCustomerResponse?>) {
                            if(response.code() == 200) {
                                for (i in 0 until response.body()?.result!!.size){
                                    list.add(ValueDataEntry(response.body()?.result!![i].bulan, response.body()?.result!![i].jumlah))
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
                    "Evaluasi customer $kdcust"+
                            "<br><a style=\"color:#0000FF; font-size: 10px;\">"+
                            "Tahun $tahun</a>"
            )
            titlePerthn.fontColor("#0000FF")

            val columnPerthn = cartesianPerthn.column(list)
            columnPerthn.tooltip(false)

            columnPerthn.labels(true)
            columnPerthn.labels().format("{%Value}{groupsSeparator:.}")
            columnPerthn.labels().fontColor("#0000FF")
            columnPerthn.labels().rotation(-90)

            cartesianPerthn.yScale().minimum(0.0)
            cartesianPerthn.yAxis(0).labels().format("{%Value}{groupsSeparator:.}")
            cartesianPerthn.yAxis(0).labels().rotation(-90)

            cartesianPerthn.tooltip().positionMode(TooltipPositionMode.POINT)
            cartesianPerthn.interactivity().hoverMode(HoverMode.BY_X)

            cartesianPerthn.xAxis(0).title("Bulan")
            cartesianPerthn.yAxis(0).title("In Rupiah")

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

    private fun getDataTableDetail(tahun: String, bulan: String){
        doAsync {
            println("---------------------------- get data detail")
            colValues.clear()
            rowValues.clear()
            cellValues.clear()
            API.instance().create(ApiService::class.java)
                    .getEvaluasiCustDetail(kdcust, tahun, bulan)
                    ?.enqueue(object: Callback<EvaluasiCustDetailResponse?> {
                        override fun onFailure(call: Call<EvaluasiCustDetailResponse?>, e: Throwable) {
                            Log.d("Get Evaluasi Detail", "Error $e")
                            FirebaseCrashlytics.getInstance().recordException(e)
                            DialogAlert(e.message, "error", requireActivity())
                        }

                        override fun onResponse(call: Call<EvaluasiCustDetailResponse?>, responseBrg: Response<EvaluasiCustDetailResponse?>) {
                            if(responseBrg.code() == 200) {
                                var totalQty = 0.0; var totalJumlah = 0.0
                                for (i in 0 until responseBrg.body()?.result!!.size){
                                    rowValues.add(RowHeaderModel(responseBrg.body()?.result!![i].kdBrg!!))
                                    addCache = mutableListOf()
                                    val xIndex = i+1

                                    addCache.add(CellModel("$xIndex"+"1", responseBrg.body()?.result!![i].nmBrg!!))
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

                                colValues.add(ColumnHeaderModel("Nama Barang"))
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

        val tvKdCust = dialog.findViewById<TextView>(R.id.tv_kdbrg)
        val tvTgl = dialog.findViewById<TextView>(R.id.tv_tgl)
        val tblView = dialog.findViewById<TableView>(R.id.container_table)
        tvKdCust?.text = kdcust
        tvTgl?.text = "Bulan $bulan, $tahun"
        tblView?.adapter = adapterTable
        adapterTable.setAllItems(colValues, rowValues, cellValues)

        dialog.setOnDismissListener {
            isViewDialogDetail = false
            println("---------------------------- dialog detail dismiss")
        }
    }

    private fun getDataLine(){
        doAsync {
            listLine.clear()
            API.instance().create(ApiService::class.java)
                    .getEvaluasiCustLine(kdcust)
                    ?.enqueue(object: Callback<EvaluasiCustLineChartResponse?> {
                        override fun onFailure(call: Call<EvaluasiCustLineChartResponse?>, e: Throwable) {
                            Log.d("Get Evaluasi LineChart", "Error $e")
                            FirebaseCrashlytics.getInstance().recordException(e)
                            DialogAlert(e.message, "error", requireActivity())
                        }

                        override fun onResponse(call: Call<EvaluasiCustLineChartResponse?>, responseBrg: Response<EvaluasiCustLineChartResponse?>) {
                            if(responseBrg.code() == 200) {
                                for (i in 0 until responseBrg.body()?.result!!.size){
                                    listLine.add(CustomDataEntry(responseBrg.body()?.result!![i].bulan, responseBrg.body()?.result!![i].jumlahThn1, responseBrg.body()?.result!![i].jumlahThn2, responseBrg.body()?.result!![i].jumlahThn3))
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
            titleLine.text("Evaluasi customer $kdcust")
            titleLine.fontColor("#0000FF")
            cartesianLine.yAxis(0).title("In Rupiah")
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

    private class CustomDataEntry(x: String?, value: Double?, value2: Double?, value3: Double?) : ValueDataEntry(x, value) {
        init {
            setValue(x, value)
            setValue("value2", value2)
            setValue("value3", value3)
        }
    }

    private fun getDataTableLoss(){
        doAsync {
            println("---------------------------- get data loss")
            colValues.clear()
            rowValues.clear()
            cellValues.clear()
            API.instance().create(ApiService::class.java)
                    .getEvaluasiLossItem(kdcust, kota)
                    ?.enqueue(object: Callback<EvaluasiLossItemResponse?> {
                        override fun onFailure(call: Call<EvaluasiLossItemResponse?>, e: Throwable) {
                            Log.d("Get Evaluasi Detail", "Error $e")
                            FirebaseCrashlytics.getInstance().recordException(e)
                            DialogAlert(e.message, "error", requireActivity())
                        }

                        override fun onResponse(call: Call<EvaluasiLossItemResponse?>, response: Response<EvaluasiLossItemResponse?>) {
                            if(response.code() == 200) {
                                var totalQtyAvg = 0.0
                                for (i in 0 until response.body()?.result!!.size){
                                    rowValues.add(RowHeaderModel(response.body()?.result!![i].kdBrg!!))
                                    addCache = mutableListOf()
                                    val xIndex = i+1

                                    addCache.add(CellModel("$xIndex"+"1", response.body()?.result!![i].nmBrg!!))
                                    addCache.add(CellModel("$xIndex"+"2", response.body()?.result!![i].nmTipe!!))
                                    addCache.add(CellModel("$xIndex"+"3", response.body()?.result!![i].nmSales!!))
                                    addCache.add(CellModel("$xIndex"+"4", response.body()?.result!![i].blnTerakhir.toString()))
                                    addCache.add(CellModel("$xIndex"+"5", nf.format(response.body()?.result!![i].qtyAvg)))
                                    addCache.add(CellModel("$xIndex"+"6", response.body()?.result!![i].satuan!!))
                                    addCache.add(CellModel("$xIndex"+"7", nf.format(response.body()?.result!![i].stok)))

                                    cellValues.add(addCache)
                                    totalQtyAvg += response.body()?.result!![i].qtyAvg!!
                                }
                                rowValues.add(RowHeaderModel("TOTAL"))

                                addCache = mutableListOf()
                                addCache.add(CellModel("${response.body()?.result!!.size}", ""))
                                addCache.add(CellModel("${response.body()?.result!!.size}"+"1", ""))
                                addCache.add(CellModel("${response.body()?.result!!.size}"+"2", ""))
                                addCache.add(CellModel("${response.body()?.result!!.size}"+"3", ""))
                                addCache.add(CellModel("${response.body()?.result!!.size}"+"4", ""))
                                addCache.add(CellModel("${response.body()?.result!!.size}"+"5", nf.format(totalQtyAvg)))
                                addCache.add(CellModel("${response.body()?.result!!.size}"+"6", ""))
                                addCache.add(CellModel("${response.body()?.result!!.size}"+"7", ""))
                                cellValues.add(addCache)

                                colValues.add(ColumnHeaderModel("Nama Barang"))
                                colValues.add(ColumnHeaderModel("Tipe Barang"))
                                colValues.add(ColumnHeaderModel("Nama Sales"))
                                colValues.add(ColumnHeaderModel("Bln Terakhir"))
                                colValues.add(ColumnHeaderModel("Qty Avg"))
                                colValues.add(ColumnHeaderModel("Satuan"))
                                colValues.add(ColumnHeaderModel("Stok"))

                                uiThread {
                                    if(!isViewDialogLoss){
                                        dialogTableLossItem()
                                    }
                                }

                            }
                        }
                    })
        }
    }

    @SuppressLint("SetTextI18n")
    private fun dialogTableLossItem(){
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
        tvKdBrg?.text = kdcust
        tvDetail?.text = "Loss Item"
        tblView?.adapter = adapterTable
        adapterTable.setAllItems(colValues, rowValues, cellValues)
        tblView?.tableViewListener = tableListener()

        dialog.setOnDismissListener { isViewDialogLoss = false }

        imgClose?.setOnClickListener { dialog.dismiss() }
    }

    private inner class tableListener: ITableViewListener{
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

    private fun getDataTableHistory(kdbrg: String){
        doAsync {
            println("---------------------------- get data history")
            colValues.clear()
            rowValues.clear()
            cellValues.clear()
            API.instance().create(ApiService::class.java)
                    .getEvaluasiCustHistory(kdcust, kdbrg, kota)
                    ?.enqueue(object: Callback<EvaluasiCustHistoryResponse?> {
                        override fun onFailure(call: Call<EvaluasiCustHistoryResponse?>, e: Throwable) {
                            Log.d("Get Evaluasi History", "Error $e")
                            FirebaseCrashlytics.getInstance().recordException(e)
                            DialogAlert(e.message, "error", requireActivity())
                        }

                        override fun onResponse(call: Call<EvaluasiCustHistoryResponse?>, response: Response<EvaluasiCustHistoryResponse?>) {
                            if(response.code() == 200) {
                                var totalQty = 0.0
                                for (i in 0 until response.body()?.result!!.size){
                                    rowValues.add(RowHeaderModel(response.body()?.result!![i].noBukti!!))
                                    addCache = mutableListOf()
                                    val xIndex = i+1
                                    var sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                                    val date = sdf.parse(response.body()?.result!![i].tgl!!)
                                    sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                                    val tgl = sdf.format(date)

                                    addCache.add(CellModel("$xIndex"+"1", tgl))
                                    addCache.add(CellModel("$xIndex"+"2", nf.format(response.body()?.result!![i].qty)))
                                    addCache.add(CellModel("$xIndex"+"3", response.body()?.result!![i].satuan!!))

                                    cellValues.add(addCache)
                                    totalQty += response.body()?.result!![i].qty!!
                                }
                                rowValues.add(RowHeaderModel("TOTAL"))

                                addCache = mutableListOf()
                                addCache.add(CellModel("${response.body()?.result!!.size}", ""))
                                addCache.add(CellModel("${response.body()?.result!!.size}"+"1", nf.format(totalQty)))
                                addCache.add(CellModel("${response.body()?.result!!.size}"+"2", ""))
                                cellValues.add(addCache)

                                colValues.add(ColumnHeaderModel("Tanggal"))
                                colValues.add(ColumnHeaderModel("Qty"))
                                colValues.add(ColumnHeaderModel("Satuan"))

                                uiThread {
                                    dialogTableHistory(kdbrg)
                                }

                            }
                        }
                    })
        }
    }

    @SuppressLint("SetTextI18n")
    private fun dialogTableHistory(kdbrg: String){
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
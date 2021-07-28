package com.yusuffahrudin.masuyamobileapp.historypenjualan

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import com.yusuffahrudin.masuyamobileapp.R
import com.yusuffahrudin.masuyamobileapp.util.SessionManager
import java.util.*

class HistoryPenjualanFragment: Fragment() {

    private lateinit var dateDari: Button
    private lateinit var dateSampai: Button
    private lateinit var btnSearch: Button
    private lateinit var edtNmbrg: EditText
    private lateinit var edtCust: EditText
    private lateinit var edtSales: EditText
    private lateinit var nmbrg: String
    private lateinit var customer:String
    private lateinit var sales:String
    private lateinit var fromTgl:String
    private lateinit var toTgl:String
    private lateinit var name:String
    private lateinit var level:String
    private lateinit var sessionManager: SessionManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val viewOfFragment = inflater.inflate(R.layout.fragment_history_penjualan, container, false)

        sessionManager = SessionManager(context)
        val user = sessionManager.userDetails
        name = user[SessionManager.kunci_email].toString()
        level = user[SessionManager.level].toString()

        //menghubungkan variabel dengan layout view dan java
        edtNmbrg = viewOfFragment.findViewById(R.id.edt_brg)
        edtCust = viewOfFragment.findViewById(R.id.edt_cust)
        edtSales = viewOfFragment.findViewById(R.id.edt_sales)
        btnSearch = viewOfFragment.findViewById(R.id.btn_search)
        dateDari = viewOfFragment.findViewById(R.id.btn_date_dari)
        dateSampai = viewOfFragment.findViewById(R.id.btn_date_sampai)

        // Use the current date as the default date in the picker
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        dateDari.text = year.toString() + "/" + (month + 1) + "/" + day
        dateSampai.text = year.toString() + "/" + (month + 1) + "/" + day

        if (level.equals("1010201010") || level.equals("1010301010")) {
            edtSales.setText(name)
            edtSales.isEnabled = false
        }

        dateDari.setOnClickListener { view -> onClick(view) }
        dateSampai.setOnClickListener { view -> onClick(view) }
        btnSearch.setOnClickListener { view-> onClick(view) }

        return viewOfFragment
    }

    //fungsi onclick search
    private fun cariHistoryPenjualan() {
        nmbrg = edtNmbrg.text.toString()
        customer = edtCust.text.toString()
        sales = edtSales.text.toString()
        toTgl = dateSampai.text.toString()
        fromTgl = dateDari.text.toString()

        val intent = Intent(activity, ListHistoryPenjualanActivity::class.java)

        intent.putExtra("nmbrg", nmbrg)
        intent.putExtra("customer", customer)
        intent.putExtra("sales", sales)
        intent.putExtra("from_tgl", fromTgl)
        intent.putExtra("to_tgl", toTgl)

        startActivity(intent)
    }

    private fun onClick(v: View) {
        // Get Current Date
        val c = Calendar.getInstance()
        val mYear = c.get(Calendar.YEAR)
        val mMonth = c.get(Calendar.MONTH)
        val mDay = c.get(Calendar.DAY_OF_MONTH)
        c.add(Calendar.DAY_OF_MONTH, 7)
        if (v === dateDari) {
            val datePickerDialog = DatePickerDialog(requireContext(), DatePickerDialog.OnDateSetListener {
                _, year, monthOfYear, dayOfMonth ->
                    dateDari.text = year.toString() + "/" + (monthOfYear + 1) + "/" + dayOfMonth }, mYear, mMonth, mDay)
            datePickerDialog.datePicker.maxDate = c.timeInMillis
            datePickerDialog.show()
        }
        if (v === dateSampai) {
            val datePickerDialog = DatePickerDialog(requireContext(), DatePickerDialog.OnDateSetListener {
                _, year, monthOfYear, dayOfMonth ->
                dateSampai.text = year.toString() + "/" + (monthOfYear + 1) + "/" + dayOfMonth }, mYear, mMonth, mDay)
            datePickerDialog.datePicker.maxDate = c.timeInMillis
            datePickerDialog.show()
        }
        if (v === btnSearch) {
            cariHistoryPenjualan()
        }
    }
}
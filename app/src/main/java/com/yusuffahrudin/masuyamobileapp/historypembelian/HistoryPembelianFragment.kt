package com.yusuffahrudin.masuyamobileapp.historypembelian

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

class HistoryPembelianFragment: Fragment() {
    private lateinit var dateDari: Button
    private lateinit var dateSampai: Button
    private lateinit var btnSearch: Button
    private lateinit var edtNmbrg: EditText
    private lateinit var edtSup:EditText
    private lateinit var nmbrg: String
    private lateinit var supplier:String
    private lateinit var fromTgl:String
    private lateinit var toTgl:String
    private lateinit var name:String
    private lateinit var level:String
    internal lateinit var sessionManager: SessionManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_history_pembelian, container, false)

        sessionManager = SessionManager(context)
        val user = sessionManager.userDetails
        name = user[SessionManager.kunci_email].toString()
        level = user[SessionManager.level].toString()

        //set date pada button
        dateDari = rootView.findViewById(R.id.btn_date_dari)
        dateSampai = rootView.findViewById(R.id.btn_date_sampai)

        // Use the current date as the default date in the picker
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        dateDari.text = year.toString() + "/" + (month + 1) + "/" + day
        dateSampai.text = year.toString() + "/" + (month + 1) + "/" + day

        //menghubungkan variabel dengan layout view dan java
        edtNmbrg = rootView.findViewById(R.id.edt_brg)
        edtSup = rootView.findViewById(R.id.edt_supplier)
        btnSearch = rootView.findViewById(R.id.btn_search)

        dateDari.setOnClickListener { view -> onClick(view) }
        dateSampai.setOnClickListener { view -> onClick(view) }
        btnSearch.setOnClickListener { view -> onClick(view) }

        return rootView
    }

    //fungsi onclick search
    private fun cariHistoryPembelian() {
        nmbrg = edtNmbrg.text.toString()
        supplier = edtSup.text.toString()
        fromTgl = dateDari.text.toString()
        toTgl = dateSampai.text.toString()

        val intent = Intent(activity, ListHistoryPembelianActivity::class.java)

        intent.putExtra("nmbrg", nmbrg)
        intent.putExtra("supplier", supplier)
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
            cariHistoryPembelian()
        }
    }
}
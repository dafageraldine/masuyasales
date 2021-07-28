package com.yusuffahrudin.masuyamobileapp.stockopname;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import com.yusuffahrudin.masuyamobileapp.R;

import java.util.Calendar;
import java.util.Date;

public class FilterStockOpname extends AppCompatActivity {

    private static Button btn_tgl_awal, btn_tgl_akhir, btn_apply;
    private static TextView tv_close;
    private static EditText edt_nmbrg;
    private static CheckBox cb_open, cb_close_withpost, cb_close_withoutpost;
    private static String tgl_awal, tgl_akhir, nmbrg, status_opname;
    public static final String TGL_AWAL = "tgl_awal";
    public static final String TGL_AKHIR = "tgl_akhir";
    public static final String NMBRG = "nmbrg";
    public static final String STATUS_OPNAME = "status_opname";
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.filter_stock_opname);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        editor = sharedPreferences.edit();
        tgl_awal = sharedPreferences.getString("tgl_awal","");
        tgl_akhir = sharedPreferences.getString("tgl_akhir","");
        nmbrg = sharedPreferences.getString("nmbrg","");
        status_opname = sharedPreferences.getString("status_opname","");

        //set date pada button
        btn_tgl_awal = findViewById(R.id.btn_tgl_awal);
        btn_tgl_akhir = findViewById(R.id.btn_tgl_akhir);
        btn_apply = findViewById(R.id.btn_apply);
        tv_close = findViewById(R.id.tv_close);
        edt_nmbrg = findViewById(R.id.edt_brg);
        cb_open = findViewById(R.id.cb_open);
        cb_close_withpost = findViewById(R.id.cb_close_withpost);
        cb_close_withoutpost = findViewById(R.id.cb_close_withoutpost);

        setLayout();

        btn_tgl_awal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment newFragment = new DatePickerFragmentFrom();
                newFragment.show(getSupportFragmentManager(), "datePicker");
            }
        });

        btn_tgl_akhir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment newFragment = new DatePickerFragmentTo();
                newFragment.show(getSupportFragmentManager(), "datePicker");
            }
        });

        btn_apply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                apply();
            }
        });

        tv_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void setLayout(){
        if (tgl_awal.equalsIgnoreCase("") && tgl_akhir.equalsIgnoreCase("")){
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            btn_tgl_awal.setText(year + "/" + (month + 1) + "/" + 01);
            btn_tgl_akhir.setText(year + "/" + (month + 1) + "/" + day);
        } else {
            btn_tgl_awal.setText(tgl_awal);
            btn_tgl_akhir.setText(tgl_akhir);
            edt_nmbrg.setText(nmbrg);
            if (status_opname.equalsIgnoreCase("'Open'")){
                cb_open.setChecked(true);
                cb_close_withpost.setChecked(false);
                cb_close_withoutpost.setChecked(false);
            } else if (status_opname.equalsIgnoreCase("'Close With Post'")){
                cb_open.setChecked(false);
                cb_close_withpost.setChecked(true);
                cb_close_withoutpost.setChecked(false);
            } else if (status_opname.equalsIgnoreCase("'Close Without Post'")){
                cb_open.setChecked(false);
                cb_close_withpost.setChecked(false);
                cb_close_withoutpost.setChecked(true);
            } else if (status_opname.equalsIgnoreCase("'Open','Close With Post'")){
                cb_open.setChecked(true);
                cb_close_withpost.setChecked(true);
                cb_close_withoutpost.setChecked(false);
            } else if (status_opname.equalsIgnoreCase("'Open','Close Without Post'")){
                cb_open.setChecked(true);
                cb_close_withpost.setChecked(false);
                cb_close_withoutpost.setChecked(true);
            } else if (status_opname.equalsIgnoreCase("'Close With Post','Close Without Post'")){
                cb_open.setChecked(false);
                cb_close_withpost.setChecked(true);
                cb_close_withoutpost.setChecked(true);
            } else if (status_opname.equalsIgnoreCase("'Open','Close With Post','Close Without Post'")){
                cb_open.setChecked(true);
                cb_close_withpost.setChecked(true);
                cb_close_withoutpost.setChecked(true);
            } else {
                cb_open.setChecked(false);
                cb_close_withpost.setChecked(false);
                cb_close_withoutpost.setChecked(false);
            }
        }
    }

    //fungsi untuk membuat dialog datepicker
    public static class DatePickerFragmentFrom extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        @TargetApi(Build.VERSION_CODES.N)
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            // Create a new instance of DatePickerDialog and return it
            DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), this, year, month, day);
            datePickerDialog.getDatePicker().setMaxDate(new Date().getTime());
            return datePickerDialog;
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            btn_tgl_awal.setText(year + "/" + (month + 1) + "/" + day);
        }
    }

    //fungsi untuk membuat dialog datepicker
    public static class DatePickerFragmentTo extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        @TargetApi(Build.VERSION_CODES.N)
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            // Create a new instance of DatePickerDialog and return it
            DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), this, year, month, day);
            datePickerDialog.getDatePicker().setMaxDate(new Date().getTime());
            return datePickerDialog;
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            btn_tgl_akhir.setText(year + "/" + (month + 1) + "/" + day);
        }
    }

    private void apply(){
        tgl_awal = btn_tgl_awal.getText().toString();
        tgl_akhir = btn_tgl_akhir.getText().toString();
        nmbrg = edt_nmbrg.getText().toString();
        String status_opname = "";

        if (cb_open.isChecked() && !cb_close_withpost.isChecked() && !cb_close_withoutpost.isChecked()){
            status_opname = "'Open'";
        } else if (!cb_open.isChecked() && cb_close_withpost.isChecked() && !cb_close_withoutpost.isChecked()){
            status_opname = "'Close With Post'";
        } else if (!cb_open.isChecked() && !cb_close_withpost.isChecked() && cb_close_withoutpost.isChecked()){
            status_opname = "'Close Without Post'";
        } else if (cb_open.isChecked() && cb_close_withpost.isChecked() && !cb_close_withoutpost.isChecked()){
            status_opname = "'Open','Close With Post'";
        } else if (cb_open.isChecked() && cb_close_withpost.isChecked() && !cb_close_withoutpost.isChecked()){
            status_opname = "'Open','Close Without Post'";
        } else if (!cb_open.isChecked() && cb_close_withpost.isChecked() && cb_close_withoutpost.isChecked()){
            status_opname = "'Close With Post','Close Without Post'";
        } else if (cb_open.isChecked() && cb_close_withpost.isChecked() && cb_close_withoutpost.isChecked()){
            status_opname = "'Open','Close With Post','Close Without Post'";
        } else {
            status_opname = "";
        }

        Intent intent = new Intent(FilterStockOpname.this, StockOpnameActivity.class);
        intent.putExtra(TGL_AWAL, tgl_awal);
        intent.putExtra(TGL_AKHIR, tgl_akhir);
        intent.putExtra(NMBRG, nmbrg);
        intent.putExtra(STATUS_OPNAME, status_opname);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }
}

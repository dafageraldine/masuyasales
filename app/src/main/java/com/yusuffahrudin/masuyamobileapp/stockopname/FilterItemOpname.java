package com.yusuffahrudin.masuyamobileapp.stockopname;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.yusuffahrudin.masuyamobileapp.R;

public class FilterItemOpname extends AppCompatActivity {

    private static Button btn_apply;
    private static TextView tv_close;
    private static EditText edt_nmbrg;
    private static CheckBox cb_open, cb_close_withpost, cb_close_withoutpost;
    private static String status, NoOpname, kota, tgl, user, nmbrg, status_opname;
    public static final String NMBRG = "nmbrg";
    public static final String STATUS_OPNAME = "status_opname";
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.filter_item_opname);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        editor = sharedPreferences.edit();
        nmbrg = sharedPreferences.getString("nmbrg","");
        status_opname = sharedPreferences.getString("status_opname","");

        //set date pada button
        btn_apply = findViewById(R.id.btn_apply);
        tv_close = findViewById(R.id.tv_close);
        edt_nmbrg = findViewById(R.id.edt_brg);
        cb_open = findViewById(R.id.cb_open);
        cb_close_withpost = findViewById(R.id.cb_close_withpost);
        cb_close_withoutpost = findViewById(R.id.cb_close_withoutpost);

        setLayout();

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

    private void apply(){
        nmbrg = edt_nmbrg.getText().toString();
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

        Intent intent = new Intent(FilterItemOpname.this, ItemOpnameActivity.class);
        intent.putExtra(NMBRG, nmbrg);
        intent.putExtra(STATUS_OPNAME, status_opname);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }
}

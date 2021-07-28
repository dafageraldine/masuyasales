package com.yusuffahrudin.masuyamobileapp.stockopname;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.yusuffahrudin.masuyamobileapp.R;
import com.yusuffahrudin.masuyamobileapp.controller.AppController;
import com.yusuffahrudin.masuyamobileapp.controller.DialogAlert;
import com.yusuffahrudin.masuyamobileapp.data.Data;
import com.yusuffahrudin.masuyamobileapp.util.Server;
import com.yusuffahrudin.masuyamobileapp.util.SessionManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by yusuf fahrudin on 15-06-2017.
 */

public class FilterFormStockOpname extends AppCompatActivity {

    private SessionManager sessionManager;
    private Button btn_scan, btn_cari;
    private EditText edt_barcode, edt_nomor;
    private Spinner spin_kota;
    private String contents, namabrg, kota, status, user_kota, kdkota, no_opname;
    private RecyclerView recyclerView;
    private Adapter adapter;
    private ArrayAdapter<String> adapterkota;
    private LinearLayoutManager layoutManager;
    private List<Data> listData = new ArrayList<>();
    private String[] kotaArray;
    private Intent intent;
    private List<String> listKota;
    private ProgressDialog pDialog;
    private static String url_select_kota;
    private static final String ACTION_SCAN = "com.google.zxing.client.android.SCAN";
    private static final String TAG = FilterFormStockOpname.class.getSimpleName();
    private static String url_select;
    private String tag_json_obj = "json_obj_req";
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setTitle("Create Opname");
        setContentView(R.layout.filter_form_stock_opname);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        sessionManager = new SessionManager(getApplicationContext());
        HashMap<String, String> user = sessionManager.getUserDetails();
        user_kota = user.get(SessionManager.kota);
        kdkota = user.get(SessionManager.kdkota);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        editor = sharedPreferences.edit();
        no_opname = sharedPreferences.getString("no_opname","");
        status = sharedPreferences.getString("status","");
        kota = sharedPreferences.getString("kota","");

        btn_scan = findViewById(R.id.btn_scan);
        btn_cari = findViewById(R.id.btn_search);
        edt_barcode = findViewById(R.id.edt_barcode);
        edt_nomor = findViewById(R.id.edt_nobukti);
        spin_kota = findViewById(R.id.spin_kota);

        listKota = new ArrayList<String>();

        //mengisi spinner kota
        if (kota.equalsIgnoreCase("")){
            if (user_kota.equalsIgnoreCase("ALL")){
                new GetKota1().execute();
            } else {
                kotaArray = new String[]{user_kota};
                adapterkota = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, kotaArray);
                spin_kota.setAdapter(adapterkota);
            }
        } else {
            kotaArray = new String[]{kota};
            adapterkota = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, kotaArray);
            spin_kota.setAdapter(adapterkota);
        }

        //adapterkota = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, kotaArray);
        //spin_kota.setAdapter(adapterkota);

        edt_nomor.setText(no_opname);
        //edt_barcode.setClickable(true);
        //edt_barcode.setActivated(true);
        edt_barcode.requestFocus();
        //menghubungkan variabel dengan layout view dan java
        recyclerView = findViewById(R.id.rv_main);
        recyclerView.setHasFixedSize(true);

        /**
         * Kita menggunakan LinearLayoutManager untuk list standar
         * yang hanya berisi daftar item
         * disusun dari atas ke bawah
         */

        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        //untuk mengisi data dari JSON ke Adapter
        adapter = new Adapter(FilterFormStockOpname.this, listData);
        recyclerView.setAdapter(adapter);

        edt_barcode.setOnKeyListener((v, keyCode, event) -> {
            if ((keyCode == KeyEvent.KEYCODE_ENTER)) {
                System.out.println("======================= enter");
                if (edt_barcode != null) {
                    System.out.println("================== klik enter");
                    namabrg = edt_barcode.getText().toString();
                    kota = spin_kota.getSelectedItem().toString();
                    select();
                }
                return true;
            }
            return false;
        });

        btn_cari.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("================== cari");
                namabrg = edt_barcode.getText().toString();
                kota = spin_kota.getSelectedItem().toString();
                select();
            }
        });

        //action button scan barcode
        btn_scan.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                try{
                    intent = new Intent(ACTION_SCAN);
                    intent.putExtra("SCAN_MODE", "PRODUCT_MODE");
                    startActivityForResult(intent, 0);
                } catch (ActivityNotFoundException anfe){
                    showDialog(FilterFormStockOpname.this, "Tidak ditemukan scanner barcode!", "Download barcode scan dari playstore?", "Ya", "Tidak").show();
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home){
            this.finish();
        }

        return super.onOptionsItemSelected(item);
    }

    //membuat dialog untuk menampilkan pilihan jika belum menginstall aplikasi scan
    private static AlertDialog showDialog(final Activity act, CharSequence title, CharSequence msg, CharSequence btnYes, CharSequence btnNo) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(act);
        dialog.setTitle(title);
        dialog.setMessage(msg);
        dialog.setPositiveButton(btnYes, new DialogInterface.OnClickListener(){

            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Uri uri = Uri.parse("market://search?q=pname:"+"com.google.zxing.client.android");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                try{
                    act.startActivity(intent);
                } catch (ActivityNotFoundException anfe){
                    new DialogAlert(anfe.getMessage(), "error", act);
                }
            }
        });

        dialog.setNegativeButton(btnNo, new DialogInterface.OnClickListener(){

            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        return dialog.show();
    }

    //menampilkan hasil scan barcode
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                contents = intent.getStringExtra("SCAN_RESULT");
                edt_barcode.setText(contents);
            }
        }
    }

    //fungsi untuk select data dari database
    public void select() {
        listData.clear();
        adapter.notifyDataSetChanged();
        Server a = new Server(kdkota);
        url_select = a.URL() + "masterbrg/select_produk.php";

        StringRequest strReq = new StringRequest(Request.Method.POST, url_select, new Response.Listener<String>(){
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Response select : " + response);
                setRV(response);

                //notifikasi adanya perubahan data pada adapter
                adapter.notifyDataSetChanged();
            }
        }, new Response.ErrorListener(){

            @Override
            public void onErrorResponse(VolleyError error) {
                new DialogAlert(error.getMessage(), "error", FilterFormStockOpname.this);
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                //posting parameter ke post url
                Map<String, String> params = new HashMap<String, String>();

                params.put("namabrg", namabrg);

                return params;
            }
        };

        AppController.getInstance().addToRequestQueue(strReq, tag_json_obj);

    }

    //fungsi untuk memasukkan data dari database ke dalam arraylist
    private void setRV(String response){
        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONArray result = jsonObject.getJSONArray("result");
            for (int i = 0; i < result.length(); i++) {
                try {
                    JSONObject obj = result.getJSONObject(i);

                    Data item = new Data();

                    item.setKdbrg(obj.getString("KdBrg"));
                    item.setNmbrg(obj.getString("NmBrg"));
                    item.setSatuan(obj.getString("Satuan"));

                    //menambah item ke array
                    listData.add(item);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            //notifikasi adanya perubahan data pada adapter
            adapter.notifyDataSetChanged();

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private class Adapter extends RecyclerView.Adapter <Adapter.ViewHolder> {

        private List<Data> rvData;
        private Activity activity;

        public Adapter(Activity activity, List<Data> tampung){
            this.activity = activity;
            this.rvData = tampung;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            //membuat view baru
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_activity_barang, parent, false);
            //mengeset ukuran view, margin, padding, dan parameter layout
            ViewHolder viewHolder = new ViewHolder(view);

            return viewHolder;
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {
            //mengambil elemen dari arraylist pada posisi yang ditentukan
            //dan memasukkannya sebagai isi dari view recyclerview
            final String kdbrg = rvData.get(position).getKdbrg();
            final String nmbrg = rvData.get(position).getNmbrg();

            holder.tvKdBrg.setText(rvData.get(position).getKdbrg());
            holder.tvNmBrg.setText(rvData.get(position).getNmbrg());

            // Set onclicklistener pada view cvMain (CardView)
            holder.cvList.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    editor.putString("status", status);
                    editor.putString("no_opname", no_opname);
                    editor.putString("kota", kota);
                    editor.putString("kdbrg", kdbrg);
                    editor.putString("nmbrg", nmbrg);
                    editor.putString("satuan", listData.get(position).getSatuan());
                    editor.putString("tgl", "");

                    editor.commit();

                    Intent intent = new Intent(activity, FormStockOpnameActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
                    activity.startActivity(intent);
                    finish();
                }
            });
        }

        @Override
        public int getItemCount() {
            //mengembalikan jumlah data yang ada pada list recyclerview
            return rvData.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            public TextView tvKdBrg;
            public TextView tvNmBrg;
            public CardView cvList;

            public ViewHolder(View itemView) {
                super(itemView);
                tvKdBrg = itemView.findViewById(R.id.tv_kdbrg);
                tvNmBrg = itemView.findViewById(R.id.tv_nmbrg);
                cvList = itemView.findViewById(R.id.cv_main);
            }
        }
    }

    private class GetKota1 extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(FilterFormStockOpname.this);
            pDialog.setMessage("Fetching kota..");
            pDialog.setCancelable(false);
            pDialog.show();

        }

        @Override
        protected Void doInBackground(Void... arg0) {
            sessionManager = new SessionManager(getApplicationContext());
            HashMap<String, String> user = sessionManager.getUserDetails();
            kdkota = user.get(SessionManager.kdkota);

            Server a = new Server(kdkota);
            url_select_kota = a.URL() + "tools/select_kota.php";

            StringRequest strReq = new StringRequest(Request.Method.POST, url_select_kota, new Response.Listener<String>(){

                @Override
                public void onResponse(String response) {
                    Log.d(TAG, "Response : " + response);
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        JSONArray result = jsonObject.getJSONArray("result");
                        listKota.clear();
                        for (int i = 0; i < result.length(); i++) {
                            try {
                                JSONObject obj = result.getJSONObject(i);
                                //menambah item ke array
                                listKota.add(obj.getString("KdKota"));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        populateSpinner();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            }, new Response.ErrorListener(){

                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e(TAG, "Error : "+ error.getMessage());
                }
            });

            AppController.getInstance().addToRequestQueue(strReq, tag_json_obj);

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            if (pDialog.isShowing())
                pDialog.dismiss();
        }
    }

    private void populateSpinner() {
        // Creating adapter for spinner
        kotaArray = listKota.toArray(new String[listKota.size()]);
        adapterkota = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, kotaArray);
        spin_kota.setAdapter(adapterkota);
    }

}

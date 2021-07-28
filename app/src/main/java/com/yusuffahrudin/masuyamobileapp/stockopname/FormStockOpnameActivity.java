package com.yusuffahrudin.masuyamobileapp.stockopname;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ParseException;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.github.clans.fab.FloatingActionButton;
import com.yusuffahrudin.masuyamobileapp.R;
import com.yusuffahrudin.masuyamobileapp.controller.AppController;
import com.yusuffahrudin.masuyamobileapp.controller.DialogAlert;
import com.yusuffahrudin.masuyamobileapp.data.ArrayTampung;
import com.yusuffahrudin.masuyamobileapp.data.Opname;
import com.yusuffahrudin.masuyamobileapp.data.Pending;
import com.yusuffahrudin.masuyamobileapp.data.UserAkses;
import com.yusuffahrudin.masuyamobileapp.util.Server;
import com.yusuffahrudin.masuyamobileapp.util.SessionManager;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class FormStockOpnameActivity extends AppCompatActivity {

    private int success;
    private String kota, kdbrg, nmbrg, gudang, message, name, status, tgl, status_opname, user, kdkota, no_opname, periode;
    private static Boolean status_periode = true;
    private static String no_opnamex, kdbrgx, satuanx, opname_prefix, opname_number;
    private Double qty_adjust;
    private TextView tv_nomor, tv_kdbrg, tv_nmbrg, tv_tgl, tv_kota;
    private FloatingActionButton fab_save, fab_edit, fab_post, fab_unpost, fab_close;
    private RecyclerView recyclerViewOpname, recyclerViewHasil, recyclerViewPending;
    private List<Opname> listData = new ArrayList<>();
    private List<Object> listGudang = new ArrayList<>();
    private List<Pending> listPending = new ArrayList<>();
    private List<Pending> listDeletePending = new ArrayList<>();
    private List<Gudang> listFisik = new ArrayList<>();
    private List<Gudang> listGstk = new ArrayList<>();
    private List<Hasil> listHasil = new ArrayList<>();
    private AdapterOpname adapterOpname;
    private AdapterHasil adapterHasil;
    private AdapterPending adapterPending;
    private LinearLayoutManager layoutManager;
    private NumberFormat nf = NumberFormat.getInstance();
    private AlertDialog.Builder dialog;
    private SessionManager sessionManager;
    private boolean isdelete = false, ispost = false;
    private List<UserAkses> listAkses = ArrayTampung.getListAkses();
    private static final int PICK_CONTACT_REQUEST = 1;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private ProgressDialog progressDialog;

    private static final String TAG = FormStockOpnameActivity.class.getSimpleName();

    private static String url_find_gudang;
    private static String url_select_stock;
    private static String url_find_pending;
    private static String url_insert_hasil;
    private static String url_select_view_opname;
    private static String url_select_view_pending;
    private static String url_update_detail;
    private static String url_close_without_post;
    private static String url_posting;
    private static String url_adjust;
    private static String url_unpost1;
    private static String url_select;
    private static String url_cek_periode;
    private String tag_json_obj = "json_obj_req";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setTitle("Form Stock Opname");
        setContentView(R.layout.activity_form_stock_opname);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        tv_kdbrg = findViewById(R.id.tv_kdbrg);
        tv_nmbrg = findViewById(R.id.tv_nmbrg);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        editor = sharedPreferences.edit();
        no_opname = sharedPreferences.getString("no_opname","");
        status = sharedPreferences.getString("status","");
        kdbrg = sharedPreferences.getString("kdbrg","");
        nmbrg = sharedPreferences.getString("nmbrg","");
        satuanx = sharedPreferences.getString("satuan","");
        user = sharedPreferences.getString("user","");
        status_opname = sharedPreferences.getString("status_opname","");
        tgl = sharedPreferences.getString("tgl","");
        kota = sharedPreferences.getString("kota","");

        sessionManager = new SessionManager(getApplicationContext());
        HashMap<String, String> hashMapuser = sessionManager.getUserDetails();
        name = hashMapuser.get(SessionManager.kunci_email);
        kdkota = hashMapuser.get(SessionManager.kdkota);

        //menghubungkan variabel dengan layout view dan java
        tv_nomor = findViewById(R.id.tv_nomor);
        tv_tgl = findViewById(R.id.tv_tgl);
        tv_kota = findViewById(R.id.tv_kota);
        fab_save = findViewById(R.id.fab_save);
        fab_edit = findViewById(R.id.fab_edit);
        fab_post = findViewById(R.id.fab_post);
        fab_unpost = findViewById(R.id.fab_unpost);
        fab_close = findViewById(R.id.fab_close);

        recyclerViewHasil = findViewById(R.id.rv_hasil);
        recyclerViewOpname = findViewById(R.id.rv_opname);
        recyclerViewPending = findViewById(R.id.rv_pending);
        recyclerViewOpname.setHasFixedSize(true);
        recyclerViewHasil.setHasFixedSize(true);
        recyclerViewPending.setHasFixedSize(true);

        /**
         * Kita menggunakan LinearLayoutManager untuk list standar
         * yang hanya berisi daftar item
         * disusun dari atas ke bawah
         */

        layoutManager = new LinearLayoutManager(this);
        recyclerViewOpname.setLayoutManager(layoutManager);
        layoutManager = new LinearLayoutManager(this);
        recyclerViewHasil.setLayoutManager(layoutManager);
        layoutManager = new LinearLayoutManager(this);
        recyclerViewPending.setLayoutManager(layoutManager);

        //untuk mengisi data dari JSON ke Adapter
        adapterOpname = new AdapterOpname(FormStockOpnameActivity.this, listData);
        recyclerViewOpname.setAdapter(adapterOpname);
        //untuk mengisi data dari JSON ke Adapter
        adapterHasil = new AdapterHasil(FormStockOpnameActivity.this, listData);
        recyclerViewHasil.setAdapter(adapterHasil);
        //untuk mengisi data dari JSON ke Adapter
        adapterPending = new AdapterPending(FormStockOpnameActivity.this, listPending);
        recyclerViewPending.setAdapter(adapterPending);

        cekAkses();
        if (status.equalsIgnoreCase("create")){
            setLayout();
            cariGudang();
            fab_edit.setEnabled(false);
            fab_post.setEnabled(false);
            fab_unpost.setEnabled(false);
            fab_close.setEnabled(false);
        } else {
            setLayout();
            selectViewOpname();
            fab_save.setEnabled(false);
            if (user.equalsIgnoreCase(name)){
                fab_edit.setEnabled(true);
            } else {
                fab_edit.setEnabled(false);
            }
            if (status_opname.equalsIgnoreCase("Open") && ispost == true){
                fab_unpost.setEnabled(false);
            } else if(status_opname.equalsIgnoreCase("Open") && ispost == false){
                fab_unpost.setEnabled(false);
                fab_post.setEnabled(false);
                fab_close.setEnabled(false);
            } else if(status_opname.equalsIgnoreCase("Close With Post") && ispost == true){
                fab_post.setEnabled(false);
                fab_close.setEnabled(false);
                fab_edit.setEnabled(false);
                fab_unpost.setEnabled(true);
            } else {
                fab_post.setEnabled(false);
                fab_close.setEnabled(false);
                fab_unpost.setEnabled(false);
                fab_edit.setEnabled(false);
            }
        }

        fab_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!tv_nomor.getText().toString().equalsIgnoreCase("AUTO")){
                    opname_prefix = tv_nomor.getText().toString().substring(0, tv_nomor.getText().toString().indexOf("-"));
                    opname_number = tv_nomor.getText().toString().substring(tv_nomor.getText().toString().indexOf("-") + 1);
                }
                simpanHasilOpname();
            }
        });

        fab_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateHasilOpname();
            }
        });

        fab_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateCloseWithoutPost();
            }
        });

        fab_post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cekStock();
            }
        });

        fab_unpost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    unPost();
                } catch (java.text.ParseException e) {
                    e.printStackTrace();
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

    private void setLayout(){
        tv_nomor.setText(no_opname);
        tv_kota.setText(kota);
        tv_kdbrg.setText(kdbrg);
        tv_nmbrg.setText(nmbrg);
        System.out.println("============================tgl "+tgl);
        if (tgl.equalsIgnoreCase("")){
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
            tv_tgl.setText(sdf.format(new Date()));
        } else {
            tv_tgl.setText(tgl);
        }
    }

    private void cariGudang() {
        Server a = new Server(kdkota);
        url_find_gudang = a.URL() + "stockopname/create/find_gudang_opname.php";

        progressDialog = ProgressDialog.show(FormStockOpnameActivity.this, "", "Please Wait...");
        new Thread() {
            public void run() {
                try{
                    sleep(10000);
                } catch (Exception e) {
                    Log.e("tag", e.getMessage());
                }
            }
        }.start();

        final StringRequest strReq = new StringRequest(Request.Method.POST, url_find_gudang, new Response.Listener<String>(){

            @Override
            public void onResponse(String response) {
                Log.v(TAG, "Response : "+ response);

                try{
                    JSONObject jsonObject = new JSONObject(response);
                    JSONArray result = jsonObject.getJSONArray("result");
                    for (int i = 0; i < result.length(); i++) {
                        try {
                            JSONObject obj = result.getJSONObject(i);

                            listGudang.add(obj.get("KdGd"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    StringBuilder b = new StringBuilder();
                    for (Object item : listGudang) {
                        b.append("'").append(item.toString().trim()).append("'" + ",");
                    }
                    gudang = b.substring(0, b.length() - 1);
                    Log.v(TAG, "Gudang : " + gudang);

                    selectStock();
                } catch (JSONException | ParseException e){
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error) {
                new DialogAlert(error.getMessage(), "error", FormStockOpnameActivity.this);
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                //Posting parameter ke post url
                Map<String, String> params = new HashMap<String, String>();
                params.put("kota", kota);

                return params;
            }
        };
        AppController.getInstance().addToRequestQueue(strReq, tag_json_obj);
    }

    private void selectStock(){
        listData.clear();
        adapterOpname.notifyDataSetChanged();
        adapterHasil.notifyDataSetChanged();
        Server a = new Server(kdkota);
        url_select_stock = a.URL() + "stockopname/create/select_opname_stock.php";

        StringRequest strReq = new StringRequest(Request.Method.POST, url_select_stock, new Response.Listener<String>(){

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Response : " + response);
                setRV(response);

                //notifikasi adanya perubahan data pada adapter
                adapterOpname.notifyDataSetChanged();
                adapterHasil.notifyDataSetChanged();

                selectPending();
            }
        }, new Response.ErrorListener(){

            @Override
            public void onErrorResponse(VolleyError error) {
                new DialogAlert(error.getMessage(), "error", FormStockOpnameActivity.this);
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
                Calendar c = Calendar.getInstance();   // this takes current date
                c.set(Calendar.DAY_OF_MONTH, 1);
                //posting parameter ke post url
                Map<String, String> params = new HashMap<String, String>();

                params.put("kdbrg", kdbrg);
                params.put("gudang", gudang);
                params.put("tgl_awal", sdf.format(c.getTime()));
                params.put("tgl_skrg", sdf.format(new Date()));

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

                    Opname item = new Opname();

                    item.setKdgd(obj.getString("KdGd"));
                    item.setSystem(obj.getDouble("System"));
                    item.setLok1(0.0);
                    item.setLok2(0.0);
                    item.setLok3(0.0);

                    //menambah item ke array
                    listData.add(item);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            boolean ketemuAS = false, ketemuAM = false,ketemuAD = false;
            boolean ketemuRS = false, ketemuRS1 = false,ketemuRM = false,ketemuRD = false;
            for (int i=0; i< listData.size(); i++){
                if (listData.get(i).getKdgd().equalsIgnoreCase("AS")){
                    ketemuAS = true;
                } else if (listData.get(i).getKdgd().equalsIgnoreCase("AM")){
                    ketemuAM = true;
                } else if (listData.get(i).getKdgd().equalsIgnoreCase("AD")){
                    ketemuAD = true;
                }
                if (listData.get(i).getKdgd().equalsIgnoreCase("RS")){
                    ketemuRS = true;
                } else if (listData.get(i).getKdgd().equalsIgnoreCase("RS1")){
                    ketemuRS1 = true;
                } else if (listData.get(i).getKdgd().equalsIgnoreCase("RM")){
                    ketemuRM = true;
                } else if (listData.get(i).getKdgd().equalsIgnoreCase("RD")){
                    ketemuRD = true;
                }
            }
            if (ketemuAS == false && tv_kota.getText().toString().equalsIgnoreCase("SBY")){
                Opname item = new Opname();

                item.setKdgd("AS");
                item.setSystem(0.0);
                item.setLok1(0.0);
                item.setLok2(0.0);
                item.setLok3(0.0);

                //menambah item ke array
                listData.add(item);
            }
            if (ketemuAM == false && tv_kota.getText().toString().equalsIgnoreCase("MLG")){
                Opname item = new Opname();

                item.setKdgd("AM");
                item.setSystem(0.0);
                item.setLok1(0.0);
                item.setLok2(0.0);
                item.setLok3(0.0);

                //menambah item ke array
                listData.add(item);
            }
            if (ketemuAD == false && tv_kota.getText().toString().equalsIgnoreCase("MKS")){
                Opname item = new Opname();

                item.setKdgd("AD");
                item.setSystem(0.0);
                item.setLok1(0.0);
                item.setLok2(0.0);
                item.setLok3(0.0);

                //menambah item ke array
                listData.add(item);
            }
            if (ketemuRS == false && tv_kota.getText().toString().equalsIgnoreCase("SBY")){
                Opname item = new Opname();

                item.setKdgd("RS");
                item.setSystem(0.0);
                item.setLok1(0.0);
                item.setLok2(0.0);
                item.setLok3(0.0);

                //menambah item ke array
                listData.add(item);
            }
            if (ketemuRS1 == false && tv_kota.getText().toString().equalsIgnoreCase("SBY")){
                Opname item = new Opname();

                item.setKdgd("RS1");
                item.setSystem(0.0);
                item.setLok1(0.0);
                item.setLok2(0.0);
                item.setLok3(0.0);

                //menambah item ke array
                listData.add(item);
            }
            if (ketemuRM == false && tv_kota.getText().toString().equalsIgnoreCase("MLG")){
                Opname item = new Opname();

                item.setKdgd("RM");
                item.setSystem(0.0);
                item.setLok1(0.0);
                item.setLok2(0.0);
                item.setLok3(0.0);

                //menambah item ke array
                listData.add(item);
            }
            if (ketemuRD == false && tv_kota.getText().toString().equalsIgnoreCase("MKS")){
                Opname item = new Opname();

                item.setKdgd("RD");
                item.setSystem(0.0);
                item.setLok1(0.0);
                item.setLok2(0.0);
                item.setLok3(0.0);

                //menambah item ke array
                listData.add(item);
            }

            //notifikasi adanya perubahan data pada adapter
            adapterOpname.notifyDataSetChanged();
            adapterHasil.notifyDataSetChanged();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void selectPending() {
        adapterPending.notifyDataSetChanged();
        Server a = new Server(kdkota);
        url_find_pending = a.URL() + "stockopname/create/find_pending_opname.php";

        final StringRequest strReq = new StringRequest(Request.Method.POST, url_find_pending, new Response.Listener<String>(){

            @Override
            public void onResponse(String response) {
                Log.v(TAG, "Response : "+ response);

                try{
                    JSONObject jsonObject = new JSONObject(response);
                    JSONArray result = jsonObject.getJSONArray("result");
                    for (int i = 0; i < result.length(); i++) {
                        try {
                            JSONObject obj = result.getJSONObject(i);

                            Pending data = new Pending();
                            data.setNobukti(obj.getString("NoBukti"));
                            String dateStr = obj.getString("Tgl");
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                            Date date = sdf.parse(dateStr);
                            sdf = new SimpleDateFormat("dd/MM/yy", Locale.getDefault());
                            String tgl = sdf.format(date);
                            data.setTgl(tgl);
                            data.setKdgd(obj.getString("KdGd"));
                            data.setQty(obj.getDouble("Qty"));
                            data.setSatuan(obj.getString("Satuan"));

                            listPending.add(data);
                        } catch (JSONException | java.text.ParseException e) {
                            e.printStackTrace();
                        }
                    }

                    progressDialog.dismiss();
                    adapterPending.notifyDataSetChanged();
                    adapterHasil.notifyDataSetChanged();
                } catch (JSONException | ParseException e){
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error) {
                new DialogAlert(error.getMessage(), "error", FormStockOpnameActivity.this);
                progressDialog.dismiss();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                //Posting parameter ke post url
                Map<String, String> params = new HashMap<String, String>();
                params.put("kdbrg", kdbrg);
                params.put("gudang", gudang);

                return params;
            }
        };
        AppController.getInstance().addToRequestQueue(strReq, tag_json_obj);
    }

    private class AdapterOpname extends RecyclerView.Adapter <AdapterOpname.ViewHolder> {

        private List<Opname> rvData;
        private Activity activity;

        public AdapterOpname(Activity activity, List<Opname> tampung){
            this.activity = activity;
            this.rvData = tampung;
        }

        @NotNull
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            //membuat view baru
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.lv_opname_layout, parent, false);
            //mengeset ukuran view, margin, padding, dan parameter layout
            ViewHolder viewHolder = new ViewHolder(view);
            View currentFocus = ((Activity)parent.getContext()).getCurrentFocus();
            if (currentFocus != null) {
                currentFocus.clearFocus();
            }

            return viewHolder;
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {
            //textwatcher
            holder.cvList.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.show_from_left));

            holder.edt_opname_lok1.addTextChangedListener(new TextWatcher(){
                public void afterTextChanged(Editable s){
                    Number lok1 = 0, lok2 = 0, lok3 = 0;
                    Gudang gd = new Gudang();
                    gd.setGd(rvData.get(position).getKdgd());
                    try {
                        lok1 = NumberFormat.getInstance().parse(holder.edt_opname_lok1.getText().toString()); // read Content edt_opname_lok1
                        lok2 = NumberFormat.getInstance().parse(holder.edt_opname_lok2.getText().toString()); // read Content edt_opname_lok2
                        lok3 = NumberFormat.getInstance().parse(holder.edt_opname_lok3.getText().toString()); // read Content edt_opname_lok3
                    } catch (ParseException e) {
                        Log.e(TAG, "Error TextWatcher : "+e);
                    } catch (java.text.ParseException e) {
                        e.printStackTrace();
                    }
                    Double hasil_fisik = Double.valueOf(lok1.toString()) + Double.valueOf(lok2.toString()) + Double.valueOf(lok3.toString());
                    gd.setQty_gd(nf.format(hasil_fisik));
                    listFisik.add(gd);
                    listData.get(position).setLok1(Double.valueOf(lok1.toString()));
                    adapterHasil.notifyDataSetChanged();
                }
                public void beforeTextChanged(CharSequence s, int start, int count, int after){ }
                public void onTextChanged(CharSequence s, int start, int before, int count){ }
            });
            holder.edt_opname_lok2.addTextChangedListener(new TextWatcher(){
                public void afterTextChanged(Editable s){
                    Number lok2 = 0, lok1 = 0, lok3 = 0;
                    Gudang gd = new Gudang();
                    gd.setGd(rvData.get(position).getKdgd());
                    try {
                        lok1 = NumberFormat.getInstance().parse(holder.edt_opname_lok1.getText().toString()); // read Content edt_opname_lok1
                        lok2 = NumberFormat.getInstance().parse(holder.edt_opname_lok2.getText().toString()); // read Content edt_opname_lok2
                        lok3 = NumberFormat.getInstance().parse(holder.edt_opname_lok3.getText().toString()); // read Content edt_opname_lok3
                    } catch (ParseException e) {
                        Log.e(TAG, "Error TextWatcher : "+e);
                    } catch (java.text.ParseException e) {
                        e.printStackTrace();
                    }
                    Double hasil_fisik = Double.valueOf(lok2.toString()) + Double.valueOf(lok1.toString()) + Double.valueOf(lok3.toString());
                    gd.setQty_gd(nf.format(hasil_fisik));
                    listFisik.add(gd);
                    listData.get(position).setLok2(Double.valueOf(lok2.toString()));
                    adapterHasil.notifyDataSetChanged();
                }
                public void beforeTextChanged(CharSequence s, int start, int count, int after){ }
                public void onTextChanged(CharSequence s, int start, int before, int count){ }
            });
            holder.edt_opname_lok3.addTextChangedListener(new TextWatcher(){
                public void afterTextChanged(Editable s){
                    Number lok3 = 0, lok1 = 0, lok2 = 0;
                    Gudang gd = new Gudang();
                    gd.setGd(rvData.get(position).getKdgd());
                    try {
                        lok1 = NumberFormat.getInstance().parse(holder.edt_opname_lok1.getText().toString()); // read Content edt_opname_lok1
                        lok2 = NumberFormat.getInstance().parse(holder.edt_opname_lok2.getText().toString()); // read Content edt_opname_lok2
                        lok3 = NumberFormat.getInstance().parse(holder.edt_opname_lok3.getText().toString()); // read Content edt_opname_lok3
                    } catch (ParseException e) {
                        Log.e(TAG, "Error TextWatcher : "+e);
                    } catch (java.text.ParseException e) {
                        e.printStackTrace();
                    }
                    Double hasil_fisik = Double.valueOf(lok3.toString()) + Double.valueOf(lok2.toString()) + Double.valueOf(lok1.toString());
                    gd.setQty_gd(nf.format(hasil_fisik));
                    listFisik.add(gd);
                    listData.get(position).setLok3(Double.valueOf(lok3.toString()));
                    adapterHasil.notifyDataSetChanged();
                }
                public void beforeTextChanged(CharSequence s, int start, int count, int after){ }
                public void onTextChanged(CharSequence s, int start, int before, int count){ }
            });
            //holder.edt_opname_lok1.addTextChangedListener(new NumberTextWatcher(holder.edt_opname_lok1));
            //holder.edt_opname_lok2.addTextChangedListener(new NumberTextWatcher(holder.edt_opname_lok2));
            //holder.edt_opname_lok3.addTextChangedListener(new NumberTextWatcher(holder.edt_opname_lok3));


            //mengambil elemen dari arraylist pada posisi yang ditentukan
            //dan memasukkannya sebagai isi dari view recyclerview
            String system = nf.format(rvData.get(position).getSystem());
            String lokasi1 = nf.format(rvData.get(position).getLok1());
            String lokasi2 = nf.format(rvData.get(position).getLok2());
            String lokasi3 = nf.format(rvData.get(position).getLok3());
            holder.tv_opname_kdgd.setText(rvData.get(position).getKdgd());
            holder.tv_opname_sistem.setText(system);
            holder.edt_opname_lok1.setText(lokasi1);
            holder.edt_opname_lok2.setText(lokasi2);
            holder.edt_opname_lok3.setText(lokasi3);
        }

        @Override
        public int getItemCount() {
            //mengembalikan jumlah data yang ada pada list recyclerview
            return rvData.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            public TextView tv_opname_kdgd;
            public TextView tv_opname_sistem;
            public EditText edt_opname_lok1;
            public EditText edt_opname_lok2;
            public EditText edt_opname_lok3;
            public CardView cvList;

            public ViewHolder(View itemView) {
                super(itemView);
                tv_opname_kdgd = itemView.findViewById(R.id.tv_opname_kdgd);
                tv_opname_sistem = itemView.findViewById(R.id.tv_opname_sistem);
                edt_opname_lok1 = itemView.findViewById(R.id.edt_opname_lok1);
                edt_opname_lok2 = itemView.findViewById(R.id.edt_opname_lok2);
                edt_opname_lok3 = itemView.findViewById(R.id.edt_opname_lok3);
                cvList = itemView.findViewById(R.id.cv_main);
            }
        }
    }

    private class AdapterHasil extends RecyclerView.Adapter <AdapterHasil.ViewHolder> {

        private List<Opname> rvData;
        private Activity activity;

        public AdapterHasil(Activity activity, List<Opname> tampung){
            this.activity = activity;
            this.rvData = tampung;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            //membuat view baru
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.lv_hasil_opname, parent, false);
            //mengeset ukuran view, margin, padding, dan parameter layout
            ViewHolder viewHolder = new ViewHolder(view);

            return viewHolder;
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            Hasil hsl = new Hasil();
            holder.tv_hasil_kdgd.setText(rvData.get(position).getKdgd());
            String system = nf.format(rvData.get(position).getSystem());
            holder.tv_hasil_system.setText(system);
            Number qty_fisik = 0;
            for (int i=0; i<listFisik.size(); i++){
                if (listFisik.get(i).getGd().equals(rvData.get(position).getKdgd())){
                    holder.tv_hasil_fisik.setText(listFisik.get(i).getQty_gd());
                    try {
                        qty_fisik = NumberFormat.getInstance().parse(listFisik.get(i).getQty_gd());
                    } catch (java.text.ParseException e) {
                        e.printStackTrace();
                    }
                }
            }

            Double pending = 0.0;
            for (int i=0; i<listPending.size(); i++){
                if (listPending.get(i).getKdgd().equals(rvData.get(position).getKdgd())) {
                    pending = pending + listPending.get(i).getQty();
                    holder.tv_hasil_pending.setText(nf.format(pending));
                } else {
                    holder.tv_hasil_pending.setText(nf.format(pending));
                }
            }
            Double selisih = Double.valueOf(qty_fisik.toString()) - (pending + rvData.get(position).getSystem());
            holder.tv_hasil_selisih.setText(nf.format(selisih));
            if (selisih < 0){
                holder.tv_hasil_selisih.setTextColor(Color.RED);
            } else if (selisih > 0){
                holder.tv_hasil_selisih.setTextColor(Color.BLUE);
            } else {
                holder.tv_hasil_selisih.setTextColor(Color.GRAY);
            }

            hsl.setGd(rvData.get(position).getKdgd());
            hsl.setQty_selisih(selisih.toString());

            if (listHasil.isEmpty()){
                listHasil.add(hsl);
            } else {
                boolean ketemu = false;
                for (int i=0; i<listHasil.size(); i++){
                    if (rvData.get(position).getKdgd().equals(listHasil.get(i).getGd())){
                        listHasil.get(i).setQty_selisih(selisih.toString());
                        ketemu = true;
                    }
                }
                if (ketemu == false){
                    listHasil.add(hsl);
                }
            }

        }

        @Override
        public int getItemCount() {
            //mengembalikan jumlah data yang ada pada list recyclerview
            return rvData.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            public TextView tv_hasil_kdgd;
            public TextView tv_hasil_system;
            public TextView tv_hasil_fisik;
            public TextView tv_hasil_pending;
            public TextView tv_hasil_selisih;
            public CardView cvList;

            public ViewHolder(View itemView) {
                super(itemView);
                tv_hasil_kdgd = itemView.findViewById(R.id.tv_hasil_kdgd);
                tv_hasil_system = itemView.findViewById(R.id.tv_hasil_system);
                tv_hasil_fisik = itemView.findViewById(R.id.tv_hasil_fisik);
                tv_hasil_pending = itemView.findViewById(R.id.tv_hasil_pending);
                tv_hasil_selisih = itemView.findViewById(R.id.tv_hasil_selisih);
                cvList = itemView.findViewById(R.id.cv_main);
            }
        }
    }

    private class AdapterPending extends RecyclerView.Adapter <AdapterPending.ViewHolder> {

        private List<Pending> rvData;
        private Activity activity;

        public AdapterPending(Activity activity, List<Pending> tampung){
            this.activity = activity;
            this.rvData = tampung;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            //membuat view baru
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.lv_pending_opname, parent, false);
            //mengeset ukuran view, margin, padding, dan parameter layout
            ViewHolder viewHolder = new ViewHolder(view);

            return viewHolder;
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {
            //mengambil elemen dari arraylist pada posisi yang ditentukan
            //dan memasukkannya sebagai isi dari view recyclerview

            holder.cvList.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.show_from_left));
            holder.tv_pending_nomor.setText(rvData.get(position).getNobukti());
            holder.tv_pending_tgl.setText(rvData.get(position).getTgl());
            holder.tv_pending_kdgd.setText(rvData.get(position).getKdgd());
            String qty = nf.format(rvData.get(position).getQty());
            holder.tv_pending_qty.setText(qty);
            holder.tv_pending_satuan.setText(rvData.get(position).getSatuan());

            // Set onclicklistener pada view cvMain (CardView)
            holder.cvList.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final int posisi = position;

                    final CharSequence[] dialogitem = {"Delete"};
                    dialog = new AlertDialog.Builder(FormStockOpnameActivity.this);
                    dialog.setCancelable(true);
                    dialog.setItems(dialogitem, new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // TODO Auto-generated method stub
                            switch (which) {
                                case 0:
                                    Delete(posisi);
                                    break;
                            }
                        }
                    }).show();
                }
            });
        }

        @Override
        public int getItemCount() {
            //mengembalikan jumlah data yang ada pada list recyclerview
            return rvData.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            public TextView tv_pending_nomor;
            public TextView tv_pending_tgl;
            public TextView tv_pending_kdgd;
            public TextView tv_pending_qty;
            public TextView tv_pending_satuan;
            public CardView cvList;

            public ViewHolder(View itemView) {
                super(itemView);
                tv_pending_nomor = itemView.findViewById(R.id.tv_pending_nomor);
                tv_pending_tgl = itemView.findViewById(R.id.tv_pending_tgl);
                tv_pending_kdgd = itemView.findViewById(R.id.tv_pending_kdgd);
                tv_pending_qty = itemView.findViewById(R.id.tv_pending_qty);
                tv_pending_satuan = itemView.findViewById(R.id.tv_pending_satuan);
                cvList = itemView.findViewById(R.id.cv_main);
            }
        }
    }

    private void Delete(final int posisi){
        AlertDialog.Builder builder1 = new AlertDialog.Builder(FormStockOpnameActivity.this);
        builder1.setTitle("Delete");
        builder1.setMessage("Yakin untuk menghapus ?");
        builder1.setCancelable(true);

        builder1.setPositiveButton(
                "Yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        delete(posisi);
                    }
                });

        builder1.setNegativeButton(
                "No",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog alert11 = builder1.create();
        alert11.show();
    }

    // fungsi untuk menghapus
    private void delete(final int posisi){
        Pending item = new Pending();
        no_opnamex = no_opname;
        kdbrgx = kdbrg;
        item.setNobukti(listPending.get(posisi).getNobukti());
        item.setTgl(listPending.get(posisi).getTgl());
        item.setKdgd(listPending.get(posisi).getKdgd());
        listDeletePending.add(item);
        listPending.remove(posisi);
        adapterPending.notifyDataSetChanged();
        adapterHasil.notifyDataSetChanged();
    }

    private class Gudang {
        private String gd, qty_gd;

        public void Gudang(String gd, String qty_gd) {
            this.gd = gd;
            this.qty_gd = qty_gd;
        }

        public String getGd() {
            return gd;
        }

        public void setGd(String gd) {
            this.gd = gd;
        }

        public String getQty_gd() {
            return qty_gd;
        }

        public void setQty_gd(String qty_gd) {
            this.qty_gd = qty_gd;
        }
    }

    private class Hasil {
        private String gd, qty_selisih;

        public void Gudang(String gd, String qty_selisih) {
            this.gd = gd;
            this.qty_selisih = qty_selisih;
        }

        public String getQty_selisih() {
            return qty_selisih;
        }

        public void setQty_selisih(String qty_selisih) {
            this.qty_selisih = qty_selisih;
        }

        public String getGd() {
            return gd;
        }

        public void setGd(String gd) {
            this.gd = gd;
        }
    }

    //menyimpan data hasil opname ke tabelStockOpnameDtl
    private void simpanHasilOpname(){
        Server a = new Server(kdkota);
        url_insert_hasil = a.URL() + "stockopname/create/insert_hasil_stock_opname.php";

        progressDialog = ProgressDialog.show(FormStockOpnameActivity.this, "", "Please Wait...");
        new Thread() {
            public void run() {
                try{
                    sleep(10000);
                } catch (Exception e) {
                    Log.e("tag", e.getMessage());
                }
            }
        }.start();

        StringRequest strReq = new StringRequest(Request.Method.POST, url_insert_hasil, response -> {
            Log.v(TAG, "Response Hasil Opname : "+ response);

            try{
                JSONObject jObj = new JSONObject(response);
                success = jObj.getInt("success");
                message = jObj.getString("message");
                no_opname = jObj.getString("no_opname");
                //cek error node pada JSON
                if (success == 1){
                    new DialogAlert(message, "success", FormStockOpnameActivity.this);
                    editor.putString("status", status);
                    editor.putString("no_opname", no_opname);
                    editor.putString("kota", kota);
                    editor.putString("status_opname", "");
                    editor.putString("nmbrg", "");
                    editor.putString("tgl", tgl);
                    editor.putString("user", user);
                    editor.commit();

                    progressDialog.dismiss();
                    Intent intent = new Intent(FormStockOpnameActivity.this, ItemOpnameActivity.class);
                    setResult(Activity.RESULT_OK, intent);
                    finish();
                } else {
                    new DialogAlert(message, "error", FormStockOpnameActivity.this);
                    progressDialog.dismiss();
                }
            } catch (JSONException e){
                e.printStackTrace();
                progressDialog.dismiss();
            }
        }, error -> {
            error.printStackTrace();
            new DialogAlert("Error saat penyimpanan, cek koneksi speed internet anda! $error.toString()", "error", FormStockOpnameActivity.this);
        }) {
            @Override
            protected Map<String, String> getParams() {
                //posting parameter ke post url
                Map<String, String> param = new HashMap<String, String>();

                //JSONArray Hasil Opname
                JSONArray paramHasil = new JSONArray();
                JSONObject arrayHasil = null;
                try {
                    for (int i = 0; i < listData.size(); i++){

                        for (int j=0; j<4; j++){
                            if (j == 0){
                                arrayHasil = new JSONObject();

                                arrayHasil.put("no_opname", no_opname);
                                arrayHasil.put("kdbrg", tv_kdbrg.getText().toString());
                                arrayHasil.put("lokasi", "System");
                                arrayHasil.put("gudang", listData.get(i).getKdgd());
                                arrayHasil.put("qty", listData.get(i).getSystem());
                                Log.v("============= system : ", listData.get(i).getSystem().toString());

                                paramHasil.put(arrayHasil);
                            } else if (j == 1){
                                arrayHasil = new JSONObject();

                                arrayHasil.put("no_opname", no_opname);
                                arrayHasil.put("kdbrg", tv_kdbrg.getText().toString());
                                arrayHasil.put("lokasi", "Lokasi1");
                                arrayHasil.put("gudang", listData.get(i).getKdgd());
                                if (listData.get(i).getLok1() == null){
                                    arrayHasil.put("qty", 0);
                                } else {
                                    arrayHasil.put("qty", listData.get(i).getLok1());
                                    Log.v("============= lok1 : ", listData.get(i).getLok1().toString());
                                }

                                paramHasil.put(arrayHasil);
                            } else if (j == 2){
                                arrayHasil = new JSONObject();

                                arrayHasil.put("no_opname", no_opname);
                                arrayHasil.put("kdbrg", tv_kdbrg.getText().toString());
                                arrayHasil.put("lokasi", "Lokasi2");
                                arrayHasil.put("gudang", listData.get(i).getKdgd());
                                if (listData.get(i).getLok2() == null){
                                    arrayHasil.put("qty", 0);
                                } else {
                                    arrayHasil.put("qty", listData.get(i).getLok2());
                                    Log.v("============= lok2 : ", listData.get(i).getLok2().toString());
                                }

                                paramHasil.put(arrayHasil);
                            } else {
                                arrayHasil = new JSONObject();

                                arrayHasil.put("no_opname", no_opname);
                                arrayHasil.put("kdbrg", tv_kdbrg.getText().toString());
                                arrayHasil.put("lokasi", "Lokasi3");
                                arrayHasil.put("gudang", listData.get(i).getKdgd());
                                if (listData.get(i).getLok3() == null){
                                    arrayHasil.put("qty", 0);
                                } else {
                                    arrayHasil.put("qty", listData.get(i).getLok3());
                                    Log.v("============= lok3 : ", listData.get(i).getLok3().toString());
                                }

                                paramHasil.put(arrayHasil);
                            }
                        }

                    }
                } catch (ParseException | JSONException e) {
                    e.printStackTrace();
                }

                //JSONArray Pending Opname
                JSONArray paramPending = new JSONArray();
                JSONObject arrayPending = null;
                try {
                    for (int i = 0; i < listPending.size(); i++){

                        arrayPending = new JSONObject();

                        String tgl = listPending.get(i).getTgl();
                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy", Locale.getDefault());
                        Date date = sdf.parse(tgl);
                        sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                        tgl = sdf.format(date);

                        arrayPending.put("no_opname", no_opname);
                        arrayPending.put("no_bukti", listPending.get(i).getNobukti());
                        arrayPending.put("tgl", tgl);
                        arrayPending.put("kdbrg", tv_kdbrg.getText().toString());
                        arrayPending.put("kdgd", listPending.get(i).getKdgd());
                        arrayPending.put("qty", listPending.get(i).getQty());
                        arrayPending.put("satuan", listPending.get(i).getSatuan());

                        paramPending.put(arrayPending);

                    }
                } catch (ParseException | java.text.ParseException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                try {
                    tgl = tv_tgl.getText().toString();
                    SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                    Date date = sdf.parse(tgl);
                    sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    tgl = sdf.format(date);

                    int TahunTgl = Calendar.getInstance().get(Calendar.YEAR);
                    String Tahun = String.valueOf(TahunTgl);
                    param.put("tahun", Tahun);
                    param.put("no_opname", tv_nomor.getText().toString());
                    if (tv_nomor.getText().toString().equalsIgnoreCase("AUTO")){
                        param.put("opname_prefix", "");
                        param.put("opname_number", "");
                    } else {
                        param.put("opname_prefix", opname_prefix);
                        param.put("opname_number", opname_number);
                    }
                    param.put("tgl", tgl);
                    param.put("kota", tv_kota.getText().toString());
                    param.put("user", name);
                    param.put("arrayHasil", paramHasil.toString());
                    param.put("arrayPending", paramPending.toString());
                } catch (java.text.ParseException e){
                    e.printStackTrace();
                }

                return param;
            }
        };
        strReq.setRetryPolicy(new DefaultRetryPolicy(0,DefaultRetryPolicy.DEFAULT_MAX_RETRIES,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        AppController.getInstance().addToRequestQueue(strReq, tag_json_obj);

    }

    private void selectViewOpname() {
        listData.clear();
        adapterOpname.notifyDataSetChanged();
        adapterHasil.notifyDataSetChanged();
        Server a = new Server(kdkota);
        url_select_view_opname = a.URL() + "stockopname/create/select_view_opname.php";

        progressDialog = ProgressDialog.show(FormStockOpnameActivity.this, "", "Please Wait...");
        new Thread() {
            public void run() {
                try{
                    sleep(10000);
                } catch (Exception e) {
                    Log.e("tag", e.getMessage());
                }
            }
        }.start();

        final StringRequest strReq = new StringRequest(Request.Method.POST, url_select_view_opname, response -> {
            Log.v(TAG, "Response : "+ response);

            try{
                JSONObject jsonObject = new JSONObject(response);
                JSONArray result = jsonObject.getJSONArray("result");
                for (int i = 0; i < result.length(); i++) {
                    try {
                        JSONObject obj = result.getJSONObject(i);

                        Opname item = new Opname();

                        item.setKdgd(obj.getString("KdGd"));
                        item.setSystem(obj.getDouble("System"));
                        item.setLok1(obj.getDouble("Lokasi1"));
                        item.setLok2(obj.getDouble("Lokasi2"));
                        item.setLok3(obj.getDouble("Lokasi3"));

                        //menambah item ke array
                        listData.add(item);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                //notifikasi adanya perubahan data pada adapter
                adapterOpname.notifyDataSetChanged();
                adapterHasil.notifyDataSetChanged();

                selectViewPending();
            } catch (JSONException | ParseException e){
                e.printStackTrace();
            }
        }, error -> new DialogAlert(error.getMessage(), "error", FormStockOpnameActivity.this)) {
            @Override
            protected Map<String, String> getParams() {
                //Posting parameter ke post url
                Map<String, String> params = new HashMap<String, String>();

                params.put("no_opname", no_opname);
                params.put("kdbrg", kdbrg);

                return params;
            }
        };
        AppController.getInstance().addToRequestQueue(strReq, tag_json_obj);
    }

    private void selectViewPending() {
        listPending.clear();
        adapterPending.notifyDataSetChanged();
        Server a = new Server(kdkota);
        url_select_view_pending = a.URL() + "stockopname/create/select_view_pending.php";

        final StringRequest strReq = new StringRequest(Request.Method.POST, url_select_view_pending, new Response.Listener<String>(){

            @Override
            public void onResponse(String response) {
                Log.v(TAG, "Response : "+ response);

                try{
                    JSONObject jsonObject = new JSONObject(response);
                    JSONArray result = jsonObject.getJSONArray("result");
                    for (int i = 0; i < result.length(); i++) {
                        try {
                            JSONObject obj = result.getJSONObject(i);

                            Pending data = new Pending();
                            data.setNobukti(obj.getString("NoBukti"));
                            String dateStr = obj.getString("Tgl");
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                            Date date = sdf.parse(dateStr);
                            sdf = new SimpleDateFormat("dd/MM/yy", Locale.getDefault());
                            String tgl = sdf.format(date);
                            data.setTgl(tgl);
                            data.setKdgd(obj.getString("KdGd"));
                            data.setQty(obj.getDouble("Qty"));
                            data.setSatuan(obj.getString("Satuan"));

                            listPending.add(data);
                        } catch (JSONException | java.text.ParseException e) {
                            e.printStackTrace();
                        }
                    }

                    progressDialog.dismiss();
                    adapterPending.notifyDataSetChanged();
                    adapterHasil.notifyDataSetChanged();
                } catch (JSONException | ParseException e){
                    e.printStackTrace();
                }
            }
        }, error -> {
            new DialogAlert(error.getMessage(), "error", FormStockOpnameActivity.this);
            progressDialog.dismiss();
        }) {
            @Override
            protected Map<String, String> getParams() {
                //Posting parameter ke post url
                Map<String, String> params = new HashMap<String, String>();

                params.put("no_opname", no_opname);
                params.put("kdbrg", kdbrg);

                return params;
            }
        };
        AppController.getInstance().addToRequestQueue(strReq, tag_json_obj);
    }

    //menyimpan data hasil opname ke tabelStockOpnameDtl
    private void updateHasilOpname(){
        Server a = new Server(kdkota);
        url_update_detail = a.URL() + "stockopname/create/update_hasil_opname.php";

        progressDialog = ProgressDialog.show(FormStockOpnameActivity.this, "", "Please Wait...");
        new Thread() {
            public void run() {
                try{
                    sleep(10000);
                } catch (Exception e) {
                    Log.e("tag", e.getMessage());
                }
            }
        }.start();

        StringRequest strReq = new StringRequest(Request.Method.POST, url_update_detail, response -> {
            Log.v(TAG, "Response Update Hasil Opname : "+ response);

            try{
                JSONObject jObj = new JSONObject(response);
                success = jObj.getInt("success");
                message = jObj.getString("message");

                //cek error node pada JSON
                if (success == 1){
                    new DialogAlert(message, "success", FormStockOpnameActivity.this);
                    List<Opname> listData = ArrayTampung.getListOpname();
                    listData.clear();

                    editor.putString("status", status);
                    editor.putString("no_opname", no_opname);
                    editor.putString("kota", kota);
                    editor.putString("tgl", tgl);
                    editor.putString("nmbrg", "");
                    editor.putString("user", user);
                    editor.putString("status_opname", "'"+status_opname+"'");
                    editor.commit();

                    progressDialog.dismiss();
                    Intent intent = new Intent(FormStockOpnameActivity.this, ItemOpnameActivity.class);
                    setResult(Activity.RESULT_OK, intent);
                    finish();
                } else {
                    new DialogAlert(message, "error", FormStockOpnameActivity.this);
                    progressDialog.dismiss();
                }
            } catch (JSONException e){
                e.printStackTrace();
                progressDialog.dismiss();
            }
        }, error -> {
            error.printStackTrace();
            new DialogAlert("Error saat penyimpanan, cek koneksi speed internet anda! $error.toString()", "error", FormStockOpnameActivity.this);
        }) {
            @Override
            protected Map<String, String> getParams() {
                //posting parameter ke post url
                Map<String, String> param = new HashMap<String, String>();

                // parameter update hasil opname
                JSONArray paramHasil = new JSONArray();
                JSONObject arrayHasil = null;
                try {
                    for (int i = 0; i < listData.size(); i++){
                        for (int j=0; j<4; j++){
                            if (j == 0){
                                arrayHasil = new JSONObject();

                                arrayHasil.put("no_opname", tv_nomor.getText().toString());
                                arrayHasil.put("kdbrg", tv_kdbrg.getText().toString());
                                arrayHasil.put("lokasi", "System");
                                arrayHasil.put("gudang", listData.get(i).getKdgd());
                                arrayHasil.put("qty", listData.get(i).getSystem());
                                Log.v("system : ", listData.get(i).getSystem().toString());

                                paramHasil.put(arrayHasil);
                            } else if (j == 1){
                                arrayHasil = new JSONObject();

                                arrayHasil.put("no_opname", tv_nomor.getText().toString());
                                arrayHasil.put("kdbrg", tv_kdbrg.getText().toString());
                                arrayHasil.put("lokasi", "Lokasi1");
                                arrayHasil.put("gudang", listData.get(i).getKdgd());
                                if (listData.get(i).getLok1() == null){
                                    arrayHasil.put("qty", 0);
                                } else {
                                    arrayHasil.put("qty", listData.get(i).getLok1());
                                }
                                Log.v("Lokasi1 : ", listData.get(i).getLok1().toString());

                                paramHasil.put(arrayHasil);
                            } else if (j == 2){
                                arrayHasil = new JSONObject();

                                arrayHasil.put("no_opname", tv_nomor.getText().toString());
                                arrayHasil.put("kdbrg", tv_kdbrg.getText().toString());
                                arrayHasil.put("lokasi", "Lokasi2");
                                arrayHasil.put("gudang", listData.get(i).getKdgd());
                                if (listData.get(i).getLok2() == null){
                                    arrayHasil.put("qty", 0);
                                } else {
                                    arrayHasil.put("qty", listData.get(i).getLok2());
                                }
                                Log.v("Lokasi2 : ", listData.get(i).getLok2().toString());

                                paramHasil.put(arrayHasil);
                            } else {
                                arrayHasil = new JSONObject();

                                arrayHasil.put("no_opname", tv_nomor.getText().toString());
                                arrayHasil.put("kdbrg", tv_kdbrg.getText().toString());
                                arrayHasil.put("lokasi", "Lokasi3");
                                arrayHasil.put("gudang", listData.get(i).getKdgd());
                                if (listData.get(i).getLok3() == null){
                                    arrayHasil.put("qty", 0);
                                } else {
                                    arrayHasil.put("qty", listData.get(i).getLok3());
                                }
                                Log.v("Lokasi3 : ", listData.get(i).getLok3().toString());

                                paramHasil.put(arrayHasil);
                            }
                        }

                    }
                } catch (ParseException | JSONException e) {
                    e.printStackTrace();
                }

                // parameter update pending
                JSONArray paramPending = new JSONArray();
                JSONObject arrayPending = null;
                try {
                    for (int i = 0; i < listDeletePending.size(); i++){
                        arrayPending = new JSONObject();

                        String tgl = listDeletePending.get(i).getTgl();
                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy", Locale.getDefault());
                        Date date = sdf.parse(tgl);
                        sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                        tgl = sdf.format(date);

                        arrayPending.put("no_opname", no_opnamex);
                        arrayPending.put("kdbrg", kdbrgx);
                        arrayPending.put("no_bukti", listDeletePending.get(i).getNobukti());
                        arrayPending.put("tgl", tgl);
                        arrayPending.put("kdgd", listDeletePending.get(i).getKdgd());

                        paramPending.put(arrayPending);
                    }
                } catch (ParseException | JSONException | java.text.ParseException e) {
                    e.printStackTrace();
                }

                param.put("arrayHasil", paramHasil.toString());
                param.put("arrayPending", paramPending.toString());

                return param;
            }
        };
        strReq.setRetryPolicy(new DefaultRetryPolicy(0,DefaultRetryPolicy.DEFAULT_MAX_RETRIES,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        AppController.getInstance().addToRequestQueue(strReq, tag_json_obj);

    }

    private void updateCloseWithoutPost() {
        Server a = new Server(kdkota);
        url_close_without_post = a.URL() + "stockopname/create/update_close_without_post.php";

        progressDialog = ProgressDialog.show(FormStockOpnameActivity.this, "", "Please Wait...");
        new Thread() {
            public void run() {
                try{
                    sleep(10000);
                } catch (Exception e) {
                    Log.e("tag", e.getMessage());
                }
            }
        }.start();

        final StringRequest strReq = new StringRequest(Request.Method.POST, url_close_without_post, response -> {
            Log.v(TAG, "Response : "+ response);

            try{
                JSONObject jObj = new JSONObject(response);
                success = jObj.getInt("success");
                message = jObj.getString("message");

                //cek error node pada JSON
                if (success == 1){
                    new DialogAlert(message, "success", FormStockOpnameActivity.this);
                    List<Opname> listData = ArrayTampung.getListOpname();
                    listData.clear();

                    editor.putString("status", status);
                    editor.putString("no_opname", no_opname);
                    editor.putString("kota", kota);
                    editor.putString("tgl", tgl);
                    editor.putString("nmbrg", "");
                    editor.putString("user", user);
                    editor.putString("status_opname", "'"+status_opname+"'");
                    editor.commit();

                    progressDialog.dismiss();
                    Intent intent = new Intent(FormStockOpnameActivity.this, ItemOpnameActivity.class);
                    setResult(Activity.RESULT_OK, intent);
                    finish();
                } else {
                    new DialogAlert(message, "error", FormStockOpnameActivity.this);
                    progressDialog.dismiss();
                }
            } catch (JSONException | ParseException e){
                e.printStackTrace();
            }
        }, error -> {
            error.printStackTrace();
            new DialogAlert("Error saat penyimpanan, cek koneksi speed internet anda! $error.toString()", "error", FormStockOpnameActivity.this);
        }) {
            @Override
            protected Map<String, String> getParams() {
                //Posting parameter ke post url
                Map<String, String> param = new HashMap<String, String>();

                param.put("no_opname", no_opname);
                param.put("kdbrg", kdbrg);

                return param;
            }
        };
        strReq.setRetryPolicy(new DefaultRetryPolicy(0,DefaultRetryPolicy.DEFAULT_MAX_RETRIES,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        AppController.getInstance().addToRequestQueue(strReq, tag_json_obj);
    }

    private void cekStock(){
        Server a = new Server(kdkota);
        url_select_stock = a.URL() + "stockopname/view/select_cek_stok.php";

        progressDialog = ProgressDialog.show(FormStockOpnameActivity.this, "Cek Stok", "Please Wait...");
        new Thread() {
            public void run() {
                try{
                    sleep(1000);
                } catch (Exception e) {
                    Log.e("tag", e.getMessage());
                }
            }
        }.start();

        StringRequest strReq = new StringRequest(Request.Method.POST, url_select_stock, new Response.Listener<String>(){

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Response : " + response);
                try {
                    JSONObject jObj = new JSONObject(response);
                    JSONArray result = jObj.getJSONArray("result");
                    List<Gudang> listStok = new ArrayList<>();
                    for (int i = 0; i < result.length(); i++) {
                        try {
                            JSONObject obj = result.getJSONObject(i);
                            Gudang tes = new Gudang();

                            tes.setGd(obj.getString("KdGd"));
                            tes.setQty_gd(obj.getString("Qty"));
                            satuanx = obj.getString("Satuan");

                            listStok.add(tes);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    boolean ketemuAS = false, ketemuAM = false,ketemuAD = false;
                    boolean ketemuRS = false, ketemuRS1 = false,ketemuRM = false,ketemuRD = false;
                    boolean ketemuATR = false;
                    for (int i=0; i< listStok.size(); i++){
                        if (listStok.get(i).getGd().equalsIgnoreCase("AS")){
                            ketemuAS = true;
                        } else if (listStok.get(i).getGd().equalsIgnoreCase("AM")){
                            ketemuAM = true;
                        } else if (listStok.get(i).getGd().equalsIgnoreCase("AD")){
                            ketemuAD = true;
                        }
                        if (listStok.get(i).getGd().equalsIgnoreCase("RS")){
                            ketemuRS = true;
                        } else if (listStok.get(i).getGd().equalsIgnoreCase("RS1")){
                            ketemuRS1 = true;
                        } else if (listStok.get(i).getGd().equalsIgnoreCase("RM")){
                            ketemuRM = true;
                        } else if (listStok.get(i).getGd().equalsIgnoreCase("RD")){
                            ketemuRD = true;
                        }
                        if (listStok.get(i).getGd().equalsIgnoreCase("ATR")){
                            ketemuATR = true;
                        }
                    }
                    if (ketemuAS == false && tv_kota.getText().toString().equalsIgnoreCase("SBY")){
                        Gudang tes = new Gudang();

                        tes.setGd("AS");
                        tes.setQty_gd("0");

                        listStok.add(tes);
                    }
                    if (ketemuAM == false && tv_kota.getText().toString().equalsIgnoreCase("MLG")){
                        Gudang tes = new Gudang();

                        tes.setGd("AM");
                        tes.setQty_gd("0");

                        listStok.add(tes);
                    }
                    if (ketemuAD == false && tv_kota.getText().toString().equalsIgnoreCase("MKS")){
                        Gudang tes = new Gudang();

                        tes.setGd("AD");
                        tes.setQty_gd("0");

                        listStok.add(tes);
                    }
                    if (ketemuRS == false && tv_kota.getText().toString().equalsIgnoreCase("SBY")){
                        Gudang tes = new Gudang();

                        tes.setGd("RS");
                        tes.setQty_gd("0");

                        listStok.add(tes);
                    }
                    if (ketemuRS1 == false && tv_kota.getText().toString().equalsIgnoreCase("SBY")){
                        Gudang tes = new Gudang();

                        tes.setGd("RS1");
                        tes.setQty_gd("0");

                        listStok.add(tes);
                    }
                    if (ketemuRM == false && tv_kota.getText().toString().equalsIgnoreCase("MLG")){
                        Gudang tes = new Gudang();

                        tes.setGd("RM");
                        tes.setQty_gd("0");

                        listStok.add(tes);
                    }
                    if (ketemuRD == false && tv_kota.getText().toString().equalsIgnoreCase("MKS")){
                        Gudang tes = new Gudang();

                        tes.setGd("RD");
                        tes.setQty_gd("0");

                        listStok.add(tes);
                    }
                    if (ketemuATR == false && tv_kota.getText().toString().equalsIgnoreCase("SBY")){
                        Gudang tes = new Gudang();

                        tes.setGd("ATR");
                        tes.setQty_gd("0");

                        listStok.add(tes);
                    }

                    boolean stok_minus = false;
                    for (int i=0; i<listHasil.size(); i++){
                        for (int j=0; j<listStok.size(); j++){
                            if (listHasil.get(i).getGd().equalsIgnoreCase(listStok.get(j).getGd())){
                                if ((Double.valueOf(listHasil.get(i).getQty_selisih()) + Double.valueOf(listStok.get(j).getQty_gd())) >= 0){
                                    //stok_minus = false;
                                } else {
                                    stok_minus = true;
                                    progressDialog.dismiss();
                                    Toast.makeText(FormStockOpnameActivity.this, "     Posting Failed..!    \n\nStok "+listStok.get(j).getGd()+" saat ini = "+Double.valueOf(listStok.get(j).getQty_gd()), Toast.LENGTH_LONG).show();
                                }
                            }
                        }
                    }
                    System.out.println("============================= stok_minus "+stok_minus);
                    if (stok_minus == false){
                        progressDialog.dismiss();
                        try {
                            simpanPosting();
                        } catch (java.text.ParseException e) {
                            e.printStackTrace();
                        }
                    }

                } catch (JSONException e){
                    progressDialog.dismiss();
                    e.printStackTrace();
                }
            }
        }, error -> {
            Log.e(TAG, "Error Cek Stok : "+ error.getMessage());
            //swipeRefreshLayout.setRefreshing(false);
            progressDialog.dismiss();
            new DialogAlert(error.getMessage(), "error", FormStockOpnameActivity.this);
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();

                params.put("kdbrg", kdbrg);
                params.put("kota", kota);

                return params;
            }
        };
        strReq.setRetryPolicy(new DefaultRetryPolicy(0,DefaultRetryPolicy.DEFAULT_MAX_RETRIES,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        AppController.getInstance().addToRequestQueue(strReq, tag_json_obj);
    }

    //menyimpan posting ke tblIvTrans
    private void simpanPosting() throws java.text.ParseException {
        status_periode = true;
        String tgl_periode = tv_tgl.getText().toString();
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        Date date = sdf.parse(tgl_periode);
        final int tahun = date.getYear()+1900;
        final int bulan = date.getMonth()+1;
        if (bulan < 10){
            periode = tahun +"0"+ bulan;
        } else {
            periode = String.valueOf(tahun)+ bulan;
        }

        Server a = new Server(kdkota);
        url_posting = a.URL() + "stockopname/view/insert_posting.php";

        progressDialog = ProgressDialog.show(FormStockOpnameActivity.this, "Posting", "Please Wait...");
        new Thread() {
            public void run() {
                try{
                    sleep(1000);
                } catch (Exception e) {
                    Log.e("tag", e.getMessage());
                }
            }
        }.start();

        StringRequest strReq = new StringRequest(Request.Method.POST, url_posting, new Response.Listener<String>(){

            @Override
            public void onResponse(String response) {
                Log.v(TAG, "Response Posting: "+ response);

                try{
                    JSONObject jObj = new JSONObject(response);
                    success = jObj.getInt("success");
                    message = jObj.getString("message");

                    //cek error node pada JSON
                    if (success == 1){
                        new DialogAlert(message, "success", FormStockOpnameActivity.this);
                        List<Opname> listData = ArrayTampung.getListOpname();
                        listData.clear();

                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                        Date date = sdf.parse(tgl);
                        sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                        tgl = sdf.format(date);

                        editor.putString("status", status);
                        editor.putString("no_opname", no_opname);
                        editor.putString("kota", kota);
                        editor.putString("tgl", tgl);
                        editor.putString("nmbrg", "");
                        editor.putString("user", user);
                        editor.putString("status_opname", "");
                        editor.commit();

                        progressDialog.dismiss();
                        Intent intent = new Intent(FormStockOpnameActivity.this, ItemOpnameActivity.class);
                        setResult(Activity.RESULT_OK, intent);
                        finish();
                    } else {
                        progressDialog.dismiss();
                        new DialogAlert(message, "error", FormStockOpnameActivity.this);
                    }
                } catch (JSONException | java.text.ParseException e){
                    progressDialog.dismiss();
                    e.printStackTrace();
                }
            }
        }, error -> {
            progressDialog.dismiss();
            error.printStackTrace();
            new DialogAlert("Error saat penyimpanan, cek koneksi speed internet anda! $error.toString()", "error", FormStockOpnameActivity.this);
        }) {
            @Override
            protected Map<String, String> getParams() {
                //posting parameter ke post url
                Map<String, String> param = new HashMap<String, String>();

                try {
                    // parameter insert Posting
                    JSONArray paramPosting = new JSONArray();
                    JSONObject arrayPosting = null;
                    try {
                        for (int i = 0; i < listHasil.size(); i++){
                            arrayPosting = new JSONObject();

                            arrayPosting.put("kdgd", listHasil.get(i).getGd());
                            arrayPosting.put("selisih", listHasil.get(i).getQty_selisih());

                            paramPosting.put(arrayPosting);
                        }
                    } catch (ParseException | JSONException e) {
                        e.printStackTrace();
                    }

                    param.put("arrayPosting", paramPosting.toString());
                    System.out.println("arrayPosting "+paramPosting.toString());
                    param.put("no_bukti", tv_nomor.getText().toString());
                    param.put("kdbrg", kdbrg);
                    param.put("satuan", satuanx);
                    param.put("user", name);

                    tgl = tv_tgl.getText().toString();
                    SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                    Date date = sdf.parse(tgl);
                    sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    tgl = sdf.format(date);
                    param.put("tgl", tgl);
                    param.put("periode", periode);
                } catch (ParseException | java.text.ParseException e){
                    e.printStackTrace();
                }

                return param;
            }
        };
        strReq.setRetryPolicy(new DefaultRetryPolicy(0,DefaultRetryPolicy.DEFAULT_MAX_RETRIES,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        AppController.getInstance().addToRequestQueue(strReq, tag_json_obj);
    }

    //delete posting ke tblIvTrans
    private void unPost() throws java.text.ParseException {
        status_periode = true;
        String tgl_periode = tv_tgl.getText().toString();
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        Date date = sdf.parse(tgl_periode);
        final int tahun = date.getYear()+1900;
        final int bulan = date.getMonth()+1;
        if (bulan < 10){
            periode = tahun +"0"+ bulan;
        } else {
            periode = String.valueOf(tahun)+ bulan;
        }
        Server a = new Server(kdkota);
        url_unpost1 = a.URL() + "stockopname/view/unpost.php";

        progressDialog = ProgressDialog.show(FormStockOpnameActivity.this, "Unpost", "Please Wait...");
        new Thread() {
            public void run() {
                try{
                    sleep(1000);
                } catch (Exception e) {
                    Log.e("tag", e.getMessage());
                }
            }
        }.start();

        StringRequest strReq = new StringRequest(Request.Method.POST, url_unpost1, new Response.Listener<String>(){

            @Override
            public void onResponse(String response) {
                Log.v(TAG, "Response Posting: "+ response);

                try{
                    JSONObject jObj = new JSONObject(response);
                    success = jObj.getInt("success");
                    message = jObj.getString("message");

                    //cek error node pada JSON
                    if (success == 1){
                        new DialogAlert(message, "success", FormStockOpnameActivity.this);
                        List<Opname> listData = ArrayTampung.getListOpname();
                        listData.clear();

                        editor.putString("status", status);
                        editor.putString("no_opname", no_opname);
                        editor.putString("kota", kota);
                        editor.putString("tgl", tgl);
                        editor.putString("nmbrg", "");
                        editor.putString("user", user);
                        editor.putString("status_opname", "");
                        editor.commit();

                        progressDialog.dismiss();
                        Intent intent = new Intent(FormStockOpnameActivity.this, ItemOpnameActivity.class);
                        setResult(Activity.RESULT_OK, intent);
                        finish();
                    } else {
                        progressDialog.dismiss();
                        new DialogAlert(message, "error", FormStockOpnameActivity.this);
                    }
                } catch (JSONException e){
                    progressDialog.dismiss();
                    e.printStackTrace();
                }
            }
        }, error -> {
            progressDialog.dismiss();
            error.printStackTrace();
            new DialogAlert("Error saat penyimpanan, cek koneksi speed internet anda! $error.toString()", "error", FormStockOpnameActivity.this);
        }) {
            @Override
            protected Map<String, String> getParams() {
                //posting parameter ke post url
                Map<String, String> param = new HashMap<String, String>();

                param.put("no_bukti", tv_nomor.getText().toString());
                param.put("kdbrg", kdbrg);
                param.put("user", name);
                param.put("periode", periode);

                return param;
            }
        };
        strReq.setRetryPolicy(new DefaultRetryPolicy(0,DefaultRetryPolicy.DEFAULT_MAX_RETRIES,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        AppController.getInstance().addToRequestQueue(strReq, tag_json_obj);
    }

    private void cekAkses(){
        for (int i=0; i<listAkses.size(); i++){
            String str = listAkses.get(i).getModul();
            String modul = str.substring(str.indexOf("-") + 1);

            if (modul.equalsIgnoreCase("View")){
                if  (listAkses.get(i).getDelete() == 1){
                    isdelete = true;
                }
                if  (listAkses.get(i).getPost() == 1){
                    ispost = true;
                }
            }
        }
    }
}

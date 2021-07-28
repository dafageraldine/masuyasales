package com.yusuffahrudin.masuyamobileapp.stockopname;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.yusuffahrudin.masuyamobileapp.R;
import com.yusuffahrudin.masuyamobileapp.controller.AppController;
import com.yusuffahrudin.masuyamobileapp.controller.DialogAlert;
import com.yusuffahrudin.masuyamobileapp.data.ArrayTampung;
import com.yusuffahrudin.masuyamobileapp.data.Opname;
import com.yusuffahrudin.masuyamobileapp.data.UserAkses;
import com.yusuffahrudin.masuyamobileapp.util.Server;
import com.yusuffahrudin.masuyamobileapp.util.SessionManager;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.yusuffahrudin.masuyamobileapp.data.Opname.NoOpnameComparator;

public class StockOpnameActivity extends AppCompatActivity {

    private String tgl_awal="", tgl_akhir="", nmbrg="", status_opname="", status, kdkota, user_kota;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar progressBar;
    private AdapterListStockOpname listAdapter;
    private ArrayList<Opname> listData = ArrayTampung.getListOpname();
    private ArrayList<datacetak> listCetak = new ArrayList<>();
    private NumberFormat nf = NumberFormat.getInstance();
    private SharedPreferences.Editor editor;
    private int i = 0;
    private static final String TAG = StockOpnameActivity.class.getSimpleName();
    private static int REQUEST_ITEM = 1;
    private static int REQUEST_FILTER_STOCK = 2;
    private List<UserAkses> listAkses = ArrayTampung.getListAkses();
    private boolean isFilter = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setTitle(getString(R.string.list_opname));
        setContentView(R.layout.activity_stock_opname);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        SessionManager sessionManager = new SessionManager(getApplicationContext());
        HashMap<String, String> user = sessionManager.getUserDetails();
        kdkota = user.get(SessionManager.kdkota);
        user_kota = user.get(SessionManager.kota);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        editor = sharedPreferences.edit();

        status = "view";

        // get the listview
        swipeRefreshLayout = findViewById(R.id.swipe_refresh);
        progressBar = findViewById(R.id.progressbar);
        RecyclerView rvSO = findViewById(R.id.lv_opname);
        rvSO.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        rvSO.setLayoutManager(layoutManager);
        progressBar.setVisibility(View.GONE);
        listAdapter = new AdapterListStockOpname(StockOpnameActivity.this, listData);
        Button btn_sort = findViewById(R.id.btn_sort);
        Button btn_filter = findViewById(R.id.btn_filter);

        cek_akses();
        rvSO.setAdapter(listAdapter);

        if  (listData.isEmpty()) {
            selectOpname(0);
        } else {
            filter();
        }

        btn_sort.setOnClickListener(v -> {
            final CharSequence[] items = { "A-Z", "Z-A" };
            AlertDialog.Builder builder = new AlertDialog.Builder(StockOpnameActivity.this);
            builder.setTitle("Sorting");
            builder.setItems(items, (dialog, item) -> {
                if (items[item].equals("A-Z")){
                    Collections.sort(listData, NoOpnameComparator);
                    listAdapter.notifyDataSetChanged();
                    dialog.dismiss();
                } else {
                    Collections.reverse(listData);
                    listAdapter.notifyDataSetChanged();
                    dialog.dismiss();
                }
            });
            AlertDialog alert = builder.create();
            alert.show();
        });

        btn_filter.setOnClickListener(v -> {
            editor.putString("tgl_awal", tgl_awal);
            editor.putString("tgl_akhir", tgl_akhir);
            editor.putString("nmbrg", nmbrg);
            editor.putString("status_opname", status_opname);
            editor.apply();
            Intent intent = new Intent(StockOpnameActivity.this, FilterStockOpname.class);
            startActivityForResult(intent, REQUEST_FILTER_STOCK);
        });

        rvSO.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int visibleItemCount = layoutManager.getChildCount();
                int totalItemCount = layoutManager.getItemCount();
                int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();
                if (firstVisibleItemPosition + visibleItemCount >= totalItemCount) {
                    if(i == 0){
                        if(!isFilter){
                            selectOpname(totalItemCount);
                            i = 1;
                        }
                    }
                } else { i = 0; }
            }
        });

        swipeRefreshLayout.setOnRefreshListener(() -> {
            listData.clear();
            selectOpname(0);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ITEM && resultCode == RESULT_OK) {
            selectOpname(0);
        } else if (requestCode == REQUEST_FILTER_STOCK && resultCode == RESULT_OK) {
            tgl_awal = data.getStringExtra(FilterStockOpname.TGL_AWAL);
            tgl_akhir = data.getStringExtra(FilterStockOpname.TGL_AKHIR);
            nmbrg = data.getStringExtra(FilterStockOpname.NMBRG);
            status_opname = data.getStringExtra(FilterStockOpname.STATUS_OPNAME);
            listData.clear();
            filter();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_stock_opname, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home){
            this.finish();
        }
        if (id == R.id.action_add){
            editor.putString("no_opname", "AUTO");
            editor.putString("status", "create");
            editor.putString("kota", "");
            editor.commit();
            Intent intent = new Intent(StockOpnameActivity.this, FilterFormStockOpname.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    //fungsi untuk select data dari database
    public void selectOpname(int itemCount) {
        progressBar.setVisibility(View.VISIBLE);
        Server a = new Server(kdkota);
        String url = a.URL() + "stockopname/view/select_list_opname.php";
        StringRequest strReq = new StringRequest(Request.Method.POST, url, response -> {
            Log.d(TAG, "Response Select Opname : " + response);
            try {
                JSONObject jsonObject = new JSONObject(response);
                JSONArray result = jsonObject.getJSONArray("result");
                for (int i = 0; i < result.length(); i++) {
                    try {
                        JSONObject obj = result.getJSONObject(i);

                        Opname item = new Opname();
                        String dateStr = obj.getString("Tgl");
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                        Date date = sdf.parse(dateStr);
                        sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                        String tgl = sdf.format(date);

                        item.setNo_opname(obj.getString("NoOpname"));
                        item.setTgl(tgl);
                        item.setKota(obj.getString("Kota"));
                        item.setUser(obj.getString("User"));

                        listData.add(item);
                    } catch (JSONException | ParseException e) {
                        progressBar.setVisibility(View.GONE);
                        e.printStackTrace();
                    }
                }
                i++;
                progressBar.setVisibility(View.GONE);
                listAdapter.notifyDataSetChanged();
                swipeRefreshLayout.setRefreshing(false);
                isFilter = false;
            } catch (JSONException e) {
                progressBar.setVisibility(View.GONE);
                e.printStackTrace();
            }
        }, error -> {
            progressBar.setVisibility(View.GONE);
            new DialogAlert(error.getMessage(), "error", StockOpnameActivity.this);
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("kdkota", user_kota);
                params.put("itemCount", String.valueOf(itemCount));
                return params;
            }
        };
        AppController.getInstance().addToRequestQueue(strReq, getString(R.string.tag_json_obj));
    }

    //fungsi untuk select data dari database
    public void filter() {
        progressBar.setVisibility(View.VISIBLE);
        Server a = new Server(kdkota);
        String url = a.URL() + "stockopname/view/filter_list_opname.php";
        StringRequest strReq = new StringRequest(Request.Method.POST, url, response -> {
            Log.v(TAG, "Response : " + response);
            setRV(response);
            listAdapter.notifyDataSetChanged();
        }, error -> {
            progressBar.setVisibility(View.GONE);
            new DialogAlert(error.getMessage(), "error", StockOpnameActivity.this);
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("nmbrg", nmbrg);
                params.put("tgl_awal", tgl_awal);
                params.put("tgl_akhir", tgl_akhir);
                params.put("status_opname", status_opname);
                return params;
            }
        };
        AppController.getInstance().addToRequestQueue(strReq, getString(R.string.tag_json_obj));
    }

    private void setRV(String response){
        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONArray result = jsonObject.getJSONArray("result");
            for (int i = 0; i < result.length(); i++) {
                try {
                    JSONObject obj = result.getJSONObject(i);

                    Opname item = new Opname();
                    item.setNo_opname(obj.getString("NoOpname"));
                    item.setTgl(obj.getString("Tgl"));
                    item.setKota(obj.getString("Kota"));
                    item.setUser(obj.getString("User"));
                    listData.add(item);

                } catch (JSONException e) {
                    progressBar.setVisibility(View.GONE);
                    e.printStackTrace();
                }
            }
            progressBar.setVisibility(View.GONE);
            listAdapter.notifyDataSetChanged();
            isFilter = true;
        } catch (JSONException e) {
            progressBar.setVisibility(View.GONE);
            e.printStackTrace();
        }
    }

    private class AdapterListStockOpname extends RecyclerView.Adapter <AdapterListStockOpname.ViewHolder> {
        private ArrayList<Opname> lvData;
        private Activity activity;

        AdapterListStockOpname(Activity activity, ArrayList<Opname> tampung){
            this.activity = activity;
            this.lvData = tampung;
        }

        @NotNull
        @Override
        public ViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.lv_nomor_opname, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NotNull ViewHolder holder, int position) {
            holder.cvList.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.show_from_left));
            holder.tvNoBukti.setText(lvData.get(position).getNo_opname());
            holder.tvTgl.setText(lvData.get(position).getTgl());
            holder.tvKota.setText(lvData.get(position).getKota());
            holder.tvUser.setText(lvData.get(position).getUser());

            holder.cvList.setOnClickListener(view -> {
                editor.putString("nmbrg", nmbrg);
                editor.putString("status_opname", status_opname);
                editor.putString("status", status);
                editor.putString("user", listData.get(position).getUser());
                editor.putString("no_opname", listData.get(position).getNo_opname());
                editor.putString("kota", listData.get(position).getKota());
                editor.putString("tgl", listData.get(position).getTgl());
                editor.commit();

                Intent intent = new Intent(activity, ItemOpnameActivity.class);
                startActivityForResult(intent, REQUEST_ITEM);
            });

            holder.btnCetak.setOnClickListener(v -> cetak(position));
        }

        @Override
        public int getItemCount() {
            return lvData.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {

            private TextView tvNoBukti;
            private TextView tvTgl;
            private TextView tvKota;
            private TextView tvUser;
            private Button btnCetak;
            private CardView cvList;

            ViewHolder(View itemView) {
                super(itemView);
                tvNoBukti = itemView.findViewById(R.id.tv_nomor);
                tvTgl = itemView.findViewById(R.id.tv_tgl);
                tvKota = itemView.findViewById(R.id.tv_kota);
                tvUser = itemView.findViewById(R.id.tv_user);
                btnCetak = itemView.findViewById(R.id.btn_cetak);
                cvList = itemView.findViewById(R.id.cv_main);
            }
        }
    }

    //fungsi untuk select data dari database
    public void cetak(final int position) {
        progressBar.setVisibility(View.VISIBLE);
        listCetak.clear();
        Server a = new Server(kdkota);
        String url = a.URL() + "stockopname/view/select_data_cetak.php";
        StringRequest strReq = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Response : " + response);
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    JSONArray result = jsonObject.getJSONArray("result");
                    for (int i = 0; i < result.length(); i++) {
                        try {
                            JSONObject obj = result.getJSONObject(i);

                            datacetak item = new datacetak();
                            item.setKdbrg(obj.getString("KdBrg"));
                            item.setSysgood(nf.format(obj.getDouble("SYSGOOD")));
                            item.setSysbook(nf.format(obj.getDouble("SYSBOOK")));
                            item.setSysbad(nf.format(obj.getDouble("SYSBAD")));
                            item.setHasilgood(nf.format(obj.getDouble("HASILGOOD")));
                            item.setHasilbook(nf.format(obj.getDouble("HASILBOOK")));
                            item.setHasilbad(nf.format(obj.getDouble("HASILBAD")));
                            item.setPending(nf.format(obj.getDouble("Pending")));
                            item.setSelisih(nf.format(obj.getDouble("Selisih")));

                            listCetak.add(item);
                        } catch (JSONException e) {
                            progressBar.setVisibility(View.GONE);
                            e.printStackTrace();
                        }
                    }

                    Thread thread = new Thread(new Runnable(){
                        @Override
                        public void run(){
                            try {
                                InputStream src = getAssets().open("Opname.pdf");
                                String dst = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
                                        + "/"+listData.get(position).getNo_opname()+".pdf";
                                PdfReader reader = new PdfReader(src);
                                PdfStamper stamper = new PdfStamper(reader,
                                        new FileOutputStream(dst));
                                AcroFields form = stamper.getAcroFields();

                                form.setField("txtNoOpname", listData.get(position).getNo_opname());
                                form.setField("txtKota", listData.get(position).getKota());
                                form.setField("txtTglOpname", listData.get(position).getTgl());
                                form.setField("txtOpnameBy", listData.get(position).getUser());
                                for(int i=0; i<listCetak.size(); i++){
                                    form.setField("txtNo"+i, String.valueOf(i+1));
                                    form.setField("txtKodeBrg"+i, listCetak.get(i).getKdbrg());
                                    form.setField("txtStockGood"+i, listCetak.get(i).getSysgood());
                                    form.setField("txtStockBook"+i, listCetak.get(i).getSysbook());
                                    form.setField("txtStockBad"+i, listCetak.get(i).getSysbad());
                                    form.setField("txtHasilGood"+i, listCetak.get(i).getHasilgood());
                                    form.setField("txtHasilBook"+i, listCetak.get(i).getHasilbook());
                                    form.setField("txtHasilBad"+i, listCetak.get(i).getHasilbad());
                                    form.setField("txtPending"+i, listCetak.get(i).getPending());
                                    form.setField("txtSelisih"+i, listCetak.get(i).getSelisih());
                                }

                                stamper.close();
                                reader.close();

                                /* ======= Membaca File PDF di Folder Download INternal Storage ====== */
                                Intent testIntent = new Intent(Intent.ACTION_VIEW);
                                testIntent.setType("application/pdf");

                                try {
                                    Intent intent = new Intent();
                                    intent.setAction(Intent.ACTION_VIEW);
                                    File fileToRead = new File(
                                            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
                                                    + "/"+listData.get(position).getNo_opname()+".pdf");
                                    Uri uri = Uri.fromFile(fileToRead.getAbsoluteFile());

                                    progressBar.setVisibility(View.GONE);
                                    intent.setDataAndType(uri, "application/pdf");
                                    startActivity(intent);
                                } catch (Exception ex) {
                                    progressBar.setVisibility(View.GONE);
                                    Log.i(getClass().toString(), ex.toString());
                                    new DialogAlert("Cannot open your selected file, try again later", "error", StockOpnameActivity.this);
                                }
                            } catch (IOException | DocumentException e) {
                                progressBar.setVisibility(View.GONE);
                                e.printStackTrace();
                            }
                        }
                    });
                    thread.start();
                } catch (JSONException e) {
                    progressBar.setVisibility(View.GONE);
                    e.printStackTrace();
                }
            }
        }, error -> {
            progressBar.setVisibility(View.GONE);
            Log.e(TAG, "Error print stock opname : " + error.getMessage());
            Toast.makeText(StockOpnameActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("no_opname", listData.get(position).getNo_opname());
                params.put("kota", listData.get(position).getKota());
                return params;
            }
        };
        AppController.getInstance().addToRequestQueue(strReq, getString(R.string.tag_json_obj));
    }

    private class datacetak {
        String kdbrg;
        String sysgood, sysbook, sysbad, hasilgood, hasilbook, hasilbad, pending, selisih;

        public void datacetak(String kdbrg, String sysgood, String sysbook, String sysbad, String hasilgood, String hasilbook, String hasilbad, String pending, String selisih) {
            this.kdbrg = kdbrg;
            this.sysgood = sysgood;
            this.sysbook = sysbook;
            this.sysbad = sysbad;
            this.hasilgood = hasilgood;
            this.hasilbook = hasilbook;
            this.hasilbad = hasilbad;
            this.pending = pending;
            this.selisih = selisih;
        }

        public String getKdbrg() {
            return kdbrg;
        }

        public void setKdbrg(String kdbrg) {
            this.kdbrg = kdbrg;
        }

        public String getSysgood() {
            return sysgood;
        }

        public void setSysgood(String sysgood) {
            this.sysgood = sysgood;
        }

        public String getSysbook() {
            return sysbook;
        }

        public void setSysbook(String sysbook) {
            this.sysbook = sysbook;
        }

        public String getSysbad() {
            return sysbad;
        }

        public void setSysbad(String sysbad) {
            this.sysbad = sysbad;
        }

        public String getHasilgood() {
            return hasilgood;
        }

        public void setHasilgood(String hasilgood) {
            this.hasilgood = hasilgood;
        }

        public String getHasilbook() {
            return hasilbook;
        }

        public void setHasilbook(String hasilbook) {
            this.hasilbook = hasilbook;
        }

        public String getHasilbad() {
            return hasilbad;
        }

        public void setHasilbad(String hasilbad) {
            this.hasilbad = hasilbad;
        }

        public String getPending() {
            return pending;
        }

        public void setPending(String pending) {
            this.pending = pending;
        }

        public String getSelisih() {
            return selisih;
        }

        public void setSelisih(String selisih) {
            this.selisih = selisih;
        }
    }

    private void cek_akses(){
        for (int j=0; j<listAkses.size(); j++){
            String str = listAkses.get(j).getModul();
            String modul = str.substring(str.indexOf("-") + 1);
            if (modul.equalsIgnoreCase("Create") && listAkses.get(j).getAkses() == 1){
                if (listAkses.get(j).getAkses() == 1){
                    //fab_add_opname.setEnabled(true);
                } else {
                    //fab_add_opname.setEnabled(false);
                }
            }
        }
    }

}

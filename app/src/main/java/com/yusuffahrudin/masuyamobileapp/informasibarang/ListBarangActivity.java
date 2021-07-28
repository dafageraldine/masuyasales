package com.yusuffahrudin.masuyamobileapp.informasibarang;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.yusuffahrudin.masuyamobileapp.R;
import com.yusuffahrudin.masuyamobileapp.controller.AppController;
import com.yusuffahrudin.masuyamobileapp.controller.DialogAlert;
import com.yusuffahrudin.masuyamobileapp.data.informasi_barang.Product;
import com.yusuffahrudin.masuyamobileapp.updatepricelist.AddSpecialPriceActivity;
import com.yusuffahrudin.masuyamobileapp.util.Server;
import com.yusuffahrudin.masuyamobileapp.util.SessionManager;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ListBarangActivity extends AppCompatActivity {
    private AdapterRVStok adapterRV;
    private final List<Product> listData = new ArrayList<>();
    private EditText edt_nmbrg;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar progressBar;
    private String nmbrg, tanggal, kdkota;
    private String kelas = "";

    private static final String TAG = ListBarangActivity.class.getSimpleName();
    public static final String KDBRG    = "KdBrg";
    public static final String NMBRG    = "NmBrg";
    public static final String SATUAN   = "Satuan";
    public static final String HRGEXC   = "HrgExc";
    public static final String HRGINC   = "HrgInc";
    public static final String TGL      = "Tgl";
    public static final String JNSPPN   = "JnsPPN";
    private int i = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_barang);
        this.setTitle(R.string.list_barang);
        SessionManager sessionManager = new SessionManager(getApplicationContext());
        HashMap<String, String> user = sessionManager.getUserDetails();
        kdkota = user.get(SessionManager.kdkota);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        Bundle extras = getIntent().getExtras();
        if (extras != null)
            kelas = getIntent().getExtras().getString("kelas", "");

        //menghubungkan variabel dengan layout view dan java
        RecyclerView recyclerView = findViewById(R.id.rv_main);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh);
        progressBar = findViewById(R.id.progressbar);
        progressBar.setVisibility(View.GONE);
        recyclerView.setHasFixedSize(true);
        edt_nmbrg = findViewById(R.id.edt_brg);
        Button btn_search = findViewById(R.id.btn_search);


        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        tanggal = sdf.format(new Date());

        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(layoutManager);

        //untuk mengisi data dari JSON ke Adapter
        adapterRV = new AdapterRVStok(ListBarangActivity.this, listData);
        recyclerView.setAdapter(adapterRV);

        edt_nmbrg.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                btn_search.performClick();
                return true;
            }
            return false;
        });

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int visibleItemCount = layoutManager.getChildCount();
                int totalItemCount = layoutManager.getItemCount();
                int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();
                if (firstVisibleItemPosition + visibleItemCount >= totalItemCount) {
                    if(i == 0){
                        selectStok(totalItemCount);
                        i = 1;
                    }
                } else {
                    i = 0;
                }
            }
        });

        btn_search.setOnClickListener(view -> {
            listData.clear();
            nmbrg = edt_nmbrg.getText().toString();
            if(listData.isEmpty()){
                selectStok(0);
            }
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(edt_nmbrg.getWindowToken(), 0);
        });

        swipeRefreshLayout.setOnRefreshListener(() -> {
            listData.clear();
            nmbrg = edt_nmbrg.getText().toString();
            selectStok(0);
        });
    }

    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_clear_cache, menu);
        return true;
    }*/

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home){
            this.finish();
        }
        /*if (id == R.id.action_clear_cache){
            Glide.get(getApplicationContext()).clearMemory();
            new clearCache().execute();
        }*/

        return super.onOptionsItemSelected(item);
    }

    //fungsi untuk select data dari database
    public void selectStok(int itemCount) {
        progressBar.setVisibility(View.VISIBLE);
        Server a = new Server(kdkota);
        String url = a.URL() + "masterbrg/select.php";
        StringRequest strReq = new StringRequest(Request.Method.POST, url, response -> {
            Log.d(TAG, "Response : " + response);
            setRV(response);
            adapterRV.notifyDataSetChanged();
            swipeRefreshLayout.setRefreshing(false);
        }, error -> {
            progressBar.setVisibility(View.GONE);
            Log.e(TAG, "Error Volley : "+ error.getMessage());
            new DialogAlert(error.getMessage(), "error", ListBarangActivity.this);
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("namabrg", nmbrg);
                params.put("itemCount", String.valueOf(itemCount));
                return params;
            }
        };
        AppController.getInstance().addToRequestQueue(strReq, getResources().getString(R.string.tag_json_obj));

    }

    //fungsi untuk memasukkan data dari database ke dalam arraylist
    private void setRV(String response){
        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONArray result = jsonObject.getJSONArray("result");
            for (int i = 0; i < result.length(); i++) {
                try {
                    JSONObject obj = result.getJSONObject(i);

                    Product item = new Product();

                    item.setKdbrg(obj.getString(KDBRG));
                    item.setNmbrg(obj.getString(NMBRG));
                    item.setSatuan(obj.getString(SATUAN));
                    item.setHrg(obj.getDouble(HRGEXC));
                    item.setHrgIncPpn(obj.getDouble(HRGINC));
                    item.setJnsPpn(obj.getString(JNSPPN));
                    item.setTgl(tanggal);

                    //menambah item ke array
                    listData.add(item);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            //notifikasi adanya perubahan data pada adapter
            adapterRV.notifyDataSetChanged();
            progressBar.setVisibility(View.GONE);
        } catch (JSONException e) {
            progressBar.setVisibility(View.GONE);
            e.printStackTrace();
        }
    }

    public class AdapterRVStok extends RecyclerView.Adapter <AdapterRVStok.ViewHolder> {
        private final List<Product> rvData;
        private final Activity activity;

        AdapterRVStok(Activity activity, List<Product> tampung){
            this.activity = activity;
            this.rvData = tampung;
        }

        @NotNull
        @Override
        public ViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_activity_list_barang, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NotNull ViewHolder holder, int position) {
            holder.tvKdBrg.setText(rvData.get(position).getKdbrg());
            holder.tvNmBrg.setText(rvData.get(position).getNmbrg());
            Server a = new Server("");
            Glide.with(getApplicationContext())
                    .load(a.URL_IMAGE() + rvData.get(position).getKdbrg() + ".jpg")
                    .apply(new RequestOptions()
                            .placeholder(R.mipmap.ic_launcher)
                            .error(R.drawable.img_not_found)
                            .override(150, 150)
                            .fitCenter())
                    .into(holder.imageView);
            holder.cvList.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.show_from_left));

            holder.cvList.setOnClickListener(view -> {
                if (kelas.equals("")){
                    Intent intent = new Intent(activity, InfoBarangActivity.class);
                    intent.putExtra(KDBRG, rvData.get(position).getKdbrg());
                    intent.putExtra(TGL, tanggal);
                    activity.startActivity(intent);
                } else {
                    System.out.println("kelas -------------- "+kelas);
                    kelas = kelas.substring(kelas.indexOf("."));
                    Intent intent = new Intent(activity, kelas.getClass());
                    intent.putExtra(KDBRG, rvData.get(position).getKdbrg());
                    intent.putExtra(NMBRG, rvData.get(position).getNmbrg());
                    intent.putExtra(SATUAN, rvData.get(position).getSatuan());
                    intent.putExtra(JNSPPN, rvData.get(position).getJnsPpn());
                    intent.putExtra(HRGEXC, rvData.get(position).getHrg());
                    intent.putExtra(HRGINC, rvData.get(position).getHrgIncPpn());
                    System.out.println("------------------------ hrg1 "+rvData.get(position).getHrg());
                    System.out.println("------------------------ hrgIncPpn1 "+rvData.get(position).getHrgIncPpn());
                    activity.setResult(RESULT_OK, intent);
                    activity.finish();
                }
            });
        }

        @Override
        public int getItemCount() {
            return rvData.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            TextView tvKdBrg;
            TextView tvNmBrg;
            TextView tvStok;
            ImageView imageView;
            CardView cvList;

            public ViewHolder(View itemView) {
                super(itemView);
                tvKdBrg = itemView.findViewById(R.id.tv_kdbrg);
                tvNmBrg = itemView.findViewById(R.id.tv_nmbrg);
                tvStok = itemView.findViewById(R.id.tv_stok);
                imageView = itemView.findViewById(R.id.imageView);
                cvList = itemView.findViewById(R.id.cv_main);
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class clearCache extends AsyncTask<Void, Void, Void>{
        @Override
        protected Void doInBackground(Void... voids) {
            System.out.println("===================== clear cache");
            Glide.get(getApplicationContext()).clearDiskCache();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            new DialogAlert("Clear cache berhasil, silahkan swipe untuk refresh data", "success", ListBarangActivity.this);
        }
    }
}

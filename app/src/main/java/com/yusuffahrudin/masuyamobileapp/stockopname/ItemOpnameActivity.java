package com.yusuffahrudin.masuyamobileapp.stockopname;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
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
import com.android.volley.toolbox.StringRequest;
import com.github.clans.fab.FloatingActionButton;
import com.yusuffahrudin.masuyamobileapp.R;
import com.yusuffahrudin.masuyamobileapp.controller.AppController;
import com.yusuffahrudin.masuyamobileapp.controller.DialogAlert;
import com.yusuffahrudin.masuyamobileapp.data.ArrayTampung;
import com.yusuffahrudin.masuyamobileapp.data.Opname;
import com.yusuffahrudin.masuyamobileapp.util.Server;
import com.yusuffahrudin.masuyamobileapp.util.SessionManager;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemOpnameActivity extends AppCompatActivity {

    private String NoOpname = "", status_opname, status, kota, tgl, user, kdkota, nmbrg;
    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeRefreshLayout;
    private AdapterListItemOpname listAdapter;
    private ArrayList<String> listItem = new ArrayList<>();
    private ArrayList<String> listNmBrg = new ArrayList<>();
    private ArrayList<String> listStatusOpname = new ArrayList<>();
    private ArrayList<String> listSatuan = new ArrayList<>();
    private int jumlah_item = 0;
    private int i = 0;
    private SharedPreferences.Editor editor;
    private static final String TAG = ItemOpnameActivity.class.getSimpleName();
    private static int REQUEST_FORM = 1;
    private static int REQUEST_FILTER_ITEM = 2;
    private boolean isFilter = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_opname);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        SessionManager sessionManager = new SessionManager(getApplicationContext());
        HashMap<String, String> cache = sessionManager.getUserDetails();
        kdkota = cache.get(SessionManager.kdkota);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        editor = sharedPreferences.edit();
        status_opname = sharedPreferences.getString("status_opname","");
        status = sharedPreferences.getString("status","");
        NoOpname = sharedPreferences.getString("no_opname","");
        kota = sharedPreferences.getString("kota","");
        tgl = sharedPreferences.getString("tgl","");
        user = sharedPreferences.getString("user","");
        nmbrg = sharedPreferences.getString("nmbrg","");
        this.setTitle(NoOpname);

        // get the listview
        swipeRefreshLayout = findViewById(R.id.swipe_refresh);
        progressBar = findViewById(R.id.progressbar);
        RecyclerView rvItem = findViewById(R.id.lv_item);
        rvItem.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        rvItem.setLayoutManager(layoutManager);
        progressBar.setVisibility(View.GONE);
        listAdapter = new AdapterListItemOpname(ItemOpnameActivity.this, listItem, listNmBrg, listStatusOpname);
        rvItem.setAdapter(listAdapter);

        if (status_opname.equalsIgnoreCase("")){
            selectItem(0);
        } else {
            filter();
        }

        FloatingActionButton fab_nomor = findViewById(R.id.fab_sub1);
        Button btn_sort = findViewById(R.id.btn_sort);
        Button btn_filter = findViewById(R.id.btn_filter);

        btn_sort.setOnClickListener(v -> {
            final CharSequence[] items = { "A-Z", "Z-A" };
            AlertDialog.Builder builder = new AlertDialog.Builder(ItemOpnameActivity.this);
            builder.setTitle("Sorting");
            builder.setItems(items, (dialog, item) -> {
                if (items[item].equals("A-Z")){
                    Collections.sort(listItem);
                    listAdapter.notifyDataSetChanged();
                    dialog.dismiss();
                } else {
                    Collections.reverse(listItem);
                    listAdapter.notifyDataSetChanged();
                    dialog.dismiss();
                }
            });
            AlertDialog alert = builder.create();
            alert.show();
        });

        btn_filter.setOnClickListener(v -> {
            editor.putString("nmbrg", nmbrg);
            editor.putString("status_opname", status_opname);
            editor.apply();

            Intent intent = new Intent(ItemOpnameActivity.this, FilterItemOpname.class);
            startActivityForResult(intent, REQUEST_FILTER_ITEM);
        });

        fab_nomor.setOnClickListener(view -> {
            if (jumlah_item > 10){
                Toast.makeText(ItemOpnameActivity.this, " Tambah Nomor Opname baru...!!! \n Jumlah item opname sudah mencapai 25 item", Toast.LENGTH_LONG).show();
            } else {
                editor.putString("status", "create");
                editor.putString("no_opname", NoOpname);
                editor.putString("kota", kota);
                editor.putString("tgl", tgl);
                editor.putString("user", user);
                editor.commit();

                Intent intent = new Intent(ItemOpnameActivity.this, FilterFormStockOpname.class);
                startActivityForResult(intent, REQUEST_FORM);
            }
        });

        rvItem.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int visibleItemCount = layoutManager.getChildCount();
                int totalItemCount = layoutManager.getItemCount();
                int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();
                if (firstVisibleItemPosition + visibleItemCount >= totalItemCount) {
                    if(i == 0){
                        selectItem(totalItemCount);
                        i = 1;
                    }
                } else { i = 0; }
            }
        });

        swipeRefreshLayout.setOnRefreshListener(() -> {
            listItem.clear();
            listNmBrg.clear();
            listSatuan.clear();
            listStatusOpname.clear();
            selectItem(0);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_FORM && resultCode == RESULT_OK) {
            selectItem(0);
        } else if(requestCode == REQUEST_FILTER_ITEM && resultCode == RESULT_OK){
            status_opname = data.getStringExtra(FilterItemOpname.STATUS_OPNAME);
            nmbrg = data.getStringExtra(FilterItemOpname.NMBRG);
            listItem.clear();
            listNmBrg.clear();
            listSatuan.clear();
            listStatusOpname.clear();
            filter();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home){
            List<Opname> listData = ArrayTampung.getListOpname();
            listData.clear();
            editor.putString("tgl_awal", "");
            editor.putString("tgl_akhir", "");
            editor.putString("nmbrg", "");
            editor.putString("status_opname", "");
            editor.putString("status", "view");
            editor.commit();
            Intent intent = new Intent(ItemOpnameActivity.this, StockOpnameActivity.class);
            setResult(Activity.RESULT_OK, intent);
            this.finish();
        }
        return super.onOptionsItemSelected(item);
    }

    //fungsi untuk select data dari database
    public void selectItem(int itemCount) {
        progressBar.setVisibility(View.VISIBLE);
        Server a = new Server(kdkota);
        String url = a.URL() + "stockopname/view/select_list_item_opname.php";
        StringRequest strReq = new StringRequest(Request.Method.POST, url, response -> {
            Log.d(TAG, "Response : " + response);
            try {
                JSONObject jsonObject = new JSONObject(response);
                JSONArray result = jsonObject.getJSONArray("result");
                for (int i = 0; i < result.length(); i++) {
                    try {
                        JSONObject obj = result.getJSONObject(i);
                        listItem.add(obj.getString("KdBrg"));
                        listNmBrg.add(obj.getString("NmBrg"));
                        listStatusOpname.add(obj.getString("StatusOpname"));
                        listSatuan.add(obj.getString("Satuan"));
                        jumlah_item = jumlah_item + 1;
                    } catch (JSONException e) {
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
            new DialogAlert(error.getMessage(), "error", ItemOpnameActivity.this);
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("no_opname", NoOpname);
                params.put("itemCount", String.valueOf(itemCount));
                return params;
            }
        };
        AppController.getInstance().addToRequestQueue(strReq, getString(R.string.tag_json_obj));
    }

    private class AdapterListItemOpname extends RecyclerView.Adapter <AdapterListItemOpname.ViewHolder> {

        private ArrayList<String> lvItem;
        private ArrayList<String> lvNmBrg;
        private ArrayList<String> lvStatusOpname;
        private Activity activity;

        AdapterListItemOpname(Activity activity, ArrayList<String> lvItem, ArrayList<String> lvNmBrg, ArrayList<String> lvStatusOpname){
            this.activity = activity;
            this.lvItem = lvItem;
            this.lvNmBrg = lvNmBrg;
            this.lvStatusOpname = lvStatusOpname;
        }

        @NotNull
        @Override
        public ViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.lv_item_opname, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NotNull ViewHolder holder, int position) {
            holder.cvList.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.show_from_left));
            holder.tvItemOpname.setText(lvItem.get(position));
            holder.tvNmBrg.setText(lvNmBrg.get(position));
            holder.tvStatus.setText(lvStatusOpname.get(position));

            holder.cvList.setOnClickListener(view -> {
                editor.putString("status", status);
                editor.putString("no_opname", NoOpname);
                editor.putString("kota", kota);
                editor.putString("tgl", tgl);
                editor.putString("user", user);
                editor.putString("kdbrg", listItem.get(position));
                editor.putString("nmbrg", listNmBrg.get(position));
                editor.putString("status_opname", listStatusOpname.get(position));
                editor.commit();

                Intent intent = new Intent(activity, FormStockOpnameActivity.class);
                startActivityForResult(intent, REQUEST_FORM);
            });
        }

        @Override
        public int getItemCount() {
            return lvItem.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {

            private TextView tvItemOpname;
            private TextView tvNmBrg;
            private TextView tvStatus;
            private CardView cvList;

            ViewHolder(View itemView) {
                super(itemView);
                tvItemOpname = itemView.findViewById(R.id.tv_item_opname);
                tvNmBrg = itemView.findViewById(R.id.tv_nmbrg_opname);
                tvStatus = itemView.findViewById(R.id.tv_status_opname);
                cvList = itemView.findViewById(R.id.cv_main);
            }
        }
    }

    //fungsi untuk select data dari database
    public void filter() {
        progressBar.setVisibility(View.VISIBLE);
        Server a = new Server(kdkota);
        String url = a.URL() + "stockopname/view/filter_list_item.php";
        StringRequest strReq = new StringRequest(Request.Method.POST, url, response -> {
            Log.v(TAG, "Response : " + response);
            setRV(response);
            listAdapter.notifyDataSetChanged();
        }, error -> {
            progressBar.setVisibility(View.GONE);
            new DialogAlert(error.getMessage(), "error", ItemOpnameActivity.this);
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("nmbrg", nmbrg);
                params.put("status_opname", status_opname);
                params.put("no_opname", NoOpname);
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
                    listItem.add(obj.getString("KdBrg"));
                    listNmBrg.add(obj.getString("NmBrg"));
                    listStatusOpname.add(obj.getString("StatusOpname"));
                    listSatuan.add(obj.getString("Satuan"));
                } catch (JSONException e) {
                    progressBar.setVisibility(View.GONE);
                    e.printStackTrace();
                }
            }
            isFilter = true;
            progressBar.setVisibility(View.GONE);
            listAdapter.notifyDataSetChanged();
        } catch (JSONException e) {
            progressBar.setVisibility(View.GONE);
            e.printStackTrace();
        }
    }

}
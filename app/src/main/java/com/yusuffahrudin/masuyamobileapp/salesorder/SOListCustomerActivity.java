package com.yusuffahrudin.masuyamobileapp.salesorder;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.yusuffahrudin.masuyamobileapp.R;
import com.yusuffahrudin.masuyamobileapp.controller.AppController;
import com.yusuffahrudin.masuyamobileapp.controller.DialogAlert;
import com.yusuffahrudin.masuyamobileapp.data.Customer;
import com.yusuffahrudin.masuyamobileapp.util.Server;
import com.yusuffahrudin.masuyamobileapp.util.SessionManager;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SOListCustomerActivity extends AppCompatActivity{

    private AdapterRV adapterRV;
    private ArrayList<Customer> listCustomer = new ArrayList<>();
    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeRefreshLayout;
    private static String name, level, kdkota;
    private static final String TAG = SOListCustomerActivity.class.getSimpleName();
    private int i = 0;

    public static final String TAG_KDCUST = "KdCust";
    public static final String TAG_NMCUST = "NmCust";
    public static final String TAG_KDKEL = "KdKel";
    public static final String TAG_ALM1 = "Alm1";
    public static final String TAG_ALM2 = "Alm2";
    public static final String TAG_ALM3 = "Alm3";
    public static final String TAG_KDSALES = "KdSales";
    public static final String TAG_NMSALES = "NmSales";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setTitle("List Customer");
        setContentView(R.layout.activity_customer);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        SessionManager sessionManager = new SessionManager(getApplicationContext());
        HashMap<String, String> user = sessionManager.getUserDetails();
        name = user.get(SessionManager.kunci_email);
        level = user.get(SessionManager.level);
        kdkota = user.get(SessionManager.kdkota);

        //menghubungkan variabel dengan layout view dan java
        RecyclerView rv_customer = findViewById(R.id.lv_cust);
        rv_customer.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        rv_customer.setLayoutManager(layoutManager);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh);
        progressBar = findViewById(R.id.progressbar);
        progressBar.setVisibility(View.GONE);

        //untuk mengisi data dari JSON ke Adapter
        adapterRV = new AdapterRV(SOListCustomerActivity.this, listCustomer);
        rv_customer.setAdapter(adapterRV);

        if(listCustomer.isEmpty()){ selectCustomer(0); }

        rv_customer.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int visibleItemCount = layoutManager.getChildCount();
                int totalItemCount = layoutManager.getItemCount();
                int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();
                if (firstVisibleItemPosition + visibleItemCount >= totalItemCount) {
                    if(i == 0){
                        selectCustomer(totalItemCount);
                        i = 1;
                    }
                } else { i = 0; }
            }
        });

        swipeRefreshLayout.setOnRefreshListener(() -> {
            listCustomer.clear();
            selectCustomer(0);
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_refresh, menu);
        MenuItem search = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) search.getActionView();
        search(searchView);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home){ this.finish(); }
        if(id == R.id.action_refresh){
            listCustomer.clear();
            selectCustomer(0);
        }
        return super.onOptionsItemSelected(item);
    }

    private void search(SearchView searchView) {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                listCustomer.clear();
                searchCustomer(query);
                return true;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }

    public void searchCustomer(String cust) {
        progressBar.setVisibility(View.VISIBLE);
        Server a = new Server(kdkota);
        String url = a.URL() + "customer/search_customer.php";
        StringRequest strReq = new StringRequest(Request.Method.POST, url, response -> {
            Log.d(TAG, "Response search : " + response);
            setRV(response);
        }, error -> {
            progressBar.setVisibility(View.GONE);
            Log.e(TAG, "Error Volley : "+ error.getMessage());
            FirebaseCrashlytics.getInstance().recordException(error);
            new DialogAlert(error.getMessage(), "error", SOListCustomerActivity.this);
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                if(level.equalsIgnoreCase(getResources().getString(R.string.sales))){
                    params.put("user", name);
                } else {
                    params.put("user", "");
                }
                params.put("cust", cust);
                return params;
            }
        };
        AppController.getInstance().addToRequestQueue(strReq, getString(R.string.tag_json_obj));
    }

    public void selectCustomer(int itemCount) {
        progressBar.setVisibility(View.VISIBLE);
        Server a = new Server(kdkota);
        String url_select = a.URL() + "customer/select_customer.php";
        StringRequest strReq = new StringRequest(Request.Method.POST, url_select, response -> {
            Log.d(TAG, "Response select : " + response);
            setRV(response);
            adapterRV.notifyDataSetChanged();
        }, error -> {
            progressBar.setVisibility(View.GONE);
            FirebaseCrashlytics.getInstance().recordException(error);
            new DialogAlert(error.getMessage(), "error", SOListCustomerActivity.this);
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                if(level.equalsIgnoreCase("sales")){
                    params.put("user", name);
                } else {
                    params.put("user", "");
                }
                params.put("itemCount", String.valueOf(itemCount));
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

                    Customer item = new Customer();
                    System.out.println();
                    item.setKdcust(obj.getString(TAG_KDCUST));
                    item.setNmcust(obj.getString(TAG_NMCUST));
                    item.setKdkel(obj.getString(TAG_KDKEL));
                    item.setAlm1(obj.getString(TAG_ALM1));
                    item.setAlm2(obj.getString(TAG_ALM2));
                    item.setAlm3(obj.getString(TAG_ALM3));
                    item.setKdsales(obj.getString(TAG_KDSALES));
                    item.setSales(obj.getString(TAG_NMSALES));

                    //menambah item ke array
                    listCustomer.add(item);
                } catch (JSONException e) {
                    progressBar.setVisibility(View.GONE);
                    e.printStackTrace();
                }
            }
            i++;
            progressBar.setVisibility(View.GONE);
            adapterRV.notifyDataSetChanged();
            swipeRefreshLayout.setRefreshing(false);
        } catch (JSONException e) {
            progressBar.setVisibility(View.GONE);
            e.printStackTrace();
            FirebaseCrashlytics.getInstance().recordException(e);
        }
    }

    public class AdapterRV extends RecyclerView.Adapter <AdapterRV.ViewHolder> {
        private ArrayList<Customer> lvCust;
        private Activity activity;

        AdapterRV(Activity activity, ArrayList<Customer> tampung){
            this.activity = activity;
            this.lvCust = tampung;
        }

        @NotNull
        @Override
        public ViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_customer, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NotNull ViewHolder holder, int position) {
            holder.tvKdCust.setText(lvCust.get(position).getKdcust());
            holder.tvNmCust.setText(lvCust.get(position).getNmcust());

            holder.cvList.setOnClickListener(view -> {
                Intent intent = new Intent(activity, CreateSOActivity.class);
                intent.putExtra(TAG_KDCUST, lvCust.get(position).getKdcust());
                intent.putExtra(TAG_NMCUST, lvCust.get(position).getNmcust());
                intent.putExtra(TAG_KDKEL, lvCust.get(position).getKdkel());
                intent.putExtra(TAG_ALM1, lvCust.get(position).getAlm1());
                intent.putExtra(TAG_ALM2, lvCust.get(position).getAlm2());
                intent.putExtra(TAG_ALM3, lvCust.get(position).getAlm3());
                intent.putExtra(TAG_KDSALES, lvCust.get(position).getKdsales());
                intent.putExtra(TAG_NMSALES, lvCust.get(position).getSales());
                activity.setResult(RESULT_OK, intent);
                activity.finish();
            });
        }

        @Override
        public int getItemCount() {
            return lvCust.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            private TextView tvKdCust;
            private TextView tvNmCust;
            private CardView cvList;

            public ViewHolder(View itemView) {
                super(itemView);
                tvKdCust = itemView.findViewById(R.id.tv_kdcust);
                tvNmCust = itemView.findViewById(R.id.tv_nmcust);
                cvList = itemView.findViewById(R.id.cv_main);
            }
        }
    }
}

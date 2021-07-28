package com.yusuffahrudin.masuyamobileapp.customer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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

public class CustomerActivity extends AppCompatActivity {

    private AdapterRV adapterRV;
    private ArrayList<Customer> listCust = new ArrayList<>();
    private static String name, level, kdkota;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar progressBar;
    private static final String TAG = CustomerActivity.class.getSimpleName();
    private int i = 0;
    private boolean isSearch = false;

    public static final String TAG_KDCUST = "KdCust";
    public static final String TAG_NMCUST = "NmCust";
    public static final String TAG_TYPECUST = "TypeCust";
    public static final String TAG_ALM1 = "Alm1";
    public static final String TAG_KOTA = "Kota";
    public static final String TAG_TELP1 = "Telp1";
    public static final String TAG_TELP2 = "Telp2";
    public static final String TAG_SALDO = "Saldo";
    public static final String TAG_KOORDINAT = "Koordinat";
    public static final String TAG_SALES = "NmSales";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setTitle(getString(R.string.my_customer));
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
        Toast.makeText(CustomerActivity.this, name, Toast.LENGTH_LONG).show();

        //menghubungkan variabel dengan layout view dan java
        RecyclerView rv_customer = findViewById(R.id.lv_cust);
        rv_customer.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        rv_customer.setLayoutManager(layoutManager);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh);
        progressBar = findViewById(R.id.progressbar);
        progressBar.setVisibility(View.GONE);
        adapterRV = new AdapterRV(CustomerActivity.this, listCust);
        rv_customer.setAdapter(adapterRV);

        if(listCust.isEmpty()){ selectCustomer(0); }

        rv_customer.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int visibleItemCount = layoutManager.getChildCount();
                int totalItemCount = layoutManager.getItemCount();
                int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();
                if (firstVisibleItemPosition + visibleItemCount >= totalItemCount) {
                    if(i == 0){
                        if (!isSearch){
                            selectCustomer(totalItemCount);
                            i = 1;
                        }
                    }
                } else { i = 0; }
            }
        });

        swipeRefreshLayout.setOnRefreshListener(() -> {
            listCust.clear();
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
        if (id == android.R.id.home){
            this.finish();
        }
        if(id == R.id.action_refresh){
            listCust.clear();
            selectCustomer(0);
        }
        return super.onOptionsItemSelected(item);
    }

    private void search(SearchView searchView) {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                listCust.clear();
                searchCustomer(query);
                return true;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }

    //fungsi untuk select data dari database
    private void selectCustomer(int itemCount) {
        progressBar.setVisibility(View.VISIBLE);
        Server a = new Server(kdkota);
        String url = a.URL() + "customer/select_customer.php";
        StringRequest strReq = new StringRequest(Request.Method.POST, url, response -> {
            Log.d(TAG, "Response select : " + response);
            isSearch = false;
            setRV(response);
        }, error -> {
            progressBar.setVisibility(View.GONE);
            Log.e(TAG, "Error Volley : "+ error.getMessage());
            new DialogAlert(error.getMessage(), "error", CustomerActivity.this);
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                if(level.equals("1010201010") || level.equals("1010301010")){
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

    //fungsi untuk select data dari database
    private void searchCustomer(String cust) {
        progressBar.setVisibility(View.VISIBLE);
        Server a = new Server(kdkota);
        String url = a.URL() + "customer/search_customer.php";
        StringRequest strReq = new StringRequest(Request.Method.POST, url, response -> {
            Log.d(TAG, "Response search : " + response);
            isSearch = true;
            setRV(response);
        }, error -> {
            progressBar.setVisibility(View.GONE);
            Log.e(TAG, "Error Volley : "+ error.getMessage());
            new DialogAlert(error.getMessage(), "error", CustomerActivity.this);
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                if(level.equals("1010201010") || level.equals("1010301010")){
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

    //fungsi untuk memasukkan data dari database ke dalam arraylist
    private void setRV(String response){
        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONArray result = jsonObject.getJSONArray("result");
            for (int i = 0; i < result.length(); i++) {
                try {
                    JSONObject obj = result.getJSONObject(i);

                    Customer item = new Customer();

                    item.setKdcust(obj.getString(TAG_KDCUST));
                    item.setNmcust(obj.getString(TAG_NMCUST));
                    item.setTypecust(obj.getString(TAG_TYPECUST));
                    item.setKdkel(obj.getString("KdKel"));
                    item.setAlm1(obj.getString(TAG_ALM1));
                    item.setAlm2(obj.getString("Alm2"));
                    item.setAlm3(obj.getString("Alm3"));
                    item.setKota(obj.getString(TAG_KOTA));
                    item.setTelp1(obj.getString(TAG_TELP1));
                    item.setTelp2(obj.getString(TAG_TELP2));
                    item.setSaldo(obj.getDouble(TAG_SALDO));
                    item.setKoordinat(obj.getString(TAG_KOORDINAT));
                    item.setTop(obj.getString("LamaKredit"));
                    item.setKreditLimit(obj.getDouble("KreditLimit"));
                    item.setSales(obj.getString(TAG_SALES));
                    item.setKdsales(obj.getString("KdSales"));
                    //menambah item ke array
                    listCust.add(item);
                } catch (JSONException e) {
                    progressBar.setVisibility(View.GONE);
                    e.printStackTrace();
                }
            }
            i++;
            adapterRV.notifyDataSetChanged();
            progressBar.setVisibility(View.GONE);
            swipeRefreshLayout.setRefreshing(false);
        } catch (JSONException e) {
            progressBar.setVisibility(View.GONE);
            e.printStackTrace();
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
            holder.cvList.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.show_from_bottom));
            holder.cvList.setOnClickListener(view -> {
                Intent intent = new Intent(activity, CustomerDetailActivity.class);
                intent.putExtra(CustomerDetailFragment.Companion.getEXTRA_CUSTOMER(), lvCust.get(position));
                activity.startActivity(intent);
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

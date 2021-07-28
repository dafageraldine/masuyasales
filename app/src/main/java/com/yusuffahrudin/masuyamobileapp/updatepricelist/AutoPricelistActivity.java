package com.yusuffahrudin.masuyamobileapp.updatepricelist;

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

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.yusuffahrudin.masuyamobileapp.R;
import com.yusuffahrudin.masuyamobileapp.controller.AppController;
import com.yusuffahrudin.masuyamobileapp.controller.DialogAlert;
import com.yusuffahrudin.masuyamobileapp.data.ArrayTampung;
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

/**
 * Created by yusuf fahrudin on 17-05-2017.
 */

public class AutoPricelistActivity extends AppCompatActivity {

    private String kdkota;
    private final ArrayList<Customer> itemList = new ArrayList<>();
    private Adapter adapter; String name, level;
    private ProgressBar progressBar;
    private static final String TAG = AutoPricelistActivity.class.getSimpleName();
    private int i = 0;
    private boolean isSearch = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setTitle("Auto Update Pricelist");
        setContentView(R.layout.activity_auto_pricelist);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        RecyclerView rv_cust = findViewById(R.id.lv_cust);
        rv_cust.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        rv_cust.setLayoutManager(layoutManager);
        progressBar = findViewById(R.id.progressbar);
        progressBar.setVisibility(View.GONE);

        SessionManager sessionManager = new SessionManager(getApplicationContext());
        HashMap<String, String> user = sessionManager.getUserDetails();
        name = user.get(SessionManager.kunci_email);
        level = user.get(SessionManager.level);
        kdkota = user.get(SessionManager.kdkota);
        Toast.makeText(AutoPricelistActivity.this, name, Toast.LENGTH_LONG).show();

        adapter = new Adapter(AutoPricelistActivity.this, itemList);
        rv_cust.setAdapter(adapter);

        if(itemList.isEmpty()){ getData(0); }

        rv_cust.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int visibleItemCount = layoutManager.getChildCount();
                int totalItemCount = layoutManager.getItemCount();
                int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();
                if (firstVisibleItemPosition + visibleItemCount >= totalItemCount) {
                    if(i == 0){
                        if(!isSearch){
                            getData(totalItemCount);
                            i = 1;
                        }
                    }
                } else { i = 0; }
            }
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
            itemList.clear();
            getData(0);
        }
        return super.onOptionsItemSelected(item);
    }

    private void search(SearchView searchView) {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                itemList.clear();
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
            new DialogAlert(error.getMessage(), "error", AutoPricelistActivity.this);
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

    private void getData(int itemCount) {
        progressBar.setVisibility(View.VISIBLE);
        Server a = new Server(kdkota);
        String url = a.URL() + "customer/select_customer.php";
        StringRequest strReq = new StringRequest(Request.Method.POST, url, response -> {
            Log.v(TAG, "Response : " + response);
            isSearch = false;
            setRV(response);
        }, error -> {
            progressBar.setVisibility(View.GONE);
            new DialogAlert(error.getMessage(), "error", AutoPricelistActivity.this);
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

    //fungsi untuk memasukkan data dari database ke dalam arraylist
    private void setRV(String response){
        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONArray result = jsonObject.getJSONArray("result");
            for (int i = 0; i < result.length(); i++) {
                try {
                    JSONObject obj = result.getJSONObject(i);

                    Customer item = new Customer();
                    item.setKdcust(obj.getString("KdCust"));
                    item.setNmcust(obj.getString("NmCust"));
                    item.setAlm1(obj.getString("Alm1"));
                    itemList.add(item);
                } catch (JSONException e) {
                    progressBar.setVisibility(View.GONE);
                    e.printStackTrace();
                    Log.e(TAG, "Error SQL "+e.getMessage());
                }
            }
            i++;
            progressBar.setVisibility(View.GONE);
            adapter.notifyDataSetChanged();
        } catch (JSONException e) {
            progressBar.setVisibility(View.GONE);
            e.printStackTrace();
            Log.e(TAG, "Error SQL "+e.getMessage());
        }
    }

    private class Adapter extends RecyclerView.Adapter <Adapter.ViewHolder> {
        private final ArrayList<Customer> lvCust;
        private final Activity activity;

        Adapter(Activity activity, ArrayList<Customer> tampung){
            this.activity = activity;
            this.lvCust = tampung;
        }

        @NotNull
        @Override
        public ViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.lv_add_customer_khusus, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NotNull ViewHolder holder, int position) {
            holder.cvList.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.show_from_left));
            holder.tvKdCust.setText(lvCust.get(position).getKdcust());
            holder.tvNmCust.setText(lvCust.get(position).getNmcust());
            holder.tvAlamat.setText(lvCust.get(position).getAlm1());

            holder.cvList.setOnClickListener(view -> {
                Intent intent = new Intent(activity, DetailAutoPricelist.class);
                intent.putExtra("kdcust", lvCust.get(position).getKdcust());
                startActivity(intent);
                ArrayTampung.getListData().clear();
                Toast.makeText(activity, lvCust.get(position).getKdcust(), Toast.LENGTH_LONG).show();
            });
        }

        @Override
        public int getItemCount() {
            return lvCust.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {

            private final TextView tvKdCust;
            private final TextView tvNmCust;
            private final TextView tvAlamat;
            private final CardView cvList;

            ViewHolder(View itemView) {
                super(itemView);
                tvKdCust = itemView.findViewById(R.id.tv_kdcust);
                tvNmCust = itemView.findViewById(R.id.tv_nmcust);
                tvAlamat = itemView.findViewById(R.id.tv_alamat);
                cvList = itemView.findViewById(R.id.cv_main);
            }
        }
    }
}

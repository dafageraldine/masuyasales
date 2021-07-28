package com.yusuffahrudin.masuyamobileapp.updatepricelist;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.yusuffahrudin.masuyamobileapp.R;
import com.yusuffahrudin.masuyamobileapp.adapter.AdapterLVUpdatePriceProduk;
import com.yusuffahrudin.masuyamobileapp.controller.AppController;
import com.yusuffahrudin.masuyamobileapp.controller.DialogAlert;
import com.yusuffahrudin.masuyamobileapp.data.Customer;
import com.yusuffahrudin.masuyamobileapp.util.Server;
import com.yusuffahrudin.masuyamobileapp.util.SessionManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UpdatePriceBrgActivity extends AppCompatActivity {
    private ListView listView;
    private AdapterLVUpdatePriceProduk adapter;
    List<Customer> listData = new ArrayList<>();
    int success;
    public String kdbrg, kdbrgx, nmbrgx, satuanx, hrgx, hrgincppnx, diskon1x, diskon2x, diskon3x, name, message, kdkota;
    SessionManager sessionManager;

    private static final String TAG = UpdatePriceBrgActivity.class.getSimpleName();
    private static String url_select;
    private static String url_insert;
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";

    String tag_json_obj = "json_obj_req";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setTitle("Update Pricelist Barang");
        setContentView(R.layout.activity_update_price_brg);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Intent i = getIntent();
        kdbrgx = i.getExtras().getString("kdbrg");
        nmbrgx = i.getExtras().getString("nmbrg");
        satuanx = i.getExtras().getString("satuan");

        NumberFormat nf = NumberFormat.getInstance();
        hrgx = nf.format(i.getExtras().getDouble("hrg"));
        hrgincppnx = nf.format(i.getExtras().getDouble("hrgincppn"));
        diskon1x = nf.format(i.getExtras().getDouble("diskon1"));
        diskon2x = nf.format(i.getExtras().getDouble("diskon2"));
        diskon3x = nf.format(i.getExtras().getDouble("diskon3"));

        sessionManager = new SessionManager(getApplicationContext());
        HashMap<String, String> user = sessionManager.getUserDetails();
        name = user.get(SessionManager.kunci_email);
        kdkota = user.get(SessionManager.kdkota);
        Toast.makeText(UpdatePriceBrgActivity.this, name, Toast.LENGTH_LONG).show();

        FloatingActionButton fab_save = findViewById(R.id.fab_save);
        fab_save.setOnClickListener(view -> simpan());

        //menghubungkan variabel dengan layout view dan java
        listView = findViewById(R.id.lv_update_price_brg);
        //swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);

        //untuk mengisi data dari JSON ke Adapter
        adapter = new AdapterLVUpdatePriceProduk(UpdatePriceBrgActivity.this, listData);
        listView.setAdapter(adapter);

        selectCust();

        // listview ditekan lama akan menampilkan dua pilihan edit atau delete data
        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            // TODO Auto-generated method stub

            return false;
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

    private void selectCust(){
        listData.clear();
        adapter.notifyDataSetChanged();
        Server a = new Server(kdkota);
        url_select = a.URL() + "updateprice/select_customer.php";

        final ProgressDialog progressDialog = ProgressDialog.show(UpdatePriceBrgActivity.this, "", "Please Wait...");
        new Thread() {
            public void run() {
                try{
                    sleep(10000);
                } catch (Exception e) {
                    Log.e("tag", e.getMessage());
                }
            }
        }.start();
        //swipeRefreshLayout.setRefreshing(true);

        StringRequest strReq = new StringRequest(Request.Method.POST, url_select, response -> {
            Log.d(TAG, "Response : " + response);
            setRV(response);
            progressDialog.dismiss();

            //notifikasi adanya perubahan data pada adapter
            adapter.notifyDataSetChanged();
        }, error -> {
            Log.e(TAG, "Error Volley : "+ error.getMessage());
            //swipeRefreshLayout.setRefreshing(false);
            new DialogAlert(error.getMessage(), "error", UpdatePriceBrgActivity.this);
            progressDialog.dismiss();
        }) {
            @Override
            protected Map<String, String> getParams() {
                //posting parameter ke post url
                Map<String, String> params = new HashMap<>();

                params.put("user", name);

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

                    Customer item = new Customer();

                    item.setKdcust(obj.getString("KdCust"));
                    item.setNmcust(obj.getString("NmCust"));

                    //menambah item ke array
                    listData.add(item);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            //notifikasi adanya perubahan data pada adapter
            adapter.notifyDataSetChanged();

            //swipeRefreshLayout.setRefreshing(false);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void simpan(){
        Server a = new Server(kdkota);
        url_insert = a.URL() + "updateprice/produk/insert_pricelist_produk.php";

        StringRequest strReq = new StringRequest(Request.Method.POST, url_insert, response -> {
            Log.v(TAG, "Response : "+ response);

            try{
                JSONObject jObj = new JSONObject(response);
                success = jObj.getInt(TAG_SUCCESS);
                message = jObj.getString(TAG_MESSAGE);

                //cek error node pada JSON
                if (success == 1){
                    new DialogAlert(message, "success", UpdatePriceBrgActivity.this);
                    listData.clear();

                    //adapter.notifyDataSetChanged();
                } else {
                    new DialogAlert(message, "error", UpdatePriceBrgActivity.this);
                    listData.clear();
                }
            } catch (JSONException e){
                e.printStackTrace();
            }
        }, error -> new DialogAlert(error.getMessage(), "error", UpdatePriceBrgActivity.this)) {
            @Override
            protected Map<String, String> getParams() {
                //posting parameter ke post url
                Map<String, String> param = new HashMap<>();
                JSONArray jParams = new JSONArray();
                JSONObject array = null;
                try {
                    for (int i = 0; i < listData.size(); i++){
                        Number harga = NumberFormat.getInstance().parse(hrgx);
                        Number hrgincppn = NumberFormat.getInstance().parse(hrgincppnx);
                        Number diskon1 = NumberFormat.getInstance().parse(diskon1x);
                        Number diskon2 = NumberFormat.getInstance().parse(diskon2x);
                        Number diskon3 = NumberFormat.getInstance().parse(diskon3x);

                        array = new JSONObject();

                        array.put("kdcust", listData.get(i).getKdcust());
                        array.put("kdbrg", kdbrgx.toUpperCase());
                        array.put("satuan", satuanx);
                        array.put("hrg", Double.valueOf(harga.toString()));
                        array.put("hrgincppn", Double.valueOf(hrgincppn.toString()));
                        array.put("diskon1", Double.valueOf(diskon1.toString()));
                        array.put("diskon2", Double.valueOf(diskon2.toString()));
                        array.put("diskon3", Double.valueOf(diskon3.toString()));

                        jParams.put(array);
                    }
                } catch (JSONException | ParseException e) {
                    e.printStackTrace();
                }

                param.put("array", jParams.toString());
                Log.v("Response : ", param.toString());

                return param;
            }
        };

        AppController.getInstance().addToRequestQueue(strReq, tag_json_obj);

    }

}

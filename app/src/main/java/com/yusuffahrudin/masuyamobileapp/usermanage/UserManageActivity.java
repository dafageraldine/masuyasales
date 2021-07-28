package com.yusuffahrudin.masuyamobileapp.usermanage;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.yusuffahrudin.masuyamobileapp.R;
import com.yusuffahrudin.masuyamobileapp.controller.AppController;
import com.yusuffahrudin.masuyamobileapp.controller.DialogAlert;
import com.yusuffahrudin.masuyamobileapp.util.Server;
import com.yusuffahrudin.masuyamobileapp.util.SessionManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserManageActivity extends AppCompatActivity {
    private CheckBox cbBottomPrice, cbHPP;
    private String user="";
    private String kdkota;
    private AutoCompleteTextView spin_user, spin_kota, spin_level;
    private ArrayList<String> listData = new ArrayList<>(), listLevel = new ArrayList<>();
    private ArrayAdapter adapterUser, adapterLevel;
    private ArrayAdapter<String> adapterKota;
    private String [] kotaArray;
    private List<String> listKota;
    private ProgressDialog pDialog;
    private SessionManager sessionManager;
    private ProgressDialog progressDialog;

    private static final String TAG = UserManageActivity.class.getSimpleName();

    @SuppressLint("WrongViewCast")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setTitle("UserAkses Management");
        setContentView(R.layout.activity_user);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        sessionManager = new SessionManager(getApplicationContext());
        HashMap<String, String> cache = sessionManager.getUserDetails();
        kdkota = cache.get(SessionManager.kdkota);
        String user_kota = cache.get(SessionManager.kota);

        spin_user = findViewById(R.id.spin_user);
        spin_kota = findViewById(R.id.spin_kota);
        spin_level = findViewById(R.id.spin_level);
        Button btn_simpan = findViewById(R.id.btn_simpan);
        Button btn_detail = findViewById(R.id.btn_detail);
        cbBottomPrice = findViewById(R.id.cb_bottom_price);
        cbHPP = findViewById(R.id.cb_hpp);

        //mengisi spinner kota
        listKota = new ArrayList<>();
        assert user_kota != null;
        if (user_kota.equalsIgnoreCase("ALL")){
            new GetKota1().execute();
        } else {
            kotaArray = new String[]{user_kota};
            adapterKota = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, kotaArray);
            spin_kota.setAdapter(adapterKota);
        }

        adapterUser = new ArrayAdapter(this, R.layout.support_simple_spinner_dropdown_item, listData);
        spin_user.setAdapter(adapterUser);
        spin_user.setThreshold(0);
        spin_user.setOnFocusChangeListener((v, hasFocus) -> {
            spin_user.showDropDown();
        });
        spin_user.setOnClickListener(v -> spin_user.showDropDown());
        spin_user.setOnItemClickListener((parent, view, position, id) -> {
            user = parent.getItemAtPosition(position).toString();
            getDetailUser();
        });

        adapterLevel = new ArrayAdapter(this, R.layout.support_simple_spinner_dropdown_item, listLevel);
        spin_level.setAdapter(adapterLevel);
        spin_level.setThreshold(0);
        spin_level.setOnFocusChangeListener((v, hasFocus) -> spin_level.showDropDown());
        spin_level.setOnClickListener(v -> spin_level.showDropDown());

        spin_kota.setThreshold(0);
        spin_kota.setOnFocusChangeListener((v, hasFocus) -> spin_kota.showDropDown());
        spin_kota.setOnClickListener(v -> spin_kota.showDropDown());

        getUser();

        btn_simpan.setOnClickListener(v -> simpanUserLevel());

        btn_detail.setOnClickListener(v -> {
            Toast.makeText(UserManageActivity.this,
                    spin_level.getText(), Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(UserManageActivity.this, UserAksesActivity.class);
            intent.putExtra("level", spin_level.getText().toString().substring(0, spin_level.getText().toString().indexOf(" ")));
            startActivity(intent);
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

    public void getUser() {
        listData.clear();
        Server a = new Server(kdkota);
        String url = a.URL() + "usermanagement/select_user.php";

        progressDialog = ProgressDialog.show(UserManageActivity.this, "", "Please Wait...");
        new Thread() {
            public void run() {
                try{
                    sleep(10000);
                } catch (Exception e) {
                    Log.e("tag", e.getMessage());
                }
            }
        }.start();

        StringRequest strReq = new StringRequest(Request.Method.POST, url, response -> {
            Log.v(TAG, "Response : " + response);
            try {
                JSONObject jsonObject = new JSONObject(response);
                JSONArray result = jsonObject.getJSONArray("result");
                for (int i = 0; i < result.length(); i++) {
                    try {
                        JSONObject obj = result.getJSONObject(i);
                        listData.add(obj.getString("UID"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                //notifikasi adanya perubahan data pada adapter
                adapterUser.notifyDataSetChanged();
                getLevel();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> new DialogAlert(error.getMessage(), "error", UserManageActivity.this));
        AppController.getInstance().addToRequestQueue(strReq, getResources().getString(R.string.tag_json_obj));
    }

    public void getDetailUser() {
        Server a = new Server(kdkota);
        String url = a.URL() + "usermanagement/select_user_detail.php";

        StringRequest strReq = new StringRequest(Request.Method.POST, url, response -> {
            Log.v(TAG, "Response : " + response);
            try {
                JSONObject jsonObject = new JSONObject(response);
                JSONArray result = jsonObject.getJSONArray("result");
                for (int i = 0; i < result.length(); i++) {
                    try {
                        JSONObject obj = result.getJSONObject(i);
                        spin_level.setText(obj.getString("KdLevel")+"  -  "+obj.getString("NmLevel"), false);
                        spin_kota.setText(obj.getString("KdKota"), false);
                        if (obj.getInt("BottomPrice") == 1){ cbBottomPrice.setChecked(true); }
                        else { cbBottomPrice.setChecked(false); }
                        if (obj.getInt("HPP") == 1){ cbHPP.setChecked(true); }
                        else { cbHPP.setChecked(false); }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                adapterUser.notifyDataSetChanged();
                adapterKota.notifyDataSetChanged();
                adapterLevel.notifyDataSetChanged();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> new DialogAlert(error.getMessage(), "error", UserManageActivity.this)) {
            @Override
            protected Map<String, String> getParams() {
                //posting parameter ke post url
                Map<String, String> params = new HashMap<>();
                params.put("user", user);
                return params;
            }
        };
        AppController.getInstance().addToRequestQueue(strReq, getResources().getString(R.string.tag_json_obj));
    }

    public void getLevel() {
        listLevel.clear();
        Server a = new Server(kdkota);
        String url = a.URL() + "usermanagement/select_level.php";

        StringRequest strReq = new StringRequest(Request.Method.POST, url, response -> {
            Log.v(TAG, "Response : " + response);
            try {
                JSONObject jsonObject = new JSONObject(response);
                JSONArray result = jsonObject.getJSONArray("result");
                for (int i = 0; i < result.length(); i++) {
                    try {
                        JSONObject obj = result.getJSONObject(i);
                        listLevel.add(obj.getString("KdLevel")+"  -  "+obj.getString("NmLevel"));
                        progressDialog.dismiss();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                adapterLevel.notifyDataSetChanged();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> {
            new DialogAlert(error.getMessage(), "error", UserManageActivity.this);
            progressDialog.dismiss();
        });
        AppController.getInstance().addToRequestQueue(strReq, getResources().getString(R.string.tag_json_obj));
    }

    //menyimpan data user level
    private void simpanUserLevel(){
        Server a = new Server(kdkota);
        String url = a.URL() + "usermanagement/insert_user_level.php";

        progressDialog = ProgressDialog.show(UserManageActivity.this, "", "Please Wait...");
        new Thread() {
            public void run() {
                try{
                    sleep(10000);
                } catch (Exception e) {
                    Log.e("tag", e.getMessage());
                }
            }
        }.start();

        StringRequest strReq = new StringRequest(Request.Method.POST, url, response -> {
            Log.v(TAG, "Response Hasil Opname : "+ response);

            try{
                JSONObject jObj = new JSONObject(response);
                int success = jObj.getInt("success");
                String message = jObj.getString("message");

                //cek error node pada JSON
                if (success == 1){
                    new DialogAlert(message, "success", UserManageActivity.this);
                    progressDialog.dismiss();
                } else {
                    new DialogAlert(message, "error", UserManageActivity.this);
                    progressDialog.dismiss();
                }
            } catch (JSONException e){
                e.printStackTrace();
            }
        }, error -> new DialogAlert(error.getMessage(), "error", UserManageActivity.this)) {
            @Override
            protected Map<String, String> getParams() {
                //posting parameter ke post url
                Map<String, String> param = new HashMap<>();
                param.put("level", spin_level.getText().toString().substring(0, spin_level.getText().toString().indexOf(" ")));
                param.put("user", spin_user.getText().toString());
                param.put("kota", spin_kota.getText().toString());

                if (cbBottomPrice.isChecked()){ param.put("bottom_price", "1"); }
                else { param.put("bottom_price", "0"); }
                if (cbHPP.isChecked()){ param.put("hpp", "1"); }
                else { param.put("hpp", "0"); }

                return param;
            }
        };
        AppController.getInstance().addToRequestQueue(strReq, getResources().getString(R.string.tag_json_obj));
    }

    @SuppressLint("StaticFieldLeak")
    private class GetKota1 extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(UserManageActivity.this);
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
            String url_select_kota = a.URL() + "tools/select_kota.php";

            StringRequest strReq = new StringRequest(Request.Method.POST, url_select_kota, response -> {
                Log.d(TAG, "Response : " + response);
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    JSONArray result = jsonObject.getJSONArray("result");
                    listKota.clear();
                    listKota.add("ALL");
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
            }, error -> Log.e(TAG, "Error : "+ error.getMessage()));
            AppController.getInstance().addToRequestQueue(strReq, getResources().getString(R.string.tag_json_obj));
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
        adapterKota = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, kotaArray);
        spin_kota.setAdapter(adapterKota);
    }

}

package com.yusuffahrudin.masuyamobileapp.usermanage;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.ParseException;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

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
import com.yusuffahrudin.masuyamobileapp.data.UserAkses;
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
 * Created by yusuf fahrudin on 26-08-2017.
 */

public class UserAksesActivity extends AppCompatActivity {
    RecyclerView rv_modul;
    Button btn_simpan;
    LinearLayoutManager layoutManager;
    AdapterModul adapterModul;
    List<UserAkses> listData = new ArrayList<>();
    String level, message, kdkota;
    int success;
    SessionManager sessionManager;
    ProgressDialog progressDialog;

    private static final String TAG = UserAksesActivity.class.getSimpleName();
    private static String url_select_modul;
    private static String url_insert_akses;

    String tag_json_obj = "json_obj_req";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setTitle(getString(R.string.hak_akses_user));
        setContentView(R.layout.fragment_akses);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Intent i = getIntent();
        level = i.getExtras().getString("level");

        sessionManager = new SessionManager(getApplicationContext());
        HashMap<String, String> user = sessionManager.getUserDetails();
        kdkota = user.get(SessionManager.kdkota);

        rv_modul = findViewById(R.id.rv_main);
        rv_modul.setHasFixedSize(true);
        btn_simpan = findViewById(R.id.btn_simpan);

        /**
         * Kita menggunakan LinearLayoutManager untuk list standar
         * yang hanya berisi daftar item
         * disusun dari atas ke bawah
         */
        layoutManager = new LinearLayoutManager(UserAksesActivity.this);
        rv_modul.setLayoutManager(layoutManager);

        //untuk mengisi data dari JSON ke Adapter
        adapterModul = new AdapterModul(UserAksesActivity.this, listData);
        rv_modul.setAdapter(adapterModul);

        selectModul();

        btn_simpan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                simpanUserAkses();
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

    private class AdapterModul extends RecyclerView.Adapter <AdapterModul.ViewHolder> {

        private List<UserAkses> rvData = new ArrayList<>();
        private Activity activity;

        public AdapterModul(Activity activity, List<UserAkses> tampung){
            this.activity = activity;
            this.rvData = tampung;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            //membuat view baru
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_akses_layout, parent, false);
            //mengeset ukuran view, margin, padding, dan parameter layout
            ViewHolder viewHolder = new ViewHolder(view);

            return viewHolder;
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {
            //mengambil elemen dari arraylist pada posisi yang ditentukan
            //dan memasukkannya sebagai isi dari view recyclerview

            holder.cb_akses.setOnCheckedChangeListener(null);
            holder.cb_add.setOnCheckedChangeListener(null);
            holder.cb_edit.setOnCheckedChangeListener(null);
            holder.cb_delete.setOnCheckedChangeListener(null);
            holder.cb_post.setOnCheckedChangeListener(null);

            holder.tv_modul.setText(rvData.get(position).getModul());
            holder.cb_akses.setChecked(rvData.get(position).getAkses() == 1);
            holder.cb_add.setChecked(rvData.get(position).getAdd() == 1);
            holder.cb_edit.setChecked(rvData.get(position).getEdit() == 1);
            holder.cb_delete.setChecked(rvData.get(position).getDelete() == 1);
            holder.cb_post.setChecked(rvData.get(position).getPost() == 1);
            holder.cvList.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.show_from_left));

            holder.cb_akses.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
                    if(holder.cb_akses.isChecked()){
                        rvData.get(position).setAkses(1);
                    } else {
                        rvData.get(position).setAkses(0);
                    }
                }
            });
            holder.cb_add.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
                    if(holder.cb_add.isChecked()){
                        rvData.get(position).setAdd(1);
                    } else {
                        rvData.get(position).setAdd(0);
                    }
                }
            });
            holder.cb_edit.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
                    if(holder.cb_edit.isChecked()){
                        rvData.get(position).setEdit(1);
                    } else {
                        rvData.get(position).setEdit(0);
                    }
                }
            });
            holder.cb_delete.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
                    if(holder.cb_delete.isChecked()){
                        rvData.get(position).setDelete(1);
                    } else {
                        rvData.get(position).setDelete(0);
                    }
                }
            });
            holder.cb_post.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
                    if(holder.cb_post.isChecked()){
                        rvData.get(position).setPost(1);
                    } else {
                        rvData.get(position).setPost(0);
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            //mengembalikan jumlah data yang ada pada list recyclerview
            return rvData.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            public TextView tv_modul;
            public CheckBox cb_akses;
            public CheckBox cb_add;
            public CheckBox cb_edit;
            public CheckBox cb_delete;
            public CheckBox cb_post;
            public CardView cvList;

            public ViewHolder(View itemView) {
                super(itemView);
                tv_modul = itemView.findViewById(R.id.tv_modul);
                cb_akses = itemView.findViewById(R.id.cb_akses);
                cb_add = itemView.findViewById(R.id.cb_add);
                cb_edit = itemView.findViewById(R.id.cb_edit);
                cb_delete = itemView.findViewById(R.id.cb_delete);
                cb_post = itemView.findViewById(R.id.cb_post);
                cvList = itemView.findViewById(R.id.cv_main);
            }
        }
    }

    private void selectModul(){
        listData.clear();
        adapterModul.notifyDataSetChanged();
        Server a = new Server(kdkota);
        url_select_modul = a.URL() + "tools/cek_akses.php";

        progressDialog = ProgressDialog.show(UserAksesActivity.this, "", "Please Wait...");
        new Thread() {
            public void run() {
                try{
                    sleep(10000);
                } catch (Exception e) {
                    Log.e("tag", e.getMessage());
                }
            }
        }.start();

        StringRequest strReq = new StringRequest(Request.Method.POST, url_select_modul, new Response.Listener<String>(){
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Response : " + response);
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    JSONArray result = jsonObject.getJSONArray("result");
                    for (int i = 0; i < result.length(); i++) {
                        try {
                            JSONObject obj = result.getJSONObject(i);

                            UserAkses item = new UserAkses();

                            item.setModul(obj.getString("Modul"));

                            if (obj.getInt("Akses") == 1){ item.setAkses(1); }
                            else { item.setAkses(0); }

                            if (obj.getInt("Add") == 1) { item.setAdd(1); }
                            else { item.setAdd(0); }

                            if (obj.getInt("Edit") == 1) { item.setEdit(1); }
                            else { item.setEdit(0); }

                            if (obj.getInt("Delete") == 1) { item.setDelete(1); }
                            else { item.setDelete(0); }

                            if (obj.getInt("Post") == 1) { item.setPost(1); }
                            else { item.setPost(0); }

                            //menambah item ke array
                            listData.add(item);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    progressDialog.dismiss();
                    //notifikasi adanya perubahan data pada adapter
                    adapterModul.notifyDataSetChanged();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Error Volley : "+ error.getMessage());
                //swipeRefreshLayout.setRefreshing(false);
                new DialogAlert(error.getMessage(), "error", UserAksesActivity.this);
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                //posting parameter ke post url
                Map<String, String> params = new HashMap<String, String>();
                params.put("level", level);
                return params;
            }
        };
        AppController.getInstance().addToRequestQueue(strReq, tag_json_obj);
    }

    //menyimpan data user akses
    private void simpanUserAkses(){
        Server a = new Server(kdkota);
        url_insert_akses = a.URL() + "usermanagement/insert_akses_level.php";

        progressDialog = ProgressDialog.show(UserAksesActivity.this, "", "Please Wait...");
        new Thread() {
            public void run() {
                try{
                    sleep(10000);
                } catch (Exception e) {
                    Log.e("tag", e.getMessage());
                }
            }
        }.start();

        StringRequest strReq = new StringRequest(Request.Method.POST, url_insert_akses, new Response.Listener<String>(){

            @Override
            public void onResponse(String response) {
                Log.v(TAG, "Response Hasil Opname : "+ response);

                try{
                    JSONObject jObj = new JSONObject(response);
                    success = jObj.getInt("success");
                    message = jObj.getString("message");

                    //cek error node pada JSON
                    if (success == 1){
                        new DialogAlert(message, "success", UserAksesActivity.this);
                        progressDialog.dismiss();
                        Intent intent = new Intent(UserAksesActivity.this, UserManageActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        progressDialog.dismiss();
                        new DialogAlert(message, "error", UserAksesActivity.this);
                    }
                } catch (JSONException e){
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener(){

            @Override
            public void onErrorResponse(VolleyError error) {
                new DialogAlert(error.getMessage(), "error", UserAksesActivity.this);
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                //posting parameter ke post url
                Map<String, String> param = new HashMap<String, String>();

                //JSONArray Pending Opname
                JSONArray paramAkses = new JSONArray();
                JSONObject arrayAkses = null;
                try {
                    for (int i = 0; i < listData.size(); i++){
                        arrayAkses = new JSONObject();
                        arrayAkses.put("modul", listData.get(i).getModul());
                        arrayAkses.put("akses", listData.get(i).getAkses());
                        arrayAkses.put("add", listData.get(i).getAdd());
                        arrayAkses.put("edit", listData.get(i).getEdit());
                        arrayAkses.put("delete", listData.get(i).getDelete());
                        arrayAkses.put("post", listData.get(i).getPost());

                        Log.v(TAG, "modul : "+ listData.get(i).getModul());
                        Log.v(TAG, "akses : "+ listData.get(i).getAkses());
                        Log.v(TAG, "add : "+ listData.get(i).getAdd());
                        Log.v(TAG, "edit : "+ listData.get(i).getEdit());
                        Log.v(TAG, "delete : "+ listData.get(i).getDelete());
                        Log.v(TAG, "post : "+ listData.get(i).getPost());
                        paramAkses.put(arrayAkses);
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    param.put("level", level);
                    param.put("arrayAkses", paramAkses.toString());
                    Log.v(TAG, "level : "+ level);
                    Log.v(TAG, "arrayAkses : "+ paramAkses.toString());
                } catch (ParseException e){
                    e.printStackTrace();
                }
                return param;
            }
        };

        AppController.getInstance().addToRequestQueue(strReq, tag_json_obj);
    }
}

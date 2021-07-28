package com.yusuffahrudin.masuyamobileapp.customer;

import android.app.Activity;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.yusuffahrudin.masuyamobileapp.R;
import com.yusuffahrudin.masuyamobileapp.controller.AppController;
import com.yusuffahrudin.masuyamobileapp.controller.DialogAlert;
import com.yusuffahrudin.masuyamobileapp.util.Server;
import com.yusuffahrudin.masuyamobileapp.util.SessionManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RealokasiActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private GoogleMap.OnCameraIdleListener onCameraIdleListener;
    private TextView tv_alamat;
    private EditText edt_koordinat;
    private LatLng latLng;
    private double lat, lng;
    private List<Address> addressList;
    private String kdcust, kdkota;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setTitle(getString(R.string.simpan_koordinat));
        setContentView(R.layout.activity_realokasi);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        SessionManager sessionManager = new SessionManager(getApplicationContext());
        HashMap<String, String> user = sessionManager.getUserDetails();
        kdkota = user.get(SessionManager.kdkota);

        Intent i = this.getIntent();
        if (i != null) {
            lat = i.getExtras().getDouble("lat", 0);
            lng = i.getExtras().getDouble("lng", 0);
            kdcust = i.getExtras().getString("kdcust");
        }

        LinearLayout btn_simpan_lokasi = findViewById(R.id.btn_save_location);
        tv_alamat = findViewById(R.id.tv_alamat);
        edt_koordinat = findViewById(R.id.edt_koordinat);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        configureCameraIdle();

        btn_simpan_lokasi.setOnClickListener(v -> simpanKoordinat());

        edt_koordinat.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                String koordinat = edt_koordinat.getText().toString().replace(" ", "");
                lat = Double.valueOf(koordinat.substring(0, koordinat.indexOf(",")));
                lng = Double.valueOf(koordinat.substring(koordinat.indexOf(",")+1));

                System.out.println("========================== lat "+lat);
                System.out.println("========================== lng "+lng);
                Geocoder geocoder=new Geocoder(getApplicationContext());
                try {
                    addressList = geocoder.getFromLocation(lat,lng,1);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                LatLng lokasi = new LatLng(lat, lng);
                float zoomLevel = 15.0f; //This goes up to 21
                mMap.clear();
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lokasi, zoomLevel));
                mMap.setOnCameraIdleListener(onCameraIdleListener);
                tv_alamat.setText(addressList.get(0).getAddressLine(0));
                return true;
            }
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

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        LatLng lokasi = new LatLng(lat, lng);
        float zoomLevel = 18.0f; //This goes up to 21
        mMap.addMarker(new MarkerOptions().position(lokasi));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lokasi, zoomLevel));
        mMap.setOnCameraIdleListener(onCameraIdleListener);
    }

    private void configureCameraIdle() {
        onCameraIdleListener = () -> {
            latLng = mMap.getCameraPosition().target;
            Geocoder geocoder=new Geocoder(getApplicationContext());
            try {
                addressList = geocoder.getFromLocation(latLng.latitude,latLng.longitude,1);
                if (addressList != null && addressList.size() > 0) {
                    //String locality = addressList.get(0).getAddressLine(0);
                    //String country = addressList.get(0).getCountryName();
                    mMap.clear();
                    mMap.addMarker(new MarkerOptions().position(latLng).title(addressList.get(0).getAddressLine(0)));
                    tv_alamat.setText(addressList.get(0).getAddressLine(0));
                    lat = latLng.latitude;
                    lng = latLng.longitude;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        };
    }

    private void simpanKoordinat(){
        Server a = new Server(kdkota);
        String url_insert_koordinat = a.URL() + "customer/insert_koordinat.php";
        StringRequest strReq = new StringRequest(Request.Method.POST, url_insert_koordinat, response -> {
            Log.d(getLocalClassName(), "Response : " + response);
            try{
                JSONObject jObj = new JSONObject(response);
                int success = jObj.getInt("success");
                if (success == 1){
                    Intent intent = new Intent(RealokasiActivity.this, CustomerDetailActivity.class);
                    intent.putExtra("lat", String.valueOf(lat));
                    intent.putExtra("lng", String.valueOf(lng));
                    setResult(Activity.RESULT_OK, intent);
                    new DialogAlert("Titik koordinat "+kdcust+" berhasil di simpan! ", "success-reply", this);
                } else {
                    new DialogAlert("Internetmu lemot..! "+jObj.toString(), "error", this);
                }
            } catch (JSONException e){
                e.printStackTrace();
                new DialogAlert("Internetmu lemot..! "+e.toString(), "error", this);
            }

        }, error -> {
            error.printStackTrace();
            new DialogAlert("Internetmu lemot..! "+error.toString(), "error", this);
        }) {
            @Override
            protected Map<String, String> getParams() {
                //posting parameter ke post url
                Map<String, String> params = new HashMap<>();
                params.put("kdcust", kdcust);
                params.put("latlng", lat+", "+lng);
                return params;
            }
        };

        strReq.setRetryPolicy(new DefaultRetryPolicy(10000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        AppController.getInstance().addToRequestQueue(strReq, getString(R.string.tag_json_obj));
    }
}

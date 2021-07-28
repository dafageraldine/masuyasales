package com.yusuffahrudin.masuyamobileapp.informasibarang;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.yusuffahrudin.masuyamobileapp.R;
import com.yusuffahrudin.masuyamobileapp.adapter.AdapterVPFotoProdukBesar;
import com.yusuffahrudin.masuyamobileapp.controller.ZoomOutPageTransformer;
import com.yusuffahrudin.masuyamobileapp.util.Server;
import com.yusuffahrudin.masuyamobileapp.util.SessionManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import me.relex.circleindicator.CircleIndicator;

/**
 * Created by yusuf fahrudin on 29-08-2017.
 */

public class ShowProduk extends AppCompatActivity {

    private String kdbrg, kdkota;
    private ArrayList<String> arrayFoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.show_produk);
        Intent i = getIntent();
        kdbrg = Objects.requireNonNull(i.getExtras()).getString("kdbrg");
        int posisi = i.getExtras().getInt("posisi");

        SessionManager sessionManager = new SessionManager(getApplicationContext());
        HashMap<String, String> user = sessionManager.getUserDetails();
        kdkota = user.get(SessionManager.kdkota);

        //inisialisasi view
        ViewPager vp_image = findViewById(R.id.vp_image);
        CircleIndicator indicator = findViewById(R.id.indicator);
        TextView tv1 = findViewById(R.id.tv1);
        tv1.setText(kdbrg);

        setFoto();

        AdapterVPFotoProdukBesar adapterVPFotoProdukBesar = new AdapterVPFotoProdukBesar(this, arrayFoto);
        vp_image.setAdapter(adapterVPFotoProdukBesar);
        indicator.setViewPager(vp_image);
        vp_image.setPageTransformer(true, new ZoomOutPageTransformer());
        vp_image.setCurrentItem(posisi);

        //Timer timer = new Timer();
        //timer.scheduleAtFixedRate(new MyTimerTask(), 2500, 2500);
    }
/*
    public class MyTimerTask extends TimerTask {

        @Override
        public void run() {

            ShowProduk.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    if (vp_image.getCurrentItem() == 0){
                        vp_image.setCurrentItem(1);
                    } else {
                        vp_image.setCurrentItem(0);
                    }
                }
            });
        }
    } */

    private void setFoto(){
        Server a = new Server(kdkota);
        arrayFoto = new ArrayList<>();
        arrayFoto.add(a.URL_IMAGE()+kdbrg+".jpg");
        arrayFoto.add(a.URL_IMAGE()+kdbrg+"_1.jpg");
        arrayFoto.add(a.URL_IMAGE()+kdbrg+"_2.jpg");
    }
}

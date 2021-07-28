package com.yusuffahrudin.masuyamobileapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.viewpager.widget.PagerAdapter;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.github.chrisbanes.photoview.PhotoView;
import com.yusuffahrudin.masuyamobileapp.R;
import com.yusuffahrudin.masuyamobileapp.informasibarang.ShowProduk;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

/**
 * Created by yusuf fahrudin on 05-05-2017.
 */

public class AdapterVPFotoProdukKecil extends PagerAdapter {

    private Context mContext;
    private ArrayList<String> arrayFoto;
    private String kdbrg;

    public AdapterVPFotoProdukKecil(Context context, ArrayList<String> arrayFoto, String kdbrg) {
        this.mContext = context;
        this.arrayFoto = arrayFoto;
        this.kdbrg = kdbrg;
    }

    @Override
    public int getCount() {
        return arrayFoto.size();
    }

    @Override
    public boolean isViewFromObject(@NotNull View view, @NotNull Object object) {
        return view == object;
    }

    @NotNull
    @Override
    public Object instantiateItem(@NotNull ViewGroup container, final int position) {
        //layoutInflater = LayoutInflater.from(context);
        LayoutInflater layoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.custom_layout, container, false);
        PhotoView imageView = view.findViewById(R.id.imageView);

        Glide.with(mContext)
                .load(arrayFoto.get(position))
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .apply(new RequestOptions()
                        .placeholder(R.mipmap.ic_launcher)
                        .error(R.drawable.img_not_found)
                        .override(300, 400)
                .fitCenter())
                .into(imageView);

        container.addView(view);
        imageView.setOnClickListener(v -> {
            Intent intent = new Intent(mContext, ShowProduk.class);
            intent.putExtra("kdbrg", kdbrg);
            intent.putExtra("posisi", position);
            mContext.startActivity(intent);
        });

        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, @NotNull Object object) {
        container.invalidate();
    }
}

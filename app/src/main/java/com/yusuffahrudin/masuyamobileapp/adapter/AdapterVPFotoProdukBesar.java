package com.yusuffahrudin.masuyamobileapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.viewpager.widget.PagerAdapter;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.github.chrisbanes.photoview.PhotoView;
import com.yusuffahrudin.masuyamobileapp.R;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

/**
 * Created by yusuf fahrudin on 05-05-2017.
 */

public class AdapterVPFotoProdukBesar extends PagerAdapter {

    private Context mContext;
    private ArrayList<String> arrayFoto;

    public AdapterVPFotoProdukBesar(Context context, ArrayList<String> arrayFoto) {
        this.mContext = context;
        this.arrayFoto = arrayFoto;
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
    public Object instantiateItem(@NotNull ViewGroup container, int position) {
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
                        .override(768, 1024)
                        .fitCenter())
                .into(imageView);

        container.addView(view);

        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, @NotNull Object object) {
        container.invalidate();
    }
}

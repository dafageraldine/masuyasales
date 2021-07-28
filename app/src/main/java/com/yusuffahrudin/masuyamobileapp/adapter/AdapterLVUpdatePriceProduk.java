package com.yusuffahrudin.masuyamobileapp.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.cardview.widget.CardView;

import com.yusuffahrudin.masuyamobileapp.R;
import com.yusuffahrudin.masuyamobileapp.data.Customer;

import java.util.List;

/**
 * Created by yusuf fahrudin on 16-02-2017.
 */

public class AdapterLVUpdatePriceProduk extends BaseAdapter {

    private List<Customer> lvData;
    private Activity activity;
    LayoutInflater inflater;

    public AdapterLVUpdatePriceProduk(Activity activity, List<Customer> tampung){
        this.activity = activity;
        this.lvData = tampung;
    }

    @Override
    public int getCount() {
        return lvData.size();
    }

    @Override
    public Object getItem(int location) {
        return lvData.get(location);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (inflater == null)
            inflater = (LayoutInflater) activity
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (convertView == null)
            convertView = inflater.inflate(R.layout.rv_customer_pricelist_layout, null);

        TextView tvKdCust = convertView.findViewById(R.id.tv_kdcust);
        TextView tvNmCust = convertView.findViewById(R.id.tv_nmcust);
        TextView tvHarga = convertView.findViewById(R.id.tv_harga);
        CheckBox cb_cek = convertView.findViewById(R.id.cb_cek);
        CardView cvList = convertView.findViewById(R.id.cv_main);
        cvList.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.show_from_left));

        Customer data = lvData.get(position);

        tvKdCust.setText(data.getKdcust());
        tvNmCust.setText(data.getNmcust());

        return convertView;
    }
}

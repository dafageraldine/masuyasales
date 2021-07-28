package com.yusuffahrudin.masuyamobileapp.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.yusuffahrudin.masuyamobileapp.R;
import com.yusuffahrudin.masuyamobileapp.data.informasi_barang.Product;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

/**
 * Created by yusuf fahrudin on 16-02-2017.
 */

public class AdapterLVAutoPricelist extends BaseAdapter {

    private List<Product> lvData;
    private Activity activity;
    private LayoutInflater inflater;
    private NumberFormat nf = NumberFormat.getInstance(new Locale("pt", "BR"));

    public AdapterLVAutoPricelist(Activity activity, List<Product> tampung){
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
            convertView = inflater.inflate(R.layout.list_item_update_pricelist, null);

        TextView tvKdBrg = convertView.findViewById(R.id.tv_kdbrg);
        TextView tvNmBrg = convertView.findViewById(R.id.tv_nmbrg);
        TextView tvTgl = convertView.findViewById(R.id.tv_tgl);
        TextView tvSatuan = convertView.findViewById(R.id.tv_satuan);
        TextView tvHrg = convertView.findViewById(R.id.tv_harga);
        TextView tvDiskon1 = convertView.findViewById(R.id.tv_diskon1);
        TextView tvDiskon2 = convertView.findViewById(R.id.tv_diskon2);
        TextView tvDiskon3 = convertView.findViewById(R.id.tv_diskon3);

        Product data = lvData.get(position);
        tvKdBrg.setText(data.getKdbrg());
        tvNmBrg.setText(data.getNmbrg());
        tvTgl.setText(data.getTgl());
        tvSatuan.setText(data.getSatuan());
        tvHrg.setText(nf.format(data.getHrgIncPpn()));
        tvDiskon1.setText("Disc1 : "+nf.format(data.getDiskon1())+"%");
        if (!nf.format(data.getDiskon1()).equals("0"))
            tvDiskon1.setTextColor(Color.RED);
        tvDiskon2.setText("Disc2 : "+nf.format(data.getDiskon2())+"%");
        if (!nf.format(data.getDiskon2()).equals("0"))
            tvDiskon2.setTextColor(Color.RED);
        tvDiskon3.setText("Disc3 : "+nf.format(data.getDiskon3())+"%");
        if (!nf.format(data.getDiskon3()).equals("0"))
            tvDiskon3.setTextColor(Color.RED);

        return convertView;
    }
}

package com.yusuffahrudin.masuyamobileapp.data;

import com.yusuffahrudin.masuyamobileapp.data.informasi_barang.Product;
import com.yusuffahrudin.masuyamobileapp.data.sales_order.ItemOrder;
import com.yusuffahrudin.masuyamobileapp.data.sales_order.SalesOrder;

import java.util.ArrayList;

/**
 * Created by yusuf fahrudin on 06-06-2017.
 */

public class ArrayTampung {
    private static ArrayList<Product> listData;
    private static ArrayList<Opname> listOpname;
    private static ArrayList<UserAkses> listAkses;
    private static ArrayList<String> listKota;
    private static ArrayList<ItemOrder> listChart;
    private static ArrayList<ItemOrder> listChartLama;
    private static ArrayList<SalesOrder> listHeaderSO;

    public static ArrayList<ItemOrder> getListChart() {
        if(listChart == null) {
            listChart = new ArrayList<>();
        }
        return listChart;
    }

    public static ArrayList<ItemOrder> getListChartLama() {
        if(listChartLama == null) {
            listChartLama = new ArrayList<>();
        }
        return listChartLama;
    }

    public static ArrayList<Opname> getListOpname() {
        if(listOpname == null) {
            listOpname = new ArrayList<>();
        }
        return listOpname;
    }

    public static ArrayList<Product> getListData() {
        if(listData == null) {
            listData = new ArrayList<>();
        }
        return listData;
    }

    public static ArrayList<UserAkses> getListAkses() {
        if(listAkses == null) {
            listAkses = new ArrayList<>();
        }
        return listAkses;
    }

    public static ArrayList<String> getListKota() {
        if(listKota == null) {
            listKota = new ArrayList<>();
        }
        return listKota;
    }

    public static ArrayList<SalesOrder> getListHeaderSO() {
        if(listHeaderSO == null) {
            listHeaderSO = new ArrayList<>();
        }
        return listHeaderSO;
    }
}

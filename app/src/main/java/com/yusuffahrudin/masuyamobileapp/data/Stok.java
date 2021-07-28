package com.yusuffahrudin.masuyamobileapp.data;

import java.io.Serializable;

/**
 * Created by yusuf fahrudin on 16-02-2017.
 */

public class Stok implements Serializable {
    private String kdbrg, nmbrg, tanggal, kota, satuan, satuan3;
    private Double qty, harga, diskon1, diskon2, diskon3, qtykvs3, m3;

    public void Data(String kdbrg, String nmbrg, Double qty, String kota, String satuan, Double harga,
                     Double diskon1, Double diskon2, Double diskon3, String satuan3, Double qtykvs3, Double m3){
        this.kdbrg = kdbrg;
        this.nmbrg = nmbrg;
        this.qty = qty;
        this.kota = kota;
        this.satuan = satuan;
        this.harga = harga;
        this.diskon1 = diskon1;
        this.diskon2 = diskon2;
        this.diskon3 = diskon3;
        this.satuan3 = satuan3;
        this.qtykvs3 = qtykvs3;
        this.m3 = m3;
    }

    public Double getM3() {
        return m3;
    }

    public void setM3(Double m3) {
        this.m3 = m3;
    }

    public String getSatuan3() {
        return satuan3;
    }

    public void setSatuan3(String satuan3) {
        this.satuan3 = satuan3;
    }

    public Double getQtykvs3() {
        return qtykvs3;
    }

    public void setQtykvs3(Double qtykvs3) {
        this.qtykvs3 = qtykvs3;
    }

    public Double getHarga() {
        return harga;
    }

    public void setHarga(Double harga) {
        this.harga = harga;
    }

    public Double getDiskon1() {
        return diskon1;
    }

    public void setDiskon1(Double diskon1) {
        this.diskon1 = diskon1;
    }

    public Double getDiskon2() {
        return diskon2;
    }

    public void setDiskon2(Double diskon2) {
        this.diskon2 = diskon2;
    }

    public Double getDiskon3() {
        return diskon3;
    }

    public void setDiskon3(Double diskon3) {
        this.diskon3 = diskon3;
    }

    public String getSatuan() {
        return satuan;
    }

    public void setSatuan(String satuan) {
        this.satuan = satuan;
    }

    public String getTanggal() { return tanggal; }

    public void setTanggal(String tanggal) { this.tanggal = tanggal; }

    public String getKdbrg() {
        return kdbrg;
    }

    public void setKdbrg(String kdbrg) {
        this.kdbrg = kdbrg;
    }

    public String getNmbrg() {
        return nmbrg;
    }

    public void setNmbrg(String nmbrg) {
        this.nmbrg = nmbrg;
    }

    public Double getQty() {
        return qty;
    }

    public void setQty(Double qty) {
        this.qty = qty;
    }

    public String getKota() {
        return kota;
    }

    public void setKota(String kota) {
        this.kota = kota;
    }
}

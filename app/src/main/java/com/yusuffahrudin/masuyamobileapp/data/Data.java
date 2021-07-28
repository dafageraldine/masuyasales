package com.yusuffahrudin.masuyamobileapp.data;

import java.io.Serializable;

/**
 * Created by yusuf fahrudin on 16-02-2017.
 */

public class Data implements Serializable {
    private String kdbrg, nmbrg, tanggal, kota, satuan, satuan3, kdgd, nmgd;
    private Double qty, harga, diskon1, diskon2, diskon3, qtykvs3, m3, subtotal, qtyOut, qtyOrder, hrgJualMin, hrgpokok;

    public void Data(String kdbrg, String nmbrg, Double qty, String kota, String satuan, String kdgd, String nmgd, Double harga,
                     Double diskon1, Double diskon2, Double diskon3, String satuan3, Double qtykvs3, Double m3, Double subtotal,
                     Double qtyOut, Double qtyOrder, Double hrgJualMin, Double hrgpokok){
        this.kdbrg = kdbrg;
        this.nmbrg = nmbrg;
        this.qty = qty;
        this.kota = kota;
        this.satuan = satuan;
        this.kdgd = kdgd;
        this.nmgd = nmgd;
        this.harga = harga;
        this.diskon1 = diskon1;
        this.diskon2 = diskon2;
        this.diskon3 = diskon3;
        this.satuan3 = satuan3;
        this.qtykvs3 = qtykvs3;
        this.m3 = m3;
        this.subtotal = subtotal;
        this.qtyOut = qtyOut;
        this.qtyOrder = qtyOrder;
        this.hrgJualMin = hrgJualMin;
        this.hrgpokok = hrgpokok;
    }

    public Double getHrgpokok() { return hrgpokok; }
    public void setHrgpokok(Double hrgpokok) { this.hrgpokok = hrgpokok; }

    public Double getHrgJualMin() { return hrgJualMin; }
    public void setHrgJualMin(Double hrgJualMin) { this.hrgJualMin = hrgJualMin; }

    public Double getQtyOrder() {
        return qtyOrder;
    }
    public void setQtyOrder(Double qtyOrder) {
        this.qtyOrder = qtyOrder;
    }

    public Double getQtyOut() { return qtyOut; }
    public void setQtyOut(Double qtyOut) { this.qtyOut = qtyOut; }

    public Double getSubtotal() { return subtotal; }
    public void setSubtotal(Double subtotal) { this.subtotal = subtotal; }

    public String getKdgd() {
        return kdgd;
    }
    public void setKdgd(String kdgd) {
        this.kdgd = kdgd;
    }

    public String getNmgd() {
        return nmgd;
    }
    public void setNmgd(String nmgd) {
        this.nmgd = nmgd;
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

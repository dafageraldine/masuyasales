package com.yusuffahrudin.masuyamobileapp.data;

/**
 * Created by yusuf fahrudin on 16-02-2017.
 */

public class Pending {
    private String nobukti, tgl, kdgd, satuan;
    private Double qty;

    public void Pending(String nobukti, String tgl, String kdgd, String satuan, Double qty) {
        this.nobukti = nobukti;
        this.tgl = tgl;
        this.kdgd = kdgd;
        this.satuan = satuan;
        this.qty = qty;
    }

    public String getNobukti() {
        return nobukti;
    }

    public void setNobukti(String nobukti) {
        this.nobukti = nobukti;
    }

    public String getTgl() {
        return tgl;
    }

    public void setTgl(String tgl) {
        this.tgl = tgl;
    }

    public String getKdgd() {
        return kdgd;
    }

    public void setKdgd(String kdgd) {
        this.kdgd = kdgd;
    }

    public String getSatuan() {
        return satuan;
    }

    public void setSatuan(String satuan) {
        this.satuan = satuan;
    }

    public Double getQty() {
        return qty;
    }

    public void setQty(Double qty) {
        this.qty = qty;
    }
}

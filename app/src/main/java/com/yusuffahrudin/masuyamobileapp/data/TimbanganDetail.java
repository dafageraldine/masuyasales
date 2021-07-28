package com.yusuffahrudin.masuyamobileapp.data;

public class TimbanganDetail {
    private String kdcust, kdgd, kdbrg, nmbrg, satuan;
    private Double qty, qtyTimb, hrg, m3;

    public void TimbanganDetail(String kdcust, String kdgd, String kdbrg, String nmbrg, String satuan, Double qty, Double qtyTimb, Double hrg, Double m3) {
        this.kdcust = kdcust;
        this.kdgd = kdgd;
        this.kdbrg = kdbrg;
        this.nmbrg = nmbrg;
        this.satuan = satuan;
        this.qty = qty;
        this.qtyTimb = qtyTimb;
        this.hrg = hrg;
        this.m3 = m3;
    }

    public String getKdcust() {
        return kdcust;
    }

    public void setKdcust(String kdcust) {
        this.kdcust = kdcust;
    }

    public String getKdgd() {
        return kdgd;
    }

    public void setKdgd(String kdgd) {
        this.kdgd = kdgd;
    }

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

    public Double getQtyTimb() {
        return qtyTimb;
    }

    public void setQtyTimb(Double qtyTimb) {
        this.qtyTimb = qtyTimb;
    }

    public Double getHrg() {
        return hrg;
    }

    public void setHrg(Double hrg) {
        this.hrg = hrg;
    }

    public Double getM3() {
        return m3;
    }

    public void setM3(Double m3) {
        this.m3 = m3;
    }
}

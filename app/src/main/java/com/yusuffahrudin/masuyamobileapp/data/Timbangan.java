package com.yusuffahrudin.masuyamobileapp.data;

import java.util.Date;

public class Timbangan {
    private String noBukti, kdgd, kdcust, nmcust, ket1, ket2, nmsales;
    private Date tgl, tglkirim, createtime;

    public void Timbangan(String noBukti, String kdgd, String kdcust, String nmcust, String ket1, String ket2, String nmsales, Date tgl, Date tglkirim, Date createtime) {
        this.noBukti = noBukti;
        this.kdgd = kdgd;
        this.kdcust = kdcust;
        this.nmcust = nmcust;
        this.ket1 = ket1;
        this.ket2 = ket2;
        this.nmsales = nmsales;
        this.tgl = tgl;
        this.tglkirim = tglkirim;
        this.createtime = createtime;
    }

    public String getNoBukti() {
        return noBukti;
    }
    public void setNoBukti(String noBukti) {
        this.noBukti = noBukti;
    }

    public String getKdgd() {
        return kdgd;
    }
    public void setKdgd(String kdgd) {
        this.kdgd = kdgd;
    }

    public String getKdcust() {
        return kdcust;
    }
    public void setKdcust(String kdcust) {
        this.kdcust = kdcust;
    }

    public String getNmcust() {
        return nmcust;
    }
    public void setNmcust(String nmcust) {
        this.nmcust = nmcust;
    }

    public String getKet1() {
        return ket1;
    }
    public void setKet1(String ket1) {
        this.ket1 = ket1;
    }

    public String getKet2() {
        return ket2;
    }
    public void setKet2(String ket2) {
        this.ket2 = ket2;
    }

    public String getNmsales() {
        return nmsales;
    }
    public void setNmsales(String nmsales) {
        this.nmsales = nmsales;
    }

    public Date getTgl() {
        return tgl;
    }
    public void setTgl(Date tgl) {
        this.tgl = tgl;
    }

    public Date getTglkirim() {
        return tglkirim;
    }
    public void setTglkirim(Date tglkirim) {
        this.tglkirim = tglkirim;
    }

    public Date getCreatetime() { return createtime; }
    public void setCreatetime(Date createtime) { this.createtime = createtime; }
}

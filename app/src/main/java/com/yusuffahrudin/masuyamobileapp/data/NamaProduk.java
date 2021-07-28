package com.yusuffahrudin.masuyamobileapp.data;

/**
 * Created by yusuf fahrudin on 16-02-2017.
 */

public class NamaProduk {
    private String kdbrg, nmbrg, jenis, nmtype, packing3;
    private double hrgjualmin, metrixton, mkubik1;

    public void NamaProduk(String kdbrg, String nmbrg, String jenis, String nmtype,
                           String packing3, double hrgjualmin, double metrixton, double mkubik1)
    {
        this.kdbrg = kdbrg;
        this.nmbrg = nmbrg;
        this.jenis = jenis;
        this.nmtype = nmtype;
        this.packing3 = packing3;
        this.hrgjualmin = hrgjualmin;
        this.metrixton = metrixton;
        this.mkubik1 = mkubik1;
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

    public String getJenis() {
        return jenis;
    }

    public void setJenis(String jenis) {
        this.jenis = jenis;
    }

    public String getNmtype() {
        return nmtype;
    }

    public void setNmtype(String nmtype) {
        this.nmtype = nmtype;
    }

    public String getPacking3() {
        return packing3;
    }

    public void setPacking3(String packing3) {
        this.packing3 = packing3;
    }

    public double getHrgjualmin() {
        return hrgjualmin;
    }

    public void setHrgjualmin(double hrgjualmin) {
        this.hrgjualmin = hrgjualmin;
    }

    public double getMetrixton() {
        return metrixton;
    }

    public void setMetrixton(double metrixton) {
        this.metrixton = metrixton;
    }

    public double getMkubik1() {
        return mkubik1;
    }

    public void setMkubik1(double mkubik1) {
        this.mkubik1 = mkubik1;
    }
}

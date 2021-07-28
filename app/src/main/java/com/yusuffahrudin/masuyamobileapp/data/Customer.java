package com.yusuffahrudin.masuyamobileapp.data;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by yusuf fahrudin on 16-05-2017.
 */

public class Customer implements Parcelable {
    private String kdcust, nmcust, typecust, kdkel, alm1, alm2, alm3, kota, telp1, telp2, koordinat, sales, kdsales, top;
    private Double saldo, kreditLimit;

    public Customer(String kdcust, String nmcust, String typecust, String kdkel, String alm1, String alm2, String alm3,
                    String kota, String telp1, String telp2, String koordinat, String top, Double saldo, String sales,
                    String kdsales, Double kreditLimit) {
        this.kdcust = kdcust;
        this.nmcust = nmcust;
        this.typecust = typecust;
        this.kdkel = kdkel;
        this.alm1 = alm1;
        this.alm2 = alm2;
        this.alm3 = alm3;
        this.kota = kota;
        this.telp1 = telp1;
        this.telp2 = telp2;
        this.koordinat = koordinat;
        this.top = top;
        this.saldo = saldo;
        this.sales = sales;
        this.kdsales = kdsales;
        this.kreditLimit = kreditLimit;
    }

    public Double getKreditLimit() { return kreditLimit; }
    public void setKreditLimit(Double kreditLimit) { this.kreditLimit = kreditLimit; }

    public String getKdkel() {
        return kdkel;
    }
    public void setKdkel(String kdkel) {
        this.kdkel = kdkel;
    }

    public String getAlm2() {
        return alm2;
    }
    public void setAlm2(String alm2) {
        this.alm2 = alm2;
    }

    public String getAlm3() {
        return alm3;
    }
    public void setAlm3(String alm3) {
        this.alm3 = alm3;
    }

    public String getKdsales() {
        return kdsales;
    }
    public void setKdsales(String kdsales) {
        this.kdsales = kdsales;
    }

    public String getSales() {
        return sales;
    }
    public void setSales(String sales) {
        this.sales = sales;
    }

    public String getTelp2() {
        return telp2;
    }
    public void setTelp2(String telp2) {
        this.telp2 = telp2;
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

    public String getTypecust() {
        return typecust;
    }
    public void setTypecust(String typecust) {
        this.typecust = typecust;
    }

    public String getAlm1() {
        return alm1;
    }
    public void setAlm1(String alm1) {
        this.alm1 = alm1;
    }

    public String getKota() {
        return kota;
    }
    public void setKota(String kota) {
        this.kota = kota;
    }

    public String getTelp1() {
        return telp1;
    }
    public void setTelp1(String telp1) {
        this.telp1 = telp1;
    }

    public String getKoordinat() {
        return koordinat;
    }
    public void setKoordinat(String koordinat) {
        this.koordinat = koordinat;
    }

    public String getTop() { return top; }
    public void setTop(String top) { this.top = top; }

    public Double getSaldo() {
        return saldo;
    }
    public void setSaldo(Double saldo) {
        this.saldo = saldo;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.kdcust);
        dest.writeString(this.nmcust);
        dest.writeString(this.typecust);
        dest.writeString(this.kdkel);
        dest.writeString(this.alm1);
        dest.writeString(this.alm2);
        dest.writeString(this.alm3);
        dest.writeString(this.kota);
        dest.writeString(this.telp1);
        dest.writeString(this.telp2);
        dest.writeString(this.koordinat);
        dest.writeString(this.top);
        dest.writeString(this.sales);
        dest.writeString(this.kdsales);
        dest.writeValue(this.saldo);
        dest.writeValue(this.kreditLimit);
    }

    public Customer() {
    }

    protected Customer(Parcel in) {
        this.kdcust = in.readString();
        this.nmcust = in.readString();
        this.typecust = in.readString();
        this.kdkel = in.readString();
        this.alm1 = in.readString();
        this.alm2 = in.readString();
        this.alm3 = in.readString();
        this.kota = in.readString();
        this.telp1 = in.readString();
        this.telp2 = in.readString();
        this.koordinat = in.readString();
        this.top = in.readString();
        this.sales = in.readString();
        this.kdsales = in.readString();
        this.saldo = (Double) in.readValue(Double.class.getClassLoader());
        this.kreditLimit = (Double) in.readValue(Double.class.getClassLoader());
    }

    public static final Parcelable.Creator<Customer> CREATOR = new Parcelable.Creator<Customer>() {
        @Override
        public Customer createFromParcel(Parcel source) {
            return new Customer(source);
        }

        @Override
        public Customer[] newArray(int size) {
            return new Customer[size];
        }
    };
}

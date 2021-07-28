package com.yusuffahrudin.masuyamobileapp.data;

import java.util.Comparator;

/**
 * Created by yusuf fahrudin on 16-02-2017.
 */

public class Opname {
    private String kdgd, kdbrg, tgl, no_opname, kota, user;
    private Double system, lok1, lok2, lok3;
    private int lok22;

    public void Opname(String kdgd, String kdbrg, String tgl, String no_opname, String kota, String user, Double system, Double lok1, Double lok2, Double lok3, int lok22) {
        this.kdgd = kdgd;
        this.kdbrg = kdbrg;
        this.system = system;
        this.lok1 = lok1;
        this.lok2 = lok2;
        this.lok3 = lok3;
        this.tgl = tgl;
        this.no_opname = no_opname;
        this.kota = kota;
        this.user = user;
        this.lok22 = lok22;
    }

    public int getLok22() {
        return lok22;
    }

    public void setLok22(int lok22) {
        this.lok22 = lok22;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getTgl() {
        return tgl;
    }

    public void setTgl(String tgl) {
        this.tgl = tgl;
    }

    public String getNo_opname() {
        return no_opname;
    }

    public void setNo_opname(String no_opname) {
        this.no_opname = no_opname;
    }

    public String getKota() {
        return kota;
    }

    public void setKota(String kota) {
        this.kota = kota;
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

    public Double getSystem() {
        return system;
    }

    public void setSystem(Double system) {
        this.system = system;
    }

    public Double getLok1() {
        return lok1;
    }

    public void setLok1(Double lok1) {
        this.lok1 = lok1;
    }

    public Double getLok2() {
        return lok2;
    }

    public void setLok2(Double lok2) {
        this.lok2 = lok2;
    }

    public Double getLok3() {
        return lok3;
    }

    public void setLok3(Double lok3) {
        this.lok3 = lok3;
    }


    /*Comparator for sorting the list by Student Name*/
    public static Comparator<Opname> TglComparator = new Comparator<Opname>() {

        public int compare(Opname s1, Opname s2) {
            String tgl1 = s1.getTgl().toUpperCase();
            String tgl2 = s2.getTgl().toUpperCase();

            //ascending order
            return tgl1.compareTo(tgl2);

            //descending order
            //return StudentName2.compareTo(StudentName1);
        }};

    /*Comparator for sorting the list by roll no*/
    public static Comparator<Opname> NoOpnameComparator = new Comparator<Opname>() {

        public int compare(Opname s1, Opname s2) {

            String no1 = s1.getNo_opname();
            String no2 = s2.getNo_opname();

	   /*For ascending order*/
            return no1.compareTo(no2);

	   /*For descending order*/
            //rollno2-rollno1;
        }};
}

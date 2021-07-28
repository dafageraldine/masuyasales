package com.yusuffahrudin.masuyamobileapp.data;

import java.io.Serializable;

/**
 * Created by yusuf fahrudin on 05-03-2018.
 */

public class SalesOrder implements Serializable {
    private String nobukti, tgl_create, tgl_kirim, kdgd, kdcust, nmcust, kdkel, alm1, alm2, alm3, kdsales, nmsales, fileName,
    noPO, ket, ket1, ket2, orderby, createby, jnsjualtax, kdbrg, nmbrg, satuan, statusorder, kodeTax, imgPO, filePO,
    ketAR, ketSM, status, tglKetAR, authoARby, authoSMby, authoBMby, ARSM;

    private double subtotal, prsdisc1, jmldisc1, prsppn, ppn, total, qty, qtyorder, qtykirim, qtykvs, qtykvs3,
    hrg, prsdisc, prsdisc2, prsdisc3, disc, hrgnet, jumlah, m3, jumlahm3;

    private int authoAR, authoSM, authoBM;

    public void SalesOrder(String nobukti, String tgl_create, String tgl_kirim, String kdgd, String kdcust,
                      String nmcust, String kdkel, String alm1, String alm2, String alm3, String kdsales, String nmsales,
                      String noPO, String ket, String ket1, String ket2, String orderby, String createby, String jnsjualtax, String kdbrg,
                      String nmbrg, String satuan, String kodeTax, double subtotal, double prsdisc1, double jmldisc1,
                      double prsppn, double ppn, double total, double qty, double qtyorder, double qtykirim,
                      double qtykvs, double qtykvs3, double hrg, double prsdisc, double prsdisc2,
                      double prsdisc3, double disc, double hrgnet, double jumlah, double m3, double jumlahm3,
                      String statusorder, String imgPO, String filePO, String fileName, String ketAR, String ketSM,
                      int authoAR, int authoSM, String status, String tglKetAR, String authoARby, String authoSMby, String ARSM,
                           String authoBMby, int authoBM) {
        this.nobukti = nobukti;
        this.tgl_create = tgl_create;
        this.tgl_kirim = tgl_kirim;
        this.kdgd = kdgd;
        this.kdcust = kdcust;
        this.nmcust = nmcust;
        this.kdkel = kdkel;
        this.alm1 = alm1;
        this.alm2 = alm2;
        this.alm3 = alm3;
        this.kdsales = kdsales;
        this.nmsales = nmsales;
        this.noPO = noPO;
        this.ket = ket;
        this.ket1 = ket1;
        this.ket2 = ket2;
        this.orderby = orderby;
        this.createby = createby;
        this.jnsjualtax = jnsjualtax;
        this.kdbrg = kdbrg;
        this.nmbrg = nmbrg;
        this.satuan = satuan;
        this.kodeTax = kodeTax;
        this.subtotal = subtotal;
        this.prsdisc1 = prsdisc1;
        this.jmldisc1 = jmldisc1;
        this.prsppn = prsppn;
        this.ppn = ppn;
        this.total = total;
        this.qty = qty;
        this.qtyorder = qtyorder;
        this.qtykirim = qtykirim;
        this.qtykvs = qtykvs;
        this.qtykvs3 = qtykvs3;
        this.hrg = hrg;
        this.prsdisc = prsdisc;
        this.prsdisc2 = prsdisc2;
        this.prsdisc3 = prsdisc3;
        this.disc = disc;
        this.hrgnet = hrgnet;
        this.jumlah = jumlah;
        this.m3 = m3;
        this.jumlahm3 = jumlahm3;
        this.statusorder = statusorder;
        this.imgPO = imgPO;
        this.filePO = filePO;
        this.fileName = fileName;
        this.ketAR = ketAR;
        this.ketSM = ketSM;
        this.authoAR = authoAR;
        this.authoSM = authoSM;
        this.authoBM = authoBM;
        this.status = status;
        this.tglKetAR = tglKetAR;
        this.authoARby = authoARby;
        this.authoSMby = authoSMby;
        this.authoBMby = authoBMby;
        this.ARSM = ARSM;
    }

    public String getAuthoBMby() { return authoBMby; }
    public void setAuthoBMby(String authoBMby) { this.authoBMby = authoBMby; }

    public int getAuthoBM() { return authoBM; }
    public void setAuthoBM(int authoBM) { this.authoBM = authoBM; }

    public String getARSM() { return ARSM; }
    public void setARSM(String ARSM) { this.ARSM = ARSM; }

    public String getAuthoARby() { return authoARby; }
    public void setAuthoARby(String authoARby) { this.authoARby = authoARby; }

    public String getAuthoSMby() { return authoSMby; }
    public void setAuthoSMby(String authoSMby) { this.authoSMby = authoSMby; }

    public String getTglKetAR() { return tglKetAR; }
    public void setTglKetAR(String tglKetAR) { this.tglKetAR = tglKetAR; }

    public String getNmsales() { return nmsales; }
    public void setNmsales(String nmsales) { this.nmsales = nmsales; }

    public int getAuthoAR() { return authoAR; }
    public void setAuthoAR(int authoAR) { this.authoAR = authoAR; }

    public int getAuthoSM() { return authoSM; }
    public void setAuthoSM(int authoSM) { this.authoSM = authoSM; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getKetAR() { return ketAR; }
    public void setKetAR(String ketAR) { this.ketAR = ketAR; }

    public String getKetSM() { return ketSM; }
    public void setKetSM(String ketSM) { this.ketSM = ketSM; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getFilePO() { return filePO; }
    public void setFilePO(String filePO) { this.filePO = filePO; }

    public String getImgPO() { return imgPO; }
    public void setImgPO(String imgPO) { this.imgPO = imgPO; }

    public String getCreateby() {
        return createby;
    }
    public void setCreateby(String createby) {
        this.createby = createby;
    }

    public String getKodeTax() {
        return kodeTax;
    }
    public void setKodeTax(String kodeTax) {
        this.kodeTax = kodeTax;
    }

    public String getStatusorder() {
        return statusorder;
    }
    public void setStatusorder(String statusorder) {
        this.statusorder = statusorder;
    }

    public String getNobukti() {
        return nobukti;
    }
    public void setNobukti(String nobukti) {
        this.nobukti = nobukti;
    }

    public String getTgl_create() {
        return tgl_create;
    }
    public void setTgl_create(String tgl_create) {
        this.tgl_create = tgl_create;
    }

    public String getTgl_kirim() {
        return tgl_kirim;
    }
    public void setTgl_kirim(String tgl_kirim) {
        this.tgl_kirim = tgl_kirim;
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

    public String getKdkel() {
        return kdkel;
    }
    public void setKdkel(String kdkel) {
        this.kdkel = kdkel;
    }

    public String getAlm1() {
        return alm1;
    }
    public void setAlm1(String alm1) {
        this.alm1 = alm1;
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

    public String getNoPO() {
        return noPO;
    }
    public void setNoPO(String noPO) {
        this.noPO = noPO;
    }

    public String getKet() { return ket; }
    public void setKet(String ket) { this.ket = ket; }

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

    public String getOrderby() {
        return orderby;
    }
    public void setOrderby(String orderby) {
        this.orderby = orderby;
    }

    public String getJnsjualtax() {
        return jnsjualtax;
    }
    public void setJnsjualtax(String jnsjualtax) {
        this.jnsjualtax = jnsjualtax;
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

    public double getSubtotal() {
        return subtotal;
    }
    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }

    public double getPrsdisc1() {
        return prsdisc1;
    }
    public void setPrsdisc1(double prsdisc1) {
        this.prsdisc1 = prsdisc1;
    }

    public double getJmldisc1() {
        return jmldisc1;
    }
    public void setJmldisc1(double jmldisc1) {
        this.jmldisc1 = jmldisc1;
    }

    public double getPrsppn() {
        return prsppn;
    }
    public void setPrsppn(double prsppn) {
        this.prsppn = prsppn;
    }

    public double getPpn() {
        return ppn;
    }
    public void setPpn(double ppn) {
        this.ppn = ppn;
    }

    public double getTotal() {
        return total;
    }
    public void setTotal(double total) {
        this.total = total;
    }

    public double getQty() {
        return qty;
    }
    public void setQty(double qty) {
        this.qty = qty;
    }

    public double getQtyorder() {
        return qtyorder;
    }
    public void setQtyorder(double qtyorder) {
        this.qtyorder = qtyorder;
    }

    public double getQtykirim() {
        return qtykirim;
    }
    public void setQtykirim(double qtykirim) {
        this.qtykirim = qtykirim;
    }

    public double getQtykvs() {
        return qtykvs;
    }
    public void setQtykvs(double qtykvs) {
        this.qtykvs = qtykvs;
    }

    public double getQtykvs3() {
        return qtykvs3;
    }
    public void setQtykvs3(double qtykvs3) {
        this.qtykvs3 = qtykvs3;
    }

    public double getHrg() {
        return hrg;
    }
    public void setHrg(double hrg) {
        this.hrg = hrg;
    }

    public double getPrsdisc() {
        return prsdisc;
    }
    public void setPrsdisc(double prsdisc) {
        this.prsdisc = prsdisc;
    }

    public double getPrsdisc2() {
        return prsdisc2;
    }
    public void setPrsdisc2(double prsdisc2) {
        this.prsdisc2 = prsdisc2;
    }

    public double getPrsdisc3() {
        return prsdisc3;
    }
    public void setPrsdisc3(double prsdisc3) {
        this.prsdisc3 = prsdisc3;
    }

    public double getDisc() {
        return disc;
    }
    public void setDisc(double disc) {
        this.disc = disc;
    }

    public double getHrgnet() {
        return hrgnet;
    }
    public void setHrgnet(double hrgnet) {
        this.hrgnet = hrgnet;
    }

    public double getJumlah() {
        return jumlah;
    }
    public void setJumlah(double jumlah) {
        this.jumlah = jumlah;
    }

    public double getM3() {
        return m3;
    }
    public void setM3(double m3) {
        this.m3 = m3;
    }

    public double getJumlahm3() {
        return jumlahm3;
    }
    public void setJumlahm3(double jumlahm3) {
        this.jumlahm3 = jumlahm3;
    }
}

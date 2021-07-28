package com.yusuffahrudin.masuyamobileapp.data.sales_order

import com.google.gson.annotations.SerializedName

data class SalesOrder (
        @SerializedName("NoBukti")
        var nobukti: String? = null,
        @SerializedName("TglOrder")
        var tglcreate: String? = null,
        @SerializedName("TglKirim")
        var tglkirim: String? = null,
        @SerializedName("KdGd")
        var kdgd: String? = null,
        @SerializedName("KdCust")
        var kdcust: String? = null,
        @SerializedName("NmCust")
        var nmcust: String? = null,
        @SerializedName("KdKel")
        var kdkel: String? = null,
        @SerializedName("Alm1")
        var alm1: String? = null,
        @SerializedName("Alm2")
        var alm2: String? = null,
        @SerializedName("Alm3")
        var alm3: String? = null,
        @SerializedName("KdSales")
        var kdsales: String? = null,
        @SerializedName("NmSales")
        var nmsales: String? = null,
        @SerializedName("FileName")
        var fileName: String? = null,
        @SerializedName("Ket")
        var ket: String? = null,
        @SerializedName("Ket1")
        var ket1: String? = null,
        @SerializedName("Ket2")
        var ket2: String? = null,
        @SerializedName("OrderBy")
        var orderby: String? = null,
        @SerializedName("CreateBy")
        var createby: String? = null,
        @SerializedName("JnsJualTax")
        var jnsjualtax: String? = null,
        @SerializedName("KdBrg")
        var kdbrg: String? = null,
        @SerializedName("NmBrg")
        var nmbrg: String? = null,
        @SerializedName("Satuan")
        var satuan: String? = null,
        @SerializedName("StatusOrder")
        var statusorder: String? = null,
        @SerializedName("KodeTax")
        var kodeTax: String? = null,
        @SerializedName("NoPO")
        var noPO: String? = null,
        @SerializedName("ImgPO")
        var imgPO: String? = null,
        @SerializedName("ImagePO")
        var filePO: String? = null,
        @SerializedName("TglPO")
        var tglPO: String? = null,
        @SerializedName("Status")
        var status: String? = null,
        @SerializedName("OtoODAR")
        var otoODAR: String? = null,
        @SerializedName("OtoODSL")
        var otoODSL: String? = null,
        @SerializedName("OtoODARBy")
        var otoODARby: String? = null,
        @SerializedName("OtoODSLBy")
        var otoODSLby: String? = null,
        @SerializedName("AlasanODAR")
        var alasanODAR: String? = null,
        @SerializedName("AlasanODSL")
        var alasanODSL: String? = null,
        @SerializedName("TglAlasanODAR")
        var tglAlasanODAR: String? = null,
        @SerializedName("KetODAR")
        var ketODAR: String? = null,
        @SerializedName("KetODSL")
        var ketODSL: String? = null,
        @SerializedName("ARSL")
        var ARSL: String? = null,
        @SerializedName("KdLevel")
        var kdLevel: String? = null,
        @SerializedName("OtoUCBy")
        var otoUCby: String? = null,
        @SerializedName("OtoUBBy")
        var otoUBby: String? = null,
        @SerializedName("OtoUC")
        var otoUC: String? = null,
        @SerializedName("OtoUB")
        var otoUB: String? = null,
        @SerializedName("LamaKredit")
        var lamaKredit: String? = null,

        @SerializedName("SubTotal")
        var subtotal: Double? = null,
        @SerializedName("JmlDisc1")
        var jmldisc1: Double? = null,
        @SerializedName("PrsPpn")
        var prsppn: Double? = null,
        @SerializedName("Ppn")
        var ppn: Double? = null,
        @SerializedName("Total")
        var total: Double? = null,
        @SerializedName("Qty")
        var qty: Double? = null,
        @SerializedName("QtyOrder")
        var qtyorder: Double? = null,
        @SerializedName("QtyKirim")
        var qtykirim: Double? = null,
        @SerializedName("Hrg")
        var hrg: Double? = null,
        @SerializedName("Disc")
        var disc: Double? = null,
        @SerializedName("HrgNet")
        var hrgnet: Double? = null,
        @SerializedName("Jumlah")
        var jumlah: Double? = null,
        @SerializedName("M3")
        var m3: Double? = null,

        @SerializedName("NeedOtoUC")
        var needOtoUC: Boolean = false,
        @SerializedName("NeedOtoUB")
        var needOtoUB: Boolean = false,
        @SerializedName("Lunas")
        var lunas: Boolean = false,
        var ischecked: Boolean? = false
)
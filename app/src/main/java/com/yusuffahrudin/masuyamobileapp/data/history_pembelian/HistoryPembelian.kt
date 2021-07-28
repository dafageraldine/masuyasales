package com.yusuffahrudin.masuyamobileapp.data.history_pembelian

import com.google.gson.annotations.SerializedName

data class HistoryPembelian (
        @SerializedName("NoBukti")
        var nobukti: String? = null,
        @SerializedName("NmSup")
        var nmsup: String? = null,
        @SerializedName("Tgl")
        var tgl: String? = null,
        @SerializedName("TglExpired")
        var tglExpired: String? = null,
        @SerializedName("KdGd")
        var kdgd: String? = null,
        @SerializedName("KdBrg")
        var kdbrg: String? = null,
        @SerializedName("NmBrg")
        var nmbrg: String? = null,
        @SerializedName("Satuan")
        var satuan: String? = null,

        @SerializedName("Qty")
        var qty: Double? = null,
        @SerializedName("Hrg")
        var hrg: Double? = null,
        @SerializedName("HrgNet")
        var hrgNet: Double? = null,
        @SerializedName("PrsDisc")
        var disc1: Double? = null,
        @SerializedName("PrsDisc2")
        var disc2: Double? = null,
        @SerializedName("PrsDisc3")
        var disc3: Double? = null,
        @SerializedName("Jumlah")
        var jumlah: Double? = null,
        @SerializedName("Total")
        var total: Double? = null
)
package com.yusuffahrudin.masuyamobileapp.data.history_penjualan

import com.google.gson.annotations.SerializedName

data class HistoryPenjualan (
        @SerializedName("NoBukti")
        var nobukti: String? = null,
        @SerializedName("NoPO")
        var nopo: String? = null,
        @SerializedName("Tgl")
        var tgl: String? = null,
        @SerializedName("Cetak")
        var cetak: String? = null,
        @SerializedName("Penyiapan")
        var penyiapan: String? = null,
        @SerializedName("NmCust")
        var nmcust: String? = null,
        @SerializedName("Kirim")
        var kirim: String? = null,
        @SerializedName("Status")
        var status: String? = null,
        @SerializedName("Diterima")
        var diterima: String? = null,
        @SerializedName("Lunas")
        var lunas: String? = null,
        @SerializedName("Kembali")
        var kembali: String? = null,
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
        @SerializedName("Total")
        var total: Double? = null,
        @SerializedName("PrsDisc")
        var diskon1: Double? = null,
        @SerializedName("PrsDisc2")
        var diskon2: Double? = null,
        @SerializedName("PrsDisc3")
        var diskon3: Double? = null,
        @SerializedName("PrsDisc1")
        var discfak: Double? = null
)
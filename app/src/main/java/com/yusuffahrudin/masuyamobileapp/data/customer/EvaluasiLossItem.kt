package com.yusuffahrudin.masuyamobileapp.data.customer

import com.google.gson.annotations.SerializedName

data class EvaluasiLossItem (
        @SerializedName("KdBrg")
        var kdBrg: String? = null,

        @SerializedName("NmBrg")
        var nmBrg: String? = null,

        @SerializedName("NmType")
        var nmTipe: String? = null,

        @SerializedName("NmSales")
        var nmSales: String? = null,

        @SerializedName("BlnTerakhir")
        var blnTerakhir: Int? = null,

        @SerializedName("QtyAvg")
        var qtyAvg: Double? = null,

        @SerializedName("Satuan")
        var satuan: String? = null,

        @SerializedName("Stok")
        var stok: Double? = null
)
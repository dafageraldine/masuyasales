package com.yusuffahrudin.masuyamobileapp.data.customer

import com.google.gson.annotations.SerializedName

data class HutangCust (
        @SerializedName("NoBukti")
        var noBukti: String? = null,
        @SerializedName("Tgl")
        var tgl: String? = null,
        @SerializedName("KdBrg")
        var kdBrg: String? = null,
        @SerializedName("NmBrg")
        var nmBrg: String? = null,
        @SerializedName("Satuan")
        var satuan: String? = null,

        @SerializedName("Total")
        var total: Double? = null,
        @SerializedName("Bayar")
        var bayar: Double? = null,
        @SerializedName("Qty")
        var qty: Double? = null
)
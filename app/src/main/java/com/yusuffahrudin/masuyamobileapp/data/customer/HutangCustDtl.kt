package com.yusuffahrudin.masuyamobileapp.data.customer

import com.google.gson.annotations.SerializedName

data class HutangCustDtl (
        @SerializedName("NoBukti")
        var noBukti: String? = null,
        @SerializedName("KdBrg")
        var kdBrg: String? = null,
        @SerializedName("NmBrg")
        var nmBrg: String? = null,
        @SerializedName("Satuan")
        var satuan: String? = null,

        @SerializedName("Qty")
        var qty: Double? = null
)
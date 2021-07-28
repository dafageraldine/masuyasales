package com.yusuffahrudin.masuyamobileapp.data.customer

import com.google.gson.annotations.SerializedName

data class EvaluasiCustDetail (
        @SerializedName("KdBrg")
        var kdBrg: String? = null,

        @SerializedName("NmBrg")
        var nmBrg: String? = null,

        @SerializedName("Qty")
        var qty: Double? = null,

        @SerializedName("Jumlah")
        var jumlah: Double? = null
)
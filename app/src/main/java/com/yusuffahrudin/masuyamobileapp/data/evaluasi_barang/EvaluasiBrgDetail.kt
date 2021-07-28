package com.yusuffahrudin.masuyamobileapp.data.evaluasi_barang

import com.google.gson.annotations.SerializedName

data class EvaluasiBrgDetail (
        @SerializedName("KdCust")
        var kdCust: String? = null,

        @SerializedName("NmCust")
        var nmCust: String? = null,

        @SerializedName("Qty")
        var qty: Double? = null,

        @SerializedName("Jumlah")
        var jumlah: Double? = null
)
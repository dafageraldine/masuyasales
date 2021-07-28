package com.yusuffahrudin.masuyamobileapp.data.evaluasi_barang

import com.google.gson.annotations.SerializedName

data class EvaluasiBrgLineChart (
        @SerializedName("KdBrg")
        var kdBrg: String? = null,

        @SerializedName("NmBrg")
        var nmBrg: String? = null,

        @SerializedName("Bulan")
        var bulan: String? = null,

        @SerializedName("QtyThn1")
        var qtyThn1: Double? = null,

        @SerializedName("QtyThn2")
        var qtyThn2: Double? = null,

        @SerializedName("QtyThn3")
        var qtyThn3: Double? = null
)
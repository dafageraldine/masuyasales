package com.yusuffahrudin.masuyamobileapp.data.evaluasi_barang

import com.google.gson.annotations.SerializedName

data class EvaluasiBarang (
        @SerializedName("KdBrg")
        var kdBrg: String? = null,

        @SerializedName("NmBrg")
        var nmBrg: String? = null,

        @SerializedName("Bulan")
        var bulan: String? = null,

        @SerializedName("Tahun")
        var tahun: String? = null,

        @SerializedName("Qty")
        var qty: Double? = null
)
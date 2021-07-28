package com.yusuffahrudin.masuyamobileapp.data.informasi_barang

import com.google.gson.annotations.SerializedName

data class Stok(
        @field:SerializedName("KdGd")
        var kdgd: String? = null,
        @field:SerializedName("NmGd")
        var nmgd: String? = null,
        @field:SerializedName("KdBrg")
        var kdbrg: String? = null,
        @field:SerializedName("NmBrg")
        var nmbrg: String? = null,
        @field:SerializedName("Qty")
        var qty: Double? = 0.0
)
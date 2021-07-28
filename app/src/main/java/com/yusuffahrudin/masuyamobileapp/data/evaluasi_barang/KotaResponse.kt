package com.yusuffahrudin.masuyamobileapp.data.evaluasi_barang

import com.google.gson.annotations.SerializedName

data class KotaResponse (
    @SerializedName("result")
    val result: MutableList<Kota>? = null
)
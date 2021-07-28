package com.yusuffahrudin.masuyamobileapp.data.informasi_barang

import com.google.gson.annotations.SerializedName

data class StokResponse (
    @SerializedName("result")
    val result: ArrayList<Stok>? = null
)
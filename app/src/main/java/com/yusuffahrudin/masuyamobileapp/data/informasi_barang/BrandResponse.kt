package com.yusuffahrudin.masuyamobileapp.data.informasi_barang

import com.google.gson.annotations.SerializedName

data class BrandResponse (
    @SerializedName("result")
    val result: MutableList<Brand>? = null
)
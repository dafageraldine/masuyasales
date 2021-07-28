package com.yusuffahrudin.masuyamobileapp.data.informasi_barang

import com.google.gson.annotations.SerializedName

data class ProductResponse (
    @SerializedName("result")
    val result: ArrayList<Product>? = null
)
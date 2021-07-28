package com.yusuffahrudin.masuyamobileapp.data.informasi_barang

import com.google.gson.annotations.SerializedName

data class Brand (
        @field:SerializedName("KdMerk")
        val kdmerk: String? = null,
        @field:SerializedName("NmMerk")
        val nmmerk: String? = null
)
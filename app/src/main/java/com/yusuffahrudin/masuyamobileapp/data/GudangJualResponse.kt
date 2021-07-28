package com.yusuffahrudin.masuyamobileapp.data

import com.google.gson.annotations.SerializedName

data class GudangJualResponse (
    @SerializedName("result")
    val result: MutableList<GudangJual>? = null
)
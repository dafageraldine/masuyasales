package com.yusuffahrudin.masuyamobileapp.data.sales_order

import com.google.gson.annotations.SerializedName

data class NoteOtorisasiResponse (
    @SerializedName("result")
    val result: MutableList<NoteOtorisasi>? = null
)
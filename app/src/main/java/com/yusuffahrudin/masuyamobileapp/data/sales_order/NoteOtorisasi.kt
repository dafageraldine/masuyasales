package com.yusuffahrudin.masuyamobileapp.data.sales_order

import com.google.gson.annotations.SerializedName

data class NoteOtorisasi (
        @field:SerializedName("Note")
        val note: String? = null,
        @field:SerializedName("AR")
        val ar: Boolean? = null,
        @field:SerializedName("ARSL")
        val arsl: Boolean? = null,
        @field:SerializedName("SL")
        val sl: Boolean? = null
)
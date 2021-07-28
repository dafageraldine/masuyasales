package com.yusuffahrudin.masuyamobileapp.data.customer

import com.google.gson.annotations.SerializedName

data class EvaluasiCustHistory (
        @SerializedName("NoBukti")
        var noBukti: String? = null,

        @SerializedName("Tgl")
        var tgl: String? = null,

        @SerializedName("Qty")
        var qty: Double? = null,

        @SerializedName("Satuan")
        var satuan: String? = null
)
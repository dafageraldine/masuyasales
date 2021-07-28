package com.yusuffahrudin.masuyamobileapp.data.customer

import com.google.gson.annotations.SerializedName

data class EvaluasiCustLineChart (
        @SerializedName("KdCust")
        var kdCust: String? = null,

        @SerializedName("NmCust")
        var nmCust: String? = null,

        @SerializedName("Bulan")
        var bulan: String? = null,

        @SerializedName("JumlahThn1")
        var jumlahThn1: Double? = null,

        @SerializedName("JumlahThn2")
        var jumlahThn2: Double? = null,

        @SerializedName("JumlahThn3")
        var jumlahThn3: Double? = null
)
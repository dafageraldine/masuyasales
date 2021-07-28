package com.yusuffahrudin.masuyamobileapp.data.customer

import com.google.gson.annotations.SerializedName

data class EvaluasiCustomer (
        @SerializedName("KdCust")
        var kdCust: String? = null,

        @SerializedName("NmCust")
        var nmCust: String? = null,

        @SerializedName("Bulan")
        var bulan: String? = null,

        @SerializedName("Tahun")
        var tahun: String? = null,

        @SerializedName("Jumlah")
        var jumlah: Double? = null
)
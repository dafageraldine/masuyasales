package com.yusuffahrudin.masuyamobileapp.data.evaluasi_barang

import com.google.gson.annotations.SerializedName

data class EvaluasiLossCust (
        @SerializedName("KdCust")
        var kdCust: String? = null,

        @SerializedName("NmCust")
        var nmCust: String? = null,

        @SerializedName("TypeCust")
        var tipeCust: String? = null,

        @SerializedName("NmSales")
        var nmSales: String? = null,

        @SerializedName("BlnTerakhir")
        var blnTerakhir: Int? = null,

        @SerializedName("NilFakturAvg")
        var nilFakturAvg: Double? = null
)
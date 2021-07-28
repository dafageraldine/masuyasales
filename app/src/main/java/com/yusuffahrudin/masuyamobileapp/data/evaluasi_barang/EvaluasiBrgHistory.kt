package com.yusuffahrudin.masuyamobileapp.data.evaluasi_barang

import com.google.gson.annotations.SerializedName

data class EvaluasiBrgHistory (
        @SerializedName("NoBukti")
        var noBukti: String? = null,

        @SerializedName("Tgl")
        var tgl: String? = null,

        @SerializedName("NilFaktur")
        var nilFaktur: Double? = null
)
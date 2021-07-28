package com.yusuffahrudin.masuyamobileapp.data.informasi_barang

import com.google.gson.annotations.SerializedName

data class KartuStok (
        @SerializedName("NoBukti")
        var noBukti: String? = null,

        @SerializedName("NoFaktur")
        var noFaktur: String? = null,

        @SerializedName("Tgl")
        var tgl: String? = null,

        @SerializedName("Keterangan")
        var ket: String? = null,

        @SerializedName("Kirim")
        var kirim: String? = null,

        @SerializedName("KdBrg")
        var kdbrg: String? = null,

        @SerializedName("NmBrg")
        var nmbrg: String? = null,

        @SerializedName("Qty")
        var qty: Double? = null,

        @SerializedName("Satuan")
        var satuan: String? = null,

        @SerializedName("Masuk")
        var masuk: Double? = null,

        @SerializedName("Keluar")
        var keluar: Double? = null
)
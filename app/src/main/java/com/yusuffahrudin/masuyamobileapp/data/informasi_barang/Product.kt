package com.yusuffahrudin.masuyamobileapp.data.informasi_barang

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Product(
        @SerializedName("KdBrg")
        var kdbrg: String? = null,
        @SerializedName("NmBrg")
        var nmbrg: String? = null,
        @SerializedName("Tgl")
        var tgl: String? = null,
        @SerializedName("Satuan")
        var satuan: String? = null,
        @SerializedName("JnsPPN")
        var jnsPpn: String? = null,
        @SerializedName("StartDate")
        var startDate: String? = null,
        @SerializedName("EndDate")
        var endDate: String? = null,
        @SerializedName("CreateBy")
        var createBy: String? = null,
        @SerializedName("CreateTime")
        var createTime: String? = null,
        @SerializedName("Tipe")
        var tipePrice: String? = null,

        @SerializedName("QtyKuota")
        var qtyKuota: Double? = null,
        @SerializedName("Hrg")
        var hrg: Double? = null,
        @SerializedName("HrgIncPpn")
        var hrgIncPpn: Double? = null,
        @SerializedName("HrgPokok")
        var hrgPokok: Double? = null,
        @SerializedName("HrgJualMin")
        var hrgJualMin: Double? = null,
        @SerializedName("PrsDisc")
        var diskon1: Double? = null,
        @SerializedName("PrsDisc2")
        var diskon2: Double? = null,
        @SerializedName("PrsDisc3")
        var diskon3: Double? = null
) : Parcelable

package com.yusuffahrudin.masuyamobileapp.data.customer

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Customer (
        @SerializedName("KdCust")
        var kdcust: String? = null,
        @SerializedName("NmCust")
        var nmcust: String? = null,
        @SerializedName("TypeCust")
        var typecust: String? = null,
        @SerializedName("KdKel")
        var kdkel: String? = null,
        @SerializedName("Alm1")
        var alm1: String? = null,
        @SerializedName("Alm2")
        var alm2: String? = null,
        @SerializedName("Alm3")
        var alm3: String? = null,
        @SerializedName("Kota")
        var kota: String? = null,
        @SerializedName("Telp1")
        var telp1: String? = null,
        @SerializedName("Telp2")
        var telp2: String? = null,
        @SerializedName("Koordinat")
        var koordinat: String? = null,
        @SerializedName("KdSales")
        var kdsales: String? = null,
        @SerializedName("NmSales")
        var sales: String? = null,
        @SerializedName("LamaKredit")
        var top: String? = null,

        @SerializedName("Saldo")
        var saldo: Double? = null,
        @SerializedName("KreditLimit")
        var kreditLimit: Double? = null,

        @SerializedName("CheckList")
        var checklist: Boolean? = false
) : Parcelable
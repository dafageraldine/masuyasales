package com.yusuffahrudin.masuyamobileapp.data.sales_order

import com.google.gson.annotations.SerializedName

data class ItemOrder(
		@field:SerializedName("KdBrg")
		var kdBrg: String? = null,

		@field:SerializedName("NmBrg")
		var nmBrg: String? = null,

		@field:SerializedName("QtyOrder")
		var qtyOrder: Double? = null,

		@field:SerializedName("QtyOut")
		var qtyOut: Double? = null,

		@field:SerializedName("QtyKvs3")
		var qtyKvs3: Double? = null,

		@field:SerializedName("HrgNet")
		var harga: Double? = null,

		@field:SerializedName("HrgPokok")
		var hrgPokok: Double? = null,

		@field:SerializedName("HrgJualMin")
		var hrgJualMin: Double? = null,

		@field:SerializedName("PrsDisc")
		var diskon1: Double? = null,

		@field:SerializedName("PrsDisc2")
		var diskon2: Double? = null,

		@field:SerializedName("PrsDisc3")
		var diskon3: Double? = null,

		@field:SerializedName("Satuan")
		var satuan: String? = null,

		@field:SerializedName("Satuan3")
		var satuan3: String? = null,

		@field:SerializedName("MKubik1")
		var mKubik1: Double? = null,

		@field:SerializedName("Jumlah")
		var subTotal: Double? = null,

		@field:SerializedName("JnsJualTax")
		var jnsJualTax: String? = null,

		@field:SerializedName("KdGd")
		var kdGd: String? = null,

		@SerializedName("NeedOtoUC")
		var needOtoUC: Boolean = false,
		@SerializedName("NeedOtoUB")
		var needOtoUB: Boolean = false,
		@SerializedName("OtoUCBy")
		var otoUCby: String? = null,
		@SerializedName("OtoUBBy")
		var otoUBby: String? = null,
		@SerializedName("OtoUC")
		var otoUC: String? = null,
		@SerializedName("OtoUB")
		var otoUB: String? = null,
		@SerializedName("KdLevel")
		var kdLevel: String? = null
)
package com.yusuffahrudin.masuyamobileapp.data.sales_order

import com.google.gson.annotations.SerializedName

data class ItemJual(
		@field:SerializedName("KdBrg")
		var kdBrg: String? = null,

		@field:SerializedName("NmBrg")
		var nmBrg: String? = null,

		@field:SerializedName("Qty")
		var qty: Double? = null,

		@field:SerializedName("QtyOut")
		var qtyOut: Double? = null,

		@field:SerializedName("QtyKvs3")
		var qtyKvs3: Double? = null,

		@field:SerializedName("Harga")
		var harga: Double? = null,

		@field:SerializedName("HrgPokok")
		var hrgPokok: Double? = null,

		@field:SerializedName("HrgJualMin")
		var hrgJualMin: Double? = null,

		@field:SerializedName("Diskon1")
		var diskon1: Double? = null,

		@field:SerializedName("Diskon2")
		var diskon2: Double? = null,

		@field:SerializedName("Diskon3")
		var diskon3: Double? = null,

		@field:SerializedName("Satuan")
		var satuan: String? = null,

		@field:SerializedName("Satuan3")
		var satuan3: String? = null,

		@field:SerializedName("MKubik1")
		var mKubik1: Double? = null
)
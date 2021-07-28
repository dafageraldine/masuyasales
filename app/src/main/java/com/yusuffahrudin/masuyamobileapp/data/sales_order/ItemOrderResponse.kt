package com.yusuffahrudin.masuyamobileapp.data.sales_order

import com.google.gson.annotations.SerializedName

data class ItemOrderResponse(

		@field:SerializedName("result")
		val result: List<ItemOrder?>? = null,

		@field:SerializedName("success")
		val success: Int? = null
)
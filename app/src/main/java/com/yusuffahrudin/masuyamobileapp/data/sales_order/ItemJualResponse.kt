package com.yusuffahrudin.masuyamobileapp.data.sales_order

import com.google.gson.annotations.SerializedName

data class ItemJualResponse(

        @field:SerializedName("result")
		val result: List<ItemJual?>? = null,

        @field:SerializedName("success")
		val success: Int? = null
)
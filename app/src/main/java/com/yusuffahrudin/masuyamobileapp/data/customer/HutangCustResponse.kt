package com.yusuffahrudin.masuyamobileapp.data.customer

import com.google.gson.annotations.SerializedName

data class HutangCustResponse(
	@SerializedName("result")
	val result: ArrayList<HutangCust>? = null
)
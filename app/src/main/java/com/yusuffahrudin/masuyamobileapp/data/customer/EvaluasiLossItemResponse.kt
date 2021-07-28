package com.yusuffahrudin.masuyamobileapp.data.customer

import com.google.gson.annotations.SerializedName

data class EvaluasiLossItemResponse(
	@SerializedName("result")
	val result: ArrayList<EvaluasiLossItem>? = null
)
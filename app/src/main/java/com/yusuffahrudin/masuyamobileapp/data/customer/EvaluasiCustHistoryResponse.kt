package com.yusuffahrudin.masuyamobileapp.data.customer

import com.google.gson.annotations.SerializedName

data class EvaluasiCustHistoryResponse(
	@SerializedName("result")
	val result: ArrayList<EvaluasiCustHistory>? = null
)
package com.yusuffahrudin.masuyamobileapp.data.customer

import com.google.gson.annotations.SerializedName

data class EvaluasiCustDetailResponse(
	@SerializedName("result")
	val result: ArrayList<EvaluasiCustDetail>? = null
)
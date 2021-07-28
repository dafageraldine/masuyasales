package com.yusuffahrudin.masuyamobileapp.data.customer

import com.google.gson.annotations.SerializedName

data class EvaluasiCustLineChartResponse(
	@SerializedName("result")
	val result: ArrayList<EvaluasiCustLineChart>? = null
)
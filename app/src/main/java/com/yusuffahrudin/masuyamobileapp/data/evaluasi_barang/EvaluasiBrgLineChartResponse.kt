package com.yusuffahrudin.masuyamobileapp.data.evaluasi_barang

import com.google.gson.annotations.SerializedName

data class EvaluasiBrgLineChartResponse(
	@SerializedName("result")
	val result: ArrayList<EvaluasiBrgLineChart>? = null
)
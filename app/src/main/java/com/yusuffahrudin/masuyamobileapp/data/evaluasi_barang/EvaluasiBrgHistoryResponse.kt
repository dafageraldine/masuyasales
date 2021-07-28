package com.yusuffahrudin.masuyamobileapp.data.evaluasi_barang

import com.google.gson.annotations.SerializedName

data class EvaluasiBrgHistoryResponse(
	@SerializedName("result")
	val result: ArrayList<EvaluasiBrgHistory>? = null
)
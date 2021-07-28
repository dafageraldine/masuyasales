package com.yusuffahrudin.masuyamobileapp.data.evaluasi_barang

import com.google.gson.annotations.SerializedName

data class EvaluasiBrgDetailResponse(
	@SerializedName("result")
	val result: ArrayList<EvaluasiBrgDetail>? = null
)
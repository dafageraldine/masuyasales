package com.yusuffahrudin.masuyamobileapp.data.evaluasi_barang

import com.google.gson.annotations.SerializedName

data class EvaluasiLossCustResponse(
	@SerializedName("result")
	val result: ArrayList<EvaluasiLossCust>? = null
)
package com.yusuffahrudin.masuyamobileapp.data.evaluasi_barang

import com.google.gson.annotations.SerializedName

data class EvaluasiBarangResponse(
	@SerializedName("result")
	val result: ArrayList<EvaluasiBarang>? = null
)
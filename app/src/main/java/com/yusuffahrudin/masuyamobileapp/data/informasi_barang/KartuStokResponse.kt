package com.yusuffahrudin.masuyamobileapp.data.informasi_barang

import com.google.gson.annotations.SerializedName

data class KartuStokResponse(
	@SerializedName("result")
	val result: ArrayList<KartuStok>? = null
)
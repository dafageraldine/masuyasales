package com.yusuffahrudin.masuyamobileapp.data.customer

import com.google.gson.annotations.SerializedName

data class EvaluasiCustomerResponse(
	@SerializedName("result")
	val result: ArrayList<EvaluasiCustomer>? = null
)
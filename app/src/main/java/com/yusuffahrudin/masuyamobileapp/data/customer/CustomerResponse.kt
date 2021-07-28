package com.yusuffahrudin.masuyamobileapp.data.customer

import com.google.gson.annotations.SerializedName

data class CustomerResponse (
        @SerializedName("result")
        val result: ArrayList<Customer>? = null
)